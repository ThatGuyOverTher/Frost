/*
  MiscPanel.java / Frost
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
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

@SuppressWarnings("serial")
class MiscPanel extends JPanel {

    private class Listener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == enableLoggingCheckBox) {
                refreshLoggingState();
            }
        }
    }

    private SettingsClass settings = null;
    private Language language = null;

    private final JLabel autoSaveIntervalLabel = new JLabel();
    private final JTextField autoSaveIntervalTextField = new JTextField(8);
    private final JCheckBox autoSaveLocalIdentitiesCheckBox = new JCheckBox();
    private final JLabel freenetNodeLabel = new JLabel();
    private final JTextField freenetNodeTextField = new JTextField();

    private final JLabel browserAddressLabel = new JLabel();
    private final JTextField browserAddressTextField = new JTextField();

    private final JCheckBox useDDACheckBox = new JCheckBox();
    private final JCheckBox usePersistenceCheckBox = new JCheckBox();
    private final JCheckBox enableLoggingCheckBox = new JCheckBox();

    private final Listener listener = new Listener();
    private final JLabel logFileSizeLabel = new JLabel();
    private final JTextField logFileSizeTextField = new JTextField(8);

    private JTranslatableComboBox logLevelComboBox = null;
    private final JLabel logLevelLabel = new JLabel();
    private final JCheckBox showSystrayIconCheckBox = new JCheckBox();
    private final JCheckBox minimizeToSystrayCheckBox = new JCheckBox();
    private final JCheckBox splashScreenCheckBox = new JCheckBox();
    private final JCheckBox compactDatabaseAtNextStartupCheckBox = new JCheckBox();

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected MiscPanel(final SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    private JPanel getLoggingPanel() {
        final JPanel subPanel = new JPanel(new GridBagLayout());

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        final Insets insets5035 = new Insets(5, 0, 5, 5);
        final Insets insets0_30_5_5 = new Insets(0, 30, 5, 5);
        final Insets insets0_5_5_5 = new Insets(0, 5, 5, 5);
        final Insets insets0_5_5_0 = new Insets(0, 5, 3, 0);
        constraints.insets = insets5035;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 4;

        subPanel.add(enableLoggingCheckBox, constraints);

        constraints.insets = insets0_30_5_5;
        constraints.gridwidth = 1;
        constraints.gridy++;
        subPanel.add(logLevelLabel, constraints);
        constraints.gridx = 1;
        final String[] searchComboBoxKeys =
            { Logging.VERY_LOW, Logging.LOW, Logging.MEDIUM, Logging.HIGH, Logging.VERY_HIGH };
        logLevelComboBox = new JTranslatableComboBox(language, searchComboBoxKeys);
        constraints.insets = insets0_5_5_5;
        subPanel.add(logLevelComboBox, constraints);

        constraints.gridx = 2;
        constraints.insets = insets0_30_5_5;
        subPanel.add(logFileSizeLabel, constraints);
        constraints.insets = insets0_5_5_0;
        constraints.gridx = 3;
        constraints.weightx = 0;
        subPanel.add(logFileSizeTextField, constraints);

        return subPanel;
    }

    private void initialize() {
        setName("MiscPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        // We create the components
        new TextComponentClipboardMenu(autoSaveIntervalTextField, language);
        new TextComponentClipboardMenu(freenetNodeTextField, language);
        new TextComponentClipboardMenu(logFileSizeTextField, language);
        new TextComponentClipboardMenu(browserAddressTextField, language);

        // Adds all of the components
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        final Insets insets5555 = new Insets(5, 5, 5, 5);
        final Insets insets0555 = new Insets(0, 5, 5, 5);

        constraints.weightx = 0;
        constraints.insets = insets5555;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        add(freenetNodeLabel, constraints);
        constraints.gridy++;
        constraints.weightx = 1;
        add(freenetNodeTextField, constraints);
        constraints.weightx = 0;

        constraints.insets = insets0555;

        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy++;
        add(useDDACheckBox, constraints);

        constraints.gridy++;
        add(usePersistenceCheckBox, constraints);

        constraints.insets = insets5555;

        constraints.weightx = 0;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy++;
        add(autoSaveIntervalLabel, constraints);
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.NONE;
        add(autoSaveIntervalTextField, constraints);

        constraints.weightx = 0;
        constraints.gridwidth = 2;
        constraints.gridx = 0;

        constraints.gridy++;
        add(autoSaveLocalIdentitiesCheckBox, constraints);

        constraints.insets = insets0555;

        constraints.gridy++;
        add(splashScreenCheckBox, constraints);
        constraints.gridy++;
        add(showSystrayIconCheckBox, constraints);
        constraints.gridy++;
        add(minimizeToSystrayCheckBox, constraints);
        constraints.gridy++;
        add(compactDatabaseAtNextStartupCheckBox, constraints);

        constraints.insets = new Insets(0,5,0,5);
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 3;
        add(getLoggingPanel(), constraints);

        constraints.insets = insets0555;

        // browserAddress
        constraints.gridy++;
        constraints.gridwidth = 2;
        add(browserAddressLabel, constraints);
        constraints.gridy++;
        constraints.weightx = 3;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(browserAddressTextField, constraints);
        constraints.weightx = 0;

        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);
        

        // Add listeners
        enableLoggingCheckBox.addActionListener(listener);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        showSystrayIconCheckBox.setSelected(settings.getBoolValue(SettingsClass.SHOW_SYSTRAY_ICON));
        minimizeToSystrayCheckBox.setSelected(settings.getBoolValue(SettingsClass.MINIMIZE_TO_SYSTRAY));
        compactDatabaseAtNextStartupCheckBox.setSelected(settings.getBoolValue(SettingsClass.PERST_COMPACT_STORAGES));
        freenetNodeTextField.setText(settings.getValue(SettingsClass.FREENET_FCP_ADDRESS));
        autoSaveIntervalTextField.setText(Integer.toString(settings.getIntValue(SettingsClass.AUTO_SAVE_INTERVAL)));
        autoSaveLocalIdentitiesCheckBox.setSelected(settings.getBoolValue(SettingsClass.AUTO_SAVE_LOCAL_IDENTITIES));
        enableLoggingCheckBox.setSelected(settings.getBoolValue(SettingsClass.LOG_TO_FILE));
        logFileSizeTextField.setText(Integer.toString(settings.getIntValue(SettingsClass.LOG_FILE_SIZE_LIMIT)));

        logLevelComboBox.setSelectedKey(settings.getDefaultValue(SettingsClass.LOG_LEVEL));
        logLevelComboBox.setSelectedKey(settings.getValue(SettingsClass.LOG_LEVEL));

        splashScreenCheckBox.setSelected(settings.getBoolValue(SettingsClass.DISABLE_SPLASHSCREEN));
        useDDACheckBox.setSelected(settings.getBoolValue(SettingsClass.FCP2_USE_DDA));
        usePersistenceCheckBox.setSelected(settings.getBoolValue(SettingsClass.FCP2_USE_PERSISTENCE));
        
        browserAddressTextField.setText(settings.getValue(SettingsClass.BROWSER_ADDRESS));

        refreshLoggingState();
    }

    public void ok() {

        final String nodes = freenetNodeTextField.getText();
        if( nodes.indexOf(",") > -1 ) {
            if( usePersistenceCheckBox.isSelected() ) {
                MiscToolkit.showMessage(
                        "Persistence is not possible with more than 1 node. Persistence disabled.",
                        JOptionPane.ERROR_MESSAGE,
                        "Warning: Persistence is not possible");
                usePersistenceCheckBox.setSelected(false);
            }
        }
        
        final String broserAddress = browserAddressTextField.getText();
        if( broserAddress.length() > 0) {
        	if( ! broserAddress.matches( "^https?://.+")) {
        		System.out.println("### INFO - Unrecognized URI format: "+ broserAddress);
        		browserAddressTextField.setText("");
        	} else if( ! broserAddress.endsWith("/") ){
        		browserAddressTextField.setText(broserAddress + "/");
        	}
        }

        saveSettings();
    }

    private void refreshLanguage() {
        freenetNodeLabel.setText(language.getString("Options.miscellaneous.freenetFcpNode"));
        useDDACheckBox.setText(language.getString("Options.miscellaneous.useDDA"));
        useDDACheckBox.setToolTipText(language.getString("Options.miscellaneous.useDDA.tooltip"));

        usePersistenceCheckBox.setText(language.getString("Options.miscellaneous.usePersistence"));

        autoSaveIntervalLabel.setText(language.getString("Options.miscellaneous.automaticSavingInterval") +
                " (60 "+language.getString("Options.common.minutes")+")");
        autoSaveLocalIdentitiesCheckBox.setText(language.getString("Options.miscellaneous.autoSaveLocalIdentities"));
        splashScreenCheckBox.setText(language.getString("Options.miscellaneous.disableSplashscreen"));
        showSystrayIconCheckBox.setText(language.getString("Options.miscellaneous.showSysTrayIcon"));
        minimizeToSystrayCheckBox.setText(language.getString("Options.miscellaneous.minimizeToSystray"));
        compactDatabaseAtNextStartupCheckBox.setText(language.getString("Options.miscellaneous.compactStoragesDuringNextStartup"));
        enableLoggingCheckBox.setText(language.getString("Options.miscellaneous.enableLogging"));
        logLevelLabel.setText(language.getString("Options.miscellaneous.loggingLevel") +
                    " (" + language.getString("Options.miscellaneous.logLevel.low") + ") ");
        logFileSizeLabel.setText(language.getString("Options.miscellaneous.logFileSizeLimit"));

        browserAddressLabel.setText(language.getString("Options.miscellaneous.browserAddress"));
    }

    private void refreshLoggingState() {
        final boolean enableLogging = enableLoggingCheckBox.isSelected();
        logLevelLabel.setEnabled(enableLogging);
        logLevelComboBox.setEnabled(enableLogging);
        logFileSizeLabel.setEnabled(enableLogging);
        logFileSizeTextField.setEnabled(enableLogging);
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue(SettingsClass.FREENET_FCP_ADDRESS, freenetNodeTextField.getText());
        settings.setValue(SettingsClass.SHOW_SYSTRAY_ICON, showSystrayIconCheckBox.isSelected());
        settings.setValue(SettingsClass.MINIMIZE_TO_SYSTRAY, minimizeToSystrayCheckBox.isSelected());
        settings.setValue(SettingsClass.PERST_COMPACT_STORAGES, compactDatabaseAtNextStartupCheckBox.isSelected());
        settings.setValue(SettingsClass.AUTO_SAVE_INTERVAL, autoSaveIntervalTextField.getText());
        settings.setValue(SettingsClass.AUTO_SAVE_LOCAL_IDENTITIES, autoSaveLocalIdentitiesCheckBox.isSelected());
        settings.setValue(SettingsClass.LOG_TO_FILE, enableLoggingCheckBox.isSelected());
        settings.setValue(SettingsClass.LOG_FILE_SIZE_LIMIT, logFileSizeTextField.getText());
        settings.setValue(SettingsClass.LOG_LEVEL, logLevelComboBox.getSelectedKey());
        settings.setValue(SettingsClass.DISABLE_SPLASHSCREEN, splashScreenCheckBox.isSelected());
        settings.setValue(SettingsClass.FCP2_USE_DDA, useDDACheckBox.isSelected());
        settings.setValue(SettingsClass.FCP2_USE_PERSISTENCE, usePersistenceCheckBox.isSelected());
        settings.setValue(SettingsClass.BROWSER_ADDRESS, browserAddressTextField.getText());
    }
}
