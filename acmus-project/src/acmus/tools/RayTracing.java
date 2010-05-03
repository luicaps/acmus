/*
 *  RayTracing.java
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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.jfree.experimental.chart.swt.ChartComposite;

import acmus.AcmusApplication;
import acmus.graphics.ChartBuilder;
import acmus.simulation.AcousticSource;
import acmus.simulation.GeometricAcousticSimulation;
import acmus.simulation.Receptor;
import acmus.simulation.math.Vector;
import acmus.simulation.rtt.RayTracingGeometricAcousticSimulationImpl;
import acmus.simulation.rtt.Sector;
import acmus.simulation.structures.MonteCarloAcousticSource;
import acmus.simulation.structures.SphericalReceptor;
import acmus.util.ArrayUtils;
import acmus.util.WaveUtils;

/**
 * @author mhct
 * @author vgp
 */
public class RayTracing extends Composite {

	static final int K = 1000;

	// GUI variables
	private Label label;
	private Text _input;
	private Button compute;
	// Algorithm variables
	private static double v_som;
	private static int taxa;

	// Todos os campos devem ficar aqui para serem usados no calculo realizado
	// por compute()
	private Text soundSpeed;

	private Spinner sourceX;
	private Spinner sourceY;
	private Spinner sourceZ;
	private Spinner receiverX;
	private Spinner receiverY;
	private Spinner receiverZ;
	private Spinner radius;
	private Spinner width;
	private Spinner height;
	private Spinner length;
	private Spinner floorCoeficient;
	private Spinner ceilCoeficient;
	private Spinner wallsCoeficients;
	private Spinner rays;
	private Text soundAtenuation;
	private ChartComposite chart;
	private Button saveIr;
	private FileDialog fileDialog;
	private Map<Float, Float> histogram;
	private ProgressBar progressBar;
	private Combo impulseResponse;

	public RayTracing(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(10, false));

		// Impulsive response

		// Source position
		// WALLS definition

		Group walls = new Group(this, SWT.None);
		walls.setText("Room Definition");
		walls.setLayout(new GridLayout(6, false));
		GridData wallsGrid = new GridData(GridData.FILL_HORIZONTAL);
		wallsGrid.horizontalSpan = 10;
		walls.setLayoutData(wallsGrid);

		label = new Label(walls, SWT.None);
		label.setText("Room Width: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		width = new Spinner(walls, SWT.None);
		setSpinner(width, 2, 10000, 100);
		setGridData(width, SWT.LEAD, SWT.CENTER, 1, 40);

		label = new Label(walls, SWT.NONE);
		label.setText("Room Length: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		length = new Spinner(walls, SWT.None);
		setSpinner(length, 2, 10000, 100);
		setGridData(length, SWT.LEAD, SWT.CENTER, 1, 40);

		label = new Label(walls, SWT.NONE);
		label.setText("Room Height: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		height = new Spinner(walls, SWT.None);
		setSpinner(height, 2, 10000, 100);
		setGridData(height, SWT.LEAD, SWT.CENTER, 1, 40);

		label = new Label(this, SWT.LEAD);
		label.setText("Frequency Band (Hz): ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		impulseResponse = new Combo(this, SWT.READ_ONLY);
		impulseResponse.setItems(new String[] { "32 to 512", "512 to 2048",
				"2048 to 8192", "8192 to 16000" });
		impulseResponse.select(0);
		impulseResponse.setFocus();
		setGridData(impulseResponse, SWT.LEAD, SWT.CENTER, 1, 150);

		label = new Label(this, SWT.NONE);
		label.setText("Source position: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		sourceX = new Spinner(this, SWT.NONE);
		setSpinner(sourceX, 2, 100, 0);
		setGridData(sourceX, SWT.LEAD, SWT.CENTER, 1);

		sourceY = new Spinner(this, SWT.NONE);
		setSpinner(sourceY, 2, 100, 0);
		setGridData(sourceY, SWT.LEAD, SWT.CENTER, 1);

		sourceZ = new Spinner(this, SWT.NONE);
		setSpinner(sourceZ, 2, 100, 0);
		setGridData(sourceZ, SWT.LEAD, SWT.CENTER, 1);

		// Empty space to fit layout...

		label = new Label(this, SWT.NONE);
		label.setText("         ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		label = new Label(this, SWT.NONE);
		label.setText("         ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 3);

		// Speed of Sound

		label = new Label(this, SWT.NONE);
		label.setText("Speed of sound (m/s): ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		soundSpeed = new Text(this, SWT.NONE);
		soundSpeed.setText("344.00"); // standard speed of sound
		setGridData(soundSpeed, SWT.LEAD, SWT.CENTER, 1, 60);

		// spherical receiver position

		label = new Label(this, SWT.NONE);
		label.setText("Spherical Receiver: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		receiverX = new Spinner(this, SWT.NONE);
		setSpinner(receiverX, 2, 100, 0);
		setGridData(receiverX, SWT.LEAD, SWT.CENTER, 1);

		receiverY = new Spinner(this, SWT.NONE);
		setSpinner(receiverY, 2, 100, 0);
		setGridData(receiverY, SWT.LEAD, SWT.CENTER, 1);

		receiverZ = new Spinner(this, SWT.NONE);
		setSpinner(receiverZ, 2, 100, 0);
		setGridData(receiverZ, SWT.LEAD, SWT.CENTER, 5);

		length.addModifyListener(new RoomSizeModifyListener(length, sourceY,
				receiverY));
		width.addModifyListener(new RoomSizeModifyListener(width, sourceX,
				receiverX));
		height.addModifyListener(new RoomSizeModifyListener(height, sourceZ,
				receiverZ));

		// Sound's atenuation on air
		label = new Label(this, SWT.NONE);
		label.setText("Sound's atenuation on air: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		soundAtenuation = new Text(this, SWT.NONE);
		soundAtenuation.setText("0.01"); // default value
		setGridData(soundAtenuation, SWT.LEAD, SWT.CENTER, 1, 60);

		// Espherical receiver's radius

		label = new Label(this, SWT.NONE);
		label.setText("Esferic receiver's radius: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		radius = new Spinner(this, SWT.NONE);
		setSpinner(radius, 2, 50, 0);
		setGridData(radius, SWT.LEAD, SWT.CENTER, 7, 40);

		// Number of rays
		label = new Label(this, SWT.NONE);
		label.setText("Number of rays: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		rays = new Spinner(this, SWT.NONE);
		setSpinner(rays, 0, Integer.MAX_VALUE, 0);
		setGridData(rays, SWT.LEAD, SWT.CENTER, 9, 40);

		// Coeficients
		Group coefficients = new Group(this, SWT.None);
		coefficients.setText("Acoustic Coefficients");
		coefficients.setLayout(new GridLayout(6, false));
		GridData coefficientsGrid = new GridData(GridData.FILL_HORIZONTAL);
		coefficientsGrid.horizontalSpan = 8;
		coefficients.setLayoutData(coefficientsGrid);
		label = new Label(coefficients, SWT.NONE);
		label.setText("Floor coefficient: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		floorCoeficient = new Spinner(coefficients, SWT.NONE);
		setSpinner(floorCoeficient, 2, 100, 0);
		setGridData(floorCoeficient, SWT.LEAD, SWT.CENTER, 1, 40);

		label = new Label(coefficients, SWT.NONE);
		label.setText("Ceil Coefficient: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		ceilCoeficient = new Spinner(coefficients, SWT.NONE);
		setSpinner(ceilCoeficient, 2, 100, 0);
		setGridData(ceilCoeficient, SWT.LEAD, SWT.CENTER, 1, 40);

		label = new Label(coefficients, SWT.NONE);
		label.setText("Walls Coefficients: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		wallsCoeficients = new Spinner(coefficients, SWT.NONE);
		setSpinner(wallsCoeficients, 2, 100, 0);
		setGridData(wallsCoeficients, SWT.LEAD, SWT.CENTER, 1, 40);

		// Button that trigger the algorithm
		compute = new Button(this, SWT.NONE);
		compute.setText("&Compute");
		setGridData(compute, SWT.LEAD, SWT.CENTER, 1);

		compute.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					compute();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Some Error");
				}
			}
		});

		saveIr = new Button(this, SWT.NONE);
		saveIr.setText("&Save IR");
		setGridData(saveIr, SWT.LEAD, SWT.CENTER, 1);
		saveIr.setEnabled(false);
		saveIr.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					saveIr();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		progressBar = new ProgressBar(this, SWT.SMOOTH);
		setGridData(progressBar, SWT.CENTER, SWT.CENTER, 10);
		fileDialog = new FileDialog(getShell(), SWT.SAVE);
		fileDialog.setFilterExtensions(new String[] { ".wav" });
		fileDialog.setFilterNames(new String[] { "WAV file" });
		fileDialog.setText("Save simulated Impulse Response as WAV");

		chart = new ChartComposite(this, SWT.NONE);
		setGridData(chart, SWT.LEAD, SWT.BOTTOM, 10, 800, 400);
		histogram = new HashMap<Float, Float>();
//		histogram.put(0.0, 0);
		plotChart();
		this.pack();
	}

	private void saveIr() {
		String filename = fileDialog.open();
		if (filename == null)
			return;

		TreeSet<Float> orderedKeySet = new TreeSet<Float>(histogram.keySet());

		int waveLength = (int) Math.ceil(orderedKeySet.last()
				* AcmusApplication.SAMPLE_RATE);

		double[] wave = new double[waveLength];
		for (Float key : orderedKeySet) {
			int i = (int) Math.floor(key * AcmusApplication.SAMPLE_RATE);
			wave[i] = histogram.get(key);
		}
		FileWriter fw = null;
		try {
			String tempFile = System.getProperty("java.io.tmpdir", "/tmp") +
					System.getProperty("file.separator") + "wave.txt";
			fw = new FileWriter(tempFile);

			for (int i = 0; i < wave.length; i++)
				fw.write(wave[i] + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		WaveUtils.wavWrite(ArrayUtils.scaleToMax(wave, WaveUtils.getLimit(16)), filename);
	}

	public void setSpinner(Spinner component, int digits, int maximum,
			int minimum) {
		component.setDigits(digits);
		component.setMaximum(maximum);
		component.setMinimum(minimum);
	}

	public void setGridData(Control component, int horizontalAlign,
			int verticalAlign, int horizontalSpan) {
		setGridData(component, horizontalAlign, verticalAlign, horizontalSpan,
				0);
	}

	public void setGridData(Control component, int horizontalAlign,
			int verticalAlign, int horizontalSpan, int width) {
		setGridData(component, horizontalAlign, verticalAlign, horizontalSpan,
				width, 0);
	}

	public void setGridData(Control component, int horizontalAlign,
			int verticalAlign, int horizontalSpan, int width, int height) {
		GridData gd = new GridData();
		gd.horizontalAlignment = horizontalAlign;
		gd.verticalAlignment = verticalAlign;
		gd.horizontalSpan = horizontalSpan;
		if (width != 0)
			gd.widthHint = width;
		if (height != 0)
			gd.heightHint = height;
		component.setLayoutData(gd);
	}

	public void validate() {
		_input.setBackground(new Color(null, 245, 245, 220));
	}

	public void compute() throws IOException {
		if (rays.getSelection() < 0)
			throw new IOException("Number of rays must be >= 0");

		getDisplay().asyncExec(new Runnable() {
			public void run() {

				List<Sector> sectors = generateSectorsFor();
				Vector soundSourceCenter = newTriadeFor(sourceX, sourceY,
						sourceZ);
				AcousticSource soundSource = new MonteCarloAcousticSource(soundSourceCenter);
				Vector sphericalReceptorCenter = newTriadeFor(receiverX,
						receiverY, receiverZ);
				float sphericalReceptorRadius = getFloatValue(radius);
				Receptor receptor = new SphericalReceptor(sphericalReceptorCenter, sphericalReceptorRadius);
				double speedOfSound = Double.valueOf(soundSpeed.getText());
				double mCoeficient = Double.valueOf(soundAtenuation.getText());
				GeometricAcousticSimulation simulation = new RayTracingGeometricAcousticSimulationImpl(
						sectors, soundSource, rays.getSelection(), receptor,
						speedOfSound, mCoeficient, K);
				progressBar.setSelection(0);
				simulation.simulate(progressBar);
				progressBar.setSelection(100);
				
				histogram = receptor.getSimulatedImpulseResponse().getEnergeticImpulseResponse();
				plotChart();
				saveIr.setEnabled(true);
			}
		});

	}

	private List<Sector> generateSectorsFor() {

		ArrayList<Sector> result = new ArrayList<Sector>();
		float w = getFloatValue(width);
		float h = getFloatValue(height);
		float l = getFloatValue(length);
		result.add(new Sector(new Vector(0, 0, 1), new Vector(l, w, 0),
				getFloatValue(floorCoeficient)));
		result.add(new Sector(new Vector(0, 1, 0), new Vector(l, 0, h),
				getFloatValue(wallsCoeficients)));
		result.add(new Sector(new Vector(1, 0, 0), new Vector(0, w, h),
				getFloatValue(wallsCoeficients)));
		result.add(new Sector(new Vector(0, 0, -1), new Vector(l, w, h),
				getFloatValue(ceilCoeficient)));
		result.add(new Sector(new Vector(0, -1, 0), new Vector(l, w, h),
				getFloatValue(wallsCoeficients)));
		result.add(new Sector(new Vector(-1, 0, 0), new Vector(l, w, h),
				getFloatValue(wallsCoeficients)));
		return result;
	}

	private Vector newTriadeFor(Spinner sourceX, Spinner sourceY,
			Spinner sourceZ) {
		return new Vector(getFloatValue(sourceX), getFloatValue(sourceY),
				getFloatValue(sourceZ));
	}
	
	private float getFloatValue(Spinner sourceX) {
		float ret;

		if (sourceX != null) {
			float base = (float) Math.pow(10, sourceX.getDigits());
			ret = sourceX.getSelection() / base;
		} else
			ret = 0.0f;

		return ret;
	}

	private void plotChart() {
		ChartBuilder builder = new ChartBuilder();
		Map<Double, Double> miliHistogram = new HashMap<Double, Double>();

		double max = 0.0;
		for (Map.Entry<Float, Float> map : histogram.entrySet()) {
			if(map.getValue() > max)
				max = map.getValue();
		}
		
		//normalize histogram
		for (Map.Entry<Float, Float> map : histogram.entrySet()) {
			miliHistogram.put((double)map.getKey(), map.getValue()/max);
		}
		
		chart.setChart(builder.getChart(miliHistogram, "Time (s)", "Energy",
				"Simulated Impulse Response for "
						+ impulseResponse.getText() + " Hz"));
		chart.forceRedraw();
	}

	public static void recordRay(Response resp, double energia, double dist) {
		int indice;
		Response aux, aux2;

		indice = (int) (dist * taxa / v_som);

		aux = resp;
		aux2 = aux;
		while ((aux != null) && (indice > aux.getIndice())) {
			aux2 = aux;
			aux = aux.getProx();
		}
		if (aux.equals(resp)) {
			if ((resp != null) && (indice == resp.getIndice())) {
				resp.setEnergia(resp.getEnergia() + energia);
				resp.setNumraios(resp.getNumraios() + 1);
			} else {
				resp = new Response();
				resp.setEnergia(energia);
				resp.setIndice(indice);
				resp.setNumraios(1);
				resp.setProx(aux);
			}
		} else if ((aux != null) && (indice == aux.getIndice())) {
			aux.setEnergia(resp.getEnergia() + energia);
			aux.setNumraios(resp.getNumraios() + 1);
		} else {
			Response aux3;
			aux = new Response();
			aux3 = aux;
			aux.setEnergia(energia);
			aux.setIndice(indice);
			aux.setNumraios(1);
			aux.setProx(aux3);
			aux2.setProx(aux);
		}
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Geometric Acoustic Simulation Tool");
		shell.setBounds(0, 0, 930, 700);

		shell.open();
		new RayTracing(shell, SWT.NONE);
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
		display.dispose();
	}

	private final class RoomSizeModifyListener implements ModifyListener {
		private final Spinner roomAxis;
		private final Spinner receiverAxis;
		private final Spinner sourceAxis;

		public RoomSizeModifyListener(Spinner roomAxis, Spinner sourceAxis,
				Spinner receiverAxis) {
			this.roomAxis = roomAxis;
			this.sourceAxis = sourceAxis;
			this.receiverAxis = receiverAxis;
		}

		public void modifyText(ModifyEvent e) {
			receiverAxis.setMaximum(roomAxis.getSelection());
			sourceAxis.setMaximum(roomAxis.getSelection());
			int min = Integer.MAX_VALUE;
			if (min > width.getSelection() / 2)
				min = width.getSelection() / 2;
			if (min > length.getSelection() / 2)
				min = length.getSelection() / 2;
			if (min > height.getSelection() / 2)
				min = height.getSelection() / 2;
			radius.setMaximum(min);
		}
	}

}