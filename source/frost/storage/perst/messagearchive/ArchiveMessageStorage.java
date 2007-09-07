/*
  MessageArchiveStorage.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage.perst.messagearchive;

import java.util.*;
import java.util.logging.*;

import org.garret.perst.*;

import frost.boards.*;
import frost.messages.*;
import frost.storage.*;

public class ArchiveMessageStorage implements Savable {

    private static final Logger logger = Logger.getLogger(ArchiveMessageStorage.class.getName());

    public static final int INSERT_OK        = 1;
    public static final int INSERT_DUPLICATE = 2;
    public static final int INSERT_ERROR     = 3;

    // FIXME: adjust page size
    private static final int PAGE_SIZE = 1; // page size for the storage in MB

    private Storage storage = null;
    private ArchiveMessageStorageRoot storageRoot = null;

    private static ArchiveMessageStorage instance = new ArchiveMessageStorage();

    protected ArchiveMessageStorage() {}

    public static ArchiveMessageStorage inst() {
        return instance;
    }

    private Storage getStorage() {
        return storage;
    }

    public boolean initStorage() {
        final String databaseFilePath = "store/messageArchive.dbs"; // path to the database file
        final int pagePoolSize = PAGE_SIZE*1024*1024; // size of page pool in bytes

        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.string.encoding", "UTF-8");
        storage.open(databaseFilePath, pagePoolSize);

        storageRoot = (ArchiveMessageStorageRoot)storage.getRoot();
        if (storageRoot == null) {
            // Storage was not initialized yet
            storageRoot = new ArchiveMessageStorageRoot(storage);
            storage.setRoot(storageRoot);
            storage.commit(); // commit transaction
        }
        return true;
    }

    public synchronized void commitStore() {
        if( getStorage() == null ) {
            return;
        }
        getStorage().commit();
    }

    public void save() throws StorageException {
        storage.close();
        storageRoot = null;
        storage = null;
        System.out.println("INFO: MessageArchiveStorage closed.");
    }

    public int getMessageCount() {
        int msgCount = 0;
        for(final PerstFrostArchiveBoardObject bo : storageRoot.getBoardsByName()) {
            if( bo.getMessageIndex() != null ) {
                msgCount += bo.getMessageIndex().size();
            }
        }
        return msgCount;
    }

    private void addBoard(final String boardName) {

        if( boardName == null ) {
            return;
        }

        // prevent duplicate board names
        if( storageRoot.getBoardsByName().contains(boardName) ) {
            return; // dup!
        }

        final PerstFrostArchiveBoardObject pfbo = new PerstFrostArchiveBoardObject(storage, boardName);
        storageRoot.getBoardsByName().put(boardName, pfbo);

        logger.severe("Added archive board: "+boardName);

        storage.commit();
    }

    public synchronized int insertMessage(final FrostMessageObject mo, final boolean doCommit) {
        final Board targetBoard = mo.getBoard();
        if( targetBoard == null ) {
            // already in store!
            logger.severe("msgInsertError: no board in msg");
            return INSERT_ERROR; // skip msg
        }
        return insertMessage(mo, targetBoard.getNameLowerCase(), doCommit);
    }

    public synchronized int insertMessage(final FrostMessageObject mo, final String boardName, final boolean doCommit) {

        if( !mo.isValid() ) {
            return INSERT_OK; // ignore invalid msgs
        }
        if( mo.isDeleted() ) {
            return INSERT_OK; // ignore deleted msgs
        }

        // add to indices, check for duplicate msgId

        PerstFrostArchiveBoardObject bo = storageRoot.getBoardsByName().get(boardName);
        if( bo == null ) {
            // create new board
            addBoard(boardName);
            bo = storageRoot.getBoardsByName().get(boardName);
            if( bo == null ) {
                logger.severe("Error: still no board???");
                return INSERT_ERROR;
            }
        }

        final PerstFrostArchiveMessageObject pmo = new PerstFrostArchiveMessageObject(mo, storage);

        if( mo.getMessageId() != null ) {
            if( !bo.getMessageIdIndex().put(mo.getMessageId(), pmo) ) {
                // duplicate messageId!
                return INSERT_DUPLICATE; // skip msg
            }
        }

        bo.getMessageIndex().put(mo.getDateAndTime().getMillis(), pmo);

        if( doCommit ) {
            commitStore();
        }

        return INSERT_OK;
    }

    public void retrieveMessagesForSearch(
            final Board board,
            final long startDate,
            final long endDate,
            final MessageCallback mc)
    {
        final PerstFrostArchiveBoardObject bo = storageRoot.getBoardsByName().get(board.getNameLowerCase());
        if( bo == null ) {
            logger.severe("error: no perst board for archive search");
            return;
        }
        // normal messages in date range
        final Iterator<PerstFrostArchiveMessageObject> i = bo.getMessageIndex().iterator(startDate, endDate, Index.ASCENT_ORDER);
        while(i.hasNext()) {
            final PerstFrostArchiveMessageObject p = i.next();
            final FrostMessageObject mo = p.toFrostMessageObject(board);
            final boolean shouldStop = mc.messageRetrieved(mo);
            if( shouldStop ) {
                break;
            }
        }
    }
}
