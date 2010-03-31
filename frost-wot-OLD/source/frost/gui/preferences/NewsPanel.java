/*
  NewsPanel.java / Frost
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
import javax.swing.border.*;
import javax.swing.event.*;

import frost.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

class NewsPanel extends JPanel {

    private class Listener implements ActionListener, ChangeListener {
        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == automaticBoardUpdateCheckBox) {
                refreshUpdateState();
            }
        }
        public void stateChanged(final ChangeEvent e) {
            if (e.getSource() == altEditCheckBox) {
                altEditChanged();
            }
        }
    }

    private SettingsClass settings = null;
    private Language language = null;

    private final JLabel uploadPrioLabel = new JLabel();
    private final JTextField uploadPrioTextField = new JTextField(8);
    private final JLabel downloadPrioLabel = new JLabel();
    private final JTextField downloadPrioTextField = new JTextField(8);


    private final JLabel displayDaysLabel = new JLabel();
    private final JTextField displayDaysTextField = new JTextField(8);

    private final JLabel downloadDaysLabel = new JLabel();
    private final JTextField downloadDaysTextField = new JTextField(8);

    private final JLabel messageBaseLabel = new JLabel();
    private final JTextField messageBaseTextField = new JTextField(16);


    private final JCheckBox alwaysDownloadBackloadCheckBox = new JCheckBox();

    private final JCheckBox automaticBoardUpdateCheckBox = new JCheckBox();
    private JPanel updatePanel = null;
    private final JLabel minimumIntervalLabel = new JLabel();
    private final JTextField minimumIntervalTextField = new JTextField(8);

    private final JLabel concurrentUpdatesLabel = new JLabel();
    private final JTextField concurrentUpdatesTextField = new JTextField(8);

    private final JCheckBox storeSentMessagesCheckBox = new JCheckBox();
    private final JCheckBox silentlyRetryCheckBox = new JCheckBox();
    private final JCheckBox acceptSignatureFormatV1CheckBox = new JCheckBox();


    private final JCheckBox altEditCheckBox = new JCheckBox();
    private final JTextField altEditTextField = new JTextField();

    private final Listener listener = new Listener();

    // 0.7 only
    private final JCheckBox useOneConnectionForMessagesCheckBox = new JCheckBox();

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected NewsPanel(final SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    private JPanel getUpdatePanel() {
        if( updatePanel == null ) {
            updatePanel = new JPanel(new GridBagLayout());
            updatePanel.setBorder(new EmptyBorder(0, 30, 0, 5));
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(0, 5, 5, 5);
            constraints.weighty = 0;
            constraints.weightx = 0;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridy = 0;

            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridx = 0;
            constraints.weightx = 0.5;
            updatePanel.add(minimumIntervalLabel, constraints);
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 1;
            constraints.weightx = 1;
            updatePanel.add(minimumIntervalTextField, constraints);

            constraints.insets = new Insets(0, 5, 0, 5); // we have a bottom inset in the containing layout!
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridx = 0;
            constraints.gridy++;
            constraints.weightx = 0.5;
            updatePanel.add(concurrentUpdatesLabel, constraints);
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 1;
            constraints.weightx = 1;
            updatePanel.add(concurrentUpdatesTextField, constraints);
        }
        return updatePanel;
    }

    private void initialize() {
        setName("NewsPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        // We create the components
        new TextComponentClipboardMenu(uploadPrioTextField, language);
        new TextComponentClipboardMenu(downloadPrioTextField, language);
        new TextComponentClipboardMenu(displayDaysTextField, language);
        new TextComponentClipboardMenu(downloadDaysTextField, language);
        new TextComponentClipboardMenu(messageBaseTextField, language);
        new TextComponentClipboardMenu(minimumIntervalTextField, language);
        new TextComponentClipboardMenu(concurrentUpdatesTextField, language);
        new TextComponentClipboardMenu(altEditTextField, language);

        // Adds all of the components
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weighty = 0.0;
        constraints.weightx = 0;

        constraints.insets = new Insets(0, 5, 5, 5);
        constraints.gridy = 0;

        constraints.gridx = 0;
        add(displayDaysLabel, constraints);
        constraints.gridx = 1;
        add(displayDaysTextField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        add(downloadDaysLabel, constraints);
        constraints.gridx = 1;
        add(downloadDaysTextField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth=2;
        add(alwaysDownloadBackloadCheckBox, constraints);
        constraints.gridwidth=1;

        constraints.gridx = 0;
        constraints.gridy++;
        add(messageBaseLabel, constraints);
        constraints.gridx = 1;
        add(messageBaseTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(uploadPrioLabel, constraints);
        constraints.gridx = 1;
        add(uploadPrioTextField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        add(downloadPrioLabel, constraints);
        constraints.gridx = 1;
        add(downloadPrioTextField, constraints);
        constraints.gridx = 0;

        constraints.gridwidth = 2;

        constraints.gridy++;
        add(automaticBoardUpdateCheckBox, constraints);

        constraints.gridy++;
        add(getUpdatePanel(), constraints);

        constraints.gridy++;
        add(useOneConnectionForMessagesCheckBox, constraints);

        constraints.gridy++;
        add(storeSentMessagesCheckBox, constraints);

        constraints.gridy++;
        add(silentlyRetryCheckBox, constraints);

        constraints.gridy++;
        add(acceptSignatureFormatV1CheckBox, constraints);

        constraints.gridwidth = 1;

        constraints.insets = new Insets(0, 5, 0, 5);

        constraints.gridy++;
        constraints.gridx = 0;
        add(altEditCheckBox, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(altEditTextField, constraints);
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;

        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);

        // Add listeners
        automaticBoardUpdateCheckBox.addActionListener(listener);
        altEditCheckBox.addChangeListener(listener);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        uploadPrioTextField.setText(settings.getValue(SettingsClass.FCP2_DEFAULT_PRIO_MESSAGE_UPLOAD));
        downloadPrioTextField.setText(settings.getValue(SettingsClass.FCP2_DEFAULT_PRIO_MESSAGE_DOWNLOAD));
        useOneConnectionForMessagesCheckBox.setSelected(settings.getBoolValue(SettingsClass.FCP2_USE_ONE_CONNECTION_FOR_MESSAGES));

        displayDaysTextField.setText(settings.getValue(SettingsClass.MAX_MESSAGE_DISPLAY));
        downloadDaysTextField.setText(settings.getValue(SettingsClass.MAX_MESSAGE_DOWNLOAD));
        messageBaseTextField.setText(settings.getValue(SettingsClass.MESSAGE_BASE));
        alwaysDownloadBackloadCheckBox.setSelected(settings.getBoolValue(SettingsClass.ALWAYS_DOWNLOAD_MESSAGES_BACKLOAD));

        minimumIntervalTextField.setText(settings.getValue(SettingsClass.BOARD_AUTOUPDATE_MIN_INTERVAL));
        concurrentUpdatesTextField.setText(settings.getValue(SettingsClass.BOARD_AUTOUPDATE_CONCURRENT_UPDATES));

        // this setting is in MainFrame
        automaticBoardUpdateCheckBox.setSelected(MainFrame.getInstance().isAutomaticBoardUpdateEnabled());
        refreshUpdateState();

        storeSentMessagesCheckBox.setSelected(settings.getBoolValue(SettingsClass.STORAGE_STORE_SENT_MESSAGES));
        silentlyRetryCheckBox.setSelected(settings.getBoolValue(SettingsClass.SILENTLY_RETRY_MESSAGES));
        acceptSignatureFormatV1CheckBox.setSelected(settings.getBoolValue(SettingsClass.ACCEPT_SIGNATURE_FORMAT_V1));

        altEditCheckBox.setSelected(settings.getBoolValue(SettingsClass.ALTERNATE_EDITOR_ENABLED));
        altEditTextField.setEnabled(altEditCheckBox.isSelected());
        altEditTextField.setText(settings.getValue(SettingsClass.ALTERNATE_EDITOR_COMMAND));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        uploadPrioLabel.setText(language.getString("Options.news.1.messageUploadPriority") + " (2)");
        downloadPrioLabel.setText(language.getString("Options.news.1.messageDownloadPriority") + " (2)");
        useOneConnectionForMessagesCheckBox.setText(language.getString("Options.news.1.useOneConnectionForMessages"));
        displayDaysLabel.setText(language.getString("Options.news.1.numberOfDaysToDisplay") + " (15)");
        downloadDaysLabel.setText(language.getString("Options.news.1.numberOfDaysToDownloadBackwards") + " (5)");
        messageBaseLabel.setText(language.getString("Options.news.1.messageBase") + " (news)");
        alwaysDownloadBackloadCheckBox.setText(language.getString("Options.news.1.alwaysDownloadBackload"));
        alwaysDownloadBackloadCheckBox.setToolTipText(language.getString("Options.news.1.alwaysDownloadBackload.tooltip"));

        final String minutes = language.getString("Options.common.minutes");

        minimumIntervalLabel.setText(language.getString("Options.news.3.minimumUpdateInterval") + " (" + minutes + ") (45)");
        concurrentUpdatesLabel.setText(language.getString("Options.news.3.numberOfConcurrentlyUpdatingBoards") + " (6)");

        automaticBoardUpdateCheckBox.setText(language.getString("Options.news.3.automaticBoardUpdate"));

        storeSentMessagesCheckBox.setText(language.getString("Options.news.1.storeSentMessages"));
        silentlyRetryCheckBox.setText(language.getString("Options.news.3.silentlyRetryFailedMessages"));
        acceptSignatureFormatV1CheckBox.setText(language.getString("Options.news.3.acceptSignatureFormatV1"));

        final String off = language.getString("Options.common.off");
        altEditCheckBox.setText(language.getString("Options.miscellaneous.useEditorForWritingMessages") + " (" + off + ")");
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue(SettingsClass.FCP2_DEFAULT_PRIO_MESSAGE_UPLOAD, uploadPrioTextField.getText());
        settings.setValue(SettingsClass.FCP2_DEFAULT_PRIO_MESSAGE_DOWNLOAD, downloadPrioTextField.getText());
        settings.setValue(SettingsClass.FCP2_USE_ONE_CONNECTION_FOR_MESSAGES, useOneConnectionForMessagesCheckBox.isSelected());

        settings.setValue(SettingsClass.MAX_MESSAGE_DISPLAY, displayDaysTextField.getText());
        settings.setValue(SettingsClass.MAX_MESSAGE_DOWNLOAD, downloadDaysTextField.getText());
        settings.setValue(SettingsClass.MESSAGE_BASE, messageBaseTextField.getText().trim().toLowerCase());
        settings.setValue(SettingsClass.ALWAYS_DOWNLOAD_MESSAGES_BACKLOAD, alwaysDownloadBackloadCheckBox.isSelected());

        settings.setValue(SettingsClass.BOARD_AUTOUPDATE_CONCURRENT_UPDATES, concurrentUpdatesTextField.getText());
        settings.setValue(SettingsClass.BOARD_AUTOUPDATE_MIN_INTERVAL, minimumIntervalTextField.getText());

        // settings.setValue(SettingsClass.BOARD_AUTOUPDATE_ENABLED, automaticBoardUpdateCheckBox.isSelected());
        // we change setting in MainFrame, this is auto-saved during frostSettings.save()
        MainFrame.getInstance().setAutomaticBoardUpdateEnabled(automaticBoardUpdateCheckBox.isSelected());

        settings.setValue(SettingsClass.STORAGE_STORE_SENT_MESSAGES, storeSentMessagesCheckBox.isSelected());
        settings.setValue(SettingsClass.SILENTLY_RETRY_MESSAGES, silentlyRetryCheckBox.isSelected());
        settings.setValue(SettingsClass.ACCEPT_SIGNATURE_FORMAT_V1, acceptSignatureFormatV1CheckBox.isSelected());

        settings.setValue(SettingsClass.ALTERNATE_EDITOR_ENABLED, altEditCheckBox.isSelected());
        settings.setValue(SettingsClass.ALTERNATE_EDITOR_COMMAND, altEditTextField.getText());
    }

    private void refreshUpdateState() {
        MiscToolkit.setContainerEnabled(getUpdatePanel(), automaticBoardUpdateCheckBox.isSelected());
    }

    private void altEditChanged() {
        altEditTextField.setEnabled(altEditCheckBox.isSelected());
    }
}
