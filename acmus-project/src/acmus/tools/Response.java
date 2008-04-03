package acmus.tools;

public class Response {
	double energia;
	int indice;
	int numraios;
	Response prox;

	/**
	 * @param energia
	 * @param indice
	 * @param numraios
	 * @param prox
	 */
	public Response() {

	}

	public Response(double energia, int indice, int numraios, Response prox) {
		super();
		// TODO Auto-generated constructor stub
		this.energia = energia;
		this.indice = indice;
		this.numraios = numraios;
		this.prox = prox;
	}

	/**
	 * @return Returns the energia.
	 */
	public double getEnergia() {
		return energia;
	}

	/**
	 * @param energia
	 *            The energia to set.
	 */
	public void setEnergia(double energia) {
		this.energia = energia;
	}

	/**
	 * @return Returns the indice.
	 */
	public int getIndice() {
		return indice;
	}

	/**
	 * @param indice
	 *            The indice to set.
	 */
	public void setIndice(int indice) {
		this.indice = indice;
	}

	/**
	 * @return Returns the numraios.
	 */
	public int getNumraios() {
		return numraios;
	}

	/**
	 * @param numraios
	 *            The numraios to set.
	 */
	public void setNumraios(int numraios) {
		this.numraios = numraios;
	}

	/**
	 * @return Returns the prox.
	 */
	public Response getProx() {
		return prox;
	}

	/**
	 * @param prox
	 *            The prox to set.
	 */
	public void setProx(Response prox) {
		this.prox = prox;
	}
}
