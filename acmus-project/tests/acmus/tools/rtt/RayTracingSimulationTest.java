package acmus.tools.rtt;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import acmus.tools.structures.NormalSector;
import acmus.tools.structures.Triade;

public class RayTracingSimulationTest {

	private List<Triade> vectors;
	private Triade soundSourceCenter;
	private Triade sphericalReceptorCenter;
	private double sphericalReceptorRadius;
	private double soundSpeed;
	private int initialEnergy;
	private double mCoeficient;
	private int k;
	private List<NormalSector> sectors;

	@Before
	public void setUp(){
		vectors = new ArrayList<Triade>();
		vectors.add(new Triade(0.7071, 0.7071, 0)); //vetor (1,1,0)

		sectors = new ArrayList<NormalSector>();
		sectors.add(new NormalSector(new Triade(0, 0, 1), new Triade(1, 1, 0), 0.5)); // base
		sectors.add(new NormalSector(new Triade(0, 0, -1), new Triade(1, 1, 10), 0.5)); // topo
		sectors.add(new NormalSector(new Triade(0, 1, 0), new Triade(1, 0, 1), 0.5)); 
		sectors.add(new NormalSector(new Triade(1, 0, 0), new Triade(0, 1, 1), 0.5));
		sectors.add(new NormalSector(new Triade(0, -1, 0), new Triade(1, 10, 1), 0.5));
		sectors.add(new NormalSector(new Triade(-1, 0, 0), new Triade(10, 1, 1), 0.5));
		
		// soundSource = new Triade(0, 5, 5);
		// sphericalReceptorCenter = new Triade(2.5, 2.5, 5);
		soundSourceCenter = new Triade(2, 2, 5);
		sphericalReceptorCenter = new Triade(8, 8, 6);
		sphericalReceptorRadius = 3.0;
		
		soundSpeed = 344.0; // em metros por segundo (m/s)
		initialEnergy = 10000000;
		mCoeficient = 0.0001;
		k = 500;
		
	}
	@Test
	public void testRayTracingSimulation() {
		RayTracingSimulation rts = new RayTracingSimulation(sectors, vectors, soundSourceCenter, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, initialEnergy, mCoeficient, k);
		
		assertNotNull(rts);
	}
	

	@Test
	public void testSimulate() {
		RayTracingSimulation rts = new RayTracingSimulation(sectors, vectors, soundSourceCenter, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, initialEnergy, mCoeficient, k);
		
		rts.simulate();
		
		Iterator<Double> itr = rts.getSphericalReceptorHistogram().keySet().iterator();
		Double expected = 0.016444344;
		assertTrue(Math.abs(expected - itr.next()) < Triade.EPS);
	}

	@Test
	public void testLista() {
		fail("Not yet implemented");
	}

	@Test
	public void testHistogram() {
		fail("Not yet implemented");
	}
	
	@Test
	public void variosPontos(){
		RandomAcousticSource ras = new RandomAcousticSource();
		List<Triade> meusVetores = ras.generate(20000);
		sphericalReceptorCenter = new Triade(8, 8, 1);
		sphericalReceptorRadius = 0.1;
		initialEnergy = 1000;
//		meusVetores.add(new Triade(0.7071, 0.7071, 0)); //vetor (1,1,0)
//		meusVetores.add(new Triade(1, 1, 0.01).normalize()); //vetor (1,1,0)
		RayTracingSimulation rts = new RayTracingSimulation(sectors, meusVetores, soundSourceCenter, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, initialEnergy, mCoeficient, k);
		rts.simulate();
		try{
			rts.lista();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

}
