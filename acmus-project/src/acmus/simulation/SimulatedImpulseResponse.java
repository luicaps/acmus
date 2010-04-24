package acmus.simulation;

import java.util.Map;

/**
 * This interface is used to provide a uniform way to the
 * GeometricAcousticSimulation use the Strategy method, to put partial results
 * in an impulse response
 * 
 * @author mahtorres
 * 
 */
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
	
	// public float[] getEIR();
}
