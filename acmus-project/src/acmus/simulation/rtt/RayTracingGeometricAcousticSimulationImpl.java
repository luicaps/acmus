package acmus.simulation.rtt;

import java.util.List;

import org.eclipse.swt.widgets.ProgressBar;

import acmus.simulation.AcousticSource;
import acmus.simulation.GeometricAcousticSimulation;
import acmus.simulation.Receptor;

public class RayTracingGeometricAcousticSimulationImpl implements GeometricAcousticSimulation {

	private List<Sector> sectors;
	private int numberOfRays;
	private AcousticSource soundSource;
	double soundSpeed;
	double airAbsorptionCoeficient;
	double k;
	private Receptor receptor;
	

	public RayTracingGeometricAcousticSimulationImpl(List<Sector> sectors,
			AcousticSource soundSource, int numberOfRays, Receptor receptor, double soundSpeed,
			double airAbsortionCoeficient, int k) {
		this.sectors = sectors;
		this.numberOfRays = numberOfRays;
		this.soundSource = soundSource;
		this.receptor = receptor;
		this.soundSpeed = soundSpeed;
		this.airAbsorptionCoeficient = airAbsortionCoeficient;
		this.k = k;
	}

	public void simulate(final ProgressBar progressBar) {
		
		/*
		 * number of threads including this one
		 */
		int numberOfThreads = Runtime.getRuntime().availableProcessors();
		int numberOfRaysDivided = numberOfRays / numberOfThreads;
		
		/*
		 * Delegates to other threads
		 */
		Thread[] threads = new Thread[numberOfThreads - 1];
		for (int j = 1; j < numberOfThreads; j++) {
			threads[j - 1] = simulateInNewThread(numberOfRaysDivided);
		}
		
		/*
		 * Does some calculation on this thread
		 */
		int actualNumberOfRays = numberOfRaysDivided + 
								numberOfRays % numberOfThreads;
		int i = 0;
		for (; i < actualNumberOfRays; i++) {
			// Updates the progressBar // TODO realistic progressBar updating
			if (i % Math.max(1,(actualNumberOfRays/100)) == 0) {
				progressBar.setSelection((int) (100.0*i/actualNumberOfRays));
			}
			Ray ray = soundSource.generate();
			ray.trace(receptor, sectors, soundSpeed,
						airAbsorptionCoeficient, k);
		}
		
		/*
		 * Waits for other threads to die
		 */
		for (int j = 1; j < numberOfThreads; j++) {
			try {
				threads[j -1].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Thread simulateInNewThread(final int raysDivided) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				int i = 0;
				for (; i < raysDivided ; i++) {
					// TODO make progressBar update thread-safe
					Ray ray = soundSource.generate();
					ray.trace(receptor, sectors, soundSpeed,
								airAbsorptionCoeficient, k);
				}
			}
		});
		t.start();
		return t;
		
	}
}
