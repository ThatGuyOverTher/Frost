/*
  SearchModel.java / Frost
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
package frost.fileTransfer.search;

import java.util.*;

import frost.fileTransfer.*;
import frost.fileTransfer.download.*;
import frost.util.model.*;

public class SearchModel extends OrderedModel {

    private DownloadModel downloadModel;

    public SearchModel() {
        super();
        downloadModel = FileTransferManager.getInstance().getDownloadManager().getModel();
    }

    public void addSearchItem(FrostSearchItem searchItem) {
        addItem(searchItem);
    }

    public void addItemsToDownloadModel(ModelItem[] selectedItems) {
        for (int i = 0; i < selectedItems.length; i++) {
            FrostSearchItem searchItem = (FrostSearchItem) selectedItems[i];
            FrostFileListFileObject flf = searchItem.getFrostFileListFileObject();
            
            // FIXME: reload FileListFile from db, maybe the key arrived in the meantime!
            FrostDownloadItem dlItem = new FrostDownloadItem(flf, flf.getDisplayName());
            downloadModel.addDownloadItem(dlItem);
        }
    }

    public synchronized void addAllItemsToDownloadModel() {
        Iterator iterator = data.iterator();
        while (iterator.hasNext()) {
            FrostSearchItem searchItem = (FrostSearchItem) iterator.next();
            FrostFileListFileObject flf = searchItem.getFrostFileListFileObject();
            
            // FIXME: reload FileListFile from db, maybe the key arrived in the meantime!
            FrostDownloadItem dlItem = new FrostDownloadItem(flf, flf.getDisplayName());
            downloadModel.addDownloadItem(dlItem);
        }
    }
}
