package acmus.tools.structures;

public class NormalSector {
	public Vector normalVector;
	public double absorptionCoeficient;
	public Vector iPoint;

	public NormalSector(Vector normalVector, Vector iPoint,
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
