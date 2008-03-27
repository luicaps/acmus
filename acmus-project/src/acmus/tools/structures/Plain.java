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
