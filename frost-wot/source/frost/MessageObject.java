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

package frost;
import java.io.File;
import java.util.*;

public class MessageObject {

    char[] evilChars = {'/', '\\', '*', '=', '|', '&', '#', '\"', '<', '>'}; // will be converted to _
			//FIXME: this one is missing the "?" char as opposed to mixed.makeFilename
    String board, content, from, subject, date, time, index, publicKey, newContent;
    File file;

    /**Get*/

    public String getNewContent() {
    return newContent;
    }

//TODO: should return AttachmentObjects (to create)
    // newContent is created here and contains whole msg without the found board tags
    public Vector getAttachments() {
    Vector table = new Vector();
    int pos = 0;
    int start = content.indexOf("<attached>");
    int end = content.indexOf("</attached>");
    newContent="";
    try {
        while (start != -1 && end != -1) {
            newContent += content.substring(pos, start).trim();

            int spaces = content.indexOf(" * ", start);
            if( spaces > 0 )
            {
                String filename = (content.substring(start + "<attached>".length(), spaces)).trim();
                String chkKey = (content.substring(spaces + " * ".length(), end)).trim();
                if( filename.length() > 0 && chkKey.length() > 0 )
                {
                    Vector rows = new Vector();
                    rows.add(filename);
                    rows.add(chkKey);
                    table.add(rows);
                }
            }
            pos = end + "</attached>".length();
            start = content.indexOf("<attached>", pos);
            end = content.indexOf("</attached>", pos);
        }
        newContent += content.substring(pos, content.length()).trim();
    } catch(Exception ex)
    {
        Core.getOut().println("Exception while reading attachments (catched):");
        ex.printStackTrace(Core.getOut());
    }

    return table;
    }

/**
 * 
 * @return list of the attached boards as raw strings
 */
	public Collection getBoardsAsStrings(){
		Collection result = new Vector();
        // always call 1 method that extracts contents savely
        Vector attboards = getBoards(false);
        
        Iterator i = attboards.iterator();
        while(i.hasNext())
        {
            Vector aboard = (Vector)i.next();
            if(aboard.size() == 3)
            {
                StringBuffer sb = new StringBuffer();
                sb.append( aboard.get(0) + " * " + aboard.get(1) + " * " + aboard.get(2) );
                result.add( sb.toString() );
            }
        }
		return result;
	}
    
    public Vector getBoards()
    {
        return getBoards(true); // default changes msg content and removes boards attachement 
    }
    
// TODO: should return AttachedBoards (to create)
    // newContent is created here and contains whole msg without the found board tags
    public Vector getBoards(boolean changeContent)
    {
        // TODO: this code does not care if the <board> or </board> appears somewhere in the content
        // if e.g. a <board></board> occurs in message, this throw a NullPointerException
        Vector table = new Vector();
        int pos = 0;
        int start = content.indexOf("<board>");
        int end = content.indexOf("</board>", start);
        if( changeContent )
            newContent = "";
        while (start != -1 && end != -1)
        {
            try
            {
                int boardPartLength = end - ( start + "<board>".length() );
                // must be at least 14, 1 char boardnamwe, 2 times " * " and keys="N/A"
                // mr. spammer: thx for pointing us at this *g*
                if(boardPartLength >= (1 + (2*3) + (2*3)) )
                {
                    if( changeContent )
                        newContent += content.substring(pos, start).trim();
                
                    int spaces = content.indexOf(" * ", start);
                    int spaces2 = content.indexOf(" * ", spaces + 1);
                    //System.out.println("* at " + spaces + " " + spaces2);
                    String boardName = (content.substring(start + "<board>".length(), spaces)).trim();
                    String pubKey = (content.substring(spaces + " * ".length(), spaces2)).trim();
                    String privKey = (content.substring(spaces2 + " * ".length(), end)).trim();
                    Vector rows = new Vector();
                    rows.add(boardName);
                    rows.add(pubKey);
                    rows.add(privKey);
                    table.add(rows);
                }
            }
            catch (RuntimeException e) // on wrong format a NullPointerException is thrown
            {
                System.out.println("Error in format of attached boards, skipping 1 entry.");
            }
            // maybe try next entry
            pos = end + "</board>".length();
            start = content.indexOf("<board>", pos);
            end = content.indexOf("</board>", pos);
        }
        if( changeContent )
            newContent += content.substring(pos, content.length()).trim();
        
        return table;
    }

    public String getPublicKey() {
    return publicKey;
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

    public String[] getRow() {
    String fatFrom = from;
    File newMessage = new File(file.getPath() + ".lck");

    if (newMessage.isFile()) {
        // this is the point where new messages get its bold look,
        // this is resetted in TOF.evalSelection to non-bold on first view
        fatFrom = new StringBuffer().append("<html><b>").append(from).append("</b></html>").toString();
    }
    if ( (content.indexOf("<attached>") != -1 && content.indexOf("</attached>") != -1) ||
        (content.indexOf("<board>") != -1 && content.indexOf("</board>") != -1) ){
        if (fatFrom.startsWith("<html><b>"))
        fatFrom = "<html><b><font color=\"blue\">" + from + "</font></b></html>";
        else
        fatFrom = "<html><font color=\"blue\">" + from + "</font></html>";
    }

    //String[] row = {index, fatFrom, subject, date + " " + time};
    String[] row = {index, fatFrom, subject, date, time};
    return row;
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

    if (date.equals(""))
        return false;
    if (time.equals(""))
        return false;
    if (subject.equals(""))
        return false;
    if (board.equals(""))
        return false;
    if (from.equals(""))
        return false;

    if (from.length() > 256)
        return false;
    if (subject.length() > 256)
        return false;
    if (board.length() > 256)
        return false;
    if (date.length() > 22)
        return false;
    // FIXED: no, this is freenet limit (actually its 32k, not 32000)
        /*
    if (content.length() > 32000)
        return false;*/

    return true;
    }

    /**Set all values*/
    public void analyzeFile()
    {
        try {
            String message = new String(FileAccess.readByteArray(file));
            if( !message.startsWith("Empty") )
            {
                Vector lines = FileAccess.readLines(file);
                this.board = SettingsFun.getValue(lines, "board");
                this.from = SettingsFun.getValue(lines, "from");
                this.subject = SettingsFun.getValue(lines, "subject");
                this.board = SettingsFun.getValue(lines, "board");
                this.date = SettingsFun.getValue(lines, "date");
                this.time = SettingsFun.getValue(lines, "time");
                this.publicKey = SettingsFun.getValue(lines, "publicKey");

                int offset = 17;
                int contentStart = message.indexOf("--- message ---\r\n");
                if( contentStart == -1 )
                {
                    contentStart = message.indexOf("--- message ---");
                    offset = 15;
                }

                if( contentStart != -1 )
                    this.content = message.substring(contentStart + offset, message.length());
                else
                    this.content = "";

                String filename = file.getName();
                this.index = (filename.substring(filename.lastIndexOf("-") + 1, filename.lastIndexOf(".txt"))).trim();

                for( int i = 0; i < evilChars.length; i++ )
                {
                    this.from = this.from.replace(evilChars[i], '_');
                    this.subject = this.subject.replace(evilChars[i], '_');
                    this.date = this.date.replace(evilChars[i], '_');
                    this.time = this.time.replace(evilChars[i], '_');
                }
            }
        } catch(Exception ex) {
            System.out.println("ERROR in MessageObject: could not read file '"+file.getPath()+"'");
            ex.printStackTrace();
        }
    }

    /**Constructor*/
    public MessageObject(File file)
    {
        this();
        this.file = file;
        analyzeFile();
    }

    /**Constructor*/
    public MessageObject() {
    this.board = "";
    this.from = "";
    this.subject = "";
    this.board = "";
    this.date = "";
    this.time = "";
    this.content = "";
    this.publicKey = "";
    }

}
