/*
  SortedTable.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import java.awt.event.*;
import java.util.*;

import javax.swing.JTable;
import javax.swing.table.*;

import frost.gui.model.SortedTableModel;

public class SortedTable extends JTable
{
    protected int sortedColumnIndex = 0;
    protected boolean sortedColumnAscending = true;
    
    private SortHeaderRenderer columnHeadersRenderer = new SortHeaderRenderer();

    public SortedTable(SortedTableModel model)
    {
        super(model);
        
		model.setParentTable(this);
        
        initSortHeader();
    }

    public void sortColumn(int col, boolean ascending)
    {
        SortedTableModel model = null;
        model = (SortedTableModel)getModel();

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
        SortedTableModel model = null;
        ((SortedTableModel)getModel()).tableEntriesChanged();
    }

    protected void setSelectedItems( ArrayList items )
    {
        if( !(getModel() instanceof SortedTableModel ))
            return;
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
        // build a list containing all selected items
        ArrayList lst = new ArrayList();
        if( !(getModel() instanceof SortedTableModel ))
            return lst;

        SortedTableModel model = (SortedTableModel)getModel();
        int selectedRows[] = getSelectedRows();
        for( int x=0; x<selectedRows.length; x++ )
        {
            lst.add( model.getRow( selectedRows[x] ) );
        }
        return lst;
    }

    public void setSavedSettings( int val, boolean val2 )
    {
        if( !(getModel() instanceof SortedTableModel ))
            return;
        sortedColumnIndex = val;
        sortedColumnAscending = val2;
        SortedTableModel model = (SortedTableModel)getModel();
        sortColumn(sortedColumnIndex, sortedColumnAscending);
    }

	/**
	 * This method sets a SortHeaderRenderer as the renderer of the headers
	 * of all columns. 
	 * 
	 * The default renderer of the JTableHeader is not touched, because when 
	 * skinks are enabled, they change that renderer. In that case, the renderers 
	 * of the headers of the columns (SortHeaderRenderers) paint the
	 * arrows (if necessary) and then call the JTableHeader default renderer (the
	 * one put by the skin) for it to finish the job.
	 */
	protected void initSortHeader() {
		Enumeration enumeration = getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			TableColumn column = (TableColumn) enumeration.nextElement();
			column.setHeaderRenderer(columnHeadersRenderer);
		}
		getTableHeader().addMouseListener(new HeaderMouseListener());
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
            if( event.isPopupTrigger() == true )
                return; // dont allow right click sorts

            TableColumnModel colModel = getColumnModel();
            int index = colModel.getColumnIndexAtX(event.getX());
            int modelIndex = colModel.getColumn(index).getModelIndex();

            SortedTableModel model = null;
            model = (SortedTableModel)getModel();

            boolean isSortable = false;
            if( model != null && model.isSortable(modelIndex) )
                isSortable = true;
            if( isSortable )
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

