package acmus.tools.structures;


import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ImpulseResponseTest {

	private final static float interval = 0.5f;
	private SimulatedImpulseResponse ir; 
	@Before
	public void setUp() throws Exception {
		ir = new EnergeticSimulatedImpulseResponse(interval);
	}

	@Test
	public void testAddValueAndGetIR() {
		ir.addValue(0.20f, 1.1f);
		ir.addValue(0.49f, 1.1f);
		ir.addValue(0.51f, 1.0f);
		ir.addValue(3.89f, 1.0f);
		
		Map<Float, Float> expected = new HashMap<Float, Float>();
		expected.put(0.5f, 2.2f);
		expected.put(1.0f, 1.0f);
		expected.put(4.0f, 1.0f);
		
		Assert.assertEquals(expected, ir.getEnergeticImpulseResponse());
	}
	
	@Test(expected=InvalidParameterException.class)
	public void testParametroInvalido() {
		ir.addValue(-1.0f, 1.0f);
	}
	
	@Test
	public void testSmallInterval() {
		ir = new EnergeticSimulatedImpulseResponse(0.0000001f);
		
		ir.addValue(0.00000020f, 1.0f);
		ir.addValue(0.00000030f, 2.0f);
		ir.addValue(0.00000039f, 3.0f);
		ir.addValue(0.00000040f, 7.0f);
		ir.addValue(0.00000041f, 11.0f);
		ir.addValue(0.1f, 17.0f);
		
		Map<Float, Float> expected = new HashMap<Float, Float>();
		expected.put(0.0000002f, 1.0f);
		expected.put(0.0000003f, 2.0f);
		expected.put(0.0000004f, 10.0f);
		expected.put(0.0000005f, 11.0f);
		expected.put(0.1f, 17.0f);
		
		Assert.assertEquals(expected, ir.getEnergeticImpulseResponse());
	}
}
