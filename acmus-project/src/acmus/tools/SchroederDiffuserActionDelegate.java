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

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;

public class SchroederDiffuserActionDelegate implements IWorkbenchWindowActionDelegate{

  private IStructuredSelection _sel;

  public void run(IAction action) {
    Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
    Shell shell = new Shell(d);
    shell.setLayout(new GridLayout(1, false));
    SchroederDiffuser sd = new SchroederDiffuser(shell, SWT.NONE);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 420;
    gridData.widthHint = 650;
    sd.setLayoutData(gridData);
    
    shell.setText("Schroeder Diffuser");
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

}
