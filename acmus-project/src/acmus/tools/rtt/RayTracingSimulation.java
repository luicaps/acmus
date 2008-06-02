package acmus.tools.rtt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.ProgressBar;

import acmus.tools.structures.NormalSector;
import acmus.tools.structures.Triade;

public class RayTracingSimulation {

	private List<Triade> vectors;
	private List<NormalSector> sectors;
	private Triade soundSource;
	private Triade sphericalReceptorCenter;
	private HashMap<Double, Double> sphericalReceptorHistogram;

	double sphericalReceptorRadius;
	double soundSpeed;
	double initialEnergy;
	double mCoeficient;
	double k;

	public RayTracingSimulation(List<NormalSector> sectors,
			List<Triade> vectors, Triade soundSourceCenter,
			Triade sphericalReceptorCenter, double sphericalReceptorRadius,
			double soundSpeed, double initialEnergy, double mCoeficient, int k) {
		this.sectors = sectors;
		this.vectors = vectors;
		this.soundSource = soundSourceCenter;
		this.sphericalReceptorCenter = sphericalReceptorCenter;
		this.sphericalReceptorRadius = sphericalReceptorRadius;
		this.soundSpeed = soundSpeed;
		this.initialEnergy = initialEnergy;
		this.mCoeficient = mCoeficient;
		this.k = k;

		sphericalReceptorHistogram = new HashMap<Double, Double>();
	}

	private void saveVectors() throws IOException {
		FileWriter fw = new FileWriter("/tmp/fonte3d.txt");

		for (int i = 0; i < vectors.size(); i++) {
			fw.write(vectors.get(i).toDat() + "\n");
		}

		// Para desenhar o grafico com o gnuplot
		// set size square
		// splot '/tmp/fonte3d.txt'
		fw.flush();
		fw.close();
	}

	public void simulate(final ProgressBar progressBar) {

		Triade q = soundSource;
		Triade g = null;
		Triade v;
		Triade nR = null;
		double e;
		double lMin = 0.0;
		double dMin = 0.0;
		double alpha = 0.0;
		double lReflection;
		int uhu = 0;

		// reflection
		for (int i = 0; i < vectors.size(); i++) {
			Triade vTemp = vectors.get(i);
			if (i % (vectors.size()/100) == 0) {
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
				lMin = 1.7E300; // this number is our MAX constant

				// verificacao de qual setor(parede) o raio incide
				for (NormalSector s : sectors) {
					// System.out.println("k#");

					if (v.produtoEscalar(s.normalVector) >= 0) {
						continue;
					} else {
						double d = s.normalVector.produtoEscalar(s.iPoint
								.sub(g));
						double l = -1 * d / (v.produtoEscalar(s.normalVector));

						// testa distancia minima da fonte a parede e ve
						// se eh
						// minima, dentre outras
						// paredes
						// este teste determina em qual parede o raio
						// "bate"
						if (l <= lMin) {
							lMin = l;
							dMin = d;
							alpha = s.absorbentCoeficient;
							nR = s.normalVector;
						}
					}
				}// fim setores
				q = g.sum(v.multiplicaVetorEscalar(lMin));
				double eTemp = e * (1 - alpha)
						* Math.pow(Math.E, -1 * mCoeficient * lMin);

				//
				// desenha o raio
				//

				//
				// verifica se este raio intercepta o receptor esferico
				// TODO corrigir estes calculos que estao errados, pois
				// ocorre
				// um caso em que
				// delta = 2 e na verdade o raio nao intercepta a esfera
				{
					Triade oc = g.sub(sphericalReceptorCenter);
					double l2oc = oc.produtoEscalar(oc);
					double tca = oc.produtoEscalar(v);

					// o raio intercepta o receptor esferico
					if (tca >= 0) {
						double t2hc = Math.pow(sphericalReceptorRadius, 2)
								- l2oc + Math.pow(tca, 2);
						if (t2hc > 0) {
							// System.out.println("INTERCEPTA");
							double lThisReflection = tca - Math.sqrt(t2hc);

							double distance = lReflection + lThisReflection;
							double time = distance / soundSpeed;
							double eSphere = e
									* (1 - alpha)
									* Math.pow(Math.E, -1 * mCoeficient
											* lThisReflection);
							if (sphericalReceptorHistogram.containsKey(time)) {
								double temp = sphericalReceptorHistogram
										.get(time);
								sphericalReceptorHistogram.put(time, temp
										+ eSphere);
								// System.out.println("t: " + time + "e:
								// " + temp
								// + eSphere);
							} else {
								sphericalReceptorHistogram.put(time, eSphere);
								// System.out.println("t: " + time + "e:
								// "
								// + eSphere);
							}
						}
					}
				}
				lReflection += lMin;
				e = eTemp;
				v = nR.multiplicaVetorEscalar(2 * dMin).sum(g.sub(q));
				v = v.multiplicaVetorEscalar(1 / v.modulo());// AGORA
				// TENHO
				// QUE
				// NORMALIZAR o
				// vetor V

			} while (e > (1 / k * initialEnergy)); // vai para a
			// proxima
			// reflexao, caso
			// a energia seja maior do que o criterio de parada

		}// fim for, vetores
		System.out.println("UHU: " + uhu);
	}

	public Map<Double, Double> getSphericalReceptorHistogram() {
		return sphericalReceptorHistogram;
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

	public void histogram() {
		double tMax = 0.0;
		double h1 = 0.0, h2 = 0.0, h3 = 0.0, h4 = 0.0, h5 = 0.0, h6 = 0.0;
		Iterator<Double> itr = sphericalReceptorHistogram.keySet().iterator();
		// controi histograma
		while (itr.hasNext()) {
			Double key = itr.next();
			if (key <= 0.01)
				h1 += sphericalReceptorHistogram.get(key);
			if (key >= 0.01 && key <= 0.02)
				h2 += sphericalReceptorHistogram.get(key);
			if (key >= 0.02 && key <= 0.03)
				h3 += sphericalReceptorHistogram.get(key);
			if (key >= 0.03 && key <= 0.04)
				h4 += sphericalReceptorHistogram.get(key);
			if (key >= 0.04 && key <= 0.05)
				h5 += sphericalReceptorHistogram.get(key);
			if (key >= 0.05)
				h6 += sphericalReceptorHistogram.get(key);

			tMax = sphericalReceptorHistogram.get(key);
		}

		System.out.println("0,01 : " + h1 / tMax);
		System.out.println("0,02 : " + h2 / tMax);
		System.out.println("0,03 : " + h3 / tMax);
		System.out.println("0,04 : " + h4 / tMax);
		System.out.println("0,05 : " + h5 / tMax);
		System.out.println("0,06 : " + h6 / tMax);

	}

}
