/*
 *  WaveformDisplay.java
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
 * Created on 24/02/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package acmus.audio;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;
import acmus.dsp.Filter;
import acmus.util.ArrayUtils;
import acmus.util.MathUtils;
import acmus.util.WaveUtils;

/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class WaveformDisplay extends Composite {

	Composite _parent;

	List<SampleArray> _sampleArrays;

	int _channels;

	long _yMax;
	double _yScale = 1.0;
	int _xvStart = 0;

	int _yBarWidth = 50;
	int _xBarHeight = 40;

	double _yZoom = 1.0;
	double _yStart = 1.0;
	double _xZoom = 1.0;

	int _selSampleStart = -1, _selSampleEnd = -1;

	ScrollBar _hbar;
	ScrollBar _vbar;

	int _xMark = -1;
	double _samplesPerDot = 1;
	int _dotRadius = 2;

	float _sampleRate;
	int _numSamples;
	int _bitsPerSample;

	List<Waveform> _wf;

	TimeBar _timeBar;

	DecimalFormat format = new DecimalFormat("#.####");
	static protected DecimalFormat format2 = new DecimalFormat("#.###");

	SampleTransform _sampleT;
	Linear _linearSampleT = new Linear();
	Power _powerSampleT = new Power();

	int _drawingAreaHeight = 1;
	int _drawingAreaWidth = 0;

	MouseMoveListener _positionDisplayListener = null;
	SelectionListener _selectionStartDisplayListener = null;
	SelectionListener _selectionEndDisplayListener = null;

	public WaveformDisplay(Composite parent, int style) {
		super(parent, style | SWT.H_SCROLL | SWT.V_SCROLL);

		_parent = parent;

		_hbar = getHorizontalBar();
		_hbar.setMinimum(0);
		_hbar.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent se) {
				widgetSelected(se);
			}

			public void widgetSelected(SelectionEvent se) {
				updateXStart();
				for (Waveform w : _wf) {
					w.redraw();
				}
				_timeBar.redraw();
			}
		});
		_hbar.setVisible(false);

		_vbar = getVerticalBar();
		_vbar.setMinimum(0);
		_vbar.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent se) {
				widgetSelected(se);
			}

			public void widgetSelected(SelectionEvent se) {
				updateYStart();
				for (Waveform w : _wf) {
					w.redraw();
				}
			}
		});
		_vbar.setVisible(false);

		_wf = new ArrayList<Waveform>();

		_sampleArrays = new ArrayList<SampleArray>();

		setBackground(AcmusGraphics.WHITE);

		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 1;
		setLayout(gl);

		_timeBar = new TimeBar(this);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = _xBarHeight;
		_timeBar.setLayoutData(gridData);

		amplitude();
	}

	public void moveWaveUp(int index) {
		if (index == 0) {
			return;
		}
		SampleArray s = _sampleArrays.get(index);
		_sampleArrays.remove(index);
		_sampleArrays.add(index - 1, s);
		drawArray(index - 1, true);
	}

	public void moveWaveDown(int index) {
		if (index == numberOfArrays() - 1) {
			return;
		}
		SampleArray s = _sampleArrays.get(index);
		_sampleArrays.remove(index);
		_sampleArrays.add(index + 1, s);
		drawArray(index + 1, true);
	}

	public int numberOfArrays() {
		return _sampleArrays.size();
	}

	public void power() {
		_sampleT = _powerSampleT;
		setScale();
	}

	public void amplitude() {
		_sampleT = _linearSampleT;
		setScale();
	}

	public void setScale() {
		setYMax((int) Math.ceil(_sampleT.transf(WaveUtils
				.getLimit(_bitsPerSample))));
		updateYStart();
		for (Waveform w : _wf) {
			w.redraw();
		}
	}

	public float getSampleRate() {
		return _sampleRate;
	}

	public int getSelectionStartInSamples() {
		return _selSampleStart;
	}

	public int getSelectionStartInMillis() {
		return (int) Math.round((double) _selSampleStart / _sampleRate * 1000);
	}

	public int getSelectionEndInSamples() {
		return _selSampleEnd;
	}

	public int getSelectionEndInMillis() {
		return (int) Math.round((double) _selSampleEnd / _sampleRate * 1000);
	}

	public void zoomMode() {
		for (Waveform w : _wf) {
			w.zoomMode();
		}
	}

	public void selectMode() {
		for (Waveform w : _wf) {
			w.selectMode();
		}
	}

	public void updateXStart() {
		_xvStart = _hbar.getSelection();
	}

	public void updateYStart() {
		_yStart = _sampleT.unitMaxValue()
				- (_sampleT.unitValueRange() * _vbar.getSelection() / (_drawingAreaHeight * _yZoom));
	}

	public void setXMarkInNanoseconds(long time) {
		double sample = (double) time / 1000000000 * _sampleRate;
		_xMark = (int) (sample / _samplesPerDot) - _xvStart;
		for (Waveform w : _wf) {
			w.redraw();
		}
	}

	public void setXMarkInSamples(long sample) {
		_xMark = (int) (sample / _samplesPerDot) - _xvStart;
		for (Waveform w : _wf) {
			w.redraw();
		}
	}

	int _winSelX;
	int _winSelY;
	int _winSelWidth = -1;
	int _winSelHeight;

	public void setWindowSelection(int x, int y, int width, int height) {
		_winSelX = x;
		_winSelY = y;
		_winSelWidth = width;
		_winSelHeight = height;
	}

	public void eraseWindowSelection() {
		_winSelWidth = -1;
	}

	public void setSelection(int sampleStart, int sampleEnd) {
		if (sampleStart > sampleEnd) {
			_selSampleStart = sampleEnd;
			_selSampleEnd = sampleStart;
		} else {
			_selSampleStart = sampleStart;
			_selSampleEnd = sampleEnd;
		}
		if (_selSampleStart < 0) {
			_selSampleStart = 0;
		}
		if (_selSampleEnd >= _numSamples) {
			_selSampleEnd = _numSamples - 1;
		}
		if (_selectionStartDisplayListener != null) {
			_selectionStartDisplayListener.widgetSelected(null);
		}
		if (_selectionEndDisplayListener != null) {
			_selectionEndDisplayListener.widgetSelected(null);
		}
	}

	public boolean selectionValid() {
		return _selSampleStart >= 0;
	}

	public void setSelectionStart(int sample) {
		setSelection(sample, _selSampleEnd);
	}

	public void setSelectionStartInMillis(int pos) {
		setSelectionStart(millisToSample(pos));
		redrawWf();
	}

	public void setSelectionEnd(int sample) {
		setSelection(_selSampleStart, sample);
	}

	public void setSelectionEndInMillis(int pos) {
		setSelectionEnd(millisToSample(pos));
		redrawWf();
	}

	public void eraseSelection() {
		_selSampleEnd = _selSampleStart = -1;
		if (_selectionStartDisplayListener != null) {
			_selectionStartDisplayListener.widgetSelected(null);
		}
		if (_selectionEndDisplayListener != null) {
			_selectionEndDisplayListener.widgetSelected(null);
		}
	}

	public int millisToSample(int m) {
		return (int) Math.round((double) m / 1000 * _sampleRate);
	}

	public int sampleToMillis(int s) {
		return (int) Math.round((double) s / _sampleRate * 1000);
	}

	public void setXPos(int pos) {
		_xvStart = pos;
		_hbar.setSelection(pos);
	}

	public void setYPos(int pos) {
		_yStart = _sampleT.unitMaxValue()
				- (_sampleT.unitValueRange() * pos / (_drawingAreaHeight * _yZoom));
		_vbar.setSelection(pos);
	}

	public void setZoomWindow(int x, int y, int width, int height) {
		setXPos(_xvStart + x - _yBarWidth);
		setYPos(_vbar.getSelection() + y);
		setZoom(_xZoom * _drawingAreaWidth / width, _yZoom * _drawingAreaHeight
				/ height);
	}

	public void setZoom(double xZoom, double yZoom) {
		_hbar
				.setMaximum(10 + (int) Math.round(_drawingAreaWidth
						* (xZoom - 1)));
		_hbar.setSelection((int) (_xvStart * xZoom / _xZoom));
		_xvStart = (int) (_xvStart * xZoom / _xZoom);

		_vbar.setMaximum(10 + (int) Math
				.round(_drawingAreaHeight * (yZoom - 1)));

		_vbar
				.setSelection((int) Math
						.round(((_sampleT.unitMaxValue() - _yStart) * (_drawingAreaHeight * yZoom))
								/ _sampleT.unitValueRange()));

		_xZoom = xZoom;
		_yZoom = yZoom;

		if (_xZoom > 1) {
			_hbar.setVisible(true);
		} else {
			_hbar.setVisible(false);
			_xvStart = 0;
		}
		if (_yZoom > 1) {
			_vbar.setVisible(true);
		} else {
			_vbar.setVisible(false);
			// This is wrong!
			//_yStart = _sampleT.unitMaxValue();
		}

		for (Waveform w : _wf) {
			w.redraw();
		}
		_timeBar.redraw();
	}

	public void updateBars() {
		updateXBar();
		updateYBar();
	}

	public final void updateXBar() {
		double prop = (double) _hbar.getSelection() / _hbar.getMaximum();

		// Must add 10... is it a bug???
		int newXMax = 10 + (int) Math.round(_drawingAreaWidth * (_xZoom - 1));
		_hbar.setMaximum(newXMax);

		double xPos = prop * newXMax;
		_hbar.setSelection((int) Math.round(xPos));
		// This seems unnecessary...
		//updateXStart();
	}

	public final void updateYBar() {
		// Must add 10... is it a bug???
		int newYMax = 10 + (int) Math.round(_drawingAreaHeight * (_yZoom - 1));

		double yPos = (double) _vbar.getSelection() / _vbar.getMaximum()
				* newYMax;
		_vbar.setSelection((int) Math.round(yPos));

		_vbar.setMaximum(newYMax);
		// This seems unnecessary and causes some problems when changing zooms
		// making the graph become off-center
		//updateYStart();
	}

	public void zoomIn() {
		setZoom(_xZoom * 2, _yZoom);
	}

	public void zoomOut() {
		setZoom(_xZoom * 0.5, _yZoom);
	}

	public void xFit() {
		setZoom(1, _yZoom);
	}

	public void yZoomIn() {
		yZoom(1.5);
	}

	public void yZoomOut() {
		yZoom(0.75);
	}

	public void yZoom(double factor) {
		_yStart -= ((_sampleT.unitValueRange() / _yZoom) - (_sampleT
				.unitValueRange() / (factor * _yZoom))) / 2;
		setZoom(_xZoom, _yZoom * factor);
	}

	public void yFit() {
		updateYStart();
		setZoom(_xZoom, 1);
	}

	public void yFit2() {
		int[] data = _sampleArrays.get(0).data;
		double max = _sampleT.transf(data[(int) (_xvStart * _samplesPerDot)]);
		double min = max;
		for (int i = (int) (_xvStart * _samplesPerDot); i < (int) ((_xvStart + _drawingAreaWidth) * _samplesPerDot)
				&& i < data.length; i++) {
			double v = _sampleT.transf(data[i]);
			if (v > max) {
				max = v;
			} else if (v < min) {
				min = v;
			}
		}
		if (Double.isInfinite(max - min)) {
			yFit();
		} else {
			updateYStart();
			setZoom(_xZoom, 0.9 * _sampleT.unitValueRange() / (max - min));
			setYPos((int) ((_drawingAreaHeight * _yZoom)
					* ((_sampleT.unitMaxValue() - max) / _sampleT
							.unitValueRange()) - 0.05 * _drawingAreaHeight));
		}

	}

	public final void redrawWf() {
		for (Waveform w : _wf) {
			w.redraw();
		}
	}

	public void setData(int data[], int channels, float sampleRate,
			int bitsPerSample) {
		setData(data, channels, sampleRate, bitsPerSample, AcmusGraphics.BLUE2);
	}

	public void setData(int data[], int channels, float sampleRate,
			int bitsPerSample, Color color) {
		if (_channels < channels) {
			if (channels > _wf.size()) {
				// make channels visible
				for (int i = _channels; i < _wf.size(); i++) {
					Waveform w = _wf.get(i);
					((GridData) w.getLayoutData()).exclude = false;
					w.setVisible(true);
				}
				// add new channels
				for (int i = _wf.size(); i < channels; i++) {
					Waveform w = new Waveform(this, i);
					_wf.add(w);
					GridData gridData = new GridData(GridData.FILL_BOTH);
					w.setLayoutData(gridData);
				}

				redrawWf();
			} else {
				// make channels visible
				for (int i = _channels; i < channels; i++) {
					Waveform w = _wf.get(i);
					((GridData) w.getLayoutData()).exclude = false;
					w.setVisible(true);
				}
				redrawWf();
			}
		} else if (_channels > channels) {
			// make channels invisible
			for (int i = _channels; i < _wf.size(); i++) {
				Waveform w = _wf.get(i);
				((GridData) w.getLayoutData()).exclude = true;
				w.setVisible(false);
			}
			redrawWf();
		}

		_sampleArrays.clear();

		SampleArray sa = new SampleArray();
		sa.channels = channels;
		sa.sampleRate = sampleRate;
		sa.data = data;
		sa.color = color;
		sa.draw = true;
		_sampleArrays.add(sa);

		_channels = channels;
		_numSamples = data.length / channels;
		_sampleRate = sampleRate;
		_bitsPerSample = bitsPerSample;

		if (_positionDisplayListener != null) {
			_wf.get(0).removeMouseMoveListener(_positionDisplayListener);
			_wf.get(0).addMouseMoveListener(_positionDisplayListener);
		}

		layout();
		setZoom(1.0, 1.0);
	}

	public void addData(int data[], int channels, float sampleRate, Color color) {
		SampleArray sa = new SampleArray();
		sa.channels = channels;
		sa.sampleRate = sampleRate;
		sa.data = data;
		sa.color = color;
		sa.draw = true;
		_sampleArrays.add(sa);
		int numSamples = data.length / channels;
		if (numSamples > _numSamples) {
			_numSamples = numSamples;
		}
	}

	public void drawArray(int index, boolean draw) {
		_sampleArrays.get(index).draw = draw;
		redrawWf();
	}

	public void setYMax(long yMax) {
		_yMax = yMax;
	}

	public long getYMax() {
		return _yMax;
	}

	public static double findStepDur(double len) {
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

	public void setPositionDisplayListener(MouseMoveListener l) {
		_positionDisplayListener = l;
	}

	public int positionInMillis(int x) {
		int samples = (int) Math.round((_xvStart + x - _yBarWidth)
				* _samplesPerDot);
		return (int) Math.round((double) samples / _sampleRate * 1000);
	}

	public void setSelectionStartDisplayListener(SelectionListener l) {
		_selectionStartDisplayListener = l;
	}

	public void setSelectionEndDisplayListener(SelectionListener l) {
		_selectionEndDisplayListener = l;
	}

	public void filter(Filter f) {
		int max = WaveUtils.getLimit(_bitsPerSample);
		for (SampleArray s : _sampleArrays) {
			int[][] streams = WaveUtils.splitAudioStream(s.channels, s.data);
			for (int i = 0; i < streams.length; i++) {
				double x[] = ArrayUtils.scaleToUnit(streams[i], max);
				x = Filter.filtfilt(f.b, f.a, x);
				streams[i] = ArrayUtils.scaleToMax(x, max, false);
			}
			s.data = WaveUtils.joinAudioStream(streams);
		}
		redrawWf();
	}

	public void cut(int startSample, int endSample) {
		for (SampleArray s : _sampleArrays) {
			int start = startSample * s.channels;
			int end = endSample * s.channels;
			s.data = ArrayUtils.subArray(s.data, start, end);
		}
		redrawWf();
	}

	public void save(String filename) {
		// save only the first samplearray
		WaveUtils.wavWrite(_sampleArrays.get(0).data,
				_sampleArrays.get(0).channels, _bitsPerSample,
				/*HARD CODED*/(float)44100, filename);
	}

	/* ======================================================================= */

	class SampleArray {
		int[] data;
		int channels;
		double sampleRate;
		Color color;
		boolean draw;
	}

	/* ======================================================================= */

	class Waveform extends Canvas implements ControlListener, PaintListener,
			MouseListener, MouseMoveListener {

		int _ch;

		ZoomMouseListener _zoomMl;
		SelectMouseListener _selMl;
		MouseListener _mouseL;
		MouseMoveListener _mMoveL;
		Color _background = AcmusGraphics.BLACK;

		public Waveform(Composite parent, int channelNumber) {
			super(parent, AcmusGraphics.DOUBLE_BUFFER_STYLE);
			_ch = channelNumber;
			setBackground(_background);
			addPaintListener(this);
			_zoomMl = new ZoomMouseListener();
			_selMl = new SelectMouseListener();
			zoomMode();
			addMouseListener(this);
			addMouseMoveListener(this);
			addControlListener(this);

		}

		public void zoomMode() {
			_mouseL = _zoomMl;
			_mMoveL = _zoomMl;
		}

		public void selectMode() {
			_mouseL = _selMl;
			_mMoveL = _selMl;
		}

		public final int drawingAreaWidth() {
			return getBounds().width - _yBarWidth;
		}

		public final int drawingAreaHeight() {
			return getBounds().height;
		}

		public void paintControl(PaintEvent e) {
			int _width = getBounds().width;
			int _height = getBounds().height;

			_samplesPerDot = ((double) _numSamples / (_width - _yBarWidth))
					/ _xZoom;

			GC gc;
			Image buffer = null;

			if (AcmusGraphics.DOUBLE_BUFFER) {
				buffer = new Image(AcmusPlugin.getDefault().getWorkbench()
						.getDisplay(), _width, _height);
				gc = new GC(buffer);
				gc.setBackground(AcmusGraphics.BLACK);
				gc.fillRectangle(0, 0, _width, _height);

			} else {
				gc = e.gc;
			}

			int x1, y1, x2, y2;

			double scale = _yZoom * _height / _sampleT.unitsPerSample(_yMax);
			int yShift = (int) Math.round((_sampleT.unitMaxValue() - _yStart)
					/ _sampleT.unitValueRange() * _yZoom * _height);

			for (SampleArray sa : _sampleArrays) {

				if (!sa.draw) {
					continue;
				}

				int[] data = sa.data;
				int channels = sa.channels;
				Color color = sa.color;

				if (_samplesPerDot < 1) {

					if (((int) Math.round(_xvStart * _samplesPerDot))
							* channels + _ch < data.length) {

						int step = (int) Math.round(1 / _samplesPerDot);
						gc.setForeground(color);

						{
							x1 = 0;
							y1 = (int) Math.round((_yMax - _sampleT
									.transf(data[((int) Math.round(_xvStart
											* _samplesPerDot))
											* channels + _ch]))
									* scale)
									- yShift;
							for (int i = (int) Math.round(_xvStart
									* _samplesPerDot) + 1, k = step; i < data.length
									/ channels
									&& k < _width + step; i++, k += step) {

								x2 = k;
								y2 = (int) Math.round((_yMax - _sampleT
										.transf(data[i * channels + _ch]))
										* scale)
										- yShift;
								gc.drawLine(x1 + _yBarWidth, y1, x2
										+ _yBarWidth, y2);
								x1 = x2;
								y1 = y2;
							}
						}

						if (_dotRadius * 4 < step) {
							for (int i = (int) Math.round(_xvStart
									* _samplesPerDot), k = 0; i < data.length
									/ channels - _samplesPerDot
									&& k < _width; i++, k += step) {
								x2 = k;
								y2 = (int) Math.round((_yMax - _sampleT
										.transf(data[i * channels + _ch]))
										* scale)
										- yShift;
								gc.setBackground(AcmusGraphics.GREEN);
								gc.fillOval(x2 - _dotRadius + _yBarWidth, y2
										- _dotRadius, _dotRadius * 2,
										_dotRadius * 2);
								gc.setForeground(AcmusGraphics.BLUE2);
								gc.drawOval(x2 - _dotRadius + _yBarWidth, y2
										- _dotRadius, _dotRadius * 2,
										_dotRadius * 2);
							}
						}
					}

				} else if (_xvStart * _samplesPerDot * channels + _ch < data.length) {

					// initial position
					{
						int i = (int) (_xvStart * _samplesPerDot);
						int iMax = i * channels + _ch;
						int iMin = i * channels + _ch;
						for (int j = i; j < i + _samplesPerDot; j++) {
							if (_sampleT.transf(data[j * channels + _ch]) > _sampleT
									.transf(data[iMax])) {
								iMax = j * channels + _ch;
							} else if (_sampleT
									.transf(data[j * channels + _ch]) < _sampleT
									.transf(data[iMin])) {
								iMin = j * channels + _ch;
							}
						}
						int left = (iMax < iMin) ? iMax : iMin;
						x1 = 0;
						y1 = (int) Math.round((_yMax - _sampleT
								.transf(data[left]))
								* scale)
								- yShift;
					}

					// gc.setAlpha(100);
					for (int i = (int) (_xvStart * _samplesPerDot), k = 0; i <= data.length
							/ channels - _samplesPerDot
							&& k < _width; i = (int) Math
							.round((_xvStart * _samplesPerDot)
									+ (_samplesPerDot * ++k))) {

						int iMax = i * channels + _ch;
						int iMin = i * channels + _ch;
						for (int j = i; j < i + _samplesPerDot; j++) {
							if (_sampleT.transf(data[j * channels + _ch]) > _sampleT
									.transf(data[iMax])) {
								iMax = j * channels + _ch;
							} else if (_sampleT
									.transf(data[j * channels + _ch]) < _sampleT
									.transf(data[iMin])) {
								iMin = j * channels + _ch;
							}
						}

						int left, right;
						if (iMax < iMin) {
							left = iMax;
							right = iMin;
						} else {
							left = iMin;
							right = iMax;
						}

						x2 = k;
						y2 = (int) Math.round((_yMax - _sampleT
								.transf(data[left]))
								* scale)
								- yShift;
						drawLine(gc, color, color, x1 + _yBarWidth, y1, x2
								+ _yBarWidth, y2);
						x1 = x2;
						y1 = y2;
						y2 = (int) Math.round((_yMax - _sampleT
								.transf(data[right]))
								* scale)
								- yShift;
						drawLine(gc, color, color, x1 + _yBarWidth, y1, x2
								+ _yBarWidth, y2);
						x1 = x2;
						y1 = y2;

					}
				}
			}

			if (_winSelWidth > 0) {
				gc.setForeground(AcmusGraphics.YELLOW);
				gc.drawRectangle(_winSelX, _winSelY, _winSelWidth,
						_winSelHeight);
			}
			if (_xMark >= 0) {
				gc.setForeground(AcmusGraphics.GREEN);
				gc.drawLine(_xMark + _yBarWidth, 0, _xMark + _yBarWidth,
						_height);
			}
			if (_selSampleStart >= 0) {
				int xS = _yBarWidth - _xvStart
						+ (int) Math.floor(_selSampleStart / _samplesPerDot);
				int xE = _yBarWidth - _xvStart
						+ (int) Math.floor(_selSampleEnd / _samplesPerDot);
				gc.setForeground(AcmusGraphics.YELLOW);
				gc.drawLine(xS, 0, xS, _height);
				gc.drawLine(xE, 0, xE, _height);
				gc.setAlpha(70);
				gc.setBackground(AcmusGraphics.YELLOW);
				gc.fillRectangle(xS, 0, xE - xS, _height);
				gc.setAlpha(255);
			}

			gc.setBackground(AcmusGraphics.GRAY);
			gc.fillRectangle(0, 0, _yBarWidth, _height);
			gc.setForeground(AcmusGraphics.WHITE);
			gc.drawLine(_yBarWidth, 0, _yBarWidth, _height);

			FontMetrics fm = gc.getFontMetrics();

			// the rotated label
			String yLabel = _sampleT.label();
			gc.setFont(AcmusGraphics.BOLD);
			gc.setForeground(AcmusGraphics.BLACK);
			fm = gc.getFontMetrics();
			Transform oldTr = new Transform(AcmusPlugin.getDefault()
					.getWorkbench().getDisplay());
			gc.getTransform(oldTr);
			Transform tr = new Transform(AcmusPlugin.getDefault()
					.getWorkbench().getDisplay());
			tr.translate(2, _height / 2 + fm.getAverageCharWidth()
					* yLabel.length() / 2);
			tr.rotate(-90);
			gc.setTransform(tr);
			gc.drawString(yLabel, 0, 0);

			gc.setFont(AcmusGraphics.DEFAULT_FONT);
			fm = gc.getFontMetrics();
			gc.setTransform(oldTr);

			double stepVal = findStepDur(_sampleT.unitValueRange() / _yZoom) * 2;

			int steps = (int) (_sampleT.unitValueRange() / _yZoom / stepVal);
			double stepLen = drawingAreaHeight()
					/ (_sampleT.unitValueRange() / _yZoom / stepVal);

			double initialGapVal = ((((_yStart + _sampleT.unitMaxValue()) / stepVal) - Math
					.floor((_yStart + _sampleT.unitMaxValue()) / stepVal)))
					* stepVal;
			int initialGapLen = (int) Math.round(initialGapVal
					* (drawingAreaHeight() * _yZoom)
					/ _sampleT.unitValueRange());

			for (int i = 0; i <= steps; i++) {
				int y = initialGapLen + (int) Math.round(i * stepLen);
				gc.setForeground(AcmusGraphics.WHITE);
				gc.drawLine(_yBarWidth - 5, y, _yBarWidth, y);
				gc.setForeground(AcmusGraphics.BLACK);
				String l = format.format(_yStart - i * stepVal - initialGapVal);
				gc.drawString(l, _yBarWidth - 8
						- (fm.getAverageCharWidth() * l.length()), y
						- fm.getHeight() / 2, true);
			}

			if (AcmusGraphics.DOUBLE_BUFFER) {
				e.gc.drawImage(buffer, 0, 0, _width, _height, 0, 0, _width,
						_height);
				gc.dispose();
				buffer.dispose();
			}

		}

		private final void drawLine(GC gc, Color out, Color in, int x1, int y1,
				int x2, int y2) {
			gc.setForeground(out);
			gc.drawLine(x1, y1, x2, y2);
		}

		public void controlMoved(ControlEvent e) {
			// TODO Auto-generated method stub

		}

		public void controlResized(ControlEvent e) {
			updateBars();
			_drawingAreaHeight = getBounds().height;
			_drawingAreaWidth = getBounds().width - _yBarWidth;
		}

		/*
		 * ----------------------------------------------------------------------
		 */

		public void mouseMove(MouseEvent e) {
			_mMoveL.mouseMove(e);
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

		/*
		 * ----------------------------------------------------------------------
		 */
		class ZoomMouseListener implements MouseListener, MouseMoveListener {
			int winX = 0, winY = 0;

			boolean drag = false;

			public void mouseDoubleClick(MouseEvent e) {
				setZoom(1.0, 1.0);
			}

			public void mouseDown(MouseEvent e) {
				winX = e.x;
				winY = e.y;
				drag = true;
			}

			public void mouseUp(MouseEvent e) {
				drag = false;
				int winX2, winY2;
				if (e.x < winX) {
					winX2 = winX;
					winX = e.x > _yBarWidth ? e.x : _yBarWidth;
				} else if (e.x > winX) {
					winX2 = e.x > _yBarWidth ? e.x : _yBarWidth;
				} else {
					return;
				}
				if (e.y < winY) {
					winY2 = winY;
					winY = e.y > 0 ? e.y : 0;
				} else if (e.y > winY) {
					winY2 = e.y > 0 ? e.y : 0;
				} else {
					return;
				}
				setZoomWindow(winX, winY, winX2 - winX, winY2 - winY);
				eraseWindowSelection();
				redraw();
			}

			public void mouseMove(MouseEvent e) {
				if (!drag) {
					return;
				}
				int x1, y1, x2, y2;
				if (e.x < winX) {
					x2 = winX;
					x1 = e.x > _yBarWidth ? e.x : _yBarWidth;
				} else if (e.x > winX) {
					x1 = winX;
					x2 = e.x > _yBarWidth ? e.x : _yBarWidth;
				} else {
					return;
				}
				if (e.y < winY) {
					y2 = winY;
					y1 = e.y > 0 ? e.y : 0;
				} else if (e.y > winY) {
					y2 = e.y > 0 ? e.y : 0;
					y1 = winY;
				} else {
					return;
				}
				setWindowSelection(x1, y1, x2 - x1, y2 - y1);
				redraw();
			}
		}

		/*
		 * ----------------------------------------------------------------------
		 */

		class SelectMouseListener implements MouseListener, MouseMoveListener {
			int xS = 0;

			boolean drag = false;

			int button = 0;

			public void mouseDoubleClick(MouseEvent e) {
				if (selectionValid()) {
					eraseSelection();
				} else {
					setSelection(0, _numSamples);
				}
				redraw();
			}

			public void mouseDown(MouseEvent e) {
				xS = e.x;
				drag = true;
				button = e.button;
			}

			public void mouseUp(MouseEvent e) {
				drag = false;
				int xE;

				if (e.button == 2) {
					int s = (int) ((e.x - _yBarWidth + _xvStart) * _samplesPerDot);
					setSelectionStart(s);
				} else if (e.button == 3) {
					int s = (int) ((e.x - _yBarWidth + _xvStart) * _samplesPerDot);
					setSelectionEnd(s);
				} else {
					if (e.x < xS) {
						xE = xS;
						xS = e.x > 0 ? e.x : 0;
					} else if (e.x > xS) {
						xE = e.x;
					} else {
						return;
					}

					int sS = (int) ((xS - _yBarWidth + _xvStart) * _samplesPerDot);
					int sE = (int) ((xE - _yBarWidth + _xvStart) * _samplesPerDot);
					setSelection(sS, sE);
				}
				redraw();
			}

			public void mouseMove(MouseEvent e) {
				if (!drag) {
					return;
				}
				if (button == 2) {
					int s = (int) ((e.x - _yBarWidth + _xvStart) * _samplesPerDot);
					setSelectionStart(s);
				} else if (button == 3) {
					int s = (int) ((e.x - _yBarWidth + _xvStart) * _samplesPerDot);
					setSelectionEnd(s);
				} else {
					int x1, x2;
					if (e.x < xS) {
						x2 = xS;
						x1 = e.x > 0 ? e.x : 0;
					} else if (e.x > xS) {
						x1 = xS;
						x2 = e.x;
					} else {
						return;
					}
					int sS = (int) ((x1 - _yBarWidth + _xvStart) * _samplesPerDot);
					int sE = (int) ((x2 - _yBarWidth + _xvStart) * _samplesPerDot);
					setSelection(sS, sE);
				}
				redraw();
			}
		}

	}

	/* ========================================================================= */

	class TimeBar extends Canvas implements PaintListener {
		public TimeBar(Composite parent) {
			super(parent, SWT.NONE);
			setBackground(AcmusGraphics.GRAY);
			addPaintListener(this);
		}

		public void paintControl(PaintEvent e) {
			GC gc = e.gc;

			int width = getBounds().width;
			int height = getBounds().height;

			_samplesPerDot = ((double) _numSamples / (width - _yBarWidth))
					/ _xZoom;

			double startDur = _xvStart * _samplesPerDot / _sampleRate;
			double dur = _samplesPerDot * (width - _yBarWidth) / _sampleRate;
			double stepDur = findStepDur(dur);
			boolean millis = (stepDur < 0.01) ? true : false;
			double initialGapDur = (1 - ((startDur / stepDur) - Math
					.floor((startDur / stepDur))))
					* stepDur;
			int steps = (int) Math.round(dur / stepDur);
			double stepLen = (width - _yBarWidth) / (dur / stepDur);
			int initialGapLen = (int) Math.round((initialGapDur / stepDur)
					* stepLen);

			if (steps > 0) {
				gc.setForeground(AcmusGraphics.BLACK);
				gc.setFont(AcmusGraphics.BOLD);
				String yLabel = "time (" + (millis ? "ms" : "s") + ")";
				gc.drawString(yLabel, width / 2
						- (gc.stringExtent(yLabel).x / 2), 2);

				gc.setFont(AcmusGraphics.DEFAULT_FONT);

				Point strLen;
				for (int i = -1; i < steps; i++) {
					gc.setForeground(AcmusGraphics.WHITE);
					int x = initialGapLen + _yBarWidth
							+ (int) Math.round(i * stepLen);
					gc.drawLine(x, height, x, height - 5);
					gc.setForeground(AcmusGraphics.BLACK);
					double val = stepDur * i + startDur + initialGapDur;
					if (millis) {
						val *= 1000;
					}
					String l = format2.format(val);
					strLen = gc.stringExtent(l);
					gc.drawString(l, x - strLen.x / 2, height - 6 - strLen.y,
							true);
				}
			}

		}
	}

	/* ========================================================================= */

	interface SampleTransform {
		public double transf(int y);

		public long unitsPerSample(long max);

		public double unitValueRange();

		public double unitMaxValue();

		public double unitMinValue();

		public String label();
	}

	class Linear implements SampleTransform {
		public final double transf(int y) {
			return (double) y / WaveUtils.getLimit(_bitsPerSample);
		}

		public long unitsPerSample(long max) {
			return 2 * max;
		}

		public double unitValueRange() {
			return 2.0;
		}

		public double unitMaxValue() {
			return 1.0;
		}

		public double unitMinValue() {
			return -1.0;
		}

		public String label() {
			return "amplitude ([1,-1])";
		}
	}

	class Power implements SampleTransform {
		public final double transf(int y) {
			double val = 20 * MathUtils.log10(Math.abs(y));
			if (val == Double.NEGATIVE_INFINITY) {
				return -100;
			}
			return val;
		}

		public long unitsPerSample(long max) {
			return max;
		}

		public double unitValueRange() {
			return transf(WaveUtils.getLimit(_bitsPerSample));
		}

		public double unitMaxValue() {
			return 0;
		}

		public double unitMinValue() {
			return transf(-1 * WaveUtils.getLimit(_bitsPerSample));
		}

		public String label() {
			return "power (db)";
		}
	}
}
