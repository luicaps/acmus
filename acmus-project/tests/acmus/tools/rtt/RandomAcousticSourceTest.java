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

import acmus.tools.structures.Triade;

public class RandomAcousticSourceTest {

	private static final int POINTS_SIZE = 10000;
	private static List<Triade> randomPoints;

	@BeforeClass
	public static void setUp() throws Exception {
		RandomAcousticSource ras = new RandomAcousticSource();

		Date d1, d2;
		d1 = new Date();
		randomPoints = ras.generate(POINTS_SIZE);
		d2 = new Date();

		long temp = d2.getTime() - d1.getTime();
		System.out.println("Tempo para construir os pontos: " + temp + " ms");
	}

	@Test
	public void visualTest() throws Exception {
		FileWriter fw = new FileWriter("/tmp/fonte3d.txt");

		for (int i = 0; i < randomPoints.size(); i++) {
			fw.write(randomPoints.get(i).toDat() + "\n");
		}

		// Para desenhar o grafico com o gnuplot
		// set size square
		// splot '/tmp/fonte3d.txt'
		fw.flush();
		fw.close();
	}

	@Test
	public void testPointsAreGeneratedInTheSurfaceOfAUnitSphere()
			throws IOException {

		for (Triade triade : randomPoints) {
			assertEquals(1.0, triade.modulo(), Triade.EPS);
		}

	}

	@Test
	public void testAllPointsAreGenerated() throws Exception {
		Assert.assertEquals(POINTS_SIZE, randomPoints.size());

	}

	@Test
	public void testPointsAreUniformelyDistributed() throws Exception {

		int octs[] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };

		for (Triade triade : randomPoints) {
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
