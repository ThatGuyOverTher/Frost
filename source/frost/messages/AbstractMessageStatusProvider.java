/*
 AbstractMessageStatusProvider.java / Frost
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
package frost.messages;

import javax.swing.tree.*;

import frost.*;
import frost.identities.*;

public abstract class AbstractMessageStatusProvider extends DefaultMutableTreeNode {

    // the message states
    private static final int xGOOD     = 1;
    private static final int xCHECK    = 2;
    private static final int xBAD      = 3;
    private static final int xOBSERVE  = 4;
    private static final int xTAMPERED = 5;
    private static final int xOLD      = 6;
    
    private static String[] messageStateStrings = {
        "*err*",
        "GOOD",
        "CHECK",
        "BAD",
        "OBSERVE",
        "FAKE",
        "NONE"
    };

    private static final int SIGNATURESTATUS_UNSET    = 0; // status not set
    private static final int SIGNATURESTATUS_TAMPERED = 1; // wrong signature
    private static final int SIGNATURESTATUS_OLD      = 2; // no signature
    private static final int SIGNATURESTATUS_VERIFIED = 3; // signature was OK
    private static final int SIGNATURESTATUS_VERIFIED_V1 = 4; // signature was OK
    private static final int SIGNATURESTATUS_VERIFIED_V2 = 5; // signature was OK

    private static final String SIGNATURESTATUS_TAMPERED_STR = "TAMPERED"; // wrong signature
    private static final String SIGNATURESTATUS_OLD_STR      = "OLD";      // no signature
    private static final String SIGNATURESTATUS_VERIFIED_STR = "VERIFIED"; // signature was OK
    
    private boolean isFromIdentityInitialized = false;
    private Identity fromIdentity = null;
    
    private String fromName = "";
    private String publicKey  = "";

    private int signatureStatus = SIGNATURESTATUS_UNSET;

    public Identity getFromIdentity() {
        if( isFromIdentityInitialized == false ) {
            // set Identity for FROM, or null
            fromIdentity = Core.getIdentities().getIdentity(getFromName());
            // if identity was NOT found, add it. maybe it was deleted by the user, 
            // but we still have a msg from this identity
            if( fromIdentity == null && getPublicKey() != null && getPublicKey().length() > 0 ) {
                fromIdentity = new Identity(getFromName(), getPublicKey());
                fromIdentity.setCHECK();
                Core.getIdentities().addIdentity(fromIdentity);
            }
            isFromIdentityInitialized = true;
        }
        return fromIdentity;
    }

    public String getFromName() {
        return fromName;
    }
    public void setFromName(String from) {
        this.fromName = from;
    }
    
    /**
     * Converts the signature status string contained in local XML message file
     * into the internal constant.
     * Only used for imports!
     */
    public boolean setSignatureStatusFromString(String sigStatusStr) {
        if( sigStatusStr.equalsIgnoreCase(SIGNATURESTATUS_VERIFIED_STR) ) {
            setSignatureStatusVERIFIED_V1();
            return true;
        } else if( sigStatusStr.equalsIgnoreCase(SIGNATURESTATUS_OLD_STR) ) {
            setSignatureStatusOLD();
            return true;
        } else if( sigStatusStr.equalsIgnoreCase(SIGNATURESTATUS_TAMPERED_STR) ) {
            setSignatureStatusTAMPERED();
            return true;
        }
        return false;
    }
    
    private int getMessageStatus(Identity fromIdent) {
        if( isSignatureStatusVERIFIED() ) {
            // get state of user
            if( fromIdent == null ) {
                return xOLD;
            }
            if( fromIdent.isCHECK() ) {
                return xCHECK;
            }
            if( fromIdent.isOBSERVE() ) {
                return xOBSERVE;
            }
            if( fromIdent.isGOOD() ) {
                return xGOOD;
            }
            if( fromIdent.isBAD() ) {
                return xBAD;
            }
        } else if( isSignatureStatusOLD() ) {
            // no signature
            return xOLD;
        } else if( isSignatureStatusTAMPERED() ) {
            // invalid signature
            return xTAMPERED;
        }
        // signature status unset
        return xOLD;
    }

    private int getMessageStatus() {
        return getMessageStatus(getFromIdentity());
    }

    public String getMessageStatusString() {
    	Identity i = getFromIdentity();
    	if (i instanceof LocalIdentity) {
			return "ME";
        } else {
            return messageStateStrings[getMessageStatus(i)];
        }
    }
    
    public boolean isMessageFromME() {
        Identity i = getFromIdentity();
        if (i instanceof LocalIdentity) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isMessageStatusGOOD() {
        return (getMessageStatus() == xGOOD );
    }
    public boolean isMessageStatusOBSERVE() {
        return (getMessageStatus() == xOBSERVE );
    }
    public boolean isMessageStatusCHECK() {
        return (getMessageStatus() == xCHECK );
    }
    public boolean isMessageStatusBAD() {
        return (getMessageStatus() == xBAD );
    }
    public boolean isMessageStatusTAMPERED() {
        return (getMessageStatus() == xTAMPERED );
    }
    public boolean isMessageStatusOLD() {
        return (getMessageStatus() == xOLD );
    }
    
    public boolean isSignatureStatusVERIFIED() {
        if( getSignatureStatus() == SIGNATURESTATUS_VERIFIED
                || getSignatureStatus() == SIGNATURESTATUS_VERIFIED_V1
                || getSignatureStatus() == SIGNATURESTATUS_VERIFIED_V2
          ) 
        {
            return true;
        }
        return false;
    }
    // utility method
    public static boolean isSignatureStatusVERIFIED(int sigstat) {
        if( sigstat == SIGNATURESTATUS_VERIFIED
                || sigstat == SIGNATURESTATUS_VERIFIED_V1
                || sigstat == SIGNATURESTATUS_VERIFIED_V2
          ) 
        {
            return true;
        }
        return false;
    }
    public boolean isSignatureStatusVERIFIED_V1() {
        return (getSignatureStatus() == SIGNATURESTATUS_VERIFIED_V1);
    }
    public boolean isSignatureStatusVERIFIED_V2() {
        return (getSignatureStatus() == SIGNATURESTATUS_VERIFIED_V2);
    }
    public boolean isSignatureStatusOLD() {
        return (getSignatureStatus() == SIGNATURESTATUS_OLD);
    }
    public boolean isSignatureStatusTAMPERED() {
        return (getSignatureStatus() == SIGNATURESTATUS_TAMPERED);
    }
    
    public void setSignatureStatusVERIFIED_V1() {
        signatureStatus = SIGNATURESTATUS_VERIFIED_V1;
    }
    public void setSignatureStatusVERIFIED_V2() {
        signatureStatus = SIGNATURESTATUS_VERIFIED_V2;
    }
    public void setSignatureStatusOLD() {
        signatureStatus = SIGNATURESTATUS_OLD;
    }
    public void setSignatureStatusTAMPERED() {
        signatureStatus = SIGNATURESTATUS_TAMPERED;
    }
    
    public int getSignatureStatus() {
        return signatureStatus;
    }
    public void setSignatureStatus(int s) {
        signatureStatus = s;
    }
    
    public String getPublicKey() {
        return publicKey;
    }
    public void setPublicKey(String pk) {
        publicKey = pk;
    }
}
