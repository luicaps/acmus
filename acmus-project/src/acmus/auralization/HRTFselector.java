package acmus.auralization;

public interface HRTFselector {
	
	/**
	 * 
	 * @param azimuth
	 * @param elevation
	 * @return double[][] result = dir.getPulse(azimuth, elevation); double[]
	 *         leftPulse = result[0]; double[] rightPulse = result[1];
	 */
	public double[][] getPulse(double azimuth, double elevation);
}
