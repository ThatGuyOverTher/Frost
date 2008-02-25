/*
  DisplayPanel.java / Frost
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

import frost.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * Display Panel. Contains appereance options
 */
class DisplayPanel extends JPanel {

    public class Listener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() == messageBodyButton) {
                messageBodyButtonPressed();
            }
            if (e.getSource() == messageListButton) {
                messageListButtonPressed();
            }
            if (e.getSource() == fileListButton) {
                fileListButtonPressed();
            }
        }
    }

    private JDialog owner = null;
    private SettingsClass settings = null;
    private Language language = null;

    private final Listener listener = new Listener();

    private final JLabel fontsLabel = new JLabel();

    private final JCheckBox saveSortStatesCheckBox = new JCheckBox();
    private final JCheckBox showColoredRowsCheckBox = new JCheckBox();

    private final JCheckBox confirmMarkAllMsgsReadCheckBox = new JCheckBox();

    private final JLabel messageBodyLabel = new JLabel();
    private final JLabel fileListLabel = new JLabel();
    private final JLabel messageListLabel = new JLabel();

    private final JButton fileListButton = new JButton();
    private final JButton messageListButton = new JButton();
    private final JButton messageBodyButton = new JButton();

    private final JLabel selectedFileListFontLabel = new JLabel();
    private final JLabel selectedMessageBodyFontLabel = new JLabel();
    private final JLabel selectedMessageListFontLabel = new JLabel();

    private Font selectedBodyFont = null;
    private Font selectedFileListFont = null;
    private Font selectedMessageListFont = null;

    /**
     * @param owner the JDialog that will be used as owner of any dialog that is popped up from this panel
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected DisplayPanel(final JDialog owner, final SettingsClass settings) {
        super();

        this.owner = owner;
        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    public void cancel() {
    }

    private void fileListButtonPressed() {
        final FontChooser fontChooser = new FontChooser(owner, language);
        fontChooser.setModal(true);
        fontChooser.setSelectedFont(selectedFileListFont);
        fontChooser.setVisible(true);
        final Font selectedFontTemp = fontChooser.getSelectedFont();
        if (selectedFontTemp != null) {
            selectedFileListFont = selectedFontTemp;
            selectedFileListFontLabel.setText(getFontLabel(selectedFileListFont));
        }
    }

    private String getFontLabel(final Font font) {
        if (font == null) {
            return "";
        } else {
            final StringBuilder returnValue = new StringBuilder();
            returnValue.append(font.getFamily());
            if (font.isBold()) {
                returnValue.append(" " + language.getString("Options.display.fontChooser.bold"));
            }
            if (font.isItalic()) {
                returnValue.append(" " + language.getString("Options.display.fontChooser.italic"));
            }
            returnValue.append(", " + font.getSize());
            return returnValue.toString();
        }
    }

    private JPanel getFontsPanel() {
        final JPanel fontsPanel = new JPanel(new GridBagLayout());
        fontsPanel.setBorder(new EmptyBorder(5, 20, 5, 5));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        final Insets inset1515 = new Insets(1, 5, 1, 5);
        final Insets inset1519 = new Insets(1, 5, 1, 9);

        constraints.insets = inset1515;
        constraints.gridx = 0;
        constraints.gridy = 0;
        fontsPanel.add(messageBodyLabel, constraints);
        constraints.insets = inset1519;
        constraints.gridx = 1;
        constraints.gridy = 0;
        fontsPanel.add(messageBodyButton, constraints);
        constraints.insets = inset1515;
        constraints.gridx = 2;
        constraints.gridy = 0;
        fontsPanel.add(selectedMessageBodyFontLabel, constraints);

        constraints.insets = inset1515;
        constraints.gridx = 0;
        constraints.gridy = 1;
        fontsPanel.add(messageListLabel, constraints);
        constraints.insets = inset1519;
        constraints.gridx = 1;
        constraints.gridy = 1;
        fontsPanel.add(messageListButton, constraints);
        constraints.insets = inset1515;
        constraints.gridx = 2;
        constraints.gridy = 1;
        fontsPanel.add(selectedMessageListFontLabel, constraints);

        constraints.insets = inset1515;
        constraints.gridx = 0;
        constraints.gridy = 2;
        fontsPanel.add(fileListLabel, constraints);
        constraints.insets = inset1519;
        constraints.gridx = 1;
        constraints.gridy = 2;
        fontsPanel.add(fileListButton, constraints);
        constraints.insets = inset1515;
        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        fontsPanel.add(selectedFileListFontLabel, constraints);

        return fontsPanel;
    }

    /**
     * Initialize the class.
     */
    private void initialize() {
        setName("DisplayPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        //Adds all of the components
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        final Insets inset5511 = new Insets(5, 5, 1, 1);
        final Insets insets2 = new Insets(15,5,1,1);

        constraints.insets = inset5511;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(fontsLabel, constraints);

        constraints.gridy++;
        add(getFontsPanel(), constraints);

        constraints.insets = insets2;

        constraints.gridy++;
        add(showColoredRowsCheckBox, constraints);

        constraints.insets = inset5511;

        constraints.gridy++;
        add(saveSortStatesCheckBox, constraints);

        constraints.insets = insets2;
        constraints.gridy++;
        add(confirmMarkAllMsgsReadCheckBox, constraints);

        constraints.gridy++;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JLabel(""), constraints);

        // add listeners
        messageBodyButton.addActionListener(listener);
        messageListButton.addActionListener(listener);
        fileListButton.addActionListener(listener);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
        String fontName = settings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
        int fontSize = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
        int fontStyle = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
        selectedBodyFont = new Font(fontName, fontStyle, fontSize);
        selectedMessageBodyFontLabel.setText(getFontLabel(selectedBodyFont));

        fontName = settings.getValue(SettingsClass.MESSAGE_LIST_FONT_NAME);
        fontSize = settings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_SIZE);
        fontStyle = settings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_STYLE);
        selectedMessageListFont = new Font(fontName, fontStyle, fontSize);
        selectedMessageListFontLabel.setText(getFontLabel(selectedMessageListFont));

        fontName = settings.getValue(SettingsClass.FILE_LIST_FONT_NAME);
        fontSize = settings.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
        fontStyle = settings.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
        selectedFileListFont = new Font(fontName, fontStyle, fontSize);
        selectedFileListFontLabel.setText(getFontLabel(selectedFileListFont));

        saveSortStatesCheckBox.setSelected(settings.getBoolValue(SettingsClass.SAVE_SORT_STATES));
        showColoredRowsCheckBox.setSelected(settings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS));
        confirmMarkAllMsgsReadCheckBox.setSelected(settings.getBoolValue(SettingsClass.CONFIRM_MARK_ALL_MSGS_READ));
    }

    private void messageBodyButtonPressed() {
        final FontChooser fontChooser = new FontChooser(owner, language);
        fontChooser.setModal(true);
        fontChooser.setSelectedFont(selectedBodyFont);
        fontChooser.setVisible(true);
        final Font selectedFontTemp = fontChooser.getSelectedFont();
        if (selectedFontTemp != null) {
            selectedBodyFont = selectedFontTemp;
            selectedMessageBodyFontLabel.setText(getFontLabel(selectedBodyFont));
        }
    }

    private void messageListButtonPressed() {
        final FontChooser fontChooser = new FontChooser(owner, language);
        fontChooser.setModal(true);
        fontChooser.setSelectedFont(selectedMessageListFont);
        fontChooser.setVisible(true);
        final Font selectedFontTemp = fontChooser.getSelectedFont();
        if (selectedFontTemp != null) {
            selectedMessageListFont = selectedFontTemp;
            selectedMessageListFontLabel.setText(getFontLabel(selectedMessageListFont));
        }
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
        final String choose = language.getString("Options.display.choose");
        fontsLabel.setText(language.getString("Options.display.fonts"));
        messageBodyLabel.setText(language.getString("Options.display.messageBody"));
        messageBodyButton.setText(choose);
        selectedMessageBodyFontLabel.setText(getFontLabel(selectedBodyFont));
        messageListLabel.setText(language.getString("Options.display.messageList"));
        messageListButton.setText(choose);
        selectedMessageListFontLabel.setText(getFontLabel(selectedMessageListFont));
        fileListLabel.setText(language.getString("Options.display.fileList"));
        fileListButton.setText(choose);
        selectedFileListFontLabel.setText(getFontLabel(selectedFileListFont));
        saveSortStatesCheckBox.setText(language.getString("Options.display.saveSortStates"));
        showColoredRowsCheckBox.setText(language.getString("Options.display.showColoredRows"));
        confirmMarkAllMsgsReadCheckBox.setText(language.getString("Options.display.confirmMarkAllMsgsRead"));
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        if (selectedBodyFont != null) {
            settings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, selectedBodyFont.getFamily());
            settings.setValue(SettingsClass.MESSAGE_BODY_FONT_STYLE, selectedBodyFont.getStyle());
            settings.setValue(SettingsClass.MESSAGE_BODY_FONT_SIZE, selectedBodyFont.getSize());
        }
        if (selectedMessageListFont != null) {
            settings.setValue(SettingsClass.MESSAGE_LIST_FONT_NAME, selectedMessageListFont.getFamily());
            settings.setValue(SettingsClass.MESSAGE_LIST_FONT_STYLE, selectedMessageListFont.getStyle());
            settings.setValue(SettingsClass.MESSAGE_LIST_FONT_SIZE, selectedMessageListFont.getSize());
        }
        if (selectedFileListFont != null) {
            settings.setValue(SettingsClass.FILE_LIST_FONT_NAME, selectedFileListFont.getFamily());
            settings.setValue(SettingsClass.FILE_LIST_FONT_STYLE, selectedFileListFont.getStyle());
            settings.setValue(SettingsClass.FILE_LIST_FONT_SIZE, selectedFileListFont.getSize());
        }
        settings.setValue(SettingsClass.SAVE_SORT_STATES, saveSortStatesCheckBox.isSelected());
        settings.setValue(SettingsClass.SHOW_COLORED_ROWS, showColoredRowsCheckBox.isSelected());
        settings.setValue(SettingsClass.CONFIRM_MARK_ALL_MSGS_READ, confirmMarkAllMsgsReadCheckBox.isSelected());
    }
}
