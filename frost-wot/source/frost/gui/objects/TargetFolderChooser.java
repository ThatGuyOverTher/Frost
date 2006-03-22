/*
  TargetFolderChooser.java / Frost
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
package frost.gui.objects;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import frost.boards.*;
import javax.swing.JScrollPane;

/**
 * This class let the user choose a folder from the folders in tofTree.
 */
public class TargetFolderChooser extends JDialog {

    private JPanel jContentPane = null;
    private JPanel buttonsPanel = null;
    private JTree folderTree = null;
    private JButton okButton = null;
    private JButton cancelButton = null;

    private DefaultTreeModel treeModel;

    private Board choosedFolder = null;
    private JScrollPane jScrollPane = null;

    /**
     * This is the default constructor
     */
    public TargetFolderChooser(TofTreeModel origModel) {
        super();
        MyTreeNode rootNode = buildTree(origModel);
        treeModel = new DefaultTreeModel(rootNode);
        initialize();
    }

    /**
     * Build a new tree which contains all folders of the TofTree.
     */
    private MyTreeNode buildTree(TofTreeModel origModel) {
        MyTreeNode rootNode = new MyTreeNode((Board)origModel.getRoot());

        addNodesRecursiv(rootNode, (DefaultMutableTreeNode)origModel.getRoot());

        return rootNode;
    }

    private void addNodesRecursiv(MyTreeNode addNode, DefaultMutableTreeNode origNode) {

        for(int x=0; x<origNode.getChildCount(); x++) {
            Board b = (Board)origNode.getChildAt(x);
            if( b.isFolder() ) {
                MyTreeNode newNode = new MyTreeNode(b);
                addNode.add(newNode);
                addNodesRecursiv(newNode, b);
            }
        }
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        int dlgSizeX = 350;
        int dlgSizeY = 400;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width-dlgSizeX)/2;
        int y = (screen.height-dlgSizeY)/2;
        setBounds(x,y,dlgSizeX,dlgSizeY);

        this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setModal(true);
        this.setTitle("Choose a target folder");
        this.setContentPane(getJContentPane());
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelButtonPressed();
            }
        });
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if( jContentPane == null ) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
            jContentPane.add(getButtonsPanel(), java.awt.BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    /**
     * This method initializes buttonsPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getButtonsPanel() {
        if( buttonsPanel == null ) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
            buttonsPanel = new JPanel();
            buttonsPanel.setLayout(flowLayout);
            buttonsPanel.add(getOkButton(), null);
            buttonsPanel.add(getCancelButton(), null);
        }
        return buttonsPanel;
    }

    /**
     * This method initializes folderTree
     *
     * @return javax.swing.JTree
     */
    private JTree getFolderTree() {
        if( folderTree == null ) {
            folderTree = new JTree(treeModel);
            folderTree.setCellRenderer(new CellRenderer());
            folderTree.setSelectionRow(0);
        }
        return folderTree;
    }

    /**
     * This method initializes okButton
     *
     * @return javax.swing.JButton
     */
    private JButton getOkButton() {
        if( okButton == null ) {
            okButton = new JButton();
            okButton.setText("Ok");
            okButton.setSelected(false);
            okButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    okButtonPressed();
                }
            });
        }
        return okButton;
    }

    /**
     * This method initializes cancelButton
     *
     * @return javax.swing.JButton
     */
    private JButton getCancelButton() {
        if( cancelButton == null ) {
            cancelButton = new JButton();
            cancelButton.setText("Cancel");
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    cancelButtonPressed();
                }
            });
        }
        return cancelButton;
    }

    private void okButtonPressed() {
        choosedFolder = ((MyTreeNode)getFolderTree().getSelectionPath().getLastPathComponent()).getFolder();
        setVisible(false);
    }

    private void cancelButtonPressed() {
        choosedFolder = null;
        setVisible(false);
    }

    public Board startDialog() {

        setVisible(true);
        return choosedFolder;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if( jScrollPane == null ) {
            jScrollPane = new JScrollPane();
            jScrollPane.setBackground(java.awt.Color.white);
            jScrollPane.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createEmptyBorder(2,2,2,2),
                    javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED)));
            jScrollPane.setViewportView(getFolderTree());
        }
        return jScrollPane;
    }

    /**
     * Simple renderer to set a nice icon for each folder.
     */
    private class CellRenderer extends DefaultTreeCellRenderer {

        ImageIcon boardIcon;

        public CellRenderer() {
            boardIcon = new ImageIcon(getClass().getResource("/data/open.gif"));
        }

        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            setIcon(boardIcon);
            return this;
        }
    }

    /**
     * A simple treenode implementation that holds a Board and returns its name as toString()
     */
    private class MyTreeNode extends DefaultMutableTreeNode {
        Board folder;
        public MyTreeNode(Object usrObj) {
            super(usrObj);
            folder = (Board)usrObj;
        }
        public String toString() {
            return folder.getName();
        }
        public Board getFolder() {
            return folder;
        }
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
