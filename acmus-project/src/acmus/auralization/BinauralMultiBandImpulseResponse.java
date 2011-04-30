package acmus.auralization;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;

import Jama.Matrix;
import acmus.dsp.NewSignal;
import acmus.util.ArrayUtils;
import acmus.util.WaveUtils;
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
			float[][] content, Vector[] direction) {
		this(range, content, direction, Float.MAX_VALUE);
	}

	public BinauralMultiBandImpulseResponse(BandRangeSeq range,
			float[][] content, Vector[] direction, float maxTime) {
		this(range, content, direction, (float) range.getSR(), maxTime);
	}

	public BinauralMultiBandImpulseResponse(BandRangeSeq range,
			float[][] content, Vector[] direction, float sampleRate,
			float maxTime) {
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

		// Begin preparations
		HRTFselector hrtfSelector = new HRTFselector();
		OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
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
				double[][] pulse = hrtfSelector.getPulse(octave,
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
		octave.close();
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

	public static void main(String[] args) {

		System.out.println("Setting up...");

		String mainPath = "/home/migmruiz/Documentos/IniciaçãoCientífica/sons/";
		String revPath = "r189.30_04_11/";
		String runNum = "2";

		String fileName = mainPath + revPath + runNum + "/info.txt";
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder(2000);

		int numberOfRays = 5000;
		float maxTime = 1.f;

		// 4 band ranges in human limits
		BandRangeSeq range = new BandRangeHarmSeq(20.0, 20000.0, 4);
		float sampleRate = (float) range.getSR();
		sb.append("Sample rate: " + sampleRate + "\n");
		sb.append(range.howMany() + " central frequencies:" + "\n");
		for (Double d : range.getList()) {
			sb.append("\t" + d + "\n");
		}

		System.out.println("Obtaining energetic impulse responses...");
		long time = System.currentTimeMillis();

		SimulatorBin sim = new SimulatorBin();

		sim.setUp(numberOfRays, sampleRate);

		float[][] content = new float[range.howMany()][];

		MultiBandSimulationViewer viewer = new MultiBandSimulationViewer(
				mainPath + revPath + runNum + "/");

		content[0] = sim.simulateCoeff(0.01, 0.01, 0.001, 0.12, 0.05, 0.05);
		viewer.view(content[0], "sim1");
		content[1] = sim.simulateCoeff(0.01, 0.02, 0.04, 0.06, 0.14, 0.12);
		viewer.view(content[1], "sim2");
		content[2] = sim.simulateCoeff(0.02, 0.03, 0.001, 0.04, 0.16, 0.1);
		viewer.view(content[2], "sim3");
		content[3] = sim.simulateCoeff(0.02, 0.04, 0.07, 0.1, 0.02, 0.03);
		viewer.view(content[3], "sim4");

		time = System.currentTimeMillis() - time;
		sb.append("Simulation expended time:" + ((double) time) / 1000.0
				+ " s\n");

		System.out
				.println("Processing to get a multi-band impulse response...");
		time = System.currentTimeMillis();

		// TODO Direction managing
		Map<Float, Vector> directionMap = sim.getDirectionIr()
				.getEnergeticImpulseResponse();

		TreeSet<Float> orderedKeySet = new TreeSet<Float>(directionMap.keySet());

		int waveLength = (int) Math.ceil(orderedKeySet.last() * sampleRate) + 1;

		Vector[] wave = new Vector[waveLength];
		for (Float key : orderedKeySet) {
			int i = (int) Math.floor(key * sampleRate);
			wave[i] = directionMap.get(key);
		}

		BinauralMultiBandImpulseResponse mbir = new BinauralMultiBandImpulseResponse(
				range, content, wave, maxTime);

		double[] leftIr;
		double[] rightIr;

		leftIr = mbir.getLeftSignal();
		leftIr = ArrayUtils.scaleToUnit(leftIr);

		rightIr = mbir.getRightSignal();
		rightIr = ArrayUtils.scaleToUnit(rightIr);

		time = System.currentTimeMillis() - time;
		sb.append("Post processing expended time:" + ((double) time) / 1000.0
				+ " s\n");

		viewer.view(leftIr, 1.0, "LeftImpulseResponse");
		viewer.view(rightIr, 1.0, "RightImpulseResponse");

		System.out.println("Convolving...");

		time = System.currentTimeMillis();

		String archStr = mainPath + "44k.wav";
		String outStr = mainPath + revPath + runNum + "/conv.wav";

		int arch[] = WaveUtils.wavRead(archStr);
		double a[] = ArrayUtils.scaleToUnit(arch);

		double[] leftConv = new double[a.length + leftIr.length - 1];
		double[] rightConv = new double[a.length + rightIr.length - 1];

		NewSignal leftIrSig = new NewSignal(leftIr);
		NewSignal rightIrSig = new NewSignal(rightIr);
		NewSignal aSig = new NewSignal(a);
		NewSignal leftOut = new NewSignal(leftConv);
		NewSignal rightOut = new NewSignal(rightConv);

		Mockery mockery = new Mockery() {
			{
				setImposteriser(ClassImposteriser.INSTANCE);
			}
		};

		final IProgressMonitor bar = mockery.mock(IProgressMonitor.class);
		mockery.checking(new Expectations() {
			{
				ignoring(bar);
			}
		});

		leftOut = aSig.convolve(leftIrSig, bar);
		rightOut = aSig.convolve(rightIrSig, bar);

		if (leftConv.length != leftOut.size()) {
			System.out.println("Different sizes");
		}

		if (rightConv.length != rightOut.size()) {
			System.out.println("Different sizes");
		}

		for (int i = 0; i < leftOut.size(); i++) {
			leftConv[i] = leftOut.get(i).re();
		}
		for (int i = 0; i < rightOut.size(); i++) {
			rightConv[i] = rightOut.get(i).re();
		}

		double[] leftScaled = ArrayUtils.scaleToMax(leftConv,
				(double) WaveUtils.getLimit(16));
		double[] rightScaled = ArrayUtils.scaleToMax(rightConv,
				(double) WaveUtils.getLimit(16));
		double[][] scaled = new double[][] { leftScaled, rightScaled };

		WaveUtils.wavWrite(WaveUtils.joinAudioStream(scaled), 2, sampleRate,
				outStr);

		time = System.currentTimeMillis() - time;
		sb.append("Convolve expended time:" + ((double) time) / 1000.0 + " s\n");

		try {
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done.");
	}
}
