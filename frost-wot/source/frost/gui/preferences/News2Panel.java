/*
  News2Panel.java / Frost
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
import frost.util.gui.*;
import frost.util.gui.translation.*;

class News2Panel extends JPanel {

    private class Listener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == blockSubjectCheckBox) {
                blockSubjectPressed();
            }
            if (e.getSource() == blockBodyCheckBox) {
                blockBodyPressed();
            }
            if (e.getSource() == blockBoardCheckBox) {
                blockBoardPressed();
            }
        }
    }

    private SettingsClass settings = null;
    private Language language = null;

    private final JCheckBox blockBoardCheckBox = new JCheckBox();
    private final JTextArea blockBoardTextField = new JTextArea(2,0);
    private final JCheckBox blockBodyCheckBox = new JCheckBox();
    private final JTextArea blockBodyTextField = new JTextArea(2,0);
    private final JCheckBox blockSubjectCheckBox = new JCheckBox();
    private final JTextArea blockSubjectTextField = new JTextArea(2,0);

    private final JCheckBox hideBadMessagesCheckBox = new JCheckBox();
    private final JCheckBox hideCheckMessagesCheckBox = new JCheckBox();
    private final JCheckBox hideObserveMessagesCheckBox = new JCheckBox();
    private final JCheckBox hideUnsignedMessagesCheckBox = new JCheckBox();

    private final JCheckBox blockBoardsFromBadCheckBox = new JCheckBox();
    private final JCheckBox blockBoardsFromCheckCheckBox = new JCheckBox();
    private final JCheckBox blockBoardsFromObserveCheckBox = new JCheckBox();
    private final JCheckBox blockBoardsFromUnsignedCheckBox = new JCheckBox();

    private final JLabel hideMessagesLabel = new JLabel();
    private final JLabel blockBoardsLabel = new JLabel();

    private final Listener listener = new Listener();

    /**
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected News2Panel(final SettingsClass settings) {
        super();

        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    private void blockBoardPressed() {
        blockBoardTextField.setEnabled(blockBoardCheckBox.isSelected());
    }

    private void blockBodyPressed() {
        blockBodyTextField.setEnabled(blockBodyCheckBox.isSelected());
    }

    private void blockSubjectPressed() {
        blockSubjectTextField.setEnabled(blockSubjectCheckBox.isSelected());
    }

    private JPanel getHideMessagesPanel() {
        final JPanel hidePanel = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();

        constraints.anchor = GridBagConstraints.NORTHWEST;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.gridwidth = 4;
        hidePanel.add(hideMessagesLabel, constraints);

        constraints.gridy++;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;

        constraints.gridx = 0;
        constraints.insets = new Insets(5, 20, 5, 5);
        hidePanel.add(hideObserveMessagesCheckBox, constraints);

        constraints.gridx = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        hidePanel.add(hideCheckMessagesCheckBox, constraints);

        constraints.gridx = 2;
        hidePanel.add(hideBadMessagesCheckBox, constraints);

        constraints.gridx = 3;
        hidePanel.add(hideUnsignedMessagesCheckBox, constraints);

        return hidePanel;
    }

    private JPanel getBlockBoardsPanel() {
        final JPanel blockBoardsPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();

        constraints.anchor = GridBagConstraints.NORTHWEST;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.gridwidth = 4;
        blockBoardsPanel.add(blockBoardsLabel, constraints);

        constraints.gridy++;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;

        constraints.gridx = 0;
        constraints.insets = new Insets(5, 20, 5, 5);
        blockBoardsPanel.add(blockBoardsFromObserveCheckBox, constraints);

        constraints.gridx = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        blockBoardsPanel.add(blockBoardsFromCheckCheckBox, constraints);

        constraints.gridx = 2;
        blockBoardsPanel.add(blockBoardsFromBadCheckBox, constraints);

        constraints.gridx = 3;
        blockBoardsPanel.add(blockBoardsFromUnsignedCheckBox, constraints);

        return blockBoardsPanel;
    }

    private void initialize() {
        setName("News2Panel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        // We create the components
        new TextComponentClipboardMenu(blockBodyTextField, language);
        new TextComponentClipboardMenu(blockBoardTextField, language);
        new TextComponentClipboardMenu(blockSubjectTextField, language);

        blockBoardTextField.setLineWrap(true);
        blockBodyTextField.setLineWrap(true);
        blockSubjectTextField.setLineWrap(true);

        JScrollPane sp;

        // Adds all of the components
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        final Insets insets5555 = new Insets(5, 5, 5, 5);
        final Insets insets0_30_5_5 = new Insets(0, 30, 5, 5);
        constraints.insets = insets5555;
        constraints.gridwidth = 2;

        constraints.gridx = 0;
        constraints.gridy = 0;
        add(blockSubjectCheckBox, constraints);
        constraints.insets = insets0_30_5_5;
        constraints.gridy++;
        constraints.weighty = 0.9;
        sp = new JScrollPane(blockSubjectTextField);
        add(sp, constraints);
        constraints.weighty = 0;

        constraints.insets = insets5555;
        constraints.gridy++;
        add(blockBodyCheckBox, constraints);
        constraints.insets = insets0_30_5_5;
        constraints.gridy++;
        constraints.weighty = 0.9;
        sp = new JScrollPane(blockBodyTextField);
        add(sp, constraints);
        constraints.weighty = 0;

        constraints.insets = insets5555;
        constraints.gridy++;
        add(blockBoardCheckBox, constraints);
        constraints.insets = insets0_30_5_5;
        constraints.gridy++;
        constraints.weighty = 0.9;
        sp = new JScrollPane(blockBoardTextField);
        add(sp, constraints);
        constraints.weighty = 0;

        constraints.insets = insets5555;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy++;
        add(getHideMessagesPanel(), constraints);

        constraints.insets = insets5555;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy++;
        add(getBlockBoardsPanel(), constraints);

        // glue
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JLabel(""), constraints);

        // Add listeners
        blockSubjectCheckBox.addActionListener(listener);
        blockBodyCheckBox.addActionListener(listener);
        blockBoardCheckBox.addActionListener(listener);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        hideUnsignedMessagesCheckBox.setSelected(settings.getBoolValue(SettingsClass.MESSAGE_HIDE_UNSIGNED));
        hideBadMessagesCheckBox.setSelected(settings.getBoolValue(SettingsClass.MESSAGE_HIDE_BAD));
        hideCheckMessagesCheckBox.setSelected(settings.getBoolValue(SettingsClass.MESSAGE_HIDE_CHECK));
        hideObserveMessagesCheckBox.setSelected(settings.getBoolValue(SettingsClass.MESSAGE_HIDE_OBSERVE));

        blockBoardsFromUnsignedCheckBox.setSelected(settings.getBoolValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_UNSIGNED));
        blockBoardsFromBadCheckBox.setSelected(settings.getBoolValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_BAD));
        blockBoardsFromCheckCheckBox.setSelected(settings.getBoolValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_CHECK));
        blockBoardsFromObserveCheckBox.setSelected(settings.getBoolValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_OBSERVE));

        blockSubjectCheckBox.setSelected(settings.getBoolValue(SettingsClass.MESSAGE_BLOCK_SUBJECT_ENABLED));
        blockSubjectTextField.setEnabled(blockSubjectCheckBox.isSelected());
        blockSubjectTextField.setText(settings.getValue(SettingsClass.MESSAGE_BLOCK_SUBJECT));
        blockBodyCheckBox.setSelected(settings.getBoolValue(SettingsClass.MESSAGE_BLOCK_BODY_ENABLED));
        blockBodyTextField.setEnabled(blockBodyCheckBox.isSelected());
        blockBodyTextField.setText(settings.getValue(SettingsClass.MESSAGE_BLOCK_BODY));
        blockBoardCheckBox.setSelected(settings.getBoolValue(SettingsClass.MESSAGE_BLOCK_BOARDNAME_ENABLED));
        blockBoardTextField.setEnabled(blockBoardCheckBox.isSelected());
        blockBoardTextField.setText(settings.getValue(SettingsClass.MESSAGE_BLOCK_BOARDNAME));
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        hideMessagesLabel.setText(language.getString("Options.news.2.hideMessagesWithTrustStates")+":");
        hideUnsignedMessagesCheckBox.setText(language.getString("Options.news.2.trustState.none"));
        hideBadMessagesCheckBox.setText(language.getString("Options.news.2.trustState.bad"));
        hideCheckMessagesCheckBox.setText(language.getString("Options.news.2.trustState.check"));
        hideObserveMessagesCheckBox.setText(language.getString("Options.news.2.trustState.observe"));

        blockBoardsLabel.setText(language.getString("Options.news.2.dontAddBoardsFromTrustStates")+":");
        blockBoardsFromUnsignedCheckBox.setText(language.getString("Options.news.2.trustState.none"));
        blockBoardsFromBadCheckBox.setText(language.getString("Options.news.2.trustState.bad"));
        blockBoardsFromCheckCheckBox.setText(language.getString("Options.news.2.trustState.check"));
        blockBoardsFromObserveCheckBox.setText(language.getString("Options.news.2.trustState.observe"));

        blockSubjectCheckBox.setText(language.getString("Options.news.2.blockMessagesWithSubject")+ ": ");
        blockBodyCheckBox.setText(language.getString("Options.news.2.blockMessagesWithBody")+ ": ");
        blockBoardCheckBox.setText(language.getString("Options.news.2.blockMessagesWithTheseBoards")+ ": ");
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        settings.setValue(SettingsClass.MESSAGE_BLOCK_SUBJECT, ((blockSubjectTextField.getText()).trim()).toLowerCase());
        settings.setValue(SettingsClass.MESSAGE_BLOCK_SUBJECT_ENABLED, blockSubjectCheckBox.isSelected());
        settings.setValue(SettingsClass.MESSAGE_BLOCK_BODY, ((blockBodyTextField.getText()).trim()).toLowerCase());
        settings.setValue(SettingsClass.MESSAGE_BLOCK_BODY_ENABLED, blockBodyCheckBox.isSelected());
        settings.setValue(SettingsClass.MESSAGE_BLOCK_BOARDNAME, ((blockBoardTextField.getText()).trim()).toLowerCase());
        settings.setValue(SettingsClass.MESSAGE_BLOCK_BOARDNAME_ENABLED, blockBoardCheckBox.isSelected());

        settings.setValue(SettingsClass.MESSAGE_HIDE_UNSIGNED, hideUnsignedMessagesCheckBox.isSelected());
        settings.setValue(SettingsClass.MESSAGE_HIDE_BAD, hideBadMessagesCheckBox.isSelected());
        settings.setValue(SettingsClass.MESSAGE_HIDE_CHECK, hideCheckMessagesCheckBox.isSelected());
        settings.setValue(SettingsClass.MESSAGE_HIDE_OBSERVE, hideObserveMessagesCheckBox.isSelected());

        settings.setValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_UNSIGNED, blockBoardsFromUnsignedCheckBox.isSelected());
        settings.setValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_BAD, blockBoardsFromBadCheckBox.isSelected());
        settings.setValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_CHECK, blockBoardsFromCheckCheckBox.isSelected());
        settings.setValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_OBSERVE, blockBoardsFromObserveCheckBox.isSelected());
    }
}
