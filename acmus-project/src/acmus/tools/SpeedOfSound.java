/*
 *  SpeedOfSound.java
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
 * SpeedOfSound.java
 * Created on 05/07/2005
 */
package acmus.tools;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import acmus.AcmusPlugin;

/**
 * @author lku
 */
public class SpeedOfSound implements IWorkbenchWindowActionDelegate {

	Shell shell;

	Text humidity;
	Text temperature;
	Button compute;
	Text speed;
	MessageBox errorDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		// FIXME: find a way to create shell only once...
		// createShell();
	}

	private void createShell() {
		Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
		shell = new Shell(d);

		shell.setText("Speed of Sound");

		shell.setLayout(new GridLayout(2, false));
		GridData gridData;

		Label label = new Label(shell, SWT.LEFT);
		label.setText("Relative humidity (0-100%)");

		humidity = new Text(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		humidity.setLayoutData(gridData);

		label = new Label(shell, SWT.LEFT);
		label.setText("Temperature (0-30\u00b0C)");

		temperature = new Text(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		temperature.setLayoutData(gridData);

		label = new Label(shell, SWT.LEFT);
		label.setText("Speed of Sound (m/s)");

		speed = new Text(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		speed.setText("           ");
		speed.setEditable(false);
		speed.setLayoutData(gridData);

		compute = new Button(shell, SWT.NONE);
		compute.setText("Compute");

		compute.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				compute();
			}
		});
		shell.pack();

		errorDialog = new MessageBox(shell, SWT.ICON_ERROR);
		errorDialog.setMessage("Please check the input data.");
	}

	public void compute() {
		double x = 0.0;
		double y = 0.0;
		try {
			String s1 = temperature.getText();
			Double d1 = Double.valueOf(s1);
			x = Math.abs(d1.doubleValue());
			String s2 = humidity.getText();
			Double d2 = Double.valueOf(s2);
			y = Math.abs(d2.doubleValue());
			if (inputValid(x, y) == true) {
				CalculateSpeedOfSound cal = new CalculateSpeedOfSound(x, y);
				double velocity = cal.calculateSpeedOfSound();
				velocity = velocity * 10;
				int aux = (int) velocity;
				velocity = (double) aux / 10;
				speed.setText("" + velocity);
			} else
				errorDialog.open();
		} catch (Exception exception) {
			errorDialog.open();
		}
	}

	/**
	 * Verifies the input validity
	 * 
	 * @param x
	 *            temperature
	 * @param y
	 *            humidity
	 * @return true if temperature and humidity are within acceptable ranges and
	 *         false otherwise
	 */
	boolean inputValid(double x, double y) {
		if ((x >= 0.0 && x <= 30.0) && (y >= 0.0 && y <= 100.0))
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		createShell();
		shell.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}
}
