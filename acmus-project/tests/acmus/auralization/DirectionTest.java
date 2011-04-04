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
		OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
		OctaveDouble a = new OctaveDouble(new double[] { 1, 2, 3, 4 }, 2, 2);
		octave.put("a", a);
		String func = "" //
				+ "function res = my_func(a)\n" //
				+ " res = 2 * a;\n" //
				+ "endfunction\n" //
				+ "";
		octave.eval(func);
		octave.eval("b = my_func(a);");
		OctaveDouble b = octave.get(OctaveDouble.class, "b");
		octave.close();
		System.out.println("length = " + b.getData().length);
	}
	
	/* TODO
	 load ../standard_hrir_database/subject_003/hrir_final.mat
	 getNearestUCDpulse(30,80, hrir_l)
	 */

}
