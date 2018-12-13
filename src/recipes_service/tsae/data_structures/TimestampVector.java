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
import java.util.concurrent.ConcurrentHashMap;

//LSim logging system imports sgeag@2017
import lsim.worker.LSimWorker;
import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class TimestampVector implements Serializable{
	// Needed for the logging system sgeag@2017
	//private transient LSimWorker lsim = LSimFactory.getWorkerInstance();

	private static final long serialVersionUID = -765026247959198886L;
	/**
	 * This class stores a summary of the timestamps seen by a node.
	 * For each node, stores the timestamp of the last received operation.
	 */
	
	private ConcurrentHashMap<String, Timestamp> timestampVector= new ConcurrentHashMap<String, Timestamp>();
	
	public TimestampVector (List<String> participants){
		// create and empty TimestampVector
		for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
			String id = it.next();
			// when sequence number of timestamp < 0 it means that the timestamp is the null timestamp
			timestampVector.put(id, new Timestamp(id, Timestamp.NULL_TIMESTAMP_SEQ_NUMBER));
		}
	}

	/**
	 * Updates the timestamp vector with a new timestamp. 
	 * @param timestamp
	 */
	public synchronized void updateTimestamp(Timestamp timestamp){
		//lsim.log(Level.TRACE, "Updating the TimestampVectorInserting with the timestamp: "+timestamp);
		
		//updating client info
		if (timestamp != null) {
            this.timestampVector.replace(timestamp.getHostid(), timestamp);
        }
		
	
	}
	
	/**
	 * merge in another vector, taking the elementwise maximum
	 * @param tsVector (a timestamp vector)
	 */
	public synchronized void updateMax(TimestampVector tsVector){
		//METODO COMPLETAMENTE NUEVO Y A MODIFICAR
		for (String key : this.timestampVector.keySet()) {
            Timestamp ts = tsVector.getLast(key);
            Timestamp ts_propio = this.getLast(key);
            if(ts_propio.compare(ts) < 0) {            
                this.timestampVector.replace(key, ts);            
            }
        }
	}
	
	/**
	 * 
	 * @param node
	 * @return the last timestamp issued by node that has been
	 * received.
	 */
	public synchronized Timestamp getLast(String node){
		//METODO COMPLETAMENTE NUEVO Y A MODIFICAR
		return this.timestampVector.get(node);
		// return generated automatically. Remove it when implementing your solution 
	
	}
	
	/**
	 * merges local timestamp vector with tsVector timestamp vector taking
	 * the smallest timestamp for each node.
	 * After merging, local node will have the smallest timestamp for each node.
	 *  @param tsVector (timestamp vector)
	 */
	public synchronized void mergeMin(TimestampVector tsVector){
		//METODO COMPLETAMENTE NUEVO Y A MODIFICAR
		for (String key : tsVector.timestampVector.keySet()) {
            Timestamp ts = tsVector.getLast(key);
            Timestamp ts_propio = this.getLast(key);
            if(ts_propio.compare(ts) > 0) {            
                this.timestampVector.put(ts_propio.getHostid(), ts);            
            }
        }
	}
	
	/**
	 * clone
	 */
	public synchronized TimestampVector clone(){
		//METODO COMPLETAMENTE NUEVO Y A MODIFICAR
		List<String> participants = new ArrayList<String>(this.timestampVector.keySet());
        TimestampVector clone_temp = new TimestampVector(participants);

        for (String key : this.timestampVector.keySet()) {
            Timestamp ts = this.timestampVector.get(key);
            clone_temp.timestampVector.put(ts.getHostid(), ts);
        }
        return clone_temp;
		// return generated automatically. Remove it when implementing your solution 
		
	}
	
	/**
	 * equals
	 */
	public synchronized boolean equals(Object obj){
	
       System.out.println("Entra equal TIMESTAMPVECTOR: **** "+obj.toString());
       try {
			this.wait(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//If is a null object or the class don't match between objects
				if (obj == null || this.getClass() != obj.getClass() ){
					return false;
				}
				
				//If the object are the same
				if (this == obj){
					return true;
				}
				//Compare de HashMap list
				TimestampVector tempVector = (TimestampVector) obj;
				
				/*System.out.println("SALE equal TIMESTAMPVECTOR: **** "+obj.toString());
				System.out.println("COMPARA LOS DOS RESULTADOS A VER SI ES CIERTO: ");
				System.out.println("ESTE ES EL PRIMERO"+ tempVector);
				System.out.println("ESTE ES timestamp 1:"+ tempVector.timestampVector);
				System.out.println("ESTE ES EL SEGUNDO"+ this);
				System.out.println("ESTE ES timestamp 2:"+ this.timestampVector);*/
				if(this.timestampVector.equals(tempVector.timestampVector)) {
					//System.out.println(" -- ES TRUE !! --");
				}else {
					System.out.println("ENTRAAA");
				}
				
				
				//If there's and error/null of the hashmap table
				if(this.timestampVector == null || tempVector.timestampVector == null){
					System.out.println("ENTRAAA222");
					return false;
				}
				
				//Return comparaison in boolean result 
				return this.timestampVector.equals(tempVector.timestampVector);
		
		
	}

	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String all="";
		if(timestampVector==null){
			return all;
		}
		for(Enumeration<String> en=timestampVector.keys(); en.hasMoreElements();){
			String name=en.nextElement();
			if(timestampVector.get(name)!=null)
				all+=timestampVector.get(name)+"\n";
		}
		return all;
	}
	
	public ConcurrentHashMap<String, Timestamp> getTimestampVector() {
        return timestampVector;
    }

}