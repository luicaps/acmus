/*
 *  AcmusWorkbenchAdvisor.java
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
package acmus;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.AboutInfo;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor;

public class AcmusWorkbenchAdvisor extends IDEWorkbenchAdvisor {

  @Override
  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor
   */
  public String getInitialWindowPerspectiveId() {
      int index = PlatformUI.getWorkbench().getWorkbenchWindowCount() - 1;

      String perspectiveId = null;
      AboutInfo[] welcomeInfos = getWelcomePerspectiveInfos();
      if (index >= 0 && welcomeInfos != null
              && index < welcomeInfos.length) {
          perspectiveId = welcomeInfos[index].getWelcomePerspectiveId();
      }
      if (perspectiveId == null) {
         //perspectiveId = IDE.RESOURCE_PERSPECTIVE_ID;
        perspectiveId = "acmus.perspective";
      }
      return perspectiveId;
  }

//  public String getInitialWindowPerspectiveId() {
//    return "acmus.perspective";
//  }
  
}
