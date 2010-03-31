/*
  FrostUploadItem.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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

import java.io.*;

import frost.*;
import frost.fileTransfer.sharing.*;
import frost.util.*;
import frost.util.model.*;

/**
 * Represents a file to upload.
 */
public class FrostUploadItem extends ModelItem implements CopyToClipboardItem {

    // the constants representing upload states
    public final static int STATE_DONE       = 1;   // a start of uploading is requested
//    public final static int STATE_UPLOADING  = 3;
    public final static int STATE_PROGRESS   = 4;   // upload runs, shows "... kb"
    public final static int STATE_ENCODING_REQUESTED  = 5; // an encoding of file is requested
    public final static int STATE_ENCODING   = 6;   // the encode is running
    public final static int STATE_WAITING    = 7;   // waiting until the next retry
    public final static int STATE_FAILED     = 8;

    private File file = null;
    private long fileSize = 0;
    private String chkKey = null;
    private Boolean enabled = Boolean.TRUE;
    private int state;
    private long uploadAddedMillis = 0;
    private long uploadStartedMillis = 0;
    private long uploadFinishedMillis = 0;
    private int retries = 0;
    private long lastUploadStopTimeMillis = 0; // millis when upload stopped the last time, needed to schedule uploads
    private String gqIdentifier = null;

    private boolean isLoggedToFile = false;

    // non-persistent fields
    private int totalBlocks = -1;
    private int doneBlocks = -1;
    private Boolean isFinalized = null;
    private String errorCodeDescription = null;
    private int priority = -1;

    // is only set if this uploaditem is a shared file
    private FrostSharedFileItem sharedFileItem = null;

    private boolean isExternal = false;

    private transient boolean internalRemoveExpected = false;
    private transient boolean stateShouldBeProgress = false;

    /**
     * Dummy to use for uploads of attachments. Is never saved.
     * Attachment uploads must never be persistent on 0.7.
     * We indicate this with gqIdentifier == null
     * Also used for external global queue items on 0.7.
     */
    public FrostUploadItem() {
    }

    /**
     * Used to add a new file to upload.
     * Either manually added or a shared file.
     */
    public FrostUploadItem(final File newFile) {

        file = newFile;
        fileSize = file.length();

        gqIdentifier = buildGqIdentifier(file.getName());

        uploadAddedMillis = System.currentTimeMillis();

        state = STATE_WAITING;
    }

    /**
     * Constructor used by loadUploadTable.
     */
    public FrostUploadItem(
            final File newFile,
            final long newFilesize,
            final String newKey,
            final boolean newIsEnabled,
            final int newState,
            final long newUploadAdded,
            final long newUploadStarted,
            final long newUploadFinished,
            final int newRetries,
            final long newLastUploadStopTimeMillis,
            final String newGqIdentifier,
            final boolean newIsLoggedToFile)
    {
        file = newFile;
        fileSize = newFilesize;
        chkKey = newKey;
        enabled = Boolean.valueOf(newIsEnabled);
        state = newState;
        uploadAddedMillis = newUploadAdded;
        uploadStartedMillis = newUploadStarted;
        uploadFinishedMillis = newUploadFinished;
        retries = newRetries;
        lastUploadStopTimeMillis = newLastUploadStopTimeMillis;
        gqIdentifier = newGqIdentifier;
        isLoggedToFile = newIsLoggedToFile;

        // set correct state
        if( state == FrostUploadItem.STATE_PROGRESS ) {
            // upload was running at end of last shutdown
            if( !isSharedFile() ) {
                stateShouldBeProgress = true;
            }
            state = FrostUploadItem.STATE_WAITING;
        } else if ((state == FrostUploadItem.STATE_ENCODING) || (state == FrostUploadItem.STATE_ENCODING_REQUESTED)) {
            state = FrostUploadItem.STATE_WAITING;
        }
    }

    public boolean isSharedFile() {
        return getSharedFileItem() != null;
    }

    public long getFileSize() {
        return fileSize;
    }
    public void setFileSize(final Long newFileSize) {
        fileSize = newFileSize.longValue();
        fireChange();
    }

    public String getKey() {
        return chkKey;
    }
    public void setKey(final String newKey) {
        chkKey = newKey;
        fireChange();
    }

    public int getState() {
        return state;
    }
    public void setState(final int newState) {
        state = newState;
        fireChange();
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }
    public void setTotalBlocks(final int newTotalBlocks) {
        totalBlocks = newTotalBlocks;
    }

    public int getRetries() {
        return retries;
    }
    public void setRetries(final int newRetries) {
        retries = newRetries;
        fireChange();
    }

    public int getDoneBlocks() {
        return doneBlocks;
    }
    public void setDoneBlocks(final int newDoneBlocks) {
        doneBlocks = newDoneBlocks;
    }

    /**
     * @param enabled new enable status of the item. If null, the current status is inverted
     */
    public void setEnabled(Boolean newEnabled) {
        if (newEnabled == null && enabled != null) {
            //Invert the enable status
            final boolean temp = enabled.booleanValue();
            newEnabled = Boolean.valueOf(!temp);
        }
        enabled = newEnabled;
        fireChange();
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public long getLastUploadStopTimeMillis() {
        return lastUploadStopTimeMillis;
    }
    public void setLastUploadStopTimeMillis(final long lastUploadStopTimeMillis) {
        this.lastUploadStopTimeMillis = lastUploadStopTimeMillis;
    }

    public long getUploadAddedMillis() {
        return uploadAddedMillis;
    }
    public long getUploadStartedMillis() {
        return uploadStartedMillis;
    }
    public void setUploadStartedMillis(final long v) {
        uploadStartedMillis = v;
        fireChange();
    }

    public long getUploadFinishedMillis() {
        return uploadFinishedMillis;
    }
    public void setUploadFinishedMillis(final long v) {
        uploadFinishedMillis = v;
        fireChange();
    }

    public String getGqIdentifier() {
        return gqIdentifier;
    }
    public void setGqIdentifier(final String i) {
        gqIdentifier = i;
    }

    public FrostSharedFileItem getSharedFileItem() {
        return sharedFileItem;
    }

    public void setSharedFileItem(final FrostSharedFileItem sharedFileItem) {
        this.sharedFileItem = sharedFileItem;
    }

    public String getFilename() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }
    public void setFile(final File f) {
        file = f;
    }

    public Boolean isFinalized() {
        return isFinalized;
    }
    public void setFinalized(final boolean finalized) {
        if( finalized ) {
            isFinalized = Boolean.TRUE;
        } else {
            isFinalized = Boolean.FALSE;
        }
    }

    public void fireValueChanged() {
        super.fireChange();
    }

    /**
     * Builds a global queue identifier.
     */
    private String buildGqIdentifier(final String filename) {
        return new StringBuilder()
            .append("Frost-")
            .append(filename.replace(' ', '_'))
            .append("-")
            .append(System.currentTimeMillis())
            .append(Core.getCrypto().getSecureRandom().nextInt(10)) // 0-9
            .toString();
    }

    public String getErrorCodeDescription() {
        return errorCodeDescription;
    }
    public void setErrorCodeDescription(final String errorCodeDescription) {
        this.errorCodeDescription = errorCodeDescription;
    }

    /**
     * @return  true if this item is an external global queue item
     */
    public boolean isExternal() {
        return isExternal;
    }
    public void setExternal(final boolean e) {
        isExternal = e;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
        super.fireChange();
    }

    /**
     * @return  true if the remove of this request was expected and item should not be removed from the table
     */
    public boolean isInternalRemoveExpected() {
        return internalRemoveExpected;
    }
    /**
     * onPersistentRequestRemoved method checks this and does not remove the request
     * from the table if the remove was expected.
     */
    public void setInternalRemoveExpected(final boolean internalRemoveExpected) {
        this.internalRemoveExpected = internalRemoveExpected;
    }

    @Override
    public String toString() {
        return getFilename();
    }

    public boolean isLoggedToFile() {
        return isLoggedToFile;
    }

    public void setLoggedToFile(final boolean isLoggedToFile) {
        this.isLoggedToFile = isLoggedToFile;
    }

    public boolean isStateShouldBeProgress() {
        return stateShouldBeProgress;
    }
}
