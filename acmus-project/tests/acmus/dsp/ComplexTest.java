package acmus.dsp;

import static org.junit.Assert.*;

import org.junit.Test;

public class ComplexTest {

	private Complex z;
	private Complex w;
	
	public ComplexTest() {
		z = new Complex(5.0, 6.0);
		w = new Complex(-3.0, 4.0);
	}
	
	@Test
	public void testAbs() {
		double result = 7.810;
		double error  = 0.001;
		assertEquals(result, z.abs(), error);		
	}

	@Test
	public void testPhase() {		
		fail("Not yet implemented");
	}

	@Test
	public void testPlusComplex() {
		Complex c = z.plus(w);
		assertEquals("2.0 + 10.0i", c.toString());		
	}

	@Test
	public void testMinus() {
		Complex c = z.minus(w);
		assertEquals("8.0 + 2.0i", c.toString());
	}

	@Test
	public void testTimesComplex() {
		fail("Not yet implemented");
	}

	@Test
	public void testTimesDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testConjugate() {
		Complex c = z.conjugate();
		assertEquals("5.0 - 6.0i", c.toString());
	}

	@Test
	public void testReciprocal() {
		fail("Not yet implemented");
	}

	@Test
	public void testDivides() {
		fail("Not yet implemented");
	}

	@Test
	public void testExp() {
		fail("Not yet implemented");
	}

	@Test
	public void testSin() {
		fail("Not yet implemented");
	}

	@Test
	public void testCos() {
		fail("Not yet implemented");
	}

	@Test
	public void testTan() {
		fail("Not yet implemented");
	}

	@Test
	public void testPlusComplexComplex() {
		fail("Not yet implemented");
	}

}
