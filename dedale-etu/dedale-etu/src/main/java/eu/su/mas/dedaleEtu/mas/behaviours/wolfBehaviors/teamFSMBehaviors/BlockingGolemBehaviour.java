package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BlockingGolemBehaviour extends SimpleBehaviour {
    private static final long serialVersionUID = 1L;
    private List<String> sentAgentList = new ArrayList<>();


	// Constructor
    public BlockingGolemBehaviour(jade.core.Agent a) {
        super(a);
    }

    @Override
    public void action() {
        // 创建一个消息模板来匹配接收到的请求
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol("ConnectionRequest")
        );

        // 接收匹配的消息
        ACLMessage request = myAgent.receive(mt);
        if (request != null && !sentAgentList.contains(request.getSender().getLocalName())) {
            // 如果接收到了消息，并且消息的发送者不在已发送消息的代理列表中

            // 回复一个"GoAway"的消息
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setProtocol("GoAway");
            reply.addReceiver(request.getSender());
            reply.setSender(this.myAgent.getAID());
            try {
                reply.setContentObject(((WolfAgent)this.myAgent).getMapManager().getObservationMap().getSerializableGraph());
            } catch (IOException e) {
                e.printStackTrace();
            }
            ((AbstractDedaleAgent)this.myAgent).sendMessage(reply);

        // 等待确认消息
        MessageTemplate mtConfirm = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
            MessageTemplate.MatchProtocol("GoAwayConfirm")
        );
        ACLMessage confirm = myAgent.receive(mtConfirm);
        if (confirm != null) {
            // 如果接收到了确认消息，将发送者添加到已发送消息的代理列表中
            sentAgentList.add(confirm.getSender().getLocalName());
            return;
        }
        }

        System.out.println("I have blocked the Golem and finished my work.");
    }

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
}