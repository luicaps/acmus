package acmus.tools.rtt;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.ProgressBar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import acmus.tools.structures.AcousticSource;
import acmus.tools.structures.MonteCarloAcousticSource;
import acmus.tools.structures.NormalSector;
import acmus.tools.structures.Vector;

public class RayTracingSimulationBenchmark {

	private int numberOfRays;
	private AcousticSource soundSource;
	private ArrayList<NormalSector> sectors;
	private Vector sphericalReceptorCenter;
	private double sphericalReceptorRadius;
	private double soundSpeed;
	private int initialEnergy;
	private double mCoeficient;
	private int k;
	private ProgressBar bar;

	@Before
	public void setUp() throws Exception {
		
		numberOfRays = 100000;
		
		Vector soundSourceCenter = new Vector(2, 2, 5);
		
		soundSource = new MonteCarloAcousticSource(soundSourceCenter);
		
		sectors = new ArrayList<NormalSector>();
		sectors.add(new NormalSector(new Vector(0, 0, 1), new Vector(1, 1, 0), 0.02)); // base
		sectors.add(new NormalSector(new Vector(0, 0, -1), new Vector(1, 1, 10), 0.02)); // topo
		sectors.add(new NormalSector(new Vector(0, 1, 0), new Vector(1, 0, 1), 0.02)); 
		sectors.add(new NormalSector(new Vector(1, 0, 0), new Vector(0, 1, 1), 0.02));
		sectors.add(new NormalSector(new Vector(0, -1, 0), new Vector(1, 10, 1), 0.02));
		sectors.add(new NormalSector(new Vector(-1, 0, 0), new Vector(10, 1, 1), 0.02));
		
		sphericalReceptorCenter = new Vector(8, 8, 6);
		sphericalReceptorRadius = 1.0;
		
		soundSpeed = 344.0; // em metros por segundo (m/s)
		initialEnergy = 10000000;
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

		GeometricAcousticSimulation gas = new RayTracingGeometricAcousticSimulationImpl(
				sectors, soundSource, numberOfRays, sphericalReceptorCenter,
				sphericalReceptorRadius, soundSpeed, mCoeficient, k);
		long ti = System.currentTimeMillis();
		gas.simulate(bar);
		long time = System.currentTimeMillis() - ti;

		System.out.println("Ray tracing acoustic simulation");
		System.out.println("optimized");
		System.out.println("rays: " + numberOfRays);
		System.out.println("time (ms): " + time);

	}

	@Test
	public void testGetSphericalReceptorHistogram() {
		fail("Not yet implemented");
	}

}
