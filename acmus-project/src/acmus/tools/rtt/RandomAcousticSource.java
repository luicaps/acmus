package acmus.tools.rtt;

import java.util.ArrayList;
import java.util.List;

import acmus.tools.structures.Vector;

/**
 * Random Acoustic Source, generated using uniform random sample
 * 
 * This class is used by the Strategy implemented in RayTracingDelegate
 * 
 * @author mhct
 * 
 */
public class RandomAcousticSource implements AcousticSource {

	public List<Vector> generate() {
		return this.generate(20000);
	}

	public List<Vector> generate(int n) {
		List<Vector> sphericalPoints = new ArrayList<Vector>();
		for(int i=0; i<n; i++) {
			float x = 2 * (float) Math.random() - 1; 
			float y = 2 * (float) Math.random() - 1;
			float z = 2 * (float) Math.random() - 1;
			
			if(x*x + y*y + z*z <= 1) {
				sphericalPoints.add(new Vector(x, y, z));
			}
		}
		
		return sphericalPoints;
	}
}
