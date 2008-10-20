package acmus.tools.rtt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.ProgressBar;

import acmus.tools.structures.NormalSector;
import acmus.tools.structures.Vector;

public class RayTracingGeometricAcousticSimulationImpl implements GeometricAcousticSimulation {

	private List<Vector> vectors;
	private List<NormalSector> sectors;
	private Vector soundSource;
	private Vector sphericalReceptorCenter;
	private HashMap<Double, Double> sphericalReceptorHistogram;

	double sphericalReceptorRadius;
	double soundSpeed;
	double initialEnergy;
	double mCoeficient;
	double k;
	
	private final static float EPS = 0.0000001f; 

	public RayTracingGeometricAcousticSimulationImpl(List<NormalSector> sectors,
			List<Vector> vectors, Vector soundSourceCenter,
			Vector sphericalReceptorCenter, double sphericalReceptorRadius,
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

		// ray tracing of rays generated by sound source
		int i = 0;
		nextRay:
		for (; i < vectors.size(); i++) {
			Vector vTemp = vectors.get(i);
			if (i % Math.max(1,(vectors.size()/100)) == 0) {
				progressBar.setSelection((int) (100.0*i/vectors.size()));
			}
			q = soundSource;
			v = vTemp;
			e = initialEnergy;
			lReflection = 0; //ray length
			
			do {
				g = q;
				lMin = Float.MAX_VALUE;

				/**
				 *  verify the interception section
				 */
				for (NormalSector s : sectors) {
					if (v.dotProduct(s.normalVector) >= 0) {
						continue;
					} else {
						float d = s.normalVector.dotProduct(g.sub(s.iPoint));
						float l = -1 * d / (v.dotProduct(s.normalVector));

						// put the point in polygon test
						if (l <= lMin) {
							lMin = l;
							dMin = d;
							alpha = s.absorptionCoeficient;
							nR = s.normalVector;
						}
					}
				}// end sectors
				
				q = g.add(v.times(lMin)); //interception point 
				double eTemp = e * (1 - alpha) * Math.pow(Math.E, -1 * mCoeficient * lMin);

				/*
				 * ray receptor interception test
				 */
				{
					Vector oc = sphericalReceptorCenter.sub(g);
					double l2oc = oc.dotProduct(oc);
					double tca = oc.dotProduct(v);

					if (tca >= 0) { 
						double t2hc = sphericalReceptorRadius * sphericalReceptorRadius - l2oc + tca * tca;

						if (t2hc > 0) { // ray V intercepts spherical receptor
							double lThisReflection = tca - Math.sqrt(t2hc);
							double distance = lReflection + lThisReflection;
							double time = distance / soundSpeed;
							double eSphere = e * Math.pow(Math.E, -1 * mCoeficient * lThisReflection);

							if (sphericalReceptorHistogram.containsKey(time)) {
								double temp = sphericalReceptorHistogram.get(time);
								sphericalReceptorHistogram.put(time, temp + eSphere);
							} else {
								sphericalReceptorHistogram.put(time, eSphere);
							}
						continue nextRay;
						}
					}
				}
				
				lReflection += lMin;
				e = eTemp;
				v = nR.times(2 * dMin).add(q.sub(g));
				v = v.normalize(); //new ray direction
				
			} while (e > (1 / k * initialEnergy)); //ray energy threshold
		}// ends tracing of rays
	}

	public Map<Double, Double> getReceptorHistogram() {
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
}
