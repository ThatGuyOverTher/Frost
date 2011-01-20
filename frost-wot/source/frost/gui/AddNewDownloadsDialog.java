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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import frost.Core;
import frost.SettingsClass;
import frost.fileTransfer.FileTransferManager;
import frost.fileTransfer.FreenetPriority;
import frost.fileTransfer.download.FrostDownloadItem;
import frost.gui.model.SortedTableModel;
import frost.gui.model.TableMember;
import frost.storage.perst.TrackDownloadKeys;
import frost.storage.perst.TrackDownloadKeysStorage;
import frost.util.DateFun;
import frost.util.FormatterUtils;
import frost.util.gui.BooleanCell;
import frost.util.gui.JSkinnablePopupMenu;
import frost.util.gui.MiscToolkit;
import frost.util.gui.translation.Language;

@SuppressWarnings("serial")
public class AddNewDownloadsDialog extends javax.swing.JFrame {

	private final Language language;

	private final TrackDownloadKeysStorage trackDownloadKeysStorage;

	private AddNewDownloadsTableModel addNewDownloadsTableModel;
	private AddNewDownloadsTable addNewDownloadsTable;
	private JButton removeAlreadyDownloadedButton;
	private JButton removeAlreadyExistsButton;
	private JButton okButton;
	private JButton cancelButton;
	private JSkinnablePopupMenu tablePopupMenu;

	private final Frame parentFrame;


	public AddNewDownloadsDialog(final JFrame frame, final List<FrostDownloadItem> frostDownloadItemList) { 
		//super(frame);
		parentFrame = frame;
		
		language = Language.getInstance();
		trackDownloadKeysStorage = TrackDownloadKeysStorage.inst();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		initGUI();
	}

	private void initGUI() {
		try {
			setTitle(language.getString("AddNewDownloadsDialog.title"));
			
			int width = (int) (parentFrame.getWidth() * 0.75);
			int height = (int) (parentFrame.getHeight() * 0.75);

			if( width < 1000 ) {
				Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

				if( screenSize.width > 1300 ) {
					width = 1200;

				} else if( screenSize.width > 1000 ) {
					width = (int) (parentFrame.getWidth() * 0.99);
				}
			}

			if( height < 500 ) {
				Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

				if( screenSize.width > 900 ) {
					height = 800;
				} else {
					height = (int) (screenSize.width * 0.85);
				}
			}

			setSize(width, height);

			this.setResizable(true);
			
			setIconImage(MiscToolkit.loadImageIcon("/data/toolbar/document-save-as.png").getImage());
			
			
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
					FileTransferManager.inst().getDownloadManager().getModel().addDownloadItemList(getDownloads());
					
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
			
			addNewDownloadsTable.setAutoResizeMode( JTable.AUTO_RESIZE_NEXT_COLUMN);
			int tableWidth = getWidth();
			
			TableColumnModel headerColumnModel = addNewDownloadsTable.getTableHeader().getColumnModel();
			int defaultColumnWidthRatio[] = addNewDownloadsTableModel.getDefaultColumnWidthRatio();
			for(int i = 0 ; i < defaultColumnWidthRatio.length ; i++) {
				headerColumnModel.getColumn(i).setMinWidth(5);
				int ratio = tableWidth * defaultColumnWidthRatio[i] /100;
				headerColumnModel.getColumn(i).setPreferredWidth(ratio);
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	private List<FrostDownloadItem> getDownloads() {
		List<FrostDownloadItem> frostDownloadItmeList = new LinkedList<FrostDownloadItem>();
		final int numberOfRows = addNewDownloadsTableModel.getRowCount();
		for( int indexPos = 0; indexPos < numberOfRows; indexPos++) {
			frostDownloadItmeList.add( addNewDownloadsTableModel.getRow(indexPos).getDownloadItem() );
		}
		return frostDownloadItmeList;
	}

	public void startDialog(List<FrostDownloadItem> frostDownloadItmeList) {
		// load data into table
		this.loadNewDownloadsIntoTable(frostDownloadItmeList);
		setLocationRelativeTo(parentFrame);

		// display table
		setVisible(true); // blocking!
	}

	private void loadNewDownloadsIntoTable(final List<FrostDownloadItem> frostDownloadItmeList) {
		this.addNewDownloadsTableModel.clearDataModel();
		for( final FrostDownloadItem froDownloadItem : frostDownloadItmeList) {
			this.addNewDownloadsTableModel.addRow(new AddNewDownloadsTableMember(froDownloadItem));
		}
	}

	private void initPopupMenu() {

		// Rename file
		final JMenuItem renameFile = new JMenuItem(language.getString("AddNewDownloadsDialog.button.renameFile"));
		renameFile.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				addNewDownloadsTable.new SelectedItemsAction() {
					protected void action(AddNewDownloadsTableMember addNewDownloadsTableMember) {
						String newName = askForNewname(addNewDownloadsTableMember.getDownloadItem().getUnprefixedFilename());
						if( newName != null ) {
							addNewDownloadsTableMember.getDownloadItem().setFileName(newName);
						}
						addNewDownloadsTableMember.updateExistsCheck();
					}
				};
			}
		});
		
		// prefix Filename
		final JMenuItem prefixFilename = new JMenuItem(language.getString("AddNewDownloadsDialog.button.prefixFilename"));
		prefixFilename.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				final String prefix = JOptionPane.showInputDialog(
					AddNewDownloadsDialog.this,
					language.getString("AddNewDownloadsDialog.prefixFilenameDialog.dialogBody"),
					language.getString("AddNewDownloadsDialog.prefixFilenameDialog.dialogTitle"),
					JOptionPane.QUESTION_MESSAGE
				);

				addNewDownloadsTable.new SelectedItemsAction() {
					protected void action(AddNewDownloadsTableMember addNewDownloadsTableMember) {
						addNewDownloadsTableMember.getDownloadItem().setFilenamePrefix(prefix);
					}
				};
			}
		});

		// Change Download Directory
		final JMenuItem changeDownloadDir = new JMenuItem(language.getString("AddNewDownloadsDialog.button.changeDownloadDir"));
		changeDownloadDir.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				changeDownloadDir_actionPerformed(actionEvent);
			}
		});
		

		// Remove item item(s) list
		final JMenuItem removeDownload = new JMenuItem(language.getString("AddNewDownloadsDialog.button.removeDownload"));
		removeDownload.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				addNewDownloadsTable.removeSelected();
			}
		});

		// Remove but selected item(s) from list
		final JMenuItem removeButSelectedDownload = new JMenuItem(language.getString("AddNewDownloadsDialog.button.removeButSelectedDownload"));
		removeButSelectedDownload.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				addNewDownloadsTable.removeButSelected();
			}
		});

		// Change Priority
		final JMenu changePriorityMenu = new JMenu(language.getString("Common.priority.changePriority"));
		for(final FreenetPriority priority : FreenetPriority.values()) {
			JMenuItem priorityMenuItem = new JMenuItem(priority.getName());
			priorityMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(final ActionEvent actionEvent) {
					addNewDownloadsTable.new SelectedItemsAction() {
						protected void action(AddNewDownloadsTableMember addNewDownloadsTableMember) {
							addNewDownloadsTableMember.getDownloadItem().setPriority(priority);
						}
					};
				}
			});
			changePriorityMenu.add(priorityMenuItem);
		}
		
		// Enable download
		final JMenuItem enableDownloadMenuItem = new JMenuItem(language.getString("AddNewDownloadsDialog.popupMenu.enableDownload"));
		enableDownloadMenuItem.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				addNewDownloadsTable.new SelectedItemsAction() {
					protected void action(AddNewDownloadsTableMember addNewDownloadsTableMember) {
						addNewDownloadsTableMember.getDownloadItem().setEnabled(true);
					}
				};
			}
		});
		
		
		// Disable download
		final JMenuItem disableDownloadMenuItem = new JMenuItem(language.getString("AddNewDownloadsDialog.popupMenu.disableDownload"));
		disableDownloadMenuItem.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				addNewDownloadsTable.new SelectedItemsAction() {
					protected void action(AddNewDownloadsTableMember addNewDownloadsTableMember) {
						addNewDownloadsTableMember.getDownloadItem().setEnabled(false);
					}
				};
			}
		});
		
		// recent download directory
		final JMenu downloadDirRecentMenu = new JMenu(language.getString("DownloadPane.toolbar.downloadDirMenu.setDownloadDirTo"));
		JMenuItem item = new JMenuItem(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD));
		item.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				setDownloadDir(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD));
			}
		});
		downloadDirRecentMenu.add(item);
		final LinkedList<String> dirs = FileTransferManager.inst().getDownloadManager().getRecentDownloadDirs();
		if( dirs.size() > 0 ) {
			downloadDirRecentMenu.addSeparator();
			final ListIterator<String> iter = dirs.listIterator(dirs.size());
			while (iter.hasPrevious()) {
				final String dir = (String) iter.previous();
				
				item = new JMenuItem(dir);
				item.addActionListener( new java.awt.event.ActionListener() {
					public void actionPerformed(final ActionEvent actionEvent) {
						setDownloadDir(dir);
					}
				});
				downloadDirRecentMenu.add(item);
			}
		}

		// Compose popup menu
		tablePopupMenu = new JSkinnablePopupMenu();
		tablePopupMenu.add(renameFile);
		tablePopupMenu.add(prefixFilename);
		tablePopupMenu.addSeparator();
		tablePopupMenu.add(changeDownloadDir);
		tablePopupMenu.addSeparator();
		tablePopupMenu.add(removeDownload);
		tablePopupMenu.add(removeButSelectedDownload);
		tablePopupMenu.addSeparator();
		tablePopupMenu.add(changePriorityMenu);
		tablePopupMenu.addSeparator();
		tablePopupMenu.add(enableDownloadMenuItem);
		tablePopupMenu.add(disableDownloadMenuItem);
		tablePopupMenu.addSeparator();
		tablePopupMenu.add(downloadDirRecentMenu);
		
		this.addNewDownloadsTable.addMouseListener(new TablePopupMenuMouseListener());
	}

	private void removeAlreadyDownloadedButton_actionPerformed(final ActionEvent actionEvent) {
		final int numberOfRows = addNewDownloadsTableModel.getRowCount();
		addNewDownloadsTable.clearSelection();
		for( int indexPos = numberOfRows -1; indexPos >= 0; indexPos--) {
			final AddNewDownloadsTableMember addNewDownloadsTableMember =
				addNewDownloadsTableModel.getRow(indexPos);
			if( trackDownloadKeysStorage.searchItemKey( addNewDownloadsTableMember.getDownloadItem().getKey() ) ) {
				addNewDownloadsTableModel.deleteRow(addNewDownloadsTableMember);
			}
		}
	}

	private void removeAlreadyExistsButton_actionPerformed(final ActionEvent actionEvent) {
		final int numberOfRows = addNewDownloadsTableModel.getRowCount();
		addNewDownloadsTable.clearSelection();
		for( int indexPos = numberOfRows -1; indexPos >= 0; indexPos--) {
			final AddNewDownloadsTableMember addNewDownloadsTableMember =
				addNewDownloadsTableModel.getRow(indexPos);
			final FrostDownloadItem frostDownloadItem = addNewDownloadsTableMember.getDownloadItem();
			if(new java.io.File(frostDownloadItem.getDownloadDir() + frostDownloadItem.getFileName()).exists() ) {
				addNewDownloadsTableModel.deleteRow(addNewDownloadsTableMember);
			}
		}
	}
	
	private String askForNewname(final String oldName) {
		return (String) JOptionPane.showInputDialog(
				this,
				language.getString("AddNewDownloadsDialog.renameFileDialog.dialogBody"),
				language.getString("AddNewDownloadsDialog.renameFileDialog.dialogTitle"),
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				oldName
		);
	}
	private void changeDownloadDir_actionPerformed(final ActionEvent actionEvent) {
		// Open dialog to request dir
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
		
		// Set dir for selected items
		setDownloadDir(chooser.getSelectedFile().toString());
	}
	
	
	private void setDownloadDir(final String downloadDir) {
		addNewDownloadsTable.new SelectedItemsAction() {
			protected void action(AddNewDownloadsTableMember addNewDownloadsTableMember) {
				addNewDownloadsTableMember.getDownloadItem().setDownloadDir(downloadDir);
			}
		};
	}

	
	private static class AddNewDownloadsTableModel extends SortedTableModel<AddNewDownloadsTableMember>{
		private Language language = null;

		protected  static String columnNames[];

		protected final static Class<?> columnClasses[] = {
			String.class,
			String.class,
			String.class,
			String.class,
			Boolean.class,
			String.class,
			String.class,
		};
		
		private final int[] defaultColumnWidthRatio = {
				35,
				10,
				10,
				35,
				5,
				5,
			};


		public AddNewDownloadsTableModel() {
			super();
			language = Language.getInstance();
			refreshLanguage(); 
			assert columnClasses.length == columnNames.length;
		}

		private void refreshLanguage() {
			columnNames = new String[] {
				language.getString("AddNewDownloadsDialog.table.name"),
				language.getString("AddNewDownloadsDialog.table.key"),
				language.getString("Common.priority"),
				language.getString("AddNewDownloadsDialog.table.downloadDir"),
				language.getString("Common.enabled"),
				language.getString("AddNewDownloadsDialog.table.downloaded"),
				language.getString("AddNewDownloadsDialog.table.exists"),
			};
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			switch(col){
				case 0:
				case 1:
				case 2:
				case 3:
				case 5:
				case 6:
					return false;
				case 4:
					return true;
				default:
					return false;
			}
		}

		@Override
		public String getColumnName(int column) {
			if( column >= 0 && column < columnNames.length )
				return columnNames[column];
			return null;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			if( column >= 0 && column < columnClasses.length )
				return columnClasses[column];
			return null;
		}
		
		@Override
		public void setValueAt(Object aValue, int row, int column) {
			switch(column){
				case 0:
				case 1:
				case 2:
				case 3:
				case 5:
				case 6:
					return;
				case 4:
					getRow(row).getDownloadItem().setEnabled((Boolean) aValue);
					return;
				default:
					return;
			}
		}
		
		public int[] getDefaultColumnWidthRatio() {
			return defaultColumnWidthRatio;
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

		@Override
		public Comparable<?> getValueAt(final int column) {
			switch( column ) {
				case 0:
					return frostDownloadItem.getFileName();
				case 1:
					return frostDownloadItem.getKey();
				case 2:
					return frostDownloadItem.getPriority().getName();
				case 3:
					return frostDownloadItem.getDownloadDir();
				case 4:
					return frostDownloadItem.isEnabled();
				case 5:
					return downloaded ? "X" : "";
				case 6:
					return exists ? "X" : "";
				default :
					throw new RuntimeException("Unknown Column pos");
			}
		}

		public void updateExistsCheck() {
			File existingFile = new java.io.File(frostDownloadItem.getDownloadDir() + frostDownloadItem.getFileName());
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

		@Override
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
		@Override
		public void mouseReleased(final MouseEvent event) {
			maybeShowPopup(event);
		}
		@Override
		public void mousePressed(final MouseEvent event) {
			maybeShowPopup(event);
		}
		@Override
		public void mouseClicked(final MouseEvent event) {}
		@Override
		public void mouseEntered(final MouseEvent event) {}
		@Override
		public void mouseExited(final MouseEvent event) {}

		protected void maybeShowPopup(final MouseEvent e) {
			if( e.isPopupTrigger() ) {
				if( addNewDownloadsTable.getSelectedRowCount() > 0 ) {
					tablePopupMenu.show(addNewDownloadsTable, e.getX(), e.getY());
				}
			}
		}
	}
	
	private class AddNewDownloadsTable extends SortedTable<AddNewDownloadsTableMember> {

		private CenterCellRenderer centerCellRenderer;
		
		private final String[] columnTooltips = {
			null,
			null,
			null,
			null,
			null,
			language.getString("AddNewDownloadsDialog.tableToolltip.downloaded"),
			language.getString("AddNewDownloadsDialog.tableToolltip.exists"),
		};
		
		
		public AddNewDownloadsTable(final AddNewDownloadsTableModel addNewDownloadsTableModel) {
			super(addNewDownloadsTableModel);
			this.setIntercellSpacing(new Dimension(5, 1));
			centerCellRenderer = new CenterCellRenderer();
		}

		@Override
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
				case 4:
					return tableModel.getValueAt(rowIndex, realColumnIndex).toString();
				case 5:
					return addNewDownloadsTableModel.getRow(rowIndex).downloadedTooltip;
				case 6:
					return addNewDownloadsTableModel.getRow(rowIndex).existsTooltip;
				default:
					assert false;
			}
			return tableModel.getValueAt(rowIndex, realColumnIndex).toString();
		}
		
		@Override
		public TableCellRenderer getCellRenderer(final int rowIndex, final int columnIndex) {
			switch(columnIndex){
				case 0:
				case 1:
				case 2:
				case 3:
					return super.getCellRenderer(rowIndex, columnIndex);
				case 4:
					return BooleanCell.RENDERER;
				case 5:
				case 6:
					return centerCellRenderer;
				default:
					assert false;
			}
			return super.getCellRenderer(rowIndex, columnIndex);
		}
		
		@Override
		public TableCellEditor getCellEditor(final int rowIndex, final int columnIndex ) {
			switch(columnIndex){
				case 0:
				case 1:
				case 2:
				case 3:
				case 5:
				case 6:
					return super.getCellEditor(rowIndex, columnIndex);
				case 4:
					return BooleanCell.EDITOR;
				default:
					assert false;
			}
			return super.getCellEditor(rowIndex, columnIndex);
		}
		
		private class CenterCellRenderer extends JLabel implements TableCellRenderer {
			public Component getTableCellRendererComponent(final JTable table,
					final Object value, final boolean isSelected, final boolean hasFocus,
					final int row, final int column) {
				this.setText(value.toString());
				this.setHorizontalAlignment(SwingConstants.CENTER);
				return this;
			}
		}
		
		@Override
		protected JTableHeader createDefaultTableHeader() {
			return new JTableHeader(columnModel) {
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
