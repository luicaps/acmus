package acmus.auralization;

import java.util.LinkedList;
import java.util.List;

public class BandRangeEqSeq implements BandRangeSeq {

	private double overallInit;
	private double overallEnd;
	private int howMany;
	private List<Double> rangeSeq;
	
	/**
	 * Equally separated Band Range Sequence
	 * @param init first frequency value 
	 * @param end last frequency value
	 * @param many how many values are
	 */
	public BandRangeEqSeq(double init, double end, int many) {
		overallInit = init;
		overallEnd = end;
		howMany = many;
		create(many);
	}

	private void create(int many) {
		rangeSeq = new LinkedList<Double>();
		double rate = (overallEnd - overallInit) / ((double) many);
		double content = overallInit;
		for (int i = 1; i < many; i++) {
			rangeSeq.add(content);
			content =+ rate;
		}
	}
	
	public List<Double> getList() {
		return new LinkedList<Double>(rangeSeq);
	}
	
	public void add() {
		create(++howMany);
	}
	
	public int howMany() {
		return howMany;
	}

	@Override
	public double getMin() {
		return overallInit;
	}

	@Override
	public double getMax() {
		return overallEnd;
	}

	@Override
	public double[] getArray() {
		double[] array = new double[rangeSeq.size()];
		for (int i = 0; i < rangeSeq.size() ; i++) {
			array[i] = rangeSeq.get(i);
		}
		return array;
	}
}
