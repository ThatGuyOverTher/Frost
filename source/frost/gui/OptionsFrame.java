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
import java.io.File;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

import frost.*;
import frost.gui.components.*;
import frost.gui.translation.UpdatingLanguageResource;

/*******************************
 * TODO: - add thread listeners (listen to all running threads) to change the
 *         updating state (bold text in table row) on demand (from bback)
 *******************************/

public class OptionsFrame extends JDialog implements ListSelectionListener {

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
		
		private Listener listener = new Listener();
		
		private JCheckBox disableRequestsCheckBox = new JCheckBox();
		private JCheckBox automaticIndexingCheckBox = new JCheckBox();
		private JCheckBox helpFriendsCheckBox = new JCheckBox();
		private JCheckBox shareDownloadsCheckBox = new JCheckBox();
		private JCheckBox signUploadsCheckBox = new JCheckBox();
	
		private JTextField htlTextField = new JTextField(6);
		private JTextField threadsTextField = new JTextField(6);
		private JTextField splitfileThreadsTextField = new JTextField(6);
		private JTextField batchSizeTextField = new JTextField(6);
		private JTextField indexFileRedundancyTextField = new JTextField(6);
		
		private JLabel htlLabel = new JLabel();
		private JLabel htlExplanationLabel = new JLabel();
		private JLabel threadsLabel = new JLabel();
		private JLabel splitfileThreadsLabel = new JLabel();
		private JLabel splitfileThreadsExplanationLabel = new JLabel();
		private JLabel batchSizeLabel = new JLabel();
		private JLabel batchSizeExplanationLabel = new JLabel();
		private JLabel indexFileRedundancyLabel = new JLabel();
		private JLabel indexFileRedundancyExplanationLabel = new JLabel();

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
			signUploadsCheckBox.setSelected(frostSettings.getBoolValue("signUploads"));
			helpFriendsCheckBox.setSelected(frostSettings.getBoolValue("helpFriends"));
			automaticIndexingCheckBox.setSelected(frostSettings.getBoolValue("automaticIndexing"));
			shareDownloadsCheckBox.setSelected(frostSettings.getBoolValue("shareDownloads"));
			htlTextField.setText(frostSettings.getValue("htlUpload"));
			threadsTextField.setText(frostSettings.getValue("uploadThreads"));
			batchSizeTextField.setText(frostSettings.getValue("uploadBatchSize"));
			indexFileRedundancyTextField.setText(frostSettings.getValue("indexFileRedundancy"));
			splitfileThreadsTextField.setText(frostSettings.getValue("splitfileUploadThreads"));
			disableRequestsCheckBox.setSelected(frostSettings.getBoolValue("disableRequests"));
			
			setEnabled(!disableRequestsCheckBox.isSelected());
		}
		
		public void ok() {
			saveSettings(frostSettings);
		}
		
		/**
		 * @param frostSettings
		 */
		private void saveSettings(SettingsClass frostSettings) {
			frostSettings.setValue("htlUpload", htlTextField.getText());
			frostSettings.setValue("uploadThreads", threadsTextField.getText());
			frostSettings.setValue("uploadBatchSize", batchSizeTextField.getText());
			frostSettings.setValue("indexFileRedundancy", indexFileRedundancyTextField.getText());
			frostSettings.setValue("splitfileUploadThreads", splitfileThreadsTextField.getText());
			frostSettings.setValue("disableRequests", disableRequestsCheckBox.isSelected());
			frostSettings.setValue("signUploads", signUploadsCheckBox.isSelected());
			frostSettings.setValue("automaticIndexing", automaticIndexingCheckBox.isSelected());
			frostSettings.setValue("shareDownloads", shareDownloadsCheckBox.isSelected());
			frostSettings.setValue("helpFriends", helpFriendsCheckBox.isSelected());
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
		 * 
		 */
		private void disableUploadsPressed() {
			// enable panel if checkbox is not selected
			setEnabled(!disableRequestsCheckBox.isSelected());
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



		}
		
		private Listener listener = new Listener();
		
		private JCheckBox disableDownloadsCheckBox = new JCheckBox();
		private JCheckBox enableRequestingCheckBox = new JCheckBox();
		private JCheckBox restartFailedDownloadsCheckBox = new JCheckBox();
		private JCheckBox removeFinishedDownloadsCheckBox = new JCheckBox();
		private JCheckBox tryAllSegmentsCheckBox = new JCheckBox();
		private JCheckBox decodeAfterEachSegmentCheckBox = new JCheckBox();
		
		private JButton browseDirectoryButton = new JButton();
		
		private JTextField directoryTextField = new JTextField();
		private JTextField requestAfterTriesTextField = new JTextField(6);
		private JTextField maxRetriesTextField = new JTextField(6);
		private JTextField waitTimeTextField = new JTextField(6);
		private JTextField threadsTextField = new JTextField(6);
		private JTextField splitfileThreadsTextField = new JTextField(6);
		
		private JLabel waitTimeLabel = new JLabel();
		private JLabel directoryLabel = new JLabel();
		private JLabel maxRetriesLabel = new JLabel();
		private JLabel requestAfterTriesLabel = new JLabel();
		private JLabel threadsTextLabel = new JLabel();
		private JLabel splitfileThreadsLabel = new JLabel();
		
		/**
		 * 
		 */
		public DownloadPanel() {
			super();
			initialize();
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
		
		public void ok() {
			saveSettings(frostSettings);
		}
		
		/**
		 * @param frostSettings
		 */
		private void saveSettings(SettingsClass frostSettings) {
			String downlDirTxt = directoryTextField.getText();
			String filesep = System.getProperty("file.separator");
			// always append a fileseparator to the end of string
			if ((!(downlDirTxt.lastIndexOf(filesep) == (downlDirTxt.length() - 1)))
				|| downlDirTxt.lastIndexOf(filesep) < 0) {
				frostSettings.setValue("downloadDirectory", downlDirTxt + filesep);
			} else {
				frostSettings.setValue("downloadDirectory", downlDirTxt);
			}
			frostSettings.setValue("downloadThreads", threadsTextField.getText());
			frostSettings.setValue(
				"removeFinishedDownloads",
				removeFinishedDownloadsCheckBox.isSelected());

			frostSettings.setValue("splitfileDownloadThreads", splitfileThreadsTextField.getText());
			frostSettings.setValue("disableDownloads", disableDownloadsCheckBox.isSelected());
			frostSettings.setValue(
				"downloadRestartFailedDownloads",
				restartFailedDownloadsCheckBox.isSelected());
			frostSettings.setValue(
				"downloadEnableRequesting",
				enableRequestingCheckBox.isSelected());
			frostSettings.setValue(
				"downloadRequestAfterTries",
				requestAfterTriesTextField.getText());
			frostSettings.setValue("downloadMaxRetries", maxRetriesTextField.getText());
			frostSettings.setValue("downloadWaittime", waitTimeTextField.getText());
			frostSettings.setValue("downloadTryAllSegments", tryAllSegmentsCheckBox.isSelected());
			frostSettings.setValue(
				"downloadDecodeAfterEachSegment",
				decodeAfterEachSegmentCheckBox.isSelected());
		}
		
		/**
		 * Load the settings of this panel
		 * @param downloadSettings class the settings will be loaded from
		 */
		public void loadSettings(SettingsClass downloadSettings) {
			removeFinishedDownloadsCheckBox.setSelected(
						frostSettings.getBoolValue("removeFinishedDownloads"));
			directoryTextField.setText(frostSettings.getValue("downloadDirectory"));
			//        downloadMinHtlTextField.setText(frostSettings.getValue("htl"));
			//        downloadMaxHtlTextField.setText(frostSettings.getValue("htlMax"));
			threadsTextField.setText(frostSettings.getValue("downloadThreads"));
			splitfileThreadsTextField.setText(frostSettings.getValue("splitfileDownloadThreads"));
			disableDownloadsCheckBox.setSelected(frostSettings.getBoolValue("disableDownloads"));
			restartFailedDownloadsCheckBox.setSelected(
				frostSettings.getBoolValue("downloadRestartFailedDownloads"));
			enableRequestingCheckBox.setSelected(
				frostSettings.getBoolValue("downloadEnableRequesting"));
			requestAfterTriesTextField.setText(
				"" + frostSettings.getIntValue("downloadRequestAfterTries"));
			maxRetriesTextField.setText("" + frostSettings.getIntValue("downloadMaxRetries"));
			waitTimeTextField.setText("" + frostSettings.getIntValue("downloadWaittime"));
			tryAllSegmentsCheckBox.setSelected(
				frostSettings.getBoolValue("downloadTryAllSegments"));
			decodeAfterEachSegmentCheckBox.setSelected(
				frostSettings.getBoolValue("downloadDecodeAfterEachSegment"));

			refreshComponentsState();
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
		private void enableRequestingChanged() {
			requestAfterTriesTextField.setEnabled(enableRequestingCheckBox.isSelected());
			requestAfterTriesLabel.setEnabled(enableRequestingCheckBox.isSelected());
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
			}



		}
		
		private Listener listener = new Listener();
		
		private SkinChooser skinChooser = null;
		private JLabel moreSkinsLabel = new JLabel();
		private JLabel fontsLabel = new JLabel();
		
		private JLabel messageBodyLabel = new JLabel();
		private JButton messageBodyButton = new JButton();
		private JLabel selectedMessageBodyFontLabel = new JLabel();
		
		private JLabel messageListLabel = new JLabel();
		private JButton messageListButton = new JButton();
		private JLabel selectedMessageListFontLabel = new JLabel();
		
		private JCheckBox messageBodyAACheckBox = new JCheckBox();
		
		private Font selectedBodyFont = null;
		private Font selectedListFont = null;

		/**
		 * Constructor
		 */
		public DisplayPanel() {
			super();
			initialize();
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
		}
		
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
			
			return fontsPanel;
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
			selectedMessageListFontLabel.setText(getFontLabel(selectedListFont));
			messageBodyAACheckBox.setText(languageResource.getString("EnableMessageBodyAA"));
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

		public void ok() {
			skinChooser.commitChanges();
			saveSettings(frostSettings);
		}

		public void cancel() {
			skinChooser.cancelChanges();
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
				displaySettings.setValue("messageBodyFontName", selectedBodyFont.getFamily());
				displaySettings.setValue("messageBodyFontStyle", selectedBodyFont.getStyle());
				displaySettings.setValue("messageBodyFontSize", selectedBodyFont.getSize());
			}
			if (selectedListFont != null) {
				displaySettings.setValue("messageListFontName", selectedListFont.getFamily());
				displaySettings.setValue("messageListFontStyle", selectedListFont.getStyle());
				displaySettings.setValue("messageListFontSize", selectedListFont.getSize());
			}
			displaySettings.setValue("messageBodyAA", messageBodyAACheckBox.isSelected());
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
			
			String fontName = displaySettings.getValue("messageBodyFontName");
			int fontSize = displaySettings.getIntValue("messageBodyFontSize");
			int fontStyle = displaySettings.getIntValue("messageBodyFontStyle");
			selectedBodyFont = new Font(fontName, fontStyle, fontSize); 
			selectedMessageBodyFontLabel.setText(getFontLabel(selectedBodyFont));	
			
			fontName = displaySettings.getValue("messageListFontName");
			fontSize = displaySettings.getIntValue("messageListFontSize");
			fontStyle = displaySettings.getIntValue("messageListFontStyle");
			selectedListFont = new Font(fontName, fontStyle, fontSize); 
			selectedMessageListFontLabel.setText(getFontLabel(selectedListFont));
			
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
			fontChooser.setSelectedFont(selectedListFont);
			fontChooser.show();
			Font selectedFontTemp = fontChooser.getSelectedFont();
			if (selectedFontTemp != null) {
				selectedListFont = selectedFontTemp;
				selectedMessageListFontLabel.setText(getFontLabel(selectedListFont));
			}
		}

	}
	//------------------------------------------------------------------------
	// Class Vars
	//------------------------------------------------------------------------

	private UpdatingLanguageResource languageResource = null;
	SettingsClass frostSettings;

	boolean exitState;

	//------------------------------------------------------------------------
	// Generate objects
	//------------------------------------------------------------------------
	JPanel mainPanel = null;
	JPanel buttonPanel = null; // OK / Cancel
	private DownloadPanel downloadPanel = null;
	private UploadPanel uploadPanel = null;
	JPanel tofPanel = null;
	JPanel tof2Panel = null;
	JPanel tof3Panel = null;
	private DisplayPanel displayPanel = null;
	JPanel miscPanel = null;
	JPanel searchPanel = null;
	JPanel contentAreaPanel = null;
	JPanel optionsGroupsPanel = null;

	JAATextArea tofTextArea = new JAATextArea(4, 50);

	JTextField tofUploadHtlTextField = new JTextField(5);
	JTextField tofDownloadHtlTextField = new JTextField(5);
	JTextField tofDisplayDaysTextField = new JTextField(5);
	JTextField tofDownloadDaysTextField = new JTextField(5);
	JTextField tofMessageBaseTextField = new JTextField(8);
	JTextField tofBlockMessageTextField = new JTextField(42);
	JTextField tofBlockMessageBodyTextField = new JTextField(42);
	JTextField miscKeyUploadHtlTextField = new JTextField(5);
	JTextField miscKeyDownloadHtlTextField = new JTextField(5);
	JTextField miscAvailableNodesTextField = new JTextField(35);
	//JTextField miscNodePortTextField = new JTextField(8);
	JTextField miscMaxKeysTextField = new JTextField(8);
	JTextField miscAltEditTextField = new JTextField(30);
	JTextField miscAutoSaveInterval = new JTextField(5);
	JCheckBox miscShowSystrayIcon = new JCheckBox();
	JTextField searchAudioExtensionTextField = new JTextField(30);
	JTextField searchVideoExtensionTextField = new JTextField(30);
	JTextField searchDocumentExtensionTextField = new JTextField(30);
	JTextField searchExecutableExtensionTextField = new JTextField(30);
	JTextField searchImageExtensionTextField = new JTextField(30);
	JTextField searchArchiveExtensionTextField = new JTextField(30);
	JTextField searchMaxSearchResults = new JTextField(8);

	JTextField TFautomaticUpdate_boardsMinimumUpdateInterval =
		new JTextField(5);
	JTextField TFautomaticUpdate_concurrentBoardUpdates = new JTextField(5);
	JCheckBox tofBoardUpdateVisualization = new JCheckBox();
	JCheckBox allowEvilBertCheckBox = new JCheckBox();
	JCheckBox miscAltEditCheckBox = new JCheckBox();
	JCheckBox miscSplashscreenCheckBox = new JCheckBox();
	JCheckBox hideBadFiles = new JCheckBox();
	JCheckBox hideAnonFiles = new JCheckBox();
	JList optionsGroupsList = null;

	// new options in WOT:
	// TODO: translation
	JTextField sampleInterval = new JTextField(5);
	JTextField spamTreshold = new JTextField(5);

	JCheckBox signedOnly = new JCheckBox();
	JCheckBox hideBadMessages = new JCheckBox();
	JCheckBox hideCheckMessages = new JCheckBox();
	JCheckBox hideNAMessages = new JCheckBox();
	JCheckBox block = new JCheckBox();
	// TODO: translate
	JCheckBox blockBody = new JCheckBox();
	JCheckBox doBoardBackoff = new JCheckBox();
	JLabel interval = new JLabel();
	JLabel treshold = new JLabel();
	//    JLabel startRequestingAfterHtlLabel = new JLabel(LangRes.getString("Insert request if HTL tops:") + " (10)");
	JCheckBox cleanUP = new JCheckBox();

	JButton chooseBoardUpdSelectedBackgroundColor = new JButton("   ");
	JButton chooseBoardUpdNonSelectedBackgroundColor = new JButton("   ");

	Color boardUpdSelectedBackgroundColor = null;
	Color boardUpdNonSelectedBackgroundColor = null;

	// this vars hold some settings from start of dialog to the end.
	// then its checked if the settings are changed by user
	boolean checkDisableRequests;
	String checkMaxMessageDisplay;
	boolean checkSignedOnly;
	boolean checkHideBadMessages;
	boolean checkHideCheckMessages;
	boolean checkHideNAMessages;
	boolean checkBlock;
	boolean checkBlockBody;
	// the result of this
	boolean shouldRemoveDummyReqFiles = false;
	boolean shouldReloadMessages = false;
	boolean _hideBad, _hideAnon;

	/**
	 * These translate* methods are used to apply translatable
	 * information to the GUI objects. If you add/remove GUI
	 * objects that use text, please update these methods. Do
	 * not apply text anywhere else.
	 */
	private void translateCheckBox() {
		miscSplashscreenCheckBox.setText(languageResource.getString("Disable splashscreen"));
		miscShowSystrayIcon.setText(languageResource.getString("Show systray icon"));
		tofBoardUpdateVisualization.setText(
			languageResource.getString("Show board update visualization")
				+ " ("
				+ languageResource.getString("On")
				+ ")");
		allowEvilBertCheckBox.setText(
			languageResource.getString("Allow 2 byte characters")
				+ " ("
				+ languageResource.getString("Off")
				+ ")");
		miscAltEditCheckBox.setText(
			languageResource.getString("Use editor for writing messages")
				+ " ("
				+ languageResource.getString("Off")
				+ ")");
		hideBadFiles.setText(languageResource.getString("Hide files from people marked BAD"));
		hideAnonFiles.setText(languageResource.getString("Hide files from anonymous users"));
		signedOnly.setText(languageResource.getString("Hide unsigned messages"));
		hideBadMessages.setText(
			languageResource.getString("Hide messages flagged BAD")
				+ " ("
				+ languageResource.getString("Off")
				+ ")");
		hideCheckMessages.setText(
			languageResource.getString("Hide messages flagged CHECK")
				+ " ("
				+ languageResource.getString("Off")
				+ ")");
		hideNAMessages.setText(
			languageResource.getString("Hide messages flagged N/A")
				+ " ("
				+ languageResource.getString("Off")
				+ ")");
		block.setText(
			languageResource.getString("Block messages with subject containing (separate by ';' )")
				+ ": ");
		blockBody.setText(
			languageResource.getString("Block messages with body containing (separate by ';' )")
				+ ": ");
		doBoardBackoff.setText(languageResource.getString("Do spam detection"));
		cleanUP.setText(languageResource.getString("Clean the keypool"));
	}
	private void translateLabel() {
		interval.setText(
			languageResource.getString("Sample interval")
				+ " ("
				+ languageResource.getString("hours")
				+ ")");
		treshold.setText(languageResource.getString("Threshold of blocked messages"));
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

		//------------------------------------------------------------------------
		// ChangeListener
		//------------------------------------------------------------------------
		miscAltEditCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource().equals(miscAltEditCheckBox))
					miscAltEditTextField.setEditable(
						miscAltEditCheckBox.isSelected());
			}
		});
		block.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource().equals(block))
					tofBlockMessageTextField.setEnabled(block.isSelected());
			}
		});
		blockBody.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource().equals(blockBody))
					tofBlockMessageBodyTextField.setEnabled(
						blockBody.isSelected());
			}
		});
		doBoardBackoff.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource().equals(doBoardBackoff)) {
					sampleInterval.setEnabled(doBoardBackoff.isSelected());
					spamTreshold.setEnabled(doBoardBackoff.isSelected());
					treshold.setEnabled(doBoardBackoff.isSelected());
					interval.setEnabled(doBoardBackoff.isSelected());
				}
			}
		});
		//------------------------------------------------------------------------

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
					getTofPanel()));
			listData.add(
				new ListBoxData(
					" " + languageResource.getString("News") + " (2) ",
					getTof2Panel()));
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
	 * Build the tof panel.
	 */
	protected JPanel getTofPanel() {
		if (tofPanel == null) {
			// Initialize AA and fot fot the tofTextArea
			String fontName = frostSettings.getValue("messageBodyFontName");
			int fontStyle = frostSettings.getIntValue("messageBodyFontStyle");
			int fontSize = frostSettings.getIntValue("messageBodyFontSize");
			Font tofFont = new Font(fontName, fontStyle, fontSize);
			if (!tofFont.getFamily().equals(fontName)) {
				System.out.println("The selected font was not found in your system");
				System.out.println("That selection will be changed to \"Monospaced\".\n");
				frostSettings.setValue("messageBodyFontName", "Monospaced");
				tofFont = new Font("Monospaced", fontStyle, fontSize);
			}
			tofTextArea.setFont(tofFont);
			tofTextArea.setAntiAliasEnabled(frostSettings.getBoolValue("messageBodyAA"));
			
			//Build the panel
			tofPanel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			tofPanel.add(
				new JLabel(languageResource.getString("Message upload HTL") + " (21)"),
				constr);
			constr.gridx = 1;
			tofPanel.add(tofUploadHtlTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			tofPanel.add(
				new JLabel(
			languageResource.getString("Message download HTL") + " (23)"),
				constr);
			constr.gridx = 1;
			tofPanel.add(tofDownloadHtlTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			tofPanel.add(
				new JLabel(
			languageResource.getString("Number of days to display") + " (10)"),
				constr);
			constr.gridx = 1;
			tofPanel.add(tofDisplayDaysTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			tofPanel.add(
				new JLabel(
			languageResource.getString("Number of days to download backwards")
						+ " (3)"),
				constr);
			constr.gridx = 1;
			tofPanel.add(tofDownloadDaysTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			tofPanel.add(
				new JLabel(languageResource.getString("Message base") + " (news)"),
				constr);
			constr.gridx = 1;
			tofPanel.add(tofMessageBaseTextField, constr);
			constr.gridy++;
			constr.gridx = 0;

			tofPanel.add(new JLabel(languageResource.getString("Signature")), constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.gridwidth = 2;
			constr.weightx = 0.7;
			constr.fill = GridBagConstraints.HORIZONTAL;
			constr.insets = new Insets(0, 5, 5, 5);
			JScrollPane tofSignatureScrollPane = new JScrollPane();
			tofSignatureScrollPane.getViewport().add(tofTextArea);
			tofPanel.add(tofSignatureScrollPane, constr);
			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			tofPanel.add(new JLabel(" "), constr);
		}
		return tofPanel;
	}

	/**
	 * Build the tof2 panel (spam options).
	 */
	protected JPanel getTof2Panel() {
		if (tof2Panel == null) {
			tof2Panel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			constr.gridwidth = 2;
			tof2Panel.add(block, constr);
			constr.gridy++;
			constr.insets = new Insets(0, 25, 5, 5);
			tof2Panel.add(tofBlockMessageTextField, constr);
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridy++;
			tof2Panel.add(blockBody, constr);
			constr.gridy++;
			constr.insets = new Insets(0, 25, 5, 5);
			tof2Panel.add(tofBlockMessageBodyTextField, constr);
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridwidth = 1;
			constr.gridy++;
			constr.gridx = 0;
			tof2Panel.add(signedOnly, constr);
			constr.gridx = 1;
			tof2Panel.add(hideBadMessages, constr);
			constr.gridy++;
			constr.gridx = 0;
			tof2Panel.add(hideCheckMessages, constr);
			constr.gridx = 1;
			tof2Panel.add(hideNAMessages, constr);
			constr.gridy++;
			constr.gridx = 0;
			tof2Panel.add(doBoardBackoff, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(0, 25, 5, 5);
			tof2Panel.add(interval, constr);
			constr.gridx = 1;
			constr.insets = new Insets(5, 0, 5, 5);
			tof2Panel.add(sampleInterval, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(0, 25, 5, 5);
			tof2Panel.add(treshold, constr);
			constr.gridx = 1;
			constr.insets = new Insets(5, 0, 5, 5);
			tof2Panel.add(spamTreshold, constr);
			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			tof2Panel.add(new JLabel(" "), constr);
		}
		return tof2Panel;
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
	 * Build the misc. panel.
	 */
	protected JPanel getMiscPanel() {
		if (miscPanel == null) {
			miscPanel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			miscPanel.add(
				new JLabel(languageResource.getString("Keyfile upload HTL") + " (21)"),
				constr);
			constr.gridx = 1;
			miscPanel.add(miscKeyUploadHtlTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(
				new JLabel(
			languageResource.getString("Keyfile download HTL") + " (24)"),
				constr);
			constr.gridx = 1;
			miscPanel.add(miscKeyDownloadHtlTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(
				new JLabel(languageResource.getString("list of nodes")),
				constr);
			constr.insets = new Insets(0, 5, 5, 5);
			constr.gridy++;
			miscPanel.add(
				new JLabel(" (nodeA:port1, nodeB:port2, ...)"),
				constr);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(miscAvailableNodesTextField, constr);
			//miscAvailableNodesTextField.setEnabled(false); 
			//constr.gridy++;
			//constr.gridx = 0;
			//miscPanel.add(new JLabel(LangRes.getString("Node port:") + " (8481)"), constr);
			//constr.gridx = 1;
			//miscPanel.add(miscNodePortTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(5, 5, 5, 5);
			miscPanel.add(
				new JLabel(
			languageResource.getString("Maximum number of keys to store")
						+ " (100000)"),
				constr);
			constr.gridx = 1;
			miscPanel.add(miscMaxKeysTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.gridwidth = 2;
			miscPanel.add(allowEvilBertCheckBox, constr);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(miscAltEditCheckBox, constr);
			constr.gridy++;
			constr.gridx = 0;
			constr.insets = new Insets(0, 25, 10, 5);
			miscPanel.add(miscAltEditTextField, constr);
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(cleanUP, constr);
			constr.gridy++;
			constr.gridx = 0;
			miscPanel.add(
				new JLabel(
			languageResource.getString("Automatic saving interval") + " (15)"),
				constr);
			constr.gridx = 1;
			miscPanel.add(miscAutoSaveInterval, constr);

			constr.gridy++;
			constr.gridx = 0;
			File splashchk = new File("nosplash.chk");
			if (splashchk.exists()) {
				miscSplashscreenCheckBox.setSelected(true);
			} else {
				miscSplashscreenCheckBox.setSelected(false);
			}
			miscPanel.add(miscSplashscreenCheckBox, constr);

			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			miscPanel.add(new JLabel(" "), constr);
		}
		return miscPanel;
	}

	/**
	 * Build the search panel
	 */
	protected JPanel getSearchPanel() {
		if (searchPanel == null) {
			searchPanel = new JPanel(new GridBagLayout());
			GridBagConstraints constr = new GridBagConstraints();
			constr.anchor = GridBagConstraints.WEST;
			constr.insets = new Insets(5, 5, 5, 5);
			constr.gridx = 0;
			constr.gridy = 0;
			searchPanel.add(
				new JLabel(languageResource.getString("Image Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchImageExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(languageResource.getString("Video Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchVideoExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(languageResource.getString("Archive Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchArchiveExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(languageResource.getString("Document Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchDocumentExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(languageResource.getString("Audio Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchAudioExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(languageResource.getString("Executable Extension")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchExecutableExtensionTextField, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(
				new JLabel(languageResource.getString("Maximum search results")),
				constr);
			constr.gridx = 1;
			searchPanel.add(searchMaxSearchResults, constr);
			constr.gridy++;
			constr.gridx = 0;
			searchPanel.add(hideBadFiles, constr);
			constr.gridx = 1;
			searchPanel.add(hideAnonFiles, constr);

			// filler (glue)
			constr.gridy++;
			constr.gridx = 1;
			constr.weightx = 0.7;
			constr.weighty = 0.7;
			constr.insets = new Insets(0, 0, 0, 0);
			constr.fill = GridBagConstraints.BOTH;
			searchPanel.add(new JLabel(" "), constr);
		}
		return searchPanel;
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
		}
		contentAreaPanel.updateUI();
	}

	/**
	 * A simple helper class to store JPanels and their name into a JList.
	 */
	class ListBoxData {
		JPanel panel;
		String name;
		public ListBoxData(String n, JPanel p) {
			panel = p;
			name = n;
		}
		public String toString() {
			return name;
		}
		public JPanel getPanel() {
			return panel;
		}
	}

	/**
	 * okButton Action Listener (OK)
	 */
	private void okButton_actionPerformed(ActionEvent e) {
		ok();
	}

	/**
	 * cancelButton Action Listener (Cancel)
	 */
	private void cancelButton_actionPerformed(ActionEvent e) {
		cancel();
	}

	private void setPanelEnabled(JPanel panel, boolean enabled) {
		int componentCount = panel.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			Component c = panel.getComponent(x);
			c.setEnabled(enabled);
		}
	}

	//------------------------------------------------------------------------

	/**
	 * Load settings
	 */
	private void setDataElements() {
		// first set some settings to check later if they are changed by user
		checkDisableRequests = frostSettings.getBoolValue("disableRequests");

		checkMaxMessageDisplay = frostSettings.getValue("maxMessageDisplay");
		checkSignedOnly = frostSettings.getBoolValue("signedOnly");
		checkHideBadMessages = frostSettings.getBoolValue("hideBadMessages");
		checkHideCheckMessages =
			frostSettings.getBoolValue("hideCheckMessages");
		checkHideNAMessages = frostSettings.getBoolValue("hideNAMessages");
		checkBlock = frostSettings.getBoolValue("blockMessageChecked");
		checkBlockBody = frostSettings.getBoolValue("blockMessageBodyChecked");
		_hideBad = frostSettings.getBoolValue("hideBadFiles");
		_hideAnon = frostSettings.getBoolValue("hideAnonFiles");

		// now load
		hideBadFiles.setSelected(_hideBad);
		hideAnonFiles.setSelected(_hideAnon);
		allowEvilBertCheckBox.setSelected(
			frostSettings.getBoolValue("allowEvilBert"));
		miscAltEditCheckBox.setSelected(
			frostSettings.getBoolValue("useAltEdit"));
		signedOnly.setSelected(frostSettings.getBoolValue("signedOnly"));
		doBoardBackoff.setSelected(
			frostSettings.getBoolValue("doBoardBackoff"));
		interval.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
		treshold.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
		sampleInterval.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
		spamTreshold.setEnabled(frostSettings.getBoolValue("doBoardBackoff"));
		sampleInterval.setText(frostSettings.getValue("sampleInterval"));
		spamTreshold.setText(frostSettings.getValue("spamTreshold"));
		hideBadMessages.setSelected(
			frostSettings.getBoolValue("hideBadMessages"));
		hideCheckMessages.setSelected(
			frostSettings.getBoolValue("hideCheckMessages"));
		hideNAMessages.setSelected(
			frostSettings.getBoolValue("hideNAMessages"));
		block.setSelected(frostSettings.getBoolValue("blockMessageChecked"));
		blockBody.setSelected(
			frostSettings.getBoolValue("blockMessageBodyChecked"));
		miscAltEditTextField.setEditable(miscAltEditCheckBox.isSelected());
		tofUploadHtlTextField.setText(frostSettings.getValue("tofUploadHtl"));
		tofDownloadHtlTextField.setText(
			frostSettings.getValue("tofDownloadHtl"));
		tofDisplayDaysTextField.setText(
			frostSettings.getValue("maxMessageDisplay"));
		tofDownloadDaysTextField.setText(
			frostSettings.getValue("maxMessageDownload"));
		miscKeyUploadHtlTextField.setText(
			frostSettings.getValue("keyUploadHtl"));
		miscKeyDownloadHtlTextField.setText(
			frostSettings.getValue("keyDownloadHtl"));
		miscShowSystrayIcon.setSelected(
			frostSettings.getBoolValue("showSystrayIcon"));
		miscAvailableNodesTextField.setText(
			frostSettings.getValue("availableNodes"));
		//miscNodePortTextField.setText(frostSettings.getValue("nodePort"));
		miscAltEditTextField.setText(frostSettings.getValue("altEdit"));
		miscMaxKeysTextField.setText(frostSettings.getValue("maxKeys"));
		tofMessageBaseTextField.setText(frostSettings.getValue("messageBase"));
		tofBlockMessageTextField.setText(
			frostSettings.getValue("blockMessage"));
		tofBlockMessageTextField.setEnabled(
			frostSettings.getBoolValue("blockMessageChecked"));
		tofBlockMessageBodyTextField.setText(
			frostSettings.getValue("blockMessageBody"));
		tofBlockMessageBodyTextField.setEnabled(
			frostSettings.getBoolValue("blockMessageBodyChecked"));
		searchMaxSearchResults.setText(
			"" + frostSettings.getIntValue("maxSearchResults"));
		searchAudioExtensionTextField.setText(
			frostSettings.getValue("audioExtension"));
		searchImageExtensionTextField.setText(
			frostSettings.getValue("imageExtension"));
		searchVideoExtensionTextField.setText(
			frostSettings.getValue("videoExtension"));
		searchDocumentExtensionTextField.setText(
			frostSettings.getValue("documentExtension"));
		searchExecutableExtensionTextField.setText(
			frostSettings.getValue("executableExtension"));
		searchArchiveExtensionTextField.setText(
			frostSettings.getValue("archiveExtension"));
		cleanUP.setSelected(frostSettings.getBoolValue("doCleanUp"));
		TFautomaticUpdate_concurrentBoardUpdates.setText(
			frostSettings.getValue("automaticUpdate.concurrentBoardUpdates"));
		TFautomaticUpdate_boardsMinimumUpdateInterval.setText(
			frostSettings.getValue(
				"automaticUpdate.boardsMinimumUpdateInterval"));
		tofBoardUpdateVisualization.setSelected(
			frostSettings.getBoolValue("boardUpdateVisualization"));

		miscAutoSaveInterval.setText(
			"" + frostSettings.getIntValue("autoSaveInterval"));


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

	/**
	 * Save settings
	 */
	private void saveSettings() {
		frostSettings.setValue("tofUploadHtl", tofUploadHtlTextField.getText());
		frostSettings.setValue(
			"tofDownloadHtl",
			tofDownloadHtlTextField.getText());
		frostSettings.setValue(
			"keyUploadHtl",
			miscKeyUploadHtlTextField.getText());
		frostSettings.setValue(
			"keyDownloadHtl",
			miscKeyDownloadHtlTextField.getText());
		frostSettings.setValue(
			"maxMessageDisplay",
			tofDisplayDaysTextField.getText());
		frostSettings.setValue(
			"maxMessageDownload",
			tofDownloadDaysTextField.getText());
		frostSettings.setValue(
			"availableNodes",
			miscAvailableNodesTextField.getText());
		//frostSettings.setValue("nodePort", miscNodePortTextField.getText());
		frostSettings.setValue("maxKeys", miscMaxKeysTextField.getText());
		frostSettings.setValue(
			"messageBase",
			((tofMessageBaseTextField.getText()).trim()).toLowerCase());
		frostSettings.setValue(
			"showSystrayIcon",
			miscShowSystrayIcon.isSelected());

		frostSettings.setValue(
			"blockMessage",
			((tofBlockMessageTextField.getText()).trim()).toLowerCase());
		frostSettings.setValue("blockMessageChecked", block.isSelected());
		frostSettings.setValue(
			"blockMessageBody",
			((tofBlockMessageBodyTextField.getText()).trim()).toLowerCase());
		frostSettings.setValue(
			"blockMessageBodyChecked",
			blockBody.isSelected());
		frostSettings.setValue("doBoardBackoff", doBoardBackoff.isSelected());
		frostSettings.setValue("spamTreshold", spamTreshold.getText());
		frostSettings.setValue("sampleInterval", sampleInterval.getText());

		frostSettings.setValue(
			"allowEvilBert",
			allowEvilBertCheckBox.isSelected());
		frostSettings.setValue(
			"maxSearchResults",
			searchMaxSearchResults.getText());
		frostSettings.setValue(
			"audioExtension",
			searchAudioExtensionTextField.getText().toLowerCase());
		frostSettings.setValue(
			"imageExtension",
			searchImageExtensionTextField.getText().toLowerCase());
		frostSettings.setValue(
			"videoExtension",
			searchVideoExtensionTextField.getText().toLowerCase());
		frostSettings.setValue(
			"documentExtension",
			searchDocumentExtensionTextField.getText().toLowerCase());
		frostSettings.setValue(
			"executableExtension",
			searchExecutableExtensionTextField.getText().toLowerCase());
		frostSettings.setValue(
			"archiveExtension",
			searchArchiveExtensionTextField.getText().toLowerCase());
		frostSettings.setValue("useAltEdit", miscAltEditCheckBox.isSelected());
		frostSettings.setValue("signedOnly", signedOnly.isSelected());
		frostSettings.setValue("hideBadMessages", hideBadMessages.isSelected());
		frostSettings.setValue(
			"hideCheckMessages",
			hideCheckMessages.isSelected());
		frostSettings.setValue("hideNAMessages", hideNAMessages.isSelected());
		frostSettings.setValue("altEdit", miscAltEditTextField.getText());
		frostSettings.setValue("doCleanUp", cleanUP.isSelected());

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

		frostSettings.setValue(
			"autoSaveInterval",
			miscAutoSaveInterval.getText());
		frostSettings.setValue("hideBadFiles", hideBadFiles.isSelected());
		frostSettings.setValue("hideAnonFiles", hideAnonFiles.isSelected());

		frostSettings.writeSettingsFile();

		// now check if some settings changed
		if (checkDisableRequests == true
			&& // BEFORE: uploads disabled?
		frostSettings.getBoolValue(
			"disableRequests")
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
			//If the display panel has been used, commit its changes
			downloadPanel.ok();
		}
		
		if (uploadPanel != null) {
			//If the display panel has been used, commit its changes
			uploadPanel.ok();
		}

		saveSettings();
		saveSignature();

		//Save splashchk
		try {
			File splashFile = new File("nosplash.chk");
			if (miscSplashscreenCheckBox.isSelected()) {
				splashFile.createNewFile();
			} else {
				splashFile.delete();
			}
		} catch (java.io.IOException ioex) {
			System.out.println(
				"Could not create splashscreen checkfile: " + ioex);
		}

		dispose();
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
	 * Loads signature.txt into tofTextArea
	 */
	private void loadSignature() {
		File signature = new File("signature.txt");
		if (signature.isFile()) {
			tofTextArea.setText(FileAccess.readFile("signature.txt", "UTF-8"));
		}
	}

	/**
	 * Saves signature.txt to disk
	 */
	private void saveSignature() {
		FileAccess.writeFile(tofTextArea.getText(), "signature.txt", "UTF-8");
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
	 * Can be called to run dialog and get its answer (true=OK, false=CANCEL)
	 */
	public boolean runDialog() {
		this.exitState = false;
		show(); // run dialog
		return this.exitState;
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
	 * Constructor, reads init file and inits the gui.
	 */
	public OptionsFrame(Frame parent, UpdatingLanguageResource newLanguageResource) {
		super(parent);
		languageResource = newLanguageResource;
		setModal(true);
		translateCheckBox();
		translateLabel();

		frostSettings = new SettingsClass();
		setDataElements();
		loadSignature();

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			Init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// set initial selection (also sets panel)
		optionsGroupsList.setSelectedIndex(0);
		
		// final layouting
		pack();

		// center dialog on parent
		setLocationRelativeTo(parent);
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

}
