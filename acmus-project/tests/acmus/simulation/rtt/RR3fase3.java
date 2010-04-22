package acmus.simulation.rtt;
//package acmus.tools.rtt;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import acmus.tools.structures.NormalSector;
//import acmus.tools.structures.SimulatedImpulseResponse;
//import acmus.tools.structures.Vector;
//
//public class RR3fase3 extends RR3Infrasctructure {
//
//	private List<NormalSector> sectors;
//	private Vector soundSourceCenter;
//	private Vector sphericalReceptorCenter;
//	private double sphericalReceptorRadius;
//	private int k;
//
//	
//	public void setUp(){
//		sectors = new ArrayList<NormalSector>();
//		sectors.add(new NormalSector(new Vector(0, 0, 1), new Vector(4.22f, 0, 0), 0.1)); // floor (1)
//		sectors.add(new NormalSector(new Vector(0, 0, -1), new Vector(4.22f, 0, 5.23f), 0.1)); // ceil (7)
//		sectors.add(new NormalSector(new Vector(0, 1, 0), new Vector(4.22f, 0, 0), 0.1)); // walls (2)
//		sectors.add(new NormalSector(new Vector(1, 0, 0), new Vector(-4.22f, 0, 0), 0.1)); // (3)
//		sectors.add(new NormalSector(new Vector(0, -1, 0), new Vector(4.22f, 9.74f, 0), 0.1)); // (5)
//		sectors.add(new NormalSector(new Vector(-1, 0, 0), new Vector(4.22f, 0, 0), 0.1)); //(6)
//		sectors.add(new NormalSector(new Vector(0.15102537f, -0.9885298f, 0), new Vector(-4.22f, 8.86f, 0), 0.1)); //(4)
//		
//		soundSourceCenter = new Vector(1.5f, 3.5f, 1.5f); // sound source 1
//
//		sphericalReceptorCenter = new Vector(-2.0f, 3.0f, 1.2f); // receptor 1
//		sphericalReceptorRadius = 0.05;
//		k = 500;
//	}
//
//	
//	public RR3fase3(int numberOfRays, String filename) {
//		super(numberOfRays, filename);
//		
//		setUp();
//		GeometricAcousticSimulation rts = new RayTracingGeometricAcousticSimulationImpl(sectors, getRays(), soundSourceCenter, sphericalReceptorCenter, sphericalReceptorRadius, soundSpeed, mCoeficient, k);
//
//		long ti = System.currentTimeMillis();
//		rts.simulate(getBar());
//		SimulatedImpulseResponse sir =  rts.getSimulatedImpulseResponse();
//		System.out.println("tempo: " + (System.currentTimeMillis() - ti) + " ms");
//		
//		salvaIR(sir);
//	}
//	
//	public static void main(String[] args){
////		if(args.length < 2){
////			System.out.println("use RoudRobin3phase1Test numero_de_raios nome_arquivo_wav");
////			System.exit(0);
////		}
////		RoundRoin3phase1Test rt = new RoundRoin3phase1Test(Integer.valueOf(args[0]));
//		System.out.println("A");
//		RR3fase3 rt = new RR3fase3(100000, "/tmp/ri-100kf1");
//		System.out.println("B");
//	}
//}
