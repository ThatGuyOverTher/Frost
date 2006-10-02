/*
  FilePointersThread.java / Frost
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
package frost.threads;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.*;
import frost.storage.database.applayer.*;
import frost.transferlayer.*;

/**
 * This thread downloads the KSK pointer files for file sharing from the public indices.
 * Received KSK files contain CHK keys of the filelists, this CHK keys are inserted into
 * the database (another thread downloads them).
 * When an update is finished, the thread checks if there are pending CHK keys that must be
 * send, and sends them.
 * Finally the thread sleeps for some time and restarts to retrieve the pointer files.
 */
public class FilePointersThread extends Thread {
    
    private static Logger logger = Logger.getLogger(FilePointersThread.class.getName());
    
    private static final int baseSleepTime = 15 * 60 * 1000;

    private GlobalIndexSlotsDatabaseTable indexSlots;
    private String keyPrefix;

    // one and only instance
    private static FilePointersThread instance = new FilePointersThread();
    
    private FilePointersThread() {
        indexSlots = new GlobalIndexSlotsDatabaseTable(GlobalIndexSlotsDatabaseTable.FILELISTS);
        String fileBase = Core.frostSettings.getValue(SettingsClass.FILE_BASE);
        keyPrefix = "KSK@frost/filepointers/" + fileBase + "-";
    }

    public static FilePointersThread getInstance() {
        return instance;
    }
    
    public boolean cancelThread() {
        return false;
    }

    /**
     * Returns true if no error occured.
     */
    private boolean uploadIndexFile(String dateStr, java.sql.Date sqlDate) throws Throwable {

        // get a list of CHK keys to send
        List sharedFileCHKkeys = SharedFilesCHKKeyManager.getCHKKeysToSend();
        if( sharedFileCHKkeys == null || sharedFileCHKkeys.size() == 0 ) {
            logger.info("FILEDN: No CHK keys to send.");
            return true;
        }

        // write a pointerfile to a tempfile
        List tmpChkStringKeys = new ArrayList(sharedFileCHKkeys.size());
        for( Iterator i = sharedFileCHKkeys.iterator(); i.hasNext(); ) {
            SharedFilesCHKKey ck = (SharedFilesCHKKey) i.next();
            tmpChkStringKeys.add( ck.getChkKey() );
        }
        
        FilePointerFileContent content = new FilePointerFileContent(System.currentTimeMillis(), tmpChkStringKeys);

        File tmpPointerFile = FileAccess.createTempFile("kskptr_", "_xml");
        tmpPointerFile.deleteOnExit();
        if( !FilePointerFile.writePointerFile(content, tmpPointerFile) ) {
            logger.severe("FILEDN: Error writing the KSK pointer file.");
            return false;
        }

        tmpChkStringKeys.clear();
        tmpChkStringKeys = null;

        // Wait some random time to not to flood the node
        Mixed.waitRandom(3000);

        logger.info("FILEDN: Starting upload of pointer file containing "+sharedFileCHKkeys.size()+" CHK keys");
        
        String insertKey = keyPrefix + dateStr + "-";
System.out.println("uploadIndexFile: Starting upload of pointer file containing "+sharedFileCHKkeys.size()+" CHK keys to "+insertKey);
        boolean wasOk = GlobalFileUploader.uploadFile(indexSlots, sqlDate, tmpPointerFile, insertKey, ".xml");
System.out.println("uploadIndexFile: upload finished, wasOk="+wasOk);
        tmpPointerFile.delete();
        if( wasOk ) {
            SharedFilesCHKKeyManager.updateCHKKeysWereSuccessfullySent(sharedFileCHKkeys);
        }
        return wasOk;
    }
    
    private void downloadDate(String dateStr, java.sql.Date sqlDate, boolean isForToday) throws Throwable {
        
        // "KSK@frost/filelistpointer/2006.11.1-<index>.xml"
        String requestKey = keyPrefix + dateStr + "-"; 

        int maxFailures;
        if (isForToday) {
            maxFailures = 3; // skip a maximum of 2 empty slots for today
        } else {
            maxFailures = 2; // skip a maximum of 1 empty slot for backload
        }
        int index = indexSlots.findFirstDownloadSlot(sqlDate);
        int failures = 0;
        while (failures < maxFailures && index >= 0 ) {

            // Wait some random time to not to flood the node
            Mixed.waitRandom(3000);

            logger.info("FILEDN: Requesting index " + index + " for date " + dateStr);

            String downKey = requestKey + index + ".xml";
System.out.println("FilePointersThread.downloadDate: requesting: "+downKey);        
            File downloadedFile = GlobalFileDownloader.downloadFile(downKey); 
            if(  downloadedFile == null ) {
System.out.println("FilePointersThread.downloadDate: failure");
                // download failed. 
                failures++;
                // next loop we try next index
                index = indexSlots.findNextDownloadSlot(index, sqlDate);
                continue;
            }
System.out.println("FilePointersThread.downloadDate: success");
            
            // download was successful, mark it
            indexSlots.setDownloadSlotUsed(index, sqlDate);
            // next loop we try next index
            index = indexSlots.findNextDownloadSlot(index, sqlDate);
            failures = 0;

            FilePointerFileContent content = FilePointerFile.readPointerFile(downloadedFile);
System.out.println("readPointerFile: result: "+content);            
            SharedFilesCHKKeyManager.processReceivedCHKKeys(content);
        }
        System.out.println("FilePointersThread.downloadDate: finished");        
    }

    public void run() {

        final int maxAllowedExceptions = 5;
        int occuredExceptions = 0;

        while( true ) {
            
            // +1 for today
            final int downloadBack = 1 + Core.frostSettings.getIntValue(SettingsClass.MAX_FILELIST_DOWNLOAD_DAYS);
            try {
                for (int i=0; i < downloadBack; i++) {
                    boolean isForToday;
                    if( i == 0 ) {
                        isForToday = true; // upload own keys today only
                    } else {
                        isForToday = false;
                    }
                    
                    String dateStr = DateFun.getDate(i);
                    java.sql.Date sqlDate = DateFun.getSqlDateGMTDaysAgo(i);

System.out.println("FilePointersThread: download for "+dateStr);                    
                    // download file pointer files for this date
                    if( !isInterrupted() ) {
                        downloadDate(dateStr, sqlDate, isForToday);
                    }
                    
                    // for today, maybe upload a file pointer file
                    if( !isInterrupted() && isForToday ) {
                        try {
System.out.println("FilePointersThread: upload for "+dateStr);                    
                            uploadIndexFile(dateStr, sqlDate);
                        } catch(Throwable t) {
                            logger.log(Level.SEVERE, "Exception during uploadIndexFile()", t);
                        }
                    }
                    
                    if( isInterrupted() ) {
                        break;
                    }
                }
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Exception catched", e);
                occuredExceptions++;
            }
            
            if( occuredExceptions > maxAllowedExceptions ) {
                logger.log(Level.SEVERE, "Stopping FilePointersThread because of too much exceptions");
                break;
            }
            if( isInterrupted() ) {
                break;
            }
            
            // random sleeptime to anonymize our uploaded pointer files
            Mixed.waitRandom(baseSleepTime); 
        }
        indexSlots.close();
    }
}
