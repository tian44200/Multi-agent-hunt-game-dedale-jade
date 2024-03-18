package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import eu.su.mas.dedaleEtu.mas.knowledge.MapManager;


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
public class ExploBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapManager mapManager;

    /**
     * 
     * @param myagent reference to the agent we are adding this behaviour to
     * @param mapManager manager of known map of the world the agent is living in
     * @param agentNames name of the agents to share the map with
     */
    public ExploBehaviour(final AbstractDedaleAgent myagent, MapManager mapManager) {
        super(myagent);
        this.mapManager=mapManager;
    }

        @Override
        public void action() {

            // if(this.mapManager==null) {
            // 	this.mapManager= new MapRepresentation();
            // 	this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,500,this.mapManager,list_agentNames));
            // }

            //0) Retrieve the current position
            Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

            if (myPosition!=null){
                //List of observable from the agent's current position
                List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
                System.out.println(lobs);
                /**
                 * Just added here to let you see what the agent is doing, otherwise he will be too quick
                 */
                try {
                    this.myAgent.doWait(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //1) remove the current node from openlist and add it to closedNodes.
                this.mapManager.addNode(myPosition.getLocationId(), MapAttribute.closed);

                //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
                String nextNodeId=null;
                Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
                while(iter.hasNext()){
                    Location accessibleNode=iter.next().getLeft();
                    boolean isNewNode=this.mapManager.addNewNode(accessibleNode.getLocationId());
                    //the node may exist, but not necessarily the edge
                    if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
                        this.mapManager.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
                        if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
                    }
                }

                //3) while openNodes is not empty, continues.
                if (!this.mapManager.getMyMap().hasOpenNode()){
                    //Explo finished
                    finished=true;
                    System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
                }else{
                    //4) select next move.
                    //4.1 If there exist one open node directly reachable, go for it,
                    //	 otherwise choose one from the openNode list, compute the shortestPath and go for it
                    if (nextNodeId==null){
                        //no directly accessible openNode
                        //chose one, compute the path and take the first step.
                        nextNodeId=this.mapManager.getMyMap().getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                        //System.out.println(this.myAgent.getLocalName()+"-- list= "+this.mapManager.getOpenNodes()+"| nextNode: "+nextNode);
                    }else {
                        //System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.mapManager.getOpenNodes()+"\n -- nextNode: "+nextNode);
                    }
                    
                    //5) At each time step, the agent check if he received a graph from a teammate. 	
                    // If it was written properly, this sharing action should be in a dedicated behaviour set.
                    MessageTemplate msgTemplate=MessageTemplate.and(
                            MessageTemplate.MatchProtocol("SHARE-TOPO"),
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                    ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
                    if (msgReceived!=null) {
                        SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
                        try {
                            sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
                        } catch (UnreadableException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        this.mapManager.mergeMap(sgreceived);
                    }

                    ((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
                }

            }
        }

	@Override
	public boolean done() {
		return finished;
	}

}
