package acmus.auralization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.swt.widgets.ProgressBar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import acmus.simulation.AcousticSource;
import acmus.simulation.GeometricAcousticSimulation;
import acmus.simulation.Receptor;
import acmus.simulation.math.Vector;
import acmus.simulation.rtt.RayTracingGeometricAcousticSimulationImpl;
import acmus.simulation.rtt.Sector;
import acmus.simulation.structures.DirectionImpulseResponse;
import acmus.simulation.structures.MonteCarloAcousticSource;
import acmus.simulation.structures.SphericalReceptor;

/**
 * Class for dealing with the geometric aspects of a multi-band simulation
 * 
 * @author migmruiz
 * 
 */
public class SimulatorBin {
	private AcousticSource soundSource;
	private int numberOfRays;
	private float sampleRate;
	private Vector soundSourceCenter;
	private Vector sphericalReceptorCenter;
	private Receptor receptor;
	private double soundSpeed;
	private double mCoeficient;
	private int k;
	private List<Sector> sectors;

	private ProgressBar bar;

	private DirectionImpulseResponse directionIr;

	/**
	 * set up the general aspects
	 * 
	 * @param numberOfRays
	 *            the number of rays in each simulation
	 * @param sampleRate
	 *            the sample rate of the simulation
	 */
	public void setUp(int numberOfRays, float sampleRate) {

		this.numberOfRays = numberOfRays;
		this.sampleRate = sampleRate;

		this.soundSourceCenter = new Vector(2, 2, 2);
		this.soundSource = new MonteCarloAcousticSource(soundSourceCenter);

		this.sectors = new ArrayList<Sector>();

		this.sphericalReceptorCenter = new Vector(8, 8, 1);
		float sphericalReceptorRadius = 3.0f;
		this.directionIr = new DirectionImpulseResponse(1/44100);
		this.receptor = new SphericalReceptor(sphericalReceptorCenter,
				sphericalReceptorRadius);

		this.soundSpeed = 344.0; // in meters per second (m/s)
		this.mCoeficient = 0.0001;
		this.k = 500;

		Mockery mockery = new Mockery() {
			{
				setImposteriser(ClassImposteriser.INSTANCE);
			}
		};

		this.bar = mockery.mock(ProgressBar.class);
		mockery.checking(new Expectations() {
			{
				ignoring(bar);
			}
		});

	}

	/**
	 * Perform the simulation for custom absorption coefficients
	 * 
	 * @param bottom
	 * @param top
	 * @param east
	 * @param west
	 * @param north
	 * @param south
	 * @return a energetic simulated impulse response array
	 */
	public float[] simulateCoeff(double bottom, double top, double east,
			double west, double north, double south) {
		sectors.add(new Sector(new Vector(0, 0, 1), new Vector(1, 1, 0), bottom));
		sectors.add(new Sector(new Vector(0, 0, -1), new Vector(1, 1, 4), top));
		sectors.add(new Sector(new Vector(0, 1, 0), new Vector(1, 0, 1), east));
		sectors.add(new Sector(new Vector(1, 0, 0), new Vector(0, 1, 1), south));
		sectors.add(new Sector(new Vector(0, -1, 0), new Vector(1, 10, 1), west));
		sectors.add(new Sector(new Vector(-1, 0, 0), new Vector(10, 1, 1),
				north));

		Map<Float, Float> histogram;

		GeometricAcousticSimulation rts = new RayTracingGeometricAcousticSimulationImpl(
				sectors, soundSource, numberOfRays, receptor, soundSpeed,
				mCoeficient, k);
		rts.simulate(bar);
		sectors.clear();

		histogram = receptor.getSimulatedImpulseResponse()
				.getEnergeticImpulseResponse();

		TreeSet<Float> orderedKeySet = new TreeSet<Float>(histogram.keySet());

		int waveLength = (int) Math.ceil(orderedKeySet.last() * sampleRate) + 1;

		float[] wave = new float[waveLength];
		for (Float key : orderedKeySet) {
			int i = (int) Math.floor(key * sampleRate);
			wave[i] = histogram.get(key).floatValue();
		}

		return wave;
	}
	
	public DirectionImpulseResponse getDirectionIr() {
		return directionIr;
	}
}
