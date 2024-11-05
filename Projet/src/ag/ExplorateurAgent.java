package ag;

import Utils.ClasseUtils;
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
    private int batterie = 80;
    private final int batterieMax = 80;
    private boolean enAttente = false;
    private Point positionDepart;

    @Override
    protected void setup() {
        this.carteGUI = (CaillouxGui) getArguments()[0];
        this.id = Integer.parseInt(getLocalName().substring(11));
        this.position = new Point(carteGUI.getLongueur() / 2, (carteGUI.getHauteur() / 2) - 1);
        this.positionDepart = new Point(carteGUI.getLongueur() / 2, (carteGUI.getHauteur() / 2) - 1);
        addBehaviour(new ExplorationBehaviour()); // Comportement pour explorer la carte
        addBehaviour(new AttenteBehaviour()); // Comportement pour gérer l'attente de confirmation ou de partage de batterie
    }

    /**
     * Comportement pour explorer la carte
     */
    private class ExplorationBehaviour extends CyclicBehaviour {
        private boolean doitRepartir = false;
        public void action() {
            // Bloque l'exploration si l'agent attend une confirmation ou d'être rechargé
            if (enAttente) {
                return;
            }
            // Si l'agent est à la base, recharge la batterie
            if (position.equals(positionDepart) && batterie < batterieMax) {
                ClasseUtils.sleep(4000);  // Ajoute un délai de 4s pour se recharger
                batterie = batterieMax;
            }
            // Si un tas de pierres est trouvé, envoie la position au superviseur
            if (carteGUI.getGrille(position.x,position.y).getNbCailloux() > 0 && !doitRepartir) { // Tas de pierres trouvé
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(getAID("superviseur"));
                msg.setContent("PositionTas :" + position.x + "," + position.y);
                send(msg);
                enAttente = true;
                doitRepartir = true;
            } else {
                deplacement(); // Déplace l'explorateur
                carteGUI.deplaceExplorateur(id, position.x, position.y); // Met à jour la position de l'explorateur sur la carte
                doitRepartir = false;
            }

            ClasseUtils.sleep(1000); // Ajoute un délai de 1s entre chaque action
        }
        /**
         * Permet de déplacer l'explorateur sur la carte de manière aléatoire
         */
        public void deplacement() {
            // Si la batterie est à 0, appel à l'aide
            if (batterie == 0) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(getAID("superviseur"));
                msg.setContent("DemandeAide :" + position.x + "," + position.y);
                send(msg);
                enAttente = true;
                System.out.println("Agent " + getLocalName() + " envoie une demande d'aide");

            // Si la batterie est inférieure à 15, l'explorateur retourne à la base pour se recharger
            } else if(batterie > 0 && batterie <= 15) {
                ClasseUtils.deplacement(position, positionDepart, carteGUI);
                // Consomme de la batterie
                if(!CaillouxGui.getGrille(position.x, position.y).isAccessible())
                    batterie -= (1 + 3);
                else
                    batterie -= 1;

            } else {
                int direction = (int) (Math.random() * 4);
                switch (direction) {
                    case 0: // Droite
                        if ((position.x < carteGUI.getWidth() / carteGUI.getTailleCellule() - 1) &&
                                ! carteGUI.getCasesVaisseau().contains(new Point(position.x + 1, position.y)) &&
                                carteGUI.getGrille(position.x + 1, position.y).isAccessible()) {
                            position.x++;
                        } else {
                            deplacement();
                        }
                        break;
                    case 1: // Bas
                        if ((position.y < carteGUI.getHeight() / carteGUI.getTailleCellule() - 1) &&
                                ! carteGUI.getCasesVaisseau().contains(new Point(position.x, position.y + 1)) &&
                                carteGUI.getGrille(position.x, position.y + 1).isAccessible()) {
                            position.y++;
                        } else {
                            deplacement();
                        }
                        break;
                    case 2: // Gauche
                        if ((position.x > 0) &&
                                ! carteGUI.getCasesVaisseau().contains(new Point(position.x - 1, position.y)) &&
                                carteGUI.getGrille(position.x - 1, position.y).isAccessible()) {
                            position.x--;
                        } else {
                            deplacement();
                        }
                        break;
                    case 3: // Haut
                        if ((position.y > 0) &&
                                ! carteGUI.getCasesVaisseau().contains(new Point(position.x, position.y - 1)) &&
                                carteGUI.getGrille(position.x, position.y - 1).isAccessible()) {
                            position.y--;
                        } else {
                            deplacement();
                        }
                        break;
                }
                // Consomme de la batterie
                if(!CaillouxGui.getGrille(position.x, position.y).isAccessible())
                    batterie -= (1 + 3);
                else
                    batterie -= 1;
            }
        }
    }

    private class AttenteBehaviour extends CyclicBehaviour {
        public void action() {
            if (enAttente) {
                ACLMessage msg = receive();
                // Attend la confirmation du superviseur pour reprendre l'exploration
                if (msg != null && msg.getPerformative() == ACLMessage.CONFIRM) {
                    enAttente = false;
                // Attend un partage de batterie d'un chargeur
                } else if (msg != null && msg.getPerformative() == ACLMessage.PROPOSE) {
                    batterie += Integer.parseInt(msg.getContent().split(":")[1]);
                    enAttente = false;
                }
            }
        }
    }
}
