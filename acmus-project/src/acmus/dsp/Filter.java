/*
 *  Filter.java
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
 * Filter.java
 * Created on 07/06/2005
 */
package acmus.dsp;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import Jama.Matrix;

/**
 * @author lku
 */
public class Filter {

	public static final DecimalFormat FORMAT;

	static {
		NumberFormat f = DecimalFormat.getInstance(new Locale("en"));
		if (f instanceof DecimalFormat) {
			FORMAT = (DecimalFormat) DecimalFormat
					.getInstance(new Locale("en"));
			FORMAT.applyPattern("#.########################E0");
		} else {
			System.err
					.println("Warning: class Filter: not instance of DecimalFormat");
			FORMAT = new DecimalFormat("#.########################E0");
		}
	}
	public double b[];
	public double a[];

	public Filter(double b[], double a[]) {
		this.b = b;
		this.a = a;
	}

	public Filter(String b, String a) {
		this.b = parseCoeficients(b);
		this.a = parseCoeficients(a);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++)
			sb.append(b[i] + " ");
		sb.append(" | ");
		for (int i = 0; i < a.length; i++)
			sb.append(a[i] + " ");
		return sb.toString();
	}

	public String bToString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++)
			sb.append(FORMAT.format(b[i]) + " ");
		return sb.toString();
	}

	public String aToString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < a.length; i++)
			sb.append(FORMAT.format(a[i]) + " ");
		return sb.toString();
	}

	public static double[] parseCoeficients(String str) {
		StringTokenizer st = new StringTokenizer(str);
		double res[] = new double[st.countTokens()];
		for (int i = 0; i < res.length; i++) {
			res[i] = Double.parseDouble(st.nextToken());
		}
		return res;
	}

	public static double[] filtfilt(double[] b, double[] a, double[] x) {
		// function y = filtfilt(b,a,x)
		// %FILTFILT Zero-phase forward and reverse digital filtering.
		// % Y = FILTFILT(B, A, X) filters the data in vector X with the filter
		// described
		// % by vectors A and B to create the filtered data Y. The filter is
		// described
		// % by the difference equation:
		// %
		// % y(n) = b(1)*x(n) + b(2)*x(n-1) + ... + b(nb+1)*x(n-nb)
		// % - a(2)*y(n-1) - ... - a(na+1)*y(n-na)
		// %
		// %
		// % After filtering in the forward direction, the filtered sequence is
		// then
		// % reversed and run back through the filter; Y is the time reverse of
		// the
		// % output of the second filtering operation. The result has precisely
		// zero
		// % phase distortion and magnitude modified by the square of the
		// filter's
		// % magnitude response. Care is taken to minimize startup and ending
		// % transients by matching initial conditions.
		// %
		// % The length of the input x must be more than three times
		// % the filter order, defined as max(length(b)-1,length(a)-1).
		// %
		// % Note that FILTFILT should not be used with differentiator and
		// Hilbert
		// FIR
		// % filters, since the operation of these filters depends heavily on
		// their
		// % phase response.
		// %
		// % See also FILTER.
		//
		// % References:
		// % [1] Sanjit K. Mitra, Digital Signal Processing, 2nd ed.,
		// McGraw-Hill,
		// 2001
		// % [2] Fredrik Gustafsson, Determining the initial states in
		// forward-backward
		// % filtering, IEEE Transactions on Signal Processing, pp. 988--992,
		// April
		// 1996,
		// % Volume 44, Issue 4
		//
		// % Author(s): L. Shure, 5-17-88
		// % revised by T. Krauss, 1-21-94
		// % Initial Conditions: Fredrik Gustafsson
		// % Copyright 1988-2002 The MathWorks, Inc.
		// % $Revision: 1.7 $ $Date: 2002/03/28 17:27:55 $
		//
		// error(nargchk(3,3,nargin))
		// if (isempty(b)|isempty(a)|isempty(x))
		// y = [];
		// return
		// end
		if (b.length == 0 || a.length == 0 || x.length == 0) {
			return new double[0];
		}

		// [m,n] = size(x);
		// if (n>1)&(m>1)
		// y = x;
		// for i=1:n % loop over columns
		// y(:,i) = filtfilt(b,a,x(:,i));
		// end
		// return
		// % error('Only works for vector input.')
		// end
		// if m==1
		// x = x(:); % convert row to column
		// end
		// len = size(x,1); % length of input

		int len = x.length;

		// b = b(:).';
		// a = a(:).';
		// nb = length(b);
		// na = length(a);
		// nfilt = max(nb,na);
		int nb = b.length;
		int na = a.length;
		int nfilt = na > nb ? na : nb;

		//
		// nfact = 3*(nfilt-1); % length of edge transients
		int nfact = 3 * (nfilt - 1);

		//
		// if (len<=nfact), % input data too short!
		// error('Data must have length more than 3 times filter order.');
		// end
		if (len <= nfact) {
			throw new Error(
					"Data must have length more than 3 times filter order. "
							+ len + " " + nfact);
		}
		//
		// % set up filter's initial conditions to remove dc offset problems at
		// the
		// % beginning and end of the sequence
		// if nb < nfilt, b(nfilt)=0; end % zero-pad if necessary
		// if na < nfilt, a(nfilt)=0; end

		if (nb < nfilt) {
			double[] btmp = b;
			b = new double[nfilt];
			for (int i = 0; i < btmp.length; i++)
				b[i] = btmp[i];
			for (int i = btmp.length; i < nfilt; i++)
				b[i] = 0;
		} else if (na < nfilt) {
			double[] atmp = a;
			a = new double[nfilt];
			for (int i = 0; i < atmp.length; i++)
				a[i] = atmp[i];
			for (int i = atmp.length; i < nfilt; i++)
				a[i] = 0;
		}

		// % use sparse matrix to solve system of linear equations for initial
		// conditions
		// % zi are the steady-state states of the filter b(z)/a(z) in the
		// state-space
		// % implementation of the 'filter' command.
		// rows = [1:nfilt-1 2:nfilt-1 1:nfilt-2];
		// cols = [ones(1,nfilt-1) 2:nfilt-1 2:nfilt-1];
		// data = [1+a(2) a(3:nfilt) ones(1,nfilt-2) -ones(1,nfilt-2)];
		// sp = sparse(rows,cols,data);
		// zi = sp \ ( b(2:nfilt).' - a(2:nfilt).'*b(1) );
		// % non-sparse:
		// % zi = ( eye(nfilt-1) - [-a(2:nfilt).' [eye(nfilt-2);
		// zeros(1,nfilt-2)]]
		// ) \ ...
		// % ( b(2:nfilt).' - a(2:nfilt).'*b(1) );

		double atmp[][] = new double[nfilt - 1][nfilt - 1];
		double btmp[][] = new double[nfilt - 1][1];
		// zi = ( eye(nfilt-1) - [-a(2:nfilt).' [eye(nfilt-2);
		// zeros(1,nfilt-2)]] )
		for (int i = 0; i < nfilt - 1; i++) {
			atmp[i][0] = Util.eye(i, 0) + a[i + 1];
		}
		for (int i = 0; i < nfilt - 2; i++) {
			for (int j = 1; j < nfilt - 1; j++) {
				atmp[i][j] = Util.eye(i, j) - Util.eye(i, j - 1);
			}
		}
		for (int j = 1; j < nfilt - 1; j++) {
			atmp[nfilt - 2][j] = Util.eye(nfilt - 2, j);
		}

		// \ ( b(2:nfilt).' - a(2:nfilt).'*b(1) );
		for (int i = 0; i < nfilt - 1; i++) {
			btmp[i][0] = b[i + 1] - a[i + 1] * b[0];
		}

		Matrix A = new Matrix(atmp);
		Matrix B = new Matrix(btmp);
		Matrix X = A.solve(B);
		double zi[] = new double[X.getRowDimension()];
		for (int i = 0; i < zi.length; i++) {
			zi[i] = X.get(i, 0);
		}

		// % Extrapolate beginning and end of data sequence using a "reflection
		// % method". Slopes of original and extrapolated sequences match at
		// % the end points.
		// % This reduces end effects.
		// y = [2*x(1)-x((nfact+1):-1:2);x;2*x(len)-x((len-1):-1:len-nfact)];

		double y[] = new double[nfact + len + nfact];

		// 2*x(1)-x((nfact+1):-1:2)
		// x
		// 2*x(len)-x((len-1):-1:len-nfact)

		int k = 0;
		for (int i = nfact; i >= 1; i--) {
			y[k++] = 2 * x[0] - x[i];
		}
		for (int i = 0; i < x.length; i++) {
			y[k++] = x[i];
		}
		for (int i = len - 2; i >= len - nfact - 1; i--) {
			y[k++] = 2 * x[len - 1] - x[i];
		}

		// Matrix mmm = new Matrix(y);
		// mmm.print(3, 5);
		// for (int i = 0; i < y.length; i++) System.out.println (y[i]);
		// System.out.println();

		//
		// % filter, reverse data, filter again, and reverse data again
		// y = filter(b,a,y,[zi*y(1)]);

		double zitmp[] = new double[zi.length];
		for (int i = 0; i < zi.length; i++) {
			zitmp[i] = zi[i] * y[0];
		}
		y = filterZ(b, a, y, zitmp);

		// y = y(length(y):-1:1);
		Util.reverseLLL(y);

		// y = filter(b,a,y,[zi*y(1)]);

		for (int i = 0; i < zi.length; i++) {
			zitmp[i] = zi[i] * y[0];
		}
		y = filterZ(b, a, y, zitmp);

		// y = y(length(y):-1:1);
		Util.reverseLLL(y);

		// % remove extrapolated pieces of y
		// y([1:nfact len+nfact+(1:nfact)]) = [];

		double[] ytmp = y;
		y = new double[len];
		for (int i = 0; i < y.length; i++) {
			y[i] = ytmp[i + nfact];
		}
		//
		// if m == 1
		// y = y.'; % convert back to row if necessary
		// end

		return y;
	}

	public static final double[] filter(double b[], double a[], double x[]) {
		return filter(b, a, x, new double[x.length]);
	}

	public static final double[] filter(double b[], double a[], double x[],
			double y[]) {
		if (b.length < a.length) {
			double btmp[] = b;
			b = new double[a.length];
			for (int i = 0; i < btmp.length; i++) {
				b[i] = btmp[i];
			}
			for (int i = btmp.length; i < b.length; i++) {
				b[i] = 0;
			}
		} else if (b.length > a.length) {
			double atmp[] = a;
			a = new double[b.length];
			for (int i = 0; i < atmp.length; i++) {
				a[i] = atmp[i];
			}
			for (int i = atmp.length; i < a.length; i++) {
				a[i] = 0;
			}
		}

		for (int i = 0; i < b.length; i++) {
			y[i] = 0;
			for (int j = i; j >= 0; j--) {
				y[i] += b[i - j] * x[j];
			}
		}
		for (int i = 0; i < a.length; i++) {
			for (int j = i - 1; j >= 0; j--) {
				y[i] -= a[i - j] * y[j];
			}
			y[i] /= a[0];
		}

		for (int i = b.length; i < x.length; i++) {
			y[i] = 0;
			for (int j = i; j > i - b.length; j--) {
				y[i] += b[i - j] * x[j];
			}
		}
		for (int i = a.length; i < x.length; i++) {
			for (int j = i - 1; j > i - a.length; j--) {
				y[i] -= a[i - j] * y[j];
			}
			y[i] /= a[0];
		}

		return y;
	}

	public static final double[] filterZ(double b[], double a[], double x[],
			double zi[]) {
		double y[] = new double[x.length];
		return filterZ(b, a, x, zi, y);
	}

	public static final double[] filterZ(double b[], double a[], double x[],
			double zi[], double y[]) {

		if (b.length < a.length) {
			double btmp[] = b;
			b = new double[a.length];
			for (int i = 0; i < btmp.length; i++) {
				b[i] = btmp[i];
			}
			for (int i = btmp.length; i < b.length; i++) {
				b[i] = 0;
			}
		} else if (b.length > a.length) {
			double atmp[] = a;
			a = new double[b.length];
			for (int i = 0; i < atmp.length; i++) {
				a[i] = atmp[i];
			}
			for (int i = atmp.length; i < a.length; i++) {
				a[i] = 0;
			}
		}

		for (int i = 0; i < b.length; i++) {
			y[i] = 0;
			for (int j = i; j >= 0; j--) {
				y[i] += b[i - j] * x[j];
			}
		}

		for (int i = 0; i < a.length - 1; i++) {
			for (int j = i - 1; j >= 0; j--) {
				y[i] -= a[i - j] * y[j];
			}
			y[i] /= a[0];
			y[i] += zi[i];
		}
		for (int j = a.length - 2; j >= 0; j--) {
			y[a.length - 1] -= a[a.length - 1 - j] * y[j];
		}
		y[a.length - 1] /= a[0];

		for (int i = b.length; i < x.length; i++) {
			y[i] = 0;
			for (int j = i; j > i - b.length; j--) {
				y[i] += b[i - j] * x[j];
			}
		}
		for (int i = a.length; i < x.length; i++) {
			for (int j = i - 1; j > i - a.length; j--) {
				y[i] -= a[i - j] * y[j];
			}
			y[i] /= a[0];
		}
		return y;
	}

	public static Filter butter(int n, double Wn1, double Wn2) {
		// FIXME!!!!
		// double b[] = { 6.8958198127898718e-02, 0.0000000000000000e+00,
		// -2.7583279251159487e-01, 0.0000000000000000e+00,
		// 4.1374918876739225e-01, 0.0000000000000000e+00,
		// -2.7583279251159487e-01, 0.0000000000000000e+00,
		// 6.8958198127898718e-02 };
		// double a[] = { 1.0000000000000000e+00, -4.3613393544232331e+00,
		// 7.9766493056040328e+00, -8.3676930923035275e+00,
		// 5.9554956728855464e+00, -3.0270160667461750e+00,
		// 9.6339964091306085e-01, -1.5978865589624947e-01,
		// 2.0292549971106792e-02 };

		// return new Filter(b, a);
		return (Filter) FilterBank.m1d8.get("");
	}

	public static void butter(int n, double[] Wn) {
		// // [btype,analog,errStr] = iirchk(Wn,varargin{:});
		// // error(errStr)
		// //
		// // if n>500
		// // error('Filter order too large.')
		// // end
		// //
		// // % step 1: get analog, pre-warped frequencies
		// // if ~analog,
		// // fs = 2;
		// // u = 2*fs*tan(pi*Wn/fs);
		// // else
		// // u = Wn;
		// // end

		int fs = 2;
		double u[] = new double[2];
		u[0] = 2 * fs * Math.tan(Math.PI * Wn[0] / fs);
		u[1] = 2 * fs * Math.tan(Math.PI * Wn[1] / fs);

		// // Bw=[];
		// // % step 2: convert to low-pass prototype estimate
		// // if btype == 1 % lowpass
		// // Wn = u;
		// // elseif btype == 2 % bandpass
		// // Bw = u(2) - u(1);
		// // Wn = sqrt(u(1)*u(2)); % center frequency

		// double Bw = u[1] - u[0];
		// double Wn2 = Math.sqrt(u[0] * u[1]);

		// // elseif btype == 3 % highpass
		// // Wn = u;
		// // elseif btype == 4 % bandstop
		// // Bw = u(2) - u(1);
		// // Wn = sqrt(u(1)*u(2)); % center frequency
		// // end
		// //
		// // % step 3: Get N-th order Butterworth analog lowpass prototype
		// // [z,p,k] = buttap(n);

		double pRe[] = new double[n];
		double pIm[] = new double[n];
		buttap(n, pRe, pIm);

	}

	public static final double buttap(int n, double[] pRe, double[] pIm) {
		// function [z,p,k] = buttap(n)
		// % Poles are on the unit circle in the left-half plane.
		// z = [];
		// p = exp(i*(pi*(1:2:n-1)/(2*n) + pi/2));
		for (int i = 0; i < n / 2; i++) {
			double y = (Math.PI * (2 * i + 1) / (2 * n) + Math.PI / 2);
			pRe[2 * i] = Math.cos(y); // * Math.exp(x)
			pIm[2 * i] = Math.sin(y); // * Math.exp(x)
		}
		// p = [p; conj(p)];
		// p = p(:);
		for (int i = 0; i < n / 2; i++) {
			pRe[2 * i + 1] = pRe[2 * i];
			pIm[2 * i + 1] = -pIm[2 * i];
		}
		// if rem(n,2)==1 % n is odd
		// p = [p; -1];
		// end
		if (n % 2 == 1) {
			pRe[n - 1] = -1;
			pIm[n - 1] = 0;
		}

		// k = real(prod(-p));
		Complex x = new Complex(1, 0);
		for (int i = 0; i < pRe.length; i++) {
			x.mult(-pRe[i], -pIm[i]);
		}

		return x.real();
	}

	public static void WindowFunc(int whichFunction, int NumSamples, double[] in) {
		int i;

		if (whichFunction == 1) {
			// Bartlett (triangular) window
			for (i = 0; i < NumSamples / 2; i++) {
				in[i] *= (i / (double) (NumSamples / 2));
				in[i + (NumSamples / 2)] *= (1.0 - (i / (double) (NumSamples / 2)));
			}
		}

		if (whichFunction == 2) {
			// Hamming
			for (i = 0; i < NumSamples; i++)
				in[i] *= 0.54 - 0.46 * Math.cos(2 * Math.PI * i
						/ (NumSamples - 1));
		}

		if (whichFunction == 3) {
			// Hanning
			for (i = 0; i < NumSamples; i++)
				in[i] *= 0.50 - 0.50 * Math.cos(2 * Math.PI * i
						/ (NumSamples - 1));
		}
	}
}
