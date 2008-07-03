/*
 *  AcmusGraphics.java
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
 * Created on 18/01/2005
 */
package acmus;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormColors;

/**
 * @author lku
 * 
 *         Convenience class for storing references to image descriptors and
 *         colors.
 */

public class AcmusGraphics {

	public static boolean DOUBLE_BUFFER = false;
	public static int DOUBLE_BUFFER_STYLE = SWT.NONE;
	public static final URL BASE_URL = AcmusPlugin.getDefault().getBundle()
			.getEntry("/");

	public static final Image EDITOR_ACTION1_IMAGE;
	public static final Image EDITOR_ACTION2_IMAGE;
	public static final Image EDITOR_ACTION3_IMAGE;
	public static final Image EDITOR_ACTION1_IMAGE_DISABLE;
	public static final Image EDITOR_ACTION2_IMAGE_DISABLE;
	public static final Image EDITOR_ACTION3_IMAGE_DISABLE;
	public static final Image EDITOR_ACTION1_IMAGE_ENABLE;
	public static final Image EDITOR_ACTION2_IMAGE_ENABLE;
	public static final Image EDITOR_ACTION3_IMAGE_ENABLE;
	public static final ImageDescriptor NEW_PROJECT_WIZARD_BANNER;

	public static final Image IMG_APP_ICON;

	public static final Image IMG_PROJECT;
	public static final Image IMG_SESSION;
	public static final Image IMG_SET;
	public static final Image IMG_MEASUREMENT;
	public static final Image IMG_AUDIO;
	public static final Image IMG_POSITIONS;
	public static final Image IMG_FILE;
	public static final Image IMG_FOLDER;
	public static final Image IMG_SIGNALFOLDER;

	public static final Image IMG_PLAY;
	public static final Image IMG_STOP;
	public static final Image IMG_PAUSE;
	public static final Image IMG_RECORD;

	public static final Image IMG_XZOOMIN;
	public static final Image IMG_XZOOMOUT;
	public static final Image IMG_XZOOMFIT;
	public static final Image IMG_YZOOMFIT;
	public static final Image IMG_YZOOMVFIT;
	public static final Image IMG_YZOOMIN;
	public static final Image IMG_YZOOMOUT;

	public static final Image IMG_ZOOM;
	public static final Image IMG_SELECTION;

	public static final Image IMG_UP;
	public static final Image IMG_DOWN;

	public static final Image IMG_TEST;

	public static final Image IMG_FORMBANNER;

	public static final Color GREEN;
	public static final Color GREEN2;
	public static final Color GREEN3;
	public static final Color LIGHT_GREEN;
	public static final Color BLUE;
	public static final Color BLUE2;
	public static final Color BLUE3;
	public static final Color LIGHT_BLUE;
	public static final Color LIGHT_BLUE2;
	public static final Color RED;
	public static final Color RED2;
	public static final Color LIGHT_RED;
	public static final Color BLACK;
	public static final Color WHITE;
	public static final Color YELLOW;
	public static final Color LIGHT_YELLOW;
	public static final Color MAGENTA;
	public static final Color GRAY;
	public static final Color DARK_PURPLE;
	public static final Color[] SPECTRUM_COLOR_SCALE;
	public static final Image IMG_SPECTRUM_COLOR_SCALE;

	public static final Color[] COMP_COLORS;

	public static final Font FIXED_FONT;
	public static final Font FIXED_SMALL;
	public static final Font BOLD;
	public static final Font DEFAULT_FONT;

	public static final FormColors FORMCOLORS;

	static {
		DOUBLE_BUFFER = System.getProperty("os.name").matches(".*Windows.*");
		// DOUBLE_BUFFER = true;
		System.out.println("System: " + System.getProperty("os.name"));
		System.out.println("Double-buffering: " + DOUBLE_BUFFER);

		if (DOUBLE_BUFFER) {
			DOUBLE_BUFFER_STYLE = SWT.NO_BACKGROUND;
		}

		String iconPath = "icons/"; //$NON-NLS-1$

		String prefix = iconPath + "full/ctool16/"; //$NON-NLS-1$
		EDITOR_ACTION1_IMAGE = createImageDescriptor(prefix + "action1.gif").createImage(); //$NON-NLS-1$
		EDITOR_ACTION2_IMAGE = createImageDescriptor(prefix + "action2.gif").createImage(); //$NON-NLS-1$
		EDITOR_ACTION3_IMAGE = createImageDescriptor(prefix + "action3.gif").createImage(); //$NON-NLS-1$

		prefix = iconPath + "full/dtool16/"; //$NON-NLS-1$
		EDITOR_ACTION1_IMAGE_DISABLE = createImageDescriptor(
				prefix + "action1.gif").createImage(); //$NON-NLS-1$
		EDITOR_ACTION2_IMAGE_DISABLE = createImageDescriptor(
				prefix + "action2.gif").createImage(); //$NON-NLS-1$
		EDITOR_ACTION3_IMAGE_DISABLE = createImageDescriptor(
				prefix + "action3.gif").createImage(); //$NON-NLS-1$

		prefix = iconPath + "full/etool16/"; //$NON-NLS-1$
		EDITOR_ACTION1_IMAGE_ENABLE = createImageDescriptor(
				prefix + "action1.gif").createImage(); //$NON-NLS-1$
		EDITOR_ACTION2_IMAGE_ENABLE = createImageDescriptor(
				prefix + "action2.gif").createImage(); //$NON-NLS-1$
		EDITOR_ACTION3_IMAGE_ENABLE = createImageDescriptor(
				prefix + "action3.gif").createImage(); //$NON-NLS-1$

		prefix = iconPath + "full/wizban/"; //$NON-NLS-1$
		NEW_PROJECT_WIZARD_BANNER = createImageDescriptor(prefix
				+ "newprj_wiz.png"); //$NON-NLS-1$

		prefix = iconPath + "/"; //$NON-NLS-1$
		IMG_APP_ICON = createImageDescriptor(prefix + "appIcon32x32.gif").createImage(); //$NON-NLS-1$

		prefix = iconPath + "full/res16/"; //$NON-NLS-1$
		IMG_PROJECT = createImageDescriptor(prefix + "project.gif").createImage(); //$NON-NLS-1$
		IMG_SESSION = createImageDescriptor(prefix + "session.gif").createImage(); //$NON-NLS-1$
		IMG_SET = createImageDescriptor(prefix + "set.gif").createImage(); //$NON-NLS-1$
		IMG_MEASUREMENT = createImageDescriptor(prefix + "measurement.gif").createImage(); //$NON-NLS-1$
		IMG_AUDIO = createImageDescriptor(prefix + "audio.gif").createImage(); //$NON-NLS-1$
		IMG_POSITIONS = createImageDescriptor(prefix + "positions.gif").createImage(); //$NON-NLS-1$
		IMG_FILE = createImageDescriptor(prefix + "file.gif").createImage(); //$NON-NLS-1$
		IMG_FOLDER = createImageDescriptor(prefix + "folder.gif").createImage(); //$NON-NLS-1$
		IMG_SIGNALFOLDER = createImageDescriptor(prefix + "signalfolder.gif").createImage(); //$NON-NLS-1$

		prefix = iconPath + "full/etool16/"; //$NON-NLS-1$
		IMG_PLAY = createImageDescriptor(prefix + "play.gif").createImage(); //$NON-NLS-1$
		IMG_STOP = createImageDescriptor(prefix + "stop.gif").createImage(); //$NON-NLS-1$
		IMG_PAUSE = createImageDescriptor(prefix + "pause.gif").createImage(); //$NON-NLS-1$
		IMG_RECORD = createImageDescriptor(prefix + "rec.gif").createImage(); //$NON-NLS-1$
		IMG_XZOOMIN = createImageDescriptor(prefix + "xzoom_in.gif").createImage(); //$NON-NLS-1$
		IMG_XZOOMOUT = createImageDescriptor(prefix + "xzoom_out.gif").createImage(); //$NON-NLS-1$
		IMG_XZOOMFIT = createImageDescriptor(prefix + "xzoom_fit.gif").createImage(); //$NON-NLS-1$
		IMG_YZOOMIN = createImageDescriptor(prefix + "yzoom_in.gif").createImage(); //$NON-NLS-1$
		IMG_YZOOMOUT = createImageDescriptor(prefix + "yzoom_out.gif").createImage(); //$NON-NLS-1$
		IMG_YZOOMFIT = createImageDescriptor(prefix + "yzoom_fit.gif").createImage(); //$NON-NLS-1$
		IMG_YZOOMVFIT = createImageDescriptor(prefix + "yzoom_vfit.gif").createImage(); //$NON-NLS-1$

		IMG_ZOOM = createImageDescriptor(prefix + "zoom.gif").createImage(); //$NON-NLS-1$
		IMG_SELECTION = createImageDescriptor(prefix + "selection.gif").createImage(); //$NON-NLS-1$

		IMG_UP = createImageDescriptor(prefix + "up.gif").createImage(); //$NON-NLS-1$
		IMG_DOWN = createImageDescriptor(prefix + "down.gif").createImage(); //$NON-NLS-1$

		prefix = iconPath; //$NON-NLS-1$
		IMG_FORMBANNER = createImageDescriptor(prefix + "form_banner.gif").createImage(); //$NON-NLS-1$

		IMG_TEST = createImageDescriptor("images/red.png").createImage(); //$NON-NLS-1$

		Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
		GREEN = new Color(d, 0, 255, 0);
		GREEN2 = new Color(d, 0, 255, 100);
		GREEN3 = new Color(d, 0, 100, 0);
		LIGHT_GREEN = new Color(d, 223, 255, 172);
		BLUE = new Color(d, 0, 0, 255);
		BLUE2 = new Color(d, 0, 100, 255);
		BLUE3 = new Color(d, 112, 225, 255);
		LIGHT_BLUE = new Color(d, 201, 231, 255);
		LIGHT_BLUE2 = new Color(d, 245, 250, 255);
		RED = new Color(d, 255, 0, 0);
		RED2 = new Color(d, 100, 0, 0);
		LIGHT_RED = new Color(d, 255, 223, 172);
		BLACK = new Color(d, 0, 0, 0);
		WHITE = new Color(d, 255, 255, 255);
		YELLOW = new Color(d, 255, 255, 0);
		LIGHT_YELLOW = new Color(d, 255, 239, 175);
		MAGENTA = new Color(d, 255, 0, 255);
		GRAY = new Color(d, 150, 150, 150);
		DARK_PURPLE = new Color(d, 73, 39, 89);
		// GRAYSCALE = new Color[256];
		// for (int i = 0; i < 256; i++) {
		// GRAYSCALE[i] = new Color(d, i, i, i);
		// }

		// GRAYSCALE = new Color[255 * 3+1];
		// for (int i = 0; i < 255; i++) {
		// GRAYSCALE[i * 3] = new Color(d, i, i, i);
		// GRAYSCALE[i * 3 + 1] = new Color(d, i + 1, i, i);
		// GRAYSCALE[i * 3 + 2] = new Color(d, i + 1, i + 1, i);
		// }
		// GRAYSCALE[765] = new Color(d, 255, 255, 255);

		SPECTRUM_COLOR_SCALE = new Color[768];
		for (int i = 0; i < 256; i++) {
			SPECTRUM_COLOR_SCALE[i] = new Color(d, i, 0, 0);
		}
		for (int i = 0; i < 256; i++) {
			SPECTRUM_COLOR_SCALE[256 + i] = new Color(d, 255, i, 0);
		}
		for (int i = 0; i < 256; i++) {
			SPECTRUM_COLOR_SCALE[512 + i] = new Color(d, 255, 255, i);
		}

		IMG_SPECTRUM_COLOR_SCALE = new Image(d, SPECTRUM_COLOR_SCALE.length, 1);
		GC gc = new GC(IMG_SPECTRUM_COLOR_SCALE);
		for (int i = 0; i < SPECTRUM_COLOR_SCALE.length; i++) {
			gc.setForeground(AcmusGraphics.SPECTRUM_COLOR_SCALE[i]);
			gc.drawPoint(i, 0);
		}

		COMP_COLORS = new Color[10];
		COMP_COLORS[0] = BLUE2;
		COMP_COLORS[1] = RED;
		COMP_COLORS[2] = GREEN;
		COMP_COLORS[3] = YELLOW;
		COMP_COLORS[4] = DARK_PURPLE;
		COMP_COLORS[5] = GRAY;
		COMP_COLORS[6] = MAGENTA;
		COMP_COLORS[7] = LIGHT_BLUE;
		COMP_COLORS[8] = LIGHT_GREEN;
		COMP_COLORS[9] = WHITE;

		FIXED_SMALL = new Font(d, "fixed", 8, SWT.NORMAL);
		FIXED_FONT = new Font(d, "courier", 9, SWT.NORMAL);
		BOLD = new Font(d, "sans", 10, SWT.BOLD);
		DEFAULT_FONT = new Font(d, "sans", 9, SWT.NORMAL);

		FORMCOLORS = new FormColors(d);
		FORMCOLORS.markShared();
	}

	/**
	 * Utility method to create an <code>ImageDescriptor</code> from a path to a
	 * file.
	 */
	public static ImageDescriptor createImageDescriptor(String path) {
		try {
			URL url = new URL(BASE_URL, path);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
		}
		return ImageDescriptor.getMissingImageDescriptor();
	}

	public static Text newText(Composite parent, String lb, String str) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(lb);
		Text t = new Text(parent, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 40;
		t.setLayoutData(gridData);
		t.setText(str);
		return t;
	}

	public static Text newText(Composite parent, String lb) {
		return newText(parent, lb, "0");
	}

	public static Combo newCombo(Composite parent, String lb, String[] values,
			int defInd) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText("Material");
		Combo c = new Combo(parent, SWT.NONE);
		for (int i = 0; i < values.length; i++) {
			c.add(values[i]);
		}
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		c.setLayoutData(gridData);
		c.select(defInd);
		return c;
	}

	public static Combo newCombo(Composite parent, String lb, String[] values) {
		return newCombo(parent, lb, values, 0);
	}

	public static GridLayout newNoMarginGridLayout(int numColumns,
			boolean makeColumnsEqualWidth) {
		GridLayout gl = new GridLayout(numColumns, makeColumnsEqualWidth);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		return gl;
	}

	public static GridLayout newPosGridLayout(int numColumns,
			boolean makeColumnsEqualWidth) {
		GridLayout gl = new GridLayout(numColumns, makeColumnsEqualWidth);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 0;
		gl.horizontalSpacing = 0;
		return gl;
	}

	public static GridLayout newGridLayout(int numColumns,
			boolean makeColumnsEqualWidth, int mh, int mw, int vs, int hs) {
		GridLayout gl = new GridLayout(numColumns, makeColumnsEqualWidth);
		gl.marginHeight = mh;
		gl.marginWidth = mw;
		gl.verticalSpacing = vs;
		gl.horizontalSpacing = hs;
		return gl;
	}

}