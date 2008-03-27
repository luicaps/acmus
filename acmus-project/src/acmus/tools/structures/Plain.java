package acmus.tools.structures;

import java.util.ArrayList;
import java.util.Vector;

public class Plain {
	Vector points;       /* Apontador para um vetor de pontos do tipo TRIADE */
	Vector line;        /* Apontador para um vetor de vetores dire��o de cada reta formada pelos pontos da parede */
	Triade normalVectorUnit;   /* Vetor normal unitario ao Plano */
	int numPoints;         /* Quantidade de pontos pertencente � parede */
	double abCoeficient;  /* Coeficiente de absor��o da parede */
	double distSourcePlain;           /* Dist�ncia do Ponto fonte ao plano */
	double distanceOfRay;    /* Dist�ncia que o raio leva pra chegar ate a parede */
	int number;        /* N�mero dado pelo usuario � parede */
	int flagReflection;
	
	public Plain() {
		
	}
	
	public Plain(Vector points, Vector line, Triade normalVectorUnit, int numPoints, double abCoeficient, double distSourcePlain, double distanceOfRay, int number, int flagReflection) {
		// TODO Auto-generated constructor stub
		this.points = points;
		this.line = line;
		this.normalVectorUnit = normalVectorUnit;
		this.numPoints = numPoints;
		this.abCoeficient = abCoeficient;
		this.distSourcePlain = distSourcePlain;
		this.distanceOfRay = distanceOfRay;
		this.number = number;
		this.flagReflection = flagReflection;
	} 
	
	public int getFlagReflection() {
		return flagReflection;
	}
	public void setFlagReflection(int flagReflection) {
		this.flagReflection = flagReflection;
	}
	public Vector getLine() {
		return line;
	}
	public void setLine(Vector line) {
		this.line = line;
	}
	public Triade getNormalVectorUnit() {
		return normalVectorUnit;
	}
	public void setNormalVectorUnit(Triade normalVectorUnit) {
		this.normalVectorUnit = normalVectorUnit;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getNumPoints() {
		return numPoints;
	}
	public void setNumPoints(int numPoints) {
		this.numPoints = numPoints;
	}
	public Vector getPoints() {
		return points;
	}
	public void setPoints(Vector points) {
		this.points = points;
	}
	public double getAbCoeficient() {
		return abCoeficient;
	}
	public double getDistanceOfRay() {
		return distanceOfRay;
	}
	public double getDistSourcePlain() {
		return distSourcePlain;
	}
	
	/***********************************************************************
	   Procedimento que calcula os vetores dire��es de cada reta formada
	   por cada par de ponto da parede.
	************************************************************************/
	public static void calculaRetas(Plain par){
	  int i;
	  Vector line = par.getLine();
	  for(i = 0; i < (par.getNumPoints() - 1); i++){
	    line.add(i, ((Triade)par.getPoints().get(i)).sub(((Triade)par.getPoints().get(i+1))));
	    line.add(i, ((Triade)line.get(i)).divideVetorEscalar(((Triade)line.get(i)).modulo()));
	  }
	  line.add(i, ((Triade)par.getPoints().get(i)).sub(((Triade)par.getPoints().get(0))));
	  line.add(((Triade)line.get(i)).divideVetorEscalar(((Triade)line.get(i)).modulo()));
	}

	/**
	 * @param distSourcePlain The distSourcePlain to set.
	 */
	public void setDistSourcePlain(double distSourcePlain) {
		this.distSourcePlain = distSourcePlain;
	}

	/**
	 * @param distanceOfRay The distanceOfRay to set.
	 */
	public void setDistanceOfRay(double distanceOfRay) {
		this.distanceOfRay = distanceOfRay;
	}
	
	/***********************************************************************
	 Fun��o que verifica se o ponto px pertence a parede par, verificando
	 se px est� no interior da regi�o interna da parede definida pela
	 sequencia de pontos da mesma. 1(V) e 0(F)
	 
	 FIXME: refatorar pois a fun��o est� muito grande
	 ************************************************************************/
	public static int verificaPontoParede(Triade px3d, Plain par3d, double precisao){
		Triade dir = new Triade();
		Triade px = new Triade();
		Triade pc = new Triade();
		ArrayList NORMAL = new ArrayList();
		Plain par = new Plain();
		int i,contador;
		double aux;
		
		/* Verifica se o ponto eh um dos vertices da parede */
		for(i=0; i < par3d.getNumPoints(); i++){
			Triade p = (Triade)par3d.getPoints().get(i);
			if(Math.abs(p.x - px3d.x) < precisao)
				if(Math.abs(p.y - px3d.y) < precisao)
					if(Math.abs(p.z - px3d.z) < precisao){
						System.out.println("Ponto eh vertice da parede!\n");
						return 1;
					}
		}
		/* Verifica se o ponto esta nos extremos da parede */
		for(i=0; i < (par3d.getNumPoints() - 1); i++){
		    Triade p1 = (Triade)par3d.getPoints().get(i);
		    Triade p2 = (Triade)par3d.getPoints().get(i+1);
			NORMAL.set(i, p1.sub(px3d).produtoVetorial(p2.sub(px3d)));
		}
		Triade p1 = (Triade)par3d.getPoints().get(i);
		Triade p0 = (Triade)par3d.getPoints().get(0);
		NORMAL.set(i, p1.sub(px3d).produtoVetorial(p0.sub(px3d)));
		/* se o ponto px3d for colinear a qualquer dois pontos da parede, ent�o ele est� no extremo da parede */
		for(i = 0; i < par3d.getNumPoints(); i++){
			if(((Triade)NORMAL.get(i)).modulo() < precisao){
				return 1;
			}
		}
		/*** Procedimento simples de verifica��o de um ponto na parede ***/
		/*                                                               */
		/*   Esse procedimento falha para parede nao convexas, porem para*/
		/*   paredes convexas, ganha-se em tempo de processamento.       */
		/*                                                               */
		/*****************************************************************/
		contador = 1;
		i = 0;
		/* se os vetores normais calculados estiverem em dire��o oposta,  */
		/* ent�o o m�todo n�o garante que o ponto esteja dentro da parede.*/
		while((i < (par3d.getNumPoints() - 1)) && (contador == 1)){
			if(((Triade)NORMAL.get(i)).produtoEscalar(((Triade)NORMAL.get(i + 1))) < 0){
				contador = 0;
			}
			i++;
		}
		if((contador == 1) && (((Triade)NORMAL.get(i)).produtoEscalar(((Triade)NORMAL.get(0))) < 0)){
			contador = 0;
		}
		
		/*** Procedimento de verifica��o de um ponto na parede generico ***/
		/*                                                                */
		/*   Esse procedimento vale para qualquer tipo de parede, so que  */
		/*   gasta mais tempo de processamento.                           */
		/*                                                                */
		/******************************************************************/
		if(contador == 0){
			par.setNumPoints(par3d.getNumPoints());
			transformaPontos(px3d, par3d, px, par);
			calculaRetas(par);
			
			dir = (Triade)par.getLine().get(0); /* Dire��o arbitr�ria pertencente ao plano em quest�o */
			
			for(i=0; i < par.getNumPoints(); i++){
				aux = dir.produtoEscalar(((Triade)par.getLine().get(i))); /* produto escalar diferente de 1 e -1 indica que
				as retas se cruzam em algum momento */
				if((aux != 1)&&(aux != -1)){
					pc = Triade.pontoIntersecaoReta(dir, px, (Triade)par.getLine().get(i), (Triade)par.getPoints().get(i));
					aux = dir.produtoEscalar(px.sub(pc).divideVetorEscalar(px.sub(pc).modulo()));
					if(aux == 1){ /* Cruza a reta apenas no segmento de reta a partir de px */
						Triade par0 = (Triade)par.getPoints().get(0);
						Triade par1 = (Triade)par.getPoints().get(i);
						Triade par2 = (Triade)par.getPoints().get(i + 1);
						if(i < (par.getNumPoints() - 1)){
							if(pc.x != par1.x || pc.y != par1.y){ /* ignora ponto se estiver nessa extremidade*/
								/* tratamento de ponto extremo */
								if(((Math.abs(pc.x - par2.x) < precisao) && Math.abs(pc.y - par2.y) < precisao)){
									double alpha,a1,a2;
									/* tratamento de maximo e minimo locais */
									alpha = ((Triade)par.getLine().get(i)).anguloVetores(((Triade)par.getLine().get(i+1)));
									
									a1 = dir.anguloVetores(((Triade)par.getLine().get(i)));
									a2 = dir.anguloVetores(((Triade)par.getLine().get(i+1)));
									if(((a1 < Math.PI - alpha) && (a2 > alpha)) || ((a1 > alpha) && (a2 < Math.PI - alpha))){
										contador++;
									}
								} else {
									double aux1,aux2;
									
									aux = par1.sub(par2).modulo();
									aux1 = pc.sub(par1).modulo();
									aux2 = pc.sub(par2).modulo();
									if(aux1 <= aux && aux2 <= aux){
										contador++;
									}
								}
							}
						}
						else{
							/* ultima reta */
							if(pc.x != par1.x || pc.y != par1.y){ /* ignora ponto se estiver nessa extremidade*/
								/* tratamento de ponto extremo */
								if(((Math.abs(pc.x - par0.x) < precisao) && Math.abs(pc.y - par0.y) < precisao)){
									double alpha, a1, a2;
									/* tratamento de maximo e minimo locais */
									alpha = ((Triade)par.getLine().get(i)).anguloVetores(((Triade)par.getLine().get(0)));
									
									a1 = dir.anguloVetores(((Triade)par.getLine().get(i)));
									a2 = dir.anguloVetores(((Triade)par.getLine().get(0)));
									if(((a1 < Math.PI - alpha) && (a2 > alpha)) || ((a1 > alpha) && (a2 < Math.PI - alpha))){
										contador++;
									}
								}
								else{
									double aux1,aux2;
									aux = p1.sub(par0).modulo();
									aux1 = pc.sub(par1).modulo();
									aux2 = pc.sub(par0).modulo();
									if(aux1 <= aux && aux2 <= aux){
										contador++;
									}
								}
							}
						}
					}
				}
			}
		}
		return contador%2; /* Se contador for impar, fun��o retorna 1, indicando VERDADEIRO */
	}
	
	/***********************************************************************
	   Fun��o que transforma os pontos do plano par3d e o ponto px3d em
	   pontos de 2 dimens�es apenas, em x e y. Isso facilitar� os c�lculos
	   posteriores.
	************************************************************************/
	public static void transformaPontos(Triade px3d, Plain par3d, Triade px, Plain par){
	  ArrayList matriz = new ArrayList(3); /* matriz de rota��o usada para a transforma��o */
	  int i;

	  matriz.set(0, ((Triade)par3d.getPoints().get(0)).sub(((Triade)par3d.getPoints().get(1))));
	  matriz.set(0,  ((Triade)matriz.get(0)).divideVetorEscalar(((Triade)matriz.get(0)).modulo()));
	  matriz.set(2, par3d.getNormalVectorUnit());
	  matriz.set(1, ((Triade)matriz.get(2)).produtoVetorial(((Triade)matriz.get(0))));/* k x i = j */

	  /* Aplica-se a matriz de transforma��o em todos os pontos pela multiplica��o
	     da matriz de rota��o pelos vetores */
	  for(i=0; i < par3d.getNumPoints(); i++){
		Triade p1 = (Triade)par.getPoints().get(i);
		Triade par1 = (Triade)par3d.getPoints().get(i);
		Triade m0 = (Triade)matriz.get(0);
		Triade m1 = (Triade)matriz.get(1);
	    p1.x = m0.x*par1.x + m0.y*par1.y + m0.z*par1.z;
	    p1.y = -(m1.x*par1.x + m1.y*par1.y + m1.z*par1.z);
	    p1.z = 0;
	  }
	  Triade m0 = (Triade)matriz.get(0);
	  Triade m1 = (Triade)matriz.get(1);
	  px.x = m0.x*px3d.x + m0.y*px3d.y + m0.z*px3d.z;
	  px.y = -(m1.x*px3d.x + m1.y*px3d.y + m1.z*px3d.z);
	  px.z = 0;
	}
}
