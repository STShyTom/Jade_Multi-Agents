package ag;

import gui.CaillouxGui;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.awt.*;

/**
 * Agent ramasseur qui récupère les cailloux
 */
public class RamasseurAgent extends Agent {
    private CaillouxGui carteGUI;
    // Identifiant de l'agent
    private int id;
    // Position de l'agent sur la carte
    private Point position;
    // Niveau de batterie de l'agent
    private int batterie = 100;
    private boolean enMission = false;

    @Override
    protected void setup() {
        carteGUI = (CaillouxGui) getArguments()[0];
        this.position = new Point(carteGUI.getLongueur() / 2 - 1, carteGUI.getHauteur() / 2 - 1);
        addBehaviour(new RamassageBehaviour());
    }

    private class RamassageBehaviour extends CyclicBehaviour {
        private Point positionTas;
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                // Réception d'une position de tas à ramasser
                if (msg.getPerformative() == ACLMessage.REQUEST &&
                        msg.getSender().getLocalName().equals("superviseur")) {
                    String messageRecu = msg.getContent();
                    // Récupère les coordonnées du tas de pierres
                    String coordonnees = messageRecu.substring(32);
                    positionTas = new Point(
                            Integer.parseInt(coordonnees.substring(1, coordonnees.indexOf(','))),
                            Integer.parseInt(coordonnees.substring(coordonnees.indexOf(',') + 2, coordonnees.length() - 1))
                    );
                    enMission = true;
                }
            } else if (enMission) {
                // Si l'agent est arrivé à la position du tas
                if (position.equals(positionTas)) {
                    // Ramasse le tas de cailloux
                    // TODO
                    System.out.println(getLocalName() + " a ramassé le tas de cailloux en " + positionTas);
                    // Envoyer une confirmation au superviseur
                    ACLMessage confirm = new ACLMessage(ACLMessage.CONFIRM);
                    confirm.addReceiver(getAID("superviseur"));
                    confirm.setContent("Tas de cailloux ramassé en : (" + position.x + ", " + position.y + ")");
                    send(confirm);
                    positionTas = null;

                    // Retourne au vaisseau pour déposer les cailloux
                    deplacement(new Point(carteGUI.getLongueur() / 2 - 1, carteGUI.getHauteur() / 2 - 1));
                    carteGUI.deplaceRamasseur(id, position.x, position.y); // Met à jour la position de l'agent sur la carte
                    enMission = false;
                    System.out.println(getLocalName() + " est retourné au vaisseau");

                } else {
                    // Déplacement vers le tas de cailloux
                    deplacement(positionTas);
                    carteGUI.deplaceRamasseur(id, position.x, position.y); // Met à jour la position de l'agent sur la carte
                }
            } else {
                block();
            }

            try {
                Thread.sleep(500);  // Ajoute un délai de 500 ms pour chaque mouvement
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Déplace l'agent vers un tas de cailloux ou le vaisseau
         * @param destination Point de destination
         */
        private void deplacement(Point destination) {
            if (position.x < destination.x) {
                position.x++;
            } else if (position.x > destination.x) {
                position.x--;
            }

            if (position.y < destination.y) {
                position.y++;
            } else if (position.y > destination.y) {
                position.y--;
            }
        }
    }
}