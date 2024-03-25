package eu.su.mas.dedaleEtu.mas.behaviours.huntBehaviors;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class ImmobileBehaviour extends Behaviour {

    private static final long serialVersionUID = -3154095905293821913L;

    public ImmobileBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        // Pause for one second
        myAgent.doWait(1000);
    }

    @Override
    public boolean done() {
        // This behaviour is done when it is executed once
        return true;
    }
}