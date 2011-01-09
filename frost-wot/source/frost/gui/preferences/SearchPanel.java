/*
  SearchPanel.java / Frost
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

import javax.swing.*;

import frost.*;
import frost.storage.perst.filelist.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

@SuppressWarnings("serial")
class SearchPanel extends JPanel {

    private SettingsClass settings = null;
    private Language language = null;

    private final JLabel archiveExtensionLabel = new JLabel();

    private final JTextField archiveExtensionTextField = new JTextField();
    private final JLabel audioExtensionLabel = new JLabel();
    private final JTextField audioExtensionTextField = new JTextField();
    private final JLabel documentExtensionLabel = new JLabel();
    private final JTextField documentExtensionTextField = new JTextField();
    private final JLabel executableExtensionLabel = new JLabel();
    private final JTextField executableExtensionTextField = new JTextField();

    private final JLabel imageExtensionLabel = new JLabel();
    private final JTextField imageExtensionTextField = new JTextField();
    private final JLabel maxSearchResultsLabel = new JLabel();
    private final JTextField maxSearchResultsTextField = new JTextField(8);
    private final JLabel videoExtensionLabel = new JLabel();
    private final JTextField videoExtensionTextField = new JTextField();

    private final JCheckBox disableFilesharingCheckBox = new JCheckBox();
    private final JCheckBox rememberSharedFileDownloadedCheckBox = new JCheckBox();
    private final JButton resetHiddenFilesButton = new JButton();

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected SearchPanel(final SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    private void initialize() {
        setName("SearchPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        // We create the components
        new TextComponentClipboardMenu(archiveExtensionTextField, language);
        new TextComponentClipboardMenu(audioExtensionTextField, language);
        new TextComponentClipboardMenu(documentExtensionTextField, language);
        new TextComponentClipboardMenu(executableExtensionTextField, language);

        new TextComponentClipboardMenu(imageExtensionTextField, language);
        new TextComponentClipboardMenu(maxSearchResultsTextField, language);
        new TextComponentClipboardMenu(videoExtensionTextField, language);

        // Adds all of the components
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 5, 5, 5);
        constraints.weighty = 0;
        constraints.gridwidth = 1;
        constraints.gridy = 0;

        constraints.weightx = 0;
        constraints.gridx = 0;
        add(imageExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(imageExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(videoExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(videoExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(archiveExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(archiveExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(documentExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(documentExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(audioExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(audioExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(executableExtensionLabel, constraints);
        constraints.weightx = 1;
        constraints.gridx = 1;
        add(executableExtensionTextField, constraints);

        constraints.gridy++;
        constraints.weightx = 0;
        constraints.gridx = 0;
        add(maxSearchResultsLabel, constraints);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 1;
        add(maxSearchResultsTextField, constraints);

        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        add(disableFilesharingCheckBox, constraints);

        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        add(rememberSharedFileDownloadedCheckBox, constraints);

        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        add(resetHiddenFilesButton, constraints);

        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);

        resetHiddenFilesButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                resetHiddenFiles();
            }
        });
    }

    private void resetHiddenFiles() {
        final int answer = JOptionPane.showConfirmDialog(
                Core.getInstance().getMainFrame(),
                language.getString("Options.search.confirmResetHiddenFiles.body"),
                language.getString("Options.search.confirmResetHiddenFiles.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (answer ==JOptionPane.NO_OPTION) {
            return;
        }

        // update the filelistfiles in database, but not in Swing thread
        new Thread() {
            @Override
            public void run() {
                FileListStorage.inst().resetHiddenFiles();
            }
        }.start();

        // disable button
        resetHiddenFilesButton.setEnabled(false);
    }

    /**
     * Loads the settings of this panel
     */
    private void loadSettings() {
        audioExtensionTextField.setText(settings.getValue(SettingsClass.FILEEXTENSION_AUDIO));
        imageExtensionTextField.setText(settings.getValue(SettingsClass.FILEEXTENSION_IMAGE));
        videoExtensionTextField.setText(settings.getValue(SettingsClass.FILEEXTENSION_VIDEO));
        documentExtensionTextField.setText(settings.getValue(SettingsClass.FILEEXTENSION_DOCUMENT));
        executableExtensionTextField.setText(settings.getValue(SettingsClass.FILEEXTENSION_EXECUTABLE));
        archiveExtensionTextField.setText(settings.getValue(SettingsClass.FILEEXTENSION_ARCHIVE));
        maxSearchResultsTextField.setText(Integer.toString(settings.getIntValue(SettingsClass.SEARCH_MAX_RESULTS)));
        disableFilesharingCheckBox.setSelected(settings.getBoolValue(SettingsClass.DISABLE_FILESHARING));
        rememberSharedFileDownloadedCheckBox.setSelected(settings.getBoolValue(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        imageExtensionLabel.setText(language.getString("Options.search.imageExtension"));
        videoExtensionLabel.setText(language.getString("Options.search.videoExtension"));
        archiveExtensionLabel.setText(language.getString("Options.search.archiveExtension"));
        documentExtensionLabel.setText(language.getString("Options.search.documentExtension"));
        audioExtensionLabel.setText(language.getString("Options.search.audioExtension"));
        executableExtensionLabel.setText(language.getString("Options.search.executableExtension"));
        maxSearchResultsLabel.setText(language.getString("Options.search.maximumSearchResults"));

        disableFilesharingCheckBox.setText(language.getString("Options.search.disableFilesharing"));
        rememberSharedFileDownloadedCheckBox.setText(language.getString("Options.search.rememberSharedFileDownloaded"));

        resetHiddenFilesButton.setText(language.getString("Options.search.resetHiddenFiles") + " (" + FileListStorage.inst().getHiddenFilesCount()+")");
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue(SettingsClass.FILEEXTENSION_AUDIO, audioExtensionTextField.getText().toLowerCase());
        settings.setValue(SettingsClass.FILEEXTENSION_IMAGE, imageExtensionTextField.getText().toLowerCase());
        settings.setValue(SettingsClass.FILEEXTENSION_VIDEO, videoExtensionTextField.getText().toLowerCase());
        settings.setValue(SettingsClass.FILEEXTENSION_DOCUMENT, documentExtensionTextField.getText().toLowerCase());
        settings.setValue(SettingsClass.FILEEXTENSION_EXECUTABLE, executableExtensionTextField.getText().toLowerCase());
        settings.setValue(SettingsClass.FILEEXTENSION_ARCHIVE, archiveExtensionTextField.getText().toLowerCase());
        settings.setValue(SettingsClass.SEARCH_MAX_RESULTS, maxSearchResultsTextField.getText());

        settings.setValue(SettingsClass.DISABLE_FILESHARING, disableFilesharingCheckBox.isSelected());
        settings.setValue(SettingsClass.REMEMBER_SHAREDFILE_DOWNLOADED, rememberSharedFileDownloadedCheckBox.isSelected());
    }
}
