/*
 *  MlsWizardFirstPage.java
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
/**
 * MlsWizardFirstPage.java
 * Created on 26/10/2005
 */
package acmus.wizard;

import java.util.List;
import java.util.Properties;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import acmus.dsp.Signal;

/**
 * @author lku
 *
 */
public class MlsWizardFirstPage extends WizardPage {
  Text fName;

  Combo _order;
  Combo _taps;
  Text _reps;
  
  Properties fProperties;

  private Listener nameModifyListener = new Listener() {
    public void handleEvent(Event e) {
      setPageComplete(validatePage());
    }
  };

  private Listener orderSelectionListener = new Listener() {
    public void handleEvent(Event e) {
      setTaps();
    }
  };

  private Listener tapsSelectionListener = new Listener() {
    public void handleEvent(Event e) {
      updateName();
    }
  };

  
  public MlsWizardFirstPage(String name) {
    super(name);
    setTitle("New MLS");
    setDescription("Choose generated MLS parameters.");
    setPageComplete(false);
    fProperties = new Properties();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(2, false));
    composite.setFont(parent.getFont());

    GridData gridData = new GridData(GridData.FILL_BOTH);
    composite.setLayoutData(gridData);

    Label l = new Label(composite, SWT.LEFT);
    l.setText("Name:");
    fName = new Text(composite, SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    fName.setLayoutData(gridData);
    fName.setEditable(false);
    fName.setText("LogSineSweep_6s_20-20000Hz");
    fName.addListener(SWT.Modify, nameModifyListener);

    l = new Label(composite, SWT.LEFT);
    l.setText("Order:");
    _order = new Combo(composite, SWT.NONE);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    _order.setLayoutData(gridData);
    _order.addListener(SWT.Selection, orderSelectionListener);

    l = new Label(composite, SWT.LEFT);
    l.setText("Taps:");
    _taps = new Combo(composite, SWT.NONE);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    _taps.setLayoutData(gridData);
    _taps.addListener(SWT.Selection, tapsSelectionListener);

    l = new Label(composite, SWT.LEFT);
    l.setText("Repetitions:");
    _reps = new Text(composite, SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    _reps.setLayoutData(gridData);
    _reps.setText("2");
    
    setControl(composite);

    for (int i = 2; i <= Signal.maxMlsOrder(); i++) {
      if (Signal.hasMlsOrder(i)) {
        _order.add("" + i);
      }
    }
    
    _order.select(0);
    setTaps();
    _taps.select(0);
    updateName();
    
  }

  public boolean validatePage() {
    return _order.getSelectionIndex() >= 0 && _taps.getSelectionIndex() >= 0 && isInt(_reps.getText());
  }
  private boolean isInt(String str) {
    // there must be a better way...
    try {
      Integer.parseInt(str);
    }
    catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  public void updateName() {
    fName.setText("MLS_" + _order.getText() +"_" + _taps.getText().replace(' ', '-'));
  }
  
  public void setTaps() {
    _taps.removeAll();
    List<int[]> l = Signal.mlsTaps(Integer.parseInt(_order.getItem(_order.getSelectionIndex())));
    for (int[] t : l) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < t.length-1; i++) {
        sb.append(t[i] + " ");
      }
      sb.append(t[t.length-1]);      
      _taps.add(sb.toString());
    }
    _taps.select(0);
    updateName();
  }
  
  /**
   * Updates and returns the <code>Properties</code> object that contains the
   * given session properties.
   * 
   * @return the given session properties
   */
  public Properties getSessionProperties() {
    fProperties.clear();
    fProperties.setProperty("Name", fName.getText());
    fProperties.setProperty("Order", _order.getText());
    fProperties.setProperty("Taps", _taps.getText());
    fProperties.setProperty("Repetitions", _reps.getText());
    return fProperties;
  }

}
