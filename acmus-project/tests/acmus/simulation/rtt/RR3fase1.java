package acmus.simulation.rtt;

import java.util.ArrayList;
import java.util.List;

import acmus.simulation.AcousticSource;
import acmus.simulation.GeometricAcousticSimulation;
import acmus.simulation.Receptor;
import acmus.simulation.SimulatedImpulseResponse;
import acmus.simulation.math.Vector;
import acmus.simulation.rtt.RayTracingGeometricAcousticSimulationImpl;
import acmus.simulation.rtt.Sector;
import acmus.simulation.structures.MonteCarloAcousticSource;
import acmus.simulation.structures.SphericalReceptor;

public class RR3fase1 extends RR3Infrasctructure {

	private List<Sector> sectors;
	private AcousticSource soundSource;
	private Vector sphericalReceptorCenter;
	private double sphericalReceptorRadius;
		
	public void setUp(Vector source, Vector receptorCenter, double receptorRadius){
		sectors = new ArrayList<Sector>();
		sectors.add(new Sector(new Vector(0, 0, 1), new Vector(4.22f, 0, 0), 0.1)); // floor (1)
		sectors.add(new Sector(new Vector(0, 0, -1), new Vector(4.22f, 0, 5.23f), 0.1)); // ceil (7)
		sectors.add(new Sector(new Vector(0, 1, 0), new Vector(4.22f, 0, 0), 0.1)); // walls (2)
		sectors.add(new Sector(new Vector(1, 0, 0), new Vector(-4.22f, 0, 0), 0.1)); // (3)
		sectors.add(new Sector(new Vector(0, -1, 0), new Vector(4.22f, 9.74f, 0), 0.1)); // (5)
		sectors.add(new Sector(new Vector(-1, 0, 0), new Vector(4.22f, 0, 0), 0.1)); //(6)
		sectors.add(new Sector(new Vector(0.15102537f, -0.9885298f, 0), new Vector(-4.22f, 8.86f, 0), 0.1)); //(4)
		
		soundSource = new MonteCarloAcousticSource(source);
		sphericalReceptorCenter = receptorCenter;
		sphericalReceptorRadius = receptorRadius;
	}

	
	public RR3fase1(int numberOfRays, Vector source, Vector receptor, double mCoefficient, double radius, String filename, int k) {
		super(numberOfRays, source, filename);
		
		setUp(source, receptor, radius);
		Receptor rec = new SphericalReceptor(sphericalReceptorCenter, (float) sphericalReceptorRadius);
		GeometricAcousticSimulation rts = new RayTracingGeometricAcousticSimulationImpl(sectors, soundSource, numberOfRays, rec , soundSpeed, mCoefficient, k);

		long ti = System.currentTimeMillis();
		rts.simulate(getBar());
		SimulatedImpulseResponse sir =  rec.getSimulatedImpulseResponse();
		
		System.out.println("time: " + (System.currentTimeMillis() - ti) + " ms");
		System.out.println("rays: " + numberOfRays);
		System.out.println("ri: " + filename);
		System.out.println("source: " + source);
		System.out.println("receptor: " + receptor);
		
		salvaIR(sir);
	}
	
	public static void main(String[] args){
		if(args.length < 7){
			System.out.println("use RoudRobin3phase1Test number_of_rays [S1 S2] [R01 R02 R03] air_absorption receptor_radius k file_name");
			System.exit(0);
		}
		
		Integer numberOfRays = Integer.valueOf(args[0]);
		Vector source = RR3Infrasctructure.getSource(args[1]);
		Vector receptor = RR3Infrasctructure.getReceptor(args[2]);
		Double mCoefficient = Double.valueOf(args[3]);
		Double radius = Double.valueOf(args[4]);
		Integer k = Integer.valueOf(args[5]);
		String filename = args[6];
		System.out.println("Begin");
		//para teste no eclipse
		/*
		Integer numberOfRays = 1000;
		Vector source = RR3Infrasctructure.getSource("S1");
		Vector receptor = RR3Infrasctructure.getReceptor("R01");
		double radius = 0.1;
		String filename = "/tmp/ri";
		*/
		@SuppressWarnings("unused")
		RR3fase1 rt = new RR3fase1(numberOfRays, source, receptor, mCoefficient, radius, filename, k);
		
		System.out.println("=======\nEND");
	}
}
