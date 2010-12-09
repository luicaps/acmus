package acmus.auralization;

import acmus.dsp.NewSignal;

public class AuralizationHandler {

	public double[] signalSample(BandRangeSeq range, float[][] content,
			float sampleRate) {
		if (content.length != range.howMany()) {
			throw new IllegalArgumentException(
					"the content's length must be the number of values in range");
		}

		int lengthMax = maxi(content);
		
		System.out.println("lengthMax = " + lengthMax);
		
		for (int i = 0; i < range.howMany(); i++) {
			content[i] = fillWithZeros(content[i], lengthMax);
		}
		int arbitraryPowerOf2 = (int) Math.pow(2, 10);
		double[] impulseResponseArray = new double[lengthMax
				+ arbitraryPowerOf2];

		for (int j = 0; j < lengthMax; j++) {
			double freqT;
			double factor = Math.floor(arbitraryPowerOf2) * 2 / sampleRate;
			double[] arrFreq = new double[arbitraryPowerOf2];
			for (int i = 0; i < range.howMany(); i++) {
				freqT = content[i][j]; //TODO check here |
				for (int k = (int) (range.getMin() * factor); k < (i + 1) * factor
						* range.getMax() / range.howMany(); k++) {
					arrFreq[k] = freqT;
					arrFreq[arbitraryPowerOf2 - k - 1] = freqT;
				}
			}
			NewSignal sigTemp = (new NewSignal(arrFreq)).ifft();
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
//			if (j % 1000.0 == 0) {
//				System.out.println(100.0*j/lengthMax + " %");
//			}
		}
		return impulseResponseArray;
	}

	private int maxi(float[][] matrix) {
		int max = 0;
		for(int i = 0; i < matrix.length; i++){
			max = Math.max(max, matrix[i].length);
		}
		return max;
	}

	private float[] fillWithZeros(float[] array, int length) {
		if (array.length < length) {
			float[] temp = new float[length];
			for (int i = 0; i < array.length; i++) {
				temp[i] = array[i];
			}
			for (int j = array.length; j < length; j++) {
				temp[j] = 0f;
			}
			return temp;
		} else {
			return array;
		}
	}

	public static void main(String[] args) {

		float sampleRate = 44100f;
		int numberOfRays = 200;

		Simulator sim = new Simulator();

		sim.setUp(numberOfRays, sampleRate);

		// 4 band ranges in human limits
		BandRangeSeq range = new BandRangeEqSeq(20.0, 2000.0, 4);

		float[][] content = new float[range.howMany()][];

		AurViewer viewer = new AurViewer();

		content[0] = sim.simulateCoeff(0.01, 0.01, 0.001, 0.12, 0.05, 0.05);
		viewer.view(content[0], 1.f, "sim1");
		content[1] = sim.simulateCoeff(0.01, 0.02, 0.04, 0.06, 0.14, 0.12);
		viewer.view(content[1], 1.f, "sim2");
		content[2] = sim.simulateCoeff(0.02, 0.03, 0.001, 0.04, 0.16, 0.1);
		viewer.view(content[2], 1.f, "sim3");
		content[3] = sim.simulateCoeff(0.02, 0.04, 0.07, 0.1, 0.02, 0.03);
		viewer.view(content[3], 1.f, "sim4");

		AuralizationHandler aur = new AuralizationHandler();

		double[] ir;
		
		long time = System.currentTimeMillis();
		ir = aur.signalSample(range, content, sampleRate);
		time = System.currentTimeMillis() - time;
		
		System.out.println("Expended time:" + ((double) time)/1000.0 + " s");
		
		viewer.view(ir, "ImpulseResponse");
	}
}
