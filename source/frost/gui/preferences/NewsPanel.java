/*
 * Created on Oct 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.preferences;

import java.awt.*;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.*;

import frost.*;
import frost.util.gui.AntialiasedTextArea;
import frost.util.gui.translation.UpdatingLanguageResource;

/**
 * @author $author$
 * @version $revision$
 */
class NewsPanel extends JPanel {
		
	private static Logger logger = Logger.getLogger(NewsPanel.class.getName());
		
	private SettingsClass settings = null;
	private UpdatingLanguageResource languageResource = null;
		
	private JLabel uploadHtlLabel = new JLabel();
	private JLabel downloadHtlLabel = new JLabel();
	private JLabel displayDaysLabel = new JLabel();
	private JLabel downloadDaysLabel = new JLabel();
	private JLabel messageBaseLabel = new JLabel();
	private JLabel signatureLabel = new JLabel();
		
	private JTextField uploadHtlTextField = new JTextField(8);
	private JTextField downloadHtlTextField = new JTextField(8);
	private JTextField displayDaysTextField = new JTextField(8);
	private JTextField downloadDaysTextField = new JTextField(8);
	private JTextField messageBaseTextField = new JTextField(16);
		
	private AntialiasedTextArea signatureTextArea;

	/**
	 * @param languageResource the LanguageResource to get localized strings from
	 * @param settings the SettingsClass instance that will be used to get and store the settings of the panel 
	 */
	protected NewsPanel(UpdatingLanguageResource languageResource, SettingsClass settings) {
		super();
		
		this.languageResource = languageResource;
		this.settings = settings;
		
		initialize();
		loadSettings();
	}

	/**
	 * 
	 */
	private void initialize() {
		setName("NewsPanel");
		setLayout(new GridBagLayout());
		refreshLanguage();

		// Adds all of the components
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.WEST;
		Insets insets5555 = new Insets(5, 5, 5, 5);
		constraints.weighty = 1;
		constraints.weightx = 0;		
			
		constraints.insets = insets5555;
		constraints.weightx = 0.4;	
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(uploadHtlLabel, constraints);	
		constraints.weightx = 0.6;		
		constraints.gridx = 1;
		add(uploadHtlTextField, constraints);
			
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(downloadHtlLabel, constraints);		
		constraints.gridx = 1;
		add(downloadHtlTextField, constraints);
						
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(displayDaysLabel, constraints);		
		constraints.gridx = 1;
		add(displayDaysTextField, constraints);
			
		constraints.gridx = 0;
		constraints.gridy = 3;
		add(downloadDaysLabel, constraints);		
		constraints.gridx = 1;
		add(downloadDaysTextField, constraints);
			
		constraints.gridx = 0;
		constraints.gridy = 4;
		add(messageBaseLabel, constraints);		
		constraints.gridx = 1;
		add(messageBaseTextField, constraints);
			
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 2;	
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 5;
		add(signatureLabel, constraints);
		constraints.gridy = 6;
		JScrollPane signatureScrollPane = new JScrollPane(getSignatureTextArea());
		add(signatureScrollPane, constraints);
	}

	/**
	 * @return
	 */
	private AntialiasedTextArea getSignatureTextArea() {
		if (signatureTextArea == null) {
			signatureTextArea = new AntialiasedTextArea(4, 50);

			String fontName = settings.getValue(SettingsClass.MESSAGE_BODY_FONT_NAME);
			int fontStyle = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_STYLE);
			int fontSize = settings.getIntValue(SettingsClass.MESSAGE_BODY_FONT_SIZE);
			Font tofFont = new Font(fontName, fontStyle, fontSize);
			if (!tofFont.getFamily().equals(fontName)) {
				logger.severe(
					"The selected font was not found in your system\n"
						+ "That selection will be changed to \"Monospaced\".");
				settings.setValue(SettingsClass.MESSAGE_BODY_FONT_NAME, "Monospaced");
				tofFont = new Font("Monospaced", fontStyle, fontSize);
			}
			signatureTextArea.setFont(tofFont);
			signatureTextArea.setAntiAliasEnabled(settings.getBoolValue("messageBodyAA"));
		}
		return signatureTextArea;
	}

	/**
	 * Load the settings of this panel
	 */
	private void loadSettings() {
		uploadHtlTextField.setText(settings.getValue("tofUploadHtl"));
		downloadHtlTextField.setText(settings.getValue("tofDownloadHtl"));
		displayDaysTextField.setText(settings.getValue("maxMessageDisplay"));
		downloadDaysTextField.setText(settings.getValue("maxMessageDownload"));
		messageBaseTextField.setText(settings.getValue("messageBase"));
			
		//Load signature
		File signature = new File("signature.txt");
		if (signature.isFile()) {
			getSignatureTextArea().setText(FileAccess.readFile("signature.txt", "UTF-8"));
		}
	}
		
	public void ok() {
		saveSettings();
	}
		
	/**
	 * 
	 */
	private void refreshLanguage() {
		uploadHtlLabel.setText(languageResource.getString("Message upload HTL") + " (21)");
		downloadHtlLabel.setText(languageResource.getString("Message download HTL") + " (23)");
		displayDaysLabel.setText(
			languageResource.getString("Number of days to display") + " (10)");
		downloadDaysLabel.setText(
			languageResource.getString("Number of days to download backwards") + " (3)");
		messageBaseLabel.setText(languageResource.getString("Message base") + " (news)");
		signatureLabel.setText(languageResource.getString("Signature"));
	}

	/**
	 * Save the settings of this panel 
	 */
	private void saveSettings() {
		settings.setValue("tofUploadHtl", uploadHtlTextField.getText());
		settings.setValue("tofDownloadHtl", downloadHtlTextField.getText());
		settings.setValue("maxMessageDisplay", displayDaysTextField.getText());
		settings.setValue("maxMessageDownload", downloadDaysTextField.getText());
		settings.setValue("messageBase", messageBaseTextField.getText().trim().toLowerCase());

		//Save signature
		FileAccess.writeFile(getSignatureTextArea().getText(), "signature.txt", "UTF-8");
	}

}
