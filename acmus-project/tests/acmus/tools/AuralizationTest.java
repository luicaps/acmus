package acmus.tools;

import java.io.FileWriter;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

import acmus.dsp.NewSignal;

public class AuralizationTest {
	static int arbitraryPowerOf2 = 32768;

	@Test
	public void auralizationSample() {

		double[] spectralResponseArray = new double[arbitraryPowerOf2];

		// 4 band ranges
		double[] content = { 0.83, 0.01, 0.12, 0.93 }; // values for test
		int bands = 4;

		for (int n = 0; n < bands; n++) {
			int faixaLength = spectralResponseArray.length / (bands * 2);
			for (int i = n * faixaLength; i <= (n + 1) * faixaLength; i++) {
				spectralResponseArray[i] = content[n];
				spectralResponseArray[spectralResponseArray.length - (n + 1)] = content[n];
			}
		}

		NewSignal spectralResponseSignal = new NewSignal(spectralResponseArray);

		NewSignal impulseResponseSignal = spectralResponseSignal.ifft();
		Assert.assertNotNull(impulseResponseSignal);

		double[] impulseResponseArray = new double[arbitraryPowerOf2];

		for (int index = 0; index < impulseResponseSignal.size() / 2; index++) {
			impulseResponseArray[impulseResponseSignal.size() / 2 + index] = impulseResponseSignal
					.get(index).re();
		}
		for (int index = impulseResponseSignal.size() / 2; index < impulseResponseSignal
				.size(); index++) {
			impulseResponseArray[index - impulseResponseSignal.size() / 2] = impulseResponseSignal
					.get(index).re();
		}

		Assert.assertNotNull(impulseResponseArray);
		try {
			print(impulseResponseArray);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Couldn't print");
		}
	}

	@Test
	public void globalTest() {
		Assert.fail("Not yet implemented");
	}

	void print(double[] array) throws IOException {
		FileWriter fw = new FileWriter("/tmp/wavelet.csv");
		StringBuilder sb = new StringBuilder(2000);

		for (int i = 0; i < array.length; i++) {

			sb.append(i / (double) arbitraryPowerOf2);
			sb.append(", ");
			sb.append(array[i]);
			sb.append("\n");

		}
		fw.write(sb.toString());
		fw.close();
	}
}
