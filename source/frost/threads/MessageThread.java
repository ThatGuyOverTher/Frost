/*
  MessageDownloadThread.java / Frost
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

package frost.threads;

import java.io.*;
import java.sql.*;
import java.util.logging.*;

import org.joda.time.*;

import frost.*;
import frost.boards.*;
import frost.identities.*;
import frost.messages.*;
import frost.storage.database.applayer.*;
import frost.transferlayer.*;
import frost.util.*;

/**
 * Download and upload messages for a board.
 */
public class MessageThread extends BoardUpdateThreadObject implements BoardUpdateThread, MessageUploaderCallback {

    private Board board;
    private int maxMessageDownload;
    private boolean downloadToday;
    
    private IndexSlotsDatabaseTable indexSlots;

    private static Logger logger = Logger.getLogger(MessageThread.class.getName());

    public MessageThread(boolean downloadToday, Board boa, int maxmsgdays) {
        super(boa);
        this.downloadToday = downloadToday;
        this.board = boa;
        this.maxMessageDownload = maxmsgdays;
        
        this.indexSlots = new IndexSlotsDatabaseTable(IndexSlotsDatabaseTable.MESSAGES, board);
    }

    public int getThreadType() {
        if (downloadToday) {
            return BoardUpdateThread.MSG_DNLOAD_TODAY;
        } else {
            return BoardUpdateThread.MSG_DNLOAD_BACK;
        }
    }

    public void run() {

        notifyThreadStarted(this);

        try {
            String tofType;
            if (downloadToday) {
                tofType = "TOF Download";
            } else {
                tofType = "TOF Download Back";
            }

            // wait a max. of 5 seconds between start of threads
            Mixed.waitRandom(5000);

            logger.info("TOFDN: " + tofType + " Thread started for board " + board.getName());

            if (isInterrupted()) {
                indexSlots.close();
                notifyThreadFinished(this);
                return;
            }

            LocalDate localDate = new LocalDate(DateTimeZone.UTC);

            if (this.downloadToday) {
                // download only current date
                downloadDate(localDate);
                // after update check if there are messages for upload and upload them
                if( !Core.frostSettings.getBoolValue(SettingsClass.MESSAGE_UPLOAD_DISABLED) ) {
                    uploadMessages();
                }
            } else {
                // download up to maxMessages days to the past
                int daysBack = 0;
                while (!isInterrupted() && daysBack < maxMessageDownload) {
                    daysBack++;
                    downloadDate(localDate.minusDays(daysBack));
                }
                // after a complete backload run, remember finish time. 
                // this ensures we ever update the complete backload days. 
                if( !isInterrupted() ) {
                    board.setLastBackloadUpdateFinishedMillis(System.currentTimeMillis());
                }
            }
            logger.info("TOFDN: " + tofType + " Thread stopped for board " + board.getName());
        } catch (Throwable t) {
            logger.log(Level.SEVERE, Thread.currentThread().getName() + ": Oo. Exception in MessageDownloadThread:", t);
        }
        indexSlots.close();
        notifyThreadFinished(this);
    }
    
    protected String composeDownKey(int index, String dirdate) {
        String downKey = null;
        // switch public / secure board
        if (board.isPublicBoard() == false) {
            downKey = new StringBuffer()
                    .append(board.getPublicKey())
                    .append("/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(dirdate)
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        } else {
            downKey = new StringBuffer()
                    .append("KSK@frost/message/")
                    .append(Core.frostSettings.getValue(SettingsClass.MESSAGE_BASE))
                    .append("/")
                    .append(dirdate)
                    .append("-")
                    .append(board.getBoardFilename())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        }
        return downKey;
    }

    protected void downloadDate(LocalDate localDate) throws SQLException {

        String dirdate = DateFun.FORMAT_DATE.print(localDate);
        long date = localDate.toDateMidnight(DateTimeZone.UTC).getMillis();
        
        int index = -1;
        int failures = 0;
        int maxFailures = 2; // skip a maximum of 2 empty slots

        while (failures < maxFailures) {

            if (isInterrupted()) {
                return;
            }

            if( index < 0 ) {
                index = indexSlots.findFirstDownloadSlot(date);
            } else {
                index = indexSlots.findNextDownloadSlot(index, date);
            }
            
            String logInfo = null;

            try { // we don't want to die for any reason

                Mixed.waitRandom(3000); // don't hurt node

                String downKey = composeDownKey(index, dirdate);
                logInfo = " board="+board.getName()+", key="+downKey;
                
                // for backload use fast download, deep for today
                boolean fastDownload = !downloadToday;

                MessageDownloaderResult mdResult = MessageDownloader.downloadMessage(downKey, index, fastDownload, logInfo);
                
                if( mdResult == null ) {
                    // file not found
                    failures++;
                    continue;
                }

                failures = 0;
                
                indexSlots.setDownloadSlotUsed(index, date);

                if( mdResult.isFailure() ) {
                    // some error occured, don't try this file again
                    receivedInvalidMessage(board, localDate, index, mdResult.getErrorMessage());
                } else if( mdResult.getMessage() != null ) {
                    // message is loaded, delete underlying received file
                    mdResult.getMessage().getFile().delete();
                    // basic validation
                    if (mdResult.getMessage().isValid() && isValidFormat(mdResult.getMessage(), localDate)) {
                        receivedValidMessage(mdResult.getMessage(), board, index);
                    } else {
                        receivedInvalidMessage(board, localDate, index, MessageDownloaderResult.INVALID_MSG);
                        logger.warning("TOFDN: Message was dropped, format validation failed: "+logInfo);
                    }
                }
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate: "+logInfo, t);
                // download failed, try next file
            }
        } // end-of: while
    }
    
    private void receivedInvalidMessage(Board b, LocalDate calDL, int index, String reason) {
        TOF.getInstance().receivedInvalidMessage(b, calDL.toDateTimeAtMidnight(), index, reason);
    }
    
    private void receivedValidMessage(MessageXmlFile mo, Board b, int index) {
        TOF.getInstance().receivedValidMessage(mo, b, index);
    }
    
    //////////////////////////////////////////////////
    ///  validation after receive 
    //////////////////////////////////////////////////
    
    /**
     * First time verify.
     */
    public boolean isValidFormat(MessageXmlFile mo, LocalDate dirDate) {
        try { // if something fails here, set msg. to N/A (maybe harmful message)
            DateTime dateTime = null;
            try {
                dateTime = mo.getDateAndTime();
            } catch(Throwable ex) {
                logger.log(Level.SEVERE, "Exception in isValidFormat() - skipping Message.", ex);
                return false;
            }

            // ensure that time/date of msg is max. 1 day before/after dirDate
            DateMidnight dm = dateTime.toDateMidnight();
            if( dm.isAfter(dirDate.plusDays(1).toDateMidnight(DateTimeZone.UTC))
                    || dm.isBefore(dirDate.minusDays(1).toDateMidnight(DateTimeZone.UTC)) )
            {
                logger.log(Level.SEVERE, "Invalid date - skipping Message:"+dirDate+";"+dateTime);
                return false;
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exception in isValidFormat() - skipping Message.", t);
            return false;
        }
        return true;
    }

    /**
     * Upload pending messages for this board.
     */
    protected void uploadMessages() {

        FrostUnsentMessageObject unsendMsg = UnsentMessagesManager.getUnsentMessage(board);
        if( unsendMsg == null ) {
            // currently no msg to send for this board
            return;
        }
        
        String fromName = unsendMsg.getFromName();
        while( unsendMsg != null ) {
            
            // create a MessageXmlFile, sign, and send
            
            Identity recipient = null;
            if( unsendMsg.getRecipientName() != null && unsendMsg.getRecipientName().length() > 0) {
                recipient = Core.getIdentities().getIdentity(unsendMsg.getRecipientName());
                if( recipient == null ) {
                    logger.severe("Can't send Message '" + unsendMsg.getSubject() + "', the recipient is not longer in your identites list!");
                    UnsentMessagesManager.deleteMessage(unsendMsg);
                    continue;
                }
            }

            UnsentMessagesManager.incRunningMessageUploads();
            
            uploadMessage(unsendMsg, recipient);
            
            UnsentMessagesManager.decRunningMessageUploads();
            
            Mixed.waitRandom(6000); // wait some time
            
            // get next message to upload
            unsendMsg = UnsentMessagesManager.getUnsentMessage(board, fromName);
        }
    }

    private void uploadMessage(FrostUnsentMessageObject mo, Identity recipient) {
        
        logger.info("Preparing upload of message to board '" + board.getName() + "'");
        
        mo.setCurrentUploadThread(this);

        try {
            // prepare upload
            
            LocalIdentity senderId = null;
            if( mo.getFromName().indexOf("@") > 0 ) {
                // not anonymous
                if( mo.getFromIdentity() instanceof LocalIdentity ) {
                    senderId = (LocalIdentity) mo.getFromIdentity();
                } else {
                    // apparently the LocalIdentity used to write the msg was deleted
                    logger.severe("The LocalIdentity used to write this unsent msg was deleted: "+mo.getFromName());
                    UnsentMessagesManager.deleteMessage(mo);
                    return;
                }
            }

            MessageXmlFile message = new MessageXmlFile(mo); 

            DateTime now = new DateTime(DateTimeZone.UTC);
            message.setDateAndTime(now);
            
            File unsentMessageFile = FileAccess.createTempFile("unsendMsg", ".xml");
            message.setFile(unsentMessageFile);
            if (!message.save()) {
                logger.severe("This was a HARD error and the file to upload is lost, please report to a dev!");
                return;
            }
            unsentMessageFile.deleteOnExit();
            
            // start upload, this signs and encrypts if needed

            MessageUploaderResult result = MessageUploader.uploadMessage(
                    message, 
                    recipient,
                    senderId,
                    this,
                    indexSlots,
                    now.toDateMidnight().getMillis(),
                    MainFrame.getInstance(), 
                    board.getName());

            // file is not any longer needed
            message.getFile().delete();

            if( !result.isSuccess() ) {
                // upload failed, unsend message was handled by MessageUploader (kept or deleted, user choosed)
                mo.setCurrentUploadThread(null); // must be marked as not uploading before delete!
                if( !result.isKeepMessage() ) {
                    // user choosed to drop the message
                    UnsentMessagesManager.deleteMessage(mo);
                } else {
                    // user choosed to retry after next startup, dequeue message now and find it again on next startup
                    UnsentMessagesManager.dequeueMessage(mo);
                }
                return;
            }

            int index = result.getUploadIndex();
            
            // upload was successful, store message in sentmessages database
            FrostMessageObject sentMo = new FrostMessageObject(message, board, index);
            
            SentMessagesManager.addSentMessage(sentMo);

            // finally delete the message in unsend messages db table
            mo.setCurrentUploadThread(null); // must be marked as not uploading before delete!
            UnsentMessagesManager.deleteMessage(mo);

        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Catched exception", t);
        }
        mo.setCurrentUploadThread(null); // paranoia
        
        logger.info("Message upload finished");
    }
    
    /**
     * This method composes the downloading key for the message, given a
     * certain index number
     * @param index index number to use to compose the key
     * @return they composed key
     */
    public String composeDownloadKey(MessageXmlFile message, int index) {
        String key;
        if (board.isWriteAccessBoard()) {
            key = new StringBuffer()
                    .append(board.getPublicKey())
                    .append("/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(message.getDateStr())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        } else {
            key = new StringBuffer()
                    .append("KSK@frost/message/")
                    .append(Core.frostSettings.getValue(SettingsClass.MESSAGE_BASE))
                    .append("/")
                    .append(message.getDateStr())
                    .append("-")
                    .append(board.getBoardFilename())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        }
        return key;
    }

    /**
     * This method composes the uploading key for the message, given a
     * certain index number
     * @param index index number to use to compose the key
     * @return they composed key
     */
    public String composeUploadKey(MessageXmlFile message, int index) {
        String key;
        if (board.isWriteAccessBoard()) {
            key = new StringBuffer()
                    .append(board.getPrivateKey())
                    .append("/")
                    .append(board.getBoardFilename())
                    .append("/")
                    .append(message.getDateStr())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        } else {
            key = new StringBuffer()
                    .append("KSK@frost/message/")
                    .append(Core.frostSettings.getValue(SettingsClass.MESSAGE_BASE))
                    .append("/")
                    .append(message.getDateStr())
                    .append("-")
                    .append(board.getBoardFilename())
                    .append("-")
                    .append(index)
                    .append(".xml")
                    .toString();
        }
        return key;
    }
}
