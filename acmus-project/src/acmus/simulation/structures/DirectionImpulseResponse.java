package acmus.simulation.structures;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import acmus.util.math.Vector;

/**
 * Class that performs check and storage operations for direction
 * 
 * @author migmruiz
 * 
 */
public final class DirectionImpulseResponse {

	private ConcurrentHashMap<Integer, Vector> impulseResposeHistogram;
	private float interval;

	public DirectionImpulseResponse(float interval) {
		if (interval < 0.0000001)
			throw new InvalidParameterException(
					"interval CAN'T be less than 0.0000001");

		this.interval = interval;
		impulseResposeHistogram = new ConcurrentHashMap<Integer, Vector>();
	}

	public Map<Float, Vector> getEnergeticImpulseResponse() {
		Map<Float, Vector> ir = new HashMap<Float, Vector>();
		for (Map.Entry<Integer, Vector> d : impulseResposeHistogram.entrySet()) {
			ir.put(d.getKey() * interval, d.getValue());
		}
		return ir;
	}

	public void addValue(float time, Vector direction) {
		if (time < 0.0f) {
			throw new InvalidParameterException("time less than ZERO");
		}

		int position = (int) Math.ceil(time / interval);

		Vector storedDirection = impulseResposeHistogram.putIfAbsent(position,
				direction);
		if (storedDirection != null) {
			while (!impulseResposeHistogram.replace(position, storedDirection,
					storedDirection.add(direction))) {
				storedDirection = impulseResposeHistogram.get(position);
			}
		}
	}

	public float getInterval() {
		return this.interval;
	}

}
