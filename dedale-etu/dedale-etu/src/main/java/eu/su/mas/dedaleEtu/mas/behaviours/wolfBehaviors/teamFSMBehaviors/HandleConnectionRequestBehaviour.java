package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;

import java.io.IOException;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import java.io.IOException;

public class HandleConnectionRequestBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = -2298837520882967065L;

    public HandleConnectionRequestBehaviour(WolfAgent agent) {
        super(agent);
    }

    @Override
    public void action() {

        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol("ConnectionRequest")
        );
        this.myAgent.doWait(50);
        long currentTime = System.currentTimeMillis();
        ACLMessage request = myAgent.receive(mt);
        while (request != null) {
            String timestampStr = request.getUserDefinedParameter("timestamp");
            if (timestampStr != null) {
                long timestamp = Long.parseLong(timestampStr);
                if (currentTime - timestamp <= 50) { // 检查时间戳是否在50毫秒之内
                    System.out.println(myAgent.getLocalName() + " - Received connection request from " + request.getSender().getLocalName() + " within 50 ms.");
                    ((WolfAgent)this.myAgent).setParent(request.getSender().getLocalName());
                    ACLMessage reply = request.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setProtocol("ConnectionResponse");
                    reply.setSender(this.myAgent.getAID());
                    reply.addReceiver(request.getSender());
                    try {
                        reply.setContentObject(((WolfAgent)this.myAgent).getMapManager().getObservationMap().getSerializableGraph());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ((AbstractDedaleAgent)this.myAgent).sendMessage(reply);
                    return;
                } else {
                    System.out.println(myAgent.getLocalName() + " - Received outdated connection request from " + request.getSender().getLocalName());
                }
            }
            request = myAgent.receive(mt);
        }
    }

    @Override
    public int onEnd() {
        if (((WolfAgent)this.myAgent).getParent() != null) {
            return 1; // 成功处理连接请求
        } else {
            return 0; // 没有处理连接请求
        }
    }
}

