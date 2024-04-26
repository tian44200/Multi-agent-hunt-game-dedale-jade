package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.OneShotBehaviour;
import javafx.util.Pair;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

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
        if (!wolfAgent.hasChildren() && wolfAgent.getTargetNode() != null) {
            System.out.println(this.myAgent.getLocalName() + " - I'm alone and I have a target");
            if (wolfAgent.getMapManager().getMyMap().hasNode(wolfAgent.getTargetNode())){
                List<String> path = wolfAgent.getMapManager().getMyMap().getShortestPath(wolfAgent.getMyPositionID(), wolfAgent.getTargetNode());
                if (!path.isEmpty()) {
                    wolfAgent.setNextNode(path.get(0));
                } else {
                    wolfAgent.setNextNode(null);
                }
            }
            else{
                return;
            }
        } 
        if (wolfAgent.getMapManager().getMyMap().areGolemsBlocked() && wolfAgent.getVerifyGolem() == false){
            System.out.println(this.myAgent.getLocalName() + " - Golem is blocked");
            this.onEndValue = 1;
            wolfAgent.setVerifyGolem(true);
            return;
        }
        this.onEndValue = 0;
        Map<String, Pair<String, String>> agentTargetNodes = wolfAgent.getMapManager().computeTargetAndNextNodeForAgent();
        System.out.println(wolfAgent.getMapManager().getObservationMap().getSerializableGraph().toString());
        System.out.println(wolfAgent.getMapManager().getMyMap().getSerializableGraph().toString());
        System.out.println(this.myAgent.getLocalName() + " - All target and next node: " + agentTargetNodes.toString());
        System.out.println(this.myAgent.getLocalName() + " - My target and next node: " + agentTargetNodes.get(wolfAgent.getMyPositionID()));
        System.out.println("My position ID is "+ wolfAgent.getMyPositionID());
        Pair<String, String> myTargetAndPriority = agentTargetNodes.get(wolfAgent.getMyPositionID());        
        wolfAgent.setTargetAndNextNode(myTargetAndPriority);
        if (wolfAgent.getNextNode() == "block"){
            wolfAgent.getMapManager().getObservationMap().clearMap();
            Set<String> block_agents = agentTargetNodes.keySet();
            for (String agent : block_agents){
                if (agentTargetNodes.get(agent).getValue().equals("block")){
                    wolfAgent.getMapManager().getObservationMap().addNode(agent, MapAttribute.block);
                }
            }
        }
        if (agentTargetNodes.size() ==0){
            return;
        }
        System.out.println(this.myAgent.getLocalName() + " - I send mission to children" + wolfAgent.getChildren().toString());
        for (String child : wolfAgent.getChildren()) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setProtocol("Task-Distribution-Protocol"); 
            msg.addReceiver(new AID(child, AID.ISLOCALNAME));
            msg.setSender(this.myAgent.getAID());
            msg.addUserDefinedParameter("timestamp", String.valueOf(System.currentTimeMillis()));
            try {
                msg.setContentObject((Serializable) agentTargetNodes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
        }
        wolfAgent.setblockUnknownPos(null);
        wolfAgent.setVerifyGolem(false);
    }

    @Override
    public int onEnd() {
        return onEndValue;
    }
}