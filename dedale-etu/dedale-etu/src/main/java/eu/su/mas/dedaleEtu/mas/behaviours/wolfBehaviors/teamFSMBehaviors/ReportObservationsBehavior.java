package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashSet;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashSet;
import java.util.Set;

public class ReportObservationsBehavior extends Behaviour {
    private static final long serialVersionUID = 1L;
    private final Set<String> receivedChildren = new HashSet<>();
    private final Set<String> expectedChildren;
    private final WolfAgent wolfAgent;
    private boolean finished = false;
    private int onEndReturnValue = 0; // 默认为0

    public ReportObservationsBehavior(WolfAgent agent) {
        super(agent);
        this.wolfAgent = agent;
        this.expectedChildren = new HashSet<>();
        if (agent.getChildren() != null) {
            this.expectedChildren.addAll(agent.getChildren());
        }
         // 假设 agent.getChildren() 返回所有子节点的名称列表
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchProtocol("ObservationData");
        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            String childName = msg.getSender().getLocalName();
            if (!receivedChildren.contains(childName) && expectedChildren.contains(childName)) {
                receivedChildren.add(childName);
                // 更新地图管理器中的臭气节点信息
                SerializableSimpleGraph<String, MapAttribute> sgreceived = null;
                try {
                    sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msg.getContentObject();
                    // 更新地图信息
                    wolfAgent.getMapManager().getObservationMap().mergeMap(sgreceived);
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
        }

        // 检查是否收到了所有预期子节点的消息
        if (!finished && receivedChildren.containsAll(expectedChildren)) {
            // 如果是Chef，设置特殊的返回值
            if (wolfAgent.isChef()) {
                onEndReturnValue = 1;
                wolfAgent.getMapManager().getMyMap().mergeMap(wolfAgent.getMapManager().getObservationMap().getSerializableGraph());
            }
            else{
                // 准备发送累积的臭气节点ID给父节点
                sendObservationsToParent();
            }
            finished = true; // 标记行为完成
            try {
                Thread.sleep(1000); // 暂停1秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private void sendObservationsToParent() {
        String parent = wolfAgent.getParent();
        if (parent != null) {
            ACLMessage obsMsg = new ACLMessage(ACLMessage.INFORM);
            obsMsg.addReceiver(new AID(parent, AID.ISLOCALNAME));
            obsMsg.setProtocol("ObservationData");
            obsMsg.setSender(this.myAgent.getAID());
            // 将地图信息转换为可序列化对象发送
            SerializableSimpleGraph<String, MapAttribute> sg=wolfAgent.getMapManager().getObservationMap().getSerializableGraph();
            try {
                obsMsg.setContentObject(sg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ((AbstractDedaleAgent)this.myAgent).sendMessage(obsMsg);
        }
    }

    @Override
    public boolean done() {
        return finished;
    }

    @Override
    public int onEnd() {
        return onEndReturnValue; // 返回0或1，取决于是否是Chef
    }
}
