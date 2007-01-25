/*
  PersistenceManager.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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

import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.*;
import frost.fcp.fcp07.*;
import frost.fcp.fcp07.persistence.*;
import frost.fileTransfer.download.*;
import frost.fileTransfer.upload.*;
import frost.util.*;
import frost.util.model.*;

/**
 * This class starts/stops/monitors the persistent requests on Freenet 0.7.
 */
public class PersistenceManager implements IFcpPersistentRequestsHandler {

    private static Logger logger = Logger.getLogger(PersistenceManager.class.getName());

    /* FIXME:
     * - show if external in table
     */
    
    // this would belong to the models, but not needed for 0.5 or without persistence, hence we maintain it here
    private Hashtable<String,FrostUploadItem> uploadModelItems = new Hashtable<String,FrostUploadItem>(); 
    private Hashtable<String,FrostDownloadItem> downloadModelItems = new Hashtable<String,FrostDownloadItem>();
    
    private UploadModel uploadModel;
    private DownloadModel downloadModel;
    
    private DirectTransferQueue directTransferQueue;
    private DirectTransferThread directTransferThread;
    
    private boolean showExternalItemsDownload;
    private boolean showExternalItemsUpload;
    
    private FcpPersistentQueue persistentQueue;
    private FcpPersistentConnectionTools fcpTools;
    
    private Set<String> directGETsInProgress = new HashSet<String>();
    private Set<String> directPUTsInProgress = new HashSet<String>();
    
    private Set<String> directPUTsWithoutAnswer = new HashSet<String>();
    
    /**
     * @return  true if Frost is configured to use persistent uploads and downloads, false if not
     */
    public static boolean isPersistenceEnabled() {
        if( FcpHandler.isFreenet07()
                && Core.frostSettings.getBoolValue(SettingsClass.FCP2_USE_PERSISTENCE) ) 
        {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isDDA() {
        if( FcpHandler.isFreenet07()
                && Core.frostSettings.getBoolValue(SettingsClass.FCP2_USE_DDA) 
                && FcpPersistentConnection.getInstance().isDDA() ) 
        {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Must be called after the upload and download model is initialized!
     */
    public PersistenceManager(UploadModel um, DownloadModel dm) {
        
        showExternalItemsDownload = Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_DOWNLOAD);
        showExternalItemsUpload = Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_UPLOAD);
        
        fcpTools = new FcpPersistentConnectionTools();
        
        Core.frostSettings.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if( evt.getPropertyName().equals(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_DOWNLOAD) ) {
                    showExternalItemsDownload = Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_DOWNLOAD);
                    if( showExternalItemsDownload ) {
                        persistentQueue.awakeSyncThread();
                    }
                } else if( evt.getPropertyName().equals(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_UPLOAD) ) {
                    showExternalItemsUpload = Core.frostSettings.getBoolValue(SettingsClass.GQ_SHOW_EXTERNAL_ITEMS_UPLOAD);
                    if( showExternalItemsUpload ) {
                        persistentQueue.awakeSyncThread();
                    }
                }
            }
        });
        
        uploadModel = um;
        downloadModel = dm;
        
        // initially get all items from model
        for(int x=0; x < uploadModel.getItemCount(); x++) {
            FrostUploadItem ul = (FrostUploadItem) uploadModel.getItemAt(x);
            if( ul.getGqIdentifier() != null ) {
                uploadModelItems.put(ul.getGqIdentifier(), ul);
            }
        }
        for(int x=0; x < downloadModel.getItemCount(); x++) {
            FrostDownloadItem ul = (FrostDownloadItem) downloadModel.getItemAt(x);
            if( ul.getGqIdentifier() != null ) {
                downloadModelItems.put(ul.getGqIdentifier(), ul);
            }
        }
        
        // enqueue listeners to keep updated about the model items
        uploadModel.addOrderedModelListener(
                new SortedModelListener() {
                    public void modelCleared() {
                        for( FrostUploadItem ul : uploadModelItems.values() ) {
                            if( ul.isExternal() == false ) {
                                fcpTools.removeRequest(ul.getGqIdentifier());
                            }
                        }
                        uploadModelItems.clear();
                    }
                    public void itemAdded(int position, ModelItem item) {
                        FrostUploadItem ul = (FrostUploadItem) item;
                        uploadModelItems.put(ul.getGqIdentifier(), ul);
                        if( !ul.isExternal() ) {
                            persistentQueue.awakeSyncThread();
                        }
                    }
                    public void itemChanged(int position, ModelItem item) {
                    }
                    public void itemsRemoved(int[] positions, ModelItem[] items) {
                        for(ModelItem item : items) {
                            FrostUploadItem ul = (FrostUploadItem) item;
                            uploadModelItems.remove(ul.getGqIdentifier());
                            if( ul.isExternal() == false ) {
                                fcpTools.removeRequest(ul.getGqIdentifier());
                            }
                        }
                    }
                });

        downloadModel.addOrderedModelListener(
                new SortedModelListener() {
                    public void modelCleared() {
                        for( FrostDownloadItem ul : downloadModelItems.values() ) {
                            if( ul.isExternal() == false ) {
                                fcpTools.removeRequest(ul.getGqIdentifier());
                            }
                        }
                        downloadModelItems.clear();
                    }
                    public void itemAdded(int position, ModelItem item) {
                        FrostDownloadItem ul = (FrostDownloadItem) item;
                        downloadModelItems.put(ul.getGqIdentifier(), ul);
                        if( !ul.isExternal() ) {
                            persistentQueue.awakeSyncThread();
                        }
                    }
                    public void itemChanged(int position, ModelItem item) {
                    }
                    public void itemsRemoved(int[] positions, ModelItem[] items) {
                        for(ModelItem item : items) {
                            FrostDownloadItem ul = (FrostDownloadItem) item;
                            downloadModelItems.remove(ul.getGqIdentifier());
                            if( ul.isExternal() == false ) {
                                fcpTools.removeRequest(ul.getGqIdentifier());
                            }
                        }
                    }
                });
        
        directTransferQueue = new DirectTransferQueue();
        directTransferThread = new DirectTransferThread();
        
        persistentQueue = new FcpPersistentQueue(new FcpPersistentConnectionTools(), this);
    }
    
    public void startThreads() {
        directTransferThread.start();
        persistentQueue.startThreads();        
    }

    public void removeRequests(List<String> requests) {
        for( String id : requests ) {
            fcpTools.removeRequest(id);
        }
        awakeSyncThread();
    }
    
    /**
     * Awake waiting syncthread to maybe start new requests immediately.
     */
    public void awakeSyncThread() {
        persistentQueue.awakeSyncThread();
    }
    
    public void changeItemPriorites(ModelItem[] items, int newPrio) {
        if( items == null || items.length == 0 ) {
            return;
        }
        for( int i = 0; i < items.length; i++ ) {
            ModelItem item = items[i];
            String gqid = null;
            if( item instanceof FrostUploadItem ) {
                FrostUploadItem ui = (FrostUploadItem) item; 
                gqid = ui.getGqIdentifier();
            } else if( item instanceof FrostDownloadItem ) {
                FrostDownloadItem di = (FrostDownloadItem) item; 
                gqid = di.getGqIdentifier();
            }
            if( gqid != null ) {
                fcpTools.changeRequestPriority(gqid, newPrio);
            }
        }
        // finally awake sync thread to get back the changed prios
        awakeSyncThread();
    }
    
    /**
     * Called after ListPersistentRequests with current upload and download requests.
     */
    public void requestsUpdated(Map<String,FcpPersistentPut> uploadRequests, Map<String,FcpPersistentGet> downloadRequests) {
        // handle disappeared requests
        handleDisappearedItems(uploadRequests, downloadRequests);
        
        // handle new requests
        handleNewRequests(uploadRequests, downloadRequests);
        
        // start new requests
        startNewUploads();
        startNewDownloads();
    }

    /**
     * Called if an upload request was updated between ListPersistentRequest.
     */
    public void uploadRequestUpdated(FcpPersistentPut uploadRequest) {
        FrostUploadItem ui = uploadModelItems.get(uploadRequest.getIdentifier());
        if( ui == null ) {
            // not (yet) in our model
            return;
        }
        applyState(ui, uploadRequest);
    }
    
    /**
     * Called if an download request was updated between ListPersistentRequest.
     */
    public void downloadRequestUpdated(FcpPersistentGet downloadRequest) {
        FrostDownloadItem dl = downloadModelItems.get( downloadRequest.getIdentifier() );
        if( dl == null ) {
            // not (yet) in our model
            return;
        }
        applyState(dl, downloadRequest);
    }

    /**
     * Enqueue a direct GET if not already enqueued, or already downloaded to download dir.
     * @return true if item was enqueued
     */
    public boolean maybeEnqueueDirectGet(FrostDownloadItem dlItem, long expectedFileSize) {
        if( !isDirectTransferInProgress(dlItem) ) {
            File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
            if( !targetFile.isFile() || targetFile.length() != expectedFileSize ) {
                directTransferQueue.appendItemToQueue(dlItem);
                return true;
            }
        }
        return false;
    }

    /**
     * Apply the states of FcpRequestGet to the FrostDownloadItem.
     */
    private void applyState(FrostDownloadItem dlItem, FcpPersistentGet getReq) {
        dlItem.setPriority(getReq.getPriority());
        if( getReq.isProgressSet() ) {
            int doneBlocks = getReq.getDoneBlocks();
            int requiredBlocks = getReq.getRequiredBlocks();
            int totalBlocks = getReq.getTotalBlocks();
            boolean isFinalized = getReq.isFinalized();
            if( totalBlocks > 0 ) {
                dlItem.setDoneBlocks(doneBlocks);
                dlItem.setRequiredBlocks(requiredBlocks);
                dlItem.setTotalBlocks(totalBlocks);
                dlItem.setFinalized(isFinalized);
                dlItem.fireValueChanged();
            }
            if( dlItem.getState() != FrostDownloadItem.STATE_PROGRESS ) {
                dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
            }
        }
        if( getReq.isSuccess() ) {
            if( dlItem.isExternal() ) {
                dlItem.setState(FrostDownloadItem.STATE_DONE);
                dlItem.setFileSize(getReq.getFilesize());
            } else {
                if( dlItem.isDirect() ) {
                    maybeEnqueueDirectGet(dlItem, getReq.getFilesize());
                } else {
                    FcpResultGet result = new FcpResultGet(true);
                    File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
                    FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);
                }
            }
        }
        if( getReq.isFailed() ) {
            String desc = getReq.getCodeDesc();
            if( dlItem.isExternal() ) {
                dlItem.setState(FrostDownloadItem.STATE_FAILED);
                dlItem.setErrorCodeDescription(desc);
            } else {
                int code = getReq.getCode();
                boolean isFatal = getReq.isFatal();
                
                FcpResultGet result = new FcpResultGet(false, code, desc, isFatal);
                File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
                boolean retry = FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);
                if( retry ) {
                    fcpTools.removeRequest(getReq.getIdentifier());
                }
            }
        }
    }

    /**
     * Apply the states of FcpRequestPut to the FrostUploadItem.
     */
    private void applyState(FrostUploadItem ulItem, FcpPersistentPut putReq) {

        if( directPUTsWithoutAnswer.contains(ulItem.getGqIdentifier()) ) {
            // we got an answer
            directPUTsWithoutAnswer.remove(ulItem.getGqIdentifier());
        }
        
        ulItem.setPriority(putReq.getPriority());
        if( putReq.isProgressSet() ) {
            int doneBlocks = putReq.getDoneBlocks();
            int totalBlocks = putReq.getTotalBlocks();
            boolean isFinalized = putReq.isFinalized();
            if( totalBlocks > 0 ) {
                ulItem.setDoneBlocks(doneBlocks);
                ulItem.setTotalBlocks(totalBlocks);
                ulItem.setFinalized(isFinalized);
                ulItem.fireValueChanged();
            }
            if( ulItem.getState() != FrostUploadItem.STATE_PROGRESS ) {
                ulItem.setState(FrostUploadItem.STATE_PROGRESS);
            }
        }
        if( putReq.isSuccess() ) {
            String chkKey = putReq.getUri();
            if( ulItem.isExternal() ) {
                ulItem.setState(FrostDownloadItem.STATE_DONE);
                ulItem.setKey(chkKey);
            } else {
                FcpResultPut result = new FcpResultPut(FcpResultPut.Success, chkKey);
                FileTransferManager.inst().getUploadManager().notifyUploadFinished(ulItem, result);
            }
        }
        if( putReq.isFailed() ) {
            String desc = putReq.getCodeDesc();
            if( ulItem.isExternal() ) {
                ulItem.setState(FrostUploadItem.STATE_FAILED);
                ulItem.setErrorCodeDescription(desc);
            } else {
                int returnCode = putReq.getCode();
                boolean isFatal = putReq.isFatal();
                
                FcpResultPut result;
                if( returnCode == 9 ) {
                    result = new FcpResultPut(FcpResultPut.KeyCollision, returnCode, desc, isFatal);
                } else if( returnCode == 5 ) {
                    result = new FcpResultPut(FcpResultPut.Retry, returnCode, desc, isFatal);
                } else {
                    result = new FcpResultPut(FcpResultPut.Error, returnCode, desc, isFatal);
                }
                FileTransferManager.inst().getUploadManager().notifyUploadFinished(ulItem, result);
            }
        }
    }

    private void handleNewRequests(
            Map<String,FcpPersistentPut> uploadRequests, 
            Map<String,FcpPersistentGet> downloadRequests) 
    {
        // check if there are new items, external or own started items
        {
            for( FcpPersistentGet dlReq : downloadRequests.values() ) {
                FrostDownloadItem dlItem = downloadModelItems.get(dlReq.getIdentifier());
                if( dlItem != null ) {
                    applyState(dlItem, dlReq);
                } else {
                    if ( showExternalItemsDownload ) {
                        dlItem = new FrostDownloadItem(
                                dlReq.getFilename(), 
                                dlReq.getUri());
                        dlItem.setExternal(true);
                        dlItem.setGqIdentifier(dlReq.getIdentifier());
                        dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
                        downloadModel.addExternalItem(dlItem);

                        applyState(dlItem, dlReq);
                    }
                }
            }
        }
        {
            for( FcpPersistentPut ulReq : uploadRequests.values() ) {
                FrostUploadItem ulItem = uploadModelItems.get(ulReq.getIdentifier());
                if( ulItem != null ) {
                    applyState(ulItem, ulReq);
                } else {
                    if( showExternalItemsUpload ) {
                        ulItem = new FrostUploadItem();
                        ulItem.setGqIdentifier(ulReq.getIdentifier());
                        ulItem.setExternal(true);
                        ulItem.setFile(new File(ulReq.getFilename()));
                        ulItem.setState(FrostUploadItem.STATE_PROGRESS);
                        uploadModel.addExternalItem(ulItem);

                        applyState(ulItem, ulReq);
                    }
                }
            }
        }
    }
    
    private void handleDisappearedItems(
            Map<String,FcpPersistentPut> uploadRequests, 
            Map<String,FcpPersistentGet> downloadRequests) 
    {
        // remove external items, or flag internal items as disappeared from global queue
        {
            List<ModelItem> itemsToRemove = new LinkedList<ModelItem>();
            for(int x=0; x < uploadModel.getItemCount(); x++) {
                FrostUploadItem ulItem = (FrostUploadItem) uploadModel.getItemAt(x);
                if( ulItem.getGqIdentifier() != null ) {
                    if( ulItem.getState() == FrostUploadItem.STATE_PROGRESS || ulItem.isExternal() ) {
                        // this item should be in the global queue
                        boolean isInGlobalQueue = uploadRequests.containsKey(ulItem.getGqIdentifier());
                        if( !isInGlobalQueue ) {
                            if( ulItem.isExternal() ) {
                                itemsToRemove.add(ulItem);
                            } else {
                                ulItem.setEnabled(false);
                                ulItem.setState(FrostUploadItem.STATE_FAILED);
                                ulItem.setErrorCodeDescription("Disappeared from global queue");
                            }
                        }
                    }
                }
            }
            ModelItem[] ri = (ModelItem[]) itemsToRemove.toArray(new ModelItem[itemsToRemove.size()]);
            uploadModel.removeItems(ri);
        }
        {
            List<ModelItem> itemsToRemove = new LinkedList<ModelItem>();
            for(int x=0; x < downloadModel.getItemCount(); x++) {
                FrostDownloadItem dlItem = (FrostDownloadItem) downloadModel.getItemAt(x);
                if( dlItem.getGqIdentifier() != null ) {
                    if( dlItem.getState() == FrostDownloadItem.STATE_PROGRESS || dlItem.isExternal() ) {
                        // this item should be in the global queue
                        boolean isInGlobalQueue = downloadRequests.containsKey(dlItem.getGqIdentifier());
                        if( !isInGlobalQueue ) {
                            if( dlItem.isExternal() ) {
                                itemsToRemove.add(dlItem);
                            } else {
                                dlItem.setEnabled(false);
                                dlItem.setState(FrostUploadItem.STATE_FAILED);
                                dlItem.setErrorCodeDescription("Disappeared from global queue");
                            }
                        }
                    }
                }
            }
            ModelItem[] ri = (ModelItem[]) itemsToRemove.toArray(new ModelItem[itemsToRemove.size()]);
            downloadModel.removeItems(ri);
        }
    }

    private void startNewUploads() {
        boolean isLimited = true;
        int currentAllowedUploadCount = 0;
        {
            int allowedConcurrentUploads = Core.frostSettings.getIntValue(SettingsClass.UPLOAD_MAX_THREADS);
            if( allowedConcurrentUploads <= 0 ) {
                isLimited = false;
            } else {
                int runningUploads = 0;
                for(FrostUploadItem ulItem : uploadModelItems.values() ) {
                    if( ulItem.getState() == FrostUploadItem.STATE_PROGRESS) {
                        runningUploads++;
                    }
                }
                currentAllowedUploadCount = allowedConcurrentUploads - runningUploads;
                if( currentAllowedUploadCount < 0 ) {
                    currentAllowedUploadCount = 0;
                }
            }
        }
        {
            while( !isLimited || currentAllowedUploadCount > 0 ) {
                FrostUploadItem ulItem = FileTransferManager.inst().getUploadManager().selectNextUploadItem();
                if( ulItem == null ) {
                    break;
                }
                // start the upload
                if( isDDA() ) {
                    boolean doMime;
                    if( ulItem.isSharedFile() ) {
                        doMime = false;
                    } else {
                        doMime = true;
                    }
                    fcpTools.startPersistentPut(
                            ulItem.getGqIdentifier(),
                            ulItem.getFile(),
                            doMime);
                } else {
                    // if UploadManager selected this file then it is not already in progress!
                    directTransferQueue.appendItemToQueue(ulItem);
                }
                
                ulItem.setState(FrostUploadItem.STATE_PROGRESS);
                currentAllowedUploadCount--;
            }
        }
    }
    
    private void startNewDownloads() {
        boolean isLimited = true;
        int currentAllowedDownloadCount = 0;
        {
            int allowedConcurrentDownloads = Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_MAX_THREADS);
            if( allowedConcurrentDownloads <= 0 ) {
                isLimited = false;
            } else {
                int runningDownloads = 0;
                for(FrostDownloadItem dlItem : downloadModelItems.values() ) {
                    if( dlItem.getState() == FrostDownloadItem.STATE_PROGRESS) {
                        runningDownloads++;
                    }
                }
                currentAllowedDownloadCount = allowedConcurrentDownloads - runningDownloads;
                if( currentAllowedDownloadCount < 0 ) {
                    currentAllowedDownloadCount = 0;
                }
            }
        }
        {
            while( !isLimited || currentAllowedDownloadCount > 0 ) {
                FrostDownloadItem dlItem = FileTransferManager.inst().getDownloadManager().selectNextDownloadItem();
                if (dlItem == null) {
                    break;
                }
                // start the download
                String gqid = dlItem.getGqIdentifier();
                File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
                fcpTools.startPersistentGet(
                        dlItem.getKey(),
                        gqid,
                        targetFile);
                
                dlItem.setState(FrostDownloadItem.STATE_PROGRESS);
                currentAllowedDownloadCount--;
            }
        }
    }
    
    public boolean isDirectTransferInProgress(FrostDownloadItem dlItem) {
        String id = dlItem.getGqIdentifier();
        return directGETsInProgress.contains(id);
    }

    public boolean isDirectTransferInProgress(FrostUploadItem ulItem) {
        String id = ulItem.getGqIdentifier();
        if( directPUTsInProgress.contains(id) ) {
            return true;
        }
        if( directPUTsWithoutAnswer.contains(id) ) {
            return true;
        }
        return false;
    }
    
    private class DirectTransferThread extends Thread {
        
        public void run() {
            
            final int maxAllowedExceptions = 5;
            int occuredExceptions = 0;

            while(true) {
                try {
                    // if there is no work in queue this call waits for a new queue item
                    ModelItem item = directTransferQueue.getItemFromQueue();
                    
                    if( item == null ) {
                        // paranoia, should never happen
                        Mixed.wait(5*1000);
                        continue;
                    }

                    if( item instanceof FrostUploadItem ) {
                        // transfer bytes to node
                        FrostUploadItem ulItem = (FrostUploadItem) item;
                        // FIXME: provide item, state=Transfer to node, % shows progress
                        String gqid = ulItem.getGqIdentifier();
                        File sourceFile = ulItem.getFile();
                        boolean doMime;
                        if( ulItem.isSharedFile() ) {
                            doMime = false;
                        } else {
                            doMime = true;
                        }
                        NodeMessage answer = fcpTools.startDirectPersistentPut(gqid, sourceFile, doMime);
                        if( answer == null ) {
                            String desc = "Could not open a new FCP2 socket for direct put!";
                            FcpResultPut result = new FcpResultPut(FcpResultPut.Error, -1, desc, false);
                            FileTransferManager.inst().getUploadManager().notifyUploadFinished(ulItem, result);

                            logger.severe(desc);
                        } else {
                            // wait for an answer, don't start request again
                            directPUTsWithoutAnswer.add(gqid);
                        }

                        directPUTsInProgress.remove(gqid);

                    } else if( item instanceof FrostDownloadItem ) {
                        // transfer bytes from node
                        FrostDownloadItem dlItem = (FrostDownloadItem) item;
                        // FIXME: provide item, state=Transfer from node, % shows progress
                        String gqid = dlItem.getGqIdentifier();
                        File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());

                        NodeMessage answer = fcpTools.startDirectPersistentGet(gqid, targetFile);
                        if( answer != null ) {
                            FcpResultGet result = new FcpResultGet(true);
                            FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);
                        } else {
                            logger.severe("Could not open a new fcp socket for direct get!");
                            FcpResultGet result = new FcpResultGet(false);
                            FileTransferManager.inst().getDownloadManager().notifyDownloadFinished(dlItem, result, targetFile);
                        }
                        
                        directGETsInProgress.remove(gqid);
                    }
                    
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Exception catched",t);
                    occuredExceptions++;
                }
                
                if( occuredExceptions > maxAllowedExceptions ) {
                    logger.log(Level.SEVERE, "Stopping DirectTransferThread because of too much exceptions");
                    break;
                }
            }
        }
    }
    
    /**
     * A queue class that queues items waiting for its direct transfer (put to node or get from node).
     */
    private class DirectTransferQueue {
        
        private LinkedList<ModelItem> queue = new LinkedList<ModelItem>();
        
        public synchronized ModelItem getItemFromQueue() {
            try {
                // let dequeueing threads wait for work
                while( queue.isEmpty() ) {
                    wait();
                }
            } catch (InterruptedException e) {
                return null; // waiting abandoned
            }
            
            if( queue.isEmpty() == false ) {
                ModelItem item = queue.removeFirst();
                return item;
            }
            return null;
        }

        public synchronized void appendItemToQueue(FrostDownloadItem item) {
            String id = item.getGqIdentifier();
            directGETsInProgress.add(id);

            queue.addLast(item);
            notifyAll(); // notify all waiters (if any) of new record
        }

        public synchronized void appendItemToQueue(FrostUploadItem item) {
            String id = item.getGqIdentifier();
            directPUTsInProgress.add(id);
            
            queue.addLast(item);
            notifyAll(); // notify all waiters (if any) of new record
        }

        
        public synchronized int getQueueSize() {
            return queue.size();
        }
    }
}
