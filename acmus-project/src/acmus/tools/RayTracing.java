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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.jfree.experimental.chart.swt.ChartComposite;

import acmus.graphics.ChartBuilder;
import acmus.tools.rtt.RandomAcousticSource;
import acmus.tools.rtt.RayTracingSimulation;
import acmus.tools.structures.NormalSector;
import acmus.tools.structures.Triade;

/**
 * @author mhct
 * @author vgp
 */
public class RayTracing extends Composite {

	static final int K = 1000;
	private static final double INITIAL_ENERGY = 100000;

	// GUI variables
	private Label label;
	private Text _input;
	private Button compute;
	private Spinner point;

	// Algorithm variables
	private static double v_som;
	private static int taxa;

	// Todos os campos devem ficar aqui para serem usados no calculo realizado
	// por compute()
	private Text respostaImpulsivaText;
	private Text soundSpeed;

	private Spinner sourceX;
	private Spinner sourceY;
	private Spinner sourceZ;
	private Spinner receiverX;
	private Spinner receiverY;
	private Spinner receiverZ;
	private Spinner radius;
	final private Text width;
	final private Text height;
	final private Text length;
	final private Text floorCoeficient;
	final private Text ceilCoeficient;
	final private Text wallsCoeficients;
	private Spinner rays;
	private Text soundAtenuation;

	public RayTracing(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(10, false));
		
		// Impulsive response

		label = new Label(this, SWT.LEAD);
		label.setText("Impulsive response (Hz): ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);
		

		respostaImpulsivaText = new Text(this, SWT.NONE);
		setGridData(respostaImpulsivaText, SWT.LEAD, SWT.CENTER, 1, 40);

		// Ponto de origem

		label = new Label(this, SWT.NONE);
		label.setText("Initial point (x, y, z): ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		point = new Spinner(this, SWT.NONE);
		setSpinner(point, 2, 10000, 0);
		setGridData(point, SWT.LEAD, SWT.CENTER, 1);

		point = new Spinner(this, SWT.NONE);
		setSpinner(point, 2, 10000, 0);
		setGridData(point, SWT.LEAD, SWT.CENTER, 1);

		point = new Spinner(this, SWT.NONE);
		setSpinner(point, 2, 10000, 0);
		setGridData(point, SWT.LEAD, SWT.CENTER, 1);

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
		setGridData(soundSpeed, SWT.LEAD, SWT.CENTER, 1, 40);

		// Estipulated position of esferic receiver

		label = new Label(this, SWT.NONE);
		label.setText("Spheric receiver's estipulated position: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		receiverX = new Spinner(this, SWT.NONE);
		setSpinner(receiverX, 2, 10000, 0);
		setGridData(receiverX, SWT.LEAD, SWT.CENTER, 1);

		receiverY = new Spinner(this, SWT.NONE);
		setSpinner(receiverY, 2, 10000, 0);
		setGridData(receiverY, SWT.LEAD, SWT.CENTER, 1);

		receiverZ = new Spinner(this, SWT.NONE);
		setSpinner(receiverZ, 2, 10000, 0);
		setGridData(receiverZ, SWT.LEAD, SWT.CENTER, 5);

		// Sound's atenuation on air

		label = new Label(this, SWT.NONE);
		label.setText("Sound's atenuation on air: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);
		
		soundAtenuation = new Text(this, SWT.NONE);
		setGridData(soundAtenuation, SWT.LEAD, SWT.CENTER, 1, 40);

		// Esferic receiver's radius

		label = new Label(this, SWT.NONE);
		label.setText("Esferic receiver's radius: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		radius = new Spinner(this, SWT.NONE);
		setSpinner(radius, 0, 5, 0);
		setGridData(radius, SWT.LEAD, SWT.CENTER, 7, 40);

		// Number of rays

		label = new Label(this, SWT.NONE);
		label.setText("Number of rays: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		rays = new Spinner(this, SWT.NONE);
		setSpinner(rays, 0, Integer.MAX_VALUE, 0);
		setGridData(rays, SWT.LEAD, SWT.CENTER, 1, 40);

		// Source position

		label = new Label(this, SWT.NONE);
		label.setText("Source position: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		sourceX = new Spinner(this, SWT.NONE);
		setSpinner(sourceX, 2, 10000, 0);
		setGridData(sourceX, SWT.LEAD, SWT.CENTER, 1);

		sourceY = new Spinner(this, SWT.NONE);
		setSpinner(sourceY, 2, 10000, 0);
		setGridData(sourceY, SWT.LEAD, SWT.CENTER, 1);

		sourceZ = new Spinner(this, SWT.NONE);
		setSpinner(sourceZ, 2, 10000, 0);
		setGridData(sourceZ, SWT.LEAD, SWT.CENTER, 5);

		// Number of walls

		label = new Label(this, SWT.NONE);
		label.setText("Room Width: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		width = new Text(this, SWT.NONE);
		setGridData(width, SWT.LEAD, SWT.CENTER, 1, 40);

		label = new Label(this, SWT.NONE);
		label.setText("Room Height: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		height = new Text(this, SWT.NONE);
		setGridData(height, SWT.LEAD, SWT.CENTER, 1, 40);
		
		label = new Label(this, SWT.NONE);
		label.setText("Room Length: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		length = new Text(this, SWT.NONE);
		setGridData(length, SWT.LEAD, SWT.CENTER, 5, 40);
		
		// Coeficients
		
		label = new Label(this, SWT.NONE);
		label.setText("Floor coeficient: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		floorCoeficient = new Text(this, SWT.NONE);
		setGridData(floorCoeficient, SWT.LEAD, SWT.CENTER, 1, 40);

		label = new Label(this, SWT.NONE);
		label.setText("Ceil Coeficient: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		ceilCoeficient = new Text(this, SWT.NONE);
		setGridData(ceilCoeficient, SWT.LEAD, SWT.CENTER, 1, 40);
		
		label = new Label(this, SWT.NONE);
		label.setText("Walls Coeficients: ");
		setGridData(label, SWT.LEAD, SWT.CENTER, 1);

		wallsCoeficients = new Text(this, SWT.NONE);
		setGridData(wallsCoeficients, SWT.LEAD, SWT.CENTER, 1, 40);
		
		
		// Button that trigger the algorithm
		compute = new Button(this, SWT.NONE);
		compute.setText("Compute");
		setGridData(compute, SWT.LEAD, SWT.CENTER, 1);

		compute.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				compute();
			}
		});

		this.pack();
	}

	public void setSpinner(Spinner component, int digits, int maximum,
			int minimum) {
		component.setDigits(digits);
		component.setMaximum(maximum);
		component.setMinimum(minimum);
	}

	public void setGridData(Control component, int horizontalAlign,
			int verticalAlign, int horizontalSpan) {
		GridData gd = new GridData();
		gd.horizontalAlignment = horizontalAlign;
		gd.verticalAlignment = verticalAlign;
		gd.horizontalSpan = horizontalSpan;
		component.setLayoutData(gd);
	}

	public void setGridData(Control component, int horizontalAlign,
			int verticalAlign, int horizontalSpan, int width) {
		GridData gd = new GridData();
		gd.horizontalAlignment = horizontalAlign;
		gd.verticalAlignment = verticalAlign;
		if (width != 0)
			gd.widthHint = width;
		gd.horizontalSpan = horizontalSpan;
		component.setLayoutData(gd);
	}
	
	public void setGridData2(Control component, int horizontalAlign,
			int verticalAlign, int width, int height) {
		GridData gd = new GridData();
		gd.horizontalAlignment = horizontalAlign;
		gd.verticalAlignment = verticalAlign;
		gd.horizontalSpan = 10;
		gd.widthHint = width;
		gd.heightHint = height;
		component.setLayoutData(gd);
	}

	public void validate() {
		_input.setBackground(new Color(null, 245, 245, 220));
	}

	public void compute() {

		List<NormalSector> sectors = generateSectorsFor();
		List<Triade> vectors = new RandomAcousticSource().generate(rays.getSelection());
		Triade soundSourceCenter = newTriadeFor(sourceX, sourceY, sourceZ);
		Triade sphericalReceptorCenter = newTriadeFor(receiverX, receiverY, receiverZ);
		double sphericalReceptorRadius = getValue(radius);
		double soundSpeed = Double.valueOf(this.soundSpeed.getText());
		double mCoeficient = Double.valueOf(soundAtenuation.getText());
		RayTracingSimulation simulation = new RayTracingSimulation(sectors, vectors, soundSourceCenter,
				sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed,
				INITIAL_ENERGY, mCoeficient, K);
		
		simulation.simulate();
		Map<Double, Double> histogram = simulation.getSphericalReceptorHistogram();
		ChartBuilder cb = new ChartBuilder(histogram);
		ChartComposite cc = cb.show(this);
		
		GridData cg = new GridData();
		setGridData2(cc, SWT.LEAD, SWT.BOTTOM, 800, 450);
		//setGridData(cc, SWT.LEAD, SWT.BOTTOM, SWT.CENTER, 10);
		this.pack();
	}

	private List<NormalSector> generateSectorsFor() {
		ArrayList<NormalSector> result = new ArrayList<NormalSector>();
		double w = Double.valueOf(this.width.getText());
		double h = Double.valueOf(this.height.getText());
		double l = Double.valueOf(this.length.getText());
		result.add(new NormalSector(new Triade(0, 0, 1), new Triade(l, w, 0), 0.5));
		result.add(new NormalSector(new Triade(0, 1, 0), new Triade(l, 0, h), 0.5));
		result.add(new NormalSector(new Triade(1, 0, 0), new Triade(0, w, h), 0.5));
		result.add(new NormalSector(new Triade(0, 0, -1), new Triade(l, w, h), 0.5));
		result.add(new NormalSector(new Triade(0, -1, 0), new Triade(l, w, h), 0.5));
		result.add(new NormalSector(new Triade(-1, 0, 0), new Triade(l, w, h), 0.5));
		return result;
	}

	private Triade newTriadeFor(Spinner sourceX, Spinner sourceY,
			Spinner sourceZ) {
		return new Triade(getValue(sourceX), getValue(sourceY), getValue(sourceZ));
	}

	private double getValue(Spinner sourceX) {
		double base = Math.pow(10, -sourceX.getDigits());
		return sourceX.getSelection() * base;
	}

	public static void registraRaio(Response resp, double energia, double dist) {
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
		} else {
			if ((aux != null) && (indice == aux.getIndice())) {
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
	}

}