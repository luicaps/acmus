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
public class MonteCarloAcousticSource implements AcousticSource {
private Vector center;
	
	public MonteCarloAcousticSource(Vector center){
		this.center = center;
	}

	public Vector getCenter() {
		return this.center;
	}
	
	public Vector generate() {
		float x, y, z;
		do {
			x = 2 * (float) Math.random() - 1; 
			y = 2 * (float) Math.random() - 1;
			z = 2 * (float) Math.random() - 1;
		} while (x*x + y*y + z*z > 1);
		return new Vector(x, y, z).normalize();
	}
	
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
