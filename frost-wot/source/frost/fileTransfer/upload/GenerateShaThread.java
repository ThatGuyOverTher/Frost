/*
  GenerateShaThread.java / Frost
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
package frost.fileTransfer.upload;

import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.*;
import frost.fileTransfer.sharing.*;
import frost.storage.database.applayer.*;
import frost.util.*;

/**
 * Generates the sha checksum of a file.
 */
public class GenerateShaThread extends Thread {

    private static Logger logger = Logger.getLogger(GenerateShaThread.class.getName());

    private static final int wait1minute = 1 * 60 * 1000;
    FileQueue fileQueue;
    
    public GenerateShaThread() {
        fileQueue = new FileQueue();
    }
    
    public void addToFileQueue(NewUploadFile f) {
        // awakes thread
        fileQueue.appendFileToQueue(f);
    }
    
    public int getQueueSize() {
        return fileQueue.getQueueSize();
    }
    
    public void run() {

        final int maxAllowedExceptions = 5;
        int occuredExceptions = 0;

        while(true) {
            try {
                // if now work is in queue this call waits for a new queueitem
                NewUploadFile newUploadFile = fileQueue.getFileFromQueue();
                
                if( newUploadFile == null ) {
                    // paranoia
                    Mixed.wait(wait1minute);
                    continue;
                }
                
                String sha = Core.getCrypto().computeChecksumSHA256(newUploadFile.getFile());
                
                if( sha != null ) {
                    
                    // create new item
                    FrostSharedFileItem sfi = new FrostSharedFileItem(
                            newUploadFile.getFile(),
                            newUploadFile.getFrom(),
                            sha);
                    
                    // add to shared files
                    FileTransferManager.getInstance().getSharedFilesManager().getModel().addNewSharedFile(sfi);
                    
                    // delete from newuploadfiles database
                    Core.getInstance().getFileTransferManager().getNewUploadFilesManager().deleteNewUploadFile(newUploadFile);
                }
                
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "Exception catched",t);
                occuredExceptions++;
            }
            
            if( occuredExceptions > maxAllowedExceptions ) {
                logger.log(Level.SEVERE, "Stopping GenerateShaThread because of too much exceptions");
                break;
            }
        }
    }
    
    private class FileQueue {
        
        private LinkedList queue = new LinkedList();
        
        public synchronized NewUploadFile getFileFromQueue() {
            try {
                // let dequeueing threads wait for work
                while( queue.isEmpty() ) {
                    wait();
                }
            } catch (InterruptedException e) {
                return null; // waiting abandoned
            }
            
            if( queue.isEmpty() == false ) {
                NewUploadFile key = (NewUploadFile) queue.removeFirst();
                return key;
            }
            return null;
        }

        public synchronized void appendFileToQueue(NewUploadFile f) {
            queue.addLast(f);
            notifyAll(); // notify all waiters (if any) of new record
        }
        
        public synchronized int getQueueSize() {
            return queue.size();
        }
    }
}
