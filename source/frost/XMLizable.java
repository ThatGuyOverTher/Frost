/*
  XMLizable.java / Frost
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
package frost;
import java.io.Serializable;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author zlatinb
 * Interface for objects that will be serializable in xml
 */
public interface XMLizable extends Serializable {
	
	/**
	 * Creates an xml element of those objects that can be serialized to xml
	 * @param container the parent document
	 * @return the element that's ready to be returned
	 */
	public Element getXMLElement(Document container); 
	
	public void loadXMLElement(Element e) throws SAXException; //this probably shouldn't be SAXException
	
}