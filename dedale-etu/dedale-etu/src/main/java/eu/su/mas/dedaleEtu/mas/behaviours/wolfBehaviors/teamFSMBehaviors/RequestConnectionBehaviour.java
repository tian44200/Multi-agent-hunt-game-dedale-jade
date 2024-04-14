package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;


public class RequestConnectionBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1L;

    public RequestConnectionBehaviour(WolfAgent a) {
        super(a);
    }

    @Override
    public void action() {
        ACLMessage connRequest = new ACLMessage(ACLMessage.REQUEST);
        connRequest.setProtocol("ConnectionRequest");
        connRequest.setSender(this.myAgent.getAID());

        // 获取除了自己以外的所有代理的名称，并添加为接收者
        for (String agentName : ((WolfAgent)myAgent).getAgentNames()) {
            connRequest.addReceiver(new AID(agentName, AID.ISLOCALNAME));
        }

        // 添加时间戳
        connRequest.addUserDefinedParameter("timestamp", String.valueOf(System.currentTimeMillis()));
        ((AbstractDedaleAgent)this.myAgent).sendMessage(connRequest);

        System.out.println(myAgent.getLocalName() + " - Sent Connection request to all other agents with timestamp.");
    }
}
