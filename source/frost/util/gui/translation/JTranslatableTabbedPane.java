/*
 JTranslatableTabbedPane.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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

import javax.swing.JTabbedPane;

/**
 * This subclass of JTabbedPane lets the user add tabs and get their index
 * by the key to an Language, instead of by the localized 
 * title. If the language of that resource changes, the titles 
 * automatically change too.
 *
 * @author $Author$
 * @version $Revision$
 */
public class JTranslatableTabbedPane extends JTabbedPane implements LanguageListener {

	private Language language;

	/**
	 * @param language
	 */
	public JTranslatableTabbedPane(Language language) {
		super();
		this.language = language;
		language.addLanguageListener(this);
	}

	/**
	 * This method returns the localized title
	 * of the tab in the specified position.
	 * 
	 * @see javax.swing.JTabbedPane#getTitleAt(int)
	 */
	public String getTitleAt(int index) {
		String key = super.getTitleAt(index); 
		return language.getString(key);
	}

	/**
	 * This method returns the position of the tab whose title
	 * has the Language key passed as a parameter, 
	 * or -1 if no one was found.
	 * 
	 * @see javax.swing.JTabbedPane#indexOfTab(java.lang.String)
	 */
	public int indexOfTab(String key) {
		for (int i = 0; i < getTabCount(); i++) {
			if (super.getTitleAt(i).equals(key == null ? "" : key)) {
				return i;
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
	 */
	public void languageChanged(LanguageEvent event) {
		revalidate();
		repaint();
	}

}
