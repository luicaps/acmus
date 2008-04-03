/*
 *  IFFT1d.java
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
 * IFFT1d.java
 * Created on 14/04/2005
 */
package acmus.dsp;

// Title: 1-d mixed radix Inverse FFT.
// Version:
// Copyright: Copyright (c) 1998
// Author: Dongyan Wang
// Company: University of Wisconsin-Milwaukee.
// Description:
// According to the definition of the inverse fourier transform,
// We can use FFT to calculate the IFFT,
// IFFT(x) = 1/N * conj(FFT(conj(x)).
//
// . Change the sign of the imaginary part of the FFT input.
// . Calculate the FFT.
// . Change the sign of the imaginary part of the FFT output.
// . Scale the output by 1/N.
//

public class IFFT1d {
	int N;

	// Constructor: IFFT of Complex data.
	public IFFT1d(int N) {
		this.N = N;
	}

	public void ifft(double inputRe[], double inputIm[]) {

		// Change the sign of the imaginary part of the FFT input.
		for (int i = 0; i < N; i++)
			inputIm[i] = -inputIm[i];

		// Calculate the FFT.
		FFT1d fft1 = new FFT1d(N);
		fft1.fft(inputRe, inputIm);

		// Change the sign of the imaginary part of the FFT output.
		// Scale output by 1/N.

		for (int i = 0; i < inputRe.length; i++) {
			inputRe[i] = inputRe[i] / N;
			inputIm[i] = -inputIm[i] / N;
		}
	}
}