/*
  FcpResults.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fcp;

/**
 * This class is a utility class to provide a datatype for results
 * returned from an FCP operation.
 * This class is used by freenet 05 and 07. 07 does not use the metadata. 
 */
public class FcpResults {
    
    private byte [] rawMetadata = null; // only used by freenet 0.5
    
    private String chkUri = null;

    public FcpResults() {
    }

    /**
     * Retrieves the CHK URI.
     * Valid for uploading.
     *
     * @return the CHK URI
     */
    public String getChkUri() {
       return chkUri;
    }

    /**
     * Sets the CHK URI.
     * Valid for uploading.
     *
     * @param chkUri the CHK URI
     */
    void setChkUri(String chkUri) {
       this.chkUri = chkUri;
    }

    /**
     * Retrieves the metadata.
     * Valid for downloading.
     *
     * @return the metadata
     */
    public String [] getMetadataAsLines() {
        if( rawMetadata == null ) {
            return null;
        }
        return new String(rawMetadata).split("\n");
    }

    /**
     * @return raw metadata bytes
     */
    public byte[] getRawMetadata() {
        return rawMetadata;
    }

    /**
     * Sets the metadata.
     * Valid for downloading.
     *
     * @param metadata the metadata
     */
    public void setRawMetadata(byte[] bs) {
        rawMetadata = bs;
    }
}
