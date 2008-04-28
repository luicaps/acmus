package acmus.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

public class ImportIRWizard extends AcmusMeasurementWizard {

	private ImpulsiveResponseImportPage importPage;
	private IStructuredSelection selection;

	@Override
	public void addPages() {
		super.addPages();
		importPage = new ImpulsiveResponseImportPage(
				"ImpulsiveResponseImportPage");
		addPage(importPage);
	}

	@Override
	public boolean performFinish() {
		try {
			IFolder set = (IFolder) selection.getFirstElement();
			Properties props = mainPage.getMeasurementProperties();
			IFolder folder = set.getFolder(props.getProperty("Name") + ".msr");
			if (!folder.exists()) {
				folder.create(true, true, null);
			}
			File file = new File(importPage.getFileName());
			FileInputStream inputStream = new FileInputStream(file);
			IFile ir = folder.getFile("ir.wav");
			ir.create(inputStream, true, null);
			
			props.setProperty("recording", "false");
			AcmusMeasurementWizard.createMeasurement(set, props);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		super.init(workbench, selection);
		setWindowTitle("Import IR");
	}

}
