/*
  UploadTableFormat.java / Frost
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
package frost.fileTransfer.upload;

import java.awt.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.*;
import frost.fileTransfer.common.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

class UploadTableFormat extends SortedTableFormat implements LanguageListener, PropertyChangeListener {

    private static final String CFGKEY_SORTSTATE_SORTEDCOLUMN = "UploadTable.sortState.sortedColumn";
    private static final String CFGKEY_SORTSTATE_SORTEDASCENDING = "UploadTable.sortState.sortedAscending";
    private static final String CFGKEY_COLUMN_TABLEINDEX = "UploadTable.tableindex.modelcolumn.";
    private static final String CFGKEY_COLUMN_WIDTH = "UploadTable.columnwidth.modelcolumn.";

    private static ImageIcon isSharedIcon = new ImageIcon((MainFrame.class.getResource("/data/shared.png")));

    private SortedModelTable modelTable = null;
    
    private boolean showColoredLines;
    
    /**
     * Renders DONE with green background and FAILED with red background.
     */
    private class BaseRenderer extends DefaultTableCellRenderer {
        public BaseRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if( !isSelected ) {
                Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
                
                ModelItem item = modelTable.getItemAt(row);
                if (item != null) {
                    FrostUploadItem uploadItem = (FrostUploadItem) item;
                    int itemState = uploadItem.getState();
                    if( itemState == FrostUploadItem.STATE_DONE) {
                        newBackground = TableBackgroundColors.getBackgroundColorDone(table, row, showColoredLines);
                    } else if( itemState == FrostUploadItem.STATE_FAILED) {
                        newBackground = TableBackgroundColors.getBackgroundColorFailed(table, row, showColoredLines);
                    }
                }
                setBackground(newBackground);
                setForeground(Color.black);
            }
            return this;
        }
    }
    
    private class BlocksProgressRenderer extends JProgressBar implements TableCellRenderer {
        public BlocksProgressRenderer() {
            super();
            setMinimum(0);
            setMaximum(100);
            setStringPainted(true);
            setBorderPainted(false);
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

            Color newBackground = TableBackgroundColors.getBackgroundColor(table, row, showColoredLines);
            setBackground(newBackground);

            setValue(0);

            ModelItem item = modelTable.getItemAt(row);
            if (item != null) {
                FrostUploadItem uploadItem = (FrostUploadItem) item;
                
                int totalBlocks = uploadItem.getTotalBlocks();
                int doneBlocks = uploadItem.getDoneBlocks();
                
                if( totalBlocks > 0 ) {
                    // format: ~0% 0/60 [60]
                    
                    int percentDone = 0;

                    percentDone = (int) ((doneBlocks * 100) / totalBlocks);
                    if( percentDone > 100 ) {
                        percentDone = 100;
                    }
                    setValue(percentDone);
                }
            }
            setString(value.toString());

            return this;
        }
    }

    private class ShowContentTooltipRenderer extends BaseRenderer {
        public ShowContentTooltipRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
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

    private class ShowStateContentTooltipRenderer extends BaseRenderer {
        public ShowStateContentTooltipRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tooltip = null;
            ModelItem item = modelTable.getItemAt(row); //It may be null
            if (item != null) {
                FrostUploadItem uploadItem = (FrostUploadItem) item;
                String errorCodeDescription = uploadItem.getErrorCodeDescription();
                if( errorCodeDescription != null && errorCodeDescription.length() > 0 ) {
                    tooltip = "Last error: "+errorCodeDescription;
                }
            }
            setToolTipText(tooltip);
            return this;
        }
    }

    private class IsSharedRenderer extends BaseRenderer {
        public IsSharedRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Boolean b = (Boolean)value;
            setText("");
            if( b.booleanValue() ) {
                // show shared icon
                setIcon(isSharedIcon);
            } else {
                setIcon(null);
            }
            setToolTipText(isSharedTooltip);
            return this;
        }
    }

    /**
     * This inner class implements the renderer for the column "FileSize"
     */
    private class RightAlignRenderer extends BaseRenderer {
        final javax.swing.border.EmptyBorder border = new javax.swing.border.EmptyBorder(0, 0, 0, 3);
        public RightAlignRenderer() {
            super();
        }
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
            setBorder(border);
            return this;
        }
    }

    /**
     * This inner class implements the comparator for the column "Name"
     */
    private class NameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            return item1.getFile().getName().compareToIgnoreCase(item2.getFile().getName());
        }
    }

    /**
     * This inner class implements the comparator for the column "Last Upload"
     */
    private class StateComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            return getStateAsString(item1, item1.getState()).
                        compareToIgnoreCase(getStateAsString(item2, item2.getState()));
        }
    }

    private class BlocksComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
//          String blocks1 =
//              getBlocksAsString(
//                  item1.getTotalBlocks(),
//                  item1.getDoneBlocks(),
//                  item1.getRequiredBlocks());
//          String blocks2 =
//              getBlocksAsString(
//                  item2.getTotalBlocks(),
//                  item2.getDoneBlocks(),
//                  item2.getRequiredBlocks());
//          return blocks1.compareToIgnoreCase(blocks2); 
            return new Integer(item1.getDoneBlocks()).compareTo(new Integer(item2.getDoneBlocks()));
        }
    }

    /**
     * This inner class implements the comparator for the column "Tries"
     */
    private class TriesComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            int retries1 = ((FrostUploadItem) o1).getRetries();
            int retries2 = ((FrostUploadItem) o2).getRetries();
            return new Integer(retries1).compareTo(new Integer(retries2));
        }
    }
    
    private class IsSharedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            Boolean b1 = Boolean.valueOf( item1.isSharedFile() );
            Boolean b2 = Boolean.valueOf( item2.isSharedFile() );
            return b1.equals(b2) ? 0 : 1 ;
        }
    }

    /**
     * This inner class implements the comparator for the column "Path"
     */
    private class PathComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            return item1.getFile().getPath().compareToIgnoreCase(item2.getFile().getPath());
        }
    }

    /**
     * This inner class implements the comparator for the column "Enabled"
     */
    private class EnabledComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            return item1.isEnabled().equals(item2.isEnabled()) ? 0 : 1 ;
        }
    }

    /**
     * This inner class implements the comparator for the column "Key"
     */
    private class KeyComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String key1 = ((FrostUploadItem) o1).getKey();
            String key2 = ((FrostUploadItem) o2).getKey();
            if (key1 == null) {
                key1 = unknown;
            }
            if (key2 == null) {
                key2 = unknown;
            }
            return key1.compareToIgnoreCase(key2);
        }
    }

    /**
     * This inner class implements the comparator for the column "FileSize"
     */
    private class FileSizeComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FrostUploadItem item1 = (FrostUploadItem) o1;
            FrostUploadItem item2 = (FrostUploadItem) o2;
            if( item1.getFileSize() > item2.getFileSize() ) {
                return 1;
            } else if( item1.getFileSize() < item2.getFileSize() ) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private Language language;

    private final static int COLUMN_COUNT = 9;

    private String stateDone;
    private String stateFailed;
    private String stateUploading;
    private String stateEncodingRequested;
    private String stateEncoding;
    private String stateWaiting;

    private String unknown;
    
    private String isSharedTooltip;

    public UploadTableFormat() {
        super(COLUMN_COUNT);

        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();

        setComparator(new EnabledComparator(), 0);
        setComparator(new IsSharedComparator(), 1);
        setComparator(new NameComparator(), 2);
        setComparator(new FileSizeComparator(), 3);
        setComparator(new StateComparator(), 4);
        setComparator(new PathComparator(), 5);
        setComparator(new BlocksComparator(), 6);
        setComparator(new TriesComparator(), 7);
        setComparator(new KeyComparator(), 8);
        
        showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
        Core.frostSettings.addPropertyChangeListener(this);
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("UploadPane.fileTable.enabled"));
        setColumnName(1, language.getString("UploadPane.fileTable.shared"));
        setColumnName(2, language.getString("UploadPane.fileTable.filename"));
        setColumnName(3, language.getString("UploadPane.fileTable.size"));
        setColumnName(4, language.getString("UploadPane.fileTable.state"));
        setColumnName(5, language.getString("UploadPane.fileTable.path"));
        setColumnName(6, language.getString("UploadPane.fileTable.blocks"));
        setColumnName(7, language.getString("UploadPane.fileTable.tries"));
        setColumnName(8, language.getString("UploadPane.fileTable.key"));

        stateDone =               language.getString("UploadPane.fileTable.state.done");
        stateFailed =             language.getString("UploadPane.fileTable.state.failed");
        stateUploading =          language.getString("UploadPane.fileTable.state.uploading");
        stateEncodingRequested =  language.getString("UploadPane.fileTable.state.encodeRequested");
        stateEncoding =           language.getString("UploadPane.fileTable.state.encodingFile") + "...";
        stateWaiting =            language.getString("UploadPane.fileTable.state.waiting");
        unknown =                 language.getString("UploadPane.fileTable.state.unknown");
        
        isSharedTooltip = language.getString("UploadPane.fileTable.shared.tooltip");

        refreshColumnNames();
    }

    public void setCellValue(Object value, ModelItem item, int columnIndex) {
        FrostUploadItem uploadItem = (FrostUploadItem) item;
        switch (columnIndex) {

            case 0 : //Enabled
                Boolean valueBoolean = (Boolean) value;
                uploadItem.setEnabled(valueBoolean);
                break;

            default :
                super.setCellValue(value, item, columnIndex);
        }
    }

    public Object getCellValue(ModelItem item, int columnIndex) {

        FrostUploadItem uploadItem = (FrostUploadItem) item;
        switch (columnIndex) {

            case 0 : //Enabled
                return uploadItem.isEnabled();

            case 1 :    // isShared
                return Boolean.valueOf(uploadItem.isSharedFile());

            case 2 :    //Filename
                return uploadItem.getFile().getName();

            case 3 :    //Size
                return SizeFormatter.formatSize(uploadItem.getFileSize());

            case 4 :    // state
                return getStateAsString(uploadItem, uploadItem.getState());

            case 5 :    //Path
                return uploadItem.getFile().getPath();

            case 6 :    //blocks
                return getUploadProgress(uploadItem);

            case 7 :    //Tries
                return new Integer(uploadItem.getRetries());

            case 8 :    //Key
                if (uploadItem.getKey() == null) {
                    return unknown;
                } else {
                    return uploadItem.getKey();
                }
            default:
                return "**ERROR**";
        }
    }

    private String getStateAsString(FrostUploadItem item, int state) {
        switch (state) {

            case FrostUploadItem.STATE_UPLOADING :
                return stateUploading;

            case FrostUploadItem.STATE_PROGRESS :
                return stateUploading;
//                return getUploadProgress(item.getTotalBlocks(), item.getDoneBlocks());

            case FrostUploadItem.STATE_ENCODING_REQUESTED :
                return stateEncodingRequested;

            case FrostUploadItem.STATE_ENCODING :
                return stateEncoding;

            case FrostUploadItem.STATE_FAILED :
                return stateFailed;

            case FrostUploadItem.STATE_DONE :
                return stateDone;

            case FrostUploadItem.STATE_WAITING :
                return stateWaiting;

            default :
                return "**ERROR**";
        }
    }

    private String getUploadProgress(FrostUploadItem uploadItem) {
        
        int totalBlocks = uploadItem.getTotalBlocks(); 
        int doneBlocks = uploadItem.getDoneBlocks();
        Boolean isFinalized = uploadItem.isFinalized();
        
        // format: ~0% 0/60 [60]
        
        if( totalBlocks <= 0 ) {
            return "";
        }
        
        int percentDone = 0;
        if (totalBlocks > 0) {
            percentDone = (int) ((doneBlocks * 100) / totalBlocks);
        }
        if( percentDone > 100 ) {
            percentDone = 100;
        }
        
        StringBuffer sb = new StringBuffer();
        
        if( isFinalized != null && !isFinalized.booleanValue() ) {
            sb.append("~");
        }
        
        sb.append(percentDone).append("% ");
        sb.append(doneBlocks).append("/").append(totalBlocks).append(" [").append(totalBlocks).append("]");
        
        return sb.toString();
    }

    public void customizeTable(ModelTable lModelTable) {
        super.customizeTable(lModelTable);

        modelTable = (SortedModelTable) lModelTable;
        
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
            modelTable.setSortedColumn(2, true);
        }

        lModelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        TableColumnModel columnModel = lModelTable.getTable().getColumnModel();

        // Column "Enabled"
        columnModel.getColumn(0).setCellRenderer(BooleanCell.RENDERER);
        columnModel.getColumn(0).setCellEditor(BooleanCell.EDITOR);
        setColumnEditable(0, true);
        // hard set sizes of checkbox column
        columnModel.getColumn(0).setMinWidth(20);
        columnModel.getColumn(0).setMaxWidth(20);
        columnModel.getColumn(0).setPreferredWidth(20);
        // hard set sizes of icon column
        columnModel.getColumn(1).setMinWidth(20);
        columnModel.getColumn(1).setMaxWidth(20);
        columnModel.getColumn(1).setPreferredWidth(20);
        columnModel.getColumn(1).setCellRenderer(new IsSharedRenderer());

        RightAlignRenderer numberRightRenderer = new RightAlignRenderer();
        ShowContentTooltipRenderer showContentTooltipRenderer = new ShowContentTooltipRenderer();
        
        columnModel.getColumn(2).setCellRenderer(showContentTooltipRenderer); // filename
        columnModel.getColumn(3).setCellRenderer(numberRightRenderer); // filesize
        columnModel.getColumn(4).setCellRenderer(new ShowStateContentTooltipRenderer()); // state
        columnModel.getColumn(5).setCellRenderer(showContentTooltipRenderer); // path
        columnModel.getColumn(6).setCellRenderer(new BlocksProgressRenderer()); // blocks
        columnModel.getColumn(7).setCellRenderer(numberRightRenderer); // tries
        columnModel.getColumn(8).setCellRenderer(showContentTooltipRenderer); // key

        if( !loadTableLayout(columnModel) ) {
            // Sets the relative widths of the columns
            int[] widths = { 20, 20, 200, 65, 30, 60, 50, 15, 70 };
            for (int i = 0; i < widths.length; i++) {
                columnModel.getColumn(i).setPreferredWidth(widths[i]);
            }
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
            // keep icon columns 0,1 as is
            if(x != 0 && x != 1) {
                tcms[x].setPreferredWidth(columnWidths[x]);
            }
        }
        // add the columns in order loaded from settings
        for(int x=0; x < tableToModelIndex.length; x++) {
            tcm.addColumn(tcms[tableToModelIndex[x]]);
        }
        return true;
    }


    public int[] getColumnNumbers(int fieldID) {
        return new int[] {};
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SettingsClass.SHOW_COLORED_ROWS)) {
            showColoredLines = Core.frostSettings.getBoolValue(SettingsClass.SHOW_COLORED_ROWS);
            modelTable.fireTableDataChanged();
        }
    }
}
