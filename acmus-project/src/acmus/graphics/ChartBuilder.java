package acmus.graphics;

import java.util.Map;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class ChartBuilder {

	private DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	private PlotOrientation orientation;
	private JFreeChart chart;

	public ChartBuilder(Map<Double, Double> map) {
		map = new TreeMap<Double, Double>(map);

		for (Map.Entry<Double, Double> e : map.entrySet())
			dataset.setValue(e.getValue(), "energy", e.getKey());

		orientation = PlotOrientation.VERTICAL;
	}

	public JFreeChart getChart(String x, String y, String title) {
		chart = ChartFactory.createBarChart(title, x, y, dataset, orientation,
				false, false, false);

		chart.getPlot().setForegroundAlpha(0.5f);

		return chart;

	}
}
