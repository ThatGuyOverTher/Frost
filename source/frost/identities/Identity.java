package frost.identities;

import java.io.*;

import frost.*;
import frost.FcpTools.*;

/**
 * Represents a user identity, should be immutable.
 */
public class Identity implements Serializable
{
    private String name;
    private String uniqueName;
    protected String key, keyaddress;
    protected transient FcpConnection con;
    public static final String NA = "NA";
    private static ThreadLocal tmpfile;

    /**
     * we use this constructor whenever we have all the info
     */
    public Identity(String name, String keyaddress, String key)
    {
        this.keyaddress = keyaddress;
        this.key = key;
        setName(name);
    }

    /**
     * this constructor fetches the key from a SSK,
     * it blocks so it should be done from the TOFDownload thread (I think)
     */
    public Identity(String name, String keyaddress) throws IllegalArgumentException
    {
        this.keyaddress = keyaddress;

        con = FcpFactory.getFcpConnectionInstance();
        if( con == null )
        {
            this.key = NA;
            return;
        }

        if( !keyaddress.startsWith("CHK@") )
        {
            this.key = NA;
            throw (new IllegalArgumentException("not a CHK"));
        }

        System.out.println("Identity: Starting to request CHK for '" + name +"'");
        String targetFile = frame1.frostSettings.getValue("temp.dir") + name + ".key.tmp";

        // try X times to get identity, its too important to not to try it ;)
        // will lower the amount of N/A messages because of non found keys
        boolean wasOK = false;
        int maxTries = 3;
        int tries = 0;
        while( wasOK == false && tries < maxTries )
        {
            try {
                wasOK = FcpRequest.getFile(keyaddress, null, new File(targetFile), 25, false);
            }
            catch(Exception e) { ; }
            mixed.wait(3500);
            tries++;
        }

        if( wasOK )
        {
            key = FileAccess.read(targetFile);
            System.out.println("Identity: CHK received for " +name);
        }
        else
        {
            key=NA;
            System.out.println("Identity: Failed to get CHK for " +name);
        }
        File tfile = new File(targetFile);
        tfile.delete();

        setName(name); // must be called after key is got!
    }

    private void setName(String nam)
    {
        this.name = nam;
        if( getKey().equals( NA ) )
            this.uniqueName = nam;
        else
            this.uniqueName = nam + "@" + frame1.getCrypto().digest( getKey() );
    }

    //obvious stuff
    public String getName()
    {
        return name;
    }
    public String getKeyAddress()
    {
        return keyaddress;
    }
    public String getKey()
    {
        return key;
    }
    public String getStrippedName()
    {
        return new String(name.substring(0,name.indexOf("@")));
    }


    public String getUniqueName()
    {
        return uniqueName;
    }
}
