package acmus.tools.structures;

import java.util.List;

public class Ray {
	private double energy;
	private Vector position;
	private Vector tempPosition;
	private Vector velocity;
	private Vector normalReflectionSector;
	private float lMin;
	private float dMin;
	private double alpha;
	private double rayLength;
	private boolean intercepted;

	public Ray(double energy, Vector position, Vector velocity) {

		this.energy = energy;
		this.position = position;
		this.velocity = velocity;
		
		this.intercepted = false;
		this.tempPosition = new Vector(position);
		this.normalReflectionSector = null;
		this.lMin = 0.0f;
		this.dMin = 0.0f;
		this.alpha = 0.0;
		this.rayLength = 0;
	}

	public Ray(double energy, float positionX, float positionY,
			float positionZ, float velocityX, float velocityY, float velocityZ) {
		this(energy, new Vector(positionX, positionY,
				positionZ), new Vector(velocityX, velocityY, velocityZ));
	}
	
	public double getEnergy() {
		return energy;
	}
	
	public Vector getPosition() {
		return position;
	}
	
	public Vector getVelocity() {
		return velocity;
	}
	
	public void follow(Vector sphericalReceptorCenter,
			double sphericalReceptorRadius, List<NormalSector> sectors,
			double soundSpeed, double airAbsorptionCoeficient, double k,
			SimulatedImpulseResponse simulatedImpulseResponse) {

		do {
			
			this.lMin = Float.MAX_VALUE;
			this.position.set(tempPosition);
			
			/**
			 *  verify the interception section
			 */
			interceptsSector(sectors);
			
			tempPosition = position.add(velocity.times(lMin)); // interception point
			double eTemp = energy * (1 - alpha)
					* Math.pow(Math.E, -1 * airAbsorptionCoeficient * lMin);

			/*
			 * ray receptor interception test
			 */
			interceptsReceptor(sphericalReceptorCenter,
					sphericalReceptorRadius, airAbsorptionCoeficient,
					soundSpeed, simulatedImpulseResponse);
			if(!intercepted){
				rayLength += lMin;
				energy = eTemp;
				Vector reflectionFactor = normalReflectionSector.times(2.0f * dMin);
				Vector positionDifference = tempPosition.sub(position);
				velocity = reflectionFactor.add(positionDifference);
				velocity  = velocity.normalize(); //new ray direction
			}
		} while (energy > (1 / k ) && !intercepted); //ray energy threshold
	}
	
	void interceptsSector(List<NormalSector> sectors) {
		for (NormalSector s : sectors) {
			if (velocity.dotProduct(s.getNormalVector()) < 0) {
				Vector projPosSec = position.sub(s.getiPoint());
				float d = s.getNormalVector().dotProduct(projPosSec);
				float l = -1 * d / (velocity.dotProduct(s.getNormalVector()));
				
				polygonTest(l, d, s);
			}
		}
	}
	
	void interceptsReceptor(Vector sphericalReceptorCenter,
			double sphericalReceptorRadius, double airAbsorptionCoeficient,
			double soundSpeed,
			SimulatedImpulseResponse simulatedImpulseResponse) {
		Vector oc = sphericalReceptorCenter.sub(position);
		double l2oc = oc.dotProduct(oc);
		double tca = oc.dotProduct(velocity);

		if (tca >= 0) { 
			double t2hc = sphericalReceptorRadius*sphericalReceptorRadius 
								- l2oc + tca*tca;

			if (t2hc > 0) { // ray V intercepts spherical receptor
				double lThisReflection = tca - Math.sqrt(t2hc);
				double distance = rayLength + lThisReflection;
				double time = distance / soundSpeed;
				double eSphere = energy
						* Math.pow(Math.E, -1 * airAbsorptionCoeficient
								* lThisReflection) * tca
						/ sphericalReceptorRadius;

				simulatedImpulseResponse.addValue((float)time, (float)eSphere);
				
				this.intercepted = true;
			}
		}
		
	}
	
	void polygonTest(float l, float d, NormalSector ns){
		if (l <= this.lMin) {
			this.lMin = l;
			this.dMin = d;
			this.alpha = ns.getAbsorptionCoeficient();
			this.normalReflectionSector = ns.getNormalVector();
		}
	}
}
