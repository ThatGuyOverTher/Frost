/*
  MessageArchiveDatabaseTable.java / Frost
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

import frost.*;
import frost.boards.*;
import frost.messages.*;
import frost.storage.database.*;

/**
 * Archived messages. Same as MessageDatabaseTable, but boards are stored as VARCHAR
 * and messages are always retrieved with content and attachments.
 * Note: boards are stored in lowercase format always!
 */
public class MessageArchiveDatabaseTable extends AbstractDatabaseTable {

    private static Logger logger = Logger.getLogger(MessageArchiveDatabaseTable.class.getName());

    private final String SQL_DDL_MESSAGES =
        "CREATE TABLE MESSAGEARCHIVE ("+
        "primkey BIGINT NOT NULL,"+
        "messageid VARCHAR,"+
        "inreplyto VARCHAR,"+
        "isvalid BOOLEAN,"+
        "invalidreason VARCHAR,"+
        "msgdate DATE NOT NULL,"+
        "msgtime TIME,"+
        "msgindex INT NOT NULL,"+
        "board VARCHAR NOT NULL,"+
        "fromname VARCHAR,"+
        "subject VARCHAR,"+
        "recipient VARCHAR,"+
        "signature VARCHAR,"+
        "signaturestatus INT,"+
        "publickey VARCHAR,"+
        "isdeleted BOOLEAN,"+
        "isnew BOOLEAN,"+
        "isreplied BOOLEAN,"+ 
        "isjunk BOOLEAN,"+
        "isflagged BOOLEAN,"+ 
        "isstarred BOOLEAN,"+
        "hasfileattachment BOOLEAN,"+
        "hasboardattachment BOOLEAN,"+
        "CONSTRAINT msgarc_pk PRIMARY KEY (primkey),"+
        "CONSTRAINT msgarc_unique UNIQUE(messageid)"+ // multiple null allowed
        ")";
    
    // this index is really important because we select messageids
    private final String SQL_DDL_MESSAGES_INDEX_MSGID =
        "CREATE UNIQUE INDEX msgarc_ix1 ON MESSAGEARCHIVE ( messageid )";
    private final String SQL_DDL_MESSAGES_INDEX_BOARD =
        "CREATE INDEX msgarc_ix2 ON MESSAGEARCHIVE ( board )";

    private final String SQL_DDL_FILEATTACHMENTS =
        "CREATE TABLE MESSAGEARCHIVEFILEATTACHMENTS ("+
        "msgref BIGINT NOT NULL,"+
        "filename VARCHAR,"+
        "filesize BIGINT,"+
        "filekey  VARCHAR,"+
        "CONSTRAINT msgarcfa_fk FOREIGN KEY (msgref) REFERENCES MESSAGEARCHIVE(primkey) ON DELETE CASCADE"+
        ")";

    private final String SQL_DDL_BOARDATTACHMENTS =
        "CREATE TABLE MESSAGEARCHIVEBOARDATTACHMENTS ("+
        "msgref BIGINT NOT NULL,"+
        "boardname VARCHAR,"+
        "boardpublickey   VARCHAR,"+
        "boardprivatekey  VARCHAR,"+
        "boarddescription VARCHAR,"+
        "CONSTRAINT msgarcba_fk FOREIGN KEY (msgref) REFERENCES MESSAGEARCHIVE(primkey) ON DELETE CASCADE"+
        ")";

    private final String SQL_DDL_CONTENT =
        "CREATE TABLE MESSAGEARCHIVECONTENTS ("+
        "msgref BIGINT NOT NULL,"+
        "msgcontent VARCHAR,"+
        "CONSTRAINT msgarcc_fk FOREIGN KEY (msgref) REFERENCES MESSAGEARCHIVE(primkey) ON DELETE CASCADE,"+
        "CONSTRAINT msgarcc_unique UNIQUE(msgref)"+
        ")";
    private final String SQL_DDL_CONTENT_INDEX =
        "CREATE INDEX msgarcc_ix ON MESSAGEARCHIVECONTENTS ( msgref )";

    public List getTableDDL() {
        ArrayList lst = new ArrayList(7);
        lst.add(SQL_DDL_MESSAGES);
        lst.add(SQL_DDL_FILEATTACHMENTS);
        lst.add(SQL_DDL_BOARDATTACHMENTS);
        lst.add(SQL_DDL_MESSAGES_INDEX_MSGID);
        lst.add(SQL_DDL_MESSAGES_INDEX_BOARD);
        lst.add(SQL_DDL_CONTENT);
        lst.add(SQL_DDL_CONTENT_INDEX);
        return lst;
    }
      
    public boolean compact(Statement stmt) throws SQLException {
        stmt.executeUpdate("COMPACT TABLE MESSAGEARCHIVE");
        stmt.executeUpdate("COMPACT TABLE MESSAGEARCHIVEFILEATTACHMENTS");
        stmt.executeUpdate("COMPACT TABLE MESSAGEARCHIVEBOARDATTACHMENTS");
        stmt.executeUpdate("COMPACT TABLE MESSAGEARCHIVECONTENTS");
        return true;
    }

    public synchronized boolean insertMessage(FrostMessageObject mo) throws SQLException {

        AttachmentList files = mo.getAttachmentsOfType(Attachment.FILE);
        AttachmentList boards = mo.getAttachmentsOfType(Attachment.BOARD);

        // insert msg and all attachments
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps = db.prepare(
            "INSERT INTO MESSAGEARCHIVE ("+
            "primkey,messageid,inreplyto,isvalid,invalidreason,msgdate,msgtime,msgindex,board,fromname,subject,recipient,signature," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,hasfileattachment,hasboardattachment"+
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        
        Long identity = null;
        Statement stmt = AppLayerDatabase.getInstance().createStatement();
        ResultSet rs = stmt.executeQuery("select UNIQUEKEY('MESSAGEARCHIVE')");
        if( rs.next() ) {
            identity = new Long(rs.getLong(1));
        } else {
            logger.log(Level.SEVERE,"Could not retrieve a new unique key!");
        }
        rs.close();
        stmt.close();
        
        int i=1;
        ps.setLong(i++, identity.longValue()); // messageid
        ps.setString(i++, mo.getMessageId()); // messageid
        ps.setString(i++, mo.getInReplyTo()); // inreplyto
        ps.setBoolean(i++, mo.isValid()); // isvalid
        ps.setString(i++, mo.getInvalidReason()); // invalidreason
        ps.setDate(i++, mo.getSqlDate()); // date  
        ps.setTime(i++, mo.getSqlTime()); // time
        ps.setInt(i++, mo.getIndex()); // index
        ps.setString(i++, mo.getBoard().getNameLowerCase()); // board
        ps.setString(i++, mo.getFromName()); // from
        ps.setString(i++, mo.getSubject()); // subject
        ps.setString(i++, mo.getRecipientName()); // recipient
        ps.setString(i++, mo.getSignature()); // signature
        ps.setInt(i++, mo.getSignatureStatus()); // signaturestatus
        ps.setString(i++, mo.getPublicKey()); // publickey
        ps.setBoolean(i++, mo.isDeleted()); // isdeleted
        ps.setBoolean(i++, mo.isNew()); // isnew
        ps.setBoolean(i++, mo.isReplied()); // isreplied
        ps.setBoolean(i++, mo.isJunk()); // isjunk
        ps.setBoolean(i++, mo.isFlagged()); // isflagged
        ps.setBoolean(i++, mo.isStarred()); // isstarred
        ps.setBoolean(i++, (files.size() > 0)); // hasfileattachment
        ps.setBoolean(i++, (boards.size() > 0)); // hasboardattachment

        int insertedCount;
        // sync to allow no updates until we got the generated identity
        insertedCount = ps.executeUpdate();
        ps.close();

        if( insertedCount == 0 ) {
            logger.log(Level.SEVERE, "message insert returned 0 !!!");
            return false;
        }
        
        mo.setMsgIdentity(identity.longValue());
        
        // content
        PreparedStatement pc = db.prepare(
                "INSERT INTO MESSAGEARCHIVECONTENTS"+
                " (msgref,msgcontent) VALUES (?,?)");
        pc.setLong(1, mo.getMsgIdentity());
        pc.setString(2, mo.getContent());
        
        insertedCount = pc.executeUpdate();
        pc.close();
        
        if( insertedCount == 0 ) {
            logger.log(Level.SEVERE, "message content insert returned 0 !!!");
            return false;
        }

        // attachments
        if( files.size() > 0 ) {
            PreparedStatement p = db.prepare(
                    "INSERT INTO MESSAGEARCHIVEFILEATTACHMENTS"+
                    " (msgref,filename,filesize,filekey)"+
                    " VALUES (?,?,?,?)");
            for(Iterator it=files.iterator(); it.hasNext(); ) {
                FileAttachment fa = (FileAttachment)it.next();
                int ix=1;
                p.setLong(ix++, mo.getMsgIdentity()); 
                p.setString(ix++, fa.getFilename()); 
                p.setLong(ix++, fa.getSize().longValue()); 
                p.setString(ix++, fa.getKey()); 
                insertedCount = p.executeUpdate();
                if( insertedCount == 0 ) {
                    logger.log(Level.SEVERE, "fileattachment insert returned 0 !!!");
                    p.close();
                    return false;
                }
            }
            p.close();
        }
        if( boards.size() > 0 ) {
            PreparedStatement p = db.prepare(
                    "INSERT INTO MESSAGEARCHIVEBOARDATTACHMENTS"+
                    " (msgref,boardname,boardpublickey,boardprivatekey,boarddescription)"+
                    " VALUES (?,?,?,?,?)");
            for(Iterator it=boards.iterator(); it.hasNext(); ) {
                BoardAttachment ba = (BoardAttachment)it.next();
                Board b = ba.getBoardObj();
                int ix=1;
                p.setLong(ix++, mo.getMsgIdentity()); 
                p.setString(ix++, b.getNameLowerCase()); 
                p.setString(ix++, b.getPublicKey()); 
                p.setString(ix++, b.getPrivateKey()); 
                p.setString(ix++, b.getDescription()); 
                insertedCount = p.executeUpdate();
                if( insertedCount == 0 ) {
                    logger.log(Level.SEVERE, "boardattachment insert returned 0 !!!");
                    p.close();
                    return false;
                }
            }
            p.close();
        }
        return true;
    }

    private void retrieveAttachments(FrostMessageObject mo) throws SQLException {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        // retrieve attachments
        if( mo.isHasFileAttachments() ) {
            PreparedStatement p2 = db.prepare(
                    "SELECT filename,filesize,filekey FROM MESSAGEARCHIVEFILEATTACHMENTS"+
                    " WHERE msgref=? ORDER BY filename");
            p2.setLong(1, mo.getMsgIdentity());
            ResultSet rs2 = p2.executeQuery();
            while(rs2.next()) {
                String name, key;
                long size;
                name = rs2.getString(1);
                size = rs2.getLong(2);
                key = rs2.getString(3);

                FileAttachment fa = new FileAttachment(name, key, size);
                mo.addAttachment(fa);
            }
            rs2.close();
            p2.close();
        }
        if( mo.isHasBoardAttachments() ) {
            PreparedStatement p2 = db.prepare(
                    "SELECT boardname,boardpublickey,boardprivatekey,boarddescription FROM MESSAGEARCHIVEBOARDATTACHMENTS"+
                    " WHERE msgref=? ORDER BY boardname");
            p2.setLong(1, mo.getMsgIdentity());
            ResultSet rs2 = p2.executeQuery();
            while(rs2.next()) {
                String name, pubkey, privkey, desc;
                name = rs2.getString(1);
                pubkey = rs2.getString(2);
                privkey = rs2.getString(3);
                desc = rs2.getString(4);
                Board b = new Board(name, pubkey, privkey, desc);
                BoardAttachment ba = new BoardAttachment(b);
                mo.addAttachment(ba);
            }
            rs2.close();
            p2.close();
        }
    }

    private void retrieveMessageContent(FrostMessageObject mo) throws SQLException {
        
        // invalid messages have no content (e.g. encrypted for someone else,...)
        if( !mo.isValid() ) {
            mo.setContent("");
            return;
        }
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();

        PreparedStatement p2 = db.prepare("SELECT msgcontent FROM MESSAGEARCHIVECONTENTS WHERE msgref=?");
        p2.setLong(1, mo.getMsgIdentity());
        ResultSet rs2 = p2.executeQuery();
        if(rs2.next()) {
            String content;
            content = rs2.getString(1);
            mo.setContent(content);
        } else {
            logger.severe("Error: No content for msgref="+mo.getMsgIdentity()+", msgid="+mo.getMessageId());
            mo.setContent("");
        }
        rs2.close();
        p2.close();
    }
    
    private FrostMessageObject resultSetToFrostMessageObject(ResultSet rs, Board board) throws SQLException {
        FrostMessageObject mo = new FrostMessageObject();
        mo.setBoard(board);
        // SELECT retrieves only valid messages:
        mo.setValid(true);

        int ix=1;
        mo.setMsgIdentity(rs.getLong(ix++));
        mo.setMessageId(rs.getString(ix++));
        mo.setInReplyTo(rs.getString(ix++));
        mo.setSqlDate(rs.getDate(ix++));
        mo.setSqlTime(rs.getTime(ix++));
        mo.setIndex(rs.getInt(ix++));
        mo.setFromName(rs.getString(ix++));
        mo.setSubject(rs.getString(ix++));
        mo.setRecipientName(rs.getString(ix++));
        mo.setSignatureStatus(rs.getInt(ix++));
        mo.setPublicKey(rs.getString(ix++));
        mo.setDeleted(rs.getBoolean(ix++));

        mo.setNew(rs.getBoolean(ix++));
        mo.setReplied(rs.getBoolean(ix++));
        mo.setJunk(rs.getBoolean(ix++));
        mo.setFlagged(rs.getBoolean(ix++));
        mo.setStarred(rs.getBoolean(ix++));
        
        mo.setHasFileAttachments( rs.getBoolean(ix++) );
        mo.setHasBoardAttachments( rs.getBoolean(ix++) );
        
        retrieveMessageContent(mo);
        retrieveAttachments(mo);
        
        return mo;
    }
    
    public void retrieveMessagesForSearch(
            Board board, 
            java.sql.Date startDate, 
            java.sql.Date endDate,
            boolean showDeleted, 
            MessageDatabaseTableCallback mc) 
    throws SQLException 
    {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,msgdate,msgtime,msgindex,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,"+
            "hasfileattachment,hasboardattachment";
            sql += " FROM MESSAGEARCHIVE WHERE msgdate>=? AND msgdate<=? AND board=? AND isvalid=TRUE AND isdeleted=?";
        PreparedStatement ps = db.prepare(sql);
        
        ps.setDate(1, startDate);
        ps.setDate(2, endDate);
        ps.setString(3, board.getNameLowerCase());
        ps.setBoolean(4, showDeleted);
        
        ResultSet rs = ps.executeQuery();

        while( rs.next() ) {
            FrostMessageObject mo = resultSetToFrostMessageObject(rs, board);
            boolean shouldStop = mc.messageRetrieved(mo); // pass to callback
            if( shouldStop ) {
                break;
            }
        }
        rs.close();
        ps.close();
    }
    
    public FrostMessageObject retrieveMessageByMessageId(
            Board board,
            String msgId,
            boolean showDeleted) 
    throws SQLException 
    {
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        String sql =
            "SELECT "+
            "primkey,messageid,inreplyto,msgdate,msgtime,msgindex,fromname,subject,recipient," +
            "signaturestatus,publickey,isdeleted,isnew,isreplied,isjunk,isflagged,isstarred,"+
            "hasfileattachment,hasboardattachment";
            sql += " FROM MESSAGEARCHIVE WHERE board=? AND messageid=?";
        PreparedStatement ps = db.prepare(sql);
        
        ps.setString(1, board.getNameLowerCase());
        ps.setString(2, msgId);
        
        ResultSet rs = ps.executeQuery();

        FrostMessageObject mo = null;
        if( rs.next() ) {
            mo = resultSetToFrostMessageObject(rs, board);
        }
        rs.close();
        ps.close();
        
        return mo;
    }

    /**
     * Returns message count by board. If maxDaysBack is <0 all messages are counted.
     */
    public int getMessageCount(Board board, int maxDaysBack) throws SQLException {
        
        AppLayerDatabase db = AppLayerDatabase.getInstance();
        PreparedStatement ps;
        if( maxDaysBack < 0 ) {
            // no date restriction
            ps = db.prepare("SELECT COUNT(primkey) FROM MESSAGEARCHIVE WHERE board=? AND isvalid=TRUE");
            ps.setString(1, board.getNameLowerCase());
        } else {
            ps = db.prepare("SELECT COUNT(primkey) FROM MESSAGEARCHIVE WHERE msgdate >=? AND board=? AND isvalid=TRUE");
            java.sql.Date startDate = DateFun.getSqlDateGMTDaysAgo(maxDaysBack);
            ps.setDate(1, startDate);
            ps.setString(2, board.getNameLowerCase());
        }
        
        int count = 0;
        ResultSet rs = ps.executeQuery();
        if( rs.next() ) {
            count = rs.getInt(1);
        }
        rs.close();
        ps.close();
        
        return count;
    }
}
