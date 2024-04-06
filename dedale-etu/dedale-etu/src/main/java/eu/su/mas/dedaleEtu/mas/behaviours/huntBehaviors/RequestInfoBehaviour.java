package eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent.Mode;

public class RequestInfoBehaviour extends OneShotBehaviour {

    private static final long serialVersionUID = 7938191979589165031L;

    public RequestInfoBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        HuntAgent thisAgent = (HuntAgent)myAgent;
        // 创建消息
        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
        if (thisAgent.getMode() == Mode.explore){
            msg.setProtocol("QUERY-REF-POSITIONS");
            msg.setProtocol("QUERY-REF-TOPO");
            // 将消息发送给每个Agent
            for (String agentName : ((HuntAgent)myAgent).getAgentNames()) {
                msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
            }
    
            // 发送消息
            myAgent.send(msg);
        }
        else if (thisAgent.getMode() == Mode.waiting){
            msg.setProtocol("QUERY-REF-POSITIONS");
            msg.setProtocol("QUERY-REF-TOPO");
            // 将消息发送给每个Agent
            for (String agentName : ((HuntAgent)myAgent).getAgentNames()) {
                msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
            }
    
            // 发送消息
            myAgent.send(msg);
        }

    }
}