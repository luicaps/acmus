package acmus.tools.structures;

import java.util.ArrayList;
import java.util.List;

public class ArbitraryAcousticSource implements AcousticSource {
	private Vector center;
	private AcousticSource auxiliarAcousticSource;
	private List<Vector> rays;
	
	public ArbitraryAcousticSource(Vector center,
			AcousticSource auxiliarAcousticSource) {
		this.center = center;
		this.auxiliarAcousticSource = auxiliarAcousticSource;
		this.rays = new ArrayList<Vector>();
	}
	
	public ArbitraryAcousticSource(Vector center){
		this(center, new MonteCarloAcousticSource(center));
	}
	
	public Vector getCenter() {
		return this.center;
	}
	
	public Vector generate() {
		return generate(1).get(0);
	}
	
	public List<Vector> generate(int n) {
		if (n > rays.size()) {	
			rays.addAll(auxiliarAcousticSource.generate(n - rays.size()));
		} else if (n < rays.size()) {
			for(int i = 0; i < rays.size() - n; i++){
				rays.remove(rays.size() - 1);
			}
		}
		return rays;
	}

	public void add(Vector ray){
		this.rays.add(ray);
	}
	
}
