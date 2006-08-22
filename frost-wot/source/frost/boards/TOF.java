/*
  TOF.java / Frost
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
package frost.boards;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.tree.*;

import frost.*;
import frost.gui.objects.*;
import frost.messages.*;
import frost.storage.database.applayer.*;
import frost.util.gui.*;

/**
 * @pattern Singleton
 *
 * @author $Author$
 * @version $Revision$
 */
public class TOF {
    
    // FIXME: wenn neue ankommt und gerade geupdated wird, dann enqueue und nach abarbeiten processe die neuen (alles im swing thread!)

    private static Logger logger = Logger.getLogger(TOF.class.getName());

    private UpdateTofFilesThread updateThread = null;
    private UpdateTofFilesThread nextUpdateThread = null;

    private TofTreeModel tofTreeModel;

    private static boolean initialized = false;

    private GlassPane glassPane = null;

    /**
     * The unique instance of this class.
     */
    private static TOF instance = null;

    /**
     * Return the unique instance of this class.
     *
     * @return the unique instance of this class
     */
    public static TOF getInstance() {
        return instance;
    }

    /**
     * Prevent instances of this class from being created from the outside.
     * @param tofTreeModel this is the TofTreeModel this TOF will operate on
     */
    private TOF(TofTreeModel tofTreeModel) {
        super();
        this.tofTreeModel = tofTreeModel;
    }

    /**
     * This method initializes the TOF.
     * If it has already been initialized, this method does nothing.
     * @param tofTreeModel this is the TofTreeModel this TOF will operate on
     */
    public static void initialize(TofTreeModel tofTreeModel) {
        if (!initialized) {
            initialized = true;
            instance = new TOF(tofTreeModel);
        }
    }

    /**
     * Resets the NEW state to READ for all messages shown in board table.
     *
     * @param tableModel  the messages table model
     * @param board  the board to reset
     */
    public void setAllMessagesRead(final Board board) {
        // now takes care if board is changed during mark read of many boards! reloads current table if needed

        final int oldNewMessageCount = board.getNewMessageCount();

        try {
            AppLayerDatabase.getMessageTable().setAllMessagesRead(board);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error marking all messages read", e);
            return;
        }
        
        // if this board is currently shown, update messages in table
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) 
            MainFrame.getInstance().getMessagePanel().getMessageTable().getTree().getModel().getRoot();

        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if( MainFrame.getInstance().getTofTreeModel().getSelectedNode() == board ) {
                    for(Enumeration e = rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
                        Object o = e.nextElement();
                        if( o instanceof FrostMessageObject ) {
                            FrostMessageObject mo = (FrostMessageObject)o;
                            if( mo.isNew() ) {
                                mo.setNew(false);
                                // fire update for visible rows in table model
                                int row = MainFrame.getInstance().getMessageTreeTable().getRowForNode(mo);
                                if( row >= 0 ) {
                                    MainFrame.getInstance().getMessageTableModel().fireTableRowsUpdated(row, row);
                                }
                            }
                        }
                    }
                }
                // set for not selected boards too, by 'select folder unread' function

                // we cleared '' new messages, but don't get to negativ (maybe user selected another message during operation!)
                // but maybe a new message arrived!
                // ATTN: maybe problem if user sets another msg unread, and a new msg arrives, during time before invokeLater.
                int diffNewMsgCount = board.getNewMessageCount() - oldNewMessageCount;
                board.setNewMessageCount( (diffNewMsgCount<0 ? 0 : diffNewMsgCount) );
                
                MainFrame.getInstance().updateMessageCountLabels(board);
                MainFrame.getInstance().updateTofTree(board);
        }});
    }

    /**
     * Add new invalid msg to database 
     */
    public void receivedInvalidMessage(Board b, Calendar calDL, int index, String reason) {
        
        // first add to database, then mark slot used. this way its ok if Frost is shut down after add to db but
        // before mark of the slot.
        FrostMessageObject invalidMsg = new FrostMessageObject(b, calDL, index, reason);
        try {
            AppLayerDatabase.getMessageTable().insertMessage(invalidMsg);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting invalid message into database", e);
            return;
        }
    }
    
    /**
     * Add new valid msg to database 
     */
    public void receivedValidMessage(MessageXmlFile currentMsg, Board board, int index) {
        FrostMessageObject newMsg = new FrostMessageObject(currentMsg, board, index);
        newMsg.setNew(true);
        try {
            AppLayerDatabase.getMessageTable().insertMessage(newMsg);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting new message into database", e);
            return;
        }
        // after add to database
        processNewMessage(newMsg, board);
    }

    /**
     * Process incoming message.
     */
    private void processNewMessage(FrostMessageObject currentMsg, Board board) {

        // check if msg would be displayed (maxMessageDays)
        Calendar minDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        minDate.add(Calendar.DATE, -1*board.getMaxMessageDisplay());
        
        Calendar msgDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        msgDate.setTimeInMillis( currentMsg.getSqlDate().getTime());
        
        if( !msgDate.before(minDate) ) {
            // add new message or notify of arrival
            addNewMessageToGui(currentMsg, board);
        } // else msg is not displayed due to maxMessageDisplay
        
        processAttachedBoards(currentMsg);
    }

    /**
     * Called by non-swing thread.
     */
    private void addNewMessageToGui(final FrostMessageObject message, final Board board) {
        
        // check if message is blocked
        if( isBlocked(message, board) ) {
//            // add this msg if it replaces a dummy!
//            // DISCUSSION: better not, if a new GOOD msg arrives later in reply to this BAD, the BAD is not loaded and 
//            // dummy is created. this differes from behaviour of clean load from database            
//            if( message.getMessageId() != null ) {
//                SwingUtilities.invokeLater( new Runnable() {
//                    public void run() {
//                        Board selectedBoard = tofTreeModel.getSelectedNode();
//                        // add only if target board is still shown
//                        if( !selectedBoard.isFolder() && selectedBoard.getName().equals( board.getName() ) ) {
//                            if( tryToFillDummyMsg(message) ) {
//                                // we filled a dummy!
//                                board.incNewMessageCount();
//                                MainFrame.getInstance().updateTofTree(board);
//                                MainFrame.displayNewMessageIcon(true);
//                                MainFrame.getInstance().updateMessageCountLabels(board);
//                            }
//                        }
//                    }
//                });
//            }
            return;
        }
        // message is not blocked
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {

                board.incNewMessageCount();
                MainFrame.getInstance().updateTofTree(board);
                MainFrame.displayNewMessageIcon(true);

                Board selectedBoard = tofTreeModel.getSelectedNode();
                // add only if target board is still shown
                if( !selectedBoard.isFolder() && selectedBoard.getName().equals( board.getName() ) ) {
                    
                    addNewMessageToModel(message, board);
                    
//                    // after adding the message ensure that selected message is still shown
//                    FrostMessageObject selectedMessage = MainFrame.getInstance().getMessagePanel().getSelectedMessage();
//                    if( selectedMessage != null ) {
//                        MainFrame.getInstance().getMessagePanel().makeNodeViewable(selectedMessage);
//                    }

                    MainFrame.getInstance().updateMessageCountLabels(board);
                }
            }
        });
    }
    private boolean tryToFillDummyMsg(FrostMessageObject newMessage) {
        FrostMessageObject rootNode = (FrostMessageObject)MainFrame.getInstance().getMessageTreeModel().getRoot();
        // is there a dummy msg for this msgid?
        for(Enumeration e=rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
            FrostMessageObject mo = (FrostMessageObject)e.nextElement();
            if( mo == rootNode ) {
                continue;
            }
            if( mo.getMessageId() != null && 
                mo.getMessageId().equals(newMessage.getMessageId()) &&
                mo.isDummy()
              ) 
            {
                // previously missing msg arrived, fill dummy with message data
                mo.fillFromOtherMessage(newMessage);
                int row = MainFrame.getInstance().getMessageTreeTable().getRowForNode(mo);
                if( row >= 0 ) {
                    MainFrame.getInstance().getMessageTableModel().fireTableRowsUpdated(row, row);
                }
                return true;
            }
        }
        return false; // no dummy found
    }

    private void addNewMessageToModel(FrostMessageObject newMessage, final Board board) {
        
        // if msg has no msgid, add to root
        // else check if there is a dummy msg with this msgid, if yes replace dummy with this msg
        // if there is no dummy find direct parent of this msg and add to it.
        // if there is no direct parent, add dummy parents until first existing parent in list

        FrostMessageObject rootNode = (FrostMessageObject)MainFrame.getInstance().getMessageTreeModel().getRoot();
        
        boolean showThreads = Core.frostSettings.getBoolValue(SettingsClass.SHOW_THREADS);

        if( showThreads == false ||
            newMessage.getMessageId() == null || 
            newMessage.getInReplyToList().size() == 0 
          ) 
        {
            rootNode.add(newMessage, false);
            return;
        }
        
        if( tryToFillDummyMsg(newMessage) == true ) {
            // dummy msg filled
            return;
        }

        LinkedList msgParents = new LinkedList(newMessage.getInReplyToList());
        
        // find direct parent
        while( msgParents.size() > 0 ) {

            String directParentId = (String)msgParents.removeLast();
            
            for(Enumeration e=rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
                Object o = e.nextElement();
                if( !(o instanceof FrostMessageObject) ) {
                    continue;
                }
                FrostMessageObject mo = (FrostMessageObject)o;
                if( mo.getMessageId() != null && 
                    mo.getMessageId().equals(directParentId)
                  ) 
                {
                    mo.add(newMessage, false);
                    return;
                }
            }
            
            FrostMessageObject dummyMsg = new FrostMessageObject(directParentId, board, null);
            dummyMsg.add(newMessage, true);
            
            newMessage = dummyMsg; 
        }

        // no parent found, add tree with dummy msgs
        rootNode.add(newMessage, false);
    }
    
    /**
     * Clears the tofTable, reads in the messages to be displayed,
     * does check validity for each message and adds the messages to
     * table. Additionaly it returns a Vector with all MessageObjects
     * @param board The selected board.
     * @param daysToRead Maximum age of the messages to be displayed
     * @param table The tofTable.
     * @return Vector containing all MessageObjects that are displayed in the table.
     */
    public void updateTofTable(Board board) {
        int daysToRead = board.getMaxMessageDisplay();
        // changed to not block the swing thread

        if( updateThread != null ) {
            if( updateThread.toString().equals( board ) ) {
                // already updating
                return;
            } else {
                // stop current thread, then start new
                updateThread.cancel();
            }
        }
        // start new thread, the thread will set itself to updateThread,
        // but first it waits until the current thread is finished
        nextUpdateThread = new UpdateTofFilesThread(board, daysToRead);
        activateGlassPane();
        nextUpdateThread.start();
    }

    private void activateGlassPane() {
        MainFrame.getInstance().showProgress();
        
        // Mount the glasspane on the component window
        GlassPane aPane = GlassPane.mount(MainFrame.getInstance(), true);

        // keep track of the glasspane as an instance variable
        glassPane = aPane;

        if (glassPane != null) {
            // Start interception UI interactions
            glassPane.setVisible(true);
        }
    }
    private void deactivateGlassPane() {
        if (glassPane != null) {
            // Stop UI interception
            glassPane.setVisible(false);
            glassPane = null;
        }
        MainFrame.getInstance().hideProgress();
    }

    private class UpdateTofFilesThread extends Thread {

        Board board;
        int daysToRead;
        boolean isCancelled = false;
        String fileSeparator = System.getProperty("file.separator");
        
        public UpdateTofFilesThread(Board board, int daysToRead) {
            this.board = board;
            this.daysToRead = daysToRead;
        }

        public synchronized void cancel() {
            isCancelled = true;
        }

        public synchronized boolean isCancel() {
            return isCancelled;
        }

        public String toString() {
            return board.getName();
        }

        /**
         * Adds new messages flat to the rootnode, blocked msgs are not added.
         */
        private class FlatMessageRetrieval implements MessageDatabaseTableCallback {
            FrostMessageObject rootNode;
            public FlatMessageRetrieval(FrostMessageObject root) {
                rootNode = root;
            }
            public boolean messageRetrieved(FrostMessageObject mo) {
                if( isBlocked(mo, board) == false ) {
                    rootNode.add(mo);
                }
                return isCancel();
            }
        }

        /**
         * Adds new messages threaded to the rootnode, blocked msgs are removed if not needed for thread.
         */
        private class ThreadedMessageRetrieval implements MessageDatabaseTableCallback {
            FrostMessageObject rootNode;
            LinkedList messageList = new LinkedList();
            public ThreadedMessageRetrieval(FrostMessageObject root) {
                rootNode = root;
            }
            public boolean messageRetrieved(FrostMessageObject mo) {
                messageList.add(mo);
                return isCancel();
            }
            public void buildThreads() {
                // messageList was filled by callback
                
                // HashSet contains a msgid if the msg was loaded OR was not existing
                HashSet messageIds = new HashSet();

                for(Iterator i=messageList.iterator(); i.hasNext(); ) {
                    FrostMessageObject mo = (FrostMessageObject)i.next();
                    if( mo.getMessageId() == null ) {
                        i.remove();
                        // old msg, maybe add to root
                        if( !isBlocked(mo, mo.getBoard()) ) {
                            rootNode.add(mo);
                        }
                    } else {
                        // collect for threading
                        messageIds.add(mo.getMessageId());
                    }
                }
                
                // for threads, check msgrefs and load all existing msgs pointed to by refs
                boolean showDeletedMessages = Core.frostSettings.getBoolValue("showDeletedMessages");
                LinkedList newLoadedMsgs = new LinkedList();
                for(Iterator i=messageList.iterator(); i.hasNext(); ) {
                    FrostMessageObject mo = (FrostMessageObject)i.next();
                    List l = mo.getInReplyToList();
                    if( l.size() == 0 ) {
                        continue; // no msg refs
                    }
                    // try to load each msgid that is referenced, put tried ids into hashset msgIds
                    for(int x=l.size()-1; x>=0; x--) {
                        String anId = (String)l.get(x);
                        if( messageIds.contains(anId) ) {
                            continue;
                        }
                        FrostMessageObject fmo = null;
                        try {
                            fmo = AppLayerDatabase.getMessageTable().retrieveMessageByMessageId(board, anId, true, true, showDeletedMessages);
                        } catch (SQLException e) {
                            logger.log(Level.SEVERE, "Error retrieving message by id "+anId, e);
                        }
                        if( fmo == null ) {
                            // for each missing msg create a dummy FrostMessageObject and add it to tree.
                            // if the missing msg arrives later, replace dummy with true msg in tree
                            LinkedList ll = new LinkedList();
                            if( x > 0 ) {
                                for(int y=0; y < x; y++) {
                                    ll.add(l.get(y));
                                }
                            }
                            fmo = new FrostMessageObject(anId, board, ll);
                        }
                        newLoadedMsgs.add(fmo);
                        messageIds.add(anId);
                    }
                }

                messageList.addAll(newLoadedMsgs);
                
                newLoadedMsgs = null;
                messageIds = null;
                
                // all msgs are loaded and dummies for missing msgs were created, now build the threads
                // - add msgs without msgid to rootnode
                // - add msgs with msgid and no ref to rootnode
                // - add msgs with msgid and ref to its direct parent (last refid in list)
                
                // first collect msgs with id into a hashtable for lookups
                Hashtable messagesTableById = new Hashtable();
                for(Iterator i=messageList.iterator(); i.hasNext(); ) {
                    FrostMessageObject mo = (FrostMessageObject)i.next();
                    messagesTableById.put(mo.getMessageId(), mo);
                }
                
                messageList = null;

                // build the threads
                for(Iterator i=messagesTableById.values().iterator(); i.hasNext(); ) {
                    FrostMessageObject mo = (FrostMessageObject)i.next();
                    LinkedList l = mo.getInReplyToList();
                    if( l.size() == 0 ) {
                        rootNode.add(mo);
                    } else {
                        String directParentId = (String)l.getLast();
                        FrostMessageObject parentMo = (FrostMessageObject)messagesTableById.get(directParentId);
                        parentMo.add(mo);
                    }
                }
                
                // finally, remove blocked msgs from the leafs!
                LinkedList itemsToRemove = new LinkedList(); 
                while(true) {
                    for(Enumeration e=rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
                        FrostMessageObject mo = (FrostMessageObject)e.nextElement();
                        if( mo.isLeaf() && mo != rootNode ) {
                            if( mo.isDummy() || isBlocked(mo, mo.getBoard()) ) {
                                itemsToRemove.add(mo);
                            }
                        }
                    }
                    if( itemsToRemove.size() > 0 ) {
                        for( Iterator iter = itemsToRemove.iterator(); iter.hasNext(); ) {
                            FrostMessageObject removeMo = (FrostMessageObject) iter.next();
                            removeMo.removeFromParent();
                        }
                        itemsToRemove.clear(); // clear for next run
                    } else {
                        // no more blocked leafs
                        break;
                    }
                }
            }
        }

        /**
         * Start to load messages one by one.
         */
        private void loadMessages(MessageDatabaseTableCallback callback) {
            
            boolean showDeletedMessages = Core.frostSettings.getBoolValue("showDeletedMessages");
            
            try {
                AppLayerDatabase.getMessageTable().retrieveMessagesForShow(
                        board, 
                        daysToRead, 
                        true, 
                        true, 
                        showDeletedMessages,
                        callback);
                
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error retrieving messages for board "+board.getName(), e);
            }
        }
        
        public void run() {
            while( updateThread != null ) {
                // wait for running thread to finish
                Mixed.wait(250);
                if( nextUpdateThread != this ) {
                    // leave, there is a newer thread than we waiting
                    return;
                }
            }
            // paranoia: are WE the next thread?
            if( nextUpdateThread != this ) {
                return;
            } else {
                updateThread = this;
            }

            // lower thread prio to allow users to select and view messages while this thread runs
            try { setPriority(getPriority() - 1); }
            catch(Throwable t) { }

            final FrostMessageObject rootNode = new FrostMessageObject(true);

            boolean loadThreads = Core.frostSettings.getBoolValue(SettingsClass.SHOW_THREADS);
            if( loadThreads  ) {
                ThreadedMessageRetrieval tmr = new ThreadedMessageRetrieval(rootNode);
                long l1 = System.currentTimeMillis();
                loadMessages(tmr);
                long l2 = System.currentTimeMillis();
                tmr.buildThreads();
                long l3 = System.currentTimeMillis();
                // FIXME: debug output only!
                System.out.println("loading board "+board.getName()+": load="+(l2-l1)+", build+subretrieve="+(l3-l2)); 
            } else {
                // load flat
                FlatMessageRetrieval ffr = new FlatMessageRetrieval(rootNode);
                loadMessages(ffr);
            }
            
            if( !isCancel() ) {
                // set rootnode to gui and update
                final Board innerTargetBoard = board;

                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        if( tofTreeModel.getSelectedNode().isFolder() == false &&
                            tofTreeModel.getSelectedNode().getName().equals( innerTargetBoard.getName() ) ) {
                            
                            MainFrame.getInstance().getMessagePanel().getMessageTable().setNewRootNode(rootNode);
                            MainFrame.getInstance().getMessageTreeTable().expandAll(true);
                            
                            MainFrame.getInstance().updateTofTree(innerTargetBoard);
                            MainFrame.getInstance().updateMessageCountLabels(innerTargetBoard);
                        }
                    }
                });
            }
            deactivateGlassPane();
            updateThread = null;
        }
    }

    /**
     * Returns true if the message should not be displayed
     * @param message The message object to check
     * @return true if message is blocked, else false
     */
    public boolean isBlocked(FrostMessageObject message, Board board) {

        if (board.getShowSignedOnly()
            && (message.isMessageStatusOLD() || message.isMessageStatusTAMPERED()) )
        {
            return true;
        }
        if (board.getHideBad() && message.isMessageStatusBAD()) {
            return true;
        }
        if (board.getHideCheck() && message.isMessageStatusCHECK()) {
            return true;
        }
        if (board.getHideObserve() && message.isMessageStatusOBSERVE()) {
            return true;
        }
        //If the message is not signed and contains a @ character in the from field, we block it.
        if (message.isMessageStatusOLD() && message.getFromName().indexOf('@') > -1) {
            return true;
        }

        // Block by subject (and rest of the header)
        if (Core.frostSettings.getBoolValue("blockMessageChecked")) {
            String header = message.getSubject().toLowerCase();
            StringTokenizer blockWords = new StringTokenizer(Core.frostSettings.getValue("blockMessage"), ";");
            boolean found = false;
            while (blockWords.hasMoreTokens() && !found) {
                String blockWord = blockWords.nextToken().trim();
                if ((blockWord.length() > 0) && (header.indexOf(blockWord) != -1)) {
                    found = true;
                }
            }
            if (found) {
                return true;
            }
        }
        // Block by body
        if (Core.frostSettings.getBoolValue("blockMessageBodyChecked")) {
            String content = message.getContent().toLowerCase();
            StringTokenizer blockWords =
                new StringTokenizer(Core.frostSettings.getValue("blockMessageBody"), ";");
            boolean found = false;
            while (blockWords.hasMoreTokens() && !found) {
                String blockWord = blockWords.nextToken().trim();
                if ((blockWord.length() > 0) && (content.indexOf(blockWord) != -1)) {
                    found = true;
                }
            }
            if (found) {
                return true;
            }
        }
        // Block by attached boards
        if (Core.frostSettings.getBoolValue("blockMessageBoardChecked")) {
            List boards = message.getAttachmentsOfType(Attachment.BOARD);
            StringTokenizer blockWords =
                new StringTokenizer(Core.frostSettings.getValue("blockMessageBoard"), ";");
            boolean found = false;
            while (blockWords.hasMoreTokens() && !found) {
                String blockWord = blockWords.nextToken().trim();
                Iterator boardsIterator = boards.iterator();
                while (boardsIterator.hasNext()) {
                    BoardAttachment boardAttachment = (BoardAttachment) boardsIterator.next();
                    Board boardObject = boardAttachment.getBoardObj();
                    if ((blockWord.length() > 0) && (boardObject.getName().equalsIgnoreCase(blockWord))) {
                        found = true;
                    }
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }

    /**
     * Maybe add the attached board to list of known boards.
     */
    private void processAttachedBoards(FrostMessageObject currentMsg) {
        if( currentMsg.isMessageStatusOLD() &&
            Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_UNSIGNED) == true )
        {
            logger.info("Boards from unsigned message blocked");
        } else if( currentMsg.isMessageStatusBAD() &&
                   Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_BAD) == true )
        {
            logger.info("Boards from BAD message blocked");
        } else if( currentMsg.isMessageStatusCHECK() &&
                   Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_CHECK) == true )
        {
            logger.info("Boards from CHECK message blocked");
        } else if( currentMsg.isMessageStatusOBSERVE() &&
                   Core.frostSettings.getBoolValue(SettingsClass.BLOCK_BOARDS_FROM_OBSERVE) == true )
        {
            logger.info("Boards from OBSERVE message blocked");
        } else if( currentMsg.isMessageStatusTAMPERED() ) {
            logger.info("Boards from TAMPERED message blocked");
        } else {
            // either GOOD user or not blocked by user
            LinkedList addBoards = new LinkedList();
            for(Iterator i=currentMsg.getAttachmentsOfType(Attachment.BOARD).iterator(); i.hasNext(); ) {
                BoardAttachment ba = (BoardAttachment) i.next();
                addBoards.add(ba.getBoardObj());
            }
            AppLayerDatabase.getKnownBoardsDatabaseTable().addNewKnownBoards(addBoards);
        }
    }

    public void searchAllNewMessages(boolean runWithinThread) {
        if( runWithinThread ) {
            new Thread() {
                public void run() {
                    searchAllNewMessages();
                }
            }.start();
        } else {
            searchAllNewMessages();
        }
    }

    public void searchNewMessages(final Board board) {
        new Thread() {
            public void run() {
                searchNewMessagesInBoard(board);
            }
        }.start();
    }

    private void searchAllNewMessages() {
        Enumeration e = ((DefaultMutableTreeNode) tofTreeModel.getRoot()).depthFirstEnumeration();
        while( e.hasMoreElements() ) {
            Board board = (Board)e.nextElement();
            searchNewMessagesInBoard(board);
        }
    }
    
    private void searchNewMessagesInBoard(final Board board) {
        if( board.isFolder() == true ) {
            return;
        }

        int daysToRead = board.getMaxMessageDisplay();

        int beforeMessages = board.getNewMessageCount(); // remember old val to track if new msg. arrived
        
        int newMessages = 0;
        try {
            newMessages = AppLayerDatabase.getMessageTable().getNewMessageCount(board, daysToRead);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving new message count", e);
        }

        // count new messages arrived while processing
        int arrivedMessages = board.getNewMessageCount() - beforeMessages;
        if( arrivedMessages > 0 ) {
            newMessages += arrivedMessages;
        }

        board.setNewMessageCount(newMessages);

        // now a board is finished, update the tree
        SwingUtilities.invokeLater( new Runnable() {
               public void run() {
                   MainFrame.getInstance().updateTofTree(board);
               }
           });
    }
}
