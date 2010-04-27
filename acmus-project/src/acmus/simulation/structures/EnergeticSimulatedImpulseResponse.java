package acmus.simulation.structures;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import acmus.simulation.SimulatedImpulseResponse;

public final class EnergeticSimulatedImpulseResponse implements SimulatedImpulseResponse {

	private ConcurrentHashMap<Integer, Float> impulseResposeHistogram;
	private float interval;
	// private float[] impulseResposeHistogramArray;
	
	public EnergeticSimulatedImpulseResponse(float interval) {
		if(interval < 0.0000001)
			throw new InvalidParameterException("interval CAN'T be less than 0.0000001");
		
		this.interval = interval;
		impulseResposeHistogram = new ConcurrentHashMap<Integer, Float>();
		
		// impulseResposeHistogramArray = new float[188200]; // to 44100 hz;
	}
	
	public Map<Float, Float> getEnergeticImpulseResponse() {
		Map<Float, Float> ir = new HashMap<Float, Float>();
		for(Map.Entry<Integer, Float> e: impulseResposeHistogram.entrySet()){
			ir.put(e.getKey() * interval, e.getValue());
		}
		return ir;
	}
	
	public void addValue(float time, float energy) {
		if (time < 0.0f || energy < 0.0f) {
			if (time < 0.0f) {
				throw new InvalidParameterException("time less than ZERO");
			} else {
				throw new InvalidParameterException("energy less than ZERO");
			}
		}
		
		int position = (int) Math.ceil(time/interval);

		Float storedEnergy;
		if ((storedEnergy = impulseResposeHistogram.putIfAbsent(position,
				energy)) != null) {
			while (!impulseResposeHistogram.replace(position, storedEnergy,
					storedEnergy + energy)){
				storedEnergy = impulseResposeHistogram.get(position);
			}
		}
		
		// Check before re-implement: ArrayIndexOutOfBoundsException
		// impulseResposeHistogramArray[(int) Math.ceil(time * 44100)] += energy;
	}

//	public float[] getEIR() {
//		return impulseResposeHistogramArray;
//	}

}
