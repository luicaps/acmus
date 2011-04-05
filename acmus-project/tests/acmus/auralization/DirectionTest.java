package acmus.auralization;

import org.junit.Assert;
import org.junit.Test;

public class DirectionTest {

	/**
	 * JavaOctave HRTF test
	 */
	@Test
	public void octaveHRTFTest() {
		HRTFselector dir = new HRTFselector();

		double[][] result = dir.getPulse(30, 80);
		
		double[] leftPulse = result[0];
		double[] rightPulse = result[1];
		
		MultiBandSimulationViewer viewer = new MultiBandSimulationViewer();
		
		Assert.assertNotNull(leftPulse);
		Assert.assertNotNull(rightPulse);
		
		viewer.view(leftPulse, "Left pulse");
		viewer.view(rightPulse, "Right pulse");
		
		System.out.println("Check the results in /tmp/");
	}

}
