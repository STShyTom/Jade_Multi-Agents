package ag;

import gui.CaillouxGui;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Agent superviseur qui gère la communication entre les agents
 */
public class SuperviseurAgent extends Agent {
    private CaillouxGui carteGUI;
    // Liste des tas de pierres trouvés avec leur position et s'ils sont en cours de collecte
    private HashMap<Point, Boolean> tasPierres = new HashMap<>();
    // Liste des ramasseurs
    private ArrayList<String> ramasseurs = new ArrayList<>();
    // Nombre de cailloux total ramassé
    private int nbCaillouxRamasses = 0;

    @Override
    protected void setup() {
        this.carteGUI = (CaillouxGui) getArguments()[0];
        for (int i = 0; i < 5; i++) {
            ramasseurs.add("ramasseur" + i);
        }
        addBehaviour(new ReceptionCaillouBehaviour());
        addBehaviour(new GestionRamasseursBehaviour());
    }

    private class ReceptionCaillouBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                // Message reçu de l'explorateur pour signaler un tas de pierres
                if (msg.getPerformative() == ACLMessage.INFORM &&
                        msg.getSender().getLocalName().contains("explorateur")) {
                    // Récupère les coordonnées du tas de pierres
                    String coordonnees = msg.getContent().split(":")[1];
                    Point position = new Point(
                            Integer.parseInt(coordonnees.split(",")[0]),
                            Integer.parseInt(coordonnees.split(",")[1])
                    );
                    if (!tasPierres.containsKey(position)) {
                        tasPierres.put(position, false);
                    }

                    // Envoie une confirmation à l'explorateur
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.CONFIRM);
                    send(reply);
                    //System.out.println("Confirmation pour repartir");
                }
                // Réception de la confirmation d'un ramasseur
                if (msg.getPerformative() == ACLMessage.CONFIRM) {
                    Point positionTas = new Point(
                            Integer.parseInt(msg.getContent().split(":")[1].split(",")[0]),
                            Integer.parseInt(msg.getContent().split(":")[1].split(",")[1])
                    );
                    // Il n'y a plus de cailloux dans le tas, le ramasseur peut repartir ailleurs
                    tasPierres.remove(positionTas);
                    ramasseurs.add(msg.getSender().getLocalName());
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
    }

    /**
     * Comportement pour gérer l'envoi des missions de collecte aux ramasseurs
     */
    private class GestionRamasseursBehaviour extends CyclicBehaviour {

        public void action() {
            // On regarde si des tas sont repérés et s'ils ne sont pas déjà en cours de collecte
            boolean tasNonCollecte = false;
            Point tas = new Point();
            for (Point t : tasPierres.keySet()) {
                if (!tasPierres.get(t)) {
                    tasNonCollecte = true;
                    tas = t;
                    tasPierres.put(t, true);
                    break;
                }
            }
            if (tasNonCollecte && !ramasseurs.isEmpty()) {
                String ramasseur = ramasseurs.removeFirst();
                ACLMessage mission = new ACLMessage(ACLMessage.REQUEST);
                mission.addReceiver(getAID(ramasseur));
                mission.setContent("DemandeCollecte :" + tas.x + "," + tas.y);
                send(mission);
                System.out.println("Mission de collecte envoyée à " + ramasseur + " pour le tas en " + tas);

            }
            try {
                Thread.sleep(1000);  // Ajoute un délai d'1 s pour chaque mouvement
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
