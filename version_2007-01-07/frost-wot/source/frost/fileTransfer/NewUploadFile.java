/*
 NewUploadFile.java / Frost
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
package frost.fileTransfer;

import java.io.*;

/**
 * Holds the data for a new upload file before SHA is calculated.
 */
public class NewUploadFile {
    
    protected File file;
    protected String from;
    
    public NewUploadFile(File f, String fromName) {
        file = f;
        from = fromName;
    }
    
    public File getFile() {
        return file;
    }
    
    public String getFrom() {
        return from;
    }
}
