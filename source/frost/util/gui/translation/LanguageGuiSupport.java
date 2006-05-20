/*
  LanguageGuiSupport.java / Frost
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
package frost.util.gui.translation;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import frost.*;
import frost.util.gui.*;

/**
 * Builds and updates the language menu in MainFrame, 
 * updating adds new external language bundles.
 */
public class LanguageGuiSupport {
    
    private static LanguageGuiSupport instance = null;
    
    private Language language = Language.getInstance();
    
    private List buildinLanguageMenuItemsList;
    private HashMap buildinLanguageMenuItemsMap;
    private JRadioButtonMenuItem languageDefaultMenuItem;
    private JRadioButtonMenuItem languageBulgarianMenuItem;
    private JRadioButtonMenuItem languageDutchMenuItem;
    private JRadioButtonMenuItem languageEnglishMenuItem;
    private JRadioButtonMenuItem languageFrenchMenuItem;
    private JRadioButtonMenuItem languageGermanMenuItem;
    private JRadioButtonMenuItem languageItalianMenuItem;
    private JRadioButtonMenuItem languageJapaneseMenuItem;
    private JRadioButtonMenuItem languageSpanishMenuItem;
    private JRadioButtonMenuItem languageRussianMenuItem;
    
    private ButtonGroup languageMenuButtonGroup;


    private LanguageGuiSupport() {
        languageDefaultMenuItem = new JRadioButtonMenuItem();
        languageBulgarianMenuItem = new JRadioButtonMenuItem();
        languageDutchMenuItem = new JRadioButtonMenuItem();
        languageEnglishMenuItem = new JRadioButtonMenuItem();
        languageFrenchMenuItem = new JRadioButtonMenuItem();
        languageGermanMenuItem = new JRadioButtonMenuItem();
        languageItalianMenuItem = new JRadioButtonMenuItem();
        languageJapaneseMenuItem = new JRadioButtonMenuItem();
        languageSpanishMenuItem = new JRadioButtonMenuItem();
        languageRussianMenuItem = new JRadioButtonMenuItem();
        
        languageMenuButtonGroup = new ButtonGroup();
        
        MiscToolkit miscToolkit = MiscToolkit.getInstance();
        languageBulgarianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_bg.png", 16, 16));
        languageGermanMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_de.png", 16, 16));
        languageEnglishMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_en.png", 16, 16));
        languageSpanishMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_es.png", 16, 16));
        languageFrenchMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_fr.png", 16, 16));
        languageItalianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_it.png", 16, 16));
        languageJapaneseMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_ja.png", 16, 16));
        languageDutchMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_nl.png", 16, 16));
        languageRussianMenuItem.setIcon(miscToolkit.getScaledImage("/data/flag_ru.png", 16, 16));
        
        // default action listeners
        languageDefaultMenuItem.addActionListener(   new LanguageAction(null, false));
        languageGermanMenuItem.addActionListener(    new LanguageAction("de", false));
        languageEnglishMenuItem.addActionListener(   new LanguageAction("en", false));
        languageDutchMenuItem.addActionListener(     new LanguageAction("nl", false));
        languageFrenchMenuItem.addActionListener(    new LanguageAction("fr", false));
        languageJapaneseMenuItem.addActionListener(  new LanguageAction("ja", false));
        languageRussianMenuItem.addActionListener(   new LanguageAction("ru", false));
        languageItalianMenuItem.addActionListener(   new LanguageAction("it", false));
        languageSpanishMenuItem.addActionListener(   new LanguageAction("es", false));
        languageBulgarianMenuItem.addActionListener( new LanguageAction("bg", false));

        buildinLanguageMenuItemsList = new ArrayList();
        buildinLanguageMenuItemsList.add(languageDefaultMenuItem);
        buildinLanguageMenuItemsList.add(languageBulgarianMenuItem);
        buildinLanguageMenuItemsList.add(languageDutchMenuItem);
        buildinLanguageMenuItemsList.add(languageEnglishMenuItem);
        buildinLanguageMenuItemsList.add(languageFrenchMenuItem);
        buildinLanguageMenuItemsList.add(languageGermanMenuItem);
        buildinLanguageMenuItemsList.add(languageItalianMenuItem);
        buildinLanguageMenuItemsList.add(languageJapaneseMenuItem);
        buildinLanguageMenuItemsList.add(languageRussianMenuItem);
        buildinLanguageMenuItemsList.add(languageSpanishMenuItem);
        
        buildinLanguageMenuItemsMap = new HashMap();
        buildinLanguageMenuItemsMap.put("default", languageDefaultMenuItem);
        buildinLanguageMenuItemsMap.put("de", languageGermanMenuItem);
        buildinLanguageMenuItemsMap.put("en", languageEnglishMenuItem);
        buildinLanguageMenuItemsMap.put("nl", languageDutchMenuItem);
        buildinLanguageMenuItemsMap.put("fr", languageFrenchMenuItem);
        buildinLanguageMenuItemsMap.put("ja", languageJapaneseMenuItem);
        buildinLanguageMenuItemsMap.put("it", languageItalianMenuItem);
        buildinLanguageMenuItemsMap.put("es", languageSpanishMenuItem);
        buildinLanguageMenuItemsMap.put("bg", languageBulgarianMenuItem);
        buildinLanguageMenuItemsMap.put("ru", languageRussianMenuItem);
    }
    
    public static LanguageGuiSupport getInstance() {
        if( instance == null ) {
            instance = new LanguageGuiSupport();
        }
        return instance;
    }
    
    public void buildInitialLanguageMenu(JMenu languageMenu) {
        
        // first add the buildins, then maybe external bundles
        // finally select currently choosed language
        boolean anItemIsSelected = false;
        
        String configuredLang = Core.frostSettings.getValue("locale");
        String langIsExternal = Core.frostSettings.getValue("localeExternal");
        boolean isExternal;
        if( langIsExternal == null || langIsExternal.length() == 0 || !langIsExternal.equals("true")) {
            isExternal = false;
        } else {
            isExternal = true;
        }
        
        for(Iterator i=buildinLanguageMenuItemsList.iterator(); i.hasNext(); ) {
            languageMenuButtonGroup.add((AbstractButton)i.next());
        }
        
        languageMenu.add(languageDefaultMenuItem);
        languageMenu.addSeparator();
        languageMenu.add(languageBulgarianMenuItem);
        languageMenu.add(languageDutchMenuItem);
        languageMenu.add(languageEnglishMenuItem);
        languageMenu.add(languageFrenchMenuItem);
        languageMenu.add(languageGermanMenuItem);
        languageMenu.add(languageItalianMenuItem);
        languageMenu.add(languageJapaneseMenuItem);
        languageMenu.add(languageRussianMenuItem);
        languageMenu.add(languageSpanishMenuItem);
        
        List externalLocales = Language.getExternalLocales();
        if( externalLocales.size() > 0 ) {
            languageMenu.addSeparator();

            for(Iterator i=externalLocales.iterator(); i.hasNext(); ) {
                Locale locale = (Locale)i.next();

                JRadioButtonMenuItem item = new JRadioButtonMenuItem();
                String localeDesc = locale.getDisplayName() + "  (external) (" + locale.getLanguage() + ")";
                item.setText(localeDesc);
                item.addActionListener(new LanguageAction(locale.getLanguage(), true));
                languageMenuButtonGroup.add(item);
                languageMenu.add(item);
                if( isExternal == true && locale.getLanguage().equals(configuredLang) ) {
                    languageMenuButtonGroup.setSelected(item.getModel(), true);
                    anItemIsSelected = true;
                }
            }            
        }
        
        if( anItemIsSelected == false && isExternal == false ) {
            // select buildin item
            Object languageItem = buildinLanguageMenuItemsMap.get(configuredLang);
            if (languageItem != null) {
                languageMenuButtonGroup.setSelected(((JMenuItem) languageItem).getModel(), true);
                anItemIsSelected = true;
            }
        }

        if( anItemIsSelected == false ) {
            languageMenuButtonGroup.setSelected(languageDefaultMenuItem.getModel(), true);
        }
        
        translateLanguageMenu();
    }

    /**
     * After the translation dialog runs, the external languages might be changed.
     * Rebuild the menu items for the external bundles. 
     */
    public void updateLanguageMenu() {
        // TODO: !!!
    }
    
    /**
     * Setter for the language
     */
    private void setLanguageResource(String newLocaleName, boolean isExternal) {
        if( newLocaleName == null ) {
            Core.frostSettings.setValue("locale", "default");
            Core.frostSettings.setValue("localeExternal", "false");
            isExternal = false;
        } else {
            Core.frostSettings.setValue("locale", newLocaleName);
            Core.frostSettings.setValue("localeExternal", ""+isExternal);
        }
        language.changeLanguage(newLocaleName, isExternal);
    }
    
    public void translateLanguageMenu() {
        languageDefaultMenuItem.setText(language.getString("MainFrame.menu.language.default"));
        languageDutchMenuItem.setText(language.getString("MainFrame.menu.language.dutch"));
        languageEnglishMenuItem.setText(language.getString("MainFrame.menu.language.english"));
        languageFrenchMenuItem.setText(language.getString("MainFrame.menu.language.french"));
        languageGermanMenuItem.setText(language.getString("MainFrame.menu.language.german"));
        languageItalianMenuItem.setText(language.getString("MainFrame.menu.language.italian"));
        languageJapaneseMenuItem.setText(language.getString("MainFrame.menu.language.japanese"));
        languageSpanishMenuItem.setText(language.getString("MainFrame.menu.language.spanish"));
        languageBulgarianMenuItem.setText(language.getString("MainFrame.menu.language.bulgarian"));
        languageRussianMenuItem.setText(language.getString("MainFrame.menu.language.russian"));
    }

    class LanguageAction implements ActionListener {
        String langCode;
        boolean isExternal;
        public LanguageAction(String langCode, boolean isExternal) {
            this.langCode = langCode;
            this.isExternal = isExternal;
        }
        public void actionPerformed(ActionEvent e) {
            setLanguageResource(langCode, isExternal);
        }
    }
}
