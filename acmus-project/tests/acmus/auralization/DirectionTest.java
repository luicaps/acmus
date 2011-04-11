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

		Assert.assertNotNull(leftPulse);
		Assert.assertNotNull(rightPulse);

		textView("Left", leftPulse);
		textView("Right", rightPulse);

		MultiBandSimulationViewer viewer = new MultiBandSimulationViewer();

		viewer.view(leftPulse, "Left_pulse");
		viewer.view(rightPulse, "Right_pulse");

		System.out.println("Check the results in " + viewer.getPath());
	}

	private void textView(String id, double[] content) {
		System.out.println(id + " pulse");
		for (int i = 0; i < content.length; i++) {
			System.out.println(content[i]);
		}
	}

}
