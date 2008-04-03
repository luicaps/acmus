/*
 *  AudioEditor.java
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
 */
package acmus.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import acmus.MeasurementProject;

/**
 * @author lku
 */
public class AudioEditor extends EditorPart {

	Composite _parent;
	FileEditorInput _input;

	AudioEditorControl _audioControl;

	// AudioPlayer _audioPlayer;
	// WaveformDisplay _waveform;
	// Composite _dbMeter;
	// SpectrumDisplay _spectrum;
	// Composite _spectrumComposite;
	// Combo _spectrumWindowFunc;
	// ToolBar _controlBar;
	// //Slider _gainSlider;
	// ToolBar _zoomBar;
	// ToolBar _selModeBar;
	// Combo _graphMode;
	// //Button _bExplain;
	// Text _tPosition;

	Composite _graphArea;

	public IEditorInput getEditorInput() {
		return _input;
	}

	public void doSave(IProgressMonitor monitor) {
	}

	public void doSaveAs() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.setSite(site);
		_input = (FileEditorInput) input;
		setPartName(MeasurementProject.removeSuffix(_input.getName()));
		// try {
		// _audioPlayer = new AudioPlayer();
		// // File f = new File(_input.getFile().getLocation().toOSString());
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public void createPartControl(Composite parent) {
		_parent = parent;
		try {
			GridLayout gridLayout;
			GridData gridData;

			gridLayout = new GridLayout(1, false);
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			_parent.setLayout(gridLayout);

			_audioControl = new AudioEditorControl(_parent, SWT.NONE, _input
					.getFile().getParent().getLocation().toOSString(), _input
					.getName(), true, true, true);
			gridData = new GridData(GridData.FILL_BOTH);
			_audioControl.setLayoutData(gridData);
			_audioControl.open(_input.getFile().getLocation().toOSString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		super.dispose();
		// _audioPlayer.dispose();
	}

}
