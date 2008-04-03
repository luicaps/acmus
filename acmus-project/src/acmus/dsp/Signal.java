/*
 *  Signal.java
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import acmus.AcmusPlugin;

public class Signal {

	private static Map<Integer, List<int[]>> _order_mlstaps;

	private static int _maxOrder = 0;

	static {
		_order_mlstaps = new HashMap<Integer, List<int[]>>();

		BufferedReader br = null;
		URL u = null;

		// String __p = "/home/lku/Workspace/acmus/data";
		try {
			u = AcmusPlugin.getDefault().getBundle().getEntry(
					"data/mlstaps.txt");
			br = new BufferedReader(new InputStreamReader(u.openStream()));
			// br = new BufferedReader(new FileReader(__p + "/mlstaps.txt"));
			readMlsTaps(br);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final static void readMlsTaps(BufferedReader br) {
		try {
			String line;
			do
				line = br.readLine();
			while (line != null && line.trim().equals(""));
			while (line != null) {
				StringTokenizer st = new StringTokenizer(line);
				int order = Integer.parseInt(st.nextToken());
				// int len = Integer.parseInt(st.nextToken());
				line = br.readLine();
				List<Integer> l = new ArrayList<Integer>();
				while (line != null && !line.trim().equals("")) {
					st = new StringTokenizer(line);
					st.nextToken();
					while (st.hasMoreTokens()) {
						l.add(new Integer(st.nextToken()));
					}
					int[] taps = new int[l.size()];
					int i = 0;
					for (Integer v : l) {
						taps[i++] = v.intValue();
					}
					// MlsTaps mlstap = new MlsTaps(order, len, cl, taps);

					if (taps.length == 2) { // FIXME: implemented only 2 taps
						List<int[]> ltaps = null;
						if (_order_mlstaps.containsKey(order)) {
							ltaps = _order_mlstaps.get(order);
						} else {
							ltaps = new ArrayList<int[]>();
							_order_mlstaps.put(order, ltaps);
						}

						ltaps.add(taps);
					}

					if (order > _maxOrder)
						_maxOrder = order;
					line = br.readLine();
				}
				do {
					line = br.readLine();
				} while (line != null && line.trim().equals(""));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<int[]> mlsTaps(int order) {
		return _order_mlstaps.get(order);
	}

	public static int maxMlsOrder() {
		return _maxOrder;
	}

	public static boolean hasMlsOrder(int order) {
		return _order_mlstaps.containsKey(order);
	}

	// public static int[] getMlsTaps(int order, String cl) {
	// return _order_mlstaps.get(order + cl).taps();
	// }
	//  
	// public static Iterator getMlsTaps() {
	// return new MlsTapsIterator(_order_mlstaps.values().iterator());
	// }

	public static double[] chirpLog(double[] t, double f0, double t1,
			double f1, double phi) {

		double beta = Util.log10(f1 - f0) / t1;
		// y = cos(
		// 2*pi *
		// (
		// (10.^(beta.*t)-1)./(beta.*log(10)) + f0.*t + phi/360
		// )
		// );

		double y[] = new double[t.length];

		for (int i = 0; i < y.length; i++) {
			y[i] = Math.cos(2
					* Math.PI
					* (((Math.pow(10, beta * t[i]) - 1) / (beta * Util._log10))
							+ f0 * t[i] + phi / 360));
		}

		return y;
	}

	public final static double[] sweepLog(double sf, double dur, double f0,
			double f1) {
		return sweepLog(sf, dur, f0, f1, 0, 0);
	}

	public final static double[] sweepLog(double sf, double dur, double f0,
			double f1, double smoothStart, double smoothEnd) {

		double t[] = new double[(int) (dur * sf)];
		for (int i = 0; i < t.length; i++) {
			t[i] = i / sf;
		}
		double y[] = chirpLog(t, f0, dur, f1, -90);

		int s = (int) (smoothStart * y.length);
		int e = y.length - (int) (smoothEnd * y.length);

		for (int i = 0; i < s; i++) {
			y[i] *= 0.5 * (-Math.cos(Math.PI * t[i] / t[s]) + 1);
		}
		for (int i = e; i < y.length; i++) {
			y[i] *= 0.5 * (-Math.cos(Math.PI * t[i] / t[s]) + 1);
		}

		return y;
	}

	// public final static void smoothSweep(double sweep[],
	// ) {
	// int s = (int)(pStart*sweep.length);
	// int e = sweep.length - (int)(pEnd*sweep.length);
	// for(int i = 0; i < s; i++) {
	//      
	// }
	//
	// }

	public final static double[] mls(int order, int tap1, int tap2, int n) {
		int l = (1 << order) - 1;
		double y[] = new double[l];
		int row[] = new int[l];
		int col[] = new int[l];

		mls(order, tap1, tap2, y, row, col);
		// mls = kron(ones(n,1),m;
		double res[] = new double[l * n];
		for (int i = 0; i < res.length; i++) {
			res[i] = y[i % l];
		}
		return res;
	}

	public final static double[] mls(int order, int tap1, int tap2, double[] y,
			int[] row, int[] col, int n) {
		int l = (1 << order) - 1;
		mls(order, tap1, tap2, y, row, col);
		// mls = kron(ones(n,1),m;
		for (int i = l; i < l * n; i++) {
			y[i] = y[i % l];
		}
		return y;
	}

	// void mls(unsigned long int n, unsigned long int tap1, unsigned long int
	// tap2,
	// double *y, double *row, double *col)
	public final static void mls(int n, int tap1, int tap2, double[] y,
			int[] row, int[] col) {
		// {
		// unsigned int i, j, p, t, L, *temp;
		// double *aux;
		// L=pow(2,n)-1;

		int L = (int) Math.pow(2, n) - 1;

		// temp = mxCalloc(L, sizeof(unsigned long int));
		// aux = mxCalloc(L, sizeof(double));

		long[] temp = new long[L];
		int[] aux = new int[L];

		// for (i=0;i<L;i++) *(temp+i) = (unsigned long int)*(y+i);
		// What for???
		for (int i = 0; i < L; i++)
			temp[i] = (long) y[i];

		//
		// for (i=0;i<n;i++) *(temp+i) = 1; //Calcula a sequencia de maximo
		for (int i = 0; i < n; i++)
			temp[i] = 1;

		// for (i=0;i<L-n;i++) *(temp+i+n) = *(temp+n+i-tap1) ^
		// *(temp+n+i-tap2);
		for (int i = 0; i < L - n; i++)
			temp[i + n] = temp[n + i - tap1] ^ temp[n + i - tap2];
		// //comprimento de ordem n.
		//
		// for (i=0;i<L;i++){
		// row[i]=0; //Calcula o vetor de permutacao
		// for (j=0;j<n;j++){ //de linhas.
		// *(row+i) += *(temp+((i+L-j)%L)) * pow(2,j);
		// }
		// }
		for (int i = 0; i < L; i++) {
			row[i] = 0; // Calcula o vetor de permutacao
			for (int j = 0; j < n; j++) { // de linhas.
				row[i] += temp[((i + L - j) % L)] * Math.pow(2, j);
			}
		}

		//
		// for (i=0;i<L;i++){
		// t=(unsigned long int)*(row+i);
		// *(aux+t-1)=i+1;
		// }
		for (int i = 0; i < L; i++) {
			int t = (int) row[i];
			aux[t - 1] = i + 1;
		}

		// for (i=0;i<L;i++) *(row+i) = *(aux+i);
		for (int i = 0; i < L; i++)
			row[i] = aux[i];

		//
		// for (i=0;i<L;i++){ //Calcula o vetor de permutacao
		// col[i]=0; //de colunas.
		// for (j=0;j<n;j++){
		// p = (unsigned long int)pow(2,j);
		// t = (unsigned long int)*(aux+p-1);
		// *(col+i) += *(temp+((t-1-i+L)%L)) * pow(2,j);
		// }
		// }

		for (int i = 0; i < L; i++) { // Calcula o vetor de permutacao
			col[i] = 0; // de colunas.
			for (int j = 0; j < n; j++) {
				int p = (int) Math.pow(2, j);
				int t = (int) aux[p - 1];
				col[i] += temp[(t - 1 - i + L) % L] * Math.pow(2, j);
			}
		}

		//
		// for (i=0;i<L;i++) *(y+i) = pow(-1,*(temp+i)); //Mapeia 1 -> -1 e 0 ->
		// 1.
		for (int i = 0; i < L; i++)
			y[i] = Math.pow(-1, temp[i]); // Mapeia 1 -> -1 e 0 -> 1.

		// mxFree(temp);
		// mxFree(aux);
		// }
	}

}

// class MlsTapsIterator implements Iterator {
//
// Iterator<MlsTaps> _it;
//  
// public MlsTapsIterator(Iterator<MlsTaps> it) {
// _it = it;
// }
//  
// public boolean hasNext() {
// return _it.hasNext();
// }
//
// public Object next() {
// return _it.next().taps();
// }
//
// public void remove() {
// }
// }
//
// class MlsTaps {
// int _order;
//
// int _len;
//
// String _cl;
//
// int[] _taps;
//
// public MlsTaps(int order, int len, String cl, int[] taps) {
// _order = order;
// _len = len;
// _cl = cl;
// _taps = taps;
// }
//
// public String tapsClass() {
// return _cl;
// }
//
// public void set_cl(String _cl) {
// this._cl = _cl;
// }
//
// public int length() {
// return _len;
// }
//
// public void set_len(int _len) {
// this._len = _len;
// }
//
// public int order() {
// return _order;
// }
//
// public void set_order(int _order) {
// this._order = _order;
// }
//
// public int[] taps() {
// return _taps;
// }
//
// public void set_taps(int[] _taps) {
// this._taps = _taps;
// }
// }
