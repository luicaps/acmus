package acmus.editor;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import acmus.AcmusGraphics;

public class MultiplePositionEditorControl extends Composite {
  
  boolean _readOnly;
  IDocumentListener _docListener;
  
  PositionEditorControl _posEditor;
  Combo _floorPlans;
  Button _bNew;
  
  Map<String,FloorPlanData> _floorPlanData;
  List<FloorPlanData> _floorPlanDataIndex;
  
  boolean _isDirty;
  
  int _selection;
  
  SelectionListener _floorplanListener;
  SelectionListener _positionListener;
  
  public MultiplePositionEditorControl(Composite parent, int style, IDocumentListener docListener, boolean readOnly) {
    super(parent, style);
    _readOnly =readOnly;
    _docListener = docListener;
    
    _floorPlanData = new HashMap<String,FloorPlanData>();
    _floorPlanDataIndex = new ArrayList<FloorPlanData>(); 
    
    FormToolkit toolkit = new FormToolkit(AcmusGraphics.FORMCOLORS);
    Form form = toolkit.createForm(this);

    FillLayout fl = new FillLayout();
    fl.marginHeight=1;
    fl.marginWidth = 1;
    setLayout(fl);
    setBackground(toolkit.getColors().getBorderColor());

    GridData gridData;
    int cols;   
    
//    setBackground(bgColor);

    Composite body = form.getBody();
    
    Label l = toolkit.createLabel(body, "Floor Plan:");

    if (_readOnly) {
      _floorPlans = new Combo(body, SWT.READ_ONLY);
      cols = 2;
    }
    else {
      _floorPlans = new Combo(body, SWT.NONE);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      _floorPlans.setLayoutData(gridData);
      _bNew = toolkit.createButton(body, "New floor plan", SWT.NONE);
      gridData = new GridData(SWT.END, SWT.BOTTOM, false, false);
      _bNew.setLayoutData(gridData);
      _bNew.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent event) {
          FloorPlanData fpd = new FloorPlanData(""+ _floorPlanDataIndex.size());
          fpd.name("New" + _floorPlanDataIndex.size());
          _floorPlanDataIndex.add(fpd);
          _floorPlans.add(fpd.name());
          _floorPlans.select(_floorPlanDataIndex.size()-1);
          loadSelectedFloorPlan();
          setDirty(true);
        }
      });
      cols = 3;
    }

    toolkit.paintBordersFor(body);

    GridLayout layout = new GridLayout(cols, false);
    layout.verticalSpacing = 10;
    layout.marginWidth = 10;
    body.setLayout(layout);
    
    _posEditor = new PositionEditorControl(body, SWT.NONE, _docListener, _readOnly, toolkit);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = cols;
    _posEditor.setLayoutData(gridData);
    
    
    _floorPlans.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        updateSelectedFloorPlan();
        if (_floorplanListener != null)
        _floorplanListener.widgetSelected(e);
      }
    });
    
    _floorPlans.addModifyListener(new ModifyListener () {

      public void modifyText(ModifyEvent e) {
        int sel = _floorPlans.getSelectionIndex();
        if (sel < 0) {
        String name = _floorPlans.getText() ;
        if (!name.trim().equals("") && !name.equals(_floorPlanDataIndex.get(_selection).name())) {
          _floorPlanDataIndex.get(_selection).name(name);
          _floorPlans.setItem(_selection, name);
          setDirty(true);
        }
        }
      }
      
    });
    
  }

  public void setFloorplanSelectionListener(SelectionListener sl) {
    _floorplanListener = sl;
  }
  // not used yet...
  public void setPositionSelectionListener(SelectionListener sl) {
    _positionListener = sl;
  }
  
  public boolean isDirty() {
    return _isDirty;
  }

  public void setDirty(boolean dirty) {
    if (_isDirty != dirty) {
      _isDirty = dirty;
    }
    if (_isDirty && _docListener != null) {
      _docListener.documentChanged(null);
    }
  }
  
  public void select(int index) {
    _posEditor.select(index);
  }
  
  public String[] getSelectedPosition() {
    String res[] = new String[2];
    if (_selection >=0) {
      res[0] = _selection + "";
      res[1] = "0";
    }
    else {
      res[0] = _selection + "";
      Position p = _posEditor.getSelection();
      if (p!=null) res[1] = p.id() + "";
      else res[1] = "0";
    }
    return res;
  }

  public void selectFloorPlan(int index) {
    _floorPlans.select(index);
    updateSelectedFloorPlan();
  }
  public void updateSelectedFloorPlan() {
    _selection = _floorPlans.getSelectionIndex();
    loadSelectedFloorPlan();
  }
  
  public FloorPlanData getSelectedFloorPlanData() {
    return _floorPlanDataIndex.get(_floorPlans.getSelectionIndex());
  }
  public void loadSelectedFloorPlan() {
    _posEditor.save();
    //System.out.println("aaa " + getSelectedFloorPlanData().imageFile() + " "+ getSelectedFloorPlanData().id());
    _posEditor.load(getSelectedFloorPlanData());
  }

  // FIXME: remendo... implementar tratamento de erros!
  private void readErrorMsg(int line, String msg) {
    System.err.println("l. " + line + " " + msg);
  }

  public void read(Reader r) {

    Properties floorPlanProps = new Properties();
    
    try {
      BufferedReader br = new BufferedReader(r);
      int lineNum = 1;
      String line = br.readLine();

      Pattern p = Pattern.compile("^\\s*\\[\\s*(.*)\\s*\\]\\s*");
      Matcher m = null;
      while (line != null) {
        m = p.matcher(line);
        if (!line.trim().equals("") && m.matches())
          break;
        line = br.readLine();
        lineNum++;
      }

      if (line == null)
        return;

      String section = m.group(1);

      while (line != null) {
        if (section.startsWith("floor plan")) {

          String idFloorPlan = section.substring(11).trim();
          
          floorPlanProps.clear();
          floorPlanProps.setProperty("name", "");
          floorPlanProps.setProperty("width", "");
          floorPlanProps.setProperty("height", "");
          floorPlanProps.setProperty("file", "");
          
          
          FloorPlanData fpd;
          
          if (_floorPlanData.containsKey(idFloorPlan)) {
            readErrorMsg(lineNum, "redefining " + idFloorPlan);
            fpd = _floorPlanData.get(idFloorPlan);
          }
          else {
            fpd = new FloorPlanData(idFloorPlan);
            _floorPlanData.put(idFloorPlan, fpd);
          }
          
          Pattern p2 = Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.+)\\s*");
          line = br.readLine();
          lineNum++;

          while (line != null) {
            Matcher m2 = p2.matcher(line);
            if (m2.matches()) {
              String prop = m2.group(1);
              String val = m2.group(2);
              if (floorPlanProps.containsKey(prop)) {
                floorPlanProps.setProperty(prop, val);
              } else {
                readErrorMsg(lineNum, "Unknown prop: " + prop);
              }
            } else {
              Matcher m3 = p.matcher(line);
              if (m3.matches()) {
                section = m3.group(1);
                break;
              } else {
                if (!line.trim().equals(""))
                  readErrorMsg(lineNum, "Ignoring: " + line);
              }
            }
            line = br.readLine();
            lineNum++;
          }
          
          fpd.name(floorPlanProps.getProperty("name"));
          fpd.width(Integer.parseInt(floorPlanProps.getProperty("width")));
          fpd.height(Integer.parseInt(floorPlanProps.getProperty("height")));
          fpd.imageFile(floorPlanProps.getProperty("file"));

          _floorPlans.add(fpd.name());
          _floorPlanDataIndex.add(fpd);
          
        } else if (section.startsWith("positions")) {

          String idFloorPlan = section.substring(10).trim();
          FloorPlanData fpd;
          if (_floorPlanData.containsKey(idFloorPlan)) {
            fpd = _floorPlanData.get(idFloorPlan);
          }
          else {
            fpd = new FloorPlanData(idFloorPlan);
            _floorPlanData.put(idFloorPlan, fpd);
          }

          Pattern p2 = Pattern
              .compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(.*)\\s*");
          line = br.readLine();
          lineNum++;
          while (line != null) {
            Matcher m2 = p2.matcher(line);
            if (m2.matches()) {
              int id = Integer.parseInt(m2.group(1));
              int x = Integer.parseInt(m2.group(2));
              int y = Integer.parseInt(m2.group(3));
              String name = m2.group(4);
              Position pos = new Position(id, name, x, y);
              fpd.add(pos);
            } else {
              Matcher m3 = p.matcher(line);
              if (m3.matches()) {
                section = m3.group(1);
                break;
              } else {
                if (!line.trim().equals(""))
                  readErrorMsg(lineNum, "Ignoring: " + line);
              }
            }
            line = br.readLine();
            lineNum++;
          }
        }
      }

      
      _floorPlans.select(0);
      loadSelectedFloorPlan();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write(PrintStream out) {
    _posEditor.save();
    for (int i = 0; i < _floorPlanDataIndex.size(); i++) {
      FloorPlanData fpd = _floorPlanDataIndex.get(i);
      out.println("[floor plan "+ i +  "]");
      out.println("name = " + fpd.name());
      out.println("width = " + fpd.width());
      out.println("height = " + fpd.height());
      out.println("file = " + fpd.imageFile());
      out.println();
      out.println("[positions " + i + "]");
      for (Position p: fpd) {
        out.println(p.id() + " " + p.x() + " " + p.y() + " " + p.name());
      }
      out.println();
    }
  }

  public Iterator iterator(String floorPlan) {
    return _floorPlanData.get(floorPlan).iterator();
    
  }
}
