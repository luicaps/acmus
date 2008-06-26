package acmus.audio;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

import acmus.AcmusGraphics;
import acmus.util.MathUtils;
import acmus.util.WaveUtils;

public class SingleMeter extends Composite implements PaintListener {
	Composite _parent;
	double _peak[];
	double _rms[];

	int _clipCount[];
	boolean _clipped;
	int _clipDisplayHeight = 10;
	int _dangerDisplayHeight = 30;

	int _n;

	double _p = 0;
	double _r = 0;
	int _maxVal = 0;
	double _maxPeak = 0;

	int _lastPointShown = 0;

	public SingleMeter(Composite parent, int style) {
		super(parent, style);
		_parent = parent;
		this.addPaintListener(this);
		this.setBackground(AcmusGraphics.BLACK);
		_maxVal = WaveUtils.getLimit(16);
	}

	public void setData(int[] data, int b, int bitsPerSample) {
		_maxVal = WaveUtils.getLimit(bitsPerSample);
		_n = data.length;
		_peak = new double[data.length / b + 1];
		_rms = new double[data.length / b + 1];

		_clipCount = new int[data.length / b + 1];
		_clipped = false;

		for (int i = 0; i < _clipCount.length; i++) {
			_clipCount[i] = 0;
		}

		int j = 0;
		for (int i = 0; i < _peak.length; i++) {
			int k = j + b;
			int l = 0;
			while (j < k && j < data.length) {
				if (Math.abs(data[j]) > _peak[i])
					_peak[i] = Math.abs(data[j]);
				_rms[i] += ((double) data[j] / _maxVal)
						* ((double) data[j] / _maxVal);
				if (Math.abs(data[j]) >= _maxVal) {
					_clipCount[i]++;
				}
				j++;
				l++;
			}
			_rms[i] = Math.sqrt(_rms[i] / l);
			_peak[i] = toDb((double) _peak[i] / _maxVal, 100);
			_rms[i] = toDb((double) _rms[i], 100);
		}
	}

	public void show(int x) {
		_p = _peak[x];
		_r = _rms[x];

		for (int i = _lastPointShown; i <= x; i++) {
			if (_peak[i] > _maxPeak)
				_maxPeak = _peak[i];
			if (_clipCount[i] > 1)
				_clipped = true;
		}
		_lastPointShown = x;
		redraw();
	}

	public void showLast() {
		show(_peak.length - 1);
	}

	public void clear() {
		unclip();
		resetPeak();
		reset();
	}

	public void reset() {
		_lastPointShown = 0;
		_p = _r = 0;
		redraw();
	}

	public void unclip() {
		_clipped = false;
	}

	public void resetPeak() {
		_maxPeak = 0;
	}

	public void paintControl(PaintEvent e) {
		int barYOffset = _clipDisplayHeight;
		int height = getBounds().height - barYOffset;
		int width = getBounds().width;
		GC gc = e.gc;
		if (_clipped)
			gc.setBackground(AcmusGraphics.RED);
		else
			gc.setBackground(AcmusGraphics.RED2);
		gc.fillRectangle(0, 0, width, _clipDisplayHeight);

		gc.setBackground(AcmusGraphics.GREEN3);
		gc.fillRectangle(0, (int) ((1 - _p) * height + barYOffset), width,
				height);
		gc.fillRectangle(0,
				(int) (Math.ceil((1 - _maxPeak) * height) + barYOffset), width,
				2);
		gc.setBackground(AcmusGraphics.GREEN2);
		gc.fillRectangle(0, (int) (Math.ceil((1 - _r) * height) + barYOffset),
				width, height);
	}

	private double clipToRangeZeroOne(double z) {
		if (z > 1.0)
			return 1.0;
		else if (z < 0.0)
			return 0.0;
		else
			return z;
	}

	private double toDb(double v, double range) {
		double db;
		if (v > 0)
			db = 20 * MathUtils.log10(Math.abs(v));
		else
			db = -999;
		return clipToRangeZeroOne((db + range) / range);
	}
}
