package acmus.dsp;

import org.junit.Assert;
import org.junit.Test;

public class UtilTest {

	@Test
	public void testGetAbsoluteMax() throws Exception {
		int[] positiveMax = new int[] { -1, 0, 234};
		
		Assert.assertEquals(234, Util.maxAbs(positiveMax));
		
		int[] negativeMax = new int[] { -234, 0, 1};
		
		Assert.assertEquals(234, Util.maxAbs(negativeMax));
		
		int[] zeroes = new int[] {0, 0, 0};
		
		Assert.assertEquals(0, Util.maxAbs(zeroes));
	}
	
	@Test
	public void testCalcScaleToUnitProperly() throws Exception {
		int[] lessThanOne = { 0, 1, -1};
		double[] result = Util.scaleToUnit(lessThanOne, 1);
		Assert.assertEquals(1, Util.maxAbs(result));
		
		for (int i = 0; i < result.length; i++) {
			Assert.assertEquals(lessThanOne[i], result[i]);
		}
		
		int[] multiplesOfFive = { 0, -5, 10, -25 };
		double[] expected = {0, -0.2, 0.4, -1};
		
		double[] actual = Util.scaleToUnit(multiplesOfFive, 25);
		Assert.assertEquals(1, Util.maxAbs(actual));
		for (int i = 0; i < actual.length; i++) {
			Assert.assertEquals(expected[i], actual[i], 0.01);
		}
	}
	
	@Test
	public void testCalcScaleToMaxProperly() throws Exception {
		double[] in = { -5, 3, 0, 17, -10};
		int[] expected = { -147, 88, 0, 500, -294 };
		Assert.assertArrayEquals(expected, Util.scaleToMax(in, 500));
		double[] in2 = { 5, -3, 0, -17, 10};
		int[] expected2 = { 147, -88, 0, -500, 294 };
		Assert.assertArrayEquals(expected2, Util.scaleToMax(in2, 500));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPassingMaxLessThanVectorMax() throws Exception {
		Util.scaleToUnit(new int[] {0, 10}, 1);
	}
}
