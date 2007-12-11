/*
  KnownBoardsFrame.java / Frost
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
package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;

import frost.*;
import frost.boards.*;
import frost.fileTransfer.common.*;
import frost.gui.model.*;
import frost.storage.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class KnownBoardsFrame extends JDialog {

    private static final Logger logger = Logger.getLogger(KnownBoardsFrame.class.getName());

    private final Language language;

    private final TofTree tofTree;

    private final HashSet<String> hiddenNames;

    private static final ImageIcon boardIcon = new ImageIcon(KnownBoardsFrame.class.getResource("/data/board.gif"));
    private static final ImageIcon writeAccessIcon = new ImageIcon(KnownBoardsFrame.class.getResource("/data/waboard.jpg"));
    private static final ImageIcon readAccessIcon = new ImageIcon(KnownBoardsFrame.class.getResource("/data/raboard.jpg"));

    private JButton Bclose;
    private JButton BboardActions;
    private JButton Bimport;
    private JButton Bexport;
    private JCheckBox CBshowHidden;
    private JTextField TFlookupBoard;
    private JTextField TFfilterBoard;
    private SortedTable boardsTable;
    private KnownBoardsTableModel tableModel;
    private NameColumnRenderer nameColRenderer;
    private DescColumnRenderer descColRenderer;
    private ShowContentTooltipRenderer showContentTooltipRenderer;

    private JSkinnablePopupMenu tablePopupMenu;

    // a list of all boards, needed as data source when we filter in the table
    private List<KnownBoardsTableMember> allKnownBoardsList;

    private final boolean showColoredLines;

    public KnownBoardsFrame(final JFrame parent, final TofTree tofTree) {

        super(parent);
        setModal(true);

        this.tofTree = tofTree;
        language = Language.getInstance();
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            initialize();
        }
        catch( final Exception e ) {
            logger.log(Level.SEVERE, "Exception thrown in constructor", e);
        }
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize((int) (parent.getWidth() * 0.75),
                (int) (parent.getHeight() * 0.75));
        setLocationRelativeTo( parent );

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                super.windowClosed(e);
                dialogClosed();
            }
        });

        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);

        hiddenNames = KnownBoardsManager.getInstance().loadHiddenBoardNames();
    }

    private void dialogClosed() {
        KnownBoardsManager.getInstance().saveHiddenBoardNames(hiddenNames);
    }

    private void updateBoardCountInTitle() {
        final int count = tableModel.getRowCount();
        setTitle( language.getString("KnownBoardsFrame.title") + " (" + count + ")");
    }

    /**
     * Build the GUI.
     */
    private void initialize() {
        setTitle(language.getString("KnownBoardsFrame.title"));

        this.setResizable(true);

        tableModel = new KnownBoardsTableModel();
        // add a special renderer to name column which shows the board icon
        nameColRenderer = new NameColumnRenderer();
        descColRenderer = new DescColumnRenderer();
        showContentTooltipRenderer = new ShowContentTooltipRenderer();
        boardsTable = new SortedTable( tableModel ) {
                @Override
                public TableCellRenderer getCellRenderer(final int row, final int column) {
                    if( column == 0 ) {
                        return nameColRenderer;
                    } else if( column == 3 ) {
                        return descColRenderer;
                    } else {
                        return showContentTooltipRenderer;
                    }
            }};
        boardsTable.setRowSelectionAllowed(true);
        boardsTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

        Bclose = new JButton(language.getString("KnownBoardsFrame.button.close"));
        BboardActions = new JButton(language.getString("KnownBoardsFrame.button.actions")+" ...");
        Bimport = new JButton(language.getString("KnownBoardsFrame.button.import")+" ...");
        Bexport = new JButton(language.getString("KnownBoardsFrame.button.export")+" ...");
        CBshowHidden = new JCheckBox();
        CBshowHidden.setToolTipText(language.getString("KnownBoardsFrame.tooltip.showHidden"));

        TFlookupBoard = new JTextField(10);
        new TextComponentClipboardMenu(TFlookupBoard, language);
        // force a max size, needed for BoxLayout
        TFlookupBoard.setMaximumSize(TFlookupBoard.getPreferredSize());

        TFlookupBoard.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(final DocumentEvent e) {
                    lookupContentChanged();
                }
                public void insertUpdate(final DocumentEvent e) {
                    lookupContentChanged();
                }
                public void removeUpdate(final DocumentEvent e) {
                    lookupContentChanged();
                }
            });

        TFfilterBoard = new JTextField(10);
        new TextComponentClipboardMenu(TFfilterBoard, language);
        // force a max size, needed for BoxLayout
        TFfilterBoard.setMaximumSize(TFfilterBoard.getPreferredSize());

        TFfilterBoard.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(final DocumentEvent e) {
                    filterContentChanged();
                }
                public void insertUpdate(final DocumentEvent e) {
                    filterContentChanged();
                }
                public void removeUpdate(final DocumentEvent e) {
                    filterContentChanged();
                }
            });

        boardsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                 public void valueChanged(final ListSelectionEvent e) {
                     boardsTableListModel_valueChanged(e);
                 } });
        BboardActions.addActionListener( new java.awt.event.ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    if( boardsTable.getSelectedRowCount() > 0 ) {
                        // don't show menu if nothing is selected
                        tablePopupMenu.show(BboardActions, 5, 5);
                    }
                } });
        Bimport.addActionListener( new java.awt.event.ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    import_actionPerformed(e);
                } });
        Bexport.addActionListener( new java.awt.event.ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    export_actionPerformed(e);
                } });
        Bclose.addActionListener( new java.awt.event.ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    dispose();
                } });
        CBshowHidden.addItemListener(new ItemListener() {
                public void itemStateChanged(final ItemEvent e) {
                    if( CBshowHidden.isSelected() ) {
                        loadKnownBoardsIntoTable(true);
                    } else {
                        removeHiddenBoards();
                    }
                } });

        // create panel
        final JPanel mainPanel = new JPanel(new BorderLayout());

        final JPanel buttons = new JPanel(new BorderLayout());
        buttons.setLayout( new BoxLayout( buttons, BoxLayout.X_AXIS ));
        buttons.add( new JLabel(language.getString("KnownBoardsFrame.label.lookup") + ":"));
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( TFlookupBoard );
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( new JLabel(language.getString("KnownBoardsFrame.label.filter") + ":"));
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( TFfilterBoard );

        buttons.add( Box.createHorizontalGlue() );
        buttons.add( CBshowHidden );
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( BboardActions );
        buttons.add(Box.createRigidArea(new Dimension(10,3)));
        buttons.add( Bimport );
        buttons.add(Box.createRigidArea(new Dimension(5,3)));
        buttons.add( Bexport );
        buttons.add(Box.createRigidArea(new Dimension(10,3)));
        buttons.add( Bclose );
        buttons.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        final JScrollPane scrollPane = new JScrollPane(boardsTable);
        scrollPane.setWheelScrollingEnabled(true);

        mainPanel.add( scrollPane, BorderLayout.CENTER );
        mainPanel.add( buttons, BorderLayout.SOUTH );
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,7,7,7));

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(mainPanel, null); // add Main panel

        BboardActions.setEnabled(false);

        initPopupMenu();
    }

    private void initPopupMenu() {
        tablePopupMenu = new JSkinnablePopupMenu();
        final JMenuItem addBoardsMenu = new JMenuItem(language.getString("KnownBoardsFrame.button.addBoards"));
        final JMenuItem addBoardsToFolderMenu = new JMenuItem(language.getString("KnownBoardsFrame.button.addBoardsToFolder")+" ...");
        final JMenuItem removeBoardEntry = new JMenuItem(language.getString("KnownBoardsFrame.button.removeBoard"));
        final JMenuItem hideBoardEntry = new JMenuItem(language.getString("KnownBoardsFrame.button.hideBoard"));
        final JMenuItem unhideBoardEntry = new JMenuItem(language.getString("KnownBoardsFrame.button.unhideBoard"));

        addBoardsMenu.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                addBoards_actionPerformed(e);
            } });
        addBoardsToFolderMenu.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                addBoardsToFolder_actionPerformed(e);
            } });
        removeBoardEntry.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                deleteBoards_actionPerformed(e);
            } });
        hideBoardEntry.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                hideBoards_actionPerformed(e);
            } });
        unhideBoardEntry.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                unhideBoards_actionPerformed(e);
            } });

        tablePopupMenu.add(addBoardsMenu);
        tablePopupMenu.add(addBoardsToFolderMenu);
        tablePopupMenu.addSeparator();
        tablePopupMenu.add(hideBoardEntry);
        tablePopupMenu.add(unhideBoardEntry);
        tablePopupMenu.addSeparator();
        tablePopupMenu.add(removeBoardEntry);

        boardsTable.addMouseListener(new TablePopupMenuMouseListener());
    }

    public void startDialog() {
        loadKnownBoardsIntoTable(CBshowHidden.isSelected());
        setVisible(true); // blocking!
    }

    private void loadKnownBoardsIntoTable(boolean showHidden) {
        allKnownBoardsList = new LinkedList<KnownBoardsTableMember>();
        this.tableModel.clearDataModel();
        TFfilterBoard.setText("");
        TFlookupBoard.setText("");
        // gets all known boards from Core, and shows all not-doubles in table
        final List<Board> frostboards = MainFrame.getInstance().getTofTreeModel().getAllBoards();
        final Iterator<KnownBoard> i = KnownBoardsManager.getKnownBoardsList().iterator();
        // check each board in list if already in boards tree, if not add to table
        while( i.hasNext() ) {
            final KnownBoard b = i.next();

            // check if board name is hidden
            if( isNameHidden(b) ) {
                if( !showHidden ) {
                    continue;
                } else {
                    b.setHidden(true);
                }
            }

            final String bname = b.getName();
            final String bprivkey = b.getPrivateKey();
            final String bpubkey = b.getPublicKey();

            // check if this board is already in boards tree (currently)
            boolean addMe = true;
            final Iterator<Board> j = frostboards.iterator();
            while( j.hasNext() ) {
                final Board board = j.next();
                if( board.getName().equalsIgnoreCase(bname)
                    && ((board.getPrivateKey() == null && bprivkey == null) ||
                        (board.getPrivateKey() != null && board.getPrivateKey().equals(bprivkey)))
                    && ((board.getPublicKey() == null && bpubkey == null) ||
                        (board.getPublicKey() != null && board.getPublicKey().equals(bpubkey))) )
                {
                    // same boards, dont add
                    addMe = false;
                    break;
                }
            }
            if( addMe ) {
                // add this new board to table
                final KnownBoardsTableMember member = new KnownBoardsTableMember(b);
                this.tableModel.addRow(member);
                allKnownBoardsList.add(member);
            }
        }
        updateBoardCountInTitle();
    }

    private void addBoards_actionPerformed(final ActionEvent e) {
        final int[] selectedRows = boardsTable.getSelectedRows();

        if( selectedRows.length > 0 ) {
            for( int z = selectedRows.length - 1; z > -1; z-- ) {
                final int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() ) {
                    continue; // paranoia
                }

                // add the board(s) to board tree and remove it from table
                final KnownBoardsTableMember row = (KnownBoardsTableMember) tableModel.getRow(rowIx);
                tofTree.addNewBoard(row.getBoard());
                tableModel.deleteRow(row);
                allKnownBoardsList.remove(row);
            }
            boardsTable.clearSelection();

            updateBoardCountInTitle();
        }
    }

    private void addBoardsToFolder_actionPerformed(final ActionEvent e) {

        final TargetFolderChooser tfc = new TargetFolderChooser(MainFrame.getInstance().getTofTreeModel());
        final Folder targetFolder = tfc.startDialog();
        if( targetFolder == null ) {
            return;
        }

        final int[] selectedRows = boardsTable.getSelectedRows();
        if( selectedRows.length > 0 ) {
            for( int z = selectedRows.length - 1; z > -1; z-- ) {
                final int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() ) {
                    continue; // paranoia
                }

                // add the board(s) to board tree and remove it from table
                final KnownBoardsTableMember row = (KnownBoardsTableMember) tableModel.getRow(rowIx);
                MainFrame.getInstance().getTofTreeModel().addNodeToTree(row.getBoard(), targetFolder);
                tableModel.deleteRow(row);
                allKnownBoardsList.remove(row);
            }
            boardsTable.clearSelection();

            updateBoardCountInTitle();
        }
    }

    private void deleteBoards_actionPerformed(final ActionEvent e) {
        final int[] selectedRows = boardsTable.getSelectedRows();
        if( selectedRows.length > 0 ) {
            for( int z = selectedRows.length - 1; z > -1; z-- ) {
                final int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() ) {
                    continue; // paranoia
                }

                final KnownBoardsTableMember row = (KnownBoardsTableMember) tableModel.getRow(rowIx);
                tableModel.deleteRow(row);

                allKnownBoardsList.remove(row);
                // remove from global list of known boards
                KnownBoardsManager.deleteKnownBoard(row.getBoard());
            }
            boardsTable.clearSelection();

            updateBoardCountInTitle();
        }
    }

    private void hideBoards_actionPerformed(final ActionEvent e) {
        final int[] selectedRows = boardsTable.getSelectedRows();
        if( selectedRows.length > 0 ) {
            for( int z = selectedRows.length - 1; z > -1; z-- ) {
                final int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() ) {
                    continue; // paranoia
                }

                final KnownBoardsTableMember row = (KnownBoardsTableMember) tableModel.getRow(rowIx);
                addHiddenName(row.getBoard().getName());
                row.getBoard().setHidden(true);
            }
            boardsTable.clearSelection();
            if( !CBshowHidden.isSelected() ) {
                removeHiddenBoards();
            } else {
                tableModel.tableEntriesChanged();
            }
            updateBoardCountInTitle();
        }
    }

    private void unhideBoards_actionPerformed(final ActionEvent e) {
        final int[] selectedRows = boardsTable.getSelectedRows();
        if( selectedRows.length > 0 ) {
            for( int z = selectedRows.length - 1; z > -1; z-- ) {
                final int rowIx = selectedRows[z];

                if( rowIx >= tableModel.getRowCount() ) {
                    continue; // paranoia
                }

                final KnownBoardsTableMember row = (KnownBoardsTableMember) tableModel.getRow(rowIx);
                removeHiddenName(row.getBoard().getName());
                row.getBoard().setHidden(false);
            }
            boardsTable.clearSelection();
            tableModel.tableEntriesChanged();

            updateBoardCountInTitle();
        }
    }

    private void removeHiddenBoards() {
        for( int row=tableModel.getRowCount()-1; row >= 0; row-- ) {
            final KnownBoardsTableMember memb = (KnownBoardsTableMember)tableModel.getRow(row);
            if( isNameHidden(memb.getBoard()) ) {
                tableModel.removeRow(row);
            }
        }
        updateBoardCountInTitle();
    }

    private void boardsTableListModel_valueChanged(final ListSelectionEvent e) {
        if( boardsTable.getSelectedRowCount() > 0 ) {
            BboardActions.setEnabled(true);
        } else {
            BboardActions.setEnabled(false);
        }
    }

    private void import_actionPerformed(final ActionEvent e) {
        final File xmlFile = chooseImportFile();
        if( xmlFile == null ) {
            return;
        }
        final List<Board> imports = KnownBoardsXmlDAO.loadKnownBoards(xmlFile);
        if( imports.size() == 0 ) {
            MiscToolkit.getInstance().showMessage(
                    language.getString("KnownBoardsFrame.noBoardsImported.body"),
                    JOptionPane.WARNING_MESSAGE,
                    language.getString("KnownBoardsFrame.noBoardsImported.title"));
        } else {
            final int added = KnownBoardsManager.addNewKnownBoards(imports);
            MiscToolkit.getInstance().showMessage(
                    language.formatMessage("KnownBoardsFrame.boardsImported.body",
                            Integer.toString(imports.size()),
                            xmlFile.getName(),
                            Integer.toString(added)),
                    JOptionPane.WARNING_MESSAGE,
                    language.getString("KnownBoardsFrame.boardsImported.title"));
            loadKnownBoardsIntoTable(CBshowHidden.isSelected());
        }
    }

    private void export_actionPerformed(final ActionEvent e) {
        final File xmlFile = chooseExportFile();
        if( xmlFile == null ) {
            return;
        }
        // don't export hidden boards
        final List<Board> frostboards = MainFrame.getInstance().getTofTreeModel().getAllBoards();
        frostboards.addAll(KnownBoardsManager.getKnownBoardsList());
        for(final Iterator<Board> i=frostboards.iterator(); i.hasNext(); ) {
            final Board b = i.next();
            if( isNameHidden(b) ) {
                i.remove();
            }
        }

        if( KnownBoardsXmlDAO.saveKnownBoards(xmlFile, frostboards) ) {
            MiscToolkit.getInstance().showMessage(
                    language.formatMessage("KnownBoardsFrame.boardsExported.body", Integer.toString(frostboards.size()), xmlFile.getName()),
                    JOptionPane.INFORMATION_MESSAGE,
                    language.getString("KnownBoardsFrame.boardsExported.title"));
        } else {
            MiscToolkit.getInstance().showMessage(
                    language.getString("KnownBoardsFrame.exportFailed.body"),
                    JOptionPane.ERROR_MESSAGE,
                    language.getString("KnownBoardsFrame.exportFailed.title"));
        }
    }

    private File chooseExportFile() {
        final FileFilter myFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if( file.isDirectory() ) {
                    return true;
                }
                if( file.getName().endsWith(".xml") ) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                return "*.xml";
            }
        };

        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(myFilter);
        final int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            final String s = f.getPath();
            if( !s.endsWith(".xml") ) {
                f = new File(s+".xml");
            }
            return f;
        }
        return null;
    }

    private File chooseImportFile() {
        final FileFilter myFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if( file.isDirectory() ) {
                    return true;
                }
                if( file.getName().endsWith(".xml") ) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                return "*.xml";
            }
        };

        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(myFilter);
        final int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * The class is a table row, holding the board and its file/message counts.
     */
    class KnownBoardsTableMember implements TableMember {

        KnownBoard frostboard;

        public KnownBoardsTableMember(final KnownBoard b) {
            this.frostboard = b;
        }

        public Object getValueAt(final int column) {
            switch( column ) {
            case 0:
                if( frostboard.isHidden() ) {
                    return frostboard.getName() + " (H)";
                } else {
                    return frostboard.getName();
                }
            case 1:
                return ((frostboard.getPublicKey() == null) ? "" : frostboard.getPublicKey());
            case 2:
                return ((frostboard.getPrivateKey() == null) ? "" : frostboard.getPrivateKey());
            case 3:
                return ((frostboard.getDescription() == null) ? "" : frostboard.getDescription());
            }
            return "*ERR*";
        }

        public int compareTo(final TableMember anOther, final int tableColumIndex) {
            final String c1 = (String) getValueAt(tableColumIndex);
            final String c2 = (String) anOther.getValueAt(tableColumIndex);
            return c1.compareToIgnoreCase(c2);
        }

        public KnownBoard getBoard() {
            return frostboard;
        }
    }

    /**
     * Called whenever the content of the lookup text field changes
     */
    private void lookupContentChanged() {
        try {
            final String txt = TFlookupBoard.getDocument().getText(0, TFlookupBoard.getDocument().getLength());
            // now try to find the first board name that starts with this txt (case insensitiv),
            // if we found one set selection to it, else leave selection untouched
            for( int row=0; row < tableModel.getRowCount(); row++ ) {
                final KnownBoardsTableMember memb = (KnownBoardsTableMember)tableModel.getRow(row);
                if( memb.getBoard().getName().toLowerCase().startsWith(txt.toLowerCase()) ) {
                    boardsTable.getSelectionModel().setSelectionInterval(row, row);
                    // now scroll to selected row, try to show it on top of table

                    // determine the count of showed rows
                    final int visibleRows = (int)(boardsTable.getVisibleRect().getHeight() / boardsTable.getCellRect(row,0,true).getHeight());
                    int scrollToRow;
                    if( row + visibleRows > tableModel.getRowCount() ) {
                        scrollToRow = tableModel.getRowCount()-1;
                    } else {
                        scrollToRow = row + visibleRows - 1;
                    }
                    if( scrollToRow > row ) {
                        scrollToRow--;
                    }
                    // scroll 2 times to make sure row is displayed
                    boardsTable.scrollRectToVisible(boardsTable.getCellRect(row,0,true));
                    boardsTable.scrollRectToVisible(boardsTable.getCellRect(scrollToRow,0,true));
                    break;
                }
            }
        } catch(final Exception ex) {}
    }

    /**
     * Called whenever the content of the filter text field changes
     */
    private void filterContentChanged() {
        try {
            TFlookupBoard.setText(""); // clear
            String txt = TFfilterBoard.getDocument().getText(0, TFfilterBoard.getDocument().getLength()).trim();
            txt = txt.toLowerCase();
            // filter: show all boards that have this txt in name
            tableModel.clearDataModel();
            for( final KnownBoardsTableMember tm : allKnownBoardsList ) {
                if( txt.length() > 0 ) {
                    final String bn = tm.getBoard().getName().toLowerCase();
                    if( bn.indexOf(txt) < 0 ) {
                        continue;
                    }
                }
                tableModel.addRow(tm);
            }
        } catch(final Exception ex) {}
        updateBoardCountInTitle();
    }

    class NameColumnRenderer extends ShowContentTooltipRenderer {
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column)
        {
            super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column);

            final KnownBoardsTableMember memb = (KnownBoardsTableMember)tableModel.getRow(row);
            if( memb.getBoard().getPublicKey() == null &&
                memb.getBoard().getPrivateKey() == null )
            {
                // public board
                setIcon(boardIcon);
            }
            else if( memb.getBoard().getPublicKey() != null &&
                     memb.getBoard().getPrivateKey() == null )
            {
                // read access board
                setIcon(readAccessIcon);
            }
            else if( memb.getBoard().getPrivateKey() != null )
            {
                // write access board (or write-only)
                setIcon(writeAccessIcon);
            }
            return this;
        }
    }

    class DescColumnRenderer extends ShowColoredLinesRenderer {
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column)
        {
            super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column);

            final KnownBoardsTableMember memb = (KnownBoardsTableMember)tableModel.getRow(row);
            if( memb.getBoard().getDescription() != null &&
                memb.getBoard().getDescription().length() > 0 )
            {
                setToolTipText(memb.getBoard().getDescription());
            } else {
                setToolTipText(null);
            }

            return this;
        }
    }

    private class ShowColoredLinesRenderer extends DefaultTableCellRenderer {
        public ShowColoredLinesRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                final Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                setBackground(newBackground);
            } else {
                setBackground(table.getSelectionBackground());
            }
            return this;
        }
    }

    private class ShowContentTooltipRenderer extends ShowColoredLinesRenderer {
        public ShowContentTooltipRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tooltip = null;
            if( value != null ) {
                tooltip = value.toString();
                if( tooltip.length() == 0 ) {
                    tooltip = null;
                }
            }
            setToolTipText(tooltip);
            return this;
        }
    }

    class TablePopupMenuMouseListener implements MouseListener {
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
                if( boardsTable.getSelectedRowCount() > 0 ) {
                    // don't show menu if nothing is selected
                    tablePopupMenu.show(boardsTable, e.getX(), e.getY());
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Hidden board names handling /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////

    private boolean isNameHidden(final Board b) {
        final String boardName = b.getName();
        return isNameHidden(boardName);
    }
    private boolean isNameHidden(final String n) {
        if( hiddenNames.contains(n.toLowerCase()) ) {
            return true;
        } else {
            return false;
        }
    }

    private void addHiddenName(final String n) {
        hiddenNames.add(n.toLowerCase());
    }
    private void removeHiddenName(final String n) {
        hiddenNames.remove(n.toLowerCase());
    }
}
