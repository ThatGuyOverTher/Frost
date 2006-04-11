/*
  MessageDownloader05.java / Frost
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
package frost.transferlayer;

import java.io.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.*;
import frost.crypt.*;
import frost.fcp.*;
import frost.identities.*;
import frost.messages.*;

/**
 * Provides method to download a message. Does all conversions to check and build
 * a valid MessageObject from received files.
 * 
 * ATTN: This class is instanciated only once, so it must behave like a static class.
 *       Use no instance variables!
 */
public class MessageDownloader05 extends MessageDownloader {

    private static Logger logger = Logger.getLogger(MessageDownloader05.class.getName());

    /**
     * Tries to download the message, performs all base checkings and decryption.
     * 
     * @return  null if not found, or MessageDownloaderResult if success or error
     */
    public MessageDownloaderResult downloadMessage(
            String downKey,
            int targetIndex,
            boolean fastDownload, 
            String logInfo) {
        
        MessageDownloaderResult mdResult = null;
        FcpResults results;
        File tmpFile = null;
        
        try {
            tmpFile = File.createTempFile("dlMsg_", "-"+targetIndex+".xml.tmp", new File(Core.frostSettings.getValue("temp.dir")));
        } catch( Throwable ex ) {
            logger.log(Level.SEVERE, "Exception thrown in downloadMessage(...)", ex);
            return null;
        }

        try {
            results = FcpHandler.inst().getFile(
                    downKey,
                    null,
                    tmpFile,
                    Core.frostSettings.getIntValue("tofDownloadHtl"),
                    false,
                    fastDownload);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate part 1."+logInfo, t);
            // download failed
            tmpFile.delete();
            return null;
        }
        
        if( results == null ) {
            tmpFile.delete();
            return null;
        }
        
        mdResult = new MessageDownloaderResult();

        try {
            // we downloaded something
            logger.info("TOFDN: A message was downloaded."+logInfo);
    
            // either null (unsigned) or signed and maybe encrypted message
            byte[] metadata = results.getRawMetadata();
    
            if( tmpFile.length() == 0 ) {
                // Frosts message files do always contain data, so the received content is wrong
                if( metadata != null && metadata.length > 0 ) {
                    logger.severe("TOFDN: Received metadata without data, maybe faked message."+logInfo);
                } else if( metadata == null || metadata.length == 0 ) {
                    // paranoia checking, should never happen if FcpResults != null
                    logger.severe("TOFDN: Received neither metadata nor data, maybe a bug or a faked message."+logInfo);
                } else {
                    // something bad happened if we ever come here :)
                    logger.severe("TOFDN: Received something, but bad things happened in code, maybe a bug or a faked message."+logInfo);
                }
                mdResult.errorMsg = MessageDownloaderResult.BROKEN_MSG;
                tmpFile.delete();
                return mdResult;
            }
    
            // compute the sha1 checksum of the original msg file
            // this digest is ONLY used to check for incoming exact duplicate files, because
            // the locally stored message xml file could be changed later by Frost
            String messageId = Core.getCrypto().digest(tmpFile);
            // Does a duplicate message exist?
            boolean isDuplicateMsg = Core.getMessageHashes().contains(messageId);
            // add to the list of message hashes to track this received message
            Core.getMessageHashes().add(messageId);
    
            if( isDuplicateMsg ) {
                logger.info(Thread.currentThread().getName()+": TOFDN: *** Duplicate Message."+logInfo+" ***");
                if( Core.frostSettings.getBoolValue(SettingsClass.RECEIVE_DUPLICATE_MESSAGES) == false ) {
                    // user don't want to see the duplicate messages
                    mdResult.errorMsg = MessageDownloaderResult.DUPLICATE_MSG;
                    tmpFile.delete();
                    return mdResult;
                }
            }
    
            // if no metadata, message wasn't signed
            if (metadata == null) {
    
                byte[] unzippedXml = FileAccess.readZipFileBinary(tmpFile);
                if( unzippedXml == null ) {
                    logger.log(Level.SEVERE, "TOFDN: Unzip of unsigned xml failed."+logInfo);
                    mdResult.errorMsg = MessageDownloaderResult.BROKEN_MSG;
                    tmpFile.delete();
                    return mdResult;
                }
                FileAccess.writeFile(unzippedXml, tmpFile);
                try {
                    VerifyableMessageObject currentMsg = new VerifyableMessageObject(tmpFile);
                    mdResult.message = currentMsg;
                    mdResult.messageState = MessageObject.SIGNATURESTATUS_OLD;
                    return mdResult;
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "TOFDN: Unsigned message is invalid."+logInfo, ex);
                    // file could not be read, mark it invalid not to confuse gui
                    mdResult.errorMsg = MessageDownloaderResult.BROKEN_MSG;
                    tmpFile.delete();
                    return mdResult;
                }
            }
    
            // verify the zipped message
            MetaData _metaData = null;
            try {
                Document doc = XMLTools.parseXmlContent(metadata, false);
                if( doc != null ) { // was metadata xml ok?
                    _metaData = MetaData.getInstance( doc.getDocumentElement() );
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "TOFDN: Invalid metadata of signed message"+logInfo, t);
                _metaData = null;
            }
            if( _metaData == null ) {
                // metadata failed, do something
                logger.log(Level.SEVERE, "TOFDN: Metadata couldn't be read. " +
                                "Offending file saved as badmetadata.xml - send to a dev for analysis."+logInfo);
                File badmetadata = new File("badmetadata.xml");
                FileAccess.writeFile(metadata, badmetadata);
                // don't try this file again
                mdResult.errorMsg = MessageDownloaderResult.BROKEN_METADATA;
                tmpFile.delete();
                return mdResult;
            }
    
            if( _metaData.getType() != MetaData.SIGN && _metaData.getType() != MetaData.ENCRYPT ) {
                logger.severe("TOFDN: Unknown type of metadata."+logInfo);
                // don't try this file again
                mdResult.errorMsg = MessageDownloaderResult.BROKEN_METADATA;
                tmpFile.delete();
                return mdResult;
            }
    
            // now the msg could be signed OR signed and encrypted
            // first check sign, later decrypt if msg was for me
    
            SignMetaData metaData = (SignMetaData)_metaData;
    
            //check if we have the owner already on the lists
            String _owner = metaData.getPerson().getUniqueName();
            Identity owner = Core.getIdentities().getIdentity(_owner);
            // if not on any list, use the parsed id and add to our identities list
            if (owner == null) {
                owner = metaData.getPerson();
                owner.setState(FrostIdentities.NEUTRAL);
                Core.getIdentities().addIdentity(owner);
            }
    
            // verify signature
            byte[] plaintext = FileAccess.readByteArray(tmpFile);
            boolean sigIsValid = Core.getCrypto().detachedVerify(plaintext, owner.getKey(), metaData.getSig());
    
            // only for correct owner (no faking allowed here)
            if( sigIsValid ) {
                // update lastSeen for this Identity
                owner.updateLastSeenTimestamp();
            }
    
            // now check if msg is encrypted and for me, if yes decrypt the zipped data
            if (_metaData.getType() == MetaData.ENCRYPT) {
                EncryptMetaData encMetaData = (EncryptMetaData)metaData;
    
                // 1. check if the message is for me
                if (!encMetaData.getRecipient().equals(Core.getIdentities().getMyId().getUniqueName())) {
                    logger.fine("TOFDN: Encrypted message was not for me.");
                    mdResult.errorMsg = MessageDownloaderResult.MSG_NOT_FOR_ME;
                    tmpFile.delete();
                    return mdResult;
                }
    
                // 2. if yes, decrypt the content
                byte[] cipherText = FileAccess.readByteArray(tmpFile);
                byte[] zipData = Core.getCrypto().decrypt(cipherText,Core.getIdentities().getMyId().getPrivKey());
    
                if( zipData == null ) {
                    logger.log(Level.SEVERE, "TOFDN: Encrypted message from "+encMetaData.getPerson().getUniqueName()+
                                             " could not be decrypted!"+logInfo);
                    mdResult.errorMsg = MessageDownloaderResult.DECRYPT_FAILED;
                    tmpFile.delete();
                    return mdResult;
                }
    
                tmpFile.delete();
                FileAccess.writeFile(zipData, tmpFile);
    
                logger.fine("TOFDN: Decrypted an encrypted message for me, sender was "+encMetaData.getPerson().getUniqueName()+"."+logInfo);
    
                // now continue as for signed files
    
            } //endif encrypted message
    
            // unzip
            byte[] unzippedXml = FileAccess.readZipFileBinary(tmpFile);
            if( unzippedXml == null ) {
                logger.log(Level.SEVERE, "TOFDN: Unzip of signed xml failed."+logInfo);
                mdResult.errorMsg = MessageDownloaderResult.BROKEN_MSG;
                tmpFile.delete();
                return mdResult;
            }
            FileAccess.writeFile(unzippedXml, tmpFile);
            
            VerifyableMessageObject currentMsg = null;
    
            // create object
            try {
                currentMsg = new VerifyableMessageObject(tmpFile);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "TOFDN: Exception when creating message object"+logInfo, ex);
                // file could not be read, mark it invalid not to confuse gui
                mdResult.errorMsg = MessageDownloaderResult.BROKEN_MSG;
                tmpFile.delete();
                return mdResult;
            }
    
            //then check if the signature was ok
            if (!sigIsValid) {
                logger.warning("TOFDN: message failed verification, status set to TAMPERED."+logInfo);
                mdResult.message = currentMsg;
                mdResult.messageState = MessageObject.SIGNATURESTATUS_TAMPERED;
                return mdResult;
            }
    
            //make sure the pubkey and from fields in the xml file are the same as those in the metadata
            String metaDataHash = Mixed.makeFilename(Core.getCrypto().digest(metaData.getPerson().getKey()));
            String messageHash = Mixed.makeFilename(
                        currentMsg.getFrom().substring(
                        currentMsg.getFrom().indexOf("@") + 1,
                        currentMsg.getFrom().length()));
    
            if (!metaDataHash.equals(messageHash)) {
                logger.warning("TOFDN: Hash in metadata doesn't match hash in message!\n" +
                               "metadata : "+metaDataHash+" , message: " + messageHash+
                               ". Message failed verification, status set to TAMPERED."+logInfo);
                mdResult.message = currentMsg;
                mdResult.messageState = MessageObject.SIGNATURESTATUS_TAMPERED;
                return mdResult;
            }
    
            mdResult.message = currentMsg;
            mdResult.messageState = MessageObject.SIGNATURESTATUS_VERIFIED;
            return mdResult;
    
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate part 2."+logInfo, t);
            // index is already increased for next try
        }
        tmpFile.delete();
        return null;
    }
}
