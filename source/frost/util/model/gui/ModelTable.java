/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import frost.util.model.*;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ModelTable extends AbstractTableModel {
	/**
	 * This inner class listens for the changes in the model and updates the 
	 * table in the Swing event thread.
	 */
	private class Listener implements OrderedModelListener {
	
		/**
		 * 
		 */
		public Listener() {
			super();
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemChanged(int, frost.util.model.ModelItem, int, java.lang.Object, java.lang.Object)
		 */
		public void itemChanged(int position, ModelItem item, int fieldID, Object oldValue, Object newValue) {
			fireTableCellUpdated(position, tableFormat.getColumnNumber(fieldID));
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemChanged(int, frost.util.model.ModelItem)
		 */
		public void itemChanged(int position, ModelItem item) {
			fireTableRowsUpdated(position, position);			
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemAdded(int, frost.util.model.ModelItem)
		 */
		public void itemAdded(int position, ModelItem item) {
			fireTableRowsInserted(position, position);			
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#itemsRemoved(int[], frost.util.model.ModelItem[])
		 */
		public void itemsRemoved(int[] positions, ModelItem[] items) {
			fireTableRowsDeleted(positions);			
		}

		/* (non-Javadoc)
		 * @see frost.util.model.OrderedModelListener#modelCleared()
		 */
		public void modelCleared() {
			fireTableDataChanged();			
		}

	}
	
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
		 * 
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
		 * 
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

		/* (non-Javadoc)
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
	
	private static Logger logger = Logger.getLogger(ModelTable.class.getName());

	private Listener listener = new Listener();

	private ModelTableFormat tableFormat;	
	protected OrderedModel model;
	
	protected JTable table;
	private JScrollPane scrollPane;

	/**
	 * 
	 */
	public ModelTable(OrderedModel newModel, ModelTableFormat newTableFormat) {
		super();
		
		model = newModel;
		tableFormat = newTableFormat;
				
		table = new JTable(this);
		scrollPane = new JScrollPane(table);
		tableFormat.customizeTable(table);
		
		model.addOrderedModelListener(listener);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return tableFormat.getColumnCount();
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
		return tableFormat.getCellValue(model.getItemAt(rowIndex), columnIndex);
	}
	
	/**
	 * @param positions
	 */
	protected void fireTableRowsDeleted(int[] positions) {
		for (int i = 0; i < positions.length; i++) {
			fireTableRowsDeleted(positions[i], positions[i]);	
		}			
	}
	
	/**
	 * @return
	 */
	public ModelItem[] getSelectedItems() {
		return new SelectionGetter().getSelectedItems();
	}
	
	/**
	 * @return
	 */
	public ModelItem getSelectedItem() {
		return new SelectionGetter().getSelectedItem();
	}
	
	/**
	 * @return
	 */
	public int getSelectedCount() {
		return table.getSelectedRowCount();
	}

	/**
	 * @return
	 */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		return tableFormat.getColumnName(column);
	}

	/**
	 * @return
	 */
	public JTable getTable() {
		return table;
	}

}
