/*
 *  CompareWaveforms.java
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
 * Created on 09/02/2006
 */
package acmus.tools;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;
import acmus.audio.AudioPlayer;
import acmus.audio.WaveformDisplay;

/**
 * @author lku
 * 
 */
public class CompareWaveforms extends Composite {

	Text _input;
	Button _inputBrowse;
	FileDialog _inputFileDialog;

	Button _bAdd;

	WaveformDisplay _waveform;

	List<Image> _disposeList = new ArrayList<Image>();

	public CompareWaveforms(Shell shell, Composite parent, int style) {
		super(parent, style);

		_inputFileDialog = new FileDialog(shell, SWT.OPEN);

		GridLayout gl;

		setLayout(new GridLayout(3, false));

		Label l;
		GridData gridData;

		ModifyListener ml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		};

		Composite c = new Composite(this, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		gl = new GridLayout(3, false);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		c.setLayout(gl);
		c.setLayoutData(gridData);

		l = new Label(c, SWT.LEFT);
		l.setText("Input");

		_input = new Text(c, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 200;
		_input.setLayoutData(gridData);
		_input.addModifyListener(ml);

		_inputBrowse = new Button(c, SWT.NONE);
		_inputBrowse.setText("Browse");
		_inputBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String filename = _inputFileDialog.open();
				if (filename != null) {
					_input.setText(filename);
				}
			}
		});

		_bAdd = new Button(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = SWT.CENTER;
		_bAdd.setLayoutData(gridData);
		_bAdd.setText("Add");
		_bAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				open(_input.getText());
				_input.setText("");
				validate();
			}
		});
		_bAdd.setEnabled(false);

		createTableViewer(this);

		c = new Composite(this, SWT.NONE);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		gl = new GridLayout(1, false);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		c.setLayout(gl);
		c.setLayoutData(gridData);

		AudioPlayer ap = new AudioPlayer();

		ToolBar zoom = ap.createZoomBar(c, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		zoom.setLayoutData(gridData);

		// _waveform = new WaveformDisplay(this, SWT.NONE);
		_waveform = ap.createWaveformDisplay(c, SWT.NONE);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = 400;
		gridData.heightHint = 300;
		_waveform.setLayoutData(gridData);
	}

	private void validate() {
		if (_input.getText().trim().equals("")) {
			_bAdd.setEnabled(false);
		} else {
			File fi = new File(_input.getText());
			if (!fi.exists())
				_bAdd.setEnabled(false);
			else
				_bAdd.setEnabled(true);
		}

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				for (Image i : _disposeList) {
					i.dispose();
				}
				_disposeList.clear();
			}
		});
	}

	public void open(IFile file) {
		open(file.getLocation().toOSString());
	}

	public void open(String filename) {
		try {
			AudioInputStream audioStream = AudioSystem
					.getAudioInputStream(new FileInputStream(filename));
			int[] audioData = AudioPlayer.readData(audioStream);
			if (_waveform.numberOfArrays() < 1) {
				_waveform
						.setData(audioData, audioStream.getFormat()
								.getChannels(), audioStream.getFormat()
								.getSampleRate(), audioStream.getFormat()
								.getSampleSizeInBits(), getColor(0));

			} else {
				_waveform.addData(audioData, audioStream.getFormat()
						.getChannels(),
						audioStream.getFormat().getSampleRate(),
						getColor(_waveform.numberOfArrays()));
			}

			String sep = System.getProperty("file.separator");
			int ind = filename.lastIndexOf(sep, filename.lastIndexOf(sep) - 1);
			String name = filename;
			if (ind > 0)
				name = filename.substring(ind + 1);

			Object o = new TableLine(name,
					getColor(_waveform.numberOfArrays() - 1));
			_tViewer.add(o);
			_tViewer.setChecked(o, true);
			
			/* Graph update - drawing the wave added 
			 * The arg true is because the wave is checked for sure. So, it need to be drawed */
			_waveform.drawArray(_waveform.numberOfArrays() - 1, true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Color getColor(int i) {
		return AcmusGraphics.COMP_COLORS[i % AcmusGraphics.COMP_COLORS.length];
	}

	/* ========================================================================= */

	private Table _table;
	private CheckboxTableViewer _tViewer;

	// Set column names
	private String[] columnNames = new String[] { "file", "color" };

	private void createTableViewer(Composite parent) {
		_table = new Table(parent, SWT.SINGLE | SWT.CHECK | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		_table.setLinesVisible(false);
		_table.setHeaderVisible(true);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		_table.setLayoutData(gridData);

		_tViewer = new CheckboxTableViewer(_table);
		_tViewer.setLabelProvider(new PositionLabelProvider());
		_tViewer.setColumnProperties(columnNames);

		_tViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				for (int i = 0; i < _waveform.numberOfArrays(); i++) {
					_waveform.drawArray(i, _tViewer.getChecked(_tViewer
							.getElementAt(i)));
				}
			}

		});

		TableColumn tc = new TableColumn(_table, SWT.LEFT);
		tc.setResizable(true);
		tc.setWidth(500);
		tc.setText(columnNames[0]);

		tc = new TableColumn(_table, SWT.LEFT);
		tc.setResizable(true);
		tc.setWidth(40);
		tc.setText(columnNames[1]);
	}

	class PositionLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex != 1)
				return null;
			TableLine t = (TableLine) element;
			return createRectangle(20, 10, t.color);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex != 0)
				return null;
			TableLine t = (TableLine) element;
			return t.name;
		}

		private Image createRectangle(int width, int height, Color c) {
			Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
			Image img = new Image(d, width, height);
			GC gc = new GC(img);
			gc.setBackground(c);
			gc.fillRectangle(0, 0, width, height);
			return img;
		}
	}

	class TableLine {
		String name;
		Color color;

		public TableLine(String name, Color color) {
			this.name = name;
			this.color = color;
		}
	}

}
