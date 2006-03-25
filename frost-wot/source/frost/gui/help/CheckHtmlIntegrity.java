/*
  CheckHtmlIntegrity.java / About Box
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
package frost.gui.help;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;

/**
 * Checks all HTML files in help.zip for 'http://', 'ftp://' links.
 * If those strings are found the help.zip is not used.
 * 
 * @author bback
 */
public class CheckHtmlIntegrity {

    private static Logger logger = Logger.getLogger(CheckHtmlIntegrity.class.getName());

    private static boolean isHtmlSecure = false;
    
    public static boolean isHtmlSecure() {
        return isHtmlSecure;
    }

    public static void scanZipFile(String fileName) {

        File file = new File(fileName);
        
        if( !file.isFile() || file.length() == 0 ) {
            logger.log(Level.SEVERE, "Zip file does not exist: "+file.getPath());
            return;
        }

        final byte[] zipData = new byte[4096];

        try {
            ZipFile zf = new ZipFile(file);
            for(Enumeration e=zf.entries(); e.hasMoreElements(); ) {
                ZipEntry ze = (ZipEntry)e.nextElement();
                String zn = ze.getName();
                if( zn.endsWith(".html") || zn.endsWith(".htm") ) {
                    
                    InputStream is = zf.getInputStream(ze);
                    ByteArrayOutputStream out = new ByteArrayOutputStream((int)ze.getSize());
                    while( true ) {
                        int len = is.read(zipData);
                        if( len < 0 ) {
                            break;
                        }
                        out.write(zipData, 0, len);
                    }
                    is.close();
                    
                    String htmlStr = new String(out.toByteArray(), "UTF-8").toLowerCase();
                    if( htmlStr.indexOf("http://") > -1 ||
                        htmlStr.indexOf("ftp://") > -1 ||
                        htmlStr.indexOf("nntp://") > -1 )
                    {
                        logger.log(Level.SEVERE, "Unsecure HTML file in help.zip found: "+zn);
                        return;
                    }
                }
            }
            // all files scanned, no unsecure found
            logger.log(Level.WARNING, "NO unsecure HTML file in help.zip found, all is ok.");
            isHtmlSecure = true;

        } catch( FileNotFoundException e ) {
            logger.log(Level.SEVERE, "Exception while reading help.zip. File is invalid.", e);
        }
        catch( IOException e ) {
            logger.log(Level.SEVERE, "Exception while reading help.zip. File is invalid.", e);
        }
    }
}
