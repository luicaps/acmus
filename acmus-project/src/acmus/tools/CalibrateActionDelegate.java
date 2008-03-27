package acmus.tools;

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

import acmus.AcmusPlugin;
import acmus.audio.Calibrate;

public class CalibrateActionDelegate implements IWorkbenchWindowActionDelegate{

  private IStructuredSelection _sel;

  public void run(IAction action) {
    Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
    Shell shell = new Shell(d);
    shell.setLayout(new GridLayout(1, false));
    Calibrate cw = new Calibrate(shell, SWT.NONE);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 100;
    gridData.widthHint = 30;
    cw.setLayoutData(gridData);
    
    shell.setText("Calibrate");
    shell.pack();
    
    
    shell.open();    
    
  }

  public void selectionChanged(IAction action, ISelection selection) {
    _sel = (IStructuredSelection)selection;
  }

  public void dispose() {
    // TODO Auto-generated method stub
  }

  public void init(IWorkbenchWindow window) {
    
  }

}
