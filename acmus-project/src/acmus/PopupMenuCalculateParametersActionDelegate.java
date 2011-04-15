/*
 *  PopupMenuCalculateParametersActionDelegate.java
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
package acmus;

import java.io.BufferedInputStream;
import java.util.Properties;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import acmus.dsp.Ir;
import acmus.util.WaveUtils;

public class PopupMenuCalculateParametersActionDelegate implements
		IObjectActionDelegate {

	// private IWorkbenchPart part;
	private IStructuredSelection sel;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// this.part = targetPart;
	}

	public void run(IAction action) {

		try {
			if (!sel.isEmpty()) {
				IFolder sessionF = (IFolder) sel.getFirstElement();
				IResource m[] = sessionF.members();
				for (int i = 0; i < m.length; i++) {
					if (m[i] instanceof IFolder) {
						if (m[i].getName().endsWith(".set")) {
							IResource m2[] = ((IFolder) m[i]).members();
							for (int j = 0; j < m2.length; j++) {
								if (m2[j] instanceof IFolder) {
									if (m2[j].getName().endsWith(".msr")) {
										calculate((IFolder) m2[j]);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		sel = (IStructuredSelection) selection;
	}

	public void calculate(IFolder measureF) throws Exception {

		IFile irFile = measureF.getFile("ir.wav");
		double ir[] = null;
		if (!irFile.exists()) {
			IFile recFile = measureF.getFile("recording.wav");

			if (!recFile.exists()) {
				System.err.println(measureF.getName()
						+ ": recording.wav not found!!!");
				return;
			}

			Properties props = new Properties();
			props
					.load(measureF.getFile("measurement.properties")
							.getContents());
			IFile sigFile = irFile.getProject().getFolder("_signals.signal")
					.getFile(props.getProperty("Signal"));
			ir = Ir.calculateIr(recFile, irFile, sigFile, null);
		} else {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(irFile
					.getContents()));
			int wav[] = WaveUtils.readData(ais);
			ir = new double[wav.length];
			ir = WaveUtils.scaleToUnitInPlace(ir, wav, ais.getFormat()
					.getSampleSizeInBits());
		}

		IFile _paramsFile = measureF.getFile("parameters.txt");
		IFolder _schroederFolder = measureF.getFolder("schroeder");
		Ir.calculateParameters(ir, null, "Chu", _paramsFile,
				_schroederFolder);

	}
}
