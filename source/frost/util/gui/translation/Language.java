/*
  Language.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
--------------------------------------------------------------------------
  DESCRIPTION:
  This file contains the whole 'Options' dialog. It first reads the
  actual config from properties file, and on 'OK' it saves all
  settings to the properties file and informs the caller to reload
  this file.
*/
package frost.util.gui.translation;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.event.*;

/**
 * @pattern Singleton
 */
public class Language {

    private static Logger logger = Logger.getLogger(Language.class.getName());

    private FrostResourceBundle RESOURCE_BUNDLE = null;
    private FrostResourceBundle ROOT_RESOURCE_BUNDLE = null;
//    private BreakIterator LINE_BREAKER = null;

    private static boolean initialized = false;
    
    private final MessageFormat formatter = new MessageFormat("");
    
    private final Object[] objectLen1 = new Object[1];
    private final Object[] objectLen2 = new Object[2];
    private final Object[] objectLen3 = new Object[3];

    /**
     * The unique instance of this class.
     */
    private static Language instance = null;

    /**
     * A list of event listeners for this component.
     */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Prevent instances of this class from being created.
     */
    private Language(String localeName) {
        super();
        ROOT_RESOURCE_BUNDLE = new FrostResourceBundle();
        RESOURCE_BUNDLE = new FrostResourceBundle(localeName.toLowerCase(), ROOT_RESOURCE_BUNDLE);
//        LINE_BREAKER = BreakIterator.getLineInstance(RESOURCE_BUNDLE.getLocale());
    }

    private Language(File bundleFile) {
        ROOT_RESOURCE_BUNDLE = new FrostResourceBundle(bundleFile);
        RESOURCE_BUNDLE = ROOT_RESOURCE_BUNDLE;
    }
    /**
     * Return the unique instance of this class.
     *
     * @return the unique instance of this class
     */
    public static Language getInstance() {
        return instance;
    }

    /**
     * If it has already been initialized, this method does nothing.
     */
    public static void initializeWithName(String localeName) {
        if( !initialized ) {
            initialized = true;

            if( localeName == null ) {
                localeName = Locale.getDefault().getCountry();
            }
            instance = new Language(localeName.toLowerCase());
        }
    }

    public static void initializeWithFile(File bundleFile) {
        if( !initialized ) {
            initialized = true;
            System.out.println("f="+bundleFile.getPath());
            instance = new Language(bundleFile);
        }
    }
    
//    /**
//     * If it has already been initialized, this method does nothing.
//     */
//    public static void initializeWithLocale(Locale locale) {
//        if( !initialized ) {
//            initialized = true;
//            instance = new Language(locale);
//        }
//    }

    /**
     * Adds an <code>LanguageListener</code> to the Language.
     * @param listener the <code>LanguageListener</code> to be added
     */
    public void addLanguageListener(LanguageListener listener) {
        listenerList.add(LanguageListener.class, listener);
    }

    /**
     * Returns an array of all the <code>LanguageListener</code>s added
     * to this Language with addLanguageListener().
     *
     * @return all of the <code>LanguageListener</code>s added or an empty
     *         array if no listeners have been added
     */
    public LanguageListener[] getLanguageListeners() {
        return (LanguageListener[]) (listenerList.getListeners(LanguageListener.class));
    }

    /**
     * Removes an <code>LanguageListener</code> from the Language.
     * @param listener the <code>LanguageListener</code> to be removed
     */
    public void removeLanguageListener(LanguageListener listener) {
        listenerList.remove(LanguageListener.class, listener);
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the <code>event</code>
     * parameter.
     *
     * @param event  the <code>LanguageEvent</code> object
     * @see EventListenerList
     */
    protected void fireLanguageChanged(LanguageEvent event) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        LanguageEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == LanguageListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new LanguageEvent(Language.this);
                }
                ((LanguageListener) listeners[i + 1]).languageChanged(e);
            }
        }
    }

    /**
     * @param resourceBundle
     */
    public synchronized void changeLanguage(String localeName) {
        if( localeName == null ) {
            localeName = Locale.getDefault().getCountry();
        }
        RESOURCE_BUNDLE = new FrostResourceBundle(localeName.toLowerCase(), ROOT_RESOURCE_BUNDLE);
//        LINE_BREAKER = BreakIterator.getLineInstance(RESOURCE_BUNDLE.getLocale());
        
        fireLanguageChanged(new LanguageEvent(this));
    }

    /**
     * @param key
     * @return
     */
    public String getString(String origKey) {
        
        // temporary: map old LangRes keys to new keys used in gui
//        String key = LangResMapping.getOldForNew(origKey);
//        if( key == null ) {
//            System.out.println("Key not in mapping: '"+origKey+"'");
//            key = origKey;
//        }
        
        String s;
        try {
            s = RESOURCE_BUNDLE.getString(origKey);
        } catch(Throwable t) {
            s = null;
            logger.log(Level.SEVERE, "Exception catched", t);
        }
        if( s == null ) {
            logger.severe("No translation found for key '"+origKey+"', using key.");
            return origKey;
        } else {
            return s;
        }
    }

/////////////////////////////////////////////////////////////////////////////////////7
//    /**
//     * Builds a String containing the source String broken into lines
//     * of maxLength length, using \n as line breaker. 
//     */
//    public synchronized String breakLinesText(String source, int maxLength) {
//
//        LINE_BREAKER.setText(source);
//        int start = LINE_BREAKER.first();
//        int end = LINE_BREAKER.next();
//        int lineLength = 0;
//        
//        StringBuffer result = new StringBuffer();
//
//        while (end != BreakIterator.DONE) {
//            String word = source.substring(start,end);
//            if( word.indexOf('\n') > -1 ) {
//                // wenn dieses word NICHT mehr passt, dann auf neue zeile, und gleich noch ne neue Zeile wg. \n
//                lineLength = 0; // TODO: fix
//            } else {
//                lineLength = lineLength + word.length();
//                if (lineLength > maxLength) {
//                    result.append("\n");
//                    lineLength = word.length();
//                }
//            }
//            result.append(word);
//            start = end;
//            end = LINE_BREAKER.next();
//        }
//        return result.toString();
//    }
//
//    /**
//     * Builds a HTML String containing the source String broken into lines
//     * of maxLength length, using 'br' as line breaker. 
//     */
//    public synchronized String breakLinesHtml(String source, int maxLength) {
//
//        LINE_BREAKER.setText(source);
//        int start = LINE_BREAKER.first();
//        int end = LINE_BREAKER.next();
//        int lineLength = 0;
//        
//        StringBuffer result = new StringBuffer();
//        result.append("<html>");
//
//        while (end != BreakIterator.DONE) {
//            String word = source.substring(start,end);
//            lineLength = lineLength + word.length();
//            if (lineLength > maxLength) {
//                result.append("<br>");
//                lineLength = word.length();
//            }
//            result.append(word);
//            start = end;
//            end = LINE_BREAKER.next();
//        }
//        result.append("</html>");
//        return result.toString();
//    }

    public synchronized String formatMessage(String msg, Object[] objs) {
        try {
            String pattern = getString(msg);
            formatter.applyPattern(pattern);
            String output = formatter.format(objs);
            return output;
        } catch(IllegalArgumentException ex) {
            return '!' + msg + '!';
        }
    }

    public synchronized String formatMessage(String msg, Object obj1) {
        objectLen1[0] = obj1;
        return formatMessage(msg, objectLen1);
    }
    public synchronized String formatMessage(String msg, Object obj1, Object obj2) {
        objectLen2[0] = obj1;
        objectLen2[1] = obj2;
        return formatMessage(msg, objectLen2);
    }
    public synchronized String formatMessage(String msg, Object obj1, Object obj2, Object obj3) {
        objectLen3[0] = obj1;
        objectLen3[1] = obj2;
        objectLen3[2] = obj3;
        return formatMessage(msg, objectLen3);
    }
}
