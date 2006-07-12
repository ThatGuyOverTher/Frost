/*
  DownloadModel.java / Frost

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
package frost.fileTransfer.download;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.util.model.*;

/**
 * This is the model that stores all FrostDownloadItems. 
 * 
 * Its implementation is thread-safe (subclasses should synchronize against
 * protected attribute data when necessary). It is also assumed that the load
 * and save methods will not be used while other threads are under way.
 */
public class DownloadModel extends OrderedModel implements Savable {
	
	private class RemoveChunksThread extends Thread {
		
		private ArrayList oldChunkFilesList;
		private String dlDir;
		
		/**
		 * @param al
		 * @param dlDir
		 */
		public RemoveChunksThread(ArrayList al, String dlDir) {
			this.oldChunkFilesList = al; 
			this.dlDir = dlDir;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			File[] files = (new File(dlDir)).listFiles();
			for (int i = 0; i < oldChunkFilesList.size(); i++) {
				String filename = (String) oldChunkFilesList.get(i);
				for (int j = 0; j < files.length; j++) { 
					// remove filename.data , .redirect, .checkblocks
					if (files[j].getName().equals(filename + ".data")
						|| files[j].getName().equals(filename + ".redirect")
						|| files[j].getName().equals(filename + ".checkblocks")) {
						logger.info("Removing " + files[j].getName());
						files[j].delete();
					}
				}
			}
		}
	}

	private static Logger logger = Logger.getLogger(DownloadModel.class.getName());

	private SettingsClass settings;

	public DownloadModel(SettingsClass frostSettings) {
		super();
		settings = frostSettings;
	}
	
	/**
	 * Will add this item to model if not already in model.
	 */
	public synchronized boolean addDownloadItem(FrostDownloadItem itemToAdd) {
		for (int x = 0; x < getItemCount(); x++) {
			FrostDownloadItem item = (FrostDownloadItem) getItemAt(x);
			if (item.getSHA1() != null
				&& item.getSHA1().equals(itemToAdd.getSHA1())
				&& item.getSourceBoard().getName().equals(itemToAdd.getSourceBoard().getName())) {
				// already in model (compared by SHA1)
				return false;
			}
			if (itemToAdd.getKey() != null
				&& item.getKey() != null
				&& item.getKey().equals(itemToAdd.getKey())) {
				// already in model (compared by key)
				return false;
			}
			if (item.getFileName().equals(itemToAdd.getFileName())) {
				// same name, but different key. - rename quitely
				int cnt = 2;
				while (true) {
					String nextNewName = itemToAdd.getFileName() + "_" + cnt;
					itemToAdd.setFileName(nextNewName);
					if (addDownloadItem(itemToAdd) == true) {
						// added to model
						return true;
					}
					cnt++;
				}
				// we should never come here
			}
		}
		// not in model, add
		addItem(itemToAdd);
		return true;
	}
	
	/**
	 * Returns true if the model contains an item with the given key.
	 */
	public synchronized boolean containsItemWithKey(String key) {
		for (int x = 0; x < getItemCount(); x++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(x);
			if (dlItem.getSHA1() != null && dlItem.getSHA1().equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes finished downloads from the download model.
	 */
	public synchronized void removeFinishedDownloads() {
		ArrayList items = new ArrayList();
		for (int i = getItemCount() - 1; i >= 0; i--) {
			FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(i);
			if (dlItem.getState() == FrostDownloadItem.STATE_DONE) {
				items.add(dlItem);
			}
		}
		if (items.size() > 0) {
			FrostDownloadItem[] itemsArray = new FrostDownloadItem[items.size()];
			for (int i = 0; i < itemsArray.length; i++) {
				itemsArray[i] = (FrostDownloadItem) items.get(i);
			}
			removeItems(itemsArray);
		}
	}
	
	/**
	 * Removes download items from the download model.
	 */
	public boolean removeItems(ModelItem[] items) {
		//First we remove the chunks from disk
		ArrayList oldChunkFilesList = new ArrayList(items.length);
		String dlDir = settings.getValue("downloadDirectory");
		for (int i = 0; i < items.length; i++) {
			FrostDownloadItem item = (FrostDownloadItem) items[i];
			oldChunkFilesList.add(item.getFileName());
		}
		RemoveChunksThread t = new RemoveChunksThread(oldChunkFilesList, dlDir);
		t.start();
		
		//And now we remove the items from the model
		return super.removeItems(items);
	}
	
	/**
	 * Called to reset the HTL value of the selected items.
	 */
	public void restartItems(ModelItem[] items) {
		// TODO: stop thread

		for (int x = items.length - 1; x >= 0; x--) {
			FrostDownloadItem dlItem = (FrostDownloadItem) items[x];
			// reset only waiting+failed items
			if (dlItem.getState() == FrostDownloadItem.STATE_FAILED
				|| dlItem.getState() == FrostDownloadItem.STATE_WAITING
				|| dlItem.getState() == FrostDownloadItem.STATE_DONE) {
				dlItem.setState(FrostDownloadItem.STATE_WAITING);
				dlItem.setRetries(0);
				dlItem.setLastDownloadStopTimeMillis(0);
				dlItem.setEnableDownload(Boolean.valueOf(true)); // enable download on restart
			}
		}
	}
	
	/**
	 * This method enables / disables download items in the model. If the
	 * enabled parameter is null, the current state of the item is inverted.
	 * @param enabled new state of the items. If null, the current state 
	 * 		  is inverted
	 */
	public synchronized void setAllItemsEnabled(Boolean enabled) {
		for (int x = 0; x < getItemCount(); x++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(x);
			if (dlItem.getState() != FrostDownloadItem.STATE_DONE) {
				dlItem.setEnableDownload(enabled);
			}
		}
	}
	
	/**
	 * This method enables / disables download items in the model. If the
	 * enabled parameter is null, the current state of the item is inverted.
	 * @param enabled new state of the items. If null, the current state 
	 * 		  is inverted
	 * @param items items to modify
	 */
	public void setItemsEnabled(Boolean enabled, ModelItem[] items) {
		for (int i = 0; i < items.length; i++) { 
			FrostDownloadItem item = (FrostDownloadItem) items[i];
			if (item.getState() != FrostDownloadItem.STATE_DONE) {
				item.setEnableDownload(enabled);
			}
		}
	}
	
	/**
	 * Removes all items from the download model.
	 */
	public synchronized void removeAllItems() {
		//First we remove the chunks from disk
		ArrayList oldChunkFilesList = new ArrayList(getItemCount());
		String dlDir = settings.getValue("downloadDirectory");
		for (int i = 0; i < getItemCount(); i++) {
			FrostDownloadItem item = (FrostDownloadItem) getItemAt(i);
			oldChunkFilesList.add(item.getFileName());
		}
		RemoveChunksThread t = new RemoveChunksThread(oldChunkFilesList, dlDir);
		t.start();
		
		//And now we remove the items from the model
		clear();
	}


	/**
	 * Saves the download model to database.
	 */
	public void save() throws StorageException {
        
        LinkedList itemList = new LinkedList();
        for (int x = 0; x < getItemCount(); x++) {
            FrostDownloadItem downloadItem = (FrostDownloadItem)getItemAt(x);
            itemList.add(downloadItem);
        }
        
        try {
            AppLayerDatabase.getDownloadFilesDatabaseTable().saveDownloadFiles(itemList);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving download items", e);
            throw new StorageException("Error saving download items");
        }
	}
	
	/**
	 * Initializes the model
	 */
	public void initialize() throws StorageException {
        
        List downloadItems; 
        try {
            downloadItems = AppLayerDatabase.getDownloadFilesDatabaseTable().loadDownloadFiles();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading download items", e);
            throw new StorageException("Error loading download items");
        }
        for(Iterator i=downloadItems.iterator(); i.hasNext(); ) {
            FrostDownloadItem di = (FrostDownloadItem)i.next();
            addDownloadItem(di);
        }
	}
}
