package acmus.tools.structures;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VectorTest {

	float x1, y1, z1;
	Vector t1;

	@Before
	public void setUp() throws Exception {
		x1 = 10.56f;
		y1 = 12.80f;
		z1 = 1.20f;
		t1 = new Vector(x1, y1, z1);
	}

	@Test
	public void testgetInstanceNormalizedTriade(){
		Vector test = new Vector(1, 1, 0);
		Vector expected = new Vector(0.70710677f, 0.70710677f, 0.0f);
		
		assertEquals(expected, test.normalize());
	}

	@Test
	public void testTriadeDoubleDoubleDouble() {
		assertNotNull(t1);
		assertEquals(t1, new Vector(this.x1, this.y1, this.z1));
	}

	@Test
	public void testSub() {
		Vector w = new Vector(1.0f, 1.0f, 1.0f);
		Vector v = new Vector(3.0f, 1.0f, -2.0f);
		
		Vector expected = new Vector(-2.0f, 0.0f, 3.0f);

		assertEquals(expected, w.sub(v));
	}

	@Test
	public void testAdd() {
		Vector w = new Vector(1.2f, 2.3f, 4.56f);
		Vector v = new Vector(2.1f, 1.23f, 3.22f);

		Vector expected = new Vector(3.3f, 3.53f, 7.7799997f);
		assertEquals(expected, w.add(v));
	}

	@Test
	public void testCrossProduct() {
		Vector w = new Vector(1.2f, 2.3f, 4.56f);
		Vector v = new Vector(2.1f, 1.23f, 3.22f);

		Vector expected = new Vector(1.7972002f, 5.711999f, -3.3539994f);

		assertEquals(expected, w.crossProduct(v));
	}

	@Test
	public void testDotProduct() {
		Vector w = new Vector(1.2f, 2.3f, 4.56f);
		Vector v = new Vector(2.1f, 1.23f, 3.22f);
		
		float expected = 20.0322f;
		
		float delta = 0.0001f;

		assertEquals(expected, v.dotProduct(w), delta);
	}

	@Test
	public void testLength() {
		Vector w = new Vector(1.2f, 2.3f, 4.56f);
		
		float expected = 5.246294f;
		
		float delta = 0.0001f;
		
		assertEquals(expected, w.length(), delta);
	}

	@Test
	public void testTimes() {
		Vector v = new Vector(2.5f, 3.4f, 4.5f);
		float scalar = 2.0f;
		
		Vector expected = new Vector(5f, 6.8f, 9.0f);
		assertEquals(expected, v.times(scalar));
	}

	@Test
	public void testToString() {
		Vector t = new Vector(0.123f, 123.45f, 456.886f);

		String expected = "(0.123, 123.45, 456.886)";
		assertEquals(expected, t.toString());
	}
	
	@Test
	public void testToDat() {
		Vector t = new Vector(0.123f, 123.45f, 456.886f);
		
		String expected = "0.123 123.45 456.886";
		assertEquals(expected, t.toDat());
	}

}
