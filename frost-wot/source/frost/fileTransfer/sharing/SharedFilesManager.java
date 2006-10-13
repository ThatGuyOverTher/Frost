/*
  UploadManager.java / Frost
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
package frost.fileTransfer.sharing;

import java.beans.*;

import frost.*;
import frost.storage.*;

public class SharedFilesManager implements PropertyChangeListener {

    private SharedFilesModel model;
    private SharedFilesPanel panel;

    public SharedFilesManager() {
        super();
    }

    public void initialize() throws StorageException {
        getPanel();
        getModel().initialize();
    }
    
    public void save() throws StorageException {
        getPanel().getTableFormat().saveTableLayout();
        getModel().save();
    }

    public void addPanelToMainFrame(MainFrame mainFrame) {
        mainFrame.addPanel("MainFrame.tabbedPane.sharing", getPanel());
        Core.frostSettings.addPropertyChangeListener(SettingsClass.DISABLE_FILESHARING, this);
        updateFileSharingStatus();
    }

    public SharedFilesPanel getPanel() {
        if (panel == null) {
            panel = new SharedFilesPanel();
            panel.setModel(getModel());
            panel.initialize();
        }
        return panel;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.DISABLE_FILESHARING)) {
            updateFileSharingStatus();
        }
    }

    private void updateFileSharingStatus() {
        boolean disableFileSharing = Core.frostSettings.getBoolValue(SettingsClass.DISABLE_FILESHARING);
        MainFrame.getInstance().setPanelEnabled("MainFrame.tabbedPane.sharing", !disableFileSharing);
    }

    public SharedFilesModel getModel() {
        if (model == null) {
            model = new SharedFilesModel();
        }
        return model;
    }
}
