package acmus.tools.rtt;


import org.eclipse.swt.widgets.ProgressBar;

import acmus.tools.structures.SimulatedImpulseResponse;

public interface GeometricAcousticSimulation {
	public void simulate(final ProgressBar progressBar);
	public SimulatedImpulseResponse getSimulatedImpulseResponse();
}
