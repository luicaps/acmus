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
	/**
	 * Creates a new CalculateFrequency
	 * 
	 * @param x
	 *            width of a room
	 * @param y
	 *            length of a room
	 * @param z
	 *            height of a room
	 */
	CalculateFrequency(double x, double y, double z) {
		width = x;
		length = y;
		height = z;
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
		if (input_valid == true) {
			for (p = 1; p <= (int) 300.0 * 2.0 * length / v; p++) {
				double frequency = v / 2.0
						* Math.sqrt((p * p) / (length * length));
				axial_frequency.addElement(new Double(frequency));
			}
			for (q = 1; q <= (int) 300.0 * 2.0 * width / v; q++) {
				double frequency = v / 2.0
						* Math.sqrt((q * q) / (width * width));
				axial_frequency.addElement(new Double(frequency));
			}
			for (r = 1; r <= (int) 300.0 * 2.0 * height / v; r++) {
				double frequency = v / 2.0
						* Math.sqrt((r * r) / (height * height));
				axial_frequency.addElement(new Double(frequency));
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
		if (input_valid == true) {
			if (1.0 / (width * width) <= 600.0 / v
					&& 1.0 / (height * height) <= 600.0 / v) {
				double x = width
						* Math.sqrt(600.0 * 600.0 / (v * v) - 1.0
								/ (height * height));
				for (q = 1; q <= (int) x; q++) {
					double y = height
							* Math.sqrt(600.0 * 600.0 / (v * v) - 1.0
									/ (width * width));
					for (r = 1; r <= (int) y; r++) {
						double frequency = v
								/ 2.0
								* Math.sqrt((r * r) / (height * height)
										+ (q * q) / (width * width));
						if (frequency <= 300.0) {
							tangential_frequency.addElement(new Double(
									frequency));
						}
					}
				}
			}
			if (1.0 / (length * length) <= 600.0 / v
					&& 1.0 / (height * height) <= 600.0 / v) {
				double x = length
						* Math.sqrt(600.0 * 600.0 / (v * v) - 1.0
								/ (height * height));
				for (p = 1; p <= (int) x; p++) {
					double y = height
							* Math.sqrt(600.0 * 600.0 / (v * v) - 1.0
									/ (length * length));
					for (r = 1; r <= (int) y; r++) {
						double frequency = v
								/ 2.0
								* Math.sqrt((r * r) / (height * height)
										+ (p * p) / (length * length));
						if (frequency <= 300.0) {
							tangential_frequency.addElement(new Double(
									frequency));
						}
					}
				}
			}
			if (1.0 / (length * length) <= 600.0 / v
					&& 1.0 / (width * width) <= 600.0 / v) {
				double x = length
						* Math.sqrt(600.0 * 600.0 / (v * v) - 1.0
								/ (width * width));
				for (p = 1; p <= (int) x; p++) {
					double y = width
							* Math.sqrt(600.0 * 600.0 / (v * v) - 1.0
									/ (length * length));
					for (q = 1; q <= (int) y; q++) {
						double frequency = v
								/ 2.0
								* Math.sqrt((q * q) / (width * width) + (p * p)
										/ (length * length));
						if (frequency <= 300.0) {
							tangential_frequency.addElement(new Double(
									frequency));
						}
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
		if (input_valid == true) {
			if (v
					* Math.sqrt(1 / (height * height) + 1 / (length * length)
							+ 1 / (width * width)) / 2.0 <= 300.0) {
				double x = length
						* Math.sqrt(600.0 * 600.0 / (v * v) - 1.0
								/ (height * height) - 1.0 / (width * width));
				for (p = 1; p <= (int) x; p++) {
					double y = width
							* Math.sqrt(600.0 * 600.0 / (v * v) - 1.0
									/ (height * height) - 1.0
									/ (length * length));
					for (q = 1; q <= (int) y; q++) {
						double z = height
								* Math.sqrt(600.0 * 600.0 / (v * v) - 1.0
										/ (width * width) - 1.0
										/ (length * length));
						for (r = 1; r <= (int) z; r++) {
							double frequency = v
									/ 2.0
									* Math.sqrt((q * q) / (width * width)
											+ (p * p) / (length * length)
											+ (r * r) / (height * height));
							if (frequency <= 300.0) {
								oblique_frequency.addElement(new Double(
										frequency));
							}
						}
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
		return input_valid;
	}

	/**
	 * Verifies the input validity
	 */

	private void testInput() {
		if (length * height * width == 0.0)
			input_valid = false;
		else
			input_valid = true;
	}

	private final double v = 344.0;
	private double width;
	private double length;
	private double height;
	private boolean input_valid;
	Vector<Double> axial_frequency = new Vector<Double>();
	Vector<Double> tangential_frequency = new Vector<Double>();
	Vector<Double> oblique_frequency = new Vector<Double>();
}/* class CalculateFrequency */
