
package com.projects.group23.Epidemic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.projects.group23.Epidemic.EpidemicActor.AssignMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;





public class EpidemicActorPush extends EpidemicActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props() {
        return Props.create(EpidemicActorPush.class);
    }
    
    
	/*
	 * Handle 3 types of message:
	 * StartMessage: set the peer list internally
	 * BroadcastMessage: broadcast the incoming text
	 * so when a process receive a BroadcastMessage, 
	 * it takes out the text and broadcast RBMessage (or another subclass) to all others
	 * some other actions may be implemented here 
	 * (see the R-broadcast, F-broadcast, C-broadcast, etc.)
	 * RBMessage: receive and handle the message from a broadcaster
	 * and eventually deliver it, the receive function is called here
	 */
	public void onReceive(final Object message) throws Exception {
		if (message instanceof StartMessage) {
			
			/*
			 * Set the peer list
			 */
        	StartMessage sm = (StartMessage) message;
        	processes = sm.group;
        	setEpidemicTimeout();
        	runSchedule();
        	
        	/*
        	 * Some initializations for different broadcast algorithms
        	 */
        	
		}else if(message instanceof AssignMessage){
			AssignMessage am = (AssignMessage) message;
			v.setValue(am.getValue());
			v.setTs(System.currentTimeMillis());
		
		}else if(message instanceof EpidemicMessage){
			onEpidemicReceive((EpidemicMessage)message);
		}
			else {
		}
            unhandled(message);
        }
	
	private Random rand = new Random(System.currentTimeMillis());

	protected ActorRef randomProcess() {
		int index = rand.nextInt(processes.size());
		while(processes.indexOf(getSelf())==index) {
			index = rand.nextInt(processes.size());
		}
		return processes.get(index);
	}
	protected void  onEpidemicTimeout() {
		ActorRef dest = randomProcess();
		EpidemicMessage message = new EpidemicMessage();
		message.setValue(v);
		dest.tell(message, getSelf());
		
	}

	protected void onEpidemicReceive(EpidemicMessage message) {
		EpidemicValue v;
		v = message.getValue();
		String msg = v.ts + " " + v.value;
		log.info(msg);
		if (v.getTs() > this.v.getTs()){
			this.v.copy(v);
		}
		
	}
    
}