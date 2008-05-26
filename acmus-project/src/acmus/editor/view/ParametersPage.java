package acmus.editor.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;
import acmus.MeasurementProject;
import acmus.dsp.Parameters;
import acmus.dsp.Plot;
import acmus.dsp.Util;
import acmus.editor.MeasurementEditor;
import acmus.editor.PositionLabelProvider;

public class ParametersPage extends Composite {

	private final MeasurementEditor parent;
	private int _parametersIndex;
	private Table table;
	private TableViewer fTPos;
	private ParametersHolder _parameters[] = new ParametersHolder[11];
	private Shell _plotShells[] = new Shell[11];
	private String[] columnNames = new String[] { "freq [Hz]", "63", "125",
			"250", "500", "1000", "2000", "4000", "8000", "A", "C", "Linear" };
	
	public ParametersPage(MeasurementEditor parent, int style) {
		super(parent.getContainer(), style);
		this.parent = parent;
		createParametersPage();
	}

	public void createParametersPage() {
		try {
//			if (!parent.getParamsFile().exists() || !parent.getSchroederFolder().exists()) {
//				parent.getImpulseResponsePage().calculateParameters();
//			}
	
			this._parametersIndex = parent.addPage(this);
			parent.setPageText(this._parametersIndex, "Parameters");
			this.setLayout(new GridLayout(1, false));
	
			// GridData gridData;
	
			createTableViewer();
	
			Label lBr = new Label(this, SWT.NONE);
			Label lTr = new Label(this, SWT.NONE);
			Label lItgd = new Label(this, SWT.NONE);
	
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
				this.fTPos.add(new Parameter(name, ch));
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

	public void createTableViewer() {
		this.table = new Table(this, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		this.table.setLinesVisible(true);
		this.table.setHeaderVisible(true);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		this.table.setLayoutData(gridData);
	
		this.fTPos = new TableViewer(this.table);
		this.fTPos.setLabelProvider(new PositionLabelProvider());
		this.fTPos.setColumnProperties(columnNames);
	
		{
			TableColumn tc = new TableColumn(this.table, SWT.LEFT);
			tc.setResizable(true);
			tc.setWidth(70);
			tc.setText(columnNames[0]);
		}
		for (int i = 1; i < columnNames.length; i++) {
			TableColumn tc = new TableColumn(this.table, SWT.LEFT);
			tc.setResizable(true);
			tc.setWidth(50);
			tc.setText(columnNames[i]);
			tc.addSelectionListener(new TColListener(i - 1));
		}
	
	
	}
	public void openPlot(int index) {
		try {
			ParametersHolder ph = _parameters[index];
	
			// read data
			if (ph == null) {
				_parameters[index] = new ParametersHolder();
				ph = _parameters[index];
	
				String separator = System.getProperty("file.separator", "/");
				BufferedReader br = new BufferedReader(new FileReader(
						parent.getSchroederFolder().getLocation().toOSString() + separator
								+ Parameters.CHANNEL_NAMES[index] + ".txt"));
				String line = br.readLine();
				int n = Integer.parseInt(line);
	
				double xs[] = new double[n];
				double ys[] = new double[n];
	
				line = br.readLine();
	
				StringTokenizer st = new StringTokenizer(line);
				for (int i = 0; i < xs.length; i++) {
					xs[i] = Double.parseDouble(st.nextToken());
				}
				line = br.readLine();
				st = new StringTokenizer(line);
				for (int i = 0; i < xs.length; i++) {
					ys[i] = Double.parseDouble(st.nextToken());
				}
	
				ph.schroederX = xs;
				ph.schroederY = ys;
	
				ph.edt = Double.parseDouble(br.readLine());
	
				line = br.readLine();
				if (!line.equals("?")) {
					st = new StringTokenizer(line);
					ph.a10 = Double.parseDouble(st.nextToken());
					ph.b10 = Double.parseDouble(st.nextToken());
					ph.t10 = Double.parseDouble(st.nextToken());
				}
	
				line = br.readLine();
				if (!line.equals("?")) {
					st = new StringTokenizer(line);
					ph.a20 = Double.parseDouble(st.nextToken());
					ph.b20 = Double.parseDouble(st.nextToken());
					ph.t20 = Double.parseDouble(st.nextToken());
				}
	
				line = br.readLine();
				if (!line.equals("?")) {
					st = new StringTokenizer(line);
					ph.a30 = Double.parseDouble(st.nextToken());
					ph.b30 = Double.parseDouble(st.nextToken());
					ph.t30 = Double.parseDouble(st.nextToken());
				}
	
				line = br.readLine();
				if (!line.equals("?")) {
					st = new StringTokenizer(line);
					ph.a40 = Double.parseDouble(st.nextToken());
					ph.b40 = Double.parseDouble(st.nextToken());
					ph.t40 = Double.parseDouble(st.nextToken());
				}
			} // read data
	
			// open shell
			if (_plotShells[index] == null || _plotShells[index].isDisposed()) {
				Display d = AcmusPlugin.getDefault().getWorkbench()
						.getDisplay();
				_plotShells[index] = new Shell(d);
				_plotShells[index].setLayout(new GridLayout(1, false));
				Plot plot = new Plot(_plotShells[index], SWT.NONE,
						"Approximated Decay Times - " + columnNames[index + 1],
						"time (s)", "dB");
				GridData gridData = new GridData(GridData.FILL_BOTH);
				plot.setLayoutData(gridData);
	
				plot.line(ph.schroederX, ph.schroederY, AcmusGraphics.BLUE,
						"Schroeder curve");
	
				// line([0,(-60-A10)/(B10)],[A10,-60],'Color','m','LineWidth',.5);
				// line([0,(-60-A20)/(B20)],[A20,-60],'Color','r','LineWidth',.5);
				// line([0,(-60-A30)/(B30)],[A30,-60],'Color','g','LineWidth',.5);
				// if nargout == 4
				// line([0,(-60-A40)/(B40)],[A40,-60],'Color','y','LineWidth',.5);
				// end
				// line([xlimit(1),xlimit(2)],[-60,-60],'Color',[.4,.4,.4],'LineWidth',.5);
	
				DecimalFormat f = new DecimalFormat("#.###");
				if (ph.a10 != Double.NaN) {
					double[] x = new double[2];
					double[] y = new double[2];
					x[0] = 0;
					x[1] = (-60 - ph.a10) / ph.b10;
					y[0] = ph.a10;
					y[1] = -60;
					plot.line(x, y, AcmusGraphics.MAGENTA, "EDT (ms) = "
							+ f.format(ph.edt));
				}
	
				if (ph.a20 != Double.NaN) {
					double[] x = new double[2];
					double[] y = new double[2];
					x[0] = 0;
					x[1] = (-60 - ph.a20) / ph.b20;
					y[0] = ph.a20;
					y[1] = -60;
					plot.line(x, y, AcmusGraphics.RED, "T20 (ms) = "
							+ f.format(ph.t20));
				}
	
				if (ph.a30 != Double.NaN) {
					double[] x = new double[2];
					double[] y = new double[2];
					x[0] = 0;
					x[1] = (-60 - ph.a30) / ph.b30;
					y[0] = ph.a30;
					y[1] = -60;
					plot.line(x, y, AcmusGraphics.GREEN, "T30 (ms) = "
							+ f.format(ph.t30));
				}
	
				if (ph.a40 != Double.NaN) {
					double[] x = new double[2];
					double[] y = new double[2];
					x[0] = 0;
					x[1] = (-60 - ph.a40) / ph.b40;
					y[0] = ph.a40;
					y[1] = -60;
					plot.line(x, y, AcmusGraphics.YELLOW, "T40 (ms) = "
							+ f.format(ph.t40));
				}
	
				{
					plot.yLimit(-70, 0);
					double tmp[] = new double[3];
					tmp[0] = ph.t20;
					tmp[1] = ph.t30;
					tmp[2] = ph.t40;
					double xMax = Util.max(tmp);
					plot.xLimit(0, xMax * 1.1);
					double[] x = new double[2];
					double[] y = new double[2];
					x[0] = 0;
					x[1] = xMax * 1.1;
					y[0] = -60;
					y[1] = -60;
					plot.line(x, y, AcmusGraphics.GRAY, "");
				}
	
				_plotShells[index]
						.setText("Decay curve - "
								+ columnNames[index + 1]
								+ " - "
								+ MeasurementProject.removeSuffix(parent.getOutFolder()
										.getName()));
				_plotShells[index].setSize(400, 300);
			}
			_plotShells[index].open();
	
			ph = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private class TColListener extends SelectionAdapter {
		int _index;

		public TColListener(int i) {
			_index = i;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			openPlot(_index);
		}
	}
}
class ParametersHolder {
	double c50, c80, d50, d80, ct;
	public double t30;
	public double t20;
	public double edt;
	public double t40;
	public double a10;
	public double b10;
	public double t10;
	public double b20;
	public double a20;
	public double a30;
	public double b30;
	public double b40;
	public double a40;
	int itdg;
	double rdr, lfc;
	public double[] schroederX;
	public double[] schroederY;

	public ParametersHolder() {
		c50 = c80 = d50 = d80 = ct = edt = t20 = t30 = t40 = rdr = lfc = Double.NaN;
		a10 = b10 = t10 = Double.NaN;
		a20 = b20 = Double.NaN;
		a30 = b30 = Double.NaN;
		a40 = b40 = Double.NaN;
		itdg = -1;
	}
}
