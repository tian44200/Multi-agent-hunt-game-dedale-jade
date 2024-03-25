package eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapManager;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CheckReceiveBehaviour extends OneShotBehaviour{
    private static final long serialVersionUID = -4763466178164421067L;
    private int state;
    
    public CheckReceiveBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        // 创建消息模板
        MessageTemplate mt1 = MessageTemplate.and(
            MessageTemplate.MatchProtocol("REQUEST-TOPO"),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
        );
        MessageTemplate mt2 = MessageTemplate.and(
            MessageTemplate.MatchProtocol("REQUEST-PARTIAL-TOPO"),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
        );
        MessageTemplate mt3 = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-TOPO"),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
        );

        // 接收消息
        ACLMessage msg = myAgent.receive(mt1);
        if (msg != null) {
            // 如果收到了匹配mt1的消息
            System.out.println(this.myAgent.getLocalName()+" - Received TOPO request from "+msg.getSender().getLocalName());
            this.getDataStore().put("msg", msg);
            this.state = 1;
            return; // 退出方法，不执行后续代码
        }

        msg = myAgent.receive(mt2);
        if (msg != null) {
            // 如果收到了匹配mt2的消息
            this.getDataStore().put("msg", msg);
            this.state = 1;
            return; // 退出方法，不执行后续代码
        }

        msg = myAgent.receive(mt3);
        if (msg != null) {
            // 如果收到了匹配mt2的消息
            SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
                        try {
                            System.out.println(this.myAgent.getLocalName()+" - Content: "+msg.getContentObject());
                            sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msg.getContentObject();
                        } catch (UnreadableException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        ((HuntAgent)myAgent).getMapManager().mergeMap(sgreceived, msg.getSender());
            this.state = 0;
            return; // 退出方法，不执行后续代码
        }
        // 如果没有收到匹配的消息
        this.state = 2;
    }
    @Override
    public int onEnd() {
        return this.state;
    }
}