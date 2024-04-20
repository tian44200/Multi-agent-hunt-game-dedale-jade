package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;

import java.io.IOException;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
        while (request != null ) {
            String timestampStr = request.getUserDefinedParameter("timestamp");
            if (timestampStr != null) {
                long timestamp = Long.parseLong(timestampStr);
                if (currentTime - timestamp <= 50) { // 检查时间戳是否在50毫秒之内
                    System.out.println(myAgent.getLocalName() + " - Received connection request from " + request.getSender().getLocalName() + " within 50 ms.");
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
                    return;
                } else {
                    System.out.println(myAgent.getLocalName() + " - Received outdated connection request from " + request.getSender().getLocalName());
                }
            }
            request = myAgent.receive(mt);
        }
            // 等待确认消息
        MessageTemplate mtConfirm = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
            MessageTemplate.MatchProtocol("ConnectionConfirm")
        );
        long endTime = System.currentTimeMillis() + 15; // 设置结束时间为当前时间后15秒
        while (System.currentTimeMillis() < endTime) {
            ACLMessage confirm = myAgent.receive(mtConfirm);
            if (confirm != null) {
                System.out.println(myAgent.getLocalName() + " - Received connection confirm from " + confirm.getSender().getLocalName());
                ((WolfAgent)this.myAgent).setParent(confirm.getSender().getLocalName());
                return;
            }
            block(5);
        }
        System.out.println(myAgent.getLocalName() + " - No connection confirm received within 15 seconds.");

    }

    @Override
    public int onEnd() {
        if (((WolfAgent)this.myAgent).getParent() != null) {
            ((WolfAgent)this.myAgent).setDisband(false);
            ((WolfAgent)this.myAgent).setTargetAndNextNode(null);
            return 1; // 成功处理连接请求
        } else {
            if (((WolfAgent)this.myAgent).isDisband()) {
                return 2; 
            }
            return 0; // 没有处理连接请求
        }
    }
}

