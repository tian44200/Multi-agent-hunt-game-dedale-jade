package eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors;

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
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.HuntAgent.Mode;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
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
public class WalkBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

    private int exitValue = 1;

    /**
     * 
     * @param myagent reference to the agent we are adding this behaviour to
     */
    public WalkBehaviour(final HuntAgent myagent) {
        super(myagent);
    }

    @Override
    public void action() {
        if(((HuntAgent)myAgent).getMapManager()==null) {
            ((HuntAgent)myAgent).setMapManager(new MapManager(new MapRepresentation(), ((HuntAgent)myAgent).getAgentNames()));
        }

        //0) Retrieve the current position
        Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        if (myPosition!=null){
            //List of observable from the agent's current position
            List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
            // System.out.println(lobs);
            /**
             * Just added here to let you see what the agent is doing, otherwise he will be too quick
             */
            try {
                // ((SkilledChaseAgent)this.myAgent).setshouldpause(true);
                this.myAgent.doWait(1000);
                Thread.sleep(1000);
                // ((SkilledChaseAgent)this.myAgent).setshouldpause(false);
                System.out.println(LocalDateTime.now()+ this.myAgent.getLocalName()+"WAITED FOR 1s - Position: "+myPosition);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(LocalDateTime.now()+ this.myAgent.getLocalName()+" - Error in Wait");
            }
            //1) remove the current node from openlist and add it to closedNodes.
            ((HuntAgent)myAgent).getMapManager().addNode(myPosition.getLocationId(), MapAttribute.closed);

            //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
            String nextNodeId=null;
            Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
            while(iter.hasNext()){
                Location accessibleNode=iter.next().getLeft();
                boolean isNewNode=((HuntAgent)myAgent).getMapManager().addNewNode(accessibleNode.getLocationId());
                //the node may exist, but not necessarily the edge
                if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
                    ((HuntAgent)myAgent).getMapManager().addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
                    if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
                }
            }

            //3) while openNodes is not empty, continues.
            if (!((HuntAgent)myAgent).getMapManager().getMyMap().hasOpenNode()){
                
                //Explo finished
                // System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
            }else{
                //4) select next move.
                //4.1 If there exist one open node directly reachable, go for it,
                //	 otherwise choose one from the openNode list, compute the shortestPath and go for it
                if (nextNodeId==null){
                    //no directly accessible openNode
                    //chose one, compute the path and take the first step.
                    nextNodeId=((HuntAgent)myAgent).getMapManager().getMyMap().getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                    //System.out.println(this.myAgent.getLocalName()+"-- list= "+((HuntAgent)myAgent).getMapManager().getOpenNodes()+"| nextNode: "+nextNode);
                }else {
                    //System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+((HuntAgent)myAgent).getMapManager().getOpenNodes()+"\n -- nextNode: "+nextNode);
                }
                System.out.println(this.myAgent.getLocalName()+" - Next node is "+nextNodeId);
                // System.out.println(this.myAgent.getLocalName()+" - Next node is "+nextNodeId);
                //5) At each time step, the agent check if he received a graph from a teammate. 	
                // If it was written properly, this sharing action should be in a dedicated behaviour set.
                // // System.out.println(this.myAgent.getLocalName()+" - Map requested to "+list_agentNames);
                if (((HuntAgent)myAgent).getMode()==Mode.goaside){
                    List<String> neighborNodes = ((HuntAgent)myAgent).getMapManager().getMyMap().getNeighborNodes(myPosition.getLocationId());
                    System.out.println(this.myAgent.getLocalName()+" - Neighbor nodes are "+ neighborNodes);
                    Random rand = new Random();
                    nextNodeId = neighborNodes.get(rand.nextInt(neighborNodes.size()));
                    ((HuntAgent)myAgent).setMode(Mode.explore);
                }
                if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
                    System.out.println(this.myAgent.getLocalName() + " - Can't move to " + nextNodeId);
                    ((HuntAgent)myAgent).setMode(Mode.goaside); 
                }
            }

        }
    }

    @Override
    public int onEnd() {
        return exitValue;
    }

}
