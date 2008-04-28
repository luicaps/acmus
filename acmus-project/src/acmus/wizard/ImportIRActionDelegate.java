package acmus.wizard;

import org.eclipse.ui.INewWizard;

public class ImportIRActionDelegate extends NewMeasurementProjectActionDelegate {

	protected INewWizard createWizard() {
		return new ImportIRWizard();
	}
}
