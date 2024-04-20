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

public class VerifyGolemPosBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1L;
    private WolfAgent wolfAgent;

    public VerifyGolemPosBehaviour(WolfAgent wolfAgent) {
        super(wolfAgent);
        this.wolfAgent = wolfAgent;
    }

    @Override
    public void action() {
        System.out.println(this.myAgent.getLocalName() + " - VerifyGolemPosBehaviour");
        ACLMessage connRequest = new ACLMessage(ACLMessage.REQUEST);
        connRequest.setProtocol("ConnectionRequest");
        connRequest.setSender(this.myAgent.getAID());
        List<String> children = wolfAgent.getChildren();
        // 获取除了自己以外的所有代理的名称，并添加为接收者
        for (String agentName : ((WolfAgent)myAgent).getAgentNames()) {
            if (!children.contains(agentName)) {
                connRequest.addReceiver(new AID(agentName, AID.ISLOCALNAME));
            }
        }

        // 添加时间戳
        connRequest.addUserDefinedParameter("timestamp", String.valueOf(System.currentTimeMillis()));
        ((AbstractDedaleAgent)this.myAgent).sendMessage(connRequest);
        
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol("ConnectionResponse")
        );
        long endTime = System.currentTimeMillis() + 200; // 设置结束时间为当前时间后50毫秒
        boolean received = false;
        while (System.currentTimeMillis() < endTime) {
            ACLMessage response = myAgent.receive(mt);
            while (response != null) {
                String senderName = response.getSender().getLocalName();
                System.out.println(myAgent.getLocalName() + " - Received golem response from " + senderName);
                ((WolfAgent)this.myAgent).addChild(senderName);
                received = true; // 标记已成功接收到响应
                try {
                    SerializableSimpleGraph<String, MapAttribute> sgReceived = (SerializableSimpleGraph) response.getContentObject();
                    System.out.println(myAgent.getLocalName() + " - Received golem object: " + sgReceived);
                    ((WolfAgent)this.myAgent).getMapManager().getMyMap().mergeMap(sgReceived);

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
            System.out.println(myAgent.getLocalName() + " - No golem responses received within 50 ms.");
        }
    }
}