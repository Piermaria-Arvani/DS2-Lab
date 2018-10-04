package com.projects.group23;


import java.util.ArrayList;
import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class ApplicationMain {

    public static void main(String[] args) {
    	
    	ActorSystem system = ActorSystem.create("MyActorSystem");
        
        int N = 4;
        List<ActorRef> ps = new ArrayList<ActorRef>();
        for (int i = 1; i <= N; i++) {
        	ps.add(system.actorOf(ReliableBroadcast.props().withDispatcher("akka.actor.my-pinned-dispatcher"), "RB" + String.valueOf(i)));
        }
        for (ActorRef p : ps) {
        	p.tell(new ReliableBroadcast.StartMessage(ps), null);
        }
        
        ps.get(0).tell(new ReliableBroadcast.BroadcastMessage("a"), null);
        system.awaitTermination();
    }

} 