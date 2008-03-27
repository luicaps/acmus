/*
 *  AcmusMeasurementProjectWizardSecondPage.java
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
 * Created on 10/01/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package acmus.wizard;

import java.util.Properties;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import acmus.editor.ProjectPropertiesControl;


/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AcmusMeasurementProjectWizardSecondPage extends WizardPage {

  AcmusMeasurementProjectWizardFirstPage _firstPage;
  ProjectPropertiesControl _propertiesControl;
  
  public AcmusMeasurementProjectWizardSecondPage(String name) {
    super(name);
    setTitle("Project info");
    setDescription("Specify information about the project.");
  }

  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    FillLayout fillLayout = new FillLayout();
    fillLayout.marginHeight = 0;
    fillLayout.marginWidth = 0;
    composite.setLayout(fillLayout);
    composite.setFont(parent.getFont());
    _propertiesControl = new ProjectPropertiesControl(composite, SWT.NONE);
    setControl(composite);
  }

  /**
   * Updates and returns the <code>Properties</code> object that contains the
   * project properties.
   * 
   * @return the project properties
   */
  public Properties getProjectProperties() {
    return _propertiesControl.getProjectProperties();
  }

}