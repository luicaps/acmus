package acmus.tools.structures;

public class NormalSector {
	public Triade normalVector;
	public double absorbentCoeficient;
	public Triade iPoint;

	public NormalSector(Triade normalVector, Triade iPoint,
			double absorbentCoeficient) {
		this.normalVector = normalVector;
		this.iPoint = iPoint;
		this.absorbentCoeficient = absorbentCoeficient;
	}
}
