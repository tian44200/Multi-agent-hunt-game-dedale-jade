package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;

import dataStructures.serializableGraph.*;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import javafx.application.Platform;
import java.util.Map;

public class MapManager implements Serializable{
    private static final long serialVersionUID = -1333959882640838272L;

    private MapRepresentation myMap;
    private Map<String, SerializableSimpleGraph<String, MapAttribute>> subgrapheToShareForAgent;

    public MapManager(MapRepresentation myMap, List<String> agents) {
        this.myMap = myMap;
        if (this.myMap == null) {
            this.myMap = new MapRepresentation();
        }
        this.subgrapheToShareForAgent = new HashMap<>();
        for (String agent : agents) {
            this.subgrapheToShareForAgent.put(agent, new SerializableSimpleGraph<>());
        }
    }

    public synchronized MapRepresentation getMyMap() {
        return this.myMap;
    }

    public synchronized void addNode(String id, MapAttribute mapAttribute) {
        // Add node to the main MapRepresentation
        this.myMap.addNode(id, mapAttribute);

        // Also add/update this node in each agent's subgraph
        for (SerializableSimpleGraph<String, MapAttribute> subgraph : this.subgrapheToShareForAgent.values()) {
            subgraph.addNode(id, mapAttribute);
        }
    }

    public synchronized boolean addNewNode(String id) {
        boolean isNodeAdded = this.myMap.addNewNode(id);
        if (isNodeAdded) {
            // Also add this node in each agent's subgraph
            for (SerializableSimpleGraph<String, MapAttribute> subgraph : this.subgrapheToShareForAgent.values()) {
                subgraph.addNode(id, MapAttribute.open);
            }
        }
        return isNodeAdded;
    }

    public synchronized void addEdge(String node1Id, String node2Id) {
        // Add edge to the main MapRepresentation
        this.myMap.addEdge(node1Id, node2Id);

        // Also add this edge in each agent's subgraph
        for (SerializableSimpleGraph<String, MapAttribute> subgraph : this.subgrapheToShareForAgent.values()) {
            subgraph.addEdge("", node1Id, node2Id); // Assuming SerializableSimpleGraph has a method to add edges
        }
    }

    public synchronized SerializableSimpleGraph<String, MapAttribute> getSerializableSubGraphToShareForAgent(String agentId) {
        SerializableSimpleGraph<String, MapAttribute> subgraph = this.subgrapheToShareForAgent.get(agentId);
        if (subgraph == null) {
            return null; // Or throw an exception
        }
        // Prepare the subgraph to be shared
        SerializableSimpleGraph<String, MapAttribute> subgraphToShare = subgraph;
        // Reset the agent's subgraph
        this.subgrapheToShareForAgent.put(agentId, new SerializableSimpleGraph<>());
        return subgraphToShare;
    }
    
    public synchronized void mergeMap(SerializableSimpleGraph<String, MapAttribute> sgreceived) {
        // Merge the received subgraph into each agent's subgraph
        for (SerializableSimpleGraph<String, MapAttribute> subgraph : this.subgrapheToShareForAgent.values()) {
            // Merge nodes
            for (SerializableNode<String, MapAttribute> node : sgreceived.getAllNodes()) {
                subgraph.addNode(node.getNodeId(), node.getNodeContent());
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
    }
}
