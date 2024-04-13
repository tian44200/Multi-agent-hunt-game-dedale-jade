package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class HandleConnectionRequestBehaviour extends Behaviour {
    private static final long serialVersionUID = -2298837520882967065L;
    private long startTime;
    private long duration = 10000; // duration in milliseconds

    public HandleConnectionRequestBehaviour(WolfAgent agent,long duration) {
        super(agent);
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }

    @Override
    public void action() {
        if (System.currentTimeMillis() - startTime > duration) {
            return;
        }

        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchProtocol("ConnectionRequest")
        );
        MessageTemplate mt2 = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol("ConnectionResponse")
        );
        MessageTemplate mt3 = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
            MessageTemplate.MatchProtocol("ConnectionConfirm")
        );

        ACLMessage request = myAgent.receive(mt);
        if (request != null) {
            System.out.println(myAgent.getLocalName() + " - Received connection request from " + request.getSender().getLocalName());
            ((WolfAgent)this.myAgent).getMapManager().addDirectCommunication(request.getSender().getLocalName());
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setProtocol("ConnectionResponse");
            reply.setSender(this.myAgent.getAID());
            ((AbstractDedaleAgent)this.myAgent).sendMessage(reply);
        }

        ACLMessage response = myAgent.receive(mt2);
        if (response != null) {
            System.out.println(myAgent.getLocalName() + " - Received connection response from " + response.getSender().getLocalName());
            ((WolfAgent)this.myAgent).getMapManager().addDirectCommunication(response.getSender().getLocalName());
            ACLMessage confirm = response.createReply();
            confirm.setPerformative(ACLMessage.CONFIRM);
            confirm.setProtocol("ConnectionConfirm");
            confirm.setSender(this.myAgent.getAID());
            ((AbstractDedaleAgent)this.myAgent).sendMessage(confirm);
            try {
                Thread.sleep(1000); // 暂停1秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ACLMessage confirm = myAgent.receive(mt3);
        if (confirm != null) {
            ((WolfAgent)this.myAgent).getMapManager().addDirectCommunication(confirm.getSender().getLocalName());
            try {
                Thread.sleep(1000); // 暂停1秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean done() {
        System.out.println("HandleConnectionRequestBehaviour done");
        return System.currentTimeMillis() - startTime > duration;
    }

    @Override
    public int onEnd() {
        if (((WolfAgent)this.myAgent).getMapManager().getCommunicationMap().getDegreeForNode(this.myAgent.getLocalName()) > 1) { // TODO: check if this is correct for local name
            return 1;
        } else {
            return 0;
        }
    }
}
