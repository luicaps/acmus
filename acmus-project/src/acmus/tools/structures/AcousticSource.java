package acmus.tools.structures;

import java.util.List;


/**
 * This interface is used to provide a uniform way to RayTracing use the
 * Strategy method, to receive generated points
 * 
 * @author mahtorres
 * 
 */
public interface AcousticSource {
	
	public Vector getCenter();
	
	public Vector generate();
	
	public List<Vector> generate(int n);
	
}
