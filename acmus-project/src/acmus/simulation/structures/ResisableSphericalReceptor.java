package acmus.simulation.structures;

import acmus.simulation.SimulatedImpulseResponse;
import acmus.simulation.math.Vector;

public final class ResisableSphericalReceptor extends SphericalReceptor{

	public ResisableSphericalReceptor(Vector center, float radius, SimulatedImpulseResponse simulatedImpulseResponse) {
		super(center, radius, simulatedImpulseResponse);
	}
	
	public void setRadius(float radius){
		super.radius = radius;
	}
}
