package acmus.audio;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class Calibrate extends Composite {
  
  Meter _meter;
  Button _record;
  
  public Calibrate(Composite parent, int style) {
    super(parent, style);
    
    GridLayout gl = new GridLayout(1, false);
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    this.setLayout(gl);
    
    GridData gridData;
    
    _meter = new Meter(this, SWT.NONE);
    gridData = new GridData(GridData.FILL_VERTICAL);
    _meter.setLayoutData(gridData);
    
    _record = new Button(this, SWT.CENTER);
    _record.setText("Rec");
    
    
  }

}
