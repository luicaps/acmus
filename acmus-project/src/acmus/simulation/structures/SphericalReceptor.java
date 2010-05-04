package acmus.simulation.structures;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import acmus.simulation.Receptor;
import acmus.simulation.SimulatedImpulseResponse;
import acmus.simulation.math.Vector;

public class SphericalReceptor implements Receptor{
	private Vector center;
	protected float radius;
	private SimulatedImpulseResponse simulatedImpulseResponse;
	
	public SphericalReceptor(Vector center, float radius,
			SimulatedImpulseResponse simulatedImpulseResponse) {
		this.center = center;
		this.radius = radius;
		this.simulatedImpulseResponse = simulatedImpulseResponse;
	}
	
	public SphericalReceptor(Vector center, float radius, float interval) {
		this(center, radius, new EnergeticSimulatedImpulseResponse(
				interval));
	}
	
	/**
	 * The default constructor
	 * uses 1/44100 as the interval for the impulse response
	 * 
	 * @param center the center of the spherical receptor's location
	 * @param radius the spherical receptor's radius of audition
	 */
	public SphericalReceptor(Vector center, float radius) {
		
		this(center, radius, (float) 1/44100 /*was 0.00001f*/);
		//interval calculated according to Gomes2008, see Mario h.c.t. Masters dissertation
	}
	
	public Vector getCenter() {
		return new Vector(center);
	}

	public float getRadius() {
		return radius;
	}

	public boolean intercept(double airAbsorptionCoeficient,
			double soundSpeed, Vector rayOrigin, Vector rayDirection,
			float rayEnergy, float rayLength) {
		
		// Local variables for better legibility and better performance
		Vector originToCenter = center.sub(rayOrigin);
		double tca = originToCenter.dotProduct(rayDirection);

		/*
		 * As seen in Kulowski, tca > 0 says that the ray is not opposed to the
		 * oldPositionToCenter direction
		 */
		if (tca > 0) {

			/**
			 * Discriminant for solving in terms of stepSizeOnThisReflection 
			 * (or s) the equation below
			 * 
			 * rayOrgin.add(rayDirection.times(stepSizeOnThisReflection)).sub(
			 * sphericalReceptorCenter).squared() <= sphericalReceptorRadius
			 * 
			 * or, in terms of P, s, D, C and R:
			 * 
			 * || P + s*D - C ||^2 <= R^2
			 * 
			 * direction (or D) is supposed with norm 1
			 * 
			 * constants are ignored as the only purpose of it is
			 * to check if it's greater then zero
			 */
			double discriminant = radius*radius - originToCenter.normSquared() + tca*tca;

			if (discriminant > 0) { // ray V intercepts spherical receptor
				
				float stepSizeOnThisReflection = originToCenter.norm();
				
				// TODO Check if there are sectors inside the receptor's volume
				
				double distance = rayLength + stepSizeOnThisReflection;
				float time = (float) (distance / soundSpeed);
				
				/*float distanceFromListener = ((rayDirection
						.times((float) stepSizeOnThisReflection))
						.sub(originToCenter)).length();
				
				if(distanceFromListener > radius){
					System.out.println("distancia do ouvinte maior que o raio");
				}*/
				
//				coherency test
//				if(time < 0.006){
//					System.out.println("stepSizeOnThisReflection : " + stepSizeOnThisReflection);
//					System.out.println("rayLength : " + rayLength);
//				}
				
				// Adjusts the energy to correspond the distance between the
				// intercepted ray position and the receptor's center
				float receptedEnergy = (float) (rayEnergy
						* Math.pow(Math.E, -1 * airAbsorptionCoeficient
								* stepSizeOnThisReflection)       );
//						* (radius - distanceFromListener) / radius);
				
				simulatedImpulseResponse.addValue(time, receptedEnergy);
				
				return true;
			}
		}
		
		return false;
		
	}

	public SimulatedImpulseResponse getSimulatedImpulseResponse() {
		return this.simulatedImpulseResponse;
	}
	
	public void lista() throws IOException {
		FileWriter fw = new FileWriter(System.getProperty("java.io.tmpdir",
				"/tmp/")
				+ "hist.txt");
		StringBuilder sx = new StringBuilder(2000);
		StringBuilder sy = new StringBuilder(2000);
		StringBuilder ss = new StringBuilder(2000);

		for (Map.Entry<Float, Float> e : getSimulatedImpulseResponse()
				.getEnergeticImpulseResponse().entrySet()) {
			sx.append(e.getKey());
			sx.append(" ");
			sy.append(e.getValue());
			sy.append(" ");

			ss.append(e.getKey());
			ss.append("\t");
			ss.append(e.getValue());
			ss.append("\n");

		}
		// fw.write("x=[" + sx.toString() + "0]; y=[" + sy.toString() + "0]");
		fw.write(ss.toString());
		fw.close();
	}
}
