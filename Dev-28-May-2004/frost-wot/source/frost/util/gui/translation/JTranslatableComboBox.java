/*
 * Created on Nov 19, 2003
 */
package frost.util.gui.translation;

import javax.swing.JComboBox;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
 * 
 */
public class JTranslatableComboBox extends JComboBox implements LanguageListener {

	/**
	 * 
	 */
	private class CheckBoxItem {

		private String key = null;
		private String value = null;

		/**
		 * 
		 */
		public CheckBoxItem(String newKey, String newValue) {
			super();
			key = newKey;
			value = newValue;
		}
		
		public String getKey() {
			return key;
		}
		
		public void setValue(String newValue) {
			value = newValue;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return value;
		}

	}
	private UpdatingLanguageResource languageResource = null;
	private String[] keys;

	/**
	 * @param newLanguageResource
	 * @param newKeys
	 */
	public JTranslatableComboBox(UpdatingLanguageResource newLanguageResource, String[] newKeys) {
		super();
		languageResource = newLanguageResource;
		keys = newKeys;
		languageResource.addLanguageListener(this);
		for (int i = 0; i < keys.length; i++) {
			String value = languageResource.getString(keys[i]);
			CheckBoxItem item = new CheckBoxItem(keys[i], value);
			addItem(item);
		}
	}

	/* (non-Javadoc)
	 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
	 */
	public void languageChanged(LanguageEvent event) {
		refreshLanguage();		
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		for (int i = 0; i < keys.length; i++) {
			CheckBoxItem item = (CheckBoxItem) getItemAt(i);
			String newValue = languageResource.getString(item.getKey());
			item.setValue(newValue);
		}
		//This is done to refresh the horizontal size
		CheckBoxItem dummy = new CheckBoxItem("","");	
		addItem(dummy);
		removeItem(dummy);	
	}
	
	/**
	 * @return
	 */
	public String getSelectedKey() { 
		Object selectedItem = getSelectedItem();
		if ((selectedItem != null) && (selectedItem instanceof CheckBoxItem)) {
			return ((CheckBoxItem) selectedItem).getKey();
		} else {
			return null;
		}
	}
	
	/**
	 * If no item contains that key, the selection remains unchanged.
	 * 
	 * @param key the key of the item to select
	 */
	public void setSelectedKey(String aKey) {
		boolean found = false;
		for (int i = 0;(i < getItemCount()) && !found; i++) {
			Object item = getItemAt(i);
			if (item instanceof CheckBoxItem) {
				if (((CheckBoxItem) item).getKey().equals(aKey)) {
					setSelectedIndex(i);
					found = true;
				}
			}
		}
	}

}
