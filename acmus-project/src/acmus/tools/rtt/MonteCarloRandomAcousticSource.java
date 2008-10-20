package acmus.tools.rtt;

import java.util.ArrayList;
import java.util.List;

import acmus.tools.structures.AcousticSource;
import acmus.tools.structures.Vector;

/**
 * Random Acoustic Source, generated using uniform random sample
 * 
 * This class is used by the Strategy implemented in RayTracingDelegate
 * 
 * @author mhct
 * 
 */
public class MonteCarloRandomAcousticSource implements AcousticSource {

	public List<Vector> generate(int n) {
		List<Vector> sphericalPoints = new ArrayList<Vector>();
		int i = 0;
		while(i < n) {
			float x = 2 * (float) Math.random() - 1; 
			float y = 2 * (float) Math.random() - 1;
			float z = 2 * (float) Math.random() - 1;
			
			if(x*x + y*y + z*z <= 1) {
				sphericalPoints.add(new Vector(x, y, z).normalize());
				i++;
			}
		}
		
		return sphericalPoints;
	}
}
