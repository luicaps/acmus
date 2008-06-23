package acmus.util;

import java.util.List;
import java.util.StringTokenizer;

public class ArrayUtils {

	public final static double[] mult(double a[], double b[], double res[]) {
		for (int i = 0; i < a.length; i++) {
			res[i] = a[i] * b[i];
		}
		return res;
	}

	public final static double[] mult(double a[], double b[]) {
		return mult(a, b, new double[a.length]);
	}

	public final static double[] multLLL(double a[], double b[]) {
		return mult(a, b, a);
	}

	public final static double[] mult(double[] a, double b, double[] res) {
		for (int i = 0; i < a.length; i++) {
			res[i] = a[i] * b;
		}
		return res;
	}

	public final static double[] mult(double[] a, double b) {
		return mult(a, b, new double[a.length]);
	}

	public final static double[] multLLL(double[] a, double b) {
		return mult(a, b, a);
	}

	public final static double[] sumLines(double a[][]) {
		double[] res = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			res[i] = sum(a[i]);
		}
		return res;
	}

	public final static double sum(double a[]) {
		return sum(a, 0, a.length);
	}

	public final static double sum(double a[], int begin, int end) {
		double res = 0;
		for (int i = begin; i < end; i++) {
			res += a[i];
		}
		return res;
	}

	// changes a
	public final static double[] sumLLL(double a[], double s) {
		for (int i = 0; i < a.length; i++) {
			a[i] += s;
		}
		return a;
	}

	public final static double[] sum(double a[], int aBegin, double b[],
			int bBegin, int len, double[] res) {
		for (int i = 0; i < len; i++) {
			res[i] = a[i + aBegin] + b[i + bBegin];
		}
		return res;
	}

	// changes a
	public final static double[] sumLLL(double a[], double b[]) {
		return sum(a, 0, b, 0, a.length, a);
	}

	// changes a
	public final static double[] sumLLL(double a[], double b[], int bBegin,
			int bEnd) {
		return sum(a, 0, b, bBegin, bEnd - bBegin, a);
	}

	public final static double[] cumsum(double a[], double res[]) {
		res[0] = a[0];
		for (int i = 1; i < a.length; i++) {
			res[i] = res[i - 1] + a[i];
		}
		return res;
	}

	public final static double[] cumsumLLL(double a[]) {
		return cumsum(a, a);
	}

	// changes a
	public final static double[] sqrLLL(double[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = a[i] * a[i];
		}
		return a;
	}

	// returns a[begin]...a[end-1]
	public final static double[] subArray(double[] a, int begin, int end) {
		// if (end < begin) return new double[0];
		double[] res = new double[end - begin];
		for (int i = begin; i < end; i++) {
			res[i - begin] = a[i];
		}
		return res;
	}

	public final static double[] subArray(double[] a, int begin) {
		return subArray(a, begin, a.length);
	}

	public final static int[] subArray(int[] a, int begin, int end) {
		// if (end < begin) return new double[0];
		int[] res = new int[end - begin];
		for (int i = begin; i < end; i++) {
			res[i - begin] = a[i];
		}
		return res;
	}

	public final static int[] subArray(int[] a, int begin) {
		return subArray(a, begin, a.length);
	}

	// returns a[begin]...a[end-1]
	public final static byte[] subArray(byte[] a, int begin, int end) {
		byte[] res = new byte[end - begin];
		for (int i = begin; i < end; i++) {
			res[i - begin] = a[i];
		}
		return res;
	}

	public final static int absMaxIndex(double[] a) {
		int maxi = 0;
		double maxv = a[0];
		for (int i = 1; i < a.length; i++) {
			if (Math.abs(a[i]) > maxv) {
				maxi = i;
				maxv = Math.abs(a[i]);
			}
		}
		return maxi;
	}

	public final static double max(double[] a) {
		double max = a[0];
		for (int i = 1; i < a.length; i++) {
			if (a[i] > max) {
				max = a[i];
			}
		}
		return max;
	}

	public final static double maxAbs(double[] a) {
		double max = Math.abs(a[0]);
		for (int i = 0 + 1; i < a.length; i++) {
			if (Math.abs(a[i]) > max) {
				max = Math.abs(a[i]);
			}
		}
		return max;
	}

	public static int maxAbs(int[] data) {
		int max = 0;
		for (int i = 0; i < data.length; ++i) {
			if (Math.abs(data[i]) > max) {
				max = Math.abs(data[i]);
			} else if (data[i] == Integer.MIN_VALUE) {
				// Check javadoc for Math.abs & Integer.MIN_VALUE before messing
				// with this
				// This is not very good, but is probably the best we can do
				max = Integer.MAX_VALUE;
			}
		}
		return max;
	}

	public final static int firstLessThanIndex(double a[], double val) {
		int res = -2;
		for (int i = 0; i < a.length; i++) {
			if (a[i] < val) {
				res = i;
				break;
			}
		}
		return res;
	}

	public final static int[] stringToIntArray(String str) {
		StringTokenizer st = new StringTokenizer(str);
		int res[] = new int[st.countTokens()];
		for (int i = 0; i < res.length; i++) {
			res[i] = Integer.parseInt(st.nextToken());
		}
		return res;
	}

	public static double[] reverseLLL(double y[]) {
		for (int i = 0; i < y.length / 2; i++) {
			double tmp = y[y.length - 1 - i];
			y[y.length - 1 - i] = y[i];
			y[i] = tmp;
		}
		return y;
	}

	public static double[] reverse(double y[]) {
		double res[] = new double[y.length];
		for (int i = 0; i < y.length; i++) {
			res[y.length - 1 - i] = y[i];
		}
		return res;
	}

	public static double[] scale(double factor, int[] data) {
		double[] scaled = new double[data.length];
		for (int i = 0; i < scaled.length; ++i) {
			scaled[i] = (double) data[i] * factor;
		}
		return scaled;
	}

	public static double[] scale(double factor, double[] data) {
		double[] scaled = new double[data.length];
		for (int i = 0; i < scaled.length; ++i) {
			scaled[i] = data[i] * factor;
		}
		return scaled;
	}

	public static double[] scaleToMax(int[] data, double currentMax,
			double newMax) {
		currentMax = Math.abs(currentMax);
		newMax = Math.abs(newMax);
		double factor = newMax / currentMax;
		return scale(factor, data);
	}

	public static double[] scaleToMax(double[] data, double currentMax,
			double newMax) {
		currentMax = Math.abs(currentMax);
		newMax = Math.abs(newMax);
		double factor = newMax / currentMax;
		return scale(factor, data);
	}

	public static final double[] scaleToMax(double[] data, double newMax) {
		newMax = Math.abs(newMax);
		double currentMax = maxAbs(data);
		return scaleToMax(data, currentMax, newMax);
	}

	public static final int[] scaleToMax(double[] data, int newMax,
			boolean dither) {
		// This method may fail for Integer.MIN_VALUE
		if (newMax == Integer.MIN_VALUE) {
			++newMax;
		}
		newMax = Math.abs(newMax);
		if (dither) {
			// dithering may clip the signal; let's leave some headroom
			--newMax;
		}
		double[] tmp = scaleToMax(data, newMax);
		return Algorithms.doubleToInt(tmp, dither);
	}

	public static final double[] scaleToUnit(int[] data, int currentMax) {
		// This method will return something smaller than -1 for
		// Integer.MIN_VALUE
		if (currentMax == Integer.MIN_VALUE) {
			++currentMax;
		}
		currentMax = Math.abs(currentMax);
		return scaleToMax(data, currentMax, 1);
	}

	public static final double[] scaleToUnit(double[] data, double currentMax) {
		return scaleToMax(data, currentMax, 1);
	}

	public static final double[] scaleToUnit(int[] data) {
		return scaleToUnit(data, ArrayUtils.maxAbs(data));
	}

	public static final double[] scaleToUnit(double[] data) {
		return scaleToUnit(data, maxAbs(data));
	}

	
	public final static double mean(double a[]) {
		return mean(a, 0, a.length);
	}

	public final static double mean(double a[], int begin, int end) {
		double res = 0;
		for (int i = begin; i < end; i++) {
			res += a[i];
		}
		return res / (end - begin);
	}
	
	public static double[] average(List<double[]> arrays) {
		int maxLen = 0;
		for (double[] v : arrays) {
			if (v.length > maxLen)
				maxLen = v.length;
		}
		double avg[] = new double[maxLen];
		for (int i = 0; i < avg.length; i++) {
			avg[i] = 0;
		}
		for (double[] v : arrays) {
			for (int i = 0; i < v.length; i++) {
				avg[i] += v[i];
			}
		}
		for (int i = 0; i < avg.length; i++) {
			avg[i] /= arrays.size();
		}
		return avg;
	}

	public static int[] average(List<int[]> arrays) {
		int maxLen = 0;
		for (int[] v : arrays) {
			if (v.length > maxLen)
				maxLen = v.length;
		}
		int avg[] = new int[maxLen];
		for (int i = 0; i < avg.length; i++) {
			avg[i] = 0;
		}
		for (int[] v : arrays) {
			for (int i = 0; i < v.length; i++) {
				avg[i] += v[i];
			}
		}
		for (int i = 0; i < avg.length; i++) {
			avg[i] /= arrays.size();
		}
		return avg;
	}

}
