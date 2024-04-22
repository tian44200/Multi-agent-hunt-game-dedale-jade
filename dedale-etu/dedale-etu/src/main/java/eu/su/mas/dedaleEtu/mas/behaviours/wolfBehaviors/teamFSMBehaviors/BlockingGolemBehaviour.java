package eu.su.mas.dedaleEtu.mas.behaviours.wolfBehaviors.teamFSMBehaviors;

import jade.core.behaviours.SimpleBehaviour;

public class BlockingGolemBehaviour extends SimpleBehaviour {
    private static final long serialVersionUID = 1L;
    private boolean announcedBlock = false;


	// Constructor
    public BlockingGolemBehaviour(jade.core.Agent a) {
        super(a);
    }

    @Override
    public void action() {
        if (!announcedBlock){
            System.out.println("I have blocked the Golem and finished my work.");
            announcedBlock = true;
        }
    }

	@Override
	public boolean done() {
		return false;
	}
}