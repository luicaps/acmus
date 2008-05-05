package acmus.graphics;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class ChartBuilder {
	
	private XYDataset dataset;
	private PlotOrientation orientation;
	private JFreeChart grafico;
	private XYSeries series = new XYSeries("Serie");

	public ChartBuilder(Map<Double, Double> map) {
			
			for (Map.Entry<Double, Double> e : map.entrySet()) {				
				series.add(e.getKey(), e.getValue());
			}
			this.dataset = new XYSeriesCollection(series);
		
		this.orientation = PlotOrientation.VERTICAL;
	}

	public void criaGrafico(String titulo) {
		this.grafico = ChartFactory.createXYLineChart(titulo, "x", "y", this.dataset, this.orientation, false, false, false);
		this.grafico.getPlot().setForegroundAlpha(0.5f);
	}
	
	public void salvar (OutputStream out) throws IOException {
		this.salvar(out, 550, 400);
	}
	
	public void salvar (OutputStream out, int x, int y) throws IOException {
		ChartUtilities.writeChartAsPNG(out, this.grafico,x, y);
	}
}
