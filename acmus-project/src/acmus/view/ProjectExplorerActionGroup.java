/*
 *  ProjectExplorerActionGroup.java
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
 * Created on 06/03/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package acmus.view;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.views.navigator.IResourceNavigator;
import org.eclipse.ui.views.navigator.MainActionGroup;

/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ProjectExplorerActionGroup extends MainActionGroup {

	private NewWizardMenu _myNewWizardMenu;

	public ProjectExplorerActionGroup(IResourceNavigator navigator) {
		super(navigator);
	}

	protected void makeSubGroups() {
		super.makeSubGroups();
		openGroup = new ProjectExplorerOpenActionGroup(navigator);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext()
				.getSelection();

		MenuManager newMenu = new MenuManager(
				ResourceNavigatorMessages.ResourceNavigator_new);
		menu.add(newMenu);
		_myNewWizardMenu = new NewWizardMenu(navigator.getSite()
				.getWorkbenchWindow());
		newMenu.add(_myNewWizardMenu);

		gotoGroup.fillContextMenu(menu);
		openGroup.fillContextMenu(menu);
		menu.add(new Separator());

		workspaceGroup.fillContextMenu(menu);

		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu
				.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
						+ "-end")); //$NON-NLS-1$
		menu.add(new Separator());

		if (selection.size() == 1) {
			propertyDialogAction.selectionChanged(selection);
			menu.add(propertyDialogAction);
		}
	}

	public void dispose() {
		super.dispose();
		_myNewWizardMenu.dispose();
	}

}