package acmus.auralization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AuralizationTest {
	private MonoauralMultiBandImpulseResponse aur;
	private BandRangeSeq range;
	private float[][] content;
	private Simulator sim;
	MultiBandSimulationViewer viewer;
	
	@Before
	public void setUp() {
		sim = new Simulator();

		sim.setUp(200, 11050);
		
		range = new BandRangeEqSeq(20.0, 20000.0, 4);
		
		content = new float[range.howMany()][];

		content[0] = sim.simulateCoeff(0.2, 0.2, 0.1, 0.1, 0.1, 0.1);
		content[1] = sim.simulateCoeff(0.2, 0.2, 0.25, 0.25, 0.3, 0.3);
		content[2] = sim.simulateCoeff(0.17, 0.17, 0.13, 0.13, 0.34, 0.34);
		content[3] = sim.simulateCoeff(0.4, 0.4, 0.2, 0.2, 0.1, 0.1);
		
		aur = new MonoauralMultiBandImpulseResponse(range, content, 44100);
		
		double[] ir;
		// 4 band ranges in human limits
		ir = aur.getSignal();
		viewer = new MultiBandSimulationViewer();
		
		viewer.view(ir, "Impulse Response");
	}
	
	@Test
	public void notNullTest() {
		Assert.assertNotNull(range);
		Assert.assertNotNull(content);
		Assert.assertNotNull(aur);
	}
}