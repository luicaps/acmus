package acmus.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.widgets.ProgressBar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import acmus.AcmusApplication;
import acmus.dsp.NewSignal;
import acmus.simulation.AcousticSource;
import acmus.simulation.GeometricAcousticSimulation;
import acmus.simulation.Receptor;
import acmus.simulation.math.Vector;
import acmus.simulation.rtt.RayTracingGeometricAcousticSimulationImpl;
import acmus.simulation.rtt.Sector;
import acmus.simulation.structures.MonteCarloAcousticSource;
import acmus.simulation.structures.SphericalReceptor;
import acmus.util.ArrayUtils;
import acmus.util.WaveUtils;

public class AuralizationTest {
	private int arbitraryPowerOf2 = 32768;
	private AcousticSource soundSource;
	private int numberOfRays;
	private Vector soundSourceCenter;
	Vector sphericalReceptorCenter;
	private Receptor receptor;
	private double soundSpeed;
	private double mCoeficient;
	private int k;
	private List<Sector> sectors;
	private ProgressBar bar;

	// private static float EPS = 0.00001f;

	@Before
	public void setUp() {
		soundSourceCenter = new Vector(2, 2, 5);
		soundSource = new MonteCarloAcousticSource(soundSourceCenter);
		numberOfRays = 2;
		
		sectors = new ArrayList<Sector>();

		sphericalReceptorCenter = new Vector(8, 8, 6);
		float sphericalReceptorRadius = 3.0f;
		receptor = new SphericalReceptor(sphericalReceptorCenter,
				sphericalReceptorRadius);

		soundSpeed = 344.0; // in meters per second (m/s)
		mCoeficient = 0.0001;
		k = 500;

		Mockery mockery = new Mockery() {
			{
				setImposteriser(ClassImposteriser.INSTANCE);
			}
		};

		bar = mockery.mock(ProgressBar.class);
		mockery.checking(new Expectations() {
			{
				ignoring(bar);
			}
		});

	}

	private void setCoeff(double bottom, double top, double east, double west,
			double north, double south) {
		sectors.add(new Sector(new Vector(0, 0, 1), new Vector(1, 1, 0), bottom));
		sectors.add(new Sector(new Vector(0, 0, -1), new Vector(1, 1, 10), top));
		sectors.add(new Sector(new Vector(0, 1, 0), new Vector(1, 0, 1), east));
		sectors.add(new Sector(new Vector(1, 0, 0), new Vector(0, 1, 1), south));
		sectors.add(new Sector(new Vector(0, -1, 0), new Vector(1, 10, 1), west));
		sectors.add(new Sector(new Vector(-1, 0, 0), new Vector(10, 1, 1),
																		north));
	}

	private void simulate() {
		GeometricAcousticSimulation rts = new RayTracingGeometricAcousticSimulationImpl(
				sectors, soundSource, numberOfRays, receptor, soundSpeed,
				mCoeficient, k);
		rts.simulate(bar);

	}

	@Test
	public void signalSample() {
	
		// 4 band ranges
		int bands = 4;
		@SuppressWarnings("unchecked")
		Set<Float>[] orderedContentSet = new Set[bands];
		@SuppressWarnings("unchecked")
		Map<Float, Float>[] histogram = new Map[bands];

		setCoeff(0.2, 0.2, 0.1, 0.1, 0.1, 0.1);
		simulate();
		histogram[0] = receptor.getSimulatedImpulseResponse()
				.getEnergeticImpulseResponse();
		orderedContentSet[0] = new TreeSet<Float>(histogram[0].keySet());

		setCoeff(0.2, 0.2, 0.25, 0.25, 0.3, 0.3);
		simulate();
		histogram[1] = receptor.getSimulatedImpulseResponse()
				.getEnergeticImpulseResponse();
		orderedContentSet[1] = new TreeSet<Float>(histogram[1].keySet());

		setCoeff(0.17, 0.17, 0.13, 0.13, 0.34, 0.34);
		simulate();
		histogram[2] = receptor.getSimulatedImpulseResponse()
				.getEnergeticImpulseResponse();
		orderedContentSet[2] = new TreeSet<Float>(histogram[2].keySet());

		setCoeff(0.4, 0.4, 0.2, 0.2, 0.1, 0.1);
		simulate();
		histogram[3] = receptor.getSimulatedImpulseResponse()
				.getEnergeticImpulseResponse();
		orderedContentSet[3] = new TreeSet<Float>(histogram[3].keySet());

		int lengthMin = Math.min(
				histogram[0].size(),
				Math.min(histogram[1].size(),
						Math.min(histogram[2].size(), histogram[3].size())));
		int lengthMax = Math.max(
				histogram[0].size(),
				Math.max(histogram[1].size(),
						Math.max(histogram[2].size(), histogram[3].size())));

		double[][] spectralResponseArray = new double[lengthMax][arbitraryPowerOf2];
		NewSignal[] spectralResponseSignal = new NewSignal[lengthMax];

		double energy, pressure;
		for (int n = 0; n < bands; n++) {
			Float[] contentArray = orderedContentSet[n].toArray(new Float[0]);
			int length = contentArray.length;
			for (int j = 0; j < length; j++) {
				energy = histogram[n].get(contentArray[j]);
				pressure = Math.sqrt(energy); // Pass energy to pressure
				int faixaLength = spectralResponseArray.length / (bands * 2);
				for (int i = n * faixaLength; i <= (n + 1) * faixaLength; i++) {
					spectralResponseArray[j][i] = pressure;
					spectralResponseArray[j][spectralResponseArray.length
							- (n + 1)] = pressure;
				}
				spectralResponseSignal[j] = new NewSignal(
						spectralResponseArray[j]);
			}
		}

		NewSignal[] impulseResponseSignal = new NewSignal[lengthMin];

		for (int i = 0; i < lengthMin; i++) {
			impulseResponseSignal[i] = spectralResponseSignal[i].ifft();
			Assert.assertNotNull(impulseResponseSignal[i]);
		}

		double[] impulseResponseArray = new double[arbitraryPowerOf2];

		for (int i = 0; i < lengthMin; i++) {
			for (int index = 0; index < impulseResponseSignal[i].size() / 2; index++) {
				impulseResponseArray[impulseResponseSignal[i].size() / 2
						+ index] = impulseResponseSignal[i].get(index).re();
			}
			for (int index = impulseResponseSignal[i].size() / 2; index < impulseResponseSignal[i]
					.size(); index++) {
				impulseResponseArray[index - impulseResponseSignal[i].size()
						/ 2] = impulseResponseSignal[i].get(index).re();
			}
		}

		Assert.assertNotNull(impulseResponseArray);
		try {
			print(impulseResponseArray);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Couldn't print");
		}
		
//		extends Composite
//		
//		FileDialog fileDialog;
//		fileDialog = new FileDialog(getShell(), SWT.SAVE);
//		fileDialog.setFilterExtensions(new String[] { ".wav" });
//		fileDialog.setFilterNames(new String[] { "WAV file" });
//		fileDialog.setText("Save simulated Impulse Response as WAV");
//		
//		String filename = fileDialog.open();
//		
		WaveUtils.wavWrite(ArrayUtils.scaleToMax(impulseResponseArray, WaveUtils.getLimit(16)),
				(float)AcmusApplication.SAMPLE_RATE, "/tmp/wave.wav");
	}

//	@Test
//	public void globalTest() {
//		Assert.fail("Not yet implemented");
//	}

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
