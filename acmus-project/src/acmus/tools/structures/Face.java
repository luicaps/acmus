package acmus.tools.structures;

public class Face {
	Edge e1;
	Edge e2;
	
	public Face() {
		
	}
	/**
	 * @param e1
	 * @param e2
	 */
	public Face(Edge e1, Edge e2) {
		super();
		// TODO Auto-generated constructor stub
		this.e1 = e1;
		this.e2 = e2;
	}
	
	/**
	 * @return Returns the e1.
	 */
	public Edge getE1() {
		return e1;
	}

	/**
	 * @param e1 The e1 to set.
	 */
	public void setE1(Edge e1) {
		this.e1 = e1;
	}

	/**
	 * @return Returns the e2.
	 */
	public Edge getE2() {
		return e2;
	}

	/**
	 * @param e2 The e2 to set.
	 */
	public void setE2(Edge e2) {
		this.e2 = e2;
	}
}
