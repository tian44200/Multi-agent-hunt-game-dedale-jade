package eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors;

import java.io.IOException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.DynamicObjectInfo;
import eu.su.mas.dedaleEtu.mas.knowledge.MapManager;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Serializable;

import java.util.Map;


public class ShareInfoBehaviour extends OneShotBehaviour{
    private static final long serialVersionUID = 5332154701848782301L;
    
    public ShareInfoBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        // 从DataStore获取数据
        ACLMessage msg = (ACLMessage) this.getDataStore().get("msg");
        if (msg.getProtocol().equals("REQUEST-TOPO")) {
            // 获取消息发送者的AID
            AID senderAID = msg.getSender();
            // 创建回复消息(Inform
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.setProtocol("SHARE-TOPO");
            reply.addReceiver(senderAID);
            // System.out.println("senderAID is "+senderAID.getName());
            // System.out.println("Hi I reply ShareBehaviour");
            try {
                // Serialize the map and set it as the content of the message
                SerializableSimpleGraph<String, MapAttribute> sg=((HuntAgent)myAgent).getMapManager().getSerialSubGraphForAgent(senderAID.getName());
                // System.out.println("sg is "+sg.toString());
                if(sg != null) {
                    System.out.println("sg is not null Map sent to "+senderAID.getName());
                    reply.setContentObject(sg);
                    // Send the message
                    // System.out.println("Map is" + sg.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 使用接收到的数据 
        else {
            if (msg.getProtocol().equals("REQUEST-POSITIONS")) {
                // 获取消息发送者的AID
                AID senderAID = msg.getSender();
                // 创建回复消息(Inform
                ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                reply.setProtocol("SHARE-POSITIONS");
                reply.addReceiver(senderAID);
                // System.out.println("senderAID is "+senderAID.getName());
                // System.out.println("Hi I reply ShareBehaviour");
                try {
                    // Serialize the map and set it as the content of the message
                    Map <String, DynamicObjectInfo> info=((HuntAgent)myAgent).getMapManager().getDynamicInfo();
                    // System.out.println("sg is "+sg.toString());
                    if(info != null) {
                        System.out.println("sg is not null Map sent to "+senderAID.getName());
                        reply.setContentObject((Serializable)info);
                        // Send the message
                        // System.out.println("Map is" + sg.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
}
