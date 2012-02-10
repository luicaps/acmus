package acmus.graphics;

import java.awt.Font;
import java.util.Map;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

public class ChartBuilder {

    private IntervalXYDataset createDataset(Map<Double, Double> map) {
		XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
		XYIntervalSeries series = new XYIntervalSeries("energy");
	
		for (Map.Entry<Double, Double> e : map.entrySet()) {
		    series.add(e.getKey(), e.getKey(), e.getKey(), e.getValue(), e.getValue(), e.getValue());
		}
	
		dataset.addSeries(series);
		return dataset;
    }

    public JFreeChart getChart(Map<Double, Double> map, String x, String y,
	    String title) {
		Font labelFont = new Font(null, Font.BOLD, 18);
		Font tickFont = new Font(null, Font.PLAIN, 15);
	
		JFreeChart chart = ChartFactory.createHistogram(title, x, y,
			createDataset(map), PlotOrientation.VERTICAL, false, false,
			false);
	
		// Setting the fonts for the axis
		chart.getXYPlot().getRangeAxis().setLabelFont(labelFont);
		chart.getXYPlot().getRangeAxis().setTickLabelFont(tickFont);
		chart.getXYPlot().getDomainAxis().setLabelFont(labelFont);
		chart.getXYPlot().getDomainAxis().setTickLabelFont(tickFont);
	
		return chart;
    }

    public HistogramBuilder getHistogram() {
    	return new HistogramBuilder();
    }
    
    public HistogramBuilder getHistogram(int bins,double[] freqRange) {
    	return new HistogramBuilder(bins,freqRange);
    }

    public class HistogramBuilder {
		private static final int DEFAULT_BINS = 300;
		private final IntervalXYDataset dataset;
		private String title;
		private String xLabel;
		private String yLabel;
        
		private void setLabels(){
		    title = "";
		    xLabel = "";
		    yLabel = "";			
		}
		public HistogramBuilder() {
		    dataset = new HistogramDataset();
            setLabels();
		}
		
		public HistogramBuilder(int bins,double[] freqRange) {
			SimpleHistogramDataset simpleHistDataset = new SimpleHistogramDataset("");
			//simpleHistDataset .
			for (int i=0;i<bins;i++){
				double lower = freqRange[i];
				double upper = freqRange[i+1];
				SimpleHistogramBin  bin = new SimpleHistogramBin(lower,upper,true,false); 
				simpleHistDataset.addBin(bin);
			}
		    this.dataset = simpleHistDataset;
            setLabels();
		}
	
		public HistogramBuilder addData(Vector<Double> data, String title) {
		    double[] points = new double[data.size()];
		    for (int i = 0; i < points.length; i++) {
			points[i] = data.get(i);
		    }
		    
		    ( (HistogramDataset) dataset).addSeries(title, points, DEFAULT_BINS);
		    
		    return this;
		}
		
		public HistogramBuilder addDataByRange(Vector<Double> data, double[] freqRange, int nRanges) {
		    
			
		   /* 
		    for (int i = 0; i < data.size(); i++) {
		    	double tmpFreq =data.get(i);
		    	for (int j=0;j<nRanges;j++){
		    	  if ( (tmpFreq>=freqRange[j]) && (tmpFreq<freqRange[j+1]) ){
			        points[j] = points[j]+1;
		    	  //So that every data point only is counted once
		    	    break;
		    	  }
		    	}
		    }
		    */
			SimpleHistogramDataset simpleHistDataset = (SimpleHistogramDataset)this.dataset;
			for (int i = 0; i < data.size(); i++) {
			  double tmpVal = 	data.get(i);
			  simpleHistDataset.addObservation(tmpVal);
			}
			//dataset.addSeries("", points, DEFAULT_BINS);
		    
		    return this;
		}
	
		public HistogramBuilder setTitle(String title) {
		    this.title = title;
		    return this;
		}
	
		public HistogramBuilder setAxisLabels(String xLabel, String yLabel) {
		    this.xLabel = xLabel;
		    this.yLabel = yLabel;
		    return this;
		}
	
		public JFreeChart build() {
		    Font labelFont = new Font(null, Font.BOLD, 18);
		    Font tickFont = new Font(null, Font.PLAIN, 15);
	
		    JFreeChart chart = ChartFactory.createHistogram(title, xLabel,
			    yLabel, dataset, PlotOrientation.VERTICAL, true, false,
			    false);
	
		    chart.getXYPlot().setForegroundAlpha(0.75f);
		    // Setting the fonts for the axis
		    chart.getXYPlot().getRangeAxis().setLabelFont(labelFont);
		    chart.getXYPlot().getRangeAxis().setTickLabelFont(tickFont);
		    chart.getXYPlot().getDomainAxis().setLabelFont(labelFont);
		    chart.getXYPlot().getDomainAxis().setTickLabelFont(tickFont);
	
		    return chart;
		}
		

    }
}
