/*
  SendMessagesTableModel.java / Frost
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
package frost.gui.sentmessages;

import java.util.*;

import frost.messages.*;
import frost.util.model.*;

public class SentMessagesTableModel extends OrderedModel {

    public SentMessagesTableModel() {
        super();
    }
    
    public void loadTableModel() {
        List msgs = SentMessagesManager.retrieveSentMessages();
        for( Iterator i = msgs.iterator(); i.hasNext(); ) {
            FrostMessageObject mo = (FrostMessageObject) i.next();
            SentMessagesTableItem item = new SentMessagesTableItem(mo);
            addSendMessageItem(item);
        }
    }
    
    public void addSendMessageItem(SentMessagesTableItem item) {
        addItem(item);
    }
    
    public void addFrostMessageObject( FrostMessageObject mo ) {
        SentMessagesTableItem item = new SentMessagesTableItem(mo);
        addSendMessageItem(item);
    }
    
    public boolean removeItems(ModelItem[] selectedItems) {
        LinkedList itemsToDelete = new LinkedList();
        for(int x=0; x < selectedItems.length; x++) {
            SentMessagesTableItem item = (SentMessagesTableItem) selectedItems[x];
            itemsToDelete.add(item.getFrostMessageObject());
        }
        if( SentMessagesManager.deleteSentMessages(itemsToDelete) == 0 ) {
            return false;
        }
        return super.removeItems(selectedItems);
    }
}
