package acmus.simulation.rtt;

import java.util.List;

import acmus.simulation.Receptor;
import acmus.simulation.math.Vector;

/**
 * Class representing a ray and encapsulates all the ray interactions while
 * tracing it.
 * 
 * @author migmruiz
 * 
 */
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
	
	/**
	 * @param energy the initial ray energy
	 * @param position the initial ray position
	 * @param direction the initial ray direction
	 */
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
			float positionZ, float directionX, float directionY, float directionZ) {
		this(energy, new Vector(positionX, positionY,
				positionZ), new Vector(directionX, directionY, directionZ));
	}
	
	public double getEnergy() {
		return energy;
	}
	
	public Vector getPosition() {
		return new Vector(position);
	}
	
	public Vector getDirection() {
		return new Vector(direction);
	}
	
	/**
	 * Trace the Ray instance
	 * 
	 * @param receptor the sound receptor
	 * @param sectors a List of {@link Sector}s defining the room geometry
	 * @param soundSpeed the sound speed
	 * @param airAbsorptionCoefficient the air absorption coefficient
	 * @param k the minimum limit for the ray's energy
	 */
	public void trace(Receptor receptor, List<Sector> sectors,
			double soundSpeed, double airAbsorptionCoefficient, double k) {
		
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
			
			/*
			 * ray receptor intercept test
			 */
			interceptsReceptor = receptor.intercept(airAbsorptionCoefficient,
					soundSpeed, oldPosition, direction, (float) this.energy,
					(float) this.size);
			
// 			// Option without using Receptor's intercept, but a waste of time
//			interceptsReceptor(receptor, airAbsorptionCoefficient, soundSpeed);

			if(!interceptsReceptor){
				size += stepSize;
				energy = energy * (1 - reflectionSector.getAbsorptionCoeficient())
				* Math.pow(Math.E, -1 * airAbsorptionCoefficient * stepSize);
				
				// Local variable for better legibility
				Vector nv = reflectionSector.getNormalVector();
				
				/*
				 * calculates and normalizes the new ray direction
				 * supposes nv with norm 1
				 */
				direction.subFromSelf(nv.times(2 * 
						(direction.dotProduct(nv))));
				direction.normalize();
			}
			
		} while (energy > (1 / k) /* ray energy threshold */
				&& !interceptsReceptor);
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
				 * if the sector is closer than the last closest one updates
				 * stepSize to the smallest and the sector to the nearest
				 */
				if (localStepSize < this.stepSize) {
					this.stepSize = localStepSize;
					this.reflectionSector = s;
				}
			}
		}
	}
	
//	// Option without using Receptor's intercept
//	void interceptsReceptor(Receptor receptor, double airAbsorptionCoeficient,
//			double soundSpeed) {
//		
//		// Local variables for better legibility and better performance
//		Vector sphericalReceptorCenter = receptor.getCenter();
//		double sphericalReceptorRadius = receptor.getRadius();
//		Vector oldPositionToCenter = sphericalReceptorCenter.sub(oldPosition);
//		double tca = oldPositionToCenter.dotProduct(direction);
//
//		/*
//		 * As seen in Kulowski, tca > 0 says that the ray is not opposed to the
//		 * oldPositionToCenter direction
//		 * Check inequality Gomes says tca >= 0
//		 */
//		if (tca > 0) {
//
//			/*
//			 * Discriminant for solving in terms of stepSizeOnThisReflection 
//			 * (or s)
//			 * 
//			 * oldPosition.add(direction.times(stepSizeOnThisReflection)).sub(
//			 * sphericalReceptorCenter).squared() <= sphericalReceptorRadius
//			 * 
//			 * or
//			 * 
//			 * || P + s*D - C ||^2 <= R^2
//			 * 
//			 * direction (or D) is supposed with norm 1
//			 */
//			double discriminant = sphericalReceptorRadius*sphericalReceptorRadius 
//								- oldPositionToCenter.squared() + tca*tca;
//
//			if (discriminant > 0) { // ray V intercepts spherical receptor
//				
//				/*
//				 * meanStepSizeOnThisReflection =  (
//				 * (2*tca + Math.sqrt(discriminant)) + (2*tca Math.sqrt(discriminant))
//				 *									 ) /2
//				 * = mean of the two solutions for stepSizeOnThisReflection
//				 */
//				double meanStepSizeOnThisReflection = 2*tca;
//				
//				// Check if there are sectors inside the receptor's volume
//				
//				double distance = size + meanStepSizeOnThisReflection;
//				double time = distance / soundSpeed;
//				
//				double receptedEnergy = energy
//						* Math.pow(Math.E, -1 * airAbsorptionCoeficient
//								* meanStepSizeOnThisReflection) * tca
//						/ sphericalReceptorRadius;
//				
//				receptor.getSimulatedImpulseResponse().addValue((float) time,
//						(float) receptedEnergy);
//
//				this.interceptsReceptor = true;
//			}
//		}
//		
//	}
}
