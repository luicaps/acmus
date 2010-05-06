package acmus.simulation.rtt;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.swt.widgets.ProgressBar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import acmus.simulation.AcousticSource;
import acmus.simulation.Receptor;
import acmus.simulation.math.Vector;
import acmus.simulation.rtt.RayTracingGeometricAcousticSimulationImpl;
import acmus.simulation.rtt.Sector;
import acmus.simulation.structures.MonteCarloAcousticSource;
import acmus.simulation.structures.SphericalReceptor;

public class RayTracingSimulationCoherenceTest {

	private AcousticSource soundSource;
	private int numberOfRays;
	private Vector soundSourceCenter;
	Vector sphericalReceptorCenter;
	private Receptor receptor;
	private double soundSpeed;
	private double mCoeficient;
	private int k;
	private List<Sector> sectors;
	private ProgressBar bar;

//	private static float EPS = 0.00001f; 
	
	@Before
	public void setUp(){
		soundSourceCenter = new Vector(9.5f, 9.5f, 9.5f);
		soundSource = new MonteCarloAcousticSource(soundSourceCenter);
		
		numberOfRays = 50000;
		
		sectors = new ArrayList<Sector>();
		sectors.add(new Sector(new Vector( 0,  0,  1), new Vector( 1,  1,  0), 0.02)); // bottom
		sectors.add(new Sector(new Vector( 0,  0, -1), new Vector( 1,  1, 10), 0.02)); // top
		sectors.add(new Sector(new Vector( 0,  1,  0), new Vector( 1,  0,  1), 0.02)); 
		sectors.add(new Sector(new Vector( 0, -1,  0), new Vector( 1, 10,  1), 0.02));
		sectors.add(new Sector(new Vector( 1,  0,  0), new Vector( 0,  1,  1), 0.02));
		sectors.add(new Sector(new Vector(-1,  0,  0), new Vector(10,  1,  1), 0.02));
		
		sphericalReceptorCenter = new Vector(0.5f, 0.5f, 0.5f);
		float sphericalReceptorRadius = 0.15f;
		receptor = new SphericalReceptor(sphericalReceptorCenter,
				sphericalReceptorRadius);
		
		soundSpeed = 344.0; // in meters per second (m/s)
		mCoeficient = 0.0001;
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
		RayTracingGeometricAcousticSimulationImpl rts = new
				RayTracingGeometricAcousticSimulationImpl(
					sectors, soundSource, numberOfRays, receptor, 
					soundSpeed, mCoeficient, k );

		rts.simulate(bar);
		
		Map<Float, Float> ir = receptor.getSimulatedImpulseResponse()
				.getEnergeticImpulseResponse();
		float interval = receptor.getSimulatedImpulseResponse().getInterval();
		
		for (Map.Entry<Float, Float> e : ir.entrySet()) {
			if (e.getKey() < 0.0472) {
				if (e.getValue() != 0) {
					Assert.assertEquals(0.045848404f, e.getKey(), interval);
				}
			}
		}

	}
	
	/**
	 * Tests if the center of the MonteCarloRandomAcousticSource and of the SphericalReceptor
	 * are fixed as expected or if they are moving after some simulation.
	 */
	@Test
	public void testCenterIsFixed() {
		Assert.assertEquals(soundSourceCenter, soundSource.getCenter());
		Assert.assertEquals(sphericalReceptorCenter, receptor.getCenter());
	}

}
