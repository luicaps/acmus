package acmus.utils;

import static acmus.utils.MathUtilsTest.assertArrayEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import acmus.util.ArrayUtils;

public class ArrayUtilsTest {

	private static final double DELTA = 0.00000000000001;

	@Test
	public void testMultiplyArrays() throws Exception {
		double[] a = new double[] { 1, 2 };
		double[] b = new double[] { 3, 4 };
		double[] expected = new double[] { 3, 8 };
		assertArrayEquals(expected, ArrayUtils.mult(a, b), DELTA);
		assertArrayEquals(expected, ArrayUtils.multLLL(a, b), DELTA);
		// Array a must be changed to answer
		assertArrayEquals(expected, a, DELTA);
	}

	@Test
	public void testMultiplyArraysByScalar() throws Exception {
		double[] a = new double[] { 1, 2 };
		double[] expected = new double[] { 3, 6 };
		assertArrayEquals(expected, ArrayUtils.mult(a, 3), DELTA);
		assertArrayEquals(expected, ArrayUtils.multLLL(a, 3), DELTA);
		// Array a must be changed to answer
		assertArrayEquals(expected, a, DELTA);
	}

	@Test
	public void testCalculateArraySum() throws Exception {
		double[] a = new double[] { 1, 2 };
		Assert.assertEquals(3, ArrayUtils.sum(a), DELTA);

		double[][] matrix = new double[][] { new double[] { 1, 2 },
				new double[] { 3, 4 } };
		assertArrayEquals(new double[] { 3, 7 }, ArrayUtils.sumLines(matrix),
				DELTA);

		ArrayUtils.sumLLL(a, 3);
		assertArrayEquals(new double[] { 4, 5 }, a, DELTA);

		ArrayUtils.sumLLL(a, new double[] { -3, -3 });
		assertArrayEquals(new double[] { 1, 2 }, a, DELTA);

		ArrayUtils.sumLLL(a, new double[] { 0, 0 }, 0, 2);
		assertArrayEquals(new double[] { 1, 2 }, a, DELTA);

		ArrayUtils.cumsumLLL(a);
		assertArrayEquals(new double[] { 1, 3 }, a, DELTA);

	}

	@Test
	public void testArrayMean() throws Exception {
		Assert.assertEquals(1.5, ArrayUtils.mean(new double[] { 1, 2 }), DELTA);
	}

	@Test
	public void testPowArrays() throws Exception {
		double[] a = new double[] { 1, 2 };
		ArrayUtils.sqrLLL(a);
		assertArrayEquals(new double[] { 1, 4 }, a, DELTA);
	}

	@Test
	public void testSubArray() throws Exception {
		double[] a = new double[] { 1, 2, 3, 4, 5, 6 };
		assertArrayEquals(new double[] {5, 6}, ArrayUtils.subArray(a, 4), DELTA);
		int[] b = new int[] { 1, 2, 3, 4, 5, 6 };
		Assert.assertArrayEquals(new int[] {5, 6}, ArrayUtils.subArray(b, 4));
		byte[] c = new byte[] { 1, 2, 3, 4, 5, 6 };
		Assert.assertArrayEquals(new byte[] {5, 6}, ArrayUtils.subArray(c, 4, 6));
	}
	
	@Test
	public void testMax() throws Exception {
		double[] a = new double[] { 1, 2, 3, 4, 5, -6 };
		Assert.assertEquals(5, ArrayUtils.absMaxIndex(a));
		Assert.assertEquals(6, ArrayUtils.maxAbs(a), DELTA);
		Assert.assertEquals(5, ArrayUtils.max(a), DELTA);
		int[] b = new int[] {1, 2, 3};
		Assert.assertEquals(3, ArrayUtils.maxAbs(b));
		Assert.assertEquals(Integer.MAX_VALUE, ArrayUtils.maxAbs(new int[] {Integer.MIN_VALUE}));		
	}
	@Test
	public void testFirstLessThan() throws Exception {
		double[] a = new double[] { 1, 2, 3, 4, 5, -6 };
		
		Assert.assertEquals(5, ArrayUtils.firstLessThanIndex(a, 0));
		Assert.assertEquals(5, ArrayUtils.firstLessThanIndex(a, 1));
	}
	
	@Test
	public void testStringToIntArray() throws Exception {
		Assert.assertArrayEquals(new int[] { 1, 2, 3 }, ArrayUtils.stringToIntArray("1 2 3"));
	}
	
	@Test
	public void testReverse() throws Exception {
		double[] a = new double[] { 1, 2, 3 };
		assertArrayEquals(new double[] {3, 2, 1}, ArrayUtils.reverse(a), DELTA);
		ArrayUtils.reverseLLL(a);
		assertArrayEquals(new double[] {3, 2, 1}, a, DELTA);
	}
	
	@Test
	public void testScaleArray() throws Exception {
		double[] a = new double[] { 1, 2, 3 };
		assertArrayEquals(new double[] {2, 4, 6}, ArrayUtils.scale(2, a), DELTA);
		int[] b = new int[] { 1, 2, 3 };
		assertArrayEquals(new double[] {2, 4, 6}, ArrayUtils.scale(2, b), DELTA);
		
		assertArrayEquals(new double[] {2, 4, 6}, ArrayUtils.scaleToMax(a, 6), DELTA);
		assertArrayEquals(new double[] {2, 4, 6}, ArrayUtils.scaleToMax(b, 3, 6), DELTA);
		
		Assert.assertArrayEquals(new int[] {2, 4, 6}, ArrayUtils.scaleToMax(a, 6, false));
		assertArrayEquals(new double[] {1.0/3, 2.0/3, 1}, ArrayUtils.scaleToUnit(a), DELTA);
		assertArrayEquals(new double[] {1.0/3, 2.0/3, 1}, ArrayUtils.scaleToUnit(b), DELTA);
	}
	
	@Test
	public void testAverage() throws Exception {
		List<double[]> a = Arrays.asList(new double[] {1, 2}, new double[] {3, 5, 3});
		assertArrayEquals(new double[] {2, 3.5, 1.5}, ArrayUtils.average(a), DELTA);
		
		List<int[]> b = Arrays.asList(new int[] {1, 2}, new int[] {3, 5, 3});
		Assert.assertArrayEquals(new int[] {2, 3, 1}, ArrayUtils.averageInt(b));
	}
	@Test
	public void testGetAbsoluteMax() throws Exception {
		int[] positiveMax = new int[] { -1, 0, 234};
		Assert.assertEquals(234, ArrayUtils.maxAbs(positiveMax));
		
		int[] negativeMax = new int[] { -234, 0, 1};
		Assert.assertEquals(234, ArrayUtils.maxAbs(negativeMax));
		
		int[] zeroes = new int[] {0, 0, 0};
		Assert.assertEquals(0, ArrayUtils.maxAbs(zeroes));
		
		int[] negativeLimit = new int[] { -7, 0, 10, Integer.MIN_VALUE };
		Assert.assertEquals(Integer.MAX_VALUE, ArrayUtils.maxAbs(negativeLimit));

		int[] negativeLimit2 = new int[] { Integer.MIN_VALUE, -7, 0, 10 };
		Assert.assertEquals(Integer.MAX_VALUE, ArrayUtils.maxAbs(negativeLimit2));
		
		int[] negativeLimit3 = new int[] { 3, -8, Integer.MIN_VALUE, -7, 0, 10 };
		Assert.assertEquals(Integer.MAX_VALUE, ArrayUtils.maxAbs(negativeLimit3));
		
		int[] positiveLimit = new int[] { -7, 0, 10, Integer.MAX_VALUE };
		Assert.assertEquals(Integer.MAX_VALUE, ArrayUtils.maxAbs(positiveLimit));
		
		int[] positiveLimit2 = new int[] { Integer.MAX_VALUE, -7, 0, 10 };
		Assert.assertEquals(Integer.MAX_VALUE, ArrayUtils.maxAbs(positiveLimit2));
		
		int[] positiveLimit3 = new int[] { 3, -8, Integer.MAX_VALUE, -7, 0, 10 };
		Assert.assertEquals(Integer.MAX_VALUE, ArrayUtils.maxAbs(positiveLimit3));
	}
	
	@Test
	public void testCalcScaleToUnitProperly() throws Exception {
		double delta = 0.000001;
		int[] lessThanOne = { 0, 1, -1};
		double[] result = ArrayUtils.scaleToUnit(lessThanOne, 1);
		Assert.assertEquals(1, ArrayUtils.maxAbs(result), delta);
		
		for (int i = 0; i < result.length; i++) {
			Assert.assertEquals(lessThanOne[i], result[i], delta);
		}
		
		int[] multiplesOfFive = { 0, -5, 10, -25 };
		double[] expected = {0, -0.2, 0.4, -1};
		
		double[] actual = ArrayUtils.scaleToUnit(multiplesOfFive, 25);
		Assert.assertEquals(1, ArrayUtils.maxAbs(actual), delta);
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

	private double[] scaledValue (double[] valuesToScale, double limit, double currentMax) throws Exception {
		limit = Math.abs(limit);
		double scaleFactor = Math.abs(limit / currentMax);
		double[] scaled = new double[valuesToScale.length];
		for (int i = 0; i < valuesToScale.length; ++i) {
			scaled[i] = valuesToScale[i] * scaleFactor;
		}
		return scaled;
	}
	
	@Test
	public void testCalcScaleToMaxInteger() throws Exception {
		
		double[] in = { -5, 3, 0, 17, -10};
		int[] out = ArrayUtils.scaleToMax(in, 500, false);
		int[] expected = { -147, 88, 0, 500, -294 };
		Assert.assertArrayEquals(expected, out);
		
		int[] outMax = ArrayUtils.scaleToMax(in, Integer.MAX_VALUE, false);
		int[] expectedMax = scaledValue (in, Integer.MAX_VALUE, 17);
		Assert.assertArrayEquals(expectedMax, outMax);
		
		int[] outMin = ArrayUtils.scaleToMax(in, Integer.MIN_VALUE, false);
		int[] expectedMin = scaledValue(in, Integer.MIN_VALUE, 17);
		Assert.assertArrayEquals(expectedMin, outMin);
		
		double[] in2 = { 5, -3, 0, -17, 10};
		int[] out2 = ArrayUtils.scaleToMax(in2, 500, false);
		int[] expected2 = { 147, -88, 0, -500, 294 };
		Assert.assertArrayEquals(expected2, out2);
		
		int[] outMax2 = ArrayUtils.scaleToMax(in2, Integer.MAX_VALUE, false);
		int[] expectedMax2 = scaledValue (in2, Integer.MAX_VALUE, 17);
		Assert.assertArrayEquals(expectedMax2, outMax2);
		
		int[] outMin2 = ArrayUtils.scaleToMax(in2, Integer.MIN_VALUE, false);
		int[] expectedMin2 = scaledValue(in2, Integer.MIN_VALUE, 17);
		Assert.assertArrayEquals(expectedMin2, outMin2);
		
		double[] in3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
		int[] out3 = ArrayUtils.scaleToMax(in3, Integer.MAX_VALUE, false);
		int[] expected3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
		Assert.assertArrayEquals(expected3, out3);
		
		double[] in4 = { 5, -3, 0, Integer.MIN_VALUE, 10 };
		int[] out4 = ArrayUtils.scaleToMax(in4, Integer.MAX_VALUE, false);
		int[] expected4 = { 5, -3, 0, Integer.MIN_VALUE + 1, 10 };
		Assert.assertArrayEquals(expected4, out4);
		
		double[] in5 = {
				Integer.MAX_VALUE / 3,
				-3,
				0,
				7,
				Integer.MIN_VALUE,
				Integer.MIN_VALUE + 4000,
				Integer.MAX_VALUE - 4000,
				Integer.MAX_VALUE
				};

		int[] expected5 = {
				Integer.MAX_VALUE / 3,
				-3,
				0,
				7,
				Integer.MIN_VALUE + 1,
				Integer.MIN_VALUE + 4001,
				Integer.MAX_VALUE - 4001,
				Integer.MAX_VALUE - 1
				};
		
		int[] out5 = ArrayUtils.scaleToMax(in5, Integer.MAX_VALUE, false);
		Assert.assertArrayEquals(expected5, out5);
	}

	@Test
	public void testCalcScaleToMaxIntegerWithDither() throws Exception {

		// Since this method uses random numbers, lets perform
		// the test a few times to increase the chance of error
		for (int j = 0; j < 200; ++j) {
			double[] in = { -5, 3, 0, 17, -10};
			int[] out = ArrayUtils.scaleToMax(in, 500, true);
			int[] expected = { -147, 88, 0, 500, -294 };
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expected[i], out[i], 2);
			}
			Assert.assertTrue(expected[3] >= out[3]);

			int[] outMax = ArrayUtils.scaleToMax(in, Integer.MAX_VALUE, true);
			int[] expectedMax = scaledValue (in, Integer.MAX_VALUE, 17);
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expectedMax[i], outMax[i], 2);
			}
			Assert.assertTrue(expectedMax[3] >= outMax[3]);

			int[] outMin = ArrayUtils.scaleToMax(in, Integer.MIN_VALUE, true);
			int[] expectedMin = scaledValue(in, Integer.MIN_VALUE, 17);
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expectedMin[i], outMin[i], 2);
			}
			Assert.assertTrue(expectedMin[3] >= outMin[3]);

			double[] in2 = { 5, -3, 0, -17, 10};
			int[] out2 = ArrayUtils.scaleToMax(in2, 500, true);
			int[] expected2 = { 147, -88, 0, -500, 294 };
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expected2[i], out2[i], 2);
			}
			Assert.assertTrue(expected2[3] <= out2[3]);

			int[] outMax2 = ArrayUtils.scaleToMax(in2, Integer.MAX_VALUE, true);
			int[] expectedMax2 = scaledValue (in2, Integer.MAX_VALUE, 17);
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expectedMax2[i], outMax2[i], 2);
			}
			Assert.assertTrue(expectedMax2[3] <= outMax2[3]);

			int[] outMin2 = ArrayUtils.scaleToMax(in2, Integer.MIN_VALUE, true);
			int[] expectedMin2 = scaledValue(in2, Integer.MIN_VALUE, 17);
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expectedMin2[i], outMin2[i], 2);
			}
			Assert.assertTrue(expectedMin2[3] <= outMin2[3]);

			double[] in3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
			int[] out3 = ArrayUtils.scaleToMax(in3, Integer.MAX_VALUE, true);
			int[] expected3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expected3[i], out3[i], 2);
			}
			Assert.assertTrue(expected3[3] >= out3[3]);

			double[] in4 = { 5, -3, 0, Integer.MIN_VALUE, 10 };
			int[] out4 = ArrayUtils.scaleToMax(in4, Integer.MAX_VALUE, true);
			int[] expected4 = { 5, -3, 0, Integer.MIN_VALUE, 10 };
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expected4[i], out4[i], 3);
			}
			Assert.assertTrue(expected4[3] <= out4[3]);
		}
	}

	@Test
	public void  testCalcScaleToMaxDouble() throws Exception {
		
		double[] in = { -5, 3, 0, 17, -10};
		double[] out = ArrayUtils.scaleToMax(in, (double) 500);
		double[] expected = { -147.06, 88.23, 0, 500, -294.12 };
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected[i], out[i], 0.01);
		}
		
		double[] outMax = ArrayUtils.scaleToMax(in, (double) Integer.MAX_VALUE);
		double[] expectedMax = scaledValue (in, (double) Integer.MAX_VALUE, (double) 17);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expectedMax[i], outMax[i], 0.01);
		}

		double[] outMin = ArrayUtils.scaleToMax(in, (double) Integer.MIN_VALUE);
		double[] expectedMin = scaledValue(in, (double) Integer.MIN_VALUE, (double) 17);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expectedMin[i], outMin[i], 0.01);
		}
		
		double[] in2 = { 5, -3, 0, -17, 10};
		double[] out2 = ArrayUtils.scaleToMax(in2, (double) 500);
		double[] expected2 = { 147.06, -88.23, 0, -500, 294.12 };
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected2[i], out2[i], 0.01);
		}
		
		double[] outMax2 = ArrayUtils.scaleToMax(in2, (double) Integer.MAX_VALUE);
		double[] expectedMax2 = scaledValue (in2, (double) Integer.MAX_VALUE, (double) 17);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expectedMax2[i], outMax2[i], 0.01);
		}
		
		double[] outMin2 = ArrayUtils.scaleToMax(in2, (double) Integer.MIN_VALUE);
		double[] expectedMin2 = scaledValue(in2, (double) Integer.MIN_VALUE, (double) 17);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expectedMin2[i], outMin2[i], 0.01);
		}
		
		double[] in3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
		double[] out3 = ArrayUtils.scaleToMax(in3, (double) Integer.MAX_VALUE);
		double[] expected3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected3[i], out3[i], 0.01);
		}

		double[] in4 = { 5, -3, 0, Integer.MIN_VALUE, 10 };
		double[] out4 = ArrayUtils.scaleToMax(in4, (double) Integer.MAX_VALUE);
		double[] expected4 = { 5, -3, 0, Integer.MIN_VALUE + 1, 10 };
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected4[i], out4[i], 0.01);
		}

		double[] in5 = {
				Integer.MAX_VALUE / 3,
				-3,
				0,
				7,
				Integer.MIN_VALUE,
				Integer.MIN_VALUE + 4000,
				Integer.MAX_VALUE - 4000,
				Integer.MAX_VALUE
				};

		double[] expected5 = {
				Integer.MAX_VALUE / 3,
				-3,
				0,
				7,
				Integer.MIN_VALUE,
				Integer.MIN_VALUE + 4000,
				Integer.MAX_VALUE - 4000,
				Integer.MAX_VALUE
				};
		double[] out5 = ArrayUtils.scaleToMax(in5, (double) Integer.MIN_VALUE);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected5[i], out5[i], 0.01);
		}
		
		double[] expected6 = {
				Integer.MAX_VALUE / 3 - 0.3333,
				-3,
				0,
				7,
				Integer.MIN_VALUE + 1,
				Integer.MIN_VALUE + 4001,
				Integer.MAX_VALUE - 4001,
				Integer.MAX_VALUE
				};
		double[] out6 = ArrayUtils.scaleToMax(in5, (double) Integer.MAX_VALUE);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected6[i], out6[i], 0.01);
		}
	}
}
