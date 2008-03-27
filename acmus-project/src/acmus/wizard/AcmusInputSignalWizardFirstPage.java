/*
 *  AcmusInputSignalWizardFirstPage.java
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
 * AcmusInputSignalWizardFirstPage.java
 * Created on 25/04/2005
 */
package acmus.wizard;

import java.util.Properties;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * @author lku
 */
public class AcmusInputSignalWizardFirstPage extends WizardPage {
  Text fName;

  Text _dur;
  Text _sFreq;
  Text _eFreq;
  Properties fProperties;

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
  
  
  public AcmusInputSignalWizardFirstPage(String name) {
    super(name);
    setTitle("New Input Signal");
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
    composite.setLayout(new GridLayout(3, false));
    composite.setFont(parent.getFont());

    GridData gridData = new GridData(GridData.FILL_BOTH);
    composite.setLayoutData(gridData);

    Label l = new Label(composite, SWT.LEFT);
    l.setText("Name:");
    fName = new Text(composite, SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 2;
    fName.setLayoutData(gridData);
    fName.addListener(SWT.Modify, nameModifyListener);
    fName.setEditable(false);
    fName.setText("LogSineSweep_6s_20-20000Hz");

    l = new Label(composite, SWT.LEFT);
    l.setText("Duration:");
    _dur = new Text(composite, SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    _dur.setLayoutData(gridData);
    _dur.setText("6");
    l = new Label(composite, SWT.LEFT);
    l.setText("s");
    _dur.addListener(SWT.Modify, paramModifyListener);

    l = new Label(composite, SWT.LEFT);
    l.setText("Start frequency:");
    _sFreq = new Text(composite, SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    _sFreq.setLayoutData(gridData);
    _sFreq.setText("20");
    l = new Label(composite, SWT.LEFT);
    l.setText("Hz");
    _sFreq.addListener(SWT.Modify, paramModifyListener);

    l = new Label(composite, SWT.LEFT);
    l.setText("End frequency:");
    _eFreq = new Text(composite, SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    _eFreq.setLayoutData(gridData);
    _eFreq.setText("20000");
    l = new Label(composite, SWT.LEFT);
    l.setText("Hz");
    _eFreq.addListener(SWT.Modify, paramModifyListener);


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
