package acmus.simulation.structures;

public final class DoubleFloatBenchmark {
	private static long iteraDouble(int iteracoes) {
		VectorDouble meuVector = new VectorDouble(Math.random(), Math.random(), Math.random());
		
		long ti = System.currentTimeMillis();
		for( int i=0; i<iteracoes; i++)
		{
			meuVector.normalize();
		}

		return System.currentTimeMillis() - ti;
	}
	
	private static long iteraFloat(int iteracoes) {
		VectorFloat myVector = new VectorFloat((float) Math.random(), (float) Math.random(), (float) Math.random());
		
		long ti = System.currentTimeMillis();
		for( int i=0; i<iteracoes; i++)
		{
			myVector.normalize();
		}

		return System.currentTimeMillis() - ti;
	}
	
	public static void main(String[] args) {
		Integer iteractions = 0;
		
		if( args.length > 0)
			iteractions = Integer.valueOf(args[0]);

		System.out.println("****************************************");
		System.out.println("Double x Float time of execution");
		System.out.println("Iteractions: " + iteractions );
		
		long acumulatedTime = 0;
		for(int i=0; i<5; i++) {
			acumulatedTime += DoubleFloatBenchmark.iteraDouble(iteractions); 
		}
		System.out.println("iteraDouble(): " + (double) acumulatedTime/5 + " ms");

		acumulatedTime = 0;
		for(int i=0; i<5; i++) {
			acumulatedTime += DoubleFloatBenchmark.iteraFloat(iteractions); 
		}
		System.out.println("iteraFloat(): " + (double) acumulatedTime/5 + " ms");
	}
}

class VectorDouble {
	double x, y, z;
	
	public VectorDouble(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public VectorDouble normalize() {
		double mod = 1/Math.sqrt(x*x + y*y + z*z);
		return new VectorDouble(x * mod, y * mod, z * mod);
	}
}

class VectorFloat {
	float x, y, z;
	
	public VectorFloat(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public VectorFloat normalize() {
		float mod = 1/(float) Math.sqrt(x*x + y*y + z*z);
		return new VectorFloat(x * mod, y * mod, z * mod);
	}
}
