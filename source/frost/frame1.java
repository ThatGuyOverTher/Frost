/*
  frame1.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>
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

package frost;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;

import frost.FcpTools.*;
import frost.components.BrowserFrame;
import frost.crypt.*;
import frost.ext.Execute;
import frost.gui.*;
import frost.gui.model.*;
import frost.gui.objects.*;
import frost.identities.*;
import frost.threads.*;

//++++ TODO: rework identities stuff + save to xml
//             - save identities together (not separated friends,enemies)
//             - each identity have 3 states: GOOD, BAD, NEUTRAL
//             - filter out enemies on read of messages

// after removing a board, let actual board selected (currently if you delete another than selected board
//   the tofTree is updates)

public class frame1 extends JFrame implements ClipboardOwner
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");

    private RunningBoardUpdateThreads runningBoardUpdateThreads = null;

    FrostBoardObject clipboard = null;
    long counter = 55;

    private static frame1 instance = null; // set in constructor
    boolean started = false;

    private boolean freenetIsOnline = false;
    private boolean freenetIsTransient = false;

    public static String fileSeparator = System.getProperty("file.separator");
    // "keypool.dir" is the corresponding key in frostSettings, is set in defaults of SettingsClass.java
    // this is the new way to access this value :)
    public static String keypool = null;
    public static String newMessageHeader = new String("");
    public static String oldMessageHeader = new String("");
    public static int activeUploadThreads = 0;
    public static int activeDownloadThreads = 0;
    private String lastSelectedMessage;

    public static FrostMessageObject selectedMessage = new FrostMessageObject();
    private static boolean isGeneratingCHK = false;

    public static volatile Object threadCountLock = new Object();

    //the identity stuff.  This really shouldn't be here but where else?
    public static ObjectInputStream id_reader;
    public ObjectOutputStream id_writer;
    public static LocalIdentity mySelf;
    public static BuddyList friends,enemies;
    public static Hashtable goodIds,badIds,myBatches;
    public static crypt crypto;

    // saved to frost.ini
    public static SettingsClass frostSettings = null;
    public static AltEdit altEdit;

    javax.swing.Timer timer; // Uploads / Downloads
    java.util.Timer timer2;

    //a shutdown hook
    public Thread saver;
    private static CleanUp fileCleaner = new CleanUp("keypool",false);

    //------------------------------------------------------------------------
    // Generate objects
    //------------------------------------------------------------------------

    // buttons that are enabled/disabled later
    JButton pasteBoardButton = null;
    JButton configBoardButton = null;
    JButton removeBoardButton = null;
    JButton cutBoardButton = null;
    JButton renameBoardButton = null;

    JButton tofNewMessageButton = null;
    JButton tofReplyButton = null;
    JButton downloadAttachmentsButton= null;
    JButton downloadBoardsButton= null;
    JButton saveMessageButton= null;
    JButton trustButton= null;
    JButton notTrustButton= null;
    JButton checkTrustButton = null;
    JButton tofUpdateButton = null;

    JButton uploadAddFilesButton = null;
    JButton searchButton = null;

    // labels that are updated later
    JLabel statusLabel = null;
    JLabel statusMessageLabel = null;
    static ImageIcon[] newMessage = new ImageIcon[2];
    JLabel timeLabel = null;

    JCheckBox searchAllBoardsCheckBox = null;
    JCheckBox downloadActivateCheckBox = null;
    JCheckBoxMenuItem tofAutomaticUpdateMenuItem = null;

    JComboBox searchComboBox = null;

    JTabbedPane tabbedPane = null;

    JSplitPane attachmentSplitPane = null;
    JSplitPane boardSplitPane = null;

    final String allMessagesCountPrefix = "Msg: ";
    final String newMessagesCountPrefix = "New: ";
    JLabel allMessagesCountLabel = new JLabel(allMessagesCountPrefix+"0");
    JLabel newMessagesCountLabel = new JLabel(newMessagesCountPrefix+"0");

    JLabel downloadItemCountLabel = new JLabel();

    private String searchResultsCountPrefix = null;
    JLabel searchResultsCountLabel = new JLabel();

    TofTree tofTree = null;

    private UploadTable uploadTable = null;
    private MessageTable messageTable = null;
    private SearchTable searchTable = null;
    private DownloadTable downloadTable = null;
    private JTable attachmentTable = null;
    private JTable boardTable = null;

    private JTextArea tofTextArea = null;
    private JTextField downloadTextField = null;
    private JTextField searchTextField = null;

//    JCheckBox uploadActivateCheckBox = new JCheckBox(new ImageIcon(frame1.class.getResource("/data/up.gif")), true);
//    JCheckBox reducedBlockCheckCheckBox= new JCheckBox();

    // all popupmenu items
    JMenuItem searchPopupDownloadSelectedKeys = null;
    JMenuItem searchPopupDownloadAllKeys = null;
    JMenuItem searchPopupCopyAttachment = null;
    JMenuItem searchPopupSetGood = null;
    JMenuItem searchPopupSetBad = null;
    JMenuItem searchPopupCancel = null;

    JMenuItem uploadPopupRemoveSelectedFiles = null;
    JMenuItem uploadPopupRemoveAllFiles = null;
    JMenuItem uploadPopupReloadSelectedFiles = null;
    JMenuItem uploadPopupReloadAllFiles = null;
    JMenuItem uploadPopupSetPrefixForSelectedFiles = null;
    JMenuItem uploadPopupSetPrefixForAllFiles = null;
    JMenuItem uploadPopupRestoreDefaultFilenamesForSelectedFiles = null;
    JMenuItem uploadPopupRestoreDefaultFilenamesForAllFiles = null;
    JMenu uploadPopupChangeDestinationBoard = null;
    JMenuItem uploadPopupAddFilesToBoard = null;
    JMenuItem uploadPopupCancel = null;

    JMenuItem downloadPopupRestartSelectedDownloads = null;
    JMenuItem downloadPopupRemoveSelectedDownloads = null;
    JMenuItem downloadPopupRemoveAllDownloads = null;
    JMenuItem downloadPopupRemoveFinished = null;
    JMenuItem downloadPopupCancel = null;
    JMenuItem downloadPopupEnableAllDownloads = null;
    JMenuItem downloadPopupDisableAllDownloads = null;
    JMenuItem downloadPopupEnableSelectedDownloads = null;
    JMenuItem downloadPopupDisableSelectedDownloads = null;
    JMenuItem downloadPopupInvertEnabledAll = null;
    JMenuItem downloadPopupInvertEnabledSelected = null;

    JMenuItem tofTextPopupSaveMessage = null;
    JMenuItem tofTextPopupSaveAttachments = null;
    JMenuItem tofTextPopupSaveAttachment = null;
    JMenuItem tofTextPopupSaveBoards = null;
    JMenuItem tofTextPopupSaveBoard = null;
    JMenuItem tofTextPopupCancel = null;

    JMenuItem msgTablePopupMarkMessageUnread = null;

    //------------------------------------------------------------------------
    // end-of: Generate objects
    //------------------------------------------------------------------------

    // returns the current id,crypt, etc.
    public static LocalIdentity getMyId() {return mySelf;}
    public static BuddyList getFriends() {return friends;}
    public static Hashtable getGoodIds() {return goodIds;}
    public static Hashtable getMyBatches() {return myBatches;}
    public static Hashtable getBadIds() {return badIds;}
    public static crypt getCrypto() {return crypto;}
    public static BuddyList getEnemies() {return enemies;}
    //------------------------------------------------------------------------

    /*************************
     * GETTER + SETTER       *
     *************************/
    public static frame1    getInstance() { return instance; }
    public UploadTable      getUploadTable() { return uploadTable; }
    public MessageTable     getMessageTable() { return messageTable; }
    public SearchTable      getSearchTable() { return searchTable; }
    public DownloadTable    getDownloadTable() { return downloadTable; }
    public JTable           getAttachmentTable() { return attachmentTable; }
    public JTable           getAttachedBoardsTable() { return boardTable; }
    public String           getTofTextAreaText() { return tofTextArea.getText(); }
    public void             setTofTextAreaText(String txt) { tofTextArea.setText(txt); }
    public TofTree          getTofTree() { return tofTree; }
    public JButton          getSearchButton() { return searchButton; }
    public RunningBoardUpdateThreads getRunningBoardUpdateThreads() {
        return runningBoardUpdateThreads;
    }

    public static boolean isGeneratingCHK() { return isGeneratingCHK; }
    public static void setGeneratingCHK( boolean val ) { isGeneratingCHK = val; }

    public boolean isFreenetOnline() { return freenetIsOnline; }
    public void setFreenetIsOnline( boolean val ) { freenetIsOnline = val; }

    public boolean isFreenetTransient() { return freenetIsTransient; }
    public void setFreenetIsTransient( boolean val ) { freenetIsTransient = val; }

    public FrostBoardObject getSelectedNode()
    {
        FrostBoardObject node = (FrostBoardObject)getTofTree().getLastSelectedPathComponent();
        if( node == null )
        {
            // nothing selected? unbelievable ! so select the root ...
            getTofTree().setSelectionRow(0);
            node = (FrostBoardObject)getTofTree().getModel().getRoot();
        }
        return node;
    }

    /**Construct the frame*/
    public frame1()
    {
        instance = this;
        frostSettings = new SettingsClass();
        keypool = frostSettings.getValue("keypool.dir");

        runningBoardUpdateThreads = new RunningBoardUpdateThreads();

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();

            saver = new Thread() {
                public void run() {
                    System.out.println("saving identities");
                    File identities = new File("identities");
                    if( identities.exists() )
                    {
                        String bakFilename = "identities.bak";
                        File bakFile = new File(bakFilename);
                        bakFile.delete();
                        identities.renameTo(bakFile);
                        identities = new File("identities");
                    }

                    try
                    { //TODO: replace this with a call to XML serializer
                        FileWriter fout = new FileWriter(identities);
                        fout.write(mySelf.getName() + "\n");
                        fout.write(mySelf.getKeyAddress() + "\n");
                        fout.write(mySelf.getKey() + "\n");
                        fout.write(mySelf.getPrivKey() + "\n");

                        //now do the friends
                        fout.write("*****************\n");
                        Iterator i = friends.values().iterator();
                        while( i.hasNext() )
                        {
                            Identity cur = (Identity)i.next();
                            fout.write(cur.getName() + "\n");
                            fout.write(cur.getKeyAddress() + "\n");
                            fout.write(cur.getKey() + "\n");
                        }
                        fout.write("*****************\n");
			i = goodIds.values().iterator();
			while (i.hasNext()) {
				fout.write((String)i.next() + "\n");
			}
			fout.write("*****************\n");
                        i = enemies.values().iterator();
                        while( i.hasNext() )
                        {
                            Identity cur = (Identity)i.next();
                            fout.write(cur.getName() + "\n");
                            fout.write(cur.getKeyAddress() + "\n");
                            fout.write(cur.getKey() + "\n");
                        }
                        fout.write("*****************\n");
			i = badIds.values().iterator();
			while (i.hasNext()) {
				fout.write((String)i.next() + "\n");
			}
			fout.write("*****************\n");
                        fout.close();
                        System.out.println("identities saved successfully.");

                    }
                    catch( IOException e )
                    {
                        System.out.println("ERROR: couldn't save identities:");
                        e.printStackTrace();
                    }
		    try {
		    	StringBuffer buf = new StringBuffer();
			Iterator i = myBatches.keySet().iterator();
			while (i.hasNext()) 
				buf.append((String)i.next()).append("_");
			if (buf.length() > 0)
				buf.deleteCharAt(buf.length()-1); //remove the _ at the end
			File batches = new File("batches");
			FileAccess.writeFile(buf.toString(),batches);
			
		    } catch (Throwable t) {
		    	t.printStackTrace();
		    }
                    saveOnExit();
                    FileAccess.cleanKeypool(keypool);
                }
                };
            Runtime.getRuntime().addShutdownHook(saver);

            TimerTask cleaner = new TimerTask() {
                int i = 0;
                public void run() {
                    // maybe each 6 hours cleanup files (12 * 30 minutes)
                    if( i==12 && frostSettings.getBoolValue("doCleanUp") )
                    {
                        i=0;
                        System.out.println("discarding old files");
                        fileCleaner.doCleanup();
                    }
                    System.out.println("freeing memory");
                    System.gc();
                    i++;
                }
            };
            timer2.schedule(cleaner,30*60*1000,30*60*1000); // all 30 minutes

            TimerTask autoSaver = new TimerTask() {
                public void run()
                {
                    getTofTree().saveTree();
                    getDownloadTable().save();
                    getUploadTable().save();
                }
            };
            int autoSaveIntervalMinutes = frostSettings.getIntValue("autoSaveInterval");
            timer2.schedule(autoSaver,
                            autoSaveIntervalMinutes*60*1000,
                            autoSaveIntervalMinutes*60*1000);
        }
        catch( Exception e ) { e.printStackTrace(); }
    }

    /**
     * Configures a button to be a default icon button
     * @param button The new icon button
     * @param toolTipText Is displayed when the mousepointer is some seconds over a button
     * @param rolloverIcon Displayed when mouse is over button
     */
    protected void configureButton(JButton button, String toolTipText, String rolloverIcon)
    {
        String text = null;
        try {
            text = LangRes.getString(toolTipText);
        } catch(MissingResourceException ex)
        {
            text = toolTipText; // better than nothing ;)
        }
        button.setToolTipText(text);

        button.setRolloverIcon(new ImageIcon(frame1.class.getResource(rolloverIcon)));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }
    /**
     * Configures a CheckBox to be a default icon CheckBox
     * @param checkBox The new icon CheckBox
     * @param toolTipText Is displayed when the mousepointer is some seconds over the CheckBox
     * @param rolloverIcon Displayed when mouse is over the CheckBox
     * @param selectedIcon Displayed when CheckBox is checked
     * @param rolloverSelectedIcon Displayed when mouse is over the selected CheckBox
     */
    public void configureCheckBox(JCheckBox checkBox, String toolTipText, String rolloverIcon,
                                    String selectedIcon, String rolloverSelectedIcon)
    {
        checkBox.setToolTipText(LangRes.getString(toolTipText));
        checkBox.setRolloverIcon(new ImageIcon(frame1.class.getResource(rolloverIcon)));
        checkBox.setSelectedIcon(new ImageIcon(frame1.class.getResource(selectedIcon)));
        checkBox.setRolloverSelectedIcon(new ImageIcon(frame1.class.getResource(rolloverSelectedIcon)));
        checkBox.setMargin(new Insets(0, 0, 0, 0));
        checkBox.setFocusPainted(false);
    }

    private JToolBar buildButtonPanel()
    {
        timeLabel = new JLabel("");
// configure buttons
        this.pasteBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/paste.gif")));
        this.configBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/configure.gif")));

        JButton newBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/newboard.gif")));
        JButton newFolderButton = new JButton(new ImageIcon(frame1.class.getResource("/data/newfolder.gif")));
        removeBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/remove.gif")));
        renameBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/rename.gif")));
        cutBoardButton = new JButton(new ImageIcon(frame1.class.getResource("/data/cut.gif")));
        JButton boardInfoButton= new JButton(new ImageIcon(frame1.class.getResource("/data/info.gif")));
        JButton systemTrayButton= new JButton(new ImageIcon(frame1.class.getResource("/data/tray.gif")));
        configureButton(newBoardButton, "New board", "/data/newboard_rollover.gif");
        configureButton(newFolderButton, "New folder", "/data/newfolder_rollover.gif");
        configureButton(removeBoardButton, "Remove board", "/data/remove_rollover.gif");
        configureButton(renameBoardButton, "Rename folder", "/data/rename_rollover.gif");
        configureButton(configBoardButton, "Configure board", "/data/configure_rollover.gif");
        configureButton(cutBoardButton, "Cut board", "/data/cut_rollover.gif");
        configureButton(pasteBoardButton, "Paste board", "/data/paste_rollover.gif");
        configureButton(boardInfoButton, "Board Information Window", "/data/info_rollover.gif");
        configureButton(systemTrayButton, "Minimize to System Tray", "/data/tray_rollover.gif");

// add action listener
        newBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getTofTree().createNewBoard(frame1.getInstance());
            } });
        newFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getTofTree().createNewFolder(frame1.getInstance());
            } });
        renameBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renameNode(getSelectedNode());
            } });
        removeBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeNode(getSelectedNode());
            } });
        cutBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cutNode(getSelectedNode());
            } });
        pasteBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pasteFromClipboard(getSelectedNode());
            } });
        configBoardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tofConfigureBoardMenuItem_actionPerformed(e, getSelectedNode());
            } });
        systemTrayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
            try { // Hide the Frost window
                Process process = Runtime.getRuntime().exec("exec" + fileSeparator + "SystemTrayHide.exe");
            }catch(IOException _IoExc) { }
            } });
        boardInfoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tofDisplayBoardInfoMenuItem_actionPerformed(e);
            } });
// build panel
        JToolBar buttonPanel = new JToolBar();
        buttonPanel.setRollover( true );
        buttonPanel.setFloatable( false );
        Dimension blankSpace = new Dimension(3,3);

        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.add(newBoardButton);
        buttonPanel.add(newFolderButton);
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.addSeparator();
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.add(configBoardButton);
        buttonPanel.add(renameBoardButton);
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.addSeparator();
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.add(cutBoardButton);
        buttonPanel.add(pasteBoardButton);
        buttonPanel.add(removeBoardButton);
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.addSeparator();
        buttonPanel.add( Box.createRigidArea( blankSpace ) );
        buttonPanel.add(boardInfoButton);
        // The System Tray Icon does only work on Windows machines.
        // It uses the Visual Basic files (compiled ones) in the data directory.
        if ((System.getProperty("os.name").startsWith("Windows")))
        {
            buttonPanel.add( Box.createRigidArea( blankSpace ) );
            buttonPanel.addSeparator();
            buttonPanel.add( Box.createRigidArea( blankSpace ) );

            buttonPanel.add(systemTrayButton);
        }
        buttonPanel.add( Box.createHorizontalGlue() );
        buttonPanel.add(timeLabel);
        buttonPanel.add( Box.createRigidArea( blankSpace ) );

        return buttonPanel;
    }

    private JPanel buildStatusPanel()
    {
        statusLabel = new JLabel(LangRes.getString("Frost by Jantho"));
        statusMessageLabel = new JLabel();

        newMessage[0] = new ImageIcon(frame1.class.getResource("/data/messagebright.gif"));
        newMessage[1] = new ImageIcon(frame1.class.getResource("/data/messagedark.gif"));
        statusMessageLabel.setIcon(newMessage[1]);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER); // Statusbar
        statusPanel.add(statusMessageLabel, BorderLayout.EAST); // Statusbar / new Message
        return statusPanel;
    }

    private JPanel buildTofMainPanel()
    {
        this.tabbedPane = new JTabbedPane();
        //add a tab for buddies perhaps?
        tabbedPane.add(LangRes.getString("News"), buildMessagePane());
        tabbedPane.add(LangRes.getString("Search"), buildSearchPane());
        tabbedPane.add(LangRes.getString("Downloads"), buildDownloadPane());
        tabbedPane.add(LangRes.getString("Uploads"), buildUploadPane());

        updateOptionsAffectedComponents();

        // this rootnode is discarded later, but if we create the tree without parameters,
        // a new Model is created wich contains some sample data by default (swing)
        // this confuses our renderer wich only expects FrostBoardObjects in the tree
        FrostBoardObject dummyRootNode = new FrostBoardObject("Frost Message System", true);
        tofTree = new TofTree(dummyRootNode);
        JScrollPane tofTreeScrollPane = new JScrollPane(tofTree);
        tofTree.setRootVisible(true);
        tofTree.setCellRenderer(new TofTreeCellRenderer());
        tofTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        // tofTree selection listener
        tofTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                tofTree_actionPerformed(e);
        } });
        //tofTree / KeyEvent
        tofTree.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) { tofTree_keyPressed(e); }
            public void keyReleased(KeyEvent e) {}
        });

        JSplitPane treeAndTabbedPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tofTreeScrollPane, tabbedPane);
        treeAndTabbedPane.setDividerLocation(160); // Vertical Board Tree / MessagePane Divider

        JPanel tofMainPanel = new JPanel(new BorderLayout());
        tofMainPanel.add(treeAndTabbedPane, BorderLayout.CENTER); // TOF/Text
        return tofMainPanel;
    }

    private JPanel buildMessagePane()
    {
// configure buttons
        this.tofNewMessageButton = new JButton(new ImageIcon(frame1.class.getResource("/data/newmessage.gif")));
        this.tofUpdateButton = new JButton(new ImageIcon(frame1.class.getResource("/data/update.gif")));
        this.tofReplyButton = new JButton(new ImageIcon(frame1.class.getResource("/data/reply.gif")));
        this.downloadAttachmentsButton= new JButton(new ImageIcon(frame1.class.getResource("/data/attachment.gif")));
        this.downloadBoardsButton= new JButton(new ImageIcon(frame1.class.getResource("/data/attachmentBoard.gif")));
        this.saveMessageButton= new JButton(new ImageIcon(frame1.class.getResource("/data/save.gif")));
        this.trustButton= new JButton(new ImageIcon(frame1.class.getResource("/data/trust.gif")));
        this.notTrustButton= new JButton(new ImageIcon(frame1.class.getResource("/data/nottrust.gif")));
        this.checkTrustButton= new JButton(new ImageIcon(frame1.class.getResource("/data/check.gif")));

        configureButton(tofNewMessageButton, "New message", "/data/newmessage_rollover.gif");
        configureButton(tofUpdateButton, "Update", "/data/update_rollover.gif");
        configureButton(tofReplyButton, "Reply", "/data/reply_rollover.gif");
        configureButton(downloadAttachmentsButton, "Download attachment(s)", "/data/attachment_rollover.gif");
        configureButton(downloadBoardsButton, "Add Board(s)", "/data/attachmentBoard_rollover.gif");
        configureButton(saveMessageButton, "Save message", "/data/save_rollover.gif");
        configureButton(trustButton, "Trust", "/data/trust_rollover.gif");
        configureButton(notTrustButton, "Do not trust", "/data/nottrust_rollover.gif");
        configureButton(checkTrustButton, "Set to CHECK", "/data/check_rollover.gif");

// add action listener to buttons
        tofUpdateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) { // Update selected board
                // restarts all finished threads if there are some long running threads
                if (isUpdateAllowed(getSelectedNode()))  { updateBoard(getSelectedNode());  }
            } });
        tofNewMessageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tofNewMessageButton_actionPerformed(e);
            } });
        downloadAttachmentsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadAttachments();
            } });
        downloadBoardsButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               downloadBoards();
           } });
       tofReplyButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               tofReplyButton_actionPerformed(e);
           } });
       saveMessageButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               FileAccess.saveDialog(getInstance(), getTofTextAreaText(), frostSettings.getValue("lastUsedDirectory"), LangRes.getString("Save message to disk"));
           } });
        trustButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               trustButton_actionPerformed(e);
           } });
       notTrustButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               notTrustButton_actionPerformed(e);
           } });
       checkTrustButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
               checkTrustButton_actionPerformed(e);
           } });
// build buttons panel
        JToolBar tofTopPanel = new JToolBar();
        tofTopPanel.setRollover( true );
        tofTopPanel.setFloatable( false );
        Dimension blankSpace = new Dimension(3,3);

        tofTopPanel.add( Box.createRigidArea( blankSpace ) );
        tofTopPanel.add(saveMessageButton); // TOF/ Save Message
        tofTopPanel.add( Box.createRigidArea( blankSpace ) );
        tofTopPanel.addSeparator();
        tofTopPanel.add( Box.createRigidArea( blankSpace ) );
        tofTopPanel.add(tofNewMessageButton); // TOF/ New Message
        tofTopPanel.add(tofReplyButton); // TOF/ Reply
        tofTopPanel.add( Box.createRigidArea( blankSpace ) );
        tofTopPanel.addSeparator();
        tofTopPanel.add( Box.createRigidArea( blankSpace ) );
        tofTopPanel.add(tofUpdateButton); // TOF/ Update
        tofTopPanel.add( Box.createRigidArea( blankSpace ) );
        tofTopPanel.addSeparator();
        tofTopPanel.add( Box.createRigidArea( blankSpace ) );
        tofTopPanel.add(downloadAttachmentsButton); // TOF/ Download Attachments
        tofTopPanel.add(downloadBoardsButton); // TOF/ Download Boards
        tofTopPanel.add( Box.createRigidArea( blankSpace ) );
        tofTopPanel.addSeparator();
        tofTopPanel.add( Box.createRigidArea( blankSpace ) );
        tofTopPanel.add(trustButton); //TOF /trust
        tofTopPanel.add(checkTrustButton); //TOF /check trust
        tofTopPanel.add(notTrustButton); //TOF /do not trust

        tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
        tofTopPanel.add( Box.createHorizontalGlue() );
        JLabel dummyLabel = new JLabel(allMessagesCountPrefix + "00000");
        dummyLabel.doLayout();
        Dimension labelSize = dummyLabel.getPreferredSize();
        allMessagesCountLabel.setPreferredSize(labelSize);
        allMessagesCountLabel.setMinimumSize(labelSize);
        newMessagesCountLabel.setPreferredSize(labelSize);
        newMessagesCountLabel.setMinimumSize(labelSize);
        tofTopPanel.add(allMessagesCountLabel);
        tofTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
        tofTopPanel.add(newMessagesCountLabel);
        tofTopPanel.add( Box.createRigidArea( blankSpace ) );
// build panel wich shows the message list + message
        MessageTableModel messageTableModel = new MessageTableModel();
        this.messageTable = new MessageTable(messageTableModel);
        messageTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        messageTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e){
                messageTableListModel_valueChanged(e);
            } });
        JScrollPane messageTableScrollPane = new JScrollPane(messageTable);

        this.tofTextArea = new JTextArea();
        JScrollPane tofTextAreaScrollPane = new JScrollPane(tofTextArea);
        tofTextArea.setEditable(false);
        tofTextArea.setLineWrap(true);
        tofTextArea.setWrapStyleWord(true);

        AttachmentTableModel attachmentTableModel = new AttachmentTableModel();
        this.attachmentTable = new JTable(attachmentTableModel);
        JScrollPane attachmentTableScrollPane = new JScrollPane(attachmentTable);

        AttachedBoardTableModel boardTableModel = new AttachedBoardTableModel();
        this.boardTable = new JTable(boardTableModel);
        JScrollPane boardTableScrollPane = new JScrollPane(boardTable);

        this.attachmentSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT,
                                                   tofTextAreaScrollPane,
                                                   attachmentTableScrollPane );
        this.boardSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT,
                                              attachmentSplitPane,
                                              boardTableScrollPane );

        JSplitPane tofSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, messageTableScrollPane, boardSplitPane);
        tofSplitPane.setDividerSize(10);
        tofSplitPane.setDividerLocation(160);
        tofSplitPane.setResizeWeight(0.5d);
        tofSplitPane.setMinimumSize(new Dimension(50, 20));

// build panel
        JPanel messageTablePanel = new JPanel(new BorderLayout());
        messageTablePanel.add(tofTopPanel, BorderLayout.NORTH);
        messageTablePanel.add(tofSplitPane, BorderLayout.CENTER);
        return messageTablePanel;
    }

    /**
     * Called by frost after call of show().
     * Sets the initial states of the message splitpanes.
     * Must be called AFTER frame is shown.
     **/
    public void resetMessageViewSplitPanes()
    {
        // initially hide the attachment tables
        attachmentSplitPane.setDividerSize( 0 );
        attachmentSplitPane.setDividerLocation(1.0);
        boardSplitPane.setDividerSize( 0 );
        boardSplitPane.setDividerLocation(1.0);
        setTofTextAreaText( LangRes.getString("Select a message to view its content.") );
    }

    private JPanel buildSearchPane()
    {
// create objects for button toolbar
        this.searchAllBoardsCheckBox= new JCheckBox("all boards",true);
        JButton searchDownloadButton = new JButton(new ImageIcon(frame1.class.getResource("/data/save.gif")));
        this.searchButton = new JButton(new ImageIcon(frame1.class.getResource("/data/search.gif")));

        this.searchTextField = new JTextField(25);
        String[] searchComboBoxItems = {"All files", "Audio", "Video", "Images", "Documents", "Executables", "Archives"};
        this.searchComboBox = new JComboBox(searchComboBoxItems);
// configure buttons
        configureButton(searchButton, "Search", "/data/search_rollover.gif");
        configureButton(searchDownloadButton, "Download selected keys", "/data/save_rollover.gif");
// build button toolbar panel
        JPanel searchTopPanel = new JPanel();
        BoxLayout dummyLayout = new BoxLayout( searchTopPanel, BoxLayout.X_AXIS );
        searchTopPanel.setLayout( dummyLayout );

        searchTextField.setMaximumSize( searchTextField.getPreferredSize() );
        searchTopPanel.add(searchTextField); // Search / text
        searchTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
        searchComboBox.setMaximumSize( searchComboBox.getPreferredSize() );
        searchTopPanel.add(searchComboBox);
        searchTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
        searchTopPanel.add(searchButton); // Search / Search button
        searchTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
        searchTopPanel.add(searchDownloadButton); // Search / Download selected files
        searchTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
        searchTopPanel.add(searchAllBoardsCheckBox);
        searchTopPanel.add( Box.createRigidArea(new Dimension(80,0)));
        searchTopPanel.add( Box.createHorizontalGlue() );

        searchResultsCountPrefix = LangRes.getString("   Results: ");
        JLabel dummyLabel = new JLabel(searchResultsCountPrefix + "00000");
        dummyLabel.doLayout();
        Dimension labelSize = dummyLabel.getPreferredSize();
        searchResultsCountLabel.setPreferredSize(labelSize);
        searchResultsCountLabel.setMinimumSize(labelSize);
        searchResultsCountLabel.setText(searchResultsCountPrefix+"0");
        searchTopPanel.add(searchResultsCountLabel);
// build table
        SearchTableModel searchTableModel = new SearchTableModel();
        this.searchTable = new SearchTable(searchTableModel);
        JScrollPane searchTableScrollPane = new JScrollPane(searchTable);
// add action listener
        searchTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchTextField_actionPerformed(e);
            } });
        searchDownloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getSearchTable().addSelectedSearchItemsToDownloadTable( getDownloadTable() );
            } });
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchButton_actionPerformed(e);
            } });
// build panel
        JPanel searchMainPanel = new JPanel(new BorderLayout());
        searchMainPanel.add(searchTopPanel, BorderLayout.NORTH); // Search / Buttons
        searchMainPanel.add(searchTableScrollPane, BorderLayout.CENTER); // Search / Results
        return searchMainPanel;
    }

    private JPanel buildDownloadPane()
    {
// create objects for buttons toolbar panel
        this.downloadActivateCheckBox = new JCheckBox(new ImageIcon(frame1.class.getResource("/data/down.gif")), true);
        configureCheckBox(downloadActivateCheckBox,
                     "Activate downloading",
                     "/data/down_rollover.gif",
                     "/data/down_selected.gif",
                     "/data/down_selected_rollover.gif");

        this.downloadTextField = new JTextField(25);
        downloadTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadTextField_actionPerformed(e);
        } });
// create buttons toolbar panel
        JPanel downloadTopPanel = new JPanel();
        BoxLayout dummyLayout = new BoxLayout( downloadTopPanel, BoxLayout.X_AXIS );
        downloadTopPanel.setLayout( dummyLayout );
        downloadTextField.setMaximumSize( downloadTextField.getPreferredSize() );
        downloadTopPanel.add(downloadTextField);//Download/Quickload
        downloadTopPanel.add( Box.createRigidArea(new Dimension(8,0)));
        downloadTopPanel.add(downloadActivateCheckBox);//Download/Start transfer
        downloadTopPanel.add( Box.createRigidArea(new Dimension(80,0)));
        downloadTopPanel.add( Box.createHorizontalGlue() );

        String downloadCountPrefix = LangRes.getString("Waiting");
        JLabel dummyLabel = new JLabel(downloadCountPrefix + " : 00000");
        dummyLabel.doLayout();
        Dimension labelSize = dummyLabel.getPreferredSize();
        downloadItemCountLabel.setPreferredSize(labelSize);
        downloadItemCountLabel.setMinimumSize(labelSize);
        downloadItemCountLabel.setText(downloadCountPrefix+" : 0");
        downloadTopPanel.add(downloadItemCountLabel);

// create downloadTable
        DownloadTableModel downloadTableModel = new DownloadTableModel();
        this.downloadTable = new DownloadTable(downloadTableModel);
        JScrollPane downloadTableScrollPane = new JScrollPane(downloadTable);
        //Downloads / KeyEvent
        downloadTable.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) { downloadTable_keyPressed(e); }
            public void keyReleased(KeyEvent e) {}
        });
// create the main download panel
        JPanel downloadMainPanel = new JPanel(new BorderLayout());
        downloadMainPanel.add(downloadTopPanel, BorderLayout.NORTH); // Download/Buttons
        downloadMainPanel.add(downloadTableScrollPane, BorderLayout.CENTER); //Downloadlist
        return downloadMainPanel;
    }

    private JPanel buildUploadPane()
    {
// create upload table
        UploadTableModel uploadTableModel = new UploadTableModel();
        this.uploadTable = new UploadTable(uploadTableModel);
        JScrollPane uploadTableScrollPane = new JScrollPane(uploadTable);
        // uploadTable / KeyEvent
        uploadTable.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_DELETE && !uploadTable.isEditing())
                    getUploadTable().removeSelectedRows();
            }
            public void keyReleased(KeyEvent e) {}
        });
// create toolbar button
        this.uploadAddFilesButton = new JButton(new ImageIcon(frame1.class.getResource("/data/browse.gif")));
        configureButton(uploadAddFilesButton, "Browse...", "/data/browse_rollover.gif");
        uploadAddFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadAddFilesButton_actionPerformed(e);
            } });
// add button to top panel
        JPanel uploadTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        uploadTopPanel.add(uploadAddFilesButton);
// add all to main upload panel
        JPanel uploadMainPanel = new JPanel(new BorderLayout());
        uploadMainPanel.add(uploadTopPanel, BorderLayout.NORTH);
        uploadMainPanel.add(uploadTableScrollPane, BorderLayout.CENTER);
        return uploadMainPanel;
    }
//**********************************************************************************************
//**********************************************************************************************
//**********************************************************************************************
    /**Component initialization*/
    private void jbInit() throws Exception  {

    setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/jtc.jpg")));
    this.setResizable(true);

    this.setTitle("Frost");

    JPanel contentPanel = (JPanel)this.getContentPane();
    contentPanel.setLayout(new BorderLayout());

    contentPanel.add( buildButtonPanel(), BorderLayout.NORTH); // buttons toolbar
    contentPanel.add( buildTofMainPanel(), BorderLayout.CENTER); // tree / tabbed pane
    contentPanel.add( buildStatusPanel(), BorderLayout.SOUTH); // Statusbar

    buildMenuBar();
    buildPopupMenus();

//**********************************************************************************************
//**********************************************************************************************
//**********************************************************************************************

    /*configureCheckBox(searchAllBoardsCheckBox,
                 "Search all boards",
                 "data/allboards_rollover.gif",
                 "data/allboards_selected.gif",
                 "data/allboards_selected_rollover.gif");*/

    // Add Popup listeners
    MouseListener popupListener = new PopupListener();
    getDownloadTable().addMouseListener(popupListener);
    getSearchTable().addMouseListener(popupListener);
    getUploadTable().addMouseListener(popupListener);
    tofTextArea.addMouseListener(popupListener);
    getTofTree().addMouseListener(popupListener);
    getAttachmentTable().addMouseListener(popupListener);
    getAttachedBoardsTable().addMouseListener(popupListener);
    messageTable.addMouseListener(popupListener);

//**********************************************************************************************
//**********************************************************************************************
//**********************************************************************************************

    timer = new javax.swing.Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            timer_actionPerformed();
    } });

    timer2 = new java.util.Timer(true);
    timer2.schedule(new checkForSpam(), 0, frostSettings.getIntValue("sampleInterval")*60*60*1000);

    //------------------------------------------------------------------------

    tofReplyButton.setEnabled(false);
    downloadAttachmentsButton.setEnabled(false);
    downloadBoardsButton.setEnabled(false);
    saveMessageButton.setEnabled(false);
    pasteBoardButton.setEnabled(false);
    searchAllBoardsCheckBox.setSelected(true);
    trustButton.setEnabled(false);
    notTrustButton.setEnabled(false);
    checkTrustButton.setEnabled(false);

    // CLEANS TEMP DIR! START NO INSERTS BEFORE THIS RUNNED
    Startup.startupCheck();

    FileAccess.cleanKeypool(keypool);

    // Display the tray icon
    try {
        Process process = Runtime.getRuntime().exec("exec" + fileSeparator + "SystemTray.exe");
    }catch(IOException _IoExc) { }

    //check whether the user is running a transient node
    setFreenetIsTransient(false);
    setFreenetIsOnline(false);

    FcpConnection con1 = FcpFactory.getFcpConnectionInstance();
    if( con1 != null )
    {
        String[] nodeInfo = con1.getInfo();
        // freenet is online
        setFreenetIsOnline(true);
        for (int ij=0;ij<nodeInfo.length;ij++)
        {
            if (nodeInfo[ij].startsWith("IsTransient") && nodeInfo[ij].indexOf("true") != -1)
            {
                setFreenetIsTransient(true);
            }
        }
    }
    else
    {
        JOptionPane.showMessageDialog(this,
        "Make sure your node is running and that you have configured frost correctly.\n"+
        "Nevertheless, to allow you to read messages, Frost will startup now.\n"+
        "Don't get confused by some error messages ;)\n",
	"Error - could not establish a connection to freenet node.",
	JOptionPane.WARNING_MESSAGE);
        setFreenetIsOnline(false);
    }

    if( isFreenetTransient() )
    {
        JOptionPane.showMessageDialog(this,
                        "      You are running a TRANSIENT node.  "+
                        "Better run a PERMANENT freenet node.",
                        "Transient node detected",
                        JOptionPane.WARNING_MESSAGE);
    }

    //create a crypt object
    crypto = new FrostCrypt();

    //load the identities
    loadIdentities();

    //on with other stuff

    getTofTree().initialize();

    // step through all messages on disk up to maxMessageDisplay and check if there are new messages
    // if a new message is in a folder, this folder is show yellow in tree
    TOF.initialSearchNewMessages();

    if( isFreenetOnline() )
    {
        downloadActivateCheckBox.setSelected(frostSettings.getBoolValue("downloadingActivated"));
        tofAutomaticUpdateMenuItem.setSelected(frostSettings.getBoolValue("automaticUpdate"));
    }
    else
    {
        downloadActivateCheckBox.setSelected(false);
        tofAutomaticUpdateMenuItem.setSelected(false);
    }
    searchAllBoardsCheckBox.setSelected(frostSettings.getBoolValue("searchAllBoards"));
    //      uploadActivateCheckBox.setSelected(frostSettings.getBoolValue("uploadingActivated"));
    //      reducedBlockCheckCheckBox.setSelected(frostSettings.getBoolValue("reducedBlockCheck"));

    if(getTofTree().getRowCount() > frostSettings.getIntValue("tofTreeSelectedRow"))
       getTofTree().setSelectionRow(frostSettings.getIntValue("tofTreeSelectedRow"));

    // make sure the font size isn't too small to see
    if (frostSettings.getFloatValue("tofFontSize") < 6.0f)
        frostSettings.setValue("tofFontSize",  6.0f);

    // always use monospaced font for tof text
    Font tofFont = new Font("Monospaced", Font.PLAIN, (int)frostSettings.getFloatValue("tofFontSize") );
    tofTextArea.setFont( tofFont );

    // Load table settings
    getDownloadTable().load();
    getUploadTable().load();

    // load size and location of window
    int lastHeight = frostSettings.getIntValue("lastFrameHeight" );
    int lastWidth = frostSettings.getIntValue("lastFrameWidth" );
//    int lastX = frostSettings.getIntValue("lastFrameLocX" );
//    int lastY = frostSettings.getIntValue("lastFrameLocY" );

    if( lastWidth < 100 || lastHeight < 100 )
    {
        // set default size
        this.setSize(new Dimension(790, 580));
    }
    else
    {
        this.setSize( lastWidth, lastHeight );
//        this.setLocation( lastX, lastY );
    }

    // a class that reinserts the pubkey each hour
    TimerTask KeyReinserter = new TimerTask() {
        public void run() {
            if( isFreenetOnline() == false )
                return;
            File tempUploadfile = null;
            try {
                tempUploadfile = File.createTempFile("pubkey_", null, new File(frame1.frostSettings.getValue("temp.dir")) );
            }
            catch(Exception ex)
            {
                tempUploadfile = new File(frame1.frostSettings.getValue("temp.dir") + "pubkey_"+System.currentTimeMillis()+".tmp");
            }
            FileAccess.writeFile(mySelf.getKey(), tempUploadfile);

            System.out.println("KeyReinserter: Re-uploading public key...");
            FcpInsert.putFile("CHK@",tempUploadfile,25,false,true,null);
            System.out.println("KeyReinserter: Finished re-uploading public key.");

            tempUploadfile.deleteOnExit();
            tempUploadfile.delete();
        }
    };
    timer2.schedule(KeyReinserter,0,60*60*1000); // run each hour

    // Start tofTree
    if( isFreenetOnline() )
    {
        resendFailedMessages();
    }
    timer.start();
    started = true;
    } // ************** end-of: jbInit()

    protected void loadIdentities()
    {
    	goodIds = new Hashtable();
	badIds = new Hashtable();
	myBatches = new Hashtable();
        File identities = new File("identities");
	
        //File contacts = new File("contacts");
        System.out.println("trying to create/load ids");
        try {
            if( identities.length() == 0 )
                identities.delete();
            if( identities.createNewFile() )
            {
                if( isFreenetOnline() == false )
                {
                    JOptionPane.showMessageDialog(this,
                                                  "Frost could not establish a connection to your freenet node. "+
                                                  "For first setup of Frost and creating your identity a connection is needed,"+
                                                  "later you can run Frost without a connection.\n"+
                                                  "Please ensure that you are online and freenet is running, then restart Frost.",
                                                  "Connect to Freenet node failed",
                                                  JOptionPane.ERROR_MESSAGE);
                    System.exit(2);
                }
                //create new identities
                try {
                    String nick = null;
                    do
                    {
                        nick = JOptionPane.showInputDialog("Choose an identity name, it doesn't have to be unique\n");
                        if( !(nick == null || nick.length() == 0) )
                        {
                            // check for a '@' in nick, this is strongly forbidden
                            if( nick.indexOf("@") > -1 )
                            {
                                JOptionPane.showMessageDialog(this,
                                                              "Your name must not contain a '@'!",
                                                              "Invalid identity name",
                                                              JOptionPane.ERROR_MESSAGE );
                                nick="";
                            }
                        }

                    } while( nick != null && nick.length() == 0 );
                    if( nick == null )
                    {
                        System.out.println("Frost can't run without an identity.");
                        System.exit(1);
                    }
                    mySelf = new LocalIdentity(nick);
                    //JOptionPane.showMessageDialog(this,new String("the following is your key ID, others may ask you for it : \n" + crypto.digest(mySelf.getKey())));
                }
                catch( Exception e ) {
                    System.out.println("couldn't create new identitiy");
                    System.out.println(e.toString());
                }
                friends = new BuddyList();
		
                if( friends.Add(frame1.getMyId()) )
                {
                    System.out.println("added myself to list");
                }
                enemies = new BuddyList();
		
            }
            else
            {
                try {
                    BufferedReader fin = new BufferedReader(new FileReader(identities));
                    String name = fin.readLine();
                    String address = fin.readLine();
                    String keys[] = new String[2];
                    keys[1] = fin.readLine();
                    keys[0] = fin.readLine();
                    if( address.startsWith("CHK@") == false )
                    {
                        // pubkey chk was not successfully computed
                        byte[] pubkeydata;
                        try { pubkeydata = keys[1].getBytes("UTF-8"); }
                        catch(UnsupportedEncodingException ex) { pubkeydata = keys[1].getBytes(); }

                        try {
                            FcpConnection con = FcpFactory.getFcpConnectionInstance();
                            if( con != null )
                            {
                                String tmp = con.putKeyFromFile("CHK@", pubkeydata, null, 0, false);
                                address = tmp.substring(tmp.indexOf("CHK@"),tmp.indexOf("CHK@") + 58);
                                System.out.println("Re-calculated my public key CHK: " + address + "\n");
                            }
                        }
                        catch( IOException e ) {
                            System.out.println("Couldn't re-calculate my public key CHK: "+e.toString());
                        }
                    }
                    mySelf = new LocalIdentity(name, keys, address);
                    System.out.println("loaded myself with name " + mySelf.getName());
                    //System.out.println("and public key" + mySelf.getKey());

                    //take out the ****
                    fin.readLine();

                    //process the friends
                    System.out.println("loading friends");
                    friends = new BuddyList();
                    boolean stop = false;
                    String key;
                    while( !stop )
                    {
                        name = fin.readLine();
                        if( name==null || name.startsWith("***") ) break;
                        address = fin.readLine();
                        key = fin.readLine();
                        friends.Add(new Identity(name, address,key));
                    }
                    System.out.println("loaded " + friends.size() + " friends");
		    
		    //just the good ids
		    while (!stop) {
		        String id = fin.readLine();
			if (id == null || id.startsWith("***")) break;
			goodIds.put(id,id);
		    }
		    System.out.println("loaded " +goodIds.size() + " good ids");

                    //and the enemies
                    enemies = new BuddyList();
                    System.out.println("loading enemies");
                    while( !stop )
                    {
                        name = fin.readLine();
                        if( name == null || name.startsWith("***") ) break;
                        address = fin.readLine();
                        key = fin.readLine();
                        enemies.Add(new Identity(name, address,key));
                    }
                    System.out.println("loaded " + enemies.size() + " enemies");
		    
		    //and the bad ids
		    while (!stop) {
		    	String id = fin.readLine();
			if (id == null || id.startsWith("***")) break;
			badIds.put(id,id);
		    }
		    System.out.println("loaded " +badIds.size() + " bad ids");
		    

                }
                catch( IOException e ) {
                    System.out.println("IOException :" + e.toString());
                    friends = new BuddyList();
                    enemies = new BuddyList();
                    friends.Add(mySelf);
                }
                catch( Exception e ) {
                    e.printStackTrace();
                }
            }
        }
        catch( IOException e ) {
            System.out.println("couldn't create identities file");
        }
        System.out.println("ME = '"+getMyId().getUniqueName()+"'");
	
	File batches = new File("batches");
	if (batches.exists())
	try{
		String allBatches = FileAccess.readFileRaw(batches);
		String[] _batches = allBatches.split("_"); //dumb.  will fix later
		
		for (int i = 0;i<_batches.length;i++)
			myBatches.put(_batches[i],_batches[i]);
			
		System.out.println("loaded "+_batches.length+" batches of shared files");
	}catch(Throwable e) {
		System.out.println("couldn't load batches");
		e.printStackTrace();
	}

    }

    /**
     * Build ALL popup menus.
     * Should be called only once.
     */
    private void buildPopupMenus()
    {
        buildPopupMenuSearch();
        buildPopupMenuUpload();
        buildPopupMenuDownload();
        buildPopupMenuTofText();
        buildPopupMenuMessageTable();
    }

    private void buildPopupMenuMessageTable()
    {
        msgTablePopupMarkMessageUnread = new JMenuItem("Mark message unread");
        msgTablePopupMarkMessageUnread.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                markSelectedMessageUnread();
            } });
    }

    /**
     * Build the search table popup menu.
     * Should be called only once.
     */
    private void buildPopupMenuSearch()
    {
// create objects
        searchPopupDownloadSelectedKeys = new JMenuItem(LangRes.getString("Download selected keys"));
        searchPopupDownloadAllKeys = new JMenuItem(LangRes.getString("Download all keys"));
        searchPopupCopyAttachment = new JMenuItem(LangRes.getString("Copy as attachment to clipboard"));
	searchPopupSetGood = new JMenuItem(LangRes.getString("help user (sets to GOOD)"));
	searchPopupSetBad = new JMenuItem(LangRes.getString("block user (sets to BAD)"));
        searchPopupCancel = new JMenuItem(LangRes.getString("Cancel"));
// add action listener
        searchPopupDownloadSelectedKeys.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getSearchTable().addSelectedSearchItemsToDownloadTable(getDownloadTable() );
            } });
        searchPopupDownloadAllKeys.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                searchTable.selectAll();
                getSearchTable().addSelectedSearchItemsToDownloadTable(getDownloadTable() );
            } });
        searchPopupCopyAttachment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String srcData = getSearchTable().getSelectedSearchItemsAsAttachmentsString();
                Clipboard clipboard = getToolkit().getSystemClipboard();
                StringSelection contents = new StringSelection(srcData);
                clipboard.setContents(contents, frame1.this);
            } });
	    
	searchPopupSetGood.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	    	java.util.List l = getSearchTable().getSelectedItemsOwners();
		Iterator i = l.iterator();
		while (i.hasNext()) {
			Identity owner_id = null;
			String owner_s = null;
			try {
				owner_s = (String)i.next();
			} catch(ClassCastException cce) {
				owner_id = (Identity)i.next();
			}
			
			if (owner_id != null) {
				getEnemies().remove(owner_id.getUniqueName());
				getFriends().Add(owner_id);
				continue;
			}
			
			if (owner_s != null) {
				getBadIds().remove(owner_s);
				getGoodIds().put(owner_s,owner_s);
				continue;
			}
		}
	}});
			
	searchPopupSetBad.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	    	java.util.List l = getSearchTable().getSelectedItemsOwners();
		Iterator i = l.iterator();
		while (i.hasNext()) {
			Identity owner_id = null;
			String owner_s = null;
			try {
				owner_s = (String)i.next();
			} catch(ClassCastException cce) {
				owner_id = (Identity)i.next();
			}
			
			if (owner_id != null) {
				getFriends().remove(owner_id.getUniqueName());
				getEnemies().Add(owner_id);
				continue;
			}
			
			if (owner_s != null) {
				getGoodIds().remove(owner_s);
				getBadIds().put(owner_s,owner_s);
				continue;
			}
		}
	}});		
;
	searchPopupSetBad.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	    	//String
	    }
	 });
    }

    /**
     * Build the upload table popup menu.
     * Should be called only once.
     */
    private void buildPopupMenuUpload()
    {
// create objects
        uploadPopupRemoveSelectedFiles = new JMenuItem(LangRes.getString("Remove selected files"));
        uploadPopupRemoveAllFiles = new JMenuItem(LangRes.getString("Remove all files"));
        uploadPopupReloadSelectedFiles = new JMenuItem(LangRes.getString("Reload selected files"));
        uploadPopupReloadAllFiles = new JMenuItem(LangRes.getString("Reload all files"));
        uploadPopupSetPrefixForSelectedFiles = new JMenuItem(LangRes.getString("Set prefix for selected files"));
        uploadPopupSetPrefixForAllFiles = new JMenuItem(LangRes.getString("Set prefix for all files"));
        uploadPopupRestoreDefaultFilenamesForSelectedFiles = new JMenuItem(LangRes.getString("Restore default filenames for selected files"));
        uploadPopupRestoreDefaultFilenamesForAllFiles = new JMenuItem(LangRes.getString("Restore default filenames for all files"));
        uploadPopupChangeDestinationBoard = new JMenu(LangRes.getString("Change destination board"));
        uploadPopupAddFilesToBoard = new JMenuItem(LangRes.getString("Add files to board"));
        uploadPopupCancel = new JMenuItem(LangRes.getString("Cancel"));
// add action listener
        // Upload / Remove selected files
        uploadPopupRemoveSelectedFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().removeSelectedRows();
            } });
        // Upload / Remove all files
        uploadPopupRemoveAllFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                UploadTableModel model = (UploadTableModel)getUploadTable().getModel();
                model.clearDataModel();
            } });
        // Upload / Reload selected files
        uploadPopupReloadSelectedFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                UploadTableModel tableModel = (UploadTableModel)getUploadTable().getModel();
                int[] selectedRows = getUploadTable().getSelectedRows();
                for (int i = 0; i < selectedRows.length; i++){
                    FrostUploadItemObject ulItem = (FrostUploadItemObject)tableModel.getRow( selectedRows[i] );
                    // Since it is difficult to identify the states where we are allowed to
                    // start an upload we decide based on the states in which we are not allowed
                    if( ulItem.getState() != FrostUploadItemObject.STATE_UPLOADING &&
                        ulItem.getState() != FrostUploadItemObject.STATE_PROGRESS )
                    {
                        ulItem.setState( FrostUploadItemObject.STATE_REQUESTED );
                        tableModel.updateRow(ulItem);
                    }
                }
            } });
        // Upload / Reload all files
        uploadPopupReloadAllFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                UploadTableModel tableModel = (UploadTableModel)getUploadTable().getModel();
                for (int i = 0; i < tableModel.getRowCount(); i++){
                    FrostUploadItemObject ulItem = (FrostUploadItemObject)tableModel.getRow( i );
                    // Since it is difficult to identify the states where we are allowed to
                    // start an upload we decide based on the states in which we are not allowed
                    if( ulItem.getState() != FrostUploadItemObject.STATE_UPLOADING &&
                        ulItem.getState() != FrostUploadItemObject.STATE_PROGRESS )
                    {
                        ulItem.setState( FrostUploadItemObject.STATE_REQUESTED );
                        tableModel.updateRow(ulItem);
                    }
                }
            } });
        // Upload / Set Prefix for selected files
        uploadPopupSetPrefixForSelectedFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().setPrefixForSelectedFiles();
            } });
        // Upload / Set Prefix for all files
        uploadPopupSetPrefixForAllFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().selectAll();
                getUploadTable().setPrefixForSelectedFiles();
            } });
        // Upload / Restore default filenames for selected files
        uploadPopupRestoreDefaultFilenamesForSelectedFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().restoreOriginalFilenamesForSelectedRows();
            } });
        // Upload / Restore default filenames for all files
        uploadPopupRestoreDefaultFilenamesForAllFiles.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().selectAll();
                getUploadTable().restoreOriginalFilenamesForSelectedRows();
            } });
        // Upload / Restore default filenames for all files
        uploadPopupAddFilesToBoard.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getUploadTable().addFilesToBoardIndex();
            } });
    }

    /**
     * Build the download table popup menu.
     * Should be called only once.
     */
    private void buildPopupMenuDownload()
    {
// construct objects
        downloadPopupRestartSelectedDownloads = new JMenuItem("Restart selected downloads");
        downloadPopupRemoveSelectedDownloads = new JMenuItem(LangRes.getString("Remove selected downloads"));
        downloadPopupRemoveAllDownloads = new JMenuItem(LangRes.getString("Remove all downloads"));
//        downloadPopupResetHtlValues = new JMenuItem(LangRes.getString("Retry selected downloads"));
        downloadPopupRemoveFinished = new JMenuItem("Remove finished downloads");

        downloadPopupEnableAllDownloads = new JMenuItem("Enable all downloads");
        downloadPopupDisableAllDownloads = new JMenuItem("Disable all downloads");
        downloadPopupEnableSelectedDownloads = new JMenuItem("Enable selected downloads");
        downloadPopupDisableSelectedDownloads = new JMenuItem("Disable selected downloads");
        downloadPopupInvertEnabledAll = new JMenuItem("Invert enabled state for all downloads");
        downloadPopupInvertEnabledSelected = new JMenuItem("Invert enabled state for selected downloads");

        // TODO: implement cancel of downloading
        downloadPopupCancel = new JMenuItem(LangRes.getString("Cancel"));

// add action listener
        downloadPopupRestartSelectedDownloads.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().restartSelectedDownloads();
            } });
        downloadPopupRemoveSelectedDownloads.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().removeSelectedItemsFromTable();
            } });
        downloadPopupRemoveAllDownloads.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().removeAllItemsFromTable();
            } });
        downloadPopupRemoveFinished.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().removeFinishedDownloads();
            } });

        downloadPopupEnableAllDownloads.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().setDownloadEnabled( 1, true ); // 1=enabled , true means ALL in table!
            } });
        downloadPopupDisableAllDownloads.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().setDownloadEnabled( 0, true ); // 0=disabled , true means ALL in table!
            } });
        downloadPopupEnableSelectedDownloads.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().setDownloadEnabled( 1, false ); // 1=enabled , false means SELECTED in table!
            } });
        downloadPopupDisableSelectedDownloads.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().setDownloadEnabled( 0, false ); // 0=disabled , false means SELECTED in table!
            } });
        downloadPopupInvertEnabledAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().setDownloadEnabled( 2, true ); // 2=invert , true means ALL in table!
            } });
        downloadPopupInvertEnabledSelected.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getDownloadTable().setDownloadEnabled( 2, false ); // 2=invert , false means SELECTED in table!
            } });
    }

    /**
     * Build the tof text popup menu.
     * Should be called only once.
     */
    private void buildPopupMenuTofText()
    {
// create objects
        tofTextPopupSaveMessage = new JMenuItem(LangRes.getString("Save message to disk"));
        tofTextPopupSaveAttachments = new JMenuItem(LangRes.getString("Download attachment(s)"));
        tofTextPopupSaveAttachment = new JMenuItem("Download selected attachment");
        tofTextPopupSaveBoards = new JMenuItem("Add board(s)");
        tofTextPopupSaveBoard = new JMenuItem("Add selected board");
        tofTextPopupCancel = new JMenuItem(LangRes.getString("Cancel"));
// add action listener
        tofTextPopupSaveMessage.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                FileAccess.saveDialog(getInstance(), getTofTextAreaText(), frostSettings.getValue("lastUsedDirectory"), LangRes.getString("Save message to disk"));
            } });
        tofTextPopupSaveAttachments.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                downloadAttachments();
            } });
         tofTextPopupSaveAttachment.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                downloadAttachments();
            } });
         tofTextPopupSaveBoards.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                downloadBoards();
            } });
         tofTextPopupSaveBoard.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                downloadBoards();
            } });
    }

    /**
     * Build the menu bar.
     * Should be called only once.
     */
    private void buildMenuBar()
    {
// create objects
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(LangRes.getString("File"));
        JMenuItem fileExitMenuItem = new JMenuItem(LangRes.getString("Exit"));

        JMenu tofMenu = new JMenu(LangRes.getString("News"));
        JMenuItem tofConfigureBoardMenuItem = new JMenuItem(LangRes.getString("Configure selected board"));
        JMenuItem tofDisplayBoardInfoMenuItem = new JMenuItem(LangRes.getString("Display board information window"));
        this.tofAutomaticUpdateMenuItem = new JCheckBoxMenuItem(LangRes.getString("Automatic message update"), true);
        JMenuItem tofIncreaseFontSizeMenuItem = new JMenuItem (LangRes.getString ("Increase Font Size"));
        JMenuItem tofDecreaseFontSizeMenuItem = new JMenuItem (LangRes.getString ("Decrease Font Size"));

        JMenu optionsMenu = new JMenu(LangRes.getString("Options"));
        JMenuItem optionsPreferencesMenuItem = new JMenuItem(LangRes.getString("Preferences"));

        JMenu pluginMenu = new JMenu("Plugin");
        JMenuItem pluginBrowserMenuItem = new JMenuItem("EFB (Experimental Freenet Browser)");

        JMenu helpMenu = new JMenu(LangRes.getString("Help"));
        JMenuItem helpHelpMenuItem = new JMenuItem(LangRes.getString("Help"));
        JMenuItem helpAboutMenuItem = new JMenuItem(LangRes.getString("About"));
// add action listener
        fileExitMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                fileExitMenuItem_actionPerformed(e);
            } });
        optionsPreferencesMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                optionsPreferencesMenuItem_actionPerformed(e);
            } });
        tofIncreaseFontSizeMenuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                // make the font size in the TOF text area one point bigger
                Font f = tofTextArea.getFont();
                frostSettings.setValue("tofFontSize",f.getSize() + 1.0f);
                f = f.deriveFont(frostSettings.getFloatValue("tofFontSize"));
                tofTextArea.setFont(f);
            }});
        tofDecreaseFontSizeMenuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                // make the font size in the TOF text area one point smaller
                Font f = tofTextArea.getFont();
                frostSettings.setValue("tofFontSize",f.getSize () - 1.0f);
                f = f.deriveFont(frostSettings.getFloatValue("tofFontSize"));
                tofTextArea.setFont(f);
            }});
        tofConfigureBoardMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                tofConfigureBoardMenuItem_actionPerformed(e, getSelectedNode());
            } });
        tofDisplayBoardInfoMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                tofDisplayBoardInfoMenuItem_actionPerformed(e);
            } });
        pluginBrowserMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                BrowserFrame browser = new BrowserFrame(true);
                browser.show();
            } });
        helpHelpMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                HelpFrame dlg = new HelpFrame(getInstance());
                dlg.show();
            } });
        helpAboutMenuItem.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                helpAboutMenuItem_actionPerformed(e);
            } });

// construct menu
        // File Menu
        fileMenu.add(fileExitMenuItem);
        // News Menu
        tofMenu.add(tofAutomaticUpdateMenuItem);
        tofMenu.addSeparator();
        tofMenu.add(tofIncreaseFontSizeMenuItem);
        tofMenu.add(tofDecreaseFontSizeMenuItem);
        tofMenu.addSeparator();
        tofMenu.add(tofConfigureBoardMenuItem);
        tofMenu.addSeparator();
        tofMenu.add(tofDisplayBoardInfoMenuItem);
        // Options Menu
        optionsMenu.add(optionsPreferencesMenuItem);
        // Plugin Menu
        pluginMenu.add(pluginBrowserMenuItem);
        // Help Menu
        helpMenu.add(helpHelpMenuItem);
        helpMenu.add(helpAboutMenuItem);
        // add all to bar
        menuBar.add(fileMenu);
        menuBar.add(tofMenu);
        menuBar.add(optionsMenu);
        menuBar.add(pluginMenu);
        menuBar.add(helpMenu);
        this.setJMenuBar(menuBar);
    }

    //------------------------------------------------------------------------
    //------------------------------------------------------------------------

    public static void displayWarning(String message)
    {
        newMessageHeader = " " + message;
    }

    /**
     * Adds either the selected or all files from the attachmentTable to downloads table.
     */
    public void downloadAttachments()
    {
        int[] selectedRows = attachmentTable.getSelectedRows();

        // If no rows are selected, add all attachments to download table
        if( selectedRows.length == 0 )
        {
            for( int i = 0; i < getAttachmentTable().getModel().getRowCount(); i++ )
            {
                String filename = (String)getAttachmentTable().getModel().getValueAt(i, 0);
                String key = (String)getAttachmentTable().getModel().getValueAt(i, 1);
                FrostDownloadItemObject dlItem = new FrostDownloadItemObject( filename,
                                                                              key,
                                                                              getSelectedNode()
                                                                            );
                 boolean added = getDownloadTable().addDownloadItem( dlItem );
            }
        }
        else
        {
            for( int i = 0; i < selectedRows.length; i++ )
            {
                String filename = (String)getAttachmentTable().getModel().getValueAt(selectedRows[i],0);
                String key = (String)getAttachmentTable().getModel().getValueAt(selectedRows[i],1);
                FrostDownloadItemObject dlItem = new FrostDownloadItemObject( filename,
                                                                              key,
                                                                              getSelectedNode()
                                                                            );
                 boolean added = getDownloadTable().addDownloadItem( dlItem );
            }
        }
    }

    /**
     * Adds all boards from the attachedBoardsTable to board list.
     */
    private void downloadBoards()
    {
        System.out.println("adding boards");
        int[] selectedRows = getAttachedBoardsTable().getSelectedRows();

        if( selectedRows.length == 0 )
        {
            // add all rows
            getAttachedBoardsTable().selectAll();
            selectedRows = getAttachedBoardsTable().getSelectedRows();
            if( selectedRows.length == 0 )
                return;
        }
        for( int i = 0; i < selectedRows.length; i++ )
        {
            String name = (String)getAttachedBoardsTable().getModel().getValueAt(selectedRows[i], 0);
            String pubKey = (String)getAttachedBoardsTable().getModel().getValueAt(selectedRows[i], 1);
            String privKey = (String)getAttachedBoardsTable().getModel().getValueAt(selectedRows[i], 2);

            // prepare key vars for creation of FrostBoardObject (val=null if key is empty)
            if( privKey.compareTo("N/A") == 0 || privKey.length() == 0 )
            {
                privKey = null;
            }
            if( pubKey.compareTo("N/A") == 0 || pubKey.length() == 0 )
            {
                pubKey = null;
            }

            // search board in exising boards list
            FrostBoardObject board = getTofTree().getBoardByName( name );

            //ask if we already have the board
            if( board != null )
            {
                if( JOptionPane.showConfirmDialog(this, "You already have a board named " + name + ".\n" +
                                                  "Are you sure you want to download this one over it?",
                                                  "Board already exists",
                                                  JOptionPane.YES_NO_OPTION) != 0 )
                {
                    continue; // next row of table / next attached board
                }
                else
                {
                    // change existing board keys to keys of new board
                    board.setPublicKey( pubKey );
                    board.setPrivateKey( privKey );
                    updateTofTree( board );
                }
            }
            else
            {
                // its a new board
                getTofTree().addNodeToTree( new FrostBoardObject(name, pubKey, privKey) );
            }
        }
    }

    /**
     * Returns true if board is allowed to be updated.
     * Also checks if board update is already running.
     */
    public boolean doUpdate(FrostBoardObject board)
    {
        if( isUpdateAllowed(board) == false )
            return false;

        if( board.isUpdating() )
            return false;

        return true;
    }

    /**
     * Returns true if board is allowed to be updated.
     * Does NOT check if board update is already running.
     */
    public boolean isUpdateAllowed(FrostBoardObject board)
    {
        if( board == null )
            return false;
        // Do not allow folders to update
        if(board.isFolder())
            return false;

        if (board.isSpammed())
            return false;

        return true;
    }

    /**tof / Update*/
    /**
     * Starts the board update threads, getRequest thread and update id thread.
     * Checks for each type of thread if its already running, and starts allowed
     * not-running threads for this board.
     */
    public void updateBoard(FrostBoardObject board)
    {
        if( board == null || board.isFolder() )
            return;

        boolean threadStarted = false;
        ChangeUpdateStateListener listener = new ChangeUpdateStateListener();

        // first download the messages of today
        if( getRunningBoardUpdateThreads().isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_TODAY) == false )
        {
            getRunningBoardUpdateThreads().startMessageDownloadToday(board, frostSettings, listener);
            System.out.println("Starting update (MSG_TODAY) of " + board.toString());
            threadStarted = true;
        }

        // maybe get the files list
        if( !frostSettings.getBoolValue("disableRequests") &&
            !getRunningBoardUpdateThreads().isThreadOfTypeRunning(board, BoardUpdateThread.BOARD_FILE_UPLOAD)
          )
        {
            getRunningBoardUpdateThreads().startBoardFilesUpload(board, frostSettings, listener);
            System.out.println("Starting update (BOARD_UPLOAD) of " + board.toString());
            threadStarted = true;
        }

        if( !frostSettings.getBoolValue("disableDownloads") &&
            !getRunningBoardUpdateThreads().isThreadOfTypeRunning(board, BoardUpdateThread.BOARD_FILE_DNLOAD)
          )
        {
            getRunningBoardUpdateThreads().startBoardFilesDownload(board, frostSettings, listener);
            System.out.println("Starting update (BOARD_DOWNLOAD) of " + board.toString());
            threadStarted = true;
        }

        // finally get the older messages
        if( getRunningBoardUpdateThreads().isThreadOfTypeRunning(board, BoardUpdateThread.MSG_DNLOAD_BACK) == false )
        {
            getRunningBoardUpdateThreads().startMessageDownloadBack(board, frostSettings, listener);
            System.out.println("Starting update (MSG_BACKLOAD) of " + board.toString());
            threadStarted = true;
        }

        // if there was a new thread started, update the lastUpdateStartTimeMillis
        if( threadStarted == true )
        {
            board.setLastUpdateStartMillis( System.currentTimeMillis() );
        }
    }

    /**
     * The listeners changes the 'updating' state of a board if a thread starts/finished.
     */
    private class ChangeUpdateStateListener implements BoardUpdateThreadListener
    {
        public void boardUpdateThreadFinished(final BoardUpdateThread thread)
        {
            int running = getRunningBoardUpdateThreads().getDownloadThreadsForBoard(thread.getTargetBoard()).size();
                //+ getRunningBoardUpdateThreads().getUploadThreadsForBoard(thread.getTargetBoard()).size();
            if( running == 0 )
            {
                // remove update state from board
                thread.getTargetBoard().setUpdating( false );
                SwingUtilities.invokeLater( new Runnable() {
                        public void run()
                        {
                            updateTofTree( thread.getTargetBoard() );
                        } });
            }
        }
        public void boardUpdateThreadStarted(final BoardUpdateThread thread)
        {
            thread.getTargetBoard().setUpdating( true );
            SwingUtilities.invokeLater( new Runnable() {
                    public void run()
                    {
                        updateTofTree( thread.getTargetBoard() );
                    } });
        }
    }

    /**
     * Fires a nodeChanged (redraw) for this board and updates buttons.
     */
    public void updateTofTree(FrostBoardObject board)
    {
        // fire update for node
        DefaultTreeModel model = (DefaultTreeModel)getTofTree().getModel();
        model.nodeChanged( board );
        // also update all parents
        TreeNode parentFolder = (FrostBoardObject)board.getParent();
        if( parentFolder != null )
        {
            model.nodeChanged( parentFolder );
            parentFolder = parentFolder.getParent();
        }

        if( board == getSelectedNode() ) // is the board actually shown?
        {
            updateButtons(board);
        }
    }
    /**
     * Fires a nodeChanged (redraw) for all boards.
     * ONLY used to redraw tree after run of OptionsFrame.
     */
    public void updateTofTree()
    {
        // fire update for node
        DefaultTreeModel model = (DefaultTreeModel)getTofTree().getModel();
        Enumeration e = ((FrostBoardObject)model.getRoot()).depthFirstEnumeration();
        while( e.hasMoreElements() )
        {
            model.nodeChanged( ((FrostBoardObject)e.nextElement()) );
        }
    }

    private void updateButtons(FrostBoardObject board)
    {
        if( board.isReadAccessBoard() )
        {
            tofNewMessageButton.setEnabled(false);
            uploadAddFilesButton.setEnabled(false);
        }
        else
        {
            tofNewMessageButton.setEnabled(true);
            uploadAddFilesButton.setEnabled(true);
        }
    }

    /**
     * Thread is invoked if the Trust or NotTrust button is clicked.
     */
    private class Truster extends Thread
    {
        private Boolean trust;
        private Identity newIdentity;
        private VerifyableMessageObject currentMsg;

        public Truster(Boolean what, VerifyableMessageObject msg)
        {
            trust=what;
            currentMsg = msg;
        }

        public void run()
        {
            String from = currentMsg.getFrom();
            String newState;

            if( trust == null )  newState = "CHECK";
            else if( trust.booleanValue() == true ) newState = "GOOD";
            else newState = "BAD";

            System.out.println("Truster: Setting '"+
                               from+
                               "' to '"+
                               newState+
                               "'.");

            if( trust == null )
            {
                // set enemy/friend to CHECK
                newIdentity = friends.Get(from);
                if( newIdentity==null )
                    newIdentity=enemies.Get(from);

                if( newIdentity == null ) // not found -> paranoia
                {
                    newIdentity = new Identity(currentMsg.getFrom(), currentMsg.getKeyAddress());
                }
                else
                {
                    friends.remove( from );
                    enemies.remove( from );
                }
            }
            else if( friends.containsKey(from) && trust.booleanValue() == false )
            {
                // set friend to bad
                newIdentity = friends.Get(from);
                friends.remove( from );
                enemies.Add( newIdentity );
            }
            else if( enemies.containsKey(from) && trust.booleanValue() == true )
            {
                // set enemy to good
                newIdentity = enemies.Get(from);
                enemies.remove( newIdentity );
                friends.Add( newIdentity );
            }
            else
            {
                // new new enemy/friend
                newIdentity = new Identity(currentMsg.getFrom(), currentMsg.getKeyAddress());
                if( trust.booleanValue() )
                    friends.Add(newIdentity);
                else
                    enemies.Add(newIdentity);
            }

            if( newIdentity == null || Identity.NA.equals( newIdentity.getKey() ) )
            {
                System.out.println("Truster - ERROR: could not get public key for '"+currentMsg.getFrom()+"'");
                System.out.println("Truster: Will stop to set message states!!!");
                return;
            }

            // get all .txt files in keypool
            ArrayList entries = FileAccess.getAllEntries( new File(frame1.frostSettings.getValue("keypool.dir")),
                                                       ".txt");
            System.out.println("Truster: Starting to update messages:");

            for( int ii=0; ii<entries.size(); ii++ )
            {
                File msgFile = (File)entries.get(ii);
                FrostMessageObject tempMsg = new FrostMessageObject( msgFile );
                if( tempMsg.getFrom().equals(currentMsg.getFrom()) &&
                    (
                      tempMsg.getStatus().trim().equals(VerifyableMessageObject.PENDING) ||
                      tempMsg.getStatus().trim().equals(VerifyableMessageObject.VERIFIED) ||
                      tempMsg.getStatus().trim().equals(VerifyableMessageObject.FAILED)
                    )
                  )
                {
                    // check if message is correctly signed
                    if( newIdentity.getKeyAddress().equals( tempMsg.getKeyAddress() ) &&
                        getCrypto().verify(tempMsg.getContent(), newIdentity.getKey()) )
                    {
                        // set new state of message
                        if( trust == null )
                            tempMsg.setStatus(VerifyableMessageObject.PENDING);
                        else if( trust.booleanValue() )
                            tempMsg.setStatus(VerifyableMessageObject.VERIFIED);
                        else
                            tempMsg.setStatus(VerifyableMessageObject.FAILED);

                        System.out.print("."); // progress
                    }
                    else
                    {
                        System.out.println("\n!Truster: Could not verify message, maybe the message is faked!" +
                                           " Message state set to N/A for '"+msgFile.getPath()+"'.");
                        tempMsg.setStatus(VerifyableMessageObject.NA);
                    }
                }
            }
            // finally step through all board files, count new messages and delete new messages from enemies
            TOF.initialSearchNewMessages();

            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        tofTree_actionPerformed(null);
                    } });
            System.out.println("\nTruster: Finished to update messages, set '"+currentMsg.getFrom()+"' to '"+
                               newState+"'");
        }
    }

    private class checkForSpam extends TimerTask
    {
        public void run()
        {
            if(frostSettings.getBoolValue("doBoardBackoff"))
            {
                Iterator iter = getTofTree().getAllBoards().iterator();
                while (iter.hasNext())
                {
                    FrostBoardObject current = (FrostBoardObject)iter.next();
                    if (current.getBlockedCount() > frostSettings.getIntValue("spamTreshold"))
                    {
                        //board is spammed
                        System.out.println("######### board '"+current.toString()+"' is spammed, update stops for 24h ############");
                        current.setSpammed(true);
                        // clear spam status in 24 hours
                        timer2.schedule(new ClearSpam(current),24*60*60*1000);

                        //now, kill all threads for board
                        Vector threads = getRunningBoardUpdateThreads().getDownloadThreadsForBoard(current);
                        Iterator i = threads.iterator();
                        while( i.hasNext() )
                        {
                            BoardUpdateThread thread = (BoardUpdateThread)i.next();
                            while( thread.isInterrupted() == false )
                                thread.interrupt();
                        }
                    }
                    current.resetBlocked();
                }
            }
        }
    }

    private class ClearSpam extends TimerTask
    {
        private FrostBoardObject clearMe;

        public ClearSpam(FrostBoardObject which) { clearMe = which; }
        public void run()
        {
            System.out.println("############ clearing spam status for board '"+clearMe.toString()+"' ###########");
            clearMe.setSpammed(false);
        }
    }

    /**TOF Board selected*/
    // TODO:
    // if e == NULL, the method is called by truster or by the reloader after options were changed
    // in this cases we usually should left select the actual message (if one) while reloading the table
    public void tofTree_actionPerformed(TreeSelectionEvent e)
    {
        int i[] = getTofTree().getSelectionRows();
        if( i != null && i.length > 0 )
        {
            frostSettings.setValue("tofTreeSelectedRow", i[0]);
        }

        FrostBoardObject node = (FrostBoardObject)getTofTree().getLastSelectedPathComponent();

        resetMessageViewSplitPanes(); // clear message view

        if(node != null)
        {
            if(node.isFolder() == false)
            {
                // node is a board
                configBoardButton.setEnabled(true);
                tofNewMessageButton.setEnabled(true);
                tofUpdateButton.setEnabled(true);

                saveMessageButton.setEnabled(false);
                removeBoardButton.setEnabled(true);

                updateButtons(node);

                System.out.println( "Board "+node.toString()+" blocked count: "+node.getBlockedCount() );

                renameBoardButton.setEnabled(false);
                tofReplyButton.setEnabled(false);
                downloadAttachmentsButton.setEnabled(false);
                downloadBoardsButton.setEnabled(false);

                // read all messages for this board into message table
                TOF.updateTofTable(node, keypool);
                messageTable.clearSelection();
            }
            else
            {
                // node is a folder
                DefaultTableModel model = (DefaultTableModel)getMessageTable().getModel();
                model.setRowCount( 0 );
                updateMessageCountLabels( node );

                renameBoardButton.setEnabled(true);
                configBoardButton.setEnabled(false);
                tofNewMessageButton.setEnabled(false);
                tofUpdateButton.setEnabled(false);
                if( node.isRoot() )
                {
                    removeBoardButton.setEnabled(false);
                    cutBoardButton.setEnabled(false);
                }
                else
                {
                    removeBoardButton.setEnabled(true);
                    cutBoardButton.setEnabled(true);
                }
            }
        }
    }

    /**
     * starts update for the selected board, or for all childs (and their childs) of a folder
     */
    private void refreshNode(FrostBoardObject node)
    {
        if( node==null )
            return;

        if( node.isFolder() == false )
        {
            if( isUpdateAllowed( node ) )
            {
                updateBoard(node);
            }
        }
        else
        {
            // update all childs recursiv
            Enumeration leafs = node.children();
            while( leafs.hasMoreElements() ) refreshNode((FrostBoardObject)leafs.nextElement());
        }
    }

    /**
     * Removes the given tree node, asks before deleting.
     */
    public void removeNode(FrostBoardObject selectedNode)
    {
        String txt;
        if( selectedNode.isFolder() )
        {
            txt = "Do you really want to delete folder '"+selectedNode.toString()+"' ???" +
                  "\nNOTE: Removing it will also remove all boards/folders inside this folder!!!";
        }
        else
        {
            txt = "Do you really want to delete board '"+selectedNode.toString()+"' ???";
        }

        int answer = JOptionPane.showConfirmDialog(this,
                                      txt,
                                      "Delete '"+selectedNode.toString()+"'?",
                                      JOptionPane.YES_NO_OPTION);
        if( answer == JOptionPane.NO_OPTION )
        {
            return;
        }

        // ask user if to delete board directory also
        boolean deleteDirectory = false;
        String boardRelDir = frostSettings.getValue("keypool.dir")+selectedNode.getBoardFilename();
        if( selectedNode.isFolder() == false )
        {
            txt = "Do you want to delete also the board directory '"+ boardRelDir + "' ?\n"+
                    "This directory contains all received messages and file lists for this board.\n"+
                    "(NOTE: The board MUST not updating to delete it!\n"+
                    "Currently there is no way to stop the updating of a board,\n"+
                    "so please ensure this board is'nt updating right now,\n"+
                    "or you have to live with the consequences ;) )\n\n"+
                    "You can also delete the directory by yourself after shutdown of Frost.";
            answer = JOptionPane.showConfirmDialog(this,
                                          txt,
                                          "Delete directory of '"+selectedNode.toString()+"'?",
                                          JOptionPane.YES_NO_CANCEL_OPTION);
            if( answer == JOptionPane.YES_OPTION )
            {
                deleteDirectory = true;
            }
            else if( answer == JOptionPane.CANCEL_OPTION )
            {
                return;
            }
        }

        // delete node from tree
        getTofTree().removeNode(selectedNode);

        // maybe delete board dir (in a thread, do not block gui)
        if( deleteDirectory )
        {
            if( selectedNode.isUpdating() == false )
            {
                new DeleteWholeDirThread( boardRelDir ).start();
            }
            else
            {
                System.out.println("WARNING: Although if warned, you tried to delete a board with is updating! Skipped ...");
            }
        }
    }

    private class DeleteWholeDirThread extends Thread
    {
        String delDir;
        public DeleteWholeDirThread(String dirToDelete)
        {
            delDir = dirToDelete;
        }
        public void run()
        {
            FileAccess.deleteDir( new File(delDir) );
        }
    }

    /**
     * Opens dialog to rename the board / folder.
     * For boards it checks for double names.
     */
    public void renameNode(FrostBoardObject selected)
    {
        if( selected == null )
            return;
        String newname = null;
        do
        {
            newname = JOptionPane.showInputDialog(this,
                                                  "Please enter the new name:\n",
                                                  selected.toString() );
            if( newname == null )
                return; // cancel
            if( selected.isFolder() == false && // double folder names are ok
                getTofTree().getBoardByName( newname ) != null )
            {
                JOptionPane.showMessageDialog(this, "You already have a board with name '"+newname+"'!\nPlease choose a new name.");
                newname = ""; // loop again
            }
        } while( newname.length()==0 );

        selected.setBoardName(newname);
        updateTofTree( selected );
    }

    public void pasteFromClipboard(FrostBoardObject node)
    {
        if( clipboard == null )
        {
            pasteBoardButton.setEnabled(false);
            return;
        }

        if( getTofTree().pasteFromClipboard(clipboard, node) == true )
        {
            clipboard = null;
            pasteBoardButton.setEnabled(false);
        }
    }

    public void cutNode(FrostBoardObject cuttedNode)
    {
        cuttedNode = getTofTree().cutNode(cuttedNode);
        if( cuttedNode != null )
        {
            clipboard = cuttedNode;
            pasteBoardButton.setEnabled(true);
        }
    }

    /**Get keyTyped for tofTree*/
    public void tofTree_keyPressed(KeyEvent e) {
    char key = e.getKeyChar();
    if (!getTofTree().isEditing())
    {
        if (key == KeyEvent.VK_DELETE)
            removeNode(getSelectedNode());
        if (key == KeyEvent.VK_N)
            getTofTree().createNewBoard(frame1.getInstance());
        if (key == KeyEvent.VK_X)
            cutNode(getSelectedNode());
        if (key == KeyEvent.VK_V)
            pasteFromClipboard(getSelectedNode());
    }
    }

    /**Get keyTyped for downloadTable*/
    public void downloadTable_keyPressed(KeyEvent e) {
    char key = e.getKeyChar();
    if (key == KeyEvent.VK_DELETE && !getDownloadTable().isEditing()) {
        getDownloadTable().removeSelectedChunks();
        getDownloadTable().removeSelectedRows();
    }
    }

    /**valueChanged messageTable (messageTableListModel / TOF)*/
    public void messageTableListModel_valueChanged(ListSelectionEvent e)
    {
        FrostBoardObject selectedBoard = getSelectedNode();
        if( selectedBoard.isFolder() )
            return;
        selectedMessage = TOF.evalSelection(e, messageTable, selectedBoard);
        if( selectedMessage != null )
        {
            displayNewMessageIcon(false);
            downloadAttachmentsButton.setEnabled(false);
            downloadBoardsButton.setEnabled(false);

            lastSelectedMessage = selectedMessage.getSubject();
            if( selectedBoard.isReadAccessBoard() == false )
            {
                tofReplyButton.setEnabled(true);
            }

            if( selectedMessage.getStatus().trim().equals(VerifyableMessageObject.PENDING) )
            {
                trustButton.setEnabled(true);
                notTrustButton.setEnabled(true);
                checkTrustButton.setEnabled(false);
            }
            else if( selectedMessage.getStatus().trim().equals(VerifyableMessageObject.VERIFIED) )
            {
                trustButton.setEnabled(false);
                notTrustButton.setEnabled(true);
                checkTrustButton.setEnabled(true);
            }
            else if( selectedMessage.getStatus().trim().equals(VerifyableMessageObject.FAILED) )
            {
                trustButton.setEnabled(true);
                notTrustButton.setEnabled(false);
                checkTrustButton.setEnabled(true);
            }
            else
            {
                trustButton.setEnabled(false);
                notTrustButton.setEnabled(false);
                checkTrustButton.setEnabled(false);
            }

            String content = selectedMessage.getContent();

            int start = content.indexOf("<attached>");
            int end = content.indexOf("</attached>");
            int bstart = content.indexOf("<board>");
            int bend = content.indexOf("</board>");
            int boardPartLength = bend - bstart; // must be at least 14, 1 char boardname, 2 times " * " and keys="N/A"

// TODO: check for validness of found , e.g. format for boards is:
//  "<board>mp3ogg * SSK@PJONkPZC6a4EuhHE~dP0nYyWK-oPAgM * N/A</board>"

            if( ( start ==-1 || end == -1) && (bstart == -1 || bend ==-1 || boardPartLength < 14) )
            {
                // Move divider to 100% and make it invisible
                attachmentSplitPane.setDividerSize(0);
                attachmentSplitPane.setDividerLocation(1.0);
                boardSplitPane.setDividerSize(0);
                boardSplitPane.setDividerLocation(1.0);

                setTofTextAreaText(selectedMessage.getPlaintext());
            }
            else
            {
                // Attachment available
                if( start != -1 && end != -1 )
                {
                    if( bstart== -1  || bend ==-1 )
                    {
                        boardSplitPane.setDividerSize(0);
                        boardSplitPane.setDividerLocation(1.0);
                    }
                    attachmentSplitPane.setDividerLocation(0.75);
                    attachmentSplitPane.setDividerSize(3);

                    // Add attachments to table
                    ((DefaultTableModel)getAttachmentTable().getModel()).setDataVector(selectedMessage.getAttachments(),
                                                                                       null); // our model does not need this
                    setTofTextAreaText(selectedMessage.getPlaintext());  //was getNewContent()
                    downloadAttachmentsButton.setEnabled(true);
                }
                // Board Available
                if( bstart != -1 && bend != -1 )
                {
                    //only a board, no attachments.
                    if( start== -1  || end ==-1 )
                    {
                        attachmentSplitPane.setDividerSize(0);
                        attachmentSplitPane.setDividerLocation(1.0);
                    }
                    boardSplitPane.setDividerLocation(0.75);
                    boardSplitPane.setDividerSize(3);

                    // Add attachments to table
                    ((DefaultTableModel)getAttachedBoardsTable().getModel()).setDataVector(selectedMessage.getBoards(),
                                                                                           null);
                    setTofTextAreaText(selectedMessage.getPlaintext());  //was getNewContent()
                    downloadBoardsButton.setEnabled(true); //TODO: downloadBoardsButton
                }
            }
            if( content.length() > 0 )
                saveMessageButton.setEnabled(true);
            else
                saveMessageButton.setEnabled(false);
        }
        else
        {
            // no msg selected
            resetMessageViewSplitPanes(); // clear message view
            tofReplyButton.setEnabled(false);
            saveMessageButton.setEnabled(false);
            downloadAttachmentsButton.setEnabled(false);
            downloadBoardsButton.setEnabled(false);
        }
    }

    /**Selects message icon in lower right corner*/
    public static void displayNewMessageIcon(boolean showNewMessageIcon)
    {
        frame1 frame1inst = frame1.getInstance();
        if( showNewMessageIcon )
        {
            frame1inst.setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/newmessage.gif")));
            frame1inst.statusMessageLabel.setIcon(newMessage[0]);
            frame1inst.setTitle("*Frost*");
        }
        else
        {
            frame1inst.setIconImage(Toolkit.getDefaultToolkit().createImage(frame1.class.getResource("/data/jtc.jpg")));
            frame1inst.statusMessageLabel.setIcon(newMessage[1]);
            frame1inst.setTitle("Frost");
        }
    }

    /**timer Action Listener (automatic download)*/
    private void timer_actionPerformed()
    {
        // this method is called by a timer each second, so this counter counts seconds
        counter++;

        // Display welcome message if no boards are available
        if( ((TreeNode)getTofTree().getModel().getRoot()).getChildCount() == 0 )
        {
            attachmentSplitPane.setDividerSize(0);
            attachmentSplitPane.setDividerLocation(1.0);
            setTofTextAreaText(LangRes.getString("Welcome message"));
        }

        //////////////////////////////////////////////////
        //   Misc. stuff
        //////////////////////////////////////////////////
        if( counter%180 == 0 ) // Check uploadTable every 3 minutes
        {
            getUploadTable().removeNotExistingFiles();
        }

        if( counter%300 == 0 && frostSettings.getBoolValue("removeFinishedDownloads") )
        {
            getDownloadTable().removeFinishedDownloads();
        }

        updateDownloadCountLabel();

        //////////////////////////////////////////////////
        //   Automatic TOF update
        //////////////////////////////////////////////////
        if( counter % 3 == 0 && // check all 3 seconds if a board update could be started
            tofAutomaticUpdateMenuItem.isSelected() &&
            getRunningBoardUpdateThreads().getUpdatingBoardCount() <
                    frostSettings.getIntValue("automaticUpdate.concurrentBoardUpdates")
          )
        {
            Vector boards = getTofTree().getAllBoards();
            if( boards.size() > 0 )
            {
                FrostBoardObject actualBoard = selectNextBoard(boards);
                if( actualBoard != null )
                {
                    updateBoard(actualBoard);
                }
            }
        }

        //////////////////////////////////////////////////
        //   Display time in button bar
        //////////////////////////////////////////////////
        timeLabel.setText( new StringBuffer().append(DateFun.getVisibleExtendedDate())
                           .append(" - ").append(DateFun.getFullExtendedTime()).append(" GMT").toString());

        /////////////////////////////////////////////////
        //   Update status bar
        /////////////////////////////////////////////////
        String newText = new StringBuffer()
                 .append(LangRes.getString("Up: ")).append(activeUploadThreads)
                 .append(LangRes.getString("   Down: ")).append(activeDownloadThreads)

                 .append(LangRes.getString("   TOFUP: "))
                 .append(getRunningBoardUpdateThreads().getUploadingBoardCount() )
                 .append("B / ")
                 .append(getRunningBoardUpdateThreads().getRunningUploadThreadCount() )
                 .append("T")

                 .append(LangRes.getString("   TOFDO: "))
                 .append(getRunningBoardUpdateThreads().getUpdatingBoardCount() )
                 .append("B / ")
                 .append( getRunningBoardUpdateThreads().getRunningDownloadThreadCount() )
                 .append("T")

                 .append(LangRes.getString("   Selected board: ")).append(getSelectedNode().toString())
                 .toString();
        statusLabel.setText(newText);

        //////////////////////////////////////////////////
        // Generate CHK's for upload table entries
        //////////////////////////////////////////////////
	/**  Do not generate CHKs, get SHA1 only!*/
        if( isGeneratingCHK() == false ) // do not start another generate if there is already 1 running
        {
            UploadTableModel ulModel = (UploadTableModel)getUploadTable().getModel();
            if( ulModel.getRowCount() > 0 )
            {
                for( int i = 0; i < ulModel.getRowCount(); i++ )
                {
                    FrostUploadItemObject ulItem = (FrostUploadItemObject)ulModel.getRow( i );
                    if( ulItem.getSHA1() == null )
                    {
                        setGeneratingCHK( true );
                        ulItem.setKey( "Working..." );
                        ulModel.updateRow( ulItem );
                        insertThread newInsert = new insertThread(ulItem, frostSettings, false);
                        newInsert.start();
                        break; // start only 1 thread per loop (=second)
                    }
                }
            }
        }

        //////////////////////////////////////////////////
        // Start upload thread
        //////////////////////////////////////////////////
        int activeUthreads = 0;
        synchronized(threadCountLock)
        {
            activeUthreads=activeUploadThreads;
        }
        if( activeUthreads < frostSettings.getIntValue("uploadThreads") )
        {
            UploadTableModel ulModel = (UploadTableModel)getUploadTable().getModel();
            if( ulModel.getRowCount() > 0 )
            {
                for( int i = 0; i < ulModel.getRowCount(); i++ )
                {
                    FrostUploadItemObject ulItem = (FrostUploadItemObject)ulModel.getRow( i );
                    if( ulItem.getState() == FrostUploadItemObject.STATE_REQUESTED &&
                        ulItem.getSHA1() != null )
                    {
                        ulItem.setState( FrostUploadItemObject.STATE_UPLOADING );
                        ulModel.updateRow( ulItem );
                        insertThread newInsert = new insertThread(ulItem, frostSettings, true);
                        newInsert.start();
                        break; // start only 1 thread per loop (=second)
                    }
                }
            }
        }

        //////////////////////////////////////////////////
        // Start download thread
        //////////////////////////////////////////////////
        int activeDthreads = 0;
        synchronized(threadCountLock) {
            activeDthreads=activeDownloadThreads;
        }
        if( counter % 3 == 0 && // check all 3 seconds if a download could be started
            activeDthreads < frostSettings.getIntValue("downloadThreads") &&
            downloadActivateCheckBox.isSelected() )
        {
            // choose first item
            FrostDownloadItemObject dlItem = selectNextDownloadItem();
            if( dlItem != null )
            {
                DownloadTableModel dlModel = (DownloadTableModel)getDownloadTable().getModel();

                dlItem.setState( FrostDownloadItemObject.STATE_TRYING );
                dlModel.updateRow( dlItem );

                requestThread newRequest = new requestThread( dlItem, getDownloadTable() );
                newRequest.start();
            }
        }
    }

    /**
     * Chooses the next FrostBoard to update (automatic update).
     * First sorts by lastUpdateStarted time, then chooses first board
     * that is allowed to update.
     * Used only for automatic updating.
     * Returns NULL if no board to update is found.
     */
    public FrostBoardObject selectNextBoard(Vector boards)
    {
        Collections.sort(boards, lastUpdateStartMillisCmp);
        // now first board in list should be the one with latest update of all
        FrostBoardObject board;
        FrostBoardObject nextBoard = null;

        long curTime = System.currentTimeMillis();
        // get in minutes
        int minUpdateInterval = frostSettings.getIntValue("automaticUpdate.boardsMinimumUpdateInterval");
        // min -> ms
        long minUpdateIntervalMillis = minUpdateInterval * 60 * 1000;

        for (int i = 0; i < boards.size(); i++)
        {
            board = (FrostBoardObject)boards.get(i);
            if( nextBoard == null &&
                doUpdate(board) &&
                (curTime - minUpdateIntervalMillis) > board.getLastUpdateStartMillis() && // minInterval
                ( ( board.isConfigured() && board.getAutoUpdateEnabled() ) || !board.isConfigured() )
              )
            {
                nextBoard = board;
                break;
            }
        }
        if( nextBoard != null )
        {
            System.out.println("*** Automatic board update started for: "+nextBoard.toString());
        }
        else
        {
            System.out.println("*** Automatic board update - no board to update.");
        }
        return nextBoard;
    }

    /**
     * Chooses next download item to start from download table.
     */
    protected FrostDownloadItemObject selectNextDownloadItem()
    {
        DownloadTableModel dlModel = (DownloadTableModel)getDownloadTable().getModel();

        // get the item with state "Waiting", minimum htl and not over maximum htl
        ArrayList waitingItems = new ArrayList();
        for( int i = 0; i < dlModel.getRowCount(); i++ )
        {
            FrostDownloadItemObject dlItem = (FrostDownloadItemObject)dlModel.getRow( i );
            if(
	    ( dlItem.getState() == FrostDownloadItemObject.STATE_WAITING &&
                ( dlItem.getEnableDownload() == null || dlItem.getEnableDownload().booleanValue() == true )
//                && dlItem.getRetries() <= frame1.frostSettings.getIntValue("downloadMaxRetries")
              )
	      ||
	      ((dlItem.getState() == FrostDownloadItemObject.STATE_REQUESTED || dlItem.getState() == FrostDownloadItemObject.STATE_REQUESTING) &&
	      	dlItem.getKey()!=null && ( dlItem.getEnableDownload() == null || dlItem.getEnableDownload().booleanValue() == true )
	      )
	    )
            {
                // check if waittime is expired
                long waittimeMillis = frostSettings.getIntValue("downloadWaittime") * 60 * 1000; // min->millisec
                if( frostSettings.getBoolValue("downloadRestartFailedDownloads") &&
                    (System.currentTimeMillis() - dlItem.getLastDownloadStopTimeMillis()) > waittimeMillis )
                {
                    waitingItems.add( dlItem );
                }
            }
        }
        if( waitingItems.size() == 0 )
            return null;

        if( waitingItems.size() > 1 ) // performance issues
        {
            Collections.sort( waitingItems, downloadDlStopMillisCmp );
        }
        return (FrostDownloadItemObject)waitingItems.get(0);
    }

    /**
     * Used to sort FrostDownloadItemObjects by lastUpdateStartTimeMillis ascending.
     */
    static final Comparator downloadDlStopMillisCmp = new Comparator() {
        public int compare(Object o1, Object o2) {
        FrostDownloadItemObject value1 = (FrostDownloadItemObject)o1;
        FrostDownloadItemObject value2 = (FrostDownloadItemObject)o2;
        if( value1.getLastDownloadStopTimeMillis() > value2.getLastDownloadStopTimeMillis() )
            return 1;
        else if( value1.getLastDownloadStopTimeMillis() < value2.getLastDownloadStopTimeMillis() )
            return -1;
        else
            return 0;
        }
    };

    /**
     * Used to sort FrostBoardObjects by lastUpdateStartMillis ascending.
     */
    static final Comparator lastUpdateStartMillisCmp = new Comparator() {
        public int compare(Object o1, Object o2) {
        FrostBoardObject value1 = (FrostBoardObject)o1;
        FrostBoardObject value2 = (FrostBoardObject)o2;
        if( value1.getLastUpdateStartMillis() > value2.getLastUpdateStartMillis() )
            return 1;
        else if( value1.getLastUpdateStartMillis() < value2.getLastUpdateStartMillis() )
            return -1;
        else
            return 0;
        }
    };

    /**searchTextField Action Listener (search)*/
    private void searchTextField_actionPerformed(ActionEvent e)
    {
        if( searchButton.isEnabled() )
        {
            searchButton_actionPerformed(e);
        }
    }

    /**downloadTextField Action Listener (Download/Quickload)*/
    private void downloadTextField_actionPerformed(ActionEvent e)
    {
        String key = (downloadTextField.getText()).trim();
        if( key.length() > 0 )
        {
            // strip the 'browser' prefix
            String stripMe = "http://127.0.0.1:8888/";
            if( key.startsWith(stripMe) )
            {
                key = key.substring(stripMe.length());
            }
            // strip the 'freenet:' prefix
            stripMe = "freenet:";
            if( key.startsWith(stripMe) )
            {
                key = key.substring(stripMe.length());
            }

            String validkeys[]={"SSK@", "CHK@", "KSK@"};
            int keyType = -1; // invalid

            for( int i = 0; i < validkeys.length; i++ )
            {
                if( key.substring(0, validkeys[i].length()).equals(validkeys[i]) )
                {
                    keyType = i;
                    break;
                }
            }

            if( keyType > -1 )
            {
                // added a way to specify a file name. The filename is preceeded by a colon.
                String fileName;

                int sepIndex = key.lastIndexOf(":");

                if( sepIndex != -1 )
                {
                    fileName = key.substring(sepIndex + 1);
                    key = key.substring(0, sepIndex);
                }
                // take the filename from the last part the SSK or KSK
                else if( -1 != (sepIndex = key.lastIndexOf("/")) )
                {
                    fileName = key.substring(sepIndex + 1);
                }
                else
                {
                    fileName = key.substring(4);
                }

                // remove filename from key for CHK
/*                if( keyType == 1 ) // CHK?
                    key = key.substring(0, key.indexOf("/") );*/

                // add valid key to download table
                FrostDownloadItemObject dlItem = new FrostDownloadItemObject( fileName, //users weren't happy with '_'
                                                                              key,
                                                                              null );
                boolean isAdded = getDownloadTable().addDownloadItem( dlItem );

                if( isAdded == true )
                    downloadTextField.setText("");
            }
            else
            {
                // show messagebox that key is invalid
                String keylist = "";
                for( int i=0; i < validkeys.length; i++ )
                {
                    if( i > 0 )
                        keylist += ", ";
                    keylist += validkeys[i];
                }
                JOptionPane.showMessageDialog(this,
                                              LangRes.getString("Invalid key.  Key must begin with one of") + ": "  + keylist,
                                              LangRes.getString("Invalid key"),
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**searchButton Action Listener (Search)*/
    private void searchButton_actionPerformed(ActionEvent e)
    {
        searchButton.setEnabled(false);
        getSearchTable().clearSelection();
        ((SearchTableModel)getSearchTable().getModel()).clearDataModel();
        Vector boardsToSearch;
        if( searchAllBoardsCheckBox.isSelected() )
        {
            // search in all boards
            boardsToSearch = getTofTree().getAllBoards();
        }
        else
        {
            if( getSelectedNode().isFolder() == false )
            {
                // search in selected board
                boardsToSearch = new Vector();
                boardsToSearch.add( getSelectedNode() );
            }
            else
            {
                // search in all boards below the selected folder
                Enumeration enu = getSelectedNode().depthFirstEnumeration();
                boardsToSearch = new Vector();
                while( enu.hasMoreElements() )
                {
                    FrostBoardObject b = (FrostBoardObject)enu.nextElement();
                    if( b.isFolder() == false )
                    {
                        boardsToSearch.add( b );
                    }
                }
            }
        }

        SearchThread searchThread = new SearchThread(searchTextField.getText(),
                                                     boardsToSearch,
                                                     keypool,
                                                     (String)searchComboBox.getSelectedItem() );
        searchThread.start();
    }

    /**tofNewMessageButton Action Listener (tof/ New Message)*/
    private void tofNewMessageButton_actionPerformed(ActionEvent e)
    {
        String subject = "No subject";

        if( frostSettings.getBoolValue("useAltEdit") )
        {
            // TODO: pass FrostBoardObject
            altEdit = new AltEdit(getSelectedNode(),
                                  subject, // subject
                                  "", // new msg
                                  frostSettings,
                                  this);
            altEdit.start();
        }
        else
        {
            MessageFrame newMessage = new MessageFrame(getSelectedNode(),
                                                       frostSettings.getValue("userName"),
                                                       subject, // subject
                                                       "",
                                                       frostSettings,
                                                       this);
            newMessage.show();
        }
    }

    /**tofReplyButton Action Listener (tof/Reply)*/
    private void tofReplyButton_actionPerformed(ActionEvent e)
    {
        String subject = lastSelectedMessage;

        if( subject.startsWith("Re:") == false )
            subject = "Re: " + subject;

        if( frostSettings.getBoolValue("useAltEdit") )
        {
            altEdit = new AltEdit(getSelectedNode(),
                                  subject, // subject
                                  getTofTextAreaText(),
                                  frostSettings,
                                  this);
            altEdit.start();
        }
        else
        {
            MessageFrame newMessage = new MessageFrame(getSelectedNode(),
                                                       frostSettings.getValue("userName"),
                                                       subject, // subject
                                                       getTofTextAreaText(),
                                                       frostSettings,
                                                       this);
            newMessage.show();
        }
    }

    private void tofDisplayBoardInfoMenuItem_actionPerformed(ActionEvent e)
    {
        if( BoardInfoFrame.isDialogShowing() == false )
        {
            BoardInfoFrame boardInfo = new BoardInfoFrame(this);
            boardInfo.startDialog();
        }
    }

    //------------------------------------------------------------------------

    private void uploadAddFilesButton_actionPerformed(ActionEvent e)
    {
        FrostBoardObject board = getSelectedNode();
        if( board.isFolder() )
            return;

        final JFileChooser fc = new JFileChooser(frostSettings.getValue("lastUsedDirectory"));
        fc.setDialogTitle("Select files you want to upload to the " + board.toString() + " board.");
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        fc.setPreferredSize(new Dimension(600, 400));

        int returnVal = fc.showOpenDialog(frame1.this);
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File file = fc.getSelectedFile();
            if( file != null )
            {
                frostSettings.setValue("lastUsedDirectory", file.getParent());
                File[] selectedFiles = fc.getSelectedFiles();

                for( int i = 0; i < selectedFiles.length; i++ )
                {
                    // collect all choosed files + files in all choosed directories
                    ArrayList allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
                    for( int j = 0; j < allFiles.size(); j++ )
                    {
                        File newFile = (File)allFiles.get(j);
                        if( newFile.isFile() && newFile.length() > 0 )
                        {
                            FrostUploadItemObject ulItem = new FrostUploadItemObject( newFile, board );
                            boolean isAdded = getUploadTable().addUploadItem( ulItem );
                        }
                    }
                }
            }
        }
    }

    /**File | Exit action performed*/
    private void fileExitMenuItem_actionPerformed(ActionEvent e) {
    // Remove the tray icon
    try {
        Process process = Runtime.getRuntime().exec("exec" + fileSeparator + "SystemTrayKill.exe");
    }catch(IOException _IoExc) { }

    System.exit(0);
    }

    /**News | Configure Board action performed*/
    private void tofConfigureBoardMenuItem_actionPerformed(ActionEvent e, FrostBoardObject board)
    {
        if( board == null || board.isFolder() )
            return;

        BoardSettingsFrame newFrame = new BoardSettingsFrame(this, board);
        if( newFrame.runDialog() == true ) // OK pressed?
        {
            updateTofTree(board);
            // update the new msg. count for board
            TOF.initialSearchNewMessages(board);

            if( board == getSelectedNode() )
            {
                // reload all messages if board is shown
                tofTree_actionPerformed(null);
            }
        }
    }

    /**Options | Preferences action performed*/
    private void optionsPreferencesMenuItem_actionPerformed(ActionEvent e)
    {
        saveSettings();
        OptionsFrame optionsDlg = new OptionsFrame(this);
        boolean okPressed = optionsDlg.runDialog();
        if( okPressed )
        {
            // read new settings
            frostSettings.readSettingsFile();

            // check if signed only+hideCheck+hideBad or blocking words settings changed
            if( optionsDlg.shouldReloadMessages() )
            {
                // update the new msg. count for all boards
                TOF.initialSearchNewMessages();
                // reload all messages
                tofTree_actionPerformed(null);
            }

            updateTofTree(); // redraw whole tree, in case the update visualization was enabled or disabled (or others)

            // check if we switched from disableRequests=true to =false (requests now enabled)
            if( optionsDlg.shouldRemoveDummyReqFiles() )
            {
                new RemoveDummyRequestFiles().start();
            }

            // update gui parts
            updateOptionsAffectedComponents();
        }
        oldMessageHeader = "";
    }

    /**
     * Search through .req files of this day in all boards and remove the
     * dummy .req files that are created by requestThread on key collosions.
     */
    private class RemoveDummyRequestFiles extends Thread
    {
        public void run()
        {
            Iterator i = getTofTree().getAllBoards().iterator();

            while( i.hasNext() )
            {
                FrostBoardObject board = (FrostBoardObject)i.next();

                String destination = new StringBuffer().append(frame1.keypool)
                                                       .append(board.getBoardFilename())
                                                       .append(fileSeparator)
                                                       .append(DateFun.getDate())
                                                       .append(fileSeparator)
                                                       .toString();
                File boarddir = new File( destination );
                if( boarddir.isDirectory() )
                {
                    File[] entries = boarddir.listFiles();
                    for( int x=0; x<entries.length; x++ )
                    {
                        File entry = entries[x];

                        if( entry.getName().endsWith(".req.sha") &&
                            FileAccess.readFileRaw( entry ).indexOf( requestThread.KEYCOLL_INDICATOR ) > -1 )
                        {
                            entry.delete();
                        }
                    }
                }
            }
        }
    }

    /**Help | About action performed*/
    private void helpAboutMenuItem_actionPerformed(ActionEvent e) {
    AboutBox dlg = new AboutBox(this);
    dlg.setModal(true);
    dlg.show();
    }

    /**Overridden so we can exit when window is closed*/
    protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
        fileExitMenuItem_actionPerformed(null);
    }
    }

    //------------------------------------------------------------------------
    //------------------------------------------------------------------------

    /**Save on exit*/
    private void saveOnExit()
    {
        System.out.println("Saving settings ...");
        saveSettings();
        System.out.println("Bye!");
    }

    /**Save settings*/
    private void saveSettings()
    {
        frostSettings.setValue("downloadingActivated", downloadActivateCheckBox.isSelected());
        //      frostSettings.setValue("uploadingActivated", uploadActivateCheckBox.isSelected());
        frostSettings.setValue("searchAllBoards", searchAllBoardsCheckBox.isSelected());
        //      frostSettings.setValue("reducedBlockCheck", reducedBlockCheckCheckBox.isSelected());
        frostSettings.setValue("automaticUpdate", tofAutomaticUpdateMenuItem.isSelected());

        // save size and location of window
        Dimension actSize = getSize();
//        Point actPos = this.getLocationOnScreen();
        frostSettings.setValue("lastFrameHeight", ""+(int)actSize.getHeight() );
        frostSettings.setValue("lastFrameWidth", ""+(int)actSize.getWidth() );
//        frostSettings.setValue("lastFrameLocX", ""+(int)actPos.getX() );
//        frostSettings.setValue("lastFrameLocY", ""+(int)actPos.getY() );

        frostSettings.writeSettingsFile();
        getTofTree().saveTree();
        getDownloadTable().save();
        getUploadTable().save();
    }

    class PopupListener extends MouseAdapter {
    public void mousePressed(MouseEvent e)
    {
        if( e.getClickCount() == 2 )
        {
            // Start file from download table
            if( e.getComponent().equals(getDownloadTable()) )
            {
                int clickedCol = getDownloadTable().columnAtPoint( e.getPoint() );
                int modelIx = getDownloadTable().getColumnModel().getColumn(clickedCol).getModelIndex();
                if( modelIx == 0 )
                    return;

                DownloadTableModel dlModel = (DownloadTableModel)getDownloadTable().getModel();
                FrostDownloadItemObject dlItem = (FrostDownloadItemObject)dlModel.getRow( getDownloadTable().getSelectedRow() );
                String execFilename = new StringBuffer()
                                            .append(System.getProperty("user.dir") )
                                            .append(fileSeparator)
                                            .append(frostSettings.getValue("downloadDirectory"))
                                            .append(dlItem.getFileName()).toString();
                File file = new File( execFilename );
                System.out.println("Executing: " + file.getPath());
                if( file.exists() )
                {
                    Execute.run("exec.bat" + " " + file.getPath());
                }
            }

            // Start file from upload table
            if( e.getComponent().equals(getUploadTable()) )
            {
                UploadTableModel ulModel = (UploadTableModel)getUploadTable().getModel();
                FrostUploadItemObject ulItem = (FrostUploadItemObject)ulModel.getRow( getUploadTable().getSelectedRow() );
                File file = new File( ulItem.getFilePath() );
                System.out.println("Executing: "+file.getPath());
                if( file.exists() )
                {
                    Execute.run("exec.bat" + " " + file.getPath());
                }
            }

            // Add search result to download table
            if( e.getComponent().equals(searchTable) )
            {
                getSearchTable().addSelectedSearchItemsToDownloadTable( getDownloadTable() );
            }

        }
        else
        {
            maybeShowPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if( e.isPopupTrigger() == false )
        {
            return;
        }
        else if( e.getComponent().equals(getUploadTable()) )
        { // Upload Popup
            showUploadTablePopupMenu(e);
        }
        else if( e.getComponent().equals(searchTable) )
        { // Search Popup
            showSearchTablePopupMenu(e);
        }
        else if( e.getComponent().equals(getDownloadTable()) )
        { // Downloads Popup
            showDownloadTablePopupMenu(e);
        }
        else if( e.getComponent().equals(tofTextArea) )
        { // TOF text popup
            showTofTextAreaPopupMenu(e);
        }
        else if( e.getComponent().equals(boardTable) )
        {// Board attached popup
            showAttachmentBoardPopupMenu(e);
        }
        else if( e.getComponent().equals(attachmentTable) )
        {// Board attached popup
            showAttachmentTablePopupMenu(e);
        }
        else if( e.getComponent().equals(getTofTree()) )
        { // TOF tree popup
            showTofTreePopupMenu(e);
        }
        else if( e.getComponent().equals(messageTable) )
        { // TOF tree popup
            showMessageTablePopupMenu(e);
        }
    }
    } // end of class popuplistener

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    //System.out.println("Clipboard contents replaced");
    }

    /**
     * Method that update the Msg and New counts for tof table
     * Expects that the boards messages are shown in table
     */
    public void updateMessageCountLabels(FrostBoardObject board)
    {
        if( board.isFolder() == true )
        {
            allMessagesCountLabel.setText("");
            newMessagesCountLabel.setText("");
        }
        else
        {
            DefaultTableModel model = (DefaultTableModel)messageTable.getModel();

            int allMessages = model.getRowCount();
            allMessagesCountLabel.setText(allMessagesCountPrefix + allMessages);

            int newMessages = board.getNewMessageCount();
            newMessagesCountLabel.setText(newMessagesCountPrefix + newMessages);
        }
    }

    /**
     * Updates the search result count.
     * Called by search thread.
     */
    public void updateSearchResultCountLabel()
    {
        DefaultTableModel model = (DefaultTableModel)searchTable.getModel();
        int searchResults = model.getRowCount();
        if( searchResults == 0 )
        {
            searchResultsCountLabel.setText(searchResultsCountPrefix + "0");
        }
        searchResultsCountLabel.setText(searchResultsCountPrefix + searchResults);
    }

    /**
     * Updates the download items count label. The label shows all WAITING items in download table.
     * Called periodically by timer_actionPerformed().
     */
    public void updateDownloadCountLabel()
    {
        if( frostSettings.getBoolValue("disableDownloads") == true )
            return;

        DownloadTableModel model = (DownloadTableModel)getDownloadTable().getModel();
        int waitingItems = 0;
        for(int x=0; x<model.getRowCount(); x++)
        {
            FrostDownloadItemObject dlItem = (FrostDownloadItemObject)model.getRow(x);
            if( dlItem.getState() == FrostDownloadItemObject.STATE_WAITING )
            {
                waitingItems++;
            }
        }
        String s = new StringBuffer().append(LangRes.getString("Waiting"))
                                            .append(" : ")
                                            .append( waitingItems ).toString();
        downloadItemCountLabel.setText( s );
    }

    private void trustButton_actionPerformed(ActionEvent e)
    {
        if( selectedMessage!=null )
        {
            if( enemies.containsKey(selectedMessage.getFrom()) )
            {
                if( JOptionPane.showConfirmDialog(getInstance(),
                              "are you sure you want to grant trust to user " +
                              selectedMessage.getFrom().substring(0,selectedMessage.getFrom().indexOf("@")) +
                              " ? \n If you choose yes, future messages from this user will be marked GOOD",
                              "re-grant trust",
                              JOptionPane.YES_NO_OPTION) != 0 )
                {
                    return;
                }
            }
            else
            {
                new Truster(Boolean.valueOf(true), selectedMessage).start();
            }
        }
        trustButton.setEnabled(false);
        notTrustButton.setEnabled(false);
        checkTrustButton.setEnabled(false);
    }

    private void notTrustButton_actionPerformed(ActionEvent e)
    {
        if( selectedMessage!=null )
        {
            if( friends.containsKey(selectedMessage.getFrom()) )
            {
                if( JOptionPane.showConfirmDialog(getInstance(),
                          "Are you sure you want to revoke trust to user " +
                          selectedMessage.getFrom().substring(0,selectedMessage.getFrom().indexOf("@")) +
                          " ? \n If you choose yes, future messages from this user will be marked BAD",
                          "revoke trust",
                          JOptionPane.YES_NO_OPTION) != 0 )
                {
                    return;
                }
            }
            else
            {
                new Truster(Boolean.valueOf(false), selectedMessage).start();
            }
        }
        trustButton.setEnabled(false);
        notTrustButton.setEnabled(false);
        checkTrustButton.setEnabled(false);
    }

    private void checkTrustButton_actionPerformed(ActionEvent e)
    {
        trustButton.setEnabled(false);
        notTrustButton.setEnabled(false);
        checkTrustButton.setEnabled(false);
        if( selectedMessage!=null )
        {
            new Truster(null, selectedMessage).start();
        }
    }

    /**
     * Tries to send old messages that have not been sent yet
     */
    protected void resendFailedMessages()
    {
        // start a thread that waits some seconds for gui to appear, then searches for
        // unsent messages
        ResendFailedMessagesThread t = new ResendFailedMessagesThread(this);
        t.start();
    }

    class ResendFailedMessagesThread extends Thread
    {
        Frame frameToLock;
        public ResendFailedMessagesThread(Frame frameToLock)
        {
            this.frameToLock = frameToLock;
        }
        public void run()
        {
            // give gui a chance to appear ... then start searching for unsent messages
            try { Thread.sleep(10000); } // wait 10 seconds
            catch(InterruptedException ex) { ; }
            if( isInterrupted() )
                return;

            System.out.println("Starting search for unsent messages ...");

            ArrayList entries = FileAccess.getAllEntries(new File(frostSettings.getValue("unsent.dir")), ".txt");

            for( int i = 0; i < entries.size(); i++ )
            {
                File unsentMsgFile = (File)entries.get(i);
                if( unsentMsgFile.getName().startsWith("unsent") )
                {
                    // Resend message
                    VerifyableMessageObject mo = new VerifyableMessageObject(unsentMsgFile);
                    if( mo.isValid() )
                    {
                        FrostBoardObject board = getTofTree().getBoardByName( mo.getBoard() );
                        if( board == null )
                        {
                            System.out.println("Can't resend Message '"+mo.getSubject()+"', the target board '"+mo.getBoard()+
                                               "' was not found in your boardlist.");
                            // TODO: maybe delete msg? or it will always be retried to send
                            continue;
                        }
                        // message will be resigned before send, actual date/time will be used
                        // no more faking here :)
                        getRunningBoardUpdateThreads().startMessageUpload(
                            board,
                            mo.getFrom(),
                            mo.getSubject(),
                            mo.getContent(),
                            "",
                            frostSettings,
                            frameToLock,
                            null);
                        System.out.println("Message '" + mo.getSubject() + "' will be resent to board '"+board.toString()+"'.");
                    }
                    // check if upload was successful before deleting the file -
                    // is not needed, the upload thread creates new unsent file
                    unsentMsgFile.delete();
                }
            }
            System.out.println("Finished search for unsent messages ...");
        }
    }

    /**
     * Called after the OptionsFrame changed some settings to reflect
     * the new settings in the GUI.
     *
     * E.g. if downloads are disabled, it removes the tabbed panes
     * 'Search' and 'Downloads'
     */
    protected void updateOptionsAffectedComponents()
    {
        if( frostSettings.getBoolValue("disableDownloads") == false )
        {
            // search + downloads enabled
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Search")), true );
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Downloads")), true );
        }
        else
        {
            // search + downloads disabled
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Search")), false );
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Downloads")), false );
        }

        if( frostSettings.getBoolValue("disableRequests") == false )
        {
            // uploads enabled
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Uploads")), true );
        }
        else
        {
            // uploads disabled
            tabbedPane.setEnabledAt( tabbedPane.indexOfTab(LangRes.getString("Uploads")), false );
        }
    }

    protected void showTofTreePopupMenu(MouseEvent e)
    {
        int selRow = getTofTree().getRowForLocation(e.getX(), e.getY());
        if( selRow == -1 )
        {
            // no node is clicked -> no menu
            return;
        }

        TreePath selPath = getTofTree().getPathForLocation(e.getX(), e.getY());
        final FrostBoardObject board = (FrostBoardObject)selPath.getLastPathComponent();

        // create menu objects
        String dtxt = ((board.isFolder())?"Folder":"Board");
        JMenuItem description = new JMenuItem(dtxt+": "+board.toString());
        String dtxt2 = ((board.isFolder())?"folder":"board");
        description.setEnabled(false);
        JMenuItem tofTreePopupRefresh = new JMenuItem("Refresh "+dtxt2);
        JMenuItem tofTreePopupAddNode = new JMenuItem("Add new board"); // TODO: translate
        JMenuItem tofTreePopupAddFolder = new JMenuItem("Add new folder");
        JMenuItem tofTreePopupRemoveNode = new JMenuItem("Remove "+dtxt2);
        JMenuItem tofTreePopupCutNode = new JMenuItem("Cut "+dtxt2);

        JMenuItem tofTreePopupPasteNode = null;
        if( clipboard != null )
        {
            String dtxt3 = ((clipboard.isFolder())?"folder":"board");
            tofTreePopupPasteNode = new JMenuItem("Paste "+dtxt3+" '"+clipboard.toString()+"'");
            tofTreePopupPasteNode.addActionListener(new ActionListener()  {
                public void actionPerformed(ActionEvent e) {
                    pasteFromClipboard(board);
                } });
        }

        JMenuItem tofTreePopupConfigureBoard = new JMenuItem(LangRes.getString("Configure selected board"));
        JMenuItem tofTreePopupCancel = new JMenuItem(LangRes.getString("Cancel"));
        // add action listeners
        tofTreePopupAddNode.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getTofTree().createNewBoard(frame1.getInstance());
            } });
        tofTreePopupAddFolder.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                getTofTree().createNewFolder(frame1.getInstance());
            } });
        tofTreePopupRefresh.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                refreshNode( board );
            } });
        tofTreePopupRemoveNode.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                removeNode(board);
            } });
        tofTreePopupCutNode.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                cutNode(board);
            } });
        tofTreePopupConfigureBoard.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                tofConfigureBoardMenuItem_actionPerformed(e, board);
            } });

        JPopupMenu pmenu = new JPopupMenu();
        pmenu.add(description);
        pmenu.addSeparator();
        pmenu.add(tofTreePopupRefresh);
        if( board.isFolder() == false )
        {
            pmenu.addSeparator();
            pmenu.add(tofTreePopupConfigureBoard);
        }
        pmenu.addSeparator();
        pmenu.add(tofTreePopupAddNode);
        pmenu.add(tofTreePopupAddFolder);
        if( board.isRoot() == false )
        {
            pmenu.add(tofTreePopupRemoveNode);
        }
        pmenu.addSeparator();
        if( board.isRoot() == false )
        {
            pmenu.add(tofTreePopupCutNode);
        }
        if( clipboard != null && tofTreePopupPasteNode != null && board.isFolder() )
        {
            pmenu.add(tofTreePopupPasteNode);
        }
        pmenu.addSeparator();
        pmenu.add(tofTreePopupCancel);
        pmenu.show( e.getComponent(), e.getX(), e.getY() );
    }

    protected void showAttachmentTablePopupMenu(MouseEvent e)
    {
        JPopupMenu pmenu = new JPopupMenu();
        if (attachmentTable.getSelectedRow() == -1)
        {
            pmenu.add(tofTextPopupSaveAttachments);
        }
        else
        {
            pmenu.add(tofTextPopupSaveAttachment);
        }
        pmenu.addSeparator();
        pmenu.add(tofTextPopupCancel);
        pmenu.show( e.getComponent(), e.getX(), e.getY() );
    }

    protected void showAttachmentBoardPopupMenu(MouseEvent e)
    {
        JPopupMenu pmenu = new JPopupMenu();
        if (boardTable.getSelectedRow() == -1)
        {
            pmenu.add(tofTextPopupSaveBoards);
        }
        else
        {
            pmenu.add(tofTextPopupSaveBoard);
        }
        pmenu.addSeparator();
        pmenu.add(tofTextPopupCancel);
        pmenu.show( e.getComponent(), e.getX(), e.getY() );
    }

    protected void showTofTextAreaPopupMenu(MouseEvent e)
    {
        if(selectedMessage == null ||  selectedMessage.getContent() == null)
            return; // no menu

        JPopupMenu pmenu = new JPopupMenu();
        pmenu.add(tofTextPopupSaveMessage);
        pmenu.addSeparator();
        pmenu.add(tofTextPopupCancel);
        pmenu.show( e.getComponent(), e.getX(), e.getY() );
    }

    protected void showDownloadTablePopupMenu(MouseEvent e)
    {
        JPopupMenu pmenu = new JPopupMenu();
        if (getDownloadTable().getSelectedRow() > -1)
        {
            pmenu.add(downloadPopupRestartSelectedDownloads);
            pmenu.addSeparator();
        }

        JMenu enabledSubMenu = new JMenu("Enable downloads ...");
        if (getDownloadTable().getSelectedRow() > -1)
        {
            enabledSubMenu.add( downloadPopupEnableSelectedDownloads );
            enabledSubMenu.add( downloadPopupDisableSelectedDownloads );
            enabledSubMenu.add( downloadPopupInvertEnabledSelected );
            enabledSubMenu.addSeparator();
        }
        enabledSubMenu.add( downloadPopupEnableAllDownloads );
        enabledSubMenu.add( downloadPopupDisableAllDownloads );
        enabledSubMenu.add( downloadPopupInvertEnabledAll );
        pmenu.add(enabledSubMenu);

        JMenu removeSubMenu = new JMenu("Remove ...");
        if (getDownloadTable().getSelectedRow() > -1)
        {
            removeSubMenu.add(downloadPopupRemoveSelectedDownloads);
        }
        removeSubMenu.add(downloadPopupRemoveAllDownloads);
        pmenu.add(removeSubMenu);

        pmenu.addSeparator();
        pmenu.add(downloadPopupRemoveFinished);
        pmenu.addSeparator();
        pmenu.add(downloadPopupCancel);
        pmenu.show( e.getComponent(), e.getX(), e.getY() );
    }

    protected void showUploadTablePopupMenu(MouseEvent e)
    {
        JPopupMenu pmenu = new JPopupMenu();

        JMenu removeSubMenu = new JMenu("Remove ...");
        if (getUploadTable().getSelectedRow() > -1) {
            removeSubMenu.add(uploadPopupRemoveSelectedFiles);
        }
        removeSubMenu.add(uploadPopupRemoveAllFiles);

        pmenu.add(removeSubMenu);
        pmenu.addSeparator();
        if (getUploadTable().getSelectedRow() > -1) {
            pmenu.add(uploadPopupReloadSelectedFiles);
        }
        pmenu.add(uploadPopupReloadAllFiles);
        pmenu.addSeparator();
        if (getUploadTable().getSelectedRow() > -1) {
            pmenu.add(uploadPopupSetPrefixForSelectedFiles);
        }
        pmenu.add(uploadPopupSetPrefixForAllFiles);
        pmenu.addSeparator();
        if (getUploadTable().getSelectedRow() > -1) {
            pmenu.add(uploadPopupRestoreDefaultFilenamesForSelectedFiles);
        }
        pmenu.add(uploadPopupRestoreDefaultFilenamesForAllFiles);
        pmenu.addSeparator();
        if (getUploadTable().getSelectedRow() > -1)
        {
            // Add boards to changeDestinationBoard submenu
            Vector boards = getTofTree().getAllBoards();
            Collections.sort(boards);
            uploadPopupChangeDestinationBoard.removeAll();
            for (int i = 0; i < boards.size(); i++)
            {
                final FrostBoardObject aBoard = (FrostBoardObject)boards.elementAt(i);
                JMenuItem boardMenuItem = new JMenuItem(aBoard.toString());
                uploadPopupChangeDestinationBoard.add(boardMenuItem);
                // add all boards to menu + set action listener for each board menu item
                boardMenuItem.addActionListener(new ActionListener()  {
                    public void actionPerformed(ActionEvent e) {
                        // set new board for all selected rows
                        UploadTableModel ulModel = (UploadTableModel)getUploadTable().getModel();
                        int[] selectedRows = getUploadTable().getSelectedRows();
                        for( int x=0; x<selectedRows.length; x++ )
                        {
                            FrostUploadItemObject ulItem = (FrostUploadItemObject)ulModel.getRow( selectedRows[x] );
                            ulItem.setTargetBoard( aBoard );
                            ulModel.updateRow( ulItem );
                        }
                    } });
            }
            pmenu.add(uploadPopupChangeDestinationBoard);
        }
        pmenu.add(uploadPopupAddFilesToBoard);
        pmenu.addSeparator();
        pmenu.add(uploadPopupCancel);
        pmenu.show( e.getComponent(), e.getX(), e.getY() );
    }

    protected void showSearchTablePopupMenu(MouseEvent e)
    {
        JPopupMenu pmenu = new JPopupMenu();

        if (searchTable.getSelectedRow() > -1)
        {
            pmenu.add(searchPopupDownloadSelectedKeys);
            pmenu.addSeparator();
        }
        pmenu.add(searchPopupDownloadAllKeys);
        pmenu.addSeparator();
        if (searchTable.getSelectedRow() > -1)
        {
            pmenu.add(searchPopupCopyAttachment);
            pmenu.addSeparator();
	    pmenu.add(searchPopupSetGood);
	    pmenu.add(searchPopupSetBad);
	    pmenu.addSeparator();
        }
        pmenu.add(searchPopupCancel);
        pmenu.show( e.getComponent(), e.getX(), e.getY() );
    }

    protected void showMessageTablePopupMenu(MouseEvent e)
    {
        JPopupMenu pmenu = new JPopupMenu();

        if(messageTable.getSelectedRow() < 0)
            return;

        pmenu.add(msgTablePopupMarkMessageUnread);
        pmenu.addSeparator();
        pmenu.add(searchPopupCancel); // ATT: misuse of another menuitem displaying 'Cancel' ;)
        pmenu.show( e.getComponent(), e.getX(), e.getY() );
    }

    /**
     * Marks current selected message unread
     */
    private void markSelectedMessageUnread()
    {
        int row = messageTable.getSelectedRow();
        if( row < 0 || selectedMessage == null || getSelectedNode() == null || getSelectedNode().isFolder() == true )
            return;

        FrostMessageObject targetMessage = selectedMessage;

        File msgFile = targetMessage.getFile();
        FileAccess.writeFile("This message is new!", msgFile.getPath() + ".lck");

        messageTable.removeRowSelectionInterval(0, messageTable.getRowCount()-1);

        String from = (String)messageTable.getModel().getValueAt(row, 1);
        if( from.indexOf("<font color=\"blue\">") != -1 )
        {
            StringBuffer sbtmp = new StringBuffer();
            sbtmp.append("<html><font color=\"blue\"><b>");
            sbtmp.append(targetMessage.getFrom());
            sbtmp.append("</b></font></html>");
            messageTable.getModel().setValueAt( sbtmp.toString(), row, 1); // Message with attachment
        }
        else
        {
            StringBuffer sbtmp = new StringBuffer();
            sbtmp.append("<html><b>");
            sbtmp.append(targetMessage.getFrom());
            sbtmp.append("</b></html>");
            messageTable.getModel().setValueAt(sbtmp.toString(), row, 1);
        }

        getSelectedNode().incNewMessageCount();
        updateTofTree( getSelectedNode() );
    }

    /**
     * Marks messages currently in table as read.
     */
/*    private void markAllMessagesRead()
    {
    }*/
}

