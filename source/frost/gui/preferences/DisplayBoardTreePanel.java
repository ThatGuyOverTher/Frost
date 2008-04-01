/*
  DisplayBoardTreePanel.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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

public class DisplayBoardTreePanel extends JPanel {

    private class Listener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if( e.getSource() == selectedColorButton ) {
                selectedColorPressed();
            } else if( e.getSource() == notSelectedColorButton ) {
                notSelectedColorPressed();
            } else if( e.getSource() == showBoardUpdateVisualizationCheckBox ) {
                refreshUpdateState();
            } else if( e.getSource() == boardTreeButton ) {
                boardTreeButtonPressed();
            }
        }
    }

    private JDialog owner = null;
    private SettingsClass settings = null;
    private Language language = null;

    private final JCheckBox showBoardDescTooltipsCheckBox = new JCheckBox();
    private final JCheckBox showBoardUpdateCountCheckBox = new JCheckBox();
    private final JCheckBox preventBoardtreeReordering = new JCheckBox();
    private final JCheckBox showFlaggedStarredIndicators = new JCheckBox();

    private final JCheckBox showBoardUpdateVisualizationCheckBox = new JCheckBox();
    private JPanel colorPanel = null;
    private final JButton selectedColorButton = new JButton();
    private final JLabel selectedColorTextLabel = new JLabel();
    private final JLabel selectedColorLabel = new JLabel();
    private final JButton notSelectedColorButton = new JButton();
    private final JLabel notSelectedColorTextLabel = new JLabel();
    private final JLabel notSelectedColorLabel = new JLabel();
    private Color selectedColor = null;
    private Color notSelectedColor = null;

    private final Listener listener = new Listener();

    // fields for font panel
    private final JLabel boardTreeLabel = new JLabel();
    private final JButton boardTreeButton = new JButton();
    private final JLabel selectedBoardTreeFontLabel = new JLabel();
    private final JLabel fontsLabel = new JLabel();
    private Font selectedBodyFont = null;

    /**
     * @param owner the JDialog that will be used as owner of any dialog that is popped up from this panel
     * @param settings the SettingsClass instance that will be used to get and store the settings of the panel
     */
    protected DisplayBoardTreePanel(final JDialog owner, final SettingsClass settings) {
        super();

        this.owner = owner;
        this.language = Language.getInstance();
        this.settings = settings;

        initialize();
        loadSettings();
    }

    public void cancel() {
    }

    private JPanel getColorPanel() {
        if (colorPanel == null) {

            colorPanel = new JPanel(new GridBagLayout());
            colorPanel.setBorder(new EmptyBorder(5, 30, 5, 5));
            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(0, 5, 5, 5);
            constraints.weighty = 1;
            constraints.weightx = 1;
            constraints.anchor = GridBagConstraints.NORTHWEST;

            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0.5;
            colorPanel.add(selectedColorTextLabel, constraints);
            constraints.fill = GridBagConstraints.VERTICAL;
            constraints.gridx = 1;
            constraints.weightx = 0.2;
            colorPanel.add(selectedColorLabel, constraints);
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 2;
            constraints.weightx = 0.5;
            colorPanel.add(selectedColorButton, constraints);

            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.weightx = 0.5;
            colorPanel.add(notSelectedColorTextLabel, constraints);
            constraints.fill = GridBagConstraints.VERTICAL;
            constraints.gridx = 1;
            constraints.weightx = 0.2;
            colorPanel.add(notSelectedColorLabel, constraints);
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 2;
            constraints.weightx = 0.5;
            colorPanel.add(notSelectedColorButton, constraints);

            selectedColorLabel.setOpaque(true);
            notSelectedColorLabel.setOpaque(true);
            selectedColorLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
            notSelectedColorLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
            selectedColorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            notSelectedColorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return colorPanel;
    }

    /**
     * Initialize the class.
     */
    private void initialize() {
        setName("DisplayPanel");
        setLayout(new GridBagLayout());
        refreshLanguage();

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        final Insets inset5511 = new Insets(5, 5, 1, 1);

        constraints.insets = inset5511;
        constraints.gridx = 0;
        constraints.gridy = 0;

        add(fontsLabel,constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridy++;
        add(getFontsPanel(),constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy++;
        add(showBoardUpdateVisualizationCheckBox, constraints);

        constraints.gridy++;
        add(getColorPanel(), constraints);

        constraints.gridy++;
        add(showBoardUpdateCountCheckBox, constraints);

        constraints.gridy++;
        add(showBoardDescTooltipsCheckBox, constraints);

        constraints.gridy++;
        add(preventBoardtreeReordering, constraints);

        constraints.gridy++;
        add(showFlaggedStarredIndicators, constraints);

        constraints.gridy++;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JLabel(""), constraints);

        // Add listeners
        selectedColorButton.addActionListener(listener);
        notSelectedColorButton.addActionListener(listener);
        showBoardUpdateVisualizationCheckBox.addActionListener(listener);
        boardTreeButton.addActionListener(listener);
    }

    /**
     * Load the settings of this panel
     */
    private void loadSettings() {
    	String fontName = settings.getValue(SettingsClass.BOARD_TREE_FONT_NAME);
        int fontSize = settings.getIntValue(SettingsClass.BOARD_TREE_FONT_SIZE);
        int fontStyle = settings.getIntValue(SettingsClass.BOARD_TREE_FONT_STYLE);
        selectedBodyFont = new Font(fontName, fontStyle, fontSize);
        selectedBoardTreeFontLabel.setText(getFontLabel(selectedBodyFont));

        showBoardUpdateCountCheckBox.setSelected(settings.getBoolValue(SettingsClass.SHOW_BOARD_UPDATED_COUNT));
        showBoardDescTooltipsCheckBox.setSelected(settings.getBoolValue(SettingsClass.SHOW_BOARDDESC_TOOLTIPS));
        preventBoardtreeReordering.setSelected(settings.getBoolValue(SettingsClass.PREVENT_BOARDTREE_REORDERING));
        showFlaggedStarredIndicators.setSelected(settings.getBoolValue(SettingsClass.SHOW_BOARDTREE_FLAGGEDSTARRED_INDICATOR));

        showBoardUpdateVisualizationCheckBox.setSelected(settings.getBoolValue(SettingsClass.BOARD_UPDATE_VISUALIZATION_ENABLED));
        refreshUpdateState();

        selectedColor = (Color) settings.getObjectValue(SettingsClass.BOARD_UPDATE_VISUALIZATION_BGCOLOR_SELECTED);
        notSelectedColor = (Color) settings.getObjectValue(SettingsClass.BOARD_UPDATE_VISUALIZATION_BGCOLOR_NOT_SELECTED);
        selectedColorLabel.setBackground(selectedColor);
        notSelectedColorLabel.setBackground(notSelectedColor);
    }

    public void ok() {
        saveSettings();
    }

    private void refreshLanguage() {
    	fontsLabel.setText(language.getString("Options.display.fonts"));
    	boardTreeLabel.setText(language.getString("Options.display.boardTree"));
        boardTreeButton.setText(language.getString("Options.display.choose"));
        selectedBoardTreeFontLabel.setText(getFontLabel(selectedBodyFont));

        showBoardUpdateCountCheckBox.setText(language.getString("Options.display.showBoardUpdateCount"));
        showBoardDescTooltipsCheckBox.setText(language.getString("Options.display.showTooltipWithBoardDescriptionInBoardTree"));
        preventBoardtreeReordering.setText(language.getString("Options.display.preventBoardtreeReordering"));
        showFlaggedStarredIndicators.setText(language.getString("Options.display.showBoardtreeFlaggedStarredIndicators"));

        final String on = language.getString("Options.common.on");
        final String color = language.getString("Options.news.3.color");
        final String choose = language.getString("Options.news.3.choose");
        showBoardUpdateVisualizationCheckBox.setText(language.getString("Options.news.3.showBoardUpdateVisualization") + " (" + on + ")");
        selectedColorTextLabel.setText(language.getString("Options.news.3.backgroundColorIfUpdatingBoardIsSelected"));
        selectedColorLabel.setText("    " + color + "    ");
        selectedColorButton.setText(choose);
        notSelectedColorTextLabel.setText(language.getString("Options.news.3.backgroundColorIfUpdatingBoardIsNotSelected"));
        notSelectedColorLabel.setText("    " + color + "    ");
        notSelectedColorButton.setText(choose);
    }

    /**
     * Save the settings of this panel
     */
    private void saveSettings() {
        if( selectedBodyFont != null ) {
            settings.setValue(SettingsClass.BOARD_TREE_FONT_NAME, selectedBodyFont.getFamily());
            settings.setValue(SettingsClass.BOARD_TREE_FONT_STYLE, selectedBodyFont.getStyle());
            settings.setValue(SettingsClass.BOARD_TREE_FONT_SIZE, selectedBodyFont.getSize());
        }

        settings.setValue(SettingsClass.SHOW_BOARD_UPDATED_COUNT, showBoardUpdateCountCheckBox.isSelected());
        settings.setValue(SettingsClass.SHOW_BOARDDESC_TOOLTIPS, showBoardDescTooltipsCheckBox.isSelected());
        settings.setValue(SettingsClass.PREVENT_BOARDTREE_REORDERING, preventBoardtreeReordering.isSelected());
        settings.setValue(SettingsClass.SHOW_BOARDTREE_FLAGGEDSTARRED_INDICATOR, showFlaggedStarredIndicators.isSelected());

        settings.setValue(SettingsClass.BOARD_UPDATE_VISUALIZATION_ENABLED, showBoardUpdateVisualizationCheckBox.isSelected());
        settings.setObjectValue(SettingsClass.BOARD_UPDATE_VISUALIZATION_BGCOLOR_SELECTED, selectedColor);
        settings.setObjectValue(SettingsClass.BOARD_UPDATE_VISUALIZATION_BGCOLOR_NOT_SELECTED, notSelectedColor);
    }

    private void selectedColorPressed() {
        final Color newCol =
            JColorChooser.showDialog(
                getTopLevelAncestor(),
                language.getString("Options.news.3.colorChooserDialog.title.chooseUpdatingColorOfSelectedBoards"),
                selectedColor);
        if (newCol != null) {
            selectedColor = newCol;
            selectedColorLabel.setBackground(selectedColor);
        }
    }

    private void notSelectedColorPressed() {
        final Color newCol =
            JColorChooser.showDialog(
                getTopLevelAncestor(),
                language.getString("Options.news.3.colorChooserDialog.title.chooseUpdatingColorOfUnselectedBoards"),
                notSelectedColor);
        if (newCol != null) {
            notSelectedColor = newCol;
            notSelectedColorLabel.setBackground(notSelectedColor);
        }
    }

    private void refreshUpdateState() {
        MiscToolkit.setContainerEnabled(getColorPanel(), showBoardUpdateVisualizationCheckBox.isSelected());
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
        fontsPanel.add(boardTreeLabel, constraints);
        constraints.insets = inset1519;
        constraints.gridx = 1;
        constraints.gridy = 0;
        fontsPanel.add(boardTreeButton, constraints);
        constraints.insets = inset1515;
        constraints.gridx = 2;
        constraints.gridy = 0;
        fontsPanel.add(selectedBoardTreeFontLabel, constraints);

        return fontsPanel;
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

    private void boardTreeButtonPressed() {
        final FontChooser fontChooser = new FontChooser(owner, language);
        fontChooser.setModal(true);
        fontChooser.setSelectedFont(selectedBodyFont);
        fontChooser.setVisible(true);
        final Font selectedFontTemp = fontChooser.getSelectedFont();
        if (selectedFontTemp != null) {
            selectedBodyFont = selectedFontTemp;
            selectedBoardTreeFontLabel.setText(getFontLabel(selectedBodyFont));
        }
    }
}
