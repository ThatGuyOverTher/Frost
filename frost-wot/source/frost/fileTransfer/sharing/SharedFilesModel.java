/*
  SharedFilesModel.java / Frost
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
package frost.fileTransfer.sharing;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.fileTransfer.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.util.model.*;

/**
 * This is the model that stores all FrostUploadItems.
 *
 * Its implementation is thread-safe (subclasses should synchronize against
 * protected attribute data when necessary). It is also assumed that the load
 * and save methods will not be used while other threads are under way.
 */
public class SharedFilesModel extends OrderedModel implements Savable {
    
    // FIXME: for shared directories: add new files to another table, waiting for owner assignment

    private static Logger logger = Logger.getLogger(SharedFilesModel.class.getName());

    public SharedFilesModel() {
        super();
    }

    /**
     * Will add this item to the model if not already in the model.
     * The new item must only have 1 FrostUploadItemOwnerBoard in its list.
     */
    public synchronized boolean addNewSharedFile(FrostSharedFileItem itemToAdd) {
        for (int x = 0; x < getItemCount(); x++) {
            FrostSharedFileItem item = (FrostSharedFileItem) getItemAt(x);
            // add if file is not shared already
            if( itemToAdd.getSha().equals(item.getSha()) ) {
                // is already in list
                return false;
            }
        }
        // not in model, add
        addItem(itemToAdd);
        return true;
    }

    /**
     * Will add this item to the model, no check for dups.
     */
    private synchronized void addConsistentSharedFileItem(FrostSharedFileItem itemToAdd) {
        addItem(itemToAdd);
    }

    /**
     * Returns true if the model contains an item with the given key.
     */
    public synchronized boolean containsItemWithSha(String sha) {
        for (int x = 0; x < getItemCount(); x++) {
            FrostSharedFileItem sfItem = (FrostSharedFileItem) getItemAt(x);
            if (sfItem.getSha() != null && sfItem.getSha().equals(sha)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method removes from the model the items whose associated files
     * no longer exist on hard disk. Using this method may be very expensive
     * if the model has a lot of items.
     */
    public synchronized void removeNotExistingFiles() {
        ArrayList items = new ArrayList();
        for (int i = getItemCount() - 1; i >= 0; i--) {
            FrostSharedFileItem sfItem = (FrostSharedFileItem) getItemAt(i);
            if (!sfItem.getFile().exists()) {
                items.add(sfItem);
            }
        }
        if (items.size() > 0) {
            FrostSharedFileItem[] itemsArray = new FrostSharedFileItem[items.size()];
            for (int i = 0; i < itemsArray.length; i++) {
                itemsArray[i] = (FrostSharedFileItem) items.get(i);
            }
            removeItems(itemsArray);
        }
    }

    /**
     * This method tells all items to start uploading (if their current state allows it)
     */
    public synchronized void requestAllItems() {
        Iterator iterator = data.iterator();
        while (iterator.hasNext()) {
            FrostSharedFileItem sfItem = (FrostSharedFileItem) iterator.next();
            if( !sfItem.isCurrentlyUploading() ) {
                FileTransferManager.getInstance().getUploadManager().getModel().addNewUploadItemFromSharedFile(sfItem);
            }
        }
    }

    /**
     * This method tells items passed as a parameter to start uploading (if their current state allows it)
     */
    public void requestItems(ModelItem[] items) {
        for (int i = 0; i < items.length; i++) {
            FrostSharedFileItem sfItem = (FrostSharedFileItem) items[i];
            if( !sfItem.isCurrentlyUploading() ) {
                FileTransferManager.getInstance().getUploadManager().getModel().addNewUploadItemFromSharedFile(sfItem);
            }
        }
    }

    /**
     * Initializes the model
     */
    public void initialize() throws StorageException {
        List uploadItems; 
        try {
            uploadItems = AppLayerDatabase.getSharedFilesDatabaseTable().loadSharedFiles();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading upload items", e);
            throw new StorageException("Error loading upload items");
        }
        for(Iterator i=uploadItems.iterator(); i.hasNext(); ) {
            FrostSharedFileItem di = (FrostSharedFileItem)i.next();
            addConsistentSharedFileItem(di); // no check for dups
        }
    }
    
    /**
     * Saves the upload model to database.
     */
    public void save() throws StorageException {
        List itemList = getItems();
        try {
            AppLayerDatabase.getSharedFilesDatabaseTable().saveSharedFiles(itemList);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving shared file items", e);
            throw new StorageException("Error saving shared file items");
        }
    }
}
