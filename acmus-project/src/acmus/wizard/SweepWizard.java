/*
 *  SweepWizard.java
 *  This file is part of AcMus.
 *  
 *  AcMus: Tools for Measurement, Analysis, and Simulation of Room Acoustics
 *  
 *  Copyright (C) 2006 Leo Ueda, Bruno Masiero
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
/**
 * SweepWizard.java
 * Created on 21/10/2005
 */
package acmus.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import acmus.dsp.Filter;
import acmus.dsp.FilterBank;
import acmus.dsp.Signal;
import acmus.dsp.Util;

/**
 * @author lku
 *
 */
public class SweepWizard extends Wizard implements INewWizard {

  private IStructuredSelection selection;

  private IWorkbench workbench;

  private SweepWizardFirstPage mainPage;

  public void addPages() {
    IResource s = (IResource) selection.getFirstElement();
    if (!(s instanceof IFolder)) {
      addPage(new ErrorPage("AcmusInputSignalWizardFirstPage",
          "You can only create a signal inside a signals folder."));
    } else {
      IFolder f = (IFolder) s;
      System.out.println(f.getName());
      if (!f.getName().equals("_signals.signal")) {
        addPage(new ErrorPage("AcmusInputSignalWizardFirstPage",
            "You can only create a signal inside a signals folder."));
      } else {
        mainPage = new SweepWizardFirstPage(
            "SweepWizardFirstPage");
        addPage(mainPage);
      }
    }
  }

  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.workbench = workbench;
    this.selection = selection;
    setWindowTitle("New Sine Sweep Signal"); //$NON-NLS-1$
  }

  public boolean performFinish() {
    IFolder folder = (IFolder) selection.getFirstElement();
    IFolder audioFolder = folder.getFolder("audio");
    Properties props = mainPage.getSessionProperties();

    try {

        IFile audioFile = audioFolder.getFile(props.getProperty("Name") + ".wav");
        double startFreq = Double.parseDouble(props
            .getProperty("StartFrequency"));
        double endFreq = Double.parseDouble(props.getProperty("EndFrequency"));
        double duration = Double.parseDouble(props.getProperty("Duration"));

        double y[] = Signal.sweepLog(44100, duration, startFreq, endFreq,0.01, 0.01);
        for (int i = 0; i < y.length; i++) {
          y[i] = y[i] * 0.8;
        }
        Util.wavWrite(y, audioFile.getLocation().toOSString());
        
        Filter f = FilterBank.getSweepButter(startFreq, endFreq, 44100); // FIXME
        props.put("ButterB", f.bToString());
        props.put("ButterA", f.aToString());
        props.put("Type", "sweep");
        props.put("SampleRate", "44100");
        
        IFile file = folder.getFile(props.getProperty("Name") + ".signal");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store(baos, "");
        ByteArrayInputStream bais = new
        ByteArrayInputStream(baos.toByteArray());
        file.create(bais,true,null);

      folder.refreshLocal(2, null);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return true;
  }



}
