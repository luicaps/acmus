package acmus.graphics;

import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartBuilder {

	private XYDataset dataset;
	private PlotOrientation orientation;
	private JFreeChart chart;
	private XYSeries series = new XYSeries("Serie");

	public ChartBuilder(Map<Double, Double> map) {

		for (Map.Entry<Double, Double> e : map.entrySet())
			this.series.add(e.getKey(), e.getValue());
		this.dataset = new XYSeriesCollection(this.series);

		this.orientation = PlotOrientation.VERTICAL;
	}

	public JFreeChart getChart(String x, String y, String title) {

		this.chart = ChartFactory.createXYLineChart(title, x, y, this.dataset,
				this.orientation, false, false, false);
		this.chart.getPlot().setForegroundAlpha(0.5f);

		return this.chart;

	}
}
