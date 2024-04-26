package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.exploBehaviors;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;
import java.time.LocalDateTime;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

/**
 * This behaviour allows an agent to share its map with other agents.
 * It listens for QUERY-REF-TOPO messages and responds with the agent's current map.
 * The map is sent as a SerializableSimpleGraph object.
 * 
 * @author hc
 */
public class ShareMapBehaviour extends CyclicBehaviour {

    private static final long serialVersionUID = 1L;
    private int messageCount = 0;  // Counter for the number of messages received

    /**
     * Constructor for ShareMapBehaviour.
     * 
     * @param a the agent this behaviour belongs to
     */
    public ShareMapBehaviour(WolfAgent a) {
        super(a);
        System.out.println(a.getLocalName() + " - ShareMapBehaviour created");
    }

    @Override
    public void action() {
        // Log the start of the action
        // System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - Checking for QUERY-REF-TOPO messages");

        // Create a message template for QUERY-REF-TOPO messages
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol("QUERY-REF-TOPO"),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)
        );

        // Receive the message
        ACLMessage msg = myAgent.receive(mt);
        if (msg != null && ((WolfAgent)myAgent).getMapManager()!=null) {
            if (msg != null && ((WolfAgent)myAgent).getMapManager()!=null) {
                // Get the timestamp from the message
                String timestampStr = msg.getUserDefinedParameter("timestamp");
                if (timestampStr != null) {
                    long messageTimestamp = Long.parseLong(timestampStr);
                    long currentTimestamp = System.currentTimeMillis();
            
                    // Check if the timestamp is not more than 100ms old
                    if (currentTimestamp - messageTimestamp <= 100) {
                        // Increment the message counter and log the message receipt
                        messageCount++;
                        System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - Received QUERY-REF-TOPO message #" + messageCount + " from " + msg.getSender().getLocalName());
                        System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - Received QUERY-REF-TOPO message content: " + msg.getContent() + " from " + msg.getSender().getLocalName());

                        // Get the sender's AID
                        AID senderAID = msg.getSender();
                        
                        // Create a reply message
                        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                        reply.setProtocol("SHARE-TOPO");
                        reply.setSender(this.myAgent.getAID());
                        reply.addReceiver(senderAID);
                        try {
                            // Log the map sharing
                            System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - Sharing map to " + senderAID.getLocalName());

                            // Get the subgraph for the agent and set it as the content of the reply
                            SerializableSimpleGraph<String, MapAttribute> sg = ((WolfAgent)myAgent).getMapManager().getSerialSubGraphForAgent(senderAID.getLocalName());
                            if (sg!=null){
                                reply.setContentObject(sg);
                                ((AbstractDedaleAgent)this.myAgent).sendMessage(reply);
                            }
                            else{
                                // Log if there is no map to share
                                System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - sg is null, no map to share");
                            }
                        } catch (IOException e) {
                            // Handle the exception and set the reply to FAILURE
                            e.printStackTrace();
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("Error sharing map");
                        }
                    }
                }
                    
            
        } else {
            // Log if no QUERY-REF-TOPO message was received and block the behaviour
            System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - No QUERY-REF-TOPO message received");
            block();
        }
    }
}}