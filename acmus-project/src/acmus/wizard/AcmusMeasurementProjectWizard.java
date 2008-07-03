/*
 *  AcmusMeasurementProjectWizard.java
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
 * Created on 07/01/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package acmus.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import acmus.AcmusGraphics;

/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings("unused")
public class AcmusMeasurementProjectWizard extends Wizard implements INewWizard {
	private IStructuredSelection selection;

	private IWorkbench workbench;

	private AcmusMeasurementProjectWizardFirstPage mainPage;

	private AcmusMeasurementProjectWizardSecondPage infoPage;

	public void addPages() {
		mainPage = new AcmusMeasurementProjectWizardFirstPage(
				"AcmusMeasurementProjectWizardFirstPage");
		infoPage = new AcmusMeasurementProjectWizardSecondPage(
				"AcmusMeasurementProjectWizardSecondPage");
		addPage(mainPage);
		addPage(infoPage);
	}

	/**
	 * (non-Javadoc) Method declared on INewWizard
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		setWindowTitle("New AcMus Project"); //$NON-NLS-1$
		setDefaultPageImageDescriptor(AcmusGraphics.NEW_PROJECT_WIZARD_BANNER);
	}

	/**
	 * (non-Javadoc) Method declared on IWizard
	 */
	public boolean performFinish() {
		IProject project = mainPage.getProjectHandle();
		try {
			project.create(null);
			project.open(null);

			Properties props = infoPage.getProjectProperties();
			// Enumeration en = props.keys();
			// while (en.hasMoreElements()) {
			// String key = (String) en.nextElement();
			// project.setPersistentProperty(new QualifiedName("acmus", key),
			// props
			// .getProperty(key));
			// }

			IFile propsFile = project.getFile("project.properties");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			props.store(baos, project.getName() + " properties");
			ByteArrayInputStream bais = new ByteArrayInputStream(baos
					.toByteArray());
			propsFile.create(bais, true, null);

			IFile posFile = project.getFile("project.positions");
			baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			ps.println("[floor plan 0]");
			ps.println("name = New");
			ps.println("file = ");
			ps.println("width = 0");
			ps.println("height = 0");
			bais = new ByteArrayInputStream(baos.toByteArray());
			posFile.create(bais, true, null);

			IFolder signalFolder = project.getFolder("_signals.signal");
			signalFolder.create(true, true, null);
			IFolder signalAudioFolder = signalFolder.getFolder("audio");
			signalAudioFolder.create(true, true, null);

			// IFile propsFile = project.getFile("project.properties");
			//
			// ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//
			// Properties props = infoPage.getProjectProperties();
			// props.store(baos, "AcMus Measurement Project "
			// + mainPage.getProjectName());
			//
			// propsFile
			// .create(new ByteArrayInputStream(baos.toByteArray()), true,
			// null);

			IFile prjFile = project.getFile(".project");
			prjFile.setTeamPrivateMember(true);
			
		} catch (CoreException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
}