package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.exploBehaviors.ExploreBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapManager;
import javafx.util.Pair;
import jade.core.behaviours.Behaviour;

/**
 * <pre>
 * ExploreCoop agent. 
 * Basic example of how to "collaboratively" explore the map
 *  - It explore the map using a DFS algorithm and blindly tries to share the topology with the agents within reach.
 *  - The shortestPath computation is not optimized
 *  - Agents do not coordinate themselves on the node(s) to visit, thus progressively creating a single file. It's bad.
 *  - The agent sends all its map, periodically, forever. Its bad x3.
 *  - You should give him the list of agents'name to send its map to in parameter when creating the agent.
 *   Object [] entityParameters={"Name1","Name2};
 *   ag=createNewDedaleAgent(c, agentName, ExploreCoopAgent.class.getName(), entityParameters);
 *  
 * It stops when all nodes have been visited.
 * 
 * 
 *  </pre>
 *  
 * @author hc
 *
 */


public class WolfAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private MapManager mapManager;
	private boolean interblock = false;
	private boolean inTeamMode = false;
	private List<String> list_agentNames;
	private boolean explorefinished = false;
	private String parent;
    private List<String> childrenIDs;
	private boolean receivedConnectionResponses = false;
	private String myPositionID;
	private String targetNode;
	private String nextNode;

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		//get the parameters added to the agent at creation (if any)
		final Object[] args = getArguments();
		
		List<String> list_agentNames=new ArrayList<String>();

		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
			this.list_agentNames = list_agentNames;
		}
		System.out.println("hi here");
		System.out.println(this);
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		// Listeners
		// lb.add(new ShareMapBehaviour(this));
		// lb.add(new MergeMapBehaviour(this));

		// Behaviours
		// Start by Exploring then switch to TeamBehaviour(FSMBehaviour)
		lb.add(new ExploreBehaviour(this));

		// System.out.println("hi I shared");		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	protected void beforeMove(){ 
		super.beforeMove(); 
		//terminate what is computer-specific
		if (this.mapManager != null){
			this.mapManager.prepareMigration();
		}
			
	   }

	protected void afterMove(){ 
		super.afterMove();
		//restart what is computer-specific
		if (this.mapManager != null){
			this.mapManager.loadSavedData();
		}
	}

	public MapManager getMapManager() {
		return this.mapManager;
	}
	public void setMapManager(MapManager mapManager) {
		this.mapManager = mapManager;
	}
	
	public List<String> getAgentNames() {
		return this.list_agentNames;
	}

    public boolean isInTeamMode() {
        return inTeamMode;
    }

	public void setParent(String parent) {
        this.parent = parent;
    }

	public String getParent() {
		return parent;
	}

	public void addChild(String child) {
        childrenIDs.add(child);
    }

	public List<String> getChildren() {
		return childrenIDs;
	}

	public boolean hasChildren() {
		return !childrenIDs.isEmpty();
	}

	public void resetChildren() {
		childrenIDs = new ArrayList<>();
	}

	public boolean isInterblock() {
		return interblock;
	}

	public void setInterblock(boolean interblock) {
		this.interblock = interblock;
	}

	public void setExplorefinished(boolean explorefinished) {
		this.explorefinished = explorefinished;
	}

	public boolean isExplorefinished() {
		return explorefinished;
	}

	public void setReceivedConnectionResponses(boolean receivedConnectionResponses) {
		this.receivedConnectionResponses = receivedConnectionResponses;
	}

	public boolean hasReceivedConnectionResponses() {
		return receivedConnectionResponses;
	}

	public void setMyPositionID(String myPositionID) {
		this.myPositionID = myPositionID;
	}

	public String getMyPositionID() {
		return myPositionID;
	}

	public boolean isChef() {
		return this.mapManager.getCommunicationMap().getChefID().equals(this.getLocalName());
	}

	public void setTargetAndNextNode(Pair<String, String> targetAndNextNode) {
		if (targetAndNextNode == null) {
			if (this.targetNode != null) {
				this.targetNode = null;
			}
			this.targetNode = null;
			
			return;
		}
		this.targetNode = targetAndNextNode.getKey();
		this.nextNode = targetAndNextNode.getValue();
	}

	public String getTargetNode() {
		return targetNode;
	}

	public String getNextNode() {
		return nextNode;
	}

	public void setNextNode(String nextNode) {
		this.nextNode = nextNode;
	}
	public void resetNextNode() {
		this.nextNode = null;
	}

	public void checkArriveTarget() {
		if (this.targetNode != null && this.targetNode.equals(this.myPositionID)) {
			this.targetNode = null;
		}
	}
}
