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

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import acmus.MeasurementProject;
import acmus.editor.view.AudioPage;
import acmus.editor.view.ImpulseResponsePage;
import acmus.editor.view.ParametersPage;

/**
 * @author lku
 */
public class MeasurementEditor extends MultiPageEditorPart {

	FileEditorInput _input;

	int _propertiesIndex = -1;

	private MeasurementPropertiesEditor _propertiesEditor;

	Text _tSource;



	Object _playLock = new Object();
	boolean _playing = false;


	private IFolder _sigFolder;
	private IFolder _sigAudioFolder;
	private IFolder _outFolder;
	private IFile _recFile;
	private IFile _recFileLf;
	private IFile _irFile;
	private IFile _irFileLf;
	private IFile _paramsFile;
	private IFolder _schroederFolder;
	IFile _propsFile;

	private Properties _props;

	boolean _RecPage;

	//--------Pages---------------------------------
	private ImpulseResponsePage impulseResponsePage;
	private ParametersPage parametersPage;
	private AudioPage audioPage;
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

		setRecFile(getOutFolder().getFile("recording.wav"));
		setRecFileLf(getOutFolder().getFile("recording2.wav"));
		IProject p = _input.getFile().getProject();
		setSigFolder(p.getFolder("_signals.signal"));
		setSigAudioFolder(getSigFolder().getFolder("audio"));
		this._paramsFile = getOutFolder().getFile("parameters.txt");
		this._schroederFolder = getOutFolder().getFolder("schroeder");
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
			audioPage = new AudioPage(this, SWT.NONE);

			if (getRecFile().exists()) {
				try {
					audioPage.getAeRec().open(getRecFile().getLocation().toOSString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				audioPage.getBIr().setEnabled(("sweep".equals(audioPage.getMethod()) || "mls"
						.equals(audioPage.getMethod())));
			}
			if (getRecFileLf().exists()) {
				try {
					audioPage.getAeRecLf().open(getRecFileLf().getLocation().toOSString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				audioPage.getBIr().setEnabled(("sweep".equals(audioPage.getMethod()) || "mls"
						.equals(audioPage.getMethod())));
			}
			this.setActivePage(audioPage.getIndex());
		}

		if (getIrFile().exists()) {
			createIrPage();
		}
		if (getParamsFile().exists() && getSchroederFolder().exists()) {
			createParametersPage();
		}

	}
	public void createIrPage() {
		this.impulseResponsePage = new ImpulseResponsePage(this, SWT.NONE);
		this.setActivePage(impulseResponsePage.getIndex());
	}
	public void createParametersPage() {
		this.parametersPage = new ParametersPage(this, SWT.NONE);
		this.setActivePage(parametersPage.getIndex());
	}
	public boolean isParametersPageCreated() {
		return parametersPage != null;
	}
	public boolean isIrPageCreated() {
		return impulseResponsePage != null;
	}
	public void removeIrPage() {
		if (isIrPageCreated()) {
			removePage(impulseResponsePage.getIndex());
			this.impulseResponsePage = null;
			if (parametersPage != null) {
				parametersPage.setIndex(parametersPage.getIndex() - 1);
			}
		}
	}

	public void removeParametersPage() {
		if (isParametersPageCreated()) {
			removePage(parametersPage.getIndex());
			this.parametersPage = null;
		}
	}

	// ----------------------------------------------------------------------------

	public void createPropertiesPage() {

		try {
			_propertiesEditor = new MeasurementPropertiesEditor();
			_propertiesIndex = addPage(_propertiesEditor, getEditorInput());
			setPageText(_propertiesIndex, "Properties");

			setProps(_propertiesEditor.getMeasurementProperties());
		} catch (PartInitException e) {
			e.printStackTrace();
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
		if (audioPage != null && audioPage.getMethod() != null) {
			audioPage.changeProperties();
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
	
	public void setRecFileLf(IFile _recFileLf) {
		this._recFileLf = _recFileLf;
	}
	public IFile getRecFileLf() {
		return _recFileLf;
	}

	public IFile getParamsFile() {
		return _paramsFile;
	}

	public IFolder getSchroederFolder() {
		return _schroederFolder;
	}

	public ImpulseResponsePage getImpulseResponsePage() {
		return impulseResponsePage;
	}
	//////////////////////////////////////////////////

	public void setSigFolder(IFolder _sigFolder) {
		this._sigFolder = _sigFolder;
	}
	public IFolder getSigFolder() {
		return _sigFolder;
	}

	public void setRecFile(IFile _recFile) {
		this._recFile = _recFile;
	}
	public IFile getRecFile() {
		return _recFile;
	}

	public void setProps(Properties _props) {
		this._props = _props;
	}
	public Properties getProps() {
		return _props;
	}

	public void openIrFile() {
		impulseResponsePage.getAeIr().open(_irFile.getLocation().toOSString());
	}

	public void openIrFileLf() {
		impulseResponsePage.getAeIrLf().open(_irFileLf.getLocation().toOSString());
	}
	public void setSigAudioFolder(IFolder _sigAudioFolder) {
		this._sigAudioFolder = _sigAudioFolder;
	}
	public IFolder getSigAudioFolder() {
		return _sigAudioFolder;
	}

}