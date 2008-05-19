/*
 *  MlsWizard.java
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
package acmus.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import acmus.dsp.Signal;
import acmus.dsp.Util;

public class MlsWizard extends Wizard implements INewWizard {

	private IStructuredSelection selection;

	@SuppressWarnings("unused")
	private IWorkbench workbench;

	private MlsWizardFirstPage mainPage;

	public void addPages() {
		// IResource s = (IResource) selection.getFirstElement();
		mainPage = new MlsWizardFirstPage("MlsWizardFirstPage");
		addPage(mainPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		setWindowTitle("New MLS Signal"); //$NON-NLS-1$
	}

	public boolean performFinish() {
		IFolder folder = (IFolder) selection.getFirstElement();
		IFolder audioFolder = folder.getFolder("audio");
		Properties props = mainPage.getSessionProperties();

		try {

			IFile audioFile = audioFolder.getFile(props.getProperty("Name")
					+ ".wav");

			int order = Integer.parseInt(props.getProperty("Order"));
			String staps = props.getProperty("Taps");
			StringTokenizer st = new StringTokenizer(staps);
			int taps[] = new int[st.countTokens()];
			for (int i = 0; i < taps.length; i++) {
				taps[i] = Integer.parseInt(st.nextToken());
			}
			int reps = Integer.parseInt(props.getProperty("Repetitions"));

			double y[] = new double[reps * ((1 << order) - 1)];
			int row[] = new int[(1 << order) - 1];
			int col[] = new int[(1 << order) - 1];

			Signal.mls(order, taps[0], taps[1], y, row, col, reps);
			for (int i = 0; i < y.length; i++) {
				y[i] = y[i] * 0.5;
			}
			double[] scaled = Util.scaleToMax(y, (double) Util.getLimit(16));
			Util.wavWrite(scaled, audioFile.getLocation().toOSString());

			props.put("Type", "mls");
			props.put("Row", Util.toString(row));
			props.put("Col", Util.toString(col));
			props.put("SampleRate", "44100");

			IFile file = folder.getFile(props.getProperty("Name") + ".signal");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			props.store(baos, "");
			ByteArrayInputStream bais = new ByteArrayInputStream(baos
					.toByteArray());
			file.create(bais, true, null);

			folder.refreshLocal(2, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

}
