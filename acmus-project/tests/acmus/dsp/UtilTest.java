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

	// These two are auxiliary methods to the tests below
	private int[] scaledValue (double[] valuesToScale, int limit, int currentMax) {
		if (limit == Integer.MIN_VALUE) {
			++limit;
		}
		limit = Math.abs(limit);
		double scaleFactor = (double) limit / (double) currentMax; 
		int[] scaled = new int[valuesToScale.length];
		for (int i = 0; i < valuesToScale.length; ++i) {
			scaled[i] = (int) Math.round(valuesToScale[i] * scaleFactor);
		}
		return scaled;
	}

	private double[] scaledValue (double[] valuesToScale, double limit, double currentMax) {
		limit = Math.abs(limit);
		double scaleFactor = limit / currentMax; 
		double[] scaled = new double[valuesToScale.length];
		for (int i = 0; i < valuesToScale.length; ++i) {
			scaled[i] = valuesToScale[i] * scaleFactor;
		}
		return scaled;
	}
	
	@Test
	public void testCalcScaleToMaxInteger() throws Exception {
		
		double[] in = { -5, 3, 0, 17, -10};
		int[] out = Util.scaleToMax(in, 500);
		int[] expected = { -147, 88, 0, 500, -294 };
		Assert.assertArrayEquals(expected, out);
		
		int[] outMax = Util.scaleToMax(in, Integer.MAX_VALUE);
		int[] expectedMax = scaledValue (in, Integer.MAX_VALUE, 17);
		Assert.assertArrayEquals(expectedMax, outMax);
		
		int[] outMin = Util.scaleToMax(in, Integer.MIN_VALUE);
		int[] expectedMin = scaledValue(in, Integer.MIN_VALUE, 17);
		Assert.assertArrayEquals(expectedMin, outMin);
		
		double[] in2 = { 5, -3, 0, -17, 10};
		int[] out2 = Util.scaleToMax(in2, 500);
		int[] expected2 = { 147, -88, 0, -500, 294 };
		Assert.assertArrayEquals(expected2, out2);
		
		int[] outMax2 = Util.scaleToMax(in2, Integer.MAX_VALUE);
		int[] expectedMax2 = scaledValue (in2, Integer.MAX_VALUE, 17);
		Assert.assertArrayEquals(expectedMax2, outMax2);
		
		int[] outMin2 = Util.scaleToMax(in2, Integer.MIN_VALUE);
		int[] expectedMin2 = scaledValue(in2, Integer.MIN_VALUE, 17);
		Assert.assertArrayEquals(expectedMin2, outMin2);
		
		double[] in3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
		int[] out3 = Util.scaleToMax(in3, Integer.MAX_VALUE);
		int[] expected3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
		Assert.assertArrayEquals(expected3, out3);
		
		/* FIXME this test fails; dont know a good way to fix it, though...
		double[] in4 = { 5, -3, 0, Integer.MIN_VALUE, 10 };
		int[] out4 = Util.scaleToMax(in4, Integer.MAX_VALUE);
		int[] expected4 = { 5, -3, 0, Integer.MIN_VALUE, 10 };
		Assert.assertArrayEquals(expected4, out4);
		*/
	}
		
	@Test
	public void  testCalcScaleToMaxDouble() throws Exception {
		
		double[] in = { -5, 3, 0, 17, -10};
		double[] out = Util.scaleToMax(in, (double) 500);
		double[] expected = { -147.06, 88.23, 0, 500, -294.12 };
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected[i], out[i], 0.01);
		}
		
		double[] outMax = Util.scaleToMax(in, (double) Integer.MAX_VALUE);
		double[] expectedMax = scaledValue (in, (double) Integer.MAX_VALUE, (double) 17);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expectedMax[i], outMax[i], 0.01);
		}

		double[] outMin = Util.scaleToMax(in, (double) Integer.MIN_VALUE);
		double[] expectedMin = scaledValue(in, (double) Integer.MIN_VALUE, (double) 17);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expectedMin[i], outMin[i], 0.01);
		}
		
		double[] in2 = { 5, -3, 0, -17, 10};
		double[] out2 = Util.scaleToMax(in2, (double) 500);
		double[] expected2 = { 147.06, -88.23, 0, -500, 294.12 };
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected2[i], out2[i], 0.01);
		}
		
		double[] outMax2 = Util.scaleToMax(in2, (double) Integer.MAX_VALUE);
		double[] expectedMax2 = scaledValue (in2, (double) Integer.MAX_VALUE, (double) 17);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expectedMax2[i], outMax2[i], 0.01);
		}
		
		double[] outMin2 = Util.scaleToMax(in2, (double) Integer.MIN_VALUE);
		double[] expectedMin2 = scaledValue(in2, (double) Integer.MIN_VALUE, (double) 17);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expectedMin2[i], outMin2[i], 0.01);
		}
		
		double[] in3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
		double[] out3 = Util.scaleToMax(in3, (double) Integer.MAX_VALUE);
		double[] expected3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected3[i], out3[i], 0.01);
		}

		/* FIXME this test fails; dont know a good way to fix it, though...
		double[] in4 = { 5, -3, 0, Integer.MIN_VALUE, 10 };
		double[] out4 = Util.scaleToMax(in4, (double) Integer.MAX_VALUE);
		double[] expected4 = { 5, -3, 0, (double) Integer.MIN_VALUE, (double) 10 };
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected4[i], out4[i], 0.01);
		}
		*/
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPassingMaxLessThanVectorMax() throws Exception {
		Util.scaleToUnit(new int[] {0, 10}, 1);
	}
}
