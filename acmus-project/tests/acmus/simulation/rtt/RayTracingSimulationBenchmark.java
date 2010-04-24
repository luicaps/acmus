package acmus.simulation.rtt;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.eclipse.swt.widgets.ProgressBar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import acmus.simulation.AcousticSource;
import acmus.simulation.GeometricAcousticSimulation;
import acmus.simulation.Receptor;
import acmus.simulation.math.Vector;
import acmus.simulation.rtt.RayTracingGeometricAcousticSimulationImpl;
import acmus.simulation.rtt.Sector;
import acmus.simulation.structures.EnergeticSimulatedImpulseResponse;
import acmus.simulation.structures.MonteCarloAcousticSource;
import acmus.simulation.structures.SphericalReceptor;
import acmus.tools.RayTracing;

public class RayTracingSimulationBenchmark {

	private int numberOfRays;
	private AcousticSource soundSource;
	private ArrayList<Sector> sectors;
	private Receptor receptor;
	private double soundSpeed;
	private double mCoeficient;
	private int k;
	private ProgressBar bar;

	@Before
	public void setUp() throws Exception {
		
		numberOfRays = 100000;
		
		Vector soundSourceCenter = new Vector(2, 2, 5);
		
		soundSource = new MonteCarloAcousticSource(soundSourceCenter);
		
		sectors = new ArrayList<Sector>();
		sectors.add(new Sector(new Vector(0, 0, 1), new Vector(1, 1, 0), 0.02)); // base
		sectors.add(new Sector(new Vector(0, 0, -1), new Vector(1, 1, 10), 0.02)); // topo
		sectors.add(new Sector(new Vector(0, 1, 0), new Vector(1, 0, 1), 0.02)); 
		sectors.add(new Sector(new Vector(1, 0, 0), new Vector(0, 1, 1), 0.02));
		sectors.add(new Sector(new Vector(0, -1, 0), new Vector(1, 10, 1), 0.02));
		sectors.add(new Sector(new Vector(-1, 0, 0), new Vector(10, 1, 1), 0.02));
		
		Vector sphericalReceptorCenter = new Vector(8, 8, 6);
		float sphericalReceptorRadius = 1.0f;
		receptor = new SphericalReceptor(sphericalReceptorCenter,
				sphericalReceptorRadius, new EnergeticSimulatedImpulseResponse(
						RayTracing.histogramInterval));

		soundSpeed = 344.0; // em metros por segundo (m/s)
		mCoeficient = 0.01;
		k = 500;

		Mockery mockery = new Mockery() {
			{
				setImposteriser(ClassImposteriser.INSTANCE);
			}
		};
		
		bar = mockery.mock(ProgressBar.class);
		mockery.checking(new Expectations() {
			{
				ignoring(bar);
			}
		});
	
	}

	@Test
	public void testSimulate() {
		
		System.out.println("\n" + "Ray tracing acoustic simulation" + "\n"
				+ "optimized");
		System.out.println("rays: " + numberOfRays);
		System.out.println("number of processors: "
				+ Runtime.getRuntime().availableProcessors());
		System.out.println("os.arch: " + System.getProperty("os.arch"));
		System.out.println("java.vm.name: "
				+ System.getProperty("java.vm.name"));
		System.out.println(System.getProperties() + "\n");
		System.out.println("time (ms): " + "\n"); 
		
		for (int i = 0; i < 100; i++) {
			GeometricAcousticSimulation gas = new RayTracingGeometricAcousticSimulationImpl(
					sectors, soundSource, numberOfRays, receptor, soundSpeed,
					mCoeficient, k);
			long ti = System.currentTimeMillis();
			gas.simulate(bar);
			long time = System.currentTimeMillis() - ti;

			System.out.println(time);
		}
	}

	@Test
	public void testGetSphericalReceptorHistogram() {
		fail("Not yet implemented");
	}

}
