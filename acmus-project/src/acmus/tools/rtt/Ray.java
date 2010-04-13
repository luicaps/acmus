package acmus.tools.rtt;

import java.util.List;

import acmus.tools.structures.Sector;
import acmus.tools.structures.SimulatedImpulseResponse;
import acmus.tools.structures.Vector;

public class Ray {
	private double energy;
	private Vector position;
	private Vector velocity;
	
	/**
	 * reflectionSector stores the nearest sector
	 */
	private Sector reflectionSector;
	/**
	 * stepDuration stores the time that coasts the ray to go from
	 * its actual position and the nearest sector's position
	 */
	private float stepDuration;
	/**
	 * rayDuration stores the time that the ray is been traced
	 */
	private double rayDuration;
	/**
	 * oldPosition stores the position in begin of each reflection
	 */
	private Vector oldPosition;
	private boolean interceptsReceptor;
	

	public Ray(double energy, Vector position, Vector direction) {

		this.energy = energy;
		this.position = position;
		this.velocity = direction;
		
		this.interceptsReceptor = false;
		this.reflectionSector = null;
		this.stepDuration = 0.0f;
		this.rayDuration = 0;
		this.oldPosition = new Vector(position);
		
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
	
	public void trace(Vector sphericalReceptorCenter,
			double sphericalReceptorRadius, List<Sector> sectors,
			double soundSpeed, double airAbsorptionCoeficient, double k,
			SimulatedImpulseResponse simulatedImpulseResponse) {
		
		do {
			velocity.normalize((float) soundSpeed);
			oldPosition.set(position);
			stepDuration = Float.MAX_VALUE;
			
			/**
			 *  verify the intercept sector
			 */
			interceptsSector(sectors);
			
			/*
			 *  position goes to intercept point
			 */
			position.addToSelf(velocity.times(stepDuration));
			double eTemp = energy * (1 - reflectionSector.getAbsorptionCoeficient())
					* Math.pow(Math.E, -1 * airAbsorptionCoeficient * stepDuration * soundSpeed);

			/*
			 * ray receptor intercept test
			 */
			interceptsReceptor(sphericalReceptorCenter,
					sphericalReceptorRadius, airAbsorptionCoeficient,
					soundSpeed, simulatedImpulseResponse);
			
			if(!interceptsReceptor){
				rayDuration += stepDuration;
				energy = eTemp;
				
				// Local variable for better legibility
				Vector nv = reflectionSector.getNormalVector();
				
				/*
				 * calculates the new ray direction
				 */
				velocity.subFromSelf(nv.times(2 * 
						(velocity.dotProduct(nv)) / nv.squared()));
			}
			
		} while (energy > (1 / k ) && !interceptsReceptor); //ray energy threshold
	}
	
	void interceptsSector(List<Sector> sectors) {
		for (Sector s : sectors) {
			if (velocity.dotProduct(s.getNormalVector()) < 0) {
				
				// Local variables for better legibility
				Vector n = s.getNormalVector();
				Vector i = s.getiPoint();

				/*
				 * localStepDuration stores the time that coasts the ray to go from
				 * its actual position and the tested sector's position
				 */
				float localStepDuration = 
					(i.sub(position).dotProduct(n)) / (velocity.dotProduct(n));
				// TODO localStepDuration is NOT always > 0
				if (localStepDuration < 0){
					localStepDuration = - localStepDuration;
				}
				
				/*
				 * if the sector is closer than the last closest one updates
				 * stepSize to the nearest sector
				 */
				if (localStepDuration < this.stepDuration) {
					this.stepDuration = localStepDuration;
					this.reflectionSector = s;
				}
			}
		}
	}
	
	void interceptsReceptor(Vector sphericalReceptorCenter,
			double sphericalReceptorRadius, double airAbsorptionCoeficient,
			double soundSpeed,
			SimulatedImpulseResponse simulatedImpulseResponse) {
		
		// Local variables for better legibility and better performance
		Vector reflectionPointToCenter = sphericalReceptorCenter.sub(oldPosition);
		double tca = reflectionPointToCenter.dotProduct(velocity);
		
		// TODO Check: Kulowski says tca > 0, Mario >= 0
		if (tca > 0) { 
			double t2hc = sphericalReceptorRadius*sphericalReceptorRadius 
								- reflectionPointToCenter.squared() + tca*tca;

			if (t2hc > 0) { // ray V intercepts spherical receptor
				
				// TODO Check: Can this be negative?
				double stepSizeOnThisReflection = tca - Math.sqrt(t2hc);
				
				// TODO Check: Does that make sense?
				double time = rayDuration + stepSizeOnThisReflection/soundSpeed;
				
				double receptedEnergy = energy
						* Math.pow(Math.E, -1 * airAbsorptionCoeficient
								* stepSizeOnThisReflection) * tca
						/ sphericalReceptorRadius;
				
				simulatedImpulseResponse.addValue((float)time, (float)receptedEnergy);
				
				this.interceptsReceptor = true;
			}
		}
		
	}
}
