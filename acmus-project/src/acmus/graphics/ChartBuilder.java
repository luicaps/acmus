package acmus.graphics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class ChartBuilder {
	private XYSeriesCollection dataset;
	private PlotOrientation orientation;
	private JFreeChart grafico;

	public ChartBuilder(Map<Double, Double> map) {
		
		XYSeries series = new XYSeries("XYGraph");
		for (Double chave : map.keySet()) {
			series.add(chave, map.get(chave));
			System.out.println(chave + " " + map.get(chave));
		}
		
		this.dataset = new XYSeriesCollection();
		this.dataset.addSeries(series);
		
		this.orientation = PlotOrientation.VERTICAL;
	}

	public void criaGrafico(String titulo) {
		this.grafico = ChartFactory.createXYBarChart(titulo, "a", false, "b", this.dataset, this.orientation, true, true, false);
		this.grafico.getPlot().setForegroundAlpha(0.5f);
	}
	
	public void salvar (OutputStream out) throws IOException {
		this.salvar(out, 550, 400);
	}
	
	public void salvar (OutputStream out, int x, int y) throws IOException {
		ChartUtilities.writeChartAsPNG(out, this.grafico,x, y);
	}
}
