/*
 *  ReverberationTimeActionDelegate.java
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

import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;

public class ReverberationTimeActionDelegate implements IWorkbenchWindowActionDelegate{

  private IStructuredSelection _sel;

  public void run(IAction action) {
    URL uSur = AcmusPlugin.getDefault().getBundle().getEntry("data/coefsurfaces.txt");
    URL uObj = AcmusPlugin.getDefault().getBundle().getEntry("data/coefobjects.txt");

    Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
    Shell shell = new Shell(d);
    shell.setLayout(new GridLayout(1, false));
    ReverberationTime rt = new ReverberationTime(shell, shell, SWT.NONE, readCoefficients(uSur), readCoefficients(uObj));    
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 700;
    gridData.widthHint = 840;
    rt.setLayoutData(gridData);
    
    shell.setText("Reverberation Time");
    shell.setImage(AcmusGraphics.IMG_APP_ICON);
    shell.pack();
    
    shell.open();    
  }

  public void selectionChanged(IAction action, ISelection selection) {
    _sel = (IStructuredSelection)selection;
  }

  public void dispose() {
  }

  public void init(IWorkbenchWindow window) {
  }

  public Map<String, Map<String,double[]>> readCoefficients(URL url) {
    Map<String, Map<String,double[]>> res = new HashMap<String, Map<String,double[]>>();
    
    try {
      TextReader tr = new TextReader(new InputStreamReader(url.openStream(), "ISO-8859-1"));

      String line = tr.readLine();
      Map<String,double[]> map = null;
      while (line != null) {
        if (line.trim().startsWith("$")) {
          String type = line.trim().substring(1).trim();
          if (res.containsKey(type)) {
            map = res.get(type);
          }
          else {
            map = new HashMap<String,double[]>();
            res.put(type, map);
          }
        }
        else {
          StringTokenizer st = new StringTokenizer(line, ";");
//          if (st.countTokens() != 10) {
//            throw new Exception("error: parse error in coefficients file: " + line + " " + st.countTokens());
//          }
          String material = st.nextToken();
          st.nextToken(); // ESP
          st.nextToken(); // NRC
          double[] coefs = new double[6];
          for (int i = 0; i < coefs.length; i++) {
            String tok = st.nextToken();
            if ("".equals(tok.trim()))
              coefs[i] = 0;
            else
              coefs[i] = Double.parseDouble(tok);
          }
          map.put(material, coefs);
        }
        line = tr.readLine();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return res;
  }
}
