/*
  Frost.java / Frost
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
package frost;

import java.io.*;
import java.nio.channels.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.UIManager.*;

import frost.util.*;
import frost.util.gui.*;
import frost.util.gui.translation.*;

public class Frost {

    private static Logger logger = Logger.getLogger(Frost.class.getName());
    private static String lookAndFeel = null;
    
    private static String cmdLineLocaleName = null;
    private static String cmdLineLocaleFileName = null;
    
    private static boolean offlineMode = false;

    /**
     * Main method
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println();
        System.out.println("Frost, Copyright (C) 2007 Frost Project");
        System.out.println("Frost comes with ABSOLUTELY NO WARRANTY!");
        System.out.println("This is free software, and you are welcome to");
        System.out.println("redistribute it under the GPL conditions.");
        System.out.println("Frost uses code from apache.org (Apache license),");
        System.out.println("bouncycastle.org (BSD license), Onion Networks (BSD license),");
        System.out.println("L2FProd.com (Apache license), Martin Newstead (LGPL license) and");
        System.out.println("Volker H. Simonis (GPL v2 license).");
        System.out.println();

        parseCommandLine(args);

        new Frost();
    }

    /**
     * This method sets the look and feel specified in the command line arguments.
     * If none was specified, the System Look and Feel is set.
     */
    private void initializeLookAndFeel() {
        LookAndFeel laf = null;
        try {
            // use cmd line setting
            if (lookAndFeel != null) {
                try {
                    laf = (LookAndFeel) Class.forName(lookAndFeel).newInstance();
                } catch(Throwable t) {}
                if (laf == null || !laf.isSupportedLookAndFeel()) {
                    laf = null;
                }
            }
            
            // still not set? use config file setting
            if( laf == null ) {
                String landf = Core.frostSettings.getValue(SettingsClass.LOOK_AND_FEEL);
                if( landf == null || landf.length() == 0 ) {
                    try {
                        laf = (LookAndFeel) Class.forName(landf).newInstance();
                    } catch(Throwable t) {}
                    if (laf == null || !laf.isSupportedLookAndFeel()) {
                        laf = null;
                    }
                }
            }
            
            // still not set? use system default
            if( laf == null ) {
                String landf = UIManager.getSystemLookAndFeelClassName();
                if( landf == null || landf.length() == 0 ) {
                    try {
                        laf = (LookAndFeel) Class.forName(landf).newInstance();
                    } catch(Throwable t) {}
                    if (laf == null || !laf.isSupportedLookAndFeel()) {
                        laf = null;
                    }
                }
            }            
            
            if (laf != null) {
                UIManager.setLookAndFeel(laf);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Using the default");
        }
    }

    /**
     * This method parses the command line arguments
     * @param args the arguments
     */
    private static void parseCommandLine(String[] args) {
        
        int count = 0;
        try {
            while (args.length > count) {
                if (args[count].equals("-?")
                    || args[count].equals("-help")
                    || args[count].equals("--help")
                    || args[count].equals("/?")
                    || args[count].equals("/help")) {
                    showHelp();
                    count++;
                } else if (args[count].equals("-lf")) {
                    lookAndFeel = args[count + 1];
                    count = count + 2;
                } else if (args[count].equals("-locale")) {
                    cmdLineLocaleName = args[count + 1]; //This settings overrides the one in the ini file
                    count = count + 2;
                } else if (args[count].equals("-localefile")) {
                    cmdLineLocaleFileName = args[count + 1]; 
                    count = count + 2;
                } else if (args[count].equals("-offline")) {
                    offlineMode = true;
                    count = count + 1;
                } else {
                    showHelp();
                }
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            showHelp();
        }
    }

    /**
     * This method shows a help message on the standard output and exits the program.
     */
    private static void showHelp() {
        System.out.println("java -jar frost.jar [-lf lookAndFeel] [-locale languageCode]\n");

        System.out.println("-lf     Sets the 'Look and Feel' Frost will use.");
        System.out.println("        (overriden by the skins preferences)\n");
        System.out.println("        These ones are currently available:");
//        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        LookAndFeelInfo[] feels = UIManager.getInstalledLookAndFeels();
        for (int i = 0; i < feels.length; i++) {
            System.out.println("           " + feels[i].getClassName());
        }
        System.out.println("\n         And this one is used by default:");
        System.out.println("           " + lookAndFeel + "\n");

        System.out.println("-locale  Sets the language Frost will use, if available.");
        System.out.println("         (overrides the setting in the preferences)\n");

        System.out.println("-localefile  Sets the language file.");
        System.out.println("             (allows tests of own language files)");
        System.out.println("             (if set the -locale setting is ignored)\n");

        System.out.println("-offline     Startup in offline mode.");

        System.out.println("Example:\n");
        System.out.print("java -jar frost.jar ");
        if (feels.length > 0) {
            System.out.print("-lf " + feels[0].getClassName() + " ");
        }
        System.out.println("-locale es\n");
        System.out.println("That command line will instruct Frost to use the");
        if (feels.length > 0) {
            System.out.println(feels[0].getClassName() + " look and feel and the");
        }
        System.out.println("Spanish language.");
        System.exit(0);
    }

    /**
     * Constructor
     */
    public Frost() {
        Core core = Core.getInstance();

        initializeLookAndFeel();

        if (!initializeLockFile(Language.getInstance())) {
            System.exit(1);
        }

        if (!checkLibs()) {
            System.exit(3);
        }

        try {
            core.initialize();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "There was a problem while initializing Frost.", e);
            System.exit(3);
        }
    }

    /**
     * This method checks for the presence of needed .jar files. If one of them
     * is missing, it shows a Dialog warning the user of the situation.
     * @return boolean true if all needed jars were present. False otherwise.
     */
    private boolean checkLibs() {
        // check for needed .jar files by loading a class and catching the error
        String jarFileName = "";
        try {
            // check for xercesImpl.jar
            jarFileName = "xercesImpl.jar";
            Class.forName("org.apache.xerces.dom.DocumentImpl");
            // check for xml-apis.jar
            jarFileName = "xml-apis.jar";
            Class.forName("org.w3c.dom.Document");
            // extra check for OutputFormat (xercesImpl.jar)
            jarFileName = "xercesImpl.jar";
            Class.forName("org.apache.xml.serialize.OutputFormat");
            // check for genChkImpl.jar
            jarFileName = "genChkImpl.jar";
            Class.forName("freenet.client.ClientKey");
            // check for fecImpl.jar
            jarFileName = "fecImpl.jar";
            Class.forName("fecimpl.FECUtils");
            // check for skinlf.jar
            jarFileName = "skinlf.jar";
            Class.forName("com.l2fprod.gui.SkinApplet");
            // check for skinlfFix.jar
            jarFileName = "skinlfFix.jar";
            Class.forName("com.l2fprod.gui.plaf.skin.SkinlfFixMarkerClass");
            // check for datechooser.jar
            jarFileName = "datechooser.jar";
            Class.forName("mseries.ui.MDateEntryField");
            // check for mckoidb.jar
            jarFileName = "mckoidb.jar";
            Class.forName("com.mckoi.JDBCDriver");
            // check for joda-time.jar
            jarFileName = "joda-time.jar";
            Class.forName("org.joda.time.DateTime");

        } catch (ClassNotFoundException e1) {
            MiscToolkit.getInstance().showMessage(
                "Please start Frost using the provided start "
                    + "scripts (frost.bat for Windows, frost.sh for Unix).\n"
                    + "If Frost was working and you updated just frost.jar, try updating with frost.zip\n"
                    + "ERROR: The jar file " + jarFileName + " is missing.",
                JOptionPane.ERROR_MESSAGE,
                "ERROR: The jar file " + jarFileName + " is missing.");
            return false;
        }
        return true;
    }
    
    private static File runLockFile = new File("frost.lock");
    private static FileChannel lockChannel;
    private static FileLock fileLock;

    /**
     * This method checks if the lockfile is present (therefore indicating that another instance
     * of Frost is running off the same directory). If it is, it shows a Dialog warning the
     * user of the situation and returns false. If not, it creates a lockfile and returns true.
     * @param language the language to use in case an error message has to be displayed.
     * @return boolean false if there was a problem while initializing the lockfile. True otherwise.
     */
    private boolean initializeLockFile(Language language) {
        // write minimal content into file
        FileAccess.writeFile("frost-lock", runLockFile);
        
        // try to aquire exclusive lock
        try {
            // Get a file channel for the file
            lockChannel = new RandomAccessFile(runLockFile, "rw").getChannel();
            fileLock = null;

            // Try acquiring the lock without blocking. This method returns
            // null or throws an exception if the file is already locked.
            try {
                fileLock = lockChannel.tryLock();
            } catch (OverlappingFileLockException e) {
                // File is already locked in this thread or virtual machine
            }
        } catch (Exception e) {
        }

        if (fileLock == null) {
            MiscToolkit.getInstance().showMessage(
                language.getString("Frost.lockFileFound") + "'" +
                    runLockFile.getAbsolutePath() + "'",
                JOptionPane.ERROR_MESSAGE,
                "ERROR: Found Frost lock file 'frost.lock'.");
            return false;
        }
        return true;
    }
    
    public static void releaseLockFile() {
        if( fileLock != null ) {
            try {
                fileLock.release();
            } catch (IOException e) {
            }
        }
        if( lockChannel != null ) {
            try {
                lockChannel.close();
            } catch (IOException e) {
            }
        }
        runLockFile.delete();
    }

    public static String getCmdLineLocaleFileName() {
        return cmdLineLocaleFileName;
    }

    public static String getCmdLineLocaleName() {
        return cmdLineLocaleName;
    }
    
    public static boolean isOfflineMode() {
        return offlineMode;
    }
}
