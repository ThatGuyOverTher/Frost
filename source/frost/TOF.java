/*
  TOF.java / Frost
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
import java.util.*;
import java.io.*;
import javax.swing.event.*; // ListeSelectionEvent
import javax.swing.*; // JTable
import javax.swing.table.*; // DefaultTableModel
import javax.swing.tree.*;

public class TOF {

    private static Hashtable messages;
    private static UpdateTofFilesThread updateThread = null;
    private static UpdateTofFilesThread nextUpdateThread = null;

    /**
     * Gets the content of the message selected in the tofTable.
     * @param e This selectionEv ent is needed to determine if the Table is just being edited
     * @param table The tofTable
     * @param messages A Vector containing all MessageObjects that are just displayed by the table
     * @return The content of the message
     */
    public static VerifyableMessageObject evalSelection(ListSelectionEvent e, JTable table) {
    DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
    if(!e.getValueIsAdjusting() && !table.isEditing())
    {
        int row = table.getSelectedRow();
        if (row != -1 && row < tableModel.getRowCount())
        {
            String index = (String)tableModel.getValueAt(row, 0);
            String date = (String)tableModel.getValueAt(row, 4);
            String from = (String)tableModel.getValueAt(row, 1);

            VerifyableMessageObject message = (VerifyableMessageObject)messages.get(index+date);

            if( message != null )
            {
                boolean newMessage = false;
                // Test if lockfile exists, remove it and
                // update the tree display
                File messageLock = new File( (message.getFile()).getPath() + ".lck");
                if (messageLock.isFile())
                {
                    // this is a new message
                    newMessage = true;
                    messageLock.delete();
                    TofTree tree = frame1.getInstance().getTofTree();
                    ((DefaultTreeModel)tree.getModel()).nodeChanged(
                        (DefaultMutableTreeNode)tree.getLastSelectedPathComponent() );
                }

                // here we reset the bold-look from sender column,
                // wich was set by MessageObject.getRow()
                if (from.indexOf("<font color=\"blue\">") != -1)
                {
                    StringBuffer sbtmp = new StringBuffer();
                    sbtmp.append("<html><font color=\"blue\">");
                    sbtmp.append(message.getFrom());
                    sbtmp.append("</font></html>");
                    tableModel.setValueAt( sbtmp.toString(), row, 1); // Message with attachment
                }
                else
                {
                    tableModel.setValueAt(message.getFrom(), row, 1);
                }
                if( newMessage == true )
                {
                    TofTree tree = frame1.getInstance().getTofTree();
                    String board = tree.getLastSelectedPathComponent().toString();
                    frame1.getInstance().updateMessageCountLabels(true, board);
                    frame1.getInstance().updateTofTree();
                }

                return message;
            }
        }
    }
    return null;
    }

    public static void addNewMessageToTable(File newMsgFile, String board)
    {
        final String targetBoard = board;
        JTable table = frame1.getInstance().getMessageTable();
        final DefaultTableModel tableModel = (DefaultTableModel)table.getModel();

        if( (newMsgFile.getName()).endsWith(".txt") &&
             newMsgFile.length() > 0 &&
             newMsgFile.length() < 32000
          )
        {
            VerifyableMessageObject message = new VerifyableMessageObject(newMsgFile);
            if( message.isValid() && !blocked(message) )
            {
                final String[] sMessage = message.getVRow();
                messages.put( message.getIndex() + sMessage[4], message);

                Hashtable htable = frame1.getInstance().getBoardsThatContainNewMsg();
                if( htable.get(targetBoard) == null )
                {
                    htable.put( targetBoard, targetBoard );
                }
                SwingUtilities.invokeLater( new Runnable() {
                        public void run()
                        {
                            // check if tof table shows this board
                            frame1.getInstance().updateTofTree();
                            if( frame1.getInstance().getLastUsedBoard().equals( targetBoard ) )
                            {
                                tableModel.addRow(sMessage);
                                frame1.getInstance().updateMessageCountLabels();
                            }
                        }
                    });
            }
        }
    }

    /**
     * Clears the tofTable, reads in the messages to be displayed,
     * does check validity for each message and adds the messages to
     * table. Additionaly it returns a Vector with all MessageObjects
     * @param board The selected board.
     * @param keypool Frost keypool directory
     * @param daysToRead Maximum age of the messages to be displayed
     * @param table The tofTable.
     * @return Vector containing all MessageObjects that are displayed in the table.
     */
    public static void updateTofTable(String board, String keypool, int daysToRead)
    {
        // change to not block the swing thread
        JTable table = frame1.getInstance().getMessageTable();

        if( updateThread != null )
        {
            if( updateThread.toString().equals( board ) )
            {
                // already updating
                return;
            }
            else
            {
                // stop actual thread, then start new
                updateThread.cancel();
            }
        }
        // start new thread
        // the thread will set hinmself to updateThread,
        // but first it waits before the actual thread is finished
        nextUpdateThread = new UpdateTofFilesThread(board,keypool,daysToRead,table);
        nextUpdateThread.start();
    }

    static class UpdateTofFilesThread extends Thread
    {
        String targetBoard;
        String keypool;
        int daysToRead;
        JTable table;
        DefaultTableModel tableModel;
        boolean isCancelled = false;
        String fileSeparator = System.getProperty("file.separator");

        public UpdateTofFilesThread(String board, String keypool, int daysToRead, JTable table)
        {
            this.targetBoard = board;
            this.keypool = keypool;
            this.daysToRead = daysToRead;
            this.table = table;
            this.tableModel = (DefaultTableModel)table.getModel();
        }

        public synchronized void cancel()
        {
            isCancelled = true;
        }
        public synchronized boolean isCancel()
        {
            return isCancelled;
        }

        public String toString()
        {
            return targetBoard;
        }

        public void run()
        {
            while( updateThread != null )
            {
                // wait for running thread to finish
                mixed.wait(250);
                if( nextUpdateThread != this )
                {
                    // leave, there is a newer thread than we waiting
                    return;
                }
            }
            // paranoia: are WE the next thread
            if( nextUpdateThread != this )
            {
                return;
            }
            else
            {
                updateThread = this;
            }

            messages = new Hashtable();

            // Clear tofTable
            final String innerTargetBoard = targetBoard;
            SwingUtilities.invokeLater( new Runnable() {
                    public void run()
                    {
                        // check if tof table shows this board
                        if( frame1.getInstance().getLastUsedBoard().equals( innerTargetBoard ) )
                        {
                            TableFun.removeAllRows(table);
                            frame1.getInstance().updateMessageCountLabels();
                        }
                    }
                });

            // Get actual date
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));

////             String nodeText = mixed.makeFilename(targetBoard); needed????
            // Read files
            // download up to maxMessages days to the past
            GregorianCalendar firstDate = new GregorianCalendar();
            firstDate.setTimeZone(TimeZone.getTimeZone("GMT"));
            firstDate.set(Calendar.YEAR, 2001);
            firstDate.set(Calendar.MONTH, 5);
            firstDate.set(Calendar.DATE, 11);
            int msgcount=0;
            int counter = 0;
            targetBoard = mixed.makeFilename(targetBoard);
            while( cal.after(firstDate) && counter < daysToRead )
            {
                String date = DateFun.getDateOfCalendar(cal);
                File loadDir = new File(new StringBuffer().append(keypool).append(targetBoard).append(fileSeparator).append(date).toString());
                if( loadDir.isDirectory() )
                {
                    File[] filePointers = loadDir.listFiles();
                    if( filePointers != null )
                    {
                        String sdate = new StringBuffer().append(date).append("-").append(targetBoard).append("-").toString();
                        for( int j = 0; j < filePointers.length; j++ )
                        {
                            if( filePointers[j].getName().endsWith(".txt.lck") )
                            {
                                // update the node that contains new messages
                                Hashtable boards = frame1.getInstance().getBoardsThatContainNewMsg();
                                if( boards.get(innerTargetBoard) == null )
                                {
                                    boards.put(innerTargetBoard, innerTargetBoard);
                                }
                            }
                            else if( (filePointers[j].getName()).endsWith(".txt") &&
                                 filePointers[j].length() > 0 &&
                                 filePointers[j].length() < 32000 &&
                                 filePointers[j].getName().startsWith(sdate)
                              )
                            {
                                VerifyableMessageObject message = new VerifyableMessageObject(filePointers[j]);
                                if( message.isValid() && !blocked(message) )
                                {
                                    msgcount++;
                                    final String[] sMessage = message.getVRow();
                                    messages.put( message.getIndex() + sMessage[4], message);
                                    // also update labels each 10 messages (or at end, see below)
                                    boolean updateMessagesCountLabels2 = false;
                                    if(msgcount > 9 && msgcount%10==0)
                                    {
                                        updateMessagesCountLabels2 = true;
                                    }
                                    final boolean updateMessagesCountLabels = updateMessagesCountLabels2;
                                    SwingUtilities.invokeLater( new Runnable() {
                                        public void run()
                                        {
                                            // check if tof table shows this board
                                            if( frame1.getInstance().getLastUsedBoard().equals( innerTargetBoard ) )
                                            {
                                                tableModel.addRow(sMessage);
                                                if(updateMessagesCountLabels)
                                                {
                                                    frame1.getInstance().updateMessageCountLabels();
                                                    frame1.getInstance().updateTofTree();
                                                }
                                            }
                                        }
                                        });
                                }
                            }
                            if( isCancel() )
                            {
                                updateThread = null;
                                return;
                            }
                        }
                    }
                }
                if( isCancel() )
                {
                    updateThread = null;
                    return;
                }
                counter++;
                cal.add(Calendar.DATE, -1);
            }
//            final String innerTargetBoard = targetBoard;
            SwingUtilities.invokeLater( new Runnable() {
                    public void run()
                    {
                        frame1.getInstance().updateTofTree();
                        if( frame1.getInstance().getLastUsedBoard().equals( innerTargetBoard ) )
                        {
                            frame1.getInstance().updateMessageCountLabels();
                            frame1.getInstance().updateTofTree();
                        }
                    }
                });
            updateThread = null;
        }
    }

    /**
     * Clears the tofTable, reads in the messages to be displayed,
     * does check validity for each message and adds the messages to
     * table. Additionaly it returns a Vector with all MessageObjects
     * @param board The selected board.
     * @param keypool Frost keypool directory
     * @param daysToRead Maximum age of the messages to be displayed
     * @param table The tofTable.
     * @return Vector containing all MessageObjects that are displayed in the table.
     */
/*    public static Vector updateAncasyTable(String selectedBoard, JList list, String keypool, int daysToRead, JTable table) {
    System.out.println("updateAncasytable");
    DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
    DefaultListModel listModel = (DefaultListModel)list.getModel();
    Vector result = new Vector();
    String fileSeparator = System.getProperty("file.separator");

    // Clear tofTable
    TableFun.removeAllRows(table);

    // Get actual date
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    String date = formatDate(cal);


    // Read files
    for (int i = 0; i < daysToRead; i++) {
        for (int k = 0; k < listModel.size() + 1; k++) {
        String board = "ancasy_outbox";
        if (k < listModel.size())
            board = "ancasy_" + mixed.makeFilename((String)listModel.elementAt(k));

        System.out.println("Reading ancasy files for: " + board);
        File loadDir = new File(keypool + board + fileSeparator + date);
        System.out.println(loadDir);
        if (loadDir.isDirectory()) {
            File[] filePointers = loadDir.listFiles();
            if (filePointers != null) {
            for (int j = 0; j < filePointers.length; j++) {
                System.out.println(filePointers[j]);
                if ((filePointers[j].getName()).endsWith(".txt") &&
                filePointers[j].length() > 0 &&
                filePointers[j].length() < 32000 &&
                filePointers[j].getName().startsWith(date)) {
                VerifyableMessageObject message = new VerifyableMessageObject(filePointers[j]);
                if (message.isValid() && !blocked(message) && message.getBoard().equals(selectedBoard)) {
                    result.add(message);
                    tableModel.addRow(message.getVRow());
                }
                }
            }
            }
        }
        }
        cal.add(Calendar.DATE, -1);
        date = formatDate(cal);
    }

    // Sort tofTable by date
    //TableSorter.sortByColumn(table, 3, true);

    return result;
    }*/

    /**
     * Returns true if the message should not be displayed
     * @param message The message object to check
     * @return true if message is blocked, else false
     */
    public static boolean blocked(VerifyableMessageObject message) {
    String header = (message.getFrom() + message.getSubject() + message.getDate() + message.getTime()).toLowerCase();
    int index = frame1.frostSettings.getValue("blockMessage").indexOf(";");
    int pos = 0;

    if (frame1.frostSettings.getBoolValue("signedOnly") && !message.isVerifyable) return true;
    if (frame1.frostSettings.getBoolValue("signedOnly") && frame1.frostSettings.getBoolValue("goodOnly") &&
    (message.getStatus().indexOf("GOOD")==-1)) return true;

    if (frame1.frostSettings.getBoolValue("blockMessageChecked")) {
    while (index != -1) {
        String block = (frame1.frostSettings.getValue("blockMessage").substring(pos, index)).trim();
        if (header.indexOf(block) != -1 && block.length() > 0)
        return true;
        //      System.out.println("'" + block + "'");
        pos = index + 1;
        index = frame1.frostSettings.getValue("blockMessage").indexOf(";", pos);
    }
    if (!frame1.frostSettings.getValue("blockMessage").endsWith(";")) {
        index =  frame1.frostSettings.getValue("blockMessage").lastIndexOf(";");
        if (index == -1)
        index = 0;
        else
        index++;
        String block = (frame1.frostSettings.getValue("blockMessage").substring(index, frame1.frostSettings.getValue("blockMessage").length())).trim();
        if (header.indexOf(block) != -1 && block.length() > 0)
        return true;
        //      System.out.println("'" + block + "'");
    }
    }

    //same with body
    if (frame1.frostSettings.getBoolValue("blockMessageBodyChecked")) {
    while (index != -1) {
        String block = (frame1.frostSettings.getValue("blockMessageBody").substring(pos, index)).trim();
        if (message.getContent().toLowerCase().indexOf(block) != -1 && block.length() > 0)
        return true;
        //      System.out.println("'" + block + "'");
        pos = index + 1;
        index = frame1.frostSettings.getValue("blockMessageBody").indexOf(";", pos);
    }
    if (!frame1.frostSettings.getValue("blockMessageBody").endsWith(";")) {
        index =  frame1.frostSettings.getValue("blockMessageBody").lastIndexOf(";");
        if (index == -1)
        index = 0;
        else
        index++;
        String block = (frame1.frostSettings.getValue("blockMessageBody").substring(index, frame1.frostSettings.getValue("blockMessageBody").length())).trim();
        if (message.getContent().toLowerCase().indexOf(block) != -1 && block.length() > 0)
        return true;
        //      System.out.println("'" + block + "'");
    }
    }
    return false;
    }

    public static void initialSearchNewMessages(JTree tree, int daysToRead)
    {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        Enumeration e = ((DefaultMutableTreeNode)model.getRoot()).depthFirstEnumeration();
        String keypool = frame1.keypool;
        while( e.hasMoreElements() )
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
            if( node.isLeaf() == false )
                continue;

            final String targetBoard = mixed.makeFilename(node.toString());
            final String fileSeparator = System.getProperty("file.separator");

            // Get actual date
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        ////             String nodeText = mixed.makeFilename(targetBoard);
            // Read files
            // download up to maxMessages days to the past
            GregorianCalendar firstDate = new GregorianCalendar();
            firstDate.setTimeZone(TimeZone.getTimeZone("GMT"));
            firstDate.set(Calendar.YEAR, 2001);
            firstDate.set(Calendar.MONTH, 5);
            firstDate.set(Calendar.DATE, 11);
            int msgcount=0;
            int counter = 0;

            while( cal.after(firstDate) && counter < daysToRead )
            {
                String date = DateFun.getDateOfCalendar(cal);
                File loadDir = new File(new StringBuffer().append(keypool).append(targetBoard).append(fileSeparator).append(date).toString());
                if( loadDir.isDirectory() )
                {
                    File[] filePointers = loadDir.listFiles();
                    if( filePointers != null )
                    {
                        String sdate = new StringBuffer().append(date).append("-").append(targetBoard).append("-").toString();
                        for( int j = 0; j < filePointers.length; j++ )
                        {
                            if( filePointers[j].getName().endsWith(".txt.lck") )
                            {
                                // update the node that contains new messages
                                Hashtable boards = frame1.getInstance().getBoardsThatContainNewMsg();
                                if( boards.get(targetBoard) == null )
                                {
                                    boards.put(targetBoard, targetBoard);
                                }
                                SwingUtilities.invokeLater( new Runnable() {
                                       public void run()
                                       {
                                           frame1.getInstance().updateTofTree();
                                       }
                                   });
                                break; // process next board
                            }
                        }
                    }
                }
                counter++;
                cal.add(Calendar.DATE, -1);
            }
        }
    }


}
