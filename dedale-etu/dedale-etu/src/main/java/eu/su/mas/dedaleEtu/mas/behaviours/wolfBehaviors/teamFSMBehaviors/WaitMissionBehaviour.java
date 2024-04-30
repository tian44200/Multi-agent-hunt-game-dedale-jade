package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javafx.util.Pair;

import java.util.Map;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;


/**
 * This class represents a behaviour of a WolfAgent that waits for a mission.
 */
public class WaitMissionBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1L;
    private WolfAgent wolfAgent;
    private long timeout;

    /**
     * Constructor for WaitMissionBehaviour.
     *
     * @param a       the WolfAgent that this behaviour belongs to
     * @param timeout the maximum time to wait for a mission
     */
    public WaitMissionBehaviour(WolfAgent a, long timeout) {
        super(a);
        this.wolfAgent = a;
        this.timeout = timeout;
    }

    /**
     * This method is called when this behaviour is started.
     * The agent waits for a mission until the timeout is reached.
     */
    @Override
    public void action() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout;
        boolean received = false;

        // Print a message indicating that the agent is waiting for a mission
        System.out.println(this.myAgent.getLocalName() + " - Waiting for mission");

        // Create a message template to filter the messages that the agent receives
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchProtocol("Task-Distribution-Protocol")
            ),
            MessageTemplate.MatchSender(new AID(wolfAgent.getParent(), AID.ISLOCALNAME))
        );

        // Wait for a mission until the timeout is reached
        while (System.currentTimeMillis() < endTime && !received) {
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null && msg.getSender().getLocalName().equals(wolfAgent.getParent())) {
                String timestampStr = msg.getUserDefinedParameter("timestamp");
                if (timestampStr != null) {
                    long messageTimestamp = Long.parseLong(timestampStr);
                    long currentTimestamp = System.currentTimeMillis();
            
                    // Check if the timestamp is not more than 1000ms old
                    if (currentTimestamp - messageTimestamp <= 500) {
                        // The rest of your code...
                        received = true;
                        try {
                            // Extract the mission from the message
                            Map<String, Pair<String, String>> agentTargets = (Map<String, Pair<String, String>>) msg.getContentObject();
                            Pair<String, String> myTargetAndPriority = agentTargets.get(wolfAgent.getMyPositionID());
                            wolfAgent.setTargetAndNextNode(myTargetAndPriority);
                            String nextNode = wolfAgent.getNextNode();
                            if (nextNode != null && nextNode.equals("block")){
                                wolfAgent.getMapManager().getObservationMap().clearMap();
                                Set<String> block_agents = agentTargets.keySet();
                                System.out.println("Received message from " + msg.getSender().getLocalName() + "for blocking mission: " + block_agents.toString());
                                for (String agent : block_agents){
                                    if (agentTargets.get(agent).getValue().equals("block")){
                                        wolfAgent.getMapManager().getObservationMap().addNode(agent, MapAttribute.block);
                                        System.out.println("Blocking agent: " + agent);
                                        System.out.println("Blocking agent's position: " + wolfAgent.getMapManager().getObservationMap().toString());
                                    }
                            }
                        }
                            // Print a message indicating that the agent received a mission
                            System.out.println(myAgent.getLocalName() + " - Received mission from " + msg.getSender().getLocalName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println(((AbstractDedaleAgent)myAgent).getLocalName() + "Message timestamp is more than 1000ms old from" + msg.getSender().getLocalName());
                    }
                }
                
            }
            if (!received) {
                block(10); // If no message was received, block the agent for 10 milliseconds
            }
        }

        // If the timeout was reached without receiving a mission, print a message
        if (!received) {
            System.out.println(myAgent.getLocalName() + " - Timeout without receiving mission");
        }
    }
}