package acmus.audio;

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import acmus.AcmusGraphics;

public class HorizontalBar extends Canvas implements PaintListener {
	private int _xStart;
	private int _xEnd;
	private double _valStart;
	private double _valEnd;
	private String _unit;
	private double _valMin = 0;

	private double _valPerDot;

	DecimalFormat format = new DecimalFormat("#.##");

	public HorizontalBar(Composite parent, String unit, int start) {
		super(parent, SWT.NONE);
		setBackground(AcmusGraphics.GRAY);
		addPaintListener(this);
		_unit = unit;
		_xStart = start;
	}

	public final void update(int xStart, int xEnd, double valStart,
			double valEnd) {
		System.out.println("update " + xStart + " " + xEnd + " " + valStart
				+ " " + valEnd);
		_xStart = xStart;
		_xEnd = xEnd;
		_valStart = valStart;
		_valEnd = valEnd;
		_valPerDot = (_valEnd - _valStart) / (_xEnd - _xStart);
	}

	public final void update(int xEnd, double valStart, double valEnd) {
		update(_xStart, xEnd, valStart, valEnd);
	}

	public void paintControl(PaintEvent e) {
		GC gc = e.gc;

		int width = getBounds().width;
		int height = getBounds().height;

		double valLength = _valEnd - _valStart;
		double[] step = findStepDur2(valLength);
		double xStepLen = step[0] / _valPerDot;
		System.out.println("stepLen " + xStepLen);

		gc.setForeground(AcmusGraphics.BLACK);
		gc.setFont(AcmusGraphics.BOLD);
		String yLabel = "time (" + _unit + " " + step[1] + ")";
		gc.drawString(yLabel, width / 2 - (gc.stringExtent(yLabel).x / 2), 2);

		int x = _xStart;
		while (x > 0) {
			x -= xStepLen;
		}

		while (x < width) {
			gc.setForeground(AcmusGraphics.WHITE);
			gc.drawLine(x, height, x, height - 5);
			gc.setForeground(AcmusGraphics.BLACK);
			double val = _valStart + ((x - _xStart) * _valPerDot);
			if (val > _valMin) {
				val *= Math.pow(10, step[1]);
				String l = format.format(val);
				Point strSize = gc.stringExtent(l);
				gc.drawString(l, x - strSize.x / 2, height - 6 - strSize.y,
						true);
			}
			x += xStepLen;
		}
	}

	private double[] findStepDur2(double len) {
		System.out.println("find " + len);
		double[] res = new double[2];
		res[0] = 1;
		res[1] = 0;
		if (len / res[0] > 1) {
			while (len / res[0] > 10) {
				res[0] *= 10;
				res[1]++;
			}
		} else {
			while (len / res[0] < 1) {
				res[0] /= 10;
				res[1]--;
			}
		}
		if (len / res[0] < 5) {
			res[0] /= 5;
		}
		System.out.println(res[0] + " " + res[1]);
		return res;
	}
}
