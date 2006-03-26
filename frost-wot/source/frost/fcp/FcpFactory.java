/*
  FcpFactory.java / Frost
  Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.fcp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import frost.*;

public class FcpFactory {
    
    private static Logger logger = Logger.getLogger(FcpFactory.class.getName());

    private static List nodes = new ArrayList(); //list of available nodes, NodeAddress objects
    private static Random random = new Random();

    public static class NodeAddress {
        public InetAddress host = null;
        public int port = -1;
    }

    /**
     * This method creates an instance of FcpConnection and handles errors.
     * Returns either the connection, or null on any error.
     */
    public static FcpConnection getFcpConnectionInstance() {
        
        FcpConnection connection = null;

        int tries = 0;
        // if we have more than one node, try each one at least once
        int maxTries = getNodes().size() > 1 ? getNodes().size() : 3;
        while (connection == null && tries < maxTries) {
            try {
                connection = getConnection();
            } catch (ConnectionException e) {
                logger.warning(
                    "FcpConnection.getFcpConnectionInstance: FcpTools.ConnectionException "
                        + e + " , this was try " + (tries + 1) + "/" + maxTries);
            } catch (FcpToolsException e) {
                logger.severe("FcpConnection.getFcpConnectionInstance: FcpToolsException " + e);
                break;
            } catch (UnknownHostException e) {
                logger.severe("FcpConnection.getFcpConnectionInstance: UnknownHostException " + e);
                break;
            } catch (java.net.ConnectException e) {
                /*  IOException java.net.ConnectException: Connection refused: connect  */
                logger.warning(
                    "FcpConnection.getFcpConnectionInstance: java.net.ConnectException "
                        + e + " , this was try " + (tries + 1) + "/" + maxTries);
            } catch (IOException e) {
                logger.warning(
                    "FcpConnection.getFcpConnectionInstance: IOException "
                        + e + " , this was try " + (tries + 1) + "/" + maxTries);
            } catch (Throwable e) {
                logger.severe("FcpConnection.getFcpConnectionInstance: Throwable " + e);
                break;
            }
            tries++;
            Mixed.wait(tries * 1250);
        }
        if (connection == null) {
            logger.warning("ERROR: FcpConnection.getFcpConnectionInstance: Could not connect to node!");
        }
        return connection;
    }

    /**
     * @return  Returns a list of available NodeAddress objects.
     */
    public static List getNodes() {
        return nodes;
    }

    /**
     * Returns a randomly selected node from the list.
     * @return NodeAddress of selected node.
     */
    protected static NodeAddress selectNode() {
        int size = nodes.size();
        if(size == 0) {
            throw new Error("All connections to nodes failed. Check your network settings and restart Frost.");
        } else if( size == 1 ) {
            return (NodeAddress)nodes.get(0);
        } else {
            return (NodeAddress)nodes.get(random.nextInt(nodes.size()));
        }
    }

    /**
     * @param s the node to be removed
     */
    protected static void delegateRemove(NodeAddress s) {
        nodes.remove(s);
    }

    /**
     * Process provided List of string (host:port or host) and create InetAddress objects for each.
     */
    public static void init(List nodeList) {
        for(Iterator i=nodeList.iterator(); i.hasNext(); ) {
            String nodeName = (String)i.next();
            NodeAddress na = new NodeAddress();
            if( nodeName.indexOf(":") < 0 ) {
                InetAddress ia = null;
                try {
                    ia = InetAddress.getByName(nodeName);
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Unknown FCP host: "+nodeName, t);
                    continue;
                }
                na.host = ia;
                na.port = 8481; // default
            } else {
                String[] splitNodeName = nodeName.split(":");
                InetAddress ia = null;
                try {
                    ia = InetAddress.getByName(splitNodeName[0]);
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Unknown FCP host: "+nodeName, t);
                    continue;
                }
                int port = -1;
                try {
                    port = Integer.parseInt(splitNodeName[1]);
                } catch(Throwable t) {
                    logger.log(Level.SEVERE, "Unknown FCP port: "+nodeName, t);
                    continue;
                }
                na.host = ia;
                na.port = port;
            }
            nodes.add(na);
        }
        logger.info("Frost will use " + nodes.size() + " Freenet nodes");
    }

    protected static synchronized FcpConnection getConnection()  throws IOException, FcpToolsException, Error {

        FcpConnection con = null;
        if (getNodes().size()==0) {
            throw new Error("No Freenet nodes available.  You need at least one.");
        }
        
        NodeAddress selectedNode = selectNode();

        logger.info("Using node "+selectedNode.host.getHostAddress()+" port "+selectedNode.port);
        try {
            con = new FcpConnection(selectedNode.host, selectedNode.port);
        } catch (IOException e) {
            // for now, remove on the first failure.
            // FIXME: maybe we should give the node few chances?
            // also, should we remove it from the settings (i.e. forever)?
            delegateRemove(selectedNode);
            throw e;
        } catch (FcpToolsException e) {
            // same here
            delegateRemove(selectedNode);
            throw e;
        }
        return con;
    }
}
