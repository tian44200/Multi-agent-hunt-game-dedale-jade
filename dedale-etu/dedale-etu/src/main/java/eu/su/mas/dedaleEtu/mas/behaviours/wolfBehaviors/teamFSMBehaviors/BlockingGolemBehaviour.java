package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.sniffer.Message;

public class BlockingGolemBehaviour extends SimpleBehaviour {
    private static final long serialVersionUID = 1L;
    private boolean announcedBlock = false;
    private List<String> list_agentNames = new ArrayList<String>();


	// Constructor
    public BlockingGolemBehaviour(jade.core.Agent a) {
        super(a);
        this.list_agentNames = new ArrayList<>(((WolfAgent)myAgent).getAgentNames());
    }

    @Override
    public void action() {
        if (!announcedBlock){
            System.out.println("I have blocked the Golem and finished my work.");
            announcedBlock = true;
        }
        block();
        // MessageTemplate mt_req = MessageTemplate.and(
        //     MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
        //     MessageTemplate.MatchProtocol("ConnectionRequest")
        // );
        // ACLMessage request = myAgent.receive(mt_req);
        // if (request != null && this.list_agentNames.contains(request.getSender().getLocalName())){
        //     ACLMessage reply = request.createReply();
        //     reply.setPerformative(ACLMessage.INFORM);
        //     reply.setProtocol("DisableSmell");
        //     reply.setSender(this.myAgent.getAID());
        //     reply.addReceiver(request.getSender());
        //     try {
        //         reply.setContentObject(((WolfAgent)this.myAgent).getMapManager().getObservationMap().getSerializableGraph());
        //     } catch (IOException e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }
        //     ((AbstractDedaleAgent)myAgent).sendMessage(reply);
        // }else
        // {
        //     block();
        // }
        // MessageTemplate mt_confirm = MessageTemplate.and(
        //     MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
        //     MessageTemplate.MatchProtocol("DisableSmell")
        // );
        // ACLMessage confirm = myAgent.receive(mt_confirm);
        // while (confirm != null){
        //     System.out.println(this.myAgent.getLocalName()+ "I have received the confirmation from "+ confirm.getSender().getLocalName());
        //     this.list_agentNames.remove(confirm.getSender().getLocalName());
        //     System.out.println(this.myAgent.getLocalName()+"The list of agents is "+ this.list_agentNames);
        //     confirm = myAgent.receive(mt_confirm);
        // }
    }

	@Override
	public boolean done() {
		return false;
	}
}