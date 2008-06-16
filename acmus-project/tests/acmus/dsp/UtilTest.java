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
		
		int[] negativeLimit = new int[] { -7, 0, 10, Integer.MIN_VALUE };
		Assert.assertEquals(Integer.MAX_VALUE, Util.maxAbs(negativeLimit));

		int[] negativeLimit2 = new int[] { Integer.MIN_VALUE, -7, 0, 10 };
		Assert.assertEquals(Integer.MAX_VALUE, Util.maxAbs(negativeLimit2));
		
		int[] negativeLimit3 = new int[] { 3, -8, Integer.MIN_VALUE, -7, 0, 10 };
		Assert.assertEquals(Integer.MAX_VALUE, Util.maxAbs(negativeLimit3));
		
		int[] positiveLimit = new int[] { -7, 0, 10, Integer.MAX_VALUE };
		Assert.assertEquals(Integer.MAX_VALUE, Util.maxAbs(positiveLimit));
		
		int[] positiveLimit2 = new int[] { Integer.MAX_VALUE, -7, 0, 10 };
		Assert.assertEquals(Integer.MAX_VALUE, Util.maxAbs(positiveLimit2));
		
		int[] positiveLimit3 = new int[] { 3, -8, Integer.MAX_VALUE, -7, 0, 10 };
		Assert.assertEquals(Integer.MAX_VALUE, Util.maxAbs(positiveLimit3));
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
		int[] out = Util.scaleToMax(in, 500, false);
		int[] expected = { -147, 88, 0, 500, -294 };
		Assert.assertArrayEquals(expected, out);
		
		int[] outMax = Util.scaleToMax(in, Integer.MAX_VALUE, false);
		int[] expectedMax = scaledValue (in, Integer.MAX_VALUE, 17);
		Assert.assertArrayEquals(expectedMax, outMax);
		
		int[] outMin = Util.scaleToMax(in, Integer.MIN_VALUE, false);
		int[] expectedMin = scaledValue(in, Integer.MIN_VALUE, 17);
		Assert.assertArrayEquals(expectedMin, outMin);
		
		double[] in2 = { 5, -3, 0, -17, 10};
		int[] out2 = Util.scaleToMax(in2, 500, false);
		int[] expected2 = { 147, -88, 0, -500, 294 };
		Assert.assertArrayEquals(expected2, out2);
		
		int[] outMax2 = Util.scaleToMax(in2, Integer.MAX_VALUE, false);
		int[] expectedMax2 = scaledValue (in2, Integer.MAX_VALUE, 17);
		Assert.assertArrayEquals(expectedMax2, outMax2);
		
		int[] outMin2 = Util.scaleToMax(in2, Integer.MIN_VALUE, false);
		int[] expectedMin2 = scaledValue(in2, Integer.MIN_VALUE, 17);
		Assert.assertArrayEquals(expectedMin2, outMin2);
		
		double[] in3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
		int[] out3 = Util.scaleToMax(in3, Integer.MAX_VALUE, false);
		int[] expected3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
		Assert.assertArrayEquals(expected3, out3);
		
		double[] in4 = { 5, -3, 0, Integer.MIN_VALUE, 10 };
		int[] out4 = Util.scaleToMax(in4, Integer.MAX_VALUE, false);
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
		
		int[] out5 = Util.scaleToMax(in5, Integer.MAX_VALUE, false);
		Assert.assertArrayEquals(expected5, out5);
	}

	@Test
	public void testCalcScaleToMaxIntegerWithDither() throws Exception {

		// Since this method uses random numbers, lets perform
		// the test a few times to increase the chance of error
		for (int j = 0; j < 200; ++j) {
			double[] in = { -5, 3, 0, 17, -10};
			int[] out = Util.scaleToMax(in, 500, true);
			int[] expected = { -147, 88, 0, 500, -294 };
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expected[i], out[i], 2);
			}
			Assert.assertTrue(expected[3] >= out[3]);

			int[] outMax = Util.scaleToMax(in, Integer.MAX_VALUE, true);
			int[] expectedMax = scaledValue (in, Integer.MAX_VALUE, 17);
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expectedMax[i], outMax[i], 2);
			}
			Assert.assertTrue(expectedMax[3] >= outMax[3]);

			int[] outMin = Util.scaleToMax(in, Integer.MIN_VALUE, true);
			int[] expectedMin = scaledValue(in, Integer.MIN_VALUE, 17);
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expectedMin[i], outMin[i], 2);
			}
			Assert.assertTrue(expectedMin[3] >= outMin[3]);

			double[] in2 = { 5, -3, 0, -17, 10};
			int[] out2 = Util.scaleToMax(in2, 500, true);
			int[] expected2 = { 147, -88, 0, -500, 294 };
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expected2[i], out2[i], 2);
			}
			Assert.assertTrue(expected2[3] <= out2[3]);

			int[] outMax2 = Util.scaleToMax(in2, Integer.MAX_VALUE, true);
			int[] expectedMax2 = scaledValue (in2, Integer.MAX_VALUE, 17);
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expectedMax2[i], outMax2[i], 2);
			}
			Assert.assertTrue(expectedMax2[3] <= outMax2[3]);

			int[] outMin2 = Util.scaleToMax(in2, Integer.MIN_VALUE, true);
			int[] expectedMin2 = scaledValue(in2, Integer.MIN_VALUE, 17);
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expectedMin2[i], outMin2[i], 2);
			}
			Assert.assertTrue(expectedMin2[3] <= outMin2[3]);

			double[] in3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
			int[] out3 = Util.scaleToMax(in3, Integer.MAX_VALUE, true);
			int[] expected3 = { 5, -3, 0, Integer.MAX_VALUE, 10 };
			for (int i = 0; i < in.length; ++i) {
				Assert.assertEquals(expected3[i], out3[i], 2);
			}
			Assert.assertTrue(expected3[3] >= out3[3]);

			double[] in4 = { 5, -3, 0, Integer.MIN_VALUE, 10 };
			int[] out4 = Util.scaleToMax(in4, Integer.MAX_VALUE, true);
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

		double[] in4 = { 5, -3, 0, Integer.MIN_VALUE, 10 };
		double[] out4 = Util.scaleToMax(in4, (double) Integer.MAX_VALUE);
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
		double[] out5 = Util.scaleToMax(in5, (double) Integer.MIN_VALUE);
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
		double[] out6 = Util.scaleToMax(in5, (double) Integer.MAX_VALUE);
		for (int i = 0; i < in.length; ++i) {
			Assert.assertEquals(expected6[i], out6[i], 0.01);
		}
	}

	// These dont make sense anymore...
	/* 
	@Test(expected=IllegalArgumentException.class)
	public void testPassingMaxLessThanVectorMax() throws Exception {
		Util.scaleToUnit(new int[] {0, 10}, 1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPassingIntegerMIN_VALUE() throws Exception {
		Util.scaleToUnit(new int[] {0, Integer.MIN_VALUE}, Integer.MAX_VALUE);
	}
	*/
	
	@Test
	public void testDownsample32to16() throws Exception {
		int[] samples = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, 346, 1000000, -1000000, 2000000 };
		int[] expected = { 0, Short.MAX_VALUE, Short.MIN_VALUE, 0, 15, -15, 31 }; 
		
		byte [] sampleBytes = new byte[28];
		for (int i = 0; i < samples.length; i++) {
			int sample = samples[i];
			sampleBytes[i * 4] = (byte) (sample & 255);
			sampleBytes[i * 4 + 1] = (byte) ((sample >> 8) & 255);
			sampleBytes[i * 4 + 2] = (byte) ((sample >> 16) & 255);
			sampleBytes[i * 4 + 3] = (byte) ((sample >> 24) & 255);
		}

		// This downsampling uses dithering, which may cause differences
		// in value. Since dithering uses random numbers, lets perform
		// the test a few times to increase the chance of error
		for (int i = 0; i < 200; ++i) {
			byte[] resultBytes = Util.downsample32to16(false, sampleBytes);

			int[] actual = new int[7];
			int s1, s2;
			for (int j = 0; j < actual.length; ++j) {
				s1 = resultBytes[j * 2];
				s2 = resultBytes[j * 2 + 1];
				s1 &= 255;
				s2 &= 255;
				actual[j] = ((s2 << 24) >> 16) | s1;
			}
			for (int j = 0; j < expected.length; ++j) {
				Assert.assertEquals(expected[j], actual[j], 3);
			}
		}
	}

	@Test
	public void testLittleEndia32bitsToInt() throws Exception {
		int[] expected = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1000000, 1000000 };
		byte[] original = { 0, 0, 0, 0, -1, -1, -1, 127, 0, 0, 0, -128, -64, -67, -16, -1, 64, 66, 15, 0 };
		int[] actual = Util.littleEndian32bitsToInt(original);
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testBigEndia32bitsToInt() throws Exception {
		int[] expected = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1000000, 1000000 };
		byte[] original = { 0, 0, 0, 0, 127, -1, -1, -1, -128, 0, 0, 0, -1, -16, -67, -64, 0, 15, 66, 64 }; 
		int[] actual = Util.bigEndian32bitsToInt(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testLittleEndian16bitsToInt() throws Exception {
		int[] expected = { 0, Short.MAX_VALUE, Short.MIN_VALUE, 1000, -1000 };
		byte[] original = { 0, 0, -1, 127, 0, -128, -24, 3, 24, -4 };
		int[] actual = Util.littleEndian16bitsToInt(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testBigEndian16bitsToInt() throws Exception {
		int[] expected = { 0, Short.MAX_VALUE, Short.MIN_VALUE, 1000, -1000 };
		byte[] original = { 0, 0, 127, -1, -128, 0, 3, -24, -4, 24 };
		int[] actual = Util.bigEndian16bitsToInt(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testIntTo16bitsLittleEndian() throws Exception {
		int[] original = { 0, Short.MAX_VALUE, Short.MIN_VALUE, 1000, -1000 };
		byte[] expected = { 0, 0, -1, 127, 0, -128, -24, 3, 24, -4 };
		byte[] actual = Util.intTo16bitsLittleEndian(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testIntTo16bitsBigEndian() throws Exception {
		int[] original = { 0, Short.MAX_VALUE, Short.MIN_VALUE, 1000, -1000 };
		byte[] expected = { 0, 0, 127, -1, -128, 0, 3, -24, -4, 24 };
		byte[] actual = Util.intTo16bitsBigEndian(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testIntTo32bitsLittleEndian() throws Exception {
		int[] original = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1000000, 1000000 };
		byte[] expected = { 0, 0, 0, 0, -1, -1, -1, 127, 0, 0, 0, -128, -64, -67, -16, -1, 64, 66, 15, 0 };
		byte[] actual = Util.intTo32bitsLittleEndian(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testIntTo32bitsBigEndian() throws Exception {
		int[] original = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1000000, 1000000 };
		byte[] expected = { 0, 0, 0, 0, 127, -1, -1, -1, -128, 0, 0, 0, -1, -16, -67, -64, 0, 15, 66, 64 };
		byte[] actual = Util.intTo32bitsBigEndian(original);
		Assert.assertArrayEquals(expected, actual);
	}
}
