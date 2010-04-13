package acmus.tools.rtt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
/*
 * unused imports ...
 * import java.util.HashMap;
 * import java.util.Iterator;
 */

import org.eclipse.swt.widgets.ProgressBar;

import acmus.tools.structures.AcousticSource;
import acmus.tools.structures.EnergeticSimulatedImpulseResponse;
import acmus.tools.structures.Sector;
import acmus.tools.structures.SimulatedImpulseResponse;
import acmus.tools.structures.Vector;
/* 
 * unused imports ...
 * import acmus.tools.structures.EnergeticSimulatedImpulseResponseArray;
 */

public class RayTracingGeometricAcousticSimulationImpl implements GeometricAcousticSimulation {

	private List<Sector> sectors;
	private int numberOfRays;
	private AcousticSource soundSource;
	private Vector sphericalReceptorCenter;
	double sphericalReceptorRadius;
	double soundSpeed;
	double airAbsorptionCoeficient;
	double k;
	private SimulatedImpulseResponse simulatedImpulseResponse;
	private float histogramInterval;
	

	public RayTracingGeometricAcousticSimulationImpl(List<Sector> sectors,
			AcousticSource soundSource, int numberOfRays, Vector sphericalReceptorCenter,
			double sphericalReceptorRadius, double soundSpeed,
			double airAbsortionCoeficient, int k) {
		this.sectors = sectors;
		this.numberOfRays = numberOfRays;
		this.soundSource = soundSource;
		this.sphericalReceptorCenter = sphericalReceptorCenter;
		this.sphericalReceptorRadius = sphericalReceptorRadius;
		this.soundSpeed = soundSpeed;
		this.airAbsorptionCoeficient = airAbsortionCoeficient;
		this.k = k;
		histogramInterval = 0.00001f;
		
		//interval calculated according to Gomes2008, see Mario h.c.t. Masters dissertation
		simulatedImpulseResponse = new EnergeticSimulatedImpulseResponse(histogramInterval);
	}

	public void simulate(final ProgressBar progressBar) {
		
		/*
		 * numberOfThreads including this one
		 */
		int numberOfThreads = 2;
		int numberOfRaysDivided = numberOfRays / numberOfThreads;
		
		/*
		 * Delegates to other threads
		 */
		Thread[] threads = new Thread[numberOfThreads - 1];
		for (int j = 1; j < numberOfThreads; j++) {
			threads[j -1] = simulateInNewThread(progressBar, numberOfRaysDivided);
		}
		
		/*
		 * Does some calculation on this thread
		 */
		int actualNumberOfRays = numberOfRaysDivided + 
								numberOfRays % numberOfThreads;
		int i = 0;
		for (; i < actualNumberOfRays; i++) {
			// Updates the progressBar
			if (i % Math.max(1,(actualNumberOfRays/100)) == 0) {
				progressBar.setSelection((int) (100.0*i/actualNumberOfRays));
			}
			Ray ray = soundSource.generate();
			ray.trace(sphericalReceptorCenter, sphericalReceptorRadius,
					sectors, soundSpeed, airAbsorptionCoeficient, k,
					simulatedImpulseResponse);
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

	private Thread simulateInNewThread(final ProgressBar progressBar, final int raysDivided) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				int i = 0;
				for (; i < raysDivided ; i++) {
					// TODO make progressBar update thread-safe
					Ray ray = soundSource.generate();
					ray.trace(sphericalReceptorCenter, sphericalReceptorRadius,
							sectors, soundSpeed, airAbsorptionCoeficient, k,
							simulatedImpulseResponse);
				}
			}
		});
		t.start();
		return t;
		
	}

	public SimulatedImpulseResponse getSimulatedImpulseResponse() {
		return simulatedImpulseResponse;
	}

	public void lista() throws IOException {
		FileWriter fw = new FileWriter("/tmp/hist.txt");
		StringBuilder sx = new StringBuilder(2000);
		StringBuilder sy = new StringBuilder(2000);
		StringBuilder ss = new StringBuilder(2000);

		for (Map.Entry<Float, Float> e : getSimulatedImpulseResponse().getEnergeticImpulseResponse().entrySet()) {
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
