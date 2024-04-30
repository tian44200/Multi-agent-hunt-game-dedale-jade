package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

/**
 * This class represents a behaviour of a WolfAgent that handles connection responses.
 */
public class HandleConnectionResponseBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = -4575489698582385498L;

    /**
     * Constructor for HandleConnectionResponseBehaviour.
     *
     * @param agent the WolfAgent that this behaviour belongs to
     */
    public HandleConnectionResponseBehaviour(WolfAgent agent) {
        super(agent);
    }

    /**
     * This method is called when this behaviour is started.
     * The agent waits for a connection response and processes it.
     */
    @Override
    public void action() {
        // Create a message template to filter the messages that the agent receives
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol("ConnectionResponse")
        );

        long endTime = System.currentTimeMillis() + 100; // Set the end time to 55 milliseconds from now
        boolean received = false;

        // Wait for a connection response until the end time
        while (System.currentTimeMillis() < endTime) {
            ACLMessage response = myAgent.receive(mt);

            // Process all received connection responses
            while (response != null) {
                String senderName = response.getSender().getLocalName();
                System.out.println(myAgent.getLocalName() + " - Received connection response from " + senderName);
                ((WolfAgent)this.myAgent).addChild(senderName);
                received = true; // Mark that a response has been received

                try {
                    // Extract the content object from the response
                    SerializableSimpleGraph<String, MapAttribute> sgReceived = (SerializableSimpleGraph) response.getContentObject();
                    System.out.println(myAgent.getLocalName() + " - Received content object: " + sgReceived);

                    // Merge the received map with the agent's map
                    ((WolfAgent)this.myAgent).getMapManager().getMyMap().mergeMap(sgReceived);

                    // Send a confirmation message
                    ACLMessage confirm = new ACLMessage(ACLMessage.CONFIRM);
                    confirm.setProtocol("ConnectionConfirm");
                    confirm.addReceiver(response.getSender());
                    confirm.setSender(this.myAgent.getAID());
                    confirm.addUserDefinedParameter("timestamp", String.valueOf(System.currentTimeMillis()));

                    // Add a timestamp
                    ((AbstractDedaleAgent)myAgent).sendMessage(confirm);
                    String timestamp = String.valueOf(System.currentTimeMillis());

                    System.out.println(timestamp + senderName + " - Sent connection confirmation to " + response.getSender().getLocalName());
                    
                } catch (UnreadableException e) {
                    // Print an error message if the content object could not be read
                    System.out.println(myAgent.getLocalName() + " - Error reading content object from response: " + e.getMessage());
                    e.printStackTrace();
                } 
                response = myAgent.receive(mt);
            } 
            block(10); // Block the agent for 10 milliseconds if no response was received
        }

        // Print a message if no connection responses were received within 55 milliseconds
        if (!received) {
            System.out.println(myAgent.getLocalName() + " - No connection responses received within 100 ms.");
        }
    }
}