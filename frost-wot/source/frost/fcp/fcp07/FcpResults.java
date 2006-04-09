/*
  FcpResults.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.fcp.fcp07;

/**
 * This class is a utility class to provide a datatype for results
 * returned from an FCP operation.
 * @author <a href=mailto:landtuna@hotmail.com>Jim Hunziker</a>
 */
public class FcpResults {

    /** the CHK URI */
    String chkUri = null;

    public FcpResults(String cu)
    {
        chkUri = cu;
    }

    public FcpResults()
    {
    }

    /**
     * Retrieves the CHK URI.
     * Valid for uploading.
     *
     * @return the CHK URI
     */
    public String getChkUri()
    {
	   return chkUri;
    }

    /**
     * Sets the CHK URI.
     * Valid for uploading.
     *
     * @param chkUri the CHK URI
     */
    void setChkUri(String chkUri)
    {
	   this.chkUri = chkUri;
    }
}
