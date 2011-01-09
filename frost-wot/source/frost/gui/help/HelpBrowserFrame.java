/*
 HelpBrowserFrame.java / Frost
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
import java.awt.event.*;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.util.gui.*;

@SuppressWarnings("serial")
public class HelpBrowserFrame extends JFrame {

  private static final Logger logger = Logger.getLogger(HelpBrowserFrame.class.getName());

  boolean plugin;
  HelpBrowser browser;

  private void Init() throws Exception {
    	//------------------------------------------------------------------------
    	// Configure objects
    	//------------------------------------------------------------------------
    	this.setTitle("Frost - Help Browser");
    	this.setResizable(true);

    	browser.setPreferredSize(new Dimension(780, 550));

    	this.getContentPane().add(browser);
    }

    @Override
    protected void processWindowEvent(final WindowEvent e) {
        if( e.getID() == WindowEvent.WINDOW_CLOSING ) {
            if( !plugin ) {
                dispose();
                System.exit(0);
            } else {
                saveWindowState();
                setVisible(false);
            }
        } else {
            super.processWindowEvent(e);
        }
    }

     /**
      * Shorthand for ziphelp usage
      */
    public HelpBrowserFrame(final String langlocale, final String zipfile) {
        this(langlocale, "jar:file:" + zipfile + "!/", "index.html", true);
    }

    /**
     * Complete for browser usage
     */
    public HelpBrowserFrame(final String langlocale, final String zipfile, final String startpage, boolean plugin) {
        this.plugin = plugin;

        this.browser = new HelpBrowser(this, langlocale, zipfile, startpage);

        setIconImage(MiscToolkit.loadImageIcon("/data/toolbar/help-browser.png").getImage());

    	enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    	try {
    	    Init();
            if( !plugin ) {
                // standalone - fix size
                this.setSize(new Dimension(780, 550));
            } else {
                loadWindowState();
            }
    	} catch(final Throwable e) {
    		logger.log(Level.SEVERE, "Exception thrown in constructor", e);
    	}
    }

    public void showHelpPage(final String page) {
        browser.setHelpPage(page);
    }

    public void showHelpPage_htmlLink(final String page) {
        browser.setHelpPage(page);
    }

    public void showHelpPage_alias(final String page) {
        browser.setHelpPage(page);
    }

    private void saveWindowState() {
        final Rectangle bounds = getBounds();
        boolean isMaximized = ((getExtendedState() & Frame.MAXIMIZED_BOTH) != 0);

        Core.frostSettings.setValue("helpBrowser.lastFrameMaximized", isMaximized);

        if (!isMaximized) { // Only save the current dimension if frame is not maximized
            Core.frostSettings.setValue("helpBrowser.lastFrameHeight", bounds.height);
            Core.frostSettings.setValue("helpBrowser.lastFrameWidth", bounds.width);
            Core.frostSettings.setValue("helpBrowser.lastFramePosX", bounds.x);
            Core.frostSettings.setValue("helpBrowser.lastFramePosY", bounds.y);
        }
    }

    private void loadWindowState() {
        // load size, location and state of window
        int lastHeight = Core.frostSettings.getIntValue("helpBrowser.lastFrameHeight");
        int lastWidth = Core.frostSettings.getIntValue("helpBrowser.lastFrameWidth");
        final int lastPosX = Core.frostSettings.getIntValue("helpBrowser.lastFramePosX");
        final int lastPosY = Core.frostSettings.getIntValue("helpBrowser.lastFramePosY");
        final boolean lastMaximized = Core.frostSettings.getBoolValue("helpBrowser.lastFrameMaximized");

        if( lastHeight <= 0 || lastWidth <= 0 ) {
            // first call
            setSize(780,550);
            setLocationRelativeTo(MainFrame.getInstance());
            return;
        }

        final Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (lastWidth < 100) {
            lastWidth = 780;
        }
        if (lastHeight < 100) {
            lastHeight = 550;
        }

        if ((lastPosX + lastWidth) > scrSize.width) {
            setSize(780,550);
            setLocationRelativeTo(MainFrame.getInstance());
            return;
        }

        if ((lastPosY + lastHeight) > scrSize.height) {
            setSize(780,550);
            setLocationRelativeTo(MainFrame.getInstance());
            return;
        }

        setBounds(lastPosX, lastPosY, lastWidth, lastHeight);

        if (lastMaximized) {
            setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
        }
    }
}
