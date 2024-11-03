package ag;

import gui.CaillouxGui;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.awt.*;

/**
 * Agent super chargeur qui recharge les agents en batterie
 */
public class SuperChargeurAgent extends Agent {
    private CaillouxGui carteGUI;
    // Identifiant de l'agent
    private int id;
    // Position de l'agent sur la carte
    private Point position;
    private Point positionDepart;
    // Niveau de batterie de l'agent
    private int batterie = 100;
    private boolean enAttente = false;
    private boolean enMission = false;

    @Override
    protected void setup() {
        this.carteGUI = (CaillouxGui) getArguments()[0];
        this.id = Integer.parseInt(getLocalName().substring(13));
        this.position = new Point((carteGUI.getLongueur() / 2) - 1, carteGUI.getHauteur() / 2);
        this.positionDepart = new Point((carteGUI.getLongueur() / 2) - 1, carteGUI.getHauteur() / 2);
        addBehaviour(new RechargeBehaviour()); // Comportement pour recharger les agents
        addBehaviour(new AttenteBatterieBehaviour()); // Comportement pour gérer l'attente d'un partage de batterie
    }

    /**
     * Comportement pour recharge les agents en panne
     */
    private class RechargeBehaviour extends CyclicBehaviour {
        private Point positionRobotEnPanne;
        private String nomRobotEnPanne;

        public void action() {
            // Bloque l'exploration si l'agent attend une confirmation ou d'être rechargé
            if (enAttente) {
                return;
            }

            // Si l'agent est à la base, recharge la batterie
            if (position.equals(positionDepart) && batterie < 100) {
                try {
                    Thread.sleep(5000);  // Ajoute un délai de 5s pour se recharger
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                batterie = 100;
            }

            // Si un agent est en panne, recharge sa batterie
            ACLMessage msg = receive();
            if (msg != null) {
                // Réception d'une position de robot à aller aider
                if (msg.getPerformative() == ACLMessage.REQUEST &&
                        msg.getSender().getLocalName().equals("superviseur") && msg.getContent().contains("DemandeRecharge")) {
                    String messageRecu = msg.getContent();
                    // Récupère les coordonnées du robot
                    String coordonnees = messageRecu.split(":")[1];
                    coordonnees = coordonnees.split(";")[0];
                    positionRobotEnPanne = new Point(
                            Integer.parseInt(coordonnees.split(",")[0]),
                            Integer.parseInt(coordonnees.split(",")[1])
                    );
                    nomRobotEnPanne = messageRecu.split(";")[1];
                    enMission = true;
                }
            } else if (enMission) {
                // Si l'agent est arrivé à la position du robot en panne
                if (position.equals(positionRobotEnPanne)) {
                    // Partage sa batterie avec le.s robot.s en panne

                    ACLMessage msgBatterie = new ACLMessage(ACLMessage.PROPOSE);
                    msgBatterie.addReceiver(getAID(nomRobotEnPanne));
                    msgBatterie.setContent("PartageBatterie :" + Math.floorDiv(batterie, 2));
                    send(msgBatterie);
                    batterie = Math.floorDiv(batterie, 2);
                    System.out.println("Agent " + getLocalName() + " partage sa batterie avec " + nomRobotEnPanne + " : " + batterie);

                    while (!position.equals(positionDepart)) {
                        deplacement(positionDepart);
                        carteGUI.deplaceChargeur(id, position.x, position.y); // Met à jour la position de l'agent sur la carte
                        try {
                            Thread.sleep(1000);  // Ajoute un délai d'1s
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    enMission = false;

                } else {
                    // Déplacement vers le robot en panne
                    deplacement(positionRobotEnPanne);
                    carteGUI.deplaceChargeur(id, position.x, position.y); // Met à jour la position de l'agent sur la carte
                }
            } else {
                block();
            }

            try {
                Thread.sleep(1000);  // Ajoute un délai d'1s pour chaque mouvement
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Déplace l'agent vers un robot en panne ou le vaisseau
         * @param destination Point de destination
         */
        private void deplacement(Point destination) {
            // Si la batterie est à 0, appel à l'aide
            if (batterie == 0) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(getAID("superviseur"));
                msg.setContent("DemandeAide :" + position.x + "," + position.y);
                send(msg);
                enAttente = true;
                System.out.println("Agent " + getLocalName() + " envoie une demande d'aide");

            // Si la batterie est inférieure à 20, l'explorateur retourne à la base pour se recharger
            } else if(batterie < 20) {
                if (position.x < positionDepart.x) {
                    position.x++;
                } else if (position.x > positionDepart.x) {
                    position.x--;
                } else if (position.y < positionDepart.y) {
                    position.y++;
                } else if (position.y > positionDepart.y) {
                    position.y--;
                }
                batterie--; // Consomme de la batterie
            } else {
                if (position.x < destination.x) {
                    position.x++;
                } else if (position.x > destination.x) {
                    position.x--;
                } else if (position.y < destination.y) {
                    position.y++;
                } else if (position.y > destination.y) {
                    position.y--;
                }
                batterie--; // Consomme de la batterie
            }
        }
    }

    /**
     * Comportement pour attendre un partage de batterie
     */
    private class AttenteBatterieBehaviour extends CyclicBehaviour {
        public void action() {
            if (enAttente) {
                ACLMessage msg = receive();
                // Attend qu'on me partage de la batterie
                if (msg != null && msg.getPerformative() == ACLMessage.PROPOSE) {
                    batterie += Integer.parseInt(msg.getContent());
                    enAttente = false;
                }
            }
        }
    }
}
