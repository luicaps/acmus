package acmus.tools.structures;

public class Triade {
	static final double EPS = 0.00001;

	double x;
	double y;
	double z;
	String nome;

	public Triade() {

	}

	public Triade(double a, double b, double c) {
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
	 * @param x
	 *            The x to set.
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @param y
	 *            The y to set.
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @param z
	 *            The z to set.
	 */
	public void setZ(double z) {
		this.z = z;
	}

	/**
	 * @param nome
	 *            The nome to set.
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	public Triade sub(Triade end) {
		Triade res = new Triade();
		res.x = end.x - this.x;
		res.y = end.y - this.y;
		res.z = end.z - this.z;
		return res;
	}

	public Triade sum(Triade v2) {
		Triade ret = new Triade();
		ret.x = this.x + v2.x;
		ret.y = this.y + v2.y;
		ret.z = this.z + v2.z;
		return ret;
	}

	public Triade produtoVetorial(Triade w) {
		Triade c = new Triade();
		c.x = this.y * w.z - this.z * w.y;
		c.y = this.z * w.x - this.x * w.z;
		c.z = this.x * w.y - this.y * w.x;
		return c;
	}

	public double produtoEscalar(Triade w) {
		return this.x * w.x + this.y * w.y + this.z * w.z;
	}

	public double modulo() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public double anguloVetores(Triade w) {
		double ang, cosseno;
		cosseno = this.produtoEscalar(w) / (this.modulo() * w.modulo());
		ang = Math.acos(cosseno);

		return ang;
	}
	
	public Triade divideVetorEscalar(double esc) {
		Triade ret;
		ret = this;
		if (esc != 0) {
			ret.x = ret.x / esc;
			ret.y = ret.y / esc;
			ret.z = ret.z / esc;
		} else {
			System.out
					.println("****** Opera��o ilegal: divis�o por zero.******\n");
		}
		return ret;
	}
	
	public Triade multiplicaVetorEscalar(double esc) {
		Triade ret = new Triade();

		ret.x = this.x * esc;
		ret.y = this.y * esc;
		ret.z = this.z * esc;

		return ret;
	}

	public String imprime() {
		return ("Triade: " + this.nome + " " + this.toString());
	}

	public String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}

	public String toDat() {
		return this.x + " " + this.y + " " + this.z;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Triade v = (Triade) obj;
		if ((Math.abs(this.x - v.x) < EPS)
				&& (Math.abs(this.y - v.y) < EPS)
				&& (Math.abs(this.z - v.z) < EPS))
			return true;
		else
			return false;
	}

	public static void main(String[] args) {
		Triade a = new Triade(0, 0, 0);
		Triade b = new Triade(4.78, 0, 0);
		Triade c = new Triade(4.6, 4.6, 0);
		Triade d = new Triade(0, 4.78, 0);

		Triade ab = a.sub(b);
		Triade ad = a.sub(d);
		Triade cb = c.sub(b);
		Triade cd = c.sub(d);

		Triade vetorNormal1 = ab.produtoVetorial(ad);
		Triade vetorNormal2 = cb.produtoVetorial(cd);

		System.out.println("vetor normal1: " + vetorNormal1);
		System.out.println("vetor normal2: " + vetorNormal2);

		System.out.println("area: "
				+ (vetorNormal1.modulo() + vetorNormal2.modulo()) / 2);
	}
}
