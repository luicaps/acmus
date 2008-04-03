/*
 *  Complex.java
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

public final class Complex {
	double _r;
	double _i;

	public Complex(double r, double i) {
		_r = r;
		_i = i;
	}

	public final double real() {
		return _r;
	}

	public final double imag() {
		return _i;
	}

	public final void real(double r) {
		_r = r;
	}

	public final void imag(double i) {
		_i = i;
	}

	public final void mult(double re, double im) {
		double x = (_r * re) - (_i * im);
		double y = (_r * im) + (_i * re);
		_r = x;
		_i = y;
	}

	public final void div(double re, double im) {
		// mult(re/(re*re + im*im), -im/(re*re + im*im));
		double x = (_r * re) + (_i * im);
		double y = (-_r * im) + (_i * re);
		double s = re * re + im * im;
		_r = x / s;
		_i = y / s;
	}

}
