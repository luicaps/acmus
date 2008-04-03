/*
 *  MeasurementProject.java
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
/**
 * MeasurementProject.java
 * Created on 24/01/2006
 */
package acmus;

import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author lku
 * 
 */
public class MeasurementProject {

	public static final String getProperty(IProject project, String prop,
			String def) {
		IFile file = project.getFile("project.properties");
		if (!file.exists())
			return def;
		Properties props = new Properties();
		try {
			props.load(file.getContents());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return props.getProperty(prop, def);
	}

	public static final String getProperty(IFile propsFile, String prop,
			String def) {
		if (!propsFile.exists())
			return def;
		Properties props = new Properties();
		try {
			props.load(propsFile.getContents());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return props.getProperty(prop, def);
	}

	public static final String[] listSets(IContainer sessionFolder) {
		return listSpecialFolder(sessionFolder, "set");
	}

	public static final String[] listTakes(IContainer setFolder) {
		return listSpecialFolder(setFolder, "msr");
	}

	public static final String[] listSpecialFolder(IContainer container,
			String suffix) {
		ArrayList<String> l = new ArrayList<String>();

		try {
			IResource members[] = container.members();
			for (int i = 0; i < members.length; i++) {
				if (members[i] instanceof IFolder) {
					String name = ((IFolder) members[i]).getName();
					if (name.endsWith("." + suffix)) {
						l.add(removeSuffix(name));
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		String[] res = new String[l.size()];
		res = l.toArray(res);
		return res;
	}

	public static final String removeSuffix(String name) {
		return name.substring(0, name.lastIndexOf('.'));
	}

	public static final IFolder[] getTakes(IContainer setFolder) {
		return getSpecialFolder(setFolder, "msr");
	}

	public static final IFolder[] getSpecialFolder(IContainer container,
			String suffix) {
		ArrayList<IFolder> l = new ArrayList<IFolder>();

		try {
			IResource members[] = container.members();
			for (int i = 0; i < members.length; i++) {
				if (members[i] instanceof IFolder) {
					IFolder folder = (IFolder) members[i];
					if (folder.getName().endsWith("." + suffix)) {
						l.add(folder);
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		IFolder[] res = new IFolder[l.size()];
		res = l.toArray(res);
		return res;
	}

}
