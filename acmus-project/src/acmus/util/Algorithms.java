package acmus.util;

import java.io.File;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import acmus.dsp.NewSignal;

import org.eclipse.core.runtime.IProgressMonitor;

public class Algorithms {

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// % intlinear.m
	// %
	// %[A,B] = intlinear(x,y)
	// %
	// % Realiza a regressao linear (minimos quadrados) dos dados fornecidos
	// (x,y)
	// % devolvendo os valores de A (res[0]) e B (res[1]), sendo (y = A + B*x).
	//
	public final static double[] intlinear(double[] x, double[] y) {
		double[] res = new double[2];
	
		double mx = ArrayUtils.mean(x);
		double my = ArrayUtils.mean(y);
	
		double mxy = ArrayUtils.mean(ArrayUtils.mult(y, x));
		double mx2 = ArrayUtils.mean(ArrayUtils.sqrLLL(x));
		
		res[0] = (mx2 * my - mx * mxy) / (mx2 - (mx * mx));
		res[1] = (mxy - (mx * my)) / (mx2 - (mx * mx));
		return res;
	}

	public static final double[] conv(double a[], double b[], double output[],
			float SR1, float SR2, IProgressMonitor monitor) {  
		monitor = SWTUtils.monitorFor(monitor);
		monitor.beginTask("Convolving...", a.length + b.length - 1);
		
		
		// New convolution
		
		NewSignal f = new NewSignal(a);
		NewSignal g = new NewSignal(b);
		NewSignal out = new NewSignal(output);
		
		if (SR1 != SR2) {
			if ((SR1 < SR2) && ((int)SR2 % (int)SR1 == 0)) {
				f = f.upsample((int)(SR2 / SR1));
			}
			else if ((SR1 > SR2) && ((int)SR1 % (int)SR2 == 0)){
				g = g.upsample((int)(SR1 / SR2));
			}
			else {
				System.out.println("Ainda não sei resolver este caso!");
			}
			// FILTRO PASSA BAIXA com freq. PI/Factor
		}

		out = f.convolve(g, monitor);
		output = new double[out.size()];
		for (int i = 0; i < out.size(); i++) {
			output[i] = out.get(i).re();
			monitor.worked(1);
		}
		
		// Old convolution
		/*
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
		*/
		
		monitor.done();
		return output;
	}

	public static int[] doubleToInt(double[] data, boolean dither) {
		Random random = new Random();
		int[] samples = new int[data.length];
		if (dither) {
			for (int i = 0; i < data.length; ++i) {
				// This is a simple dithering with Triangular
				// Probability Density Function; browse the web
				// for "dither", "noise shaping" and TPDF
				// FIXME this may overload...
				samples[i] = (int) Math.round(data[i] + random.nextDouble()
						- random.nextDouble());
			}
		} else {
			for (int i = 0; i < data.length; ++i) {
				samples[i] = (int) Math.round(data[i]);
			}
		}
		return samples;
	}

	public static final void convolve(String input1, String input2,
			String output, IProgressMonitor monitor) {
	
		int a[] = WaveUtils.wavRead(input1);
		int b[] = WaveUtils.wavRead(input2);
	
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
	
			int aa[][] = WaveUtils.splitAudioStream(format1.getChannels(), a);
			int bb[][] = WaveUtils.splitAudioStream(format2.getChannels(), b);
			int max1 = WaveUtils.getLimit(format1.getSampleSizeInBits());
			int max2 = WaveUtils.getLimit(format2.getSampleSizeInBits());
			float SR1 = format1.getSampleRate();
			float SR2 = format2.getSampleRate();
	
			monitor.beginTask("Convolving", 10 * aa.length * bb.length);
	
			int k = 0;			
			for (int i = 0; i < aa.length; i++) {
				for (int j = 0; j < bb.length; j++) {
					//System.out.println("Conv (" + i + ", " + j + ")");
					double x[] = ArrayUtils.scaleToUnit(aa[i], max1);
					double y[] = ArrayUtils.scaleToUnit(bb[j], max2);
					conv[k] = new double[x.length + y.length - 1];
					monitor.subTask("ch " + (i + 1) + " x " + " ch " + (j + 1));
					IProgressMonitor subMonitor = SWTUtils.subMonitorFor(monitor,
							10);			
					conv[k] = conv(x, y, conv[k], SR1, SR2, subMonitor);
				}
			}
	
			//System.out.println("Writing start! - " + output);
			monitor.beginTask("Writing output", 2);
			double[] scaled = ArrayUtils.scaleToMax(WaveUtils.joinAudioStream(conv),
					(double) WaveUtils.getLimit(16));
			
			if (SR1 > SR2) {
				WaveUtils.wavWrite(scaled, conv.length, SR1, output);
			}
			else {
				WaveUtils.wavWrite(scaled, conv.length, SR2, output);
			}
			monitor.worked(2);
	
			monitor.done();
			//System.out.println("Writing done!");
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	public static final double DEFAULT_IR_LENGTH = 5.0;
	public static final double DEFAULT_REC_EXTRA = 2.0; // == IR_LENGTH?

}
