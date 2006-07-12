/*
 FileAttachment.java / Frost
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
package frost.messages;

import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import frost.*;

public class FileAttachment extends Attachment {
    
    private File file = null;

    private String key = null; // Name of this key
    private Long size = new Long(0); // Filesize
    private String filename = new String();

	/* (non-Javadoc)
	 * @see frost.messages.Attachment#getType()
	 */
	public int getType() {
		return Attachment.FILE;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#getXMLElement(org.w3c.dom.Document)
	 */
	public Element getXMLElement(Document doc) {
        
        Element fileelement = doc.createElement("File");

        Element element = doc.createElement("name");
        CDATASection cdata = doc.createCDATASection(getFilename());
        element.appendChild(cdata);
        fileelement.appendChild(element);

        element = doc.createElement("size");
        Text textnode = doc.createTextNode("" + getSize().toString());
        element.appendChild(textnode);
        fileelement.appendChild(element);

        element = doc.createElement("key");
        textnode = doc.createTextNode(getKey());
        element.appendChild(textnode);
        fileelement.appendChild(element);

        element = doc.createElement("Attachment");
        element.setAttribute("type", "file");
        element.appendChild(fileelement);

		return element;
	}

	/* (non-Javadoc)
	 * @see frost.XMLizable#loadXMLElement(org.w3c.dom.Element)
	 */
	public void loadXMLElement(Element e) throws SAXException {
		Element _file = (Element) XMLTools.getChildElementsByTagName(e, "File").iterator().next();
        
        filename = XMLTools.getChildElementsCDATAValue(_file, "name");
        key = XMLTools.getChildElementsTextValue(_file, "key");
        size = new Long(XMLTools.getChildElementsTextValue(_file, "size"));
	}

	/**
	 * @param e
	 * @throws SAXException
	 */
	public FileAttachment(Element e) throws SAXException {
		loadXMLElement(e);
	}

	public FileAttachment(String fname, String k, long s) {
        filename = fname;
        size = new Long(s);
        key = k;
	}

    public FileAttachment(File f) {
        file = f;
        
        filename = file.getName();
        size = new Long(file.length());
    }

    /* 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		String myName = getFilename();
		String otherName = ((FileAttachment) o).getFilename();
		return myName.compareTo(otherName);
	}

    public String getFilename() {
        return filename;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String k) {
        key = k;
    }
    public Long getSize() {
        return size;
    }
    public File getInternalFile() {
        return file;
    }
}
