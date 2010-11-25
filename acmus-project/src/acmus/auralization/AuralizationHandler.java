package acmus.auralization;

import acmus.AcmusApplication;
import acmus.dsp.NewSignal;

public class AuralizationHandler {

	public double[] signalSample(BandRangeSeq range, Float[][] content) {
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
				Float[] temp = new Float[lengthMax];
				for (int j = 0; j < content[i].length; j++) {
					temp[j] = content[0][j];
				}
				for (int j = content[i].length; j < lengthMax; j++) {
					temp[j] = 0f;
				}
				content[i] = temp;
			}
		}

		double[] impulseResponseArray = new double[lengthMax
				+ (int) AcmusApplication.SAMPLE_RATE];

		for (int i = 0; i < lengthMax; i++) {
			double[] freqT = new double[range.howMany()];
			for (int j = 0; j < range.howMany(); j++) {
				freqT[j] = content[j][i];
			}
			int arbitraryPowerOf2 = 32768;
			double factor = arbitraryPowerOf2 / AcmusApplication.SAMPLE_RATE;
			double[] arrFreq = new double[arbitraryPowerOf2];
			for (int n = 1; n <= range.howMany(); n++) {
				for (int j = (int) (range.getMin() * factor); j < n * factor
						* range.getMax() / range.howMany(); j++) {
					arrFreq[j] = freqT[n - 1];
				}
			}
			NewSignal sigTemp = (new NewSignal(arrFreq)).fft();
			double[] arrTemp = new double[(int) AcmusApplication.SAMPLE_RATE];
			for (int j = 0; j < arbitraryPowerOf2; j++) {
				arrTemp[Math.round((float) (j / factor))] = sigTemp.get(j).re();
			}
			// shift circular
			for (int j = 0; j < AcmusApplication.SAMPLE_RATE / 2; j++) {
				impulseResponseArray[i + (int) AcmusApplication.SAMPLE_RATE / 2
						+ j] += arrTemp[j];
				impulseResponseArray[i + j] += arrTemp[j
						+ (int) AcmusApplication.SAMPLE_RATE / 2];
			}
		}

		// double[] impulseResponseArrayTemp = new double[lengthMax];
		// for (int i = 0; i < lengthMax; i++) {
		// impulseResponseArrayTemp[i] = impulseResponseArray[i
		// + (int) AcmusApplication.SAMPLE_RATE / 2];
		// }
		// impulseResponseArray = impulseResponseArrayTemp;

		return impulseResponseArray;

	}

	public static void main(String[] args) {

		Simulator sim = new Simulator();

		sim.setUp();

		// 4 band ranges in human limits
		BandRangeSeq range = new BandRangeEqSeq(20.0, 20000.0, 4);

		Float[][] content = new Float[range.howMany()][];

		content[0] = sim.simulateCoeff(0.01, 0.01, 0.001, 0.22, 0.1, 0.1);
		content[1] = sim.simulateCoeff(0.01, 0.02, 0.04, 0.13, 0.3, 0.25);
		content[2] = sim.simulateCoeff(0.02, 0.03, 0.001, 0.09, 0.34, 0.2);
		content[3] = sim.simulateCoeff(0.02, 0.04, 0.07, 0.2, 0.05, 0.06);

		double[] ir;

		AuralizationHandler aur = new AuralizationHandler();
		ir = aur.signalSample(range, content);

		AurViewer viewer = new AurViewer();

		viewer.view(ir);
	}
}
