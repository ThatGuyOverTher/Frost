/*
  SearchMessagesThread.java / Frost
  Copyright (C) 2006  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.threads;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.gui.*;
import frost.gui.objects.*;
import frost.messages.*;

public class SearchMessagesThread extends Thread {

    private static Logger logger = Logger.getLogger(SearchMessagesThread.class.getName());

    SearchMessagesDialog searchDialog; // used to add found messages
    SearchMessagesConfig searchConfig;
    
    private boolean stopRequested = false;
    
    String keypoolDir;
    String archiveDir;

    private XmlFileFilter xmlFileFilter = new XmlFileFilter();

    public SearchMessagesThread(SearchMessagesDialog searchDlg, SearchMessagesConfig searchCfg) {
        searchDialog = searchDlg;
        searchConfig = searchCfg;
        
        keypoolDir = MainFrame.keypool;
        archiveDir = Core.frostSettings.getValue("archive.dir");
        if( archiveDir == null || archiveDir.length() == 0 ) {
            logger.severe("Warning: no ARCHIVE DIR specified!");
            archiveDir = null;
        }
        archiveDir += ("messages" + File.separator);
    }
    
    public void run() {

        try {
            // select board dirs
            List boardsToSearch;
            if( searchConfig.searchBoards == SearchMessagesConfig.BOARDS_DISPLAYED ) {
                boardsToSearch = MainFrame.getInstance().getTofTreeModel().getAllBoards();
            } else if( searchConfig.searchBoards == SearchMessagesConfig.BOARDS_CHOSED ) {
                boardsToSearch = searchConfig.chosedBoards;
            } else {
                boardsToSearch = new ArrayList(); // paranoia
            }
    
            TrustStates trustStates = new TrustStates();
            DateRange dateRange = new DateRange();
    
            for(Iterator i=boardsToSearch.iterator(); i.hasNext(); ) {
                
                if( isStopRequested() ) {
                    break;
                }
                
                Board board = (Board)i.next();
                
                // build date and trust state info for this board
                updateTrustStatesForBoard(board, trustStates);
                updateDateRangeForBoard(board, dateRange);
                
                if( searchConfig.searchInKeypool ) {
                    // search in keypool
                    // Format: keypool\boards\2006.3.1\2006.3.1-boards-0.xml
                    File boardFolder = new File(keypoolDir + board.getBoardFilename());
                    if( boardFolder.isDirectory() == true ) {
                        searchBoardFolder(boardFolder, trustStates, dateRange, false);
                    } else {
                        logger.warning("No board folder in keypool for board "+board.getName());
                    }
                }
                
                if( isStopRequested() ) {
                    break;
                }
    
                if( searchConfig.searchInArchive && archiveDir != null ) {
                    // search in archive
                    // Format: keypool-archive.j\messages\boards\2005.12.7\2005.12.7-boards-0.xml
                    File boardFolder = new File(archiveDir + board.getBoardFilename());
                    if( boardFolder.isDirectory() == true ) {
                        searchBoardFolder(boardFolder, trustStates, dateRange, true);
                    } else {
                        logger.warning("No board folder in archive for board "+board.getName());
                    }
                }
            }
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Catched exception:", t);
        }
        searchDialog.notifySearchThreadFinished();
    }
    
    // Format: boards\2006.3.1\2006.3.1-boards-0.xml
    private void searchBoardFolder(File boardFolder, TrustStates ts, DateRange dr, boolean archived) {
        
        File[] boardFolderFiles = boardFolder.listFiles();
        if( boardFolderFiles == null ) {
            logger.severe("Could not get list of files for folder "+boardFolder.getPath());
            return;
        }
        for(int x=0; x < boardFolderFiles.length; x++) {
            
            if( isStopRequested() ) {
                break;
            }

            File boardFolderFile = boardFolderFiles[x];
            if( boardFolderFile.isDirectory() == false ) {
                continue;
            }
            // its a dir, we expect a name like '2006.3.1'
            Calendar dateDirCal = null;
            try {
                dateDirCal = DateFun.getCalendarFromDate(boardFolderFile.getName());
            } catch(NumberFormatException ex) {
                logger.warning("Incorrect board date folder name, must be a date: "+boardFolderFile.getPath());
                continue;
            }
            
            // check if this date dir is in the date range we want to search
            if( dr.startDate != null && dr.endDate != null &&
                (dateDirCal.before(dr.startDate) || dateDirCal.after(dr.endDate)) ) 
            {
                continue;
            }
            // get list of .xml files in the date dir
            File[] xmlFiles = boardFolderFile.listFiles(xmlFileFilter);
            if( xmlFiles == null ) {
                logger.severe("Could not get list of xml files for folder "+boardFolderFile.getPath());
                continue;
            }
            for(int y=0; y < xmlFiles.length; y++) {
                
                if( isStopRequested() ) {
                    break;
                }

                File xmlFile = xmlFiles[y];
                // search this xml file
                searchXmlFile(xmlFile, ts, archived);
            }
        }
    }
    
    private void searchXmlFile(File xmlFile, TrustStates ts, boolean archived) {
        
        FrostSearchResultMessageObject mo = null;
        try {
            mo = new FrostSearchResultMessageObject(xmlFile, archived);
        } catch(Throwable t) {
            logger.warning("Could not load xml file '"+xmlFile.getPath()+"': "+t.toString());
            return;
        }
        
        // check private only
        if( searchConfig.searchPrivateMsgsOnly ) {
            if( mo.getRecipient() == null || mo.getRecipient().length() == 0 ) {
                return;
            }
        }
        
        // check trust states
        if( matchesTrustStates(mo, ts) == false ) {
            return;
        }

        // check sender
        if( searchConfig.sender != null ) {
            if( searchInText(searchConfig.sender, mo.getFrom()) == false ) {
                // sender not found
                return;
            }
        }
        
        // check subject
        if( searchConfig.subject != null ) {
            if( searchInText(searchConfig.subject, mo.getSubject()) == false ) {
                // subject not found
                return;
            }
        }
        
        // check content
        if( searchConfig.content != null ) {
            if( searchInText(searchConfig.content, mo.getContent()) == false ) {
                // content not found
                return;
            }
        }
        
        // match, add to result table
        searchDialog.addFoundMessage(mo);
    }
    
    private boolean searchInText(List searchItems, String txt) {
        boolean found = false;
        txt = txt.toLowerCase(); // search items are already lowercase
        for(Iterator i=searchItems.iterator(); i.hasNext(); ) {
            String item = (String)i.next();
            if( txt.indexOf(item) > -1 ) {
                found = true;
                break; // one match is enough
            }
        }
        return found;
    }
    
    private boolean matchesTrustStates(FrostMessageObject msg, TrustStates ts) {
        int state = msg.getMsgStatus();
        
        if( state == VerifyableMessageObject.xGOOD && ts.trust_good == false ) {
            return false;
        }
        if( state == VerifyableMessageObject.xOBSERVE && ts.trust_observe == false ) {
            return false;
        }
        if( state == VerifyableMessageObject.xCHECK && ts.trust_check == false ) {
            return false;
        }
        if( state == VerifyableMessageObject.xBAD && ts.trust_bad == false ) {
            return false;
        }
        if( state == VerifyableMessageObject.xOLD && ts.trust_none == false ) {
            return false;
        }
        if( state == VerifyableMessageObject.xTAMPERED && ts.trust_tampered == false ) {
            return false;
        }
        
        return true;
    }
    
    private void updateTrustStatesForBoard(Board b, TrustStates ts) {
        if( searchConfig.searchTruststates == SearchMessagesConfig.TRUST_ALL ) {
            // use all trust states
            ts.trust_good = true;
            ts.trust_observe = true;
            ts.trust_check = true;
            ts.trust_bad = true;
            ts.trust_none = true;
            ts.trust_tampered = true;
        } else if( searchConfig.searchTruststates == SearchMessagesConfig.TRUST_CHOSED ) {
            // use specified trust states
            ts.trust_good = searchConfig.trust_good;
            ts.trust_observe = searchConfig.trust_observe;
            ts.trust_check = searchConfig.trust_check;
            ts.trust_bad = searchConfig.trust_bad;
            ts.trust_none = searchConfig.trust_none;
            ts.trust_tampered = searchConfig.trust_tampered;
        } else if( searchConfig.searchTruststates == SearchMessagesConfig.TRUST_DISPLAYED ) {
            // use trust states configured for board
            ts.trust_good = true;
            ts.trust_observe = !b.getHideObserve();
            ts.trust_check = !b.getHideCheck();
            ts.trust_bad = !b.getHideBad();
            ts.trust_none = !b.getShowSignedOnly();
            ts.trust_tampered = !b.getShowSignedOnly();
        }
    }
    
    private void updateDateRangeForBoard(Board b, DateRange dr) {
        if( searchConfig.searchDates == SearchMessagesConfig.DATE_DISPLAYED ) {
            dr.startDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            dr.startDate.add(Calendar.DATE, -b.getMaxMessageDisplay());
            dr.endDate = new GregorianCalendar(TimeZone.getTimeZone("GMT")); // today
        } else if( searchConfig.searchDates == SearchMessagesConfig.DATE_DAYS_BACKWARD ) {
            dr.startDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            dr.startDate.add(Calendar.DATE, -searchConfig.daysBackward);
            dr.endDate = new GregorianCalendar(TimeZone.getTimeZone("GMT")); // today
        } else if( searchConfig.searchDates == SearchMessagesConfig.DATE_BETWEEN_DATES ) {
            dr.startDate = searchConfig.startDate;
            dr.endDate = searchConfig.endDate;
        } else {
            // all dates
            dr.startDate = null;
            dr.endDate = null;
        }
    }

    public synchronized boolean isStopRequested() {
        return stopRequested;
    }
    public synchronized void requestStop() {
        stopRequested = true;
    }

    private class DateRange {
        GregorianCalendar startDate;
        GregorianCalendar endDate;
    }

    private class TrustStates {
        // current trust status to search into
        public boolean trust_good = false;
        public boolean trust_observe = false;
        public boolean trust_check = false;
        public boolean trust_bad = false;
        public boolean trust_none = false;
        public boolean trust_tampered = false;
    }
    
    private class XmlFileFilter implements FileFilter {
        public boolean accept(File f) {
            if( f.isFile() && f.getName().endsWith(".xml") ) {
                return true;
            }
            return false;
        }
    }
}
