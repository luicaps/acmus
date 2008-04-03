/*
 *  MeasurementPropertiesControl.java
 *  This file is part of AcMus.
 *  
 *  AcMus: Tools for Measurement, Analysis, and Simulation of Room Acoustics
 *  
 *  Copyright (C) 2006 Leo Ueda, Bruno Masiero
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package acmus.editor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import acmus.AcmusGraphics;

public class MeasurementPropertiesControl extends Composite {

	Text fName;
	Text fComments;

	Properties fProperties;

	String _projectName;
	String _sessionName;
	String _setName;

	FormText _names;

	private FormToolkit toolkit;
	private ScrolledForm form;

	public MeasurementPropertiesControl(Composite parent, int style,
			String suggestedName) {
		super(parent, style);
		fProperties = new Properties();
		FillLayout fl = new FillLayout();
		fl.marginHeight = 1;
		fl.marginWidth = 1;
		setLayout(fl);
		setBackground(AcmusGraphics.BLACK);

		toolkit = new FormToolkit(AcmusGraphics.FORMCOLORS);
		form = toolkit.createScrolledForm(this);

		form.setText("Take Properties");
		form.setBackgroundImage(AcmusGraphics.IMG_FORMBANNER);

		GridData gridData;
		Label l;

		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 10;
		layout.marginWidth = 10;
		Composite body = form.getBody();
		body.setLayout(layout);

		_names = toolkit.createFormText(form.getBody(), true);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		_names.setLayoutData(gridData);

		l = toolkit.createLabel(body, "Name:");
		fName = toolkit.createText(body, "");
		fName.setText(suggestedName);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fName.setLayoutData(gridData);
		toolkit.paintBordersFor(body);

		l = toolkit.createLabel(body, "Comments:");
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		l.setLayoutData(gridData);
		fComments = toolkit.createText(body, "", SWT.MULTI);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		fComments.setLayoutData(gridData);
	}

	public void updateNames(String project, String session, String set) {
		_names.setText("<form><p>project: <b>" + project + "</b>  session: <b>"
				+ session + "</b>  set: <b>" + set + "</b></p><p></p></form>",
				true, false);
	}

	public void setProjectName(String name) {
		_projectName = name;
		updateNames(_projectName, _sessionName, _setName);
	}

	public void setSessionName(String name) {
		_sessionName = name;
		updateNames(_projectName, _sessionName, _setName);
	}

	public void setSetName(String name) {
		_setName = name;
		updateNames(_projectName, _sessionName, _setName);
	}

	public void addNameModifyListener(ModifyListener l) {
		fName.addModifyListener(l);
	}

	public void addCommentsModifyListener(ModifyListener l) {
		fComments.addModifyListener(l);
	}

	public final String getMeasurementName() {
		return fName.getText();
	}

	public final void setMeasurementNameEditable(boolean editable) {
		fName.setEditable(editable);
	}

	public Properties getMeasurementProperties() {
		fProperties.setProperty("Name", fName.getText());
		fProperties.setProperty("Comments", fComments.getText());
		return fProperties;
	}

	public void loadProperties(InputStream is) {
		try {
			fProperties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		fName.setText(fProperties.getProperty("Name", "noname"));
		fComments.setText(fProperties.getProperty("Comments", ""));
	}

	/**
	 * Disposes the toolkit
	 */
	public void dispose() {
		toolkit.dispose();
		super.dispose();
	}

}
