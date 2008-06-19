/*
 *  SchroederDiffuser.java
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
 * Created on Jun 12, 2006
 */
package acmus.tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;
import acmus.util.PrintUtils;

/**
 * @author lku
 * 
 */
public class SchroederDiffuser extends Composite {

	static String newLine = System.getProperty("line.separator");
	static DecimalFormat format;// = new DecimalFormat("#.###");

	static {
		NumberFormat f = DecimalFormat.getInstance(new Locale("en"));
		if (f instanceof DecimalFormat) {
			format = (DecimalFormat) DecimalFormat
					.getInstance(new Locale("en"));
			format.applyPattern("#.#");
		} else {
			format = new DecimalFormat("#.#");
		}
	}

	Text _fMax;
	Text _f0;

	Text _speed;
	Text _gap;

	Label _periodLength;
	Text _wall;

	Text _result;
	DiffuserPanel _diffuser;
	DiffuserPanel _diffuserWall;

	Object _last;

	public SchroederDiffuser(Composite parent, int style) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);

		GridData gridData;
		Label l;

		KeyListener kl = new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
					_last = null;
					if (validate())
						compute();
				}
			}
		};

		ModifyListener ml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {

				(new Thread() {
					public void run() {
						Object foo = new Object();

						_last = foo;
						try {
							Thread.sleep(2000);
						} catch (InterruptedException ie) {
						}

						if (foo == _last) {
							AcmusPlugin.getDefault().getWorkbench()
									.getDisplay().asyncExec(new Runnable() {
										public void run() {
											if (validate())
												compute();
										}
									});
						}

					}
				}).start();
			}

		};

		l = new Label(this, SWT.NONE);
		l.setText("Start frequency (Hz):");
		_f0 = new Text(this, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_f0.setLayoutData(gridData);
		_f0.setText("1000");
		_f0.addModifyListener(ml);
		_f0.addKeyListener(kl);

		l = new Label(this, SWT.NONE);
		l.setText("Speed of Sound (m/s):");
		_speed = new Text(this, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_speed.setLayoutData(gridData);
		_speed.setText("344");
		_speed.addModifyListener(ml);
		_speed.addKeyListener(kl);

		l = new Label(this, SWT.NONE);
		l.setText("End frequency (Hz):");
		_fMax = new Text(this, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_fMax.setLayoutData(gridData);
		_fMax.setText("8000");
		_fMax.addModifyListener(ml);
		_fMax.addKeyListener(kl);

		l = new Label(this, SWT.NONE);
		l.setText("Distance between wells (mm):");
		_gap = new Text(this, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_gap.setLayoutData(gridData);
		_gap.setText("3");
		_gap.addModifyListener(ml);
		_gap.addKeyListener(kl);

		SashForm sf = new SashForm(this, SWT.HORIZONTAL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 4;
		sf.setLayoutData(gridData);

		_result = new Text(sf, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		_result.setFont(AcmusGraphics.FIXED_FONT);

		Composite c = new Composite(sf, SWT.NONE);
		c.setLayout(AcmusGraphics.newNoMarginGridLayout(2, false));

		_periodLength = new Label(c, SWT.NONE);
		_periodLength.setText("Period:");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		_periodLength.setLayoutData(gridData);
		_diffuser = new DiffuserPanel(c, SWT.NONE);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		_diffuser.setLayoutData(gridData);

		l = new Label(c, SWT.NONE);
		l.setText("Wall length (m):");
		_wall = new Text(c, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_wall.setLayoutData(gridData);
		_wall.setText("2");
		_wall.addModifyListener(ml);
		_wall.addKeyListener(kl);
		_diffuserWall = new DiffuserPanel(c, SWT.NONE);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		_diffuserWall.setLayoutData(gridData);

		int weights[] = { 2, 3 };
		sf.setWeights(weights);

		if (validate())
			compute();
	}

	public boolean validate() {
		double f0 = parseDouble(_f0.getText());
		double fMax = parseDouble(_fMax.getText());
		double c = parseDouble(_speed.getText());
		double gap = parseDouble(_gap.getText());
		double wall = parseDouble(_wall.getText());

		if (Double.isNaN(f0)) {
			_result.setText("Error: \"Start frequency\" must be a number.");
			return false;
		}
		if (f0 <= 0) {
			_result.setText("Error: \"Start frequency\" must greater than 0.");
			return false;
		}
		if (Double.isNaN(fMax)) {
			_result.setText("Error: \"End frequency\" must be a number.");
			return false;
		}
		if (Double.isNaN(c)) {
			_result.setText("Error: \"Speed of Sound\" must be a number.");
			return false;
		}
		if (Double.isNaN(gap)) {
			_result
					.setText("Error: \"Distance between wells\" must be a number.");
			return false;
		}

		if (Double.isNaN(wall)) {
			_result.setText("Error: \"Wall length\" must be a number.");
			return false;
		}

		if (f0 > fMax) {
			_result
					.setText("Error: \"End frequency\" must be greater than \"Start frequency\"");
			return false;
		}

		return true;
	}

	public void compute() {
		double f0 = parseDouble(_f0.getText());
		double fMax = parseDouble(_fMax.getText());
		double c = parseDouble(_speed.getText());
		double gap = parseDouble(_gap.getText());
		double wall = parseDouble(_wall.getText());
		double W = 1000 * c / (2 * fMax);

		int N = getPrime((int) Math.round(fMax / f0));

		_result.setText("Schroeder Diffuser parameters:" + newLine);
		_result.append("     f0 = " + f0 + " Hz" + newLine);
		_result.append("   fMax = " + fMax + " Hz" + newLine);
		_result.append("  delta = " + (fMax - f0) + " Hz" + newLine);
		_result.append("fMax/f0 = " + (fMax / f0) + newLine);
		_result.append("      c = " + c + " m/s" + newLine);
		_result.append("      T = " + gap + " mm" + newLine);

		_result.append(newLine);
		_result.append("N = " + N + newLine);
		_result.append("Well width = " + (format.format(W - gap)) + " mm ("
				+ format.format(W) + " - " + gap + ")" + newLine);
		_result.append(newLine);
		_result.append("  n   sn  depth(mm)" + newLine);
		_result.append("-------------------" + newLine);

		double[] res = new double[N];
		for (int i = 0; i < N; i++) {
			int sn = (i * i % N);
			res[i] = (double) (sn * c) / (2 * N * f0);
			res[i] *= 1000;
			_result.append(PrintUtils.formatString("" + (i), 3) + " " + PrintUtils.formatString("" + sn, 3)
					+ " " + PrintUtils.formatString(format.format(res[i]), 9) + newLine);
		}

		double periodLength = W * N;
		_periodLength.setText("Period (" + periodLength + " mm):");

		_diffuser.setData(res, W, gap);
		_diffuserWall.setData(res, W, gap, wall * 1000 / periodLength);
	}

	static double parseDouble(String str) {
		double res = Double.NaN;
		try {
			res = Double.parseDouble(str);
		} catch (NumberFormatException e) {
		}
		return res;
	}

	static boolean isPrime(int n) {
		if (n <= 2) {
			return n == 2;
		}
		if (n % 2 == 0) {
			return false;
		}
		for (int i = 3, end = (int) Math.sqrt(n); i <= end; i += 2) {
			if (n % i == 0) {
				return false;
			}
		}
		return true;
	}

	static int getPrime(int n) {
		while (!isPrime(n)) {
			n++;
		}
		return n;
	}

	static double[] schroederDiffuserDepths(double f0, double fMax,
			double speedOfSound) {
		int N = getPrime((int) Math.round(fMax / f0));
		double[] res = new double[N];
		for (int i = 0; i < N; i++) {
			int sn = (i * i % N);
			res[i] = (double) (sn * speedOfSound) / (2 * N * f0);
		}
		return res;
	}

	public static void main(String args[]) {
		int fMax = 1000;
		int f0 = 100;
		// int delta = fMax - f0;
		int c = 344;
		// int N = getPrime(Integer.parseInt(args[0]));
		int N = getPrime(fMax / f0);
		System.out.println("Prime: " + N);
		System.out.println("W = " + (double) c / (2 * fMax));

		double s[] = schroederDiffuserDepths(f0, fMax, c);

		for (int i = 0; i < 2 * N; i++) {
			int sn = (i * i % N);
			System.out.println(i + ". " + sn + " " + (double) (sn * c)
					/ (2 * N * f0) + " " + s[i % s.length]);
		}
	}
}

class DiffuserPanel extends Canvas implements PaintListener {
	Image img;
	double _periods = 1;
	boolean _drawWall = false;

	public DiffuserPanel(Composite parent, int style) {
		super(parent, style);
		addPaintListener(this);
		setBackground(AcmusGraphics.BLACK);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (img != null) {
					img.dispose();
				}
			}
		});
	}

	public void setData(double[] depths, double W, double T) {
		setData(depths, W, T, 1, false);
	}

	public void setData(double[] depths, double W, double T, double periods) {
		setData(depths, W, T, periods, true);
	}

	public void setData(double[] depths, double W, double T, double periods,
			boolean drawWall) {
		_drawWall = drawWall;
		_periods = periods;
		double mmPerPixel = T;
		int pixelsPerW = (int) Math.ceil(W / mmPerPixel);
		double maxD = 0;
		for (int i = 0; i < depths.length; i++) {
			if (depths[i] > maxD)
				maxD = depths[i];
		}
		int yOffset = 5;
		int imgWidth = (pixelsPerW + 1) * depths.length + 1;
		int imgHeight = (int) Math.ceil(maxD / mmPerPixel);

		Display d = AcmusPlugin.getDefault().getWorkbench().getDisplay();
		Image img = new Image(d, imgWidth, imgHeight + yOffset + 1);
		GC gc = new GC(img);
		gc.setBackground(AcmusGraphics.BLACK);
		gc.fillRectangle(0, 0, imgWidth, imgHeight + yOffset + 1);
		gc.setBackground(AcmusGraphics.LIGHT_YELLOW);
		for (int i = 0, x = 1; i < depths.length; i++, x += pixelsPerW + 1) {
			gc.fillRectangle(x, 1 + imgHeight
					- (int) Math.round(depths[i] / maxD * imgHeight),
					pixelsPerW, 1 + imgHeight);
		}
		gc.setBackground(AcmusGraphics.GRAY);
		gc.fillRectangle(0, imgHeight, imgWidth, yOffset + 1);
		gc.dispose();
		if (this.img != null)
			this.img.dispose();
		this.img = img;
		this.redraw();
	}

	public void paintControl(PaintEvent e) {
		if (img == null)
			return;
		GC gc = e.gc;
		int width = getBounds().width - 2;
		int height = getBounds().height - 2;
		int imgWidth = img.getBounds().width;
		int imgHeight = img.getBounds().height;
		double wScale = (double) width / imgWidth / _periods;
		double hScale = (double) height / imgHeight;
		if (wScale < hScale) {
			imgHeight = (int) Math.round((double) imgHeight * wScale);
			imgWidth = (int) Math.round((double) imgWidth * wScale);

			int start = (int) Math.round((width / 2) - (imgWidth / 2)
					- ((Math.ceil(_periods) - 1) / 2 * imgWidth)) + 1;
			int end = (int) ((width / 2) + (imgWidth / 2) + ((Math
					.ceil(_periods) - 1) / 2 * imgWidth));
			for (int x = start; x < end; x += imgWidth) {
				gc.drawImage(img, 0, 0, img.getBounds().width,
						img.getBounds().height, x,
						1 + (height - imgHeight) / 2, imgWidth, imgHeight);
			}

		} else {
			imgHeight = (int) Math.round((double) imgHeight * hScale);
			imgWidth = (int) Math.round((double) imgWidth * hScale);
			int start = (int) Math.round((width / 2) - (imgWidth / 2)
					- ((Math.ceil(_periods) - 1) / 2 * imgWidth)) + 1;
			int end = (int) ((width / 2) + (imgWidth / 2) + ((Math
					.ceil(_periods) - 1) / 2 * imgWidth));
			for (int x = start; x < end; x += imgWidth) {
				gc.drawImage(img, 0, 0, img.getBounds().width,
						img.getBounds().height, x, 1, imgWidth, imgHeight);
				// 1 + (width - imgWidth) / 2, 1, imgWidth, imgHeight);
			}
		}
		if (_drawWall) {
			gc.setForeground(AcmusGraphics.BLUE);
			int x1 = (int) (width / 2 - _periods / 2 * imgWidth);
			gc.drawRectangle(x1, -1, (int) (imgWidth * _periods), height + 3);
		}

	}

}