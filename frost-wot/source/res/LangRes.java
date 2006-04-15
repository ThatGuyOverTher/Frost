/*
  LangRes.java
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
package res;

import java.util.ListResourceBundle;

/**
 * This file contains the default translations for Frost. It is VERY IMPORTANT
 * that this file is kept UPTODATE because other translations are based on
 * this file! Please insert the text you want to be translatable at the proper
 * position in this file. If you changed texts or no longer need certain translations
 * please remove them from this file.
 * @author czornack
 */
public class LangRes extends ListResourceBundle {

    public Object[][] getContents() {
        return contents;
    }
    static final Object[][] contents = {

	///////////////////////////////////////////////////
	// Unknown translations
	// Have to look where in the GUI these translations are needed
	///////////////////////////////////////////////////
	{"Minimize to System Tray", "Minimize to System Tray"},
	{"Index", "Index"},
	{"From", "From"},
	{"Subject", "Subject"},
	{"Date", "Date"},
	{"Filename", "Filename"},
	{"Key", "Key"},
	{"Select a board to view its content.", "Select a board to view its content."},
	{"Select a message to view its content.", "Select a message to view its content."},
	{"Size", "Size"},
	{"Age", "Age"},
	{"Board", "Board"},
	{"State", "State"},
	{"Source", "Source"},
	{"Last upload", "Last upload"},
	{"Path", "Path"},
	{"Destination", "Destination"},
	{"Frost by Jantho", "Frost by Jantho"},
	{"Reload selected files", "Upload selected files"},
	{"Reload all files", "Reload all files"}, //reload????
	{"Show systray icon","Show SysTray icon"},
	{"Display","Display"},
	{"Miscellaneous","Miscellaneous"},
	{"Messages Today","Messages Today"},
	{"Public board","Public board"},
	{"Secure board","Secure board"},
	{"Generate new keypair","Generate new keypair"},
	{"Private key","Private key"},
	{"Public key","Public key"},
	{"Not available","Not available"},
    {"Error", "Error"}, // error dialog title
    {"Show memory monitor", "Show memory monitor"},

	///////////////////////////////////////////////////
	// Splash messages
	///////////////////////////////////////////////////
	{"Initializing Mainframe","Initializing Mainframe"},
	{"Hypercube fluctuating!","Hypercube fluctuating!"},
	{"Sending IP address to NSA","Sending IP address to the NSA"},
	{"Wasting more time","Wasting more time"},
	{"Reaching ridiculous speed...","Reaching ridiculous speed..."},
    
    ///////////////////////////////////////////////////
    // First Startup Dialog
    ///////////////////////////////////////////////////
    {"Frost first startup", "Frost first startup"},
    {"You can create a new identity or import an existing identities.xml file", "You can create a new identity or import an existing identities.xml file"},
    {"Please choose the version of Freenet you want to use", "Please choose the version of Freenet you want to use"},
    {"Create new identity", "Create new identity"},
    {"Import existing identities.xml file", "Import existing identities.xml file"},

	///////////////////////////////////////////////////
    // Welcome message
	///////////////////////////////////////////////////
    {"Welcome message", "Please read this!\n\nTo use Frost, you first need to select a topic from the board information window. You can open this window with the i-button above. You can send messages and files to people using the same board. It will probably take some time until the first boards show up (press the update button)."},

	///////////////////////////////////////////////////
	// Main Window Menu
	///////////////////////////////////////////////////
    // Menu File
    {"File", "File"},
    {"Exit", "Exit"},

	// Menu News
    {"News", "News"},
    {"Automatic message update", "Automatic board update"},
    {"Increase Font Size", "Increase Font Size"},
    {"Decrease Font Size", "Decrease Font Size"},
    {"Configure selected board", "Configure selected board"},
    {"Configure selected folder", "Configure selected folder"},
    {"Display board information window", "Display board information window"},
	{"Display known boards", "Display known boards"},
    {"Search messages", "Search messages"},

	// Menu Options
    {"Options", "Options"},
    {"Preferences", "Preferences"},

	// Menu Plugins
	{"Plugins", "Plugins"},
	{"Experimental Freenet Browser", "Experimental Freenet Browser"},
	{"Translate Frost into another language", "Translate Frost into another language"},

	// Menu Language
	{"Language", "Language"},
	{"Default", "Default"},
	{"German", "German"},
	{"English", "English"},
	{"Dutch", "Dutch"},
	{"Japanese", "Japanese"},
	{"French", "French"},
	{"Italian", "Italian"},
	{"Spanish", "Spanish"},
	{"Bulgarian","Bulgarian"},
    {"Russian","Russian"},

	// Menu Help
    {"Help", "Help"},
    {"About", "About"},

	///////////////////////////////////////////////////
    // Main Window ToolBar
	///////////////////////////////////////////////////
	{"New board", "New board"},
	{"New folder", "New folder"},
	{"Configure board", "Configure board"},
	{"Rename folder", "Rename folder"},
	{"Cut board", "Cut board"},
	{"Paste board", "Paste board"},
	{"Remove board", "Remove board"},
	{"Board Information Window", "Board Information Window"},
	{"Display list of known boards", "Display list of known boards"},

	///////////////////////////////////////////////////
	// Main Window Tabbed Pane
	///////////////////////////////////////////////////
	{"Search", "Search"},
	{"Downloads", "Downloads"},
	{"Uploads", "Uploads"},
	{"News", "News"},
	{"Status", "Status"},

	///////////////////////////////////////////////////
    // Main Window News Tab
	///////////////////////////////////////////////////
    // ToolBar in News Tab
	{"Save message", "Save message"},
    {"Next unread message","Next unread message"},
	{"New message", "New message"},
	{"Reply", "Reply"},
	{"Update", "Update"},
	{"Download attachment(s)", "Download attachment(s)"},
	{"Add Board(s)", "Add Board(s)"},
    {"Add Board(s) to folder", "Add Board(s) to folder"},
    {"Add board(s) to list of known boards", "Add board(s) to list of known boards"},
	{"Trust","Trust"},
	{"Set to CHECK", "Set to CHECK"},
    {"Set to OBSERVE", "Set to OBSERVE"},
	{"Do not trust","Do not trust"},

	// Popup over message table
	{"Mark message unread", "Mark message unread"},
	{"Mark ALL messages read", "Mark ALL messages read"},
	{"Delete message", "Delete message"},
	{"Undelete message", "Undelete message"},
	{"Cancel", "Cancel"},

	// Popup over message (main frame)
	{"Copy","Copy"},
	{"Save message to disk", "Save message to disk"},

	// Popup over attachments table
	{"Add selected board", "Add selected board"},
	{"Download selected attachment", "Download selected attachment"},
    
    // board states
    {"read access", "read access"},
    {"write access", "write access"},
    {"public board", "public board"},

	//Message table header
	{"Sig", "Sig"},

	///////////////////////////////////////////////////
	// Main Window Search Tab
	///////////////////////////////////////////////////
	// ToolBar in Search tab
	//{"Search", "Search"}, // Defined above
	{"Download selected keys", "Download selected keys"},
	{"all boards", "all boards"},

	// Popup over search results table
	{"Download all keys", "Download all keys"},
	{"help user (sets to GOOD)", "help user (sets to GOOD)"},
	{"block user (sets to BAD)", "block user (sets to BAD)"},
	{"set to neutral (CHECK)", "set to neutral (CHECK)"},
    {"observe user (OBSERVE)", "observe user (OBSERVE)"},

	//SearchComboBox
	{"All files","All files"},
	{"Audio","Audio"},
	{"Video","Video"},
	{"Images","Images"},
	{"Documents","Documents"},
	{"Executables","Executables"},
	{"Archives","Archives"},

	///////////////////////////////////////////////////
    // Main Window Downloads Tab
	///////////////////////////////////////////////////
	// ToolBar in Downloads Tab
	{"Activate downloading", "Activate downloading"},
    {"Pause downloading", "Pause downloading"},
	{"Show healing information", "Show healing information"},

    // Popup over download table
	{"Restart selected downloads", "Restart selected downloads"},
	{"Enable downloads", "Enable downloads"},
	{"Enable selected downloads", "Enable selected downloads"},
	{"Disable selected downloads", "Disable selected downloads"},
	{"Invert enabled state for selected downloads", "Invert enabled state for selected downloads"},
	{"Enable all downloads", "Enable all downloads"},
	{"Disable all downloads", "Disable all downloads"},
	{"Invert enabled state for all downloads", "Invert enabled state for all downloads"},
	{"Remove", "Remove"},
	{"Remove selected downloads", "Remove selected downloads"},
	{"Remove all downloads", "Remove all downloads"},
	{"Remove finished downloads", "Remove finished downloads"},

	///////////////////////////////////////////////////
    // Main Window Uploads Tab
	///////////////////////////////////////////////////
	// ToolBar in Uploads Tab
	{"Browse", "Browse"},

    // FileChooser in Uploads Tab
	{"Select files you want to upload to the", "Select files you want to upload to the"},
	{"board", "board"},

	// Popup over uploads table
	{"Remove selected files", "Remove selected files"},
	{"Remove all files", "Remove all files"},
	{"Start encoding of selected files", "Start encoding of selected files"},
	{"Upload selected files", "Upload selected files"},
	{"Upload all files", "Upload all files"},
	{"Set prefix for selected files", "Set prefix for selected files"},
	{"Set prefix for all files", "Set prefix for all files"},
	{"Restore default filenames for selected files", "Restore default filenames for selected files"},
	{"Restore default filenames for all files", "Restore default filenames for all files"},
	{"Change destination board", "Change destination board"},
	{"Please enter the prefix you want to use for your files.", "Please enter the prefix you want to use for your files."},

	///////////////////////////////////////////////////
	// Main Window Board Selection Tree
	///////////////////////////////////////////////////
    // Popup over Tree
	{"Refresh", "Refresh"},
	{"Remove", "Remove"},
	{"folder", "folder"},
	{"Folder", "Folder"},
	{"board", "board"},
	{"Board", "Board"},
	{"Cut", "Cut"},
	{"Paste", "Paste"},
	{"Add new board", "Add new board"},
	{"Add new folder", "Add new folder"},
	{"Configure selected board", "Configure selected board"},
	{"Sort folder", "Sort folder"},

	///////////////////////////////////////////////////
    // Main Window Status Bar
	///////////////////////////////////////////////////
	{"Up", "Up"},
	{"Down", "Down"},
	{"TOFUP", "TOFUP"},
	{"TOFDO", "TOFDO"},
	{"Results", "Results"},
	{"Files", "Files"},
	{"Selected board", "Selected board"},

	///////////////////////////////////////////////////
	// New Message Window
	///////////////////////////////////////////////////
	{"Create message", "Create message"},
	{"Send message", "Send message"},
	{"Add attachment(s)", "Add attachment(s)"},
	{"Sign", "Sign"},
    {"Encrypt for", "Encrypt for"},
	{"Indexed attachments", "Indexed attachments"},
	{"Should file attachments be added to upload table?", "Should file attachments be added to upload table?"},
	{"Board", "Board"},
	{"From", "From"},
	{"Subject", "Subject"},
	{"Remove", "Remove"},
	{"Do you want to enter a subject?", "Do you want to enter a subject?"},
	{"No subject specified!", "No subject specified!"},
	{"You must enter a subject!", "You must enter a subject!"},
	{"You must enter a sender name!", "You must enter a sender name!"},
	{"No 'From' specified!", "No 'From' specified!"},
	{"Choose file(s) / directory(s) to attach", "Choose file(s) / directory(s) to attach"},
	{"Choose boards to attach", "Choose boards to attach"},
    {"Choose boards", "Choose boards"},

	///////////////////////////////////////////////////
	// About box
	///////////////////////////////////////////////////
	{"About", "About"},
	{"Version", "Version"},
	{"Open Source Project (GPL license)", "Open Source Project (GPL license)"},
	{"OK", "OK"},
	{"More", "More"},
	{"Less", "Less"},
	{"Development:", "Development:"},
	{"Windows Installer:", "Windows Installer:"},
	{"System Tray Executables:", "System Tray Executables:"},
	{"Translation Support:", "Translation Support:"},
	{"Splash Screen Logo:", "Splash Screen Logo:"},
	{"Misc code contributions:", "Misc code contributions:"},

	///////////////////////////////////////////////////
	// Preferences
	///////////////////////////////////////////////////
	// More often used translations
	{"On", "On"},
	{"Off", "Off"},

	// Downloads Panel
	{"Downloads", "Downloads"},
	{"Disable downloads", "Disable downloads"},
	{"Download directory", "Download directory"},
	{"Restart failed downloads", "Restart failed downloads"},
	{"Maximum number of retries", "Maximum number of retries"},
	{"Waittime after each try", "Waittime after each try"},
	{"minutes", "minutes"},
	{"Enable requesting of failed download files", "Enable requesting of failed download files"},
	{"Request file after this count of retries", "Request file after this count of retries"},
	{"Number of simultaneous downloads", "Number of simultaneous downloads"},
	{"Number of splitfile threads", "Number of splitfile threads"},
	{"Remove finished downloads every 5 minutes", "Remove finished downloads every 5 minutes"},
	{"Try to download all segments, even if one fails", "Try to download all segments, even if one fails"},
	{"Decode each segment immediately after its download", "Decode each segment immediately after its download"},
	{"Select download directory", "Select download directory"},

	// Uploads Panel
	{"Disable uploads", "Disable uploads"},
	{"Restart failed uploads", "Restart failed uploads"},
	{"Automatic Indexing", "Automatic Indexing"},
	{"Share Downloads","Share Downloads"},
	{"Sign shared files", "Sign shared files"},
	{"Help spread files from people marked GOOD","Help spread files from people marked GOOD"},
	{"Upload HTL", "Upload HTL"},
	{"up htl explanation","(bigger is slower but more reliable)"},
	{"Number of simultaneous uploads", "Number of simultaneous uploads"},
	{"Number of splitfile threads", "Number of splitfile threads"},
	{"splitfile explanation","(bigger is faster but uses more cpu)"},
	{"Upload batch size","Upload batch size"},
	{"batch explanation", "bigger is faster but smaller is spam resistant"},
	{"Index file redundancy","Index file redundancy"},
	{"redundancy explanation", "not working yet"},

	// News (1) Panel
	{"Message upload HTL", "Message upload HTL"},
	{"Message download HTL", "Message download HTL"},
	{"Number of days to display", "Number of days to display"},
	{"Number of days to download backwards", "Number of days to download backwards"},
	{"Message base", "Message base"},
	{"Signature", "Signature"},

	// News (2) Panel
	{"Block messages with subject containing (separate by ';' )", "Block messages with subject containing (separate by ';' )"},
	{"Block messages with body containing (separate by ';' )", "Block messages with body containing (separate by ';' )"},
	{"Block messages with these attached boards (separate by ';' )", "Block messages with these attached boards (separate by ';' )"},
    
    {"Don't add boards to known boards list from users with trust states", "Don't add boards to known boards list from users with trust states"},
    {"Hide messages with trust states", "Hide messages with trust states"},
	{"Bad", "Bad"},
    {"Check", "Check"},
    {"Observe", "Observe"},
    {"None (unsigned)", "None (unsigned)"},
    
	{"Do spam detection", "Do spam detection"},
	{"Sample interval", "Sample interval"},
	{"hours", "hours"},
	{"Threshold of blocked messages", "Threshold of blocked messages"},

	// News (3) Panel
	{"Automatic update options", "Automatic update options"},
	{"Minimum update interval of a board", "Minimum update interval of a board"},
	{"Number of concurrently updating boards", "Number of concurrently updating boards"},
	{"Show board update visualization", "Show board update visualization"},
	{"Background color if updating board is selected", "Background color if updating board is selected"},
	{"Background color if updating board is not selected", "Background color if updating board is not selected"},
	{"Choose", "Choose"},
	{"Color", "Color"},
	{"Choose updating color of SELECTED boards", "Choose updating color of SELECTED boards"},
	{"Choose updating color of NON-SELECTED boards", "Choose updating color of NON-SELECTED boards"},
	{"Silently retry failed messages", "Silently retry failed messages"},
	{"Show deleted messages", "Show deleted messages"},
    {"Receive duplicate messages", "Receive duplicate messages"},

    // Expiration panel
    // TODO:
    {"Expiration", "Expiration"},
    {"Number of days before a message expires","Number of days before a message expires"},
    {"Keep expired messages in keypool","Keep expired messages in keypool"},
    {"Archive expired messages","Archive expired messages"},
    {"Archive folder","Archive folder"},
    {"Delete expired messages from keypool","Delete expired messages from keypool"},
    {"Select archive directory","Select archive directory"},

    // Search Panel
	{"Image Extension", "Image Extension"},
	{"Video Extension", "Video Extension"},
	{"Archive Extension", "Archive Extension"},
	{"Document Extension", "Document Extension"},
	{"Audio Extension", "Audio Extension"},
	{"Executable Extension", "Executable Extension"},
	{"Maximum search results", "Maximum search results"},
	{"Hide files from people marked BAD","Hide files from people marked BAD"},
	{"Hide files from anonymous users","Hide files from anonymous users"},

	// Miscelaneous Panel
	{"Keyfile upload HTL", "Keyfile upload HTL"},
	{"Keyfile download HTL", "Keyfile download HTL"},
	{"list of nodes","Comma-separated list of nodes you have FCP access to"},
	{"list of nodes 2","(nodeA:portA, nodeB:portB, ...)"},
	{"Maximum number of keys to store", "Maximum number of keys to store"},
	{"Allow 2 byte characters", "Allow 2 byte characters"},
	{"Use editor for writing messages", "Use editor for writing messages"},
	{"Clean the keypool", "Clean the keypool"},
	{"Automatic saving interval", "Automatic saving interval"},
	{"Disable splashscreen", "Disable splashscreen"},
	{"Enable logging", "Enable logging"},
	{"Logging level", "Logging level"},
	{"Log file size limit (in KB)", "Log files size limit  (in KB)"},
	{"Very high", "Very high"},
	{"High", "High"},
	{"Medium", "Medium"},
	{"Low", "Low"},
	{"Very low", "Very low"},

    // Display Panel
    {"EnableSkins", "Enable Skins"},
	{"MoreSkinsAt", "You can get more skins at"},
	{"Preview","Preview"},
	{"RefreshList","Refresh List"},
	{"NoSkinsFound","No skins found!"},
	{"AvailableSkins","Available Skins"},
	{"Plain","Plain"},
	{"Italic","Italic"},
	{"Bold","Bold"},
	{"Bold Italic","Bold Italic"},
	{"Sample","Sample"},
	{"Choose a Font","Choose a Font"},
	{"Fonts","Fonts"},
	{"Message Body","Message Body"},
	{"Message List","Message List"},
	{"File List","File List"},
	{"Choose","Choose"},
	{"EnableMessageBodyAA", "Enable antialiasing for Message Body"},

	///////////////////////////////////////////////////
	// Board Information Window
	///////////////////////////////////////////////////
	{"BoardInfoFrame.UpdateSelectedBoardButton","Update Selected Board"},
	{"BoardInfoFrame.Update","Update"},
	{"BoardInfoFrame.Update all boards","Update all boards"},
	{"BoardInfoFrame.Close","Close"},
	{"BoardInfoFrame.Board information window","Board information window"},
	{"BoardInfoFrame.Boards","Boards"},
	{"BoardInfoFrame.Messages","Messages"},
	{"BoardInfoFrame.Files","Files"},

	// Board information window table
	{"Messages","Messages"},

	///////////////////////////////////////////////////
	// List of known boards window
	///////////////////////////////////////////////////
	{"KnownBoardsFrame.List of known boards","List of known boards"},
	{"KnownBoardsFrame.Close","Close"},
	{"KnownBoardsFrame.Lookup","Lookup"},
    {"KnownBoardsFrame.Filter","Filter"},
	{"KnownBoardsTableModel.Boardname","Boardname"},

	///////////////////////////////////////////////////
	// Core start messages
	///////////////////////////////////////////////////

	{"Core.init.NodeNotRunningBody","Make sure your node is running and that you have configured Freenet correctly.\n"
									+ "Nevertheless, to allow you to read messages, Frost will startup now.\n"
                                    + "Please note that automatic board updates were disabled (Menu 'News')!\n"
									+ "Don't get confused by some error messages ;)\n"},
	{"Core.init.NodeNotRunningTitle","Error - could not establish a connection to freenet node."},
	{"Core.init.TransientNodeBody","You are running a TRANSIENT node. Better run a PERMANENT freenet node."},
	{"Core.init.TransientNodeTitle","Transient node detected"},
	{"Core.loadIdentities.ConnectionNotEstablishedBody","Frost could not establish a connection to your freenet node(s).\n"
									+ "For first setup of Frost and creating your identity a connection is needed,\n"
									+ "later you can run Frost without a connection.\n"
									+ "Please ensure that you are online and freenet is running, then restart Frost."},
	{"Core.loadIdentities.ConnectionNotEstablishedTitle","Connect to Freenet node failed"},
	{"Core.loadIdentities.ChooseName","Choose an identity name, it doesn't have to be unique\n"},
	{"Core.loadIdentities.InvalidNameBody","Your name must not contain a '@'!"},
	{"Core.loadIdentities.InvalidNameTitle","Invalid identity name"},

	// Board Settings Dialog
	{"Settings for board", "Settings for board"},
    {"Settings for all boards in folder", "Settings for all boards in folder"},
	{"Override default settings","Override default settings"},
	{"Use default","Use default"},
	{"Set to","Set to"},
	{"Yes","Yes"},
	{"No","No"},
	{"Enable automatic board update","Enable automatic board update"},
	{"Maximum message display (days)","Maximum message display (days)"},
	{"Warning","Warning"},
    {"Hide unsigned messages", "Hide unsigned messages"},
    {"Hide messages flagged BAD", "Hide messages flagged BAD"},
    {"Hide messages flagged CHECK", "Hide messages flagged CHECK"},
    {"Hide messages flagged OBSERVE", "Hide messages flagged OBSERVE"},

	//	Uploads underway warning when exiting
	 {"UploadsUnderway.title","Uploads underway"},
	 {"UploadsUnderway.body","Some messages are still being uploaded.\n"
							 + "Do you want to exit anyway?"},

	///
	///	TofTree
	///

	{"New Folder Name","New Folder Name"},
	{"New Node Name","New Board name"},
	{"newboard","newBoard"},
	{"newfolder","newFolder"},
	{"Please enter a name for the new board","Please enter a name for the new board"},
	{"Please enter a name for the new folder","Please enter a name for the new folder"},
	{"You already have a board with name","You already have a board with name"},
	{"Please choose a new name","Please choose a new name"},
	{"Do you really want to overwrite it?","Do you really want to overwrite it?"},
	{"This will not delete messages","This will not delete messages"},

	///
	///	SearchTableFormat
	///

	{"FrostSearchItemObject.Offline","Offline"},
	{"FrostSearchItemObject.Anonymous","Anonymous"},
	{"SearchTableFormat.Uploading","Uploading"},
	{"SearchTableFormat.Downloading","Downloading"},
	{"SearchTableFormat.Downloaded","Downloaded"},

	///
	/// DownloadTableFormat
	///

	{"DownloadTableFormat.Enabled", "Enabled"},
	{"Blocks", "Blocks"},
	{"Tries", "Tries"},
	{"Waiting", "Waiting"},
	{"Trying", "Trying"},
	{"Done", "Done"},
	{"Failed", "Failed"},
	{"Requesting","Requesting"},
	{"Requested","Requested"},
	{"Decoding segment","Decoding segment"},

	///
	/// UploadTableFormat
	///

	{"Never","Never"},
	{"Uploading","Uploading"},
	{"Encode requested","Encode requested"},
	{"Encoding file","Encoding file"},
	{"Unknown", "Unknown"},

	///
	///	NewBoardDialog
	///

	{"NewBoardDialog.title", "Add a new board"},
	{"NewBoardDialog.details", "Please enter the details of the new board:"},
	{"NewBoardDialog.name","Name:"},
	{"NewBoardDialog.description","Description (Optional). Do not put private information here:"},
	{"NewBoardDialog.add","Add Board"},

	///
	/// BoardSettingsFrame
	///

	{"BoardSettingsFrame.description","Description:"},
	{"BoardSettingsFrame.confirmTitle","Set Board as Public?"},
	{"BoardSettingsFrame.confirmBody","If you set this board as public, you will lose its keys. Are you sure you want to do that?"},

	///
	/// Misc (appear in several classes)
	///

	{"Description","Description"},

	//	Frost startup error messages
	{"Frost.lockFileFound", "This indicates that another Frost instance is already running in this directory.\n" +
	  					  	"Running Frost concurrently will cause data loss.\n"+
							"If you are REALLY SURE that Frost is not already running, delete the lockfile:\n"},

	//
	//  Message upload failed dialog
	//
	{"Upload of message failed", "Upload of message failed"},
	{"Frost was not able to upload your message.", "Frost was not able to upload your message."},
	{"Retry", "Retry"},
	{"Retry on next startup", "Retry on next startup"},
	{"Discard message", "Discard message"},

	//AttachedBoardTableModel
	{"Board Name", "Board name"},
	{"Access rights", "Access rights"},

	//Saver popup
	{"Saver.AutoTask.title", "Problem found."},
	{"Saver.AutoTask.message", "Frost found an error while saving a resource and it will close itself."},

	// Popup over message body (message frame)
	{"Cut","Cut"},
	{"Paste","Paste"},

	// Attach Boards Chooser (message frame)
	{"MessageFrame.ConfirmBody1", "You have the private key to board '"},
	{"MessageFrame.ConfirmBody2", "'.  Are you sure you want it attached?\n" +
								  "If you choose NO, only the public key will be attached.",},
	{"MessageFrame.ConfirmTitle", "Include private board key?"},

	// Status bar
	{"UploadStatusPanel.Uploading", "Uploading:"},
	{"StatusPanel.file", "file"},
    {"StatusPanel.files", "files"},
	{"DownloadStatusPanel.Downloading", "Downloading:"},

	// Copy to clipboard submenu
	{"Copy to clipboard", "Copy to clipboard"},
	{"Copy keys only", "Copy keys only"},
	{"Copy keys with filenames", "Copy keys with filenames"},
	{"Copy extended info", "Copy extended info"},
	{"Key not available yet", "Key not available yet"},
	{"clipboard.File:",  "File:  "},	//These three strings are a special case.
	{"clipboard.Key:",   "Key:   "},	//They must have the same length so that the
	{"clipboard.Bytes:", "Bytes: "},	//format of the output is preserved.
    
    // AltEdit support
    {"The message file returned by the alternate editor is invalid.", "The message file returned by the alternate editor is invalid."},
    {"Could not start alternate editor using command: ", "Could not start alternate editor using command: "},
    {"Could not create message file for alternate editor: ", "Could not create message file for alternate editor: "},
    {"Configured alternate editor line must contain a '%f' as placeholder for the filename.", "Configured alternate editor line must contain a '%f' as placeholder for the filename."},
    {"No alternate editor configured.", "No alternate editor configured."},
    {">>> This is a Frost alternate editor message file.                <<<", ">>> This is a Frost alternate editor message file.                <<<"},
    {">>> You can edit the subject and add text at the end of the file. <<<", ">>> You can edit the subject and add text at the end of the file. <<<"},
    {">>> Don't change or delete the marker lines!                      <<<", ">>> Don't change or delete the marker lines!                      <<<"},
    {"*--- Subject line (changeable) ---*", "*--- Subject line (changeable) ---*"}, // marker line
    {"*--- Enter your text after this line ---*", "*--- Enter your text after this line ---*"}, // marker line
    
    // Search messages dialog
    {"Sender", "Sender"},
    {"Content", "Content"},
    {"to", "to"}, // startDate 'to' endDate
    {"Boards", "Boards"},
    {"Trust state", "Trust state"},
    {"Archive", "Archive"},
    {"Attachments", "Attachments"},
    {"Search between dates", "Search between dates"},
    {"Search in messages that would be displayed", "Search in messages that would be displayed"},
    {"Search number of days backward", "Search number of days backward"},
    {"Search all dates", "Search all dates"},
    {"Search all messages, no matter which trust state is set", "Search all messages, no matter which trust state is set"},
    {"Search in messages that would be displayed", "Search in messages that would be displayed"},
    {"Search only in messages with following trust state", "Search only in messages with following trust state"},
    {"Search only in archive", "Search only in archive"},
    {"Search only in keypool", "Search only in keypool"},
    {"Search in keypool and archive", "Search in keypool and archive"},
    {"Search following boards", "Search following boards"},
    {"Search in displayed boards", "Search in displayed boards"},
    {"Search private messages only", "Search private messages only"},
    {"Good", "Good"},
    {"Tampered", "Tampered"},
    {"Stop search", "Stop search"},
    {"Close", "Close"},
    {"Open message", "Open message"},
    {"Search result", "Search result"},
    // error messages
    {"Please stop the search before closing the window.", "Please stop the search before closing the window."},
    {"No trust state is selected.", "No trust state is selected."},
    {"Invalid start date or end date specified.", "Invalid start date or end date specified."},
    {"Start date is after end date.", "Start date is after end date."},
    {"No boards to search into were chosed.", "No boards to search into were chosed."},
    {"There are no boards that could be choosed.", "There are no boards that could be choosed."},
    {"Message must contain board attachments", "Message must contain board attachments"},
    {"Message must contain file attachments", "Message must contain file attachments"}

    };
}
