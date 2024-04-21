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

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;

public class WaitMissionBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1L;
    private WolfAgent wolfAgent;
    private long timeout;

    public WaitMissionBehaviour(WolfAgent a, long timeout) {
        super(a);
        this.wolfAgent = a;
        this.timeout = timeout;
    }

    @Override
    public void action() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout;
        boolean received = false;

        System.out.println(this.myAgent.getLocalName() + " - Waiting for mission");
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchProtocol("Task-Distribution-Protocol")),MessageTemplate.MatchSender(new AID(wolfAgent.getParent(), AID.ISLOCALNAME)));
        while (System.currentTimeMillis() < endTime && !received) {
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null && msg.getSender().getLocalName().equals(wolfAgent.getParent())) {
                received = true;
                try {
                    Map<String, Pair<String, String>> agentTargets = (Map<String, Pair<String, String>>) msg.getContentObject();
                    Pair<String, String> myTargetAndPriority = agentTargets.get(wolfAgent.getMyPositionID());
                    wolfAgent.setTargetAndNextNode(myTargetAndPriority);
                    if (myTargetAndPriority != null && myTargetAndPriority.getValue() != "block"){
                        System.out.println(this.myAgent.getLocalName() + " - Received mission from " + msg.getSender().getLocalName() + " with target " + myTargetAndPriority.getKey());
                        
                    }
                    System.out.println(myAgent.getLocalName() + " - Received mission from " + msg.getSender().getLocalName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!received) {
                block(10); // 短暂休眠避免CPU过载
            }
        }

        if (!received) {
            System.out.println(myAgent.getLocalName() + " - Timeout without receiving mission");
        }
    }
}
