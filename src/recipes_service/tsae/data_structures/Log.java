/*
* Copyright (c) Joan-Manuel Marques 2013. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This file is part of the practical assignment of Distributed Systems course.
*
* This code is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
*/

package recipes_service.tsae.data_structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

//LSim logging system imports sgeag@2017
import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
import lsim.worker.LSimWorker;
//import recipesService.tsaeDataStructures.Log;
import recipes_service.tsae.data_structures.Timestamp;
import recipes_service.data.Operation;

/**
 * @author Joan-Manuel Marques, Daniel Lázaro Iglesias
 * December 2012
 *
 */
public class Log implements Serializable{
	// Needed for the logging system sgeag@2017
	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();

	private static final long serialVersionUID = -4864990265268259700L;
	/**
	 * This class implements a log, that stores the operations
	 * received  by a client.
	 * They are stored in a ConcurrentHashMap (a hash table),
	 * that stores a list of operations for each member of 
	 * the group.
	 */
	private ConcurrentHashMap<String, List<Operation>> log= new ConcurrentHashMap<String, List<Operation>>();  

	public Log(List<String> participants){
		// create an empty log
		for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
			log.put(it.next(), new Vector<Operation>());
		}
	}

	/**
	 * inserts an operation into the log. Operations are 
	 * inserted in order. If the last operation for 
	 * the user is not the previous operation than the one 
	 * being inserted, the insertion will fail.
	 * 
	 * @param op
	 * @return true if op is inserted, false otherwise.
	 */
	public synchronized boolean add(Operation op){
		lsim.log(Level.TRACE, "Inserting into Log the operation: "+op);
		boolean operation = false;
		String idHost = op.getTimestamp().getHostid();
		List<Operation> listOp = log.get(idHost);
		Operation lastOp = null;
		if( !listOp.isEmpty() )
		lastOp = listOp.get(listOp.size()-1);
		if( (lastOp == null || lastOp.getTimestamp() == null ) ||
		(op.getTimestamp().compare(lastOp.getTimestamp()) == 1)) {
		log.get(idHost).add(op);
		operation = true;
		}
		return operation;
	}
	
	/**
	 * Checks the received summary (sum) and determines the operations
	 * contained in the log that have not been seen by
	 * the proprietary of the summary.
	 * Returns them in an ordered list.
	 * @param sum
	 * @return list of operations
	 */
	public synchronized List<Operation> listNewer(TimestampVector sum){
		List<Operation> missingList = new Vector();

        /**
         * Go through all the hosts in the log
         */

        for (String node : this.log.keySet()) {
            List<Operation> operations = this.log.get(node);
            Timestamp timestampToCompare = sum.getLast(node);

        	/**
        	 * Go through all the operations per host and collect all those which are smaller
        	 * than the timestampVector passed formthe specific host.
        	 */
            for (Operation op : operations) {
                if (op.getTimestamp().compare(timestampToCompare) > 0) {
                    missingList.add(op);
                }
            }
        }
        return missingList;
		
	}
	
	/**
	 * Removes from the log the operations that have
	 * been acknowledged by all the members
	 * of the group, according to the provided
	 * ackSummary. 
	 * @param ack: ackSummary.
	 */
	public synchronized void purgeLog(TimestampMatrix ack){
		TimestampVector minAck = ack.minTimestampVector();
		  
	    //get all the keys = hosts from hashmap
		Set<String> hosts = log.keySet();
		
		for (String host : hosts) {
			//get the list of ops from a host 
			List<Operation> logOp = log.get(host);
			//get the last timestamp of the timestampVector of each host
			Timestamp lastTimestamp = minAck.getLast(host);
	        
			// If the lastTimestamp is not null, proceed to iterate over its messages
			// and compare each message's timestamp with the lastTimestamp. 
			// If the message's timestamp it's lower than the lasTimestamp then 
			// the operation it is removed from the log. 
			if (lastTimestamp != null){
				for (int i = 0; i < logOp.size(); i++) {
					Operation op = logOp.get(i);
					if (!(op.getTimestamp().compare(lastTimestamp) > 0)){
						logOp.remove(i);
					}
					
				}
			}		
		}

	}

	/**
	 * equals
	 */
	@Override
	public synchronized boolean equals(Object obj) {
		boolean isEquals = false;
		Log other = (Log) obj;
		if (this == obj) {
		isEquals = true;
		}
		if (this.log == other.log) {
		isEquals = true;
		} else {
		isEquals = this.log.equals(other.log);
		}
		return isEquals;
	}

	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String name="";
		for(Enumeration<List<Operation>> en=log.elements();
		en.hasMoreElements(); ){
		List<Operation> sublog=en.nextElement();
		for(ListIterator<Operation> en2=sublog.listIterator(); en2.hasNext();){
			name+=en2.next().toString()+"\n";
		}
	}
		
		return name;
	}
}