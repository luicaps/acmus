package acmus.tools.structures;

import java.util.ArrayList;
import java.util.List;


/**
 * Random Acoustic Source, generated using uniform random sample
 * 
 * This class is used by the Strategy implemented in RayTracingDelegate
 * 
 * @author mhct
 * 
 */
public class MonteCarloAcousticSourceImpl implements AcousticSource {

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
