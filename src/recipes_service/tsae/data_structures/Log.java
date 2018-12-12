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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
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
		
		//Obtain the client access
		String client_access = op.getTimestamp().getHostid();
		//And extract the last operation from client
		List<Operation> allClientOperations = this.log.get(client_access);
		
		//System.out.println(allClientOperations.size());
		Timestamp lastClientTimestamp=null;
		//I get the last timestamp of the last operation of client
		if( allClientOperations.isEmpty()== false || allClientOperations!= null){
			if(allClientOperations.size()>0) {
				lastClientTimestamp = allClientOperations.get(allClientOperations.size()-1).getTimestamp();
			}else {
				this.log.get(client_access).add(op);
				return true;
			}
		}
		
		
		//Now  I do de comparation and the add action and return true o false
		//First controll of theres no insert
		/*if(allClientOperations.size()==0){
			this.log.get(client_access).add(op);
			return true;
		}*/
		
		if(lastClientTimestamp == null && (op.getTimestamp().compare(lastClientTimestamp)) == 0 ){
			this.log.get(client_access).add(op);
			return true;
		}
		
		if(lastClientTimestamp != null && (op.getTimestamp().compare(lastClientTimestamp)) == 1 ){
			this.log.get(client_access).add(op);
			return true;
		}

		// return generated automatically. Remove it when implementing your solution 
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
		//METODO COMPLETAMENTE NUEVO Y A MODIFICAR
		List<Operation> tempList = new ArrayList<>();

		for (String node : this.log.keySet()) {
            List<Operation> operations = this.log.get(node);
            Timestamp timestampListComparation = sum.getLast(node);
            
            for (Operation op : operations) {
                if (op.getTimestamp().compare(timestampListComparation) > 0) {
                	tempList.add(op);
                }
            }
		}
		 
		return tempList;
		
	}
	
	/**
	 * Removes from the log the operations that have
	 * been acknowledged by all the members
	 * of the group, according to the provided
	 * ackSummary. 
	 * @param ack: ackSummary.
	 */
	public synchronized void purgeLog(TimestampMatrix ack){
		//METODO COMPLETAMENTE NUEVO Y A MODIFICAR
		TimestampVector minTSV=ack.minTimestampVector();
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
            
        }
	}

	/**
	 * equals
	 */
	@Override
	public synchronized boolean equals(Object obj) {
		
		if (this == obj)
            return true;
        
        if (obj == null)
            return false;
        
        if (getClass() != obj.getClass())
            return false;
        
        Log other = (Log) obj;
        
        if (log == null) {
            return other.log == null;
        } else {
            if (log.size() != other.log.size()){
                return false;
            }
            boolean equal = true;
            for (Iterator<String> it = log.keySet().iterator(); it.hasNext() && equal; ){
                String host_name = it.next();
                equal = log.get(host_name).equals(other.log.get(host_name));
                if (!equal){
                }
            }
            return equal;
        }
		
		/*
		//If is a null object or the class don't match between objects
		if (obj == null || this.getClass() != obj.getClass() ){
			return false;
		}
		
		//If the object are the same
		if (this == obj){
			return true;
		}
		//Compare de HashMap list
		Log tempObj = (Log) obj;
		//If there's and error/null of the hashmap table
		if(this.log == null || tempObj.log == null){
			return false;
		}
		
		//Return comparaison in boolean result 
		return this.log.equals(tempObj.log);*/
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