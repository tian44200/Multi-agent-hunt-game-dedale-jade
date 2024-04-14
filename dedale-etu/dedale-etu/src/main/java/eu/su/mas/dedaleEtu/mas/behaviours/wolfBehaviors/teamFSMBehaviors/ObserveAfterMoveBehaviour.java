package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.WolfAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedale.env.Location;
import jade.core.behaviours.OneShotBehaviour;

public class ObserveAfterMoveBehaviour extends OneShotBehaviour {

    private static final long serialVersionUID = 4917319987944083367L;
    private WolfAgent wolfAgent;
    
    public ObserveAfterMoveBehaviour(WolfAgent wolfAgent) {
        super(wolfAgent);
        this.wolfAgent = wolfAgent;
    }
    @Override
    public void action() {
        System.out.println(this.myAgent.getLocalName() + " - ObserveAfterMoveBehaviour");
        wolfAgent.getMapManager().getMyMap().restoreInitialGraph();
        wolfAgent.getMapManager().getObservationMap().clearMap();
        wolfAgent.resetChildren();
        wolfAgent.resetParent();
        wolfAgent.resetNextNode();
        Location myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();

        if (myPosition != null) {
            wolfAgent.setMyPositionID(myPosition.getLocationId());
            List<Couple<Location, List<Couple<Observation, Integer>>>> lobs = 
                ((AbstractDedaleAgent) wolfAgent).observe(); //myPosition

            // Check each observed location for stench
            for (Couple<Location, List<Couple<Observation, Integer>>> obs : lobs) {
                Location location = obs.getLeft();
                boolean stenchDetected = false;

                // Examine the observations at this location
                for (Couple<Observation, Integer> detail : obs.getRight()) {
                    if (detail.getLeft() == Observation.STENCH) {
                        // If stench is detected, check if the location is the current position
                        if (location.equals(myPosition)) {
                            // If it is, mark the node as having a stenchagent attribute
                            wolfAgent.getMapManager().getObservationMap().addNode(location.getLocationId(), MapAttribute.stenchagent);
                        } else {
                            // If it's not, mark the node as having a stench attribute
                            wolfAgent.getMapManager().getObservationMap().addNode(location.getLocationId(), MapAttribute.stench);
                        }
                        stenchDetected = true;
                        break; // Stench detected, no need to check further
                    }
                }

                // If no stench is detected, check if the location is the current position
                if (!stenchDetected) {
                    if (location.equals(myPosition)) {
                        // If it is, mark the node as having an agent attribute
                        wolfAgent.getMapManager().getObservationMap().addNode(location.getLocationId(), MapAttribute.agent);
                    } else {
                        // If it's not, mark the node as closed
                        wolfAgent.getMapManager().getObservationMap().addNode(location.getLocationId(), MapAttribute.closed);
                    }
                }
            }
            wolfAgent.checkArriveTarget();
        }
    }
}
