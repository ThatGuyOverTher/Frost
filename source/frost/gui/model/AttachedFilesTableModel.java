/*
  AttachedFilesTableModel.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

package frost.gui.model;

import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import frost.messages.*;
import frost.util.gui.translation.*;

public class AttachedFilesTableModel extends DefaultTableModel implements LanguageListener {

    private Language language = null;
    NumberFormat numberFormat = NumberFormat.getInstance();

    protected final static String columnNames[] = new String[3];

    protected final static Class columnClasses[] = {
        String.class, //LangRes.getString("Filename"),
        String.class, //LangRes.getString("Size")
        String.class // key
    };

    public AttachedFilesTableModel() {
        super();
        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }

    private void refreshLanguage() {
        columnNames[0] = language.getString("MessagePane.fileAttachmentTable.filename");
        columnNames[1] = language.getString("MessagePane.fileAttachmentTable.size");
        columnNames[2] = language.getString("MessagePane.fileAttachmentTable.key");

        fireTableStructureChanged();
    }

    /**
     * This method fills the table model with the FileAttachments
     * in the list passed as a parameter
     * @param fileAttachments list of FileAttachments fo fill the model with
     */
    public void setData(List fileAttachments) {
        setRowCount(0);
        Iterator files = fileAttachments.iterator();
        while (files.hasNext()) {
            FileAttachment attachment = (FileAttachment) files.next();
            // maybe we show a file that is not yet uploaded (unsend message file attachment)
            String key;
            if (attachment.getKey() != null && attachment.getKey().length() > 40 ) {
                key = attachment.getKey();
            } else {
                key = "?";
            }
            
            if (attachment.getFilename() != null && attachment.getFilename().length() > 0 ) {
                Object[] row = {
                        attachment.getFilename(), 
                        numberFormat.format( attachment.getFileSize() ), 
                        key};
                addRow(row);
            }
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
    public Class getColumnClass(int column) {
        if( column >= 0 && column < columnClasses.length )
            return columnClasses[column];
        return null;
    }
    
    public void configureTable(JTable t) {
        TableColumn c = t.getColumnModel().getColumn(1); // size column
        c.setCellRenderer(new NumberRightRenderer());
    }
    
    private class NumberRightRenderer extends DefaultTableCellRenderer {
        public NumberRightRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            // col is right aligned, give some space to next column
            setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 3));
            return this;
        }
    }
}
