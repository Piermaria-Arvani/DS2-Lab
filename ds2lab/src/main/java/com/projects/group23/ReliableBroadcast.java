
package com.projects.group23;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ReliableBroadcast extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props() {
        return Props.create(ReliableBroadcast.class);
    }
    
    /*
     * The StartMessage from the main function will tell a process about the peers
     */
    public static class StartMessage {
    	private final List<ActorRef> group;
		
        public StartMessage(List<ActorRef> group) {
            this.group = Collections.unmodifiableList(group);
        }
    }
    
    /*
     * The BroadcastMessage from the main function will tell a process to broadcast to all peers
     */
    public static class BroadcastMessage {
    	private final String text;
    	
    	public BroadcastMessage(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
    
    /*
     * The RBMessage is the message used in ReliableBroadcast 
     * so when a process receive this message, it should relay the message to other peers
     */
    public static class RBMessage {
        protected final String text;

        public RBMessage(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			return result;
		}
		
		/*
		 * The equals method define how we want to identify message as identical, 
		 * in this case if the text is the same then we consider the message the same
		 * We use this to check if a message is already in the delivered list
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RBMessage other = (RBMessage) obj;
			if (text == null) {
				if (other.text != null)
					return false;
			} else if (!text.equals(other.text))
				return false;
			return true;
		}
    }
    
    // The list of peers is kept here
    private List<ActorRef> processes = new ArrayList<ActorRef>();
    
    /*
     * Here is a nice debug function to print and see the broadcaster
     */
    public void Rbroadcast(Object message) {
    	BroadcastMessage m = (BroadcastMessage) message;
        log.info("broadcast message \"{}\"", m.getText());
	}
    
    /*
     * The implementation of ReliableBroadcast requires a list of delivered message,
     * so here it is
     */
    private List<RBMessage> delivered = new ArrayList<RBMessage>();
    
    /*
     * The R-deliver of ReliableBroadcast is implemented here
     * Basically it just print a log to notify it delivers the message
     * But if we go on and implement FIFO Order, it will call the FIFOOrder function and pass along the message
     * As we try we will see that R-deliver will be shown at random order 
     * but F-deliver will repsect the FIFO order
     */
	public void Rdeliver(Object message,  ActorRef me, ActorRef sender) throws Exception {
		RBMessage m = (RBMessage) message;
		log.info("R delivered message \"{}\" from {}", m.getText(), sender.path().name());
	}
	
	/*
	 * The implementation of handling incoming message in ReliableBroadcast
	 * We simply relay any new message to all other peers
	 */
	public void NoOrder(Object message, ActorRef me, ActorRef sender) throws Exception {
		// only for new message
		if (!delivered.contains(message)) {
        	for (ActorRef p : processes) { // go through all processes
        		if (!p.equals(me) && !p.equals(sender)) { // exclude itself and the sender
        			p.tell(message, me); // send the message point-2-point
        			Rdeliver(message, me, sender); // R-deliver the message
        			// add the message into delivered so it will not be delivered again
        			delivered.add((RBMessage) message); 
        		}
        	}
        }
	}
	
	int[] recvnext = null;
	int sendnext = 0;
	
	private int[] cVC = null;
	
	private void printVC() {
		String vc = "";
		for (int c : cVC) {
			vc += " " + c;
		}
		System.out.println(getSelf().path().name() + ":" + vc);
	}
	
	private Random rand = new Random(System.currentTimeMillis());
	
	/*
	 * Method to generate random delays
	 */
	private int counter = 100;
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
        	
        	/*
        	 * Some initializations for different broadcast algorithms
        	 */
        	
		} else if (message instanceof BroadcastMessage) {
			
			BroadcastMessage bm = (BroadcastMessage) message;
			Rbroadcast(message); // just for the debugging purpose, print the broadcast log
			
			/*
			 * rm is an instance of RBMessage and can be used to referenced to any subclass
			 */
			RBMessage rm = null;
			
			//--------------------------------------------------------------
			
			for (ActorRef p : processes) {
        		if (!p.equals(getSelf())) { // we don't send message to ourselves
        				/*
        				 * Simple ReliableBroadcast
        				 */
        				rm = new RBMessage(bm.getText());
        			
        			p.tell(rm, getSelf()); // send the message rm
        		}
        	}
			
			/*
			 * R-deliver is called 
			 * to deliver to self
			 * this is ReliabelBroadcast
			 */
			Rdeliver(rm, getSelf(), getSelf());
			delivered.add(rm);
		} else if (message instanceof RBMessage) {
			
			/*
			 * me and sender are two constant references to send to the Thread
			 * inside the Thread, we cannot use getSelf() and getSender()
			 */
			final ActorRef me = getSelf();
			final ActorRef sender = getSender();
			
			
				NoOrder(message, me, sender);
			
        } else {
            unhandled(message);
        }
    }
}