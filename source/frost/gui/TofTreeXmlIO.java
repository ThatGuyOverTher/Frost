/*
  TofTreeXmlIO.java / Frost
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
package frost.gui;

import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.tree.*;

import org.w3c.dom.*;

import frost.XMLTools;
import frost.gui.objects.FrostBoardObject;

public class TofTreeXmlIO
{
    /**************************************************
     * TREE LOAD METHODS ******************************
     **************************************************/

    /**
     * Methods creates the DefaultTreeModel that is used in the TofTree.
     * Model needs to be created here because we want to expand rows on loading.
     * So we also need the tree to call expandRow() on it.
     */
    public boolean loadBoardTree(JTree tree, String filename)
    {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(filename, false);
        } catch(Exception ex) { ; } // xml format error

        if( doc == null )
        {
            System.out.println("Error - loadBoardTree: factory could'nt create XML Document.");
            return false;
        }

        Element rootNode = doc.getDocumentElement();

        if( rootNode.getTagName().equals("FrostBoardTree") == false )
        {
            System.out.println("Error - boards.xml invalid: does not contain the root tag 'FrostBoardTree'");
            return false;
        }
        // check if rootnode contains only a single boardEntry wich must be a folder (root folder)
        ArrayList nodelist = XMLTools.getChildElementsByTagName(rootNode, "FrostBoardTreeEntry");

        if( nodelist.size() != 1 )
        {
            System.out.println("Error - boards.xml invalid: first element must be the one and only root folder ("+
                                nodelist.size()+")");
            return false;
        }
        Element boardRootNode = (Element)nodelist.get(0);
        String tagName = boardRootNode.getTagName();
        if( tagName == null || tagName.equals("FrostBoardTreeEntry") == false )
        {
            System.out.println("Error - boards.xml invalid: first element must the root folder");
            return false;
        }

        // add root node + set expanded state
        String name = getNameFromFrostBoardTreeEntry( boardRootNode );

        boolean isFolder = getIsFolderFromFrostBoardTreeEntry( boardRootNode );
        if( isFolder == false )
        {
            System.out.println("Error - boards.xml invalid: first element must be a folder (the root folder)");
            return false;
        }
        boolean isExpanded = getIsExpandedFromFrostBoardTreeEntry( boardRootNode );

        FrostBoardObject treeRootNode = new FrostBoardObject(name, true);
        DefaultTreeModel model = new DefaultTreeModel(treeRootNode);
        tree.setModel( model );

        // now process all childs of this node recursively
        // and add all boards/folder to root node
        loadProcessFolder( boardRootNode, treeRootNode, tree, model );

        refreshModel( model, treeRootNode );

        tree.updateUI();

System.out.println("Board tree loaded successfully.");

        return true;
    }

    /**
     * Process a nodes childs recursively.
     */
    public void loadProcessFolder(Element boardFolder, FrostBoardObject treeFolder, JTree tree, DefaultTreeModel model)
    {
        // process all childs of type "FrostBoardTreeEntry" , dive into folder and process them
        final ArrayList list = XMLTools.getChildElementsByTagName(boardFolder, "FrostBoardTreeEntry");
        for( int x=0; x<list.size(); x++ )
        {
            String nodename = null;
            boolean isFolder = false;

            Element child = (Element)list.get(x);
            // get name
            nodename = getNameFromFrostBoardTreeEntry( child );
            if( nodename == null )
                continue;
            // get isFolder
            isFolder = getIsFolderFromFrostBoardTreeEntry( child );
            // add the child
            if( isFolder == false )
            {
                String publicKey = getPublicKeyFromFrostBoardTreeEntry( child );
                String privateKey = getPrivateKeyFromFrostBoardTreeEntry( child );

                // now if the child is a board, add it
                FrostBoardObject fbobj = new FrostBoardObject(nodename, publicKey, privateKey);
                // look for <config/> element and maybe configure board
                getBoardConfiguration( child, fbobj );
                // maybe restore lastUpdateStartedMillis ( = board update progress)                
                ArrayList ltmp = XMLTools.getChildElementsByTagName(child, "lastUpdateStartedMillis");
                if( ltmp.size() > 0 )
                {
                    Text txtname = (Text) ((Node)ltmp.get(0)).getFirstChild();
                    if( txtname != null )
                    {
                        long millis = -1;
                        try { millis = Long.parseLong( txtname.getData().trim() ); }
                        catch(Exception e) { ; }
                        
                        if( millis > 0 )
                        {
                            fbobj.setLastUpdateStartMillis(millis);
                        }
                    }
                }

                treeFolder.add( fbobj );
            }
            else
            {
                boolean isExpanded = getIsExpandedFromFrostBoardTreeEntry( child );

                // if the child is a folder, add it+maybe expand and dive into it
                FrostBoardObject fbobj = new FrostBoardObject(nodename, true);
                treeFolder.add( fbobj );

                loadProcessFolder( child, fbobj, tree, model ); // dive into folder

                refreshModel( model, fbobj );

                // now expand path if previously expanded
                if( isExpanded == true )
                {
                    tree.expandPath( new TreePath(model.getPathToRoot(fbobj)) );
                }
            }
        }
    }

    protected void getBoardConfiguration( Element element, FrostBoardObject board )
    {
        ArrayList list = XMLTools.getChildElementsByTagName(element, "config");
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

        val = element.getAttribute("hideNAMessages");
        if( val.length() == 0 )
            board.setHideNA( null );
        else
            board.setHideNA( Boolean.valueOf(val) );
    }

    protected void refreshModel(DefaultTreeModel model, FrostBoardObject node)
    {
        // all childs are new, send a nodesWhereInserted to model
        int childIndicies[] = new int[node.getChildCount()];

        for(int x=0; x< node.getChildCount(); x++)
        {
            childIndicies[x] = x;
        }
        model.nodesWereInserted(node, childIndicies);
    }

    protected boolean getIsFolderFromFrostBoardTreeEntry(Element treeEntry)
    {
        String isFolder = treeEntry.getAttribute("isfolder");
        if( isFolder == null || isFolder.toLowerCase().equals("true") == false )
        {
            return false; // default is false
        }
        return true;
    }

    protected boolean getIsExpandedFromFrostBoardTreeEntry(Element treeEntry)
    {
        String isExpanded = treeEntry.getAttribute("isexpanded");
        if( isExpanded == null || isExpanded.toLowerCase().equals("true") == false )
        {
            return false; // default is false
        }
        return true;
    }

    protected String getNameFromFrostBoardTreeEntry(Element treeEntry)
    {
        ArrayList list = XMLTools.getChildElementsByTagName(treeEntry, "name");
        if( list.size() != 1 )
        {
            System.out.println("Error - boards.xml invalid: there must be 1 <name> tag for each entry");
            return null;
        }
        Text txtname = (Text) ((Node)list.get(0)).getFirstChild();
        if( txtname == null )
            return null;
        return txtname.getData().trim();
    }

    protected String getPublicKeyFromFrostBoardTreeEntry(Element treeEntry)
    {
        ArrayList list = XMLTools.getChildElementsByTagName(treeEntry, "publicKey");
        if( list.size() > 1 )
        {
            System.out.println("Error - boards.xml invalid: there should be a maximum of 1 <publicKey> tag for each entry");
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

    protected String getPrivateKeyFromFrostBoardTreeEntry(Element treeEntry)
    {
        ArrayList list = XMLTools.getChildElementsByTagName(treeEntry, "privateKey");
        if( list.size() > 1 )
        {
            System.out.println("Error - boards.xml invalid: there should be a maximum of 1 <privateKey> tag for each entry");
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

    public boolean saveBoardTree(JTree tree, String filename)
    {
        Document doc = XMLTools.createDomDocument();
        if( doc == null )
        {
            System.out.println("Error - saveBoardTree: factory could'nt create XML Document.");
            return false;
        }

        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        FrostBoardObject root = (FrostBoardObject)model.getRoot();

        Element rootElement = doc.createElement("FrostBoardTree");
        doc.appendChild(rootElement);

        Element rootBoardElement = doc.createElement("FrostBoardTreeEntry");
        rootBoardElement.setAttribute("isfolder", "true");
        rootBoardElement.setAttribute("isexpanded", "true");

        Element nameElement = doc.createElement("name");
        Text text = doc.createTextNode( root.toString() );
        nameElement.appendChild( text );

        rootBoardElement.appendChild( nameElement );
        rootElement.appendChild( rootBoardElement );

        // append all childs and subchilds
        saveProcessFolder(rootBoardElement, root, doc, model, tree);

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, filename);
            System.out.println("Board tree saved successfully.");
        } catch(Throwable t)
        {
            System.out.println("Exception - saveBoardTree:");
            t.printStackTrace();
            System.out.println("ERROR saving board tree.");
        }

        return writeOK;
    }

    protected void saveProcessFolder(Element parentElement, FrostBoardObject treeNode, Document doc,
                                     DefaultTreeModel model, JTree tree)
    {
        // parentElement = element to append to
        for(int x=0; x < treeNode.getChildCount(); x++)
        {
            FrostBoardObject boardObject = (FrostBoardObject)treeNode.getChildAt(x);

            if( boardObject.isFolder() == false )
            {
                // its a board
                appendBoardToDomTree(parentElement, boardObject, doc);
            }
            else
            {
                // its a folder
                Element newFolder = appendFolderToDomTree(parentElement, boardObject, doc, model, tree);
                saveProcessFolder(newFolder, boardObject, doc, model, tree);
            }
        }
    }

    protected void appendBoardToDomTree(Element parent, FrostBoardObject board, Document doc)
    {
        Element rootBoardElement = doc.createElement("FrostBoardTreeEntry");
        Element element;
        Text text;
        // <name>
        element = doc.createElement("name");
        text = doc.createTextNode( board.toString() );
        element.appendChild( text );
        rootBoardElement.appendChild( element );
        // pubkey
        if( board.getPublicKey() != null )
        {
            element = doc.createElement("publicKey");
            text = doc.createTextNode( board.getPublicKey() );
            element.appendChild( text );
        }
        rootBoardElement.appendChild( element );
        // privkey
        if( board.getPrivateKey() != null )
        {
            element = doc.createElement("privateKey");
            text = doc.createTextNode( board.getPrivateKey() );
            element.appendChild( text );
        }
        rootBoardElement.appendChild( element );
        // <config />
        if( board.isConfigured() )
        {
            element = doc.createElement("config");

            element.setAttribute("autoUpdate", ""+board.getAutoUpdateEnabled());
            if( board.getMaxMessageDisplayObj() != null )
            {
                element.setAttribute("maxMessageDisplay", ""+board.getMaxMessageDisplay());
            }
            if(board.getShowSignedOnlyObj() != null )
            {
                element.setAttribute("showSignedOnly", ""+board.getShowSignedOnly());
            }
            if( board.getHideBadObj() != null )
            {
                element.setAttribute("hideBadMessages", ""+board.getHideBad());
            }
            if( board.getHideCheckObj() != null )
            {
                element.setAttribute("hideCheckMessages", ""+board.getHideCheck());
            }
            if( board.getHideNAObj() != null )
            {
                element.setAttribute("hideNAMessages", ""+board.getHideNA());
            }
            rootBoardElement.appendChild( element );
        }
        
        // append lastTimeMillisUpdated
        if( board.getLastUpdateStartMillis() > 0 )
        {
            element = doc.createElement("lastUpdateStartedMillis");
            text = doc.createTextNode( "" + board.getLastUpdateStartMillis() );
            element.appendChild( text );
            rootBoardElement.appendChild( element );
        }

        parent.appendChild( rootBoardElement );
    }

    protected Element appendFolderToDomTree(Element parent, FrostBoardObject board, Document doc,
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
        Text text = doc.createTextNode( board.toString() );
        element.appendChild( text );
        rootBoardElement.appendChild( element );
        parent.appendChild( rootBoardElement );
        return rootBoardElement;
    }
}