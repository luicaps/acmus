package acmus.auralization;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import Jama.Matrix;
import acmus.dsp.NewSignal;
import acmus.util.math.Vector;

/**
 * A class to deal with an multi-band impulse response
 * 
 * @author migmruiz
 * 
 */
public class BinauralMultiBandImpulseResponse {

	private double[] leftSignal;
	private double[] rightSignal;
	private BandRangeSeq range;
	private int howMany;
	private int arbitraryPowerOf2;
	private float sampleRate;

	public BinauralMultiBandImpulseResponse(BandRangeSeq range,
			float[][] content, Vector[] direction, HRTFselector hrtfSelector) {
		this(range, content, direction, hrtfSelector, Float.MAX_VALUE);
	}

	public BinauralMultiBandImpulseResponse(BandRangeSeq range,
			float[][] content, Vector[] direction, HRTFselector hrtfSelector,
			float maxTime) {
		this(range, content, direction, hrtfSelector, (float) range.getSR(),
				maxTime);
	}

	public BinauralMultiBandImpulseResponse(BandRangeSeq range,
			float[][] content, Vector[] direction, HRTFselector hrtfSelector,
			float sampleRate, float maxTime) {
		if (content.length != range.howMany()) {
			throw new IllegalArgumentException(
					"the content's length must be the number of values in range");
		}
		this.range = range;
		this.howMany = range.howMany();
		this.sampleRate = sampleRate;

		int lengthMax = maxi(content, maxTime);
		for (int i = 0; i < howMany; i++) {
			content[i] = fillWithZeros(content[i], lengthMax);
		}
		direction = fillWithZeros(direction, lengthMax);

		this.arbitraryPowerOf2 = (int) Math.pow(2, 8);
		double[] leftImpulseResponseArray = new double[lengthMax
				+ arbitraryPowerOf2];
		double[] rightImpulseResponseArray = new double[lengthMax
				+ arbitraryPowerOf2];

		Mockery mockery = new Mockery() {
			{
				setImposteriser(ClassImposteriser.INSTANCE);
			}
		};

		final IProgressMonitor monitor = mockery.mock(IProgressMonitor.class);
		mockery.checking(new Expectations() {
			{
				ignoring(monitor);
			}
		});
		// End preparations
		for (int j = 0; j < lengthMax; j++) {
			double[] arrFreq = fillFrequencyDomain(j, content);
			double[] arrTemp = fillTimeDomain(arrFreq);

			// shift circular
			double[] arrTempSC = new double[arbitraryPowerOf2];
			for (int i = 0; i < arbitraryPowerOf2 / 2; i++) {
				arrTempSC[i + arbitraryPowerOf2 / 2] += arrTemp[i];
				arrTempSC[i] += arrTemp[i + arbitraryPowerOf2 / 2];
			}

			if (direction[j] != null) {
				double[][] pulse = hrtfSelector.getPulse(
						direction[j].azimuth(), direction[j].elevation());

				NewSignal leftPulse = new NewSignal(pulse[0]);
				NewSignal rightPulse = new NewSignal(pulse[1]);
				NewSignal sigTemp = new NewSignal(arrTempSC);

				NewSignal leftSigTemp = sigTemp.convolve(leftPulse, monitor);
				NewSignal rightSigTemp = sigTemp.convolve(rightPulse, monitor);

				for (int i = 0; i < arbitraryPowerOf2; i++) {
					leftImpulseResponseArray[j + i] += leftSigTemp.get(i).re();
					rightImpulseResponseArray[j + i] += rightSigTemp.get(i)
							.re();
				}
			}
			// if (j % 1000.0 == 0) {
			// System.out.println(100.0*j/lengthMax + " %");
			// }
		}
		this.leftSignal = new double[lengthMax];
		this.rightSignal = new double[lengthMax];
		for (int i = 0; i < lengthMax; i++) {
			leftSignal[i] = leftImpulseResponseArray[arbitraryPowerOf2 / 2 + i];
			rightSignal[i] = rightImpulseResponseArray[arbitraryPowerOf2 / 2
					+ i];
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

	/**
	 * Grown the direction Vector to length and fill it with zeros
	 * 
	 * @param direction
	 * @param length
	 * @return the growth Vector
	 */
	private Vector[] fillWithZeros(Vector[] direction, int length) {
		if (direction.length < length) {
			Vector[] temp = new Vector[length];
			for (int i = 0; i < direction.length; i++) {
				temp[i] = direction[i];
			}
			Vector ZERO = new Vector(0, 0, 0);
			for (int j = direction.length; j < length; j++) {
				temp[j] = ZERO;
			}
			return temp;
		} else {
			return direction;
		}
	}

	private double[] fillFrequencyDomain(int timeIndex, float[][] content) {
		double[] energy = new double[howMany];
		double[] freq = range.getArray();
		double[][] sistFreq = new double[howMany][];
		for (int i = 0; i < howMany; i++) {
			energy[i] = content[i][timeIndex];
			sistFreq[i] = new double[howMany];
			for (int j = 0; j < howMany; j++) {
				sistFreq[i][j] = Math.pow(freq[i], j);
			}
		}
		Matrix m = new Matrix(sistFreq);
		Matrix e = new Matrix(energy, energy.length);
		Matrix p = m.solve(e);

		double factor = sampleRate / (double) arbitraryPowerOf2;
		double[] arrFreq = new double[arbitraryPowerOf2];

		double fqVal;
		for (int i = 0; i < arbitraryPowerOf2 / 2; i++) {
			fqVal = p.get(howMany - 1, 0);
			for (int j = howMany - 2; j >= 0; j--) {
				fqVal = p.get(j, 0) + i * factor * fqVal;
			}
			arrFreq[i] = fqVal;
			if (i != 0) {
				arrFreq[arbitraryPowerOf2 - i] = fqVal;
			}
		}
		return arrFreq;
	}

	private double[] fillTimeDomain(double[] arrFreq) {
		NewSignal sigTemp = (new NewSignal(arrFreq)).ifft();
		double[] arrTemp = new double[arbitraryPowerOf2];
		for (int i = 0; i < arbitraryPowerOf2; i++) {
			arrTemp[i] = sigTemp.get(i).re();
		}
		return arrTemp;
	}

	public double[] getLeftSignal() {
		return leftSignal;
	}

	public double[] getRightSignal() {
		return rightSignal;
	}
}
