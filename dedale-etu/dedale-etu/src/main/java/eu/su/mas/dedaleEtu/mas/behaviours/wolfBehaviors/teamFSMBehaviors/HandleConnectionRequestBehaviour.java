package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;

import java.io.IOException;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This class represents a behaviour of a WolfAgent that handles connection requests.
 */
public class HandleConnectionRequestBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = -2298837520882967065L;

    /**
     * Constructor for HandleConnectionRequestBehaviour.
     *
     * @param agent the WolfAgent that this behaviour belongs to
     */
    public HandleConnectionRequestBehaviour(WolfAgent agent) {
        super(agent);
    }

    /**
     * This method is called when this behaviour is started.
     * The agent waits for a connection request and responds to it.
     */
    @Override
    public void action() {
       boolean received = false;
        // Create a message template to filter the messages that the agent receives
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol("ConnectionRequest")
        );

        // Wait for a connection request
        this.myAgent.doWait(50);
        long currentTime = System.currentTimeMillis();
        ACLMessage request = myAgent.receive(mt);

        // Process all received connection requests
        while (request != null ) {
            String timestampStr = request.getUserDefinedParameter("timestamp");
            if (timestampStr != null) {
                long timestamp = Long.parseLong(timestampStr);
                // Check if the timestamp is within 50 milliseconds
                if (currentTime - timestamp <= 50) {
                    // Print a message indicating that a connection request was received
                    System.out.println(myAgent.getLocalName() + " - Received connection request from " + request.getSender().getLocalName() + " within 50 ms.");

                    // Send a response to the connection request
                    ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                    reply.setProtocol("ConnectionResponse");
                    reply.setSender(this.myAgent.getAID());
                    reply.addReceiver(request.getSender());
                    try {
                        reply.setContentObject(((WolfAgent)this.myAgent).getMapManager().getObservationMap().getSerializableGraph());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ((AbstractDedaleAgent)this.myAgent).sendMessage(reply);
                    received = true;
                    break;
                } else {
                    // Print a message indicating that an outdated connection request was received
                    System.out.println(myAgent.getLocalName() + " - Received outdated connection request from " + request.getSender().getLocalName());
                }
            }
            request = myAgent.receive(mt);
        }

        if (!received) {
            // Print a message indicating that no connection request was received
            System.out.println(myAgent.getLocalName() + " - No connection request received within 50 ms.");
            return;
        }
        // Wait for a connection confirmation
        MessageTemplate mtConfirm = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
            MessageTemplate.MatchProtocol("ConnectionConfirm")
        );
        long endTime = System.currentTimeMillis() + 50; 
        System.out.println(System.currentTimeMillis() + myAgent.getLocalName() + " - Waiting for connection confirm to" + endTime);
        while (System.currentTimeMillis() < endTime) {
            ACLMessage confirm = myAgent.receive(mtConfirm);
            if (confirm != null) {
                // Print a message indicating that a connection confirmation was received
                System.out.println(myAgent.getLocalName() + " - Received connection confirm from " + confirm.getSender().getLocalName());

                // Set the sender of the confirmation as the parent of the agent
                ((WolfAgent)this.myAgent).setParent(confirm.getSender().getLocalName());
                return;
            }
            block(3);
        }
        // Print a message indicating that no connection confirmation was received
        System.out.println(myAgent.getLocalName() + " - No connection confirm received within 15 seconds.");
    }

    /**
     * This method is called when this behaviour ends.
     * It checks if the agent has a parent and the target and next node accordingly.
     *
     * @return 1 if the connection request was handled successfully, 0 if it was not handled
     */
    @Override
    public int onEnd() {
        if (((WolfAgent)this.myAgent).getParent() != null) {
            ((WolfAgent)this.myAgent).setTargetAndNextNode(null);
            return 1; // Connection request was handled successfully
        } 
            return 0; // Connection request was not handled
        }
    }
