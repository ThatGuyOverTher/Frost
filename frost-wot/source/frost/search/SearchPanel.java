/*
 * Created on Oct 8, 2003
 */
package frost.search;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

import frost.*;
import frost.fileTransfer.download.DownloadTable;
import frost.fileTransfer.upload.UploadModel;
import frost.gui.TofTree;
import frost.gui.components.JSkinnablePopupMenu;
import frost.gui.objects.FrostBoardObject;
import frost.gui.translation.*;
import frost.identities.*;
import frost.threads.maintenance.Truster;

/**
 * 
 */
class SearchPanel extends JPanel implements SettingsUpdater {
	/**
	 * 
	 */
	private class PopupMenuSearch
		extends JSkinnablePopupMenu
		implements ActionListener, LanguageListener {
	
		JMenuItem cancelItem = new JMenuItem();
		JMenuItem downloadAllKeysItem = new JMenuItem();
		JMenuItem downloadSelectedKeysItem = new JMenuItem();
		JMenuItem setBadItem = new JMenuItem();
		JMenuItem setGoodItem = new JMenuItem();
	
		/**
		 * 
		 */
		public PopupMenuSearch() {
			super();
			initialize();
		}
	
		/**
		 * 
		 */
		private void initialize() {
			refreshLanguage();
	
			downloadSelectedKeysItem.addActionListener(this);
			downloadAllKeysItem.addActionListener(this);
			setGoodItem.addActionListener(this);
			setBadItem.addActionListener(this);
	
			/*		copyAttachmentItem.addActionListener(new ActionListener() {
						 public void actionPerformed(ActionEvent e) {
							 String srcData =
								 getSearchTable()
									 .getSelectedSearchItemsAsAttachmentsString();
							 Clipboard clipboard = getToolkit().getSystemClipboard();
							 StringSelection contents = new StringSelection(srcData);
							 clipboard.setContents(contents, frame1.this);
						 }
					 });
			*/
		}
	
		/**
		 * 
		 */
		private void refreshLanguage() {
			downloadSelectedKeysItem.setText(
				languageResource.getString("Download selected keys"));
			downloadAllKeysItem.setText(
				languageResource.getString("Download all keys"));
			setGoodItem.setText(
				languageResource.getString("help user (sets to GOOD)"));
			setBadItem.setText(
				languageResource.getString("block user (sets to BAD)"));
			cancelItem.setText(languageResource.getString("Cancel"));
		}
	
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == downloadSelectedKeysItem) {
				downloadSelectedKeys();
			}
			if (e.getSource() == downloadAllKeysItem) {
				downloadAllKeys();
			}
			if (e.getSource() == setGoodItem) {
				setGood();
			}
			if (e.getSource() == setBadItem) {
				setBad();
			}
		}
	
		/**
		 * 
		 */
		private void setBad() {
			java.util.List l = searchTable.getSelectedItemsOwners();
			Iterator i = l.iterator();
			while (i.hasNext()) {
				Identity owner_id = (Identity) i.next();
	
				Truster truster =
					new Truster(
						identities,
						new Boolean(false),
						owner_id.getUniqueName());
				truster.start();
			}
		}
	
		/**
		 * 
		 */
		private void setGood() {
			java.util.List l = searchTable.getSelectedItemsOwners();
			Iterator i = l.iterator();
			while (i.hasNext()) {
				Identity owner_id = (Identity) i.next();
	
				Truster truster =
					new Truster(
						identities,
						new Boolean(true),
						owner_id.getUniqueName());
				truster.start();
			}
		}
	
		/**
		 * 
		 */
		private void downloadAllKeys() {
			searchTable.selectAll();
			searchTable.addSelectedSearchItemsToDownloadTable(downloadTable);
		}
	
		/**
		 * 
		 */
		private void downloadSelectedKeys() {
			searchTable.addSelectedSearchItemsToDownloadTable(downloadTable);
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
	
			if (searchTable.getSelectedRow() > -1) {
				add(downloadSelectedKeysItem);
				addSeparator();
			}
			add(downloadAllKeysItem);
			addSeparator();
			if (searchTable.getSelectedRow() > -1) {
				//	add(copyAttachmentItem);
				//	addSeparator();
				add(setGoodItem);
				add(setBadItem);
				addSeparator();
			}
			add(cancelItem);
	
			super.show(invoker, x, y);
		}
	
	}
	/**
	 * @param e
	 */
	private void downloadButton_actionPerformed(ActionEvent e) {
		searchTable.addSelectedSearchItemsToDownloadTable(downloadTable);
	}

	/**
	 * 
	 */
	private class Listener
		extends MouseAdapter
		implements
			ActionListener,
			MouseListener,
			LanguageListener,
			PropertyChangeListener,
			ListSelectionListener {

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
			if (e.getSource() == searchTextField) {
				searchTextField_actionPerformed(e);
			}
			if (e.getSource() == downloadButton) {
				downloadButton_actionPerformed(e);
			}
			if (e.getSource() == searchButton) {
				searchButton_actionPerformed(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			if ((e.getClickCount() == 2) && (e.getSource() == searchTable)) {
				searchTableDoubleClick(e);
			}
			if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {

				if ((e.getSource() == searchTable) || (e.getSource() == searchTableScrollPane)) {
					showSearchTablePopupMenu(e);
				}
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {

				if ((e.getSource() == searchTable) || (e.getSource() == searchTableScrollPane)) {
					showSearchTablePopupMenu(e);
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

		/* (non-Javadoc)
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent e) {
			listSelectionChanged(e);			
		}

	}
	
	private SearchManager searchManager;

	private FrostIdentities identities;

	private static Logger logger = Logger.getLogger(SearchPanel.class.getName());
	
	private PopupMenuSearch popupMenuSearch = null;
	
	private Listener listener = new Listener();
	
	private SearchTable searchTable = null;
	private SearchTableModel searchTableModel = null;
	private DownloadTable downloadTable = null;
	private UploadModel uploadModel = null;
	private TofTree tofTree = null;
	private SettingsClass settingsClass = null;
	private String keypool = null;
	
	private UpdatingLanguageResource languageResource = null;

	private JPanel searchTopPanel = new JPanel();
	private JCheckBox searchAllBoardsCheckBox = new JCheckBox("", true);
	private JTranslatableComboBox searchComboBox = null;
	private JButton searchButton =
		new JButton(new ImageIcon(getClass().getResource("/data/search.gif")));
	private JButton downloadButton =
		new JButton(new ImageIcon(getClass().getResource("/data/save.gif")));
	private JLabel searchResultsCountLabel = new JLabel();
	private JTextField searchTextField = new JTextField(25);
	private JScrollPane searchTableScrollPane = null;

	private boolean initialized = false;

	private boolean allBoardsSelected = true;
	private long searchResultsCount = 0;

	/**
	 * 
	 */
	public SearchPanel(SettingsClass newSettingsClass, SearchManager newSearchManager) {
		super();
		settingsClass = newSettingsClass;
		searchManager = newSearchManager;
		settingsClass.addUpdater(this);
		setAllBoardsSelected(settingsClass.getBoolValue(SettingsClass.SEARCH_ALL_BOARDS));
	}

	/**
	 * 
	 */
	public void initialize() {
		if (!initialized) {
			refreshLanguage();

			// create the top panel
			searchAllBoardsCheckBox.setEnabled(true);

			String[] searchComboBoxKeys =
				{ "All files", "Audio", "Video", "Images", "Documents", "Executables", "Archives" };
			searchComboBox = new JTranslatableComboBox(languageResource, searchComboBoxKeys);

			configureButton(searchButton, "/data/search_rollover.gif");
			configureButton(downloadButton, "/data/save_rollover.gif");
			downloadButton.setEnabled(false);
			searchComboBox.setMaximumSize(searchComboBox.getPreferredSize());
			searchTextField.setMaximumSize(searchTextField.getPreferredSize());

			BoxLayout dummyLayout = new BoxLayout(searchTopPanel, BoxLayout.X_AXIS);
			Dimension blankSpace = new Dimension(8, 0);
			searchTopPanel.setLayout(dummyLayout);
			searchTopPanel.add(searchTextField);
			searchTopPanel.add(Box.createRigidArea(blankSpace));
			searchTopPanel.add(searchComboBox);
			searchTopPanel.add(Box.createRigidArea(blankSpace));
			searchTopPanel.add(searchButton);
			searchTopPanel.add(Box.createRigidArea(blankSpace));
			searchTopPanel.add(downloadButton);
			searchTopPanel.add(Box.createRigidArea(blankSpace));
			searchTopPanel.add(searchAllBoardsCheckBox);
			searchTopPanel.add(Box.createRigidArea(new Dimension(80, 0)));
			searchTopPanel.add(Box.createHorizontalGlue());
			searchTopPanel.add(searchResultsCountLabel);
			
			// create the main search panel
			searchTable = new SearchTable(searchTableModel, identities);
			searchTableScrollPane = new JScrollPane(searchTable);
			setLayout(new BorderLayout());
			add(searchTopPanel, BorderLayout.NORTH);
			add(searchTableScrollPane, BorderLayout.CENTER);
			fontChanged();
			
			// listeners
			searchTextField.addActionListener(listener);
			downloadButton.addActionListener(listener);
			searchButton.addActionListener(listener);
			searchTable.addMouseListener(listener);
			searchTableScrollPane.addMouseListener(listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_NAME, listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_SIZE, listener);
			settingsClass.addPropertyChangeListener(SettingsClass.FILE_LIST_FONT_STYLE, listener);
			searchTable.getSelectionModel().addListSelectionListener(listener);
			
			initialized = true;
		}
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		searchAllBoardsCheckBox.setText(languageResource.getString("all boards"));
		searchButton.setToolTipText(languageResource.getString("Search"));
		downloadButton.setToolTipText(languageResource.getString("Download selected keys"));
		
		String results = languageResource.getString("Results");
		Dimension labelSize = calculateLabelSize(results + " : 00000");
		searchResultsCountLabel.setPreferredSize(labelSize);
		searchResultsCountLabel.setMinimumSize(labelSize);
		searchResultsCountLabel.setText(results + " : " + searchResultsCount);
	}
	
	private Dimension calculateLabelSize(String text) {
		JLabel dummyLabel = new JLabel(text);
		dummyLabel.doLayout();
		return dummyLabel.getPreferredSize();
	}

	/**
	 * Configures a button to be a default icon button
	 * @param button The new icon button
	 * @param rolloverIcon Displayed when mouse is over button
	 */
	private void configureButton(JButton button, String rolloverIcon) {
		button.setRolloverIcon(new ImageIcon(getClass().getResource(rolloverIcon)));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
	}
	
	/**searchButton Action Listener (Search)*/
	private void searchButton_actionPerformed(ActionEvent e) {
		searchButton.setEnabled(false);
		searchTable.clearSelection();
		((SearchTableModel) searchTable.getModel()).clearDataModel();
		Vector boardsToSearch;
		if (searchAllBoardsCheckBox.isSelected()) {
			// search in all boards
			boardsToSearch = tofTree.getAllBoards();
		} else {
			if (tofTree.getSelectedNode().isFolder() == false) {
				// search in selected board
				boardsToSearch = new Vector();
				boardsToSearch.add(tofTree.getSelectedNode());
			} else {
				// search in all boards below the selected folder
				Enumeration enu = tofTree.getSelectedNode().depthFirstEnumeration();
				boardsToSearch = new Vector();
				while (enu.hasMoreElements()) {
					FrostBoardObject b = (FrostBoardObject) enu.nextElement();
					if (b.isFolder() == false) {
						boardsToSearch.add(b);
					}
				}
			}
		}

		SearchThread searchThread =
			new SearchThread(
				searchTextField.getText(),
				boardsToSearch,
				searchComboBox.getSelectedKey(),
				searchManager);
		searchThread.setLanguageResource(languageResource);
		searchThread.setUploadModel(uploadModel);
		searchThread.start();
	}
	
	/**searchTextField Action Listener (search)*/
	private void searchTextField_actionPerformed(ActionEvent e) {
		if (searchButton.isEnabled()) {
			searchButton_actionPerformed(e);
		}
	}
	
	/**
	 * @param e
	 */
	private void showSearchTablePopupMenu(MouseEvent e) {
		getPopupMenuSearch().show(e.getComponent(), e.getX(), e.getY());
	}
	
	/**
	 * Updates the search result count.
	 * Called by search thread.
	 */
	public void updateSearchResultCountLabel() {
		DefaultTableModel model = (DefaultTableModel) searchTable.getModel();
		searchResultsCount = model.getRowCount();
		String results = languageResource.getString("Results");
		if (searchResultsCount == 0) {
			searchResultsCountLabel.setText(results + " : 0");
		}
		searchResultsCountLabel.setText(results + " : " + searchResultsCount);
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
		searchTable.setFont(font);
	}



	
	/**
	 * @param e 
	 */
	private void searchTableDoubleClick(MouseEvent e) {
		searchTable.addSelectedSearchItemsToDownloadTable(downloadTable);
	}



	
	/**
	 * @param b
	 */
	public void setAllBoardsSelected(boolean b) {
		allBoardsSelected = b;
		searchAllBoardsCheckBox.setSelected(allBoardsSelected);
	}
	
	/**
	 * @param b
	 */
	public void setSearchEnabled(boolean b) {
		searchButton.setEnabled(b);
	}
	
	/**
	 * description
	 * 
	 * @param downloadTable description
	 */
	public void setDownloadTable(DownloadTable newDownloadTable) {
		downloadTable = newDownloadTable;
	}
	
	/**
	 * description
	 * 
	 * @param newSearchTable description
	 */
	public void setTableModel(SearchTableModel newSearchTableModel) {
		searchTableModel = newSearchTableModel;
	}
	
	/**
	 * @param newTree
	 */
	public void setTofTree(TofTree newTree) {
		tofTree = newTree;
	}
	
	/**
	 * @param newKeypool
	 */
	public void setKeypool(String newKeypool) {
		keypool = newKeypool;
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
	
	/**
	 * @return
	 */
	private PopupMenuSearch getPopupMenuSearch() {
		if (popupMenuSearch == null) {
			popupMenuSearch = new PopupMenuSearch();
			languageResource.addLanguageListener(popupMenuSearch);
		}
		return popupMenuSearch;
	}

	/**
 	 * @param identities
 	 */
	public void setIdentities(FrostIdentities newIdentities) {
		identities = newIdentities;
	}

	/* (non-Javadoc)
	 * @see frost.SettingsUpdater#updateSettings()
	 */
	public void updateSettings() {
		settingsClass.setValue(SettingsClass.SEARCH_ALL_BOARDS, allBoardsSelected);	
	}
	
	/**
 	 * @param e
 	 */
	private void listSelectionChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			boolean disabled = searchTable.getSelectionModel().isSelectionEmpty();
			downloadButton.setEnabled(!disabled);
		}
	}

	/**
	 * @param model
	 */
	public void setUploadModel(UploadModel model) {
		uploadModel = model;
	}

}
