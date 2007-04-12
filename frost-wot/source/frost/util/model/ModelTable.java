/*
 ModelTable.java / Frost
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
package frost.util.model;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * This subclass of AbstractTableModel is passed an OrderedModel and a
 * TableFormat in the constructor. It then creates a JTable and displays 
 * the content of the OrderedModel on it according to the rules specified
 * in the TableFormat. 
 * 
 * It also listens for changes in the OrderedModel  and updates the JTable 
 * as necessary.
 * 
 * Besides, the user can choose which columns will be shown via a menu
 * that pops up when he right clicks on the header.
 */
public class ModelTable extends AbstractTableModel {
	
	/**
	 * Helper class to be able to safely get the selection fron any thread
	 */
	protected class SelectionGetter implements Runnable {

		private final int MODE_SINGLE = 0;
		private final int MODE_MULTIPLE = 1;

		int mode = 0;

		ModelItem[] selectedItems;
		ModelItem selectedItem;
		
		/**
		 * This method returns an array of all the ModelItems that are 
		 * selected in the JTable.
		 *  @return an array containing the ModelItems that are selected 
		 */
		public ModelItem[] getSelectedItems() {
			mode = MODE_MULTIPLE;
			if (SwingUtilities.isEventDispatchThread()) {
				run();
			} else {
				try {
					SwingUtilities.invokeAndWait(this);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "Exception thrown in SelectionGetter.run()", e);
				} catch (InvocationTargetException e) {
					logger.log(Level.WARNING, "Exception thrown in SelectionGetter.run()", e);
				}
			}
			return selectedItems;
		}

		/**
		 * This method returns the ModelItem that is selected in 
		 * the JTable (or the first one if there are several). If there is
		 * none, it returns null.
		 *  @return the ModelItem that is selected in the JTable, or
		 * 			 the first one if there are several. null if there is
		 * 			 none.
		 */
		public ModelItem getSelectedItem() {
			mode = MODE_SINGLE;
			if (SwingUtilities.isEventDispatchThread()) {
				run();
			} else {
				try {
					SwingUtilities.invokeAndWait(this);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "Exception thrown in SelectionGetter.run()", e);
				} catch (InvocationTargetException e) {
					logger.log(Level.WARNING, "Exception thrown in SelectionGetter.run()", e);
				}
			}
			return selectedItem;
		}

		/** 
		 * This method is executed in the Swing event thread. It gets the selected items
		 * and places them in an attribute so that the methods getSelectedItem and getSelectedItems
		 * can return them
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			synchronized (model) {
				switch (mode) {
					case MODE_MULTIPLE :
						int selectionCount = table.getSelectedRowCount();
						selectedItems = new ModelItem[selectionCount];
						int[] selectedRows = table.getSelectedRows();
						for (int i = 0; i < selectedRows.length; i++) {
							selectedItems[i] = model.getItemAt(selectedRows[i]);
						}
						break;
					case MODE_SINGLE :
						int selectedRow = table.getSelectedRow();
						if (selectedRow != -1) {
							selectedItem = model.getItemAt(selectedRow);
						}
						break;
				}
			}
		}

	}
	
	private static final Logger logger = Logger.getLogger(ModelTable.class.getName());

	protected ModelTableFormat tableFormat;	
	protected SortedModel model;
	
	protected JTable table;
	private JScrollPane scrollPane;
	
	/**
	 * This ArrayList contains the model indexes of the columns that are being shown
	 */
	private ArrayList<Integer> visibleColumns = new ArrayList<Integer>();
	
	/**
	 * This ArrayList contains all of the TableColumns that this ModelTable may show.
	 */
	private ArrayList<TableColumn> columns = new ArrayList<TableColumn>();

	/**
	 * This method creates an instance of Model table with the given ModelTableFormat
	 * but without an OrderedModel. The method setModel should be called before 
	 * initialization (this constructor does not perform that initialization).
	 * @param newTableFormat the ModelTableFormat that defines the visual representation
	 * 						  of the data in the OrderedModel.
	 */
	protected ModelTable(ModelTableFormat newTableFormat) {
		super();
	
		tableFormat = newTableFormat;
	}

	/**
	 * This method creates an instance of Model table with the given ModelTableFormat
	 * and OrderedModel and initializes it.
	 * @param newModel the OrderedModel that contains the data to be shown on the JTable 
	 * @param newTableFormat the ModelTableFormat that defines the visual representation
	 * 						  of the data in the OrderedModel.
	 */
	protected ModelTable(SortedModel newModel, ModelTableFormat newTableFormat) {
		super();
		
		model = newModel;
		tableFormat = newTableFormat;
				
		initialize();
	}
	
	/**
	 * This method initializes the ModelTable. It creates the default TableColumns,
	 * customizes the JTable and sets up the listener.
	 */
	protected void initialize() {
		int columnCount = tableFormat.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			visibleColumns.add(new Integer(i));
		}

		table = new JTable(this);
		scrollPane = new JScrollPane(table);
        
        scrollPane.setWheelScrollingEnabled(true);
		tableFormat.addTable(table);

		tableFormat.customizeTable(this);
		
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			columns.add(columnModel.getColumn(i));
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return visibleColumns.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return model.getItemCount();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		int index = convertColumnIndexToFormat(columnIndex);
		return tableFormat.getCellValue(model.getItemAt(rowIndex), index);
	}
	
	
	/**
	 * This method is called whenever an event is received from the 
	 * OrderedModel indicating that several items have been removed from it.
	 * @param positions the positions of the ModelItems that have
	 * been removed from the OrderedModel.
	 */
	protected void fireTableRowsDeleted(int[] positions) {
		for (int i = 0; i < positions.length; i++) {
			fireTableRowsDeleted(positions[i], positions[i]);	
		}			
	}
	
	/** 
	 * This method returns an array of all the ModelItems that are 
	 * selected in the JTable.
	 * @return an array containing the ModelItems that are selected 
	 */
	public ModelItem[] getSelectedItems() {
		return new SelectionGetter().getSelectedItems();
	}
	
	/**
	 * This method returns the selected ModelItem, or the first one
	 * if there was several of them. It returns null if there was none.
	 * @return the selected ModelItem, or the first one if there was 
	 * 			several of them. null if there was none.
	 */
	public ModelItem getSelectedItem() {
		return new SelectionGetter().getSelectedItem();
	}
	
	/**
	 * This method returns the number of rows that are selected.
	 * @return the number of rows that are selected.
	 */
	public int getSelectedCount() {
		return table.getSelectedRowCount();
	}

	/**
	 * This method returns the JScrollPane the JTable is created into.
	 * @return the JScrollPane the JTable is created into.
	 */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		int index = convertColumnIndexToFormat(column);
		return tableFormat.getColumnName(index);
	}

	/**
	 * This method returns the JTable that is used by this ModelTable 
	 * to show the contents of its OrderedModel.
	 * @return the JTable that is used by this ModelTable to show the
	 * 			contents of its OrderedModel.
	 */
	public JTable getTable() {
		return table;
	}

	/**
	 * This method changes the Font of the JTable in this ModelTable
	 * @param font the new font for the JTable in this ModelTable.
	 */
	public void setFont(Font font) {
		table.setFont(font);
		table.setRowHeight(font.getSize() + 5);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		int index = convertColumnIndexToFormat(columnIndex);
		return tableFormat.isColumnEditable(index);
	}
	
	/**
	 * This method is used to find out if the column with the given model index
	 * is currently being shown or not.
	 * @param columnIndex the model index of the column to find out if it is
	 * 					   being shown or not.
	 * @return true if the column is being shown. false otherwise.
	 */
	public boolean isColumnVisible(int columnIndex) {
		int position = convertColumnIndexToModel(columnIndex);
		if (position != -1) {
			return true;
		} else {
			return false;	
		}
	}
	
	/**
	 * This method shows or hides a particular column. In case it tries to
	 * show a column that is already being shown or to hide a column that is
	 * already hidden, the command is simply ignored.
	 * @param index the model index of the column to hide or show.
	 * @param visible if true, the column will be shown. If false, the column
	 * 		   will be hidden.
	 */
	public void setColumnVisible(int index, boolean visible) {
		TableColumnModel columnModel = getTable().getColumnModel();
		int position = convertColumnIndexToModel(index);

		if (visible) {
			if (position == -1) {
				visibleColumns.add(new Integer(index));
				TableColumn column = (TableColumn) columns.get(index);
				column.setModelIndex(visibleColumns.size() - 1);
				columnModel.addColumn(column);
			}
		} else {
			if (position != -1) {
				visibleColumns.remove(new Integer(index));
				columnModel.removeColumn((TableColumn) columns.get(index));
				//Here we have to decrease the model index of all the columns
				//that were to the right of the one we have removed.
				for (int i = 0; i < columnModel.getColumnCount(); i++) {
					TableColumn column = columnModel.getColumn(i);
					int modelIndex = column.getModelIndex();
					if (modelIndex >= position) {
						column.setModelIndex(modelIndex - 1);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		int index = convertColumnIndexToFormat(columnIndex);
		tableFormat.setCellValue(aValue, model.getItemAt(rowIndex), index);
	}

	/**
	 * This method sets a new OrderedModel for the ModelTable (mainly to be
	 * used in conjunction with the constructor that is only passed a ModelTableFormat)
	 * @param model the OrderedModel this ModelTable will get the data from
	 */
	protected void setModel(SortedModel newModel) {
		model = newModel;
	}

	/**
	 * This method returns an Iterator of all the TableColumns that this
	 * ModelTable may show.
	 * @return an Iterator of all the TableColumns that this
	 * 			ModelTable may show.
	 */
	public Iterator getColumns() {
		return columns.iterator();
	}

    public List getColumnsList() {
        return columns;
    }

	/**
	 * This method maps the index that a column has in the associated TableFormat to
	 * the index that column has in this ModelTable.
	 * 
	 * @param formatColumnIndex the index a column has in the associated TableFormat
	 * @return the index that column has in this ModelTable 
	 */
	protected int convertColumnIndexToModel(int formatColumnIndex) {
		return visibleColumns.indexOf(new Integer(formatColumnIndex));
	}
	
	/**
	 * This method maps the index that a column has in this ModelTable to
	 * the index that column has in the associated TableFormat.
	 * 
	 * @param formatColumnIndex the index a column has in this ModelTable
	 * @return the index that column has in the associated TableFormat 
	 */
	protected int convertColumnIndexToFormat(int modelColumnIndex) {
		Integer index = (Integer) visibleColumns.get(modelColumnIndex);
		return index.intValue();
	}
    
    public ModelTableFormat getTableFormat() {
        return tableFormat;
    }
}
