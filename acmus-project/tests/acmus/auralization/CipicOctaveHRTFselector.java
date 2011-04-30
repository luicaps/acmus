package acmus.auralization;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.OctaveDouble;

public class CipicOctaveHRTFselector implements HRTFselector {

	private OctaveEngine octave;

	public CipicOctaveHRTFselector() {
		this("data/CIPIC_hrtf_database/standard_hrir_database/"
				+ "subject_003/hrir_final.mat");
	}

	public CipicOctaveHRTFselector(String path) {
		this.octave = new OctaveEngineFactory().getScriptEngine();
		octave.eval("addpath('resources/cipicConnect/')");
		octave.eval("load('" + path + "');");
	}

	/**
	 * 
	 * @param azimuth
	 * @param elevation
	 * @return double[][] result = dir.getPulse(azimuth, elevation); double[]
	 *         leftPulse = result[0]; double[] rightPulse = result[1];
	 */
	public double[][] getPulse(double azimuth, double elevation) {
		OctaveDouble azim = new OctaveDouble(new double[] { azimuth }, 1, 1);
		OctaveDouble elev = new OctaveDouble(new double[] { elevation }, 1, 1);
		octave.put("azim", azim);
		octave.put("elev", elev);
		octave.eval("left = getNearestUCDpulse(azim, elev, hrir_l);");
		octave.eval("right = getNearestUCDpulse(azim, elev, hrir_r);");

		OctaveDouble left = octave.get(OctaveDouble.class, "left");
		OctaveDouble right = octave.get(OctaveDouble.class, "right");

		return new double[][] { left.getData(), right.getData() };

	}

	@Override
	protected void finalize() throws Throwable {
		octave.close();
		super.finalize();
	}
}
