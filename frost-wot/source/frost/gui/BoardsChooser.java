/*
  BoardsChooser.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>
  This file is contributed by Stefan Majewski <feuerblume@users.sourceforge.net>

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
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import frost.gui.objects.*;
import frost.util.gui.translation.*;

public class BoardsChooser extends JDialog {

    private Language language = Language.getInstance();

    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    
    JTable boardsTable;
    BoardsTableModel boardsTableModel;
    
    JButton Bcancel;
    List boardList;
    JButton Bok;
    boolean okPressed = false;

    public BoardsChooser(Frame parent, List boards, List preselectedBoards) {
        super(parent);
        setModal(true);
        setTitle(language.getString("Choose boards"));
        
        // fill given board into our list as BoardListEntries
        boardList = new ArrayList();
        for(Iterator i=boards.iterator(); i.hasNext(); ) {
            Board b = (Board)i.next();
            BoardTableEntry e = new BoardTableEntry();
            e.board = b;
            e.isSelected = new Boolean(false);

            if( preselectedBoards != null ) {
                // check if this board should be selected
                for(Iterator j=preselectedBoards.iterator(); j.hasNext(); ) {
                    Board sb = (Board)j.next();
                    if( b.getName().equals(sb.getName()) ) {
                        e.isSelected = new Boolean(true);
                    }
                }
            }
            boardList.add(e);
        }

        initGui();

        setLocationRelativeTo(parent);
    }

    public BoardsChooser(Frame parent, List boards) {
        this(parent, boards, null);
    }

    private void initGui() {
        Bok = new JButton(language.getString("OK"));
        Bok.addActionListener( new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                    okPressed = true;
                    setVisible(false);
               } });
        Bcancel = new JButton(language.getString("Cancel"));
        Bcancel.addActionListener( new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                    okPressed = false;
                    setVisible(false);
               } });
        JPanel buttonsPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT, 8, 8) );
        buttonsPanel.add( Bok );
        buttonsPanel.add( Bcancel );

        boardsTableModel = new BoardsTableModel(boardList);
        boardsTable = new JTable(boardsTableModel);
        boardsTable.setShowGrid(false);
        boardsTable.setTableHeader(null);

        boardsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        boardsTable.setRowSelectionAllowed(true);
        
        boardsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // on double click toggle the board selection state (checkbox) 
                if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int row = boardsTable.getSelectedRow();
                    if (row < 0) {
                        return;
                    }
                    BoardTableEntry en = (BoardTableEntry)boardList.get(row);
                    if( en == null ) {
                        return;
                    }
                    en.isSelected = new Boolean(!en.isSelected.booleanValue());
                    boardsTableModel.fireTableCellUpdated(row, 0);
                }
            }
        });
        
        TableColumn column = boardsTable.getColumnModel().getColumn(0);
        column.setPreferredWidth(30);
        column.setMaxWidth(30);
        
        JScrollPane listScroller = new JScrollPane();
        listScroller.setBorder( new CompoundBorder( new EmptyBorder(5,5,5,5),
                                                    new CompoundBorder( new EtchedBorder(),
                                                                        new EmptyBorder(5,5,5,5) )
                                                  ) );
        listScroller.setWheelScrollingEnabled(true);
        listScroller.setViewportView(boardsTable);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(listScroller, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        setSize(300, 400);
    }

    public List runDialog()
    {
        setVisible(true);
        if( okPressed == false ) {
            return null;
        }

        ArrayList chosed = new ArrayList();
        for(Iterator i=boardList.iterator(); i.hasNext(); ) {
            BoardTableEntry e = (BoardTableEntry)i.next();
            if( e.isSelected.booleanValue() ) {
                chosed.add(e.board);
            }
        }
        return chosed;
    }
    
    class BoardTableEntry {
        public Boolean isSelected;
        public Board board;
    }
    
    class BoardsTableModel extends AbstractTableModel {
        
        List boardsList = new ArrayList();
        
        public BoardsTableModel(List l) {
            super();
            boardsList = l;
        }
        public String getColumnName(int col) {
            return "";
        }
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        public int getRowCount() {
            if( boardsList == null ) {
                return 0;
            }
            return boardsList.size(); 
        }
        public int getColumnCount() { 
            return 2; 
        }
        public Object getValueAt(int row, int col) {
            BoardTableEntry e = (BoardTableEntry)boardsList.get(row);
            if( col==0 ) {
                return e.isSelected;
            } else {
                return e.board.getName();
            }
        }
        public boolean isCellEditable(int row, int col) { 
            if( col == 0 ) {
                return true;
            }
            return false; 
        }
        public void setValueAt(Object value, int row, int col) {
            BoardTableEntry e = (BoardTableEntry)boardsList.get(row);
            e.isSelected = (Boolean)value;
            fireTableCellUpdated(row, col);
        }
    }
}
