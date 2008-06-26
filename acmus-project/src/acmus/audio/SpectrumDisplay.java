/*
 *  SpectrumDisplay.java
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
 * SpectrumDisplay.java
 * Created on 22/04/2005
 */
package acmus.audio;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Text;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;
import acmus.dsp.FFT1d;
import acmus.dsp.Filter;
import acmus.dsp.Parameters;
import acmus.util.MathUtils;
import acmus.util.WaveUtils;

/**
 * @author lku
 */
//TODO Refactor!!!
public class SpectrumDisplay extends Composite {
	int _data[];
	double _freq[] = new double[0];

	Composite _parent;
	float _sampleRate;

	ScrollBar _hbar;
	ScrollBar _vbar;
	int _left, _top;
	int _viewWidth = -1, _viewHeight = -1;

	int _xMark = 0;

	int _windowFunc = 3;

	List<Spectrum> _specs;
	int _yBarWidth = 0;
	int _xBarHeight = 0;

	int _bucketHeight;
	int _buckets;
	int _stepSize;

	Text _positionDisplay;
	Text _freqDisplay;
	Text _dbDisplay;

	public void setPositionDisplay(Text t) {
		_positionDisplay = t;
	}

	public void setFrequencyDisplay(Text t) {
		_freqDisplay = t;
	}

	public void setDbDisplay(Text t) {
		_dbDisplay = t;
	}

	/* ======================================================================= */

	class Spectrum extends Composite {

		int _yMark = 0;
		double _zMark = 0;
		double[][] _spectrumPoints;

		boolean _useXMark = false;

		Spectrum3D _3d;
		Spectrum2D _2d;
		int _ch;

		public Spectrum(Composite parent, int channel) {
			super(parent, SWT.NONE);
			_ch = channel;

			GridLayout gl = new GridLayout(1, false);
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			this.setLayout(gl);

			GridData gridData;

			SashForm sf = new SashForm(this, SWT.HORIZONTAL);
			sf.SASH_WIDTH = 2;
			gridData = new GridData(GridData.FILL_BOTH);
			gridData.horizontalSpan = 4;
			sf.setLayoutData(gridData);
			_3d = new Spectrum3D(sf, SWT.NONE);
			_2d = new Spectrum2D(sf, SWT.NONE);

			gridData = new GridData(GridData.FILL_VERTICAL);
			_2d.setLayoutData(gridData);

			gridData = new GridData(GridData.FILL_BOTH);
			_3d.setLayoutData(gridData);

			int weights[] = { 5, 1 };
			sf.setWeights(weights);
		}

		public void setData(double[][] points) {
			_spectrumPoints = points;

			Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
			Image img = new Image(d, points.length, points[0].length);
			GC gc = new GC(img);
			for (int i = 0; i < points.length; i++) {
				for (int j = 0; j < points[i].length; j++) {
					gc
							.setForeground(AcmusGraphics.SPECTRUM_COLOR_SCALE[(int) ((AcmusGraphics.SPECTRUM_COLOR_SCALE.length - 1) * points[i][j])]);
					gc.drawPoint(i, points[i].length - j - 1);
				}
			}
			_3d.setImage(img);
			gc.dispose();
		}

		public void redraw2D() {
			_2d.redraw();
		}

		public void redraw3D() {
			_3d.redraw();
		}

		/* ======================================================================= */

		class Spectrum3D extends Canvas implements PaintListener,
				MouseListener, MouseMoveListener {

			Image _spec3DImg;
			int _x, _y;
			int _sx, _sy, _sw, _sh;
			boolean _drag;

			MouseMoveListener _mMoveL;
			MouseListener _mouseL;

			double _xScale = 1.0, _yScale = 1.0;

			public Spectrum3D(Composite parent, int style) {
				super(parent, SWT.NONE);
				setBackground(AcmusGraphics.BLACK);

				addPaintListener(this);
				addMouseMoveListener(this);
				addMouseListener(this);

				ZoomMouseListener zl = new ZoomMouseListener();
				_mMoveL = zl;
				_mouseL = zl;

				addDisposeListener(new DisposeListener() {

					public void widgetDisposed(DisposeEvent e) {
						if (_spec3DImg != null) {
							_spec3DImg.dispose();
						}
					}
				});
			}

			public void setImage(Image spec3d) {
				if (_spec3DImg != null) {
					_spec3DImg.dispose();
				}
				_spec3DImg = spec3d;
			}

			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int width = getBounds().width;
				int height = getBounds().height - _xBarHeight;

				draw(gc, 0, 0, width, height);

				_zMark = _spectrumPoints[_xMark][_spectrumPoints[0].length
						- _yMark - 1];

				gc.setAlpha(120);
				gc.setForeground(AcmusGraphics.LIGHT_BLUE);
				int xm = (int) Math.floor((_xMark - _left) * _xScale);
				int ym = (int) Math.floor((_yMark - _top) * _yScale);
				int pointWidth = (int) Math.floor(_xScale);
				int pointHeight = (int) Math.floor(_yScale);
				int x, y;
				if (_useXMark) {
					x = xm + pointWidth / 2;
					y = ym + pointHeight / 2;
				} else {
					x = _x;
					y = _y;
				}
				gc.drawLine(x, 0, x, height);
				gc.drawLine(0, y, width, y);
				gc.drawRectangle(xm, ym, pointWidth, pointHeight);
				gc
						.setForeground(AcmusGraphics.SPECTRUM_COLOR_SCALE[(int) (_zMark * (AcmusGraphics.SPECTRUM_COLOR_SCALE.length - 1))]);
				gc.drawLine(x + 1, 0, x + 1, height);
				gc.drawLine(x - 1, 0, x - 1, height);
				if (_sw > 0) {
					gc.setForeground(AcmusGraphics.MAGENTA);
					gc.drawRectangle(_sx, _sy, _sw, _sh);
				}
				_useXMark = true;
			}

			public final void draw(GC gc, int x, int y, int width, int height) {
				_xScale = (double) width / _viewWidth;
				_yScale = (double) height / _viewHeight;
				gc.drawImage(_spec3DImg, _left, _top, _viewWidth, _viewHeight,
						x, y, width, height);
			}

			public void mouseMove(MouseEvent e) {
				_useXMark = false;
				_x = e.x;
				_y = e.y;
				_mMoveL.mouseMove(e);
				_yMark = _top + (int) Math.floor(e.y / _yScale);
				if (_yMark < 0)
					_yMark = 0;
				else if (_yMark >= _bucketHeight)
					_yMark = _bucketHeight - 1;
				_xMark = _left + (int) Math.floor(e.x / _xScale);
				if (_xMark < 0)
					_xMark = 0;
				else if (_xMark >= _buckets)
					_xMark = _buckets - 1;

				if (_positionDisplay != null) {
					_positionDisplay.setText(""
							+ Math.round(_xMark / 2 * _stepSize / _sampleRate
									* 1000));
				}
				if (_freqDisplay != null) {
					_freqDisplay.setText(""
							+ Math.round((1 - (double) _yMark
									/ (_bucketHeight - 1)) * 20000));
				}
				if (_dbDisplay != null) {
					_dbDisplay.setText("" + Math.round(_zMark * 80 - 80));
				}

				redrawSpecs2D();
				redrawSpecs3D();
			}

			public void mouseDoubleClick(MouseEvent e) {
				_mouseL.mouseDoubleClick(e);
			}

			public void mouseDown(MouseEvent e) {
				_mouseL.mouseDown(e);
			}

			public void mouseUp(MouseEvent e) {
				_mouseL.mouseUp(e);
			}

			public int restrainX(int x) {
				if (x < 0)
					return 0;
				if (x > getBounds().width)
					return getBounds().width;
				return x;
			}

			public int restrainY(int y) {
				if (y < 0)
					return 0;
				if (y > getBounds().height - _xBarHeight)
					return getBounds().height - _xBarHeight;
				return y;
			}

			// --------------------------------------------
			class ZoomMouseListener implements MouseListener, MouseMoveListener {
				int winX = 0, winY = 0;

				boolean drag = false;

				public void mouseDoubleClick(MouseEvent e) {
					resetZoom();
				}

				public void mouseDown(MouseEvent e) {
					winX = restrainX(e.x);
					winY = restrainY(e.y);
					drag = true;
				}

				public void mouseUp(MouseEvent e) {
					int x = restrainX(e.x);
					int y = restrainY(e.y);
					drag = false;
					int winX2, winY2;
					if (x < winX) {
						winX2 = winX;
						winX = x > _yBarWidth ? x : _yBarWidth;
					} else if (x > winX)
						winX2 = x > _yBarWidth ? x : _yBarWidth;
					else
						return;
					if (y < winY) {
						winY2 = winY;
						winY = y > _xBarHeight ? y : _xBarHeight;
					} else if (y > winY)
						winY2 = y > _xBarHeight ? y : _xBarHeight;
					else
						return;
					setZoomWindow(winX, winY, winX2 - winX, winY2 - winY);
					eraseWindowSelection();
					redraw();
				}

				public void mouseMove(MouseEvent e) {
					if (!drag)
						return;
					int x1, y1, x2, y2;
					if (e.x < winX) {
						x2 = winX;
						x1 = e.x > _yBarWidth ? e.x : _yBarWidth;
					} else if (e.x > winX) {
						x1 = winX;
						x2 = e.x > _yBarWidth ? e.x : _yBarWidth;
					} else
						return;
					if (e.y < winY) {
						y2 = winY;
						y1 = e.y > _xBarHeight ? e.y : _xBarHeight;
					} else if (e.y > winY) {
						y2 = e.y > _xBarHeight ? e.y : _xBarHeight;
						y1 = winY;
					} else
						return;
					x1 = restrainX(x1);
					x2 = restrainX(x2);
					y1 = restrainY(y1);
					y2 = restrainY(y2);
					setWindowSelection(x1, y1, x2 - x1, y2 - y1);
					redraw();
				}
			}

			// --------------------------------------------

			public void setWindowSelection(int x, int y, int width, int height) {
				_sx = x;
				_sy = y;
				_sw = width;
				_sh = height;
			}

			public void eraseWindowSelection() {
				_sw = -1;
			}

			public void resetZoom() {
				_top = _left = 0;
				_viewWidth = _buckets;
				_viewHeight = _bucketHeight;
				_hbar.setVisible(false);
				_vbar.setVisible(false);
				redrawSpecs2D();
				redrawSpecs3D();
			}

			public void setZoomWindow(int x, int y, int width, int height) {
				int w = getBounds().width - _yBarWidth;
				int h = getBounds().height - _xBarHeight;
				_left = _left + (int) Math.round(_viewWidth * (double) x / w);
				_viewWidth = (int) Math.round(_viewWidth * (double) width / w);
				_top = _top + (int) Math.round(_viewHeight * (double) y / h);
				_viewHeight = (int) Math.round(_viewHeight * (double) height
						/ h);
				if (_left > 0 || _viewWidth < _buckets) {
					_hbar.setVisible(true);
					_hbar.setMaximum(10 + _buckets - _viewWidth);
					_hbar.setSelection(_left);
				} else {
					_hbar.setVisible(false);
				}
				if (_top > 0 || _viewHeight < _bucketHeight) {
					_vbar.setVisible(true);
					_vbar.setMaximum(10 + _bucketHeight - _viewHeight);
					_vbar.setSelection(_top);
				} else {
					_vbar.setVisible(false);
				}
				redrawSpecs2D();
				redrawSpecs3D();
			}

		}

		/* ======================================================================= */
		class Spectrum2D extends Canvas implements PaintListener {

			public Spectrum2D(Composite parent, int style) {
				super(parent, style);
				setBackground(AcmusGraphics.BLACK);

				addPaintListener(this);
			}

			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int width = getBounds().width;
				int height = getBounds().height - _xBarHeight;
				draw(gc, 0, 0, width, height);

			}

			public final void draw(GC gc, int x, int y, int width, int height) {
				Display d = AcmusPlugin.getDefault().getWorkbench()
						.getDisplay();
				double yScale = height
						/ (double) _spectrumPoints[_xMark].length;
				double xScale = width;

				Path path = new Path(d);
				int x1 = (int) Math.round(_spectrumPoints[_xMark][0] * xScale);
				int y1 = height - 1;
				path.moveTo(x1, y1);
				for (int j = 1; j < _spectrumPoints[_xMark].length; j++) {
					int x2 = (int) Math.round(_spectrumPoints[_xMark][j]
							* xScale);
					int y2 = height - 1 - (int) Math.round(j * yScale);
					path.lineTo(x2, y2);
				}
				path.lineTo(0, 0);
				path.lineTo(0, -1);
				path.lineTo(width, -1);
				path.lineTo(width, height);
				path.lineTo(0, height);
				path.close();

				_zMark = _spectrumPoints[_xMark][_spectrumPoints[0].length
						- _yMark - 1];

				gc.drawImage(AcmusGraphics.IMG_SPECTRUM_COLOR_SCALE, 0, 0,
						AcmusGraphics.SPECTRUM_COLOR_SCALE.length, 1, 0, 0,
						width, height);
				gc.setBackground(AcmusGraphics.DARK_PURPLE);
				gc.fillPath(path);

				gc.setAlpha(120);
				gc.setForeground(AcmusGraphics.LIGHT_BLUE);
				gc.drawLine(0, (int) Math.round(_yMark * yScale), width,
						(int) Math.round(_yMark * yScale));
				gc
						.setForeground(AcmusGraphics.SPECTRUM_COLOR_SCALE[(int) (_zMark * (AcmusGraphics.SPECTRUM_COLOR_SCALE.length - 1))]);
				gc.drawLine((int) Math.round(_zMark * xScale), 0, (int) Math
						.round(_zMark * xScale), height);

				gc.setForeground(AcmusGraphics.LIGHT_YELLOW);
				gc.drawLine(0, (int) Math.round(_top * yScale) - 1, width,
						(int) Math.round(_top * yScale) - 1);
				gc.drawLine(0,
						(int) Math.round((_top + _viewHeight) * yScale) - 1,
						width,
						(int) Math.round((_top + _viewHeight) * yScale) - 1);

				path.dispose();
			}
		}

	}

	/* ======================================================================= */
	public SpectrumDisplay(Composite parent, int style) {
		super(parent, style | SWT.H_SCROLL | SWT.V_SCROLL);

		_parent = parent;

		_hbar = getHorizontalBar();
		_hbar.setVisible(false);
		_hbar.setMinimum(0);
		_hbar.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent se) {
				widgetSelected(se);
			}

			public void widgetSelected(SelectionEvent se) {
				_left = _hbar.getSelection();
				redrawSpecs3D();
			}
		});

		_vbar = getVerticalBar();
		_vbar.setMinimum(0);
		_vbar.setVisible(false);
		_vbar.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent se) {
				widgetSelected(se);
			}

			public void widgetSelected(SelectionEvent se) {
				_top = _vbar.getSelection();
				redrawSpecs2D();
				redrawSpecs3D();
			}
		});

		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 1;
		this.setLayout(gl);


		_specs = new ArrayList<Spectrum>();
	}

	int _channels;
	int _bitsPerSample;

	private final void redrawSpecs3D() {
		for (Spectrum s : _specs) {
			s.redraw3D();
		}
	}

	private final void redrawSpecs2D() {
		for (Spectrum s : _specs) {
			s.redraw2D();
		}
	}

	public void setData(int data[], int channels, float sampleRate,
			int sampleSizeInBits, int stepSize) {

		double scale = ((double) _stepSize / stepSize);

		if (_channels < channels) {
			// add new channels
			for (int i = _specs.size(); i < channels; i++) {
				Spectrum s = new Spectrum(this, i);
				_specs.add(s);
				GridData gridData = new GridData(GridData.FILL_BOTH);
				s.setLayoutData(gridData);
			}
			// make channels visible
			for (int i = _channels; i < channels; i++) {
				Spectrum s = _specs.get(i);
				((GridData) s.getLayoutData()).exclude = false;
				s.setVisible(true);
			}
			this.layout();
		} else if (_channels > channels) {
			// make channels invisible
			for (int i = _channels; i < _specs.size(); i++) {
				Spectrum s = _specs.get(i);
				((GridData) s.getLayoutData()).exclude = true;
				s.setVisible(false);
			}
			this.layout();
		}

		_xMark = (int) (_xMark * scale);
		_left = (int) (_left * scale);
		_channels = channels;
		_bitsPerSample = sampleSizeInBits;
		_sampleRate = sampleRate;
		_data = data;
		_stepSize = stepSize;

		int[][] dd = WaveUtils.splitAudioStream(_channels, data);

		int steps = dd[0].length / _stepSize;

		_bucketHeight = 256;
		_buckets = steps * 2;

		if (_viewWidth < 0) {
			_viewWidth = _buckets;
			_viewHeight = _bucketHeight;
		} else {
			_viewWidth = (int) (_viewWidth * scale);
			if (_viewWidth > _buckets)
				_viewWidth = _buckets;
		}
		if (_left + _viewWidth > _buckets) {
			_left = _buckets - _viewWidth;
		}

		_hbar.setMaximum(10 + _buckets - _viewWidth);
		_hbar.setSelection(_left);

		for (int ch = 0; ch < _channels; ch++) {
			double[][] _spectrumPoints = new double[_buckets][];
			int start = 0;
			for (int i = 0; i < _spectrumPoints.length; i++) {
				_spectrumPoints[i] = new double[_bucketHeight];
				double d[] = new double[_stepSize];
				int max = WaveUtils.getLimit(_bitsPerSample);
				for (int j = 0; j < _stepSize && start + j < dd[ch].length; j++) {
					d[j] = (double) dd[ch][(start + j)] / max;
				}

				computeSpectrum(d, _stepSize, _bucketHeight, 20000, _stepSize,
						44100.0, _spectrumPoints[i]);
				start += _stepSize / 2;

			}


			_specs.get(ch).setData(_spectrumPoints);
		}

		redraw();
	}

	int _k = 0;

	public void setWindowFunc(String func) {
		if ("Bartlett".equals(func))
			_windowFunc = 1;
		else if ("Hamming".equals(func))
			_windowFunc = 2;
		else if ("Hanning".equals(func))
			_windowFunc = 3;
		else
			_windowFunc = 3;
		setData(_data, _channels, _sampleRate, _bitsPerSample, _stepSize);
		redrawSpecs3D();
		redrawSpecs2D();
	}

	public void setWindowSize(int size) {
		int stepSize = (int) (Math.pow(2, Math.ceil(acmus.util.MathUtils.log2(size))));
		setData(_data, _channels, _sampleRate, _bitsPerSample, stepSize);
		redrawSpecs3D();
		redrawSpecs2D();
	}

	public boolean computeSpectrum(double[] data, int width, int height,
			int maxFreq, int windowSize, double rate, double[] grayscaleOut) {

		if (width < windowSize)
			return false;


		double[] processed = new double[windowSize];

		int i;
		for (i = 0; i < windowSize; i++)
			processed[i] = 0.0;
		int half = windowSize / 2;

		double[] in = new double[windowSize];
		double[] out = new double[windowSize];

		int start = 0;
		int windows = 0;
		while (start + windowSize <= width) {
			for (i = 0; i < windowSize; i++)
				in[i] = data[start + i];

			Filter.WindowFunc(_windowFunc, windowSize, in);

			FFT1d fft = new FFT1d(windowSize);
			fft.fft(in, out);
			out = Parameters.fftMag(in, out);

			for (i = 0; i < half; i++) {
				processed[i] += out[i];
			}

			start += half;
			windows++;
		}

		int maxSamples = (int) (maxFreq * windowSize / rate + 0.5);
		if (maxSamples > half)
			maxSamples = half;

		// Convert to decibels
		for (i = 0; i < maxSamples; i++) {
			processed[i] = 10 * MathUtils.log10(processed[i] / windowSize / windows);
		}

		// Finally, put it into bins in grayscaleOut[], normalized to a 0.0-1.0
		// scale

		for (i = 0; i < height; i++) {
			double bin0 = (double) i * maxSamples / height;
			double bin1 = (double) (i + 1) * maxSamples / height;

			double binwidth = bin1 - bin0;

			double value = 0.0;

			if ((int) bin1 == (int) bin0)
				value = processed[(int) bin0];
			else {
				value += processed[(int) bin0] * ((int) bin0 + 1 - bin0);
				bin0 = 1 + (int) bin0;
				while (bin0 < (int) bin1) {
					value += processed[(int) bin0];
					bin0 += 1.0;
				}
				value += processed[(int) bin1] * (bin1 - (int) bin1);

				value /= binwidth;
			}

			// Last step converts dB to a 0.0-1.0 range
			value = (value + 80.0) / 80.0;

			if (value > 1.0)
				value = 1.0;
			if (value < 0.0)
				value = 0.0;

			grayscaleOut[i] = value;
		}
		return true;
	}

}
