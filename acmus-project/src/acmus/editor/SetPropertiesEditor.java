/*
 *  SetPropertiesEditor.java
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import acmus.MeasurementProject;

public class SetPropertiesEditor extends EditorPart {

  SetPropertiesControl _propertiesControl;

  FileEditorInput _input;
  boolean _isDirty = false;

  @Override
  public void doSave(IProgressMonitor monitor) {
    Properties props = _propertiesControl.getSetProperties();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      props.store(baos, _input.getFile().getProject().getName() + "."
          + props.getProperty("Name") + " properties");
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      _input.getFile().setContents(bais, true, true,  null);
    } catch (Exception e) {
      e.printStackTrace();
    }

    setDirty(false);
  }

  @Override
  public void doSaveAs() {
    // TODO Auto-generated method stub

  }

  @Override
  public void init(IEditorSite site, IEditorInput input)
      throws PartInitException {
    setSite(site);
    setInput(input);
    _input = (FileEditorInput) input;
    this.setPartName(_input.getFile().getParent().getName() + " properties");
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

    IFile posFile = _input.getFile().getProject().getFile("project.positions");
//    Set<String> posSet = new TreeSet<String>();
//    _propertiesControl = new SetPropertiesControl(composite, SWT.NONE,
//        AcmusMeasurementSetWizard.getPositions(posFile, posSet));
  _propertiesControl = new SetPropertiesControl(composite, SWT.NONE,
  posFile.getLocation().toOSString());
    GridData gridData = new GridData(GridData.FILL_BOTH);
    _propertiesControl.setLayoutData(gridData);

    _propertiesControl.createTakeListControl(_input.getFile().getParent());
    _propertiesControl.setProjectName(_input.getFile().getProject().getName());
    _propertiesControl.setSessionName(MeasurementProject
        .removeSuffix(_input.getFile().getParent().getParent().getName()));

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

    Listener l = new Listener() {
      public void handleEvent(Event event) {
        setDirty(true);

      }
    };

    // _propertiesControl.addNameModifyListener(ml);
    _propertiesControl.setSetNameEditable(false);
    _propertiesControl.addSourceModifyListener(ml);
    _propertiesControl.addSourceListener(SWT.Modify, l);
    _propertiesControl.addMicModifyListener(ml);
    _propertiesControl.addMicListener(SWT.Modify, l);
    _propertiesControl.addCommentsModifyListener(ml);
  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub

  }

}
