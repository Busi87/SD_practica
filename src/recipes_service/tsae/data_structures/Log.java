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
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

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
	
	//private transient LSimWorker lsim = LSimFactory.getWorkerInstance();

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
		//lsim.log(Level.TRACE, "Inserting into Log the operation: "+op);
		
		//PURGELOG WORKS
		/*
		String hostId = op.getTimestamp().getHostid();
        Timestamp lastTimestamp = this.getLastTimestamp(hostId);
        long timestampDifference = op.getTimestamp().compare(lastTimestamp);

      
        
        if ((lastTimestamp == null && timestampDifference == 0)
                || (lastTimestamp != null && timestampDifference == 1)) {
        	 System.out.println("Entra en true 1");
            this.log.get(hostId).add(op);
            return true;
        } else  {
            return false;
        }*/

		
		
	    //Codigo mezclado -------------------------------
		/*
		String hostId = op.getTimestamp().getHostid();
        Timestamp lastTimestamp = this.getLastTimestamp(hostId);
        long timestampDifference = op.getTimestamp().compare(lastTimestamp);

        
        List<Operation> listOp = log.get(hostId);
        
        Operation lastOp = null;
        
        if( !listOp.isEmpty() ) {
            lastOp = listOp.get(listOp.size()-1);
        }
        
   
        
        if ((lastTimestamp == null && timestampDifference == 0)   || (lastTimestamp != null && timestampDifference == 1)) {
            
            
            if( lastOp == null || lastOp.getTimestamp() == null ) {
                log.get(hostId).add(op);
                 System.out.println("Entra en true 1");
                 return true;
            } 
            //this.log.get(hostId).add(op);
            System.out.println("Entra en true 2");
            return true;
        } else  {
        	
        	System.out.println("Entra en false");
            return false;
        }
		
		*/
		
		
		//-------------------------------------
		
		// INICIAL      <------
		
        String user = op.getTimestamp().getHostid();
        
        //>... codigo nuevo
        Timestamp lastTimestamp = this.getLastTimestamp(user);
        long timestampDifference = op.getTimestamp().compare(lastTimestamp);
        //<...
        
  
        List<Operation> listOp = log.get(user);
        
        Operation lastOp = null;
        
        if( !listOp.isEmpty() )
            lastOp = listOp.get(listOp.size()-1);
        
        if( lastOp == null || lastOp.getTimestamp() == null ) {
            log.get(user).add(op);
            return true;
        } else if( op.getTimestamp().compare(lastOp.getTimestamp()) == 1 ) {
            log.get(user).add(op);
            return true;
            //lineas debajo son nuevas, no son del original
        }else if((lastTimestamp == null && timestampDifference == 0)
                || (lastTimestamp != null && timestampDifference == 1)) {
        	this.log.get(user).add(op);
            return true;
        }
        //<...
        return false;
       
        	
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
        for (String node : this.log.keySet()) {
            List<Operation> operations = this.log.get(node);
            Timestamp timestampToCompare = sum.getLast(node);


            for (Operation op : operations) {
                if (op.getTimestamp().compare(timestampToCompare) > 0) {
                    missingList.add(op);
                }
            }
        }
        return missingList;
   
			
		
		
		//METODO COMPLETAMENTE NUEVO Y A MODIFICAR
		
		
		/*
        List<Operation> ops = new ArrayList<Operation>();
        
        //get all the keys = hosts from hashmap
        Set<String> hosts = log.keySet();
        
        for (String host : hosts) {
            //t = last timestamp for index host in sumVector
            Timestamp tRef = sum.getTimestampVector().get(host); 
            //get the list of ops from a host 
            List<Operation> logOp = log.get(host);
            
            //iterate over list of operations from host(i) and compare if timestamp is new than tRef.-> add to list
            for (int i = 0; i < logOp.size(); i++) {
                if (logOp.get(i).getTimestamp().compare(tRef) > 0){
                    ops.add(logOp.get(i));
                }
            }  
            
            //Sort of the ops list 
            Collections.sort(ops, new Comparator<Operation>() {
                @Override
                public int compare(Operation o1, Operation o2) {
                    return (int) o1.getTimestamp().compare(o2.getTimestamp());
                }
            });
        }
        return ops;*/
		
	}
	
	/**
	 * Removes from the log the operations that have
	 * been acknowledged by all the members
	 * of the group, according to the provided
	 * ackSummary. 
	 * @param ack: ackSummary.
	 */
	public synchronized void purgeLog(TimestampMatrix ack){
		
		/*TimestampVector minTSV=ack.minTimestampVector();
        for(Map.Entry<String, List<Operation>> entry : log.entrySet()){
            String implicated=entry.getKey();
            List <Operation> ops=entry.getValue();
            Timestamp lastTS=minTSV.getLast(implicated);
            for(int i=ops.size()-1;i>=0;i--){
                Operation op=ops.get(i);
                if (op.getTimestamp().compare(lastTS)<0){
                    ops.remove(i);
                }
            }
            
        }*/
	
  
		//METODO COMPLETAMENTE NUEVO Y A MODIFICAR
		 /**
         * Create a minTimestampVector from the matrix. Only logs older than this will be purged later.
         * minTimestampVector guarantees that all clients have received the information up to that timestamp.
         */
		/*
        TimestampVector minTimestampVector = ack.minTimestampVector();
        

        for (Map.Entry<String, List<Operation>> entry : log.entrySet()) {
            String participant = entry.getKey();
            List<Operation> operations = entry.getValue();
            Timestamp lastTimestamp = minTimestampVector.getLast(participant);
 
            if (lastTimestamp == null) {
                continue;
            }
            for (int i = operations.size() - 1; i >= 0; i--) {
                Operation op = operations.get(i);

                if (op.getTimestamp().compare(lastTimestamp) < 0) {
                    operations.remove(i);
                }
            }
        }*/
		 
		// ------------------------------
		
		
		
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
		
		if(this==obj)
			return true;
		if(obj==null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Log other = (Log) obj;
		if(this.log == other.log)
			return true;
		if(this.log == null || other.log==null)
			return false;
		
		return this.log.equals(other.log);
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
	
	 private Timestamp getLastTimestamp(String hostId) {
	        List<Operation> operations = this.log.get(hostId);

	        if (operations == null || operations.isEmpty()) {
	            return null;
	        } else {
	            return operations.get(operations.size() - 1).getTimestamp();
	        }
	    }

}