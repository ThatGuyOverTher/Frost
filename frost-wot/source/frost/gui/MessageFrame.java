/*
MessageFrame.java / Frost
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

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import frost.*;
import frost.fcp.FcpInsert;
import frost.gui.model.*;
import frost.gui.objects.FrostBoardObject;
import frost.identities.LocalIdentity;
import frost.messages.*;
import frost.storage.StorageException;
import frost.util.gui.*;
import frost.util.gui.translation.*;
import frost.util.gui.translation.UpdatingLanguageResource;

public class MessageFrame extends JFrame
{

    private class AttachBoardsChooser extends JDialog
    {
        JButton Bcancel;
        Vector boards;
        JButton Bok;
        JList Lboards;
        boolean okPressed = false;

        public AttachBoardsChooser(Vector boards)
        {
            super();
            setTitle(languageResource.getString("Choose boards to attach"));
            setModal(true);
            this.boards = boards;
            initGui();
        }
        private void initGui()
        {
            Bok = new JButton("OK");
            Bok.addActionListener( new ActionListener() {
                   public void actionPerformed(ActionEvent e) {
                       	okPressed = true;
                       	setVisible(false);
                   } });
            Bcancel = new JButton("Cancel");
            Bcancel.addActionListener( new ActionListener() {
                   public void actionPerformed(ActionEvent e) {
                       	okPressed = false;
						setVisible(false);
                   } });
            JPanel buttonsPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT, 8, 8) );
            buttonsPanel.add( Bok );
            buttonsPanel.add( Bcancel );

            Lboards = new JList( boards );
            Lboards.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            JScrollPane listScroller = new JScrollPane( Lboards );
            listScroller.setBorder( new CompoundBorder( new EmptyBorder(5,5,5,5),
                                                        new CompoundBorder( new EtchedBorder(),
                                                                            new EmptyBorder(5,5,5,5) )
                                                      ) );
            getContentPane().add(listScroller, BorderLayout.CENTER);
            getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
            setSize(300, 400);
        }
        public Vector runDialog()
        {
            setVisible(true);
            if( okPressed == false )
                return null;

            Object[] sels = Lboards.getSelectedValues();
            Vector chosed = new Vector( Arrays.asList( sels ) );
            return chosed;
        }
    }
    class BuddyComparator implements Comparator
    {
        // compare buddies in lowercase
        public int compare(Object o1, Object o2)
        {
            String s1 = (String)o1;
            String s2 = (String)o2;
            return s1.toLowerCase().compareTo( s2.toLowerCase() );
        }
    }
    
	/**
	 * @author $author$
	 * @version $revision$
	 */
	private class Listener implements MouseListener, LanguageListener {

		protected void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				if (e.getSource() == boardsTable) {
					attBoardsPopupMenu.show(boardsTable, e.getX(), e.getY());
				}
				if (e.getSource() == filesTable) {
					attFilesPopupMenu.show(filesTable, e.getX(), e.getY());
				}
				if (e.getSource() == messageTextArea) {
					getMessageBodyPopupMenu().show(messageTextArea, e.getX(), e.getY());
				}
			}
		}

		public void mouseClicked(MouseEvent event) {
		}

		public void mouseEntered(MouseEvent event) {
		}

		public void mouseExited(MouseEvent event) {
		}

		public void mousePressed(MouseEvent event) {
			maybeShowPopup(event);
		}

		public void mouseReleased(MouseEvent event) {
			maybeShowPopup(event);
		}

		/* (non-Javadoc)
		 * @see frost.util.gui.translation.LanguageListener#languageChanged(frost.util.gui.translation.LanguageEvent)
		 */
		public void languageChanged(LanguageEvent event) {
			refreshLanguage();					
		}
	}
    
	/**
	 * @author $author$
	 * @version $revision$
	 */
	private class MessageBodyPopupMenu 
		extends JSkinnablePopupMenu 
		implements ActionListener, ClipboardOwner {
		
		private Clipboard clipboard;

		private JTextComponent sourceTextComponent;

		private JMenuItem cutItem = new JMenuItem();
		private JMenuItem copyItem = new JMenuItem();
		private JMenuItem pasteItem = new JMenuItem();
		private JMenuItem cancelItem = new JMenuItem();

		/**
		 * @param sourceTextComponent
		 */
		public MessageBodyPopupMenu(JTextComponent sourceTextComponent) {
			super();
			this.sourceTextComponent = sourceTextComponent;
			initialize();
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cutItem) {
				cutSelectedText();
			}
			if (e.getSource() == copyItem) {
				copySelectedText();
			}
			if (e.getSource() == pasteItem) {
				pasteText();
			}
		}
		
		/**
		 * 
		 */
		private void copySelectedText() {
			StringSelection selection = new StringSelection(sourceTextComponent.getSelectedText());
			clipboard.setContents(selection, this);
		}
		
		/**
		 * 
		 */
		private void cutSelectedText() {
			StringSelection selection = new StringSelection(sourceTextComponent.getSelectedText());
			clipboard.setContents(selection, this);
			
			int start = sourceTextComponent.getSelectionStart();
			int end = sourceTextComponent.getSelectionEnd();
			try {
				sourceTextComponent.getDocument().remove(start, end - start);
			} catch (BadLocationException ble) {
				logger.log(Level.SEVERE, "Problem while cutting text.", ble);
			}
		}
		
		/**
		 * 
		 */
		private void pasteText() {
			Transferable clipboardContent = clipboard.getContents(this);
			try {
				String text = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
				
				Caret caret = sourceTextComponent.getCaret();
				int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
				
				Document document = sourceTextComponent.getDocument();
				
				if (document instanceof PlainDocument) {
					((PlainDocument) document).replace(p0, p1 - p0, text, null);
				} else {
					if (p0 != p1) {
						document.remove(p0, p1 - p0);
                    }
					document.insertString(p0, text, null);
				}
			} catch (IOException ioe) {
				logger.log(Level.SEVERE, "Problem while pasting text.", ioe);
			} catch (UnsupportedFlavorException ufe) {
				logger.log(Level.SEVERE, "Problem while pasting text.", ufe);
			} catch (BadLocationException ble) {
				logger.log(Level.SEVERE, "Problem while pasting text.", ble);
			}
		}

		/**
		 *  
		 */
		private void initialize() {
			refreshLanguage();
			
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			clipboard = toolkit.getSystemClipboard();
			
			cutItem.addActionListener(this);
			copyItem.addActionListener(this);
			pasteItem.addActionListener(this);

			add(cutItem);
			add(copyItem);
			add(pasteItem);
			addSeparator();
			add(cancelItem);
		}

		/**
		 *  
		 */
		private void refreshLanguage() {
			cutItem.setText(languageResource.getString("Cut"));
			copyItem.setText(languageResource.getString("Copy"));
			pasteItem.setText(languageResource.getString("Paste"));
			cancelItem.setText(languageResource.getString("Cancel"));
		}
		
		/* (non-Javadoc)
		 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
		 */
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// Nothing here
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
		 */
		public void show(Component invoker, int x, int y) {
			if (sourceTextComponent.getSelectedText() != null) {
				cutItem.setEnabled(true);
				copyItem.setEnabled(true);
			} else {
				cutItem.setEnabled(false);
				copyItem.setEnabled(false);
			}
			Transferable clipboardContent = clipboard.getContents(this);
			if ((clipboardContent != null) &&
					(clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
				pasteItem.setEnabled(true);
			} else {
				pasteItem.setEnabled(false);
			}
			super.show(invoker, x, y);
		}
	}

    private class MFAttachedBoard implements TableMember
    {
        FrostBoardObject aBoard;
        public MFAttachedBoard(FrostBoardObject ab)
        {
            aBoard = ab;
        }
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }
        public FrostBoardObject getBoardObject()
        {
            return aBoard;
        }

		public Object getValueAt(int column) {
			switch (column) {
				case 0 :
					return aBoard.getBoardName();
				case 1 :
					return (aBoard.getPublicKey() == null) ? "N/A" : aBoard.getPublicKey();
				case 2 :
					return (aBoard.getPrivateKey() == null) ? "N/A" : aBoard.getPrivateKey();
				case 3 :
					return (aBoard.getDescription() == null) ? "N/A" : aBoard.getDescription();
			}
			return "*ERR*";
		}
    }

    private class MFAttachedBoardsTable extends SortedTable
    {
        public MFAttachedBoardsTable(MFAttachedBoardsTableModel m)
        {
            super(m);

            // set column sizes
            int[] widths = {250, 80, 80};
            for (int i = 0; i < widths.length; i++)
            {
                getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            // default for sort: sort by name ascending ?
            sortedColumnIndex = 0;
            sortedColumnAscending = true;
            resortTable();
        }
    }
    
    private class MFAttachedBoardsTableModel extends SortedTableModel
    {
        protected final Class columnClasses[] = {
            String.class, 
            String.class,
            String.class,
			String.class
        };
        protected final String columnNames[] = {
            "Boardname",
            "public key",
            "Private key", 
            "Description"
        };

        public MFAttachedBoardsTableModel()
        {
            super();
        }
        public Class getColumnClass(int column)
        {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }
        public int getColumnCount()
        {
            return columnNames.length;
        }

        public String getColumnName(int column)
        {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }

        public boolean isCellEditable(int row, int col)
        {
            return false;
        }
        public void setValueAt(Object aValue, int row, int column) {}
    }
    
    private class MFAttachedFile implements TableMember
    {
        File aFile;
        public MFAttachedFile(File af)
        {
            aFile = af;
        }
        public int compareTo( TableMember anOther, int tableColumIndex )
        {
            Comparable c1 = (Comparable)getValueAt(tableColumIndex);
            Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
            return c1.compareTo( c2 );
        }
        public File getFile()
        {
            return aFile;
        }
        public Object getValueAt(int column)
        {
            switch(column)
            {
                case 0: return aFile.getName();
                case 1: return ""+aFile.length();
            }
            return "*ERR*";
        }
    }
    private class MFAttachedFilesTable extends SortedTable
    {
        public MFAttachedFilesTable(MFAttachedFilesTableModel m)
        {
            super(m);

            // set column sizes
            int[] widths = {250, 80};
            for (int i = 0; i < widths.length; i++)
            {
                getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            // default for sort: sort by name ascending ?
            sortedColumnIndex = 0;
            sortedColumnAscending = true;
            resortTable();
        }
    }
    
    private class MFAttachedFilesTableModel extends SortedTableModel
    {
        protected final Class columnClasses[] = {
            String.class,
            String.class
        };
        protected final String columnNames[] = {
            "Filename",
            "Size"
        };

        public MFAttachedFilesTableModel()
        {
            super();
        }
        public Class getColumnClass(int column)
        {
            if( column >= 0 && column < columnClasses.length )
                return columnClasses[column];
            return null;
        }
        public int getColumnCount()
        {
            return columnNames.length;
        }

        public String getColumnName(int column)
        {
            if( column >= 0 && column < columnNames.length )
                return columnNames[column];
            return null;
        }

        public boolean isCellEditable(int row, int col)
        {
            return false;
        }
        public void setValueAt(Object aValue, int row, int column) {}
    }

	private LocalIdentity myId;

	private static Logger logger = Logger.getLogger(MessageFrame.class.getName());
	
    private UpdatingLanguageResource languageResource;

    private Listener listener = new Listener();

    private boolean initialized = false;
    
    private FrostBoardObject board;
    private String from;
    private String subject;
    private String lastUsedDirectory;
    private String keypool;
    private boolean state;
    private SettingsClass frostSettings;
        
    private MFAttachedBoardsTable boardsTable;
    private MFAttachedFilesTable filesTable;
    private MFAttachedBoardsTableModel boardsTableModel;
    private MFAttachedFilesTableModel filesTableModel;
    
	private JSplitPane messageSplitPane = null;
	private JSplitPane attachmentsSplitPane = null;
	private JScrollPane filesTableScrollPane;
	private JScrollPane boardsTableScrollPane;
    
	private JSkinnablePopupMenu attFilesPopupMenu;
	private JSkinnablePopupMenu attBoardsPopupMenu;
	private MessageBodyPopupMenu messageBodyPopupMenu;
    
	private JButton Bsend = new JButton(new ImageIcon(this.getClass().getResource("/data/send.gif")));
	private JButton Bcancel = new JButton(new ImageIcon(this.getClass().getResource("/data/remove.gif")));
	private JButton BattachFile = new JButton(new ImageIcon(this.getClass().getResource("/data/attachment.gif")));
	private JButton BattachBoard= new JButton(new ImageIcon(MainFrame.class.getResource("/data/attachmentBoard.gif")));

	private JCheckBox sign = new JCheckBox();
    private JCheckBox addAttachedFilesToUploadTable = new JCheckBox();

    private JLabel Lboard = new JLabel();
    private JLabel Lfrom = new JLabel();
    private JLabel Lsubject = new JLabel();    
    private JTextField TFboard = new JTextField(); // Board (To)
    private JTextField fromTextField = new JTextField(); // From
    private JTextField subjectTextField = new JTextField(); // Subject

    private AntialiasedTextArea messageTextArea = new AntialiasedTextArea(); // Text
    private ImmutableArea headerArea = null;
    private String oldSender = null;
    
	/**
	 * @param newSettings
	 * @param parentComponent
	 * @param languageResource
	 * @param newMyId
	 */
	public MessageFrame(SettingsClass newSettings, Component parentComponent,
			UpdatingLanguageResource languageResource, LocalIdentity newMyId) {
		super();
		this.languageResource = languageResource;
		myId = newMyId;
		state = false;
		frostSettings = newSettings;
		lastUsedDirectory = frostSettings.getValue("lastUsedDirectory");
		keypool = frostSettings.getValue("keypool.dir");

		String fontName = frostSettings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
		int fontStyle = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
		int fontSize = frostSettings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
		Font tofFont = new Font(fontName, fontStyle, fontSize);
		if (!tofFont.getFamily().equals(fontName)) {
			logger.severe("The selected font was not found in your system\n"
					+ "That selection will be changed to \"Monospaced\".");
			frostSettings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
			tofFont = new Font("Monospaced", fontStyle, fontSize);
		}
		messageTextArea.setFont(tofFont);
		messageTextArea.setAntiAliasEnabled(frostSettings.getBoolValue("messageBodyAA"));
		ImmutableAreasDocument messageDocument = new ImmutableAreasDocument();
		headerArea = new ImmutableArea(messageDocument);
		messageDocument.addImmutableArea(headerArea); //So that the user can't
													  // modify the header of
													  // the message
		messageTextArea.setDocument(messageDocument);

		setSize(600, 460);
		setLocationRelativeTo(parentComponent);
	}

	private void attachBoards_actionPerformed(ActionEvent e) {
		Vector allBoards = MainFrame.getInstance().getTofTree().getAllBoards();
		if (allBoards.size() == 0)
			return;
		Collections.sort(allBoards);

		AttachBoardsChooser chooser = new AttachBoardsChooser(allBoards);
		chooser.setLocationRelativeTo(this);
		Vector chosedBoards = chooser.runDialog();
		if (chosedBoards == null || chosedBoards.size() == 0) // nothing chosed or cancelled
			{
			return;
		}

		for (int i = 0; i < chosedBoards.size(); i++) {
			FrostBoardObject board = (FrostBoardObject) chosedBoards.get(i);

			String privKey = board.getPrivateKey();

			if (privKey != null) {
				int answer =
					JOptionPane.showConfirmDialog(
						this,
						"You have the private key to "
							+ board.toString()
							+ ".  Are you sure you want it attached?\n "
							+ "If you choose NO, only the public key will be attached.",
						"Include private board key?",
						JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.NO_OPTION) {
					privKey = null; // dont provide privkey
				}
			}
			// build a new board because maybe privKey should'nt be uploaded
			FrostBoardObject aNewBoard =
				new FrostBoardObject(board.getBoardName(), board.getPublicKey(), privKey, board.getDescription());
			MFAttachedBoard ab = new MFAttachedBoard(aNewBoard);
			boardsTableModel.addRow(ab);
		}
		positionDividers();
	}

    /**jButton3 Action Listener (Add attachment(s))*/
    private void attachFile_actionPerformed(ActionEvent e)
    {
        final JFileChooser fc = new JFileChooser(lastUsedDirectory);
        fc.setDialogTitle(languageResource.getString("Choose file(s) / directory(s) to attach"));
        fc.setFileHidingEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);

        int returnVal = fc.showOpenDialog(MessageFrame.this);
        if( returnVal == JFileChooser.APPROVE_OPTION )
        {
            File[] selectedFiles = fc.getSelectedFiles();
            for( int i = 0; i < selectedFiles.length; i++ )
            {
                // for convinience remember last used directory
                lastUsedDirectory = selectedFiles[i].getPath();
                
                // collect all choosed files + files in all choosed directories
                ArrayList allFiles = FileAccess.getAllEntries(selectedFiles[i], "");
                for (int j = 0; j < allFiles.size(); j++) 
                {
                    File aFile = (File)allFiles.get(j);
                    if (aFile.isFile() && aFile.length() > 0) 
                    {
                        MFAttachedFile af = new MFAttachedFile( aFile );
                        filesTableModel.addRow( af );
                    }
                }
            }
        }
        else
        {
            logger.fine("Open command cancelled by user.");
        }
        
        positionDividers();
    }

	/**jButton2 Action Listener (Cancel)*/
    private void cancel_actionPerformed(ActionEvent e)
    {
        state = false;
        dispose();
    }
	
	private void composeMessage(
		FrostBoardObject newBoard,
		String newFrom,
		String newSubject,
		String newText,
		boolean isReply) {
			
		headerArea.setEnabled(false);	
		board = newBoard;
		from = newFrom;
		subject = newSubject;
		String text = newText;

		String date = DateFun.getExtendedDate() + " - " + DateFun.getFullExtendedTime() + "GMT";

		if (isReply) {
			text += "\n\n";
		}
		int headerAreaStart = text.length();//Beginning of non-modifiable area
		text += "----- " + from + " ----- " + date + " -----\n\n";
		int headerAreaEnd = text.length() - 2; //End of non-modifiable area
		oldSender = from;
		
		int caretPos = text.length();

		File signatureFile = new File("signature.txt");
		if (signatureFile.isFile()) {
			String signature = FileAccess.readFile("signature.txt", "UTF-8").trim();
			if (signature.length() > 0) {
				text += "\n-- \n";
				text += signature;
			}
		}

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			initialize();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception thrown in composeMessage(...)", e);
		}

		messageTextArea.setText(text);
		headerArea.setStartPos(headerAreaStart);
		headerArea.setEndPos(headerAreaEnd);
		headerArea.setEnabled(true);
		setVisible(true);

		// reset the splitpanes       
		positionDividers();

		// Properly positions the caret (AKA cursor)
		messageTextArea.requestFocusInWindow();
		messageTextArea.getCaret().setDot(caretPos);
		messageTextArea.getCaret().setVisible(true);
	}
    
	public void composeNewMessage(FrostBoardObject newBoard, String newFrom, String newSubject, String newText) {
		composeMessage(newBoard, newFrom, newSubject, newText, false);
	}
    
	public void composeReply(FrostBoardObject newBoard, String newFrom, String newSubject, String newText) {
			composeMessage(newBoard, newFrom, newSubject, newText, true);
	}
    
	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	public void dispose() {
		if (initialized) {
			languageResource.removeLanguageListener(listener);
			initialized = false;
		}
		super.dispose();
	}
	
	/**
	 * @return
	 */
	private MessageBodyPopupMenu getMessageBodyPopupMenu() {
		if (messageBodyPopupMenu == null) {
			messageBodyPopupMenu = new MessageBodyPopupMenu(messageTextArea);
		}
		return messageBodyPopupMenu;
	}

	private void initialize() throws Exception {
		if (!initialized) {
			refreshLanguage();
			languageResource.addLanguageListener(listener);

			setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/data/newmessage.gif")));
			setResizable(true);

			boardsTableModel = new MFAttachedBoardsTableModel();
			boardsTable = new MFAttachedBoardsTable(boardsTableModel);
			boardsTableScrollPane = new JScrollPane(boardsTable);
			boardsTable.addMouseListener(listener);

			filesTableModel = new MFAttachedFilesTableModel();
			filesTable = new MFAttachedFilesTable(filesTableModel);
			filesTableScrollPane = new JScrollPane(filesTable);
			filesTable.addMouseListener(listener);

			MiscToolkit toolkit = MiscToolkit.getInstance();
			toolkit.configureButton(Bsend, "Send message", "/data/send_rollover.gif", languageResource);
			toolkit.configureButton(Bcancel, "Cancel", "/data/remove_rollover.gif", languageResource);
			toolkit.configureButton(
				BattachFile,
				"Add attachment(s)",
				"/data/attachment_rollover.gif",
				languageResource);
			toolkit.configureButton(
				BattachBoard,
				"Add Board(s)",
				"/data/attachmentBoard_rollover.gif",
				languageResource);

			TFboard.setEditable(false);
			TFboard.setText(board.toString());
			fromTextField.setText(from);

			subjectTextField.setText(subject);
			messageTextArea.setLineWrap(true);
			messageTextArea.setWrapStyleWord(true);
			messageTextArea.addMouseListener(listener);

			// check if last msg was signed and set it to remembered state
			if (from.equals(myId.getUniqueName())) {
				fromTextField.setEditable(false);
				sign.setSelected(true);
			}

			addAttachedFilesToUploadTable.setSelected(false);

			//------------------------------------------------------------------------
			// Actionlistener
			//------------------------------------------------------------------------
			Bsend.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					send_actionPerformed(e);
				}
			});
			Bcancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancel_actionPerformed(e);
				}
			});
			BattachFile.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					attachFile_actionPerformed(e);
				}
			});
			BattachBoard.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					attachBoards_actionPerformed(e);
				}
			});
			sign.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sign_ActionPerformed(e);
				}
			});
			fromTextField.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					updateHeaderArea();
				}
				public void insertUpdate(DocumentEvent e) {
					updateHeaderArea();
				}
				public void removeUpdate(DocumentEvent e) {
					updateHeaderArea();
				}
			});		
			AbstractDocument doc = (AbstractDocument) fromTextField.getDocument();
			doc.setDocumentFilter(new DocumentFilter() {
                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.text.DocumentFilter#insertString(javax.swing.text.DocumentFilter.FilterBypass,
                 *      int, java.lang.String, javax.swing.text.AttributeSet)
                 */
                public void insertString(DocumentFilter.FilterBypass fb, int offset, String string,
                        AttributeSet attr) throws BadLocationException {
                    
                    if (fromTextField.isEditable()) {
                        string = string.replaceAll("@","");
                    }
                    super.insertString(fb, offset, string, attr);

                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass,
                 *      int, int, java.lang.String,
                 *      javax.swing.text.AttributeSet)
                 */
                public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {
                    
                    if (fromTextField.isEditable()) {
                        text = text.replaceAll("@","");
                    }
                    super.replace(fb, offset, length, text, attrs);
                    
                }
            });
			
			//------------------------------------------------------------------------
			// Append objects
			//------------------------------------------------------------------------
			JPanel panelMain = new JPanel(new BorderLayout()); // Main Panel
			JPanel panelTextfields = new JPanel(new BorderLayout()); // Textfields
			JPanel panelToolbar = new JPanel(new BorderLayout()); // Toolbar / Textfields
			JPanel panelLabels = new JPanel(new BorderLayout()); // Labels
			JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

			JScrollPane bodyScrollPane = new JScrollPane(messageTextArea); // Textscrollpane
			bodyScrollPane.setMinimumSize(new Dimension(100, 50));

			panelLabels.add(Lboard, BorderLayout.NORTH);
			panelLabels.add(Lfrom, BorderLayout.CENTER);
			panelLabels.add(Lsubject, BorderLayout.SOUTH);

			panelTextfields.add(TFboard, BorderLayout.NORTH);
			panelTextfields.add(fromTextField, BorderLayout.CENTER);
			panelTextfields.add(subjectTextField, BorderLayout.SOUTH);

			panelButtons.add(Bsend);
			panelButtons.add(Bcancel);
			panelButtons.add(BattachFile);
			panelButtons.add(BattachBoard);
			panelButtons.add(sign);
			panelButtons.add(addAttachedFilesToUploadTable);

			JPanel dummyPanel = new JPanel(new BorderLayout());
			dummyPanel.add(panelLabels, BorderLayout.WEST);
			dummyPanel.add(panelTextfields, BorderLayout.CENTER);

			panelToolbar.add(panelButtons, BorderLayout.NORTH);
			panelToolbar.add(dummyPanel, BorderLayout.SOUTH);

			//Put everything together
			attachmentsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, filesTableScrollPane,
                    boardsTableScrollPane);
            attachmentsSplitPane.setResizeWeight(0.5);
            attachmentsSplitPane.setDividerSize(3);
            attachmentsSplitPane.setDividerLocation(0.5);

            messageSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bodyScrollPane,
                    attachmentsSplitPane);
            messageSplitPane.setDividerSize(0);
            messageSplitPane.setDividerLocation(1.0);
            messageSplitPane.setResizeWeight(1.0);

			panelMain.add(panelToolbar, BorderLayout.NORTH);
			panelMain.add(messageSplitPane, BorderLayout.CENTER);

			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(panelMain, BorderLayout.CENTER);

			initPopupMenu();

			initialized = true;
		}
	}
    
    protected void initPopupMenu()
    {
        attFilesPopupMenu = new JSkinnablePopupMenu();
        attBoardsPopupMenu = new JSkinnablePopupMenu();
        
        JMenuItem removeFiles = new JMenuItem(languageResource.getString("Remove"));
        JMenuItem removeBoards = new JMenuItem(languageResource.getString("Remove"));
        
        removeFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItemsFromTable(filesTable);
            }
        });
        removeBoards.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItemsFromTable(boardsTable);
            }
        });

        attFilesPopupMenu.add( removeFiles );
        attBoardsPopupMenu.add( removeBoards );
    }

	/**
	 * 
     */
    private void positionDividers() {
        int attachedFiles = filesTableModel.getRowCount();
        int attachedBoards = boardsTableModel.getRowCount();
        if (attachedFiles == 0 && attachedBoards == 0) {
            // Neither files nor boards
            messageSplitPane.setBottomComponent(null);
            messageSplitPane.setDividerSize(0);
            return;
        }
        messageSplitPane.setDividerSize(3);
        messageSplitPane.setDividerLocation(0.75);
        if (attachedFiles != 0 && attachedBoards == 0) {
            //Only files
            messageSplitPane.setBottomComponent(filesTableScrollPane);
            return;
        }
        if (attachedFiles == 0 && attachedBoards != 0) {
            //Only boards
            messageSplitPane.setBottomComponent(boardsTableScrollPane);
            return;
        }
        if (attachedFiles != 0 && attachedBoards != 0) {
            //Both files and boards
            messageSplitPane.setBottomComponent(attachmentsSplitPane);
            attachmentsSplitPane.setTopComponent(filesTableScrollPane);
            attachmentsSplitPane.setBottomComponent(boardsTableScrollPane);
        }
    }

    protected void processWindowEvent(WindowEvent e)
    {
        if( e.getID() == WindowEvent.WINDOW_CLOSING )
        {
            dispose();
        }
        super.processWindowEvent(e);
    }
    
    /**
	 * 
	 */
	private void refreshLanguage() {
		setTitle(languageResource.getString("Create message"));
		
		Bsend.setToolTipText(languageResource.getString("Send message"));
		Bcancel.setToolTipText(languageResource.getString("Cancel"));
		BattachFile.setToolTipText(languageResource.getString("Add attachment(s)"));
		BattachBoard.setToolTipText(languageResource.getString("Add Board(s)"));
		
		sign.setText(languageResource.getString("Sign"));
		addAttachedFilesToUploadTable.setText(languageResource.getString("Indexed attachments"));
		
		addAttachedFilesToUploadTable.setToolTipText(
				languageResource.getString("Should file attachments be added to upload table?"));
		
		Lboard.setText(languageResource.getString("Board") + ": ");
		Lfrom.setText(languageResource.getString("From") + ": ");
		Lsubject.setText(languageResource.getString("Subject") + ": ");
	}
        
    protected void removeSelectedItemsFromTable( JTable tbl )
    {
        SortedTableModel m = (SortedTableModel)tbl.getModel();
        int[] sel = tbl.getSelectedRows();
        for(int x=sel.length-1; x>=0; x--)
        {
            m.removeRow(sel[x]);
        }
        positionDividers();
    }
    
    /**jButton1 Action Listener (Send)*/
    private void send_actionPerformed(ActionEvent e)
    {
        from = fromTextField.getText().trim();
		fromTextField.setText(from);
        subject = subjectTextField.getText().trim();
        subjectTextField.setText(subject); // if a pbl occurs show the subject we checked
        String text = messageTextArea.getText().trim();

        boolean quit = true;

        if( subject.equals("No subject") )
        {
            int n = JOptionPane.showConfirmDialog( this,
								languageResource.getString("Do you want to enter a subject?"),
								languageResource.getString("No subject specified!"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
            if( n == JOptionPane.YES_OPTION )
            {
                return;
            }
        }
        
        if( subject.length() == 0)
        {
            JOptionPane.showMessageDialog( this,
								languageResource.getString("You must enter a subject!"),
								languageResource.getString("No subject specified!"),
            					JOptionPane.ERROR);
            return;                               
        }
        if( from.length() == 0)
        {
            JOptionPane.showMessageDialog( this,
								languageResource.getString("You must enter a sender name!"),
								languageResource.getString("No 'From' specified!"),
            					JOptionPane.ERROR);
            return;                               
        }

        // for convinience set last used user (maybe obsolete now)
        frostSettings.setValue("userName", from);
        
        // create new MessageObject to upload
        MessageObject mo = new MessageObject();
        mo.setBoard(board.getBoardName());
        mo.setFrom(from);
        mo.setSubject(subject);
        mo.setContent(text);
        if( sign.isSelected() )
        {
            mo.setPublicKey(myId.getKey());
        }
        // MessageUploadThread will set date + time !
        
        // attach all files and boards the user chosed
        for(int x=0; x < filesTableModel.getRowCount(); x++)
        {
            MFAttachedFile af = (MFAttachedFile)filesTableModel.getRow(x);
            File aChosedFile = af.getFile();
            FrostBoardObject boardObj = null;
            
            SharedFileObject sfo;
            if (aChosedFile.length() > FcpInsert.smallestChunk)
            	sfo = new FECRedirectFileObject(aChosedFile,boardObj);
            else 
            	sfo= new SharedFileObject(aChosedFile, boardObj);
			if( addAttachedFilesToUploadTable.isSelected() )
			{
						sfo.setOwner(sign.isSelected() ?
											Mixed.makeFilename(myId.getUniqueName()) :
											"Anonymous");
			}
			
			
            FileAttachment fa = new FileAttachment(sfo);
            mo.getAttachmentList().add(fa);
        }
        for(int x=0; x < boardsTableModel.getRowCount(); x++)
        {
            MFAttachedBoard ab = (MFAttachedBoard)boardsTableModel.getRow(x);
            FrostBoardObject aChosedBoard = ab.getBoardObject();
            BoardAttachment ba = new BoardAttachment(aChosedBoard);
            mo.getAttachmentList().add(ba);
        }

        // start upload thread which also saves the file, uploads attachments+signs if choosed
        MainFrame.getInstance().getRunningBoardUpdateThreads().startMessageUpload(
                                              board,
                                              mo,
                                              null);

        frostSettings.setValue("lastUsedDirectory", lastUsedDirectory);
        try {
        	frostSettings.save();
		} catch (StorageException se) {
			logger.log(Level.SEVERE, "Error while saving the settings.", se);
		}
        
        state = true; // exit state
        
        setVisible(false);        
        dispose();
    }
    
	/**
	 * @param e
	 */
	private void sign_ActionPerformed(ActionEvent e) {
		String sender;
		if (sign.isSelected()) {
			sender = myId.getUniqueName();
			fromTextField.setEditable(false);
		} else {
			sender = "Anonymous";
			fromTextField.setEditable(true);
		}
		fromTextField.setText(sender);
	}

	/**
	 * 
	 */
	private void updateHeaderArea() {
		headerArea.setEnabled(false);
		String sender = fromTextField.getText();
		try {
			messageTextArea.getDocument().remove(headerArea.getStartPos() + 6, oldSender.length());
			messageTextArea.getDocument().insertString(headerArea.getStartPos() + 6, sender, null);
			oldSender = sender;
			headerArea.setEnabled(true);
		} catch (BadLocationException exception) {
			logger.log(Level.SEVERE, "Error while updating the message header", exception);
		}
	}
}
