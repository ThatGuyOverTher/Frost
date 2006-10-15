/*
  FrostSharedFileItem.java / Frost
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

import java.io.*;
import java.util.*;

import frost.fileTransfer.*;
import frost.fileTransfer.upload.*;
import frost.gui.model.*;
import frost.threads.*;
import frost.util.*;
import frost.util.model.*;

/**
 * This item is shown in the shared files panel, and it can be shown in the
 * table of current uploads.
 */
public class FrostSharedFileItem extends ModelItem {
    
    String sha = null;
    
    File file = null;
    long fileSize = 0;
    String chkKey = null;
    
    String owner = null;
    String comment = null;
    int rating = 0;
    String keywords = null;
    
    long lastUploaded = 0;  
    int uploadCount = 0;

    long refLastSent = 0;              // last time we sent this file inside a CHK file; set to 0 if item was changed
    
    long requestLastReceived = 0;      // time when we received the last request for this sha
    int requestsReceived = 0;          // received requests count

    /**
     * Used to add a new file. SHA1 must be created and must not be already in table!
     */
    public FrostSharedFileItem(File newFile, String newOwner, String newSha) {
        file = newFile;
        fileSize = file.length();
        owner = newOwner;
        sha = newSha;
    }

    /**
     * Constructor used by loadUploadTable
     */
    public FrostSharedFileItem(
            File newFile,
            long newFilesize,
            String newKey,
            String newSha,
            String newOwner,
            String newComment,
            int newRating,
            String newKeywords,
            long newLastUploaded,
            int newUploadCount,
            long newRefLastSent,
            long newRequestLastReceived,
            int newRequestsReceived)
    {
        sha = newSha;
        file = newFile;
        fileSize = newFilesize;
        chkKey = newKey;
        owner = newOwner;
        comment = newComment;
        rating = newRating;
        keywords = newKeywords;
        lastUploaded = newLastUploaded;
        uploadCount = newUploadCount;
        refLastSent = newRefLastSent;
        requestLastReceived = newRequestLastReceived;
        requestsReceived = newRequestsReceived;
    }

    /**
     * Returns the object representing value of column. Can be string or icon
     *
     * @param   column  Column to be displayed
     * @return  Object representing table entry.
     */
    public Object getValueAt(int column) {
       return null;
    }

    public int compareTo( TableMember anOther, int tableColumIndex ) {
        Comparable c1 = (Comparable)getValueAt(tableColumIndex);
        Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
        return c1.compareTo( c2 );
    }

    /**
     * Builds an instance that can be serialized to XML.
     * Used to send file references in index file.
     */
    public SharedFileXmlFile getSharedFileXmlFileInstance() {
        SharedFileXmlFile sfxf = new SharedFileXmlFile();

        sfxf.setSha(getSha());
        sfxf.setSize(getFileSize());
        sfxf.setKey(getChkKey());
        sfxf.setFilename(getFile().getName());
        if( getLastUploaded() != 0 ) {
            sfxf.setLastUploaded(DateFun.FORMAT_DATE_EXT.print(getLastUploaded()));
        } else {
            sfxf.setLastUploaded(null);
        }
        sfxf.setComment(getComment());
        sfxf.setRating(getRating());
        sfxf.setKeywords(getKeywords());
        
        return sfxf;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
        fireChange();
        itemWasChanged();
        userActionOccured();
    }

    public String getKeywords() {
        return keywords;
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
        fireChange();
        itemWasChanged();
        userActionOccured();
    }

    public long getRefLastSent() {
        return refLastSent;
    }
    public void setRefLastSent(long refLastSent) {
        this.refLastSent = refLastSent;
    }

    public long getLastUploaded() {
        return lastUploaded;
    }
    public void setLastUploaded(long lastUploaded) {
        this.lastUploaded = lastUploaded;
        fireChange();
    }

    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
        fireChange();
        itemWasChanged();
        userActionOccured();
    }

    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
        fireChange();
        itemWasChanged();
        userActionOccured();
    }

    public long getRequestLastReceived() {
        return requestLastReceived;
    }
    public void setRequestLastReceived(long requestLastReceived) {
        this.requestLastReceived = requestLastReceived;
        fireChange();
    }

    public int getRequestsReceived() {
        return requestsReceived;
    }
    public void setRequestsReceived(int requestsReceived) {
        this.requestsReceived = requestsReceived;
        fireChange();
    }

    public int getUploadCount() {
        return uploadCount;
    }
    public void setUploadCount(int uploadCount) {
        this.uploadCount = uploadCount;
        fireChange();
    }

    public String getSha() {
        return sha;
    }
    
    public void itemWasChanged() {
        // force a send in CHK next time
        this.refLastSent = 0; 
    }
    
    public void userActionOccured() {
        // notify list upload thread that user changed something
        FileListUploadThread.getInstance().userActionOccured();
    }

    public String getChkKey() {
        return chkKey;
    }

    public void setChkKey(String chkKey) {
        this.chkKey = chkKey;
        itemWasChanged();
    }

    public File getFile() {
        return file;
    }

    public long getFileSize() {
        return fileSize;
    }
    
    public void notifySuccessfulUpload(String newKey) {
        // an upload of this file was successful
        setChkKey(newKey);
        setLastUploaded(System.currentTimeMillis());
        setUploadCount(getUploadCount() + 1);
        
        itemWasChanged();
    }
    
    public boolean isCurrentlyUploading() {
        List uploadItems = FileTransferManager.getInstance().getUploadManager().getModel().getItems();
        for(Iterator k = uploadItems.iterator(); k.hasNext(); ) {
            FrostUploadItem ulItem = (FrostUploadItem) k.next();
            FrostSharedFileItem sfi = ulItem.getSharedFileItem();
            if( sfi == this ) {
                // this upload item is for this shared file
                // the file is only in upload table if it really uploads currently
                return true;
            }
        }
        return false; // not in upload table
    }
}
