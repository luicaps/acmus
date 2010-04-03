package acmus.tools.structures;

import java.util.ArrayList;
import java.util.List;


public class NormalDeviateAcousticSource implements AcousticSource {
private Vector center;
	
	public NormalDeviateAcousticSource(Vector center){
		this.center = center;
	}

	public Vector getCenter() {
		return this.center;
	}
	
	public Vector generate() {
		double u1 = Math.random();
		double u2 = Math.random();
		float x = (float) (Math.sqrt(-2*Math.log(u1)) * Math.cos(2*Math.PI * u2));
		float y = (float) (Math.sqrt(-2*Math.log(u1)) * Math.sin(2*Math.PI * u2));
		float z = (float) (Math.sqrt(-2*Math.log(Math.random())) * Math.cos(2*Math.PI * Math.random()));
		
		float t = 1/(float)Math.sqrt(x*x + y*y + z*z);
		
		return new Vector(x*t, y*t, z*t);
	}
	
	public List<Vector> generate(int n) {
		List<Vector> sphericalPoints = new ArrayList<Vector>(n);

		for(int i=0; i<n; i++) {
			sphericalPoints.add(generate());
		}
		
		return sphericalPoints;
	}
}
