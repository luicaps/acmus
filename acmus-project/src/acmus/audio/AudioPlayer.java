/*
 *  AudioPlayer.java
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
 * AudioPlayer.java
 * Created on 10/03/2005
 */
package acmus.audio;

import java.io.File;
import java.io.FileInputStream;

import javax.media.Control;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.GainControl;
import javax.media.Manager;
import javax.media.Owned;
import javax.media.Player;
import javax.media.Renderer;
import javax.media.Time;
import javax.media.control.BufferControl;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.FileEditorInput;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;
import acmus.dsp.Util;

import com.sun.media.renderer.audio.device.JavaSoundOutput;

/**
 * @author lku
 */
public class AudioPlayer {

	public static final int BUFFERLENGTH = 100;

	Composite _parent;

	FileEditorInput _input;
	WaveformDisplay _waveform;

	SpectrumDisplay _spectrum;

	ToolBar _controlBar;
	ToolItem _tiPlay;
	ToolItem _tiStop;
	// ToolItem _tiPause;

	IMeter _dbMeter = NullDbMeter.getInstance();

	Image _imgPlay = AcmusGraphics.IMG_PLAY;
	Image _imgPause = AcmusGraphics.IMG_PAUSE;

	GainControl _gainControl;
	Slider _gainSlider;

	ToolBar _zoomBar;
	ToolItem _tiXZoomIn;
	ToolItem _tiXFit;
	ToolItem _tiXZoomOut;
	ToolItem _tiYZoomIn;
	ToolItem _tiYReset;
	ToolItem _tiYFit;
	ToolItem _tiYZoomOut;
  ToolItem _tiBackward;
  ToolItem _tiForward;

	ToolBar _selModeBar;
	ToolItem _tiZoomMode;
	ToolItem _tiSelectXMode;

	Combo _graphMode;

	Player _player;

	Button _bExplain;

	Text _tPosition;
	Text _tSelStart;
	Text _tSelEnd;
  
  Combo _spectrumWindowFunc;
  Combo _spectrumWindowSize;
  Text _spectrumFreq;
  Text _spectrumDb;
  

	JavaSoundOutput _jso;
	SourceDataLine _sdl;

	int[] _audioData;
	AudioInputStream _audioStream;

	int _status;

	static final int PLAYING = 1;
	static final int STOPPED = 2;
	static final int PAUSED = 3;

	Object _playLock = new Object();

	AudioPlayer _this;

	ControllerListener _cListener = new ControllerListener() {
		public void controllerUpdate(ControllerEvent arg0) {
			// System.out.println(arg0);
			if (arg0 instanceof EndOfMediaEvent) {
				System.out.println("EndOfMedia");
				stop();
			}
		}
	};

	public AudioPlayer() {
		_this = this;
	}

	public void open(String filename) {
		try {
			File f = new File(filename);
			if (_player != null) {
				_player.removeControllerListener(_cListener);
				_player.deallocate();
			}
			_player = Manager.createRealizedPlayer(f.toURL());
			_player.addControllerListener(_cListener);
			Control cs[] = _player.getControls();
			Object owner;
			for (int i = 0; i < cs.length; i++) {
				if (cs[i] instanceof Owned && cs[i] instanceof BufferControl) {
					owner = ((Owned) cs[i]).getOwner();
					if (owner instanceof Renderer) {
						((BufferControl) cs[i]).setBufferLength(BUFFERLENGTH);
						System.out.println("Buffer length: "
								+ ((BufferControl) cs[i]).getBufferLength());
					}
				}
			}
			_gainControl = _player.getGainControl();
      if (_audioStream != null) {
        _audioStream.close();
      }
			_audioStream = AudioSystem.getAudioInputStream(new FileInputStream(
					filename));
			_audioData = readData2(_audioStream);

			_jso = new JavaSoundOutput();
			_jso.initialize(JavaSoundOutput.convertFormat(_audioStream.getFormat()),
					BUFFERLENGTH);

      DataLine.Info info = null;
			AudioFormat format = _audioStream.getFormat();
			if (format.getSampleSizeInBits() == 32) {
				_audioBytes = Util.downsample32to16(_audioBytes);
				format = new AudioFormat(format.getEncoding(), format.getSampleRate(), 16, format.getChannels(), 2, format.getFrameRate(), format.isBigEndian());
				info = new DataLine.Info(SourceDataLine.class, format);
				System.out.println("32 bit audio playback unsupported, downsampling (for playback only).");
			}
			else {
			 info = new DataLine.Info(SourceDataLine.class, _audioStream
					.getFormat());
			}
			
			try {
			_sdl = (SourceDataLine) AudioSystem.getLine(info);
			// _sdl.open(_audioStream.getFormat());
			}
			catch (Exception e){
				e.printStackTrace();
			}

			if (_waveform != null) {
				_waveform.setData(_audioData, _audioStream.getFormat().getChannels(),
						_audioStream.getFormat().getSampleRate(), _audioStream.getFormat().getSampleSizeInBits());
			}

			_dbMeter
					.setData(_audioData, _audioStream.getFormat().getChannels(), 1000, _audioStream.getFormat().getSampleSizeInBits());

			if (_spectrum != null) {
				_spectrum.setData(_audioData, _audioStream.getFormat().getChannels(),
						_audioStream.getFormat().getSampleRate(), _audioStream.getFormat()
								.getSampleSizeInBits(), 1024);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

  public int getBitsPerSample() {
    return _audioStream.getFormat().getSampleSizeInBits();
  }
  
	int _off;
	int nBytesRead;
	byte[] _audioBytes;

	public void play() {
		System.out.println("Playing... " + this);
		_tiPlay.setImage(_imgPause);
		// _player.start();

		if (_status != PAUSED) {
			_tiStop.setEnabled(true);
			_off = 0;
			_dbMeter.resetPeak();

			try {
				_sdl.open();
			} catch (Exception e) {
				e.printStackTrace();
			}

			_sdl.start();
		}

		_status = PLAYING;

		(new Thread() {
			public void run() {
				int nBytesWritten = 1;
				int bufSizeInMillis = 1000;
				int bufsize = (int) ((double) bufSizeInMillis / 1000 * _audioStream
						.getFormat().getFrameRate())
						/ _audioStream.getFormat().getFrameSize()
						* _audioStream.getFormat().getFrameSize();
				int len = 0;
				System.out.println(bufsize + " " + bufSizeInMillis + " "
						+ _audioBytes.length);
				while (_status == PLAYING && nBytesWritten > 0
						&& _off < _audioBytes.length) {
					len = ((_off + bufsize) >= _audioBytes.length) ? _audioBytes.length
							- _off : bufsize;
					// len = ((_off + bufsize) % _audioBytes.length) - _off
					nBytesWritten = _sdl.write(_audioBytes, _off, len);
					_off += bufsize;
				}
				if (_off >= _audioBytes.length) {
					AcmusPlugin.getDefault().getWorkbench().getDisplay().asyncExec(
							new Runnable() {
								public void run() {
									_dbMeter.showLast();
								}
							});
					_sdl.drain();
					_this.stop();
				}
			}
		}).start();

		(new Thread() {
			public void run() {
				System.out.println(_sdl.getMicrosecondPosition());
				// long length = (long) (_audioStream.getFrameLength() * _audioStream
				// .getFormat().getFrameRate());
				while (_status == PLAYING) {
					AcmusPlugin.getDefault().getWorkbench().getDisplay().syncExec(
							new Runnable() {
								public void run() {
									// System.out.println(_player.getMediaNanoseconds() + " - " +
									// _status);
									_waveform
											.setXMarkInNanoseconds(_sdl.getMicrosecondPosition() * 1000);
									_dbMeter
											.show((int) (_sdl.getMicrosecondPosition() / 1000000.0
													* _audioStream.getFormat().getFrameRate() / 1000));
								}

							});
					try {
						Thread.sleep(50);
					} catch (Exception e) {
					}
				}
				// AcmusPlugin.getDefault().getWorkbench().getDisplay().syncExec(
				// new Runnable() {
				// public void run() {
				// _dbMeter.showLast();
				// System.out.println("ssss " + _showLast);
				// if (_showLast) {
				// _dbMeter.showLast();
				// }
				// }
				//
				// });
			}
		}).start();
	}

	public void stop() {
		_player.stop();
		_player.setMediaTime(new Time(0));
		_sdl.stop();
		_sdl.flush();
		_sdl.close();
		_status = STOPPED;
		synchronized (_playLock) {
			System.out.println("Stopping...");
			_playLock.notifyAll();
		}
		AcmusPlugin.getDefault().getWorkbench().getDisplay().asyncExec(
				new Runnable() {
					public void run() {
						_waveform.setXMarkInNanoseconds(0);
						_tiPlay.setImage(_imgPlay);
						_tiStop.setEnabled(false);
						_dbMeter.reset();
					}
				});
	}

	public void pause() {
		if (_status == PAUSED) {
			play();
		} else {
			_player.stop();
			_status = PAUSED;
			_tiPlay.setImage(_imgPlay);
			_tiStop.setEnabled(true);
		}
	}

	public void waitStop() {
		synchronized (_playLock) {
			while (_status == PLAYING) {
				System.out.println("Waiting...");
				try {
					_playLock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public boolean isPlaying() {
		return (_status == PLAYING);
	}
  
  public int getTotalDurationInMillis() {
    return (int)Math.ceil(1000 * (double)_audioStream.getFrameLength()/_audioStream.getFormat().getFrameRate());
  }

	public SpectrumDisplay createSpectrumDisplay(Composite parent, int style) {
		_spectrum = new SpectrumDisplay(parent, style);
    if (_audioData != null)
    _spectrum.setData(_audioData, _audioStream.getFormat().getChannels(),
        _audioStream.getFormat().getSampleRate(), _audioStream.getFormat()
            .getSampleSizeInBits(), 1024);
    _spectrum.setPositionDisplay(_tPosition);
    _spectrum.setFrequencyDisplay(_spectrumFreq);
    _spectrum.setDbDisplay(_spectrumDb);
		return _spectrum;
	}
  public Combo createSpectrumWindowFunc(Composite parent, int style) {
    Label l = new Label(parent, SWT.NONE);
    l.setText("Function:");
    _spectrumWindowFunc = new Combo(parent, SWT.READ_ONLY);
    _spectrumWindowFunc.add("Bartlett");
    _spectrumWindowFunc.add("Hamming");
    _spectrumWindowFunc.add("Hanning");
    _spectrumWindowFunc.select(2);
    _spectrumWindowFunc.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        _spectrum.setWindowFunc(_spectrumWindowFunc
            .getItem(_spectrumWindowFunc.getSelectionIndex()));
      }
    });
    return _spectrumWindowFunc;
  }
  public Combo createSpectrumWindowSize(Composite parent, int style) {
    Label l = new Label(parent, SWT.NONE);
    l.setText("Window:");
    _spectrumWindowSize= new Combo(parent, SWT.READ_ONLY);
    _spectrumWindowSize.add("256");
    _spectrumWindowSize.add("512");
    _spectrumWindowSize.add("1024");
    _spectrumWindowSize.add("2048");
    _spectrumWindowSize.add("4096");
    _spectrumWindowSize.select(2);
    _spectrumWindowSize.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {        
        _spectrum.setWindowSize(Integer.parseInt(_spectrumWindowSize
            .getItem(_spectrumWindowSize.getSelectionIndex())));
      }
    });
    return _spectrumWindowSize;
  }

  public Text createSpectrumFrequency(Composite parent, int style) {
    _spectrumFreq = new Text(parent, style);
    Label l = new Label(parent, SWT.NONE);
    l.setText("Hz");
    if (_spectrum != null) {
      _spectrum.setFrequencyDisplay(_spectrumFreq);
    }
    return _spectrumFreq;
  }
  public Text createSpectrumDb(Composite parent, int style) {
    _spectrumDb= new Text(parent, style);
    Label l = new Label(parent, SWT.NONE);
    l.setText("dB");
    if (_spectrum != null) {
      _spectrum.setDbDisplay(_spectrumDb);
    }
    return _spectrumDb;
  }

	public WaveformDisplay createWaveformDisplay(Composite parent, int style) {
		_waveform = new WaveformDisplay(parent, style);
		return _waveform;
	}

	public Composite createDbMeter(Composite parent, int style) {
		Meter meter = new Meter(parent, style);
		_dbMeter = meter;
		return meter;
	}

	public Slider createGainSlider(Composite parent, int style) {
		_gainSlider = new Slider(parent, style);
		_gainSlider.setMaximum(110);
		_gainSlider.setSelection(50);
		_gainSlider.setMinimum(0);
		_gainSlider.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				System.out.println(_gainSlider.getSelection());
				_gainControl.setLevel((float) _gainSlider.getSelection() / 100);
			}
		});
		_gainSlider.setToolTipText("Gain");
		return _gainSlider;
	}

	public ToolBar createControlBar(Composite parent, int style) {
		_controlBar = new ToolBar(parent, style);

		_tiPlay = new ToolItem(_controlBar, SWT.PUSH);
		_tiPlay.setEnabled(true);
		_tiPlay.setImage(AcmusGraphics.IMG_PLAY);
		_tiPlay.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (_status == PLAYING)
					pause();
				else
					play();
			}
		});
		_tiPlay.setToolTipText("Play/Pause/Resume");

		_tiStop = new ToolItem(_controlBar, SWT.PUSH);
		_tiStop.setImage(AcmusGraphics.IMG_STOP);
		_tiStop.setEnabled(false);
		_tiStop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				stop();
			}
		});
		_tiStop.setToolTipText("Stop");

		return _controlBar;
	}

	public ToolBar createZoomBar(Composite parent, int style) {
		_zoomBar = new ToolBar(parent, style);

		_tiXZoomIn = new ToolItem(_zoomBar, SWT.PUSH);
		_tiXZoomIn.setEnabled(true);
		_tiXZoomIn.setImage(AcmusGraphics.IMG_XZOOMIN);
		_tiXZoomIn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				_waveform.zoomIn();
			}
		});
		_tiXZoomIn.setToolTipText("X-axis zoom in");

		_tiXFit = new ToolItem(_zoomBar, SWT.PUSH);
		_tiXFit.setEnabled(true);
		_tiXFit.setImage(AcmusGraphics.IMG_XZOOMFIT);
		// _tiXFit.setText("Fit");
		_tiXFit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				_waveform.xFit();
			}
		});
		_tiXFit.setToolTipText("Reset X-axis zoom");

		_tiXZoomOut = new ToolItem(_zoomBar, SWT.PUSH);
		_tiXZoomOut.setEnabled(true);
		_tiXZoomOut.setImage(AcmusGraphics.IMG_XZOOMOUT);
    _tiXZoomOut.setText("");
		_tiXZoomOut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				_waveform.zoomOut();
			}
		});
		_tiXZoomOut.setToolTipText("X-axis zoom out");

		new ToolItem(_zoomBar, SWT.SEPARATOR);

		_tiYZoomIn = new ToolItem(_zoomBar, SWT.PUSH);
		_tiYZoomIn.setEnabled(true);
		_tiYZoomIn.setImage(AcmusGraphics.IMG_YZOOMIN);
		_tiYZoomIn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				_waveform.yZoomIn();
			}
		});
		_tiYZoomIn.setToolTipText("Y-axis zoom in");

		_tiYReset = new ToolItem(_zoomBar, SWT.PUSH);
		_tiYReset.setEnabled(true);
		_tiYReset.setImage(AcmusGraphics.IMG_YZOOMFIT);
		// _tiYFit.setText("Fit");
		_tiYReset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				_waveform.yFit();
			}
		});
		_tiYReset.setToolTipText("Reset Y-axis zoom");

		_tiYZoomOut = new ToolItem(_zoomBar, SWT.PUSH);
		_tiYZoomOut.setEnabled(true);
		_tiYZoomOut.setImage(AcmusGraphics.IMG_YZOOMOUT);
		_tiYZoomOut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				_waveform.yZoomOut();
			}
		});
		_tiYZoomOut.setToolTipText("Y-axis zoom out");

		_tiYFit = new ToolItem(_zoomBar, SWT.PUSH);
		_tiYFit.setEnabled(true);
		//_tiYFit.setText("Vertical Fit");
    _tiYFit.setImage(AcmusGraphics.IMG_YZOOMVFIT);
		_tiYFit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				_waveform.yFit2();
			}
		});
		_tiYFit.setToolTipText("Vertical Fit");

    new ToolItem(_zoomBar, SWT.SEPARATOR);

    _tiBackward = new ToolItem(_zoomBar, SWT.PUSH);
    _tiBackward.setEnabled(true);
    _tiBackward.setImage(AcmusGraphics.IMG_BACKWARD);
    _tiBackward.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        _waveform.yZoomOut();
      }
    });
    _tiBackward.setToolTipText("Go back");

    _tiForward= new ToolItem(_zoomBar, SWT.PUSH);
    _tiForward.setEnabled(true);
    _tiForward.setImage(AcmusGraphics.IMG_FORWARD);
    _tiForward.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        _waveform.yZoomOut();
      }
    });
    _tiForward.setToolTipText("Go forward");
    
		return _zoomBar;
	}

	public ToolBar createSelModeBar(Composite parent, int style) {
		_selModeBar = new ToolBar(parent, style);

		_tiZoomMode = new ToolItem(_selModeBar, SWT.RADIO);
		//_tiZoomMode.setText("Zoom");
    _tiZoomMode.setImage(AcmusGraphics.IMG_ZOOM);
		_tiZoomMode.setEnabled(true);
		_tiZoomMode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				_waveform.zoomMode();
			}
		});
		_tiZoomMode.setSelection(true);
		_tiZoomMode.setToolTipText("Zoom tool");

		_tiSelectXMode = new ToolItem(_selModeBar, SWT.RADIO);
		//_tiSelectXMode.setText("Selection");
    _tiSelectXMode.setImage(AcmusGraphics.IMG_SELECTION);
		_tiSelectXMode.setEnabled(true);
		_tiSelectXMode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				_waveform.selectMode();
			}
		});
		_tiSelectXMode.setToolTipText("Selection tool");

		return _selModeBar;
	}

	public Combo createGraphMode(Composite parent, int style) {
		_graphMode = new Combo(parent, SWT.NONE);
		_graphMode.add("Amplitude");
		_graphMode.add("Power");
		// _graphMode.add("Spectrum");
		_graphMode.select(0);
		_graphMode.setToolTipText("Select audio display mode");
		_graphMode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				switch (_graphMode.getSelectionIndex()) {
				case 0:
					_waveform.amplitude();
					break;
				case 1:
					_waveform.power();
					break;
				default:
					_waveform.amplitude();
					break;
				}

			}
		});

		return _graphMode;
	}

	public Button createExplainButton(Composite parent, int style) {
		_bExplain = new Button(parent, SWT.NONE);
		_bExplain.setText("Explain");
		_bExplain.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				explain();
			}
		});
		return _bExplain;
	}

	public Text createPositionDisplay(Composite parent, int style) {
		_tPosition = new Text(parent, SWT.BORDER | style);
		_tPosition.setToolTipText("Cursor position in milliseconds");
		_tPosition.setText("");
    if (_spectrum != null) {
      _spectrum.setPositionDisplay(_tPosition);
    }
		return _tPosition;
	}

	public Text createSelectionStartDisplay(Composite parent, int style) {
		_tSelStart = new Text(parent, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		_tSelStart.setLayoutData(gridData);
		return _tSelStart;
	}

	public Text createSelectionEndDisplay(Composite parent, int style) {
		_tSelEnd = new Text(parent, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		_tSelEnd.setLayoutData(gridData);
		return _tSelEnd;
	}

	public MouseMoveListener createPositionDisplayListener() {
		return new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				_tPosition.setText("" + _waveform.positionInMillis(e.x));
			}
		};
	}

  protected Text getPositionDisplay() {
    return _tPosition;
  }
  
	public SelectionListener createSelectionStartDisplayListener() {
		return new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			public void widgetSelected(SelectionEvent e) {
				_tSelStart.setText("" + _waveform.getSelectionStartInMillis());
			}
		};
	}

	public SelectionListener createSelectionEndDisplayListener() {
		return new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			public void widgetSelected(SelectionEvent e) {
				_tSelEnd.setText("" + _waveform.getSelectionEndInMillis());
			}
		};
	}

	public void explain() {
		Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
		Shell shell = new Shell(d);

		shell.setText("Explain");

		shell.setLayout(new GridLayout(1, false));
		GridData gridData;

		Text t = new Text(shell, SWT.BORDER | SWT.MULTI);
		gridData = new GridData(GridData.FILL_BOTH);
		t.setLayoutData(gridData);
		t.setEditable(false);

		shell.pack();
		shell.open();
	}

	public static int[] parseData(byte[] audioBytes, AudioFormat format) {

		int[] audioData = null;
		if (format.getSampleSizeInBits() == 32) {
			int nlengthInSamples = audioBytes.length / 4;
			audioData = new int[nlengthInSamples];
			if (format.isBigEndian()) {
				for (int i = 0; i < nlengthInSamples; i++) {
					/* First byte is MSB (high order) */
					int MSB = (int) audioBytes[4 * i];
					int B2 = (int) audioBytes[4 * i + 1];
					int B3 = (int) audioBytes[4 * i + 2];
					/* Last byte is LSB (low order) */
					int LSB = (int) audioBytes[4 * i + 3];
					audioData[i] = MSB << 24 | B2 << 16 | B3 << 8 | (255 & LSB);
				}
			} else {
				for (int i = 0; i < nlengthInSamples; i++) {
					/* First byte is LSB (low order) */
					int LSB = (int) audioBytes[4 * i];
					int B3 = (int) audioBytes[4 * i + 1];
					int B2 = (int) audioBytes[4 * i + 2];
					/* Last byte is MSB (high order) */
					int MSB = (int) audioBytes[4 * i + 3];
					audioData[i] = (255&MSB) << 24 | (255&B2) << 16 | (255&B3) << 8 | (255 & LSB);
//					System.out.println(MSB + " " + B2 + " " + B3 + " " + LSB);
//					System.out.println("laudioData " + i + " " + audioData[i]);
				}
			}
		} else if (format.getSampleSizeInBits() == 16) {
			int nlengthInSamples = audioBytes.length / 2;
			audioData = new int[nlengthInSamples];
			if (format.isBigEndian()) {
				for (int i = 0; i < nlengthInSamples; i++) {
					/* First byte is MSB (high order) */
					int MSB = (int) audioBytes[2 * i];
					/* Second byte is LSB (low order) */
					int LSB = (int) audioBytes[2 * i + 1];
					audioData[i] = MSB << 8 | (255 & LSB);
				}
			} else {
				for (int i = 0; i < nlengthInSamples; i++) {
					/* First byte is LSB (low order) */
					int LSB = (int) audioBytes[2 * i];
					/* Second byte is MSB (high order) */
					int MSB = (int) audioBytes[2 * i + 1];
					audioData[i] = MSB << 8 | (255 & LSB);
				}
			}
		} else if (format.getSampleSizeInBits() == 8) {
			int nlengthInSamples = audioBytes.length;
			audioData = new int[nlengthInSamples];
			if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
				for (int i = 0; i < audioBytes.length; i++) {
					audioData[i] = audioBytes[i];
				}
			} else {
				for (int i = 0; i < audioBytes.length; i++) {
					audioData[i] = audioBytes[i] - 128;
				}
			}
		}
		return audioData;
	}

	public static int[] readData(AudioInputStream ais) {

		AudioFormat format = ais.getFormat();
		byte[] audioBytes = new byte[(int) (ais.getFrameLength() * format
				.getFrameSize())];

		try {
			ais.read(audioBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return parseData(audioBytes, format);
	}

	public static final double[] normalizeInPlace(double res[], int[] data, int bits) {
		// double[]res = new double[data.length];
		int max = (1 << bits-1) -1;
		for (int i = 0; i < res.length; i++) {
			res[i] = (double) data[i] / max;
		}
		return res;
	}

	public int[] readData2(AudioInputStream ais) {

		AudioFormat format = ais.getFormat();
		_audioBytes = new byte[(int) (ais.getFrameLength() * format.getFrameSize())];

		try {
			ais.read(_audioBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return parseData(_audioBytes, format);
	}
  
  public int[] getData() {
    return _audioData;
  }
  
  public void dispose() {
    System.out.println("Closing...");
    _jso.dispose();
    _sdl.close();
    _player.deallocate();
    try {
      
    _audioStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

class NullDbMeter implements IMeter {
	static NullDbMeter _singleton = new NullDbMeter();

	public static NullDbMeter getInstance() {
		return _singleton;
	}

	public NullDbMeter() {
	}

	public final void reset() {
	}

	public final void resetPeak() {
	}

	public final void setData(int[] data, int channels, int b, int bitsPerSample) {
	}

	public final void show(int x) {
	}

	public void showLast() {
	}
}