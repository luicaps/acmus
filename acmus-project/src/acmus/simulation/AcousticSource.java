package acmus.simulation;

import java.util.List;

import acmus.simulation.rtt.Ray;
import acmus.util.math.Vector;

/**
 * This interface is used to provide a uniform way to RayTracing use the
 * Strategy method, to receive generated Rays or direction Vectors
 * 
 * @author mahtorres
 * @author migmruiz
 * 
 */
public interface AcousticSource {

	/**
	 * @return a new position {@link Vector} of the AcousticSource instance's
	 *         center
	 */
	public Vector getCenter();
	
	/**
	 * @return the energy witch the {@link Ray}s are generated
	 */
	public double getEnergy();

	/**
	 * Generates a {@link Ray}
	 * 
	 * @return a {@link Ray} with a normalized direction starting from the
	 *         AcousticSource instance's center
	 */
	public Ray generate();

	/**
	 * Generates a List of {@link Ray}s
	 * 
	 * @return a List of {@link Ray}s with normalized directions starting from
	 *         the AcousticSource instance's center
	 */
	public List<Ray> generate(int n);

	/**
	 * Returns a direction {@link Vector}
	 * 
	 * @return a normalized {@link Vector}
	 */
	public Vector direction();

	/**
	 * Returns a List of many direction {@link Vector}s
	 * 
	 * @return a List of normalized {@link Vector}s
	 */
	public List<Vector> manyDirections(int i);

}
