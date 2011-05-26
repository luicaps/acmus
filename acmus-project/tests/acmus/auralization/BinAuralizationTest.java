package acmus.auralization;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import acmus.dsp.NewSignal;
import acmus.util.ArrayUtils;
import acmus.util.WaveUtils;
import acmus.util.math.Vector;

public class BinAuralizationTest {

	private String mainPath, revPath, runNum, path;
	private FileWriter fw;
	private StringBuilder sb;
	private long time;
	private MultiBandSimulationViewer viewer;
	private BandRangeSeq range;
	private float[][] content;
	private Vector[] directionArray;

	@Before
	public void setUp() {

		System.out.println("Setting up...");

		mainPath = "data" + File.separator + "tests" + File.separator;
		revPath = (DateFormat.getDateInstance(DateFormat.SHORT,
				Locale.getDefault())).format(Calendar.getInstance().getTime())
				.replace('/', '_') + File.separator;
		runNum = "r1";

		path = mainPath + revPath + runNum + File.separator;
		fw = null;
		try {
			File file = new File(path, "info.txt");
			fw = new FileWriter(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sb = new StringBuilder(2000);

		int numberOfRays = 5000;

		// 4 band ranges in human limits
		range = new BandRangeHarmSeq(20.0, 20000.0, 4);
		float sampleRate = (float) range.getSR();
		sb.append("Sample rate: " + sampleRate + "\n");
		sb.append(range.howMany() + " central frequencies:" + "\n");
		for (Double d : range.getList()) {
			sb.append("\t" + d + "\n");
		}

		System.out.println("Obtaining energetic impulse responses...");
		time = System.currentTimeMillis();

		SimulatorBin sim = new SimulatorBin();

		sim.setUp(numberOfRays, sampleRate);

		content = new float[range.howMany()][];

		viewer = new MultiBandSimulationViewer(mainPath + revPath + runNum
				+ "/");

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

		Map<Float, Vector> directionMap = sim.getDirectionIr()
				.getEnergeticImpulseResponse();

		TreeSet<Float> orderedKeySet = new TreeSet<Float>(directionMap.keySet());

		int waveLength = (int) Math.ceil(orderedKeySet.last() * sampleRate) + 1;

		directionArray = new Vector[waveLength];
		for (Float key : orderedKeySet) {
			int i = (int) Math.floor(key * sampleRate);
			directionArray[i] = directionMap.get(key);
		}
	}

	@Test
	public void auralizazationTest() {

		float maxTime = 1.f;

		// HRTFselector hrtfSelector = new CipicOctaveHRTFselector();
		HRTFselector hrtfSelector = new CipicJavaHRTFselector();
		BinauralMultiBandImpulseResponse aur = new BinauralMultiBandImpulseResponse(
				range, content, directionArray, hrtfSelector, maxTime);

		double[] leftIr;
		double[] rightIr;

		leftIr = aur.getLeftSignal();
		leftIr = ArrayUtils.scaleToUnit(leftIr);

		rightIr = aur.getRightSignal();
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

		double a[] = null;

		try {
			AudioInputStream ais = AudioSystem
					.getAudioInputStream(new BufferedInputStream(
							new FileInputStream(new File(archStr))));
			if (ais.getFormat().getChannels() > 1) {
				System.out.println("Please use mono audio files, "
						+ "we are using now for the auralization "
						+ "only the first channel.");
			}

			a = WaveUtils.wavReadSplitDouble(ais)[0];

			ais.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}

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
			Assert.fail("Different sizes");
		}

		if (rightConv.length != rightOut.size()) {
			Assert.fail("Different sizes");
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

		float sampleRate = (float) range.getSR();
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
