package acmus.tools.structures;

public class NormalSector {
	public Vector normalVector;
	public double absorbentCoeficient;
	public Vector iPoint;

	public NormalSector(Vector normalVector, Vector iPoint,
			double absorbentCoeficient) {
		this.normalVector = normalVector;
		this.iPoint = iPoint;
		this.absorbentCoeficient = absorbentCoeficient;
	}
}
