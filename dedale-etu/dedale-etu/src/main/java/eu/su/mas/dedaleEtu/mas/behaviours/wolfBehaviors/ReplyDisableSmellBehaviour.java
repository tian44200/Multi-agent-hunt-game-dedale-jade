package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors;

import java.util.ArrayList;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class ReplyDisableSmellBehaviour extends CyclicBehaviour {

    private static final long serialVersionUID = 1L;
    private List<String> list_agentNames;
    /**
     * Constructor for ListenForDisableSmellBehaviour.
     * 
     * @param a the agent this behaviour belongs to
     */
    public ReplyDisableSmellBehaviour(Agent a) {
        super(a);
        this.list_agentNames = new ArrayList<>(((WolfAgent)myAgent).getAgentNames());
        
    }

    @Override
    public void action() {
        // Create a message template for INFORM messages with protocol "DisableSmell"
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchProtocol("DisableSmell")
        );

        // Receive the message
        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            // Log the message receipt
            System.out.println("Received INFORM message with protocol DisableSmell from " + msg.getSender().getLocalName());
            if (this.list_agentNames.contains(msg.getSender().getLocalName())){
                try {
                    // Extract the content object from the response
                    SerializableSimpleGraph<String, MapAttribute> sgReceived = (SerializableSimpleGraph) msg.getContentObject();
                    System.out.println(myAgent.getLocalName() + " - Received content object: " + sgReceived);

                    // Merge the received map with the agent's map
                    // ((WolfAgent)this.myAgent).getMapManager().getMyMap().mergeMap(sgReceived);
                    
                } catch (UnreadableException e) {
                    // Print an error message if the content object could not be read
                    System.out.println(myAgent.getLocalName() + " - Error reading content object from response: " + e.getMessage());
                    e.printStackTrace();
                } 
                ((WolfAgent)this.myAgent).set_disable_smell();
                this.list_agentNames.remove(msg.getSender().getLocalName());
            }

            // Create a reply message
            ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
            reply.setProtocol("DisableSmell");
            reply.setSender(this.myAgent.getAID());
            reply.addReceiver(msg.getSender());

            // Send the reply
            ((AbstractDedaleAgent)myAgent).sendMessage(reply);
            System.out.println("Sent CONFIRM message with protocol DisableSmell to " + msg.getSender().getLocalName());
        } else {
            // If no message was received, block the behaviour
            block();
        }
    }
}
