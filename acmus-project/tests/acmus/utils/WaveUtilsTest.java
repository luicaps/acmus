package acmus.utils;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import acmus.util.WaveUtils;

public class WaveUtilsTest {

	@Test
	public void testReadWriteWaves() throws Exception {
		int[] wave = new int[] {0, 1, -1, 10000};
		WaveUtils.wavWrite(wave, 1, 16, "temp.wav");
		Assert.assertArrayEquals(wave, WaveUtils.wavRead("temp.wav"));
		
		double[] waved = new double[] {0, 1, -1, 10000};
		
		Assert.assertTrue(new File("temp.wav").delete());
		
		WaveUtils.wavWrite(waved, "temp.wav");
		Assert.assertArrayEquals(wave, WaveUtils.wavRead("temp.wav"));
		
		Assert.assertTrue(new File("temp.wav").delete());
	}
	
	@Test
	public void testSplitJoinWaves() throws Exception {
		int[] wave = new int[] {0, 1, -1, 10000};
		
		int[][] split = WaveUtils.splitAudioStream(1, wave);
		Assert.assertArrayEquals(wave, WaveUtils.joinAudioStream(split));
		
		int[] wave2 = new int[] {0, 1, -1};
		WaveUtils.wavWrite(wave2, 1, 32, "temp.wav");
		double[][] splitd = WaveUtils.wavReadSplitDouble("temp.wav");
		MathUtilsTest.assertArrayEquals(new double[] {0, 1, -1}, WaveUtils.joinAudioStream(splitd), 0.000001);
		Assert.assertTrue(new File("temp.wav").delete());
	}
	@Test
	public void testDownsample32to16() throws Exception {
		int[] samples = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, 346, 1000000, -1000000, 2000000 };
		int[] expected = { 0, Short.MAX_VALUE, Short.MIN_VALUE, 0, 15, -15, 31 }; 
		
		byte [] sampleBytes = WaveUtils.intTo32bitsLittleEndian(samples);
		byte [] sampleBytesBig = WaveUtils.intTo32bitsBigEndian(samples);

		// This downsampling uses dithering, which may cause differences
		// in value. Since dithering uses random numbers, lets perform
		// the test a few times to increase the chance of error
		for (int i = 0; i < 200; ++i) {
			byte[] resultBytes = WaveUtils.downsample32to16(false, sampleBytes);
			byte[] resultBytesBig = WaveUtils.downsample32to16(true, sampleBytesBig);

			int[] actual = WaveUtils.littleEndian16bitsToInt(resultBytes);
			int[] actualBig = WaveUtils.bigEndian16bitsToInt(resultBytesBig);
			
			for (int j = 0; j < expected.length; ++j) {
				Assert.assertEquals(expected[j], actual[j], 3);
			}
			for (int j = 0; j < expected.length; ++j) {
				Assert.assertEquals(expected[j], actualBig[j], 3);
			}
		}
	}

	@Test
	public void testLittleEndia32bitsToInt() throws Exception {
		int[] expected = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1000000, 1000000 };
		byte[] original = { 0, 0, 0, 0, -1, -1, -1, 127, 0, 0, 0, -128, -64, -67, -16, -1, 64, 66, 15, 0 };
		int[] actual = WaveUtils.littleEndian32bitsToInt(original);
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testBigEndia32bitsToInt() throws Exception {
		int[] expected = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1000000, 1000000 };
		byte[] original = { 0, 0, 0, 0, 127, -1, -1, -1, -128, 0, 0, 0, -1, -16, -67, -64, 0, 15, 66, 64 }; 
		int[] actual = WaveUtils.bigEndian32bitsToInt(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testLittleEndian16bitsToInt() throws Exception {
		int[] expected = { 0, Short.MAX_VALUE, Short.MIN_VALUE, 1000, -1000 };
		byte[] original = { 0, 0, -1, 127, 0, -128, -24, 3, 24, -4 };
		int[] actual = WaveUtils.littleEndian16bitsToInt(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testBigEndian16bitsToInt() throws Exception {
		int[] expected = { 0, Short.MAX_VALUE, Short.MIN_VALUE, 1000, -1000 };
		byte[] original = { 0, 0, 127, -1, -128, 0, 3, -24, -4, 24 };
		int[] actual = WaveUtils.bigEndian16bitsToInt(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testIntTo16bitsLittleEndian() throws Exception {
		int[] original = { 0, Short.MAX_VALUE, Short.MIN_VALUE, 1000, -1000 };
		byte[] expected = { 0, 0, -1, 127, 0, -128, -24, 3, 24, -4 };
		byte[] actual = WaveUtils.intTo16bitsLittleEndian(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testIntTo16bitsBigEndian() throws Exception {
		int[] original = { 0, Short.MAX_VALUE, Short.MIN_VALUE, 1000, -1000 };
		byte[] expected = { 0, 0, 127, -1, -128, 0, 3, -24, -4, 24 };
		byte[] actual = WaveUtils.intTo16bitsBigEndian(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testIntTo32bitsLittleEndian() throws Exception {
		int[] original = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1000000, 1000000 };
		byte[] expected = { 0, 0, 0, 0, -1, -1, -1, 127, 0, 0, 0, -128, -64, -67, -16, -1, 64, 66, 15, 0 };
		byte[] actual = WaveUtils.intTo32bitsLittleEndian(original);
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testIntTo32bitsBigEndian() throws Exception {
		int[] original = { 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1000000, 1000000 };
		byte[] expected = { 0, 0, 0, 0, 127, -1, -1, -1, -128, 0, 0, 0, -1, -16, -67, -64, 0, 15, 66, 64 };
		byte[] actual = WaveUtils.intTo32bitsBigEndian(original);
		Assert.assertArrayEquals(expected, actual);
	}
}
