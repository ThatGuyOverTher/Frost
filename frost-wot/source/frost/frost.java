/*
  frost.java / Frost
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

import java.awt.*;
import javax.swing.UIManager;
import frost.ext.JSysTrayIcon;

public class frost
{
    public frost()
    {
        final frame1 frame = new frame1();
        frame.validate();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if( frameSize.height > screenSize.height )
        {
            frameSize.height = screenSize.height;
        }
        if( frameSize.width > screenSize.width )
        {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.show();
        
        // Display the tray icon
        if( JSysTrayIcon.createInstance(0, "Frost", "Frost") == false )
        {
            System.out.println("Could not create systray icon.");
        }

        // this really obscuring stuff is needed to change the divider size
        // after the frame is shown. The goal is to see the blank message view
        // without any attachment table after startup
        frame.resetMessageViewSplitPanes();
        mixed.wait(500);
        frame.resetMessageViewSplitPanes();
    }

    /**Main method*/
    public static void main(String[] args)
    {
        System.out.println();
        System.out.println("Frost, Copyright (C) 2001 Jan-Thomas Czornack");
        System.out.println("Frost comes with ABSOLUTELY NO WARRANTY");
        System.out.println("This is free software, and you are welcome to");
        System.out.println("redistribute it under the GPL conditions.");
	    System.out.println("Frost uses code from bouncycastle.org (BSD license) and");
	    System.out.println("apache.org (Apache license)");
        System.out.println();
        System.out.println();

        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();

        if( args.length == 1 )
        {
            if( args[0].equals("-?") ||
                args[0].equals("-help") ||
                args[0].equals("--help") ||
                args[0].equals("/?") ||
                args[0].equals("/help") )
            {

                System.out.println("frost [-lf]");
                System.out.println();
                System.out.println("-lf     Allows to set the used 'Look and Feel'.");
                System.out.println("        javax.swing.plaf.metal.MetalLookAndFeel");
                System.out.println("        com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                System.out.println("        com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                System.out.println("        javax.swing.plaf.mac.MacLookAndFeel");

                System.exit(0);
            }
        }

        if( args.length == 2 )
        {
            if( args[0].equals("-lf") )
            {
                lookAndFeel = args[1];
            }
        }

        try {
            UIManager.setLookAndFeel(lookAndFeel);
        }
        catch( Exception e ) {
            System.out.println("The selected Look and Feel was not found. I'm using default now.");
        }
        new frost();
    }
}
