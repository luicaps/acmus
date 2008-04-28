package acmus.wizard.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FileBrowseInput extends Composite {

	private FileDialog fileDialog;
	private Button browse;
	private Text fileName;
	private Label label;

	public FileBrowseInput(Composite parent, int style) {
		super(parent, style);
		fileDialog = new FileDialog(parent.getShell(), style);

		GridLayout layout = new GridLayout(3, false);
		this.setLayout(layout);

		label = new Label(this, SWT.LEFT);
		label.setText("Filename:");

		fileName = new Text(this, SWT.BORDER);
		fileName.setLayoutData(new GridData(400, 20));

		browse = new Button(this, style);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				String fileChosen = fileDialog.open();
				if (fileChosen != null) {
					fileName.setText(fileChosen);
				}

			}

		});
	}

	
	public void addModifyListener(ModifyListener listener) {
		fileName.addModifyListener(listener);
	}


	public void setFilterExtensions(String[] extensions) {
		fileDialog.setFilterExtensions(extensions);
	}

	public String getFileName() {
		return fileName.getText();
	}

	public void setLabelText(String text) {
		label.setText(text);
	}

}
