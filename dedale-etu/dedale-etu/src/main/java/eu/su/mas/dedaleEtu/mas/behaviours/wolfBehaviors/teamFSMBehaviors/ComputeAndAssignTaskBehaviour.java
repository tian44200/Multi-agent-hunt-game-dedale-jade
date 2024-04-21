package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.OneShotBehaviour;
import javafx.util.Pair;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;

public class ComputeAndAssignTaskBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 5453252789469578026L;
    private WolfAgent wolfAgent;
    private int onEndValue = 0;

    public ComputeAndAssignTaskBehaviour(WolfAgent a) {
        super(a);
        this.wolfAgent = a;
    }

    @Override
    public void action() {
        System.out.println(this.myAgent.getLocalName() + " - ComputeAndAssignTaskBehaviour");
        if (wolfAgent.isDisband()) {
            System.out.println(this.myAgent.getLocalName() + " - Disbanding");
            if (wolfAgent.getMapManager().getObservationMap().hasGolemNodeOnMap()){
                System.out.println(this.myAgent.getLocalName() + " - Disbanding with golem");
                Map<String, Pair<String, String>> agentTargetNodes = wolfAgent.getMapManager().computeTargetAndNextNodeForAgent();
                Pair<String, String> myTargetAndPriority = agentTargetNodes.get(wolfAgent.getMyPositionID());                
                if (myTargetAndPriority != null) {
                    wolfAgent.setTargetAndNextNode(myTargetAndPriority);
                } 
                wolfAgent.setDisband(false);
                return;
            }
            String nextNode = wolfAgent.getNextNode();
            if (nextNode == null) { // cant be farer
                wolfAgent.setTargetAndNextNode(null);
                wolfAgent.setDisband(false);
                return;
            }
            wolfAgent.setNextNode(nextNode);
            return;
        }
        if (!wolfAgent.hasChildren() && wolfAgent.getTargetNode() != null) {
            System.out.println(this.myAgent.getLocalName() + " - I'm alone and I have a target");
            List<String> path = wolfAgent.getMapManager().getMyMap().getShortestPath(wolfAgent.getMyPositionID(), wolfAgent.getTargetNode());
            if (!path.isEmpty()) {
                wolfAgent.setNextNode(path.get(0));
            } else {
                wolfAgent.setNextNode(null);
            }
            return;
        }
        if (wolfAgent.getMapManager().getMyMap().areGolemsBlocked() && wolfAgent.getVerifyGolem() == false){
            System.out.println(this.myAgent.getLocalName() + " - Golem is blocked");
            onEndValue = 1;
            wolfAgent.setVerifyGolem(true);
            return;
        }

        Map<String, Pair<String, String>> agentTargetNodes = wolfAgent.getMapManager().computeTargetAndNextNodeForAgent();
        System.out.println(wolfAgent.getMapManager().getObservationMap().getSerializableGraph().toString());
        System.out.println(wolfAgent.getMapManager().getMyMap().getSerializableGraph().toString());
        System.out.println(this.myAgent.getLocalName() + " - All target and next node: " + agentTargetNodes.toString());
        System.out.println(this.myAgent.getLocalName() + " - My target and next node: " + agentTargetNodes.get(wolfAgent.getMyPositionID()));
        System.out.println("My position ID is "+ wolfAgent.getMyPositionID());
        Pair<String, String> myTargetAndPriority = agentTargetNodes.get(wolfAgent.getMyPositionID());        
        wolfAgent.setTargetAndNextNode(myTargetAndPriority);
        if (agentTargetNodes.size() ==1){
            return;
        }

        System.out.println(this.myAgent.getLocalName() + " - I send mission to children" + wolfAgent.getChildren().toString());
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
        if (wolfAgent.getMapManager().getMyMap().areGolemsBlocked()){
            for (String child : wolfAgent.getChildren()) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setProtocol("block-map"); // 设置协议
                msg.addReceiver(new AID(child, AID.ISLOCALNAME));
                msg.setSender(this.myAgent.getAID());
                try {
                    List<String> list = new ArrayList<>(agentTargetNodes.keySet());
                    msg.setContentObject(((WolfAgent)this.myAgent).getMapManager().getBlockObservationGraph(list));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
            }
        }
        wolfAgent.setblockUnknownPos(null);
        wolfAgent.setVerifyGolem(false);
    }

    @Override
    public int onEnd() {
        return onEndValue;
    }
}