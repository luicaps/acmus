package acmus.simulation;

import acmus.simulation.math.Vector;
import acmus.simulation.structures.DirectionImpulseResponse;

/**
 * This interface is used to provide a uniform way to RayTracing use the
 * Strategy method, to check if the receptor is intercepted
 * 
 * @author mahtorres
 * @author migmruiz
 * 
 */
public interface Receptor {
	/**
	 * @param airAbsorptionCoefficient
	 *            the air absorption coefficient
	 * @param soundSpeed
	 *            the sound speed
	 * @param rayOrigin
	 *            the ray's origin on this step
	 * @param rayDirection
	 *            the ray's actual direction
	 * @param rayEnergy
	 *            the ray's actual energy
	 * @param rayLength
	 *            the ray's actual length
	 * @return <code>true</code> if the Ray is intercepts the instance and
	 *         <code>false</code> if does not
	 */
	public boolean intercept(double airAbsorptionCoefficient,
			double soundSpeed, Vector rayOrigin, Vector rayDirection,
			float rayEnergy, float rayLength);
	
	/**
	 * @return the simulated impulse response
	 */
	public SimulatedImpulseResponse getSimulatedImpulseResponse();
	
	/**
	 * @return the simulated directional impulse response
	 */
	public DirectionImpulseResponse getDirectionImpulseResponse();
	
	/**
	 * @return the Receptor instance's center
	 */
	public Vector getCenter();
	
	/**
	 * @return the Receptor instance's radius
	 */
	public float getRadius();
}
