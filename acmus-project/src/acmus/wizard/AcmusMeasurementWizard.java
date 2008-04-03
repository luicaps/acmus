/*
 *  AcmusMeasurementWizard.java
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
 */
package acmus.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import acmus.MeasurementProject;

/**
 * @author lku
 */
public class AcmusMeasurementWizard extends Wizard implements INewWizard {

	private IStructuredSelection selection;

	@SuppressWarnings("unused")
	private IWorkbench workbench;

	private AcmusMeasurementWizardFirstPage mainPage;

	public void addPages() {
		IFolder set = (IFolder) selection.getFirstElement();
		IFolder session = (IFolder) set.getParent();
		IProject project = session.getProject();

		mainPage = new AcmusMeasurementWizardFirstPage(
				"AcmusMeasurementWizardFirstPage", project.getName(),
				MeasurementProject.removeSuffix(session.getName()),
				MeasurementProject.removeSuffix(set.getName()),
				(IFolder) selection.getFirstElement());
		addPage(mainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		setWindowTitle("New Measurement"); //$NON-NLS-1$
	}

	public boolean performFinish() {

		IFolder set = (IFolder) selection.getFirstElement();
		Properties props = mainPage.getMeasurementProperties();
		createMeasurement(set, props);

		return true;
	}

	public static IFolder createMeasurement(IFolder set, Properties props) {
		IFolder folder = null;
		try {
			folder = set.getFolder(props.getProperty("Name") + ".msr");
			if (!folder.exists())
				folder.create(true, true, null);

			IFile propsFile = folder.getFile("measurement.properties");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			props.store(baos, props.getProperty("Name")
					+ " Measurement folder properties");
			ByteArrayInputStream bais = new ByteArrayInputStream(baos
					.toByteArray());
			propsFile.create(bais, true, null);
			// propFile.setTeamPrivateMember(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return folder;
	}

	// public boolean performFinish() {
	//
	// String inputFn = mainPage.getInput();
	// String outputFn = mainPage.getOutput();
	//    
	// RecordPlay rp = new RecordPlay(inputFn, outputFn);
	// rp.play_record();
	//    
	// return true;
	// }
}
