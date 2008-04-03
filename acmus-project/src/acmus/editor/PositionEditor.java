/*
 *  PositionEditor.java
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
package acmus.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

public class PositionEditor extends EditorPart {

	PositionEditor _this;

	FileEditorInput _input;

	boolean _isDirty;

	MultiplePositionEditorControl _positionControl;

	private IDocumentListener _editorDocListener = new IDocumentListener() {
		public void documentAboutToBeChanged(DocumentEvent event) {

		}

		public void documentChanged(DocumentEvent event) {
			setDirty(true);
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		_this = this;

		_input = (FileEditorInput) input;
		_isDirty = false;

		setPartName(_input.getFile().getProject().getName());
	}

	public boolean isDirty() {
		return _isDirty;
	}

	public void setDirty(boolean dirty) {
		if (_isDirty != dirty) {
			_isDirty = dirty;
			firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
		}
	}

	public void createPartControl(Composite parent) {
		_positionControl = new MultiplePositionEditorControl(parent, SWT.NONE,
				_editorDocListener, false);
		openFile(_input);
	}

	private void openFile(IEditorInput input) {

		if (!(input instanceof IFileEditorInput))
			return;
		try {
			InputStreamReader isr = new InputStreamReader(
					((IFileEditorInput) input).getFile().getContents());
			_positionControl.read(isr);
			isr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void save(MultiplePositionEditorControl positionControl,
			IFile file, IProgressMonitor monitor) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		positionControl.write(ps);

		ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
		try {
			file.setContents(bis, true, false, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void doSave(IProgressMonitor monitor) {
		// ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// PrintStream ps = new PrintStream(baos);
		// _positionControl.write(ps);
		//
		// ByteArrayInputStream bis = new
		// ByteArrayInputStream(baos.toByteArray());
		// try {
		// _input.getFile().setContents(bis, true, false, null);
		// } catch (CoreException e) {
		// e.printStackTrace();
		// }
		save(_positionControl, _input.getFile(), monitor);
		setDirty(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	public void doSaveAs() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
