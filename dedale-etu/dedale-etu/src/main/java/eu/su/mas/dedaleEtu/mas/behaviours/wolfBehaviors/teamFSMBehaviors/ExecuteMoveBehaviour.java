package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import jade.core.behaviours.OneShotBehaviour;

public class ExecuteMoveBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1L;
    private WolfAgent wolfAgent;
    private int onEndValue = 0;

    public ExecuteMoveBehaviour(WolfAgent a) {
        super(a);
        this.wolfAgent = a;
    }

    @Override
    public void action() {
        System.out.println(this.myAgent.getLocalName() + " - ExecuteMoveBehaviour");
        String nextNode = wolfAgent.getNextNode();
        if (nextNode != null) {
            if (nextNode.equals("block")) {
                onEndValue = 1;
                System.out.println(this.myAgent.getLocalName() + " - I block golem.");
                return;
            }
            if (nextNode.equals(wolfAgent.getMyPositionID())) {
                System.out.println(this.myAgent.getLocalName() + " - I'm already at my target");
                return;
            }
            if (((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNode))) {
                System.out.println(this.myAgent.getLocalName() + " - I move to " + nextNode);
            } else {
                wolfAgent.setblockUnknownPos(nextNode);
                // if (wolfAgent.getSuspectPos() == null){
                //     wolfAgent.setSuspectPos(nextNode);
                //     wolfAgent.setblockUnknownPos(null);
                // }else{
                    
                //     wolfAgent.setSuspectPos(null);
                // }
                System.out.println(this.myAgent.getLocalName() + " - I failed to move to " + nextNode);
            };
        } else {
            System.out.println("I don't move for this turn");
        }
    }

    @Override
    public int onEnd() {
        return onEndValue;
    }

}