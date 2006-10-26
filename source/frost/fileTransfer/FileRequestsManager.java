/*
 FileRequestsManager.java / Frost
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
package frost.fileTransfer;

import java.util.*;
import java.util.logging.*;

import frost.fileTransfer.download.*;
import frost.fileTransfer.sharing.*;
import frost.storage.database.applayer.*;

/**
 * Collects the files to request from other users.
 * Processes incoming requests.
 */
public class FileRequestsManager {

    private static Logger logger = Logger.getLogger(SharedFilesCHKKeyManager.class.getName());
    
    private static final int MAX_SHA_PER_REQUESTFILE = 350;
    
    private static final int MIN_DOWNLOAD_RETRIES = 7; // needed retries before we request a file
    
    private static final long MIN_LAST_UPLOADED = 3; // start upload if last upload is X days back

    /**
     * @return List with SHA strings that should be requested
     */
    public static List getRequestsToSend() {
        
        // get files from downloadtable that are shared and check if we should send a request for them
        // sha256 = 64 bytes, send a maximum of 350 requests per file (<32kb)
        
        // Rules for send of a request:
        // - don't send a request if the file to request was not seen in a file index for more than 14 days
        // - must be not requested since 23h ( by us or others )
        // - we DON'T have the chk OR
        // - we HAVE the chk, but download was not successful after 10 tries, and last try was not longer
        //   then 2 days before (maybe successful now)
        
        long now = System.currentTimeMillis();
        long before23hours = now - 1L * 23L * 60L * 60L * 1000L;
        long before2days = now - 2L * 24L * 60L * 60L * 1000L;
        long before14days = now - 14L * 24L * 60L * 60L * 1000L;
        
        List downloadModelItems = FileTransferManager.getInstance().getDownloadManager().getModel().getItems();
        
        List mustSendRequests = new LinkedList();

        for( Iterator i = downloadModelItems.iterator(); i.hasNext(); ) {
            FrostDownloadItem dlItem = (FrostDownloadItem) i.next();

            if( !dlItem.isSharedFile() ) {
                continue;
            }

            if( dlItem.getEnableDownload().booleanValue() == false ) {
                continue;
            }

            FrostFileListFileObject sfo = dlItem.getFileListFileObject();
            
            if( sfo.getLastReceived() < before14days ) {
                // sha not received for 14 days, is it still shared? don't request it.
                continue;
            }

            if( sfo.getRequestLastSent() > before23hours || sfo.getRequestLastReceived() > before23hours ) {
                // request received or send in last 24 hours
                continue;
            }

            if( dlItem.getKey() != null && dlItem.getKey().length() > 0 ) {
                if( dlItem.getRetries() < MIN_DOWNLOAD_RETRIES || dlItem.getLastDownloadStopTime() > before2days ) {
                    // download retries < MIN_DOWNLOAD_RETRIES or last download try was not in last 2 days (retry!)
                    continue;
                }
            }

            // we MUST request this file
            mustSendRequests.add( sfo.getSha() );
            
            if( mustSendRequests.size() == MAX_SHA_PER_REQUESTFILE ) {
                break;
            }
        }

        return mustSendRequests;
    }
    
    /**
     * @param requests a List of String objects with SHAs that were successfully sent within a request file
     */
    public static void updateRequestsWereSuccessfullySent(List requests) {

        long now = System.currentTimeMillis();
        
        for( Iterator i = requests.iterator(); i.hasNext(); ) {
            String sha = (String) i.next();
            
            // filelist files in download table
            List downloadModelItems = FileTransferManager.getInstance().getDownloadManager().getModel().getItems();

            for( Iterator j = downloadModelItems.iterator(); j.hasNext(); ) {
                FrostDownloadItem dlItem = (FrostDownloadItem) j.next();
                if( !dlItem.isSharedFile() ) {
                    continue;
                }
                FrostFileListFileObject sfo = dlItem.getFileListFileObject();
                if( !sfo.getSha().equals(sha) ) {
                    continue;
                }
                sfo.setRequestsSentCount(sfo.getRequestsSentCount() + 1);
                sfo.setRequestLastSent(now);
            }
            
            // same filelist file, but in database
            try {
                AppLayerDatabase.getFileListDatabaseTable().updateFrostFileListFileObjectAfterRequestSend(sha, now);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Exception during updateFrostSharedFileObjectAfterRequestSend", t);
            }            
        }
    }
    
    /**
     * Process the List of newly received requests (sha keys)
     */
    public static void processReceivedRequests(FileRequestFileContent content) {
        
        if( content == null || content.getShaStrings() == null || content.getShaStrings().size() == 0 ) {
            return;
        }
        
        long now = System.currentTimeMillis();
        
        List downloadModelItems = FileTransferManager.getInstance().getDownloadManager().getModel().getItems();
        List sharedFilesModelItems = FileTransferManager.getInstance().getSharedFilesManager().getModel().getItems();
        
        for( Iterator i = content.getShaStrings().iterator(); i.hasNext(); ) {
            String sha = (String) i.next();

            // filelist files in download table
            for( Iterator j = downloadModelItems.iterator(); j.hasNext(); ) {
                FrostDownloadItem dlItem = (FrostDownloadItem) j.next();
                if( !dlItem.isSharedFile() ) {
                    continue;
                }
                FrostFileListFileObject sfo = dlItem.getFileListFileObject();
                if( !sfo.getSha().equals(sha) ) {
                    continue;
                }
                sfo.setRequestsReceivedCount(sfo.getRequestsReceivedCount() + 1);
                if( sfo.getRequestLastReceived() < content.getTimestamp() ) {
                    sfo.setRequestLastReceived(content.getTimestamp());
                }
            }
            
            // same filelist file, but in database
            try {
                AppLayerDatabase.getFileListDatabaseTable().updateFrostFileListFileObjectAfterRequestReceived(sha, content.getTimestamp());
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Exception during updateFrostSharedFileObjectAfterRequestReceived", t);
            }
            
            // our own shared files in shared files table
            for( Iterator j = sharedFilesModelItems.iterator(); j.hasNext(); ) {
                FrostSharedFileItem sfo = (FrostSharedFileItem) j.next();
                if( !sfo.getSha().equals(sha) ) {
                    continue;
                }
                
                if( sfo.getRequestLastReceived() < content.getTimestamp() ) {
                    sfo.setRequestLastReceived(content.getTimestamp());
                }
                sfo.setRequestsReceived(sfo.getRequestsReceived() + 1);
                
                // Maybe start an upload
                // Rules: 
                // - only if upload is not running already
                // - only if last upload was'nt earlier than 3 days
                
                // search upload table, check if we currently upload this file
                if( !sfo.isCurrentlyUploading() && sfo.isValid() ) {
                    // is not uploading currently
                    long minDiff = MIN_LAST_UPLOADED * 24L * 60L * 60L * 1000L; // 3 days in milliseconds
                    if( sfo.getLastUploaded() < now - minDiff ) {
                        // last upload earlier than 3 days before, start upload
                        // add to upload files
                        FileTransferManager.getInstance().getUploadManager().getModel().addNewUploadItemFromSharedFile(sfo);
                    }
                }
            }
        }
    }
}
