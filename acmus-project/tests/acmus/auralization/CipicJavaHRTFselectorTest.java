package acmus.auralization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CipicJavaHRTFselectorTest {
	
	CipicJavaHRTFselector sel;
	
	@Before
	public void load() {
		sel = new CipicJavaHRTFselector();
		
		Assert.assertNotNull(sel);
		
//		System.out.println("\n" + "first value of hrir_l: " + sel.hrir_l(1,1,1));
//		System.out.println("value of hrir_l(25,1,1): " + sel.hrir_l(25,1,1));
//		
//		System.out.println("\n" + "first value of hrir_r: " + sel.hrir_r(1,1,1));
//		System.out.println("value of hrir_r(25,1,1): " + sel.hrir_r(25,1,1));
	}
	
	@Test
	public void pulseOctaveTest() {
		double[][] octavePulse, javaPulse;
		CipicOctaveHRTFselector octSel = new CipicOctaveHRTFselector();
		double rnd, azimuth, elevation;
		for (int i = -9; i < 10; i++) {
			rnd = Math.random();
			azimuth = 9.9*i; // ~ from -90 to 90
			elevation = 20*rnd*i + 90.0; // from -90 to 270 
			octavePulse = octSel.getPulse(azimuth, elevation);
			javaPulse = sel.getPulse(azimuth, elevation);
			Assert.assertEquals(javaPulse.length, octavePulse.length);
			Assert.assertEquals(javaPulse[0].length, octavePulse[0].length);
			Assert.assertEquals(javaPulse[1].length, octavePulse[1].length);
			Assert.assertArrayEquals(octavePulse[0], javaPulse[0], 0.00001);
			Assert.assertArrayEquals(octavePulse[1], javaPulse[1], 0.00001);
		}
	}
}
