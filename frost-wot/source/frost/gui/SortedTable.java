package frost.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import frost.gui.model.*;

public class SortedTable extends JTable
{
    protected int sortedColumnIndex = 0;
    protected boolean sortedColumnAscending = true;

    public SortedTable(TableModel model)
    {
        super(model);

        // model needs to know parent table to invoke sorting after updateRow
        if( model instanceof SortedTableModel )
        {
            ((SortedTableModel)model).setParentTable( this );
        }
        initSortHeader();
    }

    public void sortColumn(int col, boolean ascending)
    {
        if( ! (getModel() instanceof SortedTableModel) )
            return;
        SortedTableModel model = (SortedTableModel)getModel();

        // get the list of selected items
        ArrayList list = getListOfSelectedItems();
        clearSelection();

        // sort this column
        model.sortModelColumn( col, ascending );

        // reselect the selected items
        setSelectedItems( list );
    }

    public void resortTable()
    {
        sortColumn( sortedColumnIndex, sortedColumnAscending );
        ((SortedTableModel)getModel()).tableEntriesChanged();
    }

    protected void setSelectedItems( ArrayList items )
    {
        SortedTableModel model = (SortedTableModel)getModel();
        for( int x=0; x<model.getRowCount(); x++ )
        {
            Object item1 = model.getRow(x);

            Iterator i = items.iterator();
            while( i.hasNext() )
            {
                Object item2 = i.next();

                if( item1 == item2 )
                {
                    getSelectionModel().addSelectionInterval(x,x);
                }
            }
        }
    }

    protected ArrayList getListOfSelectedItems()
    {
        SortedTableModel model = (SortedTableModel)getModel();
        // build a list containing all selected items
        ArrayList lst = new ArrayList();
        int selectedRows[] = getSelectedRows();
        for( int x=0; x<selectedRows.length; x++ )
        {
            lst.add( model.getRow( selectedRows[x] ) );
        }
        return lst;
    }

    public void setSavedSettings( int val, boolean val2 )
    {
        sortedColumnIndex = val;
        sortedColumnAscending = val2;
        SortedTableModel model = (SortedTableModel)getModel();
        sortColumn(sortedColumnIndex, sortedColumnAscending);
    }

    protected void initSortHeader()
    {
        JTableHeader header = getTableHeader();
        header.setDefaultRenderer(new SortHeaderRenderer());
        header.addMouseListener(new HeaderMouseListener());
    }

    public int getSortedColumnIndex()
    {
        return sortedColumnIndex;
    }

    public boolean isSortedColumnAscending()
    {
        return sortedColumnAscending;
    }

    // used by TablePopupMenuMouseListener
    public SortedTable instance()
    {
        return this;
    }

    class HeaderMouseListener implements MouseListener
    {
        public void mouseReleased(MouseEvent event) {}
        public void mousePressed(MouseEvent event) {}
        public void mouseClicked(MouseEvent event)
        {
            TableColumnModel colModel = getColumnModel();
            int index = colModel.getColumnIndexAtX(event.getX());
            int modelIndex = colModel.getColumn(index).getModelIndex();

            if( !(getModel() instanceof SortedTableModel) )
            {
                System.out.println("Known problem: cant sort message table.");
                return;
            }
            SortedTableModel model = (SortedTableModel)getModel();
            if( model.isSortable(modelIndex) )
            {
                // toggle ascension, if already sorted
                if( sortedColumnIndex == modelIndex )
                {
                    sortedColumnAscending = !sortedColumnAscending;
                }
                sortedColumnIndex = modelIndex;

                sortColumn(modelIndex, sortedColumnAscending);
            }
        }
        public void mouseEntered(MouseEvent event) {}
        public void mouseExited(MouseEvent event) {}
    }
}

