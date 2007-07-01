/*
  DownloadTicker.java / Frost

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

import frost.*;
import frost.fileTransfer.*;
import frost.util.*;

public class DownloadTicker extends Thread {

	private DownloadPanel panel;
	private DownloadModel model;

	/**
	 * The number of allocated threads is used to limit the total of threads
	 * that can be running at a given time, whereas the number of running
	 * threads is the number of threads that are actually running.
	 */
	private int allocatedThreads = 0;
	private int runningThreads = 0;
	
	private Object threadCountLock = new Object();
    
    DownloadStatusPanel statusPanel;
	
	public DownloadTicker(DownloadModel newModel, DownloadPanel newPanel, DownloadStatusPanel newStatusPanel) {
		super("Download");
		model = newModel;
		panel = newPanel;
        statusPanel = newStatusPanel;
	}
	
	/**
	 * This method is called to find out if a new thread can start. It temporarily 
	 * allocates it and it will have to be relased when it is no longer
	 * needed (no matter whether the thread was actually used or not).
	 * @return true if a new thread can start. False otherwise.
	 */
	private void allocateDownloadThread() {
		synchronized (threadCountLock) {
			allocatedThreads++;
		}
	}

    private boolean canAllocateDownloadThread() {
         synchronized (threadCountLock) {
             if (allocatedThreads < Core.frostSettings.getIntValue(SettingsClass.DOWNLOAD_MAX_THREADS)) {
                 return true;
             } 
         }
         return false;   
    }

	/**
	 * This method is used to release a thread.
	 */
	private void releaseThread() {
		synchronized (threadCountLock) {
			if (allocatedThreads > 0) {
				allocatedThreads--;
			} 
		}
	}

	/**
	 * This method returns the number of threads that are running
	 * @return the number of threads that are running
	 */
	public int getRunningThreads() {
		return runningThreads;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		super.run();
		while (true) {
			Mixed.wait(1000);
			// this method is called by a timer each second
			updateDownloadCountLabel();
            if( PersistenceManager.isPersistenceEnabled() == false ) {
                startDownloadThread();
            }
		}
	}
	
	/**
	 * This method is usually called from a thread to notify the ticker that
	 * the thread has finished (so that it can notify its listeners of the fact). It also
	 * releases the thread so that new threads can start if needed.
	 */
	void threadFinished() {
		runningThreads--;
		releaseThread();
	}
	
	/**
	 * This method is called from a thread to notify the ticker that
	 * the thread has started (so that it can notify its listeners of the fact)
	 */
	void threadStarted() {
		runningThreads++;
	}

	/**
	 * Updates the download items count label. The label shows all WAITING items in download table.
	 * Called periodically by timer_actionPerformed().
	 */
	public void updateDownloadCountLabel() {
		int waitingItems = 0;
        int runningItems = 0;
		for (int x = 0; x < model.getItemCount(); x++) {
			FrostDownloadItem dlItem = (FrostDownloadItem) model.getItemAt(x);
			if (dlItem.getState() != FrostDownloadItem.STATE_DONE 
                    && dlItem.getState() != FrostDownloadItem.STATE_FAILED) 
            {
				waitingItems++;
			}
            if (dlItem.getState() == FrostDownloadItem.STATE_PROGRESS) {
                runningItems++;
            }
		}
		panel.setDownloadItemCount(waitingItems);
        statusPanel.numberChanged(runningItems);
	}

    /**
     * Maybe start a new download automatically.
     */
	private void startDownloadThread() {
        if( Core.isFreenetOnline() && panel.isDownloadingActivated() && canAllocateDownloadThread() ) {
            FrostDownloadItem dlItem = FileTransferManager.inst().getDownloadManager().selectNextDownloadItem();
            startDownload(dlItem);
        }
    }
	
	public boolean startDownload(FrostDownloadItem dlItem) {

	    if (!Core.isFreenetOnline() ) {
            return false;
        }
        if( dlItem == null || dlItem.getState() != FrostDownloadItem.STATE_WAITING ) {
            return false;
        }

        // increase allocated threads
        allocateDownloadThread();

        dlItem.setState(FrostDownloadItem.STATE_TRYING);

        File targetFile = new File(Core.frostSettings.getValue(SettingsClass.DIR_DOWNLOAD) + dlItem.getFilename());
        DownloadThread newRequest = new DownloadThread(this, dlItem, targetFile);
        newRequest.start();
        return true;
	}
}
