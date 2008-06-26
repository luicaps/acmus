package acmus.audio;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import acmus.AcmusGraphics;
import acmus.util.WaveUtils;

public class Meter extends Canvas implements IMeter, MouseListener {

	List<SingleMeter> _meters;
	int _channels = 0;
	GridLayout _gridLayout;

	public Meter(Composite parent, int style) {
		super(parent, style);
		this.setBackground(AcmusGraphics.BLACK);
		_meters = new ArrayList<SingleMeter>();
		_gridLayout = new GridLayout(1, true);
		_gridLayout.marginHeight = 0;
		_gridLayout.marginWidth = 0;
		_gridLayout.horizontalSpacing = 0;
		_gridLayout.verticalSpacing = 0;
		this.setLayout(_gridLayout);
	}

	public void setData(int data[], int channels, int b, int bitsPerSample) {
		if (channels > _channels) {
			for (int i = _meters.size(); i < channels; i++) {
				SingleMeter sm = new SingleMeter(this, SWT.NONE);
				sm.addMouseListener(this);
				_meters.add(sm);
				GridData gridData = new GridData(GridData.FILL_BOTH);
				sm.setLayoutData(gridData);
			}
			for (int i = _channels; i < channels; i++) {
				SingleMeter sm = _meters.get(i);
				GridData gridData = (GridData) sm.getLayoutData();
				gridData.exclude = false;
				sm.setVisible(true);
			}
		} else if (channels < _channels) {
			for (int i = channels; i < _channels; i++) {
				SingleMeter sm = _meters.get(i);
				GridData gridData = (GridData) sm.getLayoutData();
				gridData.exclude = true;
				sm.setVisible(false);
			}
		}
		_channels = channels;

		int d[][] = WaveUtils.splitAudioStream(channels, data);

		for (int i = 0; i < _channels; i++) {
			_meters.get(i).setData(d[i], b, bitsPerSample);
		}
		_gridLayout.numColumns = _channels;
		this.layout();
	}

	public void show(int x) {
		for (int i = 0; i < _channels; i++) {
			_meters.get(i).show(x);
		}
	}

	public void showLast() {
		for (int i = 0; i < _channels; i++) {
			_meters.get(i).showLast();
		}
	}

	public void reset() {
		for (int i = 0; i < _channels; i++) {
			_meters.get(i).reset();
		}
	}

	public void unclip() {
		for (int i = 0; i < _channels; i++) {
			_meters.get(i).unclip();
		}
	}

	public void resetPeak() {
		for (int i = 0; i < _channels; i++) {
			_meters.get(i).resetPeak();
		}
	}

	// XXX is it right?
	@SuppressWarnings("all")
	public void redrawChildren() {
		for (int i = 0; i < _channels; i++) {
			_meters.get(i).redraw();
		}
	}

	public void mouseDoubleClick(MouseEvent e) {
		unclip();
		resetPeak();
		redrawChildren();
	}

	public void mouseDown(MouseEvent e) {
	}

	public void mouseUp(MouseEvent e) {
		unclip();
		redrawChildren();
	}

}