package acmus.tools.structures;

import java.util.ArrayList;
import java.util.List;

import acmus.tools.rtt.Ray;


public class NormalDeviateAcousticSource implements AcousticSource {
private Vector center;
private double energy;
	
	public NormalDeviateAcousticSource(Vector center){
		this.center = center;
		this.energy = 1.0;
	}

	public Vector getCenter() {
		return new Vector(this.center);
	}
	
	public double getEnergy() {
		return this.energy;
	}
	
	public Ray generate() {
		return new Ray(energy, center, newDirection());
	}

	public List<Ray> generate(int n) {
		List<Vector> directions = manyDirections(n);
		List<Ray> rays = new ArrayList<Ray>(n);
		for(int i = 0; i < n; i++){
			rays.add(new Ray(energy, center, directions.get(i)));
		}
		return rays;
	}
	
	public Vector newDirection() {
		double u1 = Math.random();
		double u2 = Math.random();
		float x = (float) (Math.sqrt(-2*Math.log(u1)) * Math.cos(2*Math.PI * u2));
		float y = (float) (Math.sqrt(-2*Math.log(u1)) * Math.sin(2*Math.PI * u2));
		float z = (float) (Math.sqrt(-2*Math.log(Math.random())) * Math.cos(2*Math.PI * Math.random()));
		
		float t = 1/(float)Math.sqrt(x*x + y*y + z*z);
		
		return new Vector(x*t, y*t, z*t);
	}
	
	public List<Vector> manyDirections(int n) {
		List<Vector> directions = new ArrayList<Vector>(n);

		for(int i=0; i<n; i++) {
			directions.add(newDirection());
		}
		
		return directions;
	}
}
