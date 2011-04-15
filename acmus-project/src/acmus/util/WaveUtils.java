package acmus.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import acmus.AcmusApplication;


public class WaveUtils {

	public static void wavWrite(double t[], float SR, String filename) {
		WaveUtils.wavWrite(t, 1, SR, filename);
	}

	public static void wavWrite(double t[], int channels, float SR, String filename) {
		WaveUtils.wavWrite(t, channels, 16, SR, filename, false);
	}

	public static void wavWrite(double t[], int channels, int bitsPerSample,
			float SR, String filename, boolean dither) {
		int[] samples = Algorithms.doubleToInt(t, dither);
		WaveUtils.wavWrite(samples, channels, bitsPerSample, SR, filename);
	}

	public static void wavWrite(int t[], int channels, int bitsPerSample,
			float SR, String filename) {
		byte[] samples;
		if (bitsPerSample == 32) {
			samples = WaveUtils.intTo32bitsLittleEndian(t);
		} else if (bitsPerSample == 16) {
			samples = WaveUtils.intTo16bitsLittleEndian(t);
		} else {
			throw new RuntimeException(
					"Oops! Only know how to handle 16 or 32 bits audio");
		}
		WaveUtils.wavWrite(samples, SR, bitsPerSample, channels, false,
				filename);
	}

	public final static void wavWrite(byte data[], double rate,
			int bitsPerSample, int channels, boolean bigEndian, String filename) {
		try {
			ByteArrayInputStream baos = new ByteArrayInputStream(data);
			AudioFormat format = new AudioFormat((float) rate, bitsPerSample,
					channels, true, bigEndian);
			AudioInputStream ais = new AudioInputStream(baos, format,
					data.length * 8 / bitsPerSample);
			if (AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(
					filename)) == -1) {
				throw new IOException("Problems writing to file");
			}
			ais.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final static int[] wavRead(String filename) {
		int res[] = null;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(new File(
					filename))));
			res = WaveUtils.readData(ais);
			ais.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public final static double[][] wavReadSplitDouble(String filename) {
		double res[][] = null;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(new File(
					filename))));
			int data[][] = WaveUtils.splitAudioStream(ais.getFormat().getChannels(),
					WaveUtils.readData(ais));
			res = new double[data.length][data[0].length];
			for (int i = 0; i < res.length; i++) {
				WaveUtils.scaleToUnitInPlace(res[i], data[i], ais.getFormat()
						.getSampleSizeInBits());
			}
			ais.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public final static int[][] splitAudioStream(int channels, int[] s) {
		int[][] res = new int[channels][s.length / channels];
		for (int i = 0; i < s.length; i++) {
			res[i % channels][i / channels] = s[i];
		}
		return res;
	}

	public final static double[] joinAudioStream(double[][] s) {
		double[] res = new double[s.length * s[0].length];
		int k = 0;
		for (int j = 0; j < s[0].length; j++) {
			for (int i = 0; i < s.length; i++) {
				res[k++] = s[i][j];
			}
		}
		return res;
	}

	public final static int[] joinAudioStream(int[]... streams) {
		int[] res = new int[streams.length * streams[0].length];
		int k = 0;
		for (int j = 0; j < streams[0].length; j++) {
			for (int i = 0; i < streams.length; i++) {
				res[k++] = streams[i][j];
			}
		}
		return res;
	}

	public final static byte[] downsample32to16(boolean isBigEndian, byte[] data) {
		// We need to leave some headroom for dithering, thats why "-1"
		double factor = (double) (WaveUtils.getLimit(16) - 1)
				/ (double) WaveUtils.getLimit(32);
		int[] oldSamples, newSamples;
		double[] scaled;
		if (isBigEndian) {
			oldSamples = WaveUtils.bigEndian32bitsToInt(data);
			scaled = ArrayUtils.scale(factor, oldSamples);
			newSamples = Algorithms.doubleToInt(scaled, true);
			return WaveUtils.intTo16bitsBigEndian(newSamples);
		} else {
			oldSamples = WaveUtils.littleEndian32bitsToInt(data);
			scaled = ArrayUtils.scale(factor, oldSamples);
			newSamples = Algorithms.doubleToInt(scaled, true);
			return WaveUtils.intTo16bitsLittleEndian(newSamples);
		}
	}

	public static byte[] intTo16bitsLittleEndian(int[] data) {
		byte[] result = new byte[data.length * 2];
		for (int i = 0; i < data.length; ++i) {
			result[i * 2] = (byte) (data[i] & 255);
			result[i * 2 + 1] = (byte) ((data[i] >> 8) & 255);
		}
		return result;
	}

	public static byte[] intTo16bitsBigEndian(int[] data) {
		byte[] result = new byte[data.length * 2];
		for (int i = 0; i < data.length; ++i) {
			result[i * 2 + 1] = (byte) (data[i] & 255);
			result[i * 2] = (byte) ((data[i] >> 8) & 255);
		}
		return result;
	}

	public static byte[] intTo32bitsLittleEndian(int[] data) {
		byte[] result = new byte[data.length * 4];
		for (int i = 0; i < data.length; ++i) {
			result[i * 4] = (byte) (data[i] & 255);
			result[i * 4 + 1] = (byte) ((data[i] >> 8) & 255);
			result[i * 4 + 2] = (byte) ((data[i] >> 16) & 255);
			result[i * 4 + 3] = (byte) ((data[i] >> 24) & 255);
		}
		return result;
	}

	public static byte[] intTo32bitsBigEndian(int[] data) {
		byte[] result = new byte[data.length * 4];
		for (int i = 0; i < data.length; ++i) {
			result[i * 4 + 3] = (byte) (data[i] & 255);
			result[i * 4 + 2] = (byte) ((data[i] >> 8) & 255);
			result[i * 4 + 1] = (byte) ((data[i] >> 16) & 255);
			result[i * 4] = (byte) ((data[i] >> 24) & 255);
		}
		return result;
	}

	public final static int[] littleEndian16bitsToInt(byte[] data) {
		int[] result = new int[data.length / 2];
		for (int i = 0; i < result.length; ++i) {
			result[i] = WaveUtils.littleEndianToInt(data[i * 2], data[i * 2 + 1]);
		}
		return result;
	}

	public final static int[] bigEndian16bitsToInt(byte[] data) {
		int[] result = new int[data.length / 2];
		for (int i = 0; i < result.length; ++i) {
			result[i] = WaveUtils.bigEndianToInt(data[i * 2], data[i * 2 + 1]);
		}
		return result;
	}

	public final static int[] littleEndian32bitsToInt(byte[] data) {
		int[] result = new int[data.length / 4];
		for (int i = 0; i < result.length; ++i) {
			result[i] = WaveUtils.littleEndianToInt(data[i * 4], data[i * 4 + 1],
					data[i * 4 + 2], data[i * 4 + 3]);
		}
		return result;
	}

	public final static int[] bigEndian32bitsToInt(byte[] data) {
		int[] result = new int[data.length / 4];
		for (int i = 0; i < result.length; ++i) {
			result[i] = WaveUtils.bigEndianToInt(data[i * 4], data[i * 4 + 1],
					data[i * 4 + 2], data[i * 4 + 3]);
		}
		return result;
	}

	public final static int littleEndianToInt(byte b1, byte b2, byte b3, byte b4) {
		// YES, we need to do it this way; NO, the obvious solution does not
		// work
		// The sign bit will bite you if you are not careful
		int i1, i2, i3, i4;
		// These are NOT redundant or unnecessary
		i1 = b1 & 255;
		i2 = b2 & 255;
		i3 = b3 & 255;
		i4 = b4 & 255;
		return (i4 << 24) | (i3 << 16) | (i2 << 8) | i1;
	}

	public final static int bigEndianToInt(byte b1, byte b2, byte b3, byte b4) {
		return littleEndianToInt(b4, b3, b2, b1);
	}

	public final static int littleEndianToInt(byte b1, byte b2) {
		// YES, we need to do it this way; NO, the obvious solution does not
		// work
		// The sign bit will bite you if you are not careful
		int i1, i2;
		i1 = b1 & 255;
		i2 = b2 & 255;
		// We need to put the sign bit on the right place for an int
		return ((i2 << 24) >> 16) | i1;
	}

	public final static int bigEndianToInt(byte b1, byte b2) {
		return littleEndianToInt(b2, b1);
	}

	public static int getLimit(int bitsPerSample) {
		return (1 << (bitsPerSample - 1)) - 1;
	}

	public static void wavAverage(String outFile, int bitsPerSample,
			List<String> files) {
		String[] f = new String[files.size()];
		f = files.toArray(f);
		WaveUtils.wavAverage(outFile, bitsPerSample, f);
	}

	public static void wavAverage(String outFile, int bitsPerSample,
			String... files) {
		List<double[]> arrays = new ArrayList<double[]>();
		for (String file : files) {
			double[][] data = wavReadSplitDouble(file);
			for (int i = 0; i < data.length; i++) {
				arrays.add(data[i]);
			}
		}
		double[] scaled = ArrayUtils.scaleToMax(ArrayUtils.average(arrays),
				(double) getLimit(bitsPerSample));
		wavWrite(scaled, 1, bitsPerSample,
				(float) AcmusApplication.SAMPLE_RATE, outFile, false);
	}

	public static int[] parseData(byte[] audioBytes, AudioFormat format) {
	
		int[] audioData = null;
		if (format.getSampleSizeInBits() == 32) {
			if (format.isBigEndian()) {
				audioData = bigEndian32bitsToInt(audioBytes);
			} else {
				audioData = littleEndian32bitsToInt(audioBytes);
			}
		} else if (format.getSampleSizeInBits() == 16) {
			if (format.isBigEndian()) {
				audioData = bigEndian16bitsToInt(audioBytes);
			} else {
				audioData = littleEndian16bitsToInt(audioBytes);
			}
		} else if (format.getSampleSizeInBits() == 8) {
			int nlengthInSamples = audioBytes.length;
			audioData = new int[nlengthInSamples];
			if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
				for (int i = 0; i < audioBytes.length; i++) {
					audioData[i] = audioBytes[i];
				}
			} else {
				for (int i = 0; i < audioBytes.length; i++) {
					audioData[i] = audioBytes[i] - 128;
				}
			}
		}
		return audioData;
	}

	public static int[] readData(AudioInputStream ais) {
	
		AudioFormat format = ais.getFormat();
		byte[] audioBytes = new byte[(int) (ais.getFrameLength() * format
				.getFrameSize())];
	
		try {
			ais.read(audioBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return parseData(audioBytes, format);
	}

	public static final double[] scaleToUnitInPlace(double res[], int[] data,
			int bits) {
		// In ancient history (may/2008) this was calculated this way here;
		// but it is most likely a bug (precedence is wrong).
		//int max = (1 << bits - 1) - 1;
		// This is probably what the above line meant:
		//int max = Util.getLimit(bits);
		// But what is probably correct is this:
		int max = ArrayUtils.maxAbs(data);
		for (int i = 0; i < res.length; i++) {
			res[i] = (double) data[i] / (double) max;
		}
		return res;
	}

}
