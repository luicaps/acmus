/*
 *  ResonanceFrequency.java
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
 * ResonanceFrequency.java
 * Created on 11/07/2005
 */
package acmus.tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Vector;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jfree.experimental.chart.swt.ChartComposite;

import acmus.AcmusPlugin;
import acmus.graphics.ChartBuilder;

/**
 * @author lku
 */
public class ResonanceFrequency implements IWorkbenchWindowActionDelegate {

	private final class SelectionAdapterImpl extends SelectionAdapter {
		private NumberFormat formatter = new DecimalFormat("#####.0");

		/**
		 * Reads inputs, computes resonance frequencies and generates result in
		 * different format
		 * 
		 * @param e
		 *            ActionEvent
		 */
		@Override
		public void widgetSelected(SelectionEvent event) {
			ResonanceFrequency parent = ResonanceFrequency.this;
			try {
				CalculateFrequency cal = new CalculateFrequency(parent.width
						.getText(), parent.length.getText(), parent.height
						.getText());
				cal.calculateFrequencies();
				parent.chart.setChart(new ChartBuilder().getHistogram()
						.addData(cal.getAxialFrequencyVector(), "Axial")
						.addData(cal.getTangentialFrequencyVector(),
								"Tangential").addData(
								cal.getObliqueFrequencyVector(), "Oblique")
						.setAxisLabels("Frequency (Hz)", "Incidence").setTitle(
								"Resonance Frequencies").build());
				parent.chart.forceRedraw();

				displayFrequencies(cal);

			} catch (Exception e) {
				e.printStackTrace();
				parent.inputErrorDialog.open();
			}
		}

		private void displayFrequencies(CalculateFrequency cal) {
			text.setText("");

			displayFrequencies(cal.getAxialFrequencyVector(), "Axial");
			displayFrequencies(cal.getTangentialFrequencyVector(), "Tangential");
			displayFrequencies(cal.getObliqueFrequencyVector(), "Oblique");
		}

		private void displayFrequencies(Vector<Double> v, String type) {
			if (v.size() == 0)
				text.append("All " + type +	" frequencies are greater than 300Hz"
						+ newLine);
			else {
				text.append(type + " frequencies:" + newLine);
				displayFrequencies(v);
			}
			text.append(newLine);
		}

		private void displayFrequencies(Vector<Double> v) {
			double[] difference = new double[v.size() - 1];
			double[] frequency = new double[v.size()];
			double sum_difference = 0.0;
			for (int i = 0; i < v.size(); i++)
				frequency[i] = (v.elementAt(i)).doubleValue();

			Arrays.sort(frequency);

			ResonanceFrequency.this.text.append(""
					+ this.formatter.format(frequency[0]) + newLine);
			for (int i = 1, index = 0; i < v.size(); i++, index++) {
				difference[index] = frequency[i] - frequency[i - 1];
				sum_difference += difference[index];
				ResonanceFrequency.this.text.append(""
						+ this.formatter.format(frequency[i]) + newLine);
			}

			double average = sum_difference;
			if (v.size() > 1)
				average = sum_difference / (v.size() - 1);

			double standard_deviation = 0.0;
			for (int i = 0; i < difference.length; i++)
				standard_deviation += (difference[i] - average)
						* (difference[i] - average);

			if (v.size() > 1) {
				standard_deviation = standard_deviation / (v.size() - 1);
				sum_difference = sum_difference / (v.size() - 1);
			}

			standard_deviation = Math.sqrt(standard_deviation);
			ResonanceFrequency.this.text.append("Standard deviation: "
					+ this.formatter.format(standard_deviation) + newLine);

			ResonanceFrequency.this.text.append("Average of the differences: "
					+ this.formatter.format(sum_difference) + newLine);
		}
	}

	private Shell shell;
	private Text width;
	private Text length;
	private Text height;
	private Text text;
	private Button compute;

	private MessageBox inputErrorDialog;

	private static String newLine = System.getProperty("line.separator");

	private SelectionAdapter computeListener = new SelectionAdapterImpl(); /* computeListener */
	private ChartComposite chart;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {

	}

	private Text newText(Composite parent, String lb, String str) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(lb);
		Text t = new Text(parent, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = 50;
		t.setLayoutData(gridData);
		t.setText(str);
		return t;
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
		this.shell = new Shell(d);

		this.shell.setText("Resonance Frequency");

		this.shell.setLayout(new GridLayout(1, false));
		GridData gridData;

		Group g = new Group(this.shell, SWT.SHADOW_ETCHED_IN);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		g.setLayoutData(gridData);
		g.setLayout(new GridLayout(1, false));
		g.setText("Choose dimensions in meters");

		Composite c = new Composite(g, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.CENTER;
		c.setLayoutData(gridData);
		c.setLayout(new GridLayout(7, false));

		this.width = newText(c, "Width", "0");
		this.length = newText(c, "Length", "0");
		this.height = newText(c, "Height", "0");

		this.compute = new Button(c, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalAlignment = GridData.CENTER;
		this.compute.setLayoutData(gridData);
		this.compute.setText("Compute");
		this.compute.addSelectionListener(this.computeListener);

		g = new Group(this.shell, SWT.SHADOW_ETCHED_IN);
		gridData = new GridData(GridData.FILL_BOTH);
		g.setLayoutData(gridData);
		g.setLayout(new GridLayout(2, false));

		this.text = new Text(g, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		gridData = new GridData(GridData.FILL_VERTICAL);
		gridData.widthHint = 220;
		gridData.horizontalAlignment = GridData.END;
		this.text.setLayoutData(gridData);
		this.text.setEditable(false);

		Composite g2 = new Composite(g, SWT.NONE);
		gridData = new GridData(GridData.FILL_BOTH);
		g2.setLayoutData(gridData);
		g2.setLayout(new GridLayout(1, false));

		chart = createChart(g2);

		this.inputErrorDialog = new MessageBox(this.shell, SWT.ICON_ERROR);
		this.inputErrorDialog.setMessage("Please check the input data.");

		this.shell.pack();
	}

	private ChartComposite createChart(Composite g2) {
		ChartComposite result = new ChartComposite(g2, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 420;
		gridData.widthHint = 400;
		result.setLayoutData(gridData);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		createShell();
		this.shell.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

}