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

//		System.out.println("starting left hrir...");
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
//			System.out.println("loading a matrix with size " + dim1 + " "
//					+ dim2 + " " + dim3);
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
//		System.out.println("finished left hrir.");

//		System.out.println("starting right hrir...");
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
//			System.out.println("loading a matrix with size " + dim1 + " "
//					+ dim2 + " " + dim3);
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
//		System.out.println("finished right hrir");
	}

	@Override
	public double[][] getPulse(double azimuth, double elevation) {

		return new double[][] {
				getNearestUCDpulse(azimuth, elevation, hrir_l)[0],
				getNearestUCDpulse(azimuth, elevation, hrir_r)[0] };
	}

	private double[][] getNearestUCDpulse(double azimuth, double elevation,
			double[][][] h3D) {
		/*
		function [pulse, azerr, elerr] = getNearestUCDpulse(azimuth, elevation, h3D);
		% [pulse, azerr, elerr] = getNearestUCDpulse(azimuth, elevation, h3D);
		%
		% retrieves the impulse response from h3D that is closest to the
		% specified azimuth and elevation (in degrees);
		
		if nargin < 1,
			fprintf('Format: [pulse, azerr, elerr] = getNearestUCDpulse(azimuth, elevation, h3D)\n');
			return;
		end;
		
		azimuth = pvaldeg(azimuth);
		if (azimuth < -90) | (azimuth > 90),
			error('invalid azimith');
		end;
		elevation = pvaldeg(elevation);
		
		elmax = 50;
		elindices = 1:elmax;
		elevations = -45 + 5.625*(elindices - 1);
		el = round((elevation+45)/5.625 + 1);
		el = max(el,1);
		el = min(el,elmax);
		elerr = pvaldeg(elevation - elevations(el));
		
		azimuths = [-80 -65 -55 -45:5:45 55 65 80];
		[azerr, az] = min(abs(pvaldeg(abs(azimuths - azimuth))));
		
		pulse = squeeze(h3D(az,el,:));

		 */

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

		@SuppressWarnings("unused")
		int[] azimuths = new int[] { -80, -65, -55, -45, -40, -35, -30, -25,
				-20, -15, -10, -5, 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 55,
				65, 80 };
		// [azerr, az] = min(abs(pvaldeg(abs(azimuths - azimuth))));
		// TODO translate from octave
		// double pulse = squeeze(h3D[az][el][:]);

		return new double[][] { null /* pulse */,
				null /*new double[] { azerr }*/,
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
