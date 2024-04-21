package eu.su.mas.dedaleEtu.mas.knowledge;
import javafx.util.Pair; // Import the Pair class
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;
import org.graphstream.graph.implementations.Graphs;


import dataStructures.serializableGraph.*;
import dataStructures.tuple.Couple;
import jade.core.Agent;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

/**
 * This simple topology representation only deals with the graph, not its content.</br>
 * The knowledge representation is not well written (at all), it is just given as a minimal example.</br>
 * The viewer methods are not independent of the data structure, and the dijkstra is recomputed every-time.
 * 
 * @author hc
 */
public class MapRepresentation implements Serializable {

	/**
	 * A node is open, closed, or agent
	 * @author hc
	 *
	 */

	public enum MapAttribute {	
		agent,stenchagent,stench,open,closed,golem,block;
	}

	private static final long serialVersionUID = -1333959882640838272L;

	/*********************************
	 * Parameters for graph rendering
	 ********************************/

	private String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private String nodeStyle_open = "node.open {"+"fill-color: forestgreen;"+"}";
	private String nodeStyle_agent = "node.agent {"+"fill-color: blue;"+"}";
	private String nodeStyle_stench = "node.stench {"+"fill-color: orange;"+"}";
	private String nodeStyle_golem = "node.golem {"+"fill-color: red;"+"}";
	private String nodeStyle_stenchagent = "node.stenchagent {"+"fill-color: blue;"+"}";
	private String nodeStyle=defaultNodeStyle+nodeStyle_agent+nodeStyle_open+nodeStyle_stench+nodeStyle_golem+nodeStyle_stenchagent;

	private Graph g; //data structure non serializable
	private Viewer viewer; //ref to the display,  non serializable
	private Integer nbEdges;//used to generate the edges ids
	private String chefID;
	private SerializableSimpleGraph<String, MapAttribute> sg;//used as a temporary dataStructure during migration
	private Graph g_initial;
	private boolean exploreFinished;

	public MapRepresentation() {
		//System.setProperty("org.graphstream.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("org.graphstream.ui", "javafx");
		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);
//		Platform.startup(() ->
//		{
//		    // This block will be executed on JavaFX Thread
//			openGui();
//		});
//		boolean isFxApplicationThread = Platform.isFxApplicationThread();
//		System.out.println("Is FX Application Thread? " + isFxApplicationThread);
//		new JFXPanel();
		Platform.runLater(() -> {
			openGui();
		});
		//this.viewer = this.g.display();

		this.nbEdges=0;
	}

	/**
	 * Add or replace a node and its attribute 
	 * @param id unique identifier of the node
	 * @param mapAttribute attribute to process
	 */
	public synchronized void addNode(String id,MapAttribute mapAttribute){
		Node n;
		if (this.g.getNode(id)==null){
			n=this.g.addNode(id);
		}else{
			n=this.g.getNode(id);
		}
		n.clearAttributes();
		n.setAttribute("ui.class", mapAttribute.toString());
		n.setAttribute("ui.label",id);
	}

	/**
	 * Add a node to the graph. Do nothing if the node already exists.
	 * If new, it is labeled as open (non-visited)
	 * @param id id of the node
	 * @return true if added
	 */
	public synchronized boolean addNewNode(String id) {
		if (this.g.getNode(id)==null){
			addNode(id,MapAttribute.open);
			return true;
		}
		return false;
	}

	public synchronized boolean addAgentName(String nodeID, String agentName) {
		Node node = this.g.getNode(nodeID);
		if (node != null) {
			node.setAttribute("ui.label", agentName);
			node.setAttribute("agentName",agentName);
			return true;
		}
		return false;
	}

	/**
	 * Add an undirect edge if not already existing.
	 * @param idNode1 unique identifier of node1
	 * @param idNode2 unique identifier of node2
	 */
	public synchronized void addEdge(String idNode1,String idNode2){
		this.nbEdges++;
		try {
			this.g.addEdge(this.nbEdges.toString(), idNode1, idNode2);
		}catch (IdAlreadyInUseException e1) {
			System.err.println("ID existing");
			System.exit(1);
		}catch (EdgeRejectedException e2) {
			this.nbEdges--;
		} catch(ElementNotFoundException e3){

		}
	}

	/**
	 * Compute the shortest Path from idFrom to IdTo. The computation is currently not very efficient
	 * 
	 * 
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node
	 * @return the list of nodes to follow, null if the targeted node is not currently reachable
	 */
	public synchronized List<String> getShortestPath(String idFrom,String idTo){
		List<String> shortestPath=new ArrayList<String>();

		Dijkstra dijkstra = new Dijkstra();//number of edge
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();//compute the distance to all nodes from idFrom
		List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
		Iterator<Node> iter=path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		if (shortestPath.isEmpty()) {//The openNode is not currently reachable
			return null;
		}else {
			shortestPath.remove(0);//remove the current position
		}
		return shortestPath;
	}

	public synchronized List<String> getShortestPathInitialG(String idFrom,String idTo){
		List<String> shortestPath=new ArrayList<String>();

		Dijkstra dijkstra = new Dijkstra();//number of edge
		dijkstra.init(g_initial);
		dijkstra.setSource(g_initial.getNode(idFrom));
		dijkstra.compute();//compute the distance to all nodes from idFrom
		List<Node> path=dijkstra.getPath(g_initial.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
		Iterator<Node> iter=path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		if (shortestPath.isEmpty()) {//The openNode is not currently reachable
			return null;
		}else {
			shortestPath.remove(0);//remove the current position
		}
		return shortestPath;
	}
	public List<String> getShortestPathToClosestOpenNode(String myPosition) {
		//1) Get all openNodes
		List<String> opennodes=getOpenNodes();

		//2) select the closest one
		List<Couple<String,Integer>> lc=
				opennodes.stream()
				.map(on -> (getShortestPath(myPosition,on)!=null)? new Couple<String, Integer>(on,getShortestPath(myPosition,on).size()): new Couple<String, Integer>(on,Integer.MAX_VALUE))//some nodes my be unreachable if the agents do not share at least one common node.
				.collect(Collectors.toList());

		Optional<Couple<String,Integer>> closest=lc.stream().min(Comparator.comparing(Couple::getRight));
		//3) Compute shorterPath

		return getShortestPath(myPosition,closest.get().getLeft());
	}

	

	public void setMaxHashChef() {
		String MaxHash = this.g.nodes()
				.map(Node::getId)
				.max(Comparator.comparingInt(String::hashCode))
				.orElse(null);
		if (MaxHash != null) {
			this.chefID = MaxHash;
		}else{
			throw new IllegalStateException("No node found to set as chef.");
		}
	}

	public String getChefID() {
		return this.chefID;
	}
	
	public List<String> getShortestPathToChef(String myPosition){
		List<String> path = getShortestPath(myPosition, this.chefID);
        return path;
	}

	public synchronized List<String> getNeighborNodes(String myPosition) {
		serializeGraphTopology(); 
		List<String> neighbors = new ArrayList<>(sg.getEdges(myPosition));
		return neighbors;
	}

	public List<String> getOpenNodes(){
		return this.g.nodes()
				.filter(x ->x .getAttribute("ui.class")==MapAttribute.open.toString()) 
				.map(Node::getId)
				.collect(Collectors.toList());
	}

	public List<String> getAgentAndStenchAgentNodes() {
		return this.g.nodes()
				.filter(x -> x.getAttribute("ui.class").equals(MapAttribute.agent.toString()) 
						  || x.getAttribute("ui.class").equals(MapAttribute.stenchagent.toString()))
				.map(Node::getId)
				.collect(Collectors.toList());
	}
	

	public List<String> getStenchNodes() {
		return this.g.nodes()
				.filter(x -> x.getAttribute("ui.class").equals(MapAttribute.stench.toString()) 
						  || x.getAttribute("ui.class").equals(MapAttribute.stenchagent.toString()))
				.map(Node::getId)
				.collect(Collectors.toList());
	}
	
	public List<String> addNeighborsToNodes(List<String> nodeIds) {
		Set<String> neighborsSet = new HashSet<>(); // 使用Set来自动去重

		// 遍历每个节点ID
		for (String nodeId : nodeIds) {
			Node node = this.g.getNode(nodeId); // 获取对应的Node对象
			if (node != null) {
				// 获取每个节点的所有邻居并添加到集合中
				node.neighborNodes().forEach(neighborNode -> neighborsSet.add(neighborNode.getId()));
			}
			// 将当前节点ID也添加到集合中
			neighborsSet.add(nodeId);
		}

		// 将Set转换为List并返回
		return new ArrayList<>(neighborsSet);
	}
	
	public List<String> getOnlyNeighborNodes(List<String> nodeIds) {
		Set<String> neighborsSet = new HashSet<>(); // 使用Set来自动去重

		// 遍历每个节点ID
		for (String nodeId : nodeIds) {
			Node node = this.g.getNode(nodeId); // 获取对应的Node对象
			if (node != null) {
				// 获取每个节点的所有邻居并添加到集合中
				node.neighborNodes().forEach(neighborNode -> neighborsSet.add(neighborNode.getId()));
			}
		}

		// 从集合中删除提供的节点ID
		neighborsSet.removeAll(nodeIds);

		// 将Set转换为List并返回
		return new ArrayList<>(neighborsSet);
	}

	public double calculateNodeScore(String nodeId) {
		Node node = g.getNode(nodeId); // 从图中获取节点对象
		if (node == null) {
			return 0.0; // 如果节点不存在，返回0分
		}
		Set<String> visited = new HashSet<>(); // 用于避免重复访问节点
		return dfsCalculateScore(node, 0, visited); // 开始深度优先搜索计算分数
	}
	

	private double dfsCalculateScore(Node node, int depth, Set<String> visited) {
		double score = 0.0;
	
		// 检查节点是否已经被访问过，避免循环
		if (!visited.contains(node.getId())) {
			visited.add(node.getId());
	
			// 获取节点的MapAttribute属性
			String attributeStr = node.getAttribute("ui.class", String.class);
			
			if ("agent".equals(attributeStr) || "closed".equals(attributeStr)) {
				return 0.0;
			}
	
			if ("golem".equals(attributeStr)) {
				score += Math.pow(0.5, depth);; // 如果节点是golem
			}
			// 如果节点是stench或stenchagent，则根据深度计算分数
			if ("stench".equals(attributeStr) || "stenchagent".equals(attributeStr)) {
				score += Math.pow(0.5, depth+1);
			}
			// 对所有邻居节点递归调用dfsCalculateScore
			for (Edge edge : node.leavingEdges().collect(Collectors.toList())) {
				Node nextNode = edge.getOpposite(node);
				score += dfsCalculateScore(nextNode, depth + 1, visited);
			}
		}
	
		return score;
	}
	
public synchronized List<String> getGolemNodes() {
	return this.g.nodes()
		.filter(x -> x.getAttribute("ui.class").equals(MapAttribute.golem.toString()))
		.map(Node::getId)
		.collect(Collectors.toList());
}

public synchronized boolean areGolemsBlocked() {
	List<String> golemNodes = getGolemNodes();
	if (golemNodes.isEmpty()) {
		return false;
	}
	List<String> golemNeighborNodes = getOnlyNeighborNodes(golemNodes);
	for (String node: golemNeighborNodes){
		Node gNode = this.g.getNode(node);
		if (!((String) gNode.getAttribute("ui.class")==MapAttribute.agent.toString()) && !((String) gNode.getAttribute("ui.class")==MapAttribute.stenchagent.toString())) {
			return false;
		}
	}
	return true;
}

public synchronized Map<String, Pair<String, String>> computeBlockPositionsForAgent() {
	List<String> golemNodes = getGolemNodes();
	System.out.println("Golem nodes: " + golemNodes); // Debug print
	if (golemNodes.isEmpty()) {
		return null;
	}
	List<String> agents = getAgentAndStenchAgentNodes();
	Map<String, Pair<String, String>> agentNodeMap = new HashMap<>();
	List<String> golemNeighborNodes = getOnlyNeighborNodes(golemNodes);
	boolean isGolemBlocked = true;
	for (String node: golemNeighborNodes){
		Node gNode = this.g.getNode(node);
		if (!((String) gNode.getAttribute("ui.class")==MapAttribute.agent.toString()) && !((String) gNode.getAttribute("ui.class")==MapAttribute.stenchagent.toString())) {
			isGolemBlocked = false;
			break;
		}
		agentNodeMap.put(node, new Pair<>(golemNodes.get(0), "block"));
	}
	if (isGolemBlocked) {
		for (String agent : agents) {
			if (!agentNodeMap.containsKey(agent)) {
				agentNodeMap.put(agent, new Pair<>(golemNodes.get(0), "disband"));
			}
		}
		return agentNodeMap;
	} else {
		return null;
	}
}

public synchronized Map<String, Pair<String, String>> computeTargetAndNextNodeForAgent() {
	Map<String, Pair<String, String>> blockPositionsForAgent = computeBlockPositionsForAgent();
	if (blockPositionsForAgent != null) {
		return blockPositionsForAgent;
	}
	List<String> agents = getAgentAndStenchAgentNodes();
	System.out.println("Agents: " + agents); // Debug print

	List<String> targetNodes = new ArrayList<>();
	if (hasStenchNode()) {
		targetNodes = getStenchNodes();
		System.out.println("Stench nodes: " + targetNodes); // Debug print

		while (targetNodes.size() < agents.size()) {
			targetNodes = addNeighborsToNodes(targetNodes);
			System.out.println("Added neighbors to nodes: " + targetNodes); // Debug print
		}
	} else {
		for (String agent : agents) {
			List<String> neighbors = getNeighborNodes(agent);
			System.out.println("Neighbors of agent " + agent + ": " + neighbors); // Debug print

			if (!neighbors.isEmpty()) {
				int randomIndex = new Random().nextInt(neighbors.size());
				String randomNeighbor = neighbors.get(randomIndex);
				targetNodes.add(randomNeighbor);
				System.out.println("Added random neighbor " + randomNeighbor + " to target nodes"); // Debug print
			}
		}
	}   

	Map<String, Double> nodeScores = new HashMap<>();
	for (String node : targetNodes) {
		double score = calculateNodeScore(node);
		nodeScores.put(node, score);
		System.out.println("Node " + node + " score: " + score); // Debug print
	}
	nodeScores = nodeScores.entrySet().stream()
		.sorted((e1, e2) -> {
			int compare = e2.getValue().compareTo(e1.getValue());
			if (compare != 0) {
				return compare;
			} else {
				return Math.random() < 0.5 ? -1 : 1;
			}
		})
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	System.out.println("Sorted node scores: " + nodeScores); // Debug print
	Map<String, Pair<String, String>> agentNodeMap = new HashMap<>();
	for (Map.Entry<String, Double> entry : nodeScores.entrySet()) {
		if (agents.isEmpty()) {
			break;
		}

		String node = entry.getKey();
		String closestAgent = null;
		List<String> shortestPath = null;

		for (String agent : agents) {
			List<String> path = null;
			if (g.getNode(node) == null){
				path = getShortestPathInitialG(agent, node);
			} else {
				path = getShortestPath(agent, node);
			}
			
			System.out.println("Shortest path from agent " + agent + " to node " + node + ": " + path); // Debug print

			if (shortestPath == null || path.size() < shortestPath.size()) {
				shortestPath = path;
				closestAgent = agent;
			}
		}
		System.out.println("ShortestPath: " + shortestPath.toString()); // Debug print
		String nextNode = null;
		if (!shortestPath.isEmpty()) {
			nextNode = shortestPath.get(0);
			if (g.getNode(nextNode) == null){
				g.removeNode(nextNode); // remove the nextNode from the graph
				System.out.println("Removed node " + nextNode + " from the graph"); // Debug print
			}
		}
		if (closestAgent != null) {
			agentNodeMap.put(closestAgent, new Pair<>(node, nextNode));
			agents.remove(closestAgent);
			System.out.println("Assigned agent " + closestAgent + " to node " + node + " with next node " + nextNode); // Debug print
		}
	}
	restoreInitialGraph	();
	System.out.println("Final agent-node map: " + agentNodeMap); // Debug print
	return agentNodeMap;
}

public synchronized String computeNextNodeForDisband(String myPosition, String targetNode){
	List<String> neighbors = getNeighborNodes(myPosition);
	System.out.println("Neighbors of agent " + myPosition + ": " + neighbors); // Debug print

	Dijkstra dijkstra = new Dijkstra();
	dijkstra.init(g);
	dijkstra.setSource(g.getNode(targetNode));
	dijkstra.compute();

	Node myNode = g.getNode(myPosition);
	double myDistance = dijkstra.getPathLength(myNode);

	String farthestNeighbor = null;
	double maxDistance = Double.MIN_VALUE;

	for (String neighbor : neighbors) {
		Node neighborNode = g.getNode(neighbor);
		double distance = dijkstra.getPathLength(neighborNode);
		if (distance > maxDistance && distance > myDistance) {
			maxDistance = distance;
			farthestNeighbor = neighbor;
		}
	}

	dijkstra.clear();

	return farthestNeighbor;
}

public boolean hasGolemNodeOnMap() {
	Iterator<Node> iter = this.g.iterator();
	while(iter.hasNext()){
		Node node = iter.next();
		if (node.getAttribute("MapAttribute").toString().equals(MapAttribute.golem.toString())) {
			return true;
		}
	}
	return false;
}

	/**
	 * Before the migration we kill all non serializable components and store their data in a serializable form
	 */
	public void prepareMigration(){
		serializeGraphTopology();

		closeGui();

		this.g=null;
	}

	/**
	 * Before sending the agent knowledge of the map it should be serialized.
	 */
	private void serializeGraphTopology() {
		this.sg= new SerializableSimpleGraph<String,MapAttribute>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			sg.addNode(n.getId(),MapAttribute.valueOf((String)n.getAttribute("ui.class")));
		}
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			sg.addEdge(e.getId(), sn.getId(), tn.getId());
		}	
	}


	public synchronized SerializableSimpleGraph<String,MapAttribute> getSerializableGraph(){
		serializeGraphTopology();
		return this.sg;
	}

	/**
	 * After migration we load the serialized data and recreate the non serializable components (Gui,..)
	 */
	public synchronized void loadSavedData(){

		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		openGui();

		Integer nbEd=0;
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			this.g.addNode(n.getNodeId()).setAttribute("ui.class", n.getNodeContent().toString());
			for(String s:this.sg.getEdges(n.getNodeId())){
				this.g.addEdge(nbEd.toString(),n.getNodeId(),s);
				nbEd++;
			}
		}
		System.out.println("Loading done");
	}

	/**
	 * Method called before migration to kill all non serializable graphStream components
	 */
	private synchronized void closeGui() {
		//once the graph is saved, clear non serializable components
		if (this.viewer!=null){
			//Platform.runLater(() -> {
			try{
				this.viewer.close();
			}catch(NullPointerException e){
				System.err.println("Bug graphstream viewer.close() work-around - https://github.com/graphstream/gs-core/issues/150");
			}
			//});
			this.viewer=null;
		}
	}

	/**
	 * Method called after a migration to reopen GUI components
	 */
	private synchronized void openGui() {
		this.viewer =new FxViewer(this.g, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);//GRAPH_IN_GUI_THREAD)
		viewer.enableAutoLayout();
		viewer.setCloseFramePolicy(FxViewer.CloseFramePolicy.CLOSE_VIEWER);
		viewer.addDefaultView(true);

		g.display();
	}

	public void saveInitialGraph() {
        this.g_initial = Graphs.clone(this.g);
    }

	public void restoreInitialGraph() {
        this.g = Graphs.clone(this.g_initial);
    }

	public void mergeMap(SerializableSimpleGraph<String, MapAttribute> sgreceived) {
		//System.out.println("You should decide what you want to save and how");
		//System.out.println("We currently blindy add the topology");
		System.out.println("sg while merging map: " + sgreceived.toString());
		for (SerializableNode<String, MapAttribute> n: sgreceived.getAllNodes()){
			//System.out.println(n);
			boolean alreadyIn =false;
			//1 Add the node
			Node newnode=null;
			try {
				newnode=this.g.addNode(n.getNodeId());
			}	catch(IdAlreadyInUseException e) {
				alreadyIn=true;
				//System.out.println("Already in"+n.getNodeId());
			}
			if (!alreadyIn) {
				newnode.setAttribute("ui.label", newnode.getId());
				newnode.setAttribute("ui.class", n.getNodeContent().toString());
			}else{
				newnode=this.g.getNode(n.getNodeId());
				//3 check its attribute. If it is below the one received, update it.
				if (!exploreFinished){
					if (((String) newnode.getAttribute("ui.class"))==MapAttribute.closed.toString() || n.getNodeContent().toString()==MapAttribute.closed.toString()) {
						newnode.setAttribute("ui.class",MapAttribute.closed.toString());
					}
				}
				else {
					System.out.println("Explore finished for MapRepresentation, merge the variante node content.");
					System.out.println("Node"+ n.toString()+ "Content "+ n.getNodeContent().toString());
					String currentAttribute = (String) newnode.getAttribute("ui.class");
					if (!(currentAttribute.equals(MapAttribute.agent.toString()) || currentAttribute.equals(MapAttribute.stenchagent.toString()))) {
						if (!(currentAttribute.equals(MapAttribute.golem.toString()))) {
							newnode.setAttribute("ui.class", n.getNodeContent().toString());
					}
				}
			}
		}
	}

		//4 now that all nodes are added, we can add edges
		for (SerializableNode<String, MapAttribute> n: sgreceived.getAllNodes()){
			for(String s:sgreceived.getEdges(n.getNodeId())){
				addEdge(n.getNodeId(),s);
				
			}
		}
		//System.out.println("Merge done");
	}

	public void setExploreFinishedTrue() {
		this.exploreFinished = true;
	}

	public SerializableSimpleGraph<String, MapAttribute> mergeMapAndGetDifference(SerializableSimpleGraph<String, MapAttribute> sgreceived) {
		SerializableSimpleGraph<String, MapAttribute> diffMap = new SerializableSimpleGraph<>();

		for (SerializableNode<String, MapAttribute> n: sgreceived.getAllNodes()){
			boolean alreadyIn =false;
			Node newnode=null;
			try {
				newnode=this.g.addNode(n.getNodeId());
			} catch(IdAlreadyInUseException e) {
				alreadyIn=true;
			}
			if (!alreadyIn) {
				newnode.setAttribute("ui.label", newnode.getId());
				diffMap.addNode(n.getNodeId());
			} else {
				newnode=this.g.getNode(n.getNodeId());
				if (((String) newnode.getAttribute("ui.class"))==MapAttribute.closed.toString() || n.getNodeContent().toString()==MapAttribute.closed.toString()) {
					newnode.setAttribute("ui.class",MapAttribute.closed.toString());
				}
			}
		}

		for (SerializableNode<String, MapAttribute> n: sgreceived.getAllNodes()){
			for(String s:sgreceived.getEdges(n.getNodeId())){
				addEdge(n.getNodeId(),s);
				diffMap.addEdge(this.nbEdges.toString(),n.getNodeId(), s); // Add the new edge to the difference map
			}
		}

		// If diffMap contains any nodes, return it. Otherwise, return null.
		return diffMap.getAllNodes().isEmpty() ? null : diffMap;
	}


	/**
	 * 
	 * @return true if there exist at least one openNode on the graph 
	 */
	public boolean hasOpenNode() {
		return (this.g.nodes()
				.filter(n -> n.getAttribute("ui.class")==MapAttribute.open.toString())
				.findAny()).isPresent();
	}

	public int getDegreeForNode(String nodeID) {
		Node node = this.g.getNode(nodeID);
		if (node != null) {
			return node.getDegree();
		}
		return 0;
	}

	public List<String> getNeighborsForNode(String nodeID) {
		Node node = this.g.getNode(nodeID);
		if (node != null) {
			return node.neighborNodes().map(Node::getId).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	public boolean hasStenchNode() {
		return (this.g.nodes()
				.filter(n -> n.getAttribute("ui.class")==MapAttribute.stench.toString())
				.findAny()).isPresent();
	}

	public void clearMap() {
		this.g.clear();
		this.nbEdges = 0;
	}
}
