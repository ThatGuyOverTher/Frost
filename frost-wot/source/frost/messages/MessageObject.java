/*
  MessageObject.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.io.File;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import frost.*;
import frost.gui.objects.FrostBoardObject;

public class MessageObject implements XMLizable 
{
	AttachmentList attachments; // this is never null!
	
	public Element getXMLElement(Document d){
		return messageObjectPopulateElement(d);
	}
	
	protected Element messageObjectPopulateElement(Document d){
		Element el = d.createElement("FrostMessage");
		
		CDATASection cdata;
		Element current;
		
		//from
		current = d.createElement("From");
		cdata = d.createCDATASection(getFrom());
		current.appendChild(cdata);
		el.appendChild(current);
		
		//subject
		current = d.createElement("Subject");
		cdata = d.createCDATASection(getSubject());
		current.appendChild(cdata);
		el.appendChild(current);
		
		//date
		current = d.createElement("Date");
		cdata = d.createCDATASection(getDate());
		current.appendChild(cdata);
		el.appendChild(current);
		
		 //time
		 current = d.createElement("Time");
		 cdata = d.createCDATASection(getTime());
		 current.appendChild(cdata);
		 el.appendChild(current);
		 
		//body
		current = d.createElement("Body");
		cdata = d.createCDATASection(getContent());
		current.appendChild(cdata);
		el.appendChild(current);
		
		//board
		current = d.createElement("Board");
		cdata = d.createCDATASection(getBoard());
		current.appendChild(cdata);
		el.appendChild(current);
		
		//public Key
		if (publicKey!=null) {
			current = d.createElement("pubKey");
			cdata = d.createCDATASection(getPublicKey());
			current.appendChild(cdata);
			el.appendChild(current);
		}
	
		//attachments
        if( attachments.size() > 0 )
        {
            el.appendChild(attachments.getXMLElement(d));
        }
		
		return el;
	}
	
	public void loadXMLElement(Element e) throws SAXException {
		messageObjectPopulateFromElement(e);
	}
	
	protected void messageObjectPopulateFromElement(Element e)
		throws SAXException {
			
			from = XMLTools.getChildElementsCDATAValue(e,"From");
			date = XMLTools.getChildElementsCDATAValue(e,"Date");
			subject = XMLTools.getChildElementsCDATAValue(e,"Subject");
			time = XMLTools.getChildElementsCDATAValue(e,"Time");
			publicKey = XMLTools.getChildElementsCDATAValue(e,"pubKey");
			board = XMLTools.getChildElementsCDATAValue(e,"Board");
			content = XMLTools.getChildElementsCDATAValue(e,"Body");
			
            List l = XMLTools.getChildElementsByTagName(e,"AttachmentList");
            if( l.size() > 0 )
            {
                Element _attachments = (Element)l.get(0); 
                attachments = new AttachmentList();
                attachments.loadXMLElement(_attachments);
            }
	}

    static final char[] evilChars = {'/', '\\', '*', '=', '|', '&', '#', '\"', '<', '>'}; // will be converted to _
			//FIXME: this one is missing the "?" char as opposed to mixed.makeFilename
    String board, content, from, subject, date, time, index, publicKey, newContent;
    
    File file;

  /**
   * Creates a Vector of Vectors which contains data for the
   * attached files table.
   */    
    public Vector getFileAttachments() {
        Vector table = new Vector();
        AttachmentList files = attachments.getAllOfType(Attachment.FILE);
        Iterator i = files.iterator();
        while(i.hasNext())
        {
            FileAttachment fa = (FileAttachment)i.next();
            SharedFileObject sfo = fa.getFileObj();
            
            if( sfo.getKey() != null && sfo.getKey().length() > 40 &&
                sfo.getFilename() != null && sfo.getFilename().length() > 0 )
            {
                Vector rows = new Vector();
                rows.add(sfo.getFilename());
                rows.add(sfo.getSize());  //don't really need key
                table.add(rows);
            } 
        }
        return table;
    }

    /**
     * Creates a Vector of Vectors which contains data for the
     * attached boards table.
     */    
    public Vector getBoardAttachments()
    {
        Vector table = new Vector();
        AttachmentList boards = attachments.getAllOfType(Attachment.BOARD);
        Iterator i = boards.iterator();
        while(i.hasNext())
        {
            BoardAttachment ba = (BoardAttachment)i.next();
            FrostBoardObject aBoard = ba.getBoardObj();
            
            
            Vector rows = new Vector();
            rows.add(aBoard.getBoardName());
            if (aBoard.getPublicKey() ==null &&
            	aBoard.getPrivateKey()==null)
            	rows.add("public");
            else if (aBoard.getPublicKey() != null &&
	    	aBoard.getPrivateKey()==null)
            	rows.add("read - only");
            else
            	rows.add("read / write");
            
            table.add(rows);
        }
        return table;
    }

    public String getPublicKey() {
    return publicKey;
    }
    public void setPublicKey(String pk) {
    publicKey=pk;
    }
    public String getBoard() {
    return board;
    }
    public String getContent() {
    return content;
    }
    public String getFrom() {
    return from;
    }
    public String getSubject() {
    return subject;
    }
    public String getDate() {
    return date;
    }
    public String getTime() {
    return time;
    }
    public String getIndex() {
    return index;
    }
    public File getFile() {
    return file;
    }

    /**Set*/
    public void setBoard(String board) {
    this.board = board;
    }
    public void setContent(String content) {
    this.content = content;
    }
    public void setFrom(String from) {
    this.from = from;
    }
    public void setSubject(String subject) {
    this.subject = subject;
    }
    public void setDate(String date) {
    this.date = date;
    }
    public void setTime(String time) {
    this.time = time;
    }
    public void setFile(File file) {
    this.file = file;
    }
    public void setIndex(String index) {
    this.index = index;
    }

    public boolean isValid() {

	if (subject == null) subject = new String();
	if (content == null) content = new String();
    if (date.equals(""))
        return false;
    if (time.equals(""))
        return false;
   // if (subject.equals(""))
    //    return false;
    if (board.equals(""))
        return false;
    if (from.equals(""))
        return false;

    if (from.length() > 256)
        return false;
    if (subject!=null && subject.length() > 256)
        return false;
    if (board.length() > 256)
        return false;
    if (date.length() > 22)
        return false;
    if (content.length() > 32*1024)
        return false;

    return true;
    }

    /**Set all values*/
    public void analyzeFile() throws Exception
    {
        // set index for this msg from filename
        String filename = file.getName();
        this.index = (filename.substring(filename.lastIndexOf("-") + 1, filename.lastIndexOf(".xml"))).trim();
        // ensure all needed fields are properly filled
        if( from == null || date == null ||  time == null ||
            board == null || !isValid() )
        {
        	Core.getOut().println("Analyze file failed.  File saved as \"badMessage\", send to a dev.  Reason:");
        	if (!isValid()) Core.getOut().println("isValid failed");
        	if (content==null) Core.getOut().println("content null");
        	file.renameTo(new File("badMessage"));
            throw new Exception("Message have invalid or missing fields.");
            
        }
        // replace evil chars
        for( int i = 0; i < evilChars.length; i++ )
        {
            this.from = this.from.replace(evilChars[i], '_');
            this.subject = this.subject.replace(evilChars[i], '_');
            this.date = this.date.replace(evilChars[i], '_');
            this.time = this.time.replace(evilChars[i], '_');
        }
    }

    /**
     * Parses the XML file and passes the FrostMessage element to
     * XMLize load method.
     */    
    protected void loadFile() throws Exception
    {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(this.file, false);
        } catch(Exception ex) {  // xml format error
			File badMessage = new File("badmessage.xml");
			if (file.renameTo(badMessage))
				Core.getOut().println("Error - send the file badmessage.xml to a dev for analysis, more details below:");
        	ex.printStackTrace(Core.getOut()); 
        } 

        if( doc == null )
        {
            throw new Exception("Error - MessageObject.loadFile: could'nt parse XML Document.");
        }

        Element rootNode = doc.getDocumentElement();

        if( rootNode.getTagName().equals("FrostMessage") == false )
        {
        	File badMessage = new File("badmessage.xml");
        	if (file.renameTo(badMessage))
        		Core.getOut().println("Error - send the file badmessage.xml to a dev for analysis, more details below:");
            throw new Exception("Error - invalid message: does not contain the root tag 'FrostMessage'");
        }
        
        // load the message load itself
        loadXMLElement(rootNode);
    }

    /**
     * Constructor.
     * Used to construct an instance for an existing messagefile.
     */
    public MessageObject(File file) throws Exception
    {
        this();
        if( file == null || 
            file.exists() == false ||
            file.length() < 20 ) // prolog+needed tags are always > 20, but we need to filter 
        {                        // out the messages containing "Empty" (encrypted for someone else)
        	file.renameTo(new File("badMessage"));   
            throw new Exception("Invalid input file for MessageObject, send the file \"badMessage\" to a dev");
        }
        this.file = file;
        loadFile();
        // ensure basic contents and formats
        analyzeFile();
    }

    /**
     * Constructor.
     * Used to contruct an instance for a new message.
     */
    public MessageObject() {
    this.board = "";
    this.from = "";
    this.subject = "";
    this.board = "";
    this.date = "";
    this.time = "";
    this.content = "";
    this.publicKey = "";
    this.attachments = new AttachmentList();
    }
    
    public List getOfflineFiles() {
    	if (attachments == null) return null;
    	
    	List result = new LinkedList();
    	
    	List fileAttachments = attachments.getAllOfType(Attachment.FILE);
    	Iterator it = fileAttachments.iterator();
    	while (it.hasNext()) {
    		SharedFileObject sfo = ((FileAttachment)it.next()).getFileObj();
    		if (!sfo.isOnline())
    			result.add(sfo);
    	}
    	return result;
    }
    
    public AttachmentList getAttachmentList()
    {
        return attachments;
    }
}
