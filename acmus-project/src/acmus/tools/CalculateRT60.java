/*
 *  CalculateRT60.java
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
 * Calculates the reverberation time
 * 
 * @author Yang Yili
 */
public class CalculateRT60 {

  /**
   * Creates a new CalculateRT60
   * 
   * @param d1
   *          area of a ceiling
   * @param s1
   *          material of a ceiling
   * @param d2
   *          area of a frontwall
   * @param s2
   *          material of a frontwall
   * @param d3
   *          area of a backwall
   * @param s3
   *          material of a backwall
   * @param d4
   *          area of a leftwall
   * @param s4
   *          material of a leftwall
   * @param d5
   *          area of a rightwall
   * @param s5
   *          material of a rightwall
   * @param d6
   *          area of a floor
   * @param s6
   *          material of a floor
   * @param d7
   *          area of windows on the frontwall
   * @param s7
   *          material of windows on the frontwall
   * @param d8
   *          area of windows on the backwall
   * @param s8
   *          material of windows on the backwall
   * @param d9
   *          area of windows on the leftwall
   * @param s9
   *          material of windows on the leftwall
   * @param d10
   *          area of windows on the rightwall
   * @param s10
   *          material of windows on the rightwall
   * @param constant
   *          a constant that equals 0.161 when the units of measurement are
   *          expressed in meters and 0.049 when in feet
   * @param w
   *          width of a room
   * @param l
   *          length of a room
   * @param h
   *          height of a room
   */
  public CalculateRT60(double d1, String s1, double d2, String s2, double d3,
      String s3, double d4, String s4, double d5, String s5, double d6,
      String s6, double d7, String s7, double d8, String s8, double d9,
      String s9, double d10, String s10, double constant, double w, double l,
      double h) {
    a_c = d1;
    str[0] = s1;
    a_fw = d2;
    str[1] = s2;
    a_bw = d3;
    str[2] = s3;
    a_lw = d4;
    str[3] = s4;
    a_rw = d5;
    str[4] = s5;
    a_f = d6;
    str[5] = s6;
    a_w1 = d7;
    str[6] = s7;
    a_w2 = d8;
    str[7] = s8;
    a_w3 = d9;
    str[8] = s9;
    a_w4 = d10;
    str[9] = s10;
    k = constant;
    width = w;
    length = l;
    height = h;
  }/* construtor CalculateRT60 */

  /**
   * Calculates the reverberation time
   * 
   * @param no
   *          parameter
   */
  public void calculateRT60() {
    double volume;
    for (int j = 0; j < 10; j++)
      verifyMaterial(j, str[j]);
    volume = width * length * height;
    if (a_w1 > a_fw)
      a_w1 = a_fw;
    if (a_w2 > a_bw)
      a_w2 = a_bw;
    if (a_w3 > a_lw)
      a_w3 = a_lw;
    if (a_w4 > a_rw)
      a_w4 = a_rw;
    for (int i = 0; i < 6; i++) {
      double Sa = (a_c * coefficient[0][i] + (a_fw - a_w1) * coefficient[1][i]
          + (a_bw - a_w2) * coefficient[2][i] + (a_lw - a_w3)
          * coefficient[3][i] + (a_rw - a_w4) * coefficient[4][i] + a_f
          * coefficient[5][i] + a_w1 * coefficient[6][i] + a_w2
          * coefficient[7][i] + a_w3 * coefficient[8][i] + a_w4
          * coefficient[9][i]);
      if (Sa == 0.0)
        Sa = 1.0;
      time[i] = k * volume / Sa;
    }
  }/* method calculateRT60 */

  /**
   * Determines the absorption coefficient of materials
   * 
   * @param j
   *          the number of a surface or windows
   * @param s
   *          material name
   */
  private void verifyMaterial(int j, String s) {
    if (s.equals("Concrete-unpainted") == true)
      System.arraycopy(material1, 0, coefficient[j], 0, 6);
    else if (s.equals("Concrete-painted") == true)
      System.arraycopy(material2, 0, coefficient[j], 0, 6);
    else if (s.equals("Brick") == true)
      System.arraycopy(material3, 0, coefficient[j], 0, 6);
    else if (s.equals("Plaster on lath") == true)
      System.arraycopy(material4, 0, coefficient[j], 0, 6);
    else if (s.equals("Plywood paneling") == true)
      System.arraycopy(material5, 0, coefficient[j], 0, 6);
    else if (s.equals("Drapery-lightwt") == true)
      System.arraycopy(material6, 0, coefficient[j], 0, 6);
    else if (s.equals("Drapery-heavywt") == true)
      System.arraycopy(material7, 0, coefficient[j], 0, 6);
    else if (s.equals("Terrazzo") == true)
      System.arraycopy(material8, 0, coefficient[j], 0, 6);
    else if (s.equals("Wood floor") == true)
      System.arraycopy(material9, 0, coefficient[j], 0, 6);
    else if (s.equals("Carpet on concrete") == true)
      System.arraycopy(material10, 0, coefficient[j], 0, 6);
    else if (s.equals("Carpet on pad") == true)
      System.arraycopy(material11, 0, coefficient[j], 0, 6);
    else if (s.equals("Ac. tile suspended") == true)
      System.arraycopy(material12, 0, coefficient[j], 0, 6);
    else if (s.equals("Ac. tile on concrete") == true)
      System.arraycopy(material13, 0, coefficient[j], 0, 6);
    else if (s.equals("Gypsum  board") == true)
      System.arraycopy(material14, 0, coefficient[j], 0, 6);
    else if (s.equals("Glass-large panes") == true)
      System.arraycopy(material15, 0, coefficient[j], 0, 6);
    else if (s.equals("Glass-windows") == true)
      System.arraycopy(material16, 0, coefficient[j], 0, 6);
    else if (s.equals("Marble or Tile") == true)
      System.arraycopy(material17, 0, coefficient[j], 0, 6);
    else
      System.arraycopy(material18, 0, coefficient[j], 0, 6);
  }/* method verifyMaterial */

  double[] time = new double[10];
  private double k;
  private double width;
  private double length;
  private double height;
  private double a_c;
  private double a_fw;
  private double a_bw;
  private double a_lw;
  private double a_rw;
  private double a_f;
  private double a_w1;
  private double a_w2;
  private double a_w3;
  private double a_w4;
  private double[][] coefficient = new double[10][6];
  private String[] str = new String[10];
  private double[] material1 = { 0.36, 0.44, 0.31, 0.29, 0.39, 0.25 };
  private double[] material2 = { 0.10, 0.05, 0.06, 0.07, 0.09, 0.08 };
  private double[] material3 = { 0.03, 0.03, 0.03, 0.04, 0.05, 0.07 };
  private double[] material4 = { 0.14, 0.10, 0.06, 0.05, 0.04, 0.03 };
  private double[] material5 = { 0.28, 0.22, 0.17, 0.09, 0.10, 0.11 };
  private double[] material6 = { 0.03, 0.04, 0.11, 0.17, 0.24, 0.35 };
  private double[] material7 = { 0.14, 0.35, 0.55, 0.72, 0.70, 0.65 };
  private double[] material8 = { 0.01, 0.01, 0.02, 0.02, 0.02, 0.02 };
  private double[] material9 = { 0.15, 0.11, 0.10, 0.07, 0.06, 0.07 };
  private double[] material10 = { 0.02, 0.06, 0.14, 0.37, 0.60, 0.65 };
  private double[] material11 = { 0.08, 0.24, 0.57, 0.69, 0.71, 0.73 };
  private double[] material12 = { 0.76, 0.93, 0.83, 0.99, 0.99, 0.94 };
  private double[] material13 = { 0.14, 0.20, 0.76, 0.79, 0.58, 0.37 };
  private double[] material14 = { 0.29, 0.10, 0.05, 0.04, 0.07, 0.09 };
  private double[] material15 = { 0.05, 0.03, 0.02, 0.02, 0.03, 0.02 };
  private double[] material16 = { 0.10, 0.05, 0.04, 0.03, 0.03, 0.03 };
  private double[] material17 = { 0.01, 0.01, 0.01, 0.01, 0.02, 0.02 };
  private double[] material18 = { 0.01, 0.01, 0.01, 0.01, 0.02, 0.03 };
}/* class CalculateRT60 */
