/*
  FCPNodeToClientMessage.java / Frost
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
package hyperocha.freenet.fcp.messages.node2client;

import hyperocha.freenet.fcp.messages.*;

public abstract class FCP2NodeToClientMessage extends FCPMessage {
    
    protected String connectionId;
    protected String messageName;
    
    protected boolean isConsumed = false;
    
    protected FCP2NodeToClientMessage(String connId, String msgName) {
        connectionId = connId;
        messageName = msgName;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public boolean isConsumed() {
        return isConsumed;
    }

    public void setConsumed(boolean isConsumed) {
        this.isConsumed = isConsumed;
    }
}
