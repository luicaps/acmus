package acmus.editor.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import acmus.editor.MeasurementEditor;

public class ParametersPage extends Composite {

	private final MeasurementEditor parent;
	private int _parametersIndex;

	public ParametersPage(MeasurementEditor parent, int style) {
		super(parent.getContainer(), style);
		this.parent = parent;
		createParametersPage();
	}

	public void createParametersPage() {
		try {
			if (!parent.getParamsFile().exists() || !parent.getSchroederFolder().exists()) {
				parent.getImpulseResponsePage().calculateParameters();
			}
	
			Composite page = new Composite(parent.getContainer(), SWT.NONE);
			this._parametersIndex = parent.addPage(page);
			parent.setPageText(this._parametersIndex, "Parameters");
			page.setLayout(new GridLayout(1, false));
	
			// GridData gridData;
	
			parent.createTableViewer(page);
	
			Label lBr = new Label(page, SWT.NONE);
			Label lTr = new Label(page, SWT.NONE);
			Label lItgd = new Label(page, SWT.NONE);
	
			BufferedReader br = new BufferedReader(new FileReader(parent.getParamsFile()
					.getLocation().toOSString()));
			String line = null;
	
			line = br.readLine();
			line = br.readLine();
			while (!line.trim().equals("")) {
				StringTokenizer st = new StringTokenizer(line);
				String name = st.nextToken() + " " + st.nextToken();
				double[] ch = new double[11];
				for (int i = 0; i < ch.length; i++) {
					String s = st.nextToken();
					if (s.equals("?")) {
						ch[i] = Double.NaN;
					} else {
						ch[i] = Double.parseDouble(s);
					}
				}
				parent.fTPos.add(parent.new Parameter(name, ch));
				line = br.readLine();
			}
	
			while (line.trim().equals(""))
				line = br.readLine();
	
			lBr.setText(line);
			lTr.setText(br.readLine());
			lItgd.setText(br.readLine() + " ms");
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setIndex(int _parametersIndex) {
		this._parametersIndex = _parametersIndex;
	}

	public int getIndex() {
		return this._parametersIndex;
	}
}
