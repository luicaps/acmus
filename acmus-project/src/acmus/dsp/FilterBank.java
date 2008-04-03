/*
 *  FilterBank.java
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
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import acmus.AcmusPlugin;

public class FilterBank {
	public static final Map<FilterKey1d8, Filter> m1d8 = new Hashtable<FilterKey1d8, Filter>();;
	public static final FilterKey1d8 fk1d8 = new FilterKey1d8();

	public static final Map<FilterKeySweep, Filter> sweepButter = new Hashtable<FilterKeySweep, Filter>();;
	public static final FilterKeySweep fks = new FilterKeySweep();

	public static final Map<FilterKeyComp, Filter> comp = new Hashtable<FilterKeyComp, Filter>();;
	public static final FilterKeyComp fkc = new FilterKeyComp();

	static {
		BufferedReader br = null;

		URL u = null;
		// String __p = "/home/lku/Workspace/acmus/data/filters";
		try {
			u = AcmusPlugin.getDefault().getBundle().getEntry(
					"data/filters/1d8.txt");
			br = new BufferedReader(new InputStreamReader(u.openStream()));
			// br = new BufferedReader(new FileReader(__p + "/1d8.txt"));
			read1d8(br);

			u = AcmusPlugin.getDefault().getBundle().getEntry(
					"data/filters/sweep-butter.txt");
			br = new BufferedReader(new InputStreamReader(u.openStream()));
			// br = new BufferedReader(new FileReader(__p +
			// "/sweep-butter.txt"));
			readSweepButter(br);

			u = AcmusPlugin.getDefault().getBundle().getEntry(
					"data/filters/comp.txt");
			br = new BufferedReader(new InputStreamReader(u.openStream()));
			// br = new BufferedReader(new FileReader(__p + "/comp.txt"));
			readComp(br);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final void read1d8(BufferedReader br) {
		try {
			String line;
			do
				line = br.readLine();
			while (line != null && line.trim().equals(""));
			while (line != null) {
				StringTokenizer st = new StringTokenizer(line);
				int n = Integer.parseInt(st.nextToken());
				double fc = Double.parseDouble(st.nextToken());
				double fs = Double.parseDouble(st.nextToken());
				double a[] = new double[2 * n + 1];
				double b[] = new double[2 * n + 1];
				line = br.readLine();
				st = new StringTokenizer(line);
				if (st.countTokens() != b.length) {
					System.err.println("warning: butter: " + n + " "
							+ st.countTokens());
				}
				for (int i = 0; i < b.length; i++) {
					b[i] = Double.parseDouble(st.nextToken());
				}
				line = br.readLine();
				st = new StringTokenizer(line);
				if (st.countTokens() != a.length) {
					System.err.println("warning: butter: " + n + " "
							+ st.countTokens());
				}
				for (int i = 0; i < a.length; i++) {
					a[i] = Double.parseDouble(st.nextToken());
				}
				FilterKey1d8 fk = new FilterKey1d8(fc, fs);
				m1d8.put(fk, new Filter(b, a));
				do {
					line = br.readLine();
				} while (line != null && line.trim().equals(""));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final void readSweepButter(BufferedReader br) {
		try {
			String line;
			do
				line = br.readLine();
			while (line != null && line.trim().equals(""));
			while (line != null) {
				StringTokenizer st = new StringTokenizer(line);
				int n = Integer.parseInt(st.nextToken());
				double f0 = Double.parseDouble(st.nextToken());
				double f1 = Double.parseDouble(st.nextToken());
				double fs = Double.parseDouble(st.nextToken());
				double a[] = new double[2 * n + 1];
				double b[] = new double[2 * n + 1];
				line = br.readLine();
				st = new StringTokenizer(line);
				if (st.countTokens() != b.length) {
					System.err.println("warning: sbutter: " + n + " "
							+ st.countTokens());
				}
				for (int i = 0; i < b.length; i++) {
					b[i] = Double.parseDouble(st.nextToken());
				}
				line = br.readLine();
				st = new StringTokenizer(line);
				if (st.countTokens() != a.length) {
					System.err.println("warning: sbutter: " + n + " "
							+ st.countTokens());
				}
				for (int i = 0; i < a.length; i++) {
					a[i] = Double.parseDouble(st.nextToken());
				}
				FilterKeySweep fk = new FilterKeySweep(f0, f1, fs);
				sweepButter.put(fk, new Filter(b, a));
				do {
					line = br.readLine();
				} while (line != null && line.trim().equals(""));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final void readComp(BufferedReader br) {
		try {
			String line;
			do
				line = br.readLine();
			while (line != null && line.trim().equals(""));
			while (line != null) {
				StringTokenizer st = new StringTokenizer(line);
				String type = st.nextToken().trim();
				double fs = Double.parseDouble(st.nextToken());

				double a[] = null;
				double b[] = null;

				if (type.equals("a")) {
					a = new double[7];
					b = new double[7];
				} else if (type.equals("c")) {
					a = new double[5];
					b = new double[5];
				} else {
					System.err
							.println("warning: comp filter: unknown: " + type);
				}

				line = br.readLine();
				st = new StringTokenizer(line);
				if (st.countTokens() != b.length) {
					System.err.println("warning: comp: " + type + " "
							+ st.countTokens());
				}
				for (int i = 0; i < b.length; i++) {
					b[i] = Double.parseDouble(st.nextToken());
				}
				line = br.readLine();
				st = new StringTokenizer(line);
				if (st.countTokens() != a.length) {
					System.err.println("warning: comp: " + type + " "
							+ st.countTokens());
				}
				for (int i = 0; i < a.length; i++) {
					a[i] = Double.parseDouble(st.nextToken());
				}
				FilterKeyComp fk = new FilterKeyComp(type, fs);
				comp.put(fk, new Filter(b, a));
				do {
					line = br.readLine();
				} while (line != null && line.trim().equals(""));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final Filter get1d8(double fc, double fs) {
		fk1d8.setFc(fc);
		fk1d8.setFs(fs);
		Filter res = m1d8.get(fk1d8);
		if (res == null) {
			throw new Error("1/8 filter not found: " + fc + " " + fs);
		}
		return res;
	}

	public static final Filter getComp(String type, double fs) {
		fkc.setType(type);
		fkc.setFs(fs);
		Filter res = comp.get(fkc);
		if (res == null) {
			throw new Error("comp filter not found: " + type + " " + fs);
		}
		return res;
	}

	public static final Filter getSweepButter(double f0, double f1, double fs) {
		fks.setF0(f0);
		fks.setF1(f1);
		fks.setFs(fs);
		Filter res = sweepButter.get(fks);
		if (res == null) {
			throw new Error("sweep butter filter not found: " + f0 + " " + f1
					+ " " + fs);
		}
		return res;
	}

}

class FilterKey1d8 {
	double fc = 0;
	double fs = 0;

	public FilterKey1d8() {
	}

	public FilterKey1d8(double fc, double fs) {
		this.fc = fc;
		this.fs = fs;
	}

	public final void setFc(double f) {
		fc = f;
	}

	public final void setFs(double f) {
		fs = f;
	}

	public int hashCode() {
		return (int) (fc + fs);
	}

	public boolean equals(Object other) {
		FilterKey1d8 o = (FilterKey1d8) other;
		return o.fc == this.fc && o.fs == this.fs;
	}
}

class FilterKeyComp {
	String type = "";
	double fs = 0;

	public FilterKeyComp() {
	}

	public FilterKeyComp(String type, double fs) {
		this.type = type;
		this.fs = fs;
	}

	public final void setType(String t) {
		type = t;
	}

	public final void setFs(double f) {
		fs = f;
	}

	public int hashCode() {
		return (int) (type.hashCode() + fs);
	}

	public boolean equals(Object other) {
		FilterKeyComp o = (FilterKeyComp) other;
		return o.type.equals(this.type) && o.fs == this.fs;
	}
}

class FilterKeySweep {
	double f0 = 0;
	double f1 = 0;
	double fs = 0;

	public FilterKeySweep() {
	}

	public FilterKeySweep(double f0, double f1, double fs) {
		this.f0 = f0;
		this.f1 = f1;
		this.fs = fs;
	}

	public final void setF0(double f) {
		f0 = f;
	}

	public final void setF1(double f) {
		f1 = f;
	}

	public final void setFs(double f) {
		fs = f;
	}

	public int hashCode() {
		return (int) (f0 + f1 + fs);
	}

	public boolean equals(Object other) {
		FilterKeySweep o = (FilterKeySweep) other;
		return o.f0 == this.f0 && o.f1 == this.f1 && o.fs == this.fs;
	}
}
