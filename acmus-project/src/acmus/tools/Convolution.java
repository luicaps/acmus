/*
 *  Convolution.java
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
package acmus.tools;

import java.io.File;

import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import acmus.dsp.Util;

public class Convolution extends Composite {

  Text _input1;
  Text _input2;
  Text _output;
  
  Button _input1Browse;
  Button _input2Browse;
  Button _outputBrowse;
  
  FileDialog _input1FileDialog;
  FileDialog _input2FileDialog;
  FileDialog _outputFileDialog;
  
  Button _convolve;
  
  ProgressMonitorPart _monitor;
  
  public Convolution(Shell shell, Composite parent, int style) {
    super(parent, style);
    
    _input1FileDialog = new FileDialog(shell, SWT.OPEN);
    _input2FileDialog = new FileDialog(shell, SWT.OPEN);
    _outputFileDialog = new FileDialog(shell, SWT.SAVE);
    
    setLayout(new GridLayout(3, false));
    
    Label l;
    GridData gridData;
    
    ModifyListener ml = new ModifyListener() {
      public void modifyText(ModifyEvent e) {        
        validate();
      }
    };
    
    l = new Label(this, SWT.LEFT);
    l.setText("Input 1");
    
    _input1 = new Text(this, SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.widthHint = 200;
    _input1.setLayoutData(gridData);
    _input1.addModifyListener(ml);
    
    _input1Browse = new Button(this, SWT.NONE);
    _input1Browse.setText("Browse");
    _input1Browse.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        String filename = _input1FileDialog.open();
        if (filename != null) {
          _input1.setText(filename);
        }
      }
    });

    l = new Label(this, SWT.LEFT);
    l.setText("Input 2");
    
    _input2 = new Text(this, SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    _input2.setLayoutData(gridData);
    _input2.addModifyListener(ml);
    
    _input2Browse = new Button(this, SWT.NONE);
    _input2Browse.setText("Browse");
    _input2Browse.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        String filename = _input2FileDialog.open();
        if (filename != null) {
          _input2.setText(filename);
        }
      }
    });

    l = new Label(this, SWT.LEFT);
    l.setText("Output");
    
    _output = new Text(this, SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    _output.setLayoutData(gridData);
    _output.addModifyListener(ml);
    
    _outputBrowse = new Button(this, SWT.NONE);
    _outputBrowse.setText("Browse");
    _outputBrowse.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        String filename = _outputFileDialog.open();
        if (filename != null) {
          _output.setText(filename);
        }
      }
    });
    
    _monitor = new ProgressMonitorPart(this, null);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 3;
    _monitor.setLayoutData(gridData);

    _convolve = new Button(this, SWT.NONE);
    gridData = new GridData();
    gridData.horizontalSpan = 3;
    gridData.horizontalAlignment = SWT.CENTER;
    _convolve.setLayoutData(gridData);
    _convolve.setText("Convolve");
    _convolve.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        Util.convolve(_input1.getText(), _input2.getText(), _output.getText(), _monitor);
        validate();
      }
    });
    _convolve.setEnabled(false);
  }
  
  public void setInput1(String filename) {
    _input1.setText(filename);
  }
  public void setInput2(String filename) {
    _input2.setText(filename);
  }
  
  private void validate() {
    if (_input1.getText().trim().equals("") || _input2.getText().trim().equals("") || _output.getText().trim().equals("")) {
      _convolve.setEnabled(false);
    }
    else {
      File i1 = new File(_input1.getText());
      File i2 = new File(_input2.getText());
      if (!i1.exists() || !i2.exists()) 
        _convolve.setEnabled(false);
      else
      _convolve.setEnabled(true);
    }
  }
}