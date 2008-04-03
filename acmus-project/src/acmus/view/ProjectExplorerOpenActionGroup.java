/*
 *  ProjectExplorerOpenActionGroup.java
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.navigator.IResourceNavigator;
import org.eclipse.ui.views.navigator.OpenActionGroup;

/**
 * @author lku
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ProjectExplorerOpenActionGroup extends OpenActionGroup {

  public ProjectExplorerOpenActionGroup(IResourceNavigator navigator) {
    super(navigator);
  }

  @Override
  public void runDefaultAction(IStructuredSelection selection) {
    // System.out.println("runDefault");
    Object element = selection.getFirstElement();

    if (element instanceof IFile) {
      super.runDefaultAction(selection);
    } else if (element instanceof IFolder) {
      IFolder folder = (IFolder) element;
      if (folder.getName().endsWith(".msr")) {
        // super.runDefaultAction(new MySelection(folder
        // .getFile("measurement.properties")));

        // FIXME: I guess the following is not the correct way to open the right
        // editor.
        try {
          navigator.getSite().getPage().openEditor(
              new FileEditorInput(folder.getFile("measurement.properties")),
              "acmus.editor.MeasurementEditor");
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if (folder.getName().endsWith(".session")) {
        try {
          navigator.getSite().getPage().openEditor(
              new FileEditorInput(folder.getFile("session.properties")),
              "acmus.editor.SessionPropertiesEditor");
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if (folder.getName().endsWith(".set")) {
        try {
          navigator.getSite().getPage().openEditor(
              new FileEditorInput(folder.getFile("set.properties")),
              "acmus.editor.SetPropertiesEditor");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

    } else if (element instanceof IProject) {
      IProject project = (IProject)element;
      try {
        navigator.getSite().getPage().openEditor(
            new FileEditorInput(project.getFile("project.properties")),
            "acmus.editor.ProjectPropertiesEditor");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  class MySelection implements IStructuredSelection {

    Object _el;

    public MySelection(Object el) {
      _el = el;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredSelection#getFirstElement()
     */
    public Object getFirstElement() {
      return _el;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredSelection#iterator()
     */
    public Iterator<?> iterator() {
     return new Iterator<Object>() {
        boolean _hasNext = true;

        public boolean hasNext() {
          return _hasNext;
        }

        public Object next() {
          _hasNext = false;
          return _el;
        }

        public void remove() {
        }
      };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredSelection#size()
     */
    public int size() {
      // TODO Auto-generated method stub
      return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredSelection#toArray()
     */
    public Object[] toArray() {
      Object[] res = new Object[1];
      res[0] = _el;
      return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredSelection#toList()
     */
    public List<?> toList() {
      List<Object> res = new ArrayList<Object>();
      res.add(_el);
      return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelection#isEmpty()
     */
    public boolean isEmpty() {
      // TODO Auto-generated method stub
      return false;
    }
  }
}