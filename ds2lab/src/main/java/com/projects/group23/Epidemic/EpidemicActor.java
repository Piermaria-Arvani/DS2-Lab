
package com.projects.group23.Epidemic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;





public class EpidemicActor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props() {
        return Props.create(EpidemicActor.class);
    }
    
    
    public static class EpidemicValue {
		protected long ts = -1;
    	protected String value = null;
    	
    	public EpidemicValue(long ts, String value) {
    		this.ts = ts;
    		this.value = value;
    	}
    	public EpidemicValue(EpidemicValue v) {
    		this.ts = v.getTs();
    		this.value = v.getValue();
    	}
    	
    	public long getTs() {
			return ts;
		}
		public void setTs(long ts) {
			this.ts = ts;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
		public void copy(EpidemicValue v) {
			this.value = v.getValue();
			this.ts = v.getTs();
		}
    	
    }
    
    public static class AssignMessage{
    	protected String value;
    	
    	public AssignMessage(String value) {
    		this.value = value;
		}
    	
    	public String getValue() {
    		return value;
    	}
    	
    }
    
    public static class EpidemicMessage{
    	protected EpidemicValue value = new EpidemicValue(0, null);
    	
    	public EpidemicValue getValue() {
    		return value;
    	}
    	
    	public void setValue(EpidemicValue v) {
    		this.value.copy(v);
    	}
    }
    
    
    /*
     * The StartMessage from the main function will tell a process about the peers
     */
    public static class StartMessage {
    	protected final List<ActorRef> group;
		
        public StartMessage(List<ActorRef> group) {
            this.group = Collections.unmodifiableList(group);
        }
    }
    
    // The list of peers is kept here
    protected List<ActorRef> processes = new ArrayList<ActorRef>();
    protected EpidemicValue v = new EpidemicValue(-1, null);
    int[] recvnext = null;
	int sendnext = 0;
	
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
	public void onReceive(final Object message) throws Exception {}

	protected void onEpidemicReceive(EpidemicMessage message) {}
	protected void  onEpidemicTimeout() {}
	
	long timeout = 0;
	private final long delta = 100;
	protected void setEpidemicTimeout() {
		timeout = System.currentTimeMillis() + delta;
	}
	
	int round = 0;
	protected void runSchedule() {
		log.info("Run Schedule now");
		Thread t = new Thread(new Runnable() {
			public void run() {
				while(true) {
					if(System.currentTimeMillis() >= timeout) {
						onEpidemicTimeout();
						round++;
						setEpidemicTimeout();
					}
				}
			}
		});
		t.start();
	}
    
}