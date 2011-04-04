package acmus.auralization;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.OctaveDouble;

public class DirectionTest {

	/**
	 * General JavaOctave test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
		// OctaveDouble a = new OctaveDouble(new double[] { 1, 2, 3, 4 }, 2, 2);
		// octave.put("a", a);
		// String func = "" //
		// + "function res = my_func(a)\n" //
		// + " res = 2 * a;\n" //
		// + "endfunction\n" //
		// + "";
		// octave.eval(func);
		// octave.eval("b = my_func(a);");
		// OctaveDouble b = octave.get(OctaveDouble.class, "b");
		// octave.close();
		// System.out.println("length = " + b.getData().length);
		DirectionTest dir = new DirectionTest();

		dir.getPulse(30, 80);
	}

	/*
	 * TODO load ../standard_hrir_database/subject_003/hrir_final.mat
	 * getNearestUCDpulse(30,80, hrir_l)
	 */

	public void getPulse(double azimuth, double elevation) {
		OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
		getPulse(octave, azimuth, elevation);
		octave.close();
	}

	public void getPulse(OctaveEngine octave, double azimuth, double elevation) {
		OctaveDouble azim = new OctaveDouble(new double[] { azimuth }, 1, 1);
		OctaveDouble elev = new OctaveDouble(new double[] { elevation }, 1, 1);
		octave.put("azim", azim);
		octave.put("elev", elev);
		octave.eval("addpath('/home/acmus/migmruiz/')");
		octave.eval("load('/home/acmus/migmruiz/"
				+ "Desktop/CIPIC_hrtf_database/"
				+ "standard_hrir_database/subject_003/hrir_final.mat');");
		octave.eval("left = getNearestUCDpulse(azim, elev, hrir_l);");
		octave.eval("right = getNearestUCDpulse(azim, elev, hrir_r);");

		OctaveDouble left = octave.get(OctaveDouble.class, "left");
		OctaveDouble right = octave.get(OctaveDouble.class, "right");
		double[] rightPulse = right.getData();
		double[] leftPulse = left.getData();
				
		MultiBandSimulationViewer viewer = new MultiBandSimulationViewer();

		viewer.view(rightPulse, "Right pulse");
		viewer.view(leftPulse, "Left pulse");
		
		System.out.println("DONE");
	}

}
