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

import acmus.tools.structures.Icosaedro;
import acmus.tools.structures.Parede;
import acmus.tools.structures.Plain;
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
	ArrayList wallsLabels;
	ArrayList wallsPoints;
	ArrayList coeficientsLabels;
	ArrayList wallsCoeficients;
	
	// Algorithm variables
	static Triade Origem, Fonte, Receptor;
	static double v_som, m_ar, raio;
	static int taxa,cont,caracs;
    static Parede wallQueue;
	static Response resp1,resp2;

	static File arq;  
	
	// Todos os campos devem ficar aqui para serem usados no calculo realizado por compute()
	Text respostaImpulsivaText;
	Text velocidadeSom;
	
	
	Text resposta;
	
	public RayTracing(Composite parent, int style) {
	    super(parent, style);		

	    setLayout(new GridLayout(10, false));
	    
	    wallsLabels = new ArrayList();
	    wallsPoints = new ArrayList();
	    coeficientsLabels = new ArrayList();
	    wallsCoeficients = new ArrayList();
		    
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
			label = (Label)wallsLabels.get(i);
			label.setVisible(true);
			
			label = (Label)coeficientsLabels.get(i);
			label.setVisible(true);
			
			Text coeficiente = (Text)wallsCoeficients.get(i);
			coeficiente.setVisible(true);
			
			for(int j = 0; j < 9; j++) {
				point = (Spinner)wallsPoints.get(9*i + j);
				point.setVisible(true);
			}
		}
		for(int i = quantWalls.getSelection(); i < quantWalls.getMaximum(); i++) {
			label = (Label)wallsLabels.get(i);
			label.setVisible(false);
			
			label = (Label)coeficientsLabels.get(i);
			label.setVisible(false);
			
			Text coeficient = (Text)wallsCoeficients.get(i);
			coeficient.setVisible(false);
			
			for(int j = 0; j < 9; j++) {
				point = (Spinner)wallsPoints.get(9*i + j);
				point.setVisible(false);
			}
		}
	}

	private void showVariables() {
	    Control[] controls = this.getChildren();
	    int tam = controls.length;
	    for(int i = 0; i < tam; i++) {
	    	Text resp2 = new Text(this, SWT.NONE);
	    	resp2.setText("Var " + i + ": " + controls[i].getData());
	    	setGridData(resp2, SWT.LEAD, SWT.CENTER, 1);
//	    	System.out.println("Var " + i + ": " + controls[i]);
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
		int quantidade;
		int i,j,numraios;
//		
//		Control[] controls = this.getChildren();
		
		/* Inicializa��o da estrutura que garda as paredes (fila) */
		wallQueue = null;
		resp1 = null;
		resp2 = null;
		
		/* Inicializa��o do Icosaedro */
		Icosaedro ico = new Icosaedro();
		
		taxa = Integer.valueOf(respostaImpulsivaText.getText());

		// Essa linha abaixo mostra como devemos fazer quando tivermos calculado a resposta 	
//		resposta.setText("colocar a resposta");
		
		// TODO: Daqui em diante todas as variaveis que o programa C pega devem passadas para variaveis da classe
//		printf("Entre com o ponto Origem (x y z): ");
//		scanf("%lf %lf %lf", &Origem.x, &Origem.y, &Origem.z);
//		printf("Entre com a velocidade do som (m/s): ");
//		scanf("%lf", &v_som);
//		printf("Entre com a constante de atenuacao do som no ar: ");
//		scanf("%lf", &m_ar);
//		printf("Estipule a posicao do receptor esferico: ");
//		scanf("%lf %lf %lf", &Receptor.x, &Receptor.y, &Receptor.z);
//		printf("Estipule o raio do receptor esferico: ");
//		scanf("%lf", &raio);
//		printf("Entre com a quantidade de paredes(planos): ");
		/* Loop para leitura dos pontos das paredes*/
//		for (i = 0; i < quantWalls.getSelection(); i++){
//			addWall();
//			printf("Entre com a quantidade de pontos desejados para a parede %d: ",i+1);
//			scanf("%d", &(FilaDeParedes->parede->qpontos));
//			while(FilaDeParedes->parede->qpontos < 3){
//				printf("Entre com uma quantidade de pontos (maior que 2) para a parede %d: ",i+1);
//				scanf("%d", &(FilaDeParedes->parede->qpontos));
//			}
//			FilaDeParedes->parede->nrparede = i+1;
//			printf("malloc 9: %ld\n",(long int)FilaDeParedes->parede->qpontos * sizeof(TRIADE));
//			FilaDeParedes->parede->Ponto = (TRIADE *)malloc(FilaDeParedes->parede->qpontos * sizeof(TRIADE));
//			for(j=0; j<FilaDeParedes->parede->qpontos; j++){
//				printf("Entre com o ponto %d  para a parede %d:\n", j+1, i+1);
//				printf("Ponto %d (x y z): ", j+1);
//				scanf("%lf %lf %lf", &(FilaDeParedes->parede->Ponto[j].x),
//						&(FilaDeParedes->parede->Ponto[j].y), &(FilaDeParedes->parede->Ponto[j].z));
//			}
//			if(!verificapontos()){
//				ApPAREDES temp;
//				printf("Entre novamente com os pontos:\n");
//				/* libera mem utilizada */
//				free(FilaDeParedes->parede->Ponto);
//				free(FilaDeParedes->parede);
//				temp = FilaDeParedes->prox;
//				free(FilaDeParedes);
//				FilaDeParedes = temp;
//				i--;
//			}
//			else{
//				CalculaRetas(FilaDeParedes->parede);
//				printf("Entre Com o Coeficiente de Absorcao dessa Parede: ");
//				scanf("%lf", &(FilaDeParedes->parede->Coeficiente));
//			}
//		}
//		/* Inicio do Met�do do Tra�ado de Raios */
//		
//		arq = fopen("fonte.txt","w");
//		fprintf(arq,"%%Raios gerados\n");
//		
//		caracs = 0;
//		cont = 0;
//		printf("Entre com o ponto origem da Fonte  (x y z): ");
//		scanf("%lf %lf %lf", &Fonte.x, &Fonte.y, &Fonte.z);
//		
//		printf("Entre com um numero natural de raios: ");
//		scanf("%d", &numraios);
//		numraios = (int)(sqrt((numraios - 2)/10) + 1);
//		printf("\nO programa gerara %d raios pela divisao de cada aresta em %d partes.\n\n",(2+10*numraios*numraios),numraios);
//		/* Itera��o para os v�rtices do icosaedro */
//		cont = 1;
//		printf("Total de raios iterados: ");
//		for(i=0; i < 12; i++){
//			EscreveDec();
//			TracaRaio(Fonte,vertice[i],1,(double)1/K);
//		}
//		if(numraios > 1){
//			for(i=0; i < 30; i++){
//				aresta[i].ApPonto = DivideAresta(*(aresta[i].ponto1),*(aresta[i].ponto2),numraios,1);
//			}
//			for(i=0; i < 12; i++){
//				for(j=0; j < numraios - 1; j++){
//					DivideAresta(face[i].aresta1->ApPonto[j],face[i].aresta2->ApPonto[j],j+1,0);
//				}
//			}
//			for(i=12; i < 16; i++){
//				int k = numraios - 2;
//				for(j=0; j < numraios - 1; j++){
//					DivideAresta(face[i].aresta1->ApPonto[j],face[i].aresta2->ApPonto[k],k+1,0);
//					k--;
//				}
//			}
//			for(i=16; i < 18; i++){
//				int k = numraios - 2;
//				for(j=0; j < numraios - 1; j++){
//					DivideAresta(face[i].aresta1->ApPonto[j],face[i].aresta2->ApPonto[k],j+1,0);
//					k--;
//				}
//			}
//			for(i=18; i < 20; i++){
//				int k = numraios - 2;
//				for(j=0; j < numraios - 1; j++){
//					DivideAresta(face[i].aresta1->ApPonto[j],face[i].aresta2->ApPonto[k],k+1,0);
//					k--;
//				}
//			}
//		}
//		printf("\nImprimindo resposta impulsiva nos arquivos saidax.txt \n");
//		ImprimeArquivo(resp1,"saida1.txt");
//		ImprimeArquivo(resp2,"saida2.txt");
//		ImprimeArquivo2(resp2,"saida3.txt");
//		printf("Fim de programa.\n");
//		exit(0);
		
		
		
		
		
		
		//TODO EXECUTA O ALGORITMO AQUI!!!
//		double x = 0.0;
//		double y = 0.0;
//		try {
//		String s1 = temperature.getText();
//		Double d1 = Double.valueOf(s1);
//		x = Math.abs(d1.doubleValue());
//		String s2 = humidity.getText();
//		Double d2 = Double.valueOf(s2);
//		y = Math.abs(d2.doubleValue());
//		if (inputValid(x, y) == true) {
//		CalculateSpeedOfSound cal = new CalculateSpeedOfSound(x, y);
//		double velocity = cal.calculateSpeedOfSound();
//		velocity = velocity * 10;
//		int aux = (int) velocity;
//		velocity = (double) aux / 10;
//		speed.setText("" + velocity);
//		} else
//		errorDialog.open();
//		} catch (Exception exception) {
//		errorDialog.open();
//		}
	}
	
	/***********************************************************************
	   Fun��o que verifica se os pontos dados pelo usu�rio pertencem ao
	   mesmo plano, retornando V (1) ou F (0). Ao mesmo tempo, ela tamb�m
	   calcula o vetor normal ao plano e o vetor normal unit�rio ao plano.
	************************************************************************/
	public int verificapontos(){
		ArrayList NORMAL = new ArrayList();
		Triade aux1, aux2;
		int i;
		double aux;
		
		for(i = 0; i < (wallQueue.getParede().getNumPoints() - 2); i++){
			aux1 = ((Triade)wallQueue.getParede().getPoints().get(i)).sub(((Triade)wallQueue.getParede().getPoints().get(i+1)));
			aux2 = ((Triade)wallQueue.getParede().getPoints().get(i+1)).sub(((Triade)wallQueue.getParede().getPoints().get(i+2)));
			NORMAL.set(i, aux1.produtoVetorial(aux2));
		}
		for(i = 0; i < (wallQueue.getParede().getNumPoints() - 2); i++){
			if(((Triade)NORMAL.get(i)).modulo() == 0){
				System.out.println("Tres pontos consecutivos nao podem ser colineares!\n");
				return 0;
			}
		}
		wallQueue.getParede().setNormalVectorUnit(((Triade)NORMAL.get(0)).divideVetorEscalar(((Triade)NORMAL.get(0)).modulo()));
		for(i = 0; i < wallQueue.getParede().getNumPoints() - 3; i++){
			aux = ((Triade)NORMAL.get(i)).anguloVetores(((Triade)NORMAL.get(i + 1)));
			if(aux != 0 && ((aux < Math.PI - precisao)||(aux > Math.PI + precisao))){
				System.out.println("Os "+wallQueue.getParede().getNumPoints()+"pontos dados nao formam um plano!");
				return 0;
			}
		}
		return 1;
	}
	
	/***********************************************************************
	   Procedimento que cria as paredes e a estrutura da fila de paredes,
	   fazendo a inser��o dessa parede na FilaDeParedes.
	************************************************************************/
	public void addWall() {
	  Parede aux = new Parede();
	  Plain aux2 = new Plain();

	  aux2.setFlagReflection(0); /* inicializa todas as paredes sem reflex�o */
	  aux.setProx(wallQueue);
	  aux.setParede(aux2);
	  wallQueue = aux;
	}
	
	/***********************************************************************
	   Fun��o que calcula a dist�ncia do ponto de origem da fonte sonora
	   ao plano e a dist�ncia que vetor caminha at� chegar no plano,
	   verificando se o ponto atingido no plano pertence a parede. Se isso
	   for verdade, retorna 1. Caso contr�rio, retorna 0.
	************************************************************************/
	public static int calculaDistanciaPonto2Plano(Triade orig, Triade direcao, Plain parede) {
		double d1,d2;
		Triade pontoQ;
		
		if(parede.getFlagReflection() == 1) {
			parede.setFlagReflection(0);     /* libera parede para as proximas reflexoes */
			return 0;                 /* como a parede acabou de refletir o raio retorna 0 */
		}
		/* d2 = Produto Escalar da NormalUnit com o vetor Direcao (ambos unit�rios) */
		d2 = parede.getNormalVectorUnit().produtoEscalar(direcao);  /* angulo entre a normal e o vetor direcao */
		if(d2 == 0) return 0;                         /* condi��o em que o raio est� paralelo ao plano */
		if(d2 > 0){
			parede.setNormalVectorUnit(parede.getNormalVectorUnit().multiplicaVetorEscalar(-1));
		}
		else d2 = -d2;
		/* d1 = menor distancia do ponto de origem do raio ao plano */
		d1 = parede.getNormalVectorUnit().produtoEscalar(((Triade)parede.getPoints().get(0)).sub(orig));
		if(d1 < 0) return 0;                           /* raio n�o encontra com o plano */
		
		parede.setDistSourcePlain(d1);
		if(d1 == 0){                                   /* condi��o de reflex�es m�ltiplas, como jun��es de paredes */
			System.out.println("Reflexao multipla... \n\n");
			parede.setDistanceOfRay(precisao); /* Tira o raio da condi��o de reflex�o multipla */
			return Plain.verificaPontoParede(orig, parede, precisao);
		}
		
		parede.setDistanceOfRay(d1/d2);                     /* por pitagoras, distancia = d1/cos(angulo) */
		
		pontoQ = orig.sum(direcao.multiplicaVetorEscalar(parede.getDistanceOfRay()));
		return Plain.verificaPontoParede(pontoQ, parede, precisao);
	}
	
	/***********************************************************************
	   Fun��o que varre a fila com todas as paredes declaradas verificando
	   quais s�o interceptadas pelo raio e retorna a primeira a ser
	   interceptada.
	   ************************************************************************/
	public static Parede firstIntercepedWall(Triade dir,Triade orig){
		Parede aux1,aux2;
		
		aux1 = wallQueue; /* vetor aux1 varre a lista de paredes cadastrados no in�cio do programa */
		aux2 = null;  /* Apontador para parede interceptada mais pr�xima do raio */
		
		while(aux1 != null){
			if(calculaDistanciaPonto2Plano(orig, dir, aux1.getParede()) == 1){
				if(aux2 == null) aux2 = aux1;
				else{
					if(aux1.getParede().getDistanceOfRay() < aux2.getParede().getDistanceOfRay()) {
						aux2 = aux1;
					}
				}
			}
			aux1 = aux1.getProx();
		}
		return aux2;
	}
	
	public static void registraRaio(Response resp, double energia, double dist){
		int indice;
		double a;
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
	
	public static void tracaRaio(Triade pinic, Triade direcao, double energia, double energiaMin){
		double aux1,aux2, DistPerc;
		Parede ApParede;
		
		DistPerc = 0;
		
		ApParede = firstIntercepedWall(direcao, pinic);
//		fprintf(arq, "%.10f     %.10f     %.10f;\n", direcao.getX(), direcao.getY(), direcao.getZ());
		
		aux1 = Receptor.sub(pinic).modulo();                       /* distancia do ponto de inicio do raio ao receptor */
		
		/* cos do angulo formado pela direcao e o vetor que vai do ponto do receptor at� o ponto de in�cio do raio */
		aux2 = Receptor.sub(pinic).divideVetorEscalar(aux1).produtoEscalar(direcao);
		if(aux2 < 0){
			double aux3;
			/* c�lculo da dist�ncia da reta da dire��o ao ponto do receptor */
			aux3 = aux1*Math.sin(Math.acos(aux2));
			if(aux3 < raio){
				double temp,d;
				
				d = Math.sqrt(raio*raio - aux3*aux3);
				temp = Math.sqrt(aux3*aux3 + aux1*aux1) - d;
				registraRaio(resp1, energia*Math.exp(-m_ar*temp), DistPerc + temp);
				registraRaio(resp2, energia*Math.exp(-m_ar*temp)*d/raio,DistPerc + temp);
			}
		}
		while((ApParede != null) && (energia > energiaMin)){
			Triade normal = new Triade();
			Triade inverso = new Triade();
			
			DistPerc = DistPerc + ApParede.getParede().getDistanceOfRay();
			energia = energia*(1 - ApParede.getParede().getAbCoeficient())*Math.exp(-m_ar*ApParede.getParede().getDistanceOfRay());
			
			normal = ApParede.getParede().getNormalVectorUnit(); /* normal unitaria da parede */
			inverso = direcao.multiplicaVetorEscalar(-1); /* inverso da direcao do raio incidente */
			
			/* Calculo do novo ponto de partida do raio */
			pinic = pinic.sum(direcao.multiplicaVetorEscalar(ApParede.getParede().getDistanceOfRay()));
			
			/* Calculo do novo vetor direcao */
			direcao = normal.multiplicaVetorEscalar(2*inverso.produtoEscalar(normal)).sum(direcao);
			
			ApParede.getParede().setFlagReflection(1);;
			
			ApParede = firstIntercepedWall(direcao, pinic);
			
			aux1 = Receptor.sub(pinic).modulo();                       /* distancia do ponto de inicio do raio ao receptor */
			
			/* cos do angulo formado pela direcao e o vetor que vai do ponto do receptor at� o ponto de in�cio do raio */
			aux2 = Receptor.sub(pinic).divideVetorEscalar(aux1).produtoEscalar(direcao);
			if(aux2 < 0){
				double aux3;
				/* c�lculo da dist�ncia da reta da dire��o ao ponto do receptor */
				aux3 = aux1*Math.sin(Math.acos(aux2));
				if(aux3 < raio){
					double temp,d;
					
					d = Math.sqrt(raio*raio - aux3*aux3);
					temp = Math.sqrt(aux3*aux3 + aux1*aux1) - d;
					registraRaio(resp1, energia*Math.exp(-m_ar*temp), DistPerc + temp);
					registraRaio(resp2, energia*Math.exp(-m_ar*temp)*d/raio, DistPerc + temp);
				}
			}
		}
	}	
	
	
	public static ArrayList divideAresta(Triade p1, Triade p2, int num, int aloca){
		Triade pi = new Triade(), n;
		ArrayList ar;
		double ang,aux;
		int i;
		
		ar = null;
		ang = Math.acos(p1.produtoEscalar(p2))/num;
		n = p1.produtoVetorial(p2);
		n = n.divideVetorEscalar(n.modulo());
		
		aux = (p1.getZ()*p2.getY() - p1.getY()*p2.getZ())*n.getX() + (p1.getX()*p2.getZ() - p1.getZ()*p2.getX())*n.getY() + (p1.getY()*p2.getX() - p1.getX()*p2.getY())*n.getZ();
		
		if(Math.abs(aux) < precisao) System.out.println("!");
		
		if(aloca != 0)  ar = new ArrayList();
		for(i=1; i < num; i++){
			pi.setX((p2.getZ()*n.getY() - p2.getY()*n.getZ())*Math.cos(ang*i) + (p1.getY()*n.getZ() - p1.getZ()*n.getY())*Math.cos(ang*(num - i)));
			pi.setX(pi.getX()/aux);
			
			pi.setY((p2.getX()*n.getZ() - p2.getZ()*n.getX())*Math.cos(ang*i) + (p1.getZ()*n.getX() - p1.getX()*n.getZ())*Math.cos(ang*(num - i)));
			pi.setY(pi.getY()/aux);
			
			pi.setZ((p2.getY()*n.getX() - p2.getX()*n.getY())*Math.cos(ang*i) + (p1.getX()*n.getY() - p1.getY()*n.getX())*Math.cos(ang*(num - i)));
			pi.setZ(pi.getZ()/aux);
			
			pi = pi.divideVetorEscalar(pi.modulo());
			if(aloca != 0) ar.set(i -1, pi);
			tracaRaio(Fonte, pi, 1, (double)1/K);
//			EscreveDec();
		}
		return ar;
	}
	

	public static void main(String[] args) {
		Display display = new Display ();
		Shell shell = new Shell (display);

		RayTracing rt = new RayTracing(shell, SWT.NONE );
		
		shell.open ();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
}