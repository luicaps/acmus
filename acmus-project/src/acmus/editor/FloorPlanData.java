/*
 *  PositionFile.java
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
package acmus.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FloorPlanData implements Iterable<Position> {

  String _id;  
  
  String _name ="";  
  int _width;
  int _height;
  String _imageFile = "";
  
  List<Position> _positions;
  
  public FloorPlanData(String id) {
    _positions = new ArrayList<Position>();
    _id = id;
  }
  
  public String id() {
    return _id;
  }
  public void id(String id) {
    _id = id;
  }

  public String name() {
    return _name;
  }
  public void name(String name) {
    _name = name;
  }  
  public int height() {
    return _height;
  }
  public void height(int height) {
    this._height = height;
  }
  public String imageFile() {
    return _imageFile;
  }
  public void imageFile(String file) {
    _imageFile = file;
  }
  public List<Position> positions() {
    return _positions;
  }
  public int width() {
    return _width;
  }
  public void width(int width) {
    this._width = width;
  }

  public void add(Position p) {
    _positions.add(p);
  }

  public Iterator<Position> iterator() {
    return _positions.iterator();
  }
  public void clear() {
    _positions.clear();
    _imageFile ="";
  }

//  public void read(Reader r) throws IOException {
//    BufferedReader br = new BufferedReader(r);
//    Properties floorPlanProps= new Properties();
//    
//    int lineNum = 1;
//    String line = br.readLine();
//
//    Pattern p = Pattern.compile("^\\s*\\[(.*)\\]\\s*");
//    Matcher m = null;
//    while (line != null) {
//      m = p.matcher(line);
//      if (!line.trim().equals("") && m.matches())
//        break;
//      line = br.readLine();
//      lineNum++;
//    }
//
//    if (line == null)
//      return;
//
//    String section = m.group(1);
//
//    while (line != null) {
//      if (section.equalsIgnoreCase("floor plan")) {
//
//        floorPlanProps.setProperty("file", "");
//        floorPlanProps.setProperty("width", "0");
//        floorPlanProps.setProperty("height", "0");
//        _width = 0;
//        _height = 0;
//
//        Pattern p2 = Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.+)\\s*");
//        line = br.readLine();
//        lineNum++;
//
//        while (line != null) {
//          Matcher m2 = p2.matcher(line);
//          if (m2.matches()) {
//            String prop = m2.group(1);
//            String val = m2.group(2);
//            if (floorPlanProps.containsKey(prop)) {
//              floorPlanProps.setProperty(prop, val);
//            } else {
//              readErrorMsg(lineNum, "Unknown prop: " + prop);
//            }
//          } else {
//            Matcher m3 = p.matcher(line);
//            if (m3.matches()) {
//              section = m3.group(1);
//              break;
//            } else {
//              if (!line.trim().equals(""))
//                readErrorMsg(lineNum, "Ignoring: " + line);
//            }
//          }
//          line = br.readLine();
//          lineNum++;
//        }
//
//        _imageFile = floorPlanProps.getProperty("file", "");
//        _width = Integer.parseInt(floorPlanProps.getProperty("width", "0"));
//        _height = Integer.parseInt(floorPlanProps.getProperty("height", "0"));
//
//      } else if (section.equalsIgnoreCase("positions")) {
//        Pattern p2 = Pattern
//            .compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(.*)\\s*");
//        line = br.readLine();
//        lineNum++;
//        while (line != null) {
//          Matcher m2 = p2.matcher(line);
//          if (m2.matches()) {
//            int id = Integer.parseInt(m2.group(1));
//            int x = Integer.parseInt(m2.group(2));
//            int y = Integer.parseInt(m2.group(3));
//            String name = m2.group(4);
//            _positions.add(new Position(id, name, x, y));
//          } else {
//            Matcher m3 = p.matcher(line);
//            if (m3.matches()) {
//              section = m3.group(1);
//              break;
//            } else {
//              if (!line.trim().equals(""))
//                readErrorMsg(lineNum, "Ignoring: " + line);
//            }
//          }
//          line = br.readLine();
//          lineNum++;
//        }
//      }
//    }
//  }
//
//  public void writePositions(PrintWriter out) {
//    out.println("[floor plan]");
//    out.println("file = " + _imageFile);
//    out.println("width = " + _width);
//    out.println("height = " + _height);
//    out.println();
//    out.println("[positions]");
//    for (Position p : _positions) {
//      out.println(p.id() + " " + p.x() + " " + p.y() + " " + p.name());
//    }
//    out.flush();
//  }

  // FIXME: remendo... implementar tratamento de erros!
//  private void readErrorMsg(int line, String msg) {
//    System.err.println("l. " + line + " " + msg);
//  }

}
