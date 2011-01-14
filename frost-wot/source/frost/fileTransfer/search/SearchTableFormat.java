/*
  SearchTableFormat.java / Frost
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
package frost.fileTransfer.search;

import java.awt.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import frost.*;
import frost.fileTransfer.common.*;
import frost.gui.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

public class SearchTableFormat extends SortedTableFormat<FrostSearchItem> implements LanguageListener, PropertyChangeListener {

    private static final String CFGKEY_SORTSTATE_SORTEDCOLUMN = "SearchFilesTable.sortState.sortedColumn";
    private static final String CFGKEY_SORTSTATE_SORTEDASCENDING = "SearchFilesTable.sortState.sortedAscending";
    private static final String CFGKEY_COLUMN_TABLEINDEX = "SearchFilesTable.tableindex.modelcolumn.";
    private static final String CFGKEY_COLUMN_WIDTH = "SearchFilesTable.columnwidth.modelcolumn.";

    private static final ImageIcon hasMoreInfoIcon = MiscToolkit.loadImageIcon("/data/info.png");

    private final Language language;

    private final static int COLUMN_COUNT = 9;

    private String offline;
    private String sharing;
    private String downloading;
    private String downloaded;

    private String sourceCountTooltip;

    private SortedModelTable<FrostSearchItem> modelTable;

    private boolean showColoredLines;

    public SearchTableFormat() {
        super(COLUMN_COUNT);

        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();

        setComparator(SearchTableComparators.getFileNameComparator(), 0);
        setComparator(SearchTableComparators.getSizeComparator(), 1);
        setComparator(SearchTableComparators.getStateComparator(), 2);
        setComparator(SearchTableComparators.getLastUploadedComparator(), 3);
        setComparator(SearchTableComparators.getLastReceivedComparator(), 4);
        setComparator(SearchTableComparators.getRatingComparator(), 5);
        setComparator(SearchTableComparators.getCommentComparator(), 6);
        setComparator(SearchTableComparators.getKeywordsComparator(), 7);
        setComparator(SearchTableComparators.getSourcesComparator(), 8);

        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
        Core.frostSettings.addPropertyChangeListener(this);
    }

    public void languageChanged(final LanguageEvent event) {
        refreshLanguage();
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("SearchPane.resultTable.filename"));
        setColumnName(1, language.getString("SearchPane.resultTable.size"));
        setColumnName(2, language.getString("SearchPane.resultTable.state"));
        setColumnName(3, language.getString("SearchPane.resultTable.lastUploaded"));
        setColumnName(4, language.getString("SearchPane.resultTable.lastReceived"));
        setColumnName(5, language.getString("SearchPane.resultTable.rating"));
        setColumnName(6, language.getString("SearchPane.resultTable.comment"));
        setColumnName(7, language.getString("SearchPane.resultTable.keywords"));
        setColumnName(8, language.getString("SearchPane.resultTable.sources"));

        offline =     language.getString("SearchPane.resultTable.states.offline");
        sharing =     language.getString("SearchPane.resultTable.states.sharing");
        downloading = language.getString("SearchPane.resultTable.states.downloading");
        downloaded =  language.getString("SearchPane.resultTable.states.downloaded");

        sourceCountTooltip = language.getString("SearchPane.resultTable.sources.tooltip");

        refreshColumnNames();
    }

    public Object getCellValue(final ModelItem item, final int columnIndex) {
        if( item == null ) {
            return "*null*";
        }
        final FrostSearchItem searchItem = (FrostSearchItem) item;
        switch (columnIndex) {
            case 0 :    //Filename
                return searchItem.getFileName();

            case 1 :    //Size
                return FormatterUtils.formatSize(searchItem.getSize().longValue());

            case 2 :    //State
                return getStateStr(searchItem.getState());

            case 3 :    //lastUploaded
                return searchItem.getLastUploadedStr();

            case 4 :    //lastReceived (=lastSeen)
                return searchItem.getLastReceivedString();

            case 5 :    //rating
                return RatingStringProvider.getRatingString(searchItem.getRating().intValue());

            case 6 :    //comment
                return searchItem.getComment();

            case 7 :    // keyword
                return searchItem.getKeywords();

            case 8 :    //sources
                return searchItem.getSourceCount();

            default:
                return "**ERROR**";
        }
    }

    private String getStateStr(final int state) {
        String stateString = "";
        switch (state) {
            case FrostSearchItem.STATE_OFFLINE :
                stateString = offline;
                break;

            case FrostSearchItem.STATE_SHARING :
                stateString = sharing;
                break;

            case FrostSearchItem.STATE_DOWNLOADING :
                stateString = downloading;
                break;

            case FrostSearchItem.STATE_DOWNLOADED :
                stateString = downloaded;
                break;
        }
        return stateString;
    }

    public int[] getColumnNumbers(final int fieldID) {
        return new int[] {};
    }

    @Override
    public void customizeTable(final ModelTable<FrostSearchItem> lModelTable) {
        super.customizeTable(lModelTable);

        modelTable = (SortedModelTable<FrostSearchItem>) lModelTable;

        modelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        modelTable.setSortedColumn(0, true);

        final TableColumnModel columnModel = modelTable.getTable().getColumnModel();

        final RightAlignRenderer rightAlignRenderer = new RightAlignRenderer();
        final ShowColoredLinesRenderer showColoredLinesRenderer = new ShowColoredLinesRenderer();
        final ShowContentTooltipRenderer showContentTooltipRenderer = new ShowContentTooltipRenderer();

        columnModel.getColumn(0).setCellRenderer(new FileNameRenderer()); // filename
        columnModel.getColumn(1).setCellRenderer(rightAlignRenderer); // size
        columnModel.getColumn(2).setCellRenderer(showColoredLinesRenderer); // age
        columnModel.getColumn(3).setCellRenderer(showColoredLinesRenderer); // last uploaded
        columnModel.getColumn(4).setCellRenderer(showColoredLinesRenderer); // last received
        columnModel.getColumn(5).setCellRenderer(showColoredLinesRenderer); // rating
        columnModel.getColumn(6).setCellRenderer(showContentTooltipRenderer); // comment
        columnModel.getColumn(7).setCellRenderer(showContentTooltipRenderer); // keywords
        columnModel.getColumn(8).setCellRenderer(new SourceCountRenderer()); // source count

        if( !loadTableLayout(columnModel) ) {
            final int[] widths = { 250, 30, 40, 20, 20, 10, 50, 80, 15 };
            for (int i = 0; i < widths.length; i++) {
                columnModel.getColumn(i).setPreferredWidth(widths[i]);
            }
        }

        // add change listeners for column resizes and column moves
        columnModel.addColumnModelListener(new TableColumnModelListener() {
            public void columnMarginChanged(final ChangeEvent e) {
                saveTableLayout();
            }
            public void columnMoved(final TableColumnModelEvent e) {
                if( e.getFromIndex() != e.getToIndex() ) {
                    saveTableLayout();
                }
            }
            public void columnAdded(final TableColumnModelEvent e) {}
            public void columnRemoved(final TableColumnModelEvent e) {}
            public void columnSelectionChanged(final ListSelectionEvent e) {}
        });
    }

    private void saveTableLayout() {
        final TableColumnModel tcm = modelTable.getTable().getColumnModel();
        for(int columnIndexInTable=0; columnIndexInTable < tcm.getColumnCount(); columnIndexInTable++) {
            final TableColumn tc = tcm.getColumn(columnIndexInTable);
            final int columnIndexInModel = tc.getModelIndex();
            // save the current index in table for column with the fix index in model
            Core.frostSettings.setValue(CFGKEY_COLUMN_TABLEINDEX + columnIndexInModel, columnIndexInTable);
            // save the current width of the column
            final int columnWidth = tc.getWidth();
            Core.frostSettings.setValue(CFGKEY_COLUMN_WIDTH + columnIndexInModel, columnWidth);
        }

        if( Core.frostSettings.getBoolValue(SettingsClass.SAVE_SORT_STATES) && modelTable.getSortedColumn() > -1 ) {
            final int sortedColumn = modelTable.getSortedColumn();
            final boolean isSortedAsc = modelTable.isSortedAscending();
            Core.frostSettings.setValue(CFGKEY_SORTSTATE_SORTEDCOLUMN, sortedColumn);
            Core.frostSettings.setValue(CFGKEY_SORTSTATE_SORTEDASCENDING, isSortedAsc);
        }
    }

    private boolean loadTableLayout(final TableColumnModel tcm) {

        // load the saved tableindex for each column in model, and its saved width
        final int[] tableToModelIndex = new int[tcm.getColumnCount()];
        final int[] columnWidths = new int[tcm.getColumnCount()];

        for(int x=0; x < tableToModelIndex.length; x++) {
            final String indexKey = CFGKEY_COLUMN_TABLEINDEX + x;
            if( Core.frostSettings.getObjectValue(indexKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            final int tableIndex = Core.frostSettings.getIntValue(indexKey);
            if( tableIndex < 0 || tableIndex >= tableToModelIndex.length ) {
                return false; // invalid table index value
            }
            tableToModelIndex[tableIndex] = x;

            final String widthKey = CFGKEY_COLUMN_WIDTH + x;
            if( Core.frostSettings.getObjectValue(widthKey) == null ) {
                return false; // column not found, abort
            }
            // build array of table to model associations
            final int columnWidth = Core.frostSettings.getIntValue(widthKey);
            if( columnWidth <= 0 ) {
                return false; // invalid column width
            }
            columnWidths[x] = columnWidth;
        }
        // columns are currently added in model order, remove them all and save in an array
        // while on it, set the loaded width of each column
        final TableColumn[] tcms = new TableColumn[tcm.getColumnCount()];
        for(int x=tcms.length-1; x >= 0; x--) {
            tcms[x] = tcm.getColumn(x);
            tcm.removeColumn(tcms[x]);
            tcms[x].setPreferredWidth(columnWidths[x]);
        }
        // add the columns in order loaded from settings
        for( final int element : tableToModelIndex ) {
            tcm.addColumn(tcms[element]);
        }
        return true;
    }

    @SuppressWarnings("serial")
	private class ShowContentTooltipRenderer extends ShowColoredLinesRenderer {
        public ShowContentTooltipRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column)
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
	private class RightAlignRenderer extends ShowColoredLinesRenderer {
        final javax.swing.border.EmptyBorder border = new javax.swing.border.EmptyBorder(0, 0, 0, 3);
        public RightAlignRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            // col is right aligned, give some space to next column
            setBorder(border);
            return this;
        }
    }

    /**
     * This renderer renders the column "FileName" in different colors,
     * depending on state of search item.
     * States are: NONE, DOWNLOADED, DOWNLOADING, UPLOADING
     */
    @SuppressWarnings("serial")
	private class FileNameRenderer extends ShowContentTooltipRenderer {

        public FileNameRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                final ModelItem item = modelTable.getItemAt(row); //It may be null
                if (item != null) {
                    final FrostSearchItem searchItem = (FrostSearchItem) item;

                    if (searchItem.getState() == FrostSearchItem.STATE_DOWNLOADED) {
                        setForeground(Color.LIGHT_GRAY);
                    } else if (searchItem.getState() == FrostSearchItem.STATE_DOWNLOADING) {
                        setForeground(Color.BLUE);
                    } else if (searchItem.getState() == FrostSearchItem.STATE_SHARING) {
                        setForeground(Color.MAGENTA);
                    } else if (searchItem.getState() == FrostSearchItem.STATE_OFFLINE) {
                        setForeground(Color.DARK_GRAY);
                    } else {
                        // normal item, drawn in black
                        setForeground(Color.BLACK);
                    }
                } else {
                    return this;
                }
            }
            return this;
        }
    }

    @SuppressWarnings("serial")
	private class SourceCountRenderer extends ShowColoredLinesRenderer {

        final javax.swing.border.EmptyBorder border = new javax.swing.border.EmptyBorder(0, 0, 0, 3);

        public SourceCountRenderer() {
            super();
        }
        @Override
        public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(SwingConstants.RIGHT);
            // col is right aligned, give some space to next column
            setBorder(border);

            final ModelItem item = modelTable.getItemAt(row); //It may be null
            if (item != null) {
                final FrostSearchItem searchItem = (FrostSearchItem) item;
                if( searchItem.hasInfosFromMultipleSources().booleanValue() ) {
                    setIcon(hasMoreInfoIcon);
                } else {
                    setIcon(null);
                }
            } else {
                setIcon(null);
            }
            setToolTipText(sourceCountTooltip);
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
            final JTable table,
            final Object value,
            boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                final Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                setBackground(newBackground);
            } else {
                setBackground(table.getSelectionBackground());
            }
            return this;
        }
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.SHOW_COLORED_ROWS)) {
            showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
            modelTable.fireTableDataChanged();
        }
    }
}
