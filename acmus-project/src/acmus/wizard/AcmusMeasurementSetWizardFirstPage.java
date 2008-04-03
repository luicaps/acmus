/*
 *  AcmusMeasurementSetWizardFirstPage.java
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

import acmus.editor.SetPropertiesControl;

/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AcmusMeasurementSetWizardFirstPage extends WizardPage {

	SetPropertiesControl _propertiesControl;

	String _projectName;
	String _sessionName;

	String _posFile;

	public AcmusMeasurementSetWizardFirstPage(String name, String projectName,
			String sessionName, String posFile) {
		super(name);
		setTitle("New Measurement Set");
		setDescription("Specify measurement info.");
		setPageComplete(false);
		_projectName = projectName;
		_sessionName = sessionName;
		_posFile = posFile;
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
		_propertiesControl = new SetPropertiesControl(composite, SWT.NONE,
				_posFile);
		setControl(composite);

		_propertiesControl.setProjectName(_projectName);
		_propertiesControl.setSessionName(_sessionName);

		ModifyListener nameModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		};
		_propertiesControl.addNameModifyListener(nameModifyListener);

	}

	public boolean validatePage() {
		return !_propertiesControl.getSetName().trim().equals("");
	}

	/**
	 * Updates and returns the <code>Properties</code> object that contains
	 * the given session properties.
	 * 
	 * @return the given session properties
	 */
	public Properties getSetProperties() {
		return _propertiesControl.getSetProperties();
	}

}
