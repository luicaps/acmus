/*
 *  SessionPropertiesControl.java
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import acmus.AcmusGraphics;
import acmus.MeasurementProject;

public class SessionPropertiesControl extends Composite {

  Text fName;
  Text fTime;
  Text fTemperature;
  Text fEquipment;
  Text fComments;
  
  String _projectName;

  FormText _names;

  private FormToolkit toolkit;
  private Form form;

  Properties fProperties;

  public SessionPropertiesControl(Composite parent, int style) {
    super(parent,style);
    fProperties = new Properties();
    FillLayout fl = new FillLayout();
    fl.marginHeight=1;
    fl.marginWidth = 1;
    setLayout(fl);
    setBackground(AcmusGraphics.BLACK);

    fProperties = new Properties();

    GridLayout gridLayout;
    GridData gridData;
    Label l;

    toolkit = new FormToolkit(AcmusGraphics.FORMCOLORS);
    form = toolkit.createForm(this);

    form.setText("Session Properties");
    form.setBackgroundImage(AcmusGraphics.IMG_FORMBANNER);

    gridLayout = new GridLayout(1, true);
    Composite body = form.getBody();
    body.setLayout(gridLayout);

    _names = toolkit.createFormText(body, true);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalIndent = 5;
    gridData.horizontalSpan = 2;
    _names.setLayoutData(gridData);

    Composite bodyLeft = toolkit.createComposite(body);
    gridLayout = new GridLayout(4, false);
    gridLayout.verticalSpacing = 10;
    bodyLeft.setLayout(gridLayout);
    gridData = new GridData(GridData.FILL_BOTH);
    bodyLeft.setLayoutData(gridData);
    toolkit.paintBordersFor(bodyLeft);

    l = toolkit.createLabel(bodyLeft, "Session:");
    fName = toolkit.createText(bodyLeft, "");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 3;
    fName.setLayoutData(gridData);

    l = toolkit.createLabel(bodyLeft, "Time:");
    fTime = toolkit.createText(bodyLeft, "");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    fTime.setLayoutData(gridData);
    GregorianCalendar c = (new GregorianCalendar());
    fTime.setText((c.get(Calendar.HOUR) < 10 ? "0" : "") + c.get(Calendar.HOUR)
        + ":" + (c.get(Calendar.MINUTE) < 10 ? "0" : "")
        + c.get(Calendar.MINUTE));

    l = toolkit.createLabel(bodyLeft, "Temperature:");
    fTemperature = toolkit.createText(bodyLeft, "\u00b0");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    fTemperature.setLayoutData(gridData);

    l = toolkit.createLabel(bodyLeft,"Equipment:");
    gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    l.setLayoutData(gridData);
    fEquipment = toolkit.createText(bodyLeft, "", SWT.MULTI);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 3;
    gridData.heightHint = 60;
    fEquipment.setLayoutData(gridData);

    l = toolkit.createLabel(bodyLeft, "Comments:");
    gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    l.setLayoutData(gridData);
    fComments = toolkit.createText(bodyLeft, "", SWT.MULTI);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 3;
    gridData.heightHint = 60;
    fComments.setLayoutData(gridData);
    
  }
  
  public void updateNames(String project) {
    _names.setText("<form><p>project: <b>" + project + "</b></p><p></p></form>", true, false);
  }

  public void setProjectName(String name) {
    _projectName = name;
    updateNames(_projectName);
  }
  
  public void addNameModifyListener(ModifyListener l) {
    fName.addModifyListener(l);
  }
  public void addTimeModifyListener(ModifyListener l) {
    fTime.addModifyListener(l);
  }
  public void addTemperatureModifyListener(ModifyListener l) {
    fTemperature.addModifyListener(l);
  }
  public void addEquipmentModifyListener(ModifyListener l) {
    fEquipment.addModifyListener(l);
  }
  public void addCommentsModifyListener(ModifyListener l) {
    fComments.addModifyListener(l);
  }
  
  public final String getSessionName() {
    return fName.getText();
  }
  public final void setSessionNameEditable(boolean editable) {
    fName.setEditable(editable);
  }

  /**
   * Updates and returns the <code>Properties</code> object that contains the
   * session properties.
   * 
   * @return the session properties
   */
  public Properties getSessionProperties() {
    //fProperties.clear();
    fProperties.setProperty("Name", fName.getText());
    fProperties.setProperty("Time", fTime.getText());
    fProperties.setProperty("Temperature", fTemperature.getText());
    fProperties.setProperty("Equipment", fEquipment.getText());
    fProperties.setProperty("Comments", fComments.getText());
    return fProperties;
  }

  public void loadProperties(InputStream is) {
    try {
      fProperties.load(is);
    } catch (IOException e) {
      e.printStackTrace();
    }
    fName.setText(fProperties.getProperty("Name","noname"));
    fTime.setText(fProperties.getProperty("Time",""));
    fTemperature.setText(fProperties.getProperty("Temperature",""));
    fEquipment.setText(fProperties.getProperty("Equipment",""));
    fComments.setText(fProperties.getProperty("Comments",""));
  }
  
  // optional information
  public void createSetListControl(IContainer sessionFolder) {
    Composite body = form.getBody();
    GridLayout gridLayout = (GridLayout)body.getLayout();
    gridLayout.numColumns = 2;

    Composite bodyRight = toolkit.createComposite(form.getBody());
    gridLayout = new GridLayout(1, false);
    gridLayout.verticalSpacing = 10;
    bodyRight.setLayout(gridLayout);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    bodyRight.setLayoutData(gridData);
    toolkit.paintBordersFor(bodyRight);
    
    Section section = toolkit.createSection(bodyRight, Section.TITLE_BAR);
    section.setText("Sets");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    section.setLayoutData(gridData);

    FormText tform = toolkit.createFormText(bodyRight, true);
    String[] s = MeasurementProject.listSets(sessionFolder);
    gridData = new GridData(GridData.FILL_BOTH);
    tform.setLayoutData(gridData);
    StringBuffer sb = new StringBuffer();
    sb.append("<form>");
    for (String str: s) {
      sb.append("<li>" + str + "</li>");
    }
    sb.append("</form>");
    tform.setText(sb.toString(), true, false);
    
    body.layout();
  }
  
}
