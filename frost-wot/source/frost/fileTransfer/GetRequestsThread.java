/*
  GetRequestsThread.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fileTransfer;

import java.io.File;
import java.util.*;
import java.util.logging.*;

import frost.*;
import frost.fcp.FcpRequest;
import frost.fileTransfer.upload.*;
import frost.identities.FrostIdentities;
/**
 * Downloads file requests
 */

public class GetRequestsThread extends Thread
{
    private UploadModel model;

    private FrostIdentities identities;

    static java.util.ResourceBundle LangRes =
        java.util.ResourceBundle.getBundle("res.LangRes");

    private static Logger logger = Logger.getLogger(GetRequestsThread.class.getName());

    //public FrostBoardObject board;
    private int downloadHtl;
    private String keypool;
    private String destination;
    private String fileSeparator = System.getProperty("file.separator");
    private String date;

    /*public int getThreadType()
    {
        return BoardUpdateThread.BOARD_FILE_UPLOAD;
    }*/

    public void run() {
        //first, start today's thread.
        if (DateFun.getDate().compareTo(date) != 0) {

            GetRequestsThread todaysRequests =
                new GetRequestsThread(
                    downloadHtl,
                    keypool,
                    model,
                    DateFun.getDate(),
                    identities);
            todaysRequests.start();
            try {
                todaysRequests.join();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Exception thrown in run()", e);
            }
        }
        // notifyThreadStarted(this);
        try {

            // Wait some random time to speed up the update of the TOF table
            // ... and to not to flood the node
            int waitTime = (int) (Math.random() * 5000);
            // wait a max. of 5 seconds between start of threads
            Mixed.wait(waitTime);

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));

            String dirdate = date;

            /*destination = new StringBuffer().append(keypool)
                            .append(board.getBoardFilename()).append(fileSeparator)
                            .append(dirdate).append(fileSeparator).toString();*/

            //yes mister spammer, this is a special for you!
            destination =
                new StringBuffer()
                    .append("requests")
                    .append(fileSeparator)
                    .append(Mixed.makeFilename(identities.getMyId().getUniqueName()))
                    .toString();

            File makedir = new File(destination);
            if (!makedir.exists()) {
                logger.info(
                    Thread.currentThread().getName() + ": Creating directory: " + destination);
                makedir.mkdirs();
            }

            if (isInterrupted()) {
                //notifyThreadFinished(this);
                return;
            }

            //start the request loop
            Mixed.wait(60 * 1000); // wait 1 min before first start
            boolean firstRun = true;
            while (true) {
                dirdate = date;
                if (firstRun) {
                    firstRun = false;
                } else {
                    Mixed.wait(15 * 60 * 1000);
                }
                if (Core.getMyBatches().isEmpty())
                    continue;
                //do not start requesting until the user has shared something
                try {
                    Iterator it = Core.getMyBatches().keySet().iterator();
                    while (it.hasNext()) {
                        String currentBatch = (String) it.next();
                        int index = 0;
                        int failures = 0;
                        int maxFailures = 3;
                        // increased, skips now up to 3 request indicies (in case if gaps occured)
                        while (failures < maxFailures) {
                            String val =
                                new StringBuffer()
                                    .append(destination)
                                    .append(fileSeparator)
                                    .append(currentBatch.trim())
                                    .append("-")
                                    .append(dirdate)
                                    .append("-")
                                    .append(index)
                                    .append(".req.sha")
                                    .toString();
                            File testMe = new File(val);
                            boolean justDownloaded = false;

                            // already downloaded ?
                            if (testMe.length() > 0) {
                                index++;
                                failures = 0;
                                continue;
                            } else {
                                String tmp =
                                    new StringBuffer()
                                        .append("GetRequestsThread.run, file = ")
                                        .append(testMe.getName())
                                        .append(", failures = ")
                                        .append(failures)
                                        .toString();
                                logger.fine(tmp);

                                FcpRequest.getFile(
                                    "KSK@frost/request/"
                                        + MainFrame.frostSettings.getValue("messageBase")
                                        + "/"
                                        + Mixed.makeFilename(identities.getMyId().getUniqueName())
                                        + "-"
                                        + testMe.getName(),
                                    null,
                                    testMe,
                                    downloadHtl,
                                    false);
                                justDownloaded = true;
                            }

                            // Download successful?
                            if (testMe.length() > 0 /* && justDownloaded */
                                ) {
                                logger.fine(
                                    Thread.currentThread().getName()
                                        + " Received request "
                                        + testMe.getName());
// TODO: read multiple requests
                                String content = (FileAccess.readFile(testMe)).trim();
                                logger.fine("Request content is " + content);
                                int rowCount = model.getItemCount();

                                for (int i = 0; i < rowCount; i++) {
                                    FrostUploadItem ulItem =
                                        (FrostUploadItem) model.getItemAt(i);
                                    String SHA1 = ulItem.getSHA1();
                                    if (SHA1 == null)
                                        continue;
                                    else
                                        SHA1 = SHA1.trim();
                                    //Core.getOut().println("comparing requested "+content + " with "+SHA1);
                                    if (SHA1.equals(content)) {
                                        logger.fine("content matched!");
                                        // ? is'nt it possible to use uploadItem.getLastUploadData for this?
                                        // probably, this .lck thing is jantho's style
                                        File requestLock = new File(destination + SHA1 + ".lck");
                                        if (!requestLock.exists()) {
                                            // for handling of ENCODING state see ulItem.getNextState() javadoc
                                            // changing state ENCODING_REQUESTED to REQUESTED is ok!
                                            if (ulItem.getState()
                                                != FrostUploadItem.STATE_UPLOADING
                                                && ulItem.getState()
                                                    != FrostUploadItem.STATE_PROGRESS)
                                                //TOTHINK: this is optional
                                                {
                                                logger.fine("Request matches row " + i);
                                                if (ulItem.getState()
                                                    == FrostUploadItem.STATE_ENCODING) {
                                                    ulItem.setNextState(
                                                        FrostUploadItem.STATE_REQUESTED);
                                                } else {
                                                    ulItem.setState(
                                                        FrostUploadItem.STATE_REQUESTED);
                                                }
                                            } else
                                                logger.fine("file was in state uploading/progress");
                                        } else {
                                            logger.info(
                                                "File with hash "
                                                    + SHA1
                                                    + " was requested, but already uploaded today");
                                        }
                                    } //else Core.getOut().println("content didn't match");
                                }
                                index++;
                                failures = 0;
                            } else {
                                index++;
                                // this now skips gaps in requests, but gives each download only 1 try
                                failures++;
                            }
                            if (isInterrupted()) {
                                break;
                            }
                        }

                    }

                } catch (ConcurrentModificationException e) {
                    continue;
                }
            }
        } //people with nice ides can refactor :-P
        catch (Throwable t) {
            logger.log(
                Level.SEVERE,
                Thread.currentThread().getName() + ": Oo. EXCEPTION in GetRequestsThread:",
                t);
        }

        //notifyThreadFinished(this);
    }

    /**Constructor*/
    public GetRequestsThread(
        int dlHtl,
        String kpool,
        UploadModel newModel,
        FrostIdentities newIdentities) {

        this(dlHtl, kpool, newModel, DateFun.getDate(1), newIdentities);
    }
    public GetRequestsThread(
        int htl,
        String newKeypool,
        UploadModel newModel,
        String newDate,
        FrostIdentities newIdentities) {

        downloadHtl = htl;
        keypool = newKeypool;
        model = newModel;
        date = newDate;
        identities = newIdentities;
    }

}
