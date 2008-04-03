/*
 *  AcmusMeasurementSessionWizard.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings("unused")
public class AcmusMeasurementSessionWizard extends Wizard implements INewWizard {

	private IStructuredSelection selection;

	private IWorkbench workbench;

	private AcmusMeasurementSessionWizardFirstPage mainPage;

	public void addPages() {
		mainPage = new AcmusMeasurementSessionWizardFirstPage(
				"AcmusMeasurementSessionWizardFirstPage", ((IProject) selection
						.getFirstElement()).getName());
		addPage(mainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		setWindowTitle("New Measurement Session"); //$NON-NLS-1$
	}

	public boolean performFinish() {
		IProject project = (IProject) selection.getFirstElement();
		Properties props = mainPage.getSessionProperties();

		try {

			IFolder folder = project.getFolder(props.getProperty("Name")
					+ ".session");
			folder.create(true, true, null);

			// Enumeration en = props.keys();
			// while (en.hasMoreElements()) {
			// String key = (String) en.nextElement();
			// folder.setPersistentProperty(new QualifiedName("acmus", key),
			// props
			// .getProperty(key));
			// }

			IFile propsFile = folder.getFile("session.properties");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			props.store(baos, project.getName() + "."
					+ props.getProperty("Name") + " properties");
			ByteArrayInputStream bais = new ByteArrayInputStream(baos
					.toByteArray());
			propsFile.create(bais, true, null);

			// IFile posFile = folder.getFile("session.positions");
			//
			// ByteArrayInputStream bais = new ByteArrayInputStream(new
			// byte[0]);
			//
			// posFile.create(bais, true, null);

			// IFile propsFile = folder.getFile("session.properties");
			//
			// ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//
			// props.store(baos, "AcMus Measurement Session");
			//
			// propsFile
			// .create(new ByteArrayInputStream(baos.toByteArray()), true,
			// null);

		} catch (CoreException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
}