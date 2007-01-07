/*
  MessageDownloader.java / Frost
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
import frost.util.*;

public class MessageDownloader {

    private static Logger logger = Logger.getLogger(MessageDownloader.class.getName());

    /**
     * Process the downloaded file, decrypt, check sign.
     * @param tmpFile  downloaded file
     * @param results  the FcpResults
     * @param logInfo  info for log output
     * @return  null if unexpected Exception occurred, or results indicating state or error
     */
    protected static MessageDownloaderResult processDownloadedFile(File tmpFile, FcpResultGet results, String logInfo) {
        try {
            if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_05 ) {
                return processDownloadedFile05(tmpFile, results, logInfo);
            } else if( FcpHandler.getInitializedVersion() == FcpHandler.FREENET_07 ) {
                return processDownloadedFile07(tmpFile, results, logInfo);
            } else {
                logger.severe("Unsupported freenet version: "+FcpHandler.getInitializedVersion());
                return null;
            }
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Error processing downloaded message", t);
            MessageDownloaderResult mdResult = new MessageDownloaderResult(MessageDownloaderResult.BROKEN_MSG);
            return mdResult;
        }
    }
    
    /**
     * Tries to download the message, performs all base checkings and decryption.
     * 
     * @return  null if not found, or MessageDownloaderResult if success or error
     */
    public static MessageDownloaderResult downloadMessage(
            String downKey,
            int targetIndex,
            boolean fastDownload, 
            String logInfo) {
        
        FcpResultGet results;
        File tmpFile = FileAccess.createTempFile("dlMsg_", "-"+targetIndex+".xml.tmp");
        
        try {
            results = FcpHandler.inst().getFile(
                    FcpHandler.TYPE_MESSAGE,
                    downKey,
                    null,
                    tmpFile,
                    false,
                    fastDownload,
                    FcpHandler.MAX_KSK_SIZE_ON_07);
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate part 1."+logInfo, t);
            // download failed
            tmpFile.delete();
            return null;
        }
        
        if( results == null || results.isSuccess() == false ) {
        	tmpFile.delete();
        	if(results != null && results.getReturnCode() == 28) {
     	    	logger.severe("TOFDN: All data not found."+logInfo);
     	    	System.out.println("TOFDN: Contents of message key partially missing.");
     	    	return new MessageDownloaderResult(MessageDownloaderResult.ALLDATANOTFOUND);
            } else if(results != null && results.getReturnCode() == 21) {
                    logger.severe("TOFDN: Message file too big."+logInfo);
                    System.out.println("TOFDN: Message file too big.");
                    return new MessageDownloaderResult(MessageDownloaderResult.MSG_TOO_BIG);
        	} else {
        		return null;
        	}
        }
        
        return processDownloadedFile(tmpFile, results, logInfo);
    }
    
    /**
     * Process the downloaded file, decrypt, check sign.
     * @param tmpFile  downloaded file
     * @param results  the FcpResults
     * @param logInfo  info for log output
     * @return  null if unexpected Exception occurred, or results indicating state or error
     */
    protected static MessageDownloaderResult processDownloadedFile05(File tmpFile, FcpResultGet results, String logInfo) {
        
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
                tmpFile.delete();
                return new MessageDownloaderResult(MessageDownloaderResult.BROKEN_MSG);
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
                    tmpFile.delete();
                    return new MessageDownloaderResult(MessageDownloaderResult.DUPLICATE_MSG);
                }
            }
    
            // if no metadata, message wasn't signed
            if (metadata == null) {
                byte[] unzippedXml = FileAccess.readZipFileBinary(tmpFile);
                if( unzippedXml == null ) {
                    logger.log(Level.SEVERE, "TOFDN: Unzip of unsigned xml failed."+logInfo);
                    tmpFile.delete();
                    return new MessageDownloaderResult(MessageDownloaderResult.BROKEN_MSG);
                }
                FileAccess.writeFile(unzippedXml, tmpFile);
                try {
                    MessageXmlFile currentMsg = new MessageXmlFile(tmpFile);
                    currentMsg.setSignatureStatusOLD();
                    return new MessageDownloaderResult(currentMsg);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "TOFDN: Unsigned message is invalid."+logInfo, ex);
                    // file could not be read, mark it invalid not to confuse gui
                    tmpFile.delete();
                    return new MessageDownloaderResult(MessageDownloaderResult.BROKEN_MSG);
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
                tmpFile.delete();
                return new MessageDownloaderResult(MessageDownloaderResult.BROKEN_METADATA);
            }
    
            if( _metaData.getType() != MetaData.SIGN && _metaData.getType() != MetaData.ENCRYPT ) {
                logger.severe("TOFDN: Unknown type of metadata."+logInfo);
                // don't try this file again
                tmpFile.delete();
                return new MessageDownloaderResult(MessageDownloaderResult.BROKEN_METADATA);
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
                if( !Core.getIdentities().isNewIdentityValid(owner) ) {
                    // hash of public key does not match the unique name
                    tmpFile.delete();
                    return new MessageDownloaderResult(MessageDownloaderResult.INVALID_MSG);
                }
                owner.setCHECK();
                Core.getIdentities().addIdentity(owner);
            }
    
            // verify signature
            byte[] plaintext = FileAccess.readByteArray(tmpFile);
            boolean sigIsValid = Core.getCrypto().detachedVerify(plaintext, owner.getKey(), metaData.getSig());
    
            // now check if msg is encrypted and for me, if yes decrypt the zipped data
            if (_metaData.getType() == MetaData.ENCRYPT) {
                EncryptMetaData encMetaData = (EncryptMetaData)metaData;
    
                // 1. check if the message is for me
                if (!Core.getIdentities().isMySelf(encMetaData.getRecipient())) {
                    logger.fine("TOFDN: Encrypted message was not for me.");
                    tmpFile.delete();
                    return new MessageDownloaderResult(MessageDownloaderResult.MSG_NOT_FOR_ME);
                }
    
                // 2. if yes, decrypt the content
                LocalIdentity receiverId = Core.getIdentities().getLocalIdentity(encMetaData.getRecipient());
                byte[] cipherText = FileAccess.readByteArray(tmpFile);
                byte[] zipData = Core.getCrypto().decrypt(cipherText,receiverId.getPrivKey());
    
                if( zipData == null ) {
                    logger.log(Level.SEVERE, "TOFDN: Encrypted message from "+encMetaData.getPerson().getUniqueName()+
                                             " could not be decrypted!"+logInfo);
                    tmpFile.delete();
                    return new MessageDownloaderResult(MessageDownloaderResult.DECRYPT_FAILED);
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
                tmpFile.delete();
                return new MessageDownloaderResult(MessageDownloaderResult.BROKEN_MSG);
            }
            FileAccess.writeFile(unzippedXml, tmpFile);
            
            MessageXmlFile currentMsg = null;
    
            // create object
            try {
                currentMsg = new MessageXmlFile(tmpFile);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "TOFDN: Exception when creating message object"+logInfo, ex);
                // file could not be read, mark it invalid not to confuse gui
                tmpFile.delete();
                return new MessageDownloaderResult(MessageDownloaderResult.BROKEN_MSG);
            }
    
            //then check if the signature was ok
            if (!sigIsValid) {
                logger.warning("TOFDN: message failed verification, status set to TAMPERED."+logInfo);
                currentMsg.setSignatureStatusTAMPERED();
                return new MessageDownloaderResult(currentMsg);
            }

            // FIXME: do it the same way like when owners hash is invalid? see above
            //make sure the pubkey and from fields in the xml file are the same as those in the metadata
            String metaDataHash = Mixed.makeFilename(Core.getCrypto().digest(metaData.getPerson().getKey()));
            String messageHash = Mixed.makeFilename(
                        currentMsg.getFromName().substring(
                        currentMsg.getFromName().indexOf("@") + 1,
                        currentMsg.getFromName().length()));
    
            if (!metaDataHash.equals(messageHash)) {
                logger.warning("TOFDN: Hash in metadata doesn't match hash in message!\n" +
                               "metadata : "+metaDataHash+" , message: " + messageHash+
                               ". Message failed verification, status set to TAMPERED."+logInfo);
                currentMsg.setSignatureStatusTAMPERED();
                return new MessageDownloaderResult(currentMsg);
            }
            
            // update lastSeen for this Identity
            try {
                long lastSeenMillis = currentMsg.getDateAndTime().getMillis();
                owner.updateLastSeenTimestamp(lastSeenMillis);
            } catch(Throwable t) {
                // ignore
            }
    
            currentMsg.setSignatureStatusVERIFIED();
            return new MessageDownloaderResult(currentMsg);
    
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "TOFDN: Exception thrown in downloadDate part 2."+logInfo, t);
            // index is already increased for next try
        }
        tmpFile.delete();
        return null;
    }

    /**
     * Process the downloaded file, decrypt, check sign.
     * @param tmpFile  downloaded file
     * @param results  the FcpResults
     * @param logInfo  info for log output
     * @return  null if unexpected Exception occurred, or results indicating state or error
     */
    protected static MessageDownloaderResult processDownloadedFile07(File tmpFile, FcpResultGet results, String logInfo) {
        
        try { // we don't want to die for any reason

            // a file was downloaded
            
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
                    tmpFile.delete();
                    return new MessageDownloaderResult(MessageDownloaderResult.DUPLICATE_MSG);
                }
            }
            
            MessageXmlFile currentMsg = null;
            
            try {
                currentMsg = new MessageXmlFile(tmpFile);
            } catch (MessageCreationException ex) {
                String errorMessage;
                if( ex.getMessageNo() == MessageCreationException.MSG_NOT_FOR_ME ) {
                    logger.warning("Info: Encrypted message is not for me. "+logInfo);
                    errorMessage = MessageDownloaderResult.MSG_NOT_FOR_ME;
                } else if( ex.getMessageNo() == MessageCreationException.DECRYPT_FAILED ) {
                    logger.log(Level.WARNING, "TOFDN: Exception catched."+logInfo, ex);
                    errorMessage = MessageDownloaderResult.DECRYPT_FAILED;
                } else {
                    logger.log(Level.WARNING, "TOFDN: Exception catched."+logInfo, ex);
                    errorMessage = MessageDownloaderResult.BROKEN_MSG;
                }
                tmpFile.delete();
                return new MessageDownloaderResult(errorMessage);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "TOFDN: Exception catched."+logInfo, ex);
                // file could not be read, mark it invalid not to confuse gui
                tmpFile.delete();
                return new MessageDownloaderResult(MessageDownloaderResult.BROKEN_MSG);
            }
            
            if( currentMsg.getSignature() == null || currentMsg.getSignature().length() == 0 ) {
                // unsigned msg
                // check and maybe add msg to gui, set to unsigned
                currentMsg.setSignatureStatusOLD();
                return new MessageDownloaderResult(currentMsg);
            }
            
            // check if we have the owner (sender) already on the lists
            String _owner = currentMsg.getFromName();
            Identity owner = Core.getIdentities().getIdentity(_owner);
            // if not on any list, use the parsed id and add to our identities list
            if (owner == null) {
                owner = new Identity(currentMsg.getFromName(), currentMsg.getPublicKey());
                if( !Core.getIdentities().isNewIdentityValid(owner) ) {
                    // hash of public key does not match the unique name
                    tmpFile.delete();
                    return new MessageDownloaderResult(MessageDownloaderResult.INVALID_MSG);
                }
                owner.setCHECK();
                Core.getIdentities().addIdentity(owner);
            }

            // now verify signed content
            boolean sigIsValid = currentMsg.verifyMessageSignature(owner.getKey());

            // then check if the signature was ok
            if (!sigIsValid) {
                logger.warning("TOFDN: message failed verification, status set to TAMPERED."+logInfo);
                currentMsg.setSignatureStatusTAMPERED();
                return new MessageDownloaderResult(currentMsg);
            }
            
            // update lastSeen for this Identity
            try {
                long lastSeenMillis = currentMsg.getDateAndTime().getMillis();
                owner.updateLastSeenTimestamp(lastSeenMillis);
            } catch(Throwable t) {
                // ignore
            }

            currentMsg.setSignatureStatusVERIFIED();
            return new MessageDownloaderResult(currentMsg);

        } catch (Throwable t) {
            logger.log(Level.SEVERE, "TOFDN: Exception catched."+logInfo, t);
            // index is already increased for next try
        }
        tmpFile.delete();
        return null;
    }
}
