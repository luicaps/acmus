package acmus.simulation;

import org.eclipse.swt.widgets.ProgressBar;

/**
 * This interface is used to provide a uniform way to the final user to perform
 * a simulation using the Strategy method
 * 
 * @author mahtorres
 * 
 */
public interface GeometricAcousticSimulation {
	/**
	 * Perform the simulation
	 * 
	 * @param progressBar the simulation's progress bar
	 */
	public void simulate(final ProgressBar progressBar);

}
