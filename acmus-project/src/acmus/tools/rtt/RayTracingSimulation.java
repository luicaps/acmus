package acmus.tools.rtt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import acmus.tools.structures.NormalSector;
import acmus.tools.structures.Triade;

public class RayTracingSimulation {

	
	List<Triade> vectors;
	List<NormalSector> sectors;
	Triade soundSource;
	Triade sphericalReceptorCenter;
	HashMap<Double, Double> sphericalReceptorHistogram;
	
	double sphericalReceptorRadius;
	double soundSpeed;
	double initialEnergy;
	double mCoeficient;
	double k;
	
	public RayTracingSimulation() {
		//le dados do arquivo de entrada
		RandomAcousticSource ras = new RandomAcousticSource();
		vectors = ras.generate(2);
		try {
			saveVectors();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//TODOS vetores gerados devem ter norma 1
		
//		vectors = new ArrayList<Triade>();
//		vectors.add(new Triade(0.7071, 0.7071, 0)); //vetor (1,1,0) normalizado //vetor de teste (1,2,0) normalizado
		sectors = new ArrayList<NormalSector>();
		sectors.add(new NormalSector(new Triade(0, 0, 1), new Triade(1,1,0), 0.5)); //base
		sectors.add(new NormalSector(new Triade(0, 0, -1), new Triade(1, 1, 10), 0.5)); //topo
		sectors.add(new NormalSector(new Triade(0, 1, 0), new Triade(1, 0, 1), 0.5));
		sectors.add(new NormalSector(new Triade(1, 0, 0), new Triade(0, 1, 1), 0.5));
		sectors.add(new NormalSector(new Triade(0, -1, 0), new Triade(1, 10, 1), 0.5));
		sectors.add(new NormalSector(new Triade(-1, 0, 0), new Triade(10, 1, 1), 0.5));
		
//		soundSource = new Triade(0, 5, 5);
//		sphericalReceptorCenter = new Triade(2.5, 2.5, 5);
		soundSource = new Triade(2, 2, 5);
		sphericalReceptorCenter = new Triade(8, 8, 6);
		sphericalReceptorHistogram = new HashMap<Double, Double>();
		
		sphericalReceptorRadius = 3.0;
		soundSpeed = 344.0; //em metros por segundo (m/s)
		initialEnergy = 10000000;
		mCoeficient = 0.0001;
		k = 500;
		
	}

	private void saveVectors() throws IOException {
		FileWriter fw = new FileWriter("/tmp/fonte3d.txt");
		
		for (int i = 0; i < vectors.size(); i++) {
			fw.write(vectors.get(i).toDat()+"\n");
		}
	
		//Para desenhar o grafico com o gnuplot
		//set size square
		//splot '/tmp/fonte3d.txt'
		fw.flush();
		fw.close();
	}
	
	public void simulate(){
		Triade q = soundSource;
		Triade g = null;
		Triade v;
		Triade nR = null;
		double e;
		double lMin = 0.0;
		double dMin = 0.0;
		double alpha = 0.0;
		double lReflection;

		//reflection
		for(Triade vTemp: vectors){
			v = vTemp;
			e = initialEnergy;
			lReflection = 0; //acumulador de distancia percorrida pelo raio
			//reflexoes do raio
			//teste de qual direcao o raio vai seguir
			do{
				//notar que V jah estah normalizado
				g = q;
				//correcao no raio...
				lMin = 1.7E300; // this number is our MAX constant
				
				//verificacao de qual setor(parede) o raio incide
				for(NormalSector s: sectors){
//					System.out.println("k#");
					
					if( Triade.produtoEscalar(v, s.normalVector ) >= 0)
					{
						continue;
					}
					else
					{
						double d = Triade.produtoEscalar(s.normalVector, Triade.sub(s.iPoint, g));
						double l = -1* d/(Triade.produtoEscalar(v, s.normalVector));
						
						//testa distancia minima da fonte a parede e ve se eh minima, dentre outras
						//paredes
						//este teste determina em qual parede o raio "bate"
						if( l<=lMin ){
							lMin = l;
							dMin = d;
							alpha = s.absorbentCoeficient;
							nR = s.normalVector;
						}
					}
				}//fim setores
				q = Triade.sum(g, Triade.multiplicaVetorEscalar(v, lMin));
				double eTemp = e*(1-alpha)*Math.pow(Math.E, -1*mCoeficient*lMin);
				
				//
				//desenha o raio
				//
				
				//
				//verifica se este raio intercepta o receptor esferico
				// TODO corrigir estes calculos que estao errados, pois ocorre um caso em que 
				//delta = 2 e na verdade o raio nao intercepta a esfera
				{
					Triade oc = Triade.sub(g, sphericalReceptorCenter);
					double l2oc = Triade.produtoEscalar(oc, oc);
					double tca = Triade.produtoEscalar(oc, v);
					
					//o raio intercepta o receptor esferico
					if(tca >= 0){
						double t2hc = Math.pow(sphericalReceptorRadius, 2) - l2oc + Math.pow(tca, 2);
						if(t2hc > 0){
							System.out.println("INTERCEPTA");
							double lThisReflection = tca - Math.sqrt(t2hc);
							
							double distance = lReflection + lThisReflection;
							double time = distance / soundSpeed;
							double eSphere = e*(1-alpha)*Math.pow(Math.E, -1*mCoeficient*lThisReflection);
							if( sphericalReceptorHistogram.containsKey(time) )
							{
								double temp = sphericalReceptorHistogram.get(time);
								sphericalReceptorHistogram.put(time, temp+eSphere);
								System.out.println("t: "+ time + "e: " + temp+eSphere);
							}
							else{
								sphericalReceptorHistogram.put(time, eSphere);
								System.out.println("t: "+ time + "e: " + eSphere);
							}
						}
					}
				}
				lReflection += lMin;
				e = eTemp;
				v = Triade.sum(Triade.multiplicaVetorEscalar(nR, 2*dMin), Triade.sub(g, q));
				v = Triade.multiplicaVetorEscalar(v, 1/Triade.modulo(v));//AGORA TENHO QUE NORMALIZAR o vetor V
			
			}while( e>(1/k*initialEnergy) ); //vai para a proxima reflexao, caso 
											// a energia seja maior do que o criterio de parada
			
		}//fim for, vetores
		
	}
	public void lista() throws IOException{
		FileWriter fw = new FileWriter("/tmp/hist.txt");
		StringBuilder sx = new StringBuilder(2000);
		StringBuilder sy = new StringBuilder(2000);
		
		for(Map.Entry<Double, Double> e: sphericalReceptorHistogram.entrySet()){
			sx.append(e.getKey());
			sx.append(" ");
			sy.append(e.getValue());
			sy.append(" ");
		}
		fw.write("x=[" + sx.toString() + "0]; y=[" + sy.toString() + "0]");
		fw.close();
	}
	
	public void histogram(){
		double tMax = 0.0;
		double h1 = 0.0, h2 = 0.0, h3 = 0.0, h4 = 0.0, h5 = 0.0, h6 = 0.0;
		Iterator itr = sphericalReceptorHistogram.keySet().iterator();
		//controi histograma
		while(itr.hasNext()){
			Double key = (Double)itr.next();
			if(key <= 0.01)
				h1 += sphericalReceptorHistogram.get(key);
			if(key >= 0.01 && key <= 0.02)
				h2 += sphericalReceptorHistogram.get(key);
			if(key >= 0.02 && key <= 0.03)
				h3 += sphericalReceptorHistogram.get(key);
			if(key >= 0.03 && key <= 0.04)
				h4 += sphericalReceptorHistogram.get(key);
			if(key >= 0.04 && key <= 0.05)
				h5 += sphericalReceptorHistogram.get(key);
			if(key >= 0.05)
				h6 += sphericalReceptorHistogram.get(key);
			
			tMax = sphericalReceptorHistogram.get(key);
		}
		
		System.out.println("0,01 : " + h1/tMax);
		System.out.println("0,02 : " + h2/tMax);
		System.out.println("0,03 : " + h3/tMax);
		System.out.println("0,04 : " + h4/tMax);
		System.out.println("0,05 : " + h5/tMax);
		System.out.println("0,06 : " + h6/tMax);

	
	}
	
	public static void main(String[] args) throws IOException{
		RayTracingSimulation rts = new RayTracingSimulation();
		rts.simulate();
//		rts.histogram();
		rts.lista();
	}
}
