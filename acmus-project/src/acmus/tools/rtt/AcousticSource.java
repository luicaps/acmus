package acmus.tools.rtt;

import java.util.List;

import acmus.tools.structures.Triade;

/**
 * This interface is used to provide a uniform way to RayTracing use the
 * Strategy method, to receive generated points
 * 
 * @author mahtorres
 * 
 */
public interface AcousticSource {
	public List<Triade> generate();

	public List<Triade> generate(int n);
}
