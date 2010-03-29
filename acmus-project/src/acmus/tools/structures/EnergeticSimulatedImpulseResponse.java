package acmus.tools.structures;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public final class EnergeticSimulatedImpulseResponse implements SimulatedImpulseResponse {

	private Map<Integer, Float> impulseResposeHistogram;
	private float interval;
	
	public EnergeticSimulatedImpulseResponse(float interval) {
		if(interval < 0.0000001)
			throw new InvalidParameterException("interval CAN'T be less than 0.0000001");
		
		this.interval = interval;
		impulseResposeHistogram = new HashMap<Integer, Float>();
	}
	
	public Map<Float, Float> getEnergeticImpulseResponse() {
		Map<Float, Float> ir = new HashMap<Float, Float>();
		for(Map.Entry<Integer, Float> e: impulseResposeHistogram.entrySet()){
			ir.put(e.getKey() * interval, e.getValue());
		}
		return ir;
	}

	public void addValue(float time, float energy) {
		if(time < 0.0f || energy < 0.0f)
			throw new InvalidParameterException("time or energy less than ZERO");
		
		int position = (int) Math.ceil(time/interval);

		Float storedEnergy = 0.0f;
		if((storedEnergy = impulseResposeHistogram.get(position)) == null)
			impulseResposeHistogram.put(position, energy);
		else
			impulseResposeHistogram.put(position, storedEnergy + energy);
	}

	public float[] getEIR() {
		// TODO Auto-generated method stub
		return null;
	}

}
