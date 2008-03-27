/*
 *  AcmusMeasurementSetWizard.java
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
/*
 * Created on 12/01/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package acmus.wizard;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import acmus.MeasurementProject;

/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AcmusMeasurementSetWizard extends Wizard implements INewWizard {

  private IStructuredSelection selection;

  private IWorkbench workbench;

  private AcmusMeasurementSetWizardFirstPage mainPage;

  private Set<String> fPosSet = new TreeSet<String>(); // FIXME

  public void addPages() {
    IFolder session = (IFolder) selection.getFirstElement();
    IProject project = session.getProject();
    IFile posFile = (IFile) project.getFile("project.positions");
    fPosSet.clear();
    String posArray[] = getPositions(posFile, fPosSet);

    mainPage = new AcmusMeasurementSetWizardFirstPage(
        "AcmusMeasurementSetWizardFirstPage", project.getName(),
        MeasurementProject.removeSuffix(session.getName()), posFile.getLocation().toOSString());
    addPage(mainPage);
  }

  public static String[] getPositions(IFile posFile, Set<String> outSet) {
    String res[] = new String[0];
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(posFile
          .getContents(true)));
      String line = br.readLine();

      while (line != null && !line.matches("^\\s*\\[positions\\]\\s*")) {
        line = br.readLine();
      }
      line = br.readLine();
      while (line != null && !line.matches("^\\s*\\[.*\\]\\s*")) {
        if (!line.trim().equals("")) {
          outSet.add(line);
        }
        line = br.readLine();
      }
      res = new String[outSet.size()];
      res = (String[]) outSet.toArray(res);
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return res;
  }

  public void init(IWorkbench workbench, IStructuredSelection selection) {
    System.out.println("init");
    this.workbench = workbench;
    this.selection = selection;
    setWindowTitle("New Measurement Set"); //$NON-NLS-1$
  }

  public boolean performFinish() {

    IFolder session = (IFolder) selection.getFirstElement();
    Properties props = mainPage.getSetProperties();

    try {
      IFolder folder = session.getFolder(props.getProperty("Name") + ".set");
      folder.create(true, true, null);

      // Enumeration en = props.keys();
      // while (en.hasMoreElements()) {
      // String key = (String) en.nextElement();
      // folder.setPersistentProperty(new QualifiedName("acmus", key), props
      // .getProperty(key));
      // }

      IFile propsFile = folder.getFile("set.properties");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      props.store(baos, props.getProperty("Name") + " Set folder properties");
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      propsFile.create(bais, true, null);

      // IFile propsFile = folder.getFile("measurement.properties");
      //
      // ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //
      // props.store(baos, "AcMus Measurement Set");
      //
      // propsFile
      // .create(new ByteArrayInputStream(baos.toByteArray()), true, null);

      // IFile posFile = session.getFile("session.positions");
      // ByteArrayOutputStream baos = new ByteArrayOutputStream();
      // PrintStream ps = new PrintStream(baos);
      // String selPos = (String) props.get("Source");
      // if (!fPosSet.contains(selPos))
      // ps.println(selPos);
      // selPos = (String) props.get("Mic");
      // if (!fPosSet.contains(selPos))
      // ps.println(selPos);
      // ps.close();
      // posFile.appendContents(new ByteArrayInputStream(baos.toByteArray()),
      // true, false, null);

    } catch (CoreException e) {
      e.printStackTrace();

    } catch (Exception e) {
      e.printStackTrace();
    }

    return true;
  }
}