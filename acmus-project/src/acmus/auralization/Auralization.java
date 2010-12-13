package acmus.auralization;

import acmus.dsp.NewSignal;

public class Auralization {

	private double[] signal;
	private BandRangeSeq range;
	private int arbitraryPowerOf2;
	private double factor;
	private float sampleRate;

	public Auralization(BandRangeSeq range, float[][] content, float sampleRate) {
		this(range, content, sampleRate, Float.MAX_VALUE);
	}

	public Auralization(BandRangeSeq range, float[][] content,
			float sampleRate, float maxTime) {
		if (content.length != range.howMany()) {
			throw new IllegalArgumentException(
					"the content's length must be the number of values in range");
		}
		this.range = range;
		this.sampleRate = sampleRate;

		int lengthMax = maxi(content, maxTime);

		System.out.println("lengthMax = " + lengthMax);

		for (int i = 0; i < range.howMany(); i++) {
			content[i] = fillWithZeros(content[i], lengthMax);
		}
		this.arbitraryPowerOf2 = (int) Math.pow(2, 10);
		double[] impulseResponseArray = new double[lengthMax
				+ arbitraryPowerOf2];

		for (int j = 0; j < lengthMax; j++) {
			this.factor = Math.floor(arbitraryPowerOf2) * 2 / sampleRate;
			double[] arrFreq = fillFrequencyDomain(j, content);
			double[] arrTemp = fillTimeDomain(arrFreq);
			// shift circular
			for (int i = 0; i < arbitraryPowerOf2 / 2; i++) {
				impulseResponseArray[j + arbitraryPowerOf2 / 2 + i] += arrTemp[i];
				impulseResponseArray[j + i] += arrTemp[i + arbitraryPowerOf2
						/ 2];
			}
			// if (j % 1000.0 == 0) {
			// System.out.println(100.0*j/lengthMax + " %");
			// }
		}
		this.signal = new double[lengthMax];
		for(int i = 0; i < lengthMax; i++) {
			signal[i] = impulseResponseArray[arbitraryPowerOf2/2 + i];
		}
	}

	private double[] fillTimeDomain(double[] arrFreq) {
		NewSignal sigTemp = (new NewSignal(arrFreq)).ifft();
		double[] arrTemp = new double[(int) sampleRate];
		for (int i = 0; i < arbitraryPowerOf2; i++) {
			arrTemp[Math.round((float) (i / factor))] = sigTemp.get(i).re();
		}
		return arrTemp;
	}

	private int maxi(float[][] matrix, float maxTime) {
		int maxSample;
		if (maxTime < Float.MAX_VALUE){
			maxSample = (int) Math.floor(maxTime * sampleRate);
		} else {
			maxSample = Integer.MAX_VALUE;
		}
		
		int max = 0;
		for (int i = 0; i < matrix.length; i++) {
			max = Math.max(max, matrix[i].length);
		}
		return Math.min(max, maxSample);
	}

	private double[] fillFrequencyDomain(int timeIndex, float[][] content) {
		double freqT;
		double[] arrFreq = new double[arbitraryPowerOf2];
		for (int i = 0; i < range.howMany(); i++) {
			freqT = content[i][timeIndex]; // TODO check here |
			for (int k = (int) (range.getMin() * factor); k < (i + 1) * factor
					* range.getMax() / range.howMany(); k++) {
				arrFreq[k] = freqT;
				arrFreq[arbitraryPowerOf2 - k - 1] = freqT;
			}
		}
		return arrFreq;
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

	public double[] getSignal() {
		return signal;
	}

	public static void main(String[] args) {

		float sampleRate = 44100f;
		int numberOfRays = 1000;
		float maxTime = 0.9f;

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

		long time = System.currentTimeMillis();
		Auralization aur = new Auralization(range, content, sampleRate, maxTime);

		double[] ir;

		ir = aur.getSignal();
		time = System.currentTimeMillis() - time;

		System.out.println("Expended time:" + ((double) time) / 1000.0 + " s");

		viewer.view(ir, "ImpulseResponse");
	}
}
