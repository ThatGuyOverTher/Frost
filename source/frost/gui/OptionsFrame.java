/*
  OptionsFrame.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
--------------------------------------------------------------------------
  DESCRIPTION:
  This file contains the whole 'Options' dialog. It first reads the
  actual config from properties file, and on 'OK' it saves all
  settings to the properties file and informs the caller to reload
  this file.
*/
package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

import frost.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/*******************************
 * TODO: - add thread listeners (listen to all running threads) to change the
 *         updating state (bold text in table row) on demand (from bback)
 *******************************/

public class OptionsFrame extends JDialog implements ListSelectionListener {
	/**
	 * Display Panel. Contains appearace options: skins and more in the future
	 */
	private class DisplayPanel extends JPanel {

		/**
		 * 
		 */
		public class Listener implements ActionListener {
			/**
			 * 
			 */
			public Listener() {
				super();
			}
			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
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
		private JButton fileListButton = new JButton();
		
		private JLabel fileListLabel = new JLabel();
		private JLabel fontsLabel = new JLabel();
		
		private Listener listener = new Listener();
		
		private JCheckBox messageBodyAACheckBox = new JCheckBox();
		private JButton messageBodyButton = new JButton();
		
		private JLabel messageBodyLabel = new JLabel();
		private JButton messageListButton = new JButton();
		
		private JLabel messageListLabel = new JLabel();
		private JLabel moreSkinsLabel = new JLabel();
		
		private Font selectedBodyFont = null;
		private Font selectedFileListFont = null;
		private JLabel selectedFileListFontLabel = new JLabel();
		private JLabel selectedMessageBodyFontLabel = new JLabel();
		private Font selectedMessageListFont = null;
		private JLabel selectedMessageListFontLabel = new JLabel();
		
		private SkinChooser skinChooser = null;

		/**
		 * Constructor
		 */
		public DisplayPanel() {
			super();
			initialize();
		}

		public void cancel() {
			skinChooser.cancelChanges();
		}
		
		/**
		 * 
		 */
		private void fileListButtonPressed() {
			FontChooser fontChooser = new FontChooser(OptionsFrame.this, languageResource);
			fontChooser.setModal(true);
			fontChooser.setSelectedFont(selectedFileListFont);
			fontChooser.show();
			Font selectedFontTemp = fontChooser.getSelectedFont();
			if (selectedFontTemp != null) {
				selectedFileListFont = selectedFontTemp;
				selectedFileListFontLabel.setText(getFontLabel(selectedFileListFont));
			}
		}

		/**
		 * @param font
		 * @return
		 */
		private String getFontLabel(Font font) {
			if (font == null) {
				return "";
			} else {
				StringBuffer returnValue = new StringBuffer();
				returnValue.append(font.getFamily());
				if (font.isBold()) {
					returnValue.append(" " + languageResource.getString("Bold"));
				}
				if (font.isItalic()) {
					returnValue.append(" " + languageResource.getString("Italic"));
				}
				returnValue.append(", " + font.getSize());
				return returnValue.toString();
			}
		}
		
		/**
		 * @return
		 */
		private JPanel getFontsPanel() {
			JPanel fontsPanel = new JPanel(new GridBagLayout());
			fontsPanel.setBorder(new EmptyBorder(5, 80, 5, 5));
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weighty = 1;
			Insets inset1515 = new Insets(1, 5, 1, 5);
			Insets inset1519 = new Insets(1, 5, 1, 9);

			constraints.insets = inset1515;
			constraints.gridx = 0; 
			constraints.gridy = 0;
			constraints.weightx = 0.8;
			fontsPanel.add(messageBodyLabel, constraints);
			constraints.insets = inset1519;
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.weightx = 0.1;
			fontsPanel.add(messageBodyButton, constraints);
			constraints.insets = inset1515;
			constraints.gridx = 2;
			constraints.gridy = 0;
			constraints.weightx = 1;
			fontsPanel.add(selectedMessageBodyFontLabel, constraints);

			constraints.insets = inset1515;
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.weightx = 0.8;
			fontsPanel.add(messageListLabel, constraints);
			constraints.insets = inset1519;
			constraints.gridx = 1;
			constraints.gridy = 1;
			constraints.weightx = 0.1;
			fontsPanel.add(messageListButton, constraints);
			constraints.insets = inset1515;
			constraints.gridx = 2;
			constraints.gridy = 1;
			constraints.weightx = 1;
			fontsPanel.add(selectedMessageListFontLabel, constraints);
			
			constraints.insets = inset1515;
			constraints.gridx = 0;
			constraints.gridy = 2;
			constraints.weightx = 0.8;
			fontsPanel.add(fileListLabel, constraints);
			constraints.insets = inset1519;
			constraints.gridx = 1;
			constraints.gridy = 2;
			constraints.weightx = 0.1;
			fontsPanel.add(fileListButton, constraints);
			constraints.insets = inset1515;
			constraints.gridx = 2;
			constraints.gridy = 2;
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
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1;
			constraints.weighty = 1;
			Insets inset5511 = new Insets(5, 5, 1, 1);
			Insets inset1515 = new Insets(1, 5, 1, 5);

			constraints.insets = inset1515;
			constraints.gridx = 0;
			constraints.gridy = 0;
			skinChooser = new SkinChooser(languageResource.getResourceBundle());
			add(skinChooser, constraints);

			constraints.insets = inset1515;
			constraints.gridx = 0;
			constraints.gridy = 1;
			moreSkinsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			add(moreSkinsLabel, constraints);

			constraints.insets = inset5511;
			constraints.gridx = 0;
			constraints.gridy = 3;
			add(fontsLabel, constraints);

			constraints.gridx = 0;
			constraints.gridy = 4;
			add(getFontsPanel(), constraints);
			
			constraints.gridx = 0;
			constraints.gridy = 5;
			add(messageBodyAACheckBox, constraints);

			//Add listeners
			messageBodyButton.addActionListener(listener);
			messageListButton.addActionListener(listener);
			fileListButton.addActionListener(listener);
		}

		/**
		 * Load the settings of this panel
		 * @param displaySettings class the settings will be loaded from
		 */
		public void loadSettings(SettingsClass displaySettings) {
			boolean skinsEnabled = displaySettings.getBoolValue("skinsEnabled");
			skinChooser.setSkinsEnabled(skinsEnabled);
			String selectedSkinPath = displaySettings.getValue("selectedSkin");
			skinChooser.setSelectedSkin(selectedSkinPath);
			
			String fontName = displaySettings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
			int fontSize = displaySettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
			int fontStyle = displaySettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
			selectedBodyFont = new Font(fontName, fontStyle, fontSize); 
			selectedMessageBodyFontLabel.setText(getFontLabel(selectedBodyFont));	
			
			fontName = displaySettings.getValue(SettingsClass.MESSAGE_LIST_FONT_NAME);
			fontSize = displaySettings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_SIZE);
			fontStyle = displaySettings.getIntValue(SettingsClass.MESSAGE_LIST_FONT_STYLE);
			selectedMessageListFont = new Font(fontName, fontStyle, fontSize); 
			selectedMessageListFontLabel.setText(getFontLabel(selectedMessageListFont));
			
			fontName = displaySettings.getValue(SettingsClass.FILE_LIST_FONT_NAME);
			fontSize = displaySettings.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
			fontStyle = displaySettings.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
			selectedFileListFont = new Font(fontName, fontStyle, fontSize); 
			selectedFileListFontLabel.setText(getFontLabel(selectedFileListFont));
			
			messageBodyAACheckBox.setSelected(displaySettings.getBoolValue("messageBodyAA"));	
		}
		
		/**
		 * 
		 */
		private void messageBodyButtonPressed() {
			FontChooser fontChooser = new FontChooser(OptionsFrame.this, languageResource);
			fontChooser.setModal(true);
			fontChooser.setSelectedFont(selectedBodyFont);
			fontChooser.show();
			Font selectedFontTemp = fontChooser.getSelectedFont();
			if (selectedFontTemp != null) {
				selectedBodyFont = selectedFontTemp;
				selectedMessageBodyFontLabel.setText(getFontLabel(selectedBodyFont));
			}
		}

		/**
		 * 
		 */
		private void messageListButtonPressed() {
			FontChooser fontChooser = new FontChooser(OptionsFrame.this, languageResource);
			fontChooser.setModal(true);
			fontChooser.setSelectedFont(selectedMessageListFont);
			fontChooser.show();
			Font selectedFontTemp = fontChooser.getSelectedFont();
			if (selectedFontTemp != null) {
				selectedMessageListFont = selectedFontTemp;
				selectedMessageListFontLabel.setText(getFontLabel(selectedMessageListFont));
			}
		}

		public void ok() {
			skinChooser.commitChanges();
			saveSettings(frostSettings);
		}

		/**
		 * 
		 */
		private void refreshLanguage() {
			moreSkinsLabel.setText(languageResource.getString("MoreSkinsAt") + " http://javootoo.l2fprod.com/plaf/skinlf/");
			fontsLabel.setText(languageResource.getString("Fonts"));
			messageBodyLabel.setText(languageResource.getString("Message Body"));
			messageBodyButton.setText(languageResource.getString("Choose"));
			selectedMessageBodyFontLabel.setText(getFontLabel(selectedBodyFont));
			messageListLabel.setText(languageResource.getString("Message List"));
			messageListButton.setText(languageResource.getString("Choose"));
			selectedMessageListFontLabel.setText(getFontLabel(selectedMessageListFont));
			fileListLabel.setText(languageResource.getString("File List"));
			fileListButton.setText(languageResource.getString("Choose"));
			selectedFileListFontLabel.setText(getFontLabel(selectedFileListFont));
			messageBodyAACheckBox.setText(languageResource.getString("EnableMessageBodyAA"));
		}

		/** 
		 * Save the settings of this panel
		 * @param displaySettings class where the settings will be stored
		 */
		private void saveSettings(SettingsClass displaySettings) {
			boolean skinsEnabled = skinChooser.isSkinsEnabled();
			displaySettings.setValue("skinsEnabled", skinsEnabled);

			String selectedSkin = skinChooser.getSelectedSkin();
			if (selectedSkin == null) {
				displaySettings.setValue("selectedSkin", "none");
			} else {
				displaySettings.setValue("selectedSkin", selectedSkin);
			}
			if (selectedBodyFont != null) {
				displaySettings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, selectedBodyFont.getFamily());
				displaySettings.setValue(SettingsClass.MESSAGE_BODY_FONT_STYLE, selectedBodyFont.getStyle());
				displaySettings.setValue(SettingsClass.MESSAGE_BODY_FONT_SIZE, selectedBodyFont.getSize());
			}
			if (selectedMessageListFont != null) {
				displaySettings.setValue(SettingsClass.MESSAGE_LIST_FONT_NAME, selectedMessageListFont.getFamily());
				displaySettings.setValue(SettingsClass.MESSAGE_LIST_FONT_STYLE, selectedMessageListFont.getStyle());
				displaySettings.setValue(SettingsClass.MESSAGE_LIST_FONT_SIZE, selectedMessageListFont.getSize());
			}
			if (selectedFileListFont != null) {
				displaySettings.setValue(SettingsClass.FILE_LIST_FONT_NAME, selectedFileListFont.getFamily());
				displaySettings.setValue(SettingsClass.FILE_LIST_FONT_STYLE, selectedFileListFont.getStyle());
				displaySettings.setValue(SettingsClass.FILE_LIST_FONT_SIZE, selectedFileListFont.getSize());
			}
			displaySettings.setValue("messageBodyAA", messageBodyAACheckBox.isSelected());
		}

	}
	
	/**
	 * 
	 */
	private class DownloadPanel extends JPanel {

		/**
		 * 
		 */
		public class Listener implements ChangeListener, ActionListener {

			/**
			 * 
			 */
			public Listener() {
				super();
			}

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == disableDownloadsCheckBox) {
					refreshComponentsState();
				}	
				if (e.getSource() == browseDirectoryButton) {
					browseDirectoryPressed();	
				}			
			}

			/* (non-Javadoc)
			 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
			 */
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() == enableRequestingCheckBox) {
					enableRequestingChanged();
				}
				if (e.getSource() == restartFailedDownloadsCheckBox) {
					restartFailedDownloadsChanged();
				}
				
			}



		}
		
		private JButton browseDirectoryButton = new JButton();
		private JCheckBox decodeAfterEachSegmentCheckBox = new JCheckBox();
		private JLabel directoryLabel = new JLabel();
		
		private JTextField directoryTextField = new JTextField();
		
		private JCheckBox disableDownloadsCheckBox = new JCheckBox();
		private JCheckBox enableRequestingCheckBox = new JCheckBox();
		
		private Listener listener = new Listener();
		private JLabel maxRetriesLabel = new JLabel();
		private JTextField maxRetriesTextField = new JTextField(8);
		private JCheckBox removeFinishedDownloadsCheckBox = new JCheckBox();
		private JLabel requestAfterTriesLabel = new JLabel();
		private JTextField requestAfterTriesTextField = new JTextField(8);
		private JCheckBox restartFailedDownloadsCheckBox = new JCheckBox();
		private JLabel splitfileThreadsLabel = new JLabel();
		private JTextField splitfileThreadsTextField = new JTextField(8);
		private JTextField threadsTextField = new JTextField(8);
		private JLabel threadsTextLabel = new JLabel();
		private JCheckBox tryAllSegmentsCheckBox = new JCheckBox();
		
		private JLabel waitTimeLabel = new JLabel();
		private JTextField waitTimeTextField = new JTextField(8);
		
		/**
		 * 
		 */
		public DownloadPanel() {
			super();
			initialize();
		}
		
		/**
		 * browseDownloadDirectoryButton Action Listener (Downloads / Browse)
		 */
		private void browseDirectoryPressed() {
			final JFileChooser fc = new JFileChooser(frostSettings.getValue("lastUsedDirectory"));
			fc.setDialogTitle(languageResource.getString("Select download directory"));
			fc.setFileHidingEnabled(true);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setMultiSelectionEnabled(false);

			int returnVal = fc.showOpenDialog(OptionsFrame.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fileSeparator = System.getProperty("file.separator");
				File file = fc.getSelectedFile();
				frostSettings.setValue("lastUsedDirectory", file.getParent());
				directoryTextField.setText(file.getPath() + fileSeparator);
			}
		}

		/**
		 * 
		 */
		private void enableRequestingChanged() {
			requestAfterTriesTextField.setEnabled(enableRequestingCheckBox.isSelected());
			requestAfterTriesLabel.setEnabled(enableRequestingCheckBox.isSelected());
		}
		
		private JPanel getRequestPanel() {
			JPanel subPanel = new JPanel(new GridBagLayout());

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets(5, 5, 5, 5);
			constraints.weighty = 1;
			
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1;
			subPanel.add(requestAfterTriesLabel, constraints);
			constraints.gridx = 1;
			constraints.weightx = 0;
			subPanel.add(requestAfterTriesTextField, constraints);
			
			return subPanel;
		}
		
		private JPanel getRetriesPanel() {
			JPanel subPanel = new JPanel(new GridBagLayout());
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets(5, 5, 5, 5);			
			constraints.weighty = 1;
			
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1;
			subPanel.add(maxRetriesLabel, constraints);
			constraints.gridx = 1;
			constraints.weightx = 0;
			subPanel.add(maxRetriesTextField, constraints);
			
			constraints.gridy = 1;
			constraints.gridx = 0;
			constraints.weightx = 1;
			subPanel.add(waitTimeLabel, constraints);
			constraints.gridx = 1;
			constraints.weightx = 0;
			subPanel.add(waitTimeTextField, constraints);
			
			return subPanel;
		}

		/**
		 * 
		 */
		private void initialize() {
			setName("DownloadPanel");
			setLayout(new GridBagLayout());
			refreshLanguage();

			//Adds all of the components			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			Insets insets0555 = new Insets(0, 5, 5, 5);
			Insets insets5555 = new Insets(5, 5, 5, 5);
			Insets insets5_30_5_5 = new Insets(5, 30, 5, 5);
			constraints.weighty = 1;
			
			constraints.gridwidth = 4;
			constraints.insets = insets0555;
			constraints.gridx = 0;
			constraints.gridy = 0;
			add(disableDownloadsCheckBox, constraints);

			constraints.insets = insets5_30_5_5;
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.weightx = 0.5;
			constraints.gridwidth = 1;
			add(directoryLabel, constraints);
			constraints.insets = insets5555;
			constraints.gridx = 1;
			constraints.weightx = 1;
			constraints.gridwidth = 2;
			add(directoryTextField, constraints);
			constraints.gridx = 3;
			constraints.weightx = 0.1;
			constraints.gridwidth = 1;
			add(browseDirectoryButton, constraints);
			
			constraints.insets = insets5_30_5_5;
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.gridx = 0;
			constraints.gridy = 2;
			constraints.weightx = 0;
			constraints.gridwidth = 1;
			add(restartFailedDownloadsCheckBox, constraints);
			constraints.gridwidth = 3;
			constraints.insets = insets5555;
			constraints.gridx = 1;
			constraints.weightx = 1;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.fill = GridBagConstraints.BOTH;
			add(getRetriesPanel(), constraints);

			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridwidth = 4;
			constraints.insets = insets5_30_5_5;
			constraints.gridy = 3;
			constraints.gridx = 0;
			add(enableRequestingCheckBox, constraints);
			constraints.gridwidth = 3;
			constraints.insets = insets5555;
			constraints.gridy = 4;
			constraints.gridx = 1;
			constraints.fill = GridBagConstraints.BOTH;
			add(getRequestPanel(), constraints);

			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridwidth = 1;
			constraints.insets = insets5_30_5_5;
			constraints.gridy = 5;
			constraints.gridx = 0;
			constraints.weightx = 1;
			add(threadsTextLabel, constraints);
			constraints.gridx = 1;
			constraints.insets = insets5555;
			constraints.weightx = 0;
			add(threadsTextField, constraints);
			
			constraints.insets = insets5_30_5_5;
			constraints.gridy = 6;
			constraints.gridx = 0;
			constraints.weightx = 1;
			add(splitfileThreadsLabel, constraints);
			constraints.gridx = 1;
			constraints.insets = insets5555;
			constraints.weightx = 0;
			add(splitfileThreadsTextField, constraints);

			constraints.insets = insets5_30_5_5;
			constraints.gridwidth = 4;
			constraints.gridy = 7;
			constraints.gridx = 0;
			constraints.weightx = 1;
			add(removeFinishedDownloadsCheckBox, constraints);
			constraints.gridy = 8;
			add(tryAllSegmentsCheckBox, constraints);
			constraints.gridy = 9;
			add(decodeAfterEachSegmentCheckBox, constraints);
			
			// Add listeners
			enableRequestingCheckBox.addChangeListener(listener);
			restartFailedDownloadsCheckBox.addChangeListener(listener);
			disableDownloadsCheckBox.addActionListener(listener);
			browseDirectoryButton.addActionListener(listener);
		}
		
		/**
		 * Load the settings of this panel
		 * @param downloadSettings class the settings will be loaded from
		 */
		public void loadSettings(SettingsClass downloadSettings) {
			removeFinishedDownloadsCheckBox.setSelected(
				downloadSettings.getBoolValue("removeFinishedDownloads"));
			directoryTextField.setText(downloadSettings.getValue("downloadDirectory"));
			threadsTextField.setText(downloadSettings.getValue("downloadThreads"));
			splitfileThreadsTextField.setText(
				downloadSettings.getValue("splitfileDownloadThreads"));
			disableDownloadsCheckBox.setSelected(
				downloadSettings.getBoolValue(SettingsClass.DISABLE_DOWNLOADS));
			restartFailedDownloadsCheckBox.setSelected(
				downloadSettings.getBoolValue("downloadRestartFailedDownloads"));
			enableRequestingCheckBox.setSelected(
				downloadSettings.getBoolValue("downloadEnableRequesting"));
			requestAfterTriesTextField.setText(
				"" + downloadSettings.getIntValue("downloadRequestAfterTries"));
			maxRetriesTextField.setText("" + downloadSettings.getIntValue("downloadMaxRetries"));
			waitTimeTextField.setText("" + downloadSettings.getIntValue("downloadWaittime"));
			tryAllSegmentsCheckBox.setSelected(
				downloadSettings.getBoolValue("downloadTryAllSegments"));
			decodeAfterEachSegmentCheckBox.setSelected(
				downloadSettings.getBoolValue("downloadDecodeAfterEachSegment"));

			refreshComponentsState();
		}
		
		public void ok() {
			saveSettings(frostSettings);
		}

		/**
		 * 
		 */
		private void refreshComponentsState() {
			boolean downloadsEnabled = !disableDownloadsCheckBox.isSelected();
			if (downloadsEnabled) {
				setEnabled(true);
				requestAfterTriesTextField.setEnabled(enableRequestingCheckBox.isSelected());
				maxRetriesTextField.setEnabled(restartFailedDownloadsCheckBox.isSelected());
				waitTimeTextField.setEnabled(restartFailedDownloadsCheckBox.isSelected());
				requestAfterTriesLabel.setEnabled(enableRequestingCheckBox.isSelected());
				maxRetriesLabel.setEnabled(restartFailedDownloadsCheckBox.isSelected());
				waitTimeLabel.setEnabled(restartFailedDownloadsCheckBox.isSelected());
			} else {
				setEnabled(false);
			}
		}

		/**
		 * 
		 */
		private void refreshLanguage() {
			String off = languageResource.getString("Off");
			String on = languageResource.getString("On");
			String minutes = languageResource.getString("minutes");
			removeFinishedDownloadsCheckBox.setText(
				languageResource.getString("Remove finished downloads every 5 minutes")
					+ " (" + off + ")");
			restartFailedDownloadsCheckBox.setText(
				languageResource.getString("Restart failed downloads"));
			waitTimeLabel.setText(
				languageResource.getString("Waittime after each try") + " (" + minutes + "): ");
			maxRetriesLabel.setText(languageResource.getString("Maximum number of retries") + ": ");
			requestAfterTriesLabel.setText(
				languageResource.getString("Request file after this count of retries") + ": ");
			enableRequestingCheckBox.setText(
				languageResource.getString("Enable requesting of failed download files")
					+ " (" + on	+ ")");
			tryAllSegmentsCheckBox.setText(
				languageResource.getString("Try to download all segments, even if one fails")
					+ " (" + on	+ ")");
			decodeAfterEachSegmentCheckBox.setText(
				languageResource.getString("Decode each segment immediately after its download"));
			disableDownloadsCheckBox.setText(languageResource.getString("Disable downloads"));
			
			directoryLabel.setText(languageResource.getString("Download directory") + ": ");
			browseDirectoryButton.setText(languageResource.getString("Browse") + "...");
			threadsTextLabel.setText(languageResource.getString("Number of simultaneous downloads") + " (3)");
			splitfileThreadsLabel.setText(languageResource.getString("Number of splitfile threads") + " (30)");
		}
		
		/**
		 * 
		 */
		private void restartFailedDownloadsChanged() {
			maxRetriesTextField.setEnabled(restartFailedDownloadsCheckBox.isSelected());
			maxRetriesLabel.setEnabled(restartFailedDownloadsCheckBox.isSelected());
			waitTimeTextField.setEnabled(restartFailedDownloadsCheckBox.isSelected());
			waitTimeLabel.setEnabled(restartFailedDownloadsCheckBox.isSelected());
		}
		
		/**
		 * @param downloadSettings
		 */
		private void saveSettings(SettingsClass downloadSettings) {
			String downlDirTxt = directoryTextField.getText();
			String filesep = System.getProperty("file.separator");
			// always append a fileseparator to the end of string
			if ((!(downlDirTxt.lastIndexOf(filesep) == (downlDirTxt.length() - 1)))
				|| downlDirTxt.lastIndexOf(filesep) < 0) {
				downloadSettings.setValue("downloadDirectory", downlDirTxt + filesep);
			} else {
				downloadSettings.setValue("downloadDirectory", downlDirTxt);
			}
			downloadSettings.setValue("downloadThreads", threadsTextField.getText());
			downloadSettings.setValue(
				"removeFinishedDownloads",
				removeFinishedDownloadsCheckBox.isSelected());

			downloadSettings.setValue(
				"splitfileDownloadThreads",
				splitfileThreadsTextField.getText());
			downloadSettings.setValue(
				SettingsClass.DISABLE_DOWNLOADS,
				disableDownloadsCheckBox.isSelected());
			downloadSettings.setValue(
				"downloadRestartFailedDownloads",
				restartFailedDownloadsCheckBox.isSelected());
			downloadSettings.setValue(
				"downloadEnableRequesting",
				enableRequestingCheckBox.isSelected());
			downloadSettings.setValue(
				"downloadRequestAfterTries",
				requestAfterTriesTextField.getText());
			downloadSettings.setValue("downloadMaxRetries", maxRetriesTextField.getText());
			downloadSettings.setValue("downloadWaittime", waitTimeTextField.getText());
			downloadSettings.setValue(
				"downloadTryAllSegments",
				tryAllSegmentsCheckBox.isSelected());
			downloadSettings.setValue(
				"downloadDecodeAfterEachSegment",
				decodeAfterEachSegmentCheckBox.isSelected());
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#setEnabled(boolean)
		 */
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);

			ArrayList exceptions = new ArrayList();
			exceptions.add(disableDownloadsCheckBox);
			MiscToolkit.getInstance().setContainerEnabled(this, enabled, exceptions);
		}

	}

	/**
	 * A simple helper class to store JPanels and their name into a JList.
	 */
	class ListBoxData {
		String name;
		JPanel panel;
		public ListBoxData(String n, JPanel p) {
			panel = p;
			name = n;
		}
		public JPanel getPanel() {
			return panel;
		}
		public String toString() {
			return name;
		}
	}
	
	/**
	 * 
	 */
	private class MiscPanel extends JPanel {

		/**
		 * 
		 */
		private class Listener implements ChangeListener, ActionListener {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == enableLoggingCheckBox) {
					refreshLoggingState();
				}				
			}

			/* (non-Javadoc)
			 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
			 */
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() == altEditCheckBox) {
					altEditChanged();	
				}				
			}
		}		

		private JCheckBox allowEvilBertCheckBox = new JCheckBox();
		private JCheckBox altEditCheckBox = new JCheckBox();
		private JTextField altEditTextField = new JTextField();
		private JLabel autoSaveIntervalLabel = new JLabel();
		private JTextField autoSaveIntervalTextField = new JTextField(8);
		private JLabel availableNodesLabel1 = new JLabel();
		private JLabel availableNodesLabel2 = new JLabel();
		private JTextField availableNodesTextField = new JTextField();
		private JCheckBox cleanupCheckBox = new JCheckBox();
		private JCheckBox enableLoggingCheckBox = new JCheckBox();
		private JLabel keyDownloadHtlLabel = new JLabel();
		private JTextField keyDownloadHtlTextField = new JTextField(8);

		private JLabel keyUploadHtlLabel = new JLabel();

		private JTextField keyUploadHtlTextField = new JTextField(8);

		private Listener listener = new Listener();
		private JLabel logFileSizeLabel = new JLabel();
		private JTextField logFileSizeTextField = new JTextField(8);
		
		private JTranslatableComboBox logLevelComboBox = null;
		private JLabel logLevelLabel = new JLabel();
		private JLabel maxKeysLabel = new JLabel();
		private JTextField maxKeysTextField = new JTextField(8);
		private JCheckBox showSystrayIconCheckBox = new JCheckBox();
		private JCheckBox splashScreenCheckBox = new JCheckBox();

		/**
		 * 
		 */
		public MiscPanel() {
			super();
			initialize();
		}
		
		/**
		 * 
		 */
		private void altEditChanged() {
			altEditTextField.setEnabled(altEditCheckBox.isSelected());
		}
		
		private JPanel getLoggingPanel() {
			JPanel subPanel = new JPanel(new GridBagLayout());

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			Insets insets5055 = new Insets(5, 0, 5, 5);
			Insets insets5_30_5_0 = new Insets(5, 30, 5, 0);
			Insets insets5_30_5_5 = new Insets(5, 30, 5, 5);
			constraints.insets = insets5055;
			constraints.weightx = 1;
			constraints.weighty = 1;
						
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 2;
			subPanel.add(enableLoggingCheckBox, constraints);
			
			constraints.insets = insets5_30_5_5;
			constraints.gridwidth = 1;
			constraints.gridy = 1;
			subPanel.add(logLevelLabel, constraints);
			constraints.gridx = 1;
			String[] searchComboBoxKeys =
				{ Logging.VERY_LOW, Logging.LOW, Logging.MEDIUM, Logging.HIGH, Logging.VERY_HIGH };
			logLevelComboBox = new JTranslatableComboBox(languageResource, searchComboBoxKeys);
			subPanel.add(logLevelComboBox, constraints);
			
			constraints.gridx = 2;
			subPanel.add(logFileSizeLabel, constraints);	
			constraints.insets = insets5_30_5_0;		
			constraints.gridx = 3;
			constraints.weightx = 0;
			subPanel.add(logFileSizeTextField, constraints);
			
			return subPanel;
		}

		/**
		 * 
		 */
		private void initialize() {
			setName("MiscPanel");
			setLayout(new GridBagLayout());
			refreshLanguage();

			// Adds all of the components
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			Insets insets5555 = new Insets(5, 5, 5, 5);
			constraints.weighty = 1;
			
			constraints.weightx = 0;
			constraints.gridwidth = 1;
			constraints.insets = insets5555;
			constraints.gridx = 0;
			constraints.gridy = 0;
			add(keyUploadHtlLabel, constraints);
			constraints.gridx = 1;
			add(keyUploadHtlTextField, constraints);
			
			constraints.gridx = 0;
			constraints.gridy = 1;
			add(keyDownloadHtlLabel, constraints);
			constraints.gridx = 1;
			add(keyDownloadHtlTextField, constraints);
			
			constraints.anchor = GridBagConstraints.SOUTH;
			constraints.gridx = 0;
			constraints.gridy = 2;
			add(availableNodesLabel1, constraints);
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.gridy = 3;
			add(availableNodesLabel2, constraints);
			constraints.gridx = 1;
			constraints.weightx = 1;
			constraints.gridwidth = 2;
			add(availableNodesTextField, constraints);

			constraints.anchor = GridBagConstraints.CENTER;
			constraints.weightx = 0;
			constraints.gridwidth = 1;
			constraints.gridx = 0;
			constraints.gridy = 4;
			add(maxKeysLabel, constraints);
			constraints.gridx = 1;
			add(maxKeysTextField, constraints);
			
			constraints.gridx = 0;
			constraints.gridy = 5;
			add(altEditCheckBox, constraints);
			constraints.gridx = 1;
			constraints.gridwidth = 2;
			constraints.weightx = 1;
			add(altEditTextField, constraints);
			
			constraints.weightx = 0;
			constraints.gridwidth = 1;
			constraints.gridx = 0;
			constraints.gridy = 6;
			add(autoSaveIntervalLabel, constraints);
			constraints.gridx = 1;
			add(autoSaveIntervalTextField, constraints);
	
			constraints.gridx = 0;
			constraints.gridy = 7;
			add(allowEvilBertCheckBox, constraints);			
			constraints.gridx = 1;
			constraints.gridwidth = 2;
			constraints.weightx = 1;
			add(cleanupCheckBox, constraints);	
			
			constraints.weightx = 0;
			constraints.gridwidth = 1;
			constraints.gridx = 0;
			constraints.gridy = 8;
			add(splashScreenCheckBox, constraints);
			constraints.gridx = 1;
			constraints.gridwidth = 2;
			constraints.weightx = 1;
			add(showSystrayIconCheckBox, constraints);	
			
			constraints.gridx = 0;
			constraints.gridy = 9;
			constraints.gridwidth = 3;
			add(getLoggingPanel(), constraints);
			
			// Add listeners
			enableLoggingCheckBox.addActionListener(listener);
			altEditCheckBox.addChangeListener(listener);
		}

		/**
		 * Load the settings of this panel
		 * @param miscSettings class the settings will be loaded from
		 */
		public void loadSettings(SettingsClass miscSettings) {
			allowEvilBertCheckBox.setSelected(miscSettings.getBoolValue("allowEvilBert"));
			altEditCheckBox.setSelected(miscSettings.getBoolValue("useAltEdit"));
			altEditTextField.setEnabled(altEditCheckBox.isSelected());
			keyUploadHtlTextField.setText(miscSettings.getValue("keyUploadHtl"));
			keyDownloadHtlTextField.setText(miscSettings.getValue("keyDownloadHtl"));
			showSystrayIconCheckBox.setSelected(miscSettings.getBoolValue("showSystrayIcon"));
			availableNodesTextField.setText(miscSettings.getValue("availableNodes"));
			altEditTextField.setText(miscSettings.getValue("altEdit"));
			maxKeysTextField.setText(miscSettings.getValue("maxKeys"));
			cleanupCheckBox.setSelected(miscSettings.getBoolValue("doCleanUp"));
			autoSaveIntervalTextField.setText(
				Integer.toString(miscSettings.getIntValue(SettingsClass.AUTO_SAVE_INTERVAL)));
			enableLoggingCheckBox.setSelected(miscSettings.getBoolValue(SettingsClass.LOG_TO_FILE));
			logFileSizeTextField.setText(
				Integer.toString(miscSettings.getIntValue(SettingsClass.LOG_FILE_SIZE_LIMIT)));

			logLevelComboBox.setSelectedKey(miscSettings.getDefaultValue(SettingsClass.LOG_LEVEL));
			logLevelComboBox.setSelectedKey(miscSettings.getValue(SettingsClass.LOG_LEVEL));

			// "Load" splashchk
			File splashchk = new File("nosplash.chk");
			if (splashchk.exists()) {
				splashScreenCheckBox.setSelected(true);
			} else {
				splashScreenCheckBox.setSelected(false);
			}

			refreshLoggingState();
		}

		public void ok() {
			saveSettings(frostSettings);
		}
		
		/**
		 * 
		 */
		private void refreshLanguage() {
			keyUploadHtlLabel.setText(languageResource.getString("Keyfile upload HTL") + " (21)");
			keyDownloadHtlLabel.setText(
				languageResource.getString("Keyfile download HTL") + " (24)");
			availableNodesLabel1.setText(languageResource.getString("list of nodes"));
			availableNodesLabel2.setText(languageResource.getString("list of nodes 2"));
			maxKeysLabel.setText(
				languageResource.getString("Maximum number of keys to store") + " (100000)");
			autoSaveIntervalLabel.setText(
				languageResource.getString("Automatic saving interval") + " (15)");
			splashScreenCheckBox.setText(languageResource.getString("Disable splashscreen"));
			showSystrayIconCheckBox.setText(languageResource.getString("Show systray icon"));
			String off = languageResource.getString("Off");
			allowEvilBertCheckBox.setText(
				languageResource.getString("Allow 2 byte characters") + " (" + off + ")");
			altEditCheckBox.setText(
				languageResource.getString("Use editor for writing messages") + " (" + off + ")");
			cleanupCheckBox.setText(languageResource.getString("Clean the keypool"));

			enableLoggingCheckBox.setText(languageResource.getString("Enable logging"));
			logLevelLabel.setText(
				languageResource.getString("Logging level")
					+ " ("
					+ languageResource.getString("Low")
					+ ") ");
			logFileSizeLabel.setText(languageResource.getString("Log file size limit (in KB)"));

		}
		
		/**
		 * 
		 */
		private void refreshLoggingState() {
			boolean enableLogging = enableLoggingCheckBox.isSelected();
			logLevelLabel.setEnabled(enableLogging);
			logLevelComboBox.setEnabled(enableLogging);
			logFileSizeLabel.setEnabled(enableLogging);
			logFileSizeTextField.setEnabled(enableLogging);
		}

		/**
		 * @param miscSettings
		 */
		private void saveSettings(SettingsClass miscSettings) {
			miscSettings.setValue("keyUploadHtl", keyUploadHtlTextField.getText());
			miscSettings.setValue("keyDownloadHtl", keyDownloadHtlTextField.getText());
			miscSettings.setValue("availableNodes", availableNodesTextField.getText());
			miscSettings.setValue("maxKeys", maxKeysTextField.getText());
			miscSettings.setValue("showSystrayIcon", showSystrayIconCheckBox.isSelected());
			miscSettings.setValue("allowEvilBert", allowEvilBertCheckBox.isSelected());
			miscSettings.setValue("useAltEdit", altEditCheckBox.isSelected());
			miscSettings.setValue("altEdit", altEditTextField.getText());
			miscSettings.setValue("doCleanUp", cleanupCheckBox.isSelected());
			miscSettings.setValue(SettingsClass.AUTO_SAVE_INTERVAL, autoSaveIntervalTextField.getText());
			miscSettings.setValue(SettingsClass.LOG_TO_FILE, enableLoggingCheckBox.isSelected());
			miscSettings.setValue(
				SettingsClass.LOG_FILE_SIZE_LIMIT,
				logFileSizeTextField.getText());
			miscSettings.setValue(SettingsClass.LOG_LEVEL, logLevelComboBox.getSelectedKey());

			// Save splashchk
			try {
				File splashFile = new File("nosplash.chk");
				if (splashScreenCheckBox.isSelected()) {
					splashFile.createNewFile();
				} else {
					splashFile.delete();
				}
			} catch (IOException ioex) {
				logger.log(Level.SEVERE, "Could not create splashscreen checkfile", ioex);
			}
		}

	}
	
	/**
	 * 
	 */
	private class News2Panel extends JPanel {
		
		/**
		 * 
		 */
		private class Listener implements ActionListener {

			/* (non-Javadoc) 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == blockSubjectCheckBox) {
					blockSubjectPressed();
				}
				if (e.getSource() == blockBodyCheckBox) {
					blockBodyPressed();
				}
				if (e.getSource() == blockBoardCheckBox) {
					blockBoardPressed();
				}
				if (e.getSource() == doBoardBackoffCheckBox) {
					refreshSpamDetectionState();
				}
			}
		}
		private JCheckBox blockBoardCheckBox = new JCheckBox();
		private JTextField blockBoardTextField = new JTextField();
		private JCheckBox blockBodyCheckBox = new JCheckBox();
		private JTextField blockBodyTextField = new JTextField();
		private JCheckBox blockSubjectCheckBox = new JCheckBox();
		private JTextField blockSubjectTextField = new JTextField();
		private JCheckBox doBoardBackoffCheckBox = new JCheckBox();
		private JCheckBox hideBadMessagesCheckBox = new JCheckBox();
		private JCheckBox hideCheckMessagesCheckBox = new JCheckBox();
		private JCheckBox hideNAMessagesCheckBox = new JCheckBox();
		private JLabel intervalLabel = new JLabel();
		
		private Listener listener = new Listener();
		
		private JTextField sampleIntervalTextField = new JTextField(8);
		
		private JCheckBox signedOnlyCheckBox = new JCheckBox();
		private JTextField spamTresholdTextField = new JTextField(8);
		
		private JLabel tresholdLabel = new JLabel();

		/**
		 * 
		 */
		public News2Panel() {
			super();
			initialize();
		}
		
		/**
		 * 
		 */
		private void blockBoardPressed() {
			blockBoardTextField.setEnabled(blockBoardCheckBox.isSelected());				
		}
		

		/**
		 * 
		 */
		private void blockBodyPressed() {
			blockBodyTextField.setEnabled(blockBodyCheckBox.isSelected());				
		}

		/**
		 * 
		 */
		private void blockSubjectPressed() {
			blockSubjectTextField.setEnabled(blockSubjectCheckBox.isSelected());	
		}		
		
		/**
		 * @return
		 */
		private JPanel getSpamPanel() {
			JPanel spamPanel = new JPanel(new GridBagLayout());
			spamPanel.setBorder(new EmptyBorder(5, 30, 5, 5));
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new Insets(5, 5, 5, 5);
			constraints.weighty = 1; 
			constraints.weightx = 1;
			constraints.anchor = GridBagConstraints.NORTHWEST;

			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 0.5;
			spamPanel.add(intervalLabel, constraints);
			constraints.fill = GridBagConstraints.NONE;
			constraints.gridx = 1;
			constraints.weightx = 1;
			spamPanel.add(sampleIntervalTextField, constraints);
			
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.weightx = 0.5;
			spamPanel.add(tresholdLabel, constraints);
			constraints.fill = GridBagConstraints.NONE;
			constraints.gridx = 1;
			constraints.weightx = 1;
			spamPanel.add(spamTresholdTextField, constraints);

			return spamPanel;
		}
		
		/**
		 * 
		 */
		private void initialize() {
			setName("News2Panel");
			setLayout(new GridBagLayout());
			refreshLanguage();
			
			// Adds all of the components
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			Insets insets5555 = new Insets(5, 5, 5, 5);
			Insets insets5_30_5_5 = new Insets(5, 30, 5, 5);
			constraints.insets = insets5555;
			constraints.weighty = 1;
			constraints.weightx = 1;
			constraints.gridwidth = 2;

			constraints.gridx = 0;
			constraints.gridy = 0;
			add(blockSubjectCheckBox, constraints);
			constraints.insets = insets5_30_5_5;
			constraints.gridy = 1;
			add(blockSubjectTextField, constraints);
			
			constraints.insets = insets5555;
			constraints.gridy = 2;
			add(blockBodyCheckBox, constraints);
			constraints.insets = insets5_30_5_5;
			constraints.gridy = 3;
			add(blockBodyTextField, constraints);
			
			constraints.insets = insets5555;
			constraints.gridy = 4;
			add(blockBoardCheckBox, constraints);
			constraints.insets = insets5_30_5_5;
			constraints.gridy = 5;
			add(blockBoardTextField, constraints);
						
			constraints.insets = insets5555;
			constraints.gridwidth = 1;
			constraints.gridx = 0;
			constraints.gridy = 6;
			add(signedOnlyCheckBox, constraints);
			constraints.gridx = 1;
			add(hideBadMessagesCheckBox, constraints);
			constraints.gridx = 0;
			constraints.gridy = 7;
			add(hideCheckMessagesCheckBox, constraints);
			constraints.gridx = 1;
			add(hideNAMessagesCheckBox, constraints);
						
			constraints.gridwidth = 2;
			constraints.gridx = 0;
			constraints.gridy = 8;
			add(doBoardBackoffCheckBox, constraints);
			constraints.gridy = 9;
			constraints.weighty = 0;
			add(getSpamPanel(), constraints);
						
			// Add listeners
			blockSubjectCheckBox.addActionListener(listener);
			blockBodyCheckBox.addActionListener(listener);
			blockBoardCheckBox.addActionListener(listener);
			doBoardBackoffCheckBox.addActionListener(listener);						
		}
		
		/**
		 * Load the settings of this panel
		 * @param news2Settings class the settings will be loaded from
		 */
		public void loadSettings(SettingsClass news2Settings) {
			signedOnlyCheckBox.setSelected(news2Settings.getBoolValue("signedOnly"));
			hideBadMessagesCheckBox.setSelected(news2Settings.getBoolValue("hideBadMessages"));
			hideCheckMessagesCheckBox.setSelected(news2Settings.getBoolValue("hideCheckMessages"));
			hideNAMessagesCheckBox.setSelected(news2Settings.getBoolValue("hideNAMessages"));
			
			blockSubjectCheckBox.setSelected(news2Settings.getBoolValue("blockMessageChecked"));
			blockSubjectTextField.setEnabled(blockSubjectCheckBox.isSelected());
			blockSubjectTextField.setText(news2Settings.getValue("blockMessage"));
			blockBodyCheckBox.setSelected(news2Settings.getBoolValue("blockMessageBodyChecked"));
			blockBodyTextField.setEnabled(blockBodyCheckBox.isSelected());
			blockBodyTextField.setText(news2Settings.getValue("blockMessageBody"));
			blockBoardCheckBox.setSelected(news2Settings.getBoolValue("blockMessageBoardChecked"));
			blockBoardTextField.setEnabled(blockBoardCheckBox.isSelected());
			blockBoardTextField.setText(news2Settings.getValue("blockMessageBoard"));
			
			doBoardBackoffCheckBox.setSelected(news2Settings.getBoolValue("doBoardBackoff"));
			sampleIntervalTextField.setText(news2Settings.getValue("sampleInterval"));
			spamTresholdTextField.setText(news2Settings.getValue("spamTreshold"));
			refreshSpamDetectionState();
		}
		
		public void ok() {
			saveSettings(frostSettings);
		}

		/**
		 * 
		 */
		private void refreshLanguage() {
			String hours = languageResource.getString("hours");
			String off = languageResource.getString("Off");

			intervalLabel.setText(
				languageResource.getString("Sample interval") + " (" + hours + ")");
			tresholdLabel.setText(languageResource.getString("Threshold of blocked messages"));
			signedOnlyCheckBox.setText(languageResource.getString("Hide unsigned messages"));
			hideBadMessagesCheckBox.setText(
				languageResource.getString("Hide messages flagged BAD") + " (" + off + ")");
			hideCheckMessagesCheckBox.setText(
				languageResource.getString("Hide messages flagged CHECK") + " (" + off + ")");
			hideNAMessagesCheckBox.setText(
				languageResource.getString("Hide messages flagged N/A") + " (" + off + ")");
			blockSubjectCheckBox.setText(
				languageResource.getString(
					"Block messages with subject containing (separate by ';' )")
					+ ": ");
			blockBodyCheckBox.setText(
				languageResource.getString(
					"Block messages with body containing (separate by ';' )")
					+ ": ");
			blockBoardCheckBox.setText(
				languageResource.getString(
					"Block messages with these attached boards (separate by ';' )")
					+ ": ");
			doBoardBackoffCheckBox.setText(languageResource.getString("Do spam detection"));
		}
		
		/**
		 * 
		 */
		private void refreshSpamDetectionState() {
			boolean enableSpamDetection = doBoardBackoffCheckBox.isSelected();
			sampleIntervalTextField.setEnabled(enableSpamDetection);
			spamTresholdTextField.setEnabled(enableSpamDetection);
			tresholdLabel.setEnabled(enableSpamDetection);
			intervalLabel.setEnabled(enableSpamDetection);
		}
		
		/**
		 * @param news2Settings
		 */
		private void saveSettings(SettingsClass news2Settings) {
			news2Settings.setValue(
				"blockMessage",
				((blockSubjectTextField.getText()).trim()).toLowerCase());
			news2Settings.setValue("blockMessageChecked", blockSubjectCheckBox.isSelected());
			news2Settings.setValue(
				"blockMessageBody",
				((blockBodyTextField.getText()).trim()).toLowerCase());
			news2Settings.setValue("blockMessageBodyChecked", blockBodyCheckBox.isSelected());
			news2Settings.setValue(
				"blockMessageBoard",
				((blockBoardTextField.getText()).trim()).toLowerCase());
			news2Settings.setValue("blockMessageBoardChecked", blockBoardCheckBox.isSelected());
			news2Settings.setValue("doBoardBackoff", doBoardBackoffCheckBox.isSelected());
			news2Settings.setValue("spamTreshold", spamTresholdTextField.getText());
			news2Settings.setValue("sampleInterval", sampleIntervalTextField.getText());
			news2Settings.setValue("signedOnly", signedOnlyCheckBox.isSelected());
			news2Settings.setValue("hideBadMessages", hideBadMessagesCheckBox.isSelected());
			news2Settings.setValue("hideCheckMessages", hideCheckMessagesCheckBox.isSelected());
			news2Settings.setValue("hideNAMessages", hideNAMessagesCheckBox.isSelected());
		}
	}
	
	/**
	 * 
	 * @author $Author$
	 * @version $Revision$
	 */
	private class NewsPanel extends JPanel {
		
		private JLabel uploadHtlLabel = new JLabel();
		private JLabel downloadHtlLabel = new JLabel();
		private JLabel displayDaysLabel = new JLabel();
		private JLabel downloadDaysLabel = new JLabel();
		private JLabel messageBaseLabel = new JLabel();
		private JLabel signatureLabel = new JLabel();
		
		private JTextField uploadHtlTextField = new JTextField(8);
		private JTextField downloadHtlTextField = new JTextField(8);
		private JTextField displayDaysTextField = new JTextField(8);
		private JTextField downloadDaysTextField = new JTextField(8);
		private JTextField messageBaseTextField = new JTextField(16);
		
		private AntialiasedTextArea signatureTextArea;

		/**
		 * 
		 */
		public NewsPanel() {
			super();
			initialize();
		}

		/**
		 * 
		 */
		private void initialize() {
			setName("NewsPanel");
			setLayout(new GridBagLayout());
			refreshLanguage();

			// Adds all of the components
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.WEST;
			Insets insets5555 = new Insets(5, 5, 5, 5);
			constraints.weighty = 1;
			constraints.weightx = 0;		
			
			constraints.insets = insets5555;
			constraints.weightx = 0.4;	
			constraints.gridx = 0;
			constraints.gridy = 0;
			add(uploadHtlLabel, constraints);	
			constraints.weightx = 0.6;		
			constraints.gridx = 1;
			add(uploadHtlTextField, constraints);
			
			constraints.gridx = 0;
			constraints.gridy = 1;
			add(downloadHtlLabel, constraints);		
			constraints.gridx = 1;
			add(downloadHtlTextField, constraints);
						
			constraints.gridx = 0;
			constraints.gridy = 2;
			add(displayDaysLabel, constraints);		
			constraints.gridx = 1;
			add(displayDaysTextField, constraints);
			
			constraints.gridx = 0;
			constraints.gridy = 3;
			add(downloadDaysLabel, constraints);		
			constraints.gridx = 1;
			add(downloadDaysTextField, constraints);
			
			constraints.gridx = 0;
			constraints.gridy = 4;
			add(messageBaseLabel, constraints);		
			constraints.gridx = 1;
			add(messageBaseTextField, constraints);
			
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridwidth = 2;	
			constraints.weightx = 1;
			constraints.weighty = 0;
			constraints.gridx = 0;
			constraints.gridy = 5;
			add(signatureLabel, constraints);
			constraints.gridy = 6;
			JScrollPane signatureScrollPane = new JScrollPane(getSignatureTextArea());
			add(signatureScrollPane, constraints);
		}

		/**
		 * @return
		 */
		private AntialiasedTextArea getSignatureTextArea() {
			if (signatureTextArea == null) {
				signatureTextArea = new AntialiasedTextArea(4, 50);

				String fontName = frostSettings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
				int fontStyle = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
				int fontSize = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
				Font tofFont = new Font(fontName, fontStyle, fontSize);
				if (!tofFont.getFamily().equals(fontName)) {
					logger.severe(
						"The selected font was not found in your system\n"
							+ "That selection will be changed to \"Monospaced\".");
					frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
					tofFont = new Font("Monospaced", fontStyle, fontSize);
				}
				signatureTextArea.setFont(tofFont);
				signatureTextArea.setAntiAliasEnabled(frostSettings.getBoolValue("messageBodyAA"));
			}
			return signatureTextArea;
		}

		/**
		 * Load the settings of this panel
		 * @param newsSettings class the settings will be loaded from
		 */
		public void loadSettings(SettingsClass newsSettings) {
			uploadHtlTextField.setText(frostSettings.getValue("tofUploadHtl"));
			downloadHtlTextField.setText(frostSettings.getValue("tofDownloadHtl"));
			displayDaysTextField.setText(frostSettings.getValue("maxMessageDisplay"));
			downloadDaysTextField.setText(frostSettings.getValue("maxMessageDownload"));
			messageBaseTextField.setText(frostSettings.getValue("messageBase"));
			
			//Load signature
			File signature = new File("signature.txt");
			if (signature.isFile()) {
				getSignatureTextArea().setText(FileAccess.readFile("signature.txt", "UTF-8"));
			}
		}
		
		public void ok() {
			saveSettings(frostSettings);
		}
		
		/**
		 * 
		 */
		private void refreshLanguage() {
			uploadHtlLabel.setText(languageResource.getString("Message upload HTL") + " (21)");
			downloadHtlLabel.setText(languageResource.getString("Message download HTL") + " (23)");
			displayDaysLabel.setText(
				languageResource.getString("Number of days to display") + " (10)");
			downloadDaysLabel.setText(
				languageResource.getString("Number of days to download backwards") + " (3)");
			messageBaseLabel.setText(languageResource.getString("Message base") + " (news)");
			signatureLabel.setText(languageResource.getString("Signature"));
		}

		/**
		 * @param frostSettings
		 */
		private void saveSettings(SettingsClass frostSettings) {
			frostSettings.setValue("tofUploadHtl", uploadHtlTextField.getText());
			frostSettings.setValue("tofDownloadHtl", downloadHtlTextField.getText());
			frostSettings.setValue("maxMessageDisplay", displayDaysTextField.getText());
			frostSettings.setValue("maxMessageDownload", downloadDaysTextField.getText());
			frostSettings.setValue(
				"messageBase",
				messageBaseTextField.getText().trim().toLowerCase());
				
			//Save signature
			FileAccess.writeFile(getSignatureTextArea().getText(), "signature.txt", "UTF-8");
		}

	}
	
	/**
	 * 
	 */
	private class SearchPanel extends JPanel {
		
		private JLabel archiveExtensionLabel = new JLabel();
		
		private JTextField archiveExtensionTextField = new JTextField();
		private JLabel audioExtensionLabel = new JLabel();
		private JTextField audioExtensionTextField = new JTextField();
		private JLabel documentExtensionLabel = new JLabel();
		private JTextField documentExtensionTextField = new JTextField();
		private JLabel executableExtensionLabel = new JLabel();
		private JTextField executableExtensionTextField = new JTextField();
		
		private JCheckBox hideAnonFilesCheckBox = new JCheckBox();
		private JCheckBox hideBadFilesCheckBox = new JCheckBox();
		private JLabel imageExtensionLabel = new JLabel();
		private JTextField imageExtensionTextField = new JTextField();
		private JLabel maxSearchResultsLabel = new JLabel();
		private JTextField maxSearchResultsTextField = new JTextField(8);
		private JLabel videoExtensionLabel = new JLabel();
		private JTextField videoExtensionTextField = new JTextField();


		
		/**
		 * 
		 */
		public SearchPanel() {
			super();
			initialize();
		}

		/**
		 * 
		 */
		private void initialize() {
			setName("SearchPanel");
			setLayout(new GridBagLayout());
			refreshLanguage();

			// Adds all of the components
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets(5, 5, 5, 5);
			constraints.weighty = 1;
			constraints.gridwidth = 1;
			
			constraints.weightx = 0;
			constraints.gridx = 0;
			constraints.gridy = 0;
			add(imageExtensionLabel, constraints);
			constraints.weightx = 1;
			constraints.gridx = 1;
			add(imageExtensionTextField, constraints);
			
			constraints.weightx = 0;
			constraints.gridy = 1;
			constraints.gridx = 0;
			add(videoExtensionLabel, constraints);
			constraints.weightx = 1;
			constraints.gridx = 1;
			add(videoExtensionTextField, constraints);
			
			constraints.weightx = 0;
			constraints.gridy = 2;
			constraints.gridx = 0;
			add(archiveExtensionLabel, constraints);
			constraints.weightx = 1;
			constraints.gridx = 1;
			add(archiveExtensionTextField, constraints);
			
			constraints.weightx = 0;
			constraints.gridy = 3;
			constraints.gridx = 0;
			add(documentExtensionLabel, constraints);
			constraints.weightx = 1;
			constraints.gridx = 1;
			add(documentExtensionTextField, constraints);
			
			constraints.weightx = 0;
			constraints.gridy = 4;
			constraints.gridx = 0;
			add(audioExtensionLabel, constraints);
			constraints.weightx = 1;
			constraints.gridx = 1;
			add(audioExtensionTextField, constraints);
			
			constraints.weightx = 0;
			constraints.gridy = 5;
			constraints.gridx = 0;
			add(executableExtensionLabel, constraints);
			constraints.weightx = 1;
			constraints.gridx = 1;
			add(executableExtensionTextField, constraints);
			
			constraints.weightx = 0;
			constraints.gridy = 6;
			constraints.gridx = 0;
			add(maxSearchResultsLabel, constraints);
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.WEST;
			constraints.gridx = 1;
			add(maxSearchResultsTextField, constraints);
			
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.gridwidth = 2;
			constraints.gridy = 7;
			constraints.gridx = 0;
			add(hideBadFilesCheckBox, constraints);
			constraints.gridy = 8;
			add(hideAnonFilesCheckBox, constraints);

		}
		
		/**
		 * Load the settings of this panel
		 * @param searchSettings class the settings will be loaded from
		 */
		public void loadSettings(SettingsClass searchSettings) {
			audioExtensionTextField.setText(searchSettings.getValue("audioExtension"));
			imageExtensionTextField.setText(searchSettings.getValue("imageExtension"));
			videoExtensionTextField.setText(searchSettings.getValue("videoExtension"));
			documentExtensionTextField.setText(searchSettings.getValue("documentExtension"));
			executableExtensionTextField.setText(searchSettings.getValue("executableExtension"));
			archiveExtensionTextField.setText(searchSettings.getValue("archiveExtension"));
			maxSearchResultsTextField.setText(
				Integer.toString(searchSettings.getIntValue("maxSearchResults")));
			hideBadFilesCheckBox.setSelected(searchSettings.getBoolValue("hideBadFiles"));
			hideAnonFilesCheckBox.setSelected(searchSettings.getBoolValue("hideAnonFiles"));
		}
		
		public void ok() {
			saveSettings(frostSettings);
		}

		/**
		 * 
		 */
		private void refreshLanguage() {
			imageExtensionLabel.setText(languageResource.getString("Image Extension"));
			videoExtensionLabel.setText(languageResource.getString("Video Extension"));
			archiveExtensionLabel.setText(languageResource.getString("Archive Extension"));
			documentExtensionLabel.setText(languageResource.getString("Document Extension"));
			audioExtensionLabel.setText(languageResource.getString("Audio Extension"));
			executableExtensionLabel.setText(languageResource.getString("Executable Extension"));
			maxSearchResultsLabel.setText(languageResource.getString("Maximum search results"));
			
			hideBadFilesCheckBox.setText(languageResource.getString("Hide files from people marked BAD"));
			hideAnonFilesCheckBox.setText(languageResource.getString("Hide files from anonymous users"));
		}
		
		/**
		 * @param searchSettings
		 */
		private void saveSettings(SettingsClass searchSettings) {
			searchSettings.setValue(
				"audioExtension",
				audioExtensionTextField.getText().toLowerCase());
			searchSettings.setValue(
				"imageExtension",
				imageExtensionTextField.getText().toLowerCase());
			searchSettings.setValue(
				"videoExtension",
				videoExtensionTextField.getText().toLowerCase());
			searchSettings.setValue(
				"documentExtension",
				documentExtensionTextField.getText().toLowerCase());
			searchSettings.setValue(
				"executableExtension",
				executableExtensionTextField.getText().toLowerCase());
			searchSettings.setValue(
				"archiveExtension",
				archiveExtensionTextField.getText().toLowerCase());
			searchSettings.setValue("maxSearchResults", maxSearchResultsTextField.getText());

			searchSettings.setValue("hideBadFiles", hideBadFilesCheckBox.isSelected());
			searchSettings.setValue("hideAnonFiles", hideAnonFilesCheckBox.isSelected());
		}
		
	}
	
	/**
	 * 
	 */
	private class UploadPanel extends JPanel {

		/**
		 * 
		 */
		private class Listener implements ActionListener {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == disableRequestsCheckBox) {
					disableUploadsPressed();
				}
			}
		}
		private JCheckBox automaticIndexingCheckBox = new JCheckBox();
		private JLabel batchSizeExplanationLabel = new JLabel();
		private JLabel batchSizeLabel = new JLabel();
		private JTextField batchSizeTextField = new JTextField(8);
		
		private JCheckBox disableRequestsCheckBox = new JCheckBox();
		private JCheckBox helpFriendsCheckBox = new JCheckBox();
		private JLabel htlExplanationLabel = new JLabel();
		
		private JLabel htlLabel = new JLabel();
	
		private JTextField htlTextField = new JTextField(8);
		private JLabel indexFileRedundancyExplanationLabel = new JLabel();
		private JLabel indexFileRedundancyLabel = new JLabel();
		private JTextField indexFileRedundancyTextField = new JTextField(8);
		
		private Listener listener = new Listener();
		private JCheckBox shareDownloadsCheckBox = new JCheckBox();
		private JCheckBox signUploadsCheckBox = new JCheckBox();
		private JLabel splitfileThreadsExplanationLabel = new JLabel();
		private JLabel splitfileThreadsLabel = new JLabel();
		private JTextField splitfileThreadsTextField = new JTextField(8);
		private JLabel threadsLabel = new JLabel();
		private JTextField threadsTextField = new JTextField(8);

		/**
		 * 
		 */
		public UploadPanel() {
			super();
			initialize();
		}
		
		/**
		 * 
		 */
		private void disableUploadsPressed() {
			// enable panel if checkbox is not selected
			setEnabled(!disableRequestsCheckBox.isSelected());
		}

		/**
		 * 
		 */
		private void initialize() {
			setName("UploadPanel");
			setLayout(new GridBagLayout());
			refreshLanguage();

			//Adds all of the components
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			Insets insets0555 = new Insets(0, 5, 5, 5);
			Insets insets5555 = new Insets(5, 5, 5, 5);
			Insets insets5_30_5_5 = new Insets(5, 30, 5, 5);
			constraints.weighty = 1;
			
			constraints.weightx = 1;
			constraints.gridwidth = 3;
			constraints.insets = insets0555;
			constraints.gridx = 0;
			constraints.gridy = 0;
			add(disableRequestsCheckBox, constraints);
			
			constraints.insets = insets5_30_5_5;
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.gridwidth = 2;
			add(automaticIndexingCheckBox, constraints);
			constraints.insets = insets5555;			
			constraints.gridx = 2;
			constraints.gridwidth = 1;
			add(shareDownloadsCheckBox, constraints);
			
			constraints.insets = insets5_30_5_5;
			constraints.gridx = 0;
			constraints.gridy = 2;
			constraints.gridwidth = 2;
			add(signUploadsCheckBox, constraints);
			constraints.insets = insets5555;
			constraints.gridx = 2;
			constraints.gridwidth = 1;
			add(helpFriendsCheckBox, constraints);
			
			constraints.insets = insets5_30_5_5;
			constraints.gridx = 0;
			constraints.gridy = 3;
			add(htlLabel, constraints);
			constraints.insets = insets5555;
			constraints.gridx = 1;
			add(htlTextField, constraints);
			constraints.gridx = 2;
			add(htlExplanationLabel, constraints);
			
			constraints.insets = insets5_30_5_5;
			constraints.gridx = 0;
			constraints.gridy = 4;
			add(threadsLabel, constraints);
			constraints.insets = insets5555;
			constraints.gridx = 1;
			add(threadsTextField, constraints);
			
			constraints.insets = insets5_30_5_5;
			constraints.gridx = 0;
			constraints.gridy = 5;
			add(splitfileThreadsLabel, constraints);
			constraints.insets = insets5555;
			constraints.gridx = 1;
			add(splitfileThreadsTextField, constraints);
			constraints.gridx = 2;
			add(splitfileThreadsExplanationLabel, constraints);
			
			constraints.insets = insets5_30_5_5;
			constraints.gridx = 0;
			constraints.gridy = 6;
			add(batchSizeLabel, constraints);
			constraints.insets = insets5555;
			constraints.gridx = 1;
			add(batchSizeTextField, constraints);
			constraints.gridx = 2;
			add(batchSizeExplanationLabel, constraints);
			
			constraints.insets = insets5_30_5_5;
			constraints.gridx = 0;
			constraints.gridy = 7;
			add(indexFileRedundancyLabel, constraints);
			constraints.insets = insets5555;
			constraints.gridx = 1;
			add(indexFileRedundancyTextField, constraints);
			constraints.gridx = 2;
			add(indexFileRedundancyExplanationLabel, constraints);

			// Add listeners
			disableRequestsCheckBox.addActionListener(listener);

		}
		
		/**
		 * Load the settings of this panel
		 * @param uploadSettings class the settings will be loaded from
		 */
		public void loadSettings(SettingsClass uploadSettings) {
			signUploadsCheckBox.setSelected(uploadSettings.getBoolValue("signUploads"));
			helpFriendsCheckBox.setSelected(uploadSettings.getBoolValue("helpFriends"));
			automaticIndexingCheckBox.setSelected(uploadSettings.getBoolValue("automaticIndexing"));
			shareDownloadsCheckBox.setSelected(uploadSettings.getBoolValue("shareDownloads"));
			htlTextField.setText(uploadSettings.getValue("htlUpload"));
			threadsTextField.setText(uploadSettings.getValue("uploadThreads"));
			batchSizeTextField.setText(uploadSettings.getValue("uploadBatchSize"));
			indexFileRedundancyTextField.setText(uploadSettings.getValue("indexFileRedundancy"));
			splitfileThreadsTextField.setText(uploadSettings.getValue("splitfileUploadThreads"));
			disableRequestsCheckBox.setSelected(uploadSettings.getBoolValue(SettingsClass.DISABLE_REQUESTS));

			setEnabled(!disableRequestsCheckBox.isSelected());
		}
		
		public void ok() {
			saveSettings(frostSettings);
		}

		/**
		 * 
		 */
		private void refreshLanguage() {
			disableRequestsCheckBox.setText(languageResource.getString("Disable uploads"));
			signUploadsCheckBox.setText(languageResource.getString("Sign shared files"));
			automaticIndexingCheckBox.setText(languageResource.getString("Automatic Indexing"));
			shareDownloadsCheckBox.setText(languageResource.getString("Share Downloads"));
			helpFriendsCheckBox.setText(
				languageResource.getString("Help spread files from people marked GOOD"));
			htlLabel.setText(languageResource.getString("Upload HTL") + " (8)");
			htlExplanationLabel.setText(languageResource.getString("up htl explanation"));
			threadsLabel.setText(
				languageResource.getString("Number of simultaneous uploads") + " (3)");
			splitfileThreadsLabel.setText(
				languageResource.getString("Number of splitfile threads") + " (15)");
			splitfileThreadsExplanationLabel.setText(
				languageResource.getString("splitfile explanation"));
			batchSizeLabel.setText(languageResource.getString("Upload batch size"));
			batchSizeExplanationLabel.setText(languageResource.getString("batch explanation"));
			indexFileRedundancyLabel.setText(languageResource.getString("Index file redundancy"));
			indexFileRedundancyExplanationLabel.setText(
				languageResource.getString("redundancy explanation"));
		}
		
		/**
		 * @param uploadSettings
		 */
		private void saveSettings(SettingsClass uploadSettings) {
			uploadSettings.setValue("htlUpload", htlTextField.getText());
			uploadSettings.setValue("uploadThreads", threadsTextField.getText());
			uploadSettings.setValue("uploadBatchSize", batchSizeTextField.getText());
			uploadSettings.setValue("indexFileRedundancy", indexFileRedundancyTextField.getText());
			uploadSettings.setValue("splitfileUploadThreads", splitfileThreadsTextField.getText());
			uploadSettings.setValue(SettingsClass.DISABLE_REQUESTS, disableRequestsCheckBox.isSelected());
			uploadSettings.setValue("signUploads", signUploadsCheckBox.isSelected());
			uploadSettings.setValue("automaticIndexing", automaticIndexingCheckBox.isSelected());
			uploadSettings.setValue("shareDownloads", shareDownloadsCheckBox.isSelected());
			uploadSettings.setValue("helpFriends", helpFriendsCheckBox.isSelected());
		}
		
		/* (non-Javadoc)
		 * @see java.awt.Component#setEnabled(boolean)
		 */
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);

			ArrayList exceptions = new ArrayList();
			exceptions.add(disableRequestsCheckBox);
			MiscToolkit.getInstance().setContainerEnabled(this, enabled, exceptions);
		}
	}

	private static Logger logger = Logger.getLogger(OptionsFrame.class.getName());
	boolean _hideBad, _hideAnon;
	Color boardUpdNonSelectedBackgroundColor = null;

	Color boardUpdSelectedBackgroundColor = null;
	JPanel buttonPanel = null; // OK / Cancel
	boolean checkBlock;
	boolean checkBlockBody;

	// this vars hold some settings from start of dialog to the end.
	// then its checked if the settings are changed by user
	boolean checkDisableRequests;
	boolean checkHideBadMessages;
	boolean checkHideCheckMessages;
	boolean checkHideNAMessages;
	String checkMaxMessageDisplay;
	boolean checkSignedOnly;
	JButton chooseBoardUpdNonSelectedBackgroundColor = new JButton("   ");

	//    JLabel startRequestingAfterHtlLabel = new JLabel(LangRes.getString("Insert request if HTL tops:") + " (10)");

	JButton chooseBoardUpdSelectedBackgroundColor = new JButton("   ");
	JPanel contentAreaPanel = null;
	private DisplayPanel displayPanel = null;
	private DownloadPanel downloadPanel = null;

	boolean exitState;
	SettingsClass frostSettings;
	//------------------------------------------------------------------------
	// Class Vars
	//------------------------------------------------------------------------

	private UpdatingLanguageResource languageResource = null;

	//------------------------------------------------------------------------
	// Generate objects
	//------------------------------------------------------------------------
	JPanel mainPanel = null;
	private MiscPanel miscPanel = null;
	private News2Panel news2Panel = null;
	private NewsPanel newsPanel = null;
	JList optionsGroupsList = null;
	JPanel optionsGroupsPanel = null;
	private SearchPanel searchPanel = null;
	boolean shouldReloadMessages = false;
	// the result of this
	boolean shouldRemoveDummyReqFiles = false;
	JTextField TFautomaticUpdate_boardsMinimumUpdateInterval =
		new JTextField(5);
	JTextField TFautomaticUpdate_concurrentBoardUpdates = new JTextField(5);
	JPanel tof3Panel = null;
	JCheckBox tofBoardUpdateVisualization = new JCheckBox();
	
	private UploadPanel uploadPanel = null;

	/**
	 * Constructor, reads init file and inits the gui.
	 */
	public OptionsFrame(Frame parent, SettingsClass newSettingsClass, UpdatingLanguageResource newLanguageResource) {
		super(parent);
		languageResource = newLanguageResource;
		setModal(true);
		translateCheckBox();

		frostSettings = newSettingsClass;
		setDataElements();

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			Init();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception thrown in constructor", e);
		}
		// set initial selection (also sets panel)
		optionsGroupsList.setSelectedIndex(0);
		
		// final layouting
		pack();

		// center dialog on parent
		setLocationRelativeTo(parent);
	}

	/**
	 * Close window and do not save settings
	 */
	private void cancel() {
		exitState = false;

		if (displayPanel != null) {
			//If the display panel has been used, undo any possible skin preview
			displayPanel.cancel();
		}

		dispose();
	}

	/**
	 * cancelButton Action Listener (Cancel)
	 */
	private void cancelButton_actionPerformed(ActionEvent e) {
		cancel();
	}

	/**
	 * Computes the maximum width and height of the various options panels.
	 * Returns Dimension with max. x and y that is needed.
	 * Gets all panels from the ListModel of the option groups list.
	 */
	protected Dimension computeMaxSize(ListModel m) {
		if (m == null || m.getSize() == 0)
			return null;
		int maxX = -1;
		int maxY = -1;
		// misuse a JDialog to determine the panel size before showing
		JDialog dlgdummy = new JDialog();
		for (int x = 0; x < m.getSize(); x++) {
			ListBoxData lbdata = (ListBoxData) m.getElementAt(x);
			JPanel aPanel = lbdata.getPanel();

			contentAreaPanel.removeAll();
			contentAreaPanel.add(aPanel, BorderLayout.CENTER);
			dlgdummy.setContentPane(contentAreaPanel);
			dlgdummy.pack();
			// get size (including bordersize from contentAreaPane)
			int tmpX = contentAreaPanel.getWidth();
			int tmpY = contentAreaPanel.getHeight();
			maxX = Math.max(maxX, tmpX);
			maxY = Math.max(maxY, tmpY);
		}
		dlgdummy = null; // give some hint to gc() , in case its needed
		contentAreaPanel.removeAll();
		return new Dimension(maxX, maxY);
	}

	/**
	 * Build the button panel.
	 */
	protected JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
			// OK / Cancel

			JButton okButton = new JButton(languageResource.getString("OK"));
			JButton cancelButton = new JButton(languageResource.getString("Cancel"));

			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					okButton_actionPerformed(e);
				}
			});
			cancelButton
				.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelButton_actionPerformed(e);
				}
			});
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
		}
		return buttonPanel;
	}

	/**
	 * Build the display panel.
	 */
	private DisplayPanel getDisplayPanel() {
		if (displayPanel == null) {
			displayPanel = new DisplayPanel();
			displayPanel.loadSettings(frostSettings);
		}
		return displayPanel;
	}
	
	/**
	 * Build the download panel.
	 */
	private DownloadPanel getDownloadPanel() {
		if (downloadPanel == null) {
			downloadPanel = new DownloadPanel();
			downloadPanel.loadSettings(frostSettings);
		}
		return downloadPanel;
	}

	/**
	 * Build the misc. panel.
	 */
	private MiscPanel getMiscPanel() {
		if (miscPanel == null) {
			miscPanel = new MiscPanel();
			miscPanel.loadSettings(frostSettings);
		}
		return miscPanel;
	}
	
	/**
	 * Build the news2 panel (spam options).
	 */
	private News2Panel getNews2Panel() {
		if (news2Panel == null) {
			news2Panel = new News2Panel();
			news2Panel.loadSettings(frostSettings);
		}
		return news2Panel;
	}
	
	/**
	 * Build the news panel (general options).
	 */
	private NewsPanel getNewsPanel() {
		if (newsPanel == null) {
			newsPanel = new NewsPanel();
			newsPanel.loadSettings(frostSettings);
		}
		return newsPanel;
	}

	/**
	 * Build the panel containing the list of option groups.
	 */
	protected JPanel getOptionsGroupsPanel() {
		if (optionsGroupsPanel == null) {
			// init the list
			Vector listData = new Vector();
			listData.add(
				new ListBoxData(
					" " + languageResource.getString("Downloads") + " ",
					getDownloadPanel()));
			listData.add(
				new ListBoxData(
					" " + languageResource.getString("Uploads") + " ",
					getUploadPanel()));
			listData.add(
				new ListBoxData(
					" " + languageResource.getString("News") + " (1) ",
					getNewsPanel()));
			listData.add(
				new ListBoxData(
					" " + languageResource.getString("News") + " (2) ",
					getNews2Panel()));
			listData.add(
				new ListBoxData(
					" " + languageResource.getString("News") + " (3) ",
					getTof3Panel()));
			listData.add(
				new ListBoxData(
					" " + languageResource.getString("Search") + " ",
					getSearchPanel()));
			listData.add( 
			    new ListBoxData( 
                    " " + languageResource.getString("Display") + " ",
                    getDisplayPanel()));
			listData.add(
				new ListBoxData(
					" " + languageResource.getString("Miscellaneous") + " ",
					getMiscPanel()));
			optionsGroupsList = new JList(listData);
			optionsGroupsList.setSelectionMode(
				DefaultListSelectionModel.SINGLE_INTERVAL_SELECTION);
			optionsGroupsList.addListSelectionListener(this);

			optionsGroupsPanel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.NORTHWEST;
			constr.fill = GridBagConstraints.BOTH;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			optionsGroupsPanel.add(optionsGroupsList, constr);
			optionsGroupsPanel.setBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(5, 5, 5, 5),
					BorderFactory.createEtchedBorder()));
		}
		return optionsGroupsPanel;
	}

	/**
	 * Build the search panel
	 */
	private SearchPanel getSearchPanel() {
		if (searchPanel == null) {
			searchPanel = new SearchPanel();
			searchPanel.loadSettings(frostSettings);
		}
		return searchPanel;
	}
	
	/**
	 * Build the tof3 panel (automatic update options).
	 */
	protected JPanel getTof3Panel() {
		if (tof3Panel == null) {
			chooseBoardUpdSelectedBackgroundColor
				.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color newCol =
						JColorChooser.showDialog(
							OptionsFrame.this,
					languageResource.getString("Choose updating color of SELECTED boards"),
							boardUpdSelectedBackgroundColor);
					if (newCol != null) {
						boardUpdSelectedBackgroundColor = newCol;
						chooseBoardUpdSelectedBackgroundColor.setBackground(
							boardUpdSelectedBackgroundColor);
					}
				}
			});
			chooseBoardUpdNonSelectedBackgroundColor
				.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color newCol =
						JColorChooser.showDialog(
							OptionsFrame.this,
					languageResource.getString("Choose updating color of NON-SELECTED boards"),
							boardUpdNonSelectedBackgroundColor);
					if (newCol != null) {
						boardUpdNonSelectedBackgroundColor = newCol;
						chooseBoardUpdNonSelectedBackgroundColor.setBackground(
							boardUpdNonSelectedBackgroundColor);
					}
				}
			});

			final JPanel row1Panel =
				new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
			final JPanel row2Panel =
				new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
			row1Panel.add(chooseBoardUpdSelectedBackgroundColor);
			row1Panel.add(
				new JLabel(languageResource.getString("Choose background color if updating board is selected")));
			row2Panel.add(chooseBoardUpdNonSelectedBackgroundColor);
			row2Panel.add(
				new JLabel(languageResource.getString("Choose background color if updating board is not selected")));

			tofBoardUpdateVisualization
				.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setPanelEnabled(
						row1Panel,
						tofBoardUpdateVisualization.isSelected());
					setPanelEnabled(
						row2Panel,
						tofBoardUpdateVisualization.isSelected());
				}
			});

			tof3Panel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			tof3Panel.add(
				new JLabel(languageResource.getString("Automatic update options")),
				constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(5, 25, 5, 5);
			tof3Panel.add(
				new JLabel(
			languageResource.getString(
						"Minimum update interval of a board") + " (" + languageResource.getString("minutes")
						+ ") (45)"),
				constr);
			constr.gridx = 1;
			constr.insets = new Insets(5, 5, 5, 5);
			tof3Panel.add(
				TFautomaticUpdate_boardsMinimumUpdateInterval,
				constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(5, 25, 5, 5);
			tof3Panel.add(
				new JLabel(
			languageResource.getString(
						"Number of concurrently updating boards")
						+ " (6)"),
				constr);
			constr.gridx = 1;
			constr.insets = new Insets(5, 5, 5, 5);
			tof3Panel.add(TFautomaticUpdate_concurrentBoardUpdates, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(15, 5, 5, 5);
			tof3Panel.add(tofBoardUpdateVisualization, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(5, 25, 5, 5);
			tof3Panel.add(row1Panel, constr);
			constr.gridy++;
			tof3Panel.add(row2Panel, constr);
			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			tof3Panel.add(new JLabel(" "), constr);
		}
		return tof3Panel;
	}

	/**
	 * Build the upload panel.
	 */
	private UploadPanel getUploadPanel() {
		if (uploadPanel == null) {
			uploadPanel = new UploadPanel();
			uploadPanel.loadSettings(frostSettings);
		}
		return uploadPanel;
	}
	/**
	 * Build up the whole GUI.
	 */
	private void Init() throws Exception {
		//------------------------------------------------------------------------
		// Configure objects
		//------------------------------------------------------------------------
		this.setTitle(languageResource.getString("Options"));
		// a program should always give users a chance to change the dialog size if needed
		this.setResizable(true);

		mainPanel = new JPanel(new BorderLayout());
		this.getContentPane().add(mainPanel, null); // add Main panel

		// prepare content area panel
		contentAreaPanel = new JPanel(new BorderLayout());
		contentAreaPanel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		contentAreaPanel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 0, 5, 5),
				contentAreaPanel.getBorder()));

		mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);
		mainPanel.add(getOptionsGroupsPanel(), BorderLayout.WEST);

		// compute and set size of contentAreaPanel
		Dimension neededSize = computeMaxSize(optionsGroupsList.getModel());
		contentAreaPanel.setMinimumSize(neededSize);
		contentAreaPanel.setPreferredSize(neededSize);

		mainPanel.add(contentAreaPanel, BorderLayout.CENTER);
	}

	/**
	 * Close window and save settings
	 */
	private void ok() {
		exitState = true;

		if (displayPanel != null) {
			//If the display panel has been used, commit its changes
			displayPanel.ok();
		}
		
		if (downloadPanel != null) {
			//If the download panel has been used, commit its changes
			downloadPanel.ok();
		}
		
		if (searchPanel != null) {
			//If the search panel has been used, commit its changes
			searchPanel.ok();
		}
		
		if (uploadPanel != null) {
			//If the upload panel has been used, commit its changes
			uploadPanel.ok();
		}
		
		if (miscPanel != null) {
			//If the misc panel has been used, commit its changes
			miscPanel.ok();
		}
		
		if (newsPanel != null) {
			//If the news panel has been used, commit its changes
			newsPanel.ok();
		}
		
		if (news2Panel != null) {
			//If the news 2 panel has been used, commit its changes
			news2Panel.ok();
		}

		saveSettings();

		dispose();
	}

	/**
	 * okButton Action Listener (OK)
	 */
	private void okButton_actionPerformed(ActionEvent e) {
		ok();
	}

	/**
	 * When window is about to close, do same as if CANCEL was pressed.
	 */
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}

	/**
	 * Can be called to run dialog and get its answer (true=OK, false=CANCEL)
	 */
	public boolean runDialog() {
		this.exitState = false;
		show(); // run dialog
		return this.exitState;
	}

	/**
	 * Save settings
	 */
	private void saveSettings() {
		frostSettings.setValue(
			"automaticUpdate.concurrentBoardUpdates",
			TFautomaticUpdate_concurrentBoardUpdates.getText());
		frostSettings.setValue(
			"automaticUpdate.boardsMinimumUpdateInterval",
			TFautomaticUpdate_boardsMinimumUpdateInterval.getText());
		frostSettings.setValue(
			"boardUpdateVisualization",
			tofBoardUpdateVisualization.isSelected());

		frostSettings.setObjectValue(
			"boardUpdatingSelectedBackgroundColor",
			boardUpdSelectedBackgroundColor);
		frostSettings.setObjectValue(
			"boardUpdatingNonSelectedBackgroundColor",
			boardUpdNonSelectedBackgroundColor);

		frostSettings.save();

		// now check if some settings changed
		if (checkDisableRequests == true
			&& // BEFORE: uploads disabled?
		frostSettings.getBoolValue(
			SettingsClass.DISABLE_REQUESTS)
				== false) // AFTER: uploads enabled?
			{
			shouldRemoveDummyReqFiles = true;
		}
		if (checkMaxMessageDisplay
			.equals(frostSettings.getValue("maxMessageDisplay"))
			== false
			|| checkSignedOnly != frostSettings.getBoolValue("signedOnly")
			|| checkHideBadMessages
				!= frostSettings.getBoolValue("hideBadMessages")
			|| checkHideCheckMessages
				!= frostSettings.getBoolValue("hideCheckMessages")
			|| checkHideNAMessages != frostSettings.getBoolValue("hideNAMessages")
			|| checkBlock != frostSettings.getBoolValue("blockMessageChecked")
			|| checkBlockBody
				!= frostSettings.getBoolValue("blockMessageBodyChecked")) {
			// at least one setting changed, reload messages
			shouldReloadMessages = true;
		}
	}

	//------------------------------------------------------------------------

	/**
	 * Load settings
	 */
	private void setDataElements() {
		// first set some settings to check later if they are changed by user
		checkDisableRequests = frostSettings.getBoolValue(SettingsClass.DISABLE_REQUESTS);

		checkMaxMessageDisplay = frostSettings.getValue("maxMessageDisplay");
		checkSignedOnly = frostSettings.getBoolValue("signedOnly");
		checkHideBadMessages = frostSettings.getBoolValue("hideBadMessages");
		checkHideCheckMessages =
			frostSettings.getBoolValue("hideCheckMessages");
		checkHideNAMessages = frostSettings.getBoolValue("hideNAMessages");
		checkBlock = frostSettings.getBoolValue("blockMessageChecked");
		checkBlockBody = frostSettings.getBoolValue("blockMessageBodyChecked");

		// now load
		TFautomaticUpdate_concurrentBoardUpdates.setText(
			frostSettings.getValue("automaticUpdate.concurrentBoardUpdates"));
		TFautomaticUpdate_boardsMinimumUpdateInterval.setText(
			frostSettings.getValue(
				"automaticUpdate.boardsMinimumUpdateInterval"));
		tofBoardUpdateVisualization.setSelected(
			frostSettings.getBoolValue("boardUpdateVisualization"));

		boardUpdSelectedBackgroundColor =
			(Color) frostSettings.getObjectValue(
				"boardUpdatingSelectedBackgroundColor");
		boardUpdNonSelectedBackgroundColor =
			(Color) frostSettings.getObjectValue(
				"boardUpdatingNonSelectedBackgroundColor");
		chooseBoardUpdSelectedBackgroundColor.setBackground(
			boardUpdSelectedBackgroundColor);
		chooseBoardUpdNonSelectedBackgroundColor.setBackground(
			boardUpdNonSelectedBackgroundColor);
	}

	private void setPanelEnabled(JPanel panel, boolean enabled) {
		int componentCount = panel.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			Component c = panel.getComponent(x);
			c.setEnabled(enabled);
		}
	}

	/**
	 * Is called after the dialog is hidden.
	 * This method should return true if:
	 *  - signedOnly, hideCheck or hideBad where changed by user
	 *  - a block settings was changed by user
	 * If it returns true, the messages table should be reloaded.
	 */
	public boolean shouldReloadMessages() {
		return shouldReloadMessages;
	}

	/**
	 * Is called after the dialog is hidden.
	 * This method should return true if:
	 *  - setting 'disableRequests' is switched from TRUE to FALSE (means uploading is enabled now)
	 * If it returns true, the dummy request files (created after a key collision)
	 * of all boards should be removed.
	 */
	public boolean shouldRemoveDummyReqFiles() {
		return shouldRemoveDummyReqFiles;
	}

	/**
	 * These translate* methods are used to apply translatable
	 * information to the GUI objects. If you add/remove GUI
	 * objects that use text, please update these methods. Do
	 * not apply text anywhere else.
	 */
	private void translateCheckBox() {
		tofBoardUpdateVisualization.setText(
			languageResource.getString("Show board update visualization")
				+ " ("
				+ languageResource.getString("On")
				+ ")");
	}

	/**
	 * Implementing the ListSelectionListener.
	 * Must change the content of contentAreaPanel to the selected
	 * panel.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;

		JList theList = (JList) e.getSource();
		Object Olbdata = theList.getSelectedValue();

		contentAreaPanel.removeAll();

		if (Olbdata instanceof ListBoxData) {
			ListBoxData lbdata = (ListBoxData) Olbdata;
			JPanel newPanel = lbdata.getPanel();
			contentAreaPanel.add(newPanel, BorderLayout.CENTER);
			newPanel.revalidate();
			newPanel.repaint();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				contentAreaPanel.revalidate();
			}
		});
	}

}
