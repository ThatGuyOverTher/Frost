/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.messages;

import java.util.*;
import frost.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author zlatinb
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AttachmentList extends LinkedList implements XMLizable {
	
	/**
	 * traverses the list for attachment of given type
	 * @param type the type - BOARD, FILE, PERSON...
	 * @return another list which contains only attachments of the specified type
	 */
	public AttachmentList getAllOfType(int type) {
		Iterator i = iterator();
		AttachmentList result = new AttachmentList();
		while (i.hasNext()) {
			Attachment current = (Attachment)i.next();
			if (current.getType() == type)
				result.add(current);
		}
		return result;
	}
	
	public Element getXMLElement(Document d){
		Element el = d.createElement("AttachmentList");
		Iterator i = iterator();
		while (i.hasNext()) {
			Attachment current = (Attachment)i.next();
			el.appendChild(current.getXMLElement(d));
		}
		return el;
	}
	
	public void loadXMLElement(Element el) throws SAXException {
		Iterator i = XMLTools.getChildElementsByTagName(el,"Attachment").iterator();
		while (i.hasNext()){
			Attachment attachment;
			Element current = (Element)i.next();
			if (current.getAttribute("type").equals("file"))
				attachment = new FileAttachment(current);
			else
				attachment = new BoardAttachment(current);
			add(attachment);
		}
	}
}
