/*
  SendMessagesTableFormat.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.gui.sentmessages;

import java.awt.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.*;
import frost.fileTransfer.common.*;
import frost.gui.unsentmessages.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

public class SentMessagesTableFormat extends SortedTableFormat implements LanguageListener, PropertyChangeListener {

    private Language language;

    private final static int COLUMN_COUNT = 5;

    private SortedModelTable modelTable;
    
    private boolean showColoredLines;

    public SentMessagesTableFormat() {
        super(COLUMN_COUNT);

        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();

        setComparator(new BoardComparator(), 0);
        setComparator(new SubjectComparator(), 1);
        setComparator(new FromComparator(), 2);
        setComparator(new ToComparator(), 3);
        setComparator(new DateComparator(), 4);
        
        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
        Core.frostSettings.addPropertyChangeListener(this);
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("SentMessages.table.board"));
        setColumnName(1, language.getString("SentMessages.table.subject"));
        setColumnName(2, language.getString("SentMessages.table.from"));
        setColumnName(3, language.getString("SentMessages.table.to"));
        setColumnName(4, language.getString("SentMessages.table.date"));

        refreshColumnNames();
    }

    public Object getCellValue(ModelItem item, int columnIndex) {
        SentMessagesTableItem searchItem = (SentMessagesTableItem) item;
        switch (columnIndex) {
            case 0 :
                return searchItem.getBoardName();
            case 1 :
                return searchItem.getSubject();

            case 2 :
                return searchItem.getFrom();

            case 3 :
                return searchItem.getTo();

            case 4 :
                return searchItem.getDateAndTimeString();

            default:
                return "**ERROR**";
        }
    }

    public int[] getColumnNumbers(int fieldID) {
        return new int[] {};
    }

    public void customizeTable(ModelTable lModelTable) {
        super.customizeTable(lModelTable);
        
        modelTable = (SortedModelTable) lModelTable;
        
        modelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        TableColumnModel columnModel = modelTable.getTable().getColumnModel();
        
        ShowContentTooltipRenderer tooltipRenderer = new ShowContentTooltipRenderer();

        columnModel.getColumn(0).setCellRenderer(tooltipRenderer);
        columnModel.getColumn(1).setCellRenderer(new SubjectRenderer());
        columnModel.getColumn(2).setCellRenderer(tooltipRenderer);
        columnModel.getColumn(3).setCellRenderer(tooltipRenderer);
        columnModel.getColumn(4).setCellRenderer(new ShowColoredLinesRenderer());
        
        // Sets the relative widths of the columns
        if( !loadTableLayout(columnModel) ) {
            int[] widths = { 60, 150, 60, 60, 70 };
            for (int i = 0; i < widths.length; i++) {
                columnModel.getColumn(i).setPreferredWidth(widths[i]);
            }
        }
        
        modelTable.setSortedColumn(4, false); // init: sort by date, descending
    }
    
    public void saveTableLayout() {
        TableColumnModel tcm = modelTable.getTable().getColumnModel();
        for(int columnIndexInTable=0; columnIndexInTable < tcm.getColumnCount(); columnIndexInTable++) {
            TableColumn tc = tcm.getColumn(columnIndexInTable);
            int columnIndexInModel = tc.getModelIndex();
            // save the current index in table for column with the fix index in model
            Core.frostSettings.setValue("SentMessagesTable.tableindex.modelcolumn."+columnIndexInModel, columnIndexInTable);
            // save the current width of the column
            int columnWidth = tc.getWidth();
            Core.frostSettings.setValue("SentMessagesTable.columnwidth.modelcolumn."+columnIndexInModel, columnWidth);
        }
    }
    
    private boolean loadTableLayout(TableColumnModel tcm) {
        
        // load the saved tableindex for each column in model, and its saved width
        int[] tableToModelIndex = new int[tcm.getColumnCount()];
        int[] columnWidths = new int[tcm.getColumnCount()];

        for(int x=0; x < tableToModelIndex.length; x++) {
            String indexKey = "SentMessagesTable.tableindex.modelcolumn."+x;
            if( Core.frostSettings.getObjectValue(indexKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            int tableIndex = Core.frostSettings.getIntValue(indexKey);
            if( tableIndex < 0 || tableIndex >= tableToModelIndex.length ) {
                return false; // invalid table index value
            }
            tableToModelIndex[tableIndex] = x;

            String widthKey = "SentMessagesTable.columnwidth.modelcolumn."+x;
            if( Core.frostSettings.getObjectValue(widthKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            int columnWidth = Core.frostSettings.getIntValue(widthKey);
            if( columnWidth <= 0 ) {
                return false; // invalid column width
            }
            columnWidths[x] = columnWidth;
        }
        // columns are currently added in model order, remove them all and save in an array
        // while on it, set the loaded width of each column
        TableColumn[] tcms = new TableColumn[tcm.getColumnCount()];
        for(int x=tcms.length-1; x >= 0; x--) {
            tcms[x] = tcm.getColumn(x);
            tcm.removeColumn(tcms[x]);
            tcms[x].setPreferredWidth(columnWidths[x]);
        }
        // add the columns in order loaded from settings
        for(int x=0; x < tableToModelIndex.length; x++) {
            tcm.addColumn(tcms[tableToModelIndex[x]]);
        }
        return true;
    }
    
    private class DateComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String i1 = ((SentMessagesTableItem) o1).getDateAndTimeString();
            String i2 = ((SentMessagesTableItem) o2).getDateAndTimeString();
            return i1.compareTo(i2);
        }
    }

    private class ToComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String i1 = ((SentMessagesTableItem) o1).getTo();
            String i2 = ((SentMessagesTableItem) o2).getTo();
            return i1.compareTo(i2);
        }
    }

    private class FromComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String i1 = ((SentMessagesTableItem) o1).getFrom();
            String i2 = ((SentMessagesTableItem) o2).getFrom();
            return i1.compareTo(i2);
        }
    }
    
    private class SubjectComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String i1 = ((SentMessagesTableItem) o1).getSubject();
            String i2 = ((SentMessagesTableItem) o2).getSubject();
            return i1.compareTo(i2);
        }
    }

    private class BoardComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String i1 = ((UnsentMessagesTableItem) o1).getBoardName();
            String i2 = ((UnsentMessagesTableItem) o2).getBoardName();
            return i1.compareTo(i2);
        }
    }

    private class SubjectRenderer extends ShowContentTooltipRenderer {
        public SubjectRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) 
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if( !isSelected ) {
                setForeground(Color.BLACK);
                SentMessagesTableItem item = (SentMessagesTableItem) modelTable.getItemAt(row);
                if( item != null ) {
                    if( item.getFrostMessageObject().containsAttachments() ) {
                        setForeground(Color.BLUE);
                    }
                }
            }
            return this;
        }
    }

    private class ShowContentTooltipRenderer extends ShowColoredLinesRenderer {
        public ShowContentTooltipRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) 
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
    
    private class ShowColoredLinesRenderer extends DefaultTableCellRenderer {
        public ShowColoredLinesRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) 
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                setBackground(newBackground);
            } else {
                setBackground(table.getSelectionBackground());
            }
            return this;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.SHOW_COLORED_ROWS)) {
            showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
            modelTable.fireTableDataChanged();
        }
    }
}
