/*
 * Created on Nov 4, 2004
 * 
 */
package frost.storage;

import frost.SettingsClass;
import frost.identities.IdentitiesDAO;

/**
 * This class implements the Abstract Factory pattern. It is used by the business objects
 * to get the concrete factory for the specific storage method they use.
 * 
 * @pattern Abstract Factory
 * 
 * @author $author$
 * @version $revision$
 */
public abstract class DAOFactory {

	//List of DAO types supported by this factory
	public static final int XML = 1;
	public static final int HSQLDB = 2;
	
	private static XmlDAOFactory xmlDAOFactory = null;
	private static HsqldbDAOFactory hsqldbDAOFactory = null;
	
	private static SettingsClass settings;
	
	/**
	 * This method returns the concrete DAOFactory implementation
	 * for the storage method whose id is passed as a parameter.
	 * 
	 * @param storageMethod id of the storage method of the wanted factory
	 * @return the concrete factory for the given storage method
	 */
	public static DAOFactory getFactory(int storageMethod) {
		switch (storageMethod) {
			case XML:
				return getXmlDAOFactory();
			case HSQLDB:
				return getHsqldbDAOFactory();	
			default:
				return null;
		}
	}
	
	/**
	 * This method returns the HsqldbDAOFactory, creating it if necessary.
	 * @return the HsqldbDAOFactory
	 */
	private static HsqldbDAOFactory getHsqldbDAOFactory() {
		if (hsqldbDAOFactory == null) {
			hsqldbDAOFactory = new HsqldbDAOFactory(settings);
		}
		return hsqldbDAOFactory;	
	}
	
	/**
	 * This method returns the XmlDAOFactory, creating it if necessary.
	 * @return the XmlDAOFactory
	 */
	private static XmlDAOFactory getXmlDAOFactory() {
		if (xmlDAOFactory == null) {
			xmlDAOFactory = new XmlDAOFactory(settings);
		}	
		return xmlDAOFactory;
	}
	
	/**
	 * This method returns a data access object for the FrostIdentities
	 * business object.
	 * @return a IdentitiesDAO for the FrostIdentities business object.
	 */
	public abstract IdentitiesDAO getIdentitiesDAO();

	/**
	 * This method initializes the DAOFactory with the given SettingsClass.
	 * @param frostSettings
	 */
	public static void initialize(SettingsClass frostSettings) {
		settings = frostSettings;		
	}
	
}
