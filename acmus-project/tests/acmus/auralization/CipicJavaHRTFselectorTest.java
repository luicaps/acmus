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
		double rnd;
		for (int i = -9; i < 10; i++) {
			rnd = Math.random();
			octavePulse = octSel.getPulse(5*i, 9*rnd*i);
			javaPulse = sel.getPulse(5*i, 9*rnd*i);
			Assert.assertEquals(javaPulse.length, octavePulse.length);
			Assert.assertEquals(javaPulse[0].length, octavePulse[0].length);
			Assert.assertEquals(javaPulse[1].length, octavePulse[1].length);
			Assert.assertArrayEquals(octavePulse[0], javaPulse[0], 0.001);
			Assert.assertArrayEquals(octavePulse[1], javaPulse[1], 0.001);
		}
	}
}
