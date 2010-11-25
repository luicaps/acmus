package acmus.auralization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

import acmus.AcmusApplication;
import acmus.util.ArrayUtils;
import acmus.util.WaveUtils;

public class AurViewer {
	
	public void view(double[] array) {
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
				ArrayUtils.scaleToMax(array, WaveUtils.getLimit(16)),
				(float) AcmusApplication.SAMPLE_RATE, tempFile);
	}

	public void print(double[] array) throws IOException {
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

	public void plot(double[] array) throws InterruptedException, IOException {
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

}
