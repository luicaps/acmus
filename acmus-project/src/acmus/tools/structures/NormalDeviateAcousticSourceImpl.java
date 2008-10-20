package acmus.tools.structures;

import java.util.ArrayList;
import java.util.List;


public class NormalDeviateAcousticSourceImpl implements AcousticSource {

	public List<Vector> generate(int n) {
		List<Vector> sphericalPoints = new ArrayList<Vector>(n);

		for(int i=0; i<n; i++) {
			double u1 = Math.random();
			double u2 = Math.random();
			float x = (float) (Math.sqrt(-2*Math.log(u1)) * Math.cos(2*Math.PI * u2));
			float y = (float) (Math.sqrt(-2*Math.log(u1)) * Math.sin(2*Math.PI * u2));
			float z = (float) (Math.sqrt(-2*Math.log(Math.random())) * Math.cos(2*Math.PI * Math.random()));
			
			float t = 1/(float)Math.sqrt(x*x + y*y + z*z);
		
			sphericalPoints.add(new Vector(x*t, y*t, z*t));
		}
		
		return sphericalPoints;
	}

}
