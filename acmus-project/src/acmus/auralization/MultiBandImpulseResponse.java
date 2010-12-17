package acmus.auralization;

import Jama.Matrix;
import acmus.dsp.NewSignal;

/**
 * A class to deal with an multi-band impulse response
 * 
 * @author migmruiz
 * 
 */
public class MultiBandImpulseResponse {

	private double[] signal;
	private BandRangeSeq range;
	private int arbitraryPowerOf2;
	private float sampleRate;
	private double rangeMax;

	public MultiBandImpulseResponse(BandRangeSeq range, float[][] content) {
		this(range, content, Float.MAX_VALUE);
	}

	public MultiBandImpulseResponse(BandRangeSeq range, float[][] content,
			float maxTime) {
		this(range, content, 2.f * (float) range.getMax(), maxTime);
	}

	public MultiBandImpulseResponse(BandRangeSeq range, float[][] content,
			float sampleRate, float maxTime) {
		if (content.length != range.howMany()) {
			throw new IllegalArgumentException(
					"the content's length must be the number of values in range");
		}
		this.range = range;
		this.rangeMax = range.getMax();
		this.sampleRate = sampleRate;

		int lengthMax = maxi(content, maxTime);

		System.out.println("lengthMax = " + lengthMax);

		for (int i = 0; i < range.howMany(); i++) {
			content[i] = fillWithZeros(content[i], lengthMax);
		}
		this.arbitraryPowerOf2 = (int) Math.pow(2, 8);
		double[] impulseResponseArray = new double[lengthMax
				+ (int) Math.ceil(2 * rangeMax)];

		for (int j = 0; j < lengthMax; j++) {
			double[] arrFreq = fillFrequencyDomain(j, content);
			double[] arrTemp = fillTimeDomain(arrFreq);
			// shift circular
			for (int i = 0; i < rangeMax; i++) {
				impulseResponseArray[j + (int) Math.floor(rangeMax) + i] += arrTemp[i];
				impulseResponseArray[j + i] += arrTemp[i
						+ (int) Math.floor(rangeMax)];
			}
			// if (j % 1000.0 == 0) {
			// System.out.println(100.0*j/lengthMax + " %");
			// }
		}
		this.signal = new double[lengthMax];
		for (int i = 0; i < lengthMax; i++) {
			signal[i] = impulseResponseArray[(int) Math.floor(rangeMax) + i];
		}
	}

	/**
	 * Chooses the max sample value of the impulse response
	 * 
	 * @param matrix
	 * @param maxTime
	 * @return the max of matrix[i].length if it is less then Math.ceil(maxTime
	 *         * sampleRate), otherwise that is the returned value
	 */
	private int maxi(float[][] matrix, float maxTime) {
		int maxSample;
		if (maxTime < Float.MAX_VALUE) {
			maxSample = (int) Math.ceil(maxTime * sampleRate);
		} else {
			maxSample = Integer.MAX_VALUE;
		}

		int max = 0;
		for (int i = 0; i < matrix.length; i++) {
			max = Math.max(max, matrix[i].length);
		}
		return Math.min(max, maxSample);
	}

	/**
	 * Grown the array to length and fill it with zeros
	 * 
	 * @param array
	 * @param length
	 * @return the growth array
	 */
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

	private double[] fillFrequencyDomain(int timeIndex, float[][] content) {
		double[] energy = new double[range.howMany()];
		double[] freq = range.getArray();
		double[][] sistFreq = new double[range.howMany()][];
		for (int i = 0; i < range.howMany(); i++) {
			energy[i] = content[i][timeIndex];
			sistFreq[i] = new double[range.howMany()];
			for (int j = 0; j < range.howMany(); j++) {
				sistFreq[i][j] = Math.pow(freq[i], j);
			}
		}
		Matrix m = new Matrix(sistFreq);
		Matrix e = new Matrix(energy, energy.length);
		Matrix p = m.solve(e);

		double factor = (double) arbitraryPowerOf2 / (2 * range.getMax());
		double[] arrFreq = new double[arbitraryPowerOf2];

		double freqT;
		for (int fq = 0; fq < rangeMax; fq++) {
			freqT = 0;
			for (int j = 0; j < range.howMany(); j++) {
				freqT += p.get(j, 0) * fq;
			}
			arrFreq[Math.round((float) (fq * factor))] = freqT;
			arrFreq[arbitraryPowerOf2 - (int) Math.ceil(fq * factor) - 1] = freqT;
		}
		return arrFreq;
	}

	private double[] fillTimeDomain(double[] arrFreq) {
		NewSignal sigTemp = (new NewSignal(arrFreq)).ifft();
		double[] arrTemp = new double[(int) Math.ceil(2 * rangeMax)];
		double factor = Math.floor(arbitraryPowerOf2) / (2 * rangeMax);
		for (int i = 0; i < arbitraryPowerOf2; i++) {
			arrTemp[Math.round((float) ((double) (i) / factor))] = sigTemp.get(
					i).re();
		}
		return arrTemp;
	}

	public double[] getSignal() {
		return signal;
	}

	public static void main(String[] args) {

		int numberOfRays = 3000;
		float maxTime = 0.7f;

		// 4 band ranges in human limits
		BandRangeSeq range = new BandRangeHarmSeq(20.0, 20000.0, 4);
		float sampleRate = 2.f * (float) range.getMax();

		Simulator sim = new Simulator();

		sim.setUp(numberOfRays, sampleRate);

		float[][] content = new float[range.howMany()][];

		MultiBandSimulationViewer viewer = new MultiBandSimulationViewer();

		content[0] = sim.simulateCoeff(0.01, 0.01, 0.001, 0.12, 0.05, 0.05);
		viewer.view(content[0], 1.f, "sim1");
		content[1] = sim.simulateCoeff(0.01, 0.02, 0.04, 0.06, 0.14, 0.12);
		viewer.view(content[1], 1.f, "sim2");
		content[2] = sim.simulateCoeff(0.02, 0.03, 0.001, 0.04, 0.16, 0.1);
		viewer.view(content[2], 1.f, "sim3");
		content[3] = sim.simulateCoeff(0.02, 0.04, 0.07, 0.1, 0.02, 0.03);
		viewer.view(content[3], 1.f, "sim4");

		long time = System.currentTimeMillis();
		MultiBandImpulseResponse aur = new MultiBandImpulseResponse(range,
				content, maxTime);

		double[] ir;

		ir = aur.getSignal();
		time = System.currentTimeMillis() - time;

		System.out.println("Expended time:" + ((double) time) / 1000.0 + " s");

		viewer.view(ir, 1.f, "ImpulseResponse");
	}
}
