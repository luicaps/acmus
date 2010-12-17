package acmus.auralization;

import java.util.List;

/**
 * Band Range Sequence, a sequence of frequency values as a representation of
 * bands of frequency
 * 
 * @param init
 * @param end
 * @param many
 */
public interface BandRangeSeq {
	/**
	 * 
	 * @return a List with the sequence of frequency values
	 */
	public List<Double> getList();

	/**
	 * 
	 * @return an array with the sequence of frequency values
	 */
	public double[] getArray();

	/**
	 * 
	 * @return the number of frequency value in the Band Range Sequence
	 */
	public int howMany();

	/**
	 * 
	 * @return the minimum frequency value in the Band Range Sequence
	 */
	public double getMin();

	/**
	 * 
	 * @return the maximum frequency value in the Band Range Sequence
	 */
	public double getMax();

	/**
	 * 
	 * @return the maximum frequency value represented by the Band Range
	 *         Sequence
	 */
	public double getSR();
}
