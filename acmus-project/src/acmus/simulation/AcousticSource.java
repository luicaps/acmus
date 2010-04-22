package acmus.simulation;

import java.util.List;

import acmus.simulation.math.Vector;
import acmus.simulation.rtt.Ray;


/**
 * This interface is used to provide a uniform way to RayTracing use the
 * Strategy method, to receive generated points
 * 
 * @author mahtorres
 * 
 */
public interface AcousticSource {
	
	public Vector getCenter();
	
	public Ray generate();
	
	public List<Ray> generate(int n);

	public double getEnergy();
	
	public Vector newDirection();
	
	public List<Vector> manyDirections(int i);
	
}
