/*
 *  Ir.java
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

import org.eclipse.core.runtime.IProgressMonitor;

public class Ir {

	public static final double[] dechirp(double[] rec, double[] ref,
			double[] B, double[] A, int n, IProgressMonitor monitor) {
		monitor = Util.monitorFor(monitor);
		monitor.beginTask("dechirp", 100);
		// function ir = dechirp(gravacao,B,A,n)
		//
		// ir = real(ifft(fft(gravacao(:,1),n)./fft(gravacao(:,2),n)));
		double recRe[] = new double[n];
		double recIm[] = new double[n];
		double refRe[] = new double[n];
		double refIm[] = new double[n];
		monitor.subTask("initializing");
		for (int i = 0; i < rec.length && i < n; i++) {
			recRe[i] = rec[i];
			recIm[i] = 0;
			refRe[i] = ref[i];
			refIm[i] = 0;
		}
		monitor.worked(5);
		for (int i = rec.length; i < n; i++) {
			recRe[i] = 0;
			recIm[i] = 0;
			refRe[i] = 0;
			refIm[i] = 0;
		}
		monitor.worked(5);
		FFT1d fft = new FFT1d(n);
		monitor.subTask("FFT recording");
		fft.fft(recRe, recIm);
		monitor.worked(20);
		monitor.subTask("FFT reference");
		fft.fft(refRe, refIm);
		monitor.worked(20);
		monitor.subTask("Division");
		Util.div(recRe, recIm, refRe, refIm);
		monitor.worked(5);
		monitor.subTask("IFFT");
		IFFT1d ifft = new IFFT1d(n);
		ifft.ifft(recRe, recIm);
		monitor.worked(20);

		monitor.subTask("Filter");
		double res[] = Filter.filter(B, A, recRe);
		monitor.worked(25);
		monitor.done();
		return res;
	}

	public static final double[] dechirp2(double[] rec, double[] ref,
			double[] B, double[] A, int n) {
		// function ir = dechirp(gravacao,B,A,n)
		//
		// ir = real(ifft(fft(gravacao(:,1),n)./fft(gravacao(:,2),n)));
		double recRe[] = new double[n];
		double recIm[] = new double[n];
		double refRe[] = new double[n];
		double refIm[] = new double[n];
		for (int i = 0; i < rec.length && i < n; i++) {
			recRe[i] = rec[i];
			recIm[i] = 0;
			refRe[i] = ref[i];
			refIm[i] = 0;
		}
		for (int i = rec.length; i < n; i++) {
			recRe[i] = 0;
			recIm[i] = 0;
			refRe[i] = 0;
			refIm[i] = 0;
		}

		Parameters.doFFT(recRe, recRe, recIm);
		Parameters.doFFT(refRe, refRe, refIm);
		Util.div(recRe, recIm, refRe, refIm);
		IFFT1d ifft = new IFFT1d(n);
		ifft.ifft(recRe, recIm);

		return Filter.filter(B, A, recRe);
	}

	// // %% demls.m
	// // %%
	// // %% ir = demls(sinal,row,col,reps)
	// // %%
	// // %% Realiza a deconvolucao da resposta de uma sala a um sinal MLS. E
	// // %% necessario realizar a media das diversas repeticoes do sinal,
	// excluindo
	// // %% a primeira delas. Se o sinal de excitacao fornecido for estereo,
	// // %% assume-se que o segundo canal seja o canal de refencia, e
	// determina-se o
	// // %% inicio da medicao a partir do mesmo. Caso o sinal seja mono, o
	// inicio
	// da
	// // %% medicao e obtido a partir da propria resposta da sala.
	// // %% As variaveis de entrada, alem do sinal, sao os vetores de
	// permutacao
	// // %% dados pela funcao "mlsXtap.m" e o numero de repeticoes do sinal de
	// // %% excitacao (minimo de duas).
	// //
	public static final double[] demls(double[] rec, double ref[], int[] row,
			int[] col, int reps) {
		// // function ir = demls(sinal,row,col,reps)
		// //
		// // if size(sinal,2) == 2
		// // inicio = min(find(abs(sinal(:,2)) > max(abs(sinal(:,2)))/10));
		// // elseif size(sinal,2) == 1
		// // inicio = min(find(abs(sinal) > max(abs(sinal))/10));
		// // end

		// inicio = min(
		// find( abs(sinal(:,2)) > max(abs(sinal(:,2)))/10 ) );
		int inicio = 0;
		double val = Util.maxAbs(ref) / 10;
		for (int i = 0; i < ref.length; i++) {
			if (ref[i] > val) {
				inicio = i;
				break;
			}
		}

		// //
		// // t = length(row);
		// // aux = sinal(inicio+t:end,1);
		// // media = zeros(t,1);

		int t = row.length;
		double aux[] = Util.subArray(rec, inicio + t);
		double media[] = new double[t];
		for (int i = 0; i < media.length; i++)
			media[i] = 0;

		// // for n = 1:(reps-1)
		// // media = media + aux(1:t);
		// // aux(1:t)=[];
		// // end
		for (int n = 0, begin = 0, end = t; n < reps - 1; n++, begin += t, end += t) {
			media = Util.sumLLL(media, aux, begin, end);
		}
		// // ir=media/(reps-1);
		media = Util.divLLL(media, reps - 1);
		// // ir=ir(:);
		// // ir=ir(row);
		// // ir=[0; ir];
		double[] ir = new double[row.length + 1];
		ir[0] = 0;
		System.out.println(row.length);
		for (int i = 1; i < row.length; i++) {
			ir[i] = media[row[i] - 1];
		}
		// // ir=fht(ir);
		Parameters.fht(ir);
		// // ir(1)=[];
		ir = Util.subArray(ir, 1);
		// // ir=ir(col)/t;
		double[] ir2 = new double[col.length];
		for (int i = 0; i < col.length; i++) {
			ir2[i] = ir[col[i] - 1];
		}
		return ir2;
	}

}
