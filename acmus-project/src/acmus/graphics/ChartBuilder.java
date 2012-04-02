package acmus.graphics;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.DataUtilities;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
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
    
    public HistogramBuilder getHistogram(int bins,double[] freqRange,String[] freqRangeName) {
    	return new HistogramBuilder(bins,freqRange,freqRangeName);
    }

    public class HistogramBuilder {
		private static final int DEFAULT_BINS = 300;
		private  IntervalXYDataset dataset;
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
		
/*		public HistogramBuilder(int bins,double[] freqRange) {
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
		
	*/	
		
		private DefaultKeyedValues dataFreqBandHist;
		private CategoryDataset datasetFreqBandHist;
		
		public HistogramBuilder(int bins,double[] freqRange,String[] freqRangeName) {
							
			dataFreqBandHist = new DefaultKeyedValues();
			
			for (int i=0;i<bins;i++)
				dataFreqBandHist.addValue(freqRangeName[i], 0.0);
            
            setLabels();
		
		}
		
		
	
		public HistogramBuilder addData(Vector<Double> data, String title, double weight) {
		    double[] points = new double[data.size()];
		   
		    for (int i = 0; i < points.length; i++) {
			  points[i] = weight*data.get(i);
		    }
		    
		    ( (HistogramDataset) dataset).addSeries(title, points, DEFAULT_BINS);
		    
		    
		     return this;
		}
		
		public HistogramBuilder addDataByRange(Vector<Double> data, double[] freqRange,String[] freqRangeName, int nRanges, double weight) {
		    
			double points[] =  new double[nRanges];
		   
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
		    //weighting the modes
		    for (int j=0;j<nRanges;j++){
		    	points[j] *= weight;
		    	
		    	this.dataFreqBandHist.setValue(freqRangeName[j], this.dataFreqBandHist.getValue(freqRangeName[j]).doubleValue()+points[j]);
		    	
		    }
		    
		   /*
			SimpleHistogramDataset simpleHistDataset = (SimpleHistogramDataset)this.dataset;
			for (int i = 0; i < data.size(); i++) {
			  double tmpVal = 	data.get(i);
			  simpleHistDataset.addObservation(tmpVal);
			}
			*/
			//dataset.addSeries("", points, DEFAULT_BINS);
		    
		    return this;
		}
	    private KeyedValues createModalDensity(){
	    	DefaultKeyedValues modalDensityDataset =  new DefaultKeyedValues();
	    	for (int i=0;i<this.dataFreqBandHist.getItemCount();i++){
	    		double centreFreq = Double.parseDouble(((String)dataFreqBandHist.getKey(i)));
	    		double modes = dataFreqBandHist.getValue(i).doubleValue();
	    		double density = modes/centreFreq;
	    		modalDensityDataset.addValue(dataFreqBandHist.getKey(i), density);
	    	}
	    	return modalDensityDataset;
	    }
	    
	    public JFreeChart buildLineChart() {
	    	
	    	
	    	final KeyedValues modalDensity = createModalDensity();
			
	    	final CategoryDataset dataset2 = DatasetUtilities.createCategoryDataset(
		            "Modal Density", modalDensity
		        );	    	
	    	JFreeChart chart = ChartFactory.createBarChart(
		            title,  // chart title
		            xLabel,                     // domain axis label
		            yLabel,                     // range axis label
		            dataset2,            // data
		            PlotOrientation.VERTICAL,
		            true,                           // include legend
		            true,
		            false
		        );
	    	
	    	// get a reference to the plot for further customisation...
	        final CategoryPlot plot = chart.getCategoryPlot();
	    	
	        final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
	        plot.setRenderer(renderer2);
	        
	    	return chart;
	    	
	    }
	    
		public JFreeChart buildBarChart() {
			datasetFreqBandHist = DatasetUtilities.createCategoryDataset("FrequencyBands", dataFreqBandHist);
	        

	        
			JFreeChart chart = ChartFactory.createBarChart(
		            title,  // chart title
		            xLabel,                     // domain axis label
		            yLabel,                     // range axis label
		            datasetFreqBandHist,            // data
		            PlotOrientation.VERTICAL,
		            true,                           // include legend
		            true,
		            false
		        );
			
            
			
	        // get a reference to the plot for further customisation...
	        final CategoryPlot plot = chart.getCategoryPlot();

	        final CategoryAxis domainAxis = plot.getDomainAxis();
	        domainAxis.setLowerMargin(0.02);
	        domainAxis.setUpperMargin(0.02);

					
			return chart;
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
