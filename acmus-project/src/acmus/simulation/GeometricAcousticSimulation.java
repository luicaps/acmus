package acmus.simulation;


import org.eclipse.swt.widgets.ProgressBar;


public interface GeometricAcousticSimulation {
	public void simulate(final ProgressBar progressBar);
	public SimulatedImpulseResponse getSimulatedImpulseResponse();
}
