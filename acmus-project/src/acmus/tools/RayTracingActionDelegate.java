/*
 *  RayTracingActionDelegate.java
 *  This file is part of AcMus.
 *  
 *  AcMus: Tools for Measurement, Analysis, and Simulation of Room Acoustics
 *  
 *  Copyright (C) 2006 Mario h.c.t. Vinicius g.p.
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
 * Created on 27/10/2006
 */
package acmus.tools;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import acmus.AcmusGraphics;

public class RayTracingActionDelegate implements IWorkbenchWindowActionDelegate {

	@SuppressWarnings("unused")
	private IStructuredSelection _sel;
	private Display _display;
	
	public void dispose() {

	}

	public void init(IWorkbenchWindow window) {
		_display = window.getWorkbench().getDisplay();
	}

	public void run(IAction action) {
		
		Shell shell = new Shell(_display);
		shell.setLayout(new GridLayout(2, false));

		RayTracing rt = new RayTracing(shell, SWT.NONE);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 700;
		gridData.widthHint = 930;
		rt.setLayoutData(gridData);

		shell.setText("Ray Tracing");
		shell.setImage(AcmusGraphics.IMG_APP_ICON);
		shell.pack();
		
		shell.open();

	}

	public void selectionChanged(IAction action, ISelection selection) {
		_sel = (IStructuredSelection) selection;
	}
}
