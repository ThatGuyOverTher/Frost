/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import javax.swing.JTable;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractTableFormat implements ModelTableFormat {

	private int columnCount;

	/**
	 * 
	 */
	protected AbstractTableFormat(int newColumnCount) {
		super();
		columnCount = newColumnCount;
	}

	/* (non-Javadoc)
	 * @see frost.util.model.gui.ModelTableFormat#customizeTable(javax.swing.JTable)
	 */
	public void customizeTable(JTable table) {
		// Nothing here. Override in subclasses if necessary.

	}

	/**
	 * @return
	 */
	public int getColumnCount() {
		return columnCount;
	}

}
