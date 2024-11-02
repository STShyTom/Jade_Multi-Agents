package ag;

import Case.Case;
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
    private Point positionDepart;
    // Niveau de batterie de l'agent
    private int batterie = 100;
    private static int nbCaillouxTotal = 0;
    private boolean enMission = false;

    @Override
    protected void setup() {
        carteGUI = (CaillouxGui) getArguments()[0];
        this.id = Integer.parseInt(getLocalName().substring(9));
        this.position = new Point(carteGUI.getLongueur() / 2 - 1, carteGUI.getHauteur() / 2 - 1);
        this.positionDepart = new Point(carteGUI.getLongueur() / 2 - 1, carteGUI.getHauteur() / 2 - 1);
        addBehaviour(new RamassageBehaviour());
    }

    public static int getNbCaillouxTotal() {
        return nbCaillouxTotal;
    }

    private class RamassageBehaviour extends CyclicBehaviour {
        private Point positionTas;
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                // Réception d'une position de tas à ramasser
                if (msg.getPerformative() == ACLMessage.REQUEST &&
                        msg.getSender().getLocalName().equals("superviseur") && msg.getContent().contains("DemandeCollecte")) {
                    String messageRecu = msg.getContent();
                    // Récupère les coordonnées du tas de pierres
                    String coordonnees = messageRecu.split(":")[1];
                    positionTas = new Point(
                            Integer.parseInt(coordonnees.split(",")[0]),
                            Integer.parseInt(coordonnees.split(",")[1])
                    );
                    enMission = true;
                }
            } else if (enMission) {
                // Si l'agent est arrivé à la position du tas
                if (position.equals(positionTas)) {
                    // Ramasse des cailloux du tas
                    Case casePierres = carteGUI.getGrille(position.x, position.y);
                    int nbCaillouxTas = casePierres.getNbCailloux();
                    // Nombre de cailloux possédés par l'agent
                    int nbCaillouxPossedes = Math.min(3, nbCaillouxTas);
                    casePierres.setNbCailloux(nbCaillouxTas - nbCaillouxPossedes);

                    // S'il ne reste plus de cailloux dans le tas
                    if (casePierres.getNbCailloux() == 0) {
                        // La mission est terminée.
                        enMission = false;
                        positionTas = null;
                        // Envoyer une confirmation au superviseur
                        ACLMessage confirm = new ACLMessage(ACLMessage.CONFIRM);
                        confirm.addReceiver(getAID("superviseur"));
                        confirm.setContent("CaillouxRecup :" + position.x + "," + position.y);
                        send(confirm);
                    }

                    // Retourne au vaisseau pour déposer les cailloux
                    while (!position.equals(positionDepart)) {
                        deplacement(positionDepart);
                        carteGUI.deplaceRamasseur(id, position.x, position.y); // Met à jour la position de l'agent sur la carte
                        try {
                            Thread.sleep(2000);  // Ajoute un délai d'2s pour chaque mouvement (plus lourd donc plus de temps)
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // Dépose les cailloux au vaisseau
                    nbCaillouxTotal += nbCaillouxPossedes;

                } else {
                    // Déplacement vers le tas de cailloux
                    deplacement(positionTas);
                    carteGUI.deplaceRamasseur(id, position.x, position.y); // Met à jour la position de l'agent sur la carte
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
         * Déplace l'agent vers un tas de cailloux ou le vaisseau
         * @param destination Point de destination
         */
        private void deplacement(Point destination) {
            if (position.x < destination.x) {
                position.x++;
            } else if (position.x > destination.x) {
                position.x--;
            } else if (position.y < destination.y) {
                position.y++;
            } else if (position.y > destination.y) {
                position.y--;
            }
        }
    }
}