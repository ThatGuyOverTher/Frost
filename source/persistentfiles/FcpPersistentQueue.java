/*
  FcpPersistentQueue.java / Frost
  Copyright (C) 2007  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fcp.fcp07.filepersistence;

import java.util.*;

import frost.fcp.fcp07.*;

public class FcpPersistentQueue implements NodeMessageListener {

//    private static final Logger logger = Logger.getLogger(FcpPersistentQueue.class.getName());

    private FcpMultiRequestConnectionTools fcpTools;
    private IFcpPersistentRequestsHandler persistenceHandler;

    // we hold all requests, gui shows only the wanted requests (all or own)
    private HashMap<String,FcpPersistentPut> uploadRequests = new HashMap<String,FcpPersistentPut>();
    private HashMap<String,FcpPersistentGet> downloadRequests = new HashMap<String,FcpPersistentGet>();

    public FcpPersistentQueue(FcpMultiRequestConnectionTools tools, IFcpPersistentRequestsHandler pman) {
        fcpTools = tools;
        persistenceHandler = pman;
    }
    
    public void startThreads() {
        fcpTools.getFcpPersistentConnection().addNodeMessageListener(this);
        fcpTools.watchGlobal(true);
        fcpTools.listPersistentRequests();
    }
    
    public Map<String,FcpPersistentPut> getUploadRequests() {
        return getUploadRequestsCopy();
    }
    public Map<String,FcpPersistentGet> getDownloadRequests() {
        return getDownloadRequestsCopy();
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<String,FcpPersistentPut> getUploadRequestsCopy() {
        return (Map<String,FcpPersistentPut>)uploadRequests.clone();
    }
    @SuppressWarnings("unchecked")
    public synchronized Map<String,FcpPersistentGet> getDownloadRequestsCopy() {
        return (Map<String,FcpPersistentGet>)downloadRequests.clone();
    }
    
    public void connected() {
        persistenceHandler.connected();
        // we are reconnected
        fcpTools.watchGlobal(true);
        fcpTools.listPersistentRequests();
    }

    public void disconnected() {
        persistenceHandler.disconnected();

        uploadRequests.clear();
        downloadRequests.clear();
    }

    public void handleNodeMessage(NodeMessage nm) {
        // handle a NodeMessage without identifier
    }
    
    public void handleNodeMessage(String id, NodeMessage nm) {

        System.out.println(">>>RCV>>>>");
        System.out.println("MSG="+nm);
        System.out.println("<<<<<<<<<<");
        
        if( nm.isMessageName("PersistentGet") ) {
            onPersistentGet(id, nm);
        } else if( nm.isMessageName("DataFound") ) {
            onDataFound(id, nm);
        } else if( nm.isMessageName("GetFailed") ) {
            onGetFailed(id, nm);
        } else if( nm.isMessageName("PersistentPut") ) {
            onPersistentPut(id, nm);
        } else if( nm.isMessageName("PutSuccessful") ) {
            onPutSuccessful(id, nm);
        } else if( nm.isMessageName("PutFailed") ) {
            onPutFailed(id, nm);
        } else if( nm.isMessageName("SimpleProgress") ) {
            onSimpleProgress(id, nm);
        } else if( nm.isMessageName("PersistentRequestRemoved") ) {
            onPersistentRequestRemoved(id, nm);
        } else if( nm.isMessageName("PersistentRequestModified") ) {
            onPersistentRequestModified(id, nm);
        } else if( nm.isMessageName("IdentifierCollision") ) {
            onIdentifierCollision(id, nm);
        } else if( nm.isMessageName("ProtocolError") ) {
            onProtocolError(id, nm);
        } else {
            // unhandled msg
            System.out.println("### INFO - Unhandled msg: "+nm);
        }
    }
    
    protected void onPersistentGet(String id, NodeMessage nm) {
        if( downloadRequests.containsKey(id) ) {
            FcpPersistentGet pg = downloadRequests.get(id);
            pg.setRequest(nm);
            persistenceHandler.persistentRequestUpdated(pg);
            return;
        } else {
            FcpPersistentGet fpg = new FcpPersistentGet(nm, id);
            downloadRequests.put(id, fpg);
            persistenceHandler.persistentRequestAdded(fpg);
        }
    }
    protected void onDataFound(String id, NodeMessage nm) {
        if( !downloadRequests.containsKey(id) ) {
            System.out.println("No item in download queue: "+nm);
        } else {
            FcpPersistentGet pg = downloadRequests.get(id); 
            pg.setSuccess(nm);
            persistenceHandler.persistentRequestUpdated(pg);
        }
    }
    protected void onGetFailed(String id, NodeMessage nm) {
        if( !downloadRequests.containsKey(id) ) {
            System.out.println("No item in download queue: "+nm);
        } else {
            FcpPersistentGet pg = downloadRequests.get(id); 
            pg.setFailed(nm);
            persistenceHandler.persistentRequestUpdated(pg);
        }
    }
    protected void onPersistentPut(String id, NodeMessage nm) {
        if( uploadRequests.containsKey(id) ) {
            FcpPersistentPut pg = uploadRequests.get(id);
            pg.setRequest(nm);
            persistenceHandler.persistentRequestUpdated(pg);
        } else {
            FcpPersistentPut fpg = new FcpPersistentPut(nm, id);
            uploadRequests.put(id, fpg);
            persistenceHandler.persistentRequestAdded(fpg);
        }
    }
    protected void onPutSuccessful(String id, NodeMessage nm) {
        if( !uploadRequests.containsKey(id) ) {
            System.out.println("No item in upload queue: "+nm);
            return;
        } else {
            FcpPersistentPut pg = uploadRequests.get(id); 
            pg.setSuccess(nm);
            persistenceHandler.persistentRequestUpdated(pg);
        }
    }
    protected void onPutFailed(String id, NodeMessage nm) {
        if( !uploadRequests.containsKey(id) ) {
            System.out.println("No item in upload queue: "+nm);
            return;
        } else {
            FcpPersistentPut pp = uploadRequests.get(id); 
            pp.setFailed(nm);
            persistenceHandler.persistentRequestUpdated(pp);
        }
    }
    protected void onSimpleProgress(String id, NodeMessage nm) {
        if( downloadRequests.containsKey(id) ) {
            FcpPersistentGet pg = downloadRequests.get(id); 
            pg.setProgress(nm);
            persistenceHandler.persistentRequestUpdated(pg);
        } else if( uploadRequests.containsKey(id) ) {
            FcpPersistentPut pg = uploadRequests.get(id); 
            pg.setProgress(nm);
            persistenceHandler.persistentRequestUpdated(pg);
        } else {
            System.out.println("No item in queue: "+nm);
            return;
        }
    }
    protected void onPersistentRequestRemoved(String id, NodeMessage nm) {
        if( downloadRequests.containsKey(id) ) {
            FcpPersistentGet pg = downloadRequests.remove(id); 
            persistenceHandler.persistentRequestRemoved(pg);
        } else if( uploadRequests.containsKey(id) ) {
            FcpPersistentPut pg = uploadRequests.remove(id);
            persistenceHandler.persistentRequestRemoved(pg);
        } else {
            System.out.println("No item in queue: "+nm);
            return;
        }
    }
    protected void onPersistentRequestModified(String id, NodeMessage nm) {
        // check if the priorityClass changed, ignore other changes
        if( nm.isValueSet("PriorityClass") ) {
            int newPriorityClass = nm.getIntValue("PriorityClass");
            if( downloadRequests.containsKey(id) ) {
                FcpPersistentGet pg = downloadRequests.get(id);
                pg.setPriority(newPriorityClass);
                persistenceHandler.persistentRequestModified(pg);
            } else if( uploadRequests.containsKey(id) ) {
                FcpPersistentPut pg = uploadRequests.get(id); 
                pg.setPriority(newPriorityClass);
                persistenceHandler.persistentRequestModified(pg);
            } else {
                System.out.println("No item in queue: "+nm);
                return;
            }
        }
    }
    protected void onProtocolError(String id, NodeMessage nm) {
        if( downloadRequests.containsKey(id) ) {
            FcpPersistentGet pg = downloadRequests.get(id); 
            pg.setFailed(nm);
            persistenceHandler.persistentRequestUpdated(pg);
        } else if( uploadRequests.containsKey(id) ) {
            FcpPersistentPut pg = uploadRequests.get(id); 
            pg.setFailed(nm);
            persistenceHandler.persistentRequestUpdated(pg);
        } else {
            System.out.println("No item in queue, calling error handler: "+nm);
            persistenceHandler.persistentRequestError(id, nm);
        }
    }
    protected void onIdentifierCollision(String id, NodeMessage nm) {
        // since we use the same unique gqid, most likly this request already runs!
        System.out.println("### ATTENTION ###: "+nm);
    }
}
