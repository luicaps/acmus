/**
 * CompareWaveformsActionDelegate.java
 * Created on 13/02/2006
 */
package acmus.tools;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import acmus.AcmusGraphics;

/**
 * @author lku
 *
 */
public class CompareWaveformsActionDelegate implements IWorkbenchWindowActionDelegate{

  private IStructuredSelection _sel;
  private Display _display;

  public void run(IAction action) {
    //Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
    Shell shell = new Shell(_display);
    shell.setLayout(new FillLayout());
    CompareWaveforms cw = new CompareWaveforms(shell, shell, SWT.NONE);
    
    for (Object o : _sel.toList()) {
      IFile f = (IFile)o;
      cw.open(f);
    }
    
    shell.setText("Compare Waveforms");
    shell.setImage(AcmusGraphics.IMG_APP_ICON);
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
    _display = window.getWorkbench().getDisplay();
  }

}
