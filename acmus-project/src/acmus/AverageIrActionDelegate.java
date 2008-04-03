package acmus;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import acmus.dsp.Util;
import acmus.wizard.AcmusMeasurementWizard;

public class AverageIrActionDelegate implements IWorkbenchWindowActionDelegate {

	private IStructuredSelection _sel;

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	public void run(IAction action) {
		IFolder set = (IFolder) _sel.getFirstElement();

		IFolder takes[] = MeasurementProject.getTakes(set);

		List<String> irFiles = new ArrayList<String>();

		for (IFolder f : takes) {
			if (!f.getName().startsWith("Average")) {
				IFile irFile = f.getFile("ir.wav");
				if (irFile.exists())
					irFiles.add(irFile.getLocation().toOSString());
			}
		}

		if (irFiles.size() > 1) {
			IFolder avgFolder = set.getFolder("Average.msr");
			if (avgFolder.exists()) {
				try {
					avgFolder.delete(true, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Properties props = new Properties();
			props.setProperty("Name", "Average");
			AcmusMeasurementWizard.createMeasurement(set, props);

			Util.wavAverage(avgFolder.getLocation().toOSString() + "/ir.wav",
					32, irFiles);
			try {
				avgFolder.refreshLocal(IFolder.DEPTH_ONE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else {
			// Show error message..
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		_sel = (IStructuredSelection) selection;
	}

}
