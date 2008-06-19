package acmus.util;

import java.text.DecimalFormat;


public class PrintUtils {

	public final static void print(double[] y) {
		for (int i = 0; i < y.length; i++)
			System.out.print(PrintUtils._f.format(y[i]) + " ");
		System.out.println();
	}

	public final static void print(int[] y) {
		for (int i = 0; i < y.length; i++)
			System.out.print(PrintUtils._f.format(y[i]) + " ");
		System.out.println();
	}

	public final static String toString(int[] y) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < y.length - 1; i++) {
			sb.append(y[i] + " ");
		}
		sb.append(y[y.length - 1]);
		return sb.toString();
	}

	public final static String formatString(String str, int compr) {
		if (str.length() >= compr)
			return str;
		char[] buf = new char[compr - str.length()];
		for (int i = 0; i < buf.length; i++)
			buf[i] = ' ';
		String brancos = new String(buf);
		return new String(brancos + str);
	}

	public static DecimalFormat _f = new DecimalFormat("#.######");

}
