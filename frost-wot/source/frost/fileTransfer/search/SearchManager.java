/*
  SearchManager.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.fileTransfer.search;

import java.beans.*;

import frost.*;

public class SearchManager implements PropertyChangeListener {

    private SearchModel model;
    private SearchPanel panel;

    public SearchManager() {
        super();
    }

    public void initialize() {
        getPanel();
    }
    
    public void addPanelToMainFrame(MainFrame mainFrame) {
        mainFrame.addPanel("MainFrame.tabbedPane.search", getPanel());
        Core.frostSettings.addPropertyChangeListener(SettingsClass.DISABLE_FILESHARING, this);
        updateDownloadStatus();
    }

    public SearchPanel getPanel() {
        if (panel == null) {
            panel = new SearchPanel(this);
            panel.setModel(getModel());
            panel.initialize();
        }
        return panel;
    }

    public SearchModel getModel() {
        if (model == null) {
            model = new SearchModel();
        }
        return model;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.DISABLE_FILESHARING)) {
            updateDownloadStatus();
        }
    }

    private void updateDownloadStatus() {
        boolean disableFileSharing = Core.frostSettings.getBoolValue(SettingsClass.DISABLE_FILESHARING);
        MainFrame.getInstance().setPanelEnabled("MainFrame.tabbedPane.search", !disableFileSharing);
    }
}
