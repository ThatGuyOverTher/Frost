/*
  KnownBoardsXmlDAO.java / Frost
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
package frost.storage;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import frost.*;
import frost.gui.objects.*;
import frost.messages.*;

public class KnownBoardsXmlDAO {
// FIXME: load only 05 boards on 05, same for 07! 
    private static Logger logger = Logger.getLogger(KnownBoardsXmlDAO.class.getName());

    public static List loadKnownBoards(File file) {

        File boards = file;
        ArrayList knownBoards = new ArrayList();
        if( boards.exists() ) {
            Document doc = null;
            try {
                doc = XMLTools.parseXmlFile(boards, false);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error reading knownboards.xml", ex);
                return knownBoards;
            }
            Element rootNode = doc.getDocumentElement();
            if( rootNode.getTagName().equals("FrostKnownBoards") == false ) {
                logger.severe("Error - invalid knownboards.xml: does not contain the root tag 'FrostKnownBoards'");
                return knownBoards;
            }
            // pass this as an 'AttachmentList' to xml read method and get all board attachments
            AttachmentList al = new AttachmentList();
            try {
                al.loadXMLElement(rootNode);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error - knownboards.xml: contains unexpected content.", ex);
                return knownBoards;
            }
            List lst = al.getAllOfType(Attachment.BOARD);
            for(Iterator i=lst.iterator(); i.hasNext(); ) {
                BoardAttachment ba = (BoardAttachment)i.next();
                knownBoards.add(ba.getBoardObj());
            }
            logger.info("Loaded " + knownBoards.size() + " known boards.");
        }
        return knownBoards;
    }
    
    public static boolean saveKnownBoards(File file, List knownBoards) {
        Document doc = XMLTools.createDomDocument();
        if (doc == null) {
            logger.severe("Error - saveBoardTree: factory couldn't create XML Document.");
            return false;
        }

        Element rootElement = doc.createElement("FrostKnownBoards");
        doc.appendChild(rootElement);

        Iterator i = knownBoards.iterator();
        while (i.hasNext()) {
            Board b = (Board)i.next();
            BoardAttachment current = new BoardAttachment(b);
            Element anAttachment = current.getXMLElement(doc);
            rootElement.appendChild(anAttachment);
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, file.getPath());
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Exception while writing knownboards.xml:", ex);
        }
        if (!writeOK) {
            logger.severe("Error exporting knownboards, file was not saved");
        }
        return writeOK;
    }
}
