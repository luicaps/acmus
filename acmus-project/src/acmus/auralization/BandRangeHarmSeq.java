package acmus.auralization;

import java.util.LinkedList;
import java.util.List;

public class BandRangeHarmSeq implements BandRangeSeq {

	private double overallInit;
	private double overrallEnd;
	private int howMany;
	private int sr;
	private List<Double> rangeSeq;

	/**
	 * Creates a band sequence with harmonic separated intervals
	 * 
	 * @param init
	 * @param end
	 * @param many
	 */
	public BandRangeHarmSeq(double init, double end, int many) {
		this.overallInit = init;
		this.overrallEnd = end;
		this.sr = nextSR(end);
		this.howMany = many;
		create(many);
	}

	private int nextSR(double end) {
		int base = 0;
		while (2 * end > base) {
			base += 2205;
		}
		return base;
	}

	private void create(int many) {
		many--;
		this.rangeSeq = new LinkedList<Double>();
		double rate = Math.log(overrallEnd / overallInit) / Math.log(2);
		for (float i = 0; i < 1.001f; i += 1.f / (float) many) {
			this.rangeSeq.add(overallInit * Math.pow(2, rate * i));
		}
	}

	@Override
	public List<Double> getList() {
		return new LinkedList<Double>(rangeSeq);
	}

	@Override
	public double[] getArray() {
		double[] array = new double[rangeSeq.size()];
		for (int i = 0; i < rangeSeq.size(); i++) {
			array[i] = rangeSeq.get(i);
		}
		return array;
	}

	@Override
	public int howMany() {
		return howMany;
	}

	@Override
	public double getMin() {
		return overallInit;
	}
	
	@Override
	public double getMax() {
		return overrallEnd;
	}

	@Override
	public double getSR() {
		return sr;
	}

}
