package ag;

import Case.Pierre;
import gui.CaillouxGui;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

public class ExplorateurAgent extends Agent {
    private CaillouxGui carteGUI;
    private int x = 0, y = 0;

    protected void setup() {
        carteGUI = (CaillouxGui) getArguments()[0];
        addBehaviour(new ExplorationBehaviour());
    }

    private class ExplorationBehaviour extends Behaviour {
        private boolean finished = false;
        public void action() {
            if (x < carteGUI.getWidth() / carteGUI.getCellSize() && y < carteGUI.getHeight() / carteGUI.getCellSize()) {
                if (carteGUI.getGridElement(x,y).getClass() == Pierre.class) { // Caillou trouvé
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(getAID("superviseur"));
                    msg.setContent("Caillou trouvé en : (" + x + ", " + y + ")");
                    send(msg);
                }

                carteGUI.moveExplorer(0, x, y);

                // Déplacement vers la droite, puis descend d’une ligne
                if (x < carteGUI.getWidth() / carteGUI.getCellSize() - 1) {
                    x++;
                } else {
                    x = 0;
                    y++;
                }

                try {
                    Thread.sleep(500);  // Ajoute un délai de 500 ms pour chaque mouvement
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                finished = true;
            }
        }
        /**
         * Permet de déplacer l'explorateur sur la carte de manière aléatoire
         */
        public void move(){
            int direction = (int) (Math.random() * 4);
            switch (direction) {
                case 0: // Droite
                    if (x < carteGUI.getWidth() / carteGUI.getCellSize() - 1) {
                        x++;
                    }
                    break;
                case 1: // Bas
                    if (y < carteGUI.getHeight() / carteGUI.getCellSize() - 1) {
                        y++;
                    }
                    break;
                case 2: // Gauche
                    if (x > 0) {
                        x--;
                    }
                    break;
                case 3: // Haut
                    if (y > 0) {
                        y--;
                    }
                    break;
            }

        }

        public boolean done() {
            return finished;
        }
    }
}
