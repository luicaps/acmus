package acmus.tools.structures;

import java.util.Map;

public interface SimulatedImpulseResponse {
	
	/**
	 * Adds a value to the Simulates Impulse Response
	 * @param time
	 * @param value
	 */
	public void addValue(float time, float value);
	
	/**
	 * returns a normalized Impulse Response
	 * @return
	 */
	public Map<Float, Float> getEnergeticImpulseResponse();
	
	public float[] getEIR();
}
