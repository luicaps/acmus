package acmus.tools.rtt;

import static org.junit.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import acmus.tools.structures.Vector;

public class MonteCarloRandomAcousticSourceTest {

	private static final int POINTS_SIZE = 1000;
	private static List<Vector> randomPoints;

	@BeforeClass
	public static void setUp() throws Exception {
		AcousticSource ras = new MonteCarloRandomAcousticSource();

		Date d1, d2;
		d1 = new Date();
		randomPoints = ras.generate(POINTS_SIZE);
		d2 = new Date();

		long temp = d2.getTime() - d1.getTime();
		System.out.println("Tempo para construir os "+ POINTS_SIZE + " pontos: " + temp + " ms");
	}

	@Test
	public void visualTest() throws Exception {
		FileWriter fw = new FileWriter("/tmp/fonte3d.txt");

		for (int i = 0; i < randomPoints.size(); i++) {
			fw.write(randomPoints.get(i).toDat() + "\n");
		}

		// Para desenhar o grafico com o gnuplot
		// set size square
		// splot '/tmp/fonte3d.txt'gnuplot> set multiplot
//		multiplot> splot '/tmp/fe.txt' with point 2;
//		multiplot> splot '/tmp/fq.txt' with point 1;
//		multiplot>         
		
		fw.flush();
		fw.close();
	}

	//FIXME this that fails doesn't detect the vector (1,1,1) which does not belongs to the
	// unit sphere
	public void testPointsAreGeneratedInTheSurfaceOfAUnitSphere() throws IOException {

		for (Vector v : randomPoints) {
			assertEquals(1.0, v.length(), 0.00001);
		}

	}

	@Test
	public void testAllPointsAreGenerated() throws Exception {
		Assert.assertEquals(POINTS_SIZE, randomPoints.size());

	}

	@Test
	public void testPointsAreUniformelyDistributed() throws Exception {

		int octs[] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };

		for (Vector triade : randomPoints) {
			int i = 0;
			if (triade.getX() < 0)
				i += 1;
			if (triade.getY() < 0)
				i += 2;
			if (triade.getZ() < 0)
				i += 4;
			octs[i]++;
		}

		Assert.assertEquals("sum of octs", POINTS_SIZE, sum(octs));

		int i = new Random().nextInt(8);
		Assert.assertTrue("expected near " + POINTS_SIZE / 8 + " was: "
				+ octs[i], octs[i] > (POINTS_SIZE * 0.1)
				&& octs[i] < POINTS_SIZE * 0.15);
	}

	private int sum(int[] octs) {
		int sum = 0;
		for (int i : octs) {
			sum += i;
		}
		return sum;
	}

}
