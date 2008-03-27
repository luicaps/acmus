package acmus.tools.rtt;

import java.util.ArrayList;
import java.util.List;

import acmus.tools.structures.Triade;

/**
 * Random Acoustic Source, generated using uniform random sample
 * 
 * This class is used by the Strategy implemented in RayTracingDelegate
 * @author mahtorres
 *
 */
public class RandomAcousticSource implements AcousticSource {

	List<Triade> sphericalPoints;
	double x, y, z;
	
	public List<Triade> generate() {
		return this.generate(20000);
	}

	public List<Triade> generate(int n) {
		sphericalPoints = new ArrayList<Triade>();
	
		double theta = 0.0;
		double phi = 0.0;
		
		for(int i=0; i<n; i++)
		{
			phi = Math.PI * 2 * Math.random();
			theta = Math.PI*Math.random() - Math.PI/2;
//			sphericalPoints.add(new Triade(1.0, theta, phi));
			sphericalPoints.add(this.spherical2cartesian(1.0, theta, phi));
		}
		
		return sphericalPoints;
	}
	
	private Triade spherical2cartesian(double r, double theta, double phi) {
		x = r * Math.sin(phi) * Math.cos(theta);
		y = r * Math.sin(phi) * Math.sin(theta);
		z = r * Math.cos(phi);
		
		return new Triade(x, y, z);
	}

	
}
