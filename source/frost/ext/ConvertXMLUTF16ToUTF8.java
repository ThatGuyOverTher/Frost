/*
ConvertXMLUTF16ToUTF8.java / Frost
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
package frost.ext;

import java.io.*;

import frost.*;
import frost.messages.*;

public class ConvertXMLUTF16ToUTF8 {
    
    private File keypoolDirFile = null;
    private File archiveDirFile = null;
  
    private static XmlFileFilter xmlFileFilter = new XmlFileFilter();
    
    private static long filesProcessed = 0;
    private static long savedBytes = 0;
    
    public static void main(String[] args) throws Throwable {
        System.out.println("Converting all XML files in keypool and archive from encoding UTF-16 to UTF-8...");
        ConvertXMLUTF16ToUTF8 c = new ConvertXMLUTF16ToUTF8();
        if( c.initializeLockFile() == false ) {
            return;
        }
        c.run();
        System.out.println("Ready, processed "+filesProcessed+" files, saved "+savedBytes+" bytes.");
    }

    public ConvertXMLUTF16ToUTF8() {
        
        String archiveDir = Core.frostSettings.getValue("archive.dir");
        if( archiveDir.length() == 0 ) {
            archiveDir = null;
        } else {
            // append messages subfolder
            archiveDir += ("messages" + File.separator);
        }
        
        String keypoolDir = Core.frostSettings.getValue("keypool.dir");
        if( keypoolDir.length() == 0 ) {
            keypoolDir = null;
        }

        if( archiveDir != null ) {
            archiveDirFile = new File(archiveDir);
            if( archiveDirFile.isDirectory() == false ) {
                archiveDirFile = null;
            }
        }
        
        if( keypoolDir != null ) {
            keypoolDirFile = new File(keypoolDir);
            if( keypoolDirFile.isDirectory() == false ) {
                keypoolDirFile = null;
            }
        }
        
        // output messages
        if( keypoolDirFile == null && archiveDirFile == null ) {
            System.out.println("Neither keypool nor archive directory are existing directories, conversion skipped.");
            return;
        }
        if( keypoolDirFile == null ) {
            System.out.println("Keypool directory does not exists, converting archive only.");
            return;
        }
        if( archiveDirFile == null ) {
            System.out.println("Archive directory does not exists, converting keypool only.");
            return;
        }
    }
    
    private boolean initializeLockFile() {
        // check for running frost (lock file)
        File runLock = new File(".frost_run_lock");
        boolean fileCreated = false;
        try {
            fileCreated = runLock.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
        if (fileCreated == false) {
            System.out.println("Frost is currently running, conversion aborted.");
            return false;
        }
        runLock.deleteOnExit();
        return true;
    }

    private void run() throws Throwable {
        // scan all directories in keypoolDir and archiveDir for .xml files and convert them (load+save)
        if( keypoolDirFile != null ) {
            processMainDirectories(keypoolDirFile);
        }
        if( archiveDirFile != null ) {
            processMainDirectories(archiveDirFile);
        }
    }
    
    private void processMainDirectories(File dir) {
        File[] dirList = dir.listFiles();
        for(int x=0; x < dirList.length; x++) {
            if( dirList[x].isDirectory() == false ) {
                continue;
            }
            File[] subDirList = dirList[x].listFiles();
            for(int z=0; z < subDirList.length; z++) {
                if( subDirList[z].isDirectory() == false ) {
                    continue;
                }
            
                File[] xmlFileList = subDirList[z].listFiles(xmlFileFilter);
                for(int y=0; y < xmlFileList.length; y++ ) {
                    File xmlFile = xmlFileList[y];
                    if( xmlFile.length() < 20 ) {
                        // just a marker file, ignore silently
                        continue;
                    }
                    try {
                        long bytesBefore = xmlFile.length();
                        MessageObject mo = new MessageObject(xmlFile); // loads UTF-16 and UTF-8
                        boolean saveOk = mo.save(); // saves as UTF-8
                        if( !saveOk ) {
                            System.out.println("Message could not be saved, conversion skipped: "+xmlFile.getPath());
                            continue;
                        }
                        filesProcessed++;
                        savedBytes += (bytesBefore - xmlFile.length());
                    } catch(Throwable t) {
                        System.out.println("Message could not be loaded, conversion skipped: "+xmlFile.getPath());
                        continue;
                    }
                }
            }
        }
    }
    
    private static class XmlFileFilter implements FileFilter {
        public boolean accept(File f) {
            if( f.isFile() && f.getName().endsWith(".xml") ) {
                return true;
            }
            return false;
        }
    }
}
