/*
 *  PopupMenuCalculateIrActionDelegate.java
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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class PopupMenuExportAudioActionDelegate implements
		IWorkbenchWindowActionDelegate {

	private IStructuredSelection sel;
	private Display display;

	public void run(IAction action) {
		display = AcmusPlugin.getDefault().getWorkbench().getDisplay();	
		Shell shell = display.getActiveShell();
			
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setText("Select destination File");
		dialog.setFilterExtensions(new String[] { "*.wave", "*.wav" });
		String destFile = dialog.open();
		if (destFile != null) {
			IFile source = (IFile) sel.getFirstElement();
			copyFile(source, destFile);
		}
	}

	private void copyFile(IFile source, String destFile) {
		try {
			InputStream src = source.getContents();
			OutputStream dest = new FileOutputStream(destFile);
			byte[] buffer = new byte[1 << 15];
			int len;
			while ((len = src.read(buffer)) != -1) {
				dest.write(buffer, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		sel = (IStructuredSelection) selection;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		
	}
}
