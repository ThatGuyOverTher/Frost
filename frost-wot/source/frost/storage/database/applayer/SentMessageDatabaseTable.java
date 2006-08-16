/*
 SentMessageDatabaseTable.java / Frost
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

/**
 * Uses the functionality of MessageDatabaseTable, but different table names.
 */
public class SentMessageDatabaseTable extends MessageDatabaseTable {

    protected String getMessageTableName() {
        return "SENTMESSAGES";
    }
    protected String getFileAttachmentsTableName() {
        return "SENTFILEATTACHMENTS";
    }
    protected String getBoardAttachmentsTableName() {
        return "SENTBOARDATTACHMENTS";
    }
    protected String getUniqueMsgConstraintName() {
        return "SENTMSG_UNIQUE_ONLY";
    }
    protected String getUniqueMsgIdConstraintName() {
        return "SENTMSG_ID_UNIQUE_ONLY";
    }
    protected String getMsgIdIndexName() {
        return "SENTMSG_IX_MSGID";
    }
    protected String getBoardIndexName() {
        return "SENTMSG_IX_BOARD";
    }
    protected String getPrimKeyConstraintName() {
        return "sentmsgs_pk";
    }
    protected String getFileForeignKeyConstraintName() {
        return "sentmsgs_fileatt_1";
    }
    protected String getBoardForeignKeyConstraintName() {
        return "sentmsgs_boardatt_1";
    }
    protected String getFromnameIndexName() {
        return "SENTMSG_IX_FROM";
    }
}
