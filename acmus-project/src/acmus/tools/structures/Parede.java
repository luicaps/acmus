package acmus.tools.structures;

public class Parede {
	Plain parede;
	Parede prox;
	/**
	 * 
	 */
	public Parede() {
		// TODO Auto-generated constructor stub
		parede = new Plain();
	}
	/**
	 * @return Returns the parede.
	 */
	public Plain getParede() {
		return parede;
	}
	/**
	 * @param parede The parede to set.
	 */
	public void setParede(Plain parede) {
		this.parede = parede;
	}
	/**
	 * @return Returns the prox.
	 */
	public Parede getProx() {
		return prox;
	}
	/**
	 * @param prox The prox to set.
	 */
	public void setProx(Parede prox) {
		this.prox = prox;
	}
}
