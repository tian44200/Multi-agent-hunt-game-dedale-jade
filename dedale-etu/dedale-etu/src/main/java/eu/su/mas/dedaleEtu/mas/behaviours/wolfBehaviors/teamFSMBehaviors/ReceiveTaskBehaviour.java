package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javafx.util.Pair;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;

public class ReceiveTaskBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1L;
    private WolfAgent wolfAgent;

    public ReceiveTaskBehaviour(WolfAgent a) {
        super(a);
        this.wolfAgent = a;
    }

    @Override
    public void action() {
        System.out.println(this.myAgent.getLocalName() + " - ReceiveTaskBehaviour");
        MessageTemplate mt = MessageTemplate.MatchProtocol("Task-Distribution-Protocol");
        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            if (msg.getSender().getLocalName().equals(wolfAgent.getParent())) {
                try {
                    Map<String, Pair<String, String>> agentTargets = (Map<String, Pair<String, String>>) msg.getContentObject();

                    // 步骤1: 取出自己的target和priority并设置
                    Pair<String, String> myTargetAndPriority = agentTargets.remove(wolfAgent.getMyPositionID());
                    wolfAgent.setTargetAndNextNode(myTargetAndPriority);
                    
                    // 步骤2: 把剩下的Map发给自己的孩子们
                    for (String child : wolfAgent.getChildren()) {
                        ACLMessage forwardMsg = new ACLMessage(ACLMessage.INFORM);
                        forwardMsg.setProtocol("Task-Distribution-Protocol");
                        forwardMsg.addReceiver(new AID(child, AID.ISLOCALNAME));
                        forwardMsg.setSender(this.myAgent.getAID());
                        try {
                            forwardMsg.setContentObject((Serializable) agentTargets);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ((AbstractDedaleAgent)this.myAgent).sendMessage(forwardMsg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000); // 暂停1秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            block();
        }
    }
}