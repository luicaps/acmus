package acmus.simulation.structures;

import acmus.simulation.Receptor;
import acmus.simulation.SimulatedImpulseResponse;
import acmus.simulation.math.Vector;

public class SphericalReceptor implements Receptor{
	private Vector center;
	protected float radius;
	
	public SphericalReceptor(Vector center, float radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public Vector getCenter() {
		return new Vector(center);
	}

	public float getRadius() {
		return radius;
	}

	public boolean intercept(double airAbsorptionCoeficient,
			double soundSpeed, Vector rayOrigin, Vector rayDirection,
			float rayEnergy, float rayLength,
			SimulatedImpulseResponse simulatedImpulseResponse) {
		
		// Local variables for better legibility and better performance
		Vector oldPositionToCenter = center.sub(rayOrigin);
		double tca = oldPositionToCenter.dotProduct(rayDirection);

		/*
		 * As seen in Kulowski, tca > 0 says that the ray is not opposed to the
		 * oldPositionToCenter direction
		 * TODO Check inequality Gomes says tca >= 0
		 */
		if (tca > 0) {

			/*
			 * Discriminant for solving in terms of stepSizeOnThisReflection 
			 * (or s) the equation below
			 * 
			 * oldPosition.add(direction.times(stepSizeOnThisReflection)).sub(
			 * sphericalReceptorCenter).squared() <= sphericalReceptorRadius
			 * 
			 * or, in terms of P, s, D, C and R:
			 * 
			 * || P + s*D - C ||^2 <= R^2
			 * 
			 * direction (or D) is supposed with norm 1
			 */
			double discriminant = radius*radius - oldPositionToCenter.squared() + tca*tca;

			if (discriminant > 0) { // ray V intercepts spherical receptor
				
				/*
				 * meanStepSizeOnThisReflection =  (
				 * (2*tca + Math.sqrt(discriminant)) + (2*tca Math.sqrt(discriminant))
				 *									 ) /2
				 * = mean of the two solutions for stepSizeOnThisReflection
				 */
				double meanStepSizeOnThisReflection = 2*tca;
				
				// TODO Check if there are sectors inside the receptor's volume
				
				double distance = rayLength + meanStepSizeOnThisReflection;
				double time = distance / soundSpeed;
				
				double receptedEnergy = rayEnergy
						* Math.pow(Math.E, -1 * airAbsorptionCoeficient
								* meanStepSizeOnThisReflection) * tca
						/ radius;
				
				simulatedImpulseResponse.addValue((float)time, (float)receptedEnergy);
				
				return true;
			}
		}
		
		return false;
		
	}
}
