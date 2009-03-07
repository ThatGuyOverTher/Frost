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
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.fcp05.*;
import frost.fileTransfer.*;
import frost.storage.*;
import frost.storage.perst.*;
import frost.util.model.*;

/**
 * This is the model that stores all FrostDownloadItems.
 *
 * Its implementation is thread-safe (subclasses should synchronize against
 * protected attribute data when necessary). It is also assumed that the load
 * and save methods will not be used while other threads are under way.
 */
public class DownloadModel extends SortedModel implements ExitSavable {

	private static final Logger logger = Logger.getLogger(DownloadModel.class.getName());

	public DownloadModel(final SortedTableFormat f) {
		super(f);
	}

	/**
	 * Will add this item to model if not already in model.
	 */
	public synchronized boolean addDownloadItem(final FrostDownloadItem itemToAdd) {

        final FrostFileListFileObject flfToAdd = itemToAdd.getFileListFileObject(); // maybe null of manually added

		for (int x = 0; x < getItemCount(); x++) {
			final FrostDownloadItem item = (FrostDownloadItem) getItemAt(x);
            final FrostFileListFileObject flf = item.getFileListFileObject(); // maybe null of manually added

            if( flfToAdd != null && flf != null ) {
                if( flfToAdd.getSha().equals(flf.getSha()) ) {
                    // already in model (compared by SHA)
                    return false;
                }
            }

            // FIXME: 0.7: if we add a new uri chk/name also check if we already download chk!
            // Problem: what if CHK is wrong, then we have to add chk/name. But in the reverse case
            //          we add chk/name and name gets stripped because node reports rc=11, then we have 2 with same
            //          chk! ==> if node reports 11 then check if we have already same plain chk.

			if (itemToAdd.getKey() != null
				&& item.getKey() != null
				&& item.getKey().equals(itemToAdd.getKey()))
            {
				// already in model (compared by key)
				return false;
			}

            // FIXME: also check downloaddir for same filename and build new name
            if (item.getFilename().equals(itemToAdd.getFilename())) {
				// same name, but different key. - rename quitely
				int cnt = 2;
				while (true) {
					final String nextNewName = itemToAdd.getFilename() + "_" + cnt;
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

    public void addExternalItem(final FrostDownloadItem i) {
        addItem(i);
    }

	/**
	 * Returns true if the model contains an item with the given sha.
	 */
	public synchronized boolean containsItemWithSha(final String sha) {
		for (int x = 0; x < getItemCount(); x++) {
			final FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(x);
            final FrostFileListFileObject flf = dlItem.getFileListFileObject();
            if( flf != null ) {
                if( flf.getSha().equals(sha) ) {
                    return true;
                }
            }
		}
		return false;
	}

	/**
	 * Removes finished downloads from the download model.
	 */
	public synchronized void removeFinishedDownloads() {
		final ArrayList<FrostDownloadItem> items = new ArrayList<FrostDownloadItem>();
		for (int i = getItemCount() - 1; i >= 0; i--) {
			final FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(i);
			if (dlItem.getState() == FrostDownloadItem.STATE_DONE) {
				items.add(dlItem);
			}
		}
		if (items.size() > 0) {
			final FrostDownloadItem[] itemsArray = new FrostDownloadItem[items.size()];
			for (int i = 0; i < itemsArray.length; i++) {
				itemsArray[i] = items.get(i);
			}
			removeItems(itemsArray);
		}
	}

    /**
     * Removes external downloads from the download model.
     */
    public synchronized void removeExternalDownloads() {
        final ArrayList<FrostDownloadItem> items = new ArrayList<FrostDownloadItem>();
        for (int i = getItemCount() - 1; i >= 0; i--) {
            final FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(i);
            if (dlItem.isExternal()) {
                items.add(dlItem);
            }
        }
        if (items.size() > 0) {
            final FrostDownloadItem[] itemsArray = new FrostDownloadItem[items.size()];
            items.toArray(itemsArray);
            removeItems(itemsArray);
        }
    }

	/**
	 * Removes download items from the download model.
	 */
	@Override
    public boolean removeItems(final ModelItem[] items) {
		// First we remove the chunks from disk
		final ArrayList<String> oldChunkFilesList = new ArrayList<String>(items.length);
		final String dlDir = Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD);
		for( final ModelItem element : items ) {
			final FrostDownloadItem item = (FrostDownloadItem) element;
			oldChunkFilesList.add(item.getFilename());
		}
		final RemoveChunksThread t = new RemoveChunksThread(oldChunkFilesList, dlDir);
		t.start();

		// And now we remove the items from the model
		return super.removeItems(items);
	}

	/**
	 * Called to restart the item.
	 */
	public void restartItems(final ModelItem[] items) {
		for (int x = items.length - 1; x >= 0; x--) {
			final FrostDownloadItem dlItem = (FrostDownloadItem) items[x];
			// reset only waiting+failed items
			if (dlItem.getState() == FrostDownloadItem.STATE_FAILED
				|| dlItem.getState() == FrostDownloadItem.STATE_WAITING
				|| dlItem.getState() == FrostDownloadItem.STATE_DONE)
            {
				dlItem.setState(FrostDownloadItem.STATE_WAITING);
				dlItem.setRetries(0);
				dlItem.setLastDownloadStopTime(0);
				dlItem.setEnabled(Boolean.valueOf(true)); // enable download on restart
			}
		}
	}

	/**
	 * This method enables / disables download items in the model. If the
	 * enabled parameter is null, the current state of the item is inverted.
	 * @param enabled new state of the items. If null, the current state
	 * 		  is inverted
	 */
	public synchronized void setAllItemsEnabled(final Boolean enabled) {
		for (int x = 0; x < getItemCount(); x++) {
			final FrostDownloadItem dlItem = (FrostDownloadItem) getItemAt(x);
			if (dlItem.getState() != FrostDownloadItem.STATE_DONE) {
				dlItem.setEnabled(enabled);
                FileTransferManager.inst().getDownloadManager().notifyDownloadItemEnabledStateChanged(dlItem);
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
	public void setItemsEnabled(final Boolean enabled, final ModelItem[] items) {
		for( final ModelItem element : items ) {
			final FrostDownloadItem item = (FrostDownloadItem) element;
			if (item.getState() != FrostDownloadItem.STATE_DONE) {
				item.setEnabled(enabled);
                FileTransferManager.inst().getDownloadManager().notifyDownloadItemEnabledStateChanged(item);
			}
		}
	}

//	/**
//	 * Removes all items from the download model.
//	 */
//	public synchronized void removeAllItems() {
//		//First we remove the chunks from disk
//		ArrayList oldChunkFilesList = new ArrayList(getItemCount());
//		String dlDir = Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD);
//		for (int i = 0; i < getItemCount(); i++) {
//			FrostDownloadItem item = (FrostDownloadItem) getItemAt(i);
//			oldChunkFilesList.add(item.getFileName());
//		}
//		RemoveChunksThread t = new RemoveChunksThread(oldChunkFilesList, dlDir);
//		t.start();
//
//		//And now we remove the items from the model
//		clear();
//	}

	/**
	 * Saves the download model to database.
	 */
	public void exitSave() throws StorageException {

        final List<FrostDownloadItem> itemList = getItems();
        try {
            FrostFilesStorage.inst().saveDownloadFiles(itemList);
        } catch (final Throwable e) {
            logger.log(Level.SEVERE, "Error saving download items", e);
            throw new StorageException("Error saving download items");
        }
	}

	/**
	 * Initializes the model
	 */
	public void initialize() throws StorageException {

        List<FrostDownloadItem> downloadItems;
        try {
            downloadItems = FrostFilesStorage.inst().loadDownloadFiles();
        } catch (final Throwable e) {
            logger.log(Level.SEVERE, "Error loading download items", e);
            throw new StorageException("Error loading download items");
        }
        for( final FrostDownloadItem di : downloadItems ) {
            addDownloadItem(di);
        }
	}

    public boolean restartRunningDownloads(final List<FrostDownloadItem> dlItems) {

        final FrostDownloadItem[] itemsArray = new FrostDownloadItem[dlItems.size()];

        // don't flag as failed later when item is removed from gq
        for( final FrostDownloadItem dlItem : dlItems ) {
            dlItem.setInternalRemoveExpected(true);
        }

        dlItems.toArray(itemsArray);

        removeItems(itemsArray);

        new Thread() {
            @Override
            public void run() {
                // TODO: (ugly) wait until item is removed from global queue before starting download with same gq identifier
                try { Thread.sleep(1500); } catch (final InterruptedException e) {}
                for( final FrostDownloadItem dlItem : dlItems ) {
                    dlItem.setState(FrostDownloadItem.STATE_WAITING);
                    dlItem.setRetries(0);
                    dlItem.setLastDownloadStopTime(0);
                    addDownloadItem(dlItem);
                }
            }
        }.start();
        return true;
    }

    private class RemoveChunksThread extends Thread {

        private final ArrayList<String> oldChunkFilesList;
        private final String dlDir;

        public RemoveChunksThread(final ArrayList<String> al, final String dlDir) {
            this.oldChunkFilesList = al;
            this.dlDir = dlDir;
        }

        @Override
        public void run() {
            final File[] files = (new File(dlDir)).listFiles();
            for (int i = 0; i < oldChunkFilesList.size(); i++) {
                final String filename = oldChunkFilesList.get(i);
                for( final File element : files ) {
                    // remove filename.data , .redirect, .checkblocks
                    if (element.getName().equals(filename + FecSplitfile.FILE_DATA_EXTENSION)
                        || element.getName().equals(filename + FecSplitfile.FILE_REDIRECT_EXTENSION)
                        || element.getName().equals(filename + FecSplitfile.FILE_CHECKBLOCKS_EXTENSION)) {
                        logger.info("Removing " + element.getName());
                        element.delete();
                    }
                }
            }
        }
    }
}
