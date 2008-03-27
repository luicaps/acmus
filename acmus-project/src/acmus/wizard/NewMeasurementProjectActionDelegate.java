/*
 *  NewMeasurementProjectActionDelegate.java
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
package acmus.wizard;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;

import acmus.AcmusPlugin;

public class NewMeasurementProjectActionDelegate  implements IObjectActionDelegate{

  private IWorkbenchPart part;
  private IStructuredSelection sel;

  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    this.part = targetPart;
  }

  protected INewWizard createWizard() {
    return new AcmusMeasurementProjectWizard();
  }
  
  public void run(IAction action) {
    
    // Create the wizard
    INewWizard wizard = createWizard();
    wizard.init(getWorkbench(), sel);

    // Create the wizard dialog
    WizardDialog dialog = new WizardDialog
       (getWorkbench().getActiveWorkbenchWindow().getShell(),wizard);
    // Open the wizard dialog
    dialog.open();
    
  }

  public void selectionChanged(IAction action, ISelection selection) {
    sel = (IStructuredSelection)selection;
  }
  
  private final IWorkbench getWorkbench() {
    return AcmusPlugin.getDefault().getWorkbench();
  }

}
