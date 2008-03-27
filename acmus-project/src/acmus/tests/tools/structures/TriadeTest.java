package acmus.tests.tools.structures;

import junit.framework.TestCase;
import acmus.tools.structures.Triade;

public class TriadeTest extends TestCase {

	double x1, y1, z1;
	Triade t1;
	static double eps = 0.00001;
	
	protected void setUp() throws Exception {
		super.setUp();
		x1 = 10.56;
		y1 = 12.80;
		z1 = 1.20;
		t1 = new Triade(x1, y1, z1);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.Triade()'
	 */
	public void testTriade() {

		
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.Triade(double, double, double)'
	 */
	public void testTriadeDoubleDoubleDouble() {

		String str = "(10.56, 12.8, 1.2)";
		
		assertNotNull(t1);
		assertTrue(t1.equals(new Triade(this.x1, this.y1, this.z1)));
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.getX()'
	 */
	public void testGetX() {
		assertEquals(t1.getX(), x1);
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.getY()'
	 */
	public void testGetY() {
		assertEquals(t1.getY(), y1);
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.getZ()'
	 */
	public void testGetZ() {
		assertEquals(t1.getZ(), z1);
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.getNome()'
	 */
	public void testGetNome() {
		Triade t = new Triade(0.0, 0.0, 0.0);
		t.setNome("ponto1");
		assertEquals("ponto1", t.getNome());
		
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.setX(double)'
	 */
	public void testSetX() {
		t1.setX(8.8);
		assertEquals(t1.getX(), 8.8);
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.setY(double)'
	 */
	public void testSetY() {
		t1.setY(5.6);
		assertEquals(t1.getY(), 5.6);
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.setZ(double)'
	 */
	public void testSetZ() {
		t1.setZ(3.42);
		assertEquals(t1.getZ(), 3.42);
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.setNome(String)'
	 */
	public void testSetNome() {
		Triade t = new Triade(0.0, 0.0, 0.0);
		t.setNome("ponto1");
		
		assertEquals("ponto1", t.getNome());
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.sub(Triade, Triade)'
	 */
	public void testSub() {
		Triade w = new Triade(1.0, 1.0, 1.0);
		Triade v = new Triade(3.0, 1.0, -2.0);
		Triade r = new Triade(2.0, 0.0, -3.0);
		
		assertTrue(Triade.sub(w,v).equals(r));

	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.sum(Triade, Triade)'
	 */
	public void testSum() {
		Triade w = new Triade(1.2, 2.3, 4.560);
		Triade v = new Triade(2.1, 1.23, 3.220);

		Triade r = new Triade(2.1+1.2, 2.3+1.23, 4.56+3.22);
		
		assertTrue(Triade.sum(w,v).equals(r)); 
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.produtoVetorial(Triade, Triade)'
	 */
	public void testProdutoVetorial() {
		Triade w = new Triade(1.2, 2.3, 4.56);
		Triade v = new Triade(2.1, 1.23, 3.22);
		Triade r = new Triade(1.7972, 5.712, -3.354);

		assertTrue(Triade.produtoVetorial(w,v).equals(r));

	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.produtoEscalar(Triade, Triade)'
	 */
	public void testProdutoEscalar() {
		Triade w = new Triade(1.2, 2.3, 4.56);
		Triade v = new Triade(2.1, 1.23, 3.22);
		double r = 20.0322;

		assertEquals(Triade.produtoEscalar(v, w), r);
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.modulo(Triade)'
	 */
	public void testModulo() {
		double x = 1.2, y = 2.3, z = 4.56, r;
		
		Triade w = new Triade(x, y, z);
		r = Math.sqrt(x*x + y*y + z*z);
		
		assertEquals(Triade.modulo(w), r);
		
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.anguloVetores(Triade, Triade)'
	 */
	public void testAnguloVetores() {
		Triade v1 = new Triade(0, 1, 0);
		Triade w1 = new Triade(0, 0, 1);
		Triade w2 = new Triade(0, 1, 1);
		                                     
		double r1 = 1.57079632, r2 = 0.78539816;
		
		assertTrue(Math.abs(Triade.anguloVetores(v1,w1) - r1) < TriadeTest.eps);
		assertTrue(Math.abs(Triade.anguloVetores(v1, w2) - r2) < TriadeTest.eps);
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.divideVetorEscalar(Triade, double)'
	 */
	public void testDivideVetorEscalar() {
		Triade t = new Triade(10, 12.4, 25.6);
		double escalar = 2.0;
		Triade r = new Triade(5.0, 6.2, 12.8);

		// poruq esta acontecendo isso?
//		boolean a = Triade.divideVetorEscalar(t,escalar).equals(r);
//		assertTrue(Triade.divideVetorEscalar(t,escalar).equals(r));
		boolean a = Triade.divideVetorEscalar(t,escalar).equals(r);
		assertTrue(a);
		
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.multiplicaVetorEscalar(Triade, double)'
	 */
	public void testMultiplicaVetorEscalar() {
		Triade v = new Triade(2.5, 3.4, 4.5);
		double escalar = 2.0;
		Triade w = new Triade(5, 6.8, 9.0);
		assertTrue(Triade.multiplicaVetorEscalar(v, escalar).equals(w));
		
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.pontoIntersecaoReta(Triade, Triade, Triade, Triade)'
	 */
	public void testPontoIntersecaoReta() {
		Triade p1 = new Triade(0, 0, 0);
		Triade p2 = new Triade(3, 0, 0);
		Triade p3 = new Triade(0, 3, 0);
		Triade p4 = new Triade(0, 0, 5);
		
		System.out.println(Triade.pontoIntersecaoReta(p1, p2, p2, p3));
	}

	/*
	 * Test method for 'acmus.tools.structures.Triade.imprime()'
	 */
	public void testImprime() {
		Triade t = new Triade(0.123, 123.45, 456.886);
		t.setNome("p1");
		
		assertEquals("Triade: p1 (0.123, 123.45, 456.886)", t.imprime());
	}

}
