/**
 * 
 */
package acmus.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import acmus.wizard.widgets.FileBrowseInput;

/**
 * @author lucascs
 *
 */
public class ImpulsiveResponseImportPage extends WizardPage {

	private FileBrowseInput dialog;

	protected ImpulsiveResponseImportPage(String pageName) {
		super(pageName);
		setTitle("Import Impulsive Response");
		setDescription("Select IR from a WAV file");
		setPageComplete(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = 0;
		fillLayout.marginWidth = 0;
		composite.setLayout(fillLayout);
		composite.setFont(parent.getFont());
		
		dialog = new FileBrowseInput(composite, SWT.NONE);
		dialog.setFilterExtensions(new String[] {"*.wav", "*.wave"});
		
		setPageComplete(true);
		setControl(composite);
	}

	public String getFileName() {
		return dialog.getFileName();
	}
}
