package ag;

import Case.Case;
import Utils.ClasseUtils;
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
    private final int batterieMax = 100;
    private static int nbCaillouxTotal = 0;
    private boolean enAttente = false;
    private boolean enMission = false;
    private int nbCaillouxPossedes = 0;

    @Override
    protected void setup() {
        carteGUI = (CaillouxGui) getArguments()[0];
        this.id = Integer.parseInt(getLocalName().substring(9));
        this.position = new Point(carteGUI.getLongueur() / 2 - 1, carteGUI.getHauteur() / 2 - 1);
        this.positionDepart = new Point((carteGUI.getLongueur() / 2) - 1, (carteGUI.getHauteur() / 2) - 1);
        addBehaviour(new RamassageBehaviour()); // Comportement pour ramasser les cailloux
        addBehaviour(new AttenteBatterieBehaviour()); // Comportement pour gérer l'attente d'un partage de batterie
    }

    private class RamassageBehaviour extends CyclicBehaviour {
        private Point positionTas;
        public void action() {
            // Empêche le ramassage si l'agent attend une confirmation ou d'être rechargé
            if (enAttente) {
                block();
            } else {
                // Si l'agent est à la base, recharge la batterie
                if (position.equals(positionDepart) && batterie < batterieMax) {
                    ClasseUtils.sleep(4000);  // Ajoute un délai de 4s pour se recharger
                    batterie = batterieMax;
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
                        // Envoie une confirmation au superviseur
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.CONFIRM);
                        reply.setContent("GoCollecte");
                        send(reply);
                    }
                } else if (enMission) {
                    // Si l'agent est arrivé à la position du tas
                    if (position.equals(positionTas)) {
                        // Ramasse des cailloux du tas
                        Case casePierres = carteGUI.getGrille(position.x, position.y);
                        int nbCaillouxTas = casePierres.getNbCailloux();
                        // Nombre de cailloux possédés par l'agent
                        nbCaillouxPossedes = Math.min(3, nbCaillouxTas);
                        casePierres.setNbCailloux(nbCaillouxTas - nbCaillouxPossedes);
                        ClasseUtils.sleep(2000); // Ajoute un délai de 2s pour ramasser
                        batterie -= 3; // Consomme de la batterie

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
                        while (!position.equals(positionDepart) && !enAttente) {
                            deplacement(positionDepart);
                            carteGUI.deplaceRamasseur(id, position.x, position.y); // Met à jour la position de l'agent sur la carte
                            ClasseUtils.sleep(2000); // Ajoute un délai de 2s pour chaque mouvement car plus lourd
                        }

                        // Dépose les cailloux au vaisseau
                        if (position.equals(positionDepart)) {
                            nbCaillouxTotal += nbCaillouxPossedes;
                            carteGUI.setNbCailloux(nbCaillouxTotal);
                        }

                    } else {
                        // Déplacement vers le tas de cailloux
                        deplacement(positionTas);
                        carteGUI.deplaceRamasseur(id, position.x, position.y); // Met à jour la position de l'agent sur la carte
                    }
                    // Si l'agent n'a pas de mission, il retourne à la base
                } else {
                    if (!position.equals(positionDepart)) {
                        deplacement(positionDepart);
                        carteGUI.deplaceRamasseur(id, position.x, position.y); // Met à jour la position de l'agent sur la carte
                    } else {
                        // Si l'agent a un tas enregistré, il repart en mission
                        if (positionTas != null) {
                            enMission = true;
                        }
                    }
                }
                ClasseUtils.sleep(1000); // Ajoute un délai d' 1s entre chaque action
            }
        }

        /**
         * Déplace l'agent vers un robot en panne ou le vaisseau
         * @param destination Point de destination
         */
        private void deplacement(Point destination) {
            // Si la batterie est à 0, appel à l'aide
            if (batterie <= 0 && !enAttente) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(getAID("superviseur"));
                msg.setContent("DemandeAide :" + position.x + "," + position.y);
                send(msg);
                enMission = false; // Arrête la mission en cours
                enAttente = true; // Attend d'être rechargé
                System.out.println("Agent " + getLocalName() + " envoie une demande d'aide");

            // Si la batterie est inférieure à 20, l'explorateur retourne à la base pour se recharger
            } else if (batterie > 0 && batterie <= 20) {
                ClasseUtils.deplacement(position, positionDepart, carteGUI);
                // Consomme de la batterie
                if(!CaillouxGui.getGrille(position.x, position.y).isAccessible())
                    batterie -= (1 + nbCaillouxPossedes + 3);
                else
                    batterie -= (1 + nbCaillouxPossedes);

            } else if(batterie > 20) {
                ClasseUtils.deplacement(position, destination, carteGUI);
                // Consomme de la batterie
                if(!CaillouxGui.getGrille(position.x, position.y).isAccessible())
                    batterie -= (1 + nbCaillouxPossedes + 3);
                else
                    batterie -= (1 + nbCaillouxPossedes);
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
                    batterie = Integer.parseInt(msg.getContent().split(":")[1]);
                    enAttente = false;
                } else {
                    block();
                }
            }
            ClasseUtils.sleep(500);
        }
    }
}