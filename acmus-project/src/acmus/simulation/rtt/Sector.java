package acmus.simulation.rtt;

import acmus.simulation.math.Vector;

public class Sector {
	public Vector normalVector;
	public double absorptionCoeficient;
	public Vector iPoint;

	public Sector(Vector normalVector, Vector iPoint,
			double absorbentCoeficient) {
		this.normalVector = normalVector;
		this.iPoint = iPoint;
		this.absorptionCoeficient = absorbentCoeficient;
	}

	public Vector getNormalVector() {
		return this.normalVector;
	}

	public double getAbsorptionCoeficient() {
		return this.absorptionCoeficient;
	}

	public Vector getiPoint() {
		return this.iPoint;
	}
	
	
}
