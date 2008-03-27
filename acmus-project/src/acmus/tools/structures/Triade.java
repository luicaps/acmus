package acmus.tools.structures;

public class Triade {
	static double eps = 0.00001;
	
	double x;
	double y;
	double z;
	String nome;
	
	public Triade () {
		
	}
	
	public Triade (double a, double b, double c) {
		x = a;
		y = b;
		z = c;
	}
	
	public double getX() {
		return x;
	}
	

	public double getY() {
		return y;
	}
	

	public double getZ() {
		return z;
	}

	public String getNome() {
		return nome;
	}
	
	/**
	 * @param x The x to set.
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @param y The y to set.
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @param z The z to set.
	 */
	public void setZ(double z) {
		this.z = z;
	}
	
	/**
	 * @param nome The nome to set.
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public static Triade sub(Triade init, Triade end){
		Triade res = new Triade();
		res.x = end.x - init.x;
		res.y = end.y - init.y;
		res.z = end.z - init.z;
		return res;
	}
	
	public static Triade sum(Triade v1, Triade v2){
		Triade ret = new Triade();
		ret.x = v1.x + v2.x;
		ret.y = v1.y + v2.y;
		ret.z = v1.z + v2.z;
		return ret;
	}
	/***********************************************************************
	   Função que calcula e retorna o produto vetorial entre os vetores
	   passados como argumento (v x w).
	************************************************************************/
	public static Triade produtoVetorial(Triade v, Triade w){
	  Triade c = new Triade();
	  c.x = v.y*w.z - v.z*w.y;
	  c.y = v.z*w.x - v.x*w.z;
	  c.z = v.x*w.y - v.y*w.x;
	  return c;
	}


	/***********************************************************************
	   Função que calcula e retorna o produto escalar entre os vetores
	   passados como argumento (v . w).
	************************************************************************/
	public static double produtoEscalar(Triade v, Triade w){
	  return v.x*w.x + v.y*w.y + v.z*w.z;
	}


	/***********************************************************************
	   Função que calcula e retorna o modulo de um vetor.
	************************************************************************/
	public static double modulo(Triade v){
//	  return Math.sqrt(vx.x*vx.x + vx.y*vx.y + vx.z*vx.z);
		return Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
	}
	
	/***********************************************************************
	 Função que calcula e retorna o ângulo entre dois vetores.
	 ************************************************************************/
	public static double anguloVetores(Triade v, Triade w){
		double ang, cosseno;
		cosseno=produtoEscalar(v,w)/(modulo(v)*modulo(w));
		ang = Math.acos(cosseno);
		
		return ang;
	}

	public static Triade divideVetorEscalar(Triade v, double esc){
		Triade ret;
		ret = v;
		if(esc != 0){
			ret.x = ret.x/esc;
			ret.y = ret.y/esc;
			ret.z = ret.z/esc;
		}else{
			System.out.println("****** Operação ilegal: divisão por zero.******\n");
		}
		return ret;
	}
	
	public static Triade multiplicaVetorEscalar(Triade v, double esc){
		Triade ret = new Triade();
		
		ret.x = v.x*esc;
		ret.y = v.y*esc;
		ret.z = v.z*esc;
		
		return ret;
	}
	
	public static Triade pontoIntersecaoReta(Triade r1, Triade p1, Triade r2, Triade p2){
		Triade ret = new Triade();
		double aux;
		
		/* processo de ortogonalização do vetor direção das retas */
		aux = r1.x;
		r1.x = -r1.y;
		r1.y = aux;
		aux = r2.x;
		r2.x = -1.0*r2.y;
		r2.y = aux;
		
		if(r1.x != 0){
			ret.y = (r2.x*p2.x + r2.y*p2.y - (r2.x/r1.x)*(r1.x*p1.x + r1.y*p1.y))/(r2.y - r2.x*r1.y/r1.x);
			ret.x = (r1.x*p1.x + r1.y*p1.y)/r1.x - r1.y*ret.y/r1.x;
		}
		else{
			ret.x = (r2.x*p2.x + r2.y*p2.y - (r2.y/r1.y)*(r1.x*p1.x + r1.y*p1.y))/(r2.x - r2.y*r1.x/r1.y);
			ret.y = (r1.x*p1.x + r1.y*p1.y)/r1.y - r1.x*ret.x/r1.y;
		}
		ret.z = p1.z;
		
		return ret;
	}
	
	public String imprime(){
		return ("Triade: " + this.nome + " " + this.toString());	
	}
	
	public String toString(){
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}
	
	public String toDat(){
		return this.x + " " + this.y + " " + this.z;	
	}
	
	public boolean equals(Triade v){
		if( (Math.abs(this.x - v.x) < this.eps) && 
			(Math.abs(this.y - v.y) < this.eps) && 
			(Math.abs(this.z - v.z) < this.eps) )
			return true;
		else
			return false;	
	}
	
	public static void main(String[] args){
		Triade a = new Triade(0, 0, 0);
		Triade b = new Triade(4.78, 0, 0);
		Triade c = new Triade(4.6, 4.6, 0);
		Triade d = new Triade(0, 4.78, 0);
		
		Triade ab = Triade.sub(a, b);
		Triade ad = Triade.sub(a, d);
		Triade cb = Triade.sub(c, b);
		Triade cd = Triade.sub(c, d);
		
		Triade vetorNormal1 = Triade.produtoVetorial(ab, ad);
		Triade vetorNormal2 = Triade.produtoVetorial(cb, cd);
		
		System.out.println("vetor normal1: " + vetorNormal1);
		System.out.println("vetor normal2: " + vetorNormal2);
		
		System.out.println("area: " + (Triade.modulo(vetorNormal1)+Triade.modulo(vetorNormal2))/2);
	}
}
