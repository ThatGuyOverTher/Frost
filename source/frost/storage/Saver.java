/*
 * Created on Apr 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.storage;

import java.util.*;
import java.util.Timer;
import java.util.logging.*;

import javax.swing.*;

import frost.*;
import frost.util.gui.translation.UpdatingLanguageResource;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Saver extends Timer {

	/**
	 * @author Administrator
	 *
	 * To change the template for this generated type comment go to
	 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
	 */
	private class AutoTask extends TimerTask {

		/**
		 * 
		 */
		public AutoTask() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		public void run() {
			if (autoSavables != null) {
				Enumeration enumeration = autoSavables.elements();
				while (enumeration.hasMoreElements()) {
					Savable savable = (Savable) enumeration.nextElement();
					try {
						savable.save();
					} catch (StorageException se) {
						logger.log(Level.SEVERE,
								"Error while saving a resource inside the shutdown hook.", se);
						JOptionPane.showMessageDialog(parentFrame, 
								languageResource.getString("Saver.AutoTask.message"), 
								languageResource.getString("Saver.AutoTask.title"), 
								JOptionPane.ERROR_MESSAGE);
						System.exit(3);
					}
				}
			}
		}

	}
	/**
	 * @author Administrator
	 *
	 * To change the template for this generated type comment go to
	 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
	 */
	private class ShutdownThread extends Thread {

		/**
		 * 
		 */
		public ShutdownThread() {
		}

		/**
		 * Called by shutdown hook.
		 */
		public void run() {
			logger.info("Saving settings ...");

			if (exitSavables != null) {
				Enumeration enumeration = exitSavables.elements();
				while (enumeration.hasMoreElements()) {
					Savable savable = (Savable) enumeration.nextElement();
					try {
						savable.save();
					} catch (StorageException se) {
						logger.log(Level.SEVERE,
								"Error while saving a resource inside the shutdown hook.", se);
					}
				}
			}
			FileAccess.cleanKeypool(MainFrame.keypool);

			logger.info("Bye!");
		}
	}
	
	private static Logger logger = Logger.getLogger(Saver.class.getName());

	private UpdatingLanguageResource languageResource;
	private JFrame parentFrame;
	
	private ShutdownThread shutdownThread = new ShutdownThread();
	private AutoTask autoTask = new AutoTask();
	
	private Vector autoSavables;
	private Vector exitSavables;

	/**
	 * @param newCore
	 */
	public Saver(SettingsClass frostSettings, UpdatingLanguageResource languageResource, JFrame parentFrame) {
		super();
		this.languageResource = languageResource;
		this.parentFrame = parentFrame;
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		int autoSaveIntervalMinutes = frostSettings.getIntValue(SettingsClass.AUTO_SAVE_INTERVAL);
		schedule(
			autoTask,
			autoSaveIntervalMinutes * 60 * 1000,
			autoSaveIntervalMinutes * 60 * 1000);
	}

	/**
	 * Adds a Savable to the autoSavables list. 
	 * <p>
	 * If autoSavable is null, no exception is thrown and no action is performed.
	 *
	 * @param    autoSavable  the Savable to be added
	 *
	 * @see #removeAutoSavable 
	 */
	public synchronized void addAutoSavable(Savable autoSavable) {
		if (autoSavable == null) {
			return;
		}
		if (autoSavables == null) {
			autoSavables = new Vector();
		}
		autoSavables.addElement(autoSavable);
	}

	/**
	 * Adds a Savable to the exitSavables list. 
	 * <p>
	 * If exitSavable is null, no exception is thrown and no action is performed.
	 *
	 * @param    exitSavable  the Savable to be added
	 *
	 * @see #removeExitSavable
	 */
	public synchronized void addExitSavable(Savable exitSavable) {
		if (exitSavable == null) {
			return;
		}
		if (exitSavables == null) {
			exitSavables = new Vector();
		}
		exitSavables.addElement(exitSavable);
	}

}
