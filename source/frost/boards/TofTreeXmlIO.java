/*
  TofTreeXmlIO.java / Frost
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
package frost.boards;

import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.w3c.dom.*;

import frost.*;
import frost.util.*;

public class TofTreeXmlIO
{
    private static Logger logger = Logger.getLogger(TofTreeXmlIO.class.getName());

    /**************************************************
     * TREE LOAD METHODS ******************************
     **************************************************/

    /**
     * Methods creates the DefaultTreeModel that is used in the TofTree.
     * Model needs to be created here because we want to expand rows on loading.
     * So we also need the tree to call expandRow() on it.
     */
    public boolean loadBoardTree(TofTree tree, TofTreeModel model, String filename) {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(filename, false);
        } catch(Exception ex) { ; } // xml format error

        if( doc == null ) {
            logger.severe("Error - loadBoardTree: could'nt parse XML Document.");
            return false;
        }

        Element rootNode = doc.getDocumentElement();

        if( rootNode.getTagName().equals("FrostBoardTree") == false ) {
            logger.severe("Error - boards.xml invalid: does not contain the root tag 'FrostBoardTree'");
            return false;
        }
        // check if rootnode contains only a single boardEntry wich must be a folder (root folder)
        List nodelist = XMLTools.getChildElementsByTagName(rootNode, "FrostBoardTreeEntry");

        if( nodelist.size() != 1 ) {
            logger.severe("Error - boards.xml invalid: first element must be the one and only root folder ("+
                                nodelist.size()+")");
            return false;
        }
        Element boardRootNode = (Element)nodelist.get(0);
        String tagName = boardRootNode.getTagName();
        if( tagName == null || tagName.equals("FrostBoardTreeEntry") == false )
        {
            logger.severe("Error - boards.xml invalid: first element must the root folder");
            return false;
        }

        // add root node + set expanded state
        String name = getName( boardRootNode );

        boolean isFolder = isFolder( boardRootNode );
        if( isFolder == false )
        {
            logger.severe("Error - boards.xml invalid: first element must be a folder (the root folder)");
            return false;
        }

        Board treeRootNode = new Board(name, true);
        model.setRoot(treeRootNode);

        // now process all childs of this node recursively
        // and add all boards/folder to root node
        loadProcessFolder( boardRootNode, treeRootNode, tree, model );

        refreshModel( model, treeRootNode );
        
        model.initialSetPrimaryKeys();

        tree.updateUI();

        logger.info("Board tree loaded successfully.");

        return true;
    }

    /**
     * Process a nodes childs recursively.
     */
    private void loadProcessFolder(
        Element boardFolder,
        Board treeFolder,
        JTree tree,
        DefaultTreeModel model) {
        // process all childs of type "FrostBoardTreeEntry" , dive into folder and process them
        final List list =
            XMLTools.getChildElementsByTagName(boardFolder, "FrostBoardTreeEntry");
        for (int x = 0; x < list.size(); x++) {
            String nodename = null;
            boolean isFolder = false;

            Element child = (Element) list.get(x);
            // get name
            nodename = getName(child);
            if (nodename == null)
                continue;
            // get isFolder
            isFolder = isFolder(child);
            // add the child
            if (isFolder == false) {
                String publicKey = getPublicKey(child);
                String privateKey = getPrivateKey(child);
                String description = getDescription(child);

                // now if the child is a board, add it
                Board fbobj = new Board(nodename, publicKey, privateKey, description);
                // look for <config/> element and maybe configure board
                getBoardConfiguration(child, fbobj);
                // maybe restore lastUpdateStartedMillis ( = board update progress)
                List ltmp = XMLTools.getChildElementsByTagName(child, "lastUpdateStartedMillis");
                if (ltmp.size() > 0) {
                    Text txtname = (Text) ((Node) ltmp.get(0)).getFirstChild();
                    if (txtname != null) {
                        long millis = -1;
                        try {
                            millis = Long.parseLong(txtname.getData().trim());
                        } catch (Exception e) {
                            ;
                        }

                        if (millis > 0) {
                            fbobj.setLastUpdateStartMillis(millis);
                        }
                    }
                }

                // maybe restore lastBackloadUpdateStartedMillis ( = board update progress)
                ltmp = XMLTools.getChildElementsByTagName(child, "lastBackloadUpdateStartedMillis");
                if (ltmp.size() > 0) {
                    Text txtname = (Text) ((Node) ltmp.get(0)).getFirstChild();
                    if (txtname != null) {
                        long millis = -1;
                        try {
                            millis = Long.parseLong(txtname.getData().trim());
                        } catch (Exception e) {
                            ;
                        }

                        if (millis > 0) {
                            fbobj.setLastBackloadUpdateStartMillis(millis);
                        }
                    }
                }

                treeFolder.add(fbobj);
            } else {
                boolean isExpanded = isExpanded(child);

                // if the child is a folder, add it+maybe expand and dive into it
                Board fbobj = new Board(nodename, true);
                treeFolder.add(fbobj);

                loadProcessFolder(child, fbobj, tree, model); // dive into folder

                refreshModel(model, fbobj);

                // now expand path if previously expanded
                if (isExpanded == true) {
                    tree.expandPath(new TreePath(model.getPathToRoot(fbobj)));
                }
            }
        }
    }

    private void getBoardConfiguration( Element element, Board board )
    {
        List list = XMLTools.getChildElementsByTagName(element, "config");
        if( list.size() == 0 )
        {
            board.setConfigured( false );
            return;
        }

        board.setConfigured( true );
        element = (Element)list.get(0);

        String val = element.getAttribute("autoUpdate");
        board.setAutoUpdateEnabled( Boolean.valueOf(val).booleanValue() );

        val = element.getAttribute("maxMessageDisplay");
        if( val.length() == 0 )
            board.setMaxMessageDays( null );
        else
            board.setMaxMessageDays( new Integer(val) );

        val = element.getAttribute("showSignedOnly");
        if( val.length() == 0 )
            board.setShowSignedOnly( null );
        else
            board.setShowSignedOnly( Boolean.valueOf(val) );

        val = element.getAttribute("hideBadMessages");
        if( val.length() == 0 )
            board.setHideBad( null );
        else
            board.setHideBad( Boolean.valueOf(val) );

        val = element.getAttribute("hideCheckMessages");
        if( val.length() == 0 )
            board.setHideCheck( null );
        else
            board.setHideCheck( Boolean.valueOf(val) );

        val = element.getAttribute("hideObserveMessages");
        if( val.length() == 0 )
            board.setHideObserve( null );
        else
            board.setHideObserve( Boolean.valueOf(val) );
    }

    private void refreshModel(DefaultTreeModel model, Board node)
    {
        // all childs are new, send a nodesWhereInserted to model
        int childIndicies[] = new int[node.getChildCount()];

        for(int x=0; x< node.getChildCount(); x++)
        {
            childIndicies[x] = x;
        }
        model.nodesWereInserted(node, childIndicies);
    }

    private boolean isFolder(Element treeEntry)
    {
        String isFolder = treeEntry.getAttribute("isfolder");
        if( isFolder == null || isFolder.toLowerCase().equals("true") == false )
        {
            return false; // default is false
        }
        return true;
    }

    private boolean isExpanded(Element treeEntry)
    {
        String isExpanded = treeEntry.getAttribute("isexpanded");
        if( isExpanded == null || isExpanded.toLowerCase().equals("true") == false )
        {
            return false; // default is false
        }
        return true;
    }

    private String getName(Element treeEntry)
    {
        List list = XMLTools.getChildElementsByTagName(treeEntry, "name");
        if( list.size() != 1 )
        {
            logger.severe("Error - boards.xml invalid: there must be 1 <name> tag for each entry");
            return null;
        }
        Text txtname = (Text) ((Node)list.get(0)).getFirstChild();
        if( txtname == null )
            return null;
        return txtname.getData().trim();
    }

    private String getPublicKey(Element treeEntry)
    {
        List list = XMLTools.getChildElementsByTagName(treeEntry, "publicKey");
        if( list.size() > 1 )
        {
            logger.severe("Error - boards.xml invalid: there should be a maximum of 1 <publicKey> tag for each entry");
            return null;
        }
        if( list.size() == 0 )
        {
            return null;
        }
        Text txtname = (Text) ((Node)list.get(0)).getFirstChild();
        if( txtname == null )
            return null;
        return txtname.getData().trim();
    }

    /**
     * @param treeEntry
     * @return
     */
    private String getDescription(Element treeEntry) {
        List list = XMLTools.getChildElementsByTagName(treeEntry, "description");
        if (list.size() > 1) {
            logger.severe(
                "Error - boards.xml invalid: there should be a maximum of 1 <description> tag for each entry");
            return null;
        }
        if (list.size() == 0) {
            return null;
        }
        Text txtname = (Text) ((Node) list.get(0)).getFirstChild();
        if (txtname == null)
            return null;
        return txtname.getData().trim();
    }

    private String getPrivateKey(Element treeEntry)
    {
        List list = XMLTools.getChildElementsByTagName(treeEntry, "privateKey");
        if( list.size() > 1 )
        {
            logger.severe("Error - boards.xml invalid: there should be a maximum of 1 <privateKey> tag for each entry");
            return null;
        }
        if( list.size() == 0 )
        {
            return null;
        }
        Text txtname = (Text) ((Node)list.get(0)).getFirstChild();
        if( txtname == null )
            return null;
        return txtname.getData().trim();
    }

    /**************************************************
     * TREE SAVE METHODS ******************************
     **************************************************/

    public boolean saveBoardTree(TofTree tree, TofTreeModel model, String filename) {
        Document doc = XMLTools.createDomDocument();
        if( doc == null ) {
            logger.severe("Error - saveBoardTree: factory could'nt create XML Document.");
            return false;
        }

        Board root = (Board) model.getRoot();

        Element rootElement = doc.createElement("FrostBoardTree");
        doc.appendChild(rootElement);

        Element rootBoardElement = doc.createElement("FrostBoardTreeEntry");
        rootBoardElement.setAttribute("isfolder", "true");
        rootBoardElement.setAttribute("isexpanded", "true");

        Element nameElement = doc.createElement("name");
        Text text = doc.createTextNode( root.getName() );
        nameElement.appendChild( text );

        rootBoardElement.appendChild( nameElement );
        rootElement.appendChild( rootBoardElement );

        // append all childs and subchilds
        saveProcessFolder(rootBoardElement, root, doc, model, tree);

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, filename);
            logger.info("Board tree saved successfully.");
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Exception - saveBoardTree", t);
        }

        return writeOK;
    }

    private void saveProcessFolder(Element parentElement, Board treeNode, Document doc,
                                     DefaultTreeModel model, JTree tree)
    {
        // parentElement = element to append to
        for(int x=0; x < treeNode.getChildCount(); x++)
        {
            Board boardObject = (Board)treeNode.getChildAt(x);

            if( boardObject.isFolder() == false )
            {
                // its a board
                appendBoard(parentElement, boardObject, doc);
            }
            else
            {
                // its a folder
                Element newFolder = appendFolder(parentElement, boardObject, doc, model, tree);
                saveProcessFolder(newFolder, boardObject, doc, model, tree);
            }
        }
    }

    private void appendBoard(Element parent, Board board, Document doc) {
        Element rootBoardElement = doc.createElement("FrostBoardTreeEntry");
        Element element;

        CDATASection cdata;

        // <name>
        element = doc.createElement("name");
        cdata = doc.createCDATASection(board.getName());
        element.appendChild( cdata );
        rootBoardElement.appendChild( element );
        // pubkey
        if( board.getPublicKey() != null ) {
            element = doc.createElement("publicKey");
            cdata = doc.createCDATASection(board.getPublicKey());
            element.appendChild(cdata);
            rootBoardElement.appendChild(element);
        }
        // privkey
        if( board.getPrivateKey() != null ) {
            element = doc.createElement("privateKey");
            cdata = doc.createCDATASection(board.getPrivateKey());
            element.appendChild(cdata);
            rootBoardElement.appendChild(element);
        }
        // description
         if( board.getDescription() != null ) {
             element = doc.createElement("description");
             cdata = doc.createCDATASection(board.getDescription());
             element.appendChild( cdata );
             rootBoardElement.appendChild( element );
         }
        // <config />
        if( board.isConfigured() ) {
            element = doc.createElement("config");

            element.setAttribute("autoUpdate", "" + board.getAutoUpdateEnabled());
            if( board.getMaxMessageDisplayObj() != null ) {
                element.setAttribute("maxMessageDisplay", "" + board.getMaxMessageDisplay());
            }
            if( board.getShowSignedOnlyObj() != null ) {
                element.setAttribute("showSignedOnly", "" + board.getShowSignedOnly());
            }
            if( board.getHideBadObj() != null ) {
                element.setAttribute("hideBadMessages", "" + board.getHideBad());
            }
            if( board.getHideCheckObj() != null ) {
                element.setAttribute("hideCheckMessages", "" + board.getHideCheck());
            }
            if( board.getHideObserveObj() != null ) {
                element.setAttribute("hideObserveMessages", "" + board.getHideObserve());
            }
            rootBoardElement.appendChild(element);
        }

        // append lastUpdateStartedMillis
        if( board.getLastUpdateStartMillis() > 0 ) {
            element = doc.createElement("lastUpdateStartedMillis");
            Text text = doc.createTextNode("" + board.getLastUpdateStartMillis());
            element.appendChild(text);
            rootBoardElement.appendChild(element);
        }

        // append lastBackloadUpdateStartedMillis
        if( board.getLastUpdateStartMillis() > 0 ) {
            element = doc.createElement("lastBackloadUpdateStartedMillis");
            Text text = doc.createTextNode("" + board.getLastBackloadUpdateStartMillis());
            element.appendChild(text);
            rootBoardElement.appendChild(element);
        }

        parent.appendChild( rootBoardElement );
    }

    private Element appendFolder(Element parent, Board board, Document doc,
                                            DefaultTreeModel model, JTree tree)
    {
        Element rootBoardElement = doc.createElement("FrostBoardTreeEntry");
        rootBoardElement.setAttribute("isfolder", "true");
        boolean expanded;
        String expandedstr;
        expanded = tree.isExpanded( new TreePath(model.getPathToRoot(board)) );
        if( expanded )
            expandedstr = "true";
        else
            expandedstr = "false";

        rootBoardElement.setAttribute("isexpanded", expandedstr);
        // <name>
        Element element = doc.createElement("name");
        Text text = doc.createTextNode( board.getName() );
        element.appendChild( text );
        rootBoardElement.appendChild( element );
        parent.appendChild( rootBoardElement );
        return rootBoardElement;
    }
}