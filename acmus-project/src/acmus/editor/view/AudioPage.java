package acmus.editor.view;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;
import acmus.MeasurementProject;
import acmus.audio.AudioDevice;
import acmus.dsp.Ir;
import acmus.dsp.Util;
import acmus.editor.AudioEditorControl;
import acmus.editor.MeasurementEditor;
import acmus.wizard.AcmusMeasurementWizard;
import acmus.wizard.AcmusMeasurementWizardFirstPage;

public class AudioPage extends Composite {

	public final MeasurementEditor parent;
	private Combo _cSource;
	private AudioEditorControl _aeSource;
	private AudioEditorControl _aeRec;
	private AudioEditorControl _aeRecLf;
	private TabFolder _tfRecordings;
	private TabItem _tiRec;
	private TabItem _tiRecLf;
	private Button _bRecord;
	private Button _bRecordLf;
	private Button _bIr;
	private Text _tTakes;
	private MessageBox _warningDialog;
	private int _index;
	private IFile _signalAudioFile;
	private AudioFormat _format;
	private IFile _signalFile;
	private ProgressMonitorPart _monitor;
	private String _method = "";

	public AudioPage(MeasurementEditor parent, int style) {
		super(parent.getContainer(), style);
		this.parent = parent;
		createAudioPage();
	}

	public AudioEditorControl getAeSource() {
		return _aeSource;
	}
	public void createAudioPage() {
		this.setLayout(new GridLayout(2, false));
	
		GridData gridData;
		Label l;
	
		l = new Label(this, SWT.LEFT);
		l.setText("Input Signal: ");
		this._cSource = new Combo(this, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		this._cSource.setLayoutData(gridData);
		this._cSource.setItems(getSignals());
		this._cSource.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
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
	
		this._aeSource = new AudioEditorControl(this, SWT.NONE, true, false, false);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		this._aeSource.setLayoutData(gridData);
	
		this._tfRecordings = new TabFolder(this, SWT.TOP);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		this._tfRecordings.setLayoutData(gridData);
		_tiRec = new TabItem(this._tfRecordings, SWT.NONE);
		this._tiRec.setText("Recording");
		_tiRecLf = new TabItem(this._tfRecordings, SWT.NONE);
		this._tiRecLf.setText("Recording 2");
	
		Composite c = new Composite(this._tfRecordings, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		c.setLayout(gridLayout);
	
		this._aeRec = new AudioEditorControl(c, SWT.NONE, false, false, false);
		gridData = new GridData(GridData.FILL_BOTH);
		this._aeRec.setLayoutData(gridData);
	
		Composite c2 = new Composite(c, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		c2.setLayout(gl);
	
		_bRecord = new Button(c2, SWT.CENTER);
		this._bRecord.setImage(AcmusGraphics.IMG_RECORD);
		this._bRecord.setToolTipText("Record");
		l = new Label(c2, SWT.LEFT);
		l.setText("takes:");
		_tTakes = new Text(c2, SWT.BORDER);
		this._tTakes.setText("1");
	
		this._tiRec.setControl(c);
	
		c = new Composite(this._tfRecordings, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		c.setLayout(gridLayout);
	
		this._aeRecLf = new AudioEditorControl(c, SWT.NONE, false, false, false);
		gridData = new GridData(GridData.FILL_BOTH);
		this._aeRecLf.setLayoutData(gridData);
	
		_bRecordLf = new Button(c, SWT.CENTER);
		this._bRecordLf.setImage(AcmusGraphics.IMG_RECORD);
		this._bRecordLf.setToolTipText("Record");
	
		this._tiRecLf.setControl(c);
	
		_bIr = new Button(this, SWT.CENTER);
		this._bIr.setText("Calculate IR");
		this._bIr.setEnabled(false);
	
		this._monitor = new ProgressMonitorPart(this, null);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_monitor.setLayoutData(gridData);
	
		_warningDialog = new MessageBox(parent.getContainer().getShell(), SWT.OK
				| SWT.CANCEL);
		this._warningDialog
				.setMessage("This operation will overwrite the current recording and erase the IR and parameters data.");
		this._warningDialog.setText("Warning");
	
		this._bRecord.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
	
				if (parent.getIrFile().exists() || parent.getParamsFile().exists()) {
					int warning = _warningDialog.open();
					if (warning == SWT.CANCEL)
						return;
					try {
						if (parent.getIrFile().exists())
							parent.getIrFile().delete(true, null);
						if (parent.getParamsFile().exists())
							parent.getParamsFile().delete(true, null);
						if (parent.getSchroederFolder().exists())
							parent.getSchroederFolder().delete(true, null);
						parent.getOutFolder().refreshLocal(IFolder.DEPTH_ONE, null);
						parent.removeIrPage();
						parent.removeParametersPage();
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
				_monitor.beginTask("Recording", takes);
				_monitor.subTask("Take 1");
				record(parent.getRecFile().getLocation().toOSString());
				try {
					parent.getOutFolder().refreshLocal(IFolder.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				loadRecording();
				_monitor.worked(1);
				IFolder setFolder = (IFolder) parent.getOutFolder().getParent();
				for (int i = 2; i <= takes; i++) {
					_monitor.subTask("Take " + i);
	
					String name = AcmusMeasurementWizardFirstPage
							.generateNameFor(setFolder, MeasurementProject
									.removeSuffix(setFolder.getName()));
					Properties props = new Properties();
					props.setProperty("Name", name);
					props.setProperty("Signal", _signalFile.getName());
					props.setProperty("Method", _method);
					IFolder takeFolder = AcmusMeasurementWizard
							.createMeasurement(setFolder, props);
					record(takeFolder.getFile(parent.getRecFile().getName()).getLocation()
							.toOSString());
					try {
						takeFolder.refreshLocal(IFolder.DEPTH_INFINITE, null);
					} catch (CoreException e) {
						e.printStackTrace();
					}
					_monitor.worked(1);
				}
				_monitor.done();
			}
		});
	
		this._bRecordLf.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
	
				_monitor.beginTask("Recording LF", 1);
				record(parent.getRecFileLf().getLocation().toOSString());
				try {
					parent.getOutFolder().refreshLocal(IFolder.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				loadRecordingLf();
				_monitor.worked(1);
				_monitor.done();
			}
		});
	
		this._bIr.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (parent.isIrPageCreated())
					parent.createIrPage();
				calculateIr();
				calculateIrLf();
			}
		});
	
		this._method = parent.getProps().getProperty("Method");
	
		if (parent.getProps().containsKey("Signal")) {
			_signalFile = parent.getSigFolder().getFile(parent.getProps().getProperty("Signal"));
			for (int i = 0; i < this._cSource.getItemCount(); i++) {
				if (this._cSource.getItem(i).equals(parent.getProps().getProperty("Signal"))) {
					this._cSource.select(i);
					loadSignal(this._cSource.getItem(this._cSource.getSelectionIndex()));
					break;
				}
			}
		}
	
		this._index = parent.addPage(this);
		parent.setPageText(_index, "Recording");
	}

	public AudioEditorControl getAeRec() {
		return this._aeRec;
	}

	public AudioEditorControl getAeRecLf() {
		return this._aeRecLf;
	}

	public Button getBIr() {
		return this._bIr;
	}

	private void calculateIr() {
		Ir.calculateIr(parent.getOutFolder().getFile("recording.wav"), parent
				.getIrFile(), this._signalFile, _monitor);
		try {
			parent.getOutFolder().refreshLocal(IFolder.DEPTH_ONE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		parent.openIrFile();
	}

	private void calculateIrLf() {
		if (parent.getRecFileLf().exists()) {
			Ir.calculateIr(parent.getRecFileLf(), parent.getIrFileLf(), 
					this._signalFile, _monitor);
			try {
				parent.getOutFolder().refreshLocal(IFolder.DEPTH_ONE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			parent.openIrFileLf();
		}
	}

	public int getIndex() {
		return this._index;
	}

	private String[] getSignals() {
		File f = new File(parent.getSigFolder().getLocation().toOSString());
		return f.list(new FilenameFilter() {
			public boolean accept(File f, String name) {
				return name.endsWith(".signal");
			}
		});
	}

	private void loadSignal(String signal) {
		try {
			_signalFile = parent.getSigFolder().getFile(signal);
			Properties props = new Properties();
			props.load(this._signalFile.getContents());
			String name = props.getProperty("Name");
			this._method = props.getProperty("Type", "");
	
			_signalAudioFile = parent.getSigAudioFolder().getFile(name + ".wav");
			AudioInputStream ais = AudioSystem
					.getAudioInputStream(_signalAudioFile.getContents());
			_format = ais.getFormat();
			_format = new AudioFormat(_format.getEncoding(), _format
					.getSampleRate(), _format.getSampleSizeInBits(), 1, _format
					.getFrameSize(), _format.getFrameRate(), _format
					.isBigEndian());
			getAeSource().open(_signalAudioFile.getLocation().toOSString());
			changeProperties();
			parent.doSave(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void record(String outFile) {
		System.out.println("rec " + outFile);
	
		try {
	
			try {
	
				AudioDevice recorder = AcmusPlugin.getDefault().audioDevice;
	
				int t = (int) Math.round(1000 * Double
						.parseDouble(MeasurementProject.getProperty(parent.getRecFile()
								.getProject(), "RecordingExtraTime", ""
								+ Util.DEFAULT_REC_EXTRA)));
				recorder.record(new File(outFile), getAeSource()
						.getTotalDurationInMillis()
						+ t);
	
	
				getAeSource().play();
	
				getAeSource().waitStop();
	
				Thread.sleep(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
			parent.doSave(null);
			Thread.sleep(300);
		} catch (Exception e) {
			e.printStackTrace();
	
		}
	}

	public final void loadRecording() {
		try {
			getAeRec().open(parent.getRecFile().getLocation().toOSString());
			getBIr().setEnabled((_method.equals("sweep") || _method.equals("mls")));
		} catch (Exception e) {
			e.printStackTrace();
	
		}
	
	}

	public final void loadRecordingLf() {
		try {
			getAeRecLf().open(parent.getRecFileLf().getLocation().toOSString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public IFile getSignalFile() {
		return this._signalFile;
	}

	public String getMethod() {
		return this._method;
	}

	public void changeProperties() {
		parent.getProps().setProperty("Method", getMethod());
		parent.getProps().setProperty("Signal", getSignalFile().getName());
	}

}
