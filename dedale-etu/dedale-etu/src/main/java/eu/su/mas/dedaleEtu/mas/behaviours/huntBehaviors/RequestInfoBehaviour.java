package eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent;

public class RequestInfoBehaviour extends OneShotBehaviour {

    public RequestInfoBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        // 创建消息
        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
        msg.setProtocol("QUERY-REF-TOPO");
        // 将消息发送给每个Agent
        for (String agentName : ((HuntAgent)myAgent).getAgentNames()) {
            msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
        }

        // 发送消息
        myAgent.send(msg);
    }
}