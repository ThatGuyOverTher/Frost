/*
  FrostIdentities.java / Frost
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
package frost.identities;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.storage.*;
import frost.storage.database.applayer.*;
import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * A class that maintains identity stuff.
 */
public class FrostIdentities implements Savable {

    private static Logger logger = Logger.getLogger(FrostIdentities.class.getName());

    private Hashtable<String,Identity> identities = new Hashtable<String,Identity>();
    private Hashtable<String,LocalIdentity> localIdentities = new Hashtable<String,LocalIdentity>();
    
    Language language = Language.getInstance();
    
    private static boolean databaseUpdatesAllowed = true; // forbidden during first import

    /**
     * @param freenetIsOnline
     */
    public void initialize(boolean freenetIsOnline) throws StorageException {

        try {
            List localIdentitiesList = AppLayerDatabase.getIdentitiesDatabaseTable().getLocalIdentities();
            
            // check if there is at least one identity in database, otherwise create one
            if ( localIdentitiesList.size() == 0 ) {
                // first startup, no identity available
                if (freenetIsOnline == false) {
                    MiscToolkit.getInstance().showMessage(
                            language.getString("Core.loadIdentities.ConnectionNotEstablishedBody"),
                            JOptionPane.ERROR_MESSAGE,
                            language.getString("Core.loadIdentities.ConnectionNotEstablishedTitle"));
                    System.exit(2);
                }
                
                LocalIdentity mySelf = createIdentity(true);
                if(mySelf == null) {
                    logger.severe("Frost can't run without an identity.");
                    System.exit(1);
                }
                addLocalIdentity(mySelf); // add and save
            } else {
                for(Iterator i=localIdentitiesList.iterator(); i.hasNext(); ) {
                    LocalIdentity li = (LocalIdentity)i.next();
                    localIdentities.put(li.getUniqueName(), li);
                }
            }
            localIdentitiesList = null;
            
            // load all identities
            List identitiesList = AppLayerDatabase.getIdentitiesDatabaseTable().getIdentities();
            for(Iterator i=identitiesList.iterator(); i.hasNext(); ) {
                Identity id = (Identity)i.next();
                identities.put(id.getUniqueName(), id);
            }
            
            // remove all own identities from identities
            for(Iterator i=localIdentities.values().iterator(); i.hasNext(); ) {
                LocalIdentity li = (LocalIdentity) i.next();
                identities.remove(li.getUniqueName());
            }
            
        } catch(SQLException ex) {
            logger.log(Level.SEVERE, "error loading from database", ex);
            throw new StorageException("error loading from database");
        }
    }
    
    /**
     * Creates new local identity, and adds it to database.
     */
    public LocalIdentity createIdentity() {
        LocalIdentity li = createIdentity(false);
        if( li != null ) {
            if( addLocalIdentity(li) == false ) {
                // double
                return null;
            }
        }
        return li;
    }
    
    /**
     * Creates new local identity, and adds it to database.
     */
    private LocalIdentity createIdentity(boolean firstIdentity) {

        LocalIdentity newIdentity = null;
        
        // create new identitiy, get a valid username
        try {
            String nick = null;
            boolean isNickOk;
            do {
                nick = JOptionPane.showInputDialog(language.getString("Core.loadIdentities.ChooseName")); 
                
                if( nick == null ) {
                    break; // user cancelled
                }
                
                nick = nick.trim(); // not only blanks, no trailing/leading blanks 
                
                isNickOk = true;
                
                // check for invalid values
                if(nick.length() < 2 || nick.length() > 32 ) {
                    isNickOk = false; // not only 1 character or more than 32 characters
                } else if (nick.indexOf("@") > -1) {
                    isNickOk = false; // @ is forbidden
                } else if( !Character.isLetter(nick.charAt(0)) ) {
                    isNickOk = false; // must start with an alphanumeric character
                } else {
                    char oldChar = 0;
                    int charCount = 0;
                    for(int x=0; x < nick.length(); x++) {
                        if( nick.charAt(x) == oldChar ) {
                            charCount++;
                        } else {
                            oldChar = nick.charAt(x);
                            charCount = 1;
                        }
                        if( charCount > 3 ) {
                            isNickOk = false; // not more than 3 occurences of the same character in a row
                            break;
                        }
                    }
                }
                
                if( !isNickOk ) {
                    MiscToolkit.getInstance().showMessage(
                            language.getString("Core.loadIdentities.InvalidNameBody"),
                            JOptionPane.ERROR_MESSAGE,
                            language.getString("Core.loadIdentities.InvalidNameTitle"));
                }
            } while(!isNickOk);
            
            if (nick == null) {
                return null; // user cancelled
            }
            
            do { //make sure there's no '//' in the generated sha checksum
                newIdentity = new LocalIdentity(nick);
            } while (newIdentity.getUniqueName().indexOf("//") != -1);

        } catch (Exception e) {
            logger.severe("couldn't create new identitiy" + e.toString());
        }
        if( newIdentity != null && firstIdentity ) {
            Core.frostSettings.setValue(SettingsClass.LAST_USED_FROMNAME, newIdentity.getUniqueName());
        }
        
        return newIdentity;
    }

    public Identity getIdentity(String uniqueName) {
        if( uniqueName == null ) {
            return null;
        }
        Identity identity = null;
        identity = getLocalIdentity(uniqueName);
        if( identity == null ) {
            identity = (Identity)identities.get(uniqueName);
        }
        return identity;
    }

    public boolean addIdentity(Identity id) {
        String key = id.getUniqueName();
        if (identities.containsKey(key)) {
            return false;
        }
        if( isDatabaseUpdatesAllowed() ) {
            try {
                AppLayerDatabase.getIdentitiesDatabaseTable().insertIdentity(id);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "error inserting new identity", e);
                return false;
            }
        }
        identities.put(key, id);
        return true;
    }
    
    public boolean addLocalIdentity(LocalIdentity li) {
        if (localIdentities.containsKey(li.getUniqueName())) {
            return false;
        }
        if( isDatabaseUpdatesAllowed() ) {
            try {
                AppLayerDatabase.getIdentitiesDatabaseTable().insertLocalIdentity(li);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "error inserting new local identity", e);
                return false;
            }
        }
        localIdentities.put(li.getUniqueName(), li);
        return true;
    }
    
    public boolean deleteLocalIdentity(LocalIdentity li) {
        if (!localIdentities.containsKey(li.getUniqueName())) {
            return false;
        }
        
        localIdentities.remove(li.getUniqueName());
        
        try {
            return AppLayerDatabase.getIdentitiesDatabaseTable().removeLocalIdentity(li);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error removing local identity", e);
            return false;
        }
    }

    public boolean deleteIdentity(Identity li) {
        if (!identities.containsKey(li.getUniqueName())) {
            return false;
        }
        
        identities.remove(li.getUniqueName());
        
        try {
            return AppLayerDatabase.getIdentitiesDatabaseTable().removeIdentity(li);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error removing identity", e);
            return false;
        }
    }

    public boolean isMySelf(String uniqueName) {
        if( getLocalIdentity(uniqueName) != null ) {
            return true;
        }
        return false;
    }
    
    public LocalIdentity getLocalIdentity(String uniqueName) {
        LocalIdentity li = null;
        li = (LocalIdentity) localIdentities.get(uniqueName);
//        if( li == null ) {
//            li = (LocalIdentity) localIdentities.get(Mixed.makeFilename(uniqueName));
//        }
        return li;
    }

    public List<Identity> getAllGOODIdentities() {
        LinkedList<Identity> list = new LinkedList<Identity>();
        for( Iterator i = identities.values().iterator(); i.hasNext(); ) {
            Identity id = (Identity)i.next();
            if( id.isGOOD() ) {
                list.add(id);
            }
        }
        return list;
    }

    public List<LocalIdentity> getLocalIdentities() {
        return new ArrayList<LocalIdentity>(localIdentities.values());
    }

    public List<Identity> getIdentities() {
        return new ArrayList<Identity>(identities.values());
    }

    public static boolean isDatabaseUpdatesAllowed() {
        return databaseUpdatesAllowed;
    }

    public static void setDatabaseUpdatesAllowed(boolean dbUpdatesAllowed) {
        databaseUpdatesAllowed = dbUpdatesAllowed;
    }
    
    /**
     * Identities are saved on demand, but the information about the 
     * last files shared per board must be saved during exit.
     */
    public void save() throws StorageException {
        try {
            AppLayerDatabase.getIdentitiesDatabaseTable().saveLastFilesSharedPerIdentity(getLocalIdentities());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error saving last files shared information", e);
        }
    }
    
    /**
     * Applies trust state of source identity to target identity.
     */
    public static void takeoverTrustState(Identity source, Identity target) {
        if( source.isGOOD() ) {
            target.setGOOD();
        } else if( source.isOBSERVE() ) {
            target.setOBSERVE();
        } else if( source.isBAD() ) {
            target.setBAD();
        } else if( source.isCHECK() ) {
            target.setCHECK();
        }
    }
    
    // TODO: merge the imported identities with the existing identities (WOT), use a mergeIdentities method
    public int importIdentities(List importedIdentities) {
        // for now we import new identities, and take over the trust state if our identity state is CHECK
        int importedCount = 0;
        for(Iterator i=importedIdentities.iterator(); i.hasNext(); ) {
            Identity newId = (Identity) i.next();
            if( !isNewIdentityValid(newId) ) {
                // hash of public key does not match the unique name
                // skip identity
                continue;
            }
            Identity oldId = getIdentity(newId.getUniqueName());
            if( oldId == null ) {
                // add new id
                addIdentity(newId);
                importedCount++;
            } else if( oldId.isCHECK() && !newId.isCHECK() ) {
                // take over trust state
                takeoverTrustState(newId, oldId);
                importedCount++;
            }
        }
        return importedCount;
    }
    
    /**
     * This method checks an Identity for validity.
     * Checks if the digest of this Identity matches the pubkey (digest is the part after the @ in the username)
     */
    public boolean isIdentityValid(Identity id) {

        String uName = id.getUniqueName();
        String puKey = id.getKey();

        try {
            // check if the digest matches
            String given_digest = uName.substring(uName.indexOf("@") + 1, uName.length()).trim();
            String calculatedDigest = Core.getCrypto().digest(puKey.trim()).trim();
            calculatedDigest = Mixed.makeFilename(calculatedDigest).trim();
            if( !Mixed.makeFilename(given_digest).equals(calculatedDigest) ) {
                logger.severe("Warning: public key of sharer didn't match its digest:\n" + 
                              "given digest :'" + given_digest + "'\n" + 
                              "pubkey       :'" + puKey.trim() + "'\n" + 
                              "calc. digest :'" + calculatedDigest + "'");
                return false;
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Exception during key validation", e);
            return false;
        }        
        return true;
    }
    
    /**
     * Checks if we can accept this new identity.
     * If the public key of this identity is already assigned to another identity, then it is not valid.
     */
    public boolean isNewIdentityValid(Identity id) {
        
        // check if hash matches the public key
        if( !isIdentityValid(id) ) {
            return false;
        }
        
        // check if the public key is known, maybe someone sends with same pubkey but different names before the @
        for( Iterator<Identity> i=getIdentities().iterator(); i.hasNext(); ) {
            Identity anId = i.next();
            if( id.getKey().equals(anId.getKey()) ) {
                logger.severe("Rejecting new Identity because its public key is already used by another known Identity. "+
                        "newId='"+id.getUniqueName()+"', oldId='"+anId.getUniqueName()+"'");
                return false;
            }
        }
        
        // for sure, check own identities too
        for( Iterator<LocalIdentity> i=getLocalIdentities().iterator(); i.hasNext(); ) {
            Identity anId = i.next();
            if( id.getKey().equals(anId.getKey()) ) {
                logger.severe("Rejecting new Identity because its public key is already used by an OWN Identity. "+
                        "newId='"+id.getUniqueName()+"', oldId='"+anId.getUniqueName()+"'");
                return false;
            }
        }
        
        return true;
    }
}
