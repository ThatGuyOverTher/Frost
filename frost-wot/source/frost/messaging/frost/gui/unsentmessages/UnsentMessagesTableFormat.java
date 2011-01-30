/*
  UnsentMessagesTableFormat.java / Frost
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
package frost.messaging.frost.gui.unsentmessages;

import java.awt.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.*;
import frost.fileTransfer.common.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

public class UnsentMessagesTableFormat extends SortedTableFormat<UnsentMessagesTableItem> implements LanguageListener, PropertyChangeListener {

    private static final String CFGKEY_SORTSTATE_SORTEDCOLUMN = "UnsentMessagesTable.sortState.sortedColumn";
    private static final String CFGKEY_SORTSTATE_SORTEDASCENDING = "UnsentMessagesTable.sortState.sortedAscending";
    private static final String CFGKEY_COLUMN_TABLEINDEX = "UnsentMessagesTable.tableindex.modelcolumn.";
    private static final String CFGKEY_COLUMN_WIDTH = "UnsentMessagesTable.columnwidth.modelcolumn.";

    private Language language;

    private final static int COLUMN_COUNT = 6;

    private SortedModelTable<UnsentMessagesTableItem> modelTable;
    
    private String stateWaitingString;
    private String stateUploadingString;

    private boolean showColoredLines;

    public UnsentMessagesTableFormat() {
        super(COLUMN_COUNT);

        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();

        setComparator(new BoardComparator(), 0);
        setComparator(new SubjectComparator(), 1);
        setComparator(new FromComparator(), 2);
        setComparator(new ToComparator(), 3);
        setComparator(new StateComparator(), 4);
        setComparator(new DateComparator(), 5);
        
        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
        Core.frostSettings.addPropertyChangeListener(this);
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("UnsentMessages.table.board"));
        setColumnName(1, language.getString("UnsentMessages.table.subject"));
        setColumnName(2, language.getString("UnsentMessages.table.from"));
        setColumnName(3, language.getString("UnsentMessages.table.to"));
        setColumnName(4, language.getString("UnsentMessages.table.state"));
        setColumnName(5, language.getString("UnsentMessages.table.timeAdded"));
        
        stateWaitingString = language.getString("UnsentMessages.table.stateWaiting");
        stateUploadingString = language.getString("UnsentMessages.table.stateUploading");

        refreshColumnNames();
    }

    public Object getCellValue(UnsentMessagesTableItem searchItem, int columnIndex) {
        if( searchItem == null ) {
            return "*null*";
        }
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
                if( searchItem.getState() == UnsentMessagesTableItem.STATE_UPLOADING ) {
                    return stateUploadingString;
                } else {
                    return stateWaitingString;
                }

            case 5 :
                return searchItem.getTimeAddedString();

            default:
                return "**ERROR**";
        }
    }

    public int[] getColumnNumbers(int fieldID) {
        return new int[] {};
    }

    @Override
    public void customizeTable(ModelTable<UnsentMessagesTableItem> lModelTable) {
        super.customizeTable(lModelTable);
        
        modelTable = (SortedModelTable<UnsentMessagesTableItem>) lModelTable;
        
        modelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        TableColumnModel columnModel = modelTable.getTable().getColumnModel();
        
        ShowContentTooltipRenderer tooltipRenderer = new ShowContentTooltipRenderer();
        ShowColoredLinesRenderer showColoredLinesRenderer = new ShowColoredLinesRenderer();

        columnModel.getColumn(0).setCellRenderer(tooltipRenderer);
        columnModel.getColumn(1).setCellRenderer(new SubjectRenderer());
        columnModel.getColumn(2).setCellRenderer(tooltipRenderer);
        columnModel.getColumn(3).setCellRenderer(tooltipRenderer);
        columnModel.getColumn(4).setCellRenderer(showColoredLinesRenderer);
        columnModel.getColumn(5).setCellRenderer(showColoredLinesRenderer);
        
        // Sets the relative widths of the columns
        if( !loadTableLayout(columnModel) ) {
            int[] widths = { 60, 150, 60, 60, 40, 70 };
            for (int i = 0; i < widths.length; i++) {
                columnModel.getColumn(i).setPreferredWidth(widths[i]);
            }
        }
        
        if( Core.frostSettings.getBoolValue(SettingsClass.SAVE_SORT_STATES)
                && Core.frostSettings.getObjectValue(CFGKEY_SORTSTATE_SORTEDCOLUMN) != null
                && Core.frostSettings.getObjectValue(CFGKEY_SORTSTATE_SORTEDASCENDING) != null )
        {
            int sortedColumn = Core.frostSettings.getIntValue(CFGKEY_SORTSTATE_SORTEDCOLUMN);
            boolean isSortedAsc = Core.frostSettings.getBoolValue(CFGKEY_SORTSTATE_SORTEDASCENDING);
            if( sortedColumn > -1 ) {
                modelTable.setSortedColumn(sortedColumn, isSortedAsc);
            }
        } else {
            modelTable.setSortedColumn(5, false); // init: sort by timeAdded, descending
        }
    }
    
    public void saveTableLayout() {
        TableColumnModel tcm = modelTable.getTable().getColumnModel();
        for(int columnIndexInTable=0; columnIndexInTable < tcm.getColumnCount(); columnIndexInTable++) {
            TableColumn tc = tcm.getColumn(columnIndexInTable);
            int columnIndexInModel = tc.getModelIndex();
            // save the current index in table for column with the fix index in model
            Core.frostSettings.setValue(CFGKEY_COLUMN_TABLEINDEX + columnIndexInModel, columnIndexInTable);
            // save the current width of the column
            int columnWidth = tc.getWidth();
            Core.frostSettings.setValue(CFGKEY_COLUMN_WIDTH + columnIndexInModel, columnWidth);
        }
        
        if( Core.frostSettings.getBoolValue(SettingsClass.SAVE_SORT_STATES) && modelTable.getSortedColumn() > -1 ) {
            int sortedColumn = modelTable.getSortedColumn();
            boolean isSortedAsc = modelTable.isSortedAscending();
            Core.frostSettings.setValue(CFGKEY_SORTSTATE_SORTEDCOLUMN, sortedColumn);
            Core.frostSettings.setValue(CFGKEY_SORTSTATE_SORTEDASCENDING, isSortedAsc);
        }
    }
    
    private boolean loadTableLayout(TableColumnModel tcm) {
        
        // load the saved tableindex for each column in model, and its saved width
        int[] tableToModelIndex = new int[tcm.getColumnCount()];
        int[] columnWidths = new int[tcm.getColumnCount()];

        for(int x=0; x < tableToModelIndex.length; x++) {
            String indexKey = CFGKEY_COLUMN_TABLEINDEX + x;
            if( Core.frostSettings.getObjectValue(indexKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            int tableIndex = Core.frostSettings.getIntValue(indexKey);
            if( tableIndex < 0 || tableIndex >= tableToModelIndex.length ) {
                return false; // invalid table index value
            }
            tableToModelIndex[tableIndex] = x;

            String widthKey = CFGKEY_COLUMN_WIDTH + x;
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
    
    private class DateComparator implements Comparator<UnsentMessagesTableItem> {
    	public int compare(UnsentMessagesTableItem left, UnsentMessagesTableItem right) {
    		return left.getTimeAddedString().compareTo(right.getTimeAddedString());
    	}
    }

    private class ToComparator implements Comparator<UnsentMessagesTableItem> {
    	public int compare(UnsentMessagesTableItem left, UnsentMessagesTableItem right) {
    		return left.getTo().compareTo(right.getTo());
    	}
    }

    private class FromComparator implements Comparator<UnsentMessagesTableItem> {
    	public int compare(UnsentMessagesTableItem left, UnsentMessagesTableItem right) {
    		return left.getFrom().compareTo(right.getFrom());
    	}
    }

    private class StateComparator implements Comparator<UnsentMessagesTableItem> {
    	public int compare(UnsentMessagesTableItem left, UnsentMessagesTableItem right) {
    		return new Integer(left.getState()).compareTo(new Integer(right.getState()));
    	}
    }

    private class SubjectComparator implements Comparator<UnsentMessagesTableItem> {
    	public int compare(UnsentMessagesTableItem left, UnsentMessagesTableItem right) {
    		return left.getSubject().compareTo(right.getSubject());
    	}
    }

    private class BoardComparator implements Comparator<UnsentMessagesTableItem> {
    	public int compare(UnsentMessagesTableItem left, UnsentMessagesTableItem right) {
    		return left.getBoardName().compareTo(right.getBoardName());
    	}
    }

    @SuppressWarnings("serial")
	private class SubjectRenderer extends ShowColoredLinesRenderer {
        public SubjectRenderer() {
            super();
        }
        @Override
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
                UnsentMessagesTableItem item = (UnsentMessagesTableItem) modelTable.getItemAt(row);
                if( item != null ) {
                    if( item.getFrostUnsentMessageObject().containsAttachments() ) {
                        setForeground(Color.BLUE);
                    }
                }
            }
            
            return this;
        }
    }

    @SuppressWarnings("serial")
	private class ShowContentTooltipRenderer extends ShowColoredLinesRenderer {
        public ShowContentTooltipRenderer() {
            super();
        }
        @Override
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
    
    @SuppressWarnings("serial")
	private class ShowColoredLinesRenderer extends DefaultTableCellRenderer {
        public ShowColoredLinesRenderer() {
            super();
        }
        @Override
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
