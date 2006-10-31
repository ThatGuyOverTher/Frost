/**
 *   This file is part of JHyperochaFCPLib.
 *   
 *   Copyright (C) 2006  Hyperocha Project <saces@users.sourceforge.net>
 * 
 * JHyperochaFCPLib is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JHyperochaFCPLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JHyperochaFCPLib; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */
package hyperocha.freenet.fcp.dispatcher;

import hyperocha.freenet.fcp.FCPConnection;
import hyperocha.freenet.fcp.FCPConnectionRunner;
import hyperocha.freenet.fcp.FCPNode;
import hyperocha.freenet.fcp.IIncoming;
import hyperocha.freenet.fcp.Network;
import hyperocha.freenet.fcp.dispatcher.job.Job;
import hyperocha.freenet.fcp.dispatcher.job.UpdateNodePropertiesJob;

import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * @author saces
 *
 */
public class Dispatcher implements IIncoming {

	private Factory factory;
	
	private Thread tickTackTicker = null;
	
	private Hashtable runningJobs = new Hashtable();
	private Hashtable dummyJobs = new Hashtable();
	
	private class DummyJob extends Job {

		private DummyJob(String id) {
			super(-1, id);
		}

		public void incomingData(String id, Hashtable message, FCPConnection conn) {
			// TODO: daten nach /dev/null
			// das sollte eigentlich nicht passieren. die klasse sendet keine reqests. 
			System.out.println("Dummy Daten??? " + id + " -> message: " + message);
			throw new Error();
		}

		/* (non-Javadoc)
		 * @see hyperocha.freenet.fcp.dispatcher.job.Job#incomingMessage(java.lang.String, java.util.Hashtable)
		 */
		public void incomingMessage(String id, Hashtable message) {
			// TODO Auto-generated method stub
			//System.out.println("Dummy message " + id + " -> message: " + message);
		}
		
	}
	
	public Dispatcher(Factory f) {
		this(f, false);
	}

	public Dispatcher(Factory f, boolean boot) {
		factory = f;
	}
	
	public Dispatcher(DataInputStream dis, boolean boot) {
		factory = new Factory(dis);
	}
	
	/**
	 * check the erreichbarkeit der knoten und nimm nur erreichbare
	 */
	public void init() {
		factory.init();
	}

	public boolean isOnline() {
		return factory.isOnline();
	}

	public void startDispatcher() {
		tickTackTicker = new Thread("tick tack ticker") {
	        public void run() {
	            while (true) {
	                try {
	                    Thread.sleep(3000);
	                } catch (InterruptedException e) {
	                }
	                onTimer();
	            }
	        }
		};
		tickTackTicker.start();		
	}

	/**
	 * 
	 */
	public void goOnline(boolean wait) {
		factory.goOnline();
//		if (wait) { factory.goOnline(); }
//		if (onlineWatcher == null) {
//			tickerThread = new Thread("tick tack") {
//		            public void run() {
//		                while (true) {
//		                    Mixed.wait(1000);
//		                    try {
//		                        Thread.sleep(time);
//		                    } catch (InterruptedException e) {
//		                    }
//		                    // refactor this method in Core. lots of work :)
//		                    timer_actionPerformed();
//		                }
//		            }
//		        };
//		        tickerThread.start();
//
//			
//		}
	}

//	/**
//	 * start job queue
//	 */
//	public void startDispatcher() {
//		factory.goOnline();
//	}

	/**
	 * same as panic, but without data loss
	 * state is saved an can be resumed
	 */
	public void freezeDispatcher() {
	}
	
	/**
	 * clean and soft shutdown. complete running jobs,
	 * but dont start new ones
	 */
	public void stopDispatcher() {
		//tickTackTicker.stop();
	}
	
	public boolean loadState() {
		return false;	
	}
	
	public boolean saveState() {
		return false;	
	}

	/**
	 * The panic-button. Neccesary for all freenet apps :)
	 * This function don't return until all is done.
	 * 
	 * <bold>WARNING<bold>
	 * calling this cause data loss.
	 * @param clearqueue
	 */
	public void panic(boolean clearqueue) {
		// TODO:
		// Stop jobmanager
		// Cancel all active connections (Perstance=Connection)
		// ? clear all queues (Perstance=reboot/forever)
		// disconnect all
	}

	/**
	 * is the host:port in our nodelist?
	 * needed for security manager
	 * @param host
	 * @param port
	 * @return
	 */
	public boolean isInList(String host, int port) {
		return factory.isInList(host, port);
	}
	
	/**
	 * add a job to the queue and returns
	 * the balancer start it earlier or later
	 * @param job
	 * @return true on success (added) or false on error
	 */
	public boolean addJob(Job job) {
		// TODO
		return false;
	}

	/**
	 * returns a new fcpconnection
	 * @param networktype
	 * @return
	 */
	public FCPConnection getNewFCPConnection(int networktype) {
		return factory.getNewFCPConnection(networktype);
	}
	
	/**
	 * returns the nodes default fcpconnection
	 * use this and the dispatcher use a single connection per node
	 * (if the node/network supports this)
	 * @param networktype
	 * @return
	 */
	public FCPConnectionRunner getDefaultFCPConnectionRunner(int networktype) {
		//System.err.println("HEHE: getDefaultFCPConnection Start " + networktype);
		//FCPConnection conn = factory.getDefaultFCPConnection(networktype);
		//System.err.println("HEHE: getDefaultFCPConnection Ende " + conn);
		return factory.getDefaultFCPConnectionRunner(networktype);
	}
	
	public FCPConnectionRunner getNewFCPConnectionRunner(int networktype, String id) {
		//System.err.println("HEHE: getDefaultFCPConnection Start " + networktype);
		//FCPConnection conn = factory.getDefaultFCPConnection(networktype);
		//System.err.println("HEHE: getDefaultFCPConnection Ende " + conn);
		return factory.getNewFCPConnectionRunner(networktype, id);
	}
	
	

	/**
	 * run a job and do not return until the job is done.
	 * @param job
	 */
	public void runJob(Job job) {
		//Object lo = new Object();
		if (SwingUtilities.isEventDispatchThread()) {
			throw new Error("Hicks");
		}
		if (job.getStatus() == Job.STATUS_UNPREPARED) {
			//System.out.println(job.getJobID() + " -> job unprepared. prepare it");
			job.prepare(); 
		}
		if (job.getStatus() != Job.STATUS_PREPARED) {
			System.out.println(job.getJobID() + " - " + job + " -> job unprepared. return without execution");
			throw new Error("DEBUG/FIX/TODO");
			//return; 
		}
		
		boolean resume = false;
		if (dummyJobs.containsKey(job.getJobID())) {
			dummyJobs.remove(job.getJobID());
			resume = true;
		}
		registerJob(job);
		job.run(this, resume);
		unregisterJob(job);
	}
	
	private void registerJob(Job job) {
		registerJob(job.getJobID(), job);
	}
	
	public void registerJob(String id, Job job) {
		runningJobs.put(id, job);
	}
	
	private void unregisterJob(Job job) {
		// FIXME: remove all ids assigned to this job
		runningJobs.remove(job.getJobID());		
	}
	
	private DummyJob getDummyJob(String id) {
		DummyJob j = (DummyJob)dummyJobs.get(id);
		if (j == null) {
			j = new DummyJob(id);
			dummyJobs.put(j.getJobID(), j);
		}
		return j;
	}
	
	public void incomingMessage(String id, Hashtable message) {
		//System.out.println("D Testinger id " + id + " -> message: " + message);
		//Job j = getRunningJob((String)message.get("Identifier"));
		Job j = getRunningJob(id);
		if (j == null) { 
			j = getDummyJob(id);
		}
		j.incomingMessage(id, message);
	}
	
	

	public void incomingData(String id, Hashtable message, FCPConnection conn) {
		//System.out.println("D Testinger Data: " + message);
		//Job j = getRunningJob((String)message.get("Identifier"));
		Job j = getRunningJob(id);
		if (j == null) { 
			j = getDummyJob(id);
		}
		//if (j == null) { throw new Error("Hmmmm. this shouldnt happen."); }
		j.incomingData(id, message, conn);
//		getRunningJob((String)message.get("Identifier")).incommingData(conn, message);
	}
	
	private void onTimer() {
		//System.out.println("Tick Tack Timer");
	}
	
	private Job getRunningJob(String id) {
		//System.out.println("getRunningJob: " + id);
		return (Job)runningJobs.get(id);
	}

	/**
	 * @param wait true: dont return until all tests are done
	 */
	public void testPropertiesAllNodes(boolean wait) {
		//get all nodes ids für Network.FCP2
		List tnl = factory.getAllNodes(Network.FCP2);
		if (tnl == null) {
			//no fcp2 nodes, return
			return;
		}
		
		int i;
		for (i = 0; i < tnl.size(); i++) {
			UpdateNodePropertiesJob job = new UpdateNodePropertiesJob((FCPNode)tnl.get(i));
			runJob(job);
        }
		
	}
}