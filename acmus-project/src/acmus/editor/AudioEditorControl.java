/**
 * Created on Jul 9, 2006
 */
package acmus.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import acmus.audio.AudioPlayer;
import acmus.audio.SpectrumDisplay;
import acmus.audio.WaveformDisplay;
import acmus.dsp.Filter;
import acmus.dsp.FilterBank;

/**
 * @author lku
 * 
 */
public class AudioEditorControl extends Composite {

	AudioPlayer _ap;

	Combo _graphMode;
	ToolBar _zoomBar, _selectionBar;
	Text _tSelStart;
	Text _tSelEnd;
	Text _tPosition;

	Combo cFilters;
	String filterList[] = { "62.5", "125", "250", "500", "1000", "2000",
			"4000", "8000" };

	Composite _graphArea;
	WaveformDisplay _wf;
	SpectrumDisplay _spectrum;
	Composite _waveformComposite;
	Composite _spectrumComposite;

	// We probably don't need these:

	Composite _dbMeter;
	Composite _spectrumWindowFunc;
	Composite _saveBar;

	String _dir;
	String _file;

	public AudioEditorControl(Composite parent, int style, boolean barOnTop,
			boolean selection, boolean saveBar) {
		this(parent, style, "/tmp", "temp.wav", barOnTop, selection, saveBar);
	}

	public AudioEditorControl(Composite parent, int style, String dir,
			String file, boolean barOnTop, boolean selection, boolean saveBar) {
		super(parent, style);

		GridLayout gl;

		_dir = dir;
		_file = file;

		_ap = new AudioPlayer();

		gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		setLayout(gl);

		if (barOnTop) {
			createBar(selection);
			createGraphArea();
		} else {
			createGraphArea();
			createBar(selection);
		}
		createDisplays();
		if (saveBar) {
			createSaveBar();
		}
	}

	private void createGraphArea() {
		_graphArea = new Composite(this, SWT.NONE);

		GridData gridData;
		GridLayout gridLayout;

		gridData = new GridData(GridData.FILL_BOTH);
		_graphArea.setLayoutData(gridData);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		_graphArea.setLayout(gridLayout);
	}

	private void createDisplays() {
		GridData gridData;
		GridLayout gridLayout;

		_waveformComposite = new Composite(_graphArea, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		_waveformComposite.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_BOTH);
		_waveformComposite.setLayoutData(gridData);

		_wf = _ap.createWaveformDisplay(_waveformComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL_BOTH);
		_wf.setLayoutData(gridData);
		_wf.setPositionDisplayListener(_ap.createPositionDisplayListener());
		_wf.setSelectionStartDisplayListener(_ap
				.createSelectionStartDisplayListener());
		_wf.setSelectionEndDisplayListener(_ap
				.createSelectionEndDisplayListener());

		_dbMeter = _ap.createDbMeter(_waveformComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL_VERTICAL);
		gridData.widthHint = 18;
		_dbMeter.setLayoutData(gridData);

		_spectrumComposite = new Composite(_graphArea, SWT.NONE);
		gridLayout = new GridLayout(8, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 2;
		_spectrumComposite.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_BOTH);
		_spectrumComposite.setLayoutData(gridData);
		gridData.exclude = true;
		_spectrumComposite.setVisible(false);

		_graphMode.add("Spectrum");
		_graphMode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				switch (_graphMode.getSelectionIndex()) {
				case 0:
					((GridData) _spectrumComposite.getLayoutData()).exclude = true;
					_spectrumComposite.setVisible(false);
					((GridData) _waveformComposite.getLayoutData()).exclude = false;
					_waveformComposite.setVisible(true);
					_graphArea.layout();
					// _waveform.amplitude(); <-- already called by default
					// listener
					setZoomSelectionEnabled(true);
					break;
				case 1:
					((GridData) _spectrumComposite.getLayoutData()).exclude = true;
					_spectrumComposite.setVisible(false);
					((GridData) _waveformComposite.getLayoutData()).exclude = false;
					_waveformComposite.setVisible(true);
					_graphArea.layout();
					// _waveform.power(); <-- already called by default listener
					setZoomSelectionEnabled(true);
					break;
				case 2:
					if (_spectrum == null) {
						createSpectrum();
					}
					_spectrumComposite.setVisible(true);
					((GridData) _spectrumComposite.getLayoutData()).exclude = false;
					((GridData) _waveformComposite.getLayoutData()).exclude = true;
					_waveformComposite.setVisible(false);
					_graphArea.layout();
					setZoomSelectionEnabled(false);
					break;
				}
			}
		});
	}

	private void createSpectrum() {
		GridData gridData;
		_spectrum = _ap.createSpectrumDisplay(_spectrumComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 8;
		_spectrum.setLayoutData(gridData);
		Text t = _ap.createSpectrumFrequency(_spectrumComposite, SWT.BORDER
				| SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 40;
		t.setLayoutData(gridData);
		t = _ap
				.createSpectrumDb(_spectrumComposite, SWT.BORDER
						| SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 20;
		t.setLayoutData(gridData);
		_spectrumWindowFunc = _ap.createSpectrumWindowFunc(_spectrumComposite,
				SWT.NONE);
		_ap.createSpectrumWindowSize(_spectrumComposite, SWT.NONE);
	}

	private void setZoomSelectionEnabled(boolean enabled) {
		_zoomBar.setEnabled(enabled);
		if (_selectionBar != null) {
			_selectionBar.setEnabled(enabled);
			_tSelStart.setEnabled(enabled);
			_tSelEnd.setEnabled(enabled);
			// _tPosition.setEnabled(enabled);
		}
	}

	private void createBar(boolean selection) {
		GridData gridData;
		Label l;

		int cols = 6;
		if (selection)
			cols = 10;

		Composite bar = new Composite(this, SWT.NONE);
		GridLayout gridLayout = new GridLayout(cols, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		bar.setLayout(gridLayout);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		bar.setLayoutData(gridData);

		_ap.createControlBar(bar, SWT.NONE);
		_zoomBar = _ap.createZoomBar(bar, SWT.NONE);
		if (selection) {
			_selectionBar = _ap.createSelModeBar(bar, SWT.NONE);
		}

		_graphMode = _ap.createGraphMode(bar, SWT.NONE);

		_tPosition = _ap.createPositionDisplay(bar, SWT.NONE);
		_tPosition.setEditable(false);
		gridData = new GridData();
		gridData.widthHint = 50;
		_tPosition.setLayoutData(gridData);
		l = new Label(bar, SWT.NONE);
		l.setText("ms");

		if (selection) {
			_tSelStart = _ap.createSelectionStartDisplay(bar, SWT.NONE);
			_tSelStart.setEditable(true);
			_tSelStart
					.setToolTipText("Selection start position in milliseconds");
			gridData = new GridData();
			gridData.widthHint = 50;
			_tSelStart.setLayoutData(gridData);
			l = new Label(bar, SWT.NONE);
			l.setText("ms");

			_tSelEnd = _ap.createSelectionEndDisplay(bar, SWT.NONE);
			_tSelEnd.setEditable(true);
			_tSelEnd.setToolTipText("Selection end position in milliseconds");
			gridData = new GridData();
			gridData.widthHint = 50;
			_tSelEnd.setLayoutData(gridData);
			l = new Label(bar, SWT.NONE);
			l.setText("ms");

			_tSelStart.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
					System.out.println(e.keyCode);
					if (e.keyCode == SWT.CR) {
						_wf.setSelectionStartInMillis(Integer
								.parseInt(_tSelStart.getText()));
					}
				}
			});

			_tSelEnd.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
					System.out.println(e.keyCode);
					if (e.keyCode == SWT.CR) {
						_wf.setSelectionEndInMillis(Integer.parseInt(_tSelEnd
								.getText()));
					}
				}
			});
		}
	}

	public void createSaveBar() {
		_saveBar = new Composite(this, SWT.NONE);
		GridLayout gridLayout = new GridLayout(5, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		_saveBar.setLayout(gridLayout);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		_saveBar.setLayoutData(gridData);

		cFilters = new Combo(_saveBar, SWT.READ_ONLY);
		cFilters.setItems(filterList);
		cFilters.select(0);

		Button bFilter = new Button(_saveBar, SWT.NONE);
		bFilter.setText("Filter");

		bFilter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Filter f = FilterBank.get1d8(Double
						.parseDouble(filterList[cFilters.getSelectionIndex()]),
						_wf.getSampleRate());
				_wf.filter(f);
				_wf.save(_dir + System.getProperty("file.separator")
						+ "temp.wav");
				_ap.open(_dir + System.getProperty("file.separator")
						+ "temp.wav");
			}
		});

		Composite blank = new Composite(_saveBar, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 1;
		blank.setLayoutData(gridData);

		Button cut = new Button(_saveBar, SWT.NONE);
		cut.setText("Cut");

		cut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (selectionValid()) {
					_wf.cut(getSelectionStartInSamples(),
							getSelectionEndInSamples());
					_wf.save(_dir + System.getProperty("file.separator")
							+ "temp.wav");
					_ap.open(_dir + System.getProperty("file.separator")
							+ "temp.wav");
					eraseSelection();
				}
			}
		});

		Button save = new Button(_saveBar, SWT.NONE);
		save.setText("Save");

		save.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				_wf.save(_dir + System.getProperty("file.separator") + _file);
				_ap.open(_dir + System.getProperty("file.separator") + _file);
			}
		});

		// Button saveAs= new Button(_saveBar, SWT.NONE);
		// saveAs.setText("Save As");
	}

	public void open(String filename) {
		_ap.open(filename);
	}

	public int[] getData() {
		return _ap.getData();
	}

	public int getBitsPerSample() {
		return _ap.getBitsPerSample();
	}

	public int getSelectionStartInSamples() {
		return _wf.getSelectionStartInSamples();
	}

	public int getSelectionStartInMillis() {
		return _wf.getSelectionStartInMillis();
	}

	public int getSelectionEndInSamples() {
		return _wf.getSelectionEndInSamples();
	}

	public int getSelectionEndInMillis() {
		return _wf.getSelectionEndInMillis();
	}

	public void eraseSelection() {
		_wf.eraseSelection();
	}

	public void redrawWf() {
		_wf.redrawWf();
	}

	public boolean selectionValid() {
		return _wf.selectionValid();
	}

	public int getTotalDurationInMillis() {
		return _ap.getTotalDurationInMillis();
	}

	public void play() {
		_ap.play();
	}

	public void waitStop() {
		_ap.waitStop();
	}

}
