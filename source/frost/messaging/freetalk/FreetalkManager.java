/*
  FreetalkManager.java / Frost
  Copyright (C) 2009  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging.freetalk;

import frost.fcp.*;
import frost.fcp.fcp07.freetalk.*;

public class FreetalkManager {

    private FcpFreetalkConnection fcpFreetalkConnection = null;

    private static FreetalkManager instance = null;

    private FreetalkManager() {
        try {
            if (FcpHandler.inst().getFreenetNode() == null) {
                throw new Exception("No freenet nodes defined");
            }
            final NodeAddress na = FcpHandler.inst().getFreenetNode();
            fcpFreetalkConnection = new FcpFreetalkConnection(na);
        } catch(final Exception ex) {
            fcpFreetalkConnection = null;
        }
    }

    public static FreetalkManager getInstance() {
        return instance;
    }

    public synchronized static void initialize() {
        if (instance == null) {
            instance = new FreetalkManager();
        }
    }

    /**
     * Connection is null when Freetalk plugin is not Talkable.
     */
    public FcpFreetalkConnection getConnection() {
        return fcpFreetalkConnection;
    }
}
