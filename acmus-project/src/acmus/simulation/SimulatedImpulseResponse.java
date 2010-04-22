package acmus.simulation;

import java.util.Map;

public interface SimulatedImpulseResponse {
	
	/**
	 * Adds a value to the Simulates Impulse Response
	 * @param time
	 * @param value
	 */
	public void addValue(float time, float value);
	
	/**
	 * @return a normalized Impulse Response
	 */
	public Map<Float, Float> getEnergeticImpulseResponse();
	
	public float[] getEIR();
}
