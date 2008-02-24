/*
  SearchItemPropertiesTableFormat.java / Frost
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
package frost.fileTransfer.common;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.*;
import frost.gui.*;
import frost.identities.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;

public class FileListFileDetailsTableFormat extends SortedTableFormat implements LanguageListener {

    private static final String CFGKEY_SORTSTATE_SORTEDCOLUMN = "FileListFileDetailsDialog.sortState.sortedColumn";
    private static final String CFGKEY_SORTSTATE_SORTEDASCENDING = "FileListFileDetailsDialog.sortState.sortedAscending";
    private static final String CFGKEY_COLUMN_TABLEINDEX = "FileListFileDetailsDialog.tableindex.modelcolumn.";
    private static final String CFGKEY_COLUMN_WIDTH = "FileListFileDetailsDialog.columnwidth.modelcolumn.";

    private String stateNever;
    private String unknown;

    private final Language language = Language.getInstance();;

    private static ImageIcon ICON_GOOD = null;
    private static ImageIcon ICON_OBSERVE = null;
    private static ImageIcon ICON_CHECK = null;
    private static ImageIcon ICON_BAD = null;

    private final static int COLUMN_COUNT = 9;

    private final boolean showColoredLines;

    public FileListFileDetailsTableFormat() {
        super(COLUMN_COUNT);

        refreshLanguage();

        setComparator(new FileNameComparator(), 0);
        setComparator(new OwnerComparator(), 1);
        setComparator(new IdentityStateComparator(), 2);
        setComparator(new RatingComparator(), 3);
        setComparator(new CommentComparator(), 4);
        setComparator(new KeywordsComparator(), 5);
        setComparator(new LastUploadedComparator(), 6);
        setComparator(new LastReceivedComparator(), 7);
        setComparator(new KeyComparator(), 8);

        loadIcons();

        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
    }

    private void loadIcons() {
        if( ICON_GOOD == null ) {
            // load all icons
            ICON_GOOD = MiscToolkit.getScaledImage("/data/trust.gif", 12, 12);
            ICON_OBSERVE = MiscToolkit.getScaledImage("/data/observe.gif", 12, 12);
            ICON_CHECK = MiscToolkit.getScaledImage("/data/check.gif", 12, 12);
            ICON_BAD = MiscToolkit.getScaledImage("/data/nottrust.gif", 12, 12);
        }
    }

    public void languageChanged(final LanguageEvent event) {
        refreshLanguage();
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("FileListFileDetailsDialog.table.filename"));
        setColumnName(1, language.getString("FileListFileDetailsDialog.table.owner"));
        setColumnName(2, language.getString("FileListFileDetailsDialog.table.trustState"));
        setColumnName(3, language.getString("FileListFileDetailsDialog.table.rating"));
        setColumnName(4, language.getString("FileListFileDetailsDialog.table.comment"));
        setColumnName(5, language.getString("FileListFileDetailsDialog.table.keywords"));
        setColumnName(6, language.getString("FileListFileDetailsDialog.table.lastUploaded"));
        setColumnName(7, language.getString("FileListFileDetailsDialog.table.lastReceived"));
        setColumnName(8, language.getString("FileListFileDetailsDialog.table.key"));

        stateNever = language.getString("FileListFileDetailsDialog.table.state.never");
        unknown =    language.getString("FileListFileDetailsDialog.table.state.unknown");

        refreshColumnNames();
    }

    public Object getCellValue(final ModelItem item, final int columnIndex) {
        if( item == null ) {
            return "*null*";
        }
        final FileListFileDetailsItem detailsItem = (FileListFileDetailsItem) item;
        switch (columnIndex) {
            case 0 :    // filename
                return detailsItem.getFileOwner().getName();

            case 1 :    // owner
                return detailsItem.getFileOwner().getOwner();

            case 2 :    // state
                return detailsItem.getOwnerIdentity();

            case 3 :    // rating
                return RatingStringProvider.getRatingString( detailsItem.getFileOwner().getRating() );

            case 4 :    // comment
                return detailsItem.getDisplayComment();

            case 5 :    // keyword
                return detailsItem.getDisplayKeywords();

            case 6 :    // lastUploaded
                if( detailsItem.getDisplayLastUploaded().length() == 0 ) {
                    return stateNever;
                } else {
                    return detailsItem.getDisplayLastUploaded();
                }

            case 7 :    // lastReceived
                if( detailsItem.getDisplayLastReceived().length() == 0 ) {
                    return stateNever;
                } else {
                    return detailsItem.getDisplayLastReceived();
                }

            case 8 :    // key
                if( detailsItem.getKey() == null ) {
                    return unknown;
                } else {
                    return detailsItem.getKey();
                }

            default:
                return "**ERROR**";
        }
    }

    public int[] getColumnNumbers(final int fieldID) {
        return new int[] {};
    }

    @Override
    public void customizeTable(final ModelTable table) {
        super.customizeTable(table);

        final SortedModelTable modelTable = (SortedModelTable)table;

        modelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        if( Core.frostSettings.getBoolValue(SettingsClass.SAVE_SORT_STATES)
                && Core.frostSettings.getObjectValue(CFGKEY_SORTSTATE_SORTEDCOLUMN) != null
                && Core.frostSettings.getObjectValue(CFGKEY_SORTSTATE_SORTEDASCENDING) != null )
        {
            final int sortedColumn = Core.frostSettings.getIntValue(CFGKEY_SORTSTATE_SORTEDCOLUMN);
            final boolean isSortedAsc = Core.frostSettings.getBoolValue(CFGKEY_SORTSTATE_SORTEDASCENDING);
            if( sortedColumn > -1 ) {
                modelTable.setSortedColumn(sortedColumn, isSortedAsc);
            }
        } else {
            modelTable.setSortedColumn(7, false);
        }

        final TableColumnModel columnModel = modelTable.getTable().getColumnModel();

        final ShowContentTooltipRenderer showContentTooltipRenderer = new ShowContentTooltipRenderer();
        final ShowColoredLinesRenderer showColoredLinesRenderer = new ShowColoredLinesRenderer();

        columnModel.getColumn(0).setCellRenderer(showContentTooltipRenderer); // filename
        columnModel.getColumn(1).setCellRenderer(showContentTooltipRenderer); // owner
        columnModel.getColumn(2).setCellRenderer(new IdentityStateRenderer()); // id state
        columnModel.getColumn(3).setCellRenderer(showColoredLinesRenderer); // rating
        columnModel.getColumn(4).setCellRenderer(showContentTooltipRenderer); // comment
        columnModel.getColumn(5).setCellRenderer(showContentTooltipRenderer); // keywords
        columnModel.getColumn(6).setCellRenderer(showColoredLinesRenderer); // last uploaded
        columnModel.getColumn(7).setCellRenderer(showColoredLinesRenderer); // last received
        columnModel.getColumn(8).setCellRenderer(showContentTooltipRenderer); // key

        if( !loadTableLayout(columnModel) ) {
            // Sets the relative widths of the columns
            final int[] widths = { 150, 80, 20, 20, 80, 80, 55, 55, 55 };
            for (int i = 0; i < widths.length; i++) {
                columnModel.getColumn(i).setPreferredWidth(widths[i]);
            }
        }
    }

    public void saveTableLayout(final ModelTable table) {

        final SortedModelTable modelTable = (SortedModelTable)table;

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

    private class FileNameComparator implements Comparator<FileListFileDetailsItem> {
        public int compare(final FileListFileDetailsItem item1, final FileListFileDetailsItem item2) {
            return item1.getFileOwner().getName().compareToIgnoreCase(item2.getFileOwner().getName());
        }
    }

    private class OwnerComparator implements Comparator<FileListFileDetailsItem> {
        public int compare(final FileListFileDetailsItem item1, final FileListFileDetailsItem item2) {
            return item1.getFileOwner().getOwner().compareToIgnoreCase(item2.getFileOwner().getOwner());
        }
    }

    private class IdentityStateComparator implements Comparator<FileListFileDetailsItem> {
        public int compare(final FileListFileDetailsItem item1, final FileListFileDetailsItem item2) {
            final Integer i1 = new Integer(item1.getOwnerIdentity().getState());
            final Integer i2 = new Integer(item2.getOwnerIdentity().getState());
            return i1.compareTo(i2);
        }
    }

    private class RatingComparator implements Comparator<FileListFileDetailsItem> {
        public int compare(final FileListFileDetailsItem o1, final FileListFileDetailsItem o2) {
            final int val1 = o1.getFileOwner().getRating();
            final int val2 = o2.getFileOwner().getRating();
            return new Long(val1).compareTo(new Long(val2));
        }
    }

    private class CommentComparator implements Comparator<FileListFileDetailsItem> {
        public int compare(final FileListFileDetailsItem item1, final FileListFileDetailsItem item2) {
            return item1.getDisplayComment().compareToIgnoreCase(item2.getDisplayComment());
        }
    }

    private class KeywordsComparator implements Comparator<FileListFileDetailsItem> {
        public int compare(final FileListFileDetailsItem item1, final FileListFileDetailsItem item2) {
            return item1.getDisplayKeywords().compareToIgnoreCase(item2.getDisplayKeywords());
        }
    }

    private class LastUploadedComparator implements Comparator<FileListFileDetailsItem> {
        public int compare(final FileListFileDetailsItem o1, final FileListFileDetailsItem o2) {
            final long val1 = o1.getFileOwner().getLastUploaded();
            final long val2 = o2.getFileOwner().getLastUploaded();
            return new Long(val1).compareTo(new Long(val2));
        }
    }

    private class LastReceivedComparator implements Comparator<FileListFileDetailsItem> {
        public int compare(final FileListFileDetailsItem o1, final FileListFileDetailsItem o2) {
            final long val1 = o1.getFileOwner().getLastReceived();
            final long val2 = o2.getFileOwner().getLastReceived();
            return new Long(val1).compareTo(new Long(val2));
        }
    }
    private class KeyComparator implements Comparator<FileListFileDetailsItem> {
        public int compare(final FileListFileDetailsItem o1, final FileListFileDetailsItem o2) {
            String key1 = o1.getKey();
            String key2 = o2.getKey();
            if (key1 == null) {
                key1 = unknown;
            }
            if (key2 == null) {
                key2 = unknown;
            }
            return key1.compareToIgnoreCase(key2);
        }
    }

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

    private class IdentityStateRenderer extends ShowColoredLinesRenderer {
        public IdentityStateRenderer() {
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

            final Identity id = (Identity) value;
            if( id == null ) {
                setIcon(null);
            } else if( id.isGOOD() ) {
                setIcon(ICON_GOOD);
            } else if( id.isOBSERVE() ) {
                setIcon(ICON_OBSERVE);
            } else if( id.isCHECK() ) {
                setIcon(ICON_CHECK);
            } else if( id.isBAD() ) {
                setIcon(ICON_BAD);
            } else {
                setIcon(null);
            }
            setText("");
            return this;
        }
    }

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
}
