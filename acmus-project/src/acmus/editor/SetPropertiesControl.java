/*
 *  SetPropertiesControl.java
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

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import acmus.AcmusGraphics;
import acmus.MeasurementProject;

public class SetPropertiesControl extends Composite {

  Text fName;
  Combo fSource;
  Combo fMic;
  Text fComments;

  Label fProjectName;
  Label fSessionName;

  MultiplePositionEditorControl _posChooser;

  Properties fProperties;

  String _floorPlanId;

  Color _bgColorTitle = AcmusGraphics.LIGHT_BLUE;
  Color _bgColor = AcmusGraphics.WHITE;

  private FormToolkit toolkit;
  private Form form;

  String _projectName;
  String _sessionName;

  FormText _names;
  Composite bodyRight;

  public SetPropertiesControl(Composite parent, int style, String posFile) {
    super(parent, style);
    fProperties = new Properties();
    FillLayout fl = new FillLayout();
    fl.marginHeight = 1;
    fl.marginWidth = 1;
    setLayout(fl);
    setBackground(AcmusGraphics.BLACK);

    GridLayout gridLayout;
    GridData gridData;
    Label l;

    toolkit = new FormToolkit(AcmusGraphics.FORMCOLORS);
    form = toolkit.createForm(this);

    form.setText("Set Properties");
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
    gridLayout = new GridLayout(2, false);
    gridLayout.verticalSpacing = 10;
    bodyLeft.setLayout(gridLayout);
    gridData = new GridData(GridData.FILL_BOTH);
    bodyLeft.setLayoutData(gridData);
    toolkit.paintBordersFor(bodyLeft);

    bodyRight = toolkit.createComposite(form.getBody());
    gridLayout = new GridLayout(1, false);
    gridLayout.verticalSpacing = 10;
    bodyRight.setLayout(gridLayout);
    gridData = new GridData();
    gridData.heightHint=0;
    gridData.widthHint=0;
    bodyRight.setLayoutData(gridData);
    toolkit.paintBordersFor(bodyRight);
    
    l = toolkit.createLabel(bodyLeft, "Set:");
    fName = toolkit.createText(bodyLeft, "");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    fName.setLayoutData(gridData);

    l = toolkit.createLabel(bodyLeft, "Comments:");
    gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    l.setLayoutData(gridData);
    fComments = toolkit.createText(bodyLeft, "", SWT.MULTI);
    gridData = new GridData(GridData.FILL_BOTH);
    fComments.setLayoutData(gridData);

    Composite bodyBottom = toolkit.createComposite(body);
    gridLayout = new GridLayout(4, false);
    gridLayout.verticalSpacing = 10;
    bodyBottom.setLayout(gridLayout);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 2;
    bodyBottom.setLayoutData(gridData);
    toolkit.paintBordersFor(bodyBottom);

    Section section = toolkit.createSection(bodyBottom, Section.TITLE_BAR);
    section.setText("Positions");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 4;
    section.setLayoutData(gridData);
    
    SelectionListener sl = new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        Combo c = (Combo) e.widget;
        _posChooser.select(c.getSelectionIndex());
      }
    };

    l = toolkit.createLabel(bodyBottom, "Source:");
    fSource = new Combo(bodyBottom, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
    toolkit.adapt(fSource);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    fSource.setLayoutData(gridData);
    fSource.addSelectionListener(sl);

    l = toolkit.createLabel(bodyBottom, "Mic:");
    fMic = new Combo(bodyBottom, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
    toolkit.adapt(fMic);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    fMic.setLayoutData(gridData);
    fMic.addSelectionListener(sl);

    _posChooser = new MultiplePositionEditorControl(bodyBottom, SWT.NONE, null,
        true);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 4;
    gridData.widthHint = 400;
    gridData.heightHint = 300;
    _posChooser.setLayoutData(gridData);
    try {
      _posChooser.read(new FileReader(posFile));
    } catch (Exception e) {
      e.printStackTrace();
    }
    _posChooser.setFloorplanSelectionListener(new SelectionListener() {

      public void widgetDefaultSelected(SelectionEvent e) {
      }

      public void widgetSelected(SelectionEvent e) {
        Combo c = (Combo) e.widget;
        updatePositions(c.getSelectionIndex() + "");
      }

    });
    updatePositions("0");
  }

  public void updateNames(String project, String session) {
    _names.setText("<form><p>project: <b>" + project + "</b>  session: <b>"
        + session + "</b></p><p></p></form>", true, false);
  }

  public void setProjectName(String name) {
    _projectName = name;
    updateNames(_projectName, _sessionName);
  }

  public void setSessionName(String name) {
    _sessionName = name;
    updateNames(_projectName, _sessionName);
  }

  public void addNameModifyListener(ModifyListener l) {
    fName.addModifyListener(l);
  }

  public void addSourceModifyListener(ModifyListener l) {
    fSource.addModifyListener(l);
  }

  public void addSourceListener(int eventType, Listener l) {
    // fSource.addListener(eventType, l);
  }

  public void addMicModifyListener(ModifyListener l) {
    fMic.addModifyListener(l);
  }

  public void addMicListener(int eventType, Listener l) {
    // fMic.addListener(eventType, l);
  }

  public void addCommentsModifyListener(ModifyListener l) {
    fComments.addModifyListener(l);
  }

  public final String getSetName() {
    return fName.getText();
  }

  public final void setSetNameEditable(boolean editable) {
    fName.setEditable(editable);
  }

  /**
   * Updates and returns the <code>Properties</code> object that contains the
   * session properties.
   * 
   * @return the session properties
   */
  public Properties getSetProperties() {
    // fProperties.clear();
    fProperties.setProperty("Name", fName.getText());
    fProperties.setProperty("Floorplan", _floorPlanId + "");
    fProperties.setProperty("Mic", fMic.getSelectionIndex() + "");
    fProperties.setProperty("Source", fSource.getSelectionIndex() + "");
    fProperties.setProperty("Comments", fComments.getText());
    return fProperties;
  }

  public void loadProperties(InputStream is) {
    try {
      fProperties.load(is);
    } catch (IOException e) {
      e.printStackTrace();
    }
    fName.setText(fProperties.getProperty("Name", "noname"));
    String fpId = fProperties.getProperty("Floorplan", "0");
    _posChooser.selectFloorPlan(Integer.parseInt(fpId));
    updatePositions(fpId);
    fMic.select(Integer.parseInt(fProperties.getProperty("Mic", "0")));
    fSource.select(Integer.parseInt(fProperties.getProperty("Source", "0")));
    // fMic.select(findItem(fMic,fProperties.getProperty("Mic","")));
    // fSource.select(findItem(fSource,fProperties.getProperty("Source","")));
    fComments.setText(fProperties.getProperty("Comments", ""));
  }

//  private int findItem(Combo c, String item) {
//    int res = 0;
//    for (int i = 0; i < c.getItemCount(); i++) {
//      if (c.getItem(i).equals(item)) {
//        res = i;
//        break;
//      }
//    }
//    return res;
//  }

  // optional information
  public void createTakeListControl(IContainer sessionFolder) {
    Composite body = form.getBody();
    GridLayout gridLayout = (GridLayout) body.getLayout();
    gridLayout.numColumns = 2;

    //GridData gridData = (GridData)bodyRight.getLayoutData();
//    gridData.heightHint = SWT.DEFAULT;
//    gridData.widthHint = SWT.DEFAULT;
    GridData gridData = new GridData(GridData.FILL_BOTH);
    bodyRight.setLayoutData(gridData);
    

    Section section = toolkit.createSection(bodyRight, Section.TITLE_BAR);
    section.setText("Takes");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    section.setLayoutData(gridData);

    FormText tform = toolkit.createFormText(bodyRight, true);
    String[] s = MeasurementProject.listTakes(sessionFolder);
    gridData = new GridData(GridData.FILL_BOTH);
    tform.setLayoutData(gridData);
    StringBuffer sb = new StringBuffer();
    sb.append("<form>");
    for (String str : s) {
      sb.append("<li>" + str + "</li>");
    }
    sb.append("</form>");
    tform.setText(sb.toString(), true, false);

    body.layout();
  }

  private void updatePositions(String floorPlanId) {
    System.out.println("  " + floorPlanId);
    _floorPlanId = floorPlanId;
    int mic = fMic.getSelectionIndex();
    int source = fSource.getSelectionIndex();

    fMic.removeAll();
    fSource.removeAll();
    Iterator<Position> it = _posChooser.iterator(_floorPlanId);
    while (it.hasNext()) {
      Position p = it.next();
      fMic.add(p.id() + " " + p.name());
      fSource.add(p.id() + " " + p.name());
    }
    if (fSource.getItemCount() > source)
      fSource.select(source);
    else
      fSource.select(fSource.getItemCount() - 1);
    if (fMic.getItemCount() > mic)
      fMic.select(mic);
    else
      fMic.select(fMic.getItemCount() - 1);
  }

}
