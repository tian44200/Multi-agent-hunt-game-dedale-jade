package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.io.Serializable;

import java.util.Set;

import dataStructures.serializableGraph.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import javafx.util.Pair;

public class MapManager implements Serializable{
    private static final long serialVersionUID = -1333959882640838272L;

    private MapRepresentation myMap;
    private Map<String, SerializableSimpleGraph<String, MapAttribute>> staticSubgrapheToShareForAgent;
    private MapRepresentation communicationMap;
    private MapRepresentation observationMap;
    private String myAgentID;

    public MapManager(MapRepresentation myMap, List<String> agents) {
    	this.myMap = myMap;
        this.staticSubgrapheToShareForAgent = new HashMap<>();
        for (String agent : agents) {
            this.staticSubgrapheToShareForAgent.put(agent, new SerializableSimpleGraph<>());
        }
        System.out.println("Subgraphes created"+this.staticSubgrapheToShareForAgent.toString());
        this.communicationMap = new MapRepresentation();
        this.observationMap = new MapRepresentation();
    }

    public synchronized MapRepresentation getMyMap() {
        return this.myMap;
    }

    public synchronized void addNode(String id, MapAttribute mapAttribute) {
        // Add node to the main MapRepresentation
        this.myMap.addNode(id, mapAttribute);

        // Also add/update this node in each agent's subgraph
        for (SerializableSimpleGraph<String, MapAttribute> subgraph : this.staticSubgrapheToShareForAgent.values()) {
            subgraph.addNode(id, mapAttribute);
        }
        // System.out.println("Subgraphes added Node"+this.staticSubgrapheToShareForAgent.toString());
    }

    public synchronized boolean addNewNode(String id) {
        boolean isNodeAdded = this.myMap.addNewNode(id);
        if (isNodeAdded) {
            // Also add this node in each agent's subgraph
            for (SerializableSimpleGraph<String, MapAttribute> subgraph : this.staticSubgrapheToShareForAgent.values()) {
                subgraph.addNode(id, MapAttribute.open);
            }
            // System.out.println("Subgraphes added NewNode"+this.staticSubgrapheToShareForAgent.toString());
        }
        return isNodeAdded;
    }

    public synchronized void addEdge(String node1Id, String node2Id) {
        // Add edge to the main MapRepresentation
        this.myMap.addEdge(node1Id, node2Id);

        // Also add this edge in each agent's subgraph
        for (SerializableSimpleGraph<String, MapAttribute> subgraph : this.staticSubgrapheToShareForAgent.values()) {
            if (subgraph.getNode(node1Id) == null) {
                subgraph.addNode(node1Id, MapAttribute.open); // Assuming SerializableSimpleGraph has a method to add edges
            }
            if (subgraph.getNode(node2Id) == null) {
                subgraph.addNode(node2Id,MapAttribute.open); // Assuming SerializableSimpleGraph has a method to add edges
            }
            subgraph.addEdge("", node1Id, node2Id); // Assuming SerializableSimpleGraph has a method to add edges
        }
    }

    public synchronized SerializableSimpleGraph<String, MapAttribute> getSerialSubGraphForAgent(String agentId) {
        // Prepare the subgraph to be shared
        System.out.println("Subgraphes to share to" + "agentID"+ agentId + ":  "+this.staticSubgrapheToShareForAgent.get(agentId).toString());
        SerializableSimpleGraph<String, MapAttribute> subgraphToShare = this.staticSubgrapheToShareForAgent.get(agentId);
        if (subgraphToShare == null || subgraphToShare.toString().equals("{}")) {
            return null; 
        }
        // Reset the agent's subgraph
        this.staticSubgrapheToShareForAgent.put(agentId, new SerializableSimpleGraph<>());
        // System.out.println("Subgraphes to share to" + "agentID"+ agentId + ":  "+subgraphToShare.toString());
        return subgraphToShare;
    }
    
    public synchronized void mergeMap(SerializableSimpleGraph<String, MapAttribute> sgreceived, String senderID) {
        // System.out.println("Merge Map from "+senderID);
        // System.out.println("Subgraphs before merging"+this.staticSubgrapheToShareForAgent.toString());
        // System.out.println("MyMap before merging "+this.myMap.getSerializableGraph().toString());
        // System.out.println("Merging received subgraph"+sgreceived.toString());
        // Merge the received subgraph into each agent's subgraph
        for (SerializableSimpleGraph<String, MapAttribute> subgraph : this.staticSubgrapheToShareForAgent.values()) {
            // Merge nodes
            if (subgraph.equals(this.staticSubgrapheToShareForAgent.get(senderID))) {
                continue;
            }
            for (SerializableNode<String, MapAttribute> node : sgreceived.getAllNodes()) {
                if (!(node.getNodeContent() == MapAttribute.open && subgraph.getNode(node.getNodeId()) != null)) {
                    subgraph.addNode(node.getNodeId(), node.getNodeContent());
                }
            }

            // Merge edges
            for (SerializableNode<String, MapAttribute> node : sgreceived.getAllNodes()) {
                Set<String> edges = sgreceived.getEdges(node.getNodeId());
                if (edges != null) {
                    for (String edge : edges) {
                        subgraph.addEdge("", node.getNodeId(), edge);
                    }
                }
            }
        }
        // Also merge the received subgraph into the main MapRepresentation
        this.myMap.mergeMap(sgreceived);
        // System.out.println("Merging done"+this.staticSubgrapheToShareForAgent.toString());
        // System.out.println("Merging done"+this.myMap.getSerializableGraph().toString());
    }

    public MapRepresentation getCommunicationMap() {
        return this.communicationMap;
    }
    public void cleanCommunicationMap() {
        this.communicationMap = new MapRepresentation();
    }

    public MapRepresentation getObservationMap() {
        return this.observationMap;
    }

    public void cleanObservationMap() {
        this.observationMap = new MapRepresentation();
    }

    public void prepareMigration(){
        this.myMap.prepareMigration();
        this.communicationMap.prepareMigration();
    }

    public void loadSavedData(){
        this.myMap.loadSavedData();
        this.communicationMap.loadSavedData();
    }

    public void addDirectCommunication(String agentID) {
        this.communicationMap.addNewNode(agentID);
        this.communicationMap.addEdge(myAgentID, agentID);
    }

    public List<String> getDirectCommunications() {
        return this.communicationMap.getNeighborsForNode(myAgentID);
    }

    public Map<String, Pair<String, String>> computeTargetAndNextNodeForAgent() {
        this.myMap.mergeMap(this.observationMap.getSerializableGraph());
        return this.myMap.computeTargetAndNextNodeForAgent();
    }
}

