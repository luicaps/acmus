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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import acmus.AcmusPlugin;

public class RayTracingActionDelegate implements IWorkbenchWindowActionDelegate {

	public void dispose() {

	}

	public void init(IWorkbenchWindow window) {

	}

	public void run(IAction action) {
																			// tela
		Shell shell = new Shell();

		GridLayout gd = new GridLayout();
		gd.numColumns = 2;
		shell.setLayout(gd);

		new RayTracing(shell, SWT.NONE);

		shell.setText("Ray Tracing");
		
		shell.pack();
		
		shell.setSize(950, 700);
		
		shell.open();

	}

	public void selectionChanged(IAction action, ISelection selection) {

	}
}
