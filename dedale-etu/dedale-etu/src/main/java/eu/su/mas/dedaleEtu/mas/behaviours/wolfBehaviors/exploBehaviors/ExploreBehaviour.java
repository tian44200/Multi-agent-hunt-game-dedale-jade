package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.exploBehaviors;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.TeamFSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import eu.su.mas.dedaleEtu.mas.knowledge.MapManager;   
import jade.core.AID;


/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - Depth-First Search (DFS) which is computationally consuming because it's not optimised.
 * 
 * When all the nodes around the agent are visited, the agent randomly selects an open node and goes there to restart its DFS. 
 * This (non-optimal) behaviour is done until all nodes are explored. 
 * 
 * Note: This behaviour does not save the content of visited nodes, only the topology.
 * Note: The sub-behaviour ShareMap periodically shares the whole map.
 * 
 * @author Tian Huang, Zhengqing Lin
 */
public class ExploreBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

    private boolean explorefinished = false; // Flag to check if exploration is finished
    private int queryRefCount = 0;  // Counter for the number of queries sent

    private ShareMapBehaviour shareMapBehaviour; // Behaviour to share map
    private MergeMapBehaviour mergeMapBehaviour; // Behaviour to merge map

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
				Thread.sleep(15000);
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
                this.myAgent.removeBehaviour(this.shareMapBehaviour);
                this.myAgent.removeBehaviour(this.mergeMapBehaviour);
                thisAgent.setExplorefinished(true);
                thisAgent.getMapManager().getMyMap().saveInitialGraph();
                // this.myAgent.addBehaviour(new RespondPositionBehaviour(thisAgent));
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
                if (nextNodeId != null) {
                    System.out.println(this.myAgent.getLocalName() + " - Moving to " + nextNodeId);
                    if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
                        System.out.println(this.myAgent.getLocalName() + " - Can't move to " + nextNodeId);
                        thisAgent.setInterblock(true);
                    }
                }
        }}}

    @Override
    public boolean done() {
        return explorefinished;
    }

    private void requestMapInfo() {
        ACLMessage requestMsg = new ACLMessage(ACLMessage.QUERY_REF);
        requestMsg.setProtocol("QUERY-REF-TOPO");
        requestMsg.setContent("Request Count: " + ++queryRefCount); // 
        requestMsg.setSender(this.myAgent.getAID());
        
        for (String agentName : ((WolfAgent)myAgent).getAgentNames()) {
            requestMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
        }
        
        ((AbstractDedaleAgent)this.myAgent).sendMessage(requestMsg);
        System.out.println(LocalDateTime.now() + " - " + myAgent.getLocalName() + " - Sent TOPO request #" + queryRefCount + " to all team members");
    }
    

}
