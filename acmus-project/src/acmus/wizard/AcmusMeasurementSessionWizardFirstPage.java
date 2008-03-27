/*
 *  AcmusMeasurementSessionWizardFirstPage.java
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

import java.util.Properties;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import acmus.editor.SessionPropertiesControl;

/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AcmusMeasurementSessionWizardFirstPage extends WizardPage {

  private SessionPropertiesControl _propertiesControl;

  private String _projectName;
  
  public AcmusMeasurementSessionWizardFirstPage(String name, String projectName) {
    super(name);
    setTitle("New Measurement Session");
    setDescription("Specify session info.");
    setPageComplete(false);
    _projectName = projectName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    FillLayout fillLayout = new FillLayout();
    fillLayout.marginHeight = 0;
    fillLayout.marginWidth = 0;
    composite.setLayout(fillLayout);
    composite.setFont(parent.getFont());
    _propertiesControl = new SessionPropertiesControl(composite, SWT.NONE);
    setControl(composite);
    _propertiesControl.setProjectName(_projectName);
    
    ModifyListener nameModifyListener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setPageComplete(validatePage());
      }
    };
    _propertiesControl.addNameModifyListener(nameModifyListener);
  }

  public boolean validatePage() {
    return !_propertiesControl.getSessionName().trim().equals("");
  }

  /**
   * Updates and returns the <code>Properties</code> object that contains the
   * session properties.
   * 
   * @return the session properties
   */
  public Properties getSessionProperties() {
    return _propertiesControl.getSessionProperties();
  }

}