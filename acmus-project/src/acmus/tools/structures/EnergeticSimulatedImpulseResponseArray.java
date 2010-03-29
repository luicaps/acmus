package acmus.tools.structures;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public final class EnergeticSimulatedImpulseResponseArray implements SimulatedImpulseResponse {

	private float[] impulseResposeHistogram;
	private float interval;
	
	public EnergeticSimulatedImpulseResponseArray(float interval) {
		if(interval < 0.0000001)
			throw new InvalidParameterException("interval CAN'T be less than 0.0000001");
		
		this.interval = interval;
		impulseResposeHistogram = new float[188200]; //para 44100 hz;
	}
	
	public Map<Float, Float> getEnergeticImpulseResponse() {
//		Map<Float, Float> ir = new HashMap<Float, Float>();
//		for(Map.Entry<Integer, Float> e: impulseResposeHistogram.entrySet()){
//			ir.put(e.getKey() * interval, e.getValue());
//		}
//		for(int i=0; i<impulseResposeHistogram.length; i++){
//			ir.put(key, value)
//		return ir;
		return null;
	}

	public float[] getEIR() {
		return impulseResposeHistogram;
	}

	public void addValue(float time, float energy) {
		if(time < 0.0f || energy < 0.0f)
			throw new InvalidParameterException("time or energy less than ZERO");
		
		impulseResposeHistogram[(int) Math.ceil(time * 44100)] += energy; 
	}

}
