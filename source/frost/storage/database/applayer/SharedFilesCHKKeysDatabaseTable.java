/*
 SharedFilesCHKKeysDatabaseTable.java / Frost
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
package frost.storage.database.applayer;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.fileTransfer.*;
import frost.storage.database.*;

/**
 * Stores informations about the CHK keys of filelists that we received in the KSK pointer files.
 */
public class SharedFilesCHKKeysDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(SharedFilesCHKKeysDatabaseTable.class.getName());
    
    // Question: how to ensure own CHK keys, track them once uploaded if we ever see them again! 
    // Answer: Ignore lost keys, we resend them after some days!
    //         Handle own received keys like any other keys, we don't even know that this was our key.

    private final static String SQL_SHAREDFILESCHK_DDL =
        "CREATE TABLE IF NOT EXISTS SHAREDFILESCHK ("+
          "primkey BIGINT NOT NULL,"+
          "chkkey VARCHAR NOT NULL,"+
          "seencount INT NOT NULL,"+    // how often did we receive this CHK in a KSK pointer file
          "firstseen BIGINT NOT NULL,"+ // when did we receive this CHK the first time (time in millis)
          "lastseen BIGINT NOT NULL,"+  // when did we receive this CHK the last time (time in millis) 
          "isdownloaded BOOLEAN NOT NULL,"+  // did we download this file?
          "isvalid BOOLEAN NOT NULL,"+  // if files signature was invalid, don't distribute this file any longer!        
          "downloadretries INT NOT NULL,"+ // our tries to download this CHK, don't try too often
          "lastdownloadtrystop BIGINT NOT NULL,"+ // when was the download stopped the last time (time in millis)
          "sentcount INT NOT NULL,"+   // how often we send this CHK within a pointer file
          "lastsent BIGINT NOT NULL,"+ // time in millis when we sent this CHK the last time
        "CONSTRAINT sfiles_pk PRIMARY KEY (primkey),"+
        "CONSTRAINT sfiles_1 UNIQUE (chkkey) )";

    private final static String SQL_SHAREDFILESCHK_INDEX_PRIMKEY =
        "CREATE UNIQUE INDEX SHAREDFILESCHK_IX_PRIMKEY ON SHAREDFILESCHK ( primkey )";
    private final static String SQL_SHAREDFILESCHK_INDEX_CHKKEY =
        "CREATE UNIQUE INDEX SHAREDFILESCHK_IX_CHKKEY ON SHAREDFILESCHK ( chkkey )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(3);
        lst.add(SQL_SHAREDFILESCHK_DDL);
        lst.add(SQL_SHAREDFILESCHK_INDEX_PRIMKEY);
        lst.add(SQL_SHAREDFILESCHK_INDEX_CHKKEY);
        return lst;
    }
    
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE SHAREDFILESCHK");
        return true;
    }
    
    public List getSharedFilesCHKKeysToSend(int maxKeys) throws SQLException {
        // get a number of CHK keys from database that must be send
        // include only 1 of our new CHK keys into this list, don't send CHK keys of different identities
        // together, this compromises anonymity!

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        List keysToSend = new LinkedList();

        // first search for a CHK key that was created by us, but was never send
        {
            String chkKey = null;

            Statement s = db.createStatement();
            s.setMaxRows(1);
            ResultSet rs = s.executeQuery("SELECT chkkey FROM SHAREDFILESCHK WHERE seencount=0");
            
            if( rs.next() ) {
                chkKey = rs.getString(1);
            }
            
            rs.close();
            s.close();
            
            if( chkKey != null ) {
                SharedFilesCHKKey ck = retrieveSharedFilesCHKKey(chkKey);
                if( ck != null ) {
                    keysToSend.add(ck);
                }
            }
        }
        
        // then search for other files to send, but don't include other new files from us
        // - the CHK key must be downloaded already (our new files are not yet downloaded)
        // - key must be valid
        // - keys firstseen must be not earlier than 14 days (don't send old stuff around)
        // - keys lastseen must be more than 24h before (don't send keys we just received)
        // - keys lastsent must be more than 24h before (don't send keys we just sent)
        // - order by seencount asc -> collect keys that are not seen often
        // - collect a maximum of 300 keys
        {
            long now = System.currentTimeMillis();
            long minFirstSeen = now - (14L * 24L * 60L * 60L * 1000L); // now - 14 days
            long maxLastSeen = now - (1L * 24L * 60L * 60L * 1000L); // now - 1 day
            String sql = "SELECT chkkey FROM SHAREDFILESCHK WHERE" +
                         " isdownloaded=TRUE" +
                         " AND isvalid=TRUE" +
                         " AND lastseen<" + maxLastSeen +
                         " AND lastsent<" + maxLastSeen +
                         " AND firstseen>"+ minFirstSeen +
                         " ORDER BY seencount ASC";
            Statement s = db.createStatement();
            s.setMaxRows(maxKeys);
            ResultSet rs = s.executeQuery(sql);
            
            List tmpList = new LinkedList();
            int count = 0; // we trust noone (setMaxRows may fail *g*)
            while( rs.next() && count < maxKeys) {
                String chkKey = rs.getString(1);
                tmpList.add(chkKey);
                count++;
            }
            rs.close();
            s.close();
            
            for( Iterator i = tmpList.iterator(); i.hasNext(); ) {
                String chkKey = (String) i.next();
                SharedFilesCHKKey ck = retrieveSharedFilesCHKKey(chkKey);
                if( ck != null ) {
                    keysToSend.add(ck);
                }
            }
        }
        return keysToSend;
    }

    public SharedFilesCHKKey retrieveSharedFilesCHKKey(String chkKey) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement ps = db.prepare(
                "SELECT primkey,seencount,firstseen,lastseen,isdownloaded,isvalid,downloadretries,lastdownloadtrystop,"+
                "sentcount,lastsent "+
                "FROM SHAREDFILESCHK WHERE chkkey=?");
        
        ps.setString(1, chkKey);
        
        SharedFilesCHKKey result = null;
        
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            int ix=1;
            long primkey = rs.getLong(ix++);
            int seenCount = rs.getInt(ix++);
            long firstSeen = rs.getLong(ix++);
            long lastSeen = rs.getLong(ix++);
            boolean isDownloaded = rs.getBoolean(ix++);
            boolean isValid = rs.getBoolean(ix++);
            int downloadRetries = rs.getInt(ix++);
            long lastDownloadTryStop = rs.getLong(ix++);
            int sentCount = rs.getInt(ix++);
            long lastSent = rs.getLong(ix++);
            
            result = new SharedFilesCHKKey(
                    primkey, 
                    chkKey,
                    seenCount,
                    firstSeen,
                    lastSeen,
                    isDownloaded,
                    isValid,
                    downloadRetries,
                    lastDownloadTryStop,
                    sentCount,
                    lastSent);
        }
        rs.close();
        ps.close();
        
        return result;
    }

    /** 
     * Retrieves all unretrieved CHK keys to download.
     * @return  List of Strings
     */ 
    public List retrieveSharedFilesCHKKeysToDownload(int maxRetries) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        String sql = "SELECT chkkey FROM SHAREDFILESCHK WHERE isdownloaded=FALSE AND downloadretries<? " +
                     "ORDER BY lastdownloadtrystop ASC";

        PreparedStatement ps = db.prepare(sql);
        
        int qix = 1;
        ps.setInt(qix++, maxRetries);

        List chkKeys = new LinkedList();
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            String chk = rs.getString(1);
            
            chkKeys.add(chk);
        }
        rs.close();
        ps.close();
        
        return chkKeys;
    }

    public boolean insertSharedFilesCHKKey(SharedFilesCHKKey newkey) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        Long identity = null;
        Statement stmt = AppLayerDatabase.getInstance().createStatement();
        ResultSet rs = stmt.executeQuery("select UNIQUEKEY('SHAREDFILESCHK')");
        if( rs.next() ) {
            identity = new Long(rs.getLong(1));
        } else {
            logger.log(Level.SEVERE,"Could not retrieve a new unique key!");
        }
        rs.close();
        stmt.close();

        PreparedStatement ps = db.prepare(
                "INSERT INTO SHAREDFILESCHK (primkey,chkkey,seencount,firstseen,lastseen,"+
                  "isdownloaded,isvalid,downloadretries,lastdownloadtrystop,sentcount,lastsent) "+
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)");
        
        int ix=1;
        ps.setLong(ix++, identity.longValue());
        ps.setString(ix++, newkey.getChkKey());
        ps.setInt(ix++, newkey.getSeenCount());
        ps.setLong(ix++, newkey.getFirstSeen());
        ps.setLong(ix++, newkey.getLastSeen());
        ps.setBoolean(ix++, newkey.isDownloaded());
        ps.setBoolean(ix++, newkey.isValid());
        ps.setInt(ix++, newkey.getDownloadRetries());
        ps.setLong(ix++, newkey.getLastDownloadTryStopTime());
        ps.setInt(ix++, newkey.getSentCount());
        ps.setLong(ix++, newkey.getLastSent());
        
        int insertCount = ps.executeUpdate();
        
        return (insertCount == 1);
    }

    /**
     * Updates newkey in database.
     * primkey, chkkey, firstseen are always fix!
     */
    public boolean updateSharedFilesCHKKey(SharedFilesCHKKey newkey) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "UPDATE SHAREDFILESCHK SET seencount=?,lastseen=?,isdownloaded=?,"+
                "isvalid=?,downloadretries=?,lastdownloadtrystop=?,sentcount=?,lastsent=? "+
                "WHERE primkey=?");

        int ix=1;
        ps.setInt(ix++, newkey.getSeenCount());
        ps.setLong(ix++, newkey.getLastSeen());
        ps.setBoolean(ix++, newkey.isDownloaded());
        ps.setBoolean(ix++, newkey.isValid());
        ps.setInt(ix++, newkey.getDownloadRetries());
        ps.setLong(ix++, newkey.getLastDownloadTryStopTime());
        ps.setInt(ix++, newkey.getSentCount());
        ps.setLong(ix++, newkey.getLastSent());
        
        ps.setLong(ix++, newkey.getPrimaryKey());
        
        int updateCount = ps.executeUpdate();
        
        return (updateCount == 1);
    }
    
    /**
     * Updates newkey in database.
     * Updates only sentcount and lastsent
     */
    public boolean updateSharedFilesCHKKeyAfterSend(SharedFilesCHKKey newkey) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "UPDATE SHAREDFILESCHK SET sentcount=?,lastsent=? WHERE primkey=?");

        int ix=1;
        ps.setInt(ix++, newkey.getSentCount());
        ps.setLong(ix++, newkey.getLastSent());
        
        ps.setLong(ix++, newkey.getPrimaryKey());
        
        int updateCount = ps.executeUpdate();
        
        return (updateCount == 1);
    }
    
    /**
     * Updates newkey in database.
     * Updates only seencount and lastseen
     */
    public boolean updateSharedFilesCHKKeyAfterReceive(SharedFilesCHKKey newkey) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare("UPDATE SHAREDFILESCHK SET seencount=?,lastseen=?,firstseen=? WHERE primkey=?");

        int ix=1;
        ps.setInt(ix++, newkey.getSeenCount());
        ps.setLong(ix++, newkey.getLastSeen());
        ps.setLong(ix++, newkey.getFirstSeen());
        
        ps.setLong(ix++, newkey.getPrimaryKey());
        
        int updateCount = ps.executeUpdate();
        
        return (updateCount == 1);
    }
    
    /**
     * Updates newkey in database.
     */
    public boolean updateSharedFilesCHKKeyAfterDownloadSuccessful(String chkKey, boolean isValid) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "UPDATE SHAREDFILESCHK SET isdownloaded=TRUE,isvalid=? WHERE chkkey=?");

        int ix=1;
        ps.setBoolean(ix++, isValid);
        
        ps.setString(ix++, chkKey);
        
        int updateCount = ps.executeUpdate();
        
        return (updateCount == 1);
    }
    
    /**
     * Updates newkey in database.
     */
    public boolean updateSharedFilesCHKKeyAfterDownloadFailed(SharedFilesCHKKey newkey) throws SQLException {

        AppLayerDatabase db = AppLayerDatabase.getInstance();
        
        PreparedStatement ps = db.prepare(
                "UPDATE SHAREDFILESCHK SET downloadretries=?,lastdownloadtrystop=? WHERE primkey=?");

        int ix=1;
        ps.setInt(ix++, newkey.getDownloadRetries());
        ps.setLong(ix++, newkey.getLastDownloadTryStopTime());
        
        ps.setLong(ix++, newkey.getPrimaryKey());
        
        int updateCount = ps.executeUpdate();
        
        return (updateCount == 1);
    }
    
    /**
     * Delete all table entries that were not seen longer than maxDaysOld.
     * @return  count of deleted rows
     */
    public int cleanupTable(int maxDaysOld) throws SQLException {

        AppLayerDatabase localDB = AppLayerDatabase.getInstance();

        long minVal = System.currentTimeMillis() - ((long)maxDaysOld * 24L * 60L * 60L * 1000L);

        PreparedStatement ps = localDB.prepare("DELETE FROM SHAREDFILESCHK WHERE lastseen>0 AND lastseen<?");
        ps.setLong(1, minVal);
        
        int deletedCount = ps.executeUpdate();
        
        ps.close();

        return deletedCount;
    }
}
