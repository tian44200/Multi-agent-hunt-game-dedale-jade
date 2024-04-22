package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * This class represents a behaviour of a WolfAgent that verifies the position of a Golem.
 */
public class VerifyGolemPosBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1L;
    private WolfAgent wolfAgent;

    /**
     * Constructor for VerifyGolemPosBehaviour.
     *
     * @param wolfAgent the WolfAgent that this behaviour belongs to
     */
    public VerifyGolemPosBehaviour(WolfAgent wolfAgent) {
        super(wolfAgent);
        this.wolfAgent = wolfAgent;
    }

    /**
     * This method is called when this behaviour is started.
     * The agent sends a connection request to all other agents and waits for a response.
     */
    @Override
    public void action() {
        System.out.println(this.myAgent.getLocalName() + " - VerifyGolemPosBehaviour");
        ACLMessage connRequest = new ACLMessage(ACLMessage.REQUEST);
        connRequest.setProtocol("ConnectionRequest");
        connRequest.setSender(this.myAgent.getAID());
        List<String> children = wolfAgent.getChildren();

        // Get the names of all other agents and add them as receivers
        for (String agentName : ((WolfAgent)myAgent).getAgentNames()) {
            if (!children.contains(agentName)) {
                connRequest.addReceiver(new AID(agentName, AID.ISLOCALNAME));
            }
        }

        // Add a timestamp
        connRequest.addUserDefinedParameter("timestamp", String.valueOf(System.currentTimeMillis()));
        ((AbstractDedaleAgent)this.myAgent).sendMessage(connRequest);
        
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol("ConnectionResponse")
        );
        long endTime = System.currentTimeMillis() + 200; // Set the end time to 200 milliseconds from now
        boolean received = false;

        // Wait for a response until the end time
        while (System.currentTimeMillis() < endTime) {
            ACLMessage response = myAgent.receive(mt);

            // Process all received responses
            while (response != null) {
                String senderName = response.getSender().getLocalName();
                System.out.println(myAgent.getLocalName() + " - Received golem response from " + senderName);
                ((WolfAgent)this.myAgent).addChild(senderName);
                received = true; // Mark that a response has been received

                try {
                    // Extract the content object from the response
                    SerializableSimpleGraph<String, MapAttribute> sgReceived = (SerializableSimpleGraph) response.getContentObject();
                    System.out.println(myAgent.getLocalName() + " - Received golem object: " + sgReceived);

                    // Merge the received map with the agent's map
                    ((WolfAgent)this.myAgent).getMapManager().getMyMap().mergeMap(sgReceived);

                } catch (UnreadableException e) {
                    // Print an error message if the content object could not be read
                    System.out.println(myAgent.getLocalName() + " - Error reading content object from response: " + e.getMessage());
                    e.printStackTrace();
                } 

                // Send a confirmation message
                ACLMessage confirm = new ACLMessage(ACLMessage.CONFIRM);
                confirm.setProtocol("ConnectionConfirm");
                confirm.addReceiver(response.getSender());
                confirm.setSender(this.myAgent.getAID());
                ((AbstractDedaleAgent)myAgent).sendMessage(confirm);
                response = myAgent.receive(mt);
            } 
            block(10); // Block the agent for 10 milliseconds if no response was received
        }

        // Print a message if no responses were received within 200 milliseconds
        if (!received) {
            System.out.println(myAgent.getLocalName() + " - No golem responses received within 200 ms.");
        }
    }
}