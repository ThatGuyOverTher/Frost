package frost.gui;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.*;

import frost.*;
import frost.gui.model.*;
import frost.gui.objects.*;

public class TableXmlIO
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    /**************************************************
     * TABLE LOAD METHODS *****************************
     **************************************************/

////////  UPLOAD TABLE  /////////

    public static boolean loadUploadTableItems( UploadTableModel model, String filename )
    {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(filename, false);
        } catch(Exception ex) { ; } // xml format error

        if( doc == null )
        {
            System.out.println("Error - loadUploadTableItems: factory could'nt create XML Document.");
            return false;
        }

        Element rootNode = doc.getDocumentElement();

        if( rootNode.getTagName().equals("FrostUploadTable") == false )
        {
            System.out.println("Error - uploads.xml invalid: does not contain the root tag 'FrostUploadTable'");
            return false;
        }
        // check if rootnode contains only a single boardEntry wich must be a folder (root folder)
        ArrayList nodelist = XMLTools.getChildElementsByTagName(rootNode, "FrostUploadTableItemList");
        if( nodelist.size() != 1 )
        {
            return false;
        }

        Element itemListRootNode = (Element)nodelist.get(0);

        nodelist = XMLTools.getChildElementsByTagName( itemListRootNode, "FrostUploadTableItem" );

        if( nodelist.size() == 0 )
            return true; // empty save file

        for( int x=0; x<nodelist.size(); x++ )
        {
            Element uploadItemElement = (Element)nodelist.get(x);
            appendUploadTableItemToModel( uploadItemElement, model );
        }
System.out.println("Loaded "+nodelist.size()+" items into upload table.");
        return true;
    }

    protected static void appendUploadTableItemToModel(Element ulItemElement, UploadTableModel model)
    {
        FrostUploadItemObject ulObj = getUploadItemFromElement( ulItemElement );
        if( ulObj == null )
            return;
        model.addRow( ulObj );
    }

    protected static FrostUploadItemObject getUploadItemFromElement(Element ulItemElement)
    {
        String filename = XMLTools.getChildElementsCDATAValue(ulItemElement, "filename");
        String filepath = XMLTools.getChildElementsCDATAValue(ulItemElement, "filepath");
        String targetboardname = XMLTools.getChildElementsTextValue(ulItemElement, "targetboard");
        String state = XMLTools.getChildElementsTextValue(ulItemElement, "state");
        String lastUploadDate = XMLTools.getChildElementsTextValue(ulItemElement, "lastuploaddate");
        String key = XMLTools.getChildElementsCDATAValue(ulItemElement, "key");
	String SHA1 = XMLTools.getChildElementsCDATAValue(ulItemElement, "SHA1");

        if( filename == null || filepath == null || targetboardname == null || state == null )
        {
            System.out.println("UploadTable: Error in XML save file, skipping entry.");
            return null;
        }

        int iState = -1;
        try { iState = Integer.parseInt( state ); }
        catch(NumberFormatException ex)
        {
            // string is no number -> old format
            iState = -1;
        }

        if( iState < 0 )
        {
            // old format: states are saved in XML as LangRes Strings
            if( state.indexOf("Kb") != -1 || state.equals(LangRes.getString("Uploading")) )
            {
                iState = FrostUploadItemObject.STATE_REQUESTED;
            }
        }
        else
        {
            // new format: states are saved in XML as numbers
            if( iState == FrostUploadItemObject.STATE_PROGRESS ||
                iState == FrostUploadItemObject.STATE_UPLOADING )
            {
                iState = FrostUploadItemObject.STATE_REQUESTED;
            }
        }

        if( key != null && key.startsWith("CHK@") == false )
        {
            key = null;
        }

        File uploadFile = new File(filepath);

        if( !uploadFile.isFile() || uploadFile.length() == 0 )
        {
            System.out.println("UploadTable: file '"+filepath+"' was not found, removing file from table.");
            return null;
        }

        // check if target board exists in board tree
        FrostBoardObject board = frame1.getInstance().getTofTree().getBoardByName( targetboardname );
        if( board == null )
        {
            System.out.println("UploadTable: target board '"+targetboardname+"' for file '"+filepath+"' was not found, removing file from table.");
            return null;
        }

        // create FrostUploadItemObject
        FrostUploadItemObject ulItem = new FrostUploadItemObject( filename,
                                                                  filepath,
                                                                  uploadFile.length(),
                                                                  board,
                                                                  iState,
                                                                  lastUploadDate,
                                                                  key,
								  SHA1);
        return ulItem;
    }

////////  DOWNLOAD TABLE  /////////

    public static boolean loadDownloadTableItems( DownloadTableModel model, String filename )
    {
        Document doc = null;
        try {
            doc = XMLTools.parseXmlFile(filename, false);
        } catch(Exception ex) { ; } // xml format error

        if( doc == null )
            return false;

        Element rootNode = doc.getDocumentElement();

        if( rootNode.getTagName().equals("FrostDownloadTable") == false )
        {
            System.out.println("Error - uploads.xml invalid: does not contain the root tag 'FrostDownloadTable'");
            return false;
        }
        // check if rootnode contains only a single boardEntry wich must be a folder (root folder)
        ArrayList nodelist = XMLTools.getChildElementsByTagName(rootNode, "FrostDownloadTableItemList");
        if( nodelist.size() != 1 )
            return false;

        Element itemListRootNode = (Element)nodelist.get(0);

        nodelist = XMLTools.getChildElementsByTagName( itemListRootNode, "FrostDownloadTableItem" );

        if( nodelist.size() == 0 )
            return true; // empty save file

        for( int x=0; x<nodelist.size(); x++ )
        {
            Element downloadItemElement = (Element)nodelist.get(x);
            appendDownloadTableItemToModel( downloadItemElement, model );
        }
System.out.println("Loaded "+nodelist.size()+" items into download table.");
        return true;
    }

    protected static void appendDownloadTableItemToModel(Element dlItemElement, DownloadTableModel model)
    {
        FrostDownloadItemObject dlObj = getDownloadItemFromElement( dlItemElement );
        if( dlObj == null )
            return;
        model.addRow( dlObj );
    }

    protected static FrostDownloadItemObject getDownloadItemFromElement(Element dlItemElement)
    {
        String filename = XMLTools.getChildElementsCDATAValue(dlItemElement, "filename");
        String filesize = XMLTools.getChildElementsTextValue(dlItemElement, "filesize");
        String fileage = XMLTools.getChildElementsTextValue(dlItemElement, "fileage");
        String key = XMLTools.getChildElementsCDATAValue(dlItemElement, "key");
        String retries = XMLTools.getChildElementsTextValue(dlItemElement, "retries");
        String state = XMLTools.getChildElementsTextValue(dlItemElement, "state");
	String owner = XMLTools.getChildElementsTextValue(dlItemElement, "owner");
        String sourceboardname = XMLTools.getChildElementsTextValue(dlItemElement, "sourceboard");
        String enableDownload = dlItemElement.getAttribute("enableDownload");

        if( filename == null || key == null || state == null )
        {
            System.out.println("DownloadTable: Error in XML save file, skipping entry.");
            return null;
        }

        int iState = -1;
        try { iState = Integer.parseInt( state ); }
        catch(NumberFormatException ex)
        {
            // string is no number -> old format
            iState = -1;
        }

        if( iState < 0 )
        {
            // old format: states are saved in XML as LangRes Strings
            if( state.equals(LangRes.getString("Done")) == false )
            {
                iState = FrostDownloadItemObject.STATE_WAITING;
            }
        }
        else
        {
            // new format: states are saved in XML as numbers
            if( iState != FrostDownloadItemObject.STATE_DONE )
            {
                iState = FrostDownloadItemObject.STATE_WAITING;
            }
        }

        boolean isDownloadEnabled = false;
        if( enableDownload == null ||
            enableDownload.length() == 0 ||
            enableDownload.toLowerCase().equals("true") )
        {
            isDownloadEnabled = true; // default is true
        }

        // check if target board exists in board tree

        FrostBoardObject board = null;
        if( sourceboardname != null )
        {
            board = frame1.getInstance().getTofTree().getBoardByName( sourceboardname );
            if( board == null )
            {
                System.out.println("DownloadTable: source board '"+sourceboardname+"' for file '"+filename+"' was not found, removing file from table.");
                return null;
            }
        }

        // create FrostDownloadItemObject
        FrostDownloadItemObject dlItem = new FrostDownloadItemObject(filename,
                                                                     filesize,
                                                                     fileage,
                                                                     key,
                                                                     retries,
								     owner,
                                                                     iState,
                                                                     isDownloadEnabled,
                                                                     board);
        return dlItem;
    }

    /**************************************************
     * TABLE SAVE METHODS *****************************
     **************************************************/

////////  UPLOAD TABLE  /////////

    public static boolean saveUploadTableItems( UploadTableModel model, String filename )
    {
        Document doc = XMLTools.createDomDocument();
        if( doc == null )
        {
            System.out.println("Error - saveUploadTableItems: factory could'nt create XML Document.");
            return false;
        }

        Element rootElement = doc.createElement("FrostUploadTable");
        doc.appendChild(rootElement);

        Element itemsRoot = doc.createElement("FrostUploadTableItemList");
        rootElement.appendChild( itemsRoot );

        // now add all items to itemsRoot
        for( int x=0; x<model.getRowCount(); x++ )
        {
            FrostUploadItemObject ulItem = (FrostUploadItemObject)model.getRow( x );
            appendUploadItemToDomTree( itemsRoot, ulItem, doc );
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, filename);
System.out.println("Saved "+model.getRowCount()+" items from upload table.");
        } catch(Throwable t)
        {
            System.out.println("Exception - saveUploadTableItems:");
            t.printStackTrace();
System.out.println("ERROR saving upload table!");
        }
        return writeOK;
    }

    protected static void appendUploadItemToDomTree( Element parent, FrostUploadItemObject ulItem, Document doc )
    {
        Element itemElement = doc.createElement("FrostUploadTableItem");
        Element element;
        Text text;
        CDATASection cdata;
        // filename
        element = doc.createElement("filename");
        cdata = doc.createCDATASection( ulItem.getFileName() );
        element.appendChild( cdata );
        itemElement.appendChild( element );
        // filepath
        element = doc.createElement("filepath");
        cdata = doc.createCDATASection( ulItem.getFilePath() );
        element.appendChild( cdata );
        itemElement.appendChild( element );
        // targetboard
        element = doc.createElement("targetboard");
        text = doc.createTextNode( ulItem.getTargetBoard().toString() );
        element.appendChild( text );
        itemElement.appendChild( element );
        // state
        element = doc.createElement("state");
        text = doc.createTextNode( String.valueOf(ulItem.getState()) );
        element.appendChild( text );
        itemElement.appendChild( element );
        // key
        if( ulItem.getKey() != null )
        {
            element = doc.createElement("key");
            cdata = doc.createCDATASection( ulItem.getKey() );
            element.appendChild( cdata );
            itemElement.appendChild( element );
        }
	if( ulItem.getSHA1() != null )
        {
            element = doc.createElement("SHA1");
            cdata = doc.createCDATASection( ulItem.getSHA1() );
            element.appendChild( cdata );
            itemElement.appendChild( element );
        }
        // lastUploadDate
        if( ulItem.getLastUploadDate() != null )
        {
            element = doc.createElement("lastuploaddate");
            text = doc.createTextNode( ulItem.getLastUploadDate() );
            element.appendChild( text );
            itemElement.appendChild( element );
        }

        parent.appendChild( itemElement );
    }

////////  DOWNLOAD TABLE  /////////

    public static boolean saveDownloadTableItems( DownloadTableModel model, String filename )
    {
        Document doc = XMLTools.createDomDocument();
        if( doc == null )
        {
            System.out.println("Error - saveDownloadTableItems: factory could'nt create XML Document.");
            return false;
        }

        Element rootElement = doc.createElement("FrostDownloadTable");
        doc.appendChild(rootElement);

        Element itemsRoot = doc.createElement("FrostDownloadTableItemList");
        rootElement.appendChild( itemsRoot );

        // now add all items to itemsRoot
        for( int x=0; x<model.getRowCount(); x++ )
        {
            FrostDownloadItemObject dlItem = (FrostDownloadItemObject)model.getRow( x );
            appendDownloadItemToDomTree( itemsRoot, dlItem, doc );
        }

        boolean writeOK = false;
        try {
            writeOK = XMLTools.writeXmlFile(doc, filename);
System.out.println("Saved "+model.getRowCount()+" items from download table.");
        } catch(Throwable t)
        {
            System.out.println("Exception - saveDownloadTableItems:");
            t.printStackTrace();
System.out.println("ERROR saving download table!");
        }

        return writeOK;
    }

    protected static void appendDownloadItemToDomTree( Element parent, FrostDownloadItemObject dlItem, Document doc )
    {
        Element itemElement = doc.createElement("FrostDownloadTableItem");
        String isDownloadEnabled;
        if( dlItem.getEnableDownload() == null )
            isDownloadEnabled = "true";
        else
            isDownloadEnabled = dlItem.getEnableDownload().toString();
        itemElement.setAttribute("enableDownload", isDownloadEnabled );

        Element element;
        Text text;
        CDATASection cdata;
        // filename
        element = doc.createElement("filename");
        cdata = doc.createCDATASection( dlItem.getFileName() );
        element.appendChild( cdata );
        itemElement.appendChild( element );
        // filesize
        if( dlItem.getFileSize() != null )
        {
            element = doc.createElement("filesize");
            text = doc.createTextNode( dlItem.getFileSize().toString() );
            element.appendChild( text );
            itemElement.appendChild( element );
        }
        // fileage
        element = doc.createElement("fileage");
        text = doc.createTextNode( dlItem.getFileAge() );
        element.appendChild( text );
        itemElement.appendChild( element );
        // key
        element = doc.createElement("key");
        cdata = doc.createCDATASection( dlItem.getKey() );
        element.appendChild( cdata );
        itemElement.appendChild( element );
        // retries
        element = doc.createElement("retries");
        text = doc.createTextNode( String.valueOf(dlItem.getRetries()) );
        element.appendChild( text );
        itemElement.appendChild( element );
        // state
        element = doc.createElement("state");
        text = doc.createTextNode( String.valueOf(dlItem.getState()) );
        element.appendChild( text );
        itemElement.appendChild( element );
        // sourceboard
        if( dlItem.getSourceBoard() != null )
        {
            element = doc.createElement("sourceboard");
            text = doc.createTextNode( dlItem.getSourceBoard().toString() );
            element.appendChild( text );
            itemElement.appendChild( element );
        }

        parent.appendChild( itemElement );
    }
}
