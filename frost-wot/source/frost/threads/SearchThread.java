/*
  SearchThread.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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

import java.io.File;
import java.util.*;

import javax.swing.SwingUtilities;

import frost.*;
import frost.gui.model.SearchTableModel;
import frost.gui.objects.*;

public class SearchThread extends Thread {
    private static boolean DEBUG = false;
    private String request;
    private String board;
    private String date;
    private String keypool;
    private String searchType;
    private SearchTableModel searchTableModel;
    private Vector results = new Vector();
    private boolean searchAllBoards;
    private Map chk = Collections.synchronizedMap(new TreeMap());
    private String[] audioExtension;
    private String[] videoExtension;
    private String[] imageExtension;
    private String[] documentExtension;
    private String[] executableExtension;
    private String[] archiveExtension;
    private Vector boards;
    private static String fileSeparator = System.getProperty("file.separator");
    int allFileCount;
    int maxSearchResults;

    /**
     * Splits a String into single parts
     * @return Vector containing the single parts as Strings
     */
    private Vector getSingleRequests() {
    Vector singleRequests = new Vector();
    String tmp = request.trim();

    while(tmp.indexOf(" ") != -1) {
        int pos = tmp.indexOf(" ");
//      if (DEBUG) System.out.println("Search request: " + (tmp.substring(0, pos)).trim());
        singleRequests.add((tmp.substring(0, pos)).trim());
        tmp = (tmp.substring(pos, tmp.length())).trim();
    }

    if (tmp.length() > 0) {
//      if (DEBUG) System.out.println("Search request: " + (tmp));
        singleRequests.add(tmp);
    }

    return singleRequests;
    }

    /**
     * Reads index file and adds search results to the search table
     */
    private void getSearchResults() {
    if (request.length() > 0) {
        Vector singleRequests = getSingleRequests();

        synchronized(chk) {
        Iterator i = chk.values().iterator();
        while (i.hasNext()) {
            KeyClass key = (KeyClass)i.next();
            String filename = key.getFilename().toLowerCase().trim();
            boolean acceptFile = true;
            for (int j = 0; j < singleRequests.size(); j++) {
            String singleRequest = (String)singleRequests.elementAt(j);
            if (!singleRequest.startsWith("*")) {
                if ((filename.toLowerCase()).indexOf(singleRequest) == -1)
                acceptFile = false;
            }
            }
            if (acceptFile || request.equals("*")) {

            // Check for search type
            if (searchType.equals("All files")) {
                results.add(key);
            }
            else {
                boolean accept = false;
                if (searchType.equals("Audio"))
                accept = checkType(audioExtension, key.getFilename());
                if (searchType.equals("Video"))
                accept = checkType(videoExtension, key.getFilename());
                if (searchType.equals("Images"))
                accept = checkType(imageExtension, key.getFilename());
                if (searchType.equals("Documents"))
                accept = checkType(documentExtension, key.getFilename());
                if (searchType.equals("Executables"))
                accept = checkType(executableExtension, key.getFilename());
                if (searchType.equals("Archives"))
                accept = checkType(archiveExtension, key.getFilename());
                if (accept)
                results.add(key);
            }

            }
        }
        }
    }
    }

    /**
     * Checks extension types
     * @param extension Array with acceptable extensions
     * @param filename Filename to be checked
     * @return True if file gets accepted, else false
     */
    private boolean checkType(String[] extension, String filename) {
    boolean accepted = false;

    for (int i = 0; i < extension.length; i++) {
        if (filename.endsWith(extension[i]))
        accepted = true;
    }

    return accepted;
    }

    /**
     * Removes unwanted keys from results
     */
    private void filterSearchResults() {
    if (request.indexOf("*age") != -1) {
        int agePos = request.indexOf("*age");
        int nextSpacePos = request.indexOf(" ", agePos);
        if (nextSpacePos == -1)
        nextSpacePos = request.length();

        int age = 1;
        try { age = Integer.parseInt(request.substring(agePos + 4, nextSpacePos)); }
        catch (NumberFormatException e) { if (DEBUG) System.out.println("Did not recognice age, using default 1"); }

        if (DEBUG) System.out.println("AGE = " + age);

        GregorianCalendar today = new GregorianCalendar();
        today.setTimeZone(TimeZone.getTimeZone("GMT"));

        for (int i = results.size() - 1; i >= 0; i--) {
        	KeyClass key = (KeyClass)results.elementAt(i);
		GregorianCalendar keyCal=null;
		if (key.getDate() != null){
        		keyCal = key.getCal();

        		keyCal.add(Calendar.DATE, + (age));

        		if (keyCal.before(today)) {
         	   		results.removeElementAt(i);
        		}
		}
        }
    }

    if (request.indexOf("*-") != -1) {
        Vector removeStrings = new Vector();
        int notPos = request.indexOf("*-");
        int nextSpacePos = request.indexOf(" ", notPos);
        if (nextSpacePos == -1)
        nextSpacePos = request.length();

        String notString = request.substring(notPos + 2, nextSpacePos);
        if (notString.indexOf(";") == -1) { // only one notString
        removeStrings.add(notString);
        }
        else { //more notStrings
        while (notString.indexOf(";") != -1) {
            removeStrings.add(notString.substring(0, notString.indexOf(";")));
            if (! notString.endsWith(";"))
            notString = notString.substring(notString.indexOf(";") + 1, notString.length());
        }
        if (notString.length() > 0)
            removeStrings.add(notString);
        }

        for (int j = 0; j < removeStrings.size(); j++) {
        notString = (String)removeStrings.elementAt(j);
        for (int i = results.size() - 1; i >= 0; i--) {
            KeyClass key = (KeyClass)results.elementAt(i);
            if (((key.getFilename()).toLowerCase()).indexOf(notString.toLowerCase()) != -1) {
            results.removeElementAt(i);
            }
        }
        }
    }
    }

    /**
     * Displays search results in search table
     */
    private void displaySearchResults(FrostBoardObject board)
    {
        for( int i = 0; i < results.size(); i++ )
        {
            allFileCount++;

            if( allFileCount > this.maxSearchResults )
            {
                System.out.println("\nNOTE: maxSearchResults reached ("+maxSearchResults+")!");
                return;
            }

            KeyClass key = (KeyClass)results.elementAt(i);

            String filename = key.getFilename();
            Long size = key.getSize();
            String date = key.getDate();
            String keyData = key.getKey();
	    String SHA1 = key.getSHA1();
	    
	    if (SHA1 == null) 
	    	System.err.println(" SHA1 null in SearchThread!!! ");

            int searchItemState = FrostSearchItemObject.STATE_NONE;

            // Already downloaded files get a nice color outfit (see renderer in SearchTable)
            File file = new File(frame1.frostSettings.getValue("downloadDirectory") + filename);
            if( file.exists() )
            {
                // file is already downloaded -> light_gray
                searchItemState = FrostSearchItemObject.STATE_DOWNLOADED;
            }
            else if( frame1.getInstance().getDownloadTable().containsItemWithKey( SHA1 ) )
            {
                // this file is in download table -> blue
                searchItemState = FrostSearchItemObject.STATE_DOWNLOADING;
            }
            else if( frame1.getInstance().getUploadTable().containsItemWithKey( SHA1 ) )
            {
                // this file is in upload table -> green
                searchItemState = FrostSearchItemObject.STATE_DOWNLOADING;
            }

            final FrostSearchItemObject searchItem = new FrostSearchItemObject(board, key, searchItemState);

            boolean updateLabel2 = false;
            if( allFileCount > 9 && allFileCount%10==0 )
            {
                updateLabel2 = true;
            }
            final boolean updateLabel = updateLabel2;
            SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        searchTableModel.addRow(searchItem);
                        if( updateLabel )
                        {
                            frame1.getInstance().updateSearchResultCountLabel();
                        }
                    }
                });
        }
        SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    frame1.getInstance().updateSearchResultCountLabel();
                } });
    }

    public void run()
    {
        if( DEBUG ) System.out.println("Search for '" + request + "' on " + boards.size() + " boards started.");

        if( !request.equals("") )
        {
            int boardCount = boards.size();

            allFileCount = 0;

            for( int j = 0; j < boardCount; j++ )
            {
                FrostBoardObject frostBoard = (FrostBoardObject)boards.elementAt(j);
                String board = frostBoard.getBoardFilename();

                if( DEBUG ) System.out.println("Search for '" + request + "' on " + board + " started.");
                File keypoolDir = new File(keypool + board);
                if( keypoolDir.isDirectory() )
                {
                    File shaIndex = new File(keypoolDir+fileSeparator+"files.xml");
                    if( shaIndex.exists() )
                    {
                         chk.clear();
                         FileAccess.readKeyFile(shaIndex, chk);
                         getSearchResults();
                         if( DEBUG ) System.out.println(shaIndex.getName() + " - " + chk.size() + ";");
                        
                        
                    }
		    
		    File lastUploads = new File(keypoolDir+fileSeparator+"new_files.xml");
		    if (lastUploads.exists()) {
		    	chk.clear();
			FileAccess.readKeyFile(lastUploads,chk);
			getSearchResults();
			if( DEBUG ) System.out.println(lastUploads.getName() + " - " + chk.size() + ";");
		    }
		    
                }
                chk.clear();

                filterSearchResults();
                displaySearchResults(frostBoard);
                results.clear();
            }
        }
        frame1.getInstance().getSearchButton().setEnabled(true);
    }

    /**Constructor*/
    public SearchThread(String request,
            Vector boards, // a Vector containing all boards to search in
            String keypool,
            String searchType)
    {
        this.request = request.toLowerCase();
        this.searchTableModel = (SearchTableModel)frame1.getInstance().getSearchTable().getModel();
        this.keypool = keypool;
        this.searchType = searchType;
        this.audioExtension = frame1.frostSettings.getArrayValue("audioExtension");
        this.videoExtension = frame1.frostSettings.getArrayValue("videoExtension");
        this.documentExtension = frame1.frostSettings.getArrayValue("documentExtension");
        this.executableExtension = frame1.frostSettings.getArrayValue("executableExtension");
        this.archiveExtension = frame1.frostSettings.getArrayValue("archiveExtension");
        this.imageExtension = frame1.frostSettings.getArrayValue("imageExtension");
        this.boards = boards;
        this.maxSearchResults = frame1.frostSettings.getIntValue("maxSearchResults");
        if( this.maxSearchResults <= 0 )
            maxSearchResults = 10000; // default
    }
}

