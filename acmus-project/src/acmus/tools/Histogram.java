/*
 *  Histogram.java
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
/*
 * Graph.java
 * Created on 20/07/2005
 */
package acmus.tools;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;

/**
 * @author lku
 */
public class Histogram extends Canvas implements PaintListener {

	Composite _parent;
	String _labels[] = new String[0];

	List<Line> _data;

	double _yMax = 0;
	double _xMax = 0;

	int _xloff = 30;
	int _xroff = 50;
	int _ytoff = 30;
	int _yboff = 30;
	int _ylaboff = 10;

	int _border = 5;

	int _rWidth = 8;
	int _ticks = 0;

	int _arrowHeadH = 8;
	int _arrowHeadW = 3;
	int _arrowFoot = 5;

	String _xLabel = "";
	String _yLabel = "";

	Color _rColor = AcmusGraphics.BLUE3;
	Color _holdColor = AcmusGraphics.RED;
	Color _backColor;

	FontMetrics fm;

	static String[] foo = { "125", "250", "500", "1000", "2000", "4000" };
	static double[] foo2 = { 0.3, 0.2, 0.1, 0.38, 0.25, 0.48 };

	public Histogram(Composite parent, int style, String xLabel, String yLabel,
			String[] labels) {
		super(parent, style);

		setFont(parent.getFont());

		_parent = parent;
		_backColor = _parent.getBackground();
		setBackground(_backColor);
		_xLabel = xLabel;
		_yLabel = yLabel;
		_labels = labels;
		_xMax = labels.length;

		_data = new ArrayList<Line>();

		addPaintListener(this);
	}

	private Color getColor(int i) {
		return AcmusGraphics.COMP_COLORS[i % AcmusGraphics.COMP_COLORS.length];
	}

	public void setBarWidth(int w) {
		_rWidth = w - 1;
		// redraw();
	}

	public void setIntermediateTicks(int t) {
		_ticks = t + 1;
	}

	public void setXMax(double x) {
		_xMax = x;
	}

	public void setYMax(double y) {
		_yMax = y;
	}

	public void setData(double[] dataX, double[] dataY, String topLabels[]) {
		Line l = new Line(dataX, dataY, topLabels, getColor(_data.size()));
		_data.add(l);
		if (l.yMax > _yMax) {
			_yMax = l.yMax;
		}
		redraw();
	}

	// public void setData(double[] data, String[] labels) {
	// _dataY = data;
	// if (_holdData == null)
	// _yMax = 0;
	// for (int i = 0; i < _dataY.length; i++) {
	// if (_dataY[i] > _yMax)
	// _yMax = _dataY[i];
	// }
	// _labels = labels;
	// redraw();
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent e) {

		Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();

		int width = getBounds().width;
		int height = getBounds().height;
		GC gc = e.gc;

		gc.setAntialias(SWT.ON);
		gc.setTextAntialias(SWT.ON);

		Point xLabelSize = gc.stringExtent(_xLabel);
		Point yLabelSize = gc.stringExtent(_yLabel);

		_xloff = _border + yLabelSize.x / 2;
		_xroff = _border + xLabelSize.x + _arrowHeadH + _arrowFoot + 5;
		if ("".equals(_yLabel))
			_ytoff = _border + _arrowHeadH + _arrowFoot;
		else
			_ytoff = _border + yLabelSize.y + _arrowHeadH + _arrowFoot;

		gc.drawString(_yLabel, _border, _ytoff - yLabelSize.y - _arrowHeadH
				- _arrowFoot);
		gc.drawString(_xLabel, width - _xroff + _arrowHeadH + _arrowFoot + 5,
				height - _yboff - xLabelSize.y / 2);

		// y-axis
		gc.drawLine(_xloff, _ytoff - _arrowHeadH - _arrowFoot, _xloff, height
				- _yboff);
		gc.drawLine(_xloff - _arrowHeadW, _ytoff - _arrowFoot, _xloff, _ytoff
				- _arrowHeadH - _arrowFoot);
		gc.drawLine(_xloff + _arrowHeadW, _ytoff - _arrowFoot, _xloff, _ytoff
				- _arrowHeadH - _arrowFoot);

		// x-axis
		gc.drawLine(_xloff, height - _yboff, width - _xroff + _arrowHeadH
				+ _arrowFoot, height - _yboff);
		gc.drawLine(width - _xroff + _arrowFoot, height
				- (_yboff + _arrowHeadW), width - _xroff + _arrowHeadH
				+ _arrowFoot, height - _yboff);
		gc.drawLine(width - _xroff + _arrowFoot, height
				- (_yboff - _arrowHeadW), width - _xroff + _arrowHeadH
				+ _arrowFoot, height - _yboff);

		for (int i = 0; i < _labels.length * _ticks; i++) {
			int x = _xloff + (i + 1) * (width - _xloff - _xroff)
					/ ((_labels.length) * _ticks);
			gc.drawLine(x, height - _yboff, x, height - _yboff + 2);
		}

		for (int i = 0; i < _labels.length; i++) {
			int x = _xloff + (i + 1) * (width - _xloff - _xroff)
					/ (_labels.length);
			if (!"".equals(_labels[i]))
				gc.drawLine(x, height - _yboff, x, height - _yboff + 4);
			Point labelSize = gc.stringExtent(_labels[i]);
			gc.drawString(_labels[i], x - (labelSize.x / 2), height - _ylaboff
					- labelSize.y + 2);
		}

		// bars
		// gc.setBackground(_rColor);
		// for (int i = 0; i < _dataY.length; i++) {
		// int h = (int) ((height - _yboff - _ytoff) * _dataY[i] / _yMax);
		// int x = (int) Math.round(_xloff + (_dataX[i] / _xMax)
		// * (width - _xloff - _xroff));
		// int y = height - _yboff - h;
		// Point labelSize = gc.stringExtent(_topLabels[i]);
		// gc
		// .drawString(_topLabels[i], x - (labelSize.x / 2), y - labelSize.y,
		// true);
		// gc.fillRectangle(x - (_rWidth / 2), y, _rWidth, h);
		// gc.drawRectangle(x - (_rWidth / 2), y, _rWidth, h);
		// // gc.setBackground(_backColor);
		// }

		gc.setBackground(_rColor);

		for (Line l : _data) {
			Path path = new Path(d);

			int hi = (int) ((height - _yboff - _ytoff) * l.dataY[0] / _yMax);
			int xi = (int) Math.round(_xloff + (l.dataX[0] / _xMax)
					* (width - _xloff - _xroff));
			int yi = height - _yboff - hi;
			path.moveTo(xi, yi);

			for (int i = 1; i < l.dataY.length; i++) {
				int h = (int) ((height - _yboff - _ytoff) * l.dataY[i] / _yMax);
				int x = (int) Math.round(_xloff + (l.dataX[i] / _xMax)
						* (width - _xloff - _xroff));
				int y = height - _yboff - h;
				if (l.drawLabels) {
					Point labelSize = gc.stringExtent(l.topLabels[i]);
					gc.drawString(l.topLabels[i], x - (labelSize.x / 2), y
							- labelSize.y, true);
				}
				path.lineTo(x, y);
			}
			gc.setForeground(l.color);
			gc.drawPath(path);
			path.dispose();
		}

	}

	public void exportPs(PrintStream ps) {
		int width = getBounds().width;
		int height = getBounds().height;
		int fontSize = 10;

		PostScriptWriter gc = new PostScriptWriter(ps, "RT60", width, height,
				fontSize);
		Point xLabelSize = new Point(_xLabel.length() * 5, fontSize);
		Point yLabelSize = new Point(_yLabel.length() * 5, fontSize);

		double _xloff = _border + (double) yLabelSize.x / 2;
		double _xroff = _border + _arrowHeadH + _arrowFoot + xLabelSize.x + 5;
		double _ytoff = _border + yLabelSize.y + _arrowHeadH + _arrowFoot + 5;
		double _yboff = _border + xLabelSize.y + 7;

		gc.drawString(_yLabel, _xloff, _ytoff - yLabelSize.y - _arrowHeadH
				- _arrowFoot - 5, 0.5);
		gc.drawString(_xLabel, width - _xroff + _arrowHeadH + _arrowFoot + 5,
				height - _yboff - (double) xLabelSize.y / 2);

		// y-axis
		gc.drawLine(_xloff, _ytoff - _arrowHeadH - _arrowFoot, _xloff, height
				- _yboff);
		gc.drawLine(_xloff - _arrowHeadW, _ytoff - _arrowFoot, _xloff, _ytoff
				- _arrowHeadH - _arrowFoot);
		gc.drawLine(_xloff + _arrowHeadW, _ytoff - _arrowFoot, _xloff, _ytoff
				- _arrowHeadH - _arrowFoot);

		// x-axis
		gc.drawLine(_xloff, height - _yboff, width - _xroff + _arrowHeadH
				+ _arrowFoot, height - _yboff);
		gc.drawLine(width - _xroff + _arrowFoot, height
				- (_yboff + _arrowHeadW), width - _xroff + _arrowHeadH
				+ _arrowFoot, height - _yboff);
		gc.drawLine(width - _xroff + _arrowFoot, height
				- (_yboff - _arrowHeadW), width - _xroff + _arrowHeadH
				+ _arrowFoot, height - _yboff);

		for (int i = 0; i < _labels.length * _ticks; i++) {
			double x = _xloff + (i + 1) * (width - _xloff - _xroff)
					/ ((_labels.length) * _ticks);
			gc.drawLine(x, height - _yboff, x, height - _yboff + 2);
		}

		for (int i = 0; i < _labels.length; i++) {
			double x = _xloff + (i + 1) * (width - _xloff - _xroff)
					/ (_labels.length);
			if (!"".equals(_labels[i]))
				gc.drawLine(x, height - _yboff, x, height - _yboff + 4);
			Point labelSize = new Point(_labels[i].length() * 5, fontSize);
			gc.drawString(_labels[i], x - ((double) labelSize.x / 2), height
					- _ylaboff - labelSize.y + 2);
		}

		// bars
		// for (int i = 0; i < _dataY.length; i++) {
		// double h = (height - _yboff - _ytoff) * _dataY[i] / _yMax;
		// double x = _xloff + (_dataX[i] / _xMax) * (width - _xloff - _xroff);
		// double y = height - _yboff - h;
		// Point labelSize = new Point(_topLabels[i].length() * 5, fontSize);
		// gc.drawString(_topLabels[i], x, y - labelSize.y - 3, 0.5);
		// gc.setBackground(_rColor);
		// gc.fillRectangle(x - ((double) _rWidth / 2), y, _rWidth, h);
		// gc.drawRectangle(x - ((double) _rWidth / 2), y, _rWidth, h);
		// gc.setBackground(_backColor);
		// }
		gc.close();
	}

}

class PostScriptWriter {
	PrintStream ps;
	int _width;
	int _height;
	Color _background = AcmusGraphics.WHITE;
	Color _foreground = AcmusGraphics.BLACK;
	double _fontSize;

	public PostScriptWriter(PrintStream out) {
		this(out, "no title", 596, 842, 10);
	}

	public PostScriptWriter(PrintStream out, String title, int width,
			int height, double fontSize) {
		ps = out;
		this._width = width;
		this._height = height;
		_fontSize = fontSize;
		ps.println("%!PS-Adobe-2.0");
		ps.println("%%Creator: AcMus");
		ps.println("%%Title: " + title);
		ps.println("%%BoundingBox: 0 0 " + this._width + " " + this._height);
		ps.println("%%EndComments");
		ps.println("/Helvetica findfont");
		ps.println(fontSize + " scalefont");
		ps.println("setfont");
	}

	public void println(String str) {
		ps.println(str);
	}

	public void print(String str) {
		ps.print(str);
	}

	public void setBackground(Color c) {
		_background = c;
	}

	public void setForeground(Color c) {
		_foreground = c;
		ps.println(((double) _foreground.getRed() / 255) + " "
				+ ((double) _foreground.getGreen() / 255) + " "
				+ ((double) _foreground.getBlue() / 255) + " setrgbcolor");
	}

	public void drawString(String str, double x, double y) {
		drawString(str, x, y, 0);
	}

	public void drawString(String str, double x, double y, double halign) {
		ps.println("newpath");
		ps.println(x + " " + (_height - y - _fontSize) + " moveto");
		ps.println("(" + str + ")");
		if (halign != 0) {
			ps.print("dup stringwidth pop " + halign + " mul neg 0 rmoveto ");
		}
		ps.println("show");
	}

	public void drawLine(double x1, double y1, double x2, double y2) {
		drawLine(x1, y1, x2, y2, 1);
	}

	public void drawLine(double x1, double y1, double x2, double y2,
			double lineWidth) {
		ps.println("newpath");
		ps.println(x1 + " " + (_height - y1) + " moveto");
		ps.println(x2 + " " + (_height - y2) + " lineto");
		if (lineWidth != 1)
			ps.println(lineWidth + " setlinewidth");
		ps.println("stroke");
	}

	public void drawRectangle(double x, double y, double width, double height) {
		drawRectangle(x, y, width, height, 1);
	}

	public void drawRectangle(double x, double y, double width, double height,
			double lineWidth) {
		ps.println("newpath");
		ps.println(x + " " + (_height - y) + " moveto");
		ps.println((x + width) + " " + (_height - y) + " lineto");
		ps.println((x + width) + " " + (_height - y - height) + " lineto");
		ps.println(x + " " + (_height - y - height) + " lineto");
		ps.println("closepath");
		if (lineWidth != 1)
			ps.println(lineWidth + " setlinewidth");
		ps.println("stroke");
	}

	public void fillRectangle(double x, double y, double width, double height) {
		ps.println("newpath");
		ps.println(x + " " + (_height - y) + " moveto");
		ps.println((x + width) + " " + (_height - y) + " lineto");
		ps.println((x + width) + " " + (_height - y - height) + " lineto");
		ps.println(x + " " + (_height - y - height) + " lineto");
		ps.println("closepath");
		ps.println("gsave");
		ps.println(((double) _background.getRed() / 255) + " "
				+ ((double) _background.getGreen() / 255) + " "
				+ ((double) _background.getBlue() / 255) + " setrgbcolor");
		ps.println("fill");
		ps.println("grestore");
	}

	public void close() {
		ps.println("%%EOF");
		ps.close();
	}
}

class Line {
	double[] dataY;
	double[] dataX;
	String[] topLabels;
	boolean drawLabels;
	double yMax;
	Color color;

	public Line(double[] x, double[] y, String[] labels, Color c) {
		assert (y != null && y.length > 0);
		dataX = x;
		dataY = y;
		topLabels = labels;
		yMax = dataY[0];
		for (int i = 0; i < dataY.length; i++) {
			if (dataY[i] > yMax) {
				yMax = dataY[i];
			}
		}
		drawLabels = false;
		color = c;
	}
}