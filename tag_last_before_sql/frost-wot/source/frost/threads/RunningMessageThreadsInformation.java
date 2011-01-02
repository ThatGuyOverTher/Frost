/*
  RunningMessageThreadsInformation.java / Frost
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

/**
 * Contains all information about running message uploads and downloads.
 */
public class RunningMessageThreadsInformation {
    
    private int uploadingBoardCount = 0;
    private int runningUploadThreadCount = 0;
    
    private int attachmentsToUploadCount = 0;
    private int attachmentsToUploadRemainingCount = 0;
    
    private int downloadingBoardCount = 0;
    private int runningDownloadThreadCount = 0;
    
    /**
     * Returns the count of all file attachments of all uploading messages.
     */
    public int getAttachmentsToUploadCount() {
        return attachmentsToUploadCount;
    }
    /**
     * Returns the count of all file attachments of all uploading messages
     * that still wait to be uploaded.
     */
    public int getAttachmentsToUploadRemainingCount() {
        return attachmentsToUploadRemainingCount;
    }
    /**
     * Returns the count of ALL running download threads (of all boards).
     */
    public int getRunningDownloadThreadCount() {
        return runningDownloadThreadCount;
    }
    /**
     * Returns the count of ALL running upload threads (of all boards).
     */
    public int getRunningUploadThreadCount() {
        return runningUploadThreadCount;
    }
    /**
     * Returns the count of boards that currently have running download threads.
     */
    public int getDownloadingBoardCount() {
        return downloadingBoardCount;
    }
    /**
     * Returns the count of boards that currently have running upload threads.
     */
    public int getUploadingBoardCount() {
        return uploadingBoardCount;
    }
    
    public void addToAttachmentsToUploadCount(int valueToAdd) {
        this.attachmentsToUploadCount += valueToAdd;
    }
    public void addToAttachmentsToUploadRemainingCount(int valueToAdd) {
        this.attachmentsToUploadRemainingCount += valueToAdd;
    }
    public void addToRunningDownloadThreadCount(int valueToAdd) {
        this.runningDownloadThreadCount += valueToAdd;
    }
    public void addToRunningUploadThreadCount(int valueToAdd) {
        this.runningUploadThreadCount += valueToAdd;
    }
    public void addToDownloadingBoardCount(int valueToAdd) {
        this.downloadingBoardCount += valueToAdd;
    }
    public void addToUploadingBoardCount(int valueToAdd) {
        this.uploadingBoardCount += valueToAdd;
    }
}
