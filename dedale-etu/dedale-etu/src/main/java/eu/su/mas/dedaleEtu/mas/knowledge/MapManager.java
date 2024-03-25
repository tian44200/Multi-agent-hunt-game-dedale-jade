package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.Serializable;

import java.util.Set;

import dataStructures.serializableGraph.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import eu.su.mas.dedaleEtu.mas.knowledge.DynamicObjectInfo.Type;

public class MapManager implements Serializable{
    private static final long serialVersionUID = -1333959882640838272L;

    private MapRepresentation myMap;
    private Map<String, SerializableSimpleGraph<String, MapAttribute>> staticSubgrapheToShareForAgent;
    private Map<String, DynamicObjectInfo> dynamicObjectsByID;
    private Map<String, List<DynamicObjectInfo>> dynamicObjectsByType;

    public MapManager(MapRepresentation myMap, List<String> agents) {
    	this.myMap = myMap;
        this.staticSubgrapheToShareForAgent = new HashMap<>();
        this.dynamicObjectsByID = new HashMap<>();
        this.dynamicObjectsByType = new HashMap<>();
        for (String agent : agents) {
            AID aid = new AID(agent, AID.ISLOCALNAME);
            this.staticSubgrapheToShareForAgent.put(aid.getName(), new SerializableSimpleGraph<>());
            this.dynamicObjectsByID.put(aid.getName(), new DynamicObjectInfo(aid.getName(), Type.agent, 0L, null));
        }
        System.out.println("Subgraphes created"+this.staticSubgrapheToShareForAgent.toString());
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
        SerializableSimpleGraph<String, MapAttribute> subgraphToShare = this.staticSubgrapheToShareForAgent.get(agentId);
        if (subgraphToShare == null || subgraphToShare.toString().equals("{}")) {
            return null; 
        }
        // Reset the agent's subgraph
        this.staticSubgrapheToShareForAgent.put(agentId, new SerializableSimpleGraph<>());
        // System.out.println("Subgraphes to share to" + "agentID"+ agentId + ":  "+subgraphToShare.toString());
        return subgraphToShare;
    }

    public synchronized Map<String, DynamicObjectInfo> getDynamicInfo() {
        return this.dynamicObjectsByID;
    }
    
    public synchronized void mergeMap(SerializableSimpleGraph<String, MapAttribute> sgreceived, AID senderID) {
        // System.out.println("Merging received subgraph"+sgreceived.toString());
        // Merge the received subgraph into each agent's subgraph
        for (SerializableSimpleGraph<String, MapAttribute> subgraph : this.staticSubgrapheToShareForAgent.values()) {
            // Merge nodes
            if (subgraph.equals(this.staticSubgrapheToShareForAgent.get(senderID.getName()))) {
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
        // System.out.println("Merging done"+this.staticSubgrapheToShareForAgent.toString());
        // System.out.println("Merging done"+this.myMap.toString());
        // Also merge the received subgraph into the main MapRepresentation
        this.myMap.mergeMap(sgreceived);
    }

    public void prepareMigration(){
        this.myMap.prepareMigration();
    }

    public void loadSavedData(){
        this.myMap.loadSavedData();
    }
}

