package acmus.editor;

import java.io.FileReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

public class ProjectEditor extends MultiPageEditorPart {
	// ProjectPropertiesEditor _propsEditor;
	// PositionEditor _positionEditor;

	private ProjectPropertiesControl _propsControl;
	private MultiplePositionEditorControl _posControl;

	private int _propsIndex;
	private int _posIndex;

	private FileEditorInput _input;

	private String _posFilename;
	private IFile _posFile;

	private boolean _isDirty;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		_input = (FileEditorInput) input;
		setPartName(_input.getFile().getProject().getName());
		_posFile = _input.getFile().getProject().getFile("project.positions");
		_posFilename = _posFile.getLocation().toOSString();
	}

	@Override
	protected void createPages() {
		createPropsPage();
		createPosPage();
	}

	protected void createPropsPage() {
		Composite c = new Composite(getContainer(), SWT.NONE);

		c.setLayout(new GridLayout(1, false));

		_propsControl = new ProjectPropertiesControl(c, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		_propsControl.setLayoutData(gridData);

		try {
			_propsControl.loadProperties(_input.getFile().getContents());
		} catch (CoreException e) {
			e.printStackTrace();
		}

		ModifyListener ml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true);
			}
		};

		_propsControl.addLocationModifyListener(ml);
		_propsControl.addDateModifyListener(ml);
		_propsControl.addCommentsModifyListener(ml);
		_propsControl.addIrLengthModifyListener(ml);
		_propsControl.addRecExtraModifyListener(ml);

		_propsIndex = addPage(c);
		setPageText(_propsIndex, "Properties"); //$NON-NLS-1$
	}

	protected void createPosPage() {
		Composite c = new Composite(getContainer(), SWT.NONE);

		c.setLayout(new GridLayout(1, false));

		IDocumentListener editorDocListener = new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {

			}

			public void documentChanged(DocumentEvent event) {
				setDirty(true);
			}
		};

		_posControl = new MultiplePositionEditorControl(c, SWT.NONE,
				editorDocListener, false);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		_posControl.setLayoutData(gridData);
		try {
			_posControl.read(new FileReader(_posFilename));
		} catch (Exception e) {
			e.printStackTrace();
		}
		_posIndex = addPage(c);
		setPageText(_posIndex, "Positions"); //$NON-NLS-1$
	}

	@Override
	public boolean isDirty() {
		return _isDirty;
	}

	public void setDirty(boolean dirty) {
		if (_isDirty != dirty) {
			_isDirty = dirty;
			firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		ProjectPropertiesEditor.save(_propsControl, _input.getFile(), monitor);
		PositionEditor.save(_posControl, _posFile, monitor);

		setDirty(false);
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
