/*
 *  ConvolutionActionDelegate.java
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
/**
 * Created on 14/02/2006
 */
package acmus.tools;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;

public class ConvolutionActionDelegate implements
	IWorkbenchWindowActionDelegate {

	private IStructuredSelection _sel;

	public void dispose() {

	}

	public void init(IWorkbenchWindow window) {	

	}

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
		Shell shell = new Shell(d);
		shell.setLayout(new FillLayout());

		Convolution conv = new Convolution(shell, shell, SWT.NONE);

		if (_sel.size() == 2) {
			List<IFile> l = _sel.toList();
			IFile f1 = l.get(0);
			IFile f2 = l.get(1);
			conv.setInput1(f1.getLocation().toOSString());
			conv.setInput2(f2.getLocation().toOSString());
		}

		shell.setText("Convolution");
		shell.setImage(AcmusGraphics.IMG_APP_ICON);
		shell.pack();
		shell.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		_sel = (IStructuredSelection) selection;
	}

}
