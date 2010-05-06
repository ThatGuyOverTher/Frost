/*
  AddNewDownloadsDialog.java / Frost
  Copyright (C) 2010  Frost Project <jtcfrost.sourceforge.net>

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
package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import frost.*;
import frost.fileTransfer.download.*;
import frost.gui.model.*;
import frost.storage.perst.*;
import frost.util.DateFun;
import frost.util.FormatterUtils;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class AddNewDownloadsDialog extends javax.swing.JDialog {

	private final Language language;

	private final TrackDownloadKeysStorage trackDownloadKeysStorage;

	private AddNewDownloadsTableModel addNewDownloadsTableModel;
	private AddNewDownloadsTable addNewDownloadsTable;
	private JButton removeAlreadyDownloadedButton;
	private JButton removeAlreadyExistsButton;
	private JButton okButton;
	private JButton cancelButton;
	private JSkinnablePopupMenu tablePopupMenu;

	private boolean addDownloads;

	private final Frame parentFrame;

	private static final long serialVersionUID = 1L;

	public AddNewDownloadsDialog(final JFrame frame, final List<FrostDownloadItem> frostDownloadItemList) { 
		super(frame);
		parentFrame = frame;
		setModal(true);
		addDownloads = false;
		language = Language.getInstance();
		trackDownloadKeysStorage = TrackDownloadKeysStorage.inst();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		initGUI();
	}

	private void initGUI() {
		try {
			setTitle(language.getString("AddNewDownloadsDialog.title"));
			setSize(800, 500);
			this.setResizable(true);

			// Remove already Downloaded Button
			removeAlreadyDownloadedButton = new JButton(language.getString("AddNewDownloadsDialog.button.removeAlreadyDownloadedButton"));
			removeAlreadyDownloadedButton.setToolTipText(language.getString("AddNewDownloadsDialog.buttonTooltip.removeAlreadyDownloadedButton"));
			removeAlreadyDownloadedButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					removeAlreadyDownloadedButton_actionPerformed(e);
				}
			});

			// Remove already exists Button
			removeAlreadyExistsButton = new JButton(language.getString("AddNewDownloadsDialog.button.removeAlreadyExistsButton"));
			removeAlreadyExistsButton.setToolTipText(language.getString("AddNewDownloadsDialog.buttonTooltip.removeAlreadyExistsButton"));
			removeAlreadyExistsButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					removeAlreadyExistsButton_actionPerformed(e);
				}
			});

			// OK Button
			okButton = new JButton(language.getString("Common.ok"));
			okButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					addDownloads = true;
					dispose();
				}
			});

			// Cancel Button
			cancelButton = new JButton(language.getString("Common.cancel"));
			cancelButton.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					addNewDownloadsTableModel.clearDataModel();
					dispose();
				}
			});

			// Button row
			final JPanel buttonsPanel = new JPanel(new BorderLayout());
			buttonsPanel.setLayout( new BoxLayout( buttonsPanel, BoxLayout.X_AXIS ));

			buttonsPanel.add( removeAlreadyDownloadedButton );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));
			buttonsPanel.add( removeAlreadyExistsButton );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));

			buttonsPanel.add( Box.createHorizontalGlue() );

			buttonsPanel.add( cancelButton );
			buttonsPanel.add(Box.createRigidArea(new Dimension(10,3)));
			buttonsPanel.add( okButton );
			buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

			// Download Table
			addNewDownloadsTableModel = new AddNewDownloadsTableModel();
			addNewDownloadsTable = new AddNewDownloadsTable( addNewDownloadsTableModel );
			addNewDownloadsTable.setRowSelectionAllowed(true);
			addNewDownloadsTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			addNewDownloadsTable.setRowHeight(18);
			final JScrollPane scrollPane = new JScrollPane(addNewDownloadsTable);
			scrollPane.setWheelScrollingEnabled(true);

			// main panel
			final JPanel mainPanel = new JPanel(new BorderLayout());
			mainPanel.add( scrollPane, BorderLayout.CENTER );
			mainPanel.add( buttonsPanel, BorderLayout.SOUTH );
			mainPanel.setBorder(BorderFactory.createEmptyBorder(5,7,7,7));

			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add(mainPanel, null);

			this.initPopupMenu();

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public List<FrostDownloadItem> startDialog(List<FrostDownloadItem> frostDownloadItmeList) {
		// load data into table
		this.loadNewDownloadsIntoTable(frostDownloadItmeList);
		setLocationRelativeTo(parentFrame);

		// display table
		setVisible(true); // blocking!

		// return items in table
		frostDownloadItmeList = new LinkedList<FrostDownloadItem>();
		if( addDownloads ) {
			final int numberOfRows = addNewDownloadsTableModel.getRowCount();
			for( int indexPos = 0; indexPos < numberOfRows; indexPos++) {
				final AddNewDownloadsTableMember row = (AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(indexPos);
				frostDownloadItmeList.add( row.getDownloadItem() );
			}
		}

		return frostDownloadItmeList;
	}

	private void loadNewDownloadsIntoTable(final List<FrostDownloadItem> frostDownloadItmeList) {
		this.addNewDownloadsTableModel.clearDataModel();
		for( final FrostDownloadItem froDownloadItem : frostDownloadItmeList) {
			final AddNewDownloadsTableMember addNewDownloadsTableMember = new AddNewDownloadsTableMember(froDownloadItem);
			this.addNewDownloadsTableModel.addRow(addNewDownloadsTableMember);
		}
	}

	private void initPopupMenu() {
		tablePopupMenu = new JSkinnablePopupMenu();

		// Change Download Directory
		final JMenuItem changeDownloadDir = new JMenuItem(language.getString("AddNewDownloadsDialog.button.changeDownloadDir"));
		changeDownloadDir.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				changeDownloadDir_actionPerformed(actionEvent);
			}
		});

		// Remove item from list
		final JMenuItem removeDownload = new JMenuItem(language.getString("AddNewDownloadsDialog.button.removeDownload"));
		removeDownload.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				removeDownload_actionPerformed(actionEvent);
			}
		});

		// Change Priority
		final JMenu changePriorityMenu = new JMenu(language.getString("Common.priority.changePriority"));
		final int numberOfPriorities = 7;
		final JMenuItem[] prioItemList = new JMenuItem[numberOfPriorities];

		for(int i = 0; i < numberOfPriorities; i++) {
			final int priority = i;
			prioItemList[priority] = new JMenuItem(language.getString("Common.priority.priority" + priority));
			changePriorityMenu.add(prioItemList[priority]);
			prioItemList[priority].addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent actionEvent) {
					changePriority_actionPerformed(actionEvent, priority);
				}
			});
		}

		// Compose popup menu
		tablePopupMenu.add(changeDownloadDir);
		tablePopupMenu.addSeparator();
		tablePopupMenu.add(removeDownload);
		tablePopupMenu.addSeparator();
		tablePopupMenu.add(changePriorityMenu);

		this.addNewDownloadsTable.addMouseListener(new TablePopupMenuMouseListener());
	}

	private void removeAlreadyDownloadedButton_actionPerformed(final ActionEvent actionEvent) {
		final int numberOfRows = addNewDownloadsTableModel.getRowCount();
		for( int indexPos = numberOfRows -1; indexPos >= 0; indexPos--) {
			final AddNewDownloadsTableMember addNewDownloadsTableMember =
				(AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(indexPos);
			if( trackDownloadKeysStorage.searchItemKey( addNewDownloadsTableMember.getDownloadItem().getKey() ) ) {
				addNewDownloadsTableModel.deleteRow(addNewDownloadsTableMember);
			}
		}
		addNewDownloadsTable.clearSelection();
	}

	private void removeAlreadyExistsButton_actionPerformed(final ActionEvent actionEvent) {
		final int numberOfRows = addNewDownloadsTableModel.getRowCount();
		for( int indexPos = numberOfRows -1; indexPos >= 0; indexPos--) {
			final AddNewDownloadsTableMember addNewDownloadsTableMember =
				(AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(indexPos);
			final FrostDownloadItem frostDownloadItem = addNewDownloadsTableMember.getDownloadItem();
			if(new java.io.File(frostDownloadItem.getDownloadDir() + frostDownloadItem.getFilename()).exists() ) {
				addNewDownloadsTableModel.deleteRow(addNewDownloadsTableMember);
			}
		}
		addNewDownloadsTable.clearSelection();
	}

	private void changeDownloadDir_actionPerformed(final ActionEvent actionEvent) {
		final int[] selectedRows = addNewDownloadsTable.getSelectedRows();

		if( selectedRows.length > 0 ) {
			// Open choose Directory dialog
			final JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(
					new java.io.File(
							Core.frostSettings.getDefaultValue(SettingsClass.DIR_DOWNLOAD)
					)
			);
			chooser.setDialogTitle(language.getString("AddNewDownloadsDialog.changeDirDialog.title"));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
				return;
			}

			// Set new dir for selected items
			for( final int rowIndex : selectedRows ) {
				if( rowIndex >= addNewDownloadsTable.getRowCount() ) {
					continue; // paranoia
				}

				// add the board(s) to board tree and remove it from table
				final AddNewDownloadsTableMember row = (AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(rowIndex);
				row.getDownloadItem().setDownloadDir(chooser.getSelectedFile().toString());
				row.updateExistsCheck();
			}
			addNewDownloadsTable.clearSelection();
		}
	}

	private void removeDownload_actionPerformed(final ActionEvent e) {
		final int[] selectedRows = addNewDownloadsTable.getSelectedRows();

		if( selectedRows.length > 0 ) {
			for( int z = selectedRows.length - 1; z > -1; z-- ) {
				final int rowIx = selectedRows[z];

				if( rowIx >= addNewDownloadsTableModel.getRowCount() ) {
					continue; // paranoia
				}

				final AddNewDownloadsTableMember row = (AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(rowIx);
				addNewDownloadsTableModel.deleteRow(row);
			}
			addNewDownloadsTable.clearSelection();
		}
	}

	private void changePriority_actionPerformed(final ActionEvent e, final int priority) {
		final int[] selectedRows = addNewDownloadsTable.getSelectedRows();

		if( selectedRows.length > 0 ) {
			for( int z = selectedRows.length - 1; z > -1; z-- ) {
				final int rowIx = selectedRows[z];

				if( rowIx >= addNewDownloadsTableModel.getRowCount() ) {
					continue; // paranoia
				}

				final AddNewDownloadsTableMember row = (AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(rowIx);
				row.getDownloadItem().setPriority(priority);
			}
			addNewDownloadsTable.clearSelection();
		}
	}
	
	
	private static class AddNewDownloadsTableModel extends SortedTableModel{
		private static final long serialVersionUID = 1L;

		private Language language = null;

		protected final static String columnNames[] = new String[6];

		protected final static Class<?> columnClasses[] =  {
			String.class,
			String.class,
			String.class,
			String.class,
			String.class,
			String.class
		};

		public AddNewDownloadsTableModel() {
			super();
			assert columnClasses.length == columnNames.length;
			language = Language.getInstance();
			refreshLanguage();
		}

		private void refreshLanguage() {
			columnNames[0] = language.getString("AddNewDownloadsDialog.table.name");
			columnNames[1] = language.getString("AddNewDownloadsDialog.table.key");
			columnNames[2] = language.getString("AddNewDownloadsDialog.table.priority");
			columnNames[3] = language.getString("AddNewDownloadsDialog.table.downloadDir");
			columnNames[4] = language.getString("AddNewDownloadsDialog.table.downloaded");
			columnNames[5] = language.getString("AddNewDownloadsDialog.table.exists");
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public String getColumnName(int column) {
			if( column >= 0 && column < columnNames.length )
				return columnNames[column];
			return null;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public Class<?> getColumnClass(int column) {
			if( column >= 0 && column < columnClasses.length )
				return columnClasses[column];
			return null;
		}
	}

	private class AddNewDownloadsTableMember implements TableMember {

		private FrostDownloadItem frostDownloadItem;
		private boolean downloaded;
		private String downloadedTooltip;
		private boolean exists;
		private String existsTooltip;

		public AddNewDownloadsTableMember(final FrostDownloadItem frostDownloadItem){
			this.frostDownloadItem = frostDownloadItem;
			TrackDownloadKeys trackDownloadKeys = trackDownloadKeysStorage.getItemByKey(frostDownloadItem.getKey());
			downloaded = trackDownloadKeys != null;
			if( downloaded ) {
				final long date = trackDownloadKeys.getDownloadFinishedTime();
				downloadedTooltip = new StringBuilder("<html>")
						.append(language.getString("ManageDownloadTrackingDialog.table.finished"))
						.append(": ")
						.append(DateFun.FORMAT_DATE_VISIBLE.print(date))
						.append(" ")
						.append(DateFun.FORMAT_TIME_VISIBLE.print(date))
						.append("<br />\n")
						.append(language.getString("ManageDownloadTrackingDialog.table.board"))
						.append(": ")
						.append(trackDownloadKeys.getBoardName())
						.append("<br />\n")
						.append(language.getString("ManageDownloadTrackingDialog.table.size"))
						.append(": ")
						.append(FormatterUtils.formatSize(trackDownloadKeys.getFileSize()))
						.append("</html>")
						.toString();
			} else {
				downloadedTooltip = "";
			}
			updateExistsCheck();
		}

		public Object getValueAt(final int column) {
			switch( column ) {
				case 0:
					return frostDownloadItem.getFilename();
				case 1:
					return frostDownloadItem.getKey();
				case 2:
					if( frostDownloadItem.getPriority() == -1 ) {
						frostDownloadItem.setPriority( Core.frostSettings.getIntValue(SettingsClass.FCP2_DEFAULT_PRIO_FILE_DOWNLOAD ));
					}
					final String prio = "Common.priority.priority" + frostDownloadItem.getPriority();
					return language.getString(prio);
				case 3:
					return frostDownloadItem.getDownloadDir();
				case 4:
					return downloaded ? "X" : "";
				case 5:
					return exists ? "X" : "";
				default :
					throw new RuntimeException("Unknown Column pos");
			}
		}

		public void updateExistsCheck() {
			File existingFile = new java.io.File(frostDownloadItem.getDownloadDir() + frostDownloadItem.getFilename());
			exists = existingFile.exists();
			if( exists) {
				final long date = existingFile.lastModified();
				existsTooltip = new StringBuilder("<html>")
				.append(language.getString("AddNewDownloadsDialog.table.lastModifiedTooltip"))
				.append(": ")
				.append(DateFun.FORMAT_DATE_VISIBLE.print(date))
				.append(" ")
				.append(DateFun.FORMAT_TIME_VISIBLE.print(date))
				.append("<br />\n")
				.append(language.getString("AddNewDownloadsDialog.table.fileSizeTooltip"))
				.append(": ")
				.append(FormatterUtils.formatSize(existingFile.length()))
				.append("</html>")
				.toString();
			} else {
				existsTooltip = "";
			}
		}

		public int compareTo(final TableMember anOther, final int tableColumIndex) {
			final String c1 = (String) getValueAt(tableColumIndex);
			final String c2 = (String) anOther.getValueAt(tableColumIndex);
			return c1.compareToIgnoreCase(c2);
		}

		public FrostDownloadItem getDownloadItem(){
			return frostDownloadItem;
		}
	}

	private class TablePopupMenuMouseListener implements MouseListener {
		public void mouseReleased(final MouseEvent event) {
			maybeShowPopup(event);
		}
		public void mousePressed(final MouseEvent event) {
			maybeShowPopup(event);
		}
		public void mouseClicked(final MouseEvent event) {}
		public void mouseEntered(final MouseEvent event) {}
		public void mouseExited(final MouseEvent event) {}

		protected void maybeShowPopup(final MouseEvent e) {
			if( e.isPopupTrigger() ) {
				if( addNewDownloadsTable.getSelectedRowCount() > 0 ) {
					tablePopupMenu.show(addNewDownloadsTable, e.getX(), e.getY());
				}
			}
		}
	}
	
	private class AddNewDownloadsTable extends SortedTable {

		private static final long serialVersionUID = 1L;
		private CenterCellRenderer centerCellRenderer;
		
		private final String[] columnTooltips = {
			null,
			null,
			null,
			null,
			language.getString("AddNewDownloadsDialog.tableToolltip.downloaded"),
			language.getString("AddNewDownloadsDialog.tableToolltip.exists")
		};

		public AddNewDownloadsTable(final AddNewDownloadsTableModel addNewDownloadsTableModel) {
			super(addNewDownloadsTableModel);
			this.setIntercellSpacing(new Dimension(5, 1));
			centerCellRenderer = new CenterCellRenderer();
		}

		public String getToolTipText(final MouseEvent mouseEvent) {
			final java.awt.Point point = mouseEvent.getPoint();
			final int rowIndex = rowAtPoint(point);
			final int colIndex = columnAtPoint(point);
			final int realColumnIndex = convertColumnIndexToModel(colIndex);
			final AddNewDownloadsTableModel tableModel = (AddNewDownloadsTableModel) getModel();
			switch(realColumnIndex){
				case 0:
				case 1:
				case 2:
				case 3:
					return tableModel.getValueAt(rowIndex, realColumnIndex).toString();
				case 4:
					return ((AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(rowIndex)).downloadedTooltip;
				case 5:
					return ((AddNewDownloadsTableMember) addNewDownloadsTableModel.getRow(rowIndex)).existsTooltip;
				default:
					assert false;
			}
			return tableModel.getValueAt(rowIndex, realColumnIndex).toString();
		}
		
		public TableCellRenderer getCellRenderer(final int rowIndex, final int columnIndex) {
			switch(columnIndex){
				case 0:
				case 1:
				case 2:
				case 3:
					return super.getCellRenderer(rowIndex, columnIndex);
				case 4:
				case 5:
					return centerCellRenderer;
				default:
					assert false;
			}
			return super.getCellRenderer(rowIndex, columnIndex);
		}

		private class CenterCellRenderer extends JLabel implements TableCellRenderer {
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent(final JTable table,
					final Object value, final boolean isSelected, final boolean hasFocus,
					final int row, final int column) {
				this.setText(value.toString());
				this.setHorizontalAlignment(SwingConstants.CENTER);
				return this;
			}
		}
		
		protected JTableHeader createDefaultTableHeader() {
			return new JTableHeader(columnModel) {
				private static final long serialVersionUID = 1L;
				public String getToolTipText(final MouseEvent e) {
					final java.awt.Point p = e.getPoint();
					final int index = columnModel.getColumnIndexAtX(p.x);
					final int realIndex = columnModel.getColumn(index).getModelIndex();
					return columnTooltips[realIndex];
				}
			};
		}
	}
}
