/*
  TofTreeModel.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>
  Some changes by Stefan Majewski <e9926279@stud3.tuwien.ac.at>

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

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.tree.*;

import frost.gui.*;
import frost.storage.database.applayer.*;

/**
 * This class serves as both the data and selection models of the TofTree.
 */
public class TofTreeModel extends DefaultTreeModel {

    private static Logger logger = Logger.getLogger(TofTreeModel.class.getName());

    private DefaultTreeSelectionModel selectionModel;
    
    private Hashtable<Integer,String> boardnameByPrimaryKey = new Hashtable<Integer,String>();  
    private Hashtable<String,Integer> primaryKeyByBoardname = new Hashtable<String,Integer>();

    /**
     * This method creates a new TofTreeModel with the given TreeNode
     * as its root.
     * @param root TreeNode that will be the root of the new TofTreeModel.
     */
    public TofTreeModel(TreeNode root) {
        super(root);
        selectionModel = new DefaultTreeSelectionModel();
        
        // load all board primary keys
        try {
            Hashtable<String,Integer> boardPrimaryKeysByName = AppLayerDatabase.getBoardDatabaseTable().loadBoards();
            primaryKeyByBoardname = boardPrimaryKeysByName;
            // for reverse lookup
            for( Iterator iter = primaryKeyByBoardname.keySet().iterator(); iter.hasNext(); ) {
                String bname = (String) iter.next();
                Integer bkey = (Integer)primaryKeyByBoardname.get(bname);
                boardnameByPrimaryKey.put(bkey, bname);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Severe error: could not retrieve board primary keys", e);
        }
    }
    
    /**
     * Overwritten to fill the boards in tree with its primary keys
     * after the board tree was loaded from xml file.
     */
    public void initialSetPrimaryKeys() {
        // load boards, create if not existing (should not happen!)
        DefaultMutableTreeNode rootn = (DefaultMutableTreeNode)getRoot(); 
        for(Enumeration e = rootn.depthFirstEnumeration(); e.hasMoreElements(); ) {
            Board b = (Board)e.nextElement();
            if( b.isFolder() == false ) {
                setBoardsPrimaryKey(b);
            }
        }
    }

    /**
     * This method adds a new board to the model.
     * If the last node of the first selected path is a folder,
     * that new board is added as a child of it.
     * If the last node of the first selected path is a board, the new
     * board is added as a child of its parent.
     * If nothing is selected, the new board is added as a child of the root.
     * begins with selected node.
     *
     * @param newNode Board to be added to the model.
     */
    public void addNodeToTree(Board newNode) {
        Board selectedNode = (Board) getSelectedNode();
        if (selectedNode.isFolder() != true) {
            // add to parent of selected node
            selectedNode = (Board) selectedNode.getParent();
        }
        addNodeToTree(newNode, selectedNode);
    }

    /**
     * Adds a new boards to the specified target folder.
     */
    public void addNodeToTree(Board newNode, Board targetFolder) {
        targetFolder.add(newNode);
        
        if( setBoardsPrimaryKey(newNode) == false ) {
            return;
        }
        
        // last in list is the newly added
        int insertedIndex[] = { targetFolder.getChildCount() - 1 };
        nodesWereInserted(targetFolder, insertedIndex);
    }

    /**
     * Removes the node from the board tree.
     * If node is a folder ALL subfolders and boards and messages are deleted too.
     */
    public void removeNode(Board node, boolean removeFromDatabase) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (node != null && parent != null) {
            
            final List<Board> boardsToDelete = new LinkedList<Board>();
            if( removeFromDatabase ) {
                if( node.isFolder() ) {
                    for(Enumeration e = node.breadthFirstEnumeration(); e.hasMoreElements(); ) {
                        Board b = (Board) e.nextElement();
                        if( !b.isFolder() ) {
                            boardsToDelete.add(b);
                        }
                    }
                } else {
                    boardsToDelete.add(node);
                }
            }

            // add to known boards
            KnownBoardsManager.addNewKnownBoards(boardsToDelete);

            // remove from tree
            int[] childIndices = { parent.getIndex(node) };
            Object[] removedChilds = { node };

            node.removeFromParent();

            // FIXME: select next/prev sibling if possible
            TreePath pathToParent = new TreePath(getPathToRoot(parent));
            nodesWereRemoved(parent, childIndices, removedChilds);
            selectionModel.setSelectionPath(pathToParent);
            
            // maybe delete all boards
            if( !boardsToDelete.isEmpty() ) {
                Thread worker = new Thread() {
                    public void run() {
                        for(Iterator it = boardsToDelete.iterator(); it.hasNext(); ) {
                            Board board = (Board) it.next();
                            // remove from lookup tables
                            Integer i = (Integer)primaryKeyByBoardname.remove(board.getNameLowerCase());
                            if( i != null ) {
                                boardnameByPrimaryKey.remove(i);
                            }
                            try {
                                // due to cascade delete this deletes all messages of this board too
                                AppLayerDatabase.getBoardDatabaseTable().removeBoard(board);
                            } catch (SQLException e) {
                                logger.log(Level.SEVERE, "Severe error: could not remove a board", e);
                            }
                        }
                    }
                };
                worker.start();
            }
        }
    }

    /**
     * Returns Vector containing all Boards of the model.
     * @return Vector containing all the Boards of the model.
     */
    public LinkedList<Board> getAllBoards() {
        Board node = (Board) getRoot();
        LinkedList<Board> boards = new LinkedList<Board>();
        Enumeration e = node.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            Board child = (Board) e.nextElement();
            if (child.isFolder() == false) {
                boards.add(child);
            }
        }
        return boards;
    }
    
    /**
     * Called if user changed the days to download backward in options.
     * Resets LastBackloadUpdateFinishedMillis for all boards.
     */
    public void resetLastBackloadUpdateFinishedMillis() {
        Board node = (Board) getRoot();
        Enumeration e = node.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            Board child = (Board) e.nextElement();
            if (child.isFolder() == false) {
                child.setLastBackloadUpdateFinishedMillis(0);
            }
        }
    }

    /**
     * This method looks for a board with the name passed as a parameter. The
     * comparison is not case sensitive. Folders are ignored.
     * @param boardName the name of the board to look for
     * @return the FrostBoardObject if there was a board with that name. Null otherwise.
     */
    public Board getBoardByName(String boardName) {
        Board node = (Board) getRoot();
        Enumeration e = node.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            Board child = (Board) e.nextElement();
            if (child.isFolder() == false &&
                child.getName().compareToIgnoreCase(boardName) == 0) {
                return child;
            }
        }
        return null; // not found
    }
    
    public Board getBoardByPrimaryKey(Integer i) {
        String bname = (String)boardnameByPrimaryKey.get(i);
        if( bname != null ) {
            return (Board)getBoardByName(bname);
        }
        return null;
    }

    /**
     * This method returns the last node of the first selected path.
     * If no path is selected, the root of the model is selected and
     * returned.
     * @return the last node of the first selected path or the root if
     *          nothing was selected.
     */
    public Board getSelectedNode() {
        TreePath selectedPath = selectionModel.getSelectionPath();
        Board node;
        if (selectedPath != null) {
            node = (Board) selectedPath.getLastPathComponent();
        } else {
            // nothing selected? unbelievable ! so select the root ...
            node = (Board) getRoot();
            selectionModel.setSelectionPath(new TreePath(node));
        }
        return node;
    }

    /**
     * This method returns the selection model
     * @return the selection model.
     */
    TreeSelectionModel getSelectionModel() {
        return selectionModel;
    }
    
    /**
     * Retrieve the primary key of the board, or insert it into database.
     */
    private boolean setBoardsPrimaryKey(Board newNode) {
        Integer pk = (Integer)primaryKeyByBoardname.get(newNode.getNameLowerCase());
        if( pk == null ) {
            // add board to db
            try {
                newNode = AppLayerDatabase.getBoardDatabaseTable().addBoard(newNode);
                primaryKeyByBoardname.put(newNode.getNameLowerCase(), newNode.getPrimaryKey());
                boardnameByPrimaryKey.put(newNode.getPrimaryKey(), newNode.getNameLowerCase());
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Severe error: could not add a new board", e);
                return false;
            }
        } else {
            newNode.setPrimaryKey(pk);
        }
        return true;
    }
}
