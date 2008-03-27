package acmus.audio;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import acmus.AcmusGraphics;
import acmus.dsp.Util;

public class Meter extends Canvas implements IMeter , MouseListener{

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

    int d[][] = Util.splitAudioStream(channels, data);

    for (int i = 0; i < _channels; i++) {
      _meters.get(i).setData(d[i], b, bitsPerSample);
      //GridData gridData = (GridData) _meters.get(i).getLayoutData();
      // gridData.widthHint = this.getBounds().width/_channels;
      // gridData.widthHint = _width/_channels;
      //System.out.println(" Channel " + i + " w " + gridData.widthHint);
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

class SingleMeter extends Composite implements PaintListener {
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

  // static int foo = 0;
  // int bar = foo++;

  public SingleMeter(Composite parent, int style) {
    super(parent, style);
    _parent = parent;
    this.addPaintListener(this);
    this.setBackground(AcmusGraphics.BLACK);
    // _maxVal = 2 * Math.log(2 << 15);
    // _maxVal = (int)toDb(2<<15, 60);
    _maxVal = (2 << 15) - 1;
  }

  public void setData(int[] data, int b, int bitsPerSample) {
    _maxVal = (1 << (bitsPerSample-1)) -1;
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
        _rms[i] += ((double) data[j] / _maxVal) * ((double) data[j] / _maxVal);
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
    // System.out.println(_peak.length);

    // // int i, j;
    // // float *sptr = sampleData;
    // // int num = intmin(numChannels, mNumBars);
    // // MeterUpdateMsg msg;
    // //
    // // msg.numFrames = numFrames;
    // // for(j=0; j<mNumBars; j++) {
    // // msg.peak[j] = 0;
    // // msg.rms[j] = 0;
    // // msg.clipping[j] = false;
    // // msg.headPeakCount[j] = 0;
    // // msg.tailPeakCount[j] = 0;
    // // }
    // //
    // // for(i=0; i<numFrames; i++) {
    // // for(j=0; j<num; j++) {
    // // msg.peak[j] = floatMax(msg.peak[j], sptr[j]);
    // // msg.rms[j] += sptr[j]*sptr[j];
    // //
    // // // In addition to looking for mNumPeakSamplesToClip peaked
    // // // samples in a row, also send the number of peaked samples
    // // // at the head and tail, in case there's a run of
    // // // Send the number of peaked samples at the head and tail,
    // // // in case there's a run of peaked samples that crosses
    // // // block boundaries
    // // if (fabs(sptr[j])>=1.0) {
    // // if (msg.headPeakCount[j]==i)
    // // msg.headPeakCount[j]++;
    // // msg.tailPeakCount[j]++;
    // // if (msg.tailPeakCount[j] > mNumPeakSamplesToClip)
    // // msg.clipping[j] = true;
    // // }
    // // else
    // // msg.tailPeakCount[j] = 0;
    // // }
    // // sptr += numChannels;
    // // }
    // // for(j=0; j<mNumBars; j++)
    // // msg.rms[j] = sqrt(msg.rms[j]/numFrames);
    // //
    // // if (mDB) {
    // // for(j=0; j<mNumBars; j++) {
    // // msg.peak[j] = ToDB(msg.peak[j], mDBRange);
    // // msg.rms[j] = ToDB(msg.rms[j], mDBRange);
    // // }
    // // }
    // //
    // // mQueue.Put(msg);
    //
  }

  public void show(int x) {
    // _p = _peak[x] > 0 ? 2 * Math.log(_peak[x]) / _maxVal : 0;
    // _r = _rms[x] > 0 ? 2 * Math.log(_rms[x]) / _maxVal : 0;
    _p = _peak[x];
    // _r = _r *0.9 + _rms[x]*0.1;
    _r = _rms[x];
    // System.out.println(_p+" " + _r + " | " + _peak[x] + " " + _rms[x] + " "+
    // x);

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
    gc.fillRectangle(0, (int) ((1 - _p) * height + barYOffset), width, height);
    gc.fillRectangle(0, (int) (Math.ceil((1 - _maxPeak) * height) + barYOffset), width, 2);
    gc.setBackground(AcmusGraphics.GREEN2);
    gc.fillRectangle(0, (int) (Math.ceil((1 - _r) * height) + barYOffset), width, height);
  }

  public static final double clipZeroToOne(double z) {
    if (z > 1.0)
      return 1.0;
    else if (z < 0.0)
      return 0.0;
    else
      return z;
  }

  public static final double toDb(double v, double range) {
    double db;
    if (v > 0)
      db = 20 * Util.log10(Math.abs(v));
    else
      db = -999;
    // System.out.println("db " + db);
    return clipZeroToOne((db + range) / range);
  }

}