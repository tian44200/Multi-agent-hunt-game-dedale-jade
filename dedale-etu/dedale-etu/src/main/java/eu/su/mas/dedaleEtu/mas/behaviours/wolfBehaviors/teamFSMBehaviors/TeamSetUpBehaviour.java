package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedale.env.Location;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class TeamSetUpBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = -8548653925442367156L;

    public TeamSetUpBehaviour(WolfAgent a) {
        super(a);
    }

    @Override
    public void action() {
        MapRepresentation connectionsGraph = ((WolfAgent) myAgent).getMapManager().getCommunicationMap();
        connectionsGraph.setMaxHashChef();
        if (((WolfAgent) myAgent).isChef()) {
            System.out.println(myAgent.getLocalName() + " is the chef.");
        } else {
            System.out.println(myAgent.getLocalName() + " is not the chef.");
            String parent = connectionsGraph.getShortestPathToChef(((WolfAgent) myAgent).getMyPositionID()).get(0);; // 假设这个方法决定了父节点
            if (parent != null) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID(parent, AID.ISLOCALNAME));
                msg.setProtocol("ParentConfirmation");
                msg.setSender(this.myAgent.getAID());
                ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
                System.out.println(myAgent.getLocalName() + " informs " + parent + " as the parent.");
                ((WolfAgent) myAgent).setParent(parent);
            }
        }        
        try {
            Thread.sleep(1000); // 暂停1秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
