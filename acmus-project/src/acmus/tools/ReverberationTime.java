/*
 *  ReverberationTime.java
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
 * Created on Jun 23, 2006
 */
package acmus.tools;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;
import acmus.util.MathUtils;

/**
 * @author lku
 * 
 */
public class ReverberationTime extends Composite {

	Shell shell;

	Text _temperature;
	Text _pressure;
	Text _humidity;
	Text _volume;

	Composite _surfacesPanel;
	ScrolledComposite _scrolledSurfaces;
	Button _addSurface;
	Button _removeSurface;
	List<Surface> _s;

	Composite _objectsPanel;
	ScrolledComposite _scrolledObjects;
	List<Obj> _o;
	Button _addObject;
	Button _removeObject;

	Combo _method;
	Text _output;
	String[] _methods = { "Sabine", "Millington", "Eyring" };

	String[] _methodsHelpText = {
			"The Sabine model should be used when:\n"
					+ "* the average absorption coefficient is high (more than 0,25);\n"
					+ "* the absorbing materials are uniformly distributed;\n"
					+ "* the absorption coefficients are not precise;\n"
					+ "* there is no need for high acccuracy for the results.",

			"The Millington model should be used when:\n"
					+ "* the absorbing materials are not uniformly distributed;\n"
					+ "* the surfaces are not large;\n"
					+ "* no surface has high absorption;\n"
					+ "* the absorption oefficients are known with good accuracy;\n"
					+ "* it is important to calculate the reverberation time with precision.\n",

			"The Eyring model should be used when:\n"
					+ "* the absorbing materials are uniformly distributed;\n"
					+ "* the absorption coefficients are known with good accuracy;\n"
					+ "* it is important to calculate the reverberation time with precision." };

	Button _compute;
	Button _removeResult;
	Button _export;

	double _time[] = new double[6];

	private Map<String, Map<String, double[]>> _surfCoefs;
	String[] _surfCoefClasses;
	String[][] _surfCoefNames;

	private Map<String, Map<String, double[]>> _objCoefs;
	String[] _objCoefClasses;
	String[][] _objCoefNames;

	static int[] freqs = { 125, 250, 500, 1000, 2000, 4000 };

	private Histogram histogram;
	static String[] histLabels = { "125", "250", "500", "1000", "2000", "4000",
			"" };
	static double[] histDataX = { 1, 2, 3, 4, 5, 6 };

	FileDialog _outputFileDialog;

	/** ************************************************************************ */

	public ReverberationTime(Shell shell, Composite parent, int style,
			Map<String, Map<String, double[]>> surfaceCoefs,
			Map<String, Map<String, double[]>> objectCoefs) {

		super(parent, style);

		_outputFileDialog = new FileDialog(shell, SWT.SAVE);
		_outputFileDialog.setText("Choose output file");
		_outputFileDialog.setFileName("graph-rt60.ps");
		_outputFileDialog.setFilterPath(AcmusPlugin.getDefault().WORKSPACE_DIR);

		_surfCoefs = surfaceCoefs;
		_objCoefs = objectCoefs;

		Set<String> keys = _surfCoefs.keySet();
		_surfCoefClasses = new String[keys.size()];
		keys.toArray(_surfCoefClasses);

		_surfCoefNames = new String[_surfCoefClasses.length][];

		for (int i = 0; i < _surfCoefClasses.length; i++) {
			keys = _surfCoefs.get(_surfCoefClasses[i]).keySet();
			_surfCoefNames[i] = new String[keys.size()];
			keys.toArray(_surfCoefNames[i]);
		}

		keys = _objCoefs.keySet();
		_objCoefClasses = new String[keys.size()];
		keys.toArray(_objCoefClasses);

		_objCoefNames = new String[_objCoefClasses.length][];

		for (int i = 0; i < _objCoefClasses.length; i++) {
			keys = _objCoefs.get(_objCoefClasses[i]).keySet();
			_objCoefNames[i] = new String[keys.size()];
			keys.toArray(_objCoefNames[i]);
		}

		_s = new ArrayList<Surface>();
		_o = new ArrayList<Obj>();

		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		setLayout(gridLayout);

		Label l;
		GridData gridData;
		Group g;

		// -------------------------------------------------------------

		g = new Group(this, SWT.SHADOW_ETCHED_IN);
		g.setText("Parameters");
		g.setLayout(new GridLayout(1, false));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		g.setLayoutData(gridData);

		Composite c = new Composite(g, SWT.NONE);
		c.setLayout(AcmusGraphics.newNoMarginGridLayout(10, false));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		c.setLayoutData(gridData);

		l = new Label(c, SWT.NONE);
		l.setText("Temperature (\u00b0C):");
		_temperature = new Text(c, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_temperature.setLayoutData(gridData);
		_temperature.setText("30");

		l = new Label(c, SWT.NONE);
		l.setText("Pressure (kPa):");
		_pressure = new Text(c, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_pressure.setLayoutData(gridData);
		_pressure.setText("100");

		l = new Label(c, SWT.NONE);
		l.setText("Humidity (%):");
		_humidity = new Text(c, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_humidity.setLayoutData(gridData);
		_humidity.setText("60");

		l = new Label(c, SWT.NONE);
		l.setText("Volume (m\u00b3):");
		_volume = new Text(c, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_volume.setLayoutData(gridData);
		_volume.setText("200");

		l = new Label(c, SWT.NONE);
		l.setText("Method:");
		_method = new Combo(c, SWT.NONE);
		_method.setItems(_methods);
		_method.select(0);

		_method.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				_output.setText(_methodsHelpText[_method.getSelectionIndex()]);
			}
		});

		// -------------------------------------------------------------
		SashForm sf = new SashForm(this, SWT.VERTICAL);
		gridData = new GridData(GridData.FILL_BOTH);
		sf.setLayoutData(gridData);

		// -------------------------------------------------------------

		g = new Group(sf, SWT.SHADOW_ETCHED_IN);
		g.setText("Surfaces");
		g.setLayout(new GridLayout(2, false));
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		g.setLayoutData(gridData);

		_scrolledSurfaces = new ScrolledComposite(g, SWT.V_SCROLL
				| SWT.H_SCROLL);
		_scrolledSurfaces.setExpandHorizontal(true);
		_scrolledSurfaces.setExpandVertical(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		_scrolledSurfaces.setLayoutData(gridData);

		_surfacesPanel = new Composite(_scrolledSurfaces, SWT.NONE);
		_scrolledSurfaces.setContent(_surfacesPanel);
		_surfacesPanel.setLayout(new GridLayout(10, false));

		l = new Label(_surfacesPanel, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = 15;
		l.setLayoutData(gridData);
		l = new Label(_surfacesPanel, SWT.NONE);
		l.setText("Area (m\u00b2):");
		// gridData = new GridData();
		// gridData.widthHint = 35;
		// l.setLayoutData(gridData);
		l = new Label(_surfacesPanel, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		l.setLayoutData(gridData);
		l.setText("Material:");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		l.setLayoutData(gridData);
		l = new Label(_surfacesPanel, SWT.NONE);
		l.setText("");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		l.setLayoutData(gridData);
		for (int i = 0; i < 6; i++) {
			l = new Label(_surfacesPanel, SWT.NONE);
			l.setText("\u03b1" + histLabels[i] + ":");
			gridData = new GridData();
			gridData.widthHint = 45;
			l.setLayoutData(gridData);
		}

		Surface s = new Surface(_surfacesPanel, _s.size() + 1,
				_surfCoefClasses, _surfCoefNames, _surfCoefs);
		_s.add(s);

		_scrolledSurfaces.setMinSize(_surfacesPanel.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		_addSurface = new Button(g, SWT.NONE);
		_addSurface.setText("Add");
		_addSurface.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addSurface();
			}
		});

		_removeSurface = new Button(g, SWT.NONE);
		_removeSurface.setText("Remove");
		_removeSurface.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeSurface();
			}
		});

		// -------------------------------------------------------------

		g = new Group(sf, SWT.SHADOW_ETCHED_IN);
		g.setText("Objects");
		g.setLayout(new GridLayout(2, false));
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		g.setLayoutData(gridData);

		_scrolledObjects = new ScrolledComposite(g, SWT.V_SCROLL | SWT.H_SCROLL);
		_scrolledObjects.setExpandHorizontal(true);
		_scrolledObjects.setExpandVertical(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		_scrolledObjects.setLayoutData(gridData);

		_objectsPanel = new Composite(_scrolledObjects, SWT.NONE);
		_scrolledObjects.setContent(_objectsPanel);
		_objectsPanel.setLayout(new GridLayout(10, false));

		l = new Label(_objectsPanel, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = 15;
		l.setLayoutData(gridData);
		l = new Label(_objectsPanel, SWT.NONE);
		l.setText("Amount:");
		// gridData = new GridData();
		// gridData.widthHint = 35;
		// l.setLayoutData(gridData);
		l = new Label(_objectsPanel, SWT.NONE);
		l.setText("Object:");
		l = new Label(_objectsPanel, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		l.setLayoutData(gridData);
		l.setText("");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		l.setLayoutData(gridData);
		for (int i = 0; i < 6; i++) {
			l = new Label(_objectsPanel, SWT.NONE);
			l.setText("\u03b1" + histLabels[i] + ":");
			gridData = new GridData();
			gridData.widthHint = 45;
			l.setLayoutData(gridData);
		}

		// Obj o = new Obj(_objectsPanel,_o.size()+1);
		// _o.add(o);

		_scrolledObjects.setMinSize(_objectsPanel.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		_addObject = new Button(g, SWT.NONE);
		_addObject.setText("Add");
		_addObject.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			}
		});

		_removeObject = new Button(g, SWT.NONE);
		_removeObject.setText("Remove");
		_removeObject.setEnabled(false);
		_removeObject.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeObject();
			}
		});

		// -------------------------------------------------------------

		SashForm sf2 = new SashForm(sf, SWT.HORIZONTAL);
		gridData = new GridData(GridData.FILL_BOTH);
		sf2.setLayoutData(gridData);

		// -------------------------------------------------------------

		g = new Group(sf2, SWT.SHADOW_ETCHED_IN);
		g.setText("Results");
		g.setLayout(new GridLayout(2, false));
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 100;
		g.setLayoutData(gridData);

		_compute = new Button(g, SWT.NONE);
		_compute.setText("Compute");
		_compute.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				compute();
			}
		});

		_removeResult = new Button(g, SWT.NONE);
		_removeResult.setText("Remove");
		_removeResult.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// histogram.clearHold();
			}
		});

		createTableViewer(g);

		// -------------------------------------------------------------

		g = new Group(sf2, SWT.SHADOW_ETCHED_IN);
		g.setText("Graph");
		g.setLayout(new GridLayout(1, false));
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 100;
		g.setLayoutData(gridData);

		_export = new Button(g, SWT.NONE);
		_export.setText("Export");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.END;
		_export.setLayoutData(gridData);
		_export.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				export();
			}
		});

		g = new Group(g, SWT.SHADOW_NONE);
		g.setLayout(AcmusGraphics.newNoMarginGridLayout(1, false));
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 6;
		g.setLayoutData(gridData);
		g.setBackground(AcmusGraphics.WHITE);

		histogram = new Histogram(g, SWT.NONE, "Hz ", "time(s)", histLabels);
		gridData = new GridData(GridData.FILL_BOTH);
		// gridData.heightHint = 180;
		histogram.setLayoutData(gridData);

		// -------------------------------------------------------------

		g = new Group(sf2, SWT.SHADOW_ETCHED_IN);
		g.setText("Output");
		g.setLayout(new GridLayout(1, false));
		gridData = new GridData(GridData.FILL_BOTH);
		g.setLayoutData(gridData);

		c = new Composite(g, SWT.NONE);
		c.setLayout(AcmusGraphics.newNoMarginGridLayout(2, false));
		gridData = new GridData(GridData.FILL_BOTH);
		c.setLayoutData(gridData);

		_output = new Text(c, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		// _output = new Text(c, SWT.BORDER | SWT.MULTI | SWT.WRAP |
		// SWT.READ_ONLY
		// | SWT.V_SCROLL);
		_output.setBackground(this.getBackground());
		_output.setFont(AcmusGraphics.FIXED_FONT);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		_output.setLayoutData(gridData);
		_output.setText(_methodsHelpText[0]);

		int[] sfWeights = { 2, 5, 3 };
		sf2.setWeights(sfWeights);

	}

	private void addSurface() {
		Surface s = new Surface(_surfacesPanel, _s.size() + 1,
				_surfCoefClasses, _surfCoefNames, _surfCoefs);
		_s.add(s);
		_scrolledSurfaces.setMinSize(_surfacesPanel.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
		_surfacesPanel.layout();
		_removeSurface.setEnabled(true);
	}

	private void removeSurface() {
		if (_s.size() > 0) {
			Surface s = _s.get(_s.size() - 1);
			_s.remove(_s.size() - 1);
			s.remove();
			_scrolledSurfaces.setMinSize(_surfacesPanel.computeSize(
					SWT.DEFAULT, SWT.DEFAULT));
			_surfacesPanel.layout();
			if (_s.size() <= 0)
				_removeSurface.setEnabled(false);
		}
	}

	public void addObject() {
		Obj o = new Obj(_objectsPanel, _o.size() + 1, _objCoefClasses,
				_objCoefNames, _objCoefs);
		_o.add(o);
		_scrolledObjects.setMinSize(_objectsPanel.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
		_objectsPanel.layout();
		_removeObject.setEnabled(true);
	}

	public void removeObject() {
		if (_o.size() > 0) {
			Obj o = _o.get(_o.size() - 1);
			_o.remove(_o.size() - 1);
			o.remove();
			_scrolledObjects.setMinSize(_objectsPanel.computeSize(SWT.DEFAULT,
					SWT.DEFAULT));
			_objectsPanel.layout();
			if (_o.size() <= 0)
				_removeObject.setEnabled(false);
		}
	}

	public static double airAbsorption(double freq, double temp,
			double humidity, double pressure, Text output) {
		double Pso = 101325;
		double Tt = 273.16;
		double T = 273.15 + temp;
		double T20 = 293.15;
		double X;
		double h;
		double Fro;
		double Frn;
		double alphaO, alphaN, alphaF, alphaNp;

		X = 10.79586 * (1 - Tt / T) - 5.02808 * MathUtils.log10(T / Tt) + 1.50474e-4
				* (1 - Math.pow(10, -8.29692 * (T / Tt - 1))) + 0.42873e-3
				* (-1 + Math.pow(10, 4.76955 * (1 - Tt / T))) - 2.2195983;

		h = humidity * (Pso / pressure) * Math.pow(10, X);

		Fro = pressure / Pso * (24 + 4.41e4 * h * ((0.05 + h) / (0.391 + h)));
		Frn = pressure
				/ Pso
				* Math.sqrt(T20 / T)
				* (9 + 350 * h
						* Math.exp(-6.142 * (Math.pow(T20 / T, 1.0 / 3) - 1)));

		alphaO = 0.01275 * freq * freq * Math.pow(T20 / T, 2.5)
				* (Math.exp(-2239.1 / T) / (Fro + freq * freq / Fro));
		alphaN = 0.1068 * freq * freq * Math.pow(T20 / T, 2.5)
				* (Math.exp(-3352 / T) / (Frn + freq * freq / Frn));

		alphaF = 1.84e-11 * freq * freq * (Pso / pressure) * Math.sqrt(T / T20);
		alphaNp = alphaF + alphaO + alphaN;

		output.append("       X = " + X + "\n");
		output.append(" alpha_O = " + alphaO + "\n");
		output.append(" alpha_N = " + alphaN + "\n");
		output.append(" alpha_F = " + alphaF + "\n");
		output.append("alpha_NP = " + alphaNp + "\n");
		return alphaNp;
	}

	private void printSurfaces() {
		if (_s.size() > 0) {
			_output.append("Surfaces:\n");
			for (int i = 0; i < _s.size(); i++) {
				Surface s = _s.get(i);
				_output.append(" " + (i + 1) + ") " + s.getMaterial() + ":\n");
				_output.append("\tarea: " + s.area() + "m\u00b2\n \t\u03b1: ");
				for (int j = 0; j < 6; j++) {
					_output.append(s.coefficient(j) + " ");
				}
				_output.append("\n");
			}
			_output.append("\n");
		}
		if (_o.size() > 0) {
			_output.append("Objects:\n");
			for (int i = 0; i < _o.size(); i++) {
				Obj o = _o.get(i);
				_output.append(" " + (i + 1) + ") " + o.getMaterial() + ":\n");
				_output.append("\tamount: " + o.amount() + "\n \t\u03b1: ");
				for (int j = 0; j < 6; j++) {
					_output.append(o.coefficient(j) + " ");
				}
				_output.append("\n");
			}
			_output.append("\n");
		}
	}

	private void export() {
		String filename = _outputFileDialog.open();
		if (filename != null) {
			try {
				histogram.exportPs(new PrintStream(new FileOutputStream(
						filename)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void compute() {
		double volume = Double.parseDouble(_volume.getText());
		double temp = Double.parseDouble(_temperature.getText());
		double humidity = Double.parseDouble(_humidity.getText());
		double pressure = 1000 * Double.parseDouble(_pressure.getText());

		if (_method.getSelectionIndex() == 0) {
			// Sabine
			_output.setText(_methodsHelpText[0] + "\n\n");
			printSurfaces();
			for (int i = 0; i < _time.length; i++) {
				double totalAbs = 0;
				for (Surface s : _s) {
					totalAbs += s.coefficient(i) * s.area();
				}
				for (Obj o : _o) {
					totalAbs += o.coefficient(i) * o.amount();
				}
				double alphaNp = airAbsorption(freqs[i], temp, humidity,
						pressure, _output);
				double air = 8 * alphaNp * volume;
				_time[i] = 0.161 * volume / (totalAbs + air);
				_output.append(freqs[i] + "Hz:\n");
				_output.append("   A_air = " + air + "\n");
				_output.append("    RT60 = " + _time[i] + "\n");
				_output.append("\n");
			}
		} else if (_method.getSelectionIndex() == 1) {
			// Millington
			_output.setText(_methodsHelpText[1] + "\n\n");
			printSurfaces();
			for (int i = 0; i < _time.length; i++) {
				double totalAbs = 0;
				for (Surface s : _s) {
					totalAbs += s.area() * Math.log(1 - s.coefficient(i));
				}
				for (Obj o : _o) {
					totalAbs += o.amount() * Math.log(1 - o.coefficient(i));
				}
				double alphaNp = airAbsorption(freqs[i], temp, humidity,
						pressure, _output);
				double air = 8 * alphaNp * volume;
				_time[i] = -0.161 * volume / (totalAbs + air);
				_output.append(freqs[i] + "Hz:\n");
				_output.append("   A_air = " + air + "\n");
				_output.append("    RT60 = " + _time[i] + "\n");
				_output.append("\n");
			}
		} else {
			// Eyring
			_output.setText(_methodsHelpText[2] + "\n\n");
			printSurfaces();
			for (int i = 0; i < _time.length; i++) {
				double alpham = 0;
				double totalArea = 0;
				for (Surface s : _s) {
					alpham += s.area() * s.coefficient(i);
					totalArea += s.area();
				}
				for (Obj o : _o) {
					alpham += o.amount() * o.coefficient(i);
					totalArea += o.amount();
				}
				double alphaNp = airAbsorption(freqs[i], temp, humidity,
						pressure, _output);
				double air = 8 * alphaNp * volume;
				alpham = alpham / totalArea;
				_time[i] = -0.161 * volume
						/ ((totalArea * Math.log(1 - alpham)) + air);
				_output.append(freqs[i] + "Hz:\n");
				_output.append("   A_air = " + air + "\n");
				_output.append("    RT60 = " + _time[i] + "\n");
				_output.append("\n");
			}
		}

		double max = _time[0];
		double dataY[] = new double[6];
		DecimalFormat format = new DecimalFormat("#.##");
		String topLabels[] = new String[6];
		for (int i = 0; i < 6; i++) {
			dataY[i] = _time[i];
			topLabels[i] = format.format(dataY[i]);
			if (_time[i] > max)
				max = _time[i];
		}
		// if (max == 0.0)
		// inputErrorDialog.open();
		histogram.setData(histDataX, dataY, topLabels);

		_output.append("Graph:\n");
		for (int i = 0; i < 6; i++) {
			_output.append(histLabels[i] + "\t" + _time[i] + "\n");
		}
		_output.append("\n");

		Object o = new TableLine("" + (_table.getItemCount() + 1),
				getColor(_table.getItemCount()));
		_tViewer.add(o);
		// _tViewer.setChecked(o, true);

	}

	private Color getColor(int i) {
		return AcmusGraphics.COMP_COLORS[i % AcmusGraphics.COMP_COLORS.length];
	}

	public static double[] parseCoeficients(String str) {
		StringTokenizer st = new StringTokenizer(str, ";");

		double[] res = new double[st.countTokens()];
		for (int i = 0; i < res.length; i++) {
			res[i] = Double.parseDouble(st.nextToken());
		}
		return res;
	}

	/* ========================================================================= */

	private Table _table;
	private TableViewer _tViewer;

	// Set column names
	private String[] columnNames = new String[] { "name", "color" };

	private void createTableViewer(Composite parent) {
		_table = new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		_table.setLinesVisible(false);
		_table.setHeaderVisible(true);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		_table.setLayoutData(gridData);

		_tViewer = new TableViewer(_table);
		_tViewer.setLabelProvider(new PositionLabelProvider());
		_tViewer.setColumnProperties(columnNames);

		TableColumn tc = new TableColumn(_table, SWT.LEFT);
		tc.setResizable(true);
		tc.setWidth(70);
		tc.setText(columnNames[0]);

		tc = new TableColumn(_table, SWT.LEFT);
		tc.setResizable(true);
		tc.setWidth(40);
		tc.setText(columnNames[1]);
	}

	class PositionLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex != 1)
				return null;
			TableLine t = (TableLine) element;
			return createRectangle(20, 10, t.color);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex != 0)
				return null;
			TableLine t = (TableLine) element;
			return t.name;
		}

		private Image createRectangle(int width, int height, Color c) {
			Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
			Image img = new Image(d, width, height);
			GC gc = new GC(img);
			gc.setBackground(c);
			gc.fillRectangle(0, 0, width, height);
			return img;
		}
	}

	class TableLine {
		String name;
		Color color;

		public TableLine(String name, Color color) {
			this.name = name;
			this.color = color;
		}
	}

}

// ============================================================================
class Coeficients {

	Text[] _coefs;

	public Coeficients(Composite parent, int style) {
		int n = 6;

		_coefs = new Text[n];

		for (int i = 0; i < n; i++) {
			_coefs[i] = new Text(parent, SWT.BORDER);
			_coefs[i].setText("0.001");
			GridData gridData = new GridData();
			gridData.widthHint = 35;
			_coefs[i].setLayoutData(gridData);
		}
	}

	public double coeficient(int i) {
		return Double.parseDouble(_coefs[i].getText());
	}

	public void remove() {
		for (Control c : _coefs) {
			GridData gridData = (GridData) c.getLayoutData();
			gridData.exclude = true;
			c.setVisible(false);
		}
	}

	public void set(double[] c) {
		for (int i = 0; i < c.length; i++)
			_coefs[i].setText(c[i] + "");
	}

}

class Surface {

	int _id;
	Text _area;
	Combo _materialClass;
	Combo _material;
	Coeficients _alpha;
	List<Control> comps;
	String[] _classes;
	String[][] _materials;
	Map<String, Map<String, double[]>> _coefs;

	public Surface(Composite parent, int id, String[] classes,
			String[][] materials, Map<String, Map<String, double[]>> coefs) {
		_classes = classes;
		_materials = materials;
		_coefs = coefs;

		comps = new ArrayList<Control>();
		_id = id;

		GridData gridData;

		Label l = new Label(parent, SWT.NONE);
		l.setText(_id + ")");
		comps.add(l);

		_area = new Text(parent, SWT.BORDER);
		comps.add(_area);
		gridData = new GridData();
		gridData.widthHint = 35;
		_area.setLayoutData(gridData);
		_area.setText("10");

		_materialClass = new Combo(parent, SWT.BORDER);
		comps.add(_materialClass);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 100;
		_materialClass.setLayoutData(gridData);
		_materialClass.setItems(_classes);
		_materialClass.select(0);
		_material = new Combo(parent, SWT.BORDER);
		comps.add(_material);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 100;
		_material.setLayoutData(gridData);
		_material.setItems(_materials[0]);
		_material.select(0);

		_materialClass.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				_material.setItems(_materials[_materialClass
						.getSelectionIndex()]);
				_material.select(0);
				update();
			}
		});

		_material.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		_alpha = new Coeficients(parent, SWT.NONE);
		update();
	}

	public void update() {
		String matClass = _materialClass.getItem(_materialClass
				.getSelectionIndex());
		String mat = _material.getItem(_material.getSelectionIndex());
		double[] c = _coefs.get(matClass).get(mat);
		_alpha.set(c);
	}

	public void remove() {
		for (Control c : comps) {
			GridData gridData = (GridData) c.getLayoutData();
			gridData.exclude = true;
			c.setVisible(false);
		}
		_alpha.remove();
	}

	public double area() {
		return Double.parseDouble(_area.getText());
	}

	public double coefficient(int i) {
		return _alpha.coeficient(i);
	}

	public String getMaterial() {
		return _material.getItem(_material.getSelectionIndex());
	}
}

class Obj extends Surface {

	public Obj(Composite parent, int id, String[] classes,
			String[][] materials, Map<String, Map<String, double[]>> coefs) {
		super(parent, id, classes, materials, coefs);
		_area.setText("1");
	}

	public double amount() {
		return Double.parseDouble(_area.getText());
	}
}
