/*
 *  TextReader.java
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
 * Created on Jun 29, 2006
 */
package acmus.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author lku
 * 
 */
public class TextReader {
  BufferedReader br;
  int line;

  public TextReader(String file) throws FileNotFoundException {
    this(new FileReader(file));
  }
  public TextReader(Reader reader) {
    this(new BufferedReader(reader));
  }
  public TextReader(BufferedReader reader) {
    br= reader;
    line = 0;
  }
  
  public String readLine() throws IOException {
    String l = null;
    do {
      l = br.readLine();
      line++;
      if (l == null)
        break;
      int i = l.indexOf('#');
      if (i >= 0)
        l = l.substring(0, i);
    } while (l.trim().equals(""));
    return l;
  }

  public int lineNumber() {
    return line;
  }

  public void close() throws Exception {
    br.close();
  }
}
