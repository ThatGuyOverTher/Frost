/*
 * Created on Nov 9, 2003
 *
 */
package frost.gui.translation;

import java.util.ResourceBundle;

import javax.swing.event.EventListenerList;


public class UpdatingLanguageResource {

	private ResourceBundle languageResource;

	/** A list of event listeners for this component. */
	protected EventListenerList listenerList = new EventListenerList();

	public UpdatingLanguageResource(ResourceBundle newLanguageResource) {
		super();
		languageResource = newLanguageResource;
	}

	/**
	 * @return
	 */
	public ResourceBundle getLanguageResource() {
		return languageResource;
	}
	
	/**
	 * Adds an <code>LanguageListener</code> to the UpdatingLanguageResource.
	 * @param listener the <code>LanguageListener</code> to be added
	 */
	public void addLanguageListener(LanguageListener listener) {
		listenerList.add(LanguageListener.class, listener);
	}
	
	/**
	 * Returns an array of all the <code>LanguageListener</code>s added
	 * to this UpdatingLanguageResource with addLanguageListener().
	 *
	 * @return all of the <code>LanguageListener</code>s added or an empty
	 *         array if no listeners have been added
	 */
	public LanguageListener[] getActionListeners() {
		return (LanguageListener[])(listenerList.getListeners(
					LanguageListener.class));
	}
	
	/**
	 * Removes an <code>LanguageListener</code> from the UpdatingLanguageResource.
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
			for (int i = listeners.length-2; i>=0; i-=2) {
				if (listeners[i]==LanguageEvent.class) {
					// Lazily create the event:
					if (e == null) {
						  e = new LanguageEvent(UpdatingLanguageResource.this);
					}
					((LanguageListener)listeners[i+1]).languageChanged(e);
				}          
			}
		}

	/**
	 * @param newLanguageResource
	 */
	public void setLanguageResource(ResourceBundle newLanguageResource) {
		languageResource = newLanguageResource;
		fireLanguageChanged(new LanguageEvent(this));
	}

	/**
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		return languageResource.getString(key);
	}

}
