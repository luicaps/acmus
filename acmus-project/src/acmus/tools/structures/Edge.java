package acmus.tools.structures;


public class Edge {
	Triade p1 = new Triade();
	Triade p2 = new Triade();
	Triade Ap = new Triade();
	
	public Edge() {
		
	}
	
	/**
	 * @param p1
	 * @param p2
	 * @param ap
	 */
	public Edge(Triade p1, Triade p2, Triade ap) {
		super();
		// TODO Auto-generated constructor stub
		this.p1 = p1;
		this.p2 = p2;
		Ap = ap;
	}
	/**
	 * @return Returns the ap.
	 */
	public Triade getAp() {
		return Ap;
	}
	/**
	 * @param ap The ap to set.
	 */
	public void setAp(Triade ap) {
		Ap = ap;
	}
	/**
	 * @return Returns the p1.
	 */
	public Triade getP1() {
		return p1;
	}
	/**
	 * @param p1 The p1 to set.
	 */
	public void setP1(Triade p1) {
		this.p1 = p1;
	}
	/**
	 * @return Returns the p2.
	 */
	public Triade getP2() {
		return p2;
	}
	/**
	 * @param p2 The p2 to set.
	 */
	public void setP2(Triade p2) {
		this.p2 = p2;
	}
	
}
