package frost.identities;

import java.util.*;
import java.io.*;
import javax.swing.*;

import frost.*;
import frost.FcpTools.*;
import frost.crypt.*;

/**
 * Represents the main user's identity
 */
public class LocalIdentity extends Identity implements Serializable
{
    private String privKey;

    /**
     * a constructor that assumes that the user has inserted the
     * key in his SSK already
     */
    public LocalIdentity(String name, String[] keys,String address)
    {
        super(name, address, keys[1]);
        privKey=keys[0];
    }

    /**
     * constructor that creates an RSA Keypair,
     * and inserts it in freenet
     */
    public LocalIdentity(String name)
    {
        this(name, frame1.getCrypto().generateKeys(), null);

        try {
            con = new FcpConnection(frame1.frostSettings.getValue("nodeAddress"),
                                    frame1.frostSettings.getValue("nodePort"));
        }
        catch( FcpToolsException e ) {
            System.out.println("fcptools exception");
            this.key=NA;
        }
        catch( IOException e ) {
            this.key=NA;
        }
        if( con == null )
        {
            System.out.println("Error - could not establish a connection to freenet node.");
            return;
        }

        //this is not so brutal anymore.
        FileAccess.writeFile(key,"pubkey.txt");
        try {
            String tmp = con.putKeyFromFile("CHK@", "pubkey.txt",0, false);
            keyaddress = tmp.substring(tmp.indexOf("CHK@"),tmp.indexOf("CHK@") + 58);
            System.out.println("Calculated my public key CHK: " + keyaddress + "\n");
        }
        catch( IOException e ) {
            System.out.println("Couldn't get key CHK.");
        }

        Thread keyUploader = new Thread()
        {
            public void run()
            {
                String ret=null;
                System.out.println("Trying to upload my public key ...");
                try {
                    ret = con.putKeyFromFile("CHK@", "pubkey.txt", 25, true);
                }
                catch( IOException e ) {
                    System.out.println("... couldn't upload public key.");
                }
                System.out.println("... uploaded my public key.");
            }
        };
        keyUploader.start();
    }

    public String getPrivKey()
    {
        return privKey;
    }
}

