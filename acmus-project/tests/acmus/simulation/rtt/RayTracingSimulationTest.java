package acmus.simulation.rtt;

import static org.junit.Assert.assertNotNull;
// import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
// import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.swt.widgets.ProgressBar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import acmus.simulation.AcousticSource;
import acmus.simulation.GeometricAcousticSimulation;
import acmus.simulation.math.Vector;
import acmus.simulation.rtt.RayTracingGeometricAcousticSimulationImpl;
import acmus.simulation.rtt.Sector;
import acmus.simulation.structures.ArbitraryAcousticSource;
import acmus.simulation.structures.MonteCarloAcousticSource;

public class RayTracingSimulationTest {

	private AcousticSource soundSource;
	private int numberOfRays;
	private Vector soundSourceCenter;
	ArbitraryAcousticSource arbitarySoundSource;
	private Vector sphericalReceptorCenter;
	private double sphericalReceptorRadius;
	private double soundSpeed;
//	private int initialEnergy;
	private double mCoeficient;
	private int k;
	private List<Sector> sectors;
	private ProgressBar bar;

//	private static float EPS = 0.00001f; 
	
	@Before
	public void setUp(){
		soundSourceCenter = new Vector(2, 2, 5);
		arbitarySoundSource = new ArbitraryAcousticSource(soundSourceCenter);
		numberOfRays = 2;
		arbitarySoundSource.add(new Vector(0.7071f, 0.7071f, 0f)); //vetor (1,1,0)
		arbitarySoundSource.add(new Vector(0.7022468831767834f, 0.7022468831767834f, 0.11704114719613057f));
		
		sectors = new ArrayList<Sector>();
		sectors.add(new Sector(new Vector(0, 0, 1), new Vector(1, 1, 0), 0.02)); // base
		sectors.add(new Sector(new Vector(0, 0, -1), new Vector(1, 1, 10), 0.02)); // topo
		sectors.add(new Sector(new Vector(0, 1, 0), new Vector(1, 0, 1), 0.02)); 
		sectors.add(new Sector(new Vector(1, 0, 0), new Vector(0, 1, 1), 0.02));
		sectors.add(new Sector(new Vector(0, -1, 0), new Vector(1, 10, 1), 0.02));
		sectors.add(new Sector(new Vector(-1, 0, 0), new Vector(10, 1, 1), 0.02));
		
		sphericalReceptorCenter = new Vector(8, 8, 6);
		sphericalReceptorRadius = 3.0;
		
		soundSpeed = 344.0; // em metros por segundo (m/s)
//		initialEnergy = 10000000;
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
	public void testRayTracingSimulation() {
		RayTracingGeometricAcousticSimulationImpl rts = new RayTracingGeometricAcousticSimulationImpl(sectors, soundSource, numberOfRays, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, mCoeficient, k);
		
		assertNotNull(rts);
	}
	

	@Test
	public void testSimulate() throws FileNotFoundException, IOException {
		// FIXME this test is not testing anything
		RayTracingGeometricAcousticSimulationImpl rts = new RayTracingGeometricAcousticSimulationImpl(sectors, soundSource, numberOfRays, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, mCoeficient, k);

		rts.simulate(bar);
		
//		Iterator<Double> itr = rts.getSimulatedImpulseResponse().keySet().iterator();

//		Double expected = 0.01611629;
//		assertTrue(Math.abs(expected - itr.next()) < EPS);
//		
//		expected = 0.016444344;
//		assertTrue(Math.abs(expected - itr.next()) < EPS);
		
	}

	@Test
	public void testSimulateWallShocking() {
		// FIXME this test is not testing anything
		ArbitraryAcousticSource arbitrarySoundSource =
			new ArbitraryAcousticSource(soundSourceCenter);
		arbitrarySoundSource.add(new Vector(0.457495710997814f, 0.457495710997814f, 0.7624928516630234f));
		numberOfRays = 1;

		GeometricAcousticSimulation rts = new RayTracingGeometricAcousticSimulationImpl(sectors, arbitrarySoundSource, numberOfRays , sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, mCoeficient, k);
		
		rts.simulate(bar);
		
//		Iterator<Double> itr = rts.getSimulatedImpulseResponse().keySet().iterator();
//
//		Double expected = 0.02739239;
//		assertTrue(Math.abs(expected - itr.next()) < EPS);

	}
	
	@Test
	public void variosPontos() throws FileNotFoundException, IOException{
		// FIXME this test is not testing anything
		soundSource = new MonteCarloAcousticSource(soundSourceCenter);
		numberOfRays = 500000;
		sphericalReceptorCenter = new Vector(6, 6, 6);
		sphericalReceptorRadius = 0.5;

		GeometricAcousticSimulation rts = new RayTracingGeometricAcousticSimulationImpl(sectors, soundSource, numberOfRays, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, mCoeficient, k);
		long ti = System.currentTimeMillis();
		rts.simulate(bar);
		System.out.println("time: " + (System.currentTimeMillis() - ti) + " ms");
//		try{
//			rts.lista();
//		}
//		catch(IOException e){
//			e.printStackTrace();
//		}
//		ChartBuilder g = new ChartBuilder(rts.getSphericalReceptorHistogram());
//		g.criaGrafico("");
//		g.salvar(new FileOutputStream("histograma.jpg"));
	}

	/**
	 * Tests if the center of the MonteCarloRandomAcousticSource is fixed as
	 * expected or if it's moving after some simulation.
	 */
	@Test
	public void testCenterIsFixed() {
		Assert.assertEquals(soundSourceCenter, soundSource.getCenter());
		Assert.assertEquals(soundSourceCenter, arbitarySoundSource.getCenter());
	}
	
	public RayTracingSimulationTest() {
		setUp();
		soundSource = new MonteCarloAcousticSource(soundSourceCenter);
		long ti = System.currentTimeMillis();
		int numberOfRays = 10000;
		sphericalReceptorCenter = new Vector(6, 6, 6);
		sphericalReceptorRadius = 0.5;

		GeometricAcousticSimulation rts = new RayTracingGeometricAcousticSimulationImpl(sectors, soundSource, numberOfRays, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, mCoeficient, k);
		
		rts.simulate(bar);
		System.out.println("time: " + (System.currentTimeMillis() - ti) + " ms");
	}

}
