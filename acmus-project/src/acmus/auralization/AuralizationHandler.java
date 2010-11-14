package acmus.auralization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.widgets.ProgressBar;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

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

public class AuralizationHandler {
	private AcousticSource soundSource;
	private int numberOfRays;
	private Vector soundSourceCenter;
	private Vector sphericalReceptorCenter;
	private Receptor receptor;
	private double soundSpeed;
	private double mCoeficient;
	private int k;
	private List<Sector> sectors;

	private ProgressBar bar;

	public void signalSample(BandRangeSeq range, Float[][] content) {
		if (content.length != range.howMany()){
			throw new IllegalArgumentException("the content's length must be the number of values in range");
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
				if (j != 0) {
					arrTemp[Math.round((float) (j / factor)) - 1] = (sigTemp
							.get(j).re() + arrTemp[Math
							.round((float) (j / factor)) - 1]) / 2;
				}
				arrTemp[Math.round((float) (j / factor))] = sigTemp.get(j).re()
						+ arrTemp[Math.round((float) (j / factor))] / 2;
				if (j != arbitraryPowerOf2) {
					arrTemp[(int) (j / factor) + 1] = (sigTemp
							.get(j).re() + arrTemp[(int)(j / factor) + 1]) / 2;
				}
			}
			for (int j = 0; j < AcmusApplication.SAMPLE_RATE / 2; j++) {
				impulseResponseArray[i + (int) AcmusApplication.SAMPLE_RATE / 2
						+ j] += arrTemp[j];
				impulseResponseArray[i + j] += arrTemp[j
						+ (int) AcmusApplication.SAMPLE_RATE / 2]; // shift
																	// circular
			}
		}
		
		view(impulseResponseArray);
		
	}
	

	public void setUp() {
		soundSourceCenter = new Vector(2, 2, 5);
		soundSource = new MonteCarloAcousticSource(soundSourceCenter);
		numberOfRays = 100;

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

	public Float[] simulateCoeff(double bottom, double top, double east,
			double west, double north, double south) {
		sectors.add(new Sector(new Vector(0, 0, 1), new Vector(1, 1, 0), bottom));
		sectors.add(new Sector(new Vector(0, 0, -1), new Vector(1, 1, 10), top));
		sectors.add(new Sector(new Vector(0, 1, 0), new Vector(1, 0, 1), east));
		sectors.add(new Sector(new Vector(1, 0, 0), new Vector(0, 1, 1), south));
		sectors.add(new Sector(new Vector(0, -1, 0), new Vector(1, 10, 1), west));
		sectors.add(new Sector(new Vector(-1, 0, 0), new Vector(10, 1, 1),
				north));

		Map<Float, Float> histogram;
		Set<Float> orderedContentSet;

		GeometricAcousticSimulation rts = new RayTracingGeometricAcousticSimulationImpl(
				sectors, soundSource, numberOfRays, receptor, soundSpeed,
				mCoeficient, k);
		rts.simulate(bar);
		histogram = receptor.getSimulatedImpulseResponse()
				.getEnergeticImpulseResponse();
		orderedContentSet = new TreeSet<Float>(histogram.keySet());
		sectors.clear();
		return orderedContentSet.toArray(new Float[0]);

	}
	
	private void view(double[] array) {
		try {
			print(array);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		try {
			plot(array);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		String tempFile = System.getProperty("java.io.tmpdir", "/tmp")
				+ System.getProperty("file.separator") + "wave.wav";
		WaveUtils.wavWrite(
				ArrayUtils.scaleToMax(array,
						WaveUtils.getLimit(16)),
				(float) AcmusApplication.SAMPLE_RATE, tempFile);
	}

	private void print(double[] array) throws IOException {
		String tempFile = System.getProperty("java.io.tmpdir", "/tmp")
				+ System.getProperty("file.separator") + "wavelet.csv";
		FileWriter fw = new FileWriter(tempFile);
		StringBuilder sb = new StringBuilder(2000);

		for (int i = 0; i < array.length; i++) {

			sb.append(i / AcmusApplication.SAMPLE_RATE);
			sb.append(", ");
			sb.append(array[i]);
			sb.append("\n");

		}
		fw.write(sb.toString());
		fw.close();
	}

	private void plot(double[] array) throws InterruptedException, IOException {
		int width = 400, height = 300;

		// data set...
		final XYSeries series = new XYSeries("Impulse Response");
		for (int i = 0; i < array.length; i++) {
			series.add(i / AcmusApplication.SAMPLE_RATE, array[i]);
		}
		final XYDataset data = new XYSeriesCollection(series);

		// create a scatter chart...
		final boolean noLegend = false;
		final JFreeChart chart = ChartFactory.createScatterPlot(
				"Impulse Response", "X", "Y", data, PlotOrientation.VERTICAL,
				noLegend, false, false);

		final XYPlot plot = chart.getXYPlot();
		plot.setRenderer(new XYDotRenderer());

		// save it to png file
		String tempFile = System.getProperty("java.io.tmpdir", "/tmp")
				+ System.getProperty("file.separator") + "wavelet.png";
		File file = new File(tempFile);

		ChartUtilities.saveChartAsPNG(file, chart, width, height);

		// show chart...
		ChartFrame frame = new ChartFrame("Auralization Test", chart);
		frame.pack();
		frame.setVisible(true);

		// waits 15 seconds...
		// Thread.sleep(15000l);
	}

	public static void main(String[] args) {
		
		AuralizationHandler aur = new AuralizationHandler();

		aur.setUp();
		
		BandRangeSeq range = new BandRangeEqSeq(20.0, 20000.0, 4);
		
		Float[][] content = new Float[range.howMany()][];

		content[0] = aur.simulateCoeff(0.2, 0.2, 0.1, 0.1, 0.1, 0.1);
		content[1] = aur.simulateCoeff(0.2, 0.2, 0.25, 0.25, 0.3, 0.3);
		content[2] = aur.simulateCoeff(0.17, 0.17, 0.13, 0.13, 0.34, 0.34);
		content[3] = aur.simulateCoeff(0.4, 0.4, 0.2, 0.2, 0.1, 0.1);
		

		// 4 band ranges in human limits
		aur.signalSample(range, content);
	}
}
