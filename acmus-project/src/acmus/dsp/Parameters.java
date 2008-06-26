/*
 *  Parameters.java
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
/*
 * Dsp.java
 * Created on 07/04/2005
 */
package acmus.dsp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import acmus.AcmusApplication;
import acmus.util.Algorithms;
import acmus.util.ArrayUtils;
import acmus.util.MathUtils;
import acmus.util.PrintUtils;
import acmus.util.SWTUtils;

/**
 * @author lku
 */
public class Parameters {

	public static String CHANNEL_NAMES[] = { "62.5", "125", "250", "500",
			"1000", "2000", "4000", "8000", "A", "C", "Linear" };

	public static String energ[] = { "C50", "C80", "ST1", "D50", "D80", "DRR",
			"CT" };
	public static String energUnit[] = { "dB", "dB", "dB", "%", "%", "%", "ms" };
	public static String reverb[] = { "EDT", "T20", "T30", "T40" };

	// ==========================================================================
	double _ir[];
	double _ir2[];
	double _temp[];
	double _fs;

	public Parameters(double[] ir, double[] ir2, double fs) {
		_ir = ir;
		_ir2 = ir2;
		_fs = fs;
		_temp = new double[_ir.length];
	}

	private final double rms(double[] signal) {
		int offset = (int) Math.round(0.9 * signal.length) - 1;
		int length = signal.length - offset;
		for (int j = 0; j < length; j++) {
			_temp[j] = signal[offset + j] * signal[offset + j];
		}
		return ArrayUtils.mean(_temp, 0, length);
	}

	private double[] chuPower(double[] signal) {
		double RMS = rms(signal);
		int comeco = inicio(signal);
		double power[] = ArrayUtils.sumLLL(ArrayUtils.sqrLLL(ArrayUtils.subArray(signal, comeco,
				signal.length)), -RMS);
		return power;
	}

	public void chuParam(int directSound, int firstReflection,
			PrintStream outTable, String graphFolder, IProgressMonitor monitor) {
		monitor = SWTUtils.monitorFor(monitor);

		monitor.beginTask("Calculating parameters", 140);

		IProgressMonitor subMonitor = SWTUtils.subMonitorFor(monitor, 20);

		double[][] banda = filtros(_ir, _fs, subMonitor);

		double[][] bandaLf = null;
		if (_ir2 != null)
			bandaLf = filtros(_ir2, _fs, subMonitor);

		PrintStream outGraphs[] = channelFiles(graphFolder);

		Map<String, Parameter> params = new HashMap<String, Parameter>();
		for (int i = 0; i < energ.length; i++) {
			params.put(energ[i], new Parameter(energ[i], energUnit[i]));
		}
		if (_ir2 != null) {
			params.put("LF", new Parameter("LF", "%"));
		}
		for (int i = 0; i < reverb.length; i++) {
			params.put(reverb[i], new Parameter(reverb[i], "s"));
		}

		for (int n = 0; n < banda.length; n++) {
			double aux[] = chuPower(banda[n]);

			double en[] = energeticos(aux, _fs, directSound, firstReflection);
			for (int i = 0; i < energ.length - 1; i++) {
				params.get(energ[i]).val[n] = en[i];
			}

			params.get("CT").val[n] = en[en.length - 1] * 1000;

			if (_ir2 != null) {
				double auxLf[] = chuPower(bandaLf[n]);
				params.get("LF").val[n] = lf(aux, auxLf, _fs);
			}

			double rev[] = reverberacao(outGraphs[n], aux, _fs);
			outGraphs[n].close();

			for (int i = 0; i < rev.length; i++) {
				params.get(reverb[i]).val[n] = rev[i];
			}
			monitor.worked(10);
		}

		List<String> pOrder = new ArrayList<String>();
		for (String pName : energ)
			pOrder.add(pName);
		if (_ir2 != null) {
			pOrder.add("LF");
		}
		for (String pName : reverb)
			pOrder.add(pName);

		double br = bassRatio(params.get("T20"));
		double tr = trebleRatio(params.get("T20"));

		tabela(outTable, params, pOrder, banda.length, br, tr, (int) Math
				.round((firstReflection - directSound) / _fs * 1000));
		monitor.done();
	}

	// ==========================================================================
	private static int doFFT_nu;

	public static final void doFFT(double[] x, double[] xre, double[] xim) {
		int n;
		// assume n is a power of 2
		n = x.length;
		double ld = (Math.log(n) / Math.log(2.0));

		if ((double) ((int) ld) - ld != 0) {
			System.out.println("Klasse FastFourierTransformation:");
			System.out
					.println("Der uebergebene Vektor hat keine laenge von 2 hoch n.");
			System.out.println("Die Laenge ist:" + n
					+ " Der Logarithmus Dualis ist:" + ld);
			return;
		}
		doFFT_nu = (int) ld;
		int n2 = n / 2;
		int nu1 = doFFT_nu - 1;
		double tr, ti, p, arg, c, s;
		for (int i = 0; i < n; i++) {
			xre[i] = x[i];
			xim[i] = 0.0f;
		}
		int k = 0;

		for (int l = 1; l <= doFFT_nu; l++) {
			while (k < n) {
				for (int i = 1; i <= n2; i++) {
					p = bitrev(k >> nu1);
					arg = 2 * (double) Math.PI * p / n;
					c = (double) Math.cos(arg);
					s = (double) Math.sin(arg);
					tr = xre[k + n2] * c + xim[k + n2] * s;
					ti = xim[k + n2] * c - xre[k + n2] * s;
					xre[k + n2] = xre[k] - tr;
					xim[k + n2] = xim[k] - ti;
					xre[k] += tr;
					xim[k] += ti;
					k++;
				}
				k += n2;
			}
			k = 0;
			nu1--;
			n2 = n2 / 2;
		}
		k = 0;
		int r;
		while (k < n) {
			r = bitrev(k);
			if (r > k) {
				tr = xre[k];
				ti = xim[k];
				xre[k] = xre[r];
				xim[k] = xim[r];
				xre[r] = tr;
				xim[r] = ti;
			}
			k++;
		}

	}

	public static void PowerSpectrum(int NumSamples, double[] In, double[] Out) {
		int Half = NumSamples / 2;
		int i;

		double theta = Math.PI / Half;

		double[] RealOut = new double[Half];
		double[] ImagOut = new double[Half];

		for (i = 0; i < Half; i++) {
			RealOut[i] = In[2 * i];
			ImagOut[i] = In[2 * i + 1];
		}

		FFT1d fft = new FFT1d(Half);
		fft.fft(RealOut, ImagOut);

		double wtemp = (double) (Math.sin(0.5 * theta));

		double wpr = -2.0 * wtemp * wtemp;
		double wpi = (double) (Math.sin(theta));
		double wr = 1.0 + wpr;
		double wi = wpi;

		int i3;

		double h1r, h1i, h2r, h2i, rt, it;

		for (i = 1; i < Half / 2; i++) {

			i3 = Half - i;

			h1r = 0.5 * (RealOut[i] + RealOut[i3]);
			h1i = 0.5 * (ImagOut[i] - ImagOut[i3]);
			h2r = 0.5 * (ImagOut[i] + ImagOut[i3]);
			h2i = -0.5 * (RealOut[i] - RealOut[i3]);

			rt = h1r + wr * h2r - wi * h2i;
			it = h1i + wr * h2i + wi * h2r;

			Out[i] = rt * rt + it * it;

			rt = h1r - wr * h2r + wi * h2i;
			it = -h1i + wr * h2i + wi * h2r;

			Out[i3] = rt * rt + it * it;

			wr = (wtemp = wr) * wpr - wi * wpi + wr;
			wi = wi * wpr + wtemp * wpi + wi;
		}

		rt = (h1r = RealOut[0]) + ImagOut[0];
		it = h1r - ImagOut[0];
		Out[0] = rt * rt + it * it;

		rt = RealOut[Half / 2];
		it = ImagOut[Half / 2];
		Out[Half / 2] = rt * rt + it * it;

	}

	public static double[] fftMag(double[] xre, double xim[]) {
		int n = xre.length;
		double[] mag = new double[n / 2];
		// calculate Magnitude
		for (int i = 0; i < n / 2; i++)
			mag[i] = (xre[i] * xre[i] + xim[i] * xim[i]);
		return mag;
	}

	private static int bitrev(int j) {

		int j2;
		int j1 = j;
		int k = 0;
		for (int i = 1; i <= doFFT_nu; i++) {
			j2 = j1 / 2;
			k = 2 * k + j1 - 2 * j2;
			j1 = j2;
		}
		return k;
	}

	// * fht.c
	// *
	// * Calculates the Fast Hadamard Transform of a radix 2 sequence.
	// * The input vector must be a colummwise vector, os size 2^n.
	// *
	// * Bruno Masiero, Feb 2004
	public static final void fht(double[] data) {
		int pwr2 = (int) MathUtils.log2(data.length);
		int length = 1 << pwr2;
		for (int i = pwr2; i > 0; i--) {
			int ie = 1 << i;
			int ie_half = ie >> 1;

			for (int j = 0; j < ie_half; j++) {
				for (int k = j; k < length; k += ie) {
					int kp = k + ie_half;
					double temp = data[k] + data[kp];
					data[kp] = data[k] - data[kp];
					data[k] = temp;
				}

			}
		}
		// // }
	}

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// % chuparam.m
	// %
	// %Esta funcao calcula os diversos parametros acusticos de uma sala. O
	// %tratamento do sinal e feito pelo metodo de Chu.
	// %
	// %[s]=chuparam(IR,fs)
	// %
	// %Tem como entrada a resposta impulsiva e a taxa de amostragem.
	// %Tem como saida uma arquivo de texto com o valor dos parametros para as
	// %diversas bandas de frequencia. Se desejado, fornece uma matrix com estes
	// %valores, onde a primeira linha sao as frequencias centrais, ao inves do
	// %arquivo de texto.
	// %A variavel flag = 1 indica se deseja ver os graficos ca curva de
	// Schroeder
	//
	// function [saida]=chuparam(IR,fs,flag)
	public static void chuParamOld(double[] ir, double[] irLf, double fs,
			int directSound, int firstReflection, PrintStream outTable,
			String graphFolder, IProgressMonitor monitor) {
		monitor = SWTUtils.monitorFor(monitor);

		monitor.beginTask("Calculating parameters", 140);

		IProgressMonitor subMonitor = SWTUtils.subMonitorFor(monitor, 20);

		// // banda = filtros(IR,fs);
		double[][] banda = filtros(ir, fs, subMonitor);

		// // ruido = banda(round(.9*length(banda)):end,:).^2;
		int offset = (int) Math.round(0.9 * banda[0].length) - 1;
		double ruido[][] = new double[banda.length][banda[0].length - offset];
		for (int i = 0; i < ruido.length; i++) {
			for (int j = 0; j < ruido[i].length; j++) {
				ruido[i][j] = banda[i][offset + j] * banda[i][offset + j];
			}
		}

		double[][] bandaLf = null;
		double ruidoLf[][] = null;
		double RMSLf[] = null;
		if (irLf != null) {
			IProgressMonitor subMonitorLf = SWTUtils.subMonitorFor(monitor, 10);
			bandaLf = filtros(irLf, fs, subMonitorLf);

			// // ruido = banda(round(.9*length(banda)):end,:).^2;
			int offsetLf = (int) Math.round(0.9 * bandaLf[0].length) - 1;
			ruidoLf = new double[bandaLf.length][bandaLf[0].length - offsetLf];
			for (int i = 0; i < ruidoLf.length; i++) {
				for (int j = 0; j < ruidoLf[i].length; j++) {
					ruidoLf[i][j] = bandaLf[i][offsetLf + j]
							* bandaLf[i][offsetLf + j];
				}
			}
			RMSLf = ArrayUtils.sumLines(ruidoLf);
			RMSLf = ArrayUtils.scale(1.0/(ruidoLf[0].length), RMSLf);
		}

		PrintStream outGraphs[] = channelFiles(graphFolder);

		monitor.worked(10);
		double RMS[] = ArrayUtils.sumLines(ruido);
		RMS = ArrayUtils.scale(1.0/(ruido[0].length), RMS);

		monitor.worked(10);

		Map<String, Parameter> params = new HashMap<String, Parameter>();
		for (int i = 0; i < energ.length; i++) {
			params.put(energ[i], new Parameter(energ[i], energUnit[i]));
		}
		if (irLf != null) {
			params.put("LF", new Parameter("LF", ""));
		}
		for (int i = 0; i < reverb.length; i++) {
			params.put(reverb[i], new Parameter(reverb[i], "s"));
		}

		for (int n = 0; n < banda.length; n++) {
			int comeco = inicio(banda[n]);
			double aux[] = ArrayUtils.sumLLL(ArrayUtils.sqrLLL(ArrayUtils.subArray(banda[n],
					comeco, banda[n].length)), -RMS[n]);
			double en[] = energeticos(aux, fs, directSound, firstReflection);
			for (int i = 0; i < energ.length - 1; i++) {
				params.get(energ[i]).val[n] = en[i];
			}

			params.get("CT").val[n] = en[en.length - 1] * 1000;

			if (irLf != null) {
				int comecoLf = inicio(bandaLf[n]);
				System.out.println("b " + bandaLf[n][2000]);
				double auxLf[] = ArrayUtils.sumLLL(ArrayUtils.sqrLLL(ArrayUtils.subArray(
						bandaLf[n], comecoLf, bandaLf[n].length)), -RMSLf[n]);
				params.get("LF").val[n] = lf(aux, auxLf, fs);
			}

			double rev[] = reverberacao(outGraphs[n], aux, fs);
			outGraphs[n].close();

			for (int i = 0; i < rev.length; i++) {
				params.get(reverb[i]).val[n] = rev[i];
			}
			monitor.worked(10);
		}

		List<String> pOrder = new ArrayList<String>();
		for (String pName : energ)
			pOrder.add(pName);
		if (irLf != null) {
			pOrder.add("LF");
		}
		for (String pName : reverb)
			pOrder.add(pName);

		double br = bassRatio(params.get("T20"));
		double tr = trebleRatio(params.get("T20"));

		tabela(outTable, params, pOrder, banda.length, br, tr, (int) Math
				.round((firstReflection - directSound) / fs * 1000));
		monitor.done();
	}

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// % inicio.m
	// %
	// % [ponto,rms] = inicio(impulse)
	// %
	// %% Acha o inicio do sinal de acordo com as recomendacoes da ISO 3382
	// % e se desejado, fornece o nivel de ruido antes do inicio do sinal.
	// % A entrada e a resposta impulsiva do sinal.
	//
	public final static int inicioOld(double impulse[]) {
		int maximo = ArrayUtils.absMaxIndex(impulse);
		double energia[] = ArrayUtils.sqrLLL(ArrayUtils.scale(1.0/(impulse[maximo]), impulse));
		int ponto = maximo;
		for (int i = 0; i < maximo; i++) {
			if (energia[i] > 0.01) {
				ponto = i;
				break;
			}
		}

		return ponto;
	}

	// // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// // % inicio.m
	// // %
	// // % [ponto,rms] = inicio(impulse)
	// // %
	// // %% Acha o inicio do sinal de acordo com as recomendacoes da ISO 3382
	// // % e se desejado, fornece o nivel de ruido antes do inicio do sinal.
	// // % A entrada e a resposta impulsiva do sinal.
	// //
	// // function [ponto,rms] = inicio(impulse)
	public final static int inicio(double impulse[]) {
		int maximo = ArrayUtils.absMaxIndex(impulse);

		double energia[] = ArrayUtils.sqrLLL(ArrayUtils.scale(1.0/(impulse[maximo]), impulse));
		int ponto = maximo;

		for (int i = maximo / 2; i < maximo; i++) {
			if (energia[i] > 0.001) {
				ponto = i;
				break;
			}
		}

		return ponto;
	}

	// // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// // % energeticos.m
	// //
	// // %Esta funcao calcula alguns dos parametros acusticos para
	// // %salas.
	// // %Tem como entrada a resposta impulsiva ao quadrado e a
	// // %taxa de amostragem do sinal.
	// //
	// // function [C50,C80,D50,D80,CT]=energeticos(energia,Fs);
	public final static double[] energeticos(double[] energia, double fs,
			int somDireto, int primeiraReflexao) {
		int t10 = (int) Math.round(0.01 * fs);
		int t20 = (int) Math.round(0.02 * fs);
		int t50 = (int) Math.round(0.05 * fs);
		int t80 = (int) Math.round(0.08 * fs);
		int t100 = (int) Math.round(0.1 * fs);
		double c50 = 10 * MathUtils.log10(ArrayUtils.sum(energia, 0, t50)
				/ ArrayUtils.sum(energia, t50 - 1, energia.length));
		double c80 = 10 * MathUtils.log10(ArrayUtils.sum(energia, 0, t80)
				/ ArrayUtils.sum(energia, t80 - 1, energia.length));
		double st1 = 10 * MathUtils.log10(ArrayUtils.sum(energia, t20, t100)
				/ ArrayUtils.sum(energia, 0, t10));

		double d50 = (ArrayUtils.sum(energia, 0, t50) / ArrayUtils.sum(energia)) * 100;
		double d80 = (ArrayUtils.sum(energia, 0, t80) / ArrayUtils.sum(energia)) * 100;

		// drr (not in matlab prototype)
		double drr = (ArrayUtils.sum(energia, somDireto, primeiraReflexao) / ArrayUtils
				.sum(energia, somDireto, energia.length)) * 100;

		double x[] = new double[energia.length];
		for (int i = 0; i < x.length; i++)
			x[i] = i / fs;
		double ct = ArrayUtils.sum(ArrayUtils.multLLL(x, energia)) / ArrayUtils.sum(energia);

		double res[] = new double[7];
		res[0] = c50;
		res[1] = c80;
		res[2] = st1;
		res[3] = d50;
		res[4] = d80;
		res[5] = drr;
		res[6] = ct;
		return res;
	}

	public static double lf(double[] energia1, double[] energia2, double fs) {
		int t5 = (int) Math.round(0.005 * fs);
		int t80 = (int) Math.round(0.08 * fs);
		double lfc = (ArrayUtils.sum(energia2, t5, t80 < energia2.length ? t80
				: energia2.length) / ArrayUtils.sum(energia1, 0, t80)) * 100;
		return lfc;
	}

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// % reverberacao.m
	// %
	// %Esta funcao calcula o tempo de reverberacao de uma sala
	// %em funcao da sua resposta impulsiva.
	// %
	// % [EDT,T20,T30,T40] = reverberacao(IR^2,Fs,flag)
	// %
	// %Tem como entrada a a resposta impulsiva ao quadrado e a taxa de
	// amostragem
	// %Esta curva de Schroeder deve ter sido obtida apos descartar o atraso
	// %da resposta impulsiva.
	// %Tem como saida os tempos de reverberacao T20, T30 e T40, alem do EDT
	// %
	// %A variavel de flag define se a funcao ira plotar ou nao as assintotas
	// %calculadas. Caso nao seja definido, a funcao nao ira gerar estes
	// graficos.
	//
	public static double[] reverberacao(PrintStream out, double ir[], double fs) {
		double[] E = ArrayUtils.scale(1.0/(ArrayUtils.sum(ir)), ArrayUtils.cumsumLLL(ArrayUtils.reverse(ir)));
		ArrayUtils.reverseLLL(E);

		int neg = -1;
		for (int i = 0; i < E.length; i++) {
			if (E[i] < 0) {
				neg = i;
				break;
			}
		}
		if (neg >= 0) {
			E = ArrayUtils.subArray(E, 0, neg);
		}
		E = ArrayUtils.multLLL(MathUtils.log10LLL(E), 10);

		double x[] = new double[E.length];
		for (int i = 0; i < x.length; i++) {
			x[i] = (double) i / AcmusApplication.SAMPLE_RATE;
		}

		// // % Calcula os tempos de reverberacao da resposta impulsiva (T20 e
		// T30)
		// // a partir
		// // % curva de Schroeder (em dB) fornecida no argumento de entrada.
		// //
		out.println(x.length);
		for (int i = 0; i < x.length; i++) {
			out.print(x[i] + " ");
		}
		out.println();
		for (int i = 0; i < E.length; i++) {
			out.print(E[i] + " ");
		}
		out.println();

		// %Calcula o Early Decay Time (EDT) do sinal. A curva de Schroeder
		// fornecida deve
		// %ter sido obtida a partir de uma resposta impulsiva sem ruido de
		// inicio.

		int t10 = ArrayUtils.firstLessThanIndex(E, -15);
		double[] ab = Algorithms.intlinear(ArrayUtils.subArray(x, 0, t10 + 1), ArrayUtils
				.subArray(E, 0, t10 + 1));
		double EDT = ((double) -60) / (ab[1]);

		out.println(EDT);
		out.println(ab[0] + " " + ab[1] + " " + t10);

		//
		// begin = min(find(E < -5));
		// t25 = min(find(E < -25)); %Se a curva nao for monotonica, o primeiro
		// ponto onde a curva
		// t35 = min(find(E < -35)); %atinge -25 e -35dB limita a regiao de
		// iteracao.
		// t45 = min(find(E < -45));

		int begin = ArrayUtils.firstLessThanIndex(E, -5);
		int t25 = ArrayUtils.firstLessThanIndex(E, -25);
		int t35 = ArrayUtils.firstLessThanIndex(E, -35);
		int t45 = ArrayUtils.firstLessThanIndex(E, -45);

		//
		// %Usando 20dB
		// if ~isempty(t25)
		// [A20,B20] = intlinear(x(begin:t25),E(begin:t25));
		// T20 = (-60)/(B20);
		// else
		// T20=NaN; %Caso a resposta impulsiva nao apresentefaixa dinamica
		// suficiente
		// end

		double T20 = Double.NaN;
		if (t25 >= 0) {
			ab = Algorithms.intlinear(ArrayUtils.subArray(x, begin, t25 + 1), ArrayUtils
					.subArray(E, begin, t25 + 1));
			T20 = ((double) -60) / (ab[1]);
			out.println(ab[0] + " " + ab[1] + " " + T20);
		} else
			out.println("?");

		//
		// %Usando 30dB
		// if ~isempty(t35)
		// [A30,B30] = intlinear(x(begin:t35),E(begin:t35));
		// T30 = (-60)/(B30);
		// else
		// T30=NaN; %Caso a resposta impulsiva nao apresentefaixa dinamica
		// suficiente
		// end

		double T30 = Double.NaN;
		if (t35 >= 0) {
			ab = Algorithms.intlinear(ArrayUtils.subArray(x, begin, t35 + 1), ArrayUtils
					.subArray(E, begin, t35 + 1));
			T30 = ((double) -60) / (ab[1]);
			out.println(ab[0] + " " + ab[1] + " " + T30);
		} else
			out.println("?");

		// else
		// T40=NaN; %Caso a resposta impulsiva nao apresentefaixa dinamica
		// suficiente
		// end

		double T40 = Double.NaN;
		if (t45 >= 0) {
			ab = Algorithms.intlinear(ArrayUtils.subArray(x, begin, t45 + 1), ArrayUtils
					.subArray(E, begin, t45 + 1));
			T40 = ((double) -60) / (ab[1]);
			out.println(ab[0] + " " + ab[1] + " " + T40);
		} else
			out.println("?");

		double res[] = new double[4];
		res[0] = EDT;
		res[1] = T20;
		res[2] = T30;
		res[3] = T40;
		return res;
	}

	public static double bassRatio(Parameter t20) {
		return (t20.val[1] + t20.val[2]) / (t20.val[3] + t20.val[4]);
	}

	public static double trebleRatio(Parameter t20) {
		return (t20.val[5] + t20.val[6]) / (t20.val[3] + t20.val[4]);
	}

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// % tabela.m
	// %
	// %Esta funcao apenas gera uma tabela em .txt com os valores obtidos para
	// os
	// %parametros acusticos.
	// %
	// %tabela(s,n)
	//
	public final static void tabela(PrintStream out, Map<String, Parameter> s,
			List<String> paramsOrder, int n, double br, double tr, int itdg) {

		DecimalFormat format;
		NumberFormat f = DecimalFormat.getInstance(new Locale("en"));
		if (f instanceof DecimalFormat) {
			format = (DecimalFormat) DecimalFormat
					.getInstance(new Locale("en"));
			format.applyPattern("#.###");
		} else {
			format = new DecimalFormat("#.###");
		}
		int k = 9;

		out.print(PrintUtils.formatString("freq [Hz]", k));
		for (int i = 0; i < n - 3; i++) {
			out.print(PrintUtils.formatString(CHANNEL_NAMES[i], k) + " ");
		}
		out.print(PrintUtils.formatString("A", k) + " ");
		out.print(PrintUtils.formatString("C", k) + " ");
		out.print(PrintUtils.formatString("Linear", k) + " ");
		out.println();

		for (String p : paramsOrder) {
			out.print(PrintUtils.formatString(p + " [" + s.get(p).unit + "]", k) + " ");
			for (int j = 0; j < n; j++) {
				double val = s.get(p).val[j];
				if (val == Double.NaN)
					out.print(PrintUtils.formatString("NaN", k));
				else
					out.print(PrintUtils.formatString(format.format(val), k));
				out.print(" ");
			}
			out.println();
		}
		out.println();
		out.println();

		out.println("   BR:   " + format.format(br));
		out.println("   TR:   " + format.format(tr));
		out.println(" ITDG:   " + (itdg));

	}

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// % Lundeby.m
	// %
	// %Esta funcao realiza as iteracoes do metodo de Lundeby.
	// %Aplicar metodo de Lundeby para determinar ponto de truncamento.
	// %
	// %[ponto,C]=lundeby(IR,Fs,flag)
	// %
	// %Tem como entrada a resposta impulsiva e a taxa de amostragem
	// %Tem como saida o ponto de cruzamento da assintota de decaimento o nivel
	// de
	// %ruido e se desejado, a constante C usada para compensar o truncamento no
	// %calculo da curva de Schroeder.
	// %Se nao for fornecida variavel de saida, a funcao imprime um grafico dos
	// %niveis encontrados.
	//
	public static double[] lundeby(double[] ir, double fs) {
		double ponto, C;

		double energia_impulso[] = new double[ir.length];
		for (int i = 0; i < energia_impulso.length; i++) {
			energia_impulso[i] = ir[i] * ir[i];
		}

		// %Calcula o nivel de ruido dos ultimos 10% do sinal, onde se assume
		// que o
		// %ruido ja domine o sinal
		double max = ArrayUtils.max(energia_impulso);
		double rms_dB = 10 * MathUtils.log10(ArrayUtils.mean(energia_impulso, (int) Math
				.round(0.9 * energia_impulso.length) - 1,
				energia_impulso.length)
				/ max);

		System.out.println("max: " + max + "  rms:" + rms_dB);
		// %divide em intervalos e obtem media
		int t = (int) Math.floor(energia_impulso.length / fs / 0.01);
		int v = (int) Math.floor(energia_impulso.length / t);

		System.out.println("t: " + t + "  v:" + v);

		double media[] = new double[t];
		double eixo_tempo[] = new double[t];
		for (int n = 0; n < t; n++) {
			media[n] = ArrayUtils.mean(energia_impulso, n * v, (n + 1) * v);
			eixo_tempo[n] = Math.ceil((double) v / 2) + (n * v);
		}

		PrintUtils.print(media);
		PrintUtils.print(eixo_tempo);

		double mediadB[] = ArrayUtils
				.multLLL(MathUtils.log10LLL(ArrayUtils.scale(1.0/(max), media)), 10);

		// %obtem a regressao linear o intervalo de 0dB e a media mais proxima
		// de
		// %rms+10dB
		int r = -1;
		for (int i = 0; i < mediadB.length; i++) {
			if (mediadB[i] > rms_dB + 10)
				r = i;
		}

		System.out.println("r: " + r);
		for (int i = 0; i <= r; i++) {
			if (mediadB[i] < rms_dB + 10) {
				r = i;
				break;
			}
		}
		System.out.println("r: " + r);
		if (r < 9) {
			r = 9;
		}

		System.out.println("r: " + r);


		double[] ab = Algorithms.intlinear(ArrayUtils.subArray(eixo_tempo, 0, r + 1), ArrayUtils
				.subArray(mediadB, 0, r + 1));
		double cruzamento = (rms_dB - ab[0]) / ab[1];
		System.out.println("cruzamento: " + cruzamento + " " + rms_dB);

		if (rms_dB > -20) {

			// %Relacao sinal ruido insuficiente
			ponto = energia_impulso.length;
			C = 0;

		} else {
			//
			// %%%%%%%%%%%%%%%%%%%%%%%%INICIA A PARTE ITERATIVA DO
			// PROCESSO%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
			double erro = 1;
			int INTMAX = 50;
			int vezes = 1;

			while (erro > 0.0001 & vezes <= INTMAX) {
				//
				// %Calcula novos intervalos de tempo para media, com
				// aproximadamente p
				// %passos por 10dB
				// clear r t v n media eixo_tempo;
				//
				// p = 5; %numero de passos por decada
				int p = 5;
				// delta = abs(10/B); %numero de amostras para o a linha de
				// tendencia
				// % decair 10dB
				double delta = Math.abs(10 / ab[1]);
				// v = floor(delta/p); %intervalo para obtencao de media
				v = (int) Math.floor(delta / p);
				// t =
				// floor(length(energia_impulso(1:round(cruzamento-delta)))/v);
				t = (int) Math.floor((Math.round(cruzamento - delta)) / v);

				System.out.println("t: " + t + "  v:" + v + " delta: " + delta);

				// if t < 2 %numero de intervalos para obtencao da nova media no
				// % intervalo
				// t=2; %que vai do inicio ate 10dB antes do ponto de
				// cruzamento.
				// elseif isempty(t)
				// t=2;
				// end
				if (t < 2) {
					t = 1;
				}

				if (t != media.length) {
					media = new double[t];
					eixo_tempo = new double[t];
				}
				System.out.println("eim " + energia_impulso.length + " " + t);
				for (int n = 0; n < t; n++) {
					System.out.println("  " + n);
					media[n] = ArrayUtils.mean(energia_impulso, n * v, (n + 1) * v);
					eixo_tempo[n] = Math.ceil((double) v / 2) + (n * v);
				}

				PrintUtils.print(media);
				PrintUtils.print(eixo_tempo);
				mediadB = ArrayUtils.multLLL(MathUtils.log10LLL(ArrayUtils.scale(1.0/(ArrayUtils
				.max(energia_impulso)), media)), 10);

				ab = Algorithms.intlinear(eixo_tempo, mediadB);

				int noiseStart = (int) Math.round(cruzamento + delta) - 1;
				if (energia_impulso.length - noiseStart < Math
						.round(0.1 * energia_impulso.length)) {
					noiseStart = (int) Math.round(0.9 * energia_impulso.length);
				}
				double noise[] = ArrayUtils.subArray(energia_impulso, noiseStart);
				System.out.println("ns: " + noiseStart + " " + noise.length);
				rms_dB = 10 * MathUtils.log10(ArrayUtils.mean(noise)
						/ ArrayUtils.max(energia_impulso));

				System.out.println("rms: " + rms_dB);
				PrintUtils.print(ab);
				//
				// %novo ponto de cruzamento.
				// erro = abs(cruzamento - (rms_dB-A)/B)/cruzamento;
				erro = Math.abs(cruzamento - (rms_dB - ab[0]) / ab[1])
						/ cruzamento;
				// cruzamento = round((rms_dB-A)/B);
				cruzamento = Math.round((rms_dB - ab[0]) / ab[1]);
				// vezes = vezes + 1;
				vezes++;
				// end
				System.out.println(vezes + " erro: " + erro + " cruzamento:"
						+ cruzamento);
				System.out.println("---------------------------");

			}
			// end
			// } <--- bug no prot�tipo?

			//
			// if nargout == 1
			// if cruzamento > length(energia_impulso) %caso o sinal nao atinja
			// o
			// patamar de ruido
			// ponto = length(energia_impulso); %nas amostras fornecidas,
			// considera-se o
			// ponto
			// else %de cruzamento a ultima amosta, o que equivale
			// ponto = cruzamento; %a nao truncar o sinal.
			// end
			// elseif nargout == 2
			// if cruzamento > length(energia_impulso)
			// ponto = length(energia_impulso);
			// else
			// ponto = cruzamento;
			// end
			// C=max(energia_impulso)*10^(A/10)*exp(B/10/log10(exp(1))*cruzamento)/(-B/10/log10(exp(1)));
			// end
			ponto = (cruzamento > energia_impulso.length) ? energia_impulso.length
					: cruzamento;
			C = ArrayUtils.max(energia_impulso)
					* Math.pow(10, (ab[0] / 10))
					* Math.exp(ab[1] / 10 / MathUtils.log10(Math.exp(1))
							* cruzamento)
					/ (-ab[1] / 10 / MathUtils.log10(Math.exp(1)));

		}// <--- correcao do bug???

		System.out.println(ponto + " C:" + C);
		double[] res = new double[2];
		res[0] = ponto;
		res[1] = C;
		return res;
	}

	// // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// // % ldbparam.m
	// // %
	// // %Esta funcao calcula os diversos parametros acusticos de uma sala. O
	// // %tratamento do sinal e feito pelo metodo de Lundeby.
	// // %
	// // %[s]=parametros(IR,fs,flag)
	// // %
	// // %Tem como entrada a resposta impulsiva e a taxa de amostragem.
	// // %Tem como saida uma arquivo de texto com o valor dos parametros para
	// as
	// // %diversas bandas de frequencia. Se desejado, fornece uma matrix com
	// estes
	// // %valores, onde a primeira linha sao as frequencias centrais, ao inves
	// do
	// // %arquivo de texto.
	// // %A variavel flag = 1 indica se deseja ver os graficos ca curva de
	// Schroeder
	// // %e metodo de Lundeby
	// //
	// // function [saida]=ldbparam(IR,fs,flag)
	public static void lundebyParam(double[] ir, double irLf[], double fs,
			int directSound, int firstReflection, PrintStream outTable,
			String graphFolder) {
		double[][] banda = filtros(ir, fs, null);
		int t = banda.length;

		double[][] bandaLf = null;
		if (irLf != null)
			bandaLf = filtros(irLf, fs, null);

		PrintStream outGraphs[] = channelFiles(graphFolder);

		Map<String, Parameter> params = new HashMap<String, Parameter>();
		for (int i = 0; i < energ.length; i++) {
			params.put(energ[i], new Parameter(energ[i], energUnit[i]));
		}
		if (irLf != null) {
			params.put("LF", new Parameter("LF", ""));
		}
		for (int i = 0; i < reverb.length; i++) {
			params.put(reverb[i], new Parameter(reverb[i], "s"));
		}

		for (int n = 0; n < t; n++) {
			int comeco = inicio(banda[n]);
			int fim = comeco
					+ (int) lundeby(ArrayUtils.subArray(banda[n], comeco), fs)[0];
			double aux[] = ArrayUtils.sqrLLL(ArrayUtils.subArray(banda[n], comeco, fim));
			double en[] = energeticos(aux, fs, directSound, firstReflection);

			for (int i = 0; i < energ.length - 1; i++) {
				params.get(energ[i]).val[n] = en[i];
			}

			params.get("CT").val[n] = en[en.length - 1] * 1000;

			if (irLf != null) {
				int comecoLf = inicio(bandaLf[n]);
				int fimLf = comecoLf
						+ (int) lundeby(ArrayUtils.subArray(bandaLf[n], comecoLf), fs)[0];
				System.out.println(comecoLf + " " + fimLf);
				double auxLf[] = ArrayUtils.sqrLLL(ArrayUtils.subArray(bandaLf[n],
						comecoLf, fimLf));

				params.get("LF").val[n] = lf(aux, auxLf, fs);
			}

			double rev[] = reverberacao(outGraphs[n], aux, fs);
			for (int i = 0; i < rev.length; i++) {
				params.get(reverb[i]).val[n] = rev[i];
			}
		}

		List<String> pOrder = new ArrayList<String>();
		for (String pName : energ)
			pOrder.add(pName);
		if (irLf != null) {
			pOrder.add("LF");
		}
		for (String pName : reverb)
			pOrder.add(pName);

		double br = bassRatio(params.get("T20"));
		double tr = trebleRatio(params.get("T20"));

		tabela(outTable, params, pOrder, banda.length, br, tr, (int) Math
				.round((firstReflection - directSound) / fs * 1000));

	}

	// // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// // % hrtparam.m
	// // %
	// // %Esta funcao calcula os diversos parametros acusticos de uma sala. O
	// // %tratamento do sinal e feito pelo metodo de Hirata.
	// // %
	// // %[s]=parametros(IR1,IR2,fs,flag)
	// // %
	// // %Tem como entrada as duas resposta impulsiva e a taxa de amostragem.
	// As
	// // %duas respostas devem ser obtidas sob a mesma configuracao do sistema
	// de
	// // %medida.
	// // %Tem como saida uma arquivo de texto com o valor dos parametros para
	// as
	// // %diversas bandas de frequencia. Se desejado, fornece uma matrix com
	// estes
	// // %valores, onde a primeira linha sao as frequencias centrais, ao inves
	// do
	// // %arquivo de texto.
	// // %A variavel flag = 1 indica se deseja ver os graficos ca curva de
	// Schroeder
	// //
	// //
	// // function [saida]=hrtparam(IR1,IR2,fs,flag)
	public static void hirataParam(double[] ir1, double ir2[], double fs,
			int directSound, int firstReflection, PrintStream outTable,
			String graphFolder) {
		if (ir1.length < ir2.length) {
			ir2 = ArrayUtils.subArray(ir2, 0, ir1.length);
		} else if (ir1.length > ir2.length) {
			ir1 = ArrayUtils.subArray(ir1, 0, ir2.length);
		}

		double[][] banda1 = filtros(ir1, fs, null);
		double[][] banda2 = filtros(ir2, fs, null);

		PrintStream outGraphs[] = channelFiles(graphFolder);
		Map<String, Parameter> params = new HashMap<String, Parameter>();
		for (int i = 0; i < energ.length; i++) {
			params.put(energ[i], new Parameter(energ[i], energUnit[i]));
		}
		for (int i = 0; i < reverb.length; i++) {
			params.put(reverb[i], new Parameter(reverb[i], "s"));
		}

		for (int n = 0; n < banda1.length; n++) {
			double aux[] = ArrayUtils.multLLL(banda1[n], banda2[n]);

			int comeco = inicio(banda1[n]);

			aux = ArrayUtils.subArray(aux, comeco);

			double en[] = energeticos(aux, fs, directSound, firstReflection);

			for (int i = 0; i < energ.length - 1; i++) {
				params.get(energ[i]).val[n] = en[i];
			}

			params.get("CT").val[n] = en[en.length - 1] * 1000;

			double rev[] = reverberacao(outGraphs[n], aux, fs);
			for (int i = 0; i < rev.length; i++) {
				params.get(reverb[i]).val[n] = rev[i];
			}

		}

		List<String> pOrder = new ArrayList<String>();
		for (String pName : energ)
			pOrder.add(pName);
		for (String pName : reverb)
			pOrder.add(pName);

		double br = bassRatio(params.get("T20"));
		double tr = trebleRatio(params.get("T20"));

		tabela(outTable, params, pOrder, banda1.length, br, tr, (int) Math
				.round((firstReflection - directSound) / fs * 1000));
	}

	public static PrintStream[] channelFiles(String graphFolder) {
		PrintStream res[] = new PrintStream[11];
		String separator = System.getProperty("file.separator", "/");
		for (int i = 0; i < res.length; i++) {
			try {
				res[i] = new PrintStream(new FileOutputStream(graphFolder
						+ separator + CHANNEL_NAMES[i] + ".txt"));
			} catch (FileNotFoundException e) {
				// eh?
				e.printStackTrace();
			}
		}
		return res;
	}

	// // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// // %filtros.m
	// // %
	// // % [bandas] = filtros(sinal,fs)
	// // %
	// // %Banco de Filtros 1/8 e de compensacao A e C. A ultima linha apresenta
	// a
	// // %resposta impulsiva sem ser filtrada.
	// // %Fs = frequencia de amostragem
	// // %Os filtros de compensacao foram extraidos do tool box Octave.
	// // %Realiza a filtragem em bandas de oitava, como recomendado pela norma
	// // %IEC 61620.
	// //
	// // function [bandas] = filtros(sinal,fs)
	public static double[][] filtros(double[] signal, double fs,
			IProgressMonitor monitor) {
		monitor = SWTUtils.monitorFor(monitor);

		monitor.beginTask("filtering", 100);

		double[][] res = new double[11][];

		double fc[] = { 62.5, 125, 250, 500, 1000, 2000, 4000, 8000 };

		for (int i = 0; i < fc.length; i++) {
			Filter f = FilterBank.get1d8(fc[i], fs);
			System.out.println(f);
			res[i] = Filter.filtfilt(f.b, f.a, signal);
			monitor.worked(10);
		}

		//    
		// // %-------------Compencao A-------
		// // f1 = 20.598997;
		// // f2 = 107.65265;
		// // f3 = 737.86223;
		// // f4 = 12194.217;
		// // A1000 = 1.9997;
		// // NUMs = [ (2*pi*f4)^2*(10^(A1000/20)) 0 0 0 0 ];
		// // DENs = conv([1 +4*pi*f4 (2*pi*f4)^2],[1 +4*pi*f1 (2*pi*f1)^2]);
		// // DENs = conv(conv(DENs,[1 2*pi*f3]),[1 2*pi*f2]);
		// // [B,A] = bilinear(NUMs,DENs,fs);
		// // bandas(:,9) = filter(B,A,sinal);

		Filter f = FilterBank.getComp("a", fs);
		res[8] = Filter.filter(f.b, f.a, signal);
		monitor.worked(10);

		//
		// // %-------------Compencao C-------
		// // f1 = 20.598997;
		// // f4 = 12194.217;
		// // C1000 = 0.0619;
		// // pi = 3.14159265358979;
		// // NUMs = [ (2*pi*f4)^2*(10^(C1000/20)) 0 0 ];
		// // DENs = conv([1 +4*pi*f4 (2*pi*f4)^2],[1 +4*pi*f1 (2*pi*f1)^2]);
		// // [B,A] = bilinear(NUMs,DENs,fs);
		// // bandas(:,10) = filter(B,A,sinal);

		f = FilterBank.getComp("c", fs);
		res[9] = Filter.filter(f.b, f.a, signal);
		monitor.worked(10);

		res[10] = signal;

		monitor.done();
		return res;
	}

}

class Parameter {
	public String name;
	public String unit;
	public double val[];

	public Parameter(String name, String unit) {
		this.name = name;
		this.unit = unit;
		this.val = new double[11];
	}
}