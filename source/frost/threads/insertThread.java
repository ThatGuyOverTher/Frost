/*
  insertThread.java / Frost
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

package frost.threads;

import java.io.File;

import frost.*;
import frost.gui.model.UploadTableModel;
import frost.gui.objects.*;

public class insertThread extends Thread
{
    static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes")/*#BundleType=List*/;

    private String destination;
    private File file;
    private String htl;
    private FrostBoardObject board;
    private boolean mode;

    FrostUploadItemObject uploadItem;

    public void run()
    {
        System.out.println("Upload of " + file + " with HTL " + htl + " started.");

        if( mode )
            synchronized(frame1.threadCountLock)
            {
                frame1.activeUploadThreads++;
            }
        else
        {
            frame1.setGeneratingCHK( true );
        }

        try {

        String lastUploadDate = null; // NEVER uploaded
        boolean success = false;
        String[] result = {"Error", "Error"};

        if( file.length() > 0 && file.isFile() )
        {
            String oldLastUploadDate = uploadItem.getLastUploadDate();

	    if (mode) {
            result = FcpInsert.putFile("CHK@",
                                       file,
                                       htl,
                                       true,
                                       mode,
                                       board.getBoardFilename());

            if( result[0].equals("Success") )
            {
                success = true;
                System.out.println("Upload of " + file + " successfull.");
		uploadItem.setKey(result[1]);
            }
            else if( result[0].equals("KeyCollision") )
            {
                // collided means file is already in freenet
                success = true;
                System.out.println("Upload of " + file + " collided.");
		uploadItem.setKey(result[1]);
            }
	    } else 
	    	success =true; //so that we generate SHA1
	     
	    
	    String SHA1=null;
            if( success )
            {
	    	//generate SHA1 - its fast
	
		long now = System.currentTimeMillis();
		SHA1 = frame1.getCrypto().digest(file);
		System.out.println("digest generated in "+(System.currentTimeMillis()-now) +
				 "  " + SHA1);
		
                String date = DateFun.getExtendedDate();
                lastUploadDate = date;
		KeyClass newKey;
		if (mode) {
                	newKey = new KeyClass();
			newKey.setDate(date);
			newKey.setKey(result[1]);
		}
		else  {
			newKey = new KeyClass();
			newKey.setKey(null);
			newKey.setDate(null);
		}
		newKey.setSHA1(SHA1);  
                newKey.setFilename(destination);
                newKey.setSize(file.length());
		newKey.setOwner(frame1.getMyId().getUniqueName());
                Index.add(newKey, new File(frame1.keypool + board.getBoardFilename()));
            }
            else
            {
                lastUploadDate = oldLastUploadDate; // NEVER uploaded
            }

            // update table item
            if( mode )
            {
                // item uploaded (maybe)
                uploadItem.setLastUploadDate( lastUploadDate ); // if NULL then upload failed -> shows NEVER in table
		uploadItem.setKey(result[1]);
            }
            else if( success )
            {
                // key was computed?
                uploadItem.setSHA1( SHA1 );
		uploadItem.setKey(null);
		uploadItem.setLastUploadDate(null);
            }
            uploadItem.setState( FrostUploadItemObject.STATE_IDLE );

            UploadTableModel tableModel = (UploadTableModel)frame1.getInstance().getUploadTable().getModel();
            tableModel.updateRow( uploadItem );
        }
        }
        catch(Throwable t)
        {
            System.out.println("Oo. EXCEPTION in insertThread.run:");
            t.printStackTrace();
        }

        if( mode )
        {
            synchronized(frame1.threadCountLock)
            {
                frame1.activeUploadThreads--;
            }
        }
        else
        {
            frame1.setGeneratingCHK( false );
        }
    }

    /**Constructor*/
    public insertThread(FrostUploadItemObject ulItem, SettingsClass config, boolean mode)
    {
        this.destination = ulItem.getFileName();
        this.file = new File(ulItem.getFilePath());

        this.uploadItem = ulItem;

        this.htl = config.getValue("htlUpload");
        this.board = ulItem.getTargetBoard();
        this.mode = mode; // true=upload file false=generate chk (do not upload)
    }
}
