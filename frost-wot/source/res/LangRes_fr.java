package res;

import java.util.ListResourceBundle;
public class LangRes_fr extends ListResourceBundle {																			 	
    public Object[][] getContents() { 
	return contents;
    }
    static final Object[][] contents = {
	// Tabbed pane
	{"Search", "Recherche"},
	{"Downloads", "T�l�chargements"},
	{"Uploads", "Envois"},
	{"News", "Forums"},

	// Menues
	{"File", "Fichier"},
	{"Exit", "Quitter"},
	{"News", "Forums"},
	{"Force update of selected board", "Forcer la mise � jour du forum s�lectionn�"},
	{"Options", "Options"},
	{"Preferences", "Pr�f�rences"},
	{"Help", "Aide"},
	{"About", "A propos"},

	// Buttons
	{"Download", "T�l�charger"},
	{"Browse...", "Parcourir..."},
	{"New message", "Nouveau message"},
	{"Reply", "R�pondre"},
	{"Update", "Mettre � jour"},
	{"Add board", "Ajouter"},
	{"Remove selected board", "Supprimer le forum s�lectionn�"},
	{"Download attachment(s)", "T�l�charger la(les) pi�ce(s) jointe(s)"},

	// Check Boxes
	{"Automatic update", "Mise � jour automatique"},
	{"Activate downloading", "Activer le t�l�chargement"},
	{"Activate uploading", "Activer l'envoi"},

	// Popup Menues
	{"Download selected keys", "T�l�charger les clefs s�lectionn�es"},
	{"Download all keys", "T�l�charger toutes les clefs"},
	{"Cancel", "Annuler"},

	{"Remove selected downloads", "Effacer les t�l�chargements s�lectionn�s"},
	{"Remove all downloads", "Effacer tous les t�l�chargements"},
	{"Retry selected downloads", "R�essayer les t�l�chargements s�lectionn�s"},
	{"Move selected downloads up", "D�placer les t�l�chargements s�lectionn�s vers le haut"},
	{"Move selected downloads down", "D�placer les t�l�chargements s�lectionn�s vers le bas"},
	{"Remove selected files", "Effacer les fichiers s�lectionn�s"},
	{"Remove all files", "Effacer tous les fichiers"},
	{"Move selected files up", "D�placer les fichiers s�lectionn�s vers le haut"},
	{"Move selected files down", "D�placer les fichiers s�lectionn�s vers le bas"},
	{"Reload selected files", "Recharger les fichiers s�lectionn�s"},
	{"Reload all files", "Recharger tous les fichiers"},
	{"Set prefix for selected files", "D�finir le pr�fixe des fichiers s�lectionn�s"},
	{"Set prefix for all files", "D�finir le pr�fixe de tous les fichiers"},
	{"Restore default filenames for selected files", "Restaurer les noms de fichiers par d�faut des fichiers s�lectionn�s"},
	{"Restore default filenames for all files", "Restaurer les noms de fichiers par d�faut de tous les fichiers"},
	{"Save message to disk", "Enregistrer ce message sur le disque..."},

	// Preferences
	{"Options", "Options"},
	{"OK", "Ok"},
	{"On", "On"},
	{"Off", "Off"},

	{"Select download directory.", "S�lectionnez le dossier de t�l�chargement"},
	{"Download directory:", "Dossier de t�l�chargement:"},
	{"Minimum HTL (5):", "HTL mimimum (5):"},
	{"Maximum HTL (25):", "HTL maximum (25):"},
	{"Number of simultaneous downloads (3):", "Nombre de t�l�chargements simultan�s (3):"},
	{"Number of splitfile threads (3):", "Nombre de t�ches par fichiers (3):"},
	{"Remove finished downloads every 5 minutes.(off)", "Effacer les t�l�chargements termin�s toutes les 5 minutes (off)"},
	{"Upload HTL (5):", "HTL d'envoi (5):"},
	{"Number of simultaneous uploads (3):", "Nombre d'envois simultan�s (3):"},
	{"Number of splitfile threads (3):", "Nombre de t�ches par fichier (3):"},
	{"Reload interval (8 days):", "Interval de rechargement (8 jours):"},
	{"Message upload HTL (5):", "HTL d'envoi des messages (5):"},
	{"Message download HTL(15):", "HTL de t�l�chargement des messages (15):"},
	{"Number of days to display (10):", "Nombre de jours � afficher (10):"},
	{"Number of days to download backwards (3):", "Nombre de jours � t�l�charger (3):"},
	{"Keypool path:", "Dossier de stockage des clefs:"},
	{"Keyfile upload HTL (5):", "HTL d'envoi du fichier de clefs (5):"},
	{"Keyfile download HTL (15):", "HTL de t�l�chargement du fichier de clefs (15):"},
	{"Node address (127.0.0.1):", "Adresse du node (127.0.0.1):"},
	{"Node port (8481):", "Port du node (8481):"},
	{"Maximum number of keys to store (100000):", "Nombre maximum de clefs � stocker (100000):"},
	{"Use skins, please restart Frost after changing this.(off)", "Utiliser les th�mes, veuillez red�marrer Frost apr�s avoir chang� ceci. (off)"},
	{"Image Extension:", "Image Extension:"},
	{"Video Extension:", "Video Extension:"},
	{"Archive Extension:", "Archive Extension:"},
	{"Audio Extension:", "Audio Extension:"},
	{"Document Extension:", "Document Extension:"},
	{"Executable Extension:", "Executable Extension:"},
	{"Show time in titlebar", "Show Time in titlebar"},
	{"Show time", "Show time"},

	//	Preferences. Display panel		//TODO: Translate
	{"EnableSkins", "Enable Skins"}, 	
	{"MessageBodyFont", "Message Body Font"}, 

	// Preferences / Tabbed Pane
	{"Downloads", "T�l�chargements"},
	{"Uploads", "Envois"},
	{"Miscellaneous", "Divers"},
	{"Skins", "Th�mes"},
	{"News", "Forums"},
	{"Display", "Display"},		//TODO: Translate

	// Search table
	{"Filename", "Nom du fichier"},
	{"Size", "Taille"},
	{"Age", "Age"},
	{"Key", "Clef"},
	
	// Download table
	{"Status", "Statut"},
	{"HTL", "HTL"},
	{"Trying", "Essai en cours"},
	{"Waiting", "En attente"},
	{"Done", "Termin�"},
	{"Failed", "Echou�"},

	// Upload table
	{"Last upload", "Dernier envoi"},
	{"Path", "Chemin"},
	{"Never", "Jamais"},
	{"Uploading", "Envoi en cours"},
	
	// News table
	{"Index", "Index"},
	{"From", "Auteur"},
	{"Subject", "Sujet"},
	{"Date", "Date"},
	
	// About box
	{"About", "A propos"},
	
	// Board settings frame
	{"Generate new keypair", "G�n�rer une nouvelle paire de clefs"},
	{"Public board", "Forum public"},
	{"Secure board", "Forum s�curis�"},
	{"Board settings for ", "Param�tres du forum "},
	{"Private key :", "Clef priv�e :"},
	{"Public key :", "Clef publique :"},
	{"Not available", "Indisponible"},
	{"Configure selected board", "Configurer le forum s�lectionn�"},

	// Message frame
	{"Send message", "Envoyer le message"},
	{"Add attachment(s)", "Joindre"},
	{"Board: ", "Forum: "},
	{"From: ", "Auteur: "},
	{"Subject: ", "Sujet: "},
	{"Create message", "Nouveau message"},
	{"Choose file(s) / directory(s) to attach", "S�lectionnez le(s) fichier(s) / dossier(s) � joindre"},
	{"Do you want to enter a subject?", "Voulez-vous entrer un sujet?"},
	{"No subject specified!", "Aucun sujet sp�cifi�!"},
	{"Yes", "Oui"},
	{"No", "Non"},

	// Status bar
	{"Up: ", "Envois: "},
	{"   Down: ", "   T�l�chargements: "},
	{"   TOFUP: ", "   TOFEN: "},
	{"   TOFDO: ", "   TOFTE: "},
	{"   Results: ", "   R�sultats: "},
	{"   Files: ", "   Fichiers: "},

	//	Skin chooser				//TODO: Translate
	{"AvailableSkins", "Available Skins"},
	{"Preview", "Preview"},
	{"RefreshList", "Refresh List"},

	// Other 
	{"Frost by Jantho", "Frost par Jantho"},
	{"Copyright (c) 2001 Jan-Thomas Czornack", "Copyright (c) 2001 Jan-Thomas Czornack"},
	{"Select a message to view its content.", "S�lectionnez un message pour voir son contenu."},
	{"Choose file(s) and/or directories to upload", "S�lectionnez le(s) fichier(s) / dossier(s) � envoyer"},
	{"Please enter the prefix you want to use for your files.", "Veuillez entrer le pr�fixe que vous voulez utiliser pour vos fichiers:"},
	{"Retry", "R�essayer"},
	{"I was not able to upload your message.", "Impossible d'envoyer le message."},
	{"Upload of message failed", "L'envoi du message � �chou�"},
    }; 
}
