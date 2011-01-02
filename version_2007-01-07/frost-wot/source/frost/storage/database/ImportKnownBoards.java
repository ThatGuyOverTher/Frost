/*
  ImportKnownBoards.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.storage.database;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import frost.gui.*;
import frost.storage.*;
import frost.storage.database.applayer.*;

public class ImportKnownBoards {
    
    private static Logger logger = Logger.getLogger(ImportKnownBoards.class.getName());

    public void importKnownBoards(File knownBoardsXmlFile) {
        List knownBoards = KnownBoardsXmlDAO.loadKnownBoards(knownBoardsXmlFile);

        try {
            AppLayerDatabase.getInstance().setAutoCommitOff();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error set autocommit off", e);
        }

        KnownBoardsManager.addNewKnownBoards(knownBoards);
        
        try {
            AppLayerDatabase.getInstance().setAutoCommitOn();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "error set autocommit on", e);
        }
    }
}
