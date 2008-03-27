/*
 *  SweepWizardFirstPage.java
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
 * SweepWizardFirstPage.java
 * Created on 21/10/2005
 */
package acmus.wizard;

import java.util.Properties;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import acmus.AcmusGraphics;

/**
 * @author lku
 *
 */
public class SweepWizardFirstPage extends WizardPage {
  Text fName;

  Text _dur;
  Text _sFreq;
  Text _eFreq;
  Properties fProperties;

  private FormToolkit toolkit;
  private Form form;

  private Listener nameModifyListener = new Listener() {
    public void handleEvent(Event e) {
      setPageComplete(validatePage());
    }
  };

  private Listener paramModifyListener = new Listener() {
    public void handleEvent(Event e) {
      updateName();
    }
  };
  
  
  public SweepWizardFirstPage(String name) {
    super(name);
    setTitle("New Sine Sweep");
    setDescription("Choose generated logarithmic sine sweep parameters.");
    setPageComplete(false);
    fProperties = new Properties();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    FillLayout fl = new FillLayout();
    fl.marginHeight = 1;
    fl.marginWidth = 1;
    composite.setLayout(fl);
    composite.setBackground(AcmusGraphics.BLACK);

    GridLayout gridLayout;
    GridData gridData;
    Label l;

    toolkit = new FormToolkit(AcmusGraphics.FORMCOLORS);
    form = toolkit.createForm(composite);
    
    Composite body = form.getBody();
    
    gridData = new GridData(GridData.FILL_BOTH);
    body.setLayoutData(gridData);
    gridLayout = new GridLayout(3, false);
    gridLayout.verticalSpacing = 10;
    body.setLayout(gridLayout);
    toolkit.paintBordersFor(body);

    l = toolkit.createLabel(body, "Name:");
    fName = toolkit.createText(body, "");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 2;
    fName.setLayoutData(gridData);
    fName.addListener(SWT.Modify, nameModifyListener);
    fName.setEditable(false);
    fName.setText("LogSineSweep_6s_20-20000Hz");

    l = toolkit.createLabel(body, "Duration:");
    _dur = toolkit.createText(body, "6");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.widthHint = 50;
    _dur.setLayoutData(gridData);
    l = toolkit.createLabel(body, "s");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    l.setLayoutData(gridData);
    _dur.addListener(SWT.Modify, paramModifyListener);

    l = toolkit.createLabel(body, "Start frequency:");
    _sFreq = toolkit.createText(body, "20");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.widthHint = 100;
    _sFreq.setLayoutData(gridData);
    l = toolkit.createLabel(body,"Hz");
    _sFreq.addListener(SWT.Modify, paramModifyListener);
    _sFreq.setEnabled(false);

    l = toolkit.createLabel(body, "End frequency:");
    _eFreq = toolkit.createText(body, "20000");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.widthHint = 100;
    _eFreq.setLayoutData(gridData);
    l = toolkit.createLabel(body, "Hz");
    _eFreq.addListener(SWT.Modify, paramModifyListener);
    _eFreq.setEnabled(false);

    setControl(composite);
  }

  public boolean validatePage() {
    return true;
  }

  public void updateName() {
    fName.setText("LogSineSweep_" + _dur.getText() +"s_" +
        _sFreq.getText() + "-" + _eFreq.getText() + "Hz");
  }
  
  /**
   * Updates and returns the <code>Properties</code> object that contains the
   * given session properties.
   * 
   * @return the given session properties
   */
  public Properties getSessionProperties() {
    fProperties.clear();
    fProperties.setProperty("Name", fName.getText());
    fProperties.setProperty("Duration", _dur.getText());
    fProperties.setProperty("StartFrequency", _sFreq.getText());
    fProperties.setProperty("EndFrequency", _eFreq.getText());
    return fProperties;
  }

}
