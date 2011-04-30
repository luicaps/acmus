package acmus.simulation.structures;

import java.util.ArrayList;
import java.util.List;

import acmus.simulation.AcousticSource;
import acmus.simulation.rtt.Ray;
import acmus.util.math.Vector;

public class ArbitraryAcousticSource implements AcousticSource {
	private Vector center;
	private AcousticSource auxiliarAcousticSource;
	private List<Vector> directions;
	private double energy;
	private int cont;
	
	public ArbitraryAcousticSource(Vector center,
			AcousticSource auxiliarAcousticSource) {
		this.center = center;
		this.auxiliarAcousticSource = auxiliarAcousticSource;
		this.directions = new ArrayList<Vector>();
		this.energy = 1.0;
		this.cont = 0;
	}
	
	public ArbitraryAcousticSource(Vector center){
		this(center, new MonteCarloAcousticSource(center));
	}
	
	public Vector getCenter() {
		return new Vector(this.center);
	}
	
	public double getEnergy() {
		return this.energy;
	}
	
	public Ray generate() {
		return new Ray(energy, getCenter(), direction());
	}

	public List<Ray> generate(int n) {
		List<Vector> directions = manyDirections(n);
		List<Ray> rays = new ArrayList<Ray>(n);
		for(int i = 0; i < n; i++){
			rays.add(new Ray(energy, getCenter(), directions.get(i)));
		}
		return rays;
	}
	
	public Vector direction() {
		return directions.get(cont++);
	}
	
	public List<Vector> manyDirections(int n) {
		if (n > directions.size()) {	
			directions.addAll(auxiliarAcousticSource.manyDirections(n - directions.size()));
		} else if (n < directions.size()) {
			for(int i = 0; i < directions.size() - n; i++){
				directions.remove(directions.size() - 1);
			}
		}
		return new ArrayList<Vector>(directions);
	}

	public void add(Vector ray){
		this.directions.add(ray);
	}

}
