package acmus.tools.rtt;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3d;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glViewport;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.GLU;

import acmus.tools.structures.NormalSector;
import acmus.tools.structures.Vector;

public class RayTracingSimulationLWJGL implements Runnable{

	
	List<Vector> vectors;
	List<NormalSector> sectors;
	Vector soundSource;
	Vector sphericalReceptorCenter;
	HashMap<Double, Double> sphericalReceptorHistogram;
	
	double sphericalReceptorRadius;
	double soundSpeed;
	double initialEnergy;
	double mCoeficient;
	double k;
	
	
	//lista de pontos para serem tracados com o opengl
	static List<Vector> lista = new ArrayList<Vector>();
	
	public RayTracingSimulationLWJGL() {
		
		
		//le dados do arquivo de entrada
		MonteCarloRandomAcousticSource ras = new MonteCarloRandomAcousticSource();
		vectors = ras.generate(40000);
//		try {
//			saveVectors();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		//TODOS vetores gerados devem ter norma 1
		
//		vectors = new ArrayList<Triade>();

//		vectors.add(new Triade(0.1825741, 0.912870, 0)); //vetor (0.5,1,0) normalizado
//		vectors.add(new Triade(0.912870, 0.1825741, 0)); //vetor (0.5,1,0) normalizado
//		vectors.add(new Triade(0.7071, 0.7071, 0)); //vetor (1,1,0) normalizado
		sectors = new ArrayList<NormalSector>();
		sectors.add(new NormalSector(new Vector(0, 0, 1), new Vector(1,1,0), 0.5)); //base
		sectors.add(new NormalSector(new Vector(0, 0, -1), new Vector(1, 1, 10), 0.5)); //topo
		sectors.add(new NormalSector(new Vector(0, 1, 0), new Vector(1, 0, 1), 0.5));
		sectors.add(new NormalSector(new Vector(1, 0, 0), new Vector(0, 1, 1), 0.5));
		sectors.add(new NormalSector(new Vector(0, -1, 0), new Vector(1, 10, 1), 0.5));
		sectors.add(new NormalSector(new Vector(-1, 0, 0), new Vector(10, 1, 1), 0.5));
		
		soundSource = new Vector(4, 5, 5);
		sphericalReceptorCenter = new Vector(2.5f, 2.5f, 5.0f);
//		soundSource = new Triade(2, 2, 5);
//		sphericalReceptorCenter = new Triade(8, 8, 6);
		sphericalReceptorHistogram = new HashMap<Double, Double>();
		
		sphericalReceptorRadius = 1.0;
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
	
	public void run(){
		Vector q = null;
		Vector g = null;
		Vector v;
		Vector nR = null;
		double e;
		float lMin = 0.0f;
		float dMin = 0.0f;
		double alpha = 0.0;
		double lReflection;

		//opengl
//		lista.add(q);
		
		//reflection
		for(Vector vTemp: vectors){
			doCirclefill(sphericalReceptorCenter.getX(), sphericalReceptorCenter.getY(), sphericalReceptorRadius);
			//
			//comeca a fazer coisas para o opengln 
			//
			lista.add(null);
			lista.add(soundSource);
			glBegin(GL_LINE_STRIP);
			glColor3d(1.0, 0.0, 0.0);
			glVertex2d(soundSource.getX(), soundSource.getY());
//			GL11.glVertex3d(soundSource.getX(), soundSource.getY(), soundSource.getZ());
			glColor3d(0.1, 0.8, 0.0);


			
			q = soundSource;
			v = vTemp;
			e = initialEnergy;
			lReflection = 0; //acumulador de distancia percorrida pelo raio
			//reflexoes do raio
			//teste de qual direcao o raio vai seguir
			do{
				//notar que V jah estah normalizado
				g = q;
				//correcao no raio...
				lMin = 1.7E10f; // this number is our MAX constant
				
				//verificacao de qual setor(parede) o raio incide
				for(NormalSector s: sectors){
//					System.out.println("k#");
					
					if( v.dotProduct(s.normalVector ) >= 0)
					{
						continue;
					}
					else
					{
						float d = s.normalVector.dotProduct(s.iPoint.sub(g));
						float l = -1* d/(v.dotProduct(s.normalVector));
						
						//testa distancia minima da fonte a parede e ve se eh minima, dentre outras
						//paredes
						//este teste determina em qual parede o raio "bate"
						if( l<=lMin ){
							lMin = l;
							dMin = d;
							alpha = s.absorptionCoeficient;
							nR = s.normalVector;
						}
					}
				}//fim setores
				q = g.add(v.times(lMin));
				double eTemp = e*(1-alpha)*Math.pow(Math.E, -1*mCoeficient*lMin);
				
				//opengl
				lista.add(q);
				GL11.glVertex3d(q.getX(), q.getY(), 0.0);
//				GL11.glVertex3d(q.getX(), q.getY(), q.getZ());
				//
				//verifica se este raio intercepta o receptor esferico
				{
					Vector oc = g.sub(sphericalReceptorCenter);
					double l2oc = oc.dotProduct(oc);
					double tca = oc.dotProduct(v);
					
					//o raio intercepta o receptor esferico
					if(tca >= 0){
						double t2hc = Math.pow(sphericalReceptorRadius, 2) - l2oc + Math.pow(tca, 2);
						if(t2hc > 0){
							System.out.println("INTERCEPTA");
							
							double lThisReflection = tca - Math.sqrt(t2hc);
							
							//desenha ponto onde bate o raio no receptor
//							Triade pontoIntersecao = Triade.sum(g, Triade.multiplicaVetorEscalar(v, lThisReflection));
//							doCirclefill(0.4, 0.9, 0.5);
							
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
				v = nR.times(2*dMin).add(g.sub(q));
				v = v.times(1/v.length());//AGORA TENHO QUE NORMALIZAR o vetor V
				
			}while( e>(1/k*initialEnergy) ); //vai para a proxima reflexao, caso 
											// a energia seja maior do que o criterio de parada
			glEnd();
			Display.update();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
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
	
	public static void main(String[] args) throws IOException, LWJGLException{

		RayTracingSimulationLWJGL rts = new RayTracingSimulationLWJGL();
		
		
		Display.setLocation(0, 0);
		Display.setDisplayMode(new DisplayMode(800, 600));
		Display.setTitle("Ray Tracing Simulation LWJGL");
		Display.create();

		//
		//myInit
		//

		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glColor3d(1.0, 0.0, 0.0);
		glLineWidth(1.0f);
		glPointSize(3.0f);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
				
//		Keyboard.init(); //cria os manipuladores de eventos de teclado
		org.lwjgl.input.Keyboard.create();
		double x, y,  z;
		x = y = 0.0;
		z = -0.8;
		
		float rotacaoEixoX = 0.0f, rotacaoEixoY = 0.0f, rotacaoEixoZ = 0.0f;
		
		while( ! Display.isCloseRequested() ){
					
			glClear(GL_COLOR_BUFFER_BIT);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();

			setWindow(0.0f, 10.0f, 0.0f, 10.0f); //define o tamanho da world window
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_Z))
				z -= 0.05;

			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_X))
				z += 0.05;
			
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_LEFT))
				x -= 0.05;
			
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_RIGHT))
				x += 0.05;
			
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_UP))
				y += 0.05;
			
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_DOWN))
				y -= 0.05;
			

			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_Q))
				rotacaoEixoX += 0.1;
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_W))
				rotacaoEixoX -= 0.1;
			
			
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_Y))
				rotacaoEixoY += 0.1;
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_U))
				rotacaoEixoY -= 0.1;
			
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_K))
				rotacaoEixoZ += 0.1;
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_L))
				rotacaoEixoZ -= 0.1;
			
			if(org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_HOME))
			{
				x = y = 0.0;
				rotacaoEixoX = rotacaoEixoY = rotacaoEixoZ = 0.0f;
				
			}
//			GL11.glTranslated(x, y, z);
//			GL11.glRotatef(rotacaoEixoY, 0.0f, 1.0f, 0.0f);
//			GL11.glRotatef(rotacaoEixoX, 1.0f, 0.0f, 0.0f);
//			GL11.glRotatef(rotacaoEixoZ, 0.0f, 0.0f, 1.0f);
			//brincadeira com o viewport
			glViewport(0, 0, 800, 600);
			
			rts.run();
//			montaDesenhoTeste();

			
			// vai para o modo view
//			glMatrixMode(GL11.GL_PROJECTION);
//			glLoadIdentity();
//			GLU.gluLookAt(eyex, eyey, eyez, centerx, centery, centerz, upx, upy, upz);
//			GLU.gluLookAt(-1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
			Display.update();
			
		}
		while( ! Display.isCloseRequested()){}
	}

	private static void montaDesenhoTeste() {
		glBegin(GL11.GL_LINE);
		glColor3d(1.0, 1.0, 1.0);
		GL11.glVertex3d(-1.0, 0.5, 0.0);
		GL11.glVertex3d(1.0, 0.5, 0.0);
		GL11.glEnd();
		
		glBegin(GL11.GL_POLYGON);
		glColor3d(1.0, 0.0, 0.0); // vermelho CHAO
		GL11.glVertex3d(0.0, 0.0, 0.0);
		GL11.glVertex3d(0.4, 0.0, 0.0);
		GL11.glVertex3d(0.4, 0.0, 0.4);
		GL11.glVertex3d(0.0, 0.0, 0.4);
		glEnd();
		
		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glColor3d(1.0, 1.0, 1.0); // branco parede ESQUERDA
		GL11.glVertex3d(0.0, 0.0, 0.0);
		GL11.glVertex3d(0.0, 0.0, 0.60);
		GL11.glVertex3d(0.0, 0.40, 0.60);
		GL11.glVertex3d(0.0, 0.40, 0.0);
		GL11.glEnd();
		

		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glColor3d(0.1, 0.4, 0.5); // X parede ESQUERDA referencia
		GL11.glVertex3d(-0.10, 0.0, 0.0);
		GL11.glVertex3d(-0.10, 0.0, 0.60);
		GL11.glVertex3d(-0.10, 0.40, 0.60);
		GL11.glVertex3d(-0.10, 0.40, 0.0);
		GL11.glEnd();
		

		
		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glColor3d(0.0, 1.0, 0.0); // verde parede DIREITA
		GL11.glVertex3d(0.0, 0.0, 0.0);
		GL11.glVertex3d(0.40, 0.0, 0.0);
		GL11.glVertex3d(0.4, 0.40, 0.0);
		GL11.glVertex3d(0.0, 0.40, 0.0);
		GL11.glEnd();
		


		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glColor3d(0.0, 1.0, 1.0); // TETO
		GL11.glVertex3d(0.0, 0.40, 0.0);
		GL11.glVertex3d(0.40, 0.40, 0.0);
		GL11.glVertex3d(0.4, 0.4, 0.4);
		GL11.glVertex3d(0.0, 0.40, 0.4);
		GL11.glEnd();
	}

	private static void setWindow(float left, float right, float bottom, float top){
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		GLU.gluOrtho2D(left, right, bottom, top);
	}
	
	//! Draw Filled Circle to bitmap or passed bitmap
	private void doCirclefill(double x, double y, double radius)
	{
		// TODO: Set to specified texture if necessary
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColor4d(1.0, 0.0, 1.0, 0.2);
		double y1=y;
		double x1=x;
		GL11.glBegin(GL11.GL_TRIANGLES);  
		for(int i=0;i<=360;i++)
		{
			double angle=(float)(((double)i)/57.29577957795135);   
			double x2=x+(radius*(float)Math.sin((double)angle));
			double y2=y+(radius*(float)Math.cos((double)angle));             
			GL11.glVertex2d(x,y);
			GL11.glVertex2d(x1,y1);
			GL11.glVertex2d(x2,y2);
			y1=y2;
			x1=x2;
		}
		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);
	}
	

}
