/*
 FrostSharedFileObject.java / Frost
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

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fileTransfer.download.*;
import frost.identities.*;
import frost.storage.database.applayer.*;
import frost.util.*;

public class FrostFileListFileObject {

    private static Logger logger = Logger.getLogger(FrostFileListFileObject.class.getName());

    private Long primkey = null;
    private String sha = null;  // SHA of the file
    private long size = 0;      // Filesize
    private String key = null;  // CHK key
    
    private long lastDownloaded = 0;
    private long lastUploaded = 0;
    private long firstReceived = 0;
    private long lastReceived = 0;

    private long requestLastReceived = 0;  // time when we received the last request for this sha
    private int requestsReceivedCount = 0; // received requests count
    
    private long requestLastSent = 0;      // time when we sent the last request for this file
    private int requestsSentCount = 0;     // sent requests count
    
    // non-persistent fields
    private String displayName = null;
    private String displayComment = null;
    private int displayRating = -1;
    private Boolean hasInfosFromMultipleSources = null;

    private List frostFileListFileObjectOwnerList = new LinkedList();
    
    private List listeners = new ArrayList();
    
    /**
     * Used if item is loaded from database.
     */
    public FrostFileListFileObject(
            long newPrimkey,
            String newSha1, 
            long newSize, 
            String newKey, 
            long newLastDownloaded, 
            long newLastUploaded, 
            long newFirstReceived,
            long newLastReceived,
            long newRequestLastReceived,
            int newRequestsReceivedCount,
            long newRequestLastSent,
            int newRequestSentCount) 
    {
        primkey = new Long(newPrimkey);
        sha = newSha1;
        size = newSize;
        key = newKey;
        lastDownloaded = newLastDownloaded;
        lastUploaded = newLastUploaded;
        firstReceived = newFirstReceived;
        lastReceived = newLastReceived;
        requestLastReceived = newRequestLastReceived;
        requestsReceivedCount = newRequestsReceivedCount;
        requestLastSent = newRequestLastSent;
        requestsSentCount = newRequestSentCount;
    }

    /**
     * Create instance with data from SharedFilesXmlFile.
     * After creation this item should only be saved to database,
     * this merges the data from this item in case this files is
     * already in the filelist (adds new owner/board).
     */
    public FrostFileListFileObject(SharedFileXmlFile sfo, Identity owner, long timestamp) {
        
        sha = sfo.getSha();
        size = sfo.getSize().longValue();
        key = sfo.getKey();
        lastDownloaded = 0;
        firstReceived = timestamp;
        lastReceived = timestamp; // set or updated after add

        long lastUploadDate = 0;
        if( sfo.getKey() != null ) {
            if( sfo.getLastUploaded() != null ) {
                Calendar cal = DateFun.getCalendarFromDate(sfo.getLastUploaded());
                lastUploadDate = cal.getTimeInMillis();
            } else {
                lastUploadDate = 0;
            }
        }
        lastUploaded = lastUploadDate;
        
        FrostFileListFileObjectOwner ob = new FrostFileListFileObjectOwner(
                sfo.getFilename(),
                owner.getUniqueName(),
                sfo.getComment(),
                sfo.getKeywords(),
                sfo.getRating(),
                timestamp,
                lastUploadDate);

        addFrostFileListFileObjectOwner(ob);
    }

    /**
     * Save this item to the database (insert or update).
     */
    public boolean save() {
        try {
            AppLayerDatabase.getFileListDatabaseTable().insertOrUpdateFrostFileListFileObject(this);
        } catch(SQLException e) {
            logger.log(Level.SEVERE, "Error updating shared file object", e);
            return false;
        }
        return true;
    }
    
    public List getFrostFileListFileObjectOwnerList() {
        return frostFileListFileObjectOwnerList;
    }
    public void addFrostFileListFileObjectOwner(FrostFileListFileObjectOwner v) {
        frostFileListFileObjectOwnerList.add(v);
    }
    public void deleteFrostFileListFileObjectOwner(FrostFileListFileObjectOwner v) {
        frostFileListFileObjectOwnerList.remove(v);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        notifyListeners();
    }

    public long getLastDownloaded() {
        return lastDownloaded;
    }

    public void setLastDownloaded(long lastDownloaded) {
        this.lastDownloaded = lastDownloaded;
    }

    public long getLastUploaded() {
        return lastUploaded;
    }

    public void setLastUploaded(long lastUploaded) {
        this.lastUploaded = lastUploaded;
        notifyListeners();
    }

    public long getLastReceived() {
        return lastReceived;
    }

    public void setLastReceived(long lastReceived) {
        this.lastReceived = lastReceived;
        notifyListeners();
    }

    public String getSha() {
        return sha;
    }

    public long getSize() {
        return size;
    }

    public Long getPrimkey() {
        return primkey;
    }

    public long getRequestLastReceived() {
        return requestLastReceived;
    }

    public void setRequestLastReceived(long requestLastReceived) {
        this.requestLastReceived = requestLastReceived;
        notifyListeners();
    }

    public long getRequestLastSent() {
        return requestLastSent;
    }

    public void setRequestLastSent(long requestLastSent) {
        this.requestLastSent = requestLastSent;
        notifyListeners();
    }

    public int getRequestsReceivedCount() {
        return requestsReceivedCount;
    }

    public void setRequestsReceivedCount(int requestsReceivedCount) {
        this.requestsReceivedCount = requestsReceivedCount;
    }

    public int getRequestsSentCount() {
        return requestsSentCount;
    }

    public void setRequestsSentCount(int requestsSentCount) {
        this.requestsSentCount = requestsSentCount;
    }
    
    public long getFirstReceived() {
        return firstReceived;
    }
    public void setFirstReceived(long v) {
        firstReceived = v;
    }
    
    static class MutableInt {
        public int i = 0;
        public String name = "";
        public int rating = 0;
    }
    
    private static MutableInt defaultMutableInt = new MutableInt();

    public String getDisplayName() {
        if( displayName == null ) {
            List lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                displayName = "(no sources)";
            } else {
                // choose most often used name
                Hashtable ht = new Hashtable();
                for( Iterator i = lst.iterator(); i.hasNext(); ) {
                    FrostFileListFileObjectOwner e = (FrostFileListFileObjectOwner) i.next();
                    MutableInt mi = (MutableInt)ht.get( e.getName() );
                    if( mi == null ) {
                        mi = new MutableInt();
                        mi.name = e.getName();
                        mi.i = 1;
                        ht.put(e.getName(), mi);
                    } else {
                        mi.i++;
                    }
                }
                MutableInt bestMi = defaultMutableInt;
                for(Iterator i = ht.values().iterator(); i.hasNext(); ) {
                    MutableInt mi = (MutableInt)i.next();
                    if( mi.i > bestMi.i ) {
                        bestMi = mi;
                    }
                }
                displayName = bestMi.name;
            }
        }
        return displayName;
    }

    /**
     * @return  true if this file has infos from multiple sources (comments, ratings, keywords)
     */
    public Boolean hasInfosFromMultipleSources() {
        if( hasInfosFromMultipleSources == null ) {
            if( getFrostFileListFileObjectOwnerList().size() > 1 ) {
                int valuesCount = 0;
                for(Iterator i=getFrostFileListFileObjectOwnerList().iterator(); i.hasNext(); ) {
                    FrostFileListFileObjectOwner o = (FrostFileListFileObjectOwner) i.next();
                    // valuesCount is increased by 1 per FrostFileListFileObjectOwner
                    if( o.getComment() != null && o.getComment().length() > 0 ) {
                        valuesCount++;
                    } else if( o.getKeywords() != null && o.getKeywords().length() > 0 ) {
                        valuesCount++;
                    } else if( o.getRating() > 0 ) {
                        valuesCount++;
                    }
                    // if valuesCount is greater 1 we have at least 2 sources that provide informations
                    if( valuesCount > 1 ) {
                        hasInfosFromMultipleSources = Boolean.TRUE;
                        break;
                    }
                }
                if( hasInfosFromMultipleSources == null ) {
                    hasInfosFromMultipleSources = Boolean.FALSE;
                }
            } else {
                hasInfosFromMultipleSources = Boolean.FALSE;
            }
        }
        return hasInfosFromMultipleSources;
    }

    public String getDisplayComment() {
        if( displayComment == null ) {
            List lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                displayComment = "(no sources)";
            } else {
                // choose most often used name
                Hashtable ht = new Hashtable();
                for( Iterator i = lst.iterator(); i.hasNext(); ) {
                    FrostFileListFileObjectOwner e = (FrostFileListFileObjectOwner) i.next();
                    String c = e.getComment();
                    if( c == null || c.length() == 0 ) {
                        continue;
                    }
                    MutableInt mi = (MutableInt)ht.get( c );
                    if( mi == null ) {
                        mi = new MutableInt();
                        mi.name = c;
                        mi.i = 1;
                        ht.put(c, mi);
                    } else {
                        mi.i++;
                    }
                }
                MutableInt bestMi = defaultMutableInt;
                for(Iterator i = ht.values().iterator(); i.hasNext(); ) {
                    MutableInt mi = (MutableInt)i.next();
                    if( mi.i > bestMi.i ) {
                        bestMi = mi;
                    }
                }
                displayComment = bestMi.name;
            }
        }
        return displayComment;
    }
    
    public int getDisplayRating() {
        if( displayRating < 0 ) {
            List lst = getFrostFileListFileObjectOwnerList();
            if( lst == null || lst.size() == 0 ) {
                return 0;
            }
            // choose most often used rating
            // choose most often used name
            int ratings[] = new int[6];
            for( Iterator i = lst.iterator(); i.hasNext(); ) {
                FrostFileListFileObjectOwner e = (FrostFileListFileObjectOwner) i.next();
                int r = e.getRating();
                if( r < 1 || r > 5 ) {
                    continue;
                }
                ratings[r]++;
            }
            int bestRating = 0;
            for(int x=1; x < ratings.length; x++ ) {
                if( ratings[x] > bestRating ) {
                    bestRating = x;
                }
            }
            displayRating = bestRating;
        }
        return displayRating;
    }
    
    public void addListener(FrostDownloadItem d) {
        if( !listeners.contains(d) ) {
            listeners.add(d);
        }
    }
    public void removeListener(FrostDownloadItem d) {
        if( listeners.contains(d) ) {
            listeners.remove(d);
        }
    }
    public List getListeners() {
        return listeners;
    }
    private void notifyListeners() {
        for(Iterator i=listeners.iterator(); i.hasNext(); ) {
            FrostDownloadItem dl = (FrostDownloadItem) i.next();
            dl.fireValueChanged();
        }
    }
}
