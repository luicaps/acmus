/*
 *  AcmusMeasurementWizardFirstPage.java
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
 * Created on 14/01/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package acmus.wizard;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import acmus.editor.MeasurementPropertiesControl;

/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AcmusMeasurementWizardFirstPage extends WizardPage {

	IFolder fMeasurementSet;
	MeasurementPropertiesControl _propertiesControl;
	static DecimalFormat _nFormat = new DecimalFormat("00");

	String _projectName;
	String _sessionName;
	String _setName;

	public AcmusMeasurementWizardFirstPage(String name, String projectName,
			String sessionName, String setName, IFolder mSet) {
		super(name);
		setTitle("New Measurement");
		setDescription("");
		setPageComplete(false);
		fMeasurementSet = mSet;
		_projectName = projectName;
		_sessionName = sessionName;
		_setName = setName;
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
		String nameSuggestion = generateNameFor(fMeasurementSet, _setName);
		_propertiesControl = new MeasurementPropertiesControl(composite,
				SWT.NONE, nameSuggestion);

		_propertiesControl.setProjectName(_projectName);
		_propertiesControl.setSessionName(_sessionName);
		_propertiesControl.setSetName(_setName);

		ModifyListener nameModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		};
		_propertiesControl.addNameModifyListener(nameModifyListener);
		setPageComplete(validatePage());
		setControl(composite);
	}

	public boolean validatePage() {
		return !_propertiesControl.getMeasurementName().trim().equals("");
		// return !(fSource.getText().trim().equals("") ||
		// fDest.getText().trim().equals(""));
	}

	public Properties getMeasurementProperties() {
		return _propertiesControl.getMeasurementProperties();
	}

	public static String generateNameFor(IFolder measurementSet, String prefix) {
		int n = findNextSuffix(measurementSet, prefix, _nFormat, 1);
		return prefix + "_" + _nFormat.format(n);
	}

	public static int findNextSuffix(IFolder folder, String prefix,
			NumberFormat format, int start) {
		int res = start;
		while (true) {
			IFolder f = folder.getFolder(prefix + "_" + format.format(res)
					+ ".msr");
			if (!f.exists())
				break;
			res++;
		}
		return res;
	}

}