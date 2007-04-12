/*
 HelpBrowser.java / Frost
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
package frost.gui.help;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import frost.util.gui.*;
import frost.util.gui.translation.*;

/**
 * Browser Component
 * @author Jantho
 * modified by notitaccu
 */
public class HelpBrowser extends JPanel {

    private static final Logger logger = Logger.getLogger(HelpBrowser.class.getName());
    
    private static Language language = Language.getInstance();
    
    private String url_prefix;
//    private String url_locale;
    private String homePage;
    
    private BrowserHistory browserHistory = null;
    
    // Global Variables
    JFrame parent;
    // GUI Objects
    JButton backButton;
    JButton homeButton;
    JButton forwardButton;
    
    JTextField TFsearchTxt;
    JButton BfindNext;
    JButton BfindPrev;

    JEditorPane editorPane;
    
    HelpHTMLEditorKit helpHTMLEditorKit;
    
    int lastSearchPosStart = 0;
    int lastSearchPosEnd = 0;
    String lastSearchText = null;

    public HelpBrowser(JFrame parent, String locale, String zipfile, String homePage) {
        this.parent = parent;
        this.url_prefix = zipfile;
        this.homePage = homePage;
        setHelpLocale(locale);
        init();
    }
    
    private void init() {
      
        // history init
        browserHistory = new BrowserHistory();
        browserHistory.resetToHomepage(homePage);
        
        editorPane = new JEditorPane();
        editorPane.setCaret(new SelectionPreservingCaret());
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if( e.getEventType() == HyperlinkEvent.EventType.ENTERED ) {
                    ((JEditorPane) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }
                if( e.getEventType() == HyperlinkEvent.EventType.EXITED ) {
                    ((JEditorPane) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;
                }
                if( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
                    ((JEditorPane) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    browserHistory.setCurrentPage(e.getURL().toString());
                    setHelpPage(e.getURL().toString());
                }
            }
        });
        editorPane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showEditorPanePopupMenu(e);
                }
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showEditorPanePopupMenu(e);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setWheelScrollingEnabled(true);

        backButton = new JButton(new ImageIcon(this.getClass().getResource("/data/back.png")));
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( browserHistory.isBackwardPossible() ) {
                    setHelpPage(browserHistory.backwardPage());
                }
            }
        });

        forwardButton = new JButton(new ImageIcon(this.getClass().getResource("/data/forward.png")));
        forwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if( browserHistory.isForwardPossible() ) {
                    setHelpPage(browserHistory.forwardPage());
                }
            }
        });

        homeButton = new JButton(new ImageIcon(this.getClass().getResource("/data/gohome.png")));
        homeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browserHistory.resetToHomepage(homePage);
                setHelpPage(homePage);
            }
        });
        
        JLabel Lsearch = new JLabel(new ImageIcon(getClass().getResource("/data/search.gif")));
        TFsearchTxt = new JTextField(15);
        
        BfindNext = new JButton(new ImageIcon(getClass().getResource("/data/searchNext.png")));
        BfindNext.setDefaultCapable(true);
        BfindNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchText(true); // search forward
            }
        });

        BfindPrev = new JButton(new ImageIcon(getClass().getResource("/data/searchPrev.png")));
        BfindPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchText(false); // search backward
            }
        });

        JPanel contentPanel = this;
        contentPanel.setLayout(new BorderLayout());

        JPanel buttonPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanelLeft.add(backButton);
        buttonPanelLeft.add(homeButton);
        buttonPanelLeft.add(forwardButton);

        JPanel buttonPanelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanelRight.add(Lsearch);
        buttonPanelRight.add(TFsearchTxt);
        buttonPanelRight.add(BfindNext);
        buttonPanelRight.add(BfindPrev);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(buttonPanelLeft, BorderLayout.WEST);
        buttonPanel.add(buttonPanelRight, BorderLayout.EAST);

        editorPane.setEditable(false);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
   
        helpHTMLEditorKit = new HelpHTMLEditorKit(url_prefix);
        editorPane.setEditorKit(helpHTMLEditorKit);
        
        setHelpPage(homePage);
    }
    
    private void showEditorPanePopupMenu(MouseEvent e) {
        JPopupMenu p = new PopupMenuTofText(editorPane);
        p.show(e.getComponent(), e.getX(), e.getY());
    }
    
    private void searchText(boolean forward) {
        
        String searchTxt = TFsearchTxt.getText();
        if( searchTxt == null ) {
            return;
        }
        searchTxt = searchTxt.trim();
        if( searchTxt.length() == 0 ) {
            return;
        }
        
        searchTxt = searchTxt.toLowerCase();
        
        if( lastSearchText == null ) {
            lastSearchText = searchTxt;
        } else if( lastSearchText != null && searchTxt.equals(lastSearchText) == false ) {
            // search from the beginning
            lastSearchPosStart=0;
            lastSearchPosEnd=0;
            lastSearchText=searchTxt;
        }
        
        String docTxt = null;
        try {
            docTxt = helpHTMLEditorKit.getHelpHTMLDocument().getText(0, helpHTMLEditorKit.getHelpHTMLDocument().getLength());
            docTxt = docTxt.toLowerCase();
        } catch (BadLocationException e1) {
            logger.log(Level.SEVERE, "Could not get text from document.", e1);
            return;
        }

        int pos;
        if( forward ) {
            pos = docTxt.indexOf(searchTxt, lastSearchPosEnd); // search after last found endPos
        } else {
            // search before last found startPos
            if( lastSearchPosStart > 0 ) {
                String tmpStr = docTxt.substring(0, lastSearchPosStart);
                pos = tmpStr.lastIndexOf(searchTxt);
            } else {
                // we are already at the begin
                return;
            }
        }
        if( pos > -1 ) {
            // scroll to text and select
            int endPos = pos + searchTxt.length();
            editorPane.setCaretPosition(pos);
            editorPane.moveCaretPosition(endPos);
            
            lastSearchPosStart = pos;
            lastSearchPosEnd = endPos;
        } else {
            editorPane.setCaretPosition(0);
            lastSearchPosStart = 0;
            lastSearchPosEnd = 0;
        }
    }

    void setHelpPage(String url) {

        if( url == null ) {
            url = homePage;
        }
        
        if( url.startsWith(url_prefix) ) {
            url = url.substring(url_prefix.length());
        }

        try {
            editorPane.setPage(url_prefix + url);

            lastSearchPosStart = 0; // reset pos
            lastSearchPosEnd = 0; // reset pos
            lastSearchText = null;
            
            editorPane.requestFocus();

        } catch (IOException e1) {
            logger.log(Level.INFO, "HELP: Missing file: '" + url + "'");
        }
        
        updateBrowserButtons();
    }

    private void updateBrowserButtons() {
        forwardButton.setEnabled( browserHistory.isForwardPossible() ); 
        backButton.setEnabled( browserHistory.isBackwardPossible() ); 
    }

    void setHelpLocale(String newLocale) {
        // Hier ist ne schoene stelle zum pruefen.        
//        if( newLocale.equals("default") ) {
//            url_locale = "";
//        } else {
//            url_locale = newLocale;
//        }
    }
    
    private class BrowserHistory {

        private ArrayList<String> history = new ArrayList<String>();
        private int historypos = -1; // this means history ist invalid

        public boolean isForwardPossible() {
            if( historypos < history.size()-1 ) {
                return true;
            }
            return false;
        }
        public String forwardPage() {
            if( !isForwardPossible() ) {
                return null;
            }
            historypos++;
            return (String)history.get(historypos);
        }
        public boolean isBackwardPossible() {
            if( historypos > 0 ) {
                return true;
            }
            return false;
        }
        public String backwardPage() {
            if( !isBackwardPossible() ) {
                return null;
            }
            historypos--;
            return (String)history.get(historypos);
        }
        public void setCurrentPage(String page) {
            // a link was clicked, add this new page after current historypos and clear alll forward pages
            // this is the behaviour of Mozilla too
            if( historypos < history.size()-1 ) {
                history.subList(historypos+1, history.size()).clear();
            }
            history.add(page);
            historypos++;
        }
        public void resetToHomepage(String homepage) {
            history.clear();
            history.add(homepage);
            historypos = 0; // current page is page at index 0
        }
    }
    
    /**
     * Caret implementation that doesn't blow away the selection when
     * we lose focus.
     */
    public class SelectionPreservingCaret extends DefaultCaret {
        /*
         * The last SelectionPreservingCaret that lost focus
         */
        private SelectionPreservingCaret last = null;

        /**
         * The last event that indicated loss of focus
         */
        private FocusEvent lastFocusEvent = null;

        public SelectionPreservingCaret() {
            // The blink rate is set by BasicTextUI when the text component
            // is created, and is not (re-) set when a new Caret is installed.
            // This implementation attempts to pull a value from the UIManager,
            // and defaults to a 500ms blink rate. This assumes that the
            // look and feel uses the same blink rate for all text components
            // (and hence we just pull the value for TextArea). If you are
            // using a look and feel for which this is not the case, you may
            // need to set the blink rate after creating the Caret.
            int blinkRate = 500;
            Object o = UIManager.get("TextArea.caretBlinkRate");
            if ((o != null) && (o instanceof Integer)) {
                Integer rate = (Integer) o;
                blinkRate = rate.intValue();
            }
            setBlinkRate(blinkRate);
        }

        /**
         * Called when the component containing the caret gains focus. 
         * DefaultCaret does most of the work, while the subclass checks
         * to see if another instance of SelectionPreservingCaret previously
         * had focus.
         *
         * @param e the focus event
         * @see java.awt.event.FocusListener#focusGained
         */
        public void focusGained(FocusEvent evt) {
            super.focusGained(evt);

            // If another instance of SelectionPreservingCaret had focus and
            // we defered a focusLost event, deliver that event now.
            if ((last != null) && (last != this)) {
                last.hide();
            }
        }

        /**
         * Called when the component containing the caret loses focus. Instead
         * of hiding both the caret and the selection, the subclass only 
         * hides the caret and saves a (static) reference to the event and this
         * specific caret instance so that the event can be delivered later
         * if appropriate.
         *
         * @param e the focus event
         * @see java.awt.event.FocusListener#focusLost
         */
        public void focusLost(FocusEvent evt) {
            setVisible(false);
            last = this;
            lastFocusEvent = evt;
        }

        /**
         * Delivers a defered focusLost event to this caret.
         */
        protected void hide() {
            if (last == this) {
                super.focusLost(lastFocusEvent);
                last = null;
                lastFocusEvent = null;
            }
        }
    }
    
    private class PopupMenuTofText
    extends JSkinnablePopupMenu
    implements ActionListener, LanguageListener, ClipboardOwner {

        private Clipboard clipboard;

        private JTextComponent sourceTextComponent;

        private JMenuItem copyItem = new JMenuItem();
        private JMenuItem cancelItem = new JMenuItem();

        public PopupMenuTofText(JTextComponent sourceTextComponent) {
            super();
            this.sourceTextComponent = sourceTextComponent;
            initialize();
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == copyItem) {
                // copy selected text
                StringSelection selection = new StringSelection(sourceTextComponent.getSelectedText());
                clipboard.setContents(selection, this);
            }
        }

        private void initialize() {
            languageChanged(null);

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            clipboard = toolkit.getSystemClipboard();

            copyItem.addActionListener(this);

            add(copyItem);
            addSeparator();
            add(cancelItem);
        }

        public void languageChanged(LanguageEvent event) {
            copyItem.setText(language.getString("Common.copy"));
            cancelItem.setText(language.getString("Common.cancel"));
        }

        public void show(Component invoker, int x, int y) {
            if (sourceTextComponent.getSelectedText() != null) {
                copyItem.setEnabled(true);
            } else {
                copyItem.setEnabled(false);
            }
            super.show(invoker, x, y);
        }

        public void lostOwnership(Clipboard tclipboard, Transferable contents) {
            // Nothing here
        }
    }
}
