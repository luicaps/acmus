package acmus.tests.tools.rtt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import acmus.tools.rtt.RandomAcousticSource;
import acmus.tools.structures.Triade;

public class RandomAcousticSourceTest extends TestCase {

	public void testGenerate() {
		fail("Not yet implemented");
	}

	
	public void testGenerateInt() throws IOException {
		RandomAcousticSource ras = new RandomAcousticSource();
		List<Triade> pontosAleatorios;
		Date d1, d2;
		d1 = new Date();
		pontosAleatorios = ras.generate(1000);
		d2 = new Date();

		long temp = d2.getTime() - d1.getTime();
		System.out.println("Tempo para construir os pontos: " + temp + " ms");

		FileWriter fw = new FileWriter("/tmp/fonte3d.txt");
		
		for (int i = 0; i < pontosAleatorios.size(); i++) {
			fw.write(pontosAleatorios.get(i).toDat()+"\n");
		}
	
		//Para desenhar o grafico com o gnuplot
		//set size square
		//splot '/tmp/fonte3d.txt'
		fw.flush();
		fw.close();

	}

}
