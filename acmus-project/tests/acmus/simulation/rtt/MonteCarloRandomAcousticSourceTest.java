package acmus.simulation.rtt;

import static org.junit.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import acmus.simulation.AcousticSource;
import acmus.simulation.math.Vector;
import acmus.simulation.structures.MonteCarloAcousticSource;

public class MonteCarloRandomAcousticSourceTest {

	private static final int POINTS_SIZE = 1000;
	private static List<Vector> randomPoints;

	@BeforeClass
	public static void setUp() {
		AcousticSource ras = new MonteCarloAcousticSource(new Vector(0, 0, 0));

		Date d1, d2;
		d1 = new Date();
		randomPoints = ras.manyDirections(POINTS_SIZE);
		d2 = new Date();

		long time = d2.getTime() - d1.getTime();
		System.out.println("Direction generation time for "+ POINTS_SIZE + " points: " + time + " ms");
	}

	@Test
	public void visualTest() throws IOException {
		String dataFile = System.getProperty("java.io.tmpdir")
				+ System.getProperty("file.separator") + "fonte3d.txt";
		FileWriter fw = new FileWriter(dataFile);

		for (Vector vecVisual : randomPoints) {
			fw.write(vecVisual.toDat() + "\n");
		}
		
		fw.flush();
		fw.close();
		
		System.out.println();
		System.out.println("Graphing with gnuplot:");
		System.out.println("\t $ gnuplot");
		System.out.println("\t gnuplot> set size square");
		System.out.println("\t gnuplot> splot '" + dataFile + "'");
		
	}

	@Test
	public void testPointsAreGeneratedInTheSurfaceOfAUnitSphere() {

		for (Vector v : randomPoints) {
			assertEquals(1.0f , v.norm(), 0.000001f);
		}

	}

	@Test
	public void testAllPointsAreGenerated() {
		Assert.assertEquals(POINTS_SIZE, randomPoints.size());

	}

	@Test
	public void testPointsAreUniformelyDistributed() {

		int octs[] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };

		for (Vector triade : randomPoints) {
			int i = 0;
			if (triade.x < 0)
				i += 1;
			if (triade.y < 0)
				i += 2;
			if (triade.z < 0)
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
