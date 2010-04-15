package acmus.util;

import acmus.dsp.Complex;

public class MathUtils {

	public final static double log10(double x) {
		return Math.log10(Math.abs(x));
	}

	public final static double log2(double x) {
		return Math.log(x) / MathUtils._log2;
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

	public static final double _log2 = Math.log(2);

	/**
	 * Complex Division for (aRe + iaIm) / (bRe + i bIm)
	 * stores result in a 
	 */
	public static final void complexDivision(double aRe[], double aIm[], double bRe[],
			double bIm[]) {
		Complex c = new Complex(0, 0);
	
		for (int i = 0; i < aRe.length; i++) {
			/*
			c.real(aRe[i]);
			c.imag(aIm[i]);
			c.div(bRe[i], bIm[i]);
			aRe[i] = c.real();
			aIm[i] = c.imag();
			*/
			c.setComplex(aRe[i], aIm[i]);
			c = c.divides(new Complex(bRe[i], bIm[i]));
			aRe[i] = c.re();
			aIm[i] = c.im();
		}
	}

}
