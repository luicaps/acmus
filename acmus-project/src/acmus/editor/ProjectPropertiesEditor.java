/*
 *  ProjectPropertiesEditor.java
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
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

public class ProjectPropertiesEditor extends EditorPart {

  ProjectPropertiesControl _propertiesControl;

  FileEditorInput _input;
  boolean _isDirty = false;

  public static void save(ProjectPropertiesControl propertiesControl, IFile file, IProgressMonitor monitor) {
    Properties props = propertiesControl.getProjectProperties();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      props.store(baos, file.getProject().getName() +" properties");
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      file.setContents(bais, true, true, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void doSave(IProgressMonitor monitor) {
//    Properties props = _propertiesControl.getProjectProperties();
//
//    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//    try {
//      props.store(baos, _input.getFile().getProject().getName() +" properties");
//      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//      _input.getFile().setContents(bais, true, true, null);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
    save(_propertiesControl, _input.getFile(), monitor);

    setDirty(false);
  }

  @Override
  public void doSaveAs() {
  }

  @Override
  public void init(IEditorSite site, IEditorInput input)
      throws PartInitException {
    setSite(site);
    setInput(input);
    _input = (FileEditorInput) input;
    this.setPartName(_input.getFile().getProject().getName() + " properties");
  }

  @Override
  public boolean isDirty() {
    return _isDirty;
  }

  public void setDirty(boolean dirty) {
    if (_isDirty != dirty) {
      _isDirty = dirty;
      firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
    }
  }

  @Override
  public boolean isSaveAsAllowed() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void createPartControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(1, false));
    composite.setFont(parent.getFont());

    _propertiesControl = new ProjectPropertiesControl(composite, SWT.NONE);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    _propertiesControl.setLayoutData(gridData);

    try {
      _propertiesControl.loadProperties(_input.getFile().getContents());
    } catch (CoreException e) {
      e.printStackTrace();
    }

    ModifyListener ml = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setDirty(true);
      }
    };

    _propertiesControl.addLocationModifyListener(ml);
    _propertiesControl.addDateModifyListener(ml);
    _propertiesControl.addCommentsModifyListener(ml);
    _propertiesControl.addIrLengthModifyListener(ml);
    _propertiesControl.addRecExtraModifyListener(ml);
  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub

  }

}
