package acmus.simulation.rtt;

import java.util.List;

import acmus.simulation.Receptor;
import acmus.simulation.math.Vector;

/**
 * Class representing a ray and encapsulates all the ray interactions while
 * tracing it.
 * 
 * @author mahtorres
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
	 * length stores the length of the path that the ray is tracing
	 */
	private double length;
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
		this.length = 0;
		this.oldPosition = new Vector(position);
		
	}

	public Ray(double energy, float positionX, float positionY,
			float positionZ, float directionX, float directionY,
			float directionZ) {
		this(energy, new Vector(positionX, positionY, positionZ),
				new Vector(directionX, directionY, directionZ));
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
			 * ray receptor intercept test
			 */
			interceptsReceptor = receptor.intercept(airAbsorptionCoefficient,
					soundSpeed, oldPosition, direction, (float) this.energy,
					(float) this.length);
			

			if(!interceptsReceptor){
				/*
				 *  position goes to intercept point
				 */
				position.addToSelf(direction.times(stepSize));
				
				this.length += stepSize;
				this.energy = this.energy
						* (1 - reflectionSector.getAbsorptionCoeficient())
						* Math.pow(Math.E, -1 * airAbsorptionCoefficient
								* stepSize);
				
				// Local variable for better legibility
				Vector nv = reflectionSector.getNormalVector();
				
				/*
				 * calculates and normalizes the new ray direction
				 * supposes nv with norm 1
				 */
				direction.subFromSelf(nv.times(2 * 
						direction.dotProduct(nv)));
				
				// Should I normalize it?
//				if(Math.abs(direction.normSquared() - 1) > 0.001){
//					System.out.println("Errors in direction updating," + 
//						" you should still normalize it");
//				}
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
					((i.sub(position)).dotProduct(n)) / (direction.dotProduct(n));
				
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
}
