package acmus.tools.structures;

public interface Receptor {
	public boolean intercept(Vector origin, Vector ray, float pathLength, float rayLength, float rayEnergy);
}
