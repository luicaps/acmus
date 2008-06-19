package acmus.editor.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import acmus.AcmusApplication;
import acmus.dsp.Parameters;
import acmus.editor.AudioEditorControl;
import acmus.editor.MeasurementEditor;
import acmus.util.ArrayUtils;
import acmus.util.WaveUtils;

public class ImpulseResponsePage extends Composite {

	private final MeasurementEditor parent;
	private AudioEditorControl _aeIr;
	private int _irIndex;
	private int _firstReflection;
	private int _directSound;
	private ProgressMonitorPart _paramMonitor;
	private Combo _cParamMethod;
	private Button _bParam;
	private AudioEditorControl _aeIrLf;
	private Button _bSetItdg;
	private Label _lItdg;
	private Button _bCutIr;

	public ImpulseResponsePage(MeasurementEditor parent, int style) {
		super(parent.getContainer(), style);
		this.parent = parent;
		createPage();
	}

	private void createPage() {

		this.setLayout(new GridLayout(1, false));

		GridData gridData;
		GridLayout gl;

		TabFolder tf = new TabFolder(this, SWT.TOP);
		gridData = new GridData(GridData.FILL_BOTH);
		tf.setLayoutData(gridData);
		TabItem ti = new TabItem(tf, SWT.NONE);
		ti.setText("IR");
		TabItem tiLf = new TabItem(tf, SWT.NONE);
		tiLf.setText("IR 2");

		// ------------------------------------------------------------------

		int cols = 1;
		Composite c = new Composite(tf, SWT.NONE);
		gl = new GridLayout(cols, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		c.setLayout(gl);

		_aeIr = new AudioEditorControl(c, SWT.NONE, true, true, false);
		gridData = new GridData(GridData.FILL_BOTH);
		_aeIr.setLayoutData(gridData);

		// -------------

		Composite comp = new Composite(c, SWT.NONE);
		gl = new GridLayout(3, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);

		this._bSetItdg = new Button(comp, SWT.CENTER);
		_bSetItdg.setText("Set ITDG");
		this._lItdg = new Label(comp, SWT.NONE);
		gridData = new GridData(SWT.LEFT);
		gridData.widthHint = 100;
		_lItdg.setLayoutData(gridData);

		this._bCutIr = new Button(comp, SWT.CENTER);
		_bCutIr.setText("Save selected IR");
		_bCutIr.setToolTipText("Replace IR with selection");
		gridData = new GridData(SWT.NONE);
		_bCutIr.setLayoutData(gridData);

		ti.setControl(c);

		// ------------------------------------------------------------------

		cols = 1;
		c = new Composite(tf, SWT.NONE);
		gl = new GridLayout(cols, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		c.setLayout(gl);

		this._aeIrLf = new AudioEditorControl(c, SWT.NONE, true, false, false);
		gridData = new GridData(GridData.FILL_BOTH);
		_aeIrLf.setLayoutData(gridData);

		tiLf.setControl(c);
		// -----------------------------------------------------------------------
		comp = new Composite(this, SWT.NONE);
		gl = new GridLayout(3, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gridData);

		this._bParam = new Button(comp, SWT.CENTER);
		_bParam.setText("Calculate Parameters");
		_bParam
				.setToolTipText("Calculate acoustical parameteres using selected IR");

		this._cParamMethod = new Combo(comp, SWT.NONE);
		_cParamMethod.add("Chu");
		_cParamMethod.add("Lundeby");
		if (parent.getIrFileLf().exists()) {
			_cParamMethod.add("Hirata");
		}
		_cParamMethod.select(0);

		this._paramMonitor = new ProgressMonitorPart(comp, null);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_paramMonitor.setLayoutData(gridData);

		_bParam.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				calculateParameters();
				if (parent.isParametersPageCreated()) {
					parent.removeParametersPage();
				}
				parent.createParametersPage();
			}
		});

		_bSetItdg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				showITDGInMilis();
			}
		});

		_bCutIr.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				cutIrSelection();
			}
		});

		try {
			if (parent.getIrFile().exists()) {
				_aeIr.open(parent.getIrFile().getLocation().toOSString());
			}
			if (parent.getIrFileLf().exists()) {
				_aeIrLf.open(parent.getIrFileLf().getLocation().toOSString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this._irIndex = parent.addPage(this);
		parent.setPageText(this._irIndex, "Impulse Response");
	}

	public AudioEditorControl getAeIr() {
		return _aeIr;
	}

	public AudioEditorControl getAeIrLf() {
		return _aeIrLf;
	}

	public int getIndex() {
		return this._irIndex;
	}

	public double[] getIr(AudioEditorControl ae) {
		if (ae.getData() != null) {
			return ArrayUtils.scaleToUnit(ae.getData(), ae.getMaxSample());
		}
		return null;
	}

	public void calculateParameters() {
		try {
			ByteArrayOutputStream baosTable = new ByteArrayOutputStream();
			PrintStream outTable = new PrintStream(baosTable);

			if (!parent.getSchroederFolder().exists()) {
				parent.getSchroederFolder().create(true, true, null);
			}

			double[] ir2 = getIr(getAeIr());
			if (getAeIr().selectionValid()) {
				ir2 = ArrayUtils.subArray(ir2,
						getAeIr().getSelectionStartInSamples(), getAeIr()
								.getSelectionEndInSamples());
			}

			double[] ir2Lf = getIr(_aeIrLf);
			if (ir2Lf != null) {
				int end = Parameters.inicio(ir2Lf) + (int) (AcmusApplication.SAMPLE_RATE * 0.20); // guessed
				ir2Lf = ArrayUtils.subArray(ir2Lf, 0, end);
			}

			if ("Chu".equals(_cParamMethod.getItem(_cParamMethod
					.getSelectionIndex()))) {
				Parameters p = new Parameters(ir2, ir2Lf, 44100);
				p.chuParam(this._directSound, this._firstReflection, outTable,
						parent.getSchroederFolder().getLocation().toOSString(),
						_paramMonitor);
			} else if ("Lundeby".equals(_cParamMethod.getItem(_cParamMethod
					.getSelectionIndex()))) {
				Parameters.lundebyParam(ir2, ir2Lf, AcmusApplication.SAMPLE_RATE, this._directSound,
						this._firstReflection, outTable, parent
								.getSchroederFolder().getLocation()
								.toOSString());
			} else if ("Hirata".equals(_cParamMethod.getItem(_cParamMethod
					.getSelectionIndex()))) {
				Parameters.hirataParam(ir2, ir2Lf, AcmusApplication.SAMPLE_RATE, this._directSound,
						this._firstReflection, outTable, parent
								.getSchroederFolder().getLocation()
								.toOSString());
			} else {
				System.err.println("Unknown method: "
						+ _cParamMethod.getItem(_cParamMethod
								.getSelectionIndex()));
				System.err.println("  using Chu...");
				Parameters.chuParamOld(ir2, ir2Lf, AcmusApplication.SAMPLE_RATE, this._directSound,
						this._firstReflection, outTable, parent
								.getSchroederFolder().getLocation()
								.toOSString(), _paramMonitor);
			}

			byte[] buf = baosTable.toByteArray();
			if (!parent.getParamsFile().exists()) {
				parent.getParamsFile().create(new ByteArrayInputStream(buf),
						true, null);
			} else {
				parent.getParamsFile().setContents(
						new ByteArrayInputStream(buf), true, true, null);
			}

			parent.getOutFolder().refreshLocal(IFolder.DEPTH_ONE, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showITDGInMilis() {
		_directSound = _aeIr.getSelectionStartInSamples();
		_firstReflection = _aeIr.getSelectionEndInSamples();
		int itdgInMillis = _aeIr.getSelectionEndInMillis()
				- _aeIr.getSelectionStartInMillis();
		_bSetItdg.setToolTipText(_firstReflection + " - "
				+ _directSound + " = "
				+ (_firstReflection - _directSound));
		_lItdg.setText("ITDG: " + itdgInMillis + " ms");
		_aeIr.eraseSelection();
		_aeIr.redrawWf();
	}

	private void cutIrSelection() {
		if (_aeIr.selectionValid()) {
			double[] ir = ArrayUtils.subArray(getIr(_aeIr), _aeIr
					.getSelectionStartInSamples(), _aeIr
					.getSelectionEndInSamples());
			// FIXME This is one of the places to set 16 or 32 bits
			// if we want to change the IR resolution
			ir = ArrayUtils.scaleToMax(ir, (double) WaveUtils.getLimit(32));
			WaveUtils.wavWrite(ir, 1, 32, parent.getIrFile().getLocation()
					.toOSString(), false);
			try {
				parent.getOutFolder().refreshLocal(IFolder.DEPTH_ONE,
						null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			_aeIr.open(parent.getIrFile().getLocation().toOSString());
			_aeIr.eraseSelection();
		}
	}
}
