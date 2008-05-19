/*
 *  Util.java
 *  This file is part of AcMus.
 *  
 *  AcMus: Tools for Measurement, Analysis, and Simulation of Room Acoustics
 *  
 *  Copyright (C) 2006 Leo Ueda, Bruno Masiero
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package acmus.dsp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import acmus.audio.AudioPlayer;

public final class Util {

	public static final double DEFAULT_IR_LENGTH = 5.0;
	public static final double DEFAULT_REC_EXTRA = 2.0; // == IR_LENGTH?

	public static final double _log10 = Math.log(10);
	public static final double _log2 = Math.log(2);

	public final static double log10(double x) {
		return Math.log(Math.abs(x)) / Util._log10;
	}

	public final static double log2(double x) {
		return Math.log(x) / _log2;
	}

	public final static double log(double x, double b) {
		return Math.log(x) / Math.log(b);
	}

	public final static double[] log10(double a[], int begin, int end,
			double[] res) {
		for (int i = begin; i < end; i++) {
			res[i - begin] = log10(a[i]);
		}
		return res;
	}

	public final static double[] log10(double a[], double res[]) {
		return log10(a, 0, a.length, res);
	}

	public final static double[] log10LLL(double a[]) {
		return log10(a, a);
	}

	public final static double[] log10(double a[]) {
		double[] res = new double[a.length];
		return log10(a, 0, a.length, res);
	}

	// stores result in a.
	public static final void div(double aRe[], double aIm[], double bRe[],
			double bIm[]) {
		Complex c = new Complex(0, 0);

		for (int i = 0; i < aRe.length; i++) {
			c.real(aRe[i]);
			c.imag(aIm[i]);
			c.div(bRe[i], bIm[i]);
			aRe[i] = c.real();
			aIm[i] = c.imag();
		}
	}

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

	public final static double[] cumsum(double a[]) {
		return cumsum(a, new double[a.length]);
	}

	public final static double[] cumsumLLL(double a[]) {
		return cumsum(a, a);
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

	public final static double[] div(double[] a, double b) {
		double[] res = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			res[i] = a[i] / b;
		}
		return res;
	}

	// changes a
	public final static double[] divLLL(double[] a, double b) {
		for (int i = 0; i < a.length; i++) {
			a[i] = a[i] / b;
		}
		return a;
	}

	public final static double[] pow(double[] a, double b, double res[]) {
		for (int i = 0; i < a.length; i++) {
			res[i] = Math.pow(a[i], b);
		}
		return res;
	}

	public final static double[] pow(double[] a, double b) {
		return pow(a, b, new double[a.length]);
	}

	// changes a
	public final static double[] powLLL(double[] a, double b) {
		return pow(a, b, a);
	}

	public final static double[] sqr(double[] a, double res[]) {
		for (int i = 0; i < a.length; i++) {
			res[i] = a[i] * a[i];
		}
		return res;
	}

	public final static double[] sqr(double[] a) {
		return sqr(a, new double[a.length]);
	}

	// changes a
	public final static double[] sqrLLL(double[] a) {
		return sqr(a, a);
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

	public final static double[][] cutLines(double[][] a, int begin, int end) {
		double[][] res = new double[a.length][];
		for (int i = 0; i < res.length; i++) {
			res[i] = subArray(a[i], begin, end);
		}
		return res;
	}

	// returns the first index
	public final static int maxIndAbs(double[] a) {
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

	public final static double min(double[] a) {
		double min = a[0];
		for (int i = 1; i < a.length; i++) {
			if (a[i] < min) {
				min = a[i];
			}
		}
		return min;
	}

	public final static double maxAbs(double[] a, int begin, int end) {
		double max = Math.abs(a[begin]);
		for (int i = begin + 1; i < end; i++) {
			if (Math.abs(a[i]) > max) {
				max = Math.abs(a[i]);
			}
		}
		return max;
	}

	public final static double maxAbs(double[] a) {
		return maxAbs(a, 0, a.length);
	}

	public final static int firstLessThan(double a[], double val) {
		int res = -2;
		for (int i = 0; i < a.length; i++) {
			if (a[i] < val) {
				res = i;
				break;
			}
		}
		return res;
	}

	public final static void print(double[] y) {
		for (int i = 0; i < y.length; i++)
			System.out.print(_f.format(y[i]) + " ");
		System.out.println();
	}

	public final static void print(int[] y) {
		for (int i = 0; i < y.length; i++)
			System.out.print(_f.format(y[i]) + " ");
		System.out.println();
	}

	public final static void print2(double[] y) {
		for (int i = 0; i < y.length; i++)
			System.out.println(y[i] + " ");
		System.out.println();
	}

	public final static String toString(double[] y, Format format) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < y.length - 1; i++) {
			sb.append(format.format(y[i]) + " ");
		}
		sb.append(format.format(y[y.length - 1]) + " ");
		return sb.toString();
	}

	public final static String toString(int[] y) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < y.length - 1; i++) {
			sb.append(y[i] + " ");
		}
		sb.append(y[y.length - 1]);
		return sb.toString();
	}

	public final static int[] parseIntArray(String str) {
		StringTokenizer st = new StringTokenizer(str);
		int res[] = new int[st.countTokens()];
		for (int i = 0; i < res.length; i++) {
			res[i] = Integer.parseInt(st.nextToken());
		}
		return res;
	}

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// % intlinear.m
	// %
	// %[A,B] = intlinear(x,y)
	// %
	// % Realiza a regressao linear (minimos quadrados) dos dados fornecidos
	// (x,y)
	// % devolvendo os valores de A e B, sendo (y = A + B*x).
	//
	public final static double[] intlinear(double[] x, double[] y) {
		// // function [A,B] = intlinear(x,y)
		// //
		double[] res = new double[2];

		// // mx = mean(x); my = mean(y);
		// // mx2 = mean(x.^2); my2 = mean(y.^2);
		// // mxy = mean(x.*y);
		double mx = mean(x);
		double my = mean(y);
		// double mx2 = mean(pow(x, 2));
		// double my2 = mean(pow(y, 2)); // FIXME: never used...

		double mxy = mean(mult(y, x));

		double mx2 = mean(sqrLLL(x));
		// double my2 = mean(sqrLLL(y)); // FIXME: never used...

		// // A = (mx2*my-mx*mxy)/(mx2-mx^2);
		// // B = (mxy - (mx*my))/(mx2-mx^2);
		res[0] = (mx2 * my - mx * mxy) / (mx2 - (mx * mx));
		res[1] = (mxy - (mx * my)) / (mx2 - (mx * mx));
		return res;
	}

	public static int eye(int i, int j) {
		return (i == j) ? 1 : 0;
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

	public final static String fs(String str, int compr) {
		return formatString(str, compr, ' ', 1);
	}

	public final static String formatString(String str, int compr, char b,
			int alinhamento) {
		if (str.length() >= compr)
			return str;
		if (alinhamento == 1) {
			char[] buf = new char[compr - str.length()];
			for (int i = 0; i < buf.length; i++)
				buf[i] = b;
			String brancos = new String(buf);
			return new String(brancos + str);
		} else if (alinhamento == 2) {
			int metade = (compr - str.length()) / 2;
			char[] bufDir = new char[metade];
			char[] bufEsq = new char[compr - str.length() - metade];
			for (int i = 0; i < bufDir.length; i++)
				bufDir[i] = b;
			for (int i = 0; i < bufEsq.length; i++)
				bufEsq[i] = b;
			String brancosDir = new String(bufDir);
			String brancosEsq = new String(bufEsq);
			return new String(brancosDir + str + brancosEsq);
		} else {
			char[] buf = new char[compr - str.length()];
			for (int i = 0; i < buf.length; i++)
				buf[i] = b;
			String brancos = new String(buf);
			return new String(str + brancos);
		}
	}

	public static final double[] conv2(double a[], double b[], double output[]) {
		// // function c = conv(a, b)
		// // %CONV Convolution and polynomial multiplication.
		// // % C = CONV(A, B) convolves vectors A and B. The resulting
		// // % vector is length LENGTH(A)+LENGTH(B)-1.
		// // % If A and B are vectors of polynomial coefficients, convolving
		// // % them is equivalent to multiplying the two polynomials.
		// // %
		// // % See also DECONV, CONV2, CONVN, FILTER and, in the Signal
		// // % Processing Toolbox, XCORR, CONVMTX.
		// //
		// // % Copyright 1984-2002 The MathWorks, Inc.
		// // % $Revision: 5.16 $ $Date: 2002/06/05 17:06:40 $
		// //
		// // na = length(a);
		// // nb = length(b);
		// //
		// // if na ~= prod(size(a)) | nb ~= prod(size(b))
		// // error('A and B must be vectors.');
		// // end
		// //
		// // % Convolution, polynomial multiplication, and FIR digital
		// // % filtering are all the same operations. Since FILTER
		// // % is a fast built-in primitive, we'll use it for CONV.
		// //
		// // % CONV(A,B) is the same as CONV(B,A), but we can make it go
		// // % substantially faster if we swap arguments to make the first
		// // % argument to filter the shorter of the two.
		// // if na > nb
		// // if nb > 1
		// // a(na+nb-1) = 0;
		// // end
		// // c = filter(b, 1, a);
		// // else
		// // if na > 1
		// // b(na+nb-1) = 0;
		// // end
		// // c = filter(a, 1, b);
		// // end
		double x[], y[];
		if (a.length > b.length) {
			x = new double[a.length + b.length - 1];
			y = b;
			for (int i = 0; i < a.length; i++) {
				x[i] = a[i];
			}
			for (int i = a.length; i < a.length + b.length - 1; i++) {
				x[i] = 0;
			}
		} else {
			x = new double[a.length + b.length - 1];
			y = a;
			for (int i = 0; i < b.length; i++) {
				x[i] = b[i];
			}
			for (int i = b.length; i < a.length + b.length - 1; i++) {
				x[i] = 0;
			}
		}

		double one[] = { 1 };

		Filter.filter(y, one, x, output);

		return output;
	}

	public static final double[] conv(double a[], double b[], double output[],
			IProgressMonitor monitor) {
		monitor = Util.monitorFor(monitor);
		monitor.beginTask("Convolving...", a.length + b.length - 1);
		double f[], g[];
		if (a.length > b.length) {
			f = a;
			g = b;
		} else {
			f = b;
			g = a;
		}

		for (int i = 0; i < g.length - 1; i++) {
			for (int j = 0; j <= i; j++)
				output[i] += f[i - j] * g[j];
			monitor.worked(1);
		}

		for (int i = g.length - 1; i < f.length; i++) {
			for (int j = g.length - 1; j >= 0; j--)
				output[i] += f[i - j] * g[j];
			monitor.worked(1);
		}

		for (int i = f.length; i < f.length + g.length - 1; i++) {
			for (int j = g.length - 1; j > i - f.length; j--) {
				output[i] += f[i - j] * g[j];
			}
			monitor.worked(1);
		}
		monitor.done();
		return output;
	}

	public static final double[] scaleToUnit(int a[], int max) {
		if (max == Integer.MIN_VALUE) {
			++max;
		}
		max = Math.abs(max);
		double[] y = new double[a.length];
		for (int i = 0; i < y.length; i++) {
			if(Math.abs(a[i]) > max) {
				// This works even for Integer.MIN_VALUE, check
				// javadoc for Math.abs()
				throw new IllegalArgumentException("Value in vector larger than max");
			}
			y[i] = a[i] / (double) max;
		}
		return y;
	}

	public static final double[] scaleToUnit(double a[], double max) {
		double[] y = new double[a.length];
		max = Math.abs(max);
		for (int i = 0; i < y.length; i++) {
			if(Math.abs(a[i]) > max)
				throw new IllegalArgumentException("Value in vector larger than max");
			y[i] = a[i] / max;
		}
		return y;
	}
	
	public static final int[] scaleToMax(double[] in, int max) {
		// This method may fail for Integer.MIN_VALUE
	
		if (max == Integer.MIN_VALUE) {
			++max;
		}
		max = Math.abs(max);
		double currentMax = maxAbs(in);
		int [] out = new int[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = (int) Math.round((in[i] / currentMax) * max );
		}
		return out;
	}

	public static final double[] scaleToMax(double[] in, double max) {
		// This method may fail for Integer.MIN_VALUE
		
		max = Math.abs(max);
		double currentMax = maxAbs(in);
		double [] out = new double[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = (in[i] / currentMax) * max ;
		}
		return out;
	}

	public static void wavWrite(double t[], String filename) {
		wavWrite(t, 1, filename);
	}

	public static void wavWrite(double t[], int channels, String filename) {
		wavWrite(t, channels, 16, filename);
	}

	public static void wavWrite(double t[], int channels, int bitsPerSample,
			String filename) {
		int[] samples = new int[t.length];
		for (int i = 0; i < t.length; ++i) {
			samples[i] = (int) t[i];
		}
		wavWrite(samples, channels, bitsPerSample, filename);
	}

	public static void wavWrite(int t[], int channels, int bitsPerSample,
			String filename) {
		byte[] samples = prepareSampleByteArray(t, bitsPerSample);
		wavWrite(samples, (double) 44100, bitsPerSample, channels, false, filename);
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
	
	private static byte[] prepareSampleByteArray(int[] samples, int bitsPerSample) {
		byte[] res = new byte[samples.length * (bitsPerSample / 8)];
		if (bitsPerSample == 32) {
			for (int i = 0; i < samples.length; i++) {
				int sample = samples[i];
				/* First byte is LSB (low order) */
				res[i * 4] = (byte) (sample & 0xff);
				res[i * 4 + 1] = (byte) (sample >> 8);
				res[i * 4 + 2] = (byte) (sample >> 16);
				/* Second byte is MSB (high order) */
				res[i * 4 + 3] = (byte) (sample >> 24);
			}
		} else if (bitsPerSample == 16) {
			for (int i = 0; i < samples.length; i++) {
				int sample = samples[i];
				/* First byte is LSB (low order) */
				res[i * 2] = (byte) (sample & 0xff);
				/* Second byte is MSB (high order) */
				res[i * 2 + 1] = (byte) (sample >> 8);
			}
		}
		return res;
	}

	public final static int[] wavRead(String filename) {
		int res[] = null;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(
					filename));
			res = AudioPlayer.readData(ais);
			ais.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public final static int[][] wavReadSplit(String filename) {
		int res[][] = null;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(
					filename));
			res = splitAudioStream(ais.getFormat().getChannels(), AudioPlayer
					.readData(ais));
			ais.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public final static double[][] wavReadSplitDouble(String filename) {
		double res[][] = null;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(
					filename));
			int data[][] = splitAudioStream(ais.getFormat().getChannels(),
					AudioPlayer.readData(ais));
			res = new double[data.length][data[0].length];
			for (int i = 0; i < res.length; i++) {
				AudioPlayer.normalizeInPlace(res[i], data[i], ais.getFormat()
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

	public final static byte[] downsample32to16(byte[] data) {
		byte[] res = new byte[data.length / 2];
		// int i = 0;
		int j = 0;
		for (int k = 0; k < data.length / 4; k++) {
			int oldSample = littleEndian(data[k * 4], data[k * 4 + 1],
					data[k * 4 + 2], data[k * 4 + 3]);
			int newSample = (Util.getLimit(16) * oldSample) / Util.getLimit(32);
			res[j++] = (byte) (newSample & 255);
			res[j++] = (byte) (newSample >> 8);
		}
		return res;
	}

	public final static int littleEndian(byte b1, byte b2, byte b3, byte b4) {
		return b4 << 24 | b3 << 16 | b2 << 8 | (255 & b1);
	}

	public final static int littleEndian(byte b1, byte b2) {
		return b2 << 8 | (255 & b1);
	}

	public static final void convolve(String input1, String input2,
			String output, IProgressMonitor monitor) {

		int a[] = wavRead(input1);
		int b[] = wavRead(input2);

		try {
			AudioInputStream ais1 = AudioSystem.getAudioInputStream(new File(
					input1));
			AudioInputStream ais2 = AudioSystem.getAudioInputStream(new File(
					input2));
			AudioFormat format1 = ais1.getFormat();
			AudioFormat format2 = ais2.getFormat();
			ais1.close();
			ais2.close();

			double[][] conv = new double[format1.getChannels()
					* format2.getChannels()][];

			int aa[][] = Util.splitAudioStream(format1.getChannels(), a);
			int bb[][] = Util.splitAudioStream(format2.getChannels(), b);
			int max1 = getLimit(format1.getSampleSizeInBits());
			int max2 = getLimit(format2.getSampleSizeInBits());

			monitor.beginTask("Convolving", 10 * aa.length * bb.length);

			int k = 0;
			for (int i = 0; i < aa.length; i++) {
				for (int j = 0; j < bb.length; j++) {
					double x[] = Util.scaleToUnit(aa[i], max1);
					double y[] = Util.scaleToUnit(bb[j], max2);
					conv[k] = new double[x.length + y.length - 1];
					monitor.subTask("ch " + (i + 1) + " x " + " ch " + (j + 1));
					IProgressMonitor subMonitor = Util.subMonitorFor(monitor,
							10);
					Util.conv(x, y, conv[k++], subMonitor);
				}
			}

			monitor.beginTask("Writing output", 2);
			double[] scaled = Util.scaleToMax(joinAudioStream(conv), (double) Util.getLimit(16));
			Util.wavWrite(scaled, conv.length, output);
			monitor.worked(2);

			monitor.done();


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static final IProgressMonitor monitorFor(IProgressMonitor monitor) {
		return monitor == null ? new NullProgressMonitor() : monitor;
	}

	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor,
			int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks);
	}

	public static final double average(double[] x) {
		return average(x, 0, x.length);
	}

	public static final double average(double[] x, int begin, int end) {
		double sum = 0;
		for (int i = begin; i < end; i++) {
			sum += x[i];
		}
		return sum / (end - begin);
	}

	// public static double[] average(double[]... arrays) {
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

	public static void wavAverage(String outFile, int bitsPerSample,
			List<String> files) {
		String[] f = new String[files.size()];
		f = files.toArray(f);
		wavAverage(outFile, bitsPerSample, f);
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
		double[] scaled = scaleToMax (average(arrays), (double) getLimit(bitsPerSample));
		wavWrite(scaled, 1, bitsPerSample, outFile);
	}

	static DecimalFormat _f = new DecimalFormat("#.######");

	public static int maxAbs(int[] data) {
		int max = 0;
		for (int i = 0; i < data.length; ++i) {
			if (Math.abs(data[i]) > max) {
				max = Math.abs(data[i]);
			}
		}
		return max;
	}
	
	public static int getLimit (int bitsPerSample) {
		return (1 << (bitsPerSample - 1)) - 1;
	}

}
