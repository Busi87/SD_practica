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
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

//LSim logging system imports sgeag@2017
import edu.uoc.dpcs.lsim.LSimFactory;
import lsim.worker.LSimWorker;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;

/**
 * @author Joan-Manuel Marques, Daniel Lázaro Iglesias
 * December 2012
 *
 */
public class TimestampMatrix implements Serializable{
	// Needed for the logging system sgeag@2017
	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();
	
	private static final long serialVersionUID = 3331148113387926667L;
	ConcurrentHashMap<String, TimestampVector> timestampMatrix = new ConcurrentHashMap<String, TimestampVector>();
	
	public TimestampMatrix(List<String> participants){
		// create and empty TimestampMatrix
		for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
			timestampMatrix.put(it.next(), new TimestampVector(participants));
		}
	}
	
	public TimestampMatrix() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Not private for testing purposes.
	 * @param node
	 * @return the timestamp vector of node in this timestamp matrix
	 */
	TimestampVector getTimestampVector(String node){
		return this.timestampMatrix.get(node);
		// return generated automatically. Remove it when implementing your solution 
		//return null;
	}
	
	/**
	 * Merges two timestamp matrix taking the elementwise maximum
	 * @param tsMatrix
	 */
	public synchronized void updateMax(TimestampMatrix tsMatrix){
		 for (Map.Entry<String, TimestampVector> entry : tsMatrix.timestampMatrix.entrySet()) {
	            String key = entry.getKey();
	            TimestampVector otherValue = entry.getValue();

	            TimestampVector thisValue = this.timestampMatrix.get(key);
	            if (thisValue != null) {
	                thisValue.updateMax(otherValue);
	            }
	        }

	}
	
	/**
	 * substitutes current timestamp vector of node for tsVector
	 * @param node
	 * @param tsVector
	 */
	public synchronized void update(String node, TimestampVector tsVector){
		if(timestampMatrix.get(node) != null) {
			this.timestampMatrix.replace(node, tsVector);
		}else {
			timestampMatrix.put(node, tsVector);
		}
	}
	
	/**
	 * 
	 * @return a timestamp vector containing, for each node, 
	 * the timestamp known by all participants
	 */
	public synchronized TimestampVector minTimestampVector(){
		TimestampVector minVector = null;
        Set<String> hosts = timestampMatrix.keySet();

        //iterate between the vectors on this timestampMatrix
        for (String host : hosts) {
            TimestampVector tsv = timestampMatrix.get(host);
            if (minVector != null){
                //merge current vector (tsv) with minVector to 
                //obtain the minium timestamp of both of them
                minVector.mergeMin(tsv);
            }else{
                //Initialization of the minVector with a clone of
                //the current vector(tsv)
                minVector = tsv.clone();
            }
        }
        return minVector;

	}
	
	/**
	 * clone
	 */
	public synchronized TimestampMatrix clone(){
		///TODO and check
        TimestampMatrix clonedMatrix = new TimestampMatrix();
        for (Map.Entry<String, TimestampVector> entry : timestampMatrix.entrySet()) {
            clonedMatrix.timestampMatrix.put(entry.getKey(), entry.getValue().clone());
        }

        return clonedMatrix;

		
	}
	
	/**
	 * equals
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) 
            return false;
        if (this == obj) 
            return true;
        if ((getClass() != obj.getClass())) 
            return false;
  
        TimestampMatrix other = (TimestampMatrix) obj;

        if (this.timestampMatrix == other.timestampMatrix) 
            return true;
        if (this.timestampMatrix == null) 
            return false;
        if (other.timestampMatrix == null) 
            return false;
        else 
            return this.timestampMatrix.equals(other.timestampMatrix);

	}

	
	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String all="";
		if(timestampMatrix==null){
			return all;
		}
		for(Enumeration<String> en=timestampMatrix.keys(); en.hasMoreElements();){
			String name=en.nextElement();
			if(timestampMatrix.get(name)!=null)
				all+=name+":   "+timestampMatrix.get(name)+"\n";
		}
		return all;
	}
	
	 /**
     * @param node
     * @return the timestamp vector of node in this timestamp matrix
     */
    /*private TimestampVector getTimestampVector(String node) {
        return this.timestampMatrix.get(node);
    }*/

}