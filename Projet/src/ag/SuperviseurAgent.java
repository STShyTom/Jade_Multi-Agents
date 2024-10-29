package ag;

import gui.CaillouxGui;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class SuperviseurAgent extends Agent {
    private CaillouxGui carteGUI;

    protected void setup() {
        carteGUI = (CaillouxGui) getArguments()[0];
        addBehaviour(new ReceptionCaillouBehaviour());
    }

    private class ReceptionCaillouBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                System.out.println("Message reçu de l'explorateur: " + msg.getContent());
            } else {
                block();
            }
        }
    }
}
