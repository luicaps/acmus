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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;

import acmus.MeasurementProject;
import acmus.audio.AudioPlayer;

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

	private static void splitChannels(double[] left, double[] right,
			int[] stereo) {

		for (int i = 0; i < left.length; i++) {
			left[i] = stereo[i * 2];
			right[i] = stereo[i * 2 + 1];
		}
	}

	private static boolean swap(IFile recFile) {
		String swapChannels = MeasurementProject.getProperty(recFile
				.getProject(), "SWAP_RECORDING_CHANNELS", "no");
		return !swapChannels.equalsIgnoreCase("no")
				&& !swapChannels.equalsIgnoreCase("false");
	}

	public static double[] calculateIr(IFile recFile, IFile irFile,
			IFile signalFile, IProgressMonitor monitor) {

		double ir[] = null;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(recFile
					.getContents());
			int data[] = AudioPlayer.readData(ais);
			double[] left = new double[data.length / 2];
			double[] right = new double[data.length / 2];
			if (swap(recFile)) {
				System.out.println("Swap channels...");
				splitChannels(right, left, data);
			}
			else
				splitChannels(left, right, data);
			
			left = Util.scaleToUnit(left, Util.maxAbs(left));
			right = Util.scaleToUnit(right, Util.maxAbs(right));
			Properties props = new Properties();
			props.load(signalFile.getContents());
			String method = props.getProperty("Type", "");

			if (method.equals("sweep")) {
				Filter f = new Filter(props.getProperty("ButterB"), props
						.getProperty("ButterA"));
				// FIXME
				System.out.println("Sweep filter: " + f);

				ir = dechirp(left, right, f.b, f.a, (int) Math.pow(2, Math.ceil(Math
						.log(left.length * 2)
						/ Math.log(2))), monitor);

				double sr = Double.parseDouble(props.getProperty("SampleRate",
						"44100"));
				double irLen = Double.parseDouble(MeasurementProject
						.getProperty(recFile.getProject(), "IrLength", ""
								+ Util.DEFAULT_IR_LENGTH));
				int samples = (int) (sr * irLen);
				// file
				if (samples > ir.length)
					samples = ir.length;
				ir = Util.subArray(ir, 0, samples);

			} else if (method.equals("mls")) {
				int[] row = Util.parseIntArray(props.getProperty("Row"));
				int[] col = Util.parseIntArray(props.getProperty("Col"));
				int reps = Integer.parseInt(props.getProperty("Repetitions"));
				ir = demls(left, right, row, col, reps);
			} else {
				throw new IllegalArgumentException(
						"Method must be either mls or sweep");
			}
			// FIXME This is one of the places to set 16 or 32 bits
			// if we want to change the IR resolution
			ir = Util.scaleToMax(ir, (double) Util.getLimit(32));
			Util.wavWrite(ir, 1, 32, irFile.getLocation().toOSString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ir;
	}

	public static void calculateParameters(double[] ir, double[] irLf,
			String method, IFile paramsFile, IFolder schroederFolder) {
		try {
			ByteArrayOutputStream baosTable = new ByteArrayOutputStream();
			PrintStream outTable = new PrintStream(baosTable);
			if (!schroederFolder.exists()) {
				schroederFolder.create(true, true, null);
			}
			String graphFolder = schroederFolder.getLocation().toOSString();
			Parameters p = new Parameters(ir, irLf, 44100);
			p.chuParam(0, 0, outTable, graphFolder, null);
			byte[] buf = baosTable.toByteArray();
			if (!paramsFile.exists()) {
				paramsFile.create(new ByteArrayInputStream(buf), true, null);
			} else {
				paramsFile.setContents(new ByteArrayInputStream(buf), true,
						true, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
