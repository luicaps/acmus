package acmus.editor;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import acmus.AcmusGraphics;
import acmus.AcmusPlugin;

public class PositionEditorControl extends Composite implements
    Iterable<Position> {

  Image _floorPlan;
  Properties _floorPlanProps;
//  Map _posFileSections;
  int _fpWidth;
  int _fpHeight;
  int _fpDestWidth;
  int _fpDestHeight;
  String _imageFile;

  Composite cImage;

  Button fBChooseImage;
  Button fBAdd;
  Button fBRemove;
  Button fBUp;
  Button fBDown;


  FileDialog _fdChooseImage;

  boolean _isDirty;

  IDocumentListener _docListener;
  boolean _readOnly;

  double _fpXScale = 1;
  double _fpYScale = 1;

  FloorPlanData _fpData;

  boolean _drawLabels = false;
  int _labelPos = 2;

  public PositionEditorControl(Composite parent, int style,
      IDocumentListener docListener, boolean readOnly, FormToolkit toolkit) {    
    super(parent, style);
    _readOnly = readOnly;
    _docListener = docListener;

    FillLayout fl = new FillLayout();
    setLayout(fl);
    setBackground(AcmusGraphics.BLACK);

    Form form = toolkit.createForm(this);
    Composite body = form.getBody();
    body.setLayout(new FillLayout());
    toolkit.paintBordersFor(body);

    GridLayout gridLayout;
    GridData gridData;
    
    SashForm sf = new SashForm(body, SWT.HORIZONTAL);
    sf.setVisible(true);
    //sf.setBackground(toolkit.getColors().getBackground());
    toolkit.adapt(sf);

    Composite gImage = toolkit.createComposite(sf);
    cImage = toolkit.createComposite(gImage, SWT.NONE);
    fl = new FillLayout();
    fl.marginWidth = 1;
    fl.marginHeight = 1;
    gImage.setLayout(fl);
    gImage.setBackground(toolkit.getColors().getBorderColor());
    cImage.setBackground(AcmusGraphics.WHITE);

    Composite cControls = toolkit.createComposite(sf, SWT.NONE);

    int weights[] = { 2, 1 };
    sf.setWeights(weights);

    gridLayout = new GridLayout(1, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    cControls.setLayout(gridLayout);

    if (!_readOnly) {

      fBChooseImage = toolkit.createButton(cControls,"Choose image", SWT.CENTER);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      // gridData.horizontalSpan = 2;
      fBChooseImage.setLayoutData(gridData);

      _fdChooseImage = new FileDialog(this.getShell(), SWT.OPEN);
      fBChooseImage.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent event) {
          String fn = _fdChooseImage.open();
          loadFloorPlan(fn);
          _floorPlanProps.setProperty("file", fn);
          cImage.redraw();
          setDirty(true);
        }
      });
      //Group gPositions = new Group(cControls, SWT.SHADOW_ETCHED_IN);      
      Section section = toolkit.createSection(cControls,Section.TITLE_BAR);
      section.setText("Positions");
      section.clientVerticalSpacing = 0;
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      section.setLayoutData(gridData);

      Composite gPositions = toolkit.createComposite(cControls);      
      gridData = new GridData(GridData.FILL_BOTH);
      gPositions.setLayoutData(gridData);
      gPositions.setLayout(new GridLayout(2, true));
      
      createTableViewer(gPositions, _readOnly, toolkit);

      fBUp = toolkit.createButton(gPositions, "Up", SWT.CENTER);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      fBUp.setLayoutData(gridData);
      fBUp.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent event) {
          up();
          cImage.redraw();
        }
      });

      fBDown = toolkit.createButton(gPositions, "Down", SWT.CENTER);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      fBDown.setLayoutData(gridData);
      fBDown.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent event) {
          down();
          cImage.redraw();
        }
      });

      fBAdd = toolkit.createButton(gPositions, "Add", SWT.CENTER);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      fBAdd.setLayoutData(gridData);
      fBAdd.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent event) {
          createPosition(-1, -1);
        }
      });

      fBRemove = toolkit.createButton(gPositions, "Remove", SWT.CENTER);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      fBRemove.setLayoutData(gridData);
      fBRemove.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent event) {
          removeSelected();
          cImage.redraw();
        }
      });

    } else {
      createTableViewer(cControls, _readOnly, toolkit);
    }

    PositionsListener pl = new PositionsListener(cImage, _readOnly);
    cImage.addPaintListener(pl);
    cImage.addMouseListener(pl);
    cImage.addMouseMoveListener(pl);

    fTPos.addSelectionChangedListener(pl);

    _floorPlan = null;

    _floorPlanProps = new Properties();
    _floorPlanProps.setProperty("file", "");
    _floorPlanProps.setProperty("width", "0");
    _floorPlanProps.setProperty("height", "0");

    _isDirty = false;

    // if (_readOnly) {
    // fBChooseImage.setEnabled(false);
    // fBAdd.setEnabled(false);
    // fBRemove.setEnabled(false);
    // fBUp.setEnabled(false);
    // fBDown.setEnabled(false);
    // table.setEnabled(false);
    // }

    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        if (_floorPlan != null)
        _floorPlan.dispose();
      }
    });

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

  public Position getSelection() {
    if (table.getSelectionIndex() >= 0)
      return (Position) fTPos.getElementAt(table.getSelectionIndex());
    else
      return null;
  }

  public void loadFloorPlan(String filename) {
    _imageFile = filename;
    if (filename != null && !filename.trim().equals("")) {
      ImageLoader il = new ImageLoader();
      ImageData idata[] = il.load(filename);
      _floorPlan = new Image(AcmusPlugin.getDefault().getWorkbench()
          .getDisplay(), idata[0]);

      _fpDestWidth = Integer.parseInt(_floorPlanProps.getProperty("width"));
      _fpDestHeight = Integer.parseInt(_floorPlanProps.getProperty("height"));

      _fpWidth = _floorPlan.getBounds().width;
      _fpHeight = _floorPlan.getBounds().height;

      if (_fpDestWidth <= 0)
        _fpDestWidth = _fpWidth;
      if (_fpDestHeight <= 0)
        _fpDestHeight = _fpHeight;
    } else {
      _floorPlan = null;
      // _fpWidth = _fpDestWidth = cImage.getBounds().width;
      // _fpHeight = _fpDestHeight= cImage.getBounds().height;
      // _fpWidth = _fpDestWidth = 400;
      // _fpHeight = _fpDestHeight = 400;
    }
    //System.out.println("floorp " + _floorPlan);
  }

  // FIXME: remendo... implementar tratamento de erros!
  private void printReadErrorMsg(int line, String msg) {
    System.err.println("l. " + line + " " + msg);
  }

  public void load(FloorPlanData fpd) {
    _fpData = fpd;
    clearTable();
    loadFloorPlan(fpd.imageFile());
    // width, height ignored for now...

    for (Position p : fpd) {
      addPosition(p);
    }
    cImage.redraw();
  }

  public FloorPlanData save() {
    if (_fpData != null)
      save(_fpData);
    return _fpData;
  }

  public void save(FloorPlanData fpd) {
    fpd.clear();
    fpd.imageFile(_imageFile);
    for (Position p : this) {
      fpd.add(p);
    }
  }

  public void read(Reader r) {

    clearTable();

    try {
      BufferedReader br = new BufferedReader(r);
      int lineNum = 1;
      String line = br.readLine();

      Pattern p = Pattern.compile("^\\s*\\[(.*)\\]\\s*");
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
        if (section.equalsIgnoreCase("floor plan")) {

          // FIXME: what to do when "file=..." is removed?
          _floorPlanProps.setProperty("width", "0");
          _floorPlanProps.setProperty("height", "0");

          Pattern p2 = Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.+)\\s*");
          line = br.readLine();
          lineNum++;

          while (line != null) {
            Matcher m2 = p2.matcher(line);
            if (m2.matches()) {
              String prop = m2.group(1);
              String val = m2.group(2);
              if (_floorPlanProps.containsKey(prop)) {
                _floorPlanProps.setProperty(prop, val);
              } else {
                printReadErrorMsg(lineNum, "Unknown prop: " + prop);
              }
            } else {
              Matcher m3 = p.matcher(line);
              if (m3.matches()) {
                section = m3.group(1);
                break;
              } else {
                if (!line.trim().equals(""))
                  printReadErrorMsg(lineNum, "Ignoring: " + line);
              }
            }
            line = br.readLine();
            lineNum++;
          }
          loadFloorPlan(_floorPlanProps.getProperty("file"));

        } else if (section.equalsIgnoreCase("positions")) {

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
              createPosition(id, name, x, y);
            } else {
              Matcher m3 = p.matcher(line);
              if (m3.matches()) {
                section = m3.group(1);
                break;
              } else {
                if (!line.trim().equals(""))
                  printReadErrorMsg(lineNum, "Ignoring: " + line);
              }
            }
            line = br.readLine();
            lineNum++;
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void write(PrintStream out) {
    Iterator<?> it = (new TreeSet<Object>(_floorPlanProps.keySet()).iterator());
    out.println("[floor plan]");
    while (it.hasNext()) {
      String prop = (String) it.next();
      out.println(prop + " = " + _floorPlanProps.getProperty(prop));
    }
    out.println();
    out.println("[positions]");
    Iterator<Position> it2 = posIterator();
    while (it2.hasNext()) {
      Position p = it2.next();
      out.println(p.id() + " " + p.x() + " " + p.y() + " " + p.name());
    }
  }

  /* ======================================================================= */

  private Table table;
  private TableViewer fTPos;
  private int _lastId = 0;

  // Set column names
  private String[] columnNames = new String[] { "id", "name", "position" };

  private void createTableViewer(Composite parent, boolean readOnly, FormToolkit toolkit) {

    table = toolkit.createTable(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL
        | SWT.H_SCROLL);
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 2;
    table.setLayoutData(gridData);

    fTPos = new TableViewer(table);
    fTPos.setLabelProvider(new PositionLabelProvider());
    fTPos.setColumnProperties(columnNames);

    TableColumn tc = new TableColumn(table, SWT.LEFT);
    tc.setResizable(true);
    tc.setWidth(50);
    tc.setText("Id");
    tc = new TableColumn(table, SWT.LEFT);
    tc.setResizable(true);
    tc.setWidth(100);
    tc.setText("Name");
    tc = new TableColumn(table, SWT.LEFT);
    tc.setWidth(100);
    tc.setResizable(true);
    tc.setText("Position");

    if (!readOnly) {
      CellEditor[] editors = new CellEditor[3];
      editors[0] = null;
      TextCellEditor textEditor = new TextCellEditor(table);
      ((Text) textEditor.getControl()).setTextLimit(60);
      editors[1] = textEditor;
      textEditor = new TextCellEditor(table);
      ((Text) textEditor.getControl()).setTextLimit(60);
      ((Text) textEditor.getControl()).addVerifyListener(new VerifyListener() {
        public void verifyText(VerifyEvent e) {
          e.doit = e.text.matches("\\(\\d+,\\d+\\)");
        }
      });

      editors[2] = textEditor;
      fTPos.setCellEditors(editors);

      fTPos.setCellModifier(new CellModifier());
    }

  }

  public Position createPosition(int x, int y) {
    setDirty(true);
    return createPosition(++_lastId, "", x, y);
  }

  public Position createPosition(int id, int x, int y) {
    return createPosition(id, "", x, y);
  }

  public void addPosition(Position p) {
    if (p.id() > _lastId)
      _lastId = p.id();
    fTPos.remove(p);
    fTPos.add(p);
    if (!_readOnly)
      fBRemove.setEnabled(true);
  }

  public Position createPosition(int id, String name, int x, int y) {
    // System.out.println("p " + x + " " + y);
    Position p = new Position(id, name, x, y);
    addPosition(p);
    return p;
  }

  /**
   * Return the column names in a collection
   * 
   * @return List containing column names
   */
  public List<String> getColumnNames() {
    return Arrays.asList(columnNames);
  }

  public void add(Object el) {
    fTPos.add(el);
  }

  public void select(Position p) {
    fTPos.setSelection(new StructuredSelection(p));
  }

  public void select(int index) {
    select((Position) fTPos.getElementAt(index));
  }

  public void addSelectionChangedListener(ISelectionChangedListener l) {
    fTPos.addSelectionChangedListener(l);
  }

  public Position getSelected() {
    return (Position) fTPos.getElementAt(table.getSelectionIndex());
  }

  public int indexOf(Position p) {
    for (int i = 0; i < table.getItemCount(); i++) {
      if (p.equals(fTPos.getElementAt(i)))
        return i;
    }
    return -1;
  }

  public void clearTable() {
    while (table.getItemCount() > 0) {
      fTPos.remove(fTPos.getElementAt(0));
    }
    _lastId = 0;
  }

  static String[] _updatePropId = { "id" };

  public void removeSelected() {
    int sel = table.getSelectionIndex();
    fTPos.remove(fTPos.getElementAt(sel));
    _lastId--;
    if (table.getItemCount() > 0) {
      select(sel < table.getItemCount() ? sel : (sel - 1));
      for (int i = 0; i < table.getItemCount(); i++) {
        Position p = (Position) fTPos.getElementAt(i);
        p.setId(i + 1);
        fTPos.update(p, _updatePropId);
      }
    } else {
      fBRemove.setEnabled(false);
    }
    setDirty(true);
  }

  public void swap(int i, int j) {
    Position p = (Position) fTPos.getElementAt(i);
    Position q = (Position) fTPos.getElementAt(j);
    p.swap(q);
    fTPos.update(p, _updatePropId);
    fTPos.update(q, _updatePropId);
    setDirty(true);
  }

  public void up() {
    int sel = table.getSelectionIndex();
    if (sel > 0) {
      swap(sel, sel - 1);
      select(sel - 1);
    }
  }

  public void down() {
    int sel = table.getSelectionIndex();
    if (sel < table.getItemCount() - 1) {
      swap(sel, sel + 1);
      select(sel + 1);
    }
  }

  private Iterator<Position> posIterator() {
    return new PositionsIterator();
  }

  public Iterator<Position> iterator() {
    return posIterator();
  }

  class PositionsIterator implements Iterator<Position> {

    int _i = 0;

    public boolean hasNext() {
      return _i < table.getItemCount();
    }

    public Position next() {
      return (Position) (fTPos.getElementAt(_i++));
    }

    public void remove() {
    }
  }

  /* ======================================================================= */

  class PositionsListener implements PaintListener, MouseListener,
      MouseMoveListener, ISelectionChangedListener {

    boolean _add = true;

    Position _selP;
    Position _hovP;
    Composite _parent;
    boolean _readOnly;

    public PositionsListener(Composite parent, boolean readOnly) {
      _parent = parent;
      _readOnly = readOnly;
    }

    // private final void select(Position p) {
    // _curP = p;
    // table.select(indexOf(p));
    // }

    public void paintControl(PaintEvent event) {
      int width = _parent.getBounds().width;
      int height = _parent.getBounds().height;

      GC gc = event.gc;
      if (_floorPlan == null) {
        _fpXScale = _fpYScale = 1;
      } else {
        _fpXScale = (double) width / _fpDestWidth;
        _fpYScale = (double) height / _fpDestHeight;
        if (_fpXScale > _fpYScale) {
          _fpXScale = _fpYScale;
        } else {
          _fpYScale = _fpXScale;
        }
        gc.drawImage(_floorPlan, 0, 0, _fpWidth, _fpHeight, 0, 0,
            (int) (_fpWidth * _fpXScale), (int) (_fpHeight * _fpYScale));
      }

      gc.setBackground(AcmusGraphics.BLUE);
      gc.setForeground(AcmusGraphics.BLACK);
      Iterator<Position> it = posIterator();
      while (it.hasNext()) {
        Position p = it.next();
        if (_drawLabels) {
          if (!p.name().trim().equals(""))
            drawString(gc, p.name(), (int) (p.x() * _fpXScale),
                (int) (p.y() * _fpYScale), _labelPos);
        }
        gc.fillOval((int) (p.x() * _fpXScale) - 2,
            (int) (p.y() * _fpYScale) - 2, 4, 4);
        gc.drawOval((int) (p.x() * _fpXScale) - 2,
            (int) (p.y() * _fpYScale) - 2, 4, 4);
        drawString(gc, p.id() + "", (int) (p.x() * _fpXScale),
            (int) (p.y() * _fpYScale), 0);
      }

      if (_selP != null) {
        gc.drawOval((int) (_selP.x() * _fpXScale) - 4,
            (int) (_selP.y() * _fpYScale) - 4, 8, 8);
        gc.setForeground(AcmusGraphics.RED2);
        drawString(gc, _selP.name(), (int) (_selP.x() * _fpXScale),
            (int) (_selP.y() * _fpYScale), _labelPos);
      }
      if (_hovP != null) {
        gc.setForeground(AcmusGraphics.GREEN3);
        drawString(gc, _hovP.name(), (int) (_hovP.x() * _fpXScale),
            (int) (_hovP.y() * _fpYScale), _labelPos);
      }

    }

    private final void drawString(GC gc, String str, int x, int y, int pos) {
      Point strSize = gc.stringExtent(str);
      int xoffset = 0, yoffset = 0;

      switch (pos % 4) {
      case 0: // left
        xoffset = -strSize.x - 6;
        yoffset = -strSize.y / 2;
        break;
      case 1: // top
        xoffset = -strSize.x / 2;
        yoffset = -strSize.y - 2;
        break;
      case 2: // right
        xoffset = 6;
        yoffset = -strSize.y / 2;
        break;
      case 3: // bottom
        xoffset = -strSize.x / 2;
        yoffset = 6;
        break;
      }

      gc.drawString(str, x + xoffset, y + yoffset, true);
    }

    public void mouseDoubleClick(MouseEvent e) {
    }

    public void mouseDown(MouseEvent e) {
      Iterator<Position> it = posIterator();
      _add = true;
      while (it.hasNext()) {
        Position p = it.next();
        if (p.isIn((int) (e.x / _fpXScale), (int) (e.y / _fpYScale))) {
          _add = false;
          select(p);
          cImage.redraw();
          break;
        }
      }
    }

    public void mouseUp(MouseEvent e) {
      if (_add && !_readOnly) {
        Position p = createPosition((int) (e.x / _fpXScale),
            (int) (e.y / _fpYScale));
        select(p);

      } else {
        _add = true;
      }
      cImage.redraw();
    }

    public void mouseMove(MouseEvent e) {
      if (!_add && !_readOnly) {
        _selP.setX((int) Math.round(e.x / _fpXScale));
        _selP.setY((int) Math.round(e.y / _fpYScale));
        // fParent.redraw();
        cImage.redraw();
        fTPos.update(_selP, null);
        setDirty(true);
      } else {
        Iterator<Position> it = posIterator();
        Position pos = null;
        while (it.hasNext()) {
          Position p = it.next();
          if (p.isIn((int) (e.x / _fpXScale), (int) (e.y / _fpYScale))) {
            pos = p;
            break;
          }
        }
        if (pos != _hovP) {
          _hovP = pos;
          cImage.redraw();
        } else {
          _hovP = pos;
        }
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
      // select(_ptv.getSelected());
      _selP = getSelected();
      // fParent.redraw();
      cImage.redraw();
    }
  }

  /* ======================================================================= */

  class PositionLabelProvider extends LabelProvider implements
      ITableLabelProvider {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
      // TODO Auto-generated method stub
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    public String getColumnText(Object element, int columnIndex) {
      Position p = (Position) element;
      String result = null;
      switch (columnIndex) {
      case 0:
        result = "" + p.id();
        break;
      case 1:
        result = p.name();
        break;
      case 2:
        result = "(" + p.x() + "," + p.y() + ")";
        break;
      default:
        break;
      }
      return result;
    }
  }

  /* ======================================================================= */

  class CellModifier implements ICellModifier {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object,
     *      java.lang.String)
     */
    public boolean canModify(Object element, String property) {
      // Find the index of the column
      int columnIndex = getColumnNames().indexOf(property);
      if (columnIndex == 0)
        return false;
      return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
     *      java.lang.String)
     */
    public Object getValue(Object element, String property) {

      // Find the index of the column
      int columnIndex = getColumnNames().indexOf(property);

      Object result = null;
      Position pos = (Position) element;

      switch (columnIndex) {
      case 0:
        result = pos.id() + "";
        break;
      case 1:
        result = pos.name();
        break;
      case 2:
        result = "(" + pos.x() + "," + pos.y() + ")";
        break;
      default:
        result = "";
      }
      return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object,
     *      java.lang.String, java.lang.Object)
     */
    public void modify(Object element, String property, Object value) {
      // Find the index of the column
      int columnIndex = getColumnNames().indexOf(property);

      TableItem item = (TableItem) element;
      Position pos = (Position) item.getData();
      switch (columnIndex) {
      case 0:
        break;
      case 1:
        pos.setName(((String) value).trim());
        fTPos.update(pos, null);
        cImage.redraw();
        setDirty(true);
        break;
      case 2:
        //String s = (String) value;
        setDirty(true);
        break;
      default:
      }

    }
  }

}

// ============================================================================
class Position {

  int _id;
  String _name;
  int _x;
  int _y;

  public Position(int id, String name, int x, int y) {
    setAll(id, name, x, y);
  }

  public Position(int id, int x, int y) {
    this(id, "", x, y);
  }

  public final void setAll(int id, String name, int x, int y) {
    _name = name;
    _id = id;
    _x = x;
    _y = y;
  }

  public String name() {
    return _name;
  }

  public boolean isIn(int x, int y) {
    return (x <= _x + 4 && x >= _x - 4) && (y <= _y + 4 && y >= _y - 4);
  }

  public int id() {
    return _id;
  }

  public int x() {
    return _x;
  }

  public int y() {
    return _y;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setX(int x) {
    _x = x;
  }

  public void setY(int y) {
    _y = y;
  }

  public void setId(int id) {
    _id = id;
  }

  public void swap(Position q) {
    int id = q.id();
    String name = q.name();
    int x = q.x();
    int y = q.y();
    q.setAll(id, _name, _x, _y);
    this.setAll(_id, name, x, y);
  }

  public boolean equals(Object other) {
    Position p = (Position) other;
    if (p == null)
      return false;
    return (this.id() == p.id());
  }
}