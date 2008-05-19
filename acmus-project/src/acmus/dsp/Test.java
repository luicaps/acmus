/*
 *  Test.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import acmus.audio.AudioPlayer;

@Deprecated
@SuppressWarnings("all")
public class Test {

	public static void main(String args[]) {
		// testHirata();
		// testDeMls();
		// testFht();
		// testMlsWav();
		// testMls();
		// testLundebyParam();
		// testLundeby();
		// testChu();
		// testFiltfilt();
		// testSweep();
		// testButtap();
		// testFft();
		// testFft2();
		// testFft3();
		// testDechirp();
		// testDechirp2();
		// testFilter();
		// testFilter2();
		// testFilter3(262144);
		// testConv();
		// testConv2();
		// testWavWrite();
		// testFilter3();
		// testWavAverage();
		// testSplit();
		// testChu2();
		testChu3();
	}

	public static void testHirata() {
		try {
			// double a[] = new double[4000];
			double fs = 44100;

			// for (int i = 0; i < a.length; i++) {
			// a[i] = i * 1000;
			// }
			BufferedReader br = new BufferedReader(new FileReader(
					"/home/lku/Workspace/acmus/data/ir.mat"));
			List l = new ArrayList();
			String line = br.readLine();
			while (line != null) {
				l.add(new Double(line.trim()));
				line = br.readLine();
			}
			double[] a = new double[l.size()];
			for (int i = 0; i < a.length; i++) {
				a[i] = ((Double) l.get(i)).doubleValue();
			}
			System.out.println(" len " + a.length);

			br = new BufferedReader(new FileReader(
					"/home/lku/Workspace/acmus/data/ir3.mat"));

			l.clear();
			line = br.readLine();
			while (line != null) {
				l.add(new Double(line.trim()));
				line = br.readLine();
			}
			double[] b = new double[l.size()];
			for (int i = 0; i < b.length; i++) {
				b[i] = ((Double) l.get(i)).doubleValue();
			}
			System.out.println(" len " + b.length);

			Parameters.hirataParam(a, b, fs, 0, 0, System.out, "/tmp");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testDeMls() {
		double rec[] = { 1, 2, 3, 4, 5, 6, 7, 8, 32, 6, 7, 83, 27, 8, 9 };
		double ref[] = { 0, 2, 4, 3, 5, 6, 7, 9, 34, 6, 8, 4, 44, 50 };

		int reps = 3;
		int n = 9;
		int l = (1 << n) - 1;
		double y[] = new double[l * reps];
		int row[] = new int[l];
		int col[] = new int[l];

		Signal.mls(n, 9, 5, y, row, col, reps);

		for (int i = 0; i < y.length; i++) {
			System.out.print(y[i] + " ");
		}

		double x[] = Util.reverse(y);
		double ir[] = Ir.demls(y, x, row, col, reps);

		for (int i = 0; i < ir.length; i++) {
			System.out.println(ir[i]);
		}
	}

	public static void testFht() {
		// double a[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		double a[] = { 2, 1, -3, -4, 1, 7, 4, 1, 1, 9, 1403, 0, 2 };

		Parameters.fht(a);
		Util.print(a);

	}

	public static void testMlsWav() {
		double mls[] = Signal.mls(15, 15, 1, 3);
		Util.multLLL(mls, 0.5);
		double[] scaled = Util.scaleToMax (mls, (double) Util.getLimit(16));
		Util.wavWrite(scaled, "/tmp/acmus-mls.wav");
	}

	public static void testMls() {
		int n = 9;
		int l = (1 << n) - 1;
		double y[] = new double[l];
		int row[] = new int[l];
		int col[] = new int[l];

		Signal.mls(n, 9, 5, y, row, col);
		System.out.println("y = ");
		Util.print(y);
		System.out.println("row = ");
		Util.print(row);
		System.out.println("col = ");
		Util.print(col);
	}

	public static void testChu() {
		try {
			// double a[] = new double[4000];
			double fs = 44100;

			// for (int i = 0; i < a.length; i++) {
			// a[i] = i * 1000;
			// }
			BufferedReader br = new BufferedReader(new FileReader(
					"/home/lku/Workspace/acmus/data/ir3.mat"));
			PrintStream ps = new PrintStream(new FileOutputStream(
					"/tmp/acmus.tmp.txt"));
			List l = new ArrayList();
			String line = br.readLine();
			while (line != null) {
				l.add(new Double(line.trim()));
				line = br.readLine();
			}
			double[] a = new double[l.size()];
			for (int i = 0; i < a.length; i++) {
				a[i] = ((Double) l.get(i)).doubleValue();
			}
			System.out.println(" len " + a.length);

			Parameters.chuParamOld(a, null, fs, 0, 0, ps, "/tmp", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testChu2() {
		try {
			// double a[] = new double[4000];
			double fs = 44100;

			double[] a = Util.scaleToUnit(Util.wavRead("/tmp/ir.wav"),
					Util.getLimit(32));
			double[] b = Util.scaleToUnit(Util.wavRead("/tmp/ir2.wav"),
					Util.getLimit(32));
			System.out.println(" len " + a.length);

			PrintStream ps = new PrintStream(new FileOutputStream(
					"/tmp/acmus.tmp.txt"));
			long time = System.currentTimeMillis();
			Parameters.chuParamOld(a, b, fs, 0, 0, ps, "/tmp", null);
			System.out.println(System.currentTimeMillis() - time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testChu3() {
		try {
			// double a[] = new double[4000];
			double fs = 44100;

			double[] a = Util.scaleToUnit(Util.wavRead("/tmp/ir.wav"),
					Util.getLimit(32));
			double[] b = Util.scaleToUnit(Util.wavRead("/tmp/ir2.wav"),
					Util.getLimit(32));
			System.out.println(" len " + a.length);

			PrintStream ps = new PrintStream(new FileOutputStream(
					"/tmp/acmus.tmp.txt"));
			long time = System.currentTimeMillis();
			Parameters p = new Parameters(a, b, 44100);
			p.chuParam(0, 0, ps, "/tmp", null);
			System.out.println(System.currentTimeMillis() - time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testLundeby() {
		try {
			// double a[] = new double[4000];
			double fs = 44100;

			// for (int i = 0; i < a.length; i++) {
			// a[i] = i * 1000;
			// }
			BufferedReader br = new BufferedReader(new FileReader(
					"/home/lku/Workspace/acmus/data/ir.mat"));
			List l = new ArrayList();
			String line = br.readLine();
			while (line != null) {
				l.add(new Double(line.trim()));
				line = br.readLine();
			}
			double[] a = new double[l.size()];
			for (int i = 0; i < a.length; i++) {
				a[i] = ((Double) l.get(i)).doubleValue();
			}
			System.out.println(" len " + a.length);

			Parameters.lundeby(a, fs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testLundebyParam() {
		try {
			// double a[] = new double[4000];
			double fs = 44100;

			// for (int i = 0; i < a.length; i++) {
			// a[i] = i * 1000;
			// }
			BufferedReader br = new BufferedReader(new FileReader(
					"/home/lku/Workspace/acmus/data/ir.mat"));
			List l = new ArrayList();
			String line = br.readLine();
			while (line != null) {
				l.add(new Double(line.trim()));
				line = br.readLine();
			}
			double[] a = new double[l.size()];
			for (int i = 0; i < a.length; i++) {
				a[i] = ((Double) l.get(i)).doubleValue();
			}
			System.out.println(" len " + a.length);

			Parameters.lundebyParam(a, null, fs, 0, 0, System.out, "/tmp");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testFiltfilt() {
		double a[] = { 1, 2, 3, 4 };
		double b[] = { 5, 6, 7, 8 };
		// double x[] = { 5, 6, 7, 8, 5, 6, 7, 8, 5, 6, 7, 8 , 9 , 9, 9 ,9, 9 ,
		// 9, 9
		// ,9, 9 , 9, 9 ,9};
		double x[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
		double y[] = Filter.filtfilt(b, a, x);

		for (int i = 0; i < y.length; i++)
			System.out.println(y[i]);

	}

	public static void testFilter() {
		double x[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };

		double b[] = { 1, 0, -3, 0, 1, 0, 1, 0, 1 };
		double a[] = { 2, 1, -3, -4, 1, 1, 4, 1, 1 };

		double ir[] = Filter.filter(b, a, x);
		for (int i = 0; i < ir.length; i++) {
			System.out.println(Util._f.format(ir[i]));
		}
	}

	public static void testFilter2() {
		// double x[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
		//
		// double b[] = { 1, 0, -3, 0, 1, 0, 1, 0, 1 };
		// double a[] = { 2, 1, -3, -4, 1, 1, 4, 1, 1 };
		// double zi[] = {2 ,4, 5, -1, 3,4, 1,-3} ;

		double x[] = { 1, 1, 1, 1, 1, 1, 1 };

		double b[] = { 2, 1 };
		double a[] = { 1, 1, 1 };
		double zi[] = { 2, 1 };

		double ir[] = Filter.filterZ(b, a, x, zi);
		for (int i = 0; i < ir.length; i++) {
			System.out.println(Util._f.format(ir[i]));
		}
	}

	public static void testDechirp() {
		double x[] = { 1, 2, 3, 4, 5, 6, 7, 8 };
		double y[] = { 0, 2, 4, 3, 5, 6, 7, 9 };
		// double b[] = {0.068958, 0, -0.27583, 0, 0.41375, 0, -0.27583, 0,
		// 0.068958};
		// double a[] = {1, -4.3613, 7.9766, -8.3677, 5.9555, -3.027, 0.9634,
		// -0.15979, 0.020293};
		// double ir[] = {0.87795, -0.02162, 0.027758, 0.023528, -0.048674,
		// 0.0059901, 0.043819, -0.045384, -0.05117, 0.10738, 0.042063,
		// -0.16144,
		// 0.10651, -0.01483, 0.0017451, 0.10638};

		double b[] = { 6.8958198127898718e-02, 0.0000000000000000e+00,
				-2.7583279251159487e-01, 0.0000000000000000e+00,
				4.1374918876739225e-01, 0.0000000000000000e+00,
				-2.7583279251159487e-01, 0.0000000000000000e+00,
				6.8958198127898718e-02 };
		double a[] = { 1.0000000000000000e+00, -4.3613393544232331e+00,
				7.9766493056040328e+00, -8.3676930923035275e+00,
				5.9554956728855464e+00, -3.0270160667461750e+00,
				9.6339964091306085e-01, -1.5978865589624947e-01,
				2.0292549971106792e-02 };
		// double ir[] = {8.7795294836709159e-01, -2.1619707439127157e-02,
		// 2.7757686718965925e-02,
		// 2.3528124996866642e-02,-4.8674336678340813e-02,
		// 5.9900905029896525e-03,4.3818659747500370e-02,
		// -4.5384208584541542e-02,
		// -5.1169816398999091e-02, 1.0738264068538364e-01,
		// 4.2063183738654059e-02,
		// -1.6144387813396605e-01, 1.0650658932563289e-01,
		// -1.4829946826169206e-02,
		// 1.7450851794950087e-03, 1.0637688479856403e-01};

		// double b[] = { 0.0690, 0, -0.2758, 0, 0.4137, 0, -0.2758, 0, 0.0690
		// };
		// double a[] = { 1.0000, -4.3613, 7.9766, -8.3677, 5.9555, -3.0270,
		// 0.9634,
		// -0.1598, 0.0203 };
		double ir[] = Ir.dechirp(x, y, b, a, 16, null);
		// ir = filter(b, a, ir);
		for (int i = 0; i < ir.length; i++) {
			System.out.println(ir[i]);
		}
	}

	public static void testDechirp2() {
		// int N = 44100 * 8;
		int N = (int) Math.pow(2, 18);

		double x[] = new double[N];
		double y[] = new double[N];
		double x2[] = new double[N];
		double y2[] = new double[N];

		double b[] = { 6.8958198127898718e-02, 0.0000000000000000e+00,
				-2.7583279251159487e-01, 0.0000000000000000e+00,
				4.1374918876739225e-01, 0.0000000000000000e+00,
				-2.7583279251159487e-01, 0.0000000000000000e+00,
				6.8958198127898718e-02 };
		double a[] = { 1.0000000000000000e+00, -4.3613393544232331e+00,
				7.9766493056040328e+00, -8.3676930923035275e+00,
				5.9554956728855464e+00, -3.0270160667461750e+00,
				9.6339964091306085e-01, -1.5978865589624947e-01,
				2.0292549971106792e-02 };

		Random r = new Random();
		for (int i = 0; i < N; i++) {
			x[i] = x2[i] = r.nextInt(30000) - 15000;
			y[i] = y2[i] = r.nextInt(30000) - 15000;
		}

		long t = System.currentTimeMillis();
		double ir[] = Ir.dechirp(x, y, b, a, 2 * N, null);
		System.out.println("done: " + (System.currentTimeMillis() - t));
		for (int i = 0; i < 10; i++) {
			System.out.println(ir[i]);
		}
		for (int i = ir.length - 10; i < ir.length; i++) {
			System.out.println(ir[i]);
		}

		// t = System.currentTimeMillis();
		// ir = Ir.dechirp2(x, y, b, a, 2 * N);
		// System.out.println("done: " + (System.currentTimeMillis() - t));
		// for (int i = 0; i < 10; i++) {
		// System.out.println(ir[i]);
		// }
		// for (int i = ir.length - 10; i < ir.length; i++) {
		// System.out.println(ir[i]);
		// }

	}

	public static void testFft() {
		double xre[] = { 1, 2, 3, 4, 5, 6, 7, 8, 0 };
		double xim[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		FFT1d f = new FFT1d(xre.length);
		f.fft(xre, xim);
		for (int i = 0; i < xre.length; i++) {
			System.out.println(Util._f.format(xre[i]) + " "
					+ Util._f.format(xim[i]) + "i");
		}
		IFFT1d ifft = new IFFT1d(xre.length);
		ifft.ifft(xre, xim);
		for (int i = 0; i < xre.length; i++) {
			System.out.println(Util._f.format(xre[i]) + " "
					+ Util._f.format(xim[i]) + "i");
		}
		// doFFT(x);
	}

	public static void testFft2() {
		double xre[] = { 1, 2, 3, 4, 5, 6, 7, 8 };
		double xim[] = { 0, 0, 0, 0, 0, 0, 0, 0 };
		double xre2[] = (double[]) xre.clone();
		double xim2[] = (double[]) xim.clone();

		FFT1d f = new FFT1d(xre.length);
		f.fft(xre, xim);
		Parameters.doFFT(xre2, xre2, xim2);

		for (int i = 0; i < xre.length; i++) {
			System.out.println(xre[i] + " " + xim[i] + "i  |  " + xre2[i] + " "
					+ xim2[i] + "i");
		}
	}

	public static void testFft3() {
		int N = (int) Math.pow(2, 18);

		double x[] = new double[N];
		double y[] = new double[N];
		double x2[] = new double[N];
		double y2[] = new double[N];

		Random r = new Random();
		for (int i = 0; i < N; i++) {
			x[i] = x2[i] = r.nextInt(30000) - 15000;
		}

		long t = System.currentTimeMillis();
		FFT1d f = new FFT1d(x.length);
		f.fft(x, y);
		System.out.println("done: " + (System.currentTimeMillis() - t));
		t = System.currentTimeMillis();
		Parameters.doFFT(x2, x2, y2);
		System.out.println("done: " + (System.currentTimeMillis() - t));
		for (int i = 0; i < 10; i++) {
			System.out.println(x[i] + " " + y[i] + "i  |  " + x2[i] + " "
					+ y2[i] + "i");
		}
		for (int i = x.length - 10; i < x.length; i++) {
			System.out.println(x[i] + " " + y[i] + "i  |  " + x2[i] + " "
					+ y2[i] + "i");
		}

	}

	public static void testButtap() {
		int n = 10;
		double pRe[] = new double[n];
		double pIm[] = new double[n];

		double k = Filter.buttap(n, pRe, pIm);

		for (int i = 0; i < n; i++) {
			System.out.println(Util._f.format(pRe[i]) + " "
					+ Util._f.format(pIm[i]) + "i");
		}
		System.out.println(Util._f.format(k));
	}

	public static void testSweep() {
		double y[] = Signal.sweepLog(44100, 3, 20, 20000);
		for (int i = 0; i < y.length; i++) {
			// System.out.println(_f.format(y[i]));
			// y[i] = 0.8*y[i];
		}
		// double y[] = sweepLog(10, 1, 20, 40);
		double[] scaled = Util.scaleToMax (y, (double) Util.getLimit(16));
		Util.wavWrite(scaled, "/tmp/foo.wav");
		// for (int i = 0; i < y.length; i++) { System.out.println(y[i] + " ");
		// }
	}

	public static void testFilter3(int n) {
		double b[] = { 6.8958198127898718e-02, 0.0000000000000000e+00,
				-2.7583279251159487e-01, 0.0000000000000000e+00,
				4.1374918876739225e-01, 0.0000000000000000e+00,
				-2.7583279251159487e-01, 0.0000000000000000e+00,
				6.8958198127898718e-02 };
		double a[] = { 1.0000000000000000e+00, -4.3613393544232331e+00,
				7.9766493056040328e+00, -8.3676930923035275e+00,
				5.9554956728855464e+00, -3.0270160667461750e+00,
				9.6339964091306085e-01, -1.5978865589624947e-01,
				2.0292549971106792e-02 };
		double zi[] = { 1, 32, 34, 1, 5, 6, 7, 8, 8 };

		try {
			double fs = 44100;

			DecimalFormat f = new DecimalFormat("0.000000E00");

			BufferedReader br = new BufferedReader(new FileReader(
					"/home/lku/Workspace/acmus/data/c/ir.mat"));
			double[] x = new double[n];
			double y[] = new double[n];
			String line = br.readLine();
			int j = 0;
			while (line != null) {
				x[j++] = Double.parseDouble(line.trim());
				line = br.readLine();
			}
			long t = System.currentTimeMillis();

			for (int i = 0; i < 20; i++) {
				Filter.filterZ(b, a, x, zi, y);
			}

			System.out.println(System.currentTimeMillis() - t);

			// for (int i = 0 ; i < n; i++) {
			// System.out.println(f.format(y[i]));
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void testConv() {
		try {
			double z[];
			{
				double x[], y[];

				{
					AudioInputStream ais = AudioSystem
							.getAudioInputStream(new File(
									"/home/lku/Workspace/acmus/data/tone1.wav"));
					int a[] = AudioPlayer.readData(ais);
					ais = AudioSystem.getAudioInputStream(new File(
							"/home/lku/Workspace/acmus/data/ir3.wav"));
					int b[] = AudioPlayer.readData(ais);

					System.out.println(a.length + " " + b.length);
					x = Util.scaleToUnit(a, Util.getLimit(16));
					y = Util.scaleToUnit(b, Util.getLimit(16));
					System.out.println(x.length + " " + y.length);
				}

				z = new double[x.length + y.length - 1];
				long t = System.currentTimeMillis();
				Util.conv(x, y, z, null);
				System.out.println("t: " + (System.currentTimeMillis() - t));
				System.out.println(z.length);
			}
			double[] scaled = Util.scaleToMax(z, (double) Util.getLimit(16));
			Util.wavWrite(scaled, "/home/lku/Workspace/acmus/data/tone1conv4.wav");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testConv2() {
		double a[] = { 2, -2, 1 };
		double b[] = { 1, 3, 0.5, -1 };
		double z[] = new double[a.length + b.length - 1];

		Util.conv2(a, b, z);
		Util.print(z);

	}

	public static void testFilter3() {
		double a[] = { 2, -2, 1 };
		double b[] = { 1, 0, 0 };
		double x[] = { 1, 3, 0.5, -1 };
		double y[] = new double[a.length + x.length - 1];

		// Filter.filter(a,b,x,y);
		y = Filter.filter(a, b, x);
		Util.print(y);

	}

	public static void testWavWrite() {
		double x[] = { 1.0, 1.0, 1.0, 1.0, 1.0, -1.0, -1.0, -1.0, -1.0, -1.0 };
		double y[] = new double[44100];
		for (int i = 0; i < 5000; i++) {
			y[2 * i + 30000] = 1;
			y[2 * i + 30001] = -1;
		}
		double[] scaled = Util.scaleToMax(x, (double) Util.getLimit(16));
		Util.wavWrite(scaled, 1, 16, "/tmp/clip16.wav");
		scaled = Util.scaleToMax(x, (double) Util.getLimit(32));
		Util.wavWrite(x, 1, 32, "/tmp/clip32.wav");
		scaled = Util.scaleToMax(y, (double) Util.getLimit(16));
		Util.wavWrite(y, 1, 16, "/tmp/clip.wav");
	}

	public static void testSplit() {
		int[] d = Util.wavRead("/tmp/stereo.wav");
		int[][] dd = Util.splitAudioStream(2, d);
		Util.wavWrite(dd[0], 1, 16, "/tmp/split1.wav");
		Util.wavWrite(dd[1], 1, 16, "/tmp/split2.wav");
	}

	public static void testWavAverage() {
		int x1[] = { 0, 1, 2, 3, 4, 5, -1, -2, -3, -4, -5 };
		int x2[] = { 0, 1, 2 };
		int x3[] = { 0, 1, 2, 3, 4, 5 };
		int x4[] = { 0, 1, 2, 3, 4, 5, -1, -2, -3 };
		Util.wavWrite(x1, 1, 32, "/tmp/1.wav");
		Util.wavWrite(x2, 1, 32, "/tmp/2.wav");
		Util.wavWrite(x3, 1, 32, "/tmp/3.wav");
		Util.wavWrite(x4, 1, 32, "/tmp/4.wav");

		Util.wavAverage("/tmp/avg.wav", 32, "/tmp/1.wav", "/tmp/2.wav",
				"/tmp/3.wav", "/tmp/4.wav");
		int[] d = Util.wavRead("/tmp/avg.wav");
		for (int i = 0; i < d.length; i++) {
			System.out.print(d[i] + " ");
		}
		System.out.println();

	}

}
