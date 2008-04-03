/**
 * 
 */
package acmus.audio;

/**
 * @author lku
 * 
 */
public interface IMeter {

	public void setData(int data[], int channels, int b, int bitsPerSample);

	public void show(int x);

	public void showLast();

	public void reset();

	public void resetPeak();

}
