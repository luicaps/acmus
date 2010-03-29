package acmus.tools.structures;

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
		VectorFloat meuVector = new VectorFloat((float) Math.random(), (float) Math.random(), (float) Math.random());
		
		long ti = System.currentTimeMillis();
		for( int i=0; i<iteracoes; i++)
		{
			meuVector.normalize();
		}

		return System.currentTimeMillis() - ti;
	}
	
	public static void main(String[] args) {
		Integer iteracoes = 0;
		
		if( args.length > 0)
			iteracoes = Integer.valueOf(args[0]);

		System.out.println("****************************************");
		System.out.println("Tempo de execucao Double x Float");
		System.out.println("Iterações: " + iteracoes );
		
		long tempoAcumulado = 0;
		for(int i=0; i<5; i++) {
			tempoAcumulado += DoubleFloatBenchmark.iteraDouble(iteracoes); 
		}
		System.out.println("iteraDouble(): " + (double) tempoAcumulado/5 + " ms");

		tempoAcumulado = 0;
		for(int i=0; i<5; i++) {
			tempoAcumulado += DoubleFloatBenchmark.iteraFloat(iteracoes); 
		}
		System.out.println("iteraFloat(): " + (double) tempoAcumulado/5 + " ms");
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
