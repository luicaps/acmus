package acmus.tools.rtt;

import java.util.Map;

import org.eclipse.swt.widgets.ProgressBar;

public interface GeometricAcousticSimulation {
	public void simulate(final ProgressBar progressBar);
	public Map<Double, Double> getReceptorHistogram();
}
