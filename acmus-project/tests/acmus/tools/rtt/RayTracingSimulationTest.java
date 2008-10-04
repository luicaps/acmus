package acmus.tools.rtt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.ProgressBar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import acmus.tools.structures.NormalSector;
import acmus.tools.structures.Vector;

public class RayTracingSimulationTest {

	private List<Vector> vectors;
	private Vector soundSourceCenter;
	private Vector sphericalReceptorCenter;
	private double sphericalReceptorRadius;
	private double soundSpeed;
	private int initialEnergy;
	private double mCoeficient;
	private int k;
	private List<NormalSector> sectors;
	private ProgressBar bar;

	private static float EPS = 0.00001f; 
	
	@Before
	public void setUp(){
		vectors = new ArrayList<Vector>();
		vectors.add(new Vector(0.7071f, 0.7071f, 0f)); //vetor (1,1,0)
		vectors.add(new Vector(0.7022468831767834f, 0.7022468831767834f, 0.11704114719613057f));
		
		sectors = new ArrayList<NormalSector>();
		sectors.add(new NormalSector(new Vector(0, 0, 1), new Vector(1, 1, 0), 0.02)); // base
		sectors.add(new NormalSector(new Vector(0, 0, -1), new Vector(1, 1, 10), 0.02)); // topo
		sectors.add(new NormalSector(new Vector(0, 1, 0), new Vector(1, 0, 1), 0.02)); 
		sectors.add(new NormalSector(new Vector(1, 0, 0), new Vector(0, 1, 1), 0.02));
		sectors.add(new NormalSector(new Vector(0, -1, 0), new Vector(1, 10, 1), 0.02));
		sectors.add(new NormalSector(new Vector(-1, 0, 0), new Vector(10, 1, 1), 0.02));
		
		soundSourceCenter = new Vector(2, 2, 5);
		sphericalReceptorCenter = new Vector(8, 8, 6);
		sphericalReceptorRadius = 3.0;
		
		soundSpeed = 344.0; // em metros por segundo (m/s)
		initialEnergy = 10000000;
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
		RayTracingSimulation rts = new RayTracingSimulation(sectors, vectors, soundSourceCenter, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, initialEnergy, mCoeficient, k);
		
		assertNotNull(rts);
	}
	

	@Test
	public void testSimulate() throws FileNotFoundException, IOException {
		RayTracingSimulation rts = new RayTracingSimulation(sectors, vectors, soundSourceCenter, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, initialEnergy, mCoeficient, k);

		rts.simulate(bar);
		
		Iterator<Double> itr = rts.getSphericalReceptorHistogram().keySet().iterator();

		Double expected = 0.01611629;
		assertTrue(Math.abs(expected - itr.next()) < EPS);
		
		expected = 0.016444344;
		assertTrue(Math.abs(expected - itr.next()) < EPS);
		
	}

	@Test
	public void testSimulateWallShocking() {
		vectors = new ArrayList<Vector>();
		vectors.add(new Vector(0.457495710997814f, 0.457495710997814f, 0.7624928516630234f));

		RayTracingSimulation rts = new RayTracingSimulation(sectors, vectors, soundSourceCenter, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, initialEnergy, mCoeficient, k);
		
		rts.simulate(bar);
		
		Iterator<Double> itr = rts.getSphericalReceptorHistogram().keySet().iterator();

		Double expected = 0.02739239;
		assertTrue(Math.abs(expected - itr.next()) < EPS);

	}
	
	@Test
	public void variosPontos() throws FileNotFoundException, IOException{
		//FIXME this test isnt testing anything
		MonteCarloRandomAcousticSource ras = new MonteCarloRandomAcousticSource();
		List<Vector> meusVetores = ras.generate(50000);
		sphericalReceptorCenter = new Vector(6, 6, 6);
		sphericalReceptorRadius = 0.5;
		initialEnergy = 1000;
//		meusVetores.add(new Triade(0.7071, 0.7071, 0)); //vetor (1,1,0)
//		meusVetores.add(new Triade(1, 1, 0.01).normalize()); //vetor (1,1,0)
		RayTracingSimulation rts = new RayTracingSimulation(sectors, meusVetores, soundSourceCenter, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, initialEnergy, mCoeficient, k);
		rts.simulate(bar);
		try{
			rts.lista();
		}
		catch(IOException e){
			e.printStackTrace();
		}
//		ChartBuilder g = new ChartBuilder(rts.getSphericalReceptorHistogram());
//		g.criaGrafico("");
//		g.salvar(new FileOutputStream("histograma.jpg"));
	}

}
