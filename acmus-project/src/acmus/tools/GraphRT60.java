/*
 *  GraphRT60.java
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
 * GraphRT60.java
 * Created on 11/07/2005
 */
package acmus.tools;

import java.text.DecimalFormat;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;

/**
 * @author lku
 */
public class GraphRT60 implements IWorkbenchWindowActionDelegate {

  private String[] material1 = { "Concrete-unpainted", "Concrete-painted",
      "Brick", "Plaster on lath", "Plywood paneling", "Drapery-lightwt",
      "Drapery-heavywt", "Terrazzo", "Wood floor", "Carpet on concrete",
      "Carpet on pad", "Ac. tile suspended", "Ac. tile on concrete",
      "Gypsum  board", "Glass-large panes", "Glass-windows", "Marble or Tile",
      "Water Surface" };
  private String[] material2 = { "Glass-windows", "Concrete-unpainted",
      "Concrete-painted", "Brick", "Plaster on lath", "Plywood paneling",
      "Drapery-lightwt", "Drapery-heavywt", "Terrazzo", "Wood floor",
      "Carpet on concrete", "Carpet on pad", "Ac. tile suspended",
      "Ac. tile on concrete", "Gypsum  board", "Glass-large panes",
      "Marble or Tile", "Water Surface" };

  Shell shell;

  private double constant = 0.161;
  private double w = 0.0;
  private double l = 0.0;
  private double h = 0.0;
  private String material_c;
  private String material_fw;
  private String material_bw;
  private String material_lw;
  private String material_rw;
  private String material_f;
  private String material_w1;
  private String material_w2;
  private String material_w3;
  private String material_w4;
  private Text width;
  private Text length;
  private Text height;
//  private Button meter;
//  private Button foot;
  private Text a_c;
  private Text a_fw;
  private Text a_bw;
  private Text a_lw;
  private Text a_rw;
  private Text a_f;
  private Text w_fw;
  private Text w_bw;
  private Text w_lw;
  private Text w_rw;
  private Combo ceiling;
  private Combo frontwall;
  private Combo backwall;
  private Combo leftwall;
  private Combo rightwall;
  private Combo floor;
  private Combo window_fw;
  private Combo window_bw;
  private Combo window_lw;
  private Combo window_rw;
  private double area_c;
  private double area_fw;
  private double area_bw;
  private double area_lw;
  private double area_rw;
  private double area_f;

  private Histogram histogram;
  static String[] histLabels = { "125", "250", "500", "1000", "2000", "4000",
      "" };
  static double[] histDataX = { 1.0, 2, 3, 4, 5, 6 };

  MessageBox inputErrorDialog;

  SelectionAdapter computeSurfacesListener = new SelectionAdapter() {
    public void widgetSelected(SelectionEvent event) {
      try {
        w = Double.parseDouble(width.getText());
        l = Double.parseDouble(length.getText());
        h = Double.parseDouble(height.getText());
        System.out.println(w + " " + l + " " + h);
        area_c = w * l;
        area_f = w * l;
        area_fw = l * h;
        area_bw = l * h;
        area_lw = w * h;
        area_rw = w * h;
        System.out.println(w * l + " " + w * h + " " + h * l);
        a_c.setText("" + area_c);
        a_f.setText("" + area_f);
        a_fw.setText("" + area_fw);
        a_bw.setText("" + area_bw);
        a_lw.setText("" + area_lw);
        a_rw.setText("" + area_rw);
      } catch (Exception exception) {
        inputErrorDialog.open();
      }
    }

  };// computeSurfaceListener

  SelectionAdapter drawGraphListener = new SelectionAdapter() {
    public void widgetSelected(SelectionEvent event) {
      double a1 = 0.0;
      double a2 = 0.0;
      double a3 = 0.0;
      double a4 = 0.0;
      material_c = material1[ceiling.getSelectionIndex()];
      material_fw = material1[frontwall.getSelectionIndex()];
      material_bw = material1[backwall.getSelectionIndex()];
      material_lw = material1[leftwall.getSelectionIndex()];
      material_rw = material1[rightwall.getSelectionIndex()];
      material_f = material1[floor.getSelectionIndex()];
      material_w1 = material2[window_fw.getSelectionIndex()];
      material_w2 = material2[window_bw.getSelectionIndex()];
      material_w3 = material2[window_lw.getSelectionIndex()];
      material_w4 = material2[window_rw.getSelectionIndex()];
      try {
        a1 = Math.abs(Double.parseDouble(w_fw.getText()));
        a2 = Math.abs(Double.parseDouble(w_bw.getText()));
        a3 = Math.abs(Double.parseDouble(w_lw.getText()));
        a4 = Math.abs(Double.parseDouble(w_rw.getText()));
        CalculateRT60 cal = new CalculateRT60(area_c, material_c, area_fw,
            material_fw, area_bw, material_bw, area_lw, material_lw, area_rw,
            material_rw, area_f, material_f, a1, material_w1, a2, material_w2,
            a3, material_w3, a4, material_w4, constant, w, l, h);
        cal.calculateRT60();
        double max = cal.time[0];
        double dataY[] = new double[6];
        DecimalFormat format = new DecimalFormat("#.##");
        String topLabels[] = new String[6];
        for (int i = 0; i < 6; i++) {
          dataY[i] = cal.time[i];
          topLabels[i] = format.format(dataY[i]);
          if (cal.time[i] > max)
            max = cal.time[i];
        }
        if (max == 0.0)
          inputErrorDialog.open();
        histogram.setData(histDataX, dataY, topLabels);
      } catch (Exception exception) {
        inputErrorDialog.open();
      }
    }
  };

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {
    // TODO Auto-generated method stub
    System.out.println("dispose");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
    // FIXME: find a way to create shell only once...
    // createShell();
  }

  private void createShell() {
    Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
    shell = new Shell(d);

    shell.setText("Reverberation time graph");

    shell.setLayout(new GridLayout(1, false));
    GridData gridData;

    Group g = new Group(shell, SWT.SHADOW_ETCHED_IN);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    g.setLayoutData(gridData);
    g.setLayout(AcmusGraphics.newGridLayout(1, false, 5, 0, 5, 0));
    g.setText("Choose dimensions");

    Composite c2 = new Composite(g, SWT.NONE);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.LEFT;
    c2.setLayoutData(gridData);
    c2.setLayout(new GridLayout(2, false));

    Composite c = new Composite(c2, SWT.NONE);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    c.setLayoutData(gridData);
    c.setLayout(AcmusGraphics.newNoMarginGridLayout(2, false));

    width = AcmusGraphics.newText(c, "Width");
    length = AcmusGraphics.newText(c, "Length");
    height = AcmusGraphics.newText(c, "Height");

    c = new Composite(c2, SWT.NONE);
    gridData = new GridData(SWT.NONE);
    gridData.horizontalAlignment = SWT.LEFT;
    c.setLayoutData(gridData);
    c.setLayout(AcmusGraphics.newGridLayout(1, false, 0, 10, 5, 0));

    Label label = new Label(c, SWT.LEFT);
    label.setText("Units:");
    Button meter = new Button(c, SWT.RADIO);
    meter.setText("meters");
    meter.setSelection(true);
    // meter.addActionListener(new Meter());
    Button foot = new Button(c, SWT.RADIO);
    foot.setText("feet");
    // foot.addActionListener(new Foot());

    Button button1 = new Button(c2, SWT.NONE);
    button1.setText("Compute Surface Areas");
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    gridData.horizontalSpan = 2;
    button1.setLayoutData(gridData);
    button1.addSelectionListener(computeSurfacesListener);

    g = new Group(shell, SWT.SHADOW_ETCHED_IN);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    g.setLayoutData(gridData);
    g.setLayout(new GridLayout(8, false));
    g.setText("Enter surfaces areas and materials");

    a_c = AcmusGraphics.newText(g, "Ceiling");
    ceiling = AcmusGraphics.newCombo(g, "Material", material1);
    // label = new Label(g, SWT.LEFT);
    // label.setText("Enter areas:");
    label = new Label(g, SWT.NONE);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 4;
    label.setLayoutData(gridData);

    a_fw = AcmusGraphics.newText(g, "First wall");
    frontwall = AcmusGraphics.newCombo(g, "Material", material1);
    w_fw = AcmusGraphics.newText(g, "Window");
    window_fw = AcmusGraphics.newCombo(g, "Material", material2);
    a_bw = AcmusGraphics.newText(g, "Second wall");
    backwall = AcmusGraphics.newCombo(g, "Material", material1);
    w_bw = AcmusGraphics.newText(g, "Window");
    window_bw = AcmusGraphics.newCombo(g, "Material", material2);
    a_lw = AcmusGraphics.newText(g, "Third wall");
    leftwall = AcmusGraphics.newCombo(g, "Material", material1);
    w_lw = AcmusGraphics.newText(g, "Window");
    window_lw = AcmusGraphics.newCombo(g, "Material", material2);
    a_rw = AcmusGraphics.newText(g, "Fourth wall");
    rightwall = AcmusGraphics.newCombo(g, "Material", material1);
    w_rw = AcmusGraphics.newText(g, "Window");
    window_rw = AcmusGraphics.newCombo(g, "Material", material2);
    a_f = AcmusGraphics.newText(g, "Floor");

    floor = AcmusGraphics.newCombo(g, "Material", material1);
    label = new Label(g, SWT.NONE);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 4;
    label.setLayoutData(gridData);

    Button button2 = new Button(g, SWT.NONE);
    button2.setText("Draw Graph");
    button2.addSelectionListener(drawGraphListener);

    // button3.setEnabled(false);
    // button3.addActionListener(new Button3());
    // add(button3, gbl, gbc, 0, 11, 2, 1);

    g = new Group(shell, SWT.SHADOW_ETCHED_IN);
    gridData = new GridData(GridData.FILL_BOTH);
    g.setLayoutData(gridData);
    g.setLayout(AcmusGraphics.newPosGridLayout(1, false));
    g.setText("Graph");

    histogram = new Histogram(g, SWT.NONE, "Hz", "time(s)", histLabels);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 180;
    histogram.setLayoutData(gridData);

    inputErrorDialog = new MessageBox(shell, SWT.ICON_ERROR);
    inputErrorDialog.setMessage("Please check the input data.");

    shell.pack();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    createShell();
    shell.open();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    // TODO Auto-generated method stub

  }

}