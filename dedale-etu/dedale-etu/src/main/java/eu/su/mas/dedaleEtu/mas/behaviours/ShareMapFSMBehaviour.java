package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapManager;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javafx.util.Pair;
import java.util.Map;
import java.util.HashMap;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class ShareMapFSMBehaviour extends FSMBehaviour {

    private static final long serialVersionUID = 1L;

    private static final String STATE_WAIT = "Wait";
    private static final String STATE_SHARE = "Share";

    private MapManager mapManager;
    private AID senderAID;

    public ShareMapFSMBehaviour(Agent a, MapManager mapManager, List<String> agentNames) {
        super(a);

        this.mapManager = mapManager;
        if(this.mapManager==null) {
            this.mapManager= new MapManager(null,agentNames);
        }

        // Register states
        registerFirstState(new WaitBehaviour(a), STATE_WAIT);
        registerState(new ShareBehaviour(a), STATE_SHARE);

        // Define transitions
        registerDefaultTransition(STATE_WAIT, STATE_SHARE);
        registerDefaultTransition(STATE_SHARE, STATE_WAIT);
    }

    class WaitBehaviour extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;
        private MessageTemplate mt;

        public WaitBehaviour(Agent a) {
            super(a);
            mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-TOPO"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            );
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Save the sender's AID
                senderAID = msg.getSender();
                myAgent.removeBehaviour(this);
            } else {
                block();
            }
        }
    }

    class ShareBehaviour extends OneShotBehaviour {
        private static final long serialVersionUID = 1L;

        public ShareBehaviour(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            // Create a new ACLMessage
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setProtocol("SHARE-TOPO");
            reply.addReceiver(senderAID);

            try {
                // Serialize the map and set it as the content of the message
                System.out.println("checkpoint0");
                SerializableSimpleGraph<String, MapAttribute> sg=mapManager.getSerializableSubGraphToShareForAgent(senderAID.getName());
                System.out.println("checkpoint1");
                reply.setContentObject(sg);
                System.out.println("checkpoint2");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Send the message
            ((AbstractDedaleAgent)this.myAgent).send(reply);
        }
    }
}