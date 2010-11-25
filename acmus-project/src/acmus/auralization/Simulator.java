package acmus.auralization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import acmus.simulation.structures.MonteCarloAcousticSource;
import acmus.simulation.structures.SphericalReceptor;

public class Simulator {
	private AcousticSource soundSource;
	private int numberOfRays;
	private Vector soundSourceCenter;
	private Vector sphericalReceptorCenter;
	private Receptor receptor;
	private double soundSpeed;
	private double mCoeficient;
	private int k;
	private List<Sector> sectors;

	private ProgressBar bar;
	
	public void setUp() {
		soundSourceCenter = new Vector(2, 2, 5);
		soundSource = new MonteCarloAcousticSource(soundSourceCenter);
		numberOfRays = 100;

		sectors = new ArrayList<Sector>();

		sphericalReceptorCenter = new Vector(8, 8, 6);
		float sphericalReceptorRadius = 3.0f;
		receptor = new SphericalReceptor(sphericalReceptorCenter,
				sphericalReceptorRadius);

		soundSpeed = 344.0; // in meters per second (m/s)
		mCoeficient = 0.0001;
		k = 500;

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

	public Float[] simulateCoeff(double bottom, double top, double east,
			double west, double north, double south) {
		sectors.add(new Sector(new Vector(0, 0, 1), new Vector(1, 1, 0), bottom));
		sectors.add(new Sector(new Vector(0, 0, -1), new Vector(1, 1, 10), top));
		sectors.add(new Sector(new Vector(0, 1, 0), new Vector(1, 0, 1), east));
		sectors.add(new Sector(new Vector(1, 0, 0), new Vector(0, 1, 1), south));
		sectors.add(new Sector(new Vector(0, -1, 0), new Vector(1, 10, 1), west));
		sectors.add(new Sector(new Vector(-1, 0, 0), new Vector(10, 1, 1),
				north));

		Map<Float, Float> histogram;
		Set<Float> orderedContentSet;

		GeometricAcousticSimulation rts = new RayTracingGeometricAcousticSimulationImpl(
				sectors, soundSource, numberOfRays, receptor, soundSpeed,
				mCoeficient, k);
		rts.simulate(bar);
		histogram = receptor.getSimulatedImpulseResponse()
				.getEnergeticImpulseResponse();
		orderedContentSet = new TreeSet<Float>(histogram.keySet());
		sectors.clear();
		return orderedContentSet.toArray(new Float[0]);

	}
}
