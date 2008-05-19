/*
 *  MeasurementEditor.java
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
 * Created on 04/03/2005
 */
package acmus.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;
import acmus.MeasurementProject;
import acmus.audio.AudioDevice;
import acmus.dsp.Parameters;
import acmus.dsp.Plot;
import acmus.dsp.Util;
import acmus.editor.view.ImpulseResponsePage;
import acmus.wizard.AcmusMeasurementWizard;
import acmus.wizard.AcmusMeasurementWizardFirstPage;

/**
 * @author lku
 */
public class MeasurementEditor extends MultiPageEditorPart {

	FileEditorInput _input;

	int _audioIndex = -1;
	int _parametersIndex = -1;
	int _propertiesIndex = -1;

	MeasurementPropertiesEditor _propertiesEditor;

	Combo _cSource;
	Text _tSource;

	private AudioEditorControl _aeSource;
	private AudioEditorControl _aeRec;
	private AudioEditorControl _aeRecLf;


	TabFolder _tfRecordings;
	TabItem _tiRec;
	TabItem _tiRecLf;

	Object _playLock = new Object();
	boolean _playing = false;

	Button _bRecord;
	Button _bRecordLf;
	Button _bIr;
	Text _tTakes;

	IFolder _sigFolder;
	private IFile _signalFile;
	IFolder _sigAudioFolder;
	IFile _signalAudioFile;
	private IFolder _outFolder;
	IFile _recFile;
	private IFile _recFileLf;
	private IFile _irFile;
	private IFile _irFileLf;
	private IFile _paramsFile;
	private IFolder _schroederFolder;
	IFile _propsFile;

	Properties _props;

	String _method = "";

	AudioFormat _format;

	private ProgressMonitorPart _monitor;

	MessageBox _warningDialog;

	boolean _RecPage;

	private ImpulseResponsePage impulseResponsePage;

	// -------------------------------------------------------------------------

	@Override
	public Composite getContainer() {
		return super.getContainer();
	}
	@Override
	public void setPageText(int pageIndex, String text) {
		super.setPageText(pageIndex, text);
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		_input = (FileEditorInput) input;
		setOutFolder((IFolder) _input.getFile().getParent());

		_recFile = getOutFolder().getFile("recording.wav");
		setRecFileLf(getOutFolder().getFile("recording2.wav"));
		IProject p = _input.getFile().getProject();
		_sigFolder = p.getFolder("_signals.signal");
		_sigAudioFolder = _sigFolder.getFolder("audio");
		setParamsFile(getOutFolder().getFile("parameters.txt"));
		setSchroederFolder(getOutFolder().getFolder("schroeder"));
		setIrFile(getOutFolder().getFile("ir.wav"));
		setIrFileLf(getOutFolder().getFile("ir2.wav"));

		_RecPage = Boolean.parseBoolean(MeasurementProject.getProperty(_input.getFile(),
				"recording", "true"));

		setPartName(MeasurementProject.removeSuffix(_input.getFile()
				.getParent().getName()));

	}

	// -------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
	 */
	protected void createPages() {

		createPropertiesPage();

		if (_RecPage) {
			createAudioPage();

			if (_recFile.exists()) {
				try {
					_aeRec.open(_recFile.getLocation().toOSString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				_bIr.setEnabled(("sweep".equals(_method) || "mls"
						.equals(_method)));
			}
			if (getRecFileLf().exists()) {
				try {
					_aeRecLf.open(getRecFileLf().getLocation().toOSString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				_bIr.setEnabled(("sweep".equals(_method) || "mls"
						.equals(_method)));
			}
			this.setActivePage(_audioIndex);
		}

		if (getIrFile().exists()) {
			impulseResponsePage = newImpulseResponsePage();
			this.setActivePage(impulseResponsePage.getIndex());
		}
		if (getParamsFile().exists() && getSchroederFolder().exists()) {
			createParametersPage();
			this.setActivePage(_parametersIndex);
		}

	}
	private ImpulseResponsePage newImpulseResponsePage() {
		return new ImpulseResponsePage(this, SWT.NONE);
	}

	private boolean irPageCreated() {
		return impulseResponsePage != null;
	}

	public boolean parametersPageCreated() {
		return _parametersIndex >= 0;
	}

	private void removeIrPage() {
		if (irPageCreated()) {
			removePage(impulseResponsePage.getIndex());
			impulseResponsePage = null;
			_parametersIndex--;
		}
	}

	public void removeParametersPage() {
		if (parametersPageCreated()) {
			removePage(_parametersIndex);
			_parametersIndex = -1;
		}
	}

	// ----------------------------------------------------------------------------

	public void createPropertiesPage() {

		try {
			_propertiesEditor = new MeasurementPropertiesEditor();
			_propertiesIndex = addPage(_propertiesEditor, getEditorInput());
			setPageText(_propertiesIndex, "Properties");

			_props = _propertiesEditor.getMeasurementProperties();
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createAudioPage() {

		Composite page = new Composite(getContainer(), SWT.NONE);
		page.setLayout(new GridLayout(2, false));

		GridData gridData;
		Label l;

		l = new Label(page, SWT.LEFT);
		l.setText("Input Signal: ");
		_cSource = new Combo(page, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_cSource.setLayoutData(gridData);
		_cSource.setItems(getSignals());
		_cSource.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				try {
					Combo c = (Combo) e.widget;
					loadSignal(c.getItem(c.getSelectionIndex()));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		_aeSource = new AudioEditorControl(page, SWT.NONE, true, false, false);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		_aeSource.setLayoutData(gridData);

		_tfRecordings = new TabFolder(page, SWT.TOP);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		_tfRecordings.setLayoutData(gridData);
		_tiRec = new TabItem(_tfRecordings, SWT.NONE);
		_tiRec.setText("Recording");
		_tiRecLf = new TabItem(_tfRecordings, SWT.NONE);
		_tiRecLf.setText("Recording 2");

		Composite c = new Composite(_tfRecordings, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		c.setLayout(gridLayout);

		_aeRec = new AudioEditorControl(c, SWT.NONE, false, false, false);
		gridData = new GridData(GridData.FILL_BOTH);
		_aeRec.setLayoutData(gridData);

		Composite c2 = new Composite(c, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		c2.setLayout(gl);

		_bRecord = new Button(c2, SWT.CENTER);
		_bRecord.setImage(AcmusGraphics.IMG_RECORD);
		_bRecord.setToolTipText("Record");
		l = new Label(c2, SWT.LEFT);
		l.setText("takes:");
		_tTakes = new Text(c2, SWT.BORDER);
		_tTakes.setText("1");

		_tiRec.setControl(c);

		c = new Composite(_tfRecordings, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		c.setLayout(gridLayout);

		_aeRecLf = new AudioEditorControl(c, SWT.NONE, false, false, false);
		gridData = new GridData(GridData.FILL_BOTH);
		_aeRecLf.setLayoutData(gridData);

		_bRecordLf = new Button(c, SWT.CENTER);
		_bRecordLf.setImage(AcmusGraphics.IMG_RECORD);
		_bRecordLf.setToolTipText("Record");

		_tiRecLf.setControl(c);

		_bIr = new Button(page, SWT.CENTER);
		_bIr.setText("Calculate IR");
		_bIr.setEnabled(false);

		setMonitor(new ProgressMonitorPart(page, null));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		getMonitor().setLayoutData(gridData);

		_warningDialog = new MessageBox(this.getContainer().getShell(), SWT.OK
				| SWT.CANCEL);
		_warningDialog
				.setMessage("This operation will overwrite the current recording and erase the IR and parameters data.");
		_warningDialog.setText("Warning");

		_bRecord.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {

				if (getIrFile().exists() || getParamsFile().exists()) {
					int warning = _warningDialog.open();
					if (warning == SWT.CANCEL)
						return;
					try {
						if (getIrFile().exists())
							getIrFile().delete(true, null);
						if (getParamsFile().exists())
							getParamsFile().delete(true, null);
						if (getSchroederFolder().exists())
							getSchroederFolder().delete(true, null);
						getOutFolder().refreshLocal(IFolder.DEPTH_ONE, null);
						removeIrPage();
						removeParametersPage();
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
				int takes = 1;
				try {
					takes = Integer.parseInt(_tTakes.getText());
					if (takes < 1)
						takes = 1;
				} catch (NumberFormatException e) {
					System.err.println("warning: invalid number of takes: "
							+ _tTakes.getText());
				}
				getMonitor().beginTask("Recording", takes);
				getMonitor().subTask("Take 1");
				record(_recFile.getLocation().toOSString());
				try {
					getOutFolder().refreshLocal(IFolder.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				loadRecording();
				getMonitor().worked(1);
				IFolder setFolder = (IFolder) getOutFolder().getParent();
				for (int i = 2; i <= takes; i++) {
					getMonitor().subTask("Take " + i);

					String name = AcmusMeasurementWizardFirstPage
							.generateNameFor(setFolder, MeasurementProject
									.removeSuffix(setFolder.getName()));
					Properties props = new Properties();
					props.setProperty("Name", name);
					props.setProperty("Signal", getSignalFile().getName());
					props.setProperty("Method", _method);
					IFolder takeFolder = AcmusMeasurementWizard
							.createMeasurement(setFolder, props);
					record(takeFolder.getFile(_recFile.getName()).getLocation()
							.toOSString());
					try {
						takeFolder.refreshLocal(IFolder.DEPTH_INFINITE, null);
					} catch (CoreException e) {
						e.printStackTrace();
					}
					getMonitor().worked(1);
				}
				getMonitor().done();
			}
		});

		_bRecordLf.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {

				getMonitor().beginTask("Recording LF", 1);
				record(getRecFileLf().getLocation().toOSString());
				try {
					getOutFolder().refreshLocal(IFolder.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				loadRecordingLf();
				getMonitor().worked(1);
				getMonitor().done();
			}
		});

		_bIr.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (!irPageCreated())
					impulseResponsePage = newImpulseResponsePage();
				impulseResponsePage.calculateIr();
				impulseResponsePage.calculateIrLf();
			}
		});

		_method = _props.getProperty("Method");

		if (_props.containsKey("Signal")) {
			setSignalFile(_sigFolder.getFile(_props.getProperty("Signal")));
			for (int i = 0; i < _cSource.getItemCount(); i++) {
				if (_cSource.getItem(i).equals(_props.getProperty("Signal"))) {
					_cSource.select(i);
					loadSignal(_cSource.getItem(_cSource.getSelectionIndex()));
					break;
				}
			}
		}

		_audioIndex = addPage(page);
		setPageText(_audioIndex, "Recording");
	}

	Shell _plotShells[] = new Shell[11];
	ParametersHolder _parameters[] = new ParametersHolder[11];

	public void openPlot(int index) {
		try {
			ParametersHolder ph = _parameters[index];

			// read data
			if (ph == null) {
				_parameters[index] = new ParametersHolder();
				ph = _parameters[index];

				String separator = System.getProperty("file.separator", "/");
				BufferedReader br = new BufferedReader(new FileReader(
						getSchroederFolder().getLocation().toOSString() + separator
								+ Parameters.CHANNEL_NAMES[index] + ".txt"));
				String line = br.readLine();
				int n = Integer.parseInt(line);

				double xs[] = new double[n];
				double ys[] = new double[n];

				line = br.readLine();

				StringTokenizer st = new StringTokenizer(line);
				for (int i = 0; i < xs.length; i++) {
					xs[i] = Double.parseDouble(st.nextToken());
				}
				line = br.readLine();
				st = new StringTokenizer(line);
				for (int i = 0; i < xs.length; i++) {
					ys[i] = Double.parseDouble(st.nextToken());
				}

				ph.schroederX = xs;
				ph.schroederY = ys;

				ph.edt = Double.parseDouble(br.readLine());

				line = br.readLine();
				if (!line.equals("?")) {
					st = new StringTokenizer(line);
					ph.a10 = Double.parseDouble(st.nextToken());
					ph.b10 = Double.parseDouble(st.nextToken());
					ph.t10 = Double.parseDouble(st.nextToken());
				}

				line = br.readLine();
				if (!line.equals("?")) {
					st = new StringTokenizer(line);
					ph.a20 = Double.parseDouble(st.nextToken());
					ph.b20 = Double.parseDouble(st.nextToken());
					ph.t20 = Double.parseDouble(st.nextToken());
				}

				line = br.readLine();
				if (!line.equals("?")) {
					st = new StringTokenizer(line);
					ph.a30 = Double.parseDouble(st.nextToken());
					ph.b30 = Double.parseDouble(st.nextToken());
					ph.t30 = Double.parseDouble(st.nextToken());
				}

				line = br.readLine();
				if (!line.equals("?")) {
					st = new StringTokenizer(line);
					ph.a40 = Double.parseDouble(st.nextToken());
					ph.b40 = Double.parseDouble(st.nextToken());
					ph.t40 = Double.parseDouble(st.nextToken());
				}
			} // read data

			// open shell
			if (_plotShells[index] == null || _plotShells[index].isDisposed()) {
				Display d = AcmusPlugin.getDefault().getWorkbench()
						.getDisplay();
				_plotShells[index] = new Shell(d);
				_plotShells[index].setLayout(new GridLayout(1, false));
				Plot plot = new Plot(_plotShells[index], SWT.NONE,
						"Approximated Decay Times - " + columnNames[index + 1],
						"time (s)", "dB");
				GridData gridData = new GridData(GridData.FILL_BOTH);
				plot.setLayoutData(gridData);

				plot.line(ph.schroederX, ph.schroederY, AcmusGraphics.BLUE,
						"Schroeder curve");

				// line([0,(-60-A10)/(B10)],[A10,-60],'Color','m','LineWidth',.5);
				// line([0,(-60-A20)/(B20)],[A20,-60],'Color','r','LineWidth',.5);
				// line([0,(-60-A30)/(B30)],[A30,-60],'Color','g','LineWidth',.5);
				// if nargout == 4
				// line([0,(-60-A40)/(B40)],[A40,-60],'Color','y','LineWidth',.5);
				// end
				// line([xlimit(1),xlimit(2)],[-60,-60],'Color',[.4,.4,.4],'LineWidth',.5);

				DecimalFormat f = new DecimalFormat("#.###");
				if (ph.a10 != Double.NaN) {
					double[] x = new double[2];
					double[] y = new double[2];
					x[0] = 0;
					x[1] = (-60 - ph.a10) / ph.b10;
					y[0] = ph.a10;
					y[1] = -60;
					plot.line(x, y, AcmusGraphics.MAGENTA, "EDT (ms) = "
							+ f.format(ph.edt));
				}

				if (ph.a20 != Double.NaN) {
					double[] x = new double[2];
					double[] y = new double[2];
					x[0] = 0;
					x[1] = (-60 - ph.a20) / ph.b20;
					y[0] = ph.a20;
					y[1] = -60;
					plot.line(x, y, AcmusGraphics.RED, "T20 (ms) = "
							+ f.format(ph.t20));
				}

				if (ph.a30 != Double.NaN) {
					double[] x = new double[2];
					double[] y = new double[2];
					x[0] = 0;
					x[1] = (-60 - ph.a30) / ph.b30;
					y[0] = ph.a30;
					y[1] = -60;
					plot.line(x, y, AcmusGraphics.GREEN, "T30 (ms) = "
							+ f.format(ph.t30));
				}

				if (ph.a40 != Double.NaN) {
					double[] x = new double[2];
					double[] y = new double[2];
					x[0] = 0;
					x[1] = (-60 - ph.a40) / ph.b40;
					y[0] = ph.a40;
					y[1] = -60;
					plot.line(x, y, AcmusGraphics.YELLOW, "T40 (ms) = "
							+ f.format(ph.t40));
				}

				{
					plot.yLimit(-70, 0);
					double tmp[] = new double[3];
					tmp[0] = ph.t20;
					tmp[1] = ph.t30;
					tmp[2] = ph.t40;
					double xMax = Util.max(tmp);
					plot.xLimit(0, xMax * 1.1);
					double[] x = new double[2];
					double[] y = new double[2];
					x[0] = 0;
					x[1] = xMax * 1.1;
					y[0] = -60;
					y[1] = -60;
					plot.line(x, y, AcmusGraphics.GRAY, "");
				}

				_plotShells[index]
						.setText("Decay curve - "
								+ columnNames[index + 1]
								+ " - "
								+ MeasurementProject.removeSuffix(getOutFolder()
										.getName()));
				_plotShells[index].setSize(400, 300);
			}
			_plotShells[index].open();

			ph = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createParametersPage() {
		try {
			if (!getParamsFile().exists() || !getSchroederFolder().exists()) {
				impulseResponsePage.calculateParameters(this);
			}

			Composite page = new Composite(getContainer(), SWT.NONE);
			_parametersIndex = addPage(page);
			setPageText(_parametersIndex, "Parameters");
			page.setLayout(new GridLayout(1, false));

			// GridData gridData;

			createTableViewer(page);

			Label lBr = new Label(page, SWT.NONE);
			Label lTr = new Label(page, SWT.NONE);
			Label lItgd = new Label(page, SWT.NONE);

			BufferedReader br = new BufferedReader(new FileReader(getParamsFile()
					.getLocation().toOSString()));
			String line = null;

			line = br.readLine();
			line = br.readLine();
			while (!line.trim().equals("")) {
				StringTokenizer st = new StringTokenizer(line);
				String name = st.nextToken() + " " + st.nextToken();
				double[] ch = new double[11];
				for (int i = 0; i < ch.length; i++) {
					String s = st.nextToken();
					if (s.equals("?")) {
						ch[i] = Double.NaN;
					} else {
						ch[i] = Double.parseDouble(s);
					}
				}
				fTPos.add(new Parameter(name, ch));
				line = br.readLine();
			}

			while (line.trim().equals(""))
				line = br.readLine();

			lBr.setText(line);
			lTr.setText(br.readLine());
			lItgd.setText(br.readLine() + " ms");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -------------------------------------------------------------------------

	public String[] getSignals() {
		File f = new File(_sigFolder.getLocation().toOSString());
		return f.list(new FilenameFilter() {
			public boolean accept(File f, String name) {
				return name.endsWith(".signal");
			}
		});
	}

	private final void loadSignal(String signal) {
		try {
			setSignalFile(_sigFolder.getFile(signal));
			Properties props = new Properties();
			props.load(getSignalFile().getContents());
			String name = props.getProperty("Name");
			_method = props.getProperty("Type", "");

			_signalAudioFile = _sigAudioFolder.getFile(name + ".wav");
			AudioInputStream ais = AudioSystem
					.getAudioInputStream(_signalAudioFile.getContents());
			_format = ais.getFormat();
			_format = new AudioFormat(_format.getEncoding(), _format
					.getSampleRate(), _format.getSampleSizeInBits(), 1, _format
					.getFrameSize(), _format.getFrameRate(), _format
					.isBigEndian());
			_aeSource.open(_signalAudioFile.getLocation().toOSString());
			doSave(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final void record(String outFile) {
		System.out.println("rec " + outFile);

		try {

			try {

				AudioDevice recorder = AcmusPlugin.getDefault().audioDevice;

				int t = (int) Math.round(1000 * Double
						.parseDouble(MeasurementProject.getProperty(_recFile
								.getProject(), "RecordingExtraTime", ""
								+ Util.DEFAULT_REC_EXTRA)));
				recorder.record(new File(outFile), _aeSource
						.getTotalDurationInMillis()
						+ t);


				_aeSource.play();

				_aeSource.waitStop();

				Thread.sleep(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
			doSave(null);
			Thread.sleep(300);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private final void loadRecording() {
		try {
			_aeRec.open(_recFile.getLocation().toOSString());
			_bIr.setEnabled((_method.equals("sweep") || _method.equals("mls")));
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	private final void loadRecordingLf() {
		try {
			_aeRecLf.open(getRecFileLf().getLocation().toOSString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		if (_method != null) {
			_props.setProperty("Method", _method);
			_props.setProperty("Signal", getSignalFile().getName());
		}
		_propertiesEditor.doSave(monitor);
		// firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/* ========================================================================= */

	private Table table;
	private TableViewer fTPos;

	class TColListener extends SelectionAdapter {
		int _index;

		public TColListener(int i) {
			_index = i;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			openPlot(_index);

		}
	}

	// Set column names
	private String[] columnNames = new String[] { "freq [Hz]", "63", "125",
			"250", "500", "1000", "2000", "4000", "8000", "A", "C", "Linear" };

	private void createTableViewer(Composite parent) {
		table = new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gridData);

		fTPos = new TableViewer(table);
		fTPos.setLabelProvider(new PositionLabelProvider());
		fTPos.setColumnProperties(columnNames);

		{
			TableColumn tc = new TableColumn(table, SWT.LEFT);
			tc.setResizable(true);
			tc.setWidth(70);
			tc.setText(columnNames[0]);
		}
		for (int i = 1; i < columnNames.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.LEFT);
			tc.setResizable(true);
			tc.setWidth(50);
			tc.setText(columnNames[i]);
			tc.addSelectionListener(new TColListener(i - 1));
		}


	}

	public void setIrFileLf(IFile _irFileLf) {
		this._irFileLf = _irFileLf;
	}

	public IFile getIrFileLf() {
		return _irFileLf;
	}

	public void setIrFile(IFile _irFile) {
		this._irFile = _irFile;
	}

	public IFile getIrFile() {
		return _irFile;
	}

	public void setOutFolder(IFolder _outFolder) {
		this._outFolder = _outFolder;
	}

	public IFolder getOutFolder() {
		return _outFolder;
	}

	public void setSignalFile(IFile _signalFile) {
		this._signalFile = _signalFile;
	}
	public IFile getSignalFile() {
		return _signalFile;
	}

	public void setRecFileLf(IFile _recFileLf) {
		this._recFileLf = _recFileLf;
	}
	public IFile getRecFileLf() {
		return _recFileLf;
	}

	public void setMonitor(ProgressMonitorPart _monitor) {
		this._monitor = _monitor;
	}
	public ProgressMonitorPart getMonitor() {
		return _monitor;
	}

	public void setParamsFile(IFile _paramsFile) {
		this._paramsFile = _paramsFile;
	}
	public IFile getParamsFile() {
		return _paramsFile;
	}

	public void setSchroederFolder(IFolder _schroederFolder) {
		this._schroederFolder = _schroederFolder;
	}
	public IFolder getSchroederFolder() {
		return _schroederFolder;
	}

	class PositionLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			Parameter p = (Parameter) element;
			String result = null;
			DecimalFormat f = new DecimalFormat("#.###");
			if (columnIndex == 0) {
				result = p.name();
			} else {
				result = f.format(p.channel(columnIndex - 1));
			}
			return result;
		}
	}

	class Parameter {
		double _channels[] = new double[11];
		String _name;

		public Parameter(String name, double[] ch) {
			_name = name;
			_channels = ch;
		}

		public String name() {
			return _name;
		}

		public double channel(int i) {
			return _channels[i];
		}

	}

}

class ParametersHolder {
	double c50, c80, d50, d80, ct;
	double edt, t20, t30, t40;
	double a10, b10, t10;
	double a20, b20;
	double a30, b30;
	double a40, b40;
	int itdg;
	double rdr, lfc;
	double[] schroederX;
	double[] schroederY;

	public ParametersHolder() {
		c50 = c80 = d50 = d80 = ct = edt = t20 = t30 = t40 = rdr = lfc = Double.NaN;
		a10 = b10 = t10 = Double.NaN;
		a20 = b20 = Double.NaN;
		a30 = b30 = Double.NaN;
		a40 = b40 = Double.NaN;
		itdg = -1;
	}
}