package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.exploBehaviors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;
import java.time.LocalDateTime;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class ShareMapBehaviour extends CyclicBehaviour {

    private static final long serialVersionUID = 1L;
    private int messageCount = 0;

    public ShareMapBehaviour(WolfAgent a) {
        super(a);
        System.out.println(a.getLocalName() + " - ShareMapBehaviour created");
    }

    @Override
    public void action() {
        System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - Checking for QUERY-REF-TOPO messages");
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol("QUERY-REF-TOPO"),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)
        );

        ACLMessage msg = myAgent.receive(mt);
        if (msg != null && ((WolfAgent)myAgent).getMapManager()!=null) {
            messageCount++;
            System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - Received QUERY-REF-TOPO message #" + messageCount + " from " + msg.getSender().getLocalName());
            System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - Received QUERY-REF-TOPO message content: " + msg.getContent() + " from " + msg.getSender().getLocalName());

            AID senderAID = msg.getSender();
            
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setProtocol("SHARE-TOPO");
            reply.setSender(this.myAgent.getAID());
            reply.addReceiver(senderAID);
            try {
                System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - Sharing map to " + senderAID.getLocalName());
                SerializableSimpleGraph<String, MapAttribute> sg = ((WolfAgent)myAgent).getMapManager().getSerialSubGraphForAgent(senderAID.getLocalName());
                if (sg!=null){
                    reply.setContentObject(sg);
                    myAgent.send(reply);
                }
                else{
                    System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - sg is null, no map to share");
                }
            } catch (IOException e) {
                e.printStackTrace();
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("Error sharing map");
            }
            
        } else {
            System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - No QUERY-REF-TOPO message received");
            block();
        }
    }
}
