/*
 * Created on Oct 8, 2003
 */
package frost;

import java.awt.*;
import java.awt.Insets;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import frost.gui.*;
import frost.gui.SearchTable;
import frost.gui.model.SearchTableModel;
import frost.gui.objects.FrostBoardObject;
import frost.gui.translation.*;
import frost.gui.translation.UpdatingLanguageResource;
import frost.identities.Identity;
import frost.threads.SearchThread;
import frost.threads.maintenance.Truster;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
 * 
 */
public class SearchPanel extends JPanel {
	/**
	 * 
	 */
	private class PopupMenuSearch
		extends JPopupMenu
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
						Core.getInstance(),
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
						Core.getInstance(),
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
	private void searchDownloadButton_actionPerformed(ActionEvent e) {
		searchTable.addSelectedSearchItemsToDownloadTable(downloadTable);
	}

	/**
	 * 
	 */
	private class Listener implements ActionListener, MouseListener, LanguageListener {

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
			if (e.getSource() == searchDownloadButton) {
				searchDownloadButton_actionPerformed(e);
			}
			if (e.getSource() == searchButton) {
				searchButton_actionPerformed(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			// Nothing here		
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
			// Nothing here					
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			// Nothing here					
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			if ((e.getClickCount() == 2) && (e.getSource() == searchTable)) {
				searchTableDoubleClick(e);
			}
			if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {

				if ((e.getSource() == searchTable)
					|| (e.getSource() == searchTableScrollPane)) {
					showSearchTablePopupMenu(e);
				}

			}	
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			if ((e.getClickCount() == 1) && (e.isPopupTrigger())) {

				if ((e.getSource() == searchTable)
					|| (e.getSource() == searchTableScrollPane)) {
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

	}
	
	private PopupMenuSearch popupMenuSearch = null;
	
	private Listener listener = new Listener();
	
	private SearchTable searchTable = null;
	private DownloadTable downloadTable = null;
	private TofTree tofTree = null;
	private String keypool = null;
	
	private UpdatingLanguageResource languageResource = null;

	private JPanel searchTopPanel = new JPanel();
	private JCheckBox searchAllBoardsCheckBox = new JCheckBox("", true);
	private JComboBox searchComboBox = new JComboBox();
	private JButton searchButton =
		new JButton(new ImageIcon(getClass().getResource("/data/search.gif")));
	private JButton searchDownloadButton =
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
	public SearchPanel() {
		super();
	}

	/**
	 * 
	 */
	public void initialize() {
		if (!initialized) {
			refreshLanguage();

			// create the top panel
			searchAllBoardsCheckBox.setEnabled(true);

			// TODO: Temporary. Until I make a TranslatableComboBox
			String[] searchComboBoxItems =
				{ "All files", "Audio", "Video", "Images", "Documents", "Executables", "Archives" };
			searchComboBox = new JComboBox(searchComboBoxItems);

			configureButton(searchButton, "/data/search_rollover.gif");
			configureButton(searchDownloadButton, "/data/save_rollover.gif");
			searchComboBox.setMaximumSize(searchComboBox.getPreferredSize());
			searchTextField.setMaximumSize(searchTextField.getPreferredSize());

			BoxLayout dummyLayout = new BoxLayout(searchTopPanel, BoxLayout.X_AXIS);
			searchTopPanel.setLayout(dummyLayout);
			searchTopPanel.add(searchTextField);
			searchTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
			searchTopPanel.add(searchComboBox);
			searchTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
			searchTopPanel.add(searchButton);
			searchTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
			searchTopPanel.add(searchDownloadButton);
			searchTopPanel.add(Box.createRigidArea(new Dimension(8, 0)));
			searchTopPanel.add(searchAllBoardsCheckBox);
			searchTopPanel.add(Box.createRigidArea(new Dimension(80, 0)));
			searchTopPanel.add(Box.createHorizontalGlue());
			searchTopPanel.add(searchResultsCountLabel);
			
			// create the main search panel
			searchTableScrollPane = new JScrollPane(searchTable);
			setLayout(new BorderLayout());
			add(searchTopPanel, BorderLayout.NORTH);
			add(searchTableScrollPane, BorderLayout.CENTER);
			
			// listeners
			searchTextField.addActionListener(listener);
			searchDownloadButton.addActionListener(listener);
			searchButton.addActionListener(listener);
			searchTableScrollPane.addMouseListener(listener);
			
			initialized = true;
		}
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		searchAllBoardsCheckBox.setText(languageResource.getString("all boards"));
		searchButton.setToolTipText(languageResource.getString("Search"));
		searchDownloadButton.setToolTipText(languageResource.getString("Download selected keys"));
		
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
			if (getSelectedNode().isFolder() == false) {
				// search in selected board
				boardsToSearch = new Vector();
				boardsToSearch.add(getSelectedNode());
			} else {
				// search in all boards below the selected folder
				Enumeration enu = getSelectedNode().depthFirstEnumeration();
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
				keypool,
				(String) searchComboBox.getSelectedItem(),
				this);
		searchThread.start();
	}
	
	private FrostBoardObject getSelectedNode() { //TODO: move this method to TofTree
		FrostBoardObject node = (FrostBoardObject) tofTree.getLastSelectedPathComponent();
		if (node == null) {
			// nothing selected? unbelievable ! so select the root ...
			tofTree.setSelectionRow(0);
			node = (FrostBoardObject) tofTree.getModel().getRoot();
		}
		return node;
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
	public void setSearchTable(SearchTable newSearchTable) {
		if (searchTable != null) {
			searchTable.removeMouseListener(listener);
		}
		searchTable = newSearchTable;
		searchTable.addMouseListener(listener);
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
	public boolean isAllBoardsSelected() {
		return allBoardsSelected;
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




}
