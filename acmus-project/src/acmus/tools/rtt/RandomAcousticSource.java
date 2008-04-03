package acmus.tools.rtt;

import java.util.ArrayList;
import java.util.List;

import acmus.tools.structures.Triade;

/**
 * Random Acoustic Source, generated using uniform random sample
 * 
 * This class is used by the Strategy implemented in RayTracingDelegate
 * 
 * @author mahtorres
 * 
 */
public class RandomAcousticSource implements AcousticSource {

	List<Triade> sphericalPoints;
	double x, y, z;

	public List<Triade> generate() {
		return this.generate(20000);
	}

	public List<Triade> generate(int n) {
		sphericalPoints = new ArrayList<Triade>();

		double x0 = 0.0, x1 = 0.0, x2 = 0.0, x3 = 0.0;

		while (sphericalPoints.size() < n) {
			x0 = uniformeUmMenosUm();
			x1 = uniformeUmMenosUm();
			x2 = uniformeUmMenosUm();
			x3 = uniformeUmMenosUm();

			if (Math.sqrt(squareNorma(x0, x1, x2, x3)) < 1)
				sphericalPoints.add(this.quaternionToCartesian(x0, x1, x2, x3));
		}

		return sphericalPoints;
	}

	private double squareNorma(double x0, double x1, double x2, double x3) {
		return Math.pow(x0, 2) + Math.pow(x1, 2) + Math.pow(x2, 2)
				+ Math.pow(x3, 2);
	}

	private double uniformeUmMenosUm() {
		return 2 * Math.random() - 1;
	}

	private Triade quaternionToCartesian(double x0, double x1, double x2,
			double x3) {
		double divisor = squareNorma(x0, x1, x2, x3);

		double x = 2 * (x1 * x3 + x0 * x2) / divisor;

		double y = 2 * (x2 * x3 - x0 * x1) / divisor;

		double z = 2
				* (Math.pow(x0, 2) + Math.pow(x3, 2) - Math.pow(x1, 2) - Math
						.pow(x2, 2)) / divisor;

		Triade temp = new Triade(x, y, z);
		return new Triade(x / temp.modulo(), y / temp.modulo(), z
				/ temp.modulo());
	}

}
