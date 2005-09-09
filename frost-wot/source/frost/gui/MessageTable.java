/*
  MessageTable.java / Frost
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

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.gui.model.*;
import frost.gui.objects.*;
import frost.messages.*;

public class MessageTable extends SortedTable
{
	private CellRenderer cellRenderer = new CellRenderer();
	
	public MessageTable(MessageTableModel m) {
		super(m);

		setDefaultRenderer(Object.class, cellRenderer);

		// default for messages: sort by date descending
		sortedColumnIndex = 4;
		sortedColumnAscending = false;
		resortTable();
	}
    
    /**
     * This renderer renders rows in different colors.
     * New messages gets a bold look, messages with attachments a blue color.
     * Encrypted messages get a red color, no matter if they have attachments.
     */
    private class CellRenderer extends DefaultTableCellRenderer
    {
        private Font boldFont = null;
        private Font normalFont = null;
        private boolean isDeleted = false;
        private final Color col_good    = new Color(0x00, 0x80, 0x00);
        private final Color col_check   = new Color(0xFF, 0xCC, 0x00);
        private final Color col_observe = new Color(0x00, 0xD0, 0x00);
        private final Color col_bad     = new Color(0xFF, 0x00, 0x00);
        
        public CellRenderer() {
        	Font baseFont = MessageTable.this.getFont();
            normalFont = baseFont.deriveFont(Font.PLAIN);
            boldFont = baseFont.deriveFont(Font.BOLD);
        }
        
        public void paintComponent (Graphics g) {
        	super.paintComponent(g);
        	if(isDeleted) {
        		Dimension size = getSize();
        		g.drawLine(0, size.height / 2, size.width, size.height / 2);
        	}
        }
        
		public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column) {
            
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			MessageTableModel model = (MessageTableModel) getModel();
			FrostMessageObject msg = (FrostMessageObject) model.getRow(row);
            
            // get the original model column index (maybe columns were reordered by user) 
            TableColumn tableColumn = getColumnModel().getColumn(column);
            column = tableColumn.getModelIndex();

			// do nice things for FROM and SIG column
            if( column == 1 ) {
                // FROM
                // first set font, bold for new msg or normal
                if (msg.isMessageNew()) {
                    setFont(boldFont);
                } else {
                    setFont(normalFont);
                }
                // now set color
                if (!isSelected) {
                    if( msg.getRecipient() != null && msg.getRecipient().length() > 0) {
                        setForeground(Color.RED);
                    } else if (msg.containsAttachments()) {
                        setForeground(Color.BLUE);
                    } else {
                        setForeground(Color.BLACK);
                    }
                }
            } else if( column == 3 ) {
                // SIG
                // state == good/bad/check/observe -> bold and coloured
                int state = msg.getMsgStatus();
                if( state == VerifyableMessageObject.xGOOD ) {
                    setFont(boldFont);
                    setForeground(col_good);
                } else if( state == VerifyableMessageObject.xCHECK ) {
                    setFont(boldFont);
                    setForeground(col_check);
                } else if( state == VerifyableMessageObject.xOBSERVE ) {
                    setFont(boldFont);
                    setForeground(col_observe);
                } else if( state == VerifyableMessageObject.xBAD ) {
                    setFont(boldFont);
                    setForeground(col_bad);
                } else {
                    setFont(normalFont);
                    if (!isSelected) {
                        setForeground(Color.BLACK);
                    }
                }
            } else {
                setFont(normalFont);
                if (!isSelected) {
                    setForeground(Color.BLACK);
                }
            }
			
			setDeleted(msg.isDeleted());
			
			return this;
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#setFont(java.awt.Font)
		 */
		public void setFont(Font font) {
			super.setFont(font);
			normalFont = font.deriveFont(Font.PLAIN);
			boldFont = font.deriveFont(Font.BOLD);
		}
		
		public void setDeleted(boolean value) {
			isDeleted = value;
		}
    }

	/* (non-Javadoc)
	 * @see javax.swing.JTable#createDefaultColumnsFromModel()
	 */
	public void createDefaultColumnsFromModel() {
		super.createDefaultColumnsFromModel();

		// set column sizes
		int[] widths = { 30, 150, 250, 50, 150 };
		for (int i = 0; i < widths.length; i++) {
			getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#setFont(java.awt.Font)
	 */
	public void setFont(Font font) {
		super.setFont(font);
		if (cellRenderer != null) {
			cellRenderer.setFont(font);
		}
		setRowHeight(font.getSize() + 5);
	}
}

