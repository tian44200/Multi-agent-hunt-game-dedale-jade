package eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors.ShareMapFSMBehaviour.ShareBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors.ShareMapFSMBehaviour.WaitBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapManager;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
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
    private List<String> agentNames;
    private AID senderAID;

    public ShareMapFSMBehaviour(final AbstractDedaleAgent a, MapManager mapManager, List<String> agentNames) {
        super(a);
        this.mapManager = mapManager;
        this.agentNames = agentNames;
        // Register states
        registerFirstState(new WaitStaticMapBehaviour(a), STATE_WAIT);
        registerState(new SendStaticMapBehaviour(a), STATE_SHARE);

        // Define transitions
        registerDefaultTransition(STATE_WAIT, STATE_SHARE);
        registerDefaultTransition(STATE_SHARE, STATE_WAIT);
    }


    class WaitStaticMapBehaviour extends SimpleBehaviour {
        private static final long serialVersionUID = 1L;
        private MessageTemplate mt;
        private boolean finished;

        public WaitStaticMapBehaviour(Agent a) {
            super(a);
            mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol("REQUEST-TOPO"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
            );
            finished = false;
        }
            
        @Override
        public void action() {
            if(mapManager == null) {
                System.out.println("MapManager is null");
                MapRepresentation myMap = new MapRepresentation();
                mapManager = new MapManager(myMap, agentNames);
                ((HuntAgent)myAgent).setMapManager(mapManager);
                System.out.println("MapManager is now initialized"+ mapManager);
                System.out.println("MapManager is now initialized in agent"+ ((HuntAgent)myAgent).getMapManager());
            }
            // System.out.println("myAgent " + myAgent);
            // System.out.println("this.myAgent " + this.myAgent);
            // System.out.println("myAgent local name " + this.myAgent.getLocalName());
            // System.err.println("((SkilledChaseAgent)myAgent) " + ((SkilledChaseAgent)myAgent));
            ACLMessage msg = this.myAgent.receive(mt);
            if (msg != null) {
                // Save the sender's AID
                senderAID = msg.getSender();
                // System.out.println("Hi I received a message from " + senderAID);
                finished = true;
            }else{
                block();
            }
        }

        public boolean done() {
            return finished;
        }
    }

    class SendStaticMapBehaviour extends OneShotBehaviour {
        private static final long serialVersionUID = 1L;

        public SendStaticMapBehaviour(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            // Create a new ACLMessage
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setProtocol("SHARE-TOPO");
            reply.addReceiver(senderAID);
            // System.out.println("senderAID is "+senderAID.getName());
            // System.out.println("Hi I reply ShareBehaviour");

            try {
                // Serialize the map and set it as the content of the message
                SerializableSimpleGraph<String, MapAttribute> sg=mapManager.getStaticSerialSubGraphForAgent(senderAID.getName());
                // System.out.println("sg is "+sg.toString());
                if(sg != null) {
                    System.out.println("sg is not null Map sent to "+senderAID.getName());
                    reply.setContentObject(sg);
                    // Send the message
                    // System.out.println("Map is" + sg.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class WaitDynamicMapBehavior extends SimpleBehaviour{
        private static final long serialVersionUID = 1L;
        private MessageTemplate mt;
        private boolean finished;

        public WaitDynamicMapBehavior(Agent a) {
            super(a);
            mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol("REQUEST-DYNAMIC-MAP"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
            );
            finished = false;
        }
            
        @Override
        public void action() {
            if(mapManager == null) {
                System.out.println("MapManager is null");
                MapRepresentation myMap = new MapRepresentation();
                mapManager = new MapManager(myMap, agentNames);
                ((HuntAgent)myAgent).setMapManager(mapManager);
                System.out.println("MapManager is now initialized"+ mapManager);
                System.out.println("MapManager is now initialized in agent"+ ((HuntAgent)myAgent).getMapManager());
            }
            // System.out.println("myAgent " + myAgent);
            // System.out.println("this.myAgent " + this.myAgent);
            // System.out.println("myAgent local name " + this.myAgent.getLocalName());
            // System.err.println("((SkilledChaseAgent)myAgent) " + ((SkilledChaseAgent)myAgent));
            ACLMessage msg = this.myAgent.receive(mt);
            if (msg != null) {
                // Save the sender's AID
                senderAID = msg.getSender();
                // System.out.println("Hi I received a message from " + senderAID);
                finished = true;
            }
        }

        public boolean done() {
            return finished;
        }
    }

    class SendDynamicMapBehavior extends OneShotBehaviour{
        private static final long serialVersionUID = 1L;

        public SendDynamicMapBehavior(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            // Create a new ACLMessage
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setProtocol("SHARE-DYNAMIC-MAP");
            reply.addReceiver(senderAID);
            // System.out.println("senderAID is "+senderAID.getName());
            // System.out.println("Hi I reply ShareBehaviour");

            try {
                // Serialize the map and set it as the content of the message
                SerializableSimpleGraph<String, MapAttribute> sg=mapManager.getDynamicSerialSubGraphForAgent(senderAID.getName());
                // System.out.println("sg is "+sg.toString());
                if(sg != null) {
                    System.out.println("sg is not null Map sent to "+senderAID.getName());
                    reply.setContentObject(sg);
                    // Send the message
                    // System.out.println("Map is" + sg.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}