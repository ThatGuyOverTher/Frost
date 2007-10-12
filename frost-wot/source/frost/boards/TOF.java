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
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.joda.time.*;

import frost.*;
import frost.gui.*;
import frost.gui.messagetreetable.*;
import frost.messages.*;
import frost.storage.*;
import frost.storage.perst.messages.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * @pattern Singleton
 */
public class TOF {

    // ATTN: if a new message arrives during update of a board, the msg cannot be inserted into db because
    //       the methods are synchronized. So the add of msg occurs after the load of the board.
    //       there is no sync problem.

    private static final Logger logger = Logger.getLogger(TOF.class.getName());

    private static final Language language = Language.getInstance();

    private UpdateTofFilesThread updateThread = null;
    private UpdateTofFilesThread nextUpdateThread = null;

    private final TofTreeModel tofTreeModel;

    private static boolean initialized = false;

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
    private TOF(final TofTreeModel tofTreeModel) {
        super();
        this.tofTreeModel = tofTreeModel;
    }

    /**
     * This method initializes the TOF.
     * If it has already been initialized, this method does nothing.
     * @param tofTreeModel this is the TofTreeModel this TOF will operate on
     */
    public static void initialize(final TofTreeModel tofTreeModel) {
        if (!initialized) {
            initialized = true;
            instance = new TOF(tofTreeModel);
        }
    }

    public void markAllMessagesRead(final AbstractNode node) {
        markAllMessagesRead(node, true);
    }

    private void markAllMessagesRead(final AbstractNode node, final boolean confirm) {
        if (node == null) {
            return;
        }

        if (node.isBoard()) {
            if( confirm ) {
                final int answer = MiscToolkit.showSuppressableConfirmDialog(
                        MainFrame.getInstance(),
                        language.formatMessage("TOF.markAllReadConfirmation.board.content", node.getName()),
                        language.getString("TOF.markAllReadConfirmation.board.title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        SettingsClass.CONFIRM_MARK_ALL_MSGS_READ,
                        language.getString("Common.suppressConfirmationCheckbox") );
                if( answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            setAllMessagesRead((Board)node);
        } else if(node.isFolder()) {
            if( confirm ) {
                final int answer = MiscToolkit.showSuppressableConfirmDialog(
                        MainFrame.getInstance(),
                        language.formatMessage("TOF.markAllReadConfirmation.folder.content", node.getName()),
                        language.getString("TOF.markAllReadConfirmation.folder.title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        SettingsClass.CONFIRM_MARK_ALL_MSGS_READ,
                        language.getString("Common.suppressConfirmationCheckbox") );
                if( answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            // process all childs recursive
            final Enumeration<AbstractNode> leafs = node.children();
            while (leafs.hasMoreElements()) {
                markAllMessagesRead(leafs.nextElement(), false);
            }
        }
    }

    /**
     * Resets the NEW state to READ for all messages shown in board table.
     *
     * @param tableModel  the messages table model
     * @param board  the board to reset
     */
    private void setAllMessagesRead(final Board board) {
        // now takes care if board is changed during mark read of many boards! reloads current table if needed

        final int oldNewMessageCount = board.getNewMessageCount();

        MessageStorage.inst().setAllMessagesRead(board);

        // if this board is currently shown, update messages in table
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)
            MainFrame.getInstance().getMessagePanel().getMessageTable().getTree().getModel().getRoot();

        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if( MainFrame.getInstance().getTofTreeModel().getSelectedNode() == board ) {
                    for(final Enumeration e = rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
                        final Object o = e.nextElement();
                        if( o instanceof FrostMessageObject ) {
                            final FrostMessageObject mo = (FrostMessageObject)o;
                            if( mo.isNew() ) {
                                mo.setNew(false);
                                // fire update for visible rows in table model
                                final int row = MainFrame.getInstance().getMessageTreeTable().getRowForNode(mo);
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
                final int diffNewMsgCount = board.getNewMessageCount() - oldNewMessageCount;
                board.setNewMessageCount( (diffNewMsgCount<0 ? 0 : diffNewMsgCount) );

                MainFrame.getInstance().updateMessageCountLabels(board);
                MainFrame.getInstance().updateTofTree(board);
        }});
    }

    /**
     * Add new invalid msg to database
     */
    public void receivedInvalidMessage(final Board b, final DateTime date, final int index, final String reason) {
        // first add to database, then mark slot used. this way its ok if Frost is shut down after add to db but
        // before mark of the slot.
        final FrostMessageObject invalidMsg = new FrostMessageObject(b, date, index, reason);
        invalidMsg.setNew(false);
        try {
            MessageStorage.inst().insertMessage(invalidMsg, true);
        } catch (final Throwable e) {
            // paranoia
            logger.log(Level.SEVERE, "Error inserting invalid message into database", e);
        }
    }

    /**
     * Add new valid msg to database
     */
    public void receivedValidMessage(final MessageXmlFile currentMsg, final Board board, final int index) {
        final FrostMessageObject newMsg = new FrostMessageObject(currentMsg, board, index);
        receivedValidMessage(newMsg, board, index);
    }
    /**
     * Add new valid msg to database
     */
    public void receivedValidMessage(final FrostMessageObject newMsg, final Board board, final int index) {

        if( newMsg.isMessageFromME() && Core.frostSettings.getBoolValue(SettingsClass.HANDLE_OWN_MESSAGES_AS_NEW_DISABLED) ) {
            newMsg.setNew(false);
        } else {
            newMsg.setNew(true);
        }

        final boolean isBlocked = isBlocked(newMsg, board);
        if( isBlocked ) {
            // if message is blocked, reset new state
            newMsg.setNew(false);
        }

        int messageInsertedRC;
        try {
            messageInsertedRC = MessageStorage.inst().insertMessage(newMsg, true);
        } catch (final Throwable e) {
            // paranoia
            logger.log(Level.SEVERE, "Error inserting new message into database", e);
            return;
        }

        // don't add msg if it was a duplicate or if insert into database failed
        if( messageInsertedRC != MessageStorage.INSERT_OK ) {
            return; // not inserted into database, do not add to gui
        }

        // after add to database
        processNewMessage(newMsg, board, isBlocked);
    }

    /**
     * Process incoming message.
     */
    private void processNewMessage(final FrostMessageObject currentMsg, final Board board, final boolean isBlocked) {

        // check if msg would be displayed (maxMessageDays)
        final DateTime min = new LocalDate(DateTimeZone.UTC).minusDays(board.getMaxMessageDisplay()).toDateTimeAtMidnight();
        final DateTime msgDate = new DateTime(currentMsg.getDateAndTime(), DateTimeZone.UTC);

        if( msgDate.getMillis() > min.getMillis() ) {
            // add new message or notify of arrival
            addNewMessageToGui(currentMsg, board, isBlocked);
        } // else msg is not displayed due to maxMessageDisplay

        processAttachedBoards(currentMsg);
    }

    /**
     * Called by non-swing thread.
     */
    private void addNewMessageToGui(final FrostMessageObject message, final Board board, final boolean isBlocked) {

        // check if message is blocked
        if( isBlocked ) {
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
                if( message.isNew() ) {
                    board.newMessageReceived(); // notify receive of new msg (for board update)
                    board.incNewMessageCount(); // increment new message count
                    MainFrame.getInstance().updateTofTree(board);
                    MainFrame.getInstance().displayNewMessageIcon(true);
                }

                final AbstractNode selectedNode = tofTreeModel.getSelectedNode();
                // add only if target board is still shown
                if( selectedNode.isBoard() && selectedNode.getName().equals( board.getName() ) ) {
                    addNewMessageToModel(message, board);
                    MainFrame.getInstance().updateMessageCountLabels(board);
                }
            }
        });
    }
    private boolean tryToFillDummyMsg(final FrostMessageObject newMessage) {
        final FrostMessageObject rootNode = (FrostMessageObject)MainFrame.getInstance().getMessageTreeModel().getRoot();
        // is there a dummy msg for this msgid?
        for(final Enumeration e=rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
            final FrostMessageObject mo = (FrostMessageObject)e.nextElement();
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
                final int row = MainFrame.getInstance().getMessageTreeTable().getRowForNode(mo);
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

        final FrostMessageObject rootNode = (FrostMessageObject)MainFrame.getInstance().getMessageTreeModel().getRoot();

        final boolean showThreads = Core.frostSettings.getBoolValue(SettingsClass.SHOW_THREADS);

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

        final LinkedList<String> msgParents = new LinkedList<String>(newMessage.getInReplyToList());

        // find direct parent
        while( msgParents.size() > 0 ) {

            final String directParentId = msgParents.removeLast();

            for(final Enumeration e = rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
                final FrostMessageObject mo = (FrostMessageObject) e.nextElement();
                if( mo.getMessageId() != null &&
                    mo.getMessageId().equals(directParentId)
                  )
                {
                    mo.add(newMessage, false);
                    return;
                }
            }

            final FrostMessageObject dummyMsg = new FrostMessageObject(directParentId, board, null);
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
    public void updateTofTable(final Board board, final FrostMessageObject prevSelectedMsg) {
        final int daysToRead = board.getMaxMessageDisplay();

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
        nextUpdateThread = new UpdateTofFilesThread(board, daysToRead, prevSelectedMsg);
        MainFrame.getInstance().activateGlassPane();
        nextUpdateThread.start();
    }

    private class UpdateTofFilesThread extends Thread {

        Board board;
        int daysToRead;
        boolean isCancelled = false;
        String fileSeparator = System.getProperty("file.separator");
        FrostMessageObject previousSelectedMsg;

        public UpdateTofFilesThread(final Board board, final int daysToRead, final FrostMessageObject prevSelectedMsg) {
            this.board = board;
            this.daysToRead = daysToRead;
            this.previousSelectedMsg = prevSelectedMsg;
        }

        public synchronized void cancel() {
            isCancelled = true;
        }

        public synchronized boolean isCancel() {
            return isCancelled;
        }

        @Override
        public String toString() {
            return board.getName();
        }

        /**
         * Adds new messages flat to the rootnode, blocked msgs are not added.
         */
        private class FlatMessageRetrieval implements MessageCallback {

            private final FrostMessageObject rootNode;
            private final boolean blockMsgSubject;
            private final boolean blockMsgBody;
            private final boolean blockMsgBoardname;

            public FlatMessageRetrieval(final FrostMessageObject root) {
                rootNode = root;
                blockMsgSubject = Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BLOCK_SUBJECT_ENABLED);
                blockMsgBody = Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BLOCK_BODY_ENABLED);
                blockMsgBoardname = Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BLOCK_BOARDNAME_ENABLED);
            }
            public boolean messageRetrieved(final FrostMessageObject mo) {
                if( !isBlocked(mo, board, blockMsgSubject, blockMsgBody, blockMsgBoardname) ) {
                    rootNode.add(mo);
                } else {
                    System.out.println("blocked message");
                }
                return isCancel();
            }
        }

        /**
         * Adds new messages threaded to the rootnode, blocked msgs are removed if not needed for thread.
         */
        private class ThreadedMessageRetrieval implements MessageCallback {

            final FrostMessageObject rootNode;
            LinkedList<FrostMessageObject> messageList = new LinkedList<FrostMessageObject>();

            public ThreadedMessageRetrieval(final FrostMessageObject root) {
                rootNode = root;
            }
            public boolean messageRetrieved(final FrostMessageObject mo) {
                messageList.add(mo);
                return isCancel();
            }
            public void buildThreads() {
                // messageList was filled by callback

                // HashSet contains a msgid if the msg was loaded OR was not existing
                HashSet<String> messageIds = new HashSet<String>();

                for(final Iterator<FrostMessageObject> i=messageList.iterator(); i.hasNext(); ) {
                    final FrostMessageObject mo = i.next();
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
                final boolean showDeletedMessages = Core.frostSettings.getBoolValue("showDeletedMessages");
                LinkedList<FrostMessageObject> newLoadedMsgs = new LinkedList<FrostMessageObject>();
                LinkedList<FrostMessageObject> newLoadedMsgs2 = new LinkedList<FrostMessageObject>();

                loadInReplyToMessages(messageList, messageIds, showDeletedMessages, newLoadedMsgs);

                // load all linked messages, only needed when only new msgs are shown and some msgs have invalid
                // refs (sometimes sent by other clients)
                while( loadInReplyToMessages(newLoadedMsgs, messageIds, showDeletedMessages, newLoadedMsgs2) ) {
                    messageList.addAll(newLoadedMsgs);
                    newLoadedMsgs = newLoadedMsgs2;
                    newLoadedMsgs2 = new LinkedList<FrostMessageObject>();
                }
                messageList.addAll(newLoadedMsgs);

                // help the garbage collector
                newLoadedMsgs = null;
                messageIds = null;

                // all msgs are loaded and dummies for missing msgs were created, now build the threads
                // - add msgs without msgid to rootnode
                // - add msgs with msgid and no ref to rootnode
                // - add msgs with msgid and ref to its direct parent (last refid in list)

                // first collect msgs with id into a Map for lookups
                final HashMap<String,FrostMessageObject> messagesTableById = new HashMap<String,FrostMessageObject>();
                for( final FrostMessageObject mo : messageList ) {
                    messagesTableById.put(mo.getMessageId(), mo);
                }

                // help the garbage collector
                messageList = null;

                // build the threads
                for( final FrostMessageObject mo : messagesTableById.values() ) {
                    final ArrayList<String> l = mo.getInReplyToList();
                    if( l.size() == 0 ) {
                        // a root message, no replyTo
                        rootNode.add(mo);
                    } else {
                        // add to direct parent
                        final String directParentId = l.get(l.size()-1); // getLast
                        if( directParentId == null ) {
                            logger.log(Level.SEVERE, "Should never happen: directParentId is null; msg="+mo.getMessageId()+"; parentMsg="+directParentId);
                            continue;
                        }
                        final FrostMessageObject parentMo = messagesTableById.get(directParentId);
                        if( parentMo == null ) {
                            // FIXME: happens if someone sends a faked msg with parentids from 2 different threads.
                            //  gives NPE if one of the messages is already in its own thread
                            logger.log(Level.SEVERE, "Should never happen: parentMo is null; msg="+mo.getMessageId()+"; parentMsg="+directParentId+"; irtl="+mo.getInReplyTo());
                            continue;
                        }
                        parentMo.add(mo);
                    }
                }

                // remove blocked msgs from the leafs
                final boolean blockMsgSubject = Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BLOCK_SUBJECT_ENABLED);
                final boolean blockMsgBody = Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BLOCK_BODY_ENABLED);
                final boolean blockMsgBoardname = Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BLOCK_BOARDNAME_ENABLED);
                final List<FrostMessageObject> itemsToRemove = new ArrayList<FrostMessageObject>();
                final Set<String> checkedMessageIds = new HashSet<String>();
                while(true) {
                    for(final Enumeration e=rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
                        final FrostMessageObject mo = (FrostMessageObject)e.nextElement();
                        if( mo.isLeaf() && mo != rootNode ) {
                            if( mo.isDummy() ) {
                                itemsToRemove.add(mo);
                            } else if( mo.getMessageId() != null) {
                                if( isBlocked(mo, mo.getBoard(), blockMsgSubject, blockMsgBody, blockMsgBoardname) ) {
                                    itemsToRemove.add(mo);
                                }
                            } else {
                                if( checkedMessageIds.contains(mo.getMessageId()) ) {
                                    continue; // already checked
                                }
                                if( isBlocked(mo, mo.getBoard(), blockMsgSubject, blockMsgBody, blockMsgBoardname) ) {
                                    itemsToRemove.add(mo);
                                } else {
                                    // checked and not blocked
                                    checkedMessageIds.add(mo.getMessageId());
                                }
                            }
                        }
                    }
                    if( itemsToRemove.size() > 0 ) {
                        for( final FrostMessageObject removeMo : itemsToRemove ) {
                            removeMo.removeFromParent();
                        }
                        itemsToRemove.clear(); // clear for next run
                    } else {
                        // no more blocked leafs
                        break;
                    }
                }
                // clean up
                checkedMessageIds.clear();

                // apply the subject of first child message to dummy ROOT messages
                for(final Enumeration e=rootNode.children(); e.hasMoreElements(); ) {
                    final FrostMessageObject mo = (FrostMessageObject)e.nextElement();
                    if( mo.isDummy() ) {
                        // this thread root node has no subject, get subject of first valid child
                        for(final Enumeration e2=mo.breadthFirstEnumeration(); e2.hasMoreElements(); ) {
                            final FrostMessageObject childMo = (FrostMessageObject)e2.nextElement();
                            if( !childMo.isDummy() && childMo.getSubject() != null ) {
                                final StringBuilder sb = new StringBuilder(childMo.getSubject().length() + 2);
                                sb.append("[").append(childMo.getSubject()).append("]");
                                mo.setSubject(sb.toString());
                                break;
                            }
                        }
                    }
                }
            }

            private boolean loadInReplyToMessages(
                    final List<FrostMessageObject> messages,
                    final HashSet<String> messageIds,
                    final boolean showDeletedMessages,
                    final LinkedList<FrostMessageObject> newLoadedMsgs)
            {
                boolean msgWasMissing = false;
                for(final FrostMessageObject mo : messages ) {
                    final List<String> l = mo.getInReplyToList();
                    if( l.size() == 0 ) {
                        continue; // no msg refs
                    }

                    // try to load each referenced msgid, put tried ids into hashset msgIds
                    for(int x=l.size()-1; x>=0; x--) {
                        final String anId = l.get(x);
                        if( anId == null ) {
                            logger.log(Level.SEVERE, "Should never happen: message id is null! msgId="+mo.getMessageId());
                            continue;
                        }

                        if( messageIds.contains(anId) ) {
                            continue;
                        }

                        FrostMessageObject fmo = MessageStorage.inst().retrieveMessageByMessageId(
                                board,
                                anId,
                                false,
                                false,
                                showDeletedMessages);
                        if( fmo == null ) {
                            // for each missing msg create a dummy FrostMessageObject and add it to tree.
                            // if the missing msg arrives later, replace dummy with true msg in tree
                            final ArrayList<String> ll = new ArrayList<String>(x);
                            if( x > 0 ) {
                                for(int y=0; y < x; y++) {
                                    ll.add(l.get(y));
                                }
                            }
                            fmo = new FrostMessageObject(anId, board, ll);
                        }
                        newLoadedMsgs.add(fmo);
                        messageIds.add(anId);
                        if( !msgWasMissing ) {
                            msgWasMissing = true;
                        }
                    }
                }
                return msgWasMissing;
            }
        }

        /**
         * Start to load messages one by one.
         */
        private void loadMessages(final MessageCallback callback) {

            final boolean showDeletedMessages = Core.frostSettings.getBoolValue("showDeletedMessages");
            final boolean showUnreadOnly = Core.frostSettings.getBoolValue(SettingsClass.SHOW_UNREAD_ONLY);
            MessageStorage.inst().retrieveMessagesForShow(
                    board,
                    daysToRead,
                    false,
                    false,
                    showDeletedMessages,
                    showUnreadOnly,
                    callback);
        }

        @Override
        public void run() {
            while( updateThread != null ) {
                // wait for running thread to finish
                Mixed.wait(150);
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

            final FrostMessageObject rootNode = new FrostMessageObject(true);

            final boolean loadThreads = Core.frostSettings.getBoolValue(SettingsClass.SHOW_THREADS);

            // update SortStateBean
            MessageTreeTableSortStateBean.setThreaded(loadThreads);

            try {
                if( loadThreads  ) {
                    final ThreadedMessageRetrieval tmr = new ThreadedMessageRetrieval(rootNode);
                    final long l1 = System.currentTimeMillis();
                    loadMessages(tmr);
                    final long l2 = System.currentTimeMillis();
                    tmr.buildThreads();
                    final long l3 = System.currentTimeMillis();
                    // TODO: debug output only!
                    System.out.println("loading board "+board.getName()+": load="+(l2-l1)+", build+subretrieve="+(l3-l2));
                } else {
                    // load flat
                    final FlatMessageRetrieval ffr = new FlatMessageRetrieval(rootNode);
                    loadMessages(ffr);
                }
            } catch (final Throwable t) {
                logger.log(Level.SEVERE, "Excpetion during thread load/build", t);
            }

            if( !isCancel() ) {
                // count new messages and check if board has flagged or starred messages
                int newMessageCountWork = 0;
                boolean hasStarredWork = false;
                boolean hasFlaggedWork = false;
                for(final Enumeration e=rootNode.depthFirstEnumeration(); e.hasMoreElements(); ) {
                    final FrostMessageObject mo = (FrostMessageObject)e.nextElement();
                    if( mo.isNew() ) {
                        newMessageCountWork++;
                    }
                    if( !hasStarredWork && mo.isStarred() ) {
                        hasStarredWork = true;
                    }
                    if( !hasFlaggedWork && mo.isFlagged() ) {
                        hasFlaggedWork = true;
                    }
                }

                // set rootnode to gui and update
                final Board innerTargetBoard = board;
                final int newMessageCount = newMessageCountWork;
                final boolean newHasFlagged = hasFlaggedWork;
                final boolean newHasStarred = hasStarredWork;
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        innerTargetBoard.setNewMessageCount(newMessageCount);
                        innerTargetBoard.hasFlaggedMessages(newHasFlagged);
                        innerTargetBoard.hasStarredMessages(newHasStarred);
                        setNewRootNode(innerTargetBoard, rootNode, previousSelectedMsg);
                    }
                });
            } else if( nextUpdateThread == null ) {
                MainFrame.getInstance().deactivateGlassPane();
            }
            updateThread = null;
        }

        /**
         * Set rootnode to gui and update.
         */
        private void setNewRootNode(final Board innerTargetBoard, final FrostMessageObject rootNode, final FrostMessageObject previousSelectedMsg) {
            if( tofTreeModel.getSelectedNode().isBoard() &&
                    tofTreeModel.getSelectedNode().getName().equals( innerTargetBoard.getName() ) )
            {
                final MessageTreeTable treeTable = MainFrame.getInstance().getMessageTreeTable();

                treeTable.setNewRootNode(rootNode);
                if( !Core.frostSettings.getBoolValue(SettingsClass.MSGTABLE_SHOW_COLLAPSED_THREADS) ) {
                    treeTable.expandAll(true);
                }

                MainFrame.getInstance().updateTofTree(innerTargetBoard);
                MainFrame.getInstance().updateMessageCountLabels(innerTargetBoard);

                // maybe select previously selected message
                if( previousSelectedMsg != null && previousSelectedMsg.getMessageId() != null ) {
                    for(final Enumeration e=rootNode.breadthFirstEnumeration(); e.hasMoreElements(); ) {
                        final FrostMessageObject mo = (FrostMessageObject) e.nextElement();
                        if( mo.getMessageId() != null && mo.getMessageId().equals(previousSelectedMsg.getMessageId()) ) {
                            int row = treeTable.getRowForNode(mo);
                            if( row > -1 ) {
                                treeTable.getSelectionModel().setSelectionInterval(row, row);
                                // scroll to selected row
                                if( (row+1) < treeTable.getRowCount() ) {
                                    row++;
                                }
                                final Rectangle r = treeTable.getCellRect(row, 0, true);
                                treeTable.scrollRectToVisible(r);
                            }
                            break;
                        }
                    }
                }
                MainFrame.getInstance().deactivateGlassPane();
            }
        }
    }

    /**
     * Returns true if the message should not be displayed
     * @return true if message is blocked, else false
     */
    public boolean isBlocked(final FrostMessageObject message, final Board board) {
        return isBlocked(
                message,
                board,
                Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BLOCK_SUBJECT_ENABLED),
                Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BLOCK_BODY_ENABLED),
                Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_BLOCK_BOARDNAME_ENABLED));
    }

    /**
     * Returns true if the message should not be displayed
     * @return true if message is blocked, else false
     */
    public boolean isBlocked(
            final FrostMessageObject message,
            final Board board,
            final boolean blockMsgSubject,
            final boolean blockMsgBody,
            final boolean blockMsgBoardname)
    {

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
            // FIXME: here we fix a problem with older frosts: a message is sent anonymously,
            //        but the sender is a unique name. We remove the '@' to avoid confusion
            // -> this solves the problem that a new msg is indicated, but not shown
            message.setFromName(message.getFromName().replace('@','_'));
            // return false; // we would block this message
        }

        // Block by subject (and rest of the header)
        if ( blockMsgSubject ) {
            final String header = message.getSubject().toLowerCase();
            final StringTokenizer blockWords =
                new StringTokenizer(Core.frostSettings.getValue(SettingsClass.MESSAGE_BLOCK_SUBJECT), ";");
            while (blockWords.hasMoreTokens()) {
                final String blockWord = blockWords.nextToken().trim();
                if ((blockWord.length() > 0) && (header.indexOf(blockWord) >= 0)) {
                    return true;
                }
            }
        }
        // Block by body
        if ( blockMsgBody ) {
            final String content = message.getContent().toLowerCase();
            final StringTokenizer blockWords =
                new StringTokenizer(Core.frostSettings.getValue(SettingsClass.MESSAGE_BLOCK_BODY), ";");
            while (blockWords.hasMoreTokens()) {
                final String blockWord = blockWords.nextToken().trim();
                if ((blockWord.length() > 0) && (content.indexOf(blockWord) >= 0)) {
                    return true;
                }
            }
        }
        // Block by attached boards
        if ( blockMsgBoardname ) {
            final List<BoardAttachment> boards = message.getAttachmentsOfType(Attachment.BOARD);
            final StringTokenizer blockWords =
                new StringTokenizer(Core.frostSettings.getValue(SettingsClass.MESSAGE_BLOCK_BOARDNAME), ";");
            while (blockWords.hasMoreTokens()) {
                final String blockWord = blockWords.nextToken().trim();
                for( final BoardAttachment boardAttachment : boards ) {
                    final Board boardObject = boardAttachment.getBoardObj();
                    if ((blockWord.length() > 0) && (boardObject.getName().equalsIgnoreCase(blockWord))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Maybe add the attached board to list of known boards.
     */
    private void processAttachedBoards(final FrostMessageObject currentMsg) {
        if( currentMsg.isMessageStatusOLD() &&
            Core.frostSettings.getBoolValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_UNSIGNED) == true )
        {
            logger.info("Boards from unsigned message blocked");
        } else if( currentMsg.isMessageStatusBAD() &&
                   Core.frostSettings.getBoolValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_BAD) == true )
        {
            logger.info("Boards from BAD message blocked");
        } else if( currentMsg.isMessageStatusCHECK() &&
                   Core.frostSettings.getBoolValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_CHECK) == true )
        {
            logger.info("Boards from CHECK message blocked");
        } else if( currentMsg.isMessageStatusOBSERVE() &&
                   Core.frostSettings.getBoolValue(SettingsClass.KNOWNBOARDS_BLOCK_FROM_OBSERVE) == true )
        {
            logger.info("Boards from OBSERVE message blocked");
        } else if( currentMsg.isMessageStatusTAMPERED() ) {
            logger.info("Boards from TAMPERED message blocked");
        } else {
            // either GOOD user or not blocked by user
            final LinkedList<Board> addBoards = new LinkedList<Board>();
            for(final Iterator i=currentMsg.getAttachmentsOfType(Attachment.BOARD).iterator(); i.hasNext(); ) {
                final BoardAttachment ba = (BoardAttachment) i.next();
                addBoards.add(ba.getBoardObj());
            }
            KnownBoardsManager.addNewKnownBoards(addBoards);
        }
    }

    public void searchAllNewMessages(final boolean runWithinThread) {
        if( runWithinThread ) {
            new Thread() {
                @Override
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
            @Override
            public void run() {
                searchNewMessagesInBoard(board);
            }
        }.start();
    }

    private void searchAllNewMessages() {
        final Enumeration<AbstractNode> e = tofTreeModel.getRoot().depthFirstEnumeration();
        while( e.hasMoreElements() ) {
            final AbstractNode node = e.nextElement();
            if( node.isBoard() ) {
                searchNewMessagesInBoard((Board)node);
            }
        }
    }

    private void searchNewMessagesInBoard(final Board board) {
        if( !board.isBoard() ) {
            return;
        }

        final int beforeMessages = board.getNewMessageCount(); // remember old val to track if new msg. arrived

        int newMessages = 0;
        newMessages = MessageStorage.inst().getNewMessageCount(board);

        // count new messages arrived while processing
        final int arrivedMessages = board.getNewMessageCount() - beforeMessages;
        if( arrivedMessages > 0 ) {
            newMessages += arrivedMessages;
        }

        board.setNewMessageCount(newMessages);

        // check for flagged and starred messages in board
        boolean hasFlagged = false;
        boolean hasStarred = false;
        hasFlagged = MessageStorage.inst().hasFlaggedMessages(board);
        hasStarred = MessageStorage.inst().hasStarredMessages(board);

        board.hasFlaggedMessages(hasFlagged);
        board.hasStarredMessages(hasStarred);

        // update the tree
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                MainFrame.getInstance().updateTofTree(board);
            }
        });
    }
}
