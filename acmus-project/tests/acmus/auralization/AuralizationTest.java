package acmus.auralization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AuralizationTest {
	private AuralizationHandler aur;
	private BandRangeSeq range;
	private Float[][] content;
	
	@Before
	public void setUp() {
		aur = new AuralizationHandler();

		aur.setUp();
		
		range = new BandRangeEqSeq(20.0, 20000.0, 4);
		
		content = new Float[range.howMany()][];

		content[0] = aur.simulateCoeff(0.2, 0.2, 0.1, 0.1, 0.1, 0.1);
		content[1] = aur.simulateCoeff(0.2, 0.2, 0.25, 0.25, 0.3, 0.3);
		content[2] = aur.simulateCoeff(0.17, 0.17, 0.13, 0.13, 0.34, 0.34);
		content[3] = aur.simulateCoeff(0.4, 0.4, 0.2, 0.2, 0.1, 0.1);
		
		// 4 band ranges in human limits
		aur.signalSample(range, content);

	}
	
	@Test
	public void notNullTest() {
		Assert.assertNotNull(range);
		Assert.assertNotNull(content);
		Assert.assertNotNull(aur);
	}
}