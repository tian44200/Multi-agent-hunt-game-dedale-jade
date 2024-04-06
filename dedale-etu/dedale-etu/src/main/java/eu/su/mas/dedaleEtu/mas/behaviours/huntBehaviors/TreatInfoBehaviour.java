package eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors;

import java.time.LocalDateTime;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapManager;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class TreatInfoBehaviour extends OneShotBehaviour{
    private static final long serialVersionUID = -4763466178164421067L;
    private int state;
    
    public TreatInfoBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchProtocol("QUERY-REF-TOPO"),
            MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)
        );
        MessageTemplate mt2 = MessageTemplate.and(
            MessageTemplate.MatchProtocol("QUERY-REF-PARTIAL-TOPO"),
            MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)
        );
        MessageTemplate mt3 = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-TOPO"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage msg = myAgent.receive(mt1);
        if (msg != null) {
            System.out.println(this.myAgent.getLocalName()+" - Received TOPO request from "+msg.getSender().getLocalName());
            this.getDataStore().put("msg", msg);
            this.state = 1;
            return; 
        }

        msg = myAgent.receive(mt2);
        if (msg != null) {
            System.out.println(this.myAgent.getLocalName()+" - Received TOPO PT request from "+msg.getSender().getLocalName());
            this.getDataStore().put("msg", msg);
            this.state = 1;
            return; 
        }
        msg = myAgent.receive(mt3);
        if (msg != null) {
            System.out.println("received msg!");
            SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
                try {
                    System.out.println(LocalDateTime.now() + this.myAgent.getLocalName()+"received - Content: "+msg.getContentObject());
                    sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msg.getContentObject();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                System.out.println(LocalDateTime.now() + this.myAgent.getLocalName()+" - CURRENT MAP : "+((HuntAgent)myAgent).getMapManager().getMyMap().getSerializableGraph().toString());
                ((HuntAgent)myAgent).getMapManager().mergeMap(sgreceived, msg.getSender());
                System.out.println(LocalDateTime.now() + this.myAgent.getLocalName()+" - CURRENT MAP : "+((HuntAgent)myAgent).getMapManager().getMyMap().getSerializableGraph().toString());
            this.state = 0;
            return; 
        }
        this.state = 2;
    }
    @Override
    public int onEnd() {
        return this.state;
    }
}