/*
  MessageObjectFile.java / Frost
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

package frost.messages;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.bouncycastle.util.encoders.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;
import frost.identities.*;

public class MessageXmlFile extends AbstractMessageObject implements XMLizable {

    private static Logger logger = Logger.getLogger(MessageXmlFile.class.getName());
    
//    private static final char[] evilChars = {'/', '\\', '*', '=', '|', '&', '#', '\"', '<', '>'}; // will be converted to _

    private String boardName = "";
    private String dateStr = "";
    private String timeStr = "";

    protected File file;
    
    /**
     * Constructor.
     * Used to construct an instance for a new message.
     */
    public MessageXmlFile(FrostMessageObject mo) {
        
        // date and time is set by the uploadthread
        
        setMessageId(mo.getMessageId()); // messageid
        setInReplyTo(mo.getInReplyTo()); // inreplyto
        setBoardName(mo.getBoard().getName()); // board
        setFromName(mo.getFromName()); // from
        setSubject(mo.getSubject()); // subject
        setRecipientName(mo.getRecipientName()); // recipient
        setContent(mo.getContent()); // msgcontent
        
        setAttachmentList(mo.getAttachmentList());
    }

    /**
     * Constructor.
     * Used to construct an instance for an existing messagefile.
     * @param file
     * @throws MessageCreationException
     */
    public MessageXmlFile(File file) throws MessageCreationException {

        if (file == null) {
        	throw new MessageCreationException("Invalid input file for MessageObject. File is null.");
        } else if (!file.exists()) {
            throw new MessageCreationException(
        					"Invalid input file '" + file.getName() + "' for MessageObject. File doesn't exist.");
        } else if (file.length() < 20) { // prolog+needed tags are always > 20, but we need to filter
                                         // out the messages containing "Empty", "Invalid", "Double", "Broken"
                                         // (encrypted for someone else OR completely invalid)
            throw new MessageCreationException(
                            "Info only: Empty input file '" + file.getName() + "' for MessageObject (size < 20).", true);
        }
        this.file = file;
        try {
            loadFile();
            // ensure basic contents and formats
            analyzeFile();
        } catch (MessageCreationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new MessageCreationException(
                            "Invalid input file '" + file.getName() + "' for MessageObject (load/analyze failed).", exception);
        }
    }

    /**
     * Set all values after load
     */
    public void analyzeFile() throws Exception {
        // ensure all needed fields are properly filled
        if( getFromName() == null || getDateStr() == null || getTimeStr() == null || getContent() == null ||
            getBoardName() == null || !isValid() )
        {
            logger.severe("Analyze file failed.");
            throw new Exception("Message have invalid or missing fields.");
        }
    }

    public File getFile() {
        return file;
    }

    /**
     * Signs message and sets signature.
     * 
     * @param privateKey
     */
    public void signMessage(String privateKey) {
        String sig = Core.getCrypto().detachedSign(getSignableContent(), privateKey);
        setSignature(sig);
    }
    
    /**
     * Returns true if the message signature is valid.
     * 
     * @param pubKey
     * @return
     */
    public boolean verifyMessageSignature(String pubKey) {
        boolean sigIsValid = Core.getCrypto().detachedVerify(getSignableContent(), pubKey, getSignature());
        return sigIsValid;
    }
    
    /**
     * Returns a String containing all content that is used to sign/verify the message. 
     * @return
     */
    private String getSignableContent() {
        
        StringBuffer allContent = new StringBuffer();
        allContent.append(getDateStr());
        allContent.append(getTimeStr());
        allContent.append(getSubject());
        allContent.append(getContent());
        // attachments
        for(Iterator it = getAttachmentList().iterator(); it.hasNext(); ) {
            Attachment a = (Attachment)it.next();
            if( a.getType() == Attachment.BOARD ) {
                BoardAttachment ba = (BoardAttachment)a;
                allContent.append( ba.getBoardObj().getBoardFilename() );
                if( ba.getBoardObj().getPublicKey() != null ) {
                    allContent.append( ba.getBoardObj().getPublicKey() );
                }
                if( ba.getBoardObj().getPrivateKey() != null ) {
                    allContent.append( ba.getBoardObj().getPrivateKey() );
                }
            } else if( a.getType() == Attachment.FILE ) {
                FileAttachment fa = (FileAttachment)a;
                allContent.append( fa.getFilename() );
                allContent.append( fa.getKey() );
            }
        }
        return allContent.toString();
    }

    /**
     * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
     */
    public Element getXMLElement(Document d) {
        Element el = d.createElement("FrostMessage");

        CDATASection cdata;
        Element current;

        if( getMessageId() != null ) {
            current = d.createElement("MessageId");
            cdata = d.createCDATASection(getMessageId());
            current.appendChild(cdata);
            el.appendChild(current);
        }

        if( getInReplyTo() != null ) {
            current = d.createElement("InReplyTo");
            cdata = d.createCDATASection(getInReplyTo());
            current.appendChild(cdata);
            el.appendChild(current);
        }

        //from
        current = d.createElement("From");
        cdata = d.createCDATASection(getFromName());
        current.appendChild(cdata);
        el.appendChild(current);

        //subject
        current = d.createElement("Subject");
        cdata = d.createCDATASection(getSubject());
        current.appendChild(cdata);
        el.appendChild(current);

        //date
        current = d.createElement("Date");
        cdata = d.createCDATASection(getDateStr());
        current.appendChild(cdata);
        el.appendChild(current);

        //time
        current = d.createElement("Time");
        cdata = d.createCDATASection(getTimeStr());
        current.appendChild(cdata);
        el.appendChild(current);

        //body
        current = d.createElement("Body");
        cdata = d.createCDATASection(getContent());
        current.appendChild(cdata);
        el.appendChild(current);

        //board
        current = d.createElement("Board");
        cdata = d.createCDATASection(getBoardName());
        current.appendChild(cdata);
        el.appendChild(current);

        //public Key
        if (getPublicKey() != null && getPublicKey().length() > 0) {
            current = d.createElement("pubKey");
            cdata = d.createCDATASection(getPublicKey());
            current.appendChild(cdata);
            el.appendChild(current);
        }

        // recipient
        if (getRecipientName() != null && getRecipientName().length() > 0) {
            current = d.createElement("recipient");
            cdata = d.createCDATASection(getRecipientName());
            current.appendChild(cdata);
            el.appendChild(current);
        }

        // signature
        if (getSignature() != null && getSignature().length() > 0) {
            current = d.createElement("Signature");
            cdata = d.createCDATASection(getSignature());
            current.appendChild(cdata);
            el.appendChild(current);
        }

//        // signature status
//        if( getSignatureStatus() != SIGNATURESTATUS_UNSET ) {
//            current = d.createElement("signatureStatus");
//            if( getSignatureStatus() == SIGNATURESTATUS_TAMPERED ) {
//                cdata = d.createCDATASection(SIGNATURESTATUS_TAMPERED_STR);
//            } else if( signatureStatus == SIGNATURESTATUS_OLD ) {
//                cdata = d.createCDATASection(SIGNATURESTATUS_OLD_STR);
//            } else if( signatureStatus == SIGNATURESTATUS_VERIFIED ) {
//                cdata = d.createCDATASection(SIGNATURESTATUS_VERIFIED_STR);
//            }
//            current.appendChild(cdata);
//            el.appendChild(current);
//        }

        //attachments
        if (getAttachmentList().size() > 0) {
            el.appendChild(getAttachmentList().getXMLElement(d));
        }

        return el;
    }

    /**
     * @return true if the message is basically valid
     */
    public boolean isValid() {

        if (getDateStr() == null || getDateStr().length() == 0 || getDateStr().length() > 22 ) {
            return false;
        }
        if (getTimeStr() == null || getTimeStr().length() == 0) {
            return false;
        }
        if (getBoardName() == null || getBoardName().length() == 0 || getBoardName().length() > 256 ) {
            return false;
        }
        if (getFromName() == null || getFromName().length() == 0 || getFromName().length() > 256 ) {
            return false;
        }

        if (getSubject() == null) {
            setSubject(""); // we accept empty subjects
        } else if ( getSubject().length() > 256 ) {
            return false;
        }

        if (getContent() == null) {
            setContent("");
        } else if (getContent().length() > (64 * 1024)) { // 64k or whatever fits in zipped data
            return false;
        }

        return true;
    }

    /**
     * Parses the XML file and passes the FrostMessage element to XMLize load method.
     */
    protected void loadFile() throws Exception {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(this.file, false);
        } catch(Exception ex) {  // xml format error
            File badMessage = new File("badmessage.xml");
            if (file.renameTo(badMessage)) {
                logger.log(Level.SEVERE, "Error - send the file badmessage.xml to a dev for analysis, more details below:", ex);
            }
        }

        if( doc == null ) {
            throw new Exception("Error - MessageObject.loadFile: couldn't parse XML Document - " +
                                "File name: '" + file.getName() + "'");
        }

        Element rootNode = doc.getDocumentElement();

        // transparently decrypt and continue load on success
        if( rootNode.getTagName().equals("EncryptedFrostMessage") ) {
            // get recipient (must be I to continue)
            setRecipientName(XMLTools.getChildElementsCDATAValue(rootNode, "recipient"));
            if( getRecipientName() == null ) {
                // no recipient
                throw new Exception("Error - encrypted message contains no 'recipient' section.");
            }
            FrostIdentities identities = Core.getIdentities();
            if( !identities.isMySelf(getRecipientName()) ) {
                // not for me
                throw new MessageCreationException("Info: Encrypted message is not for me.",
                        MessageCreationException.MSG_NOT_FOR_ME);
            }
            String base64enc = XMLTools.getChildElementsCDATAValue(rootNode, "content");
            if( base64enc == null ) {
                // no content
                throw new Exception("Error - encrypted message contains no 'content' section.");
            }
            byte[] base64bytes = base64enc.getBytes("ISO-8859-1");
            byte[] encBytes = Base64.decode(base64bytes);

            // decrypt content
            LocalIdentity receiverId = identities.getLocalIdentity(getRecipientName());
            byte[] decContent = Core.getCrypto().decrypt(encBytes, receiverId.getPrivKey());
            if( decContent == null ) {
                logger.log(Level.SEVERE, "TOFDN: Encrypted message could not be decrypted!");
                throw new MessageCreationException("Error: Encrypted message could not be decrypted.",
                        MessageCreationException.DECRYPT_FAILED);
            }
            // decContent is an complete XML file, save it to file and load it
            FileAccess.writeFile(decContent, this.file);
            // try to load again
            try {
                doc = XMLTools.parseXmlFile(this.file, false);
            } catch(Exception ex) {  // xml format error
                File badMessage = new File("badmessage.xml");
                if (file.renameTo(badMessage)) {
                    logger.log(Level.SEVERE, "Error - send the file badmessage.xml to a dev for analysis, more details below:", ex);
                }
            }

            if( doc == null ) {
                throw new Exception("Error - MessageObject.loadFile: couldn't parse XML Document - " +
                                    "File name: '" + file.getName() + "'");
            }
            rootNode = doc.getDocumentElement();
        }
        if( rootNode.getTagName().equals("FrostMessage") == false ) {
            File badMessage = new File("badmessage.xml");
            if (file.renameTo(badMessage)) {
                logger.severe("Error - send the file badmessage.xml to a dev for analysis.");
            }
            throw new Exception("Error - invalid message: does not contain the root tag 'FrostMessage'");
        }
        // load the message load itself
        loadXMLElement(rootNode);
    }
    
    /**
     * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
     */
    public void loadXMLElement(Element e) throws SAXException {
        setMessageId(XMLTools.getChildElementsCDATAValue(e, "MessageId"));
        setInReplyTo(XMLTools.getChildElementsCDATAValue(e, "InReplyTo"));
        setFromName(XMLTools.getChildElementsCDATAValue(e, "From"));
        setDateStr(XMLTools.getChildElementsCDATAValue(e, "Date"));
        setSubject(XMLTools.getChildElementsCDATAValue(e, "Subject"));
        setTimeStr(XMLTools.getChildElementsCDATAValue(e, "Time"));
        setPublicKey(XMLTools.getChildElementsCDATAValue(e, "pubKey"));
        setRecipientName(XMLTools.getChildElementsCDATAValue(e, "recipient"));
        setBoardName(XMLTools.getChildElementsCDATAValue(e, "Board"));
        setContent(XMLTools.getChildElementsCDATAValue(e, "Body"));
        setSignature(XMLTools.getChildElementsCDATAValue(e, "Signature"));

        // this parameter is contained in local XML messages only
        String sigstat = XMLTools.getChildElementsCDATAValue(e, "signatureStatus");
        if( sigstat != null && (sigstat=sigstat.trim()).length() > 0 ) {
            setSignatureStatusFromString(sigstat);
        }

        List l = XMLTools.getChildElementsByTagName(e, "AttachmentList");
        if (l.size() > 0) {
            Element attachmentsElement = (Element) l.get(0);
            getAttachmentList().loadXMLElement(attachmentsElement);
        }
    }

    public void setFile(File f) {
        file = f;
    }
    
    /**
     * Save the message.
     */
    public boolean save() {
        if( file == null ) {
            logger.log(Level.SEVERE, "Error: internal File pointer is not set");
            return false;
        }
        return saveToFile(file);
    }

    /**
     * Save the message to the specified file.
     * Does not change the internal File pointer.
     */
    public boolean saveToFile(File f) {
        File tmpFile = new File(f.getPath() + "sav.tmp");
        boolean success = false;
        try {
            Document doc = XMLTools.createDomDocument();
            doc.appendChild(getXMLElement(doc));
            success = XMLTools.writeXmlFile(doc, tmpFile.getPath());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while saving message.", e);
        }
        if (success && tmpFile.length() > 0) {
            if( f.isFile() && f.delete() == false ) {
                logger.log(Level.SEVERE, "Error while saving message, delete failed.");
            }
            if( tmpFile.renameTo(f) == false ) {
                logger.log(Level.SEVERE, "Error while saving message, renameTo failed.");
                return false;
            }
        } else {
            tmpFile.delete();
        }
        return success;
    }
    
    /**
     * Encrypt the complete XML message file with public key of recipient and
     * create an EncryptedFrostMessage XML file that contains the encryted content
     * in base64 format. Saves the resulting XML file into targetFile.
     * 
     * @param msgFile  the message xml file that is the input for encryption
     * @param recipientPublicKey  recipients public key
     * @param targetFile  the target xml file for the encrypted message
     * @return
     */
    public static boolean encryptForRecipientAndSaveCopy(File msgFile, Identity recipient, File targetFile) {
        byte[] xmlContent = FileAccess.readByteArray(msgFile);
        byte[] encContent = Core.getCrypto().encrypt(xmlContent, recipient.getKey());
        String base64enc;
        try {
            base64enc = new String(Base64.encode(encContent), "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, "ISO-8859-1 encoding is not supported.", ex);
            return false;
        }
        
        Document doc = XMLTools.createDomDocument();
        Element el = doc.createElement("EncryptedFrostMessage");

        CDATASection cdata;
        Element current;

        // recipient
        current = doc.createElement("recipient");
        cdata = doc.createCDATASection(recipient.getUniqueName());
        current.appendChild(cdata);
        el.appendChild(current);

        // base64 content
        current = doc.createElement("content");
        cdata = doc.createCDATASection(base64enc);
        current.appendChild(cdata);
        el.appendChild(current);

        doc.appendChild(el);
        return XMLTools.writeXmlFile(doc, targetFile.getPath());
    }
    
    /**
     * Compares the given message in otherMsgFile with this message.
     * Compares content (body), subject, from and attachments.
     */
    public boolean compareTo(File otherMsgFile) {
        try {
            MessageXmlFile otherMessage = new MessageXmlFile(otherMsgFile);
            return compareTo(otherMessage);
        } catch(Throwable t) {
            logger.log(Level.WARNING, "Handled Exception in compareTo(File otherMsgFile)", t);
            return false; // We assume that the other message is different (it may be corrupted)
        }
    }
    
    /**
     * Compares the given otherMsg with this message.
     * Compares content (body), subject, from and attachments.
     */
    public boolean compareTo(MessageXmlFile otherMsg) {
        try {
            // We compare the messages by content (body), subject, from and attachments
            if (!getContent().equals(otherMsg.getContent())) {
                return false;
            }
            if (!getSubject().equals(otherMsg.getSubject())) {
                return false;
            }
            if (!getFromName().equals(otherMsg.getFromName())) {
                return false;
            }
            AttachmentList attachments1 = otherMsg.getAttachmentList();
            AttachmentList attachments2 = getAttachmentList();
            if (attachments1.size() != attachments2.size()) {
                return false;
            }
            Iterator iterator1 = attachments1.iterator();
            Iterator iterator2 = attachments2.iterator();
            while (iterator1.hasNext()) {
                Attachment attachment1 = (Attachment) iterator1.next();
                Attachment attachment2 = (Attachment) iterator2.next();
                if (attachment1.compareTo(attachment2) != 0) {
                    return false;
                }
            }
            return true;
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Handled Exception in compareTo(MessageObject otherMsg)", t);
            return false; // We assume that the local message is different (it may be corrupted)
        }
    }
    
    public String getBoardName() {
        return boardName;
    }
    public String getDateStr() {
        return dateStr;
    }
    public String getTimeStr() {
        return timeStr;
    }

    public void setBoardName(String board) {
        this.boardName = board;
    }
    public void setDateStr(String date) {
        this.dateStr = date;
    }
    public void setTimeStr(String time) {
        this.timeStr = time;
    }
    
    public boolean isMessageNew() {
        File newMessage = new File(getFile().getPath() + ".lck");
        if( newMessage.isFile() ) {
            return true;
        } else {
            return false;
        }
    }
}
