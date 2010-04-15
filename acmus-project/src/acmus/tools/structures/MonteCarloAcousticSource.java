package acmus.tools.structures;

import java.util.ArrayList;
import java.util.List;

import acmus.tools.rtt.Ray;


/**
 * Random Acoustic Source, generated using uniform random sample
 * 
 * This class is used by the Strategy implemented in RayTracingDelegate
 * 
 * @author mhct
 * 
 */
public class MonteCarloAcousticSource implements AcousticSource {
private final Vector center;
private final double energy;
	
	public MonteCarloAcousticSource(Vector center){
		this.center = center;
		this.energy = 1.0;
	}

	public Vector getCenter() {
		return new Vector(this.center);
	}
	
	public double getEnergy() {
		return this.energy;
	}
	
	public Ray generate(){
		return new Ray(getEnergy(), getCenter(), newDirection());
	}
	
	public List<Ray> generate(int n){
		List<Vector> directions = manyDirections(n);
		List<Ray> rays = new ArrayList<Ray>(n);
		for(int i = 0; i < n; i++){
			rays.add(new Ray(energy, center, directions.get(i)));
		}
		return rays;
	}
	
	public Vector newDirection() {
		float x, y, z;
		do {
			x = 2 * (float) Math.random() - 1; 
			y = 2 * (float) Math.random() - 1;
			z = 2 * (float) Math.random() - 1;
		} while (x*x + y*y + z*z > 1);
		return (new Vector(x, y, z)).normalized();
	}
	
	public List<Vector> manyDirections(int n) {
		List<Vector> sphericalPoints = new ArrayList<Vector>();
		int i = 0;
		while(i < n) {
			float x = 2 * (float) Math.random() - 1; 
			float y = 2 * (float) Math.random() - 1;
			float z = 2 * (float) Math.random() - 1;
			
			if(x*x + y*y + z*z <= 1) {
				sphericalPoints.add((new Vector(x, y, z)).normalized());
				i++;
			}
		}
		
		return sphericalPoints;
	}
}
