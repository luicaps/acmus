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
		public void widgetSelected(SelectionEvent event) {
			try {
				w = Math.abs(Double.parseDouble(width.getText()));
				l = Math.abs(Double.parseDouble(length.getText()));
				h = Math.abs(Double.parseDouble(height.getText()));
				CalculateFrequency cal = new CalculateFrequency(w, l, h);
				cal.calculateAxialFrequency();
				cal.calculateTangentialFrequency();
				cal.calculateObliqueFrequency();
				if (cal.returnInputValidity() == false)
					inputErrorDialog.open();
				else {
					drawGraph(hAxial, cal.axial_frequency, lAxial, "axial",
							"Axial");
					drawGraph2(hTangential, cal.tangential_frequency,
							lTangential, "Tangential", "Tangential");
					drawGraph2(hOblique, cal.oblique_frequency, lOblique,
							"oblique", "Oblique");
					Vector<Double> v = cal.axial_frequency;
					text.setText("");
					if (v.size() == 0) {
						text
								.append("All axial frequencies are greater than 300Hz"
										+ newLine);
					} else {
						displayAxialFrequencies(v);
					}
					text.append(newLine);
					v = cal.tangential_frequency;
					if (v.size() == 0) {
						text
								.append("All tangential frequencies are greater than 300Hz"
										+ newLine);
					} else {
						displayTangentialFrequencies(v);
					}
					text.append(newLine);
					v = cal.oblique_frequency;
					if (v.size() == 0) {
						text
								.append("All oblique frequencies are greater than 300Hz"
										+ newLine);
					} else {
						displayObliqueFrequencies(v);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				inputErrorDialog.open();
			}
		}

		private void displayObliqueFrequencies(Vector<Double> v) {
			text.append("Oblique frequencies:" + newLine);
			double[] difference = new double[v.size() - 1];
			int index = 0;
			double[] oblique_frequency = new double[v.size()];
			// double sum = 0.0;
			double sum_difference = 0.0;
			for (int i = 0; i < v.size(); i++) {
				double d = ((Double) v.elementAt(i)).doubleValue();
				oblique_frequency[i] = d;
			}
			sort(oblique_frequency);
			for (int i = 0; i < v.size(); i++) {
				double d = oblique_frequency[i];
				d = d * 10;
				int aux = (int) d;
				d = (double) aux / 10.0;
				if (i > 0) {
					double d1 = oblique_frequency[i - 1];
					d1 = d1 * 10;
					int aux1 = (int) d1;
					d1 = (double) aux1 / 10.0;
					double d2 = d - d1;
					difference[index] = d2;
					index++;
					sum_difference += d2;
					d2 = d2 * 10;
					int aux2 = (int) d2;
					d2 = (double) aux2 / 10.0;
					text.append("" + d + newLine);
				} else {
					text.append("" + d + newLine);
				}
			}
			double average;
			if (v.size() > 1)
				average = (double) sum_difference / (v.size() - 1);
			else
				average = sum_difference;
			double standard_deviation = 0.0;
			for (int i = 0; i < index; i++) {
				standard_deviation += (difference[i] - average)
						* (difference[i] - average);
			}
			if (v.size() > 1)
				standard_deviation = (double) standard_deviation
						/ (v.size() - 1);
			standard_deviation = Math.sqrt(standard_deviation);
			standard_deviation *= 10.0;
			int sd = (int) standard_deviation;
			standard_deviation = (double) sd / 10.0;
			text.append("Standard deviation: " + standard_deviation + newLine);
			if (v.size() > 1)
				sum_difference = (double) sum_difference / (v.size() - 1);
			sum_difference *= 10.0;
			int s_d = (int) sum_difference;
			sum_difference = (double) s_d / 10.0;
			text.append("Average of the differences: " + sum_difference
					+ newLine);
		}

		private void displayTangentialFrequencies(Vector<Double> v) {
			text.append("Tangential frequencies:" + newLine);
			double[] difference = new double[v.size() - 1];
			int index = 0;
			double[] tangential_frequency = new double[v.size()];
			double sum_difference = 0.0;
			for (int i = 0; i < v.size(); i++) {
				double d = ((Double) v.elementAt(i)).doubleValue();
				tangential_frequency[i] = d;
			}
			sort(tangential_frequency);
			for (int i = 0; i < v.size(); i++) {
				double d = tangential_frequency[i];
				d = d * 10;
				int aux = (int) d;
				d = (double) aux / 10.0;
				if (i > 0) {
					double d1 = tangential_frequency[i - 1];
					d1 = d1 * 10;
					int aux1 = (int) d1;
					d1 = (double) aux1 / 10.0;
					double d2 = d - d1;
					difference[index] = d2;
					index++;
					sum_difference += d2;
					d2 = d2 * 10;
					int aux2 = (int) d2;
					d2 = (double) aux2 / 10.0;
					text.append("" + d + newLine);
				} else {
					text.append("" + d + newLine);
				}
			}
			double average;
			if (v.size() > 1)
				average = (double) sum_difference / (v.size() - 1);
			else
				average = sum_difference;
			double standard_deviation = 0.0;
			for (int i = 0; i < index; i++) {
				standard_deviation += (difference[i] - average)
						* (difference[i] - average);
			}
			if (v.size() > 1)
				standard_deviation = (double) standard_deviation
						/ (v.size() - 1);
			standard_deviation = Math.sqrt(standard_deviation);
			standard_deviation *= 10.0;
			int sd = (int) standard_deviation;
			standard_deviation = (double) sd / 10.0;
			text.append("Standard deviation: " + standard_deviation + newLine);
			if (v.size() > 1)
				sum_difference = (double) sum_difference / (v.size() - 1);
			sum_difference *= 10.0;
			int s_d = (int) sum_difference;
			sum_difference = (double) s_d / 10.0;
			text.append("Average of the differences: " + sum_difference
					+ newLine);
		}

		private void displayAxialFrequencies(Vector<Double> v) {
			text.append("Axial frequencies:" + newLine);
			double[] difference = new double[v.size() - 1];
			int index = 0;
			double[] axial_frequency = new double[v.size()];
			double sum_difference = 0.0;
			for (int i = 0; i < v.size(); i++) {
				double d = v.elementAt(i);
				axial_frequency[i] = d;
			}
			sort(axial_frequency);
			for (int i = 0; i < v.size(); i++) {
				double d = axial_frequency[i];
				d = d * 10;
				int aux = (int) d;
				d = (double) aux / 10.0;
				if (i > 0) {
					double d1 = axial_frequency[i - 1];
					d1 = d1 * 10;
					int aux1 = (int) d1;
					d1 = (double) aux1 / 10.0;
					double d2 = d - d1;
					difference[index] = d2;
					index++;
					sum_difference += d2;
					d2 = d2 * 10;
					int aux2 = (int) d2;
					d2 = (double) aux2 / 10.0;
					text.append("" + d + newLine);
				} else {
					text.append("" + d + newLine);
				}
			}
			double average;
			if (v.size() > 1)
				average = (double) sum_difference / (v.size() - 1);
			else
				average = sum_difference;
			double standard_deviation = 0.0;
			for (int i = 0; i < index; i++) {
				standard_deviation += (difference[i] - average)
						* (difference[i] - average);
			}
			if (index > 1)
				standard_deviation = (double) standard_deviation
						/ (v.size() - 1);
			standard_deviation = Math.sqrt(standard_deviation);
			standard_deviation *= 10.0;
			int sd = (int) standard_deviation;
			standard_deviation = (double) sd / 10.0;
			text.append("Standard deviation: " + standard_deviation + newLine);
			if (v.size() > 1)
				sum_difference = (double) sum_difference / (v.size() - 1);
			sum_difference *= 10.0;
			int s_d = (int) sum_difference;
			sum_difference = (double) s_d / 10.0;
			text.append("Average of the differences: " + sum_difference
					+ newLine);
		}

		private void sort(double[] d) {
			for (int i = 0; i < d.length; i++) {
				int index = i;
				double min = d[i];
				for (int j = i + 1; j < d.length; j++) {
					if (d[j] < min) {
						index = j;
						min = d[j];
					}
				}
				if (index != i) {
					double aux = d[i];
					d[i] = d[index];
					d[index] = aux;
				}
			}
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
		shell = new Shell(d);

		shell.setText("Resonance Frequency");

		shell.setLayout(new GridLayout(1, false));
		GridData gridData;

		Group g = new Group(shell, SWT.SHADOW_ETCHED_IN);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		g.setLayoutData(gridData);
		g.setLayout(new GridLayout(1, false));
		g.setText("Choose dimensions in meters");

		Composite c = new Composite(g, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.CENTER;
		c.setLayoutData(gridData);
		c.setLayout(new GridLayout(7, false));

		width = newText(c, "Width", "0");
		length = newText(c, "Length", "0");
		height = newText(c, "Height", "0");

		compute = new Button(c, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalAlignment = GridData.CENTER;
		compute.setLayoutData(gridData);
		compute.setText("Compute");
		compute.addSelectionListener(computeListener);

		g = new Group(shell, SWT.SHADOW_ETCHED_IN);
		gridData = new GridData(GridData.FILL_BOTH);
		g.setLayoutData(gridData);
		g.setLayout(new GridLayout(2, false));

		text = new Text(g, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gridData = new GridData(GridData.FILL_VERTICAL);
		gridData.widthHint = 220;
		gridData.horizontalAlignment = GridData.END;
		text.setLayoutData(gridData);
		text.setEditable(false);

		Composite g2 = new Composite(g, SWT.NONE);
		gridData = new GridData(GridData.FILL_BOTH);
		g2.setLayoutData(gridData);
		g2.setLayout(new GridLayout(1, false));

		lAxial = new Label(g2, SWT.NONE);
		lAxial.setText("Axial");
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.CENTER);
		lAxial.setLayoutData(gridData);
		hAxial = new Histogram(g2, SWT.NONE, "Hz", "", histLabels);
		hAxial.setIntermediateTicks(4);
		hAxial.setBarWidth(1);
		hAxial.setXMax(300);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 140;
		gridData.widthHint = 400;
		hAxial.setLayoutData(gridData);

		lTangential = new Label(g2, SWT.NONE);
		lTangential.setText("Tangential");
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.CENTER);
		lTangential.setLayoutData(gridData);
		hTangential = new Histogram(g2, SWT.NONE, "Hz", "", histLabels);
		hTangential.setIntermediateTicks(4);
		hTangential.setBarWidth(1);
		hTangential.setXMax(300);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 140;
		gridData.widthHint = 400;
		hTangential.setLayoutData(gridData);

		lOblique = new Label(g2, SWT.NONE);
		lOblique.setText("Oblique");
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.CENTER);
		lOblique.setLayoutData(gridData);
		hOblique = new Histogram(g2, SWT.NONE, "Hz", "", histLabels);
		hOblique.setIntermediateTicks(4);
		hOblique.setBarWidth(1);
		hOblique.setXMax(300);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 140;
		gridData.widthHint = 400;
		hOblique.setLayoutData(gridData);

		inputErrorDialog = new MessageBox(shell, SWT.ICON_ERROR);
		inputErrorDialog.setMessage("Please check the input data.");

		shell.pack();
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