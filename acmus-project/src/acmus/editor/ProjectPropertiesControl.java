/*
 *  ProjectPropertiesControl.java
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import acmus.AcmusGraphics;
import acmus.util.Algorithms;

public class ProjectPropertiesControl extends Composite {

	Text fProjectLocation;
	Text fProjectDate;
	Text fProjectComments;
	Text fIrLength;
	Text fRecExtra;

	Properties fProperties;

	private FormToolkit toolkit;
	private ScrolledForm form;

	public ProjectPropertiesControl(Composite parent, int style) {
		super(parent, style);
		setFont(parent.getFont());
		FillLayout fl = new FillLayout();
		fl.marginHeight = 1;
		fl.marginWidth = 1;
		setLayout(fl);
		setBackground(AcmusGraphics.BLACK);

		fProperties = new Properties();

		toolkit = new FormToolkit(AcmusGraphics.FORMCOLORS);
		form = toolkit.createScrolledForm(this);

		GridLayout gridLayout;
		GridData gridData;

		form.setText("Project Properties");
		form.setBackgroundImage(AcmusGraphics.IMG_FORMBANNER);
		Composite body = form.getBody();
		gridLayout = new GridLayout(2, true);
		body.setLayout(gridLayout);

		Composite bodyLeft = toolkit.createComposite(body);
		gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 10;
		bodyLeft.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_BOTH);
		bodyLeft.setLayoutData(gridData);

		Label l = toolkit.createLabel(bodyLeft, "Location:");
		fProjectLocation = toolkit.createText(bodyLeft, "");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fProjectLocation.setLayoutData(gridData);

		l = toolkit.createLabel(bodyLeft, "Date:");
		fProjectDate = toolkit.createText(bodyLeft, "");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fProjectDate.setLayoutData(gridData);
		GregorianCalendar c = (new GregorianCalendar());
		fProjectDate.setText(c.get(Calendar.YEAR) + "/"
				+ (c.get(Calendar.MONTH) + 1) + "/"
				+ c.get(Calendar.DAY_OF_MONTH));

		l = toolkit.createLabel(bodyLeft, "Comments:");
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		l.setLayoutData(gridData);
		fProjectComments = toolkit.createText(bodyLeft, "", SWT.MULTI);
		gridData = new GridData(GridData.FILL_BOTH);
		fProjectComments.setLayoutData(gridData);

		Composite bodyRight = toolkit.createComposite(body);
		gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 10;
		bodyRight.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_BOTH);
		bodyRight.setLayoutData(gridData);

		Section section = toolkit.createSection(bodyRight, Section.TITLE_BAR);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		section.setLayoutData(gridData);
		section.setText("Preferences");

		l = toolkit.createLabel(bodyRight, "Ir length (s):");
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		l.setLayoutData(gridData);
		fIrLength = toolkit.createText(bodyRight, "" + Algorithms.DEFAULT_IR_LENGTH);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fIrLength.setLayoutData(gridData);

		l = toolkit.createLabel(bodyRight, "Recording extra time (s):");
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		l.setLayoutData(gridData);
		fRecExtra = toolkit.createText(bodyRight, "" + Algorithms.DEFAULT_REC_EXTRA);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fRecExtra.setLayoutData(gridData);

		toolkit.paintBordersFor(bodyLeft);
		toolkit.paintBordersFor(bodyRight);
	}

	/**
	 * Updates and returns the <code>Properties</code> object that contains
	 * the project properties.
	 * 
	 * @return the project properties
	 */
	public Properties getProjectProperties() {
		// fProperties.clear();
		fProperties.setProperty("Location", fProjectLocation.getText());
		fProperties.setProperty("Date", fProjectDate.getText());
		fProperties.setProperty("Comments", fProjectComments.getText());
		fProperties.setProperty("IrLength", fIrLength.getText());
		fProperties.setProperty("RecordingExtraTime", fRecExtra.getText());
		return fProperties;
	}

	public void loadProperties(InputStream is) {
		try {
			fProperties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		fProjectLocation.setText(fProperties.getProperty("Location", ""));
		fProjectDate.setText(fProperties.getProperty("Date", ""));
		fProjectComments.setText(fProperties.getProperty("Comments", ""));
		fIrLength.setText(fProperties.getProperty("IrLength", ""
				+ Algorithms.DEFAULT_IR_LENGTH));
		fRecExtra.setText(fProperties.getProperty("RecordingExtraTime", ""
				+ Algorithms.DEFAULT_REC_EXTRA));
	}

	public void setProperties(Properties props) {
		fProperties = props;
	}

	public void addLocationModifyListener(ModifyListener l) {
		fProjectLocation.addModifyListener(l);
	}

	public void addDateModifyListener(ModifyListener l) {
		fProjectDate.addModifyListener(l);
	}

	public void addCommentsModifyListener(ModifyListener l) {
		fProjectComments.addModifyListener(l);
	}

	public void addIrLengthModifyListener(ModifyListener l) {
		fIrLength.addModifyListener(l);
	}

	public void addRecExtraModifyListener(ModifyListener l) {
		fRecExtra.addModifyListener(l);
	}
}
