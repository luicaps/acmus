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

import acmus.AcmusPlugin;

/**
 * @author lku
 */
public class ResonanceFrequency implements IWorkbenchWindowActionDelegate {

	Shell shell;
	private Text width;
	private Text length;
	private Text height;
	private Text text;
	private Button compute;

	private double w = 0.0;
	private double l = 0.0;
	private double h = 0.0;

	MessageBox inputErrorDialog;

	Histogram hAxial;
	Histogram hTangential;
	Histogram hOblique;
	static String[] histLabels = { "50", "100", "150", "200", "250", "300" };
	static String[] emptyTopLabels = new String[0];
	static double[] emptyDataX = new double[0];
	static double[] emptyDataY = new double[0];
	Label lAxial;
	Label lTangential;
	Label lOblique;
	// static String newLine = System.getenv("line.separator");
	static String newLine = System.getProperty("line.separator");

	SelectionAdapter computeListener = new SelectionAdapter() {

		private void drawGraph(Histogram hist, Vector<Double> val, Label label,
				String str1, String str2) {
			if (val.isEmpty() == true) {
				label.setText("All " + str1
						+ " frequencies are greater than 300Hz");
				hist.setData(emptyDataX, emptyDataY, emptyTopLabels);
			} else {
				label.setText(str2);
				double dataX[] = new double[val.size()];
				double dataY[] = new double[dataX.length];
				String topLabels[] = new String[dataX.length];

				for (int i = 0; i < val.size(); i++) {
					dataX[i] = val.elementAt(i);
					dataY[i] = 1;
					int number = 0;
					for (int j = 0; j < val.size(); j++) {
						double aux1 = val.elementAt(i);
						double aux2 = val.elementAt(j);
						if (aux1 == aux2)
							number++;
					}
					if (number > 1)
						topLabels[i] = "" + number;
					else
						topLabels[i] = "";
				}
				hist.setData(dataX, dataY, topLabels);
				hist.setYMax(1.5);
			}
		}

		private void drawGraph2(Histogram hist, Vector<Double> val,
				Label label, String str1, String str2) {
			if (val.isEmpty() == true) {
				label.setText("All " + str1
						+ " frequencies are greater than 300Hz");
				hist.setData(emptyDataX, emptyDataY, emptyTopLabels);
			} else {
				label.setText(str2);
				double dataX[] = new double[val.size()];
				double dataY[] = new double[dataX.length];
				String topLabels[] = new String[dataX.length];

				for (int i = 0; i < val.size(); i++) {
					dataX[i] = val.elementAt(i);
					dataY[i] = 1;
					topLabels[i] = "";
				}
				hist.setData(dataX, dataY, topLabels);
				hist.setYMax(1.5);
			}
		}/* method drawAxialGraph */

		/**
		 * Reads inputs, computes resonance frequencies and generates result in
		 * different format
		 * 
		 * @param e
		 *            ActionEvent
		 */
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				ResonanceFrequency.this.w = Math.abs(Double
						.parseDouble(ResonanceFrequency.this.width.getText()));
				ResonanceFrequency.this.l = Math.abs(Double
						.parseDouble(ResonanceFrequency.this.length.getText()));
				ResonanceFrequency.this.h = Math.abs(Double
						.parseDouble(ResonanceFrequency.this.height.getText()));
				CalculateFrequency cal = new CalculateFrequency(
						ResonanceFrequency.this.w, ResonanceFrequency.this.l,
						ResonanceFrequency.this.h);
				cal.calculateAxialFrequency();
				cal.calculateTangentialFrequency();
				cal.calculateObliqueFrequency();
				if (cal.returnInputValidity() == false)
					ResonanceFrequency.this.inputErrorDialog.open();
				else {
					drawGraph(ResonanceFrequency.this.hAxial, cal
							.getAxialFrequencyVector(),
							ResonanceFrequency.this.lAxial, "axial", "Axial");
					drawGraph2(ResonanceFrequency.this.hTangential, cal
							.getTangentialFrequencyVector(),
							ResonanceFrequency.this.lTangential, "Tangential",
							"Tangential");
					drawGraph2(ResonanceFrequency.this.hOblique, cal
							.getObliqueFrequencyVector(),
							ResonanceFrequency.this.lOblique, "oblique",
							"Oblique");
					Vector<Double> v = cal.getAxialFrequencyVector();
					ResonanceFrequency.this.text.setText("");
					if (v.size() == 0)
						ResonanceFrequency.this.text
								.append("All axial frequencies are greater than 300Hz"
										+ newLine);
					else
						displayAxialFrequencies(v);
					ResonanceFrequency.this.text.append(newLine);
					v = cal.getTangentialFrequencyVector();
					if (v.size() == 0)
						ResonanceFrequency.this.text
								.append("All tangential frequencies are greater than 300Hz"
										+ newLine);
					else
						displayTangentialFrequencies(v);
					ResonanceFrequency.this.text.append(newLine);
					v = cal.getObliqueFrequencyVector();
					if (v.size() == 0)
						ResonanceFrequency.this.text
								.append("All oblique frequencies are greater than 300Hz"
										+ newLine);
					else
						displayObliqueFrequencies(v);
				}
			} catch (Exception e) {
				e.printStackTrace();
				ResonanceFrequency.this.inputErrorDialog.open();
			}
		}

		private void displayObliqueFrequencies(Vector<Double> v) {
			ResonanceFrequency.this.text.append("Oblique frequencies:"
					+ newLine);
			double[] difference = new double[v.size() - 1];
			int index = 0;
			double[] oblique_frequency = new double[v.size()];
			// double sum = 0.0;
			double sum_difference = 0.0;
			for (int i = 0; i < v.size(); i++) {
				double d = (v.elementAt(i)).doubleValue();
				oblique_frequency[i] = d;
			}
			Arrays.sort(oblique_frequency);
			for (int i = 0; i < v.size(); i++) {
				double d = oblique_frequency[i];
				d = d * 10;
				int aux = (int) d;
				d = aux / 10.0;
				if (i > 0) {
					double d1 = oblique_frequency[i - 1];
					d1 = d1 * 10;
					int aux1 = (int) d1;
					d1 = aux1 / 10.0;
					double d2 = d - d1;
					difference[index] = d2;
					index++;
					sum_difference += d2;
					d2 = d2 * 10;
					int aux2 = (int) d2;
					d2 = aux2 / 10.0;
					ResonanceFrequency.this.text.append("" + d + newLine);
				} else
					ResonanceFrequency.this.text.append("" + d + newLine);
			}
			double average;
			if (v.size() > 1)
				average = sum_difference / (v.size() - 1);
			else
				average = sum_difference;
			double standard_deviation = 0.0;
			for (int i = 0; i < index; i++)
				standard_deviation += (difference[i] - average)
						* (difference[i] - average);
			if (v.size() > 1)
				standard_deviation = standard_deviation / (v.size() - 1);
			standard_deviation = Math.sqrt(standard_deviation);
			standard_deviation *= 10.0;
			int sd = (int) standard_deviation;
			standard_deviation = sd / 10.0;
			ResonanceFrequency.this.text.append("Standard deviation: "
					+ standard_deviation + newLine);
			if (v.size() > 1)
				sum_difference = sum_difference / (v.size() - 1);
			sum_difference *= 10.0;
			int s_d = (int) sum_difference;
			sum_difference = s_d / 10.0;
			ResonanceFrequency.this.text.append("Average of the differences: "
					+ sum_difference + newLine);
		}

		private void displayTangentialFrequencies(Vector<Double> v) {
			ResonanceFrequency.this.text.append("Tangential frequencies:"
					+ newLine);
			double[] difference = new double[v.size() - 1];
			int index = 0;
			double[] tangential_frequency = new double[v.size()];
			double sum_difference = 0.0;
			for (int i = 0; i < v.size(); i++) {
				double d = (v.elementAt(i)).doubleValue();
				tangential_frequency[i] = d;
			}
			Arrays.sort(tangential_frequency);
			for (int i = 0; i < v.size(); i++) {
				double d = tangential_frequency[i];
				d = d * 10;
				int aux = (int) d;
				d = aux / 10.0;
				if (i > 0) {
					double d1 = tangential_frequency[i - 1];
					d1 = d1 * 10;
					int aux1 = (int) d1;
					d1 = aux1 / 10.0;
					double d2 = d - d1;
					difference[index] = d2;
					index++;
					sum_difference += d2;
					d2 = d2 * 10;
					int aux2 = (int) d2;
					d2 = aux2 / 10.0;
					ResonanceFrequency.this.text.append("" + d + newLine);
				} else
					ResonanceFrequency.this.text.append("" + d + newLine);
			}
			double average;
			if (v.size() > 1)
				average = sum_difference / (v.size() - 1);
			else
				average = sum_difference;
			double standard_deviation = 0.0;
			for (int i = 0; i < index; i++)
				standard_deviation += (difference[i] - average)
						* (difference[i] - average);
			if (v.size() > 1)
				standard_deviation = standard_deviation / (v.size() - 1);
			standard_deviation = Math.sqrt(standard_deviation);
			standard_deviation *= 10.0;
			int sd = (int) standard_deviation;
			standard_deviation = sd / 10.0;
			ResonanceFrequency.this.text.append("Standard deviation: "
					+ standard_deviation + newLine);
			if (v.size() > 1)
				sum_difference = sum_difference / (v.size() - 1);
			sum_difference *= 10.0;
			int s_d = (int) sum_difference;
			sum_difference = s_d / 10.0;
			ResonanceFrequency.this.text.append("Average of the differences: "
					+ sum_difference + newLine);
		}

		private void displayAxialFrequencies(Vector<Double> v) {
			ResonanceFrequency.this.text.append("Axial frequencies:" + newLine);
			double[] difference = new double[v.size() - 1];
			int index = 0;
			double[] axial_frequency = new double[v.size()];
			double sum_difference = 0.0;
			for (int i = 0; i < v.size(); i++) {
				double d = v.elementAt(i);
				axial_frequency[i] = d;
			}
			Arrays.sort(axial_frequency);
			for (int i = 0; i < v.size(); i++) {
				double d = axial_frequency[i];
				d = d * 10;
				int aux = (int) d;
				d = aux / 10.0;
				if (i > 0) {
					double d1 = axial_frequency[i - 1];
					d1 = d1 * 10;
					int aux1 = (int) d1;
					d1 = aux1 / 10.0;
					double d2 = d - d1;
					difference[index] = d2;
					index++;
					sum_difference += d2;
					d2 = d2 * 10;
					int aux2 = (int) d2;
					d2 = aux2 / 10.0;
					ResonanceFrequency.this.text.append("" + d + newLine);
				} else
					ResonanceFrequency.this.text.append("" + d + newLine);
			}
			double average;
			if (v.size() > 1)
				average = sum_difference / (v.size() - 1);
			else
				average = sum_difference;
			double standard_deviation = 0.0;
			for (int i = 0; i < index; i++)
				standard_deviation += (difference[i] - average)
						* (difference[i] - average);
			if (index > 1)
				standard_deviation = standard_deviation / (v.size() - 1);
			standard_deviation = Math.sqrt(standard_deviation);
			standard_deviation *= 10.0;
			int sd = (int) standard_deviation;
			standard_deviation = sd / 10.0;
			ResonanceFrequency.this.text.append("Standard deviation: "
					+ standard_deviation + newLine);
			if (v.size() > 1)
				sum_difference = sum_difference / (v.size() - 1);
			sum_difference *= 10.0;
			int s_d = (int) sum_difference;
			sum_difference = s_d / 10.0;
			ResonanceFrequency.this.text.append("Average of the differences: "
					+ sum_difference + newLine);
		}

	}; /* computeListener */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

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

	// private Text newText(Composite parent, String lb) {
	// return newText(parent, lb, "0");
	// }

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

		this.lAxial = new Label(g2, SWT.NONE);
		this.lAxial.setText("Axial");
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.CENTER);
		this.lAxial.setLayoutData(gridData);
		this.hAxial = new Histogram(g2, SWT.NONE, "Hz", "", histLabels);
		this.hAxial.setIntermediateTicks(4);
		this.hAxial.setBarWidth(1);
		this.hAxial.setXMax(300);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 140;
		gridData.widthHint = 400;
		this.hAxial.setLayoutData(gridData);

		this.lTangential = new Label(g2, SWT.NONE);
		this.lTangential.setText("Tangential");
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.CENTER);
		this.lTangential.setLayoutData(gridData);
		this.hTangential = new Histogram(g2, SWT.NONE, "Hz", "", histLabels);
		this.hTangential.setIntermediateTicks(4);
		this.hTangential.setBarWidth(1);
		this.hTangential.setXMax(300);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 140;
		gridData.widthHint = 400;
		this.hTangential.setLayoutData(gridData);

		this.lOblique = new Label(g2, SWT.NONE);
		this.lOblique.setText("Oblique");
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.CENTER);
		this.lOblique.setLayoutData(gridData);
		this.hOblique = new Histogram(g2, SWT.NONE, "Hz", "", histLabels);
		this.hOblique.setIntermediateTicks(4);
		this.hOblique.setBarWidth(1);
		this.hOblique.setXMax(300);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 140;
		gridData.widthHint = 400;
		this.hOblique.setLayoutData(gridData);

		this.inputErrorDialog = new MessageBox(this.shell, SWT.ICON_ERROR);
		this.inputErrorDialog.setMessage("Please check the input data.");

		this.shell.pack();
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
		// TODO Auto-generated method stub

	}
}