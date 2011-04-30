package acmus.simulation.structures;

import acmus.util.math.Vector;

public final class ResisableSphericalReceptor extends SphericalReceptor{

	public ResisableSphericalReceptor(Vector center, float radius) {
		super(center, radius);
	}
	
	public void setRadius(float radius){
		super.radius = radius;
	}
}
