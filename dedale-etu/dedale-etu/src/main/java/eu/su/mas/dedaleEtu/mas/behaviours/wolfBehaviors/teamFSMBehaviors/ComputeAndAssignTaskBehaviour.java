package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.OneShotBehaviour;
import javafx.util.Pair;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;

public class ComputeAndAssignTaskBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 5453252789469578026L;
    private WolfAgent wolfAgent;

    public ComputeAndAssignTaskBehaviour(WolfAgent a) {
        super(a);
        this.wolfAgent = a;
    }

    @Override
    public void action() {
        System.out.println(this.myAgent.getLocalName() + " - ComputeAndAssignTaskBehaviour");
        if (wolfAgent.getMapManager().getDirectCommunications().isEmpty()) {
            if (wolfAgent.getTargetNode() != null) {
                List<String> path = wolfAgent.getMapManager().getMyMap().getShortestPath(wolfAgent.getMyPositionID(), wolfAgent.getTargetNode());
                if (!path.isEmpty()) {
                    wolfAgent.setNextNode(path.get(0));
                } else {
                    wolfAgent.setNextNode(null);
                }
            } else {
                computeTargetNodes();
            }
        } else {
            Map<String, Pair<String, String>> agentTargetNodes = computeTargetNodes();
            // 步骤3: 把剩下的Map发给自己的孩子们
            for (String child : wolfAgent.getChildren()) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setProtocol("Task-Distribution-Protocol"); // 设置协议
                msg.addReceiver(new AID(child, AID.ISLOCALNAME));
                msg.setSender(this.myAgent.getAID());
                try {
                    msg.setContentObject((Serializable) agentTargetNodes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
            }
        } 
        try {
            Thread.sleep(3000); // 暂停1秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private Map<String, Pair<String, String>> computeTargetNodes() {
        Map<String, Pair<String, String>> agentTargetNodes = wolfAgent.getMapManager().computeTargetAndNextNodeForAgent();
    
        System.out.println(wolfAgent.getMapManager().getObservationMap().getSerializableGraph().toString());
        System.out.println(wolfAgent.getMapManager().getMyMap().getSerializableGraph().toString());
        System.out.println(this.myAgent.getLocalName() + " - All target and priority: " + agentTargetNodes.toString());
        System.out.println(this.myAgent.getLocalName() + " - My target and priority: " + agentTargetNodes.get(wolfAgent.getMyPositionID()));
        System.out.println("My position ID is "+ wolfAgent.getMyPositionID());
    
        Pair<String, String> myTargetAndPriority = agentTargetNodes.remove(wolfAgent.getMyPositionID());
        if (myTargetAndPriority != null) {
            wolfAgent.setTargetAndNextNode(myTargetAndPriority);
        } else {
            throw new RuntimeException("myTargetAndPriority is null");
        }
        return agentTargetNodes;
    }
}