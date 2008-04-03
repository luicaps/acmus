/*
 *  AcmusPerspective.java
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
/*
 * Created on 07/01/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package acmus;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AcmusPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String editorArea = layout.getEditorArea();

		// Top left: Resource Navigator view and Bookmarks view placeholder
		IFolderLayout topLeft = layout.createFolder("topLeft",
				IPageLayout.LEFT, 0.25f, editorArea);
		// topLeft.addView(IPageLayout.ID_RES_NAV);
		topLeft.addView("acmus.view.ProjectExplorer");
		topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);

		layout.addNewWizardShortcut("acmus.new.measurementProject"); //$NON-NLS-1$
		// layout.addNewWizardShortcut("acmus.new.measurementSession");
		// //$NON-NLS-1$
		// layout.addNewWizardShortcut("acmus.new.measurementSet");
		// //$NON-NLS-1$
		// layout.addNewWizardShortcut("acmus.new.measurement"); //$NON-NLS-1$
		// layout.addNewWizardShortcut("acmus.new.signal"); //$NON-NLS-1$

	}

}