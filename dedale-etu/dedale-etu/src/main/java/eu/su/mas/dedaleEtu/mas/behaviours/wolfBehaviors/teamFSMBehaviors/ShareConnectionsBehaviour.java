package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class ShareConnectionsBehaviour extends Behaviour {
    private static final long serialVersionUID = -3424809172703662346L;
    private long startTime;
    private long duration; // duration in milliseconds

    public ShareConnectionsBehaviour(WolfAgent agent, long timeout) {
        super(agent);
        this.startTime = System.currentTimeMillis();
        this.duration = timeout;

        // 获取当前智能体的通信图
        MapRepresentation communicationMap = ((WolfAgent) myAgent).getMapManager().getCommunicationMap();

        // 获取直接通信的智能体列表
        List<String> directCommunications = ((WolfAgent) myAgent).getMapManager().getDirectCommunications();

        // 创建一个新的ACL消息
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("ShareCommunicationMap");
        try {
            msg.setContentObject(communicationMap.getSerializableGraph());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 将消息发送给直接通信的智能体
        for (String agentName : directCommunications) {
            msg.addReceiver(((WolfAgent) myAgent).getAID(agentName));
        }
        myAgent.send(msg);
    }

    @Override
    public void action() {
        // Check if the duration has passed
        if (System.currentTimeMillis() - startTime > duration) {
            return;
        }

        // Receive connection info
        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            SerializableSimpleGraph<String, MapAttribute> receivedMap = null;
            try {
                // Get the received map
                receivedMap = (SerializableSimpleGraph<String, MapAttribute>) msg.getContentObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Merge the received map and get the difference
            SerializableSimpleGraph<String, MapAttribute> diffMap = ((WolfAgent) myAgent).getMapManager().getObservationMap().mergeMapAndGetDifference(receivedMap);

            // If the difference map is not empty, share it with all other agents
            if (diffMap != null && !diffMap.getAllNodes().isEmpty()) {
                List<String> directCommunications = ((WolfAgent) myAgent).getMapManager().getDirectCommunications();

                // Create a new ACL message
                ACLMessage diffMsg = new ACLMessage(ACLMessage.INFORM);
                diffMsg.setProtocol("ShareCommunicationMap");
                try {
                    diffMsg.setContentObject(diffMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Send the message to all other agents
                for (String agentName : directCommunications) {
                    if (!agentName.equals(msg.getSender().getName())) {
                        diffMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
                    }
                }
                myAgent.send(diffMsg);
                try {
                    Thread.sleep(1000); // 暂停1秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean done() {
        // The behaviour is done when the duration has passed
        System.out.println("ShareConnectionsBehaviour done");
        return System.currentTimeMillis() - startTime > duration;
    }
}