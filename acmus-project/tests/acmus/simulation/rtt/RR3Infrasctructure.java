package acmus.simulation.rtt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.eclipse.swt.widgets.ProgressBar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import acmus.AcmusApplication;
import acmus.simulation.AcousticSource;
import acmus.simulation.SimulatedImpulseResponse;
import acmus.simulation.math.Vector;
import acmus.simulation.structures.NormalDeviateAcousticSource;
import acmus.util.ArrayUtils;
import acmus.util.WaveUtils;

public abstract class RR3Infrasctructure {

	private String filename;
	private List<Vector> rays;
	private ProgressBar bar;
	public final double soundSpeed = 343.21; //meters per second 
	
	public RR3Infrasctructure(int numberOfRays, Vector source, String filename) {
		this.filename = filename;
		
//		MonteCarloAcousticSource ras = new MonteCarloAcousticSource();
		AcousticSource ras = new NormalDeviateAcousticSource(source);
		rays = ras.manyDirections(numberOfRays);

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
	
	protected void salvaIR(SimulatedImpulseResponse sir) {
		TreeSet<Float> orderedKeySet = new TreeSet<Float>(sir.getEnergeticImpulseResponse().keySet());
		int waveLength = (int) Math.ceil(orderedKeySet.last() * AcmusApplication.SAMPLE_RATE);
		
		double max = 0.0;
		double[] wave = new double[waveLength];
		for (Float key : orderedKeySet) {
			int i = (int) Math.floor(key * AcmusApplication.SAMPLE_RATE);
			
			wave[i] += sir.getEnergeticImpulseResponse().get(key);
			
			if(max < wave[i])
				max = wave[i];
		}
		
		salvaTXT(wave, max);

//		WaveUtils.wavWrite(ArrayUtils.scaleToMax(wave, WaveUtils.getLimit(16)), filename + ".wav");
	}

	public static void converteRIParaWave(String filename) {
		double[] wave = new double[64510];

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			
			String linha = null;
			int i=0;
			while((linha = br.readLine()) != null) {
				wave[i] = Double.valueOf(linha.substring(0, 12));
				i++;
			}
		
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		WaveUtils.wavWrite(ArrayUtils.scaleToMax(wave, WaveUtils.getLimit(16)), filename + ".wav");
	}
	
	protected void salvaTXT(double[] wave, double max) {
		FileWriter fw = null;
		
		try {
			fw = new FileWriter(filename + ".txt");
			
			final double delta = 1.0/44100;
			double acumulado = 0.0;
			max = 1/max;
			for (int i = 0; i < wave.length; i++){
				fw.write(String.format(Locale.US, "%13.10f  %13.10f \n", (wave[i]*max), acumulado));
				acumulado += delta;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<Vector> getRays() {
		return rays;
	}
	
	public ProgressBar getBar() {
		return bar;
	}
	
	public static Vector getSource(String sourcename) {
		if("S1".equals(sourcename))
			return new Vector(1.5f, 3.5f, 1.5f);
		if("S2".equals(sourcename))
			return new Vector(-1.5f, 5.5f, 1.5f);
		
		throw new InvalidParameterException("S1 ou S2");
	}
	
	public static Vector getReceptor(String receptorname) {
		if("R01".equals(receptorname))
			return new Vector(-2.0f, 3.0f, 1.2f);
		if("R02".equals(receptorname))
			return new Vector(2.0f, 6.0f, 1.2f);
		if("R03".equals(receptorname))
			return new Vector(0.0f, 7.5f, 1.2f);
		
		throw new InvalidParameterException("R01, R02 ou R03");
	}
}
