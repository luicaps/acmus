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
	private final double p = 101000;
	private double[] a = new double[16];
	private double temperature;
	private double humidity;
	private double T;
	private double f, h, Psv, Xw, Xc;
	private double c;
	private double speed;

	/**
	 * Creates a new CalculateSpeedOfSound
	 * 
	 * @param temperature
	 *            temperature
	 * @param humidity
	 *            humidity
	 */
	public CalculateSpeedOfSound(double temperature, double humidity) {
		this.temperature = temperature;
		this.humidity = humidity;
	}

	/**
	 * Computes speed of sound
	 * 
	 * @param no
	 *            parameter
	 * @return speed speed of sound
	 */
	public double calculateSpeedOfSound() {
		this.a[0] = 331.5024;
		this.a[1] = 0.603055;
		this.a[2] = -0.000528;
		this.a[3] = 51.471935;
		this.a[4] = 0.1495874;
		this.a[5] = -0.000782;
		this.a[6] = -1.82e-7;
		this.a[7] = 3.73e-8;
		this.a[8] = -2.93e-10;
		this.a[9] = -85.20931;
		this.a[10] = -0.228525;
		this.a[11] = 5.91e-5;
		this.a[12] = -2.835149;
		this.a[13] = -2.15e-13;
		this.a[14] = 29.179762;
		this.a[15] = 0.000486;
		this.T = this.temperature + 273.15;
		this.h = this.humidity / 100.0;
		this.f = 1.00062 + 0.0000000314 * this.p + 0.00000056 * this.temperature
				* this.temperature;
		this.Psv = Math.exp(0.000012811805 * this.T * this.T - 0.019509874
				* this.T + 34.04926034 - 6353.6311 / this.T);
		this.Xw = this.h * this.f * this.Psv / this.p;
		this.c = 331.45 - this.a[0] - this.p * this.a[6] - this.a[13] * this.p
				* this.p;
		this.c = Math.sqrt(this.a[9] * this.a[9] + 4 * this.a[14] * this.c);
		this.Xc = ((-1) * this.a[9] - this.c) / (2 * this.a[14]);
		this.speed = this.a[0]
				+ this.a[1]
				* this.temperature
				+ this.a[2]
				* this.temperature
				* this.temperature
				+ (this.a[3] + this.a[4] * this.temperature + this.a[5] * this.temperature
						* this.temperature)
				* this.Xw
				+ (this.a[6] + this.a[7] * this.temperature + this.a[8] * this.temperature
						* this.temperature)
				* this.p
				+ (this.a[9] + this.a[10] * this.temperature + this.a[11] * this.temperature
						* this.temperature) * this.Xc + this.a[12] * this.Xw * this.Xw
				+ this.a[13] * this.p * this.p + this.a[14] * this.Xc * this.Xc
				+ this.a[15] * this.Xw * this.p * this.Xc;
		return (this.speed);
	}/* method calculateSpeedOfSound */

}
