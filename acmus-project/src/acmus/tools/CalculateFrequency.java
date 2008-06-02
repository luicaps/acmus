/*
 *  CalculateFrequency.java
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

import java.util.Vector;

/**
 * Computes the resonance frequencies
 * 
 * @author Yang Yili
 * @author yili@linux.ime.usp.br
 */
public class CalculateFrequency {
	private final double v = 344.0;
	private double width;
	private double length;
	private double height;
	private boolean input_valid;
	private Vector<Double> axial_frequency = new Vector<Double>();
	private Vector<Double> tangential_frequency = new Vector<Double>();
	private Vector<Double> oblique_frequency = new Vector<Double>();

	/**
	 * Creates a new CalculateFrequency
	 * 
	 * @param width
	 *            width of a room
	 * @param length
	 *            length of a room
	 * @param height
	 *            height of a room
	 */
	CalculateFrequency(double width, double length, double height) {
		this.width = width;
		this.length = length;
		this.height = height;
	}

	/**
	 * Computes the axial frequencies
	 * 
	 * @param no
	 *            parameter
	 */
	public void calculateAxialFrequency() {
		int p, q, r;
		testInput();
		if (this.input_valid == true) {
			for (p = 1; p <= (int) 300.0 * 2.0 * this.length / this.v; p++) {
				double frequency = this.v / 2.0
						* Math.sqrt((p * p) / (this.length * this.length));
				this.axial_frequency.addElement(new Double(frequency));
			}
			for (q = 1; q <= (int) 300.0 * 2.0 * this.width / this.v; q++) {
				double frequency = this.v / 2.0
						* Math.sqrt((q * q) / (this.width * this.width));
				this.axial_frequency.addElement(new Double(frequency));
			}
			for (r = 1; r <= (int) 300.0 * 2.0 * this.height / this.v; r++) {
				double frequency = this.v / 2.0
						* Math.sqrt((r * r) / (this.height * this.height));
				this.axial_frequency.addElement(new Double(frequency));
			}
		}

	}/* method calculateAxialFrequency */

	/**
	 * Computes the tangential frequencies
	 * 
	 * @param no
	 *            parameter
	 */
	public void calculateTangentialFrequency() {
		int p, q, r;
		testInput();
		if (this.input_valid == true) {
			if (1.0 / (this.width * this.width) <= 600.0 / this.v
					&& 1.0 / (this.height * this.height) <= 600.0 / this.v) {
				double x = this.width
						* Math.sqrt(600.0 * 600.0 / (this.v * this.v) - 1.0
								/ (this.height * this.height));
				for (q = 1; q <= (int) x; q++) {
					double y = this.height
							* Math.sqrt(600.0 * 600.0 / (this.v * this.v) - 1.0
									/ (this.width * this.width));
					for (r = 1; r <= (int) y; r++) {
						double frequency = this.v
								/ 2.0
								* Math.sqrt((r * r)
										/ (this.height * this.height) + (q * q)
										/ (this.width * this.width));
						if (frequency <= 300.0)
							this.tangential_frequency.addElement(new Double(
									frequency));
					}
				}
			}
			if (1.0 / (this.length * this.length) <= 600.0 / this.v
					&& 1.0 / (this.height * this.height) <= 600.0 / this.v) {
				double x = this.length
						* Math.sqrt(600.0 * 600.0 / (this.v * this.v) - 1.0
								/ (this.height * this.height));
				for (p = 1; p <= (int) x; p++) {
					double y = this.height
							* Math.sqrt(600.0 * 600.0 / (this.v * this.v) - 1.0
									/ (this.length * this.length));
					for (r = 1; r <= (int) y; r++) {
						double frequency = this.v
								/ 2.0
								* Math.sqrt((r * r)
										/ (this.height * this.height) + (p * p)
										/ (this.length * this.length));
						if (frequency <= 300.0)
							this.tangential_frequency.addElement(new Double(
									frequency));
					}
				}
			}
			if (1.0 / (this.length * this.length) <= 600.0 / this.v
					&& 1.0 / (this.width * this.width) <= 600.0 / this.v) {
				double x = this.length
						* Math.sqrt(600.0 * 600.0 / (this.v * this.v) - 1.0
								/ (this.width * this.width));
				for (p = 1; p <= (int) x; p++) {
					double y = this.width
							* Math.sqrt(600.0 * 600.0 / (this.v * this.v) - 1.0
									/ (this.length * this.length));
					for (q = 1; q <= (int) y; q++) {
						double frequency = this.v
								/ 2.0
								* Math
										.sqrt((q * q)
												/ (this.width * this.width)
												+ (p * p)
												/ (this.length * this.length));
						if (frequency <= 300.0)
							this.tangential_frequency.addElement(new Double(
									frequency));
					}
				}
			}
		}

	}/* method calculateTangentialFrequency */

	/**
	 * Computes the oblique frequencies
	 * 
	 * @param no
	 *            parameter
	 */
	public void calculateObliqueFrequency() {
		int p, q, r;
		testInput();
		if (this.input_valid == true)
			if (this.v
					* Math.sqrt(1 / (this.height * this.height) + 1
							/ (this.length * this.length) + 1
							/ (this.width * this.width)) / 2.0 <= 300.0) {
				double x = this.length
						* Math.sqrt(600.0 * 600.0 / (this.v * this.v) - 1.0
								/ (this.height * this.height) - 1.0
								/ (this.width * this.width));
				for (p = 1; p <= (int) x; p++) {
					double y = this.width
							* Math.sqrt(600.0 * 600.0 / (this.v * this.v) - 1.0
									/ (this.height * this.height) - 1.0
									/ (this.length * this.length));
					for (q = 1; q <= (int) y; q++) {
						double z = this.height
								* Math.sqrt(600.0 * 600.0 / (this.v * this.v)
										- 1.0 / (this.width * this.width) - 1.0
										/ (this.length * this.length));
						for (r = 1; r <= (int) z; r++) {
							double frequency = this.v
									/ 2.0
									* Math.sqrt((q * q)
											/ (this.width * this.width)
											+ (p * p)
											/ (this.length * this.length)
											+ (r * r)
											/ (this.height * this.height));
							if (frequency <= 300.0)
								this.oblique_frequency.addElement(new Double(
										frequency));
						}
					}
				}
			}

	}/* method calculateObliqueFrequency */

	/**
	 * returns the input validity
	 * 
	 * @return input_valid the input validity
	 */
	public boolean returnInputValidity() {
		return this.input_valid;
	}

	/**
	 * Verifies the input validity
	 */
	private void testInput() {
		if (this.length * this.height * this.width == 0.0)
			this.input_valid = false;
		else
			this.input_valid = true;
	}

	public Vector<Double> getAxialFrequencyVector() {
		return this.axial_frequency;
	}

	public Vector<Double> getTangentialFrequencyVector() {
		return this.tangential_frequency;
	}

	public Vector<Double> getObliqueFrequencyVector() {
		return this.oblique_frequency;
	}
}
