package acmus.auralization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import acmus.AcmusApplication;
import acmus.util.ArrayUtils;
import acmus.util.WaveUtils;

/**
 * Class to plot charts and create archives of a multi-band simulation
 * 
 * @author migmruiz
 * 
 */
public class MultiBandSimulationViewer {

	private String path;

	public MultiBandSimulationViewer() {
		this(System.getProperty("java.io.tmpdir", "/tmp")
				+ System.getProperty("file.separator"));
	}

	public MultiBandSimulationViewer(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}

	public void view(float[] floats, String title) {
		view(floats, 1.f, title);
	}

	public void view(float[] floats, float rate, String title) {
		XYBarRenderer renderer = new XYBarRenderer();
		view(floats, rate, title, renderer);
	}

	public void view(float[] floats, float rate, String title,
			XYItemRenderer renderer) {
		double[] doubles = new double[floats.length];
		for (int i = 0; i < floats.length; i++) {
			doubles[i] = floats[i];
		}
		view(doubles, rate, title, renderer);
	}

	public void view(float[] floats, String title, XYItemRenderer renderer) {
		double[] doubles = new double[floats.length];
		for (int i = 0; i < floats.length; i++) {
			doubles[i] = floats[i];
		}
		view(doubles, title, renderer);
	}

	public void view(Float[] floats, Float rate, String title,
			XYItemRenderer renderer) {
		double[] doubles = new double[floats.length];
		for (int i = 0; i < floats.length; i++) {
			doubles[i] = floats[i];
		}
		view(doubles, rate.doubleValue(), title, renderer);
	}

	public void view(Float[] floats, Float rate, String title) {
		double[] doubles = new double[floats.length];
		for (int i = 0; i < floats.length; i++) {
			doubles[i] = floats[i];
		}
		view(doubles, rate.doubleValue(), title);
	}

	public void view(Float[] floats, String title, XYItemRenderer renderer) {
		double[] doubles = new double[floats.length];
		for (int i = 0; i < floats.length; i++) {
			doubles[i] = floats[i];
		}
		view(doubles, title, renderer);
	}

	public void view(Float[] floats, String title) {
		double[] doubles = new double[floats.length];
		for (int i = 0; i < floats.length; i++) {
			doubles[i] = floats[i];
		}
		view(doubles, title);
	}

	public void view(double[] array, String title) {
		view(array, 1 / AcmusApplication.SAMPLE_RATE, title);
	}

	public void view(double[] array, String title, XYItemRenderer renderer) {
		view(array, 1 / AcmusApplication.SAMPLE_RATE, title, renderer);
	}

	public void view(double[] array, double rate, String title) {
		view(array, rate, title, new XYDotRenderer());
	}

	public void view(double[] array, double rate, String title,
			XYItemRenderer renderer) {
		try {
			print(array, rate, title);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		try {
			plot(array, rate, title, renderer);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		String fileName = path + "wave" + title + ".wav";
		WaveUtils.wavWrite(
				ArrayUtils.scaleToMax(array, WaveUtils.getLimit(16)),
				(float) AcmusApplication.SAMPLE_RATE, fileName);
	}

	public void print(double[] array, double rate, String title)
			throws IOException {
		String fileName = path + "wave" + title + ".csv";
		FileWriter fw = new FileWriter(fileName);
		StringBuilder sb = new StringBuilder(2000);

		for (int i = 0; i < array.length; i++) {

			sb.append(i * rate);
			sb.append(", ");
			sb.append(array[i]);
			sb.append("\n");

		}
		fw.write(sb.toString());
		fw.close();
	}

	public void plot(double[] array, double rate, String title,
			XYItemRenderer renderer) throws InterruptedException, IOException {
		int width = 800, height = 600;

		// data set...
		final XYSeries series = new XYSeries(title);
		for (int i = 0; i < array.length; i++) {
			series.add(i * rate, array[i]);
		}
		final XYDataset data = new XYSeriesCollection(series);

		// create a scatter chart...
		final boolean noLegend = false;
		final JFreeChart chart = ChartFactory.createScatterPlot(title, "X",
				"Y", data, PlotOrientation.VERTICAL, noLegend, false, false);

		final XYPlot plot = chart.getXYPlot();

		plot.setRenderer(renderer);

		// save it to png file
		String fileName = path + "wave" + title + ".png";
		File file = new File(fileName);

		ChartUtilities.saveChartAsPNG(file, chart, width, height);

		// show chart...
		// ChartFrame frame = new ChartFrame(title, chart);
		// frame.pack();
		// frame.setVisible(true);

		// waits 15 seconds...
		// Thread.sleep(15000l);
	}
}
