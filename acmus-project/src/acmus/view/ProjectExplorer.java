/*
 *  ProjectExplorer.java
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.navigator.ResourceNavigator;

import acmus.AcmusApplication;
import acmus.AcmusGraphics;

/**
 * @author lku
 */
public class ProjectExplorer extends ResourceNavigator {

	// the listener we register with the selection service 
	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			if(ProjectExplorer.class.isInstance(sourcepart)){
				String[] temp = selection.toString().split("P/");
				if(temp.length > 1)
					AcmusApplication.setTitle("AcMus - " + temp[1].substring(0, temp[1].length()-1));
			}
		}
	};

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		
		if(site != null){
			site.getWorkbenchWindow().getSelectionService().addSelectionListener(listener);
		}
	}
	
	protected void handleOpen(OpenEvent event) {
		super.handleOpen(event);
		System.out.println("handleOpen " + getActionGroup());
	}

	protected void makeActions() {
		setActionGroup(new ProjectExplorerActionGroup(this));
	}

	protected void initLabelProvider(TreeViewer viewer) {
		viewer.setLabelProvider(new MyLabelProvider());
	}

	class MyLabelProvider extends LabelProvider {

		public Image getImage(Object element) {
			Image res = null;

			if (element instanceof IProject)
				res = AcmusGraphics.IMG_PROJECT;
			else if (element instanceof IFolder) {
				String name = ((IFolder) element).getName();
				if (name.endsWith(".session"))
					res = AcmusGraphics.IMG_SESSION;
				else if (name.endsWith(".set"))
					res = AcmusGraphics.IMG_SET;
				else if (name.endsWith(".msr"))
					res = AcmusGraphics.IMG_MEASUREMENT;
				else if (name.endsWith(".signal"))
					res = AcmusGraphics.IMG_SIGNALFOLDER;
				else
					res = AcmusGraphics.IMG_FOLDER;
			} else if (element instanceof IFile) {
				String name = ((IFile) element).getName();
				if (name.endsWith(".wav"))
					res = AcmusGraphics.IMG_AUDIO;
				else if (name.endsWith(".positions"))
					res = AcmusGraphics.IMG_POSITIONS;
				else
					res = AcmusGraphics.IMG_FILE;
			}

			if (res == null)
				return super.getImage(element);
			return res;
		}

		public String getText(Object element) {
			if (element instanceof IProject) {
				return ((IProject) element).getName();
			}
			if (element instanceof IFolder) {
				String name = ((IFolder) element).getName();
				int i = name.lastIndexOf('.');
				if (name.equals("_signals.signal"))
					return name.substring(1, i);
				if (i > 0)
					return name.substring(0, i);
				else
					return name;
			}
			if (element instanceof IFile) {
				String name = ((IFile) element).getName();
				if (name.endsWith(".wav"))
					return name.substring(0, name.lastIndexOf('.'));
				return ((IFile) element).getName();
			}
			return super.getText(element);
		}
	}
}