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

        // get all agent name and add as receivers
        for (String agentName : ((WolfAgent)myAgent).getAgentNames()) {
            connRequest.addReceiver(new AID(agentName, AID.ISLOCALNAME));
        }

        // add timestamp
        connRequest.addUserDefinedParameter("timestamp", String.valueOf(System.currentTimeMillis()));
        ((AbstractDedaleAgent)this.myAgent).sendMessage(connRequest);

        System.out.println(myAgent.getLocalName() + " - Sent Connection request to all other agents with timestamp.");
    }
}
