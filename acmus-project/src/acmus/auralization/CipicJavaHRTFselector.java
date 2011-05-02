package acmus.auralization;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CipicJavaHRTFselector implements HRTFselector {

	private double[][][] hrir_l;
	private double[][][] hrir_r;

	CipicJavaHRTFselector() {
		this("data/CIPIC_hrtf_database/standard_hrir_database/"
				+ "subject_003/");
	}

	CipicJavaHRTFselector(String path) {
		Scanner scanner = null;

		try {
			scanner = new Scanner(new FileInputStream(path + "hrir_l.txt"),
					"UTF-8");
			while (scanner.hasNext("#")) {
				scanner.nextLine();
			}
			int dim1 = scanner.nextInt(), dim2 = scanner.nextInt(), dim3 = scanner
					.nextInt();
			hrir_l = new double[dim1][dim2][dim3];
			scanner.nextLine();
			for (int k = 0; k < dim3; k++) {
				for (int j = 0; j < dim2; j++) {
					for (int i = 0; i < dim1; i++) {
						hrir_l[i][j][k] = Double
								.parseDouble(scanner.nextLine());
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}

		try {
			scanner = new Scanner(new FileInputStream(path + "hrir_r.txt"),
					"UTF-8");
			while (scanner.hasNext("#")) {
				scanner.nextLine();
			}
			int dim1 = scanner.nextInt(), dim2 = scanner.nextInt(), dim3 = scanner
					.nextInt();
			hrir_r = new double[dim1][dim2][dim3];
			scanner.nextLine();
			for (int k = 0; k < dim3; k++) {
				for (int j = 0; j < dim2; j++) {
					for (int i = 0; i < dim1; i++) {
						hrir_r[i][j][k] = Double
								.parseDouble(scanner.nextLine());
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}

	@Override
	public double[][] getPulse(double azimuth, double elevation) {

		return new double[][] {
				getNearestUCDpulse(azimuth, elevation, hrir_l)[0],
				getNearestUCDpulse(azimuth, elevation, hrir_r)[0] };
	}

	private double[][] getNearestUCDpulse(double azimuth, double elevation,
			double[][][] h3D) {

		azimuth = pvaldeg(azimuth);
		if (azimuth < -90 || azimuth > 90) {
			throw new IllegalArgumentException("invalid azimith");
		}
		elevation = pvaldeg(elevation);

		int elmax = 50;
		double[] elevations = new double[elmax];
		for (int i = 0; i < elmax; i++) {
			elevations[i] = -45 + 5.625 * i;
		}
		int el = (int) Math.round((elevation + 45) / 5.625 + 1);
		el = Math.max(el, 1);
		el = Math.min(el, elmax);
		double elerr = pvaldeg(elevation - elevations[el - 1]);

		int[] azimuths = new int[] { -80, -65, -55, -45, -40, -35, -30, -25,
				-20, -15, -10, -5, 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 55,
				65, 80 };

		double azerrOld = Double.MAX_VALUE, azerr = 0;
		int az = 1;
		for (int i = 0; i < azimuths.length; i++) {
			azerr = Math.abs(pvaldeg(Math.abs(azimuths[i] - azimuth)));
			if (azerr < azerrOld) {
				az = i + 1;
				azerrOld = azerr;
			} else {
				azerr = azerrOld;
			}
		}

		int length = h3D[az - 1][el - 1].length;
		double[] pulse = new double[length];

		for (int i = 0; i < length; i++) {
			pulse[i] = h3D[az - 1][el - 1][i];
		}

		return new double[][] { pulse, new double[] { azerr },
				new double[] { elerr } };
	}

	private double pvaldeg(double angle) {
		double dtr = Math.PI / 180.0;
		angle = Math.atan2(Math.sin(angle * dtr), Math.cos(angle * dtr)) / dtr;
		if (angle < -90) {
			angle = angle + 360;
		}
		return angle;
	}

	public double hrir_l(int i, int j, int k) {
		return this.hrir_l[i - 1][j - 1][k - 1];
	}

	public double hrir_r(int i, int j, int k) {
		return this.hrir_r[i - 1][j - 1][k - 1];
	}

}