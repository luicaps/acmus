package acmus.tests.tools.rtt;

import static org.junit.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import acmus.tools.rtt.RandomAcousticSource;
import acmus.tools.structures.Triade;

public class RandomAcousticSourceTest {

	private static List<Triade> pontosAleatorios;

	@BeforeClass
	public static void setUp() throws Exception {
		RandomAcousticSource ras = new RandomAcousticSource();

		Date d1, d2;
		d1 = new Date();
		pontosAleatorios = ras.generate(1000);
		d2 = new Date();

		long temp = d2.getTime() - d1.getTime();
		System.out.println("Tempo para construir os pontos: " + temp + " ms");
	}

	public void visualTest() throws Exception {
		FileWriter fw = new FileWriter("/tmp/fonte3d.txt");

		for (int i = 0; i < pontosAleatorios.size(); i++) {
			fw.write(pontosAleatorios.get(i).toDat() + "\n");
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

		for (Triade triade : pontosAleatorios) {
			assertEquals(1.0, triade.modulo(), Triade.EPS);
		}

	}

}
