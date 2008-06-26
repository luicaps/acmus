/*
 *  Plot.java
 *  This file is part of AcMus.
 *  
 *  AcMus: Tools for Measurement, Analysis, and Simulation of Room Acoustics
 *  
 *  Copyright (C) 2006 Leo Ueda, Bruno Masiero
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package acmus.dsp;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;

public class Plot extends Canvas implements PaintListener, ControlListener {

	Composite _parent;
	List<Dots> _points;
	String _title;

	Coords _coords;

	int _xFrame = 45;
	int _yFrame = 30;

	String _xLabel;
	String _yLabel;

	double _xStep;
	double _yStep;

	static Color[] colors = { AcmusGraphics.BLUE, AcmusGraphics.MAGENTA,
			AcmusGraphics.RED, AcmusGraphics.GREEN, AcmusGraphics.YELLOW,
			AcmusGraphics.BLACK };

	public Plot(Composite parent, int style, String title, String xLabel,
			String yLabel) {
		super(parent, style);
		_title = title;
		_parent = parent;
		_points = new ArrayList<Dots>();
		_coords = new Coords(_xFrame, _yFrame);
		_xLabel = xLabel;
		_yLabel = yLabel;
		addPaintListener(this);
		addControlListener(this);

		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				_points.clear();
				_points = null;
			}

		});
	}

	public int getWidth() {
		return _coords.width();
	}

	public int getHeight() {
		return _coords.height();
	}

	public void xLimit(double x1, double x2) {
		_coords.xLimit(x1, x2);
		_xStep = findStep(x2 - x1);
	}

	public void yLimit(double y1, double y2) {
		_coords.yLimit(y1, y2);
		_yStep = findStep(y2 - y1);
	}

	public double findStep(double len) {
		double s = 1;
		if (len / s > 1) {
			while (len / s > 10) {
				s *= 10;
			}
		} else {
			while (len / s < 1) {
				s /= 10;
			}
		}
		if (len / s < 5) {
			s /= 5;
		}
		return s;
	}

	public void dots(double[] x, double[] y, Color c, String legend) {
		_points.add(new Dots(_coords, x, y, c, legend));
		this.redraw();
	}

	public void dots(double[] x, double[] y) {
		dots(x, y, AcmusGraphics.BLACK, "");
	}

	public void line(double[] x, double[] y, Color c, String legend) {
		_points.add(new Line(_coords, x, y, c, legend));
		this.redraw();
	}

	public void line(double[] x, double[] y) {
		line(x, y, AcmusGraphics.BLACK, "");
	}

	public void paintControl(PaintEvent e) {
		GC gc = e.gc;

		Color back = gc.getBackground();

		// drawing area
		gc.setBackground(AcmusGraphics.WHITE);
		gc.fillRectangle(_xFrame, _yFrame, _coords.width() - 2 * _xFrame,
				_coords.height() - 2 * _yFrame);

		// drawing area border
		gc.setForeground(AcmusGraphics.BLACK);
		gc.drawRectangle(_xFrame, _yFrame, _coords.width() - 2 * _xFrame,
				_coords.height() - 2 * _yFrame);
		// 
		for (Dots dots : _points) {
			dots.draw(gc);
		}
		// clear area around drawing area
		gc.setBackground(back);
		gc.fillRectangle(0, 0, _xFrame, _coords.height());
		gc.fillRectangle(_xFrame, 0, _coords.width() - _xFrame, _yFrame);
		gc.fillRectangle(_coords.width() - _xFrame + 1, _yFrame, _xFrame,
				_coords.height() - _yFrame);
		gc.fillRectangle(_xFrame, _coords.height() - _yFrame + 1, _coords
				.width()
				- _xFrame, _yFrame);

		// labels

		gc.setForeground(AcmusGraphics.BLACK);

		gc.drawString(_title, _coords.width() / 2
				- (gc.stringExtent(_title).x / 2), 0);

		DecimalFormat f = new DecimalFormat("#.##");
		double xm = _coords.xMin();
		while (xm <= _coords.xMax()) {
			int x = _coords.x(xm);
			int y = _coords.y(_coords.yMin());
			gc.drawLine(x, y, x, y - 5);
			String str = f.format(xm);
			gc.drawString(str, x - (gc.stringExtent(str).x / 2), y, true);
			xm += _xStep;
		}
		Point strLen = gc.stringExtent(_xLabel);
		gc.drawString(_xLabel, _coords.width() / 2 - strLen.x / 2, _coords
				.height()
				- strLen.y);

		double ym = _coords.yMin();
		while (ym <= _coords.yMax()) {
			int x = _coords.x(_coords.xMin());
			int y = _coords.y(ym);
			gc.drawLine(x, y, x + 5, y);
			String str = f.format(ym);
			strLen = gc.stringExtent(str);
			gc.drawString(str, x - strLen.x - 8, y - strLen.y / 2, true);
			ym += _yStep;
		}

		FontMetrics fm = gc.getFontMetrics();
		// legend
		int tabW = 0;
		int tabH = 0;
		for (Dots dots : _points) {
			String l = dots.legend();
			if (!l.trim().equals("")) {
				strLen = gc.stringExtent(l);
				tabH++;
				if (strLen.x > tabW) {
					tabW = strLen.x;
				}
			}
		}
		tabW += 50;
		// tabW *= 1.1; // FIXME
		tabH *= fm.getHeight();

		int legX = _coords.width() - _xFrame - tabW;
		int legY = _yFrame + 5;
		gc.setAlpha(220);
		gc.setBackground(AcmusGraphics.WHITE);
		gc.fillRectangle(legX - 5, legY, tabW, tabH);
		gc.setAlpha(255);
		gc.drawRectangle(legX - 5, legY, tabW, tabH);
		for (Dots dots : _points) {
			if (dots.legend().length() > 0) {
				gc.setForeground(dots.color());
				gc.drawLine(legX, legY + fm.getHeight() / 2, legX + 20, legY
						+ fm.getHeight() / 2);
				gc.setForeground(AcmusGraphics.BLACK);
				gc.drawString(dots.legend(), legX + 30, legY, true);
				legY += fm.getHeight();
			}
		}

		// the rotated label (_yLabel)
		Transform tr = new Transform(AcmusPlugin.getDefault().getWorkbench()
				.getDisplay());
		tr.translate(0, _coords.height() / 2 + fm.getAverageCharWidth()
				* _yLabel.length() / 2);
		tr.rotate(-90);
		gc.setTransform(tr);
		gc.drawString(_yLabel, 0, 0);

	}

	public static final Color getColor(int i) {
		return colors[i % colors.length];
	}

	public void controlMoved(ControlEvent e) {
	}

	public void controlResized(ControlEvent e) {
		_coords.height(getBounds().height);
		_coords.width(getBounds().width);
		redraw();
	}
}

/** ************************************************************************* */

class Coords {
	double _xLen = 0;
	double _yLen = 0;
	double _xMin = 0;
	double _yMin = 0;

	int _h;
	int _w;

	int _xOff = 10;
	int _yOff = 10;

	int _areaH;
	int _areaW;

	public Coords(int xoff, int yoff) {
		_xOff = xoff;
		_yOff = yoff;
		width(300);
		height(300);
	}

	public void xLimit(double min, double max) {
		_xMin = min;
		_xLen = max - min;
	}

	public void yLimit(double min, double max) {
		_yMin = min;
		_yLen = max - min;
	}

	public void height(int h) {
		_h = h;
		_areaH = _h - 2 * _yOff;
	}

	public void width(int w) {
		_w = w;
		_areaW = _w - 2 * _xOff;
	}

	public int height() {
		return _h;
	}

	public int width() {
		return _w;
	}

	public void xOffset(int o) {
		_xOff = o;
	}

	public void yOffset(int o) {
		_yOff = o;
	}

	public final int x(double val) {
		return (int) (_xOff + (((val - _xMin) / _xLen) * (_areaW)));
	}

	public final int y(double val) {
		return (int) (_yOff + (_areaH - (((val - _yMin) / _yLen) * (_areaH))));
	}

	public final int xDist(double len) {
		return (int) (((len - _xMin) / _xLen) * (_areaW));
	}

	public final int yDist(double len) {
		return (int) (((len - _yMin) / _yLen) * (_areaH));
	}

	public final double xLen() {
		return _xLen;
	}

	public final double yLen() {
		return _yLen;
	}

	public final double xMin() {
		return _xMin;
	}

	public final double yMin() {
		return _yMin;
	}

	public final double xMax() {
		return _xMin + _xLen;
	}

	public final double yMax() {
		return _yMin + _yLen;
	}

}

class Dots {
	double[] _x;
	double[] _y;
	Color _color;
	Coords _coords;
	String _legend;

	public Dots(Coords coords, double[] x, double y[], Color c, String legend) {
		_x = x;
		_y = y;
		_coords = coords;
		_color = c;
		_legend = legend;
	}

	public void setColor(Color c) {
		_color = c;
	}

	public void draw(GC gc) {
		gc.setForeground(_color);
		for (int i = 0; i < _x.length; i++) {
			gc.drawPoint(_coords.x(_x[i]), _coords.y(_y[i]));
		}
	}

	public String legend() {
		return _legend;

	}

	public Color color() {
		return _color;
	}
}

class Line extends Dots {

	public Line(Coords coords, double[] x, double y[], Color c, String legend) {
		super(coords, x, y, c, legend);
	}

	public void draw(GC gc) {
		gc.setForeground(_color);
		for (int i = 1; i < _x.length; i++) {
			gc.drawLine(_coords.x(_x[i - 1]), _coords.y(_y[i - 1]), _coords
					.x(_x[i]), _coords.y(_y[i]));
		}
	}
}
