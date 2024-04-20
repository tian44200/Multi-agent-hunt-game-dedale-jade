package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class HandleGoAwayBehaviour extends CyclicBehaviour {
    private static final long serialVersionUID = 1L;

    public HandleGoAwayBehaviour(WolfAgent a) {
        super(a);
    }
@Override
public void action() {
    // 创建一个消息模板来匹配接收到的 "GoAway" 消息
    MessageTemplate mt = MessageTemplate.and(
        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
        MessageTemplate.MatchProtocol("GoAway")
    );

    // 接收匹配的消息
    ACLMessage msg = myAgent.receive(mt);
    if (msg != null) {
        System.out.println(myAgent.getLocalName() + " - Received 'GoAway' message.");
        try {
                    SerializableSimpleGraph<String, MapAttribute> sgReceived = (SerializableSimpleGraph) response.getContentObject();
                    System.out.println(myAgent.getLocalName() + " - Received content object: " + sgReceived);
                    ((WolfAgent)this.myAgent).getMapManager().getMyMap().mergeMap(sgReceived);
        } catch (UnreadableException e) {
					// TODO Auto-generated catch block
                    System.out.println(myAgent.getLocalName() + " - Error reading content object from response: " + e.getMessage());
					e.printStackTrace();
				} 
        // 回复一个 "GoAwayConfirm" 消息
        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
        reply.setProtocol("GoAwayConfirm");
        reply.addReceiver(msg.getSender());
        reply.setSender(this.myAgent.getAID());
        ((AbstractDedaleAgent) myAgent).sendMessage(reply);
        System.out.println(myAgent.getLocalName() + " - Responded with 'GoAwayConfirm' message.");
    } else {
        block();
    }
}
}
