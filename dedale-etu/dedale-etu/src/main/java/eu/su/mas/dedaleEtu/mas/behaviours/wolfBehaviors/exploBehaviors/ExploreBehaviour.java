package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.exploBehaviors;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent.Mode;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.TeamFSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import weka.core.pmml.jaxbbindings.True;
import eu.su.mas.dedaleEtu.mas.knowledge.MapManager;   
import jade.core.AID;


/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class ExploreBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

    private boolean explorefinished = false;
    private int queryRefCount = 0;  // 消息发送计数器

    private ShareMapBehaviour shareMapBehaviour;
    private MergeMapBehaviour mergeMapBehaviour;

    /**
     * 
     * @param myagent reference to the agent we are adding this behaviour to
     */
    public ExploreBehaviour(final WolfAgent myagent) {
        super(myagent);
    }

    @Override
    public void action() {
        WolfAgent thisAgent = ((WolfAgent)myAgent);
        if(thisAgent.getMapManager()==null) {
            thisAgent.setMapManager(new MapManager(new MapRepresentation(), thisAgent.getAgentNames()));
            this.shareMapBehaviour = new ShareMapBehaviour(thisAgent);
            this.mergeMapBehaviour = new MergeMapBehaviour(thisAgent);
            thisAgent.addBehaviour(this.shareMapBehaviour);
            thisAgent.addBehaviour(this.mergeMapBehaviour);
            try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        //0) Retrieve the current position
        Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        thisAgent.setMyPositionID(myPosition.getLocationId());
        if (myPosition!=null){
            //List of observable from the agent's current position
            List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
            // System.out.println(lobs);
            /**
             * Just added here to let you see what the agent is doing, otherwise he will be too quick
             */
            requestMapInfo();
            try {
                // ((SkilledChaseAgent)this.myAgent).setshouldpause(true);
                this.myAgent.doWait(1000);
                // Thread.sleep(1000);
                // ((SkilledChaseAgent)this.myAgent).setshouldpause(false);
                System.out.println(LocalDateTime.now()+ this.myAgent.getLocalName()+"WAITED FOR 1s - Position: "+myPosition);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(LocalDateTime.now()+ this.myAgent.getLocalName()+" - Error in Wait");
            }
            //1) remove the current node from openlist and add it to closedNodes.
            thisAgent.getMapManager().addNode(myPosition.getLocationId(), MapAttribute.closed);

            //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
            String nextNodeId=null;
            Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
            while(iter.hasNext()){
                Location accessibleNode=iter.next().getLeft();
                boolean isNewNode=thisAgent.getMapManager().addNewNode(accessibleNode.getLocationId());
                //the node may exist, but not necessarily the edge
                if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
                    thisAgent.getMapManager().addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
                    if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
                }
            }
            //3) while openNodes is not empty, continues Exploring. Else, go back to base
            if (!thisAgent.getMapManager().getMyMap().hasOpenNode()){
                this.explorefinished=true;
                thisAgent.getMapManager().getMyMap().setExploreFinishedTrue();
                // this.myAgent.removeBehaviour(this.shareMapBehaviour);
                this.myAgent.removeBehaviour(this.mergeMapBehaviour);
                thisAgent.setExplorefinished(true);
                thisAgent.getMapManager().getMyMap().saveInitialGraph();
                this.myAgent.addBehaviour(new TeamFSMBehaviour(thisAgent));
                //Explo finished
                System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
                System.out.println(this.myAgent.getLocalName()+" - Map: "+thisAgent.getMapManager().getMyMap().toString());
                this.myAgent.removeBehaviour(this);
            }else{
                //4) select next move.
                //4.1 If there exist one open node directly reachable, go for it,
                //	 otherwise choose one from the openNode list, compute the shortestPath and go for it
                if (nextNodeId==null){
                    //no directly accessible openNode
                    //chose one, compute the path and take the first step.
                    nextNodeId=thisAgent.getMapManager().getMyMap().getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                    //System.out.println(this.myAgent.getLocalName()+"-- list= "+thisAgent.getMapManager().getOpenNodes()+"| nextNode: "+nextNode);
                }else {
                    //System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+thisAgent.getMapManager().getOpenNodes()+"\n -- nextNode: "+nextNode);
                }
                System.out.println(this.myAgent.getLocalName()+" - Next node is "+nextNodeId);
                // System.out.println(this.myAgent.getLocalName()+" - Next node is "+nextNodeId);
                //5) At each time step, the agent check if he received a graph from a teammate. 	
                // If it was written properly, this sharing action should be in a dedicated behaviour set.
                // // System.out.println(this.myAgent.getLocalName()+" - Map requested to "+list_agentNames);
                
                if (thisAgent.isInterblock()){
                    List<String> neighborNodes = thisAgent.getMapManager().getMyMap().getNeighborNodes(myPosition.getLocationId());
                    System.out.println(this.myAgent.getLocalName()+" - Neighbor nodes are "+ neighborNodes);
                    Random rand = new Random();
                    nextNodeId = neighborNodes.get(rand.nextInt(neighborNodes.size()));
                    thisAgent.setInterblock(false);
                }
                // if (nextNodeId != null) {
                //     System.out.println(this.myAgent.getLocalName() + " - Moving to " + nextNodeId);
                //     if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
                //         System.out.println(this.myAgent.getLocalName() + " - Can't move to " + nextNodeId);
                //         thisAgent.setInterblock(true);
                //     }
                // }
        }}}

    @Override
    public boolean done() {
        return explorefinished;
    }

    private void requestMapInfo() {
        ACLMessage requestMsg = new ACLMessage(ACLMessage.QUERY_REF);
        requestMsg.setProtocol("QUERY-REF-TOPO");
        requestMsg.setContent("Request Count: " + ++queryRefCount); // 预先增加计数器
        
        // 添加所有代理作为接收者
        for (String agentName : ((WolfAgent)myAgent).getAgentNames()) {
            requestMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
        }
        
        myAgent.send(requestMsg);
        queryRefCount++;  // 增加消息计数
        System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - Sent TOPO request #" + queryRefCount + " to all team members");
    }
    

}
