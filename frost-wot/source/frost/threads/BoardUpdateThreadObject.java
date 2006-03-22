/*
  BoardUpdateThreadObject.java / Frost
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

package frost.threads;

import java.util.*;

import frost.gui.objects.Board;
import frost.identities.FrostIdentities;

/**
 * This class implements most methods needed by the BoardUpdateThread interface.
 * A thread have to extend this class and to implement BoardUpdateThread.
 */
public class BoardUpdateThreadObject extends Thread {

    protected FrostIdentities identities;
    Board targetBoard = null;
    long startTimeMillis = -1;
    boolean isFinished = false;
    Vector registeredListeners = null;

    public BoardUpdateThreadObject(Board board, FrostIdentities newIdentities) {
        super(board.getName());
        this.targetBoard = board;
        this.registeredListeners = new Vector();
        identities = newIdentities;
    }

    // FrostBoard getTargetBoard()
    public Board getTargetBoard() {
        return targetBoard;
    }

    /**
     * Returns -1 if not yet started
    */
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    // not using isAlive() here, because Thread will call listener before it dies
    // so using own implementation of finished
    public synchronized boolean isFinished() {
        return isFinished;
    }
    protected synchronized void threadFinished() {
        isFinished = true;
    }

    /**
     * Called from Thread to notify all listeners that thread is started now
     */
    protected void notifyThreadStarted(BoardUpdateThread thread) {
        this.startTimeMillis = System.currentTimeMillis();
        // notify listeners
        Iterator i = registeredListeners.iterator();
        while( i.hasNext() ) {
            ((BoardUpdateThreadListener)i.next()).boardUpdateThreadStarted(thread);
        }
    }
    /**
     * Called from Thread to notify all listeners that thread is started now
     */
    protected void notifyThreadFinished(BoardUpdateThread thread) {
        threadFinished();
        // notify listeners
        Iterator i = registeredListeners.iterator();
        while( i.hasNext() ) {
            ((BoardUpdateThreadListener)i.next()).boardUpdateThreadFinished(thread);
        }
    }

    // allow to register listener, should only be used by RunningBoardUpdateThreads class
    // other classes should register to the RunningBoardUpdateThreads class
    // the difference is: if the thread class the listener directly, the ArrayList = null
    // the underlying Thread class should fire the finished event with parameters:
    //  boardUpdateThreadFinished(null, this)
    public void addBoardUpdateThreadListener(BoardUpdateThreadListener listener) {
        registeredListeners.add( listener );
    }
    public void removeBoardUpdateThreadListener(BoardUpdateThreadListener listener) {
        registeredListeners.remove( listener );
    }
}
