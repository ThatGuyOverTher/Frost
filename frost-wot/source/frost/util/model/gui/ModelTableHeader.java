/*
 * Created on May 31, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.model.gui;

import java.awt.Component;
import java.awt.event.*;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.table.*;

/**
 * This is subclass of JTableHeader that listens for mouse clicks on it.
 * It does nothing with them, but provides a couple of methods (headerClicked 
 * and headerReleased) that subclasses can override as necessary.
 * @author $Author$
 * @version $Revision$
 */
public class ModelTableHeader extends JTableHeader {

	/**
	 * This inner class is the popup menu that will be shown to let the user choose
	 * which columns the table should show.
	 */
	private class ColumnsPopupMenu extends JPopupMenu {

		/* (non-Javadoc)
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			removeAll();
			Iterator columns = modelTable.getColumns();
			int i = 0;
			int shownColumns = 0;
			JCheckBoxMenuItem lastShownItem = null;
			while (columns.hasNext()) {
				TableColumn column = (TableColumn) columns.next();
				JCheckBoxMenuItem menuItem =
					new JCheckBoxMenuItem(column.getIdentifier().toString());
				if (modelTable.isColumnVisible(i)) {
					menuItem.setSelected(true);
					shownColumns++;
					lastShownItem = menuItem;
				} else {
					menuItem.setSelected(false);
				}
				menuItem.addActionListener(listener);
				add(menuItem);
				i++;
			}
			//If there is only one column showing, we disable its
			//checkbox to prevent the user from hiding that one too.
			if (shownColumns == 1) {
				lastShownItem.setEnabled(false);
			}
			super.show(invoker, x, y);
		}

	}

	/**
	 * This inner class listens for mouse clicks on the header and
	 * for selections in the popup menu
	 */
	private class Listener extends MouseAdapter implements ActionListener {

		/**
		 *	This constructor creates a new instance of Listener
		 */
		public Listener() {
			super();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			headerClicked(e);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			headerReleased(e);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (popup != null) {
				int position = popup.getComponentIndex((Component) e.getSource());
				if (position != -1) {
					popupMenu_actionPerformed(position);
				}
			}
		}

	}

	private Listener listener = new Listener();

	private ModelTable modelTable;

	private ColumnsPopupMenu popup;

	/**
	 * This constructor creates a new instance of ModelTableHeader associated
	 * to the ModelTable that is passed as a parameter.
	 * @param cm the ModelTable that is going to have this header
	 */
	public ModelTableHeader(ModelTable newModelTable) {
		super(newModelTable.getTable().getColumnModel());

		modelTable = newModelTable;

		addMouseListener(listener);
	}

	/**
	 * This method is called whenever the user clicks on the header
	 * (when the mouse button is pressed)
	 * @param e the MouseEvent
	 */
	protected void headerClicked(MouseEvent e) {
		if (e.isPopupTrigger()) {
			getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * This method is called whenever the user clicks on the header
	 * (when the mouse button is released)
	 * @param e the MouseEvent
	 */
	protected void headerReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * This method is called whenever a MenuItem of the
	 * ColumnsPopupMenu is selected
	 * @param position the position of the MenuItem in the 
	 * 					ColumnsPopupMenu that was selected
	 */
	private void popupMenu_actionPerformed(int position) {
		JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) popup.getComponent(position);
		if (menuItem.isSelected()) {
			modelTable.setColumnVisible(position, true);
		} else {
			modelTable.setColumnVisible(position, false);
		}
	}

	/**
	 * This method returns a reference to the inner ColumnsPopupMenu, 
	 * creating one instance of it if it hadn't been created yet.
	 * @return an instance of ColumnsPopupMenu
	 */
	private JPopupMenu getPopupMenu() {
		if (popup == null) {
			popup = new ColumnsPopupMenu();
		}
		return popup;
	}

}
