package acmus.tools.rtt;

import java.util.List;

import acmus.tools.structures.Sector;
import acmus.tools.structures.SimulatedImpulseResponse;
import acmus.tools.structures.Vector;

public class Ray {
	private double energy;
	private Vector position;
	private Vector direction;
	
	/**
	 * reflectionSector stores the nearest sector
	 */
	private Sector reflectionSector;
	/**
	 * stepSize stores the length between the ray actual position
	 * and the nearest sector's position
	 */
	private float stepSize;
	/**
	 * raySize stores the length of the path that the ray is tracing
	 */
	private double size;
	/**
	 * oldPosition stores the position in begin of each reflection
	 */
	private Vector oldPosition;
	private boolean interceptsReceptor;
	

	public Ray(double energy, Vector position, Vector direction) {

		this.energy = energy;
		this.position = position;
		this.direction = direction;
		
		this.interceptsReceptor = false;
		this.reflectionSector = null;
		this.stepSize = 0.0f;
		this.size = 0;
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
		return direction;
	}
	
	public void trace(Vector sphericalReceptorCenter,
			double sphericalReceptorRadius, List<Sector> sectors,
			double soundSpeed, double airAbsorptionCoeficient, double k,
			SimulatedImpulseResponse simulatedImpulseResponse) {
		
		do {
			oldPosition.set(position);
			stepSize = Float.MAX_VALUE;
			
			/**
			 *  verify the intercept sector
			 */
			interceptsSector(sectors);
			
			/*
			 *  position goes to intercept point
			 */
			position.addToSelf(direction.times(stepSize));
			// TODO Error handling: position is NOT always inside the limited region
			
			/*
			 * ray receptor intercept test
			 */
			interceptsReceptor(sphericalReceptorCenter,
					sphericalReceptorRadius, airAbsorptionCoeficient,
					soundSpeed, simulatedImpulseResponse);
			
			if(!interceptsReceptor){
				size += stepSize;
				energy = energy * (1 - reflectionSector.getAbsorptionCoeficient())
				* Math.pow(Math.E, -1 * airAbsorptionCoeficient * stepSize);
				
				// Local variable for better legibility
				Vector nv = reflectionSector.getNormalVector();
				
				/*
				 * calculates and normalizes the new ray direction
				 */
				direction.subFromSelf(nv.times(2 * 
						(direction.dotProduct(nv)) / nv.squared()));
				direction.normalize();
			}
			
		} while (energy > (1 / k ) && !interceptsReceptor); //ray energy threshold
	}
	
	void interceptsSector(List<Sector> sectors) {
		for (Sector s : sectors) {
			if (direction.dotProduct(s.getNormalVector()) < 0) {
				
				// Local variables for better legibility
				Vector n = s.getNormalVector();
				Vector i = s.getiPoint();

				/*
				 * localStepSize stores the length between the ray
				 * actual position and the tested sector's position
				 */
				float localStepSize = 
					(i.sub(position).dotProduct(n)) / (direction.dotProduct(n));
				/*
				 * TODO localStepSize is NOT always > 0
				 * as i.sub(position).dotProduct(n) is not always < 0
				 * because position is NOT always inside the limited region
				 */
				
				/*
				 * if the sector is closer than the last closest one updates
				 * stepSize to the smallest and the sector to the nearest
				 */
				if (localStepSize > 0 && localStepSize < this.stepSize) {
					this.stepSize = localStepSize;
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
		Vector oldPositionToCenter = sphericalReceptorCenter.sub(oldPosition);
		double tca = oldPositionToCenter.dotProduct(direction);

		/*
		 * As seen in Kulowski, tca > 0 says that the ray is not opposed to the
		 * oldPositionToCenter direction
		 * TODO Check: Gomes says tca >= 0
		 */
		if (tca > 0) {

			/*
			 * Discriminant for solving in terms of stepSizeOnThisReflection 
			 * (or s)
			 * 
			 * oldPosition.add(direction.times(stepSizeOnThisReflection)).sub(
			 * sphericalReceptorCenter).squared() <= sphericalReceptorRadius
			 * 
			 * or
			 * 
			 * || P + s*D - C ||^2 <= R^2
			 * 
			 * direction (or D) is supposed with norm 1
			 */
			double discriminant = sphericalReceptorRadius*sphericalReceptorRadius 
								- oldPositionToCenter.squared() + tca*tca;

			if (discriminant > 0) { // ray V intercepts spherical receptor
				
				/*
				 * meanStepSizeOnThisReflection =  (
				 * (2*tca + Math.sqrt(discriminant)) + (2*tca Math.sqrt(discriminant))
				 *									 ) /2
				 * = mean of the two solutions for stepSizeOnThisReflection
				 */
				double meanStepSizeOnThisReflection = 2*tca;
				
				// TODO Check if there are sectors inside the receptor's volume
				
				double distance = size + meanStepSizeOnThisReflection;
				double time = distance / soundSpeed;
				
				double receptedEnergy = energy
						* Math.pow(Math.E, -1 * airAbsorptionCoeficient
								* meanStepSizeOnThisReflection) * tca
						/ sphericalReceptorRadius;
				
				simulatedImpulseResponse.addValue((float)time, (float)receptedEnergy);
				
				this.interceptsReceptor = true;
			}
		}
		
	}
}
