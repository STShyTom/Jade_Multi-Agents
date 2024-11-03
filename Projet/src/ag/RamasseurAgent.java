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
    private boolean enAttente = false;
    private boolean enMission = false;

    @Override
    protected void setup() {
        carteGUI = (CaillouxGui) getArguments()[0];
        this.id = Integer.parseInt(getLocalName().substring(9));
        this.position = new Point(carteGUI.getLongueur() / 2 - 1, carteGUI.getHauteur() / 2 - 1);
        this.positionDepart = new Point((carteGUI.getLongueur() / 2) - 1, (carteGUI.getHauteur() / 2) - 1);
        addBehaviour(new RamassageBehaviour()); // Comportement pour ramasser les cailloux
        addBehaviour(new AttenteBatterieBehaviour()); // Comportement pour gérer l'attente d'un partage de batterie
    }

    public static int getNbCaillouxTotal() {
        return nbCaillouxTotal;
    }

    private class RamassageBehaviour extends CyclicBehaviour {
        private Point positionTas;
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
                batterie = batterie - 2; // Consomme de la batterie
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