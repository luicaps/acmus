/*
 *  CalculateSpeedOfSound.java
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
package acmus.tools;

/**
 * Computes speed of sound
 * 
 * @author Yang Yili
 */
public class CalculateSpeedOfSound {
	/**
	 * Creates a new CalculateSpeedOfSound
	 * 
	 * @param x
	 *            temperature
	 * @param y
	 *            humidity
	 */
	public CalculateSpeedOfSound(double x, double y) {
		temp = x;
		humidity = y;
	}

	/**
	 * Computes speed of sound
	 * 
	 * @param no
	 *            parameter
	 * @return speed speed of sound
	 */
	public double calculateSpeedOfSound() {
		a[0] = 331.5024;
		a[1] = 0.603055;
		a[2] = -0.000528;
		a[3] = 51.471935;
		a[4] = 0.1495874;
		a[5] = -0.000782;
		a[6] = -1.82e-7;
		a[7] = 3.73e-8;
		a[8] = -2.93e-10;
		a[9] = -85.20931;
		a[10] = -0.228525;
		a[11] = 5.91e-5;
		a[12] = -2.835149;
		a[13] = -2.15e-13;
		a[14] = 29.179762;
		a[15] = 0.000486;
		T = temp + 273.15;
		h = humidity / 100.0;
		f = 1.00062 + 0.0000000314 * p + 0.00000056 * temp * temp;
		Psv = Math.exp(0.000012811805 * T * T - 0.019509874 * T + 34.04926034
				- 6353.6311 / T);
		Xw = h * f * Psv / p;
		c = 331.45 - a[0] - p * a[6] - a[13] * p * p;
		c = Math.sqrt(a[9] * a[9] + 4 * a[14] * c);
		Xc = ((-1) * a[9] - c) / (2 * a[14]);
		speed = a[0] + a[1] * temp + a[2] * temp * temp
				+ (a[3] + a[4] * temp + a[5] * temp * temp) * Xw
				+ (a[6] + a[7] * temp + a[8] * temp * temp) * p
				+ (a[9] + a[10] * temp + a[11] * temp * temp) * Xc + a[12] * Xw
				* Xw + a[13] * p * p + a[14] * Xc * Xc + a[15] * Xw * p * Xc;
		return (speed);
	}/* method calculateSpeedOfSound */

	private final double p = 101000;
	private double[] a = new double[16];
	private double temp;
	private double humidity;
	private double T;
	private double f, h, Psv, Xw, Xc;
	private double c;
	private double speed;
}/* class CalculateSpeedOfSound */
