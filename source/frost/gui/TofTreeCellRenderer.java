/*
TofTreeCellRenderer.java / Frost
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

package frost.gui;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import frost.*;
import frost.gui.objects.*;

public class TofTreeCellRenderer extends DefaultTreeCellRenderer
{
    ImageIcon writeAccessIcon;
    ImageIcon writeAccessNewIcon;
    ImageIcon readAccessIcon;
    ImageIcon readAccessNewIcon;
    ImageIcon boardIcon;
    ImageIcon boardNewIcon;
    ImageIcon boardSpammedIcon;
    String fileSeparator;
    Color notUpdatingSelectedColor;
    Color notUpdatingNonSelectedColor;

    Font boldFont = null;
    Font normalFont = null;

    public TofTreeCellRenderer()
    {
        fileSeparator = System.getProperty("file.separator");
        boardIcon = new ImageIcon(frame1.class.getResource("/data/board.gif"));
        boardNewIcon = new ImageIcon(frame1.class.getResource("/data/boardnew.gif"));
        boardSpammedIcon = new ImageIcon(frame1.class.getResource("/data/boardspam.gif"));
        writeAccessIcon = new ImageIcon(frame1.class.getResource("/data/waboard.jpg"));
        writeAccessNewIcon = new ImageIcon(frame1.class.getResource("/data/waboardnew.jpg"));
        readAccessIcon = new ImageIcon(frame1.class.getResource("/data/raboard.jpg"));
        readAccessNewIcon = new ImageIcon(frame1.class.getResource("/data/raboardnew.jpg"));
        this.setLeafIcon(new ImageIcon(frame1.class.getResource("/data/board.gif")));
        this.setClosedIcon(new ImageIcon(frame1.class.getResource("/data/closed.gif")));
        this.setOpenIcon(new ImageIcon(frame1.class.getResource("/data/open.gif")));

        JTable dummyTable = new JTable();
        normalFont = dummyTable.getFont();
        boldFont = normalFont.deriveFont( Font.BOLD );

        notUpdatingSelectedColor = dummyTable.getSelectionBackground();
        notUpdatingNonSelectedColor = Color.WHITE;
    }

    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        FrostBoardObject board = null;
        if( value instanceof FrostBoardObject )
        {
            board = (FrostBoardObject)value;
        }
        else
        {
            System.out.println("Error - TofTreeCellRenderer: got a tree value wich is no FrostBoardObject:");
            System.out.println("   node value='"+value+"'  ;  node class='"+value.getClass()+"'");
            System.out.println("This should never happen, please report the error.");
            return this;
        }

        boolean containsNewMessage = board.containsNewMessage();

        if( board.isFolder() )
        {
            // if this is a folder, check board for new messages
            int childs = board.getChildCount();
            boolean newMessage = false;

            for(int c=0; c<childs; c++)
            {
                FrostBoardObject childBoard = (FrostBoardObject)board.getChildAt(c);
                if( childBoard.containsNewMessage() )
                {
                    newMessage = true;
                    break;
                }
            }
            if( newMessage == true )
            {
                setFont( boldFont );
            }
            else
            {
                setFont( normalFont );
            }
        }
        else
        {
            // set the sdpecial text (board name + if new msg. a ' (2)' is appended and bold)
            setText( board.getVisibleText() );
            if( containsNewMessage )
            {
                setFont( boldFont );
            }
            else
            {
                setFont( normalFont );
            }
        }

        // maybe update visualization
        if( frame1.frostSettings.getBoolValue("boardUpdateVisualization") &&
            board.isUpdating() == true )
        {
            // set special updating colors
            Color c;
            c = (Color)frame1.frostSettings.getObjectValue("boardUpdatingNonSelectedBackgroundColor");
            setBackgroundNonSelectionColor( c );

            c = (Color)frame1.frostSettings.getObjectValue("boardUpdatingSelectedBackgroundColor");
            setBackgroundSelectionColor( c );
        }
        else
        {
            // set normal colors
            setBackgroundNonSelectionColor( notUpdatingNonSelectedColor );
            setBackgroundSelectionColor( notUpdatingSelectedColor );
        }

        // set the icon
        if( leaf == true )
        {
            if( board.isPublicBoard() )
            {
                if( containsNewMessage )
                {
                    setIcon(boardNewIcon);
                }
                else
                {
                    setIcon(boardIcon);
                }
            }
            else if( board.isSpammed() )
            {
                setIcon(boardSpammedIcon);
            }
            else if( board.isWriteAccessBoard() )
            {
                if( containsNewMessage )
                {
                    setIcon(writeAccessNewIcon);
                }
                else
                {
                    setIcon(writeAccessIcon);
                }
            }
            else if( board.isReadAccessBoard() )
            {
                if( containsNewMessage )
                {
                    setIcon(readAccessNewIcon);
                }
                else
                {
                    setIcon(readAccessIcon);
                }
            }
        }
        return this;
    }
}
