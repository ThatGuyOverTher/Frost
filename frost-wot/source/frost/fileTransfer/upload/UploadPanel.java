/*
 * Created on Nov 14, 2003
 */
package frost.fileTransfer.upload;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;

import frost.*;
import frost.ext.Execute;
import frost.gui.TofTree;
import frost.gui.objects.FrostBoardObject;
import frost.util.gui.JSkinnablePopupMenu;
import frost.util.gui.translation.*;
import frost.util.model.ModelItem;
import frost.util.model.gui.SortedModelTable;

/**
 * 
 */
public class UploadPanel extends JPanel {
	/**
	 *  
	 */
	private class PopupMenuUpload extends JSkinnablePopupMenu implements ActionListener, LanguageListener {

		private JMenuItem cancelItem = new JMenuItem();
		private JMenuItem copyChkKeyAndFilenameToClipboardItem = new JMenuItem();
		private JMenuItem copyChkKeyToClipboardItem = new JMenuItem();
		private JMenuItem generateChkForSelectedFilesItem = new JMenuItem();
		private JMenuItem reloadAllFilesItem = new JMenuItem();
		private JMenuItem reloadSelectedFilesItem = new JMenuItem();
		private JMenuItem removeAllFilesItem = new JMenuItem();
		private JMenuItem removeSelectedFilesItem = new JMenuItem();
		private JMenuItem restoreDefaultFilenamesForAllFilesItem = new JMenuItem();
		private JMenuItem restoreDefaultFilenamesForSelectedFilesItem = new JMenuItem();
		private JMenuItem setPrefixForAllFilesItem = new JMenuItem();
		private JMenuItem setPrefixForSelectedFilesItem = new JMenuItem();

		private JMenu changeDestinationBoardMenu = new JMenu();
		private JMenu copyToClipboardMenu = new JMenu();

		/**
		 *  
		 */
		public PopupMenuUpload() {
			super();
			initialize();
		}

		private void initialize() {
			refreshLanguage();

			copyToClipboardMenu.add(copyChkKeyToClipboardItem);
			copyToClipboardMenu.add(copyChkKeyAndFilenameToClipboardItem);

			copyChkKeyToClipboardItem.addActionListener(this);
			copyChkKeyAndFilenameToClipboardItem.addActionListener(this);
			removeSelectedFilesItem.addActionListener(this);
			removeAllFilesItem.addActionListener(this);
			reloadSelectedFilesItem.addActionListener(this);
			reloadAllFilesItem.addActionListener(this);
			generateChkForSelectedFilesItem.addActionListener(this);
			setPrefixForSelectedFilesItem.addActionListener(this);
			setPrefixForAllFilesItem.addActionListener(this);
			restoreDefaultFilenamesForSelectedFilesItem.addActionListener(this);
			restoreDefaultFilenamesForAllFilesItem.addActionListener(this);
		}

		private void refreshLanguage() {
			cancelItem.setText(languageResource.getString("Cancel"));
			copyChkKeyAndFilenameToClipboardItem.setText(
				languageResource.getString("CHK key + filename"));
			copyChkKeyToClipboardItem.setText(languageResource.getString("CHK key"));
			generateChkForSelectedFilesItem.setText(
				languageResource.getString("Start encoding of selected files"));
			reloadAllFilesItem.setText(languageResource.getString("Reload all files"));
			reloadSelectedFilesItem.setText(languageResource.getString("Reload selected files"));
			removeAllFilesItem.setText(languageResource.getString("Remove all files"));
			removeSelectedFilesItem.setText(languageResource.getString("Remove selected files"));
			restoreDefaultFilenamesForAllFilesItem.setText(
				languageResource.getString("Restore default filenames for all files"));
			restoreDefaultFilenamesForSelectedFilesItem.setText(
				languageResource.getString("Restore default filenames for selected files"));
			setPrefixForAllFilesItem.setText(
				languageResource.getString("Set prefix for all files"));
			setPrefixForSelectedFilesItem.setText(
				languageResource.getString("Set prefix for selected files"));

			changeDestinationBoardMenu.setText(
				languageResource.getString("Change destination board"));
			copyToClipboardMenu.setText(languageResource.getString("Copy to clipboard") + "...");
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == copyChkKeyToClipboardItem) {
				copyChkKeyToClipboard();
			}
			if (e.getSource() == copyChkKeyAndFilenameToClipboardItem) {
				copyChkKeyAndFilenameToClipboard();
			}
			if (e.getSource() == removeSelectedFilesItem) {
				removeSelectedFiles();
			}
			if (e.getSource() == removeAllFilesItem) {
				removeAllFiles();
			}
			if (e.getSource() == reloadSelectedFilesItem) {
				reloadSelectedFiles();
			}
			if (e.getSource() == reloadAllFilesItem) {
				reloadAllFiles();
			}
			if (e.getSource() == generateChkForSelectedFilesItem) {
				generateChkForSelectedFiles();
			}
			if (e.getSource() == setPrefixForSelectedFilesItem) {
				setPrefixForSelectedFiles();
			}
			if (e.getSource() == setPrefixForAllFilesItem) {
				setPrefixForAllFiles();
			}
			if (e.getSource() == restoreDefaultFilenamesForSelectedFilesItem) {
				restoreDefaultFilenamesForSelectedFiles();
			}
			if (e.getSource() == restoreDefaultFilenamesForAllFilesItem) {
				restoreDefaultFilenamesForAllFiles();
			}
		}

		/**
		 * Restore default filenames for all files
		 */
		private void restoreDefaultFilenamesForAllFiles() {
			model.removePrefixFromAllItems();
		}

		/**
		 * Restore default filenames for selected files
		 */
		private void restoreDefaultFilenamesForSelectedFiles() {
			model.removePrefixFromItems(modelTable.getSelectedItems());
		}

		/**
		 * Set Prefix for all files
		 */
		private void setPrefixForAllFiles() {
			String prefix =
				JOptionPane.showInputDialog(
					languageResource.getString(
						"Please enter the prefix you want to use for your files."));
			if (prefix != null) {
				model.setPrefixToAllItems(prefix);
			}
		}
		/**
		 * Set Prefix for selected files
		 */
		private void setPrefixForSelectedFiles() {
			String prefix =
				JOptionPane.showInputDialog(
					languageResource.getString(
						"Please enter the prefix you want to use for your files."));
			if (prefix != null) {
				model.setPrefixToItems(modelTable.getSelectedItems(), prefix);
			}
		}

		/**
		 * Generate CHK for selected files 
		 */
		private void generateChkForSelectedFiles() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.generateChkItems(selectedItems);
		} 
		
		/**
		 * Reload all files
		 */
		private void reloadAllFiles() {
			model.requestAllItems();
		}

		/**
		 * Reload selected files
		 */
		private void reloadSelectedFiles() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.requestItems(selectedItems);
		}

		/**
		 * Remove all files
		 */
		private void removeAllFiles() {
			model.clear();
		}

		/**
		 * Remove selected files
		 */
		private void removeSelectedFiles() {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.removeItems(selectedItems);
		}

		/**
		 * add CHK key + filename to clipboard 
		 */
		private void copyChkKeyAndFilenameToClipboard() {
			ModelItem selectedItem = modelTable.getSelectedItem();
			if (selectedItem != null) {
				FrostUploadItem ulItem = (FrostUploadItem) selectedItem;
				String chkKey = ulItem.getKey();
				String filename = ulItem.getFileName();
				if (chkKey != null && filename != null) {
					mixed.setSystemClipboard(chkKey + "/" + filename);
				}
			}
		}

		/**
		 * add CHK key to clipboard
		 */
		private void copyChkKeyToClipboard() {
			ModelItem selectedItem = modelTable.getSelectedItem();
			if (selectedItem != null) {
				FrostUploadItem ulItem = (FrostUploadItem) selectedItem;
				String chkKey = ulItem.getKey();
				if (chkKey != null) {
					mixed.setSystemClipboard(chkKey);
				}
			}
		}

		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			removeAll();

			ModelItem[] selectedItems = modelTable.getSelectedItems();

			if (selectedItems.length == 1) {
				// if 1 item is selected
				FrostUploadItem item = (FrostUploadItem) selectedItems[0];
				if (item.getKey() != null) {
					add(copyToClipboardMenu);
					addSeparator();
				}
			}

			JMenu removeSubMenu = new JMenu(languageResource.getString("Remove") + "...");
			if (selectedItems.length != 0) {
				//If at least 1 item is selected
				removeSubMenu.add(removeSelectedFilesItem);
			}
			removeSubMenu.add(removeAllFilesItem);

			add(removeSubMenu);
			addSeparator();
			if (selectedItems.length != 0) {
				// If at least 1 item is selected
				add(generateChkForSelectedFilesItem);
				add(reloadSelectedFilesItem);
			}
			add(reloadAllFilesItem);
			addSeparator();
			//these options available only if automatic indexing is off and file is not 
			//hashed already.
			//the options that operate on all files (not only selected) are removed for now
			//because making them check whether the file has been indexed or not is a pain
			//feel free to implement ;)
			if (!settingsClass.getBoolValue("automaticIndexing")) {
				boolean shouldEnable = true;
				for (int i = 0; i < selectedItems.length; i++) {
					FrostUploadItem item = (FrostUploadItem) selectedItems[i];
					if (item.getSHA1() != null && item.getSHA1().length() > 0) {
						shouldEnable = false;
						break;
					}
				}
				if (selectedItems.length != 0) {
					// If at least 1 item is selected
					add(setPrefixForSelectedFilesItem);
					setPrefixForSelectedFilesItem.setEnabled(shouldEnable);
				}
				//	add(setPrefixForAllFilesItem);
				//	addSeparator();
				if (selectedItems.length != 0) {
					// If at least 1 item is selected
					add(restoreDefaultFilenamesForSelectedFilesItem);
					restoreDefaultFilenamesForSelectedFilesItem.setEnabled(shouldEnable);
				}
				//	add(restoreDefaultFilenamesForAllFilesItem);
				addSeparator();
				if (selectedItems.length != 0) {
					// If at least 1 item is selected
					// Add boards to changeDestinationBoard submenu
					Vector boards = tofTree.getAllBoards();
					Collections.sort(boards);
					changeDestinationBoardMenu.removeAll();
					for (int i = 0; i < boards.size(); i++) {
						final FrostBoardObject aBoard = (FrostBoardObject) boards.elementAt(i);
						JMenuItem boardMenuItem = new JMenuItem(aBoard.toString());
						changeDestinationBoardMenu.add(boardMenuItem);
						// add all boards to menu + set action listener for each board menu item
						boardMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								// set new board for all selected rows
								ModelItem[] selectedItems = modelTable.getSelectedItems();
								for (int x = 0; x < selectedItems.length; x++) {
									FrostUploadItem ulItem = (FrostUploadItem) selectedItems[x];
									//also check whether the item has not been hashed, i.e. added to
									//an index already - big mess to change it if that's the case
									ulItem.setTargetBoard(aBoard);
								}
							}
						});
					}
					add(changeDestinationBoardMenu);
					changeDestinationBoardMenu.setEnabled(shouldEnable);
				}
			} //end of options which are available if automatic indexing turned off
			addSeparator();
			add(cancelItem);

			super.show(invoker, x, y);
		}

	}

	/**
	 * 
	 */
	private class Listener
		extends MouseAdapter
		implements LanguageListener, KeyListener, ActionListener, MouseListener, PropertyChangeListener {

		/**
		 * 
		 */
		public Listener() {
			super();
		}

		/* (non-Javadoc)
		 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
		 */
		public void keyPressed(KeyEvent e) {
			if (e.getSource() == modelTable.getTable()) {
				uploadTable_keyPressed(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
		 */
		public void keyReleased(KeyEvent e) {
			// Nothing here
		}

		/* (non-Javadoc)
		 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
		 */
		public void keyTyped(KeyEvent e) {
			// Nothing here
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == uploadAddFilesButton) {
				uploadAddFilesButton_actionPerformed(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (e.getSource() == modelTable.getTable()) {
					// Start file from download table. Is this a good idea?
					uploadTableDoubleClick(e);
				}
			} else if (e.isPopupTrigger())
				if ((e.getSource() == modelTable.getTable())
					|| (e.getSource() == modelTable.getScrollPane())) {
					showUploadTablePopupMenu(e);
				}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {

				if ((e.getSource() == modelTable.getTable())
					|| (e.getSource() == modelTable.getScrollPane())) {
					showUploadTablePopupMenu(e);
				}

			}
		}
		
		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_NAME)) {
				fontChanged();
			}
			if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_SIZE)) {
				fontChanged();
			}
			if (evt.getPropertyName().equals(SettingsClass.FILE_LIST_FONT_STYLE)) {
				fontChanged();
			}
		}
		
	}

	private PopupMenuUpload popupMenuUpload = null;

	private Listener listener = new Listener();
	
	private static Logger logger = Logger.getLogger(UploadPanel.class.getName());

	private UploadModel model = null;

	private TofTree tofTree = null;
	private SettingsClass settingsClass = null;

	private UpdatingLanguageResource languageResource = null;

	private JPanel uploadTopPanel = new JPanel();
	private JButton uploadAddFilesButton =
		new JButton(new ImageIcon(getClass().getResource("/data/browse.gif")));
	private SortedModelTable modelTable;

	private boolean initialized = false;

	/**
	 * 
	 */
	public UploadPanel(SettingsClass newSettingsClass) {
		super();
		settingsClass = newSettingsClass;
	}

	/**
	 * 
	 */
	public void initialize() {
		if (!initialized) {
			refreshLanguage();

			// create the top panel
			configureButton(uploadAddFilesButton, "/data/browse_rollover.gif");
			uploadTopPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
			uploadTopPanel.add(uploadAddFilesButton);

			// create the main upload panel
			UploadTableFormat tableFormat = new UploadTableFormat(languageResource);
			
			modelTable = new SortedModelTable(model, tableFormat);
			setLayout(new BorderLayout());
			add(uploadTopPanel, BorderLayout.NORTH);
			add(modelTable.getScrollPane(), BorderLayout.CENTER);
			fontChanged();

			// listeners
			uploadAddFilesButton.addActionListener(listener);
			modelTable.getScrollPane().addMouseListener(listener);
			modelTable.getTable().addKeyListener(listener);
			modelTable.getTable().addMouseListener(listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);

			initialized = true;
		}
	}

	/**
	 * @param bundle
	 */
	public void setLanguageResource(UpdatingLanguageResource newLanguageResource) {
		if (languageResource != null) {
			languageResource.removeLanguageListener(listener);
		}
		languageResource = newLanguageResource;
		languageResource.addLanguageListener(listener);
	}

	public void setAddFilesButtonEnabled(boolean enabled) {
		uploadAddFilesButton.setEnabled(enabled);
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		uploadAddFilesButton.setToolTipText(languageResource.getString("Browse") + "...");
	}

	/**
	 * @return
	 */
	private PopupMenuUpload getPopupMenuUpload() {
		if (popupMenuUpload == null) {
			popupMenuUpload = new PopupMenuUpload();
			languageResource.addLanguageListener(popupMenuUpload);
		}
		return popupMenuUpload;
	}

	/**
	 * @param e
	 */
	private void uploadTable_keyPressed(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_DELETE && !modelTable.getTable().isEditing()) {
			ModelItem[] selectedItems = modelTable.getSelectedItems();
			model.removeItems(selectedItems);
			modelTable.getTable().clearSelection();
		}
	}

	//------------------------------------------------------------------------

	public void uploadAddFilesButton_actionPerformed(ActionEvent e) {
		FrostBoardObject board = tofTree.getSelectedNode();
		if (board.isFolder())
			return;

		final JFileChooser fc = new JFileChooser(settingsClass.getValue("lastUsedDirectory"));
		fc.setDialogTitle(
			languageResource.getString("Select files you want to upload to the")
				+ " "
				+ board.toString()
				+ " "
				+ languageResource.getString("board")
				+ ".");
		fc.setFileHidingEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		fc.setPreferredSize(new Dimension(600, 400));

		int returnVal = fc.showOpenDialog(this); //TODO: does this work?
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file != null) {
				settingsClass.setValue("lastUsedDirectory", file.getParent());
				File[] selectedFiles = fc.getSelectedFiles();

				for (int i = 0; i < selectedFiles.length; i++) {
					// collect all choosed files + files in all choosed directories
					ArrayList allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
					for (int j = 0; j < allFiles.size(); j++) {
						File newFile = (File) allFiles.get(j);
						if (newFile.isFile() && newFile.length() > 0) {
							FrostUploadItem ulItem =
								new FrostUploadItem(newFile, board);
							boolean isAdded = model.addUploadItem(ulItem);
						}
					}
				}
			}
		}
	}

	private void showUploadTablePopupMenu(MouseEvent e) {
		getPopupMenuUpload().show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * Configures a button to be a default icon button
	 * @param button The new icon button
	 * @param rolloverIcon Displayed when mouse is over button
	 */
	private void configureButton(JButton button, String rolloverIcon) {
		button.setRolloverIcon(new ImageIcon(frame1.class.getResource(rolloverIcon)));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
	}

	/**
	 * @param tree
	 */
	public void setTofTree(TofTree newTree) {
		tofTree = newTree;
	}

	/**
	 * @param e
	 */
	private void uploadTableDoubleClick(MouseEvent e) {
		ModelItem[] selectedItems = modelTable.getSelectedItems();
		if (selectedItems.length != 0) {
			FrostUploadItem ulItem = (FrostUploadItem) selectedItems[0];
			File file = new File(ulItem.getFilePath());
			logger.info("Executing: " + file.getPath());
			if (file.exists()) {
				Execute.run("exec.bat" + " \"" + file.getPath() + "\"");
			}
		}
	}
	
	/**
	 * 
	 */
	private void fontChanged() {
		String fontName = settingsClass.getValue(SettingsClass.FILE_LIST_FONT_NAME);
		int fontStyle = settingsClass.getIntValue(SettingsClass.FILE_LIST_FONT_STYLE);
		int fontSize = settingsClass.getIntValue(SettingsClass.FILE_LIST_FONT_SIZE);
		Font font = new Font(fontName, fontStyle, fontSize);
		if (!font.getFamily().equals(fontName)) {
			logger.severe("The selected font was not found in your system\n" +
						   "That selection will be changed to \"SansSerif\".");
			settingsClass.setValue(SettingsClass.FILE_LIST_FONT_NAME, "SansSerif");
			font = new Font("SansSerif", fontStyle, fontSize);
		}
		modelTable.setFont(font);
	}

	/**
	 * @param model
	 */
	public void setModel(UploadModel model) {
		this.model = model;
	}

}
