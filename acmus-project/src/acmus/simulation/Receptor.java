package acmus.simulation;

import acmus.simulation.math.Vector;

public interface Receptor {
	public boolean intercept(double airAbsorptionCoeficient,
			double soundSpeed, Vector rayOrigin, Vector rayDirection,
			float rayEnergy, float rayLength,
			SimulatedImpulseResponse simulatedImpulseResponse);
}
