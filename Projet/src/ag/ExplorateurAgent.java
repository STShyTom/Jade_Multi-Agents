package ag;

import Case.Pierre;
import gui.CaillouxGui;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.awt.*;

/**
 * Agent explorateur qui explore la carte pour trouver des cailloux
 */
public class ExplorateurAgent extends Agent {
    private CaillouxGui carteGUI;
    // Identifiant de l'agent
    private int id;
    // Position de l'agent sur la carte
    private Point position;
    // Niveau de batterie de l'agent
    private int batterie = 100;
    private boolean enAttente = false;

    @Override
    protected void setup() {
        this.carteGUI = (CaillouxGui) getArguments()[0];
        this.id = Integer.parseInt(getLocalName().substring(11));
        this.position = new Point(carteGUI.getLongueur() / 2, carteGUI.getHauteur() / 2);
        addBehaviour(new ExplorationBehaviour()); // Comportement pour explorer la carte
        addBehaviour(new AttenteConfirmationBehaviour()); // Comportement pour gérer la confirmation
    }

    /**
     * Comportement pour explorer la carte
     */
    private class ExplorationBehaviour extends CyclicBehaviour {
        private boolean doitRepartir = false;
        public void action() {
            // Bloque l'exploration si l'agent attend une confirmation
            if (enAttente) {
                return;
            }

            if (position.x < carteGUI.getWidth() / carteGUI.getTailleCellule() &&
                    position.y < carteGUI.getHeight() / carteGUI.getTailleCellule()) {
                if (carteGUI.getGrille(position.x,position.y).getClass() == Pierre.class && !doitRepartir) { // Tas de pierres trouvé
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(getAID("superviseur"));
                    msg.setContent("Tas de pierres trouvé en : (" + position.x + ", " + position.y + ")");
                    //System.out.println(msg.getContent());
                    send(msg);
                    enAttente = true;
                    doitRepartir = true;
                } else {
                    deplacement(); // Déplace l'explorateur
                    carteGUI.deplaceExplorateur(id, position.x, position.y); // Met à jour la position de l'explorateur sur la carte
                    batterie--; // Consomme de la batterie
                    doitRepartir = false;
                }

                try {
                    Thread.sleep(500);  // Ajoute un délai de 500 ms pour chaque mouvement
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        /**
         * Permet de déplacer l'explorateur sur la carte de manière aléatoire
         */
        public void deplacement(){
            int direction = (int) (Math.random() * 4);
            switch (direction) {
                case 0: // Droite
                    if ((position.x < carteGUI.getWidth() / carteGUI.getTailleCellule() - 1) &&
                        ! carteGUI.getCasesVaisseau().contains(new Point(position.x + 1, position.y))) {
                        position.x++;
                    } else {
                        deplacement();
                    }
                    break;
                case 1: // Bas
                    if ((position.y < carteGUI.getHeight() / carteGUI.getTailleCellule() - 1) &&
                        ! carteGUI.getCasesVaisseau().contains(new Point(position.x, position.y + 1))) {
                        position.y++;
                    } else {
                        deplacement();
                    }
                    break;
                case 2: // Gauche
                    if ((position.x > 0) && ! carteGUI.getCasesVaisseau().contains(new Point(position.x - 1, position.y))) {
                        position.x--;
                    } else {
                        deplacement();
                    }
                    break;
                case 3: // Haut
                    if ((position.y > 0) && ! carteGUI.getCasesVaisseau().contains(new Point(position.x, position.y - 1))) {
                        position.y--;
                    } else {
                        deplacement();
                    }
                    break;
            }
        }
    }

    /**
     * Comportement pour attendre la confirmation du superviseur
     */
    private class AttenteConfirmationBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            // Attend la confirmation du superviseur pour reprendre l'exploration
            if (msg != null && msg.getPerformative() == ACLMessage.CONFIRM) {
                enAttente = false;
            }
        }
    }
}
