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

import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import frost.gui.*;
import frost.util.gui.translation.*;
import frost.util.model.*;
import frost.util.model.gui.*;

public class FileListFileDetailsTableFormat  extends SortedTableFormat implements LanguageListener {
    
    private String stateNever;

    NumberFormat numberFormat = NumberFormat.getInstance();

    private Language language;

    private final static int COLUMN_COUNT = 7;

    public FileListFileDetailsTableFormat() {
        super(COLUMN_COUNT);

        language = Language.getInstance();
        language.addLanguageListener(this);
        refreshLanguage();

        setComparator(new FileNameComparator(), 0);
        setComparator(new OwnerComparator(), 1);
        setComparator(new RatingComparator(), 2);
        setComparator(new CommentComparator(), 3);
        setComparator(new KeywordsComparator(), 4);
        setComparator(new LastUploadedComparator(), 5);
        setComparator(new LastReceivedComparator(), 6);
    }

    public void languageChanged(LanguageEvent event) {
        refreshLanguage();
    }

    private void refreshLanguage() {
        setColumnName(0, language.getString("SearchItemPropertiesDialog.table.filename"));
        setColumnName(1, language.getString("SearchItemPropertiesDialog.table.owner"));
        setColumnName(2, language.getString("SearchItemPropertiesDialog.table.rating"));
        setColumnName(3, language.getString("SearchItemPropertiesDialog.table.comment"));
        setColumnName(4, language.getString("SearchItemPropertiesDialog.table.keywords"));
        setColumnName(5, language.getString("SearchItemPropertiesDialog.table.lastUploaded"));
        setColumnName(6, language.getString("SearchItemPropertiesDialog.table.lastReceived"));

        stateNever = language.getString("SearchItemPropertiesDialog.table.state.never");

        refreshColumnNames();
    }

    public Object getCellValue(ModelItem item, int columnIndex) {
        FileListFileDetailsItem searchItem = (FileListFileDetailsItem) item;
        switch (columnIndex) {
            case 0 :    // filename
                return searchItem.getFileOwner().getName();

            case 1 :    // owner
                return searchItem.getFileOwner().getOwner();

            case 2 :    // rating
                return RatingStringProvider.getRatingString( searchItem.getFileOwner().getRating() );

            case 3 :    // comment
                return searchItem.getDisplayComment();

            case 4 :    // keyword
                return searchItem.getDisplayKeywords();

            case 5 :    // lastUploaded
                if( searchItem.getDisplayLastUploaded().length() == 0 ) {
                    return stateNever;
                } else {
                    return searchItem.getDisplayLastUploaded();
                }

            case 6 :    // lastReceived
                if( searchItem.getDisplayLastReceived().length() == 0 ) {
                    return stateNever;
                } else {
                    return searchItem.getDisplayLastReceived();
                }

            default:
                return "**ERROR**";
        }
    }

    public int[] getColumnNumbers(int fieldID) {
        return new int[] {};
    }

    public void customizeTable(ModelTable modelTable) {
        super.customizeTable(modelTable);
        
        modelTable.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        // Sets the relative widths of the columns
        TableColumnModel columnModel = modelTable.getTable().getColumnModel();
        int[] widths = { 175, 80, 30, 60, 60, 70, 70 };
        for (int i = 0; i < widths.length; i++) {
            columnModel.getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private class FileNameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FileListFileDetailsItem item1 = (FileListFileDetailsItem) o1;
            FileListFileDetailsItem item2 = (FileListFileDetailsItem) o2;
            return item1.getFileOwner().getName().compareToIgnoreCase(item2.getFileOwner().getName());
        }
    }

    private class OwnerComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FileListFileDetailsItem item1 = (FileListFileDetailsItem) o1;
            FileListFileDetailsItem item2 = (FileListFileDetailsItem) o2;
            return item1.getFileOwner().getOwner().compareToIgnoreCase(item2.getFileOwner().getOwner());
        }
    }

    private class RatingComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            int val1 = ((FileListFileDetailsItem) o1).getFileOwner().getRating();
            int val2 = ((FileListFileDetailsItem) o2).getFileOwner().getRating();
            return new Long(val1).compareTo(new Long(val2));
        }
    }

    private class CommentComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FileListFileDetailsItem item1 = (FileListFileDetailsItem) o1;
            FileListFileDetailsItem item2 = (FileListFileDetailsItem) o2;
            return item1.getDisplayComment().compareToIgnoreCase(item2.getDisplayComment());
        }
    }

    private class KeywordsComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FileListFileDetailsItem item1 = (FileListFileDetailsItem) o1;
            FileListFileDetailsItem item2 = (FileListFileDetailsItem) o2;
            return item1.getDisplayKeywords().compareToIgnoreCase(item2.getDisplayKeywords());
        }
    }

    private class LastUploadedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            long val1 = ((FileListFileDetailsItem) o1).getFileOwner().getLastUploaded();
            long val2 = ((FileListFileDetailsItem) o2).getFileOwner().getLastUploaded();
            return new Long(val1).compareTo(new Long(val2));
        }
    }

    private class LastReceivedComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            long val1 = ((FileListFileDetailsItem) o1).getFileOwner().getLastReceived();
            long val2 = ((FileListFileDetailsItem) o2).getFileOwner().getLastReceived();
            return new Long(val1).compareTo(new Long(val2));
        }
    }
}
