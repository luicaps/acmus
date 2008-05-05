package acmus.graphics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

public class ChartBuilder {

	private XYDataset dataset;
	private PlotOrientation orientation;
	private JFreeChart chart;
	private XYSeries series = new XYSeries("Serie");

	public ChartBuilder(Map<Double, Double> map) {

		for (Map.Entry<Double, Double> e : map.entrySet()) {
			series.add(e.getKey(), e.getValue());
		}
		this.dataset = new XYSeriesCollection(series);

		this.orientation = PlotOrientation.VERTICAL;
	}

	public void save(OutputStream out) throws IOException {
		this.save(out, 550, 400);
	}

	public void save(OutputStream out, int x, int y) throws IOException {
		ChartUtilities.writeChartAsPNG(out, this.chart, x, y);
	}

	public void show(Composite parent) {
		
		this.chart = ChartFactory.createXYLineChart(null, "x", "y",
				this.dataset, this.orientation, false, false, false);
		this.chart.getPlot().setForegroundAlpha(0.5f);
	
		try {
			ChartUtilities.saveChartAsPNG(new File("/tmp/abc.png"), chart, 800, 600);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ChartComposite composite = new ChartComposite(parent, SWT.NONE, chart, true);
		composite.setVisible(true);
		parent.redraw();

	}
}
