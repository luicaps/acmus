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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import acmus.tools.structures.Triade;

/**
 * @author mhct
 * @author vgp
 */
public class RayTracing extends Composite {
	
	static final double precisao = 0.0000001;
	static final double K = 1000;

	// GUI variables
	Label label;
	Text _input;
	Button _inputBrowse;
	Button _bAdd;
	Button compute;
	Spinner point;
	Spinner quantWalls;
	Text resp;
	List<Label> wallsLabels;
	List<Spinner> wallsPoints;
	List<Label> coeficientsLabels;
	List<Text> wallsCoeficients;
	
	// Algorithm variables
	static Triade Origem, Fonte, Receptor;
	static double v_som, m_ar, raio;
	static int taxa,cont,caracs;
	static Response resp1,resp2;

	static File arq;  
	
	// Todos os campos devem ficar aqui para serem usados no calculo realizado por compute()
	Text respostaImpulsivaText;
	Text velocidadeSom;
	
	
	Text resposta;
	
	public RayTracing(Composite parent, int style) {
	    super(parent, style);		

	    setLayout(new GridLayout(10, false));
	    
	    wallsLabels = new ArrayList<Label>();
	    wallsPoints = new ArrayList<Spinner>();
	    coeficientsLabels = new ArrayList<Label>();
	    wallsCoeficients = new ArrayList<Text>();
		    
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
	    
	    velocidadeSom = new Text(this, SWT.NONE);
 	    setGridData(velocidadeSom, SWT.LEAD, SWT.CENTER, 1, 40);
	    
	    
	    // Estipulated position of esferic receiver
	    
	    label = new Label(this, SWT.NONE);
	    label.setText("Esferic receiver's estipulated position: ");
	    setGridData(label, SWT.LEAD, SWT.CENTER, 1);
	    
	    point = new Spinner(this, SWT.NONE);
	    setSpinner(point, 2, 10000, 0);
	    setGridData(point, SWT.LEAD, SWT.CENTER, 1);
	 
	    point = new Spinner(this, SWT.NONE);
	    setSpinner(point, 2, 10000, 0);
	    setGridData(point, SWT.LEAD, SWT.CENTER, 1);
	    
	    point = new Spinner(this, SWT.NONE);
	    setSpinner(point, 2, 10000, 0);
	    setGridData(point, SWT.LEAD, SWT.CENTER, 5);
	  	    
	    // Sound's atenuation on air
	    
	    label = new Label(this, SWT.NONE);
	    label.setText("Sound's atenuation on air: ");
	    setGridData(label, SWT.LEAD, SWT.CENTER, 1);
	    
	    Text atenuacaoSom = new Text(this, SWT.NONE);
 	    setGridData(atenuacaoSom, SWT.LEAD, SWT.CENTER, 1, 40); 	    

	    // Esferic receiver's radius	    

	    label = new Label(this, SWT.NONE);
	    label.setText("Esferic receiver's radius: ");
	    setGridData(label, SWT.LEAD, SWT.CENTER, 1);
	 
	    point = new Spinner(this, SWT.NONE);
	    setSpinner(point, 0, 5, 0);
	    setGridData(point, SWT.LEAD, SWT.CENTER, 7, 40);

	    // Number of rays

	    label = new Label(this, SWT.NONE);
	    label.setText("Number of rays: ");
	    setGridData(label, SWT.LEAD, SWT.CENTER, 1);
	 
	    point = new Spinner(this, SWT.NONE);
	    setSpinner(point, 0, 5, 0);
	    setGridData(point, SWT.LEAD, SWT.CENTER, 1, 40);

        // Source position
	    
	    label = new Label(this, SWT.NONE);
	    label.setText("Source position: ");
	    setGridData(label, SWT.LEAD, SWT.CENTER, 1);
	 
	    point = new Spinner(this, SWT.NONE);
	    setSpinner(point, 2, 10000, 0);
	    setGridData(point, SWT.LEAD, SWT.CENTER, 1);
	 
	    point = new Spinner(this, SWT.NONE);
	    setSpinner(point, 2, 10000, 0);
	    setGridData(point, SWT.LEAD, SWT.CENTER, 1);
	    
	    point = new Spinner(this, SWT.NONE);
	    setSpinner(point, 2, 10000, 0);
	    setGridData(point, SWT.LEAD, SWT.CENTER, 5);
	 	 	    
        // Number of walls
	    
	    label = new Label(this, SWT.NONE);
	    label.setText("Number of walls: ");
	    setGridData(label, SWT.LEAD, SWT.CENTER, 1);

	    quantWalls = new Spinner(this, SWT.NONE);
	    setSpinner(quantWalls, 0, 10, 0);
	    setGridData(quantWalls, SWT.LEAD, SWT.CENTER, 1, 40);
	    
	    // Button that trigger the algorithm
	    compute = new Button(this, SWT.NONE);
	    compute.setText("Compute");
	    setGridData(compute, SWT.LEAD, SWT.CENTER, 1);
	    
	    compute.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent event) {
	          compute();
	        }
	      });    
	    
	    // Resposta final do algoritmo
	    resposta = new Text(this, SWT.NONE);
	    resposta.setText("        ");
	    resposta.setEditable(false);
 	    setGridData(resposta, SWT.LEAD, SWT.CENTER, 7, 40);
	    
	    ModifyListener mlistener = new ModifyListener() {
	        public void modifyText(ModifyEvent event) {
	        	addWallsFields();
	        }
	    };

	    quantWalls.addModifyListener(mlistener);	    
	    
	    for(int i = 0; i < quantWalls.getMaximum(); i++) {
	    	label = new Label(this, SWT.NONE);
	    	label.setText("Points of wall " + (i + 1) + ": ");
	    	setGridData(label, SWT.LEAD, SWT.CENTER, 2);
	    	label.setVisible(false);
	    	wallsLabels.add(label);
	    	
	    	label = new Label(this, SWT.NONE);
	    	label.setText("Absorptium coeficient: ");
	    	setGridData(label, SWT.LEAD, SWT.CENTER, 1);
	    	label.setVisible(false);
	    	coeficientsLabels.add(label);

		    Text coeficiente = new Text(this, SWT.NONE);
	 	    setGridData(coeficiente, SWT.LEAD, SWT.CENTER, 7, 40);
	 	    coeficiente.setVisible(false);
	 	    wallsCoeficients.add(coeficiente);
		    
	    	for(int j = 0; j < 9; j++) {
	    		point = new Spinner(this, SWT.NONE);
	    	    setSpinner(point, 2, 10000, 0);
	    	    if(j == 2) {
	    	    	setGridData(point, SWT.LEAD, SWT.CENTER, 1);
	    	    } else if (j == 5){
	    	    	setGridData(point, SWT.LEAD, SWT.CENTER, 2);
	    	    } else {
	    	    	setGridData(point, SWT.RIGHT, SWT.CENTER, 1);	
	    	    }
	    	    point.setVisible(false);
	    	    wallsPoints.add(point);
	    	}
	    }
	    this.pack();
	}
	
	protected void addWallsFields() {
		for(int i = 0; i < quantWalls.getSelection(); i++) {
			label = wallsLabels.get(i);
			label.setVisible(true);
			
			label = coeficientsLabels.get(i);
			label.setVisible(true);
			
			Text coeficiente = wallsCoeficients.get(i);
			coeficiente.setVisible(true);
			
			for(int j = 0; j < 9; j++) {
				point = wallsPoints.get(9*i + j);
				point.setVisible(true);
			}
		}
		for(int i = quantWalls.getSelection(); i < quantWalls.getMaximum(); i++) {
			label = wallsLabels.get(i);
			label.setVisible(false);
			
			label = coeficientsLabels.get(i);
			label.setVisible(false);
			
			Text coeficient = wallsCoeficients.get(i);
			coeficient.setVisible(false);
			
			for(int j = 0; j < 9; j++) {
				point = wallsPoints.get(9*i + j);
				point.setVisible(false);
			}
		}
	}

	public void setSpinner(Spinner component, int digits, int maximum, int minimum) {
		component.setDigits(digits);
	    component.setMaximum(maximum);
	    component.setMinimum(minimum);
	}
	
	public void setGridData(Control component, int horizontalAlign, int verticalAlign, int horizontalSpan)
	{
		GridData gd = new GridData();
		gd.horizontalAlignment = horizontalAlign;
		gd.verticalAlignment = verticalAlign;
		gd.horizontalSpan = horizontalSpan;
		component.setLayoutData(gd);
	}
	
	public void setGridData(Control component, int horizontalAlign, int verticalAlign,  int horizontalSpan, int width) {
		GridData gd = new GridData();
		gd.horizontalAlignment = horizontalAlign;
		gd.verticalAlignment = verticalAlign;
		if(width != 0)
			gd.widthHint = width;
		gd.horizontalSpan = horizontalSpan;
		component.setLayoutData(gd);
	}
	
	public void validate(){
		_input.setBackground(new Color(null, 245, 245, 220));
	}
	
	public void compute() {
		
		/* Inicializa��o da estrutura que garda as paredes (fila) */
		resp1 = null;
		resp2 = null;
		
		/* Inicializa��o do Icosaedro */
		
		taxa = Integer.valueOf(respostaImpulsivaText.getText());

	}
	
	
	public static void registraRaio(Response resp, double energia, double dist){
		int indice;
		Response aux,aux2;
		
		indice = (int)(dist*taxa/v_som);
		
		aux = resp;
		aux2 = aux;
		while((aux != null)&&(indice > aux.getIndice())){
			aux2 = aux;
			aux = aux.getProx();
		}
		if(aux.equals(resp)){
			if((resp != null)&&(indice == resp.getIndice())){
				resp.setEnergia(resp.getEnergia() + energia);
				resp.setNumraios(resp.getNumraios() + 1);
			}
			else{
				resp = new Response();
				resp.setEnergia(energia);
				resp.setIndice(indice);
				resp.setNumraios(1);
				resp.setProx(aux);
			}
		}
		else{
			if((aux != null) && (indice == aux.getIndice())){
				aux.setEnergia(resp.getEnergia() + energia);
				aux.setNumraios(resp.getNumraios() + 1);
			}
			else{
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

	public static void main(String[] args) {
		Display display = new Display ();
		Shell shell = new Shell (display);

		shell.open ();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
}