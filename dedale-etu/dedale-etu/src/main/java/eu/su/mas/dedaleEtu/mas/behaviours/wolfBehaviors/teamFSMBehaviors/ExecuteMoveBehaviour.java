package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import jade.core.behaviours.OneShotBehaviour;

public class ExecuteMoveBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1L;
    private WolfAgent wolfAgent;

    public ExecuteMoveBehaviour(WolfAgent a) {
        super(a);
        this.wolfAgent = a;
    }

    @Override
    public void action() {
        System.out.println(this.myAgent.getLocalName() + " - ExecuteMoveBehaviour");
        String nextNode = wolfAgent.getNextNode();
        if (nextNode != null) {
            if (((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNode))) {
                System.out.println(this.myAgent.getLocalName() + " - I move to " + nextNode);
            } else {
                System.out.println(this.myAgent.getLocalName() + " - I failed to move to " + nextNode);
            };
        } else {
            System.out.println("I don't move for this turn");
        }
        try {
            Thread.sleep(1000); // 暂停1秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}