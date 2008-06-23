package acmus.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import acmus.util.MathUtils;
public class MathUtilsTest {

	@Test
	public void testCalculateLogsProperly() throws Exception {
		assertEquals(4, MathUtils.log10(10000), 0.1);
		assertEquals(4, MathUtils.log2(16), 0.1);
		assertEquals(3, MathUtils.log(27, 3), 0.1);
		double[] expected = new double[] {1, 2};
		double[] test = new double[] {10, 100};
		assertArrayEquals(expected, MathUtils.log10(test, new double[2]), 0.1);
		assertArrayEquals(expected, MathUtils.log10(test), 0.1);
		assertArrayEquals(expected, MathUtils.log10LLL(test), 0.1);
		
	}

	@Test
	public void testCalculateComplexDivision() throws Exception {
		double[] aRe = new double[] {1, 2};
		double[] aIm = new double[] {1, 2};
		double[] bRe = new double[] {1, 2};
		double[] bIm = new double[] {1, 2};
		
		MathUtils.complexDivision(aRe, aIm, bRe, bIm);
		assertEquals(1, aRe[0], 0.1);
		assertEquals(1, aRe[1], 0.1);
	}
	public static void assertArrayEquals(double[] d1, double[] d2, double delta) {
		assertEquals(d1.length, d2.length);
		for (int i = 0; i < d2.length; i++) {
			assertEquals(d1[i], d2[i], delta);
		}
	}
}
