package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.exploBehaviors;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MergeMapBehaviour extends CyclicBehaviour {

    private static final long serialVersionUID = -8184309378974273921L;

    public MergeMapBehaviour(WolfAgent a) {
        super(a);
        System.out.println(a.getLocalName() + " - MergeMapBehaviour created");
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-TOPO"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            System.out.println(myAgent.getLocalName() + " - Received map info from " + msg.getSender().getLocalName());
            try {
                SerializableSimpleGraph<String, MapAttribute> receivedMap = (SerializableSimpleGraph<String, MapAttribute>) msg.getContentObject();
                // Assuming MapManager is a class that contains methods to manage the map
                ((WolfAgent)myAgent).getMapManager().mergeMap(receivedMap, msg.getSender().getLocalName());
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }
}
