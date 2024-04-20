package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class HandleConnectionResponseBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = -4575489698582385498L;
    public HandleConnectionResponseBehaviour(WolfAgent agent) {
        super(agent);
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol("ConnectionResponse")
        );
        long endTime = System.currentTimeMillis() + 55; // 设置结束时间为当前时间后50毫秒
        boolean received = false;
        while (System.currentTimeMillis() < endTime) {
            ACLMessage response = myAgent.receive(mt);
            while (response != null) {
                String senderName = response.getSender().getLocalName();
                System.out.println(myAgent.getLocalName() + " - Received connection response from " + senderName);
                ((WolfAgent)this.myAgent).addChild(senderName);
                received = true; // 标记已成功接收到响应
                try {
                    SerializableSimpleGraph<String, MapAttribute> sgReceived = (SerializableSimpleGraph) response.getContentObject();
                    System.out.println(myAgent.getLocalName() + " - Received content object: " + sgReceived);
                    ((WolfAgent)this.myAgent).getMapManager().getMyMap().mergeMap(sgReceived);

                    // 发送确认消息
                    ACLMessage confirm = new ACLMessage(ACLMessage.CONFIRM);
                    confirm.setProtocol("ConnectionConfirm");
                    confirm.addReceiver(response.getSender());
                    confirm.setSender(this.myAgent.getAID());
                    ((AbstractDedaleAgent)myAgent).sendMessage(confirm);
                    
                } catch (UnreadableException e) {
					// TODO Auto-generated catch block
                    System.out.println(myAgent.getLocalName() + " - Error reading content object from response: " + e.getMessage());
					e.printStackTrace();
				} 
                response = myAgent.receive(mt);
            } 
            block(10);
        }

        if (!received) {
            System.out.println(myAgent.getLocalName() + " - No connection responses received within 50 ms.");
        }
    }
}

