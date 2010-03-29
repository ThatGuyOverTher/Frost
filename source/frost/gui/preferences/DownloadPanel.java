/*
  DownloadPanel.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.gui.preferences;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import frost.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

class DownloadPanel extends JPanel {

    public class Listener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == browseDirectoryButton) {
                browseDirectoryPressed();
            } else if (e.getSource() == browseExecButton) {
                browseExecPressed();
            }
        }
    }

    private JDialog owner = null;
    private SettingsClass settings = null;
    private Language language = null;

    private final JLabel priorityLabel = new JLabel();
    private final JTextField priorityTextField = new JTextField(6);
    private final JCheckBox enforceFrostPriorityFileDownload = new JCheckBox();

    private final JButton browseDirectoryButton = new JButton();
    private final JLabel directoryLabel = new JLabel();

    private final JTextField directoryTextField = new JTextField(20);

    private final Listener listener = new Listener();
    private final JLabel maxRetriesLabel = new JLabel();
    private final JTextField maxRetriesTextField = new JTextField(6);
    private final JTextField threadsTextField = new JTextField(6);
    private final JLabel threadsTextLabel = new JLabel();

    private final JCheckBox logDownloadsCheckBox = new JCheckBox();
    private final JCheckBox trackDownloadsCheckBox = new JCheckBox();

    private final JLabel waitTimeLabel = new JLabel();
    private final JTextField waitTimeTextField = new JTextField(6);

    private final JButton browseExecButton = new JButton();
    private final JLabel execLabel = new JLabel();
    private final JTextField execTextField = new JTextField(20);

    /**
     * @param owner the JDialog that will be used as owner of any dialog that is popped up from this panel
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected DownloadPanel(final JDialog owner, final SettingsClass settings) {
        super();

        this.owner = owner;
        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    /**
     * browseDownloadDirectoryButton Action Listener (Downloads / Browse)
     */
    private void browseDirectoryPressed() {
        final JFileChooser fc = new JFileChooser(settings.getValue(SettingsClass.DIR_LAST_USED));
        fc.setDialogTitle(language.getString("Options.downloads.filechooser.title"));
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);

        final int returnVal = fc.showOpenDialog(owner);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final String fileSeparator = System.getProperty("file.separator");
            final File file = fc.getSelectedFile();
            settings.setValue(SettingsClass.DIR_LAST_USED, file.getParent());
            directoryTextField.setText(file.getPath() + fileSeparator);
        }
    }

    private void browseExecPressed() {
        final JFileChooser fc = new JFileChooser(settings.getValue(SettingsClass.DIR_LAST_USED));
        fc.setDialogTitle(language.getString("Options.downloads.filechooser.title"));
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);

        final int returnVal = fc.showOpenDialog(owner);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            settings.setValue(SettingsClass.DIR_LAST_USED, file.getParent());
            execTextField.setText(file.getPath());
        }
    }

    private void initialize() {
        setName("DownloadPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        //We create the components
        new TextComponentClipboardMenu(directoryTextField, language);
        new TextComponentClipboardMenu(maxRetriesTextField, language);
        new TextComponentClipboardMenu(threadsTextField, language);
        new TextComponentClipboardMenu(waitTimeTextField, language);
        new TextComponentClipboardMenu(execTextField, language);

        // Adds all of the components
        final GridBagConstraints constraints = new GridBagConstraints();
        final Insets insets0555 = new Insets(0, 5, 5, 5);

        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.insets = insets0555;
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        add(directoryLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(directoryTextField, constraints);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 2;
        constraints.weightx = 0.0;
        add(browseDirectoryButton, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(maxRetriesLabel, constraints);
        constraints.gridx = 1;
        add(maxRetriesTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(waitTimeLabel, constraints);
        constraints.gridx = 1;
        add(waitTimeTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(threadsTextLabel, constraints);
        constraints.gridx = 1;
        add(threadsTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(priorityLabel, constraints);
        constraints.gridx = 1;
        add(priorityTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 3;
        constraints.insets = insets0555;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        add(logDownloadsCheckBox, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 3;
        constraints.insets = insets0555;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        add(trackDownloadsCheckBox, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 3;
        constraints.insets = insets0555;
        add(enforceFrostPriorityFileDownload, constraints);

        constraints.gridy++;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        add(execLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(execTextField, constraints);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 2;
        constraints.weightx = 0.0;
        add(browseExecButton, constraints);

        // glue
        constraints.gridy++;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);

        // Add listeners
        browseDirectoryButton.addActionListener(listener);
        browseExecButton.addActionListener(listener);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        directoryTextField.setText(settings.getValue(SettingsClass.DIR_DOWNLOAD));
        threadsTextField.setText(settings.getValue(SettingsClass.DOWNLOAD_MAX_THREADS));
        maxRetriesTextField.setText("" + settings.getIntValue(SettingsClass.DOWNLOAD_MAX_RETRIES));
        waitTimeTextField.setText("" + settings.getIntValue(SettingsClass.DOWNLOAD_WAITTIME));
        logDownloadsCheckBox.setSelected(settings.getBoolValue(SettingsClass.LOG_DOWNLOADS_ENABLED));
        trackDownloadsCheckBox.setSelected(settings.getBoolValue(SettingsClass.TRACK_DOWNLOADS_ENABLED));
        priorityTextField.setText(settings.getValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE_DOWNLOAD));
        enforceFrostPriorityFileDownload.setSelected(settings.getBoolValue(SettingsClass.FCP2_ENFORCE_FROST_PRIO_FILE_DOWNLOAD));
        execTextField.setText(settings.getValue(SettingsClass.EXEC_ON_DOWNLOAD));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        final String minutes = language.getString("Options.common.minutes");

        waitTimeLabel.setText(language.getString("Options.downloads.waittimeAfterEachTry") + " (" + minutes + ")");
        maxRetriesLabel.setText(language.getString("Options.downloads.maximumNumberOfRetries"));

        logDownloadsCheckBox.setText(language.getString("Options.downloads.logDownloads"));
        trackDownloadsCheckBox.setText(language.getString("Options.downloads.trackDownloads"));

        directoryLabel.setText(language.getString("Options.downloads.downloadDirectory"));
        browseDirectoryButton.setText(language.getString("Common.browse") + "...");
        threadsTextLabel.setText(language.getString("Options.downloads.numberOfSimultaneousDownloads") + " (3)");
        priorityLabel.setText(language.getString("Options.downloads.downloadPriority") + " (3)");
        enforceFrostPriorityFileDownload.setText(language.getString("Options.downloads.enforceFrostPriorityFileDownload"));

        execLabel.setText(language.getString("Options.downloads.downloadExec"));
        browseExecButton.setText(language.getString("Common.browse") + "...");
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        final String downlDirTxt = directoryTextField.getText();
        final String filesep = System.getProperty("file.separator");
        // always append a fileseparator to the end of string
        if ((!(downlDirTxt.lastIndexOf(filesep) == (downlDirTxt.length() - 1)))
            || downlDirTxt.lastIndexOf(filesep) < 0) {
                settings.setValue(SettingsClass.DIR_DOWNLOAD, downlDirTxt + filesep);
        } else {
            settings.setValue(SettingsClass.DIR_DOWNLOAD, downlDirTxt);
        }
        settings.setValue(SettingsClass.DOWNLOAD_MAX_THREADS, threadsTextField.getText());

        settings.setValue(SettingsClass.DOWNLOAD_MAX_RETRIES, maxRetriesTextField.getText());
        settings.setValue(SettingsClass.DOWNLOAD_WAITTIME, waitTimeTextField.getText());

        settings.setValue(SettingsClass.LOG_DOWNLOADS_ENABLED, logDownloadsCheckBox.isSelected());
        settings.setValue(SettingsClass.TRACK_DOWNLOADS_ENABLED, trackDownloadsCheckBox.isSelected());
        settings.setValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE_DOWNLOAD, priorityTextField.getText());
        settings.setValue(SettingsClass.FCP2_ENFORCE_FROST_PRIO_FILE_DOWNLOAD, enforceFrostPriorityFileDownload.isSelected());

        settings.setValue(SettingsClass.EXEC_ON_DOWNLOAD, execTextField.getText());
    }
}
