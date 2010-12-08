package acmus.auralization;

import acmus.dsp.NewSignal;

public class AuralizationHandler {

	public double[] signalSample(BandRangeSeq range, float[][] content,
			float sampleRate) {
		if (content.length != range.howMany()) {
			throw new IllegalArgumentException(
					"the content's length must be the number of values in range");
		}

		int lengthMax = Math.max(
				content[0].length,
				Math.max(content[1].length,
						Math.max(content[2].length, content[3].length)));

		for (int i = 0; i < range.howMany(); i++) {
			if (content[i].length < lengthMax) {
				float[] temp = new float[lengthMax];
				for (int j = 0; j < content[i].length; j++) {
					temp[j] = content[i][j];
				}
				for (int j = content[i].length; j < lengthMax; j++) {
					temp[j] = 0f;
				}
				content[i] = temp;
			}
		}
		int arbitraryPowerOf2 = (int) Math.pow(2, 12);
		double[] impulseResponseArray = new double[lengthMax
				+ arbitraryPowerOf2];

		for (int j = 0; j < lengthMax; j++) {
			double[] freqT = new double[range.howMany()];
			for (int i = 0; i < range.howMany(); i++) {
				freqT[i] = content[i][j];
			}
			double factor = Math.floor(arbitraryPowerOf2) / sampleRate;
			double[] arrFreq = new double[arbitraryPowerOf2];
			for (int i = 1; i <= range.howMany(); i++) {
				for (int k = (int) (range.getMin() * factor); k < i * factor
						* range.getMax() / range.howMany(); k++) {
					arrFreq[k] = freqT[i - 1];
				}
			}
			NewSignal sigTemp = (new NewSignal(arrFreq)).fft();
			double[] arrTemp = new double[(int) sampleRate];
			for (int i = 0; i < arbitraryPowerOf2; i++) {
				arrTemp[Math.round((float) (i / factor))] = sigTemp.get(i).re();
			}
			// shift circular
			for (int i = 0; i < arbitraryPowerOf2 / 2; i++) {
				impulseResponseArray[j + arbitraryPowerOf2 / 2 + i] += arrTemp[i];
				impulseResponseArray[j + i] += arrTemp[i + arbitraryPowerOf2
						/ 2];
			}
		}

		return impulseResponseArray;

	}

	public static void main(String[] args) {

		float sampleRate = 44100f;
		int numberOfRays = 250;

		Simulator sim = new Simulator();

		sim.setUp(numberOfRays, sampleRate);

		// 4 band ranges in human limits
		BandRangeSeq range = new BandRangeEqSeq(20.0, 2000.0, 4);

		float[][] content = new float[range.howMany()][];

		AurViewer viewer = new AurViewer();

		content[0] = sim.simulateCoeff(0.01, 0.01, 0.001, 0.22, 0.1, 0.1);
		viewer.view(content[0], 1.f, "coeffs 0.01, 0.01, 0.001, 0.22, 0.1, 0.1");
		content[1] = sim.simulateCoeff(0.01, 0.02, 0.04, 0.13, 0.3, 0.25);
		viewer.view(content[1], 1.f, "coeffs 0.01, 0.02, 0.04, 0.13, 0.3, 0.25");
		content[2] = sim.simulateCoeff(0.02, 0.03, 0.001, 0.09, 0.34, 0.2);
		viewer.view(content[2], 1.f,
				"coeffs 0.02, 0.03, 0.001, 0.09, 0.34, 0.2");
		content[3] = sim.simulateCoeff(0.02, 0.04, 0.07, 0.2, 0.05, 0.06);
		viewer.view(content[3], 1.f, "coeffs 0.02, 0.04, 0.07, 0.2, 0.05, 0.06");

		AuralizationHandler aur = new AuralizationHandler();

		double[] ir;

		ir = aur.signalSample(range, content, sampleRate);

		viewer.view(ir, "Impulse Response");
	}
}
