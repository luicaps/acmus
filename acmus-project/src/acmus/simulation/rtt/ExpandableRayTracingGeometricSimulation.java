package acmus.simulation.rtt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * unused imports ...
 * import java.util.Iterator;
 */

import org.eclipse.swt.widgets.ProgressBar;

import acmus.simulation.GeometricAcousticSimulation;
import acmus.simulation.SimulatedImpulseResponse;
import acmus.util.math.Vector;

public class ExpandableRayTracingGeometricSimulation implements GeometricAcousticSimulation {

	private List<Vector> vectors;
	private List<Sector> sectors;
	private Vector soundSource;
	private HashMap<Double, Double> sphericalReceptorHistogram;
	/*
	 * unused variables ...
	 * private Vector sphericalReceptorCenter;
	 */

	double sphericalReceptorRadius;
	double soundSpeed;
	double initialEnergy;
	double mCoeficient;
	double k;

	public ExpandableRayTracingGeometricSimulation(List<Sector> sectors,
			List<Vector> vectors, Vector soundSourceCenter,
			Vector sphericalReceptorCenter, double sphericalReceptorRadius,
			double soundSpeed, double initialEnergy, double mCoeficient, int k) {
		this.sectors = sectors;
		this.vectors = vectors;
		this.soundSource = soundSourceCenter;
		// this.sphericalReceptorCenter = sphericalReceptorCenter;
		this.sphericalReceptorRadius = sphericalReceptorRadius;
		this.soundSpeed = soundSpeed;
		this.initialEnergy = initialEnergy;
		this.mCoeficient = mCoeficient;
		this.k = k;

		sphericalReceptorHistogram = new HashMap<Double, Double>();
	}

	public void simulate(final ProgressBar progressBar) {

		Vector q = soundSource;
		Vector g = null;
		Vector v;
		Vector nR = null;
		double e;
		float lMin = 0.0f;
		float dMin = 0.0f;
		double alpha = 0.0;
		double lReflection;
		int uhu = 0;

		// reflection
		for (int i = 0; i < vectors.size(); i++) {
			Vector vTemp = vectors.get(i);
			if (i % Math.max(1,(vectors.size()/100)) == 0) {
				progressBar.setSelection((int) (100.0*i/vectors.size()));
			}
			q = soundSource;
			v = vTemp;
			e = initialEnergy;
			lReflection = 0; // acumulador de distancia percorrida
			// pelo raio
			// reflexoes do raio
			// teste de qual direcao o raio vai seguir
			do {
				uhu++;
				// notar que V jah estah normalizado
				g = q;
				// correcao no raio...
				lMin = Float.MAX_VALUE;

				// verificacao de qual setor(parede) o raio incide
				for (Sector s : sectors) {
					// System.out.println("k#");

					if (v.dotProduct(s.normalVector) >= 0) {
						continue;
					} else {
						float d = s.normalVector.dotProduct(g.sub(s.iPoint));
						float l = -1 * d / (v.dotProduct(s.normalVector));

						// testa distancia minima da fonte a parede e ve
						// se eh
						// minima, dentre outras
						// paredes
						// este teste determina em qual parede o raio
						// "bate"
						if (l <= lMin) {
							lMin = l;
							dMin = d;
							alpha = s.absorptionCoeficient;
							nR = s.normalVector;
						}
					}
				}// fim setores
				q = g.add(v.scale(lMin));
				double eTemp = e * (1 - alpha)
						* Math.pow(Math.E, -1 * mCoeficient * lMin);

				// verifica se raio bate no receptor COLOCAR O CODIGO AQUI
				
				lReflection += lMin;
				e = eTemp;
				v = nR.scale(2 * dMin).add(q.sub(g));
				v = v.normalized();
			} while (e > (1 / k * initialEnergy)); // vai para a
			// proxima
			// reflexao, caso
			// a energia seja maior do que o criterio de parada

		}// fim for, vetores
		System.out.println("UHU: " + uhu);
	}

	public SimulatedImpulseResponse getSimulatedImpulseResponse() {
//		return sphericalReceptorHistogram;
		return null;
	}

	public void lista() throws IOException {
		FileWriter fw = new FileWriter("/tmp/hist.txt");
		StringBuilder sx = new StringBuilder(2000);
		StringBuilder sy = new StringBuilder(2000);
		StringBuilder ss = new StringBuilder(2000);

		for (Map.Entry<Double, Double> e : sphericalReceptorHistogram
				.entrySet()) {
			sx.append(e.getKey());
			sx.append(" ");
			sy.append(e.getValue());
			sy.append(" ");

			ss.append(e.getKey());
			ss.append("\t");
			ss.append(e.getValue());
			ss.append("\n");

		}
		// fw.write("x=[" + sx.toString() + "0]; y=[" + sy.toString() + "0]");
		fw.write(ss.toString());
		fw.close();
	}
}
