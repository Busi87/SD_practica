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

package recipes_service.tsae.sessions;


import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import communication.ObjectInputStream_DS;
import communication.ObjectOutputStream_DS;
import recipes_service.ServerData;
import recipes_service.communication.Message;
import recipes_service.communication.MessageAErequest;
import recipes_service.communication.MessageEndTSAE;
import recipes_service.communication.MessageOperation;
import recipes_service.communication.MsgType;
import recipes_service.data.Operation;
import recipes_service.tsae.data_structures.TimestampMatrix;
import recipes_service.tsae.data_structures.TimestampVector;

//LSim logging system imports sgeag@2017
import lsim.worker.LSimWorker;
import edu.uoc.dpcs.lsim.LSimFactory;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;

/**
 * @author Joan-Manuel Marques
 * December 2012
 *
 */
public class TSAESessionPartnerSide extends Thread{
	// Needed for the logging system sgeag@2017
	private LSimWorker lsim = LSimFactory.getWorkerInstance();
	
	private Socket socket = null;
	private ServerData serverData = null;
	
	public TSAESessionPartnerSide(Socket socket, ServerData serverData) {
		super("TSAEPartnerSideThread");
		this.socket = socket;
		this.serverData = serverData;
	}

	public void run() {

		Message msg = null;

		int current_session_number = -1;
		try {
			ObjectOutputStream_DS out = new ObjectOutputStream_DS(socket.getOutputStream());
			ObjectInputStream_DS in = new ObjectInputStream_DS(socket.getInputStream());

			// receive originator's summary and ack
			msg = (Message) in.readObject();
			current_session_number = msg.getSessionNumber();
			lsim.log(Level.TRACE, "[TSAESessionPartnerSide] [session: "+current_session_number+"] TSAE session");
			lsim.log(Level.TRACE, "[TSAESessionPartnerSide] [session: "+current_session_number+"] received message: "+ msg);
			if (msg.type() == MsgType.AE_REQUEST){
				 MessageAErequest msgAe = (MessageAErequest) msg;

              TimestampMatrix localAck;
              TimestampVector localSummary;
              
           /** Initiate of the structures localAck and localSummary with the data of the server (serverData).
  			 * It must be synchronized because the data is been accessed concurrently and we want to avoid interferences 
  			 * between threads (for example, delete data or change it). 
  			 */
              synchronized (serverData) {
              	// Initiate localSummary with a copy of the summary in the serverData server.  
                  localSummary = serverData.getSummary().clone();
                  serverData.getAck().update(serverData.getId(), localSummary);
                  // Initiate localAck with a copy of the acknowledge vector in the serverData server. 
                  localAck = serverData.getAck().clone();
              }
              
			  // send operations
              List<Operation> newLogs = serverData.getLog().listNewer(msgAe.getSummary());
              for (Operation op : newLogs) {
	              out.writeObject(new MessageOperation(op));
	                }

			// send to originator: local's summary and ack
				msg = new MessageAErequest(localSummary, localAck);
	            out.writeObject(msg);

			// receive operations
				List <MessageOperation> listOperations = new ArrayList();
	            msg = (Message) in.readObject();
	            
	            while (msg.type() == MsgType.OPERATION){
	            	listOperations.add((MessageOperation) msg); 
	            	msg = (Message) in.readObject();
	            }
				
			// receive message to inform about the ending of the TSAE session
	            if (msg.type() == MsgType.END_TSAE){
	            	// send and "end of TSAE session" 
	            	msg = new MessageEndTSAE();
	            	out.writeObject(msg);
	            }
					
					//Update summary
					serverData.getSummary().updateMax(msgAe.getSummary());
                   serverData.getAck().updateMax(msgAe.getAck());
                   serverData.getLog().purgeLog(serverData.getAck());
           }
			
			socket.close();		
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			lsim.log(Level.FATAL, "[TSAESessionPartnerSide] [session: "+current_session_number+"]" + e.getMessage());
			e.printStackTrace();
            System.exit(1);
		}catch (IOException e) {
	    }
		
		lsim.log(Level.TRACE, "[TSAESessionPartnerSide] [session: "+current_session_number+"] End TSAE session");
	}
}