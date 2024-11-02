package ag;

import gui.CaillouxGui;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.ArrayList;

/**
 * Agent superviseur qui gère la communication entre les agents
 */
public class SuperviseurAgent extends Agent {
    private CaillouxGui carteGUI;
    // Liste des tas de pierres trouvés
    private ArrayList<Point> tasPierres = new ArrayList<>();
    // Liste des ramasseurs
    private ArrayList<String> ramasseurs = new ArrayList<>();

    @Override
    protected void setup() {
        this.carteGUI = (CaillouxGui) getArguments()[0];
        for (int i = 0; i < 3; i++) {
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
                    String messageRecu = msg.getContent();
                    // Récupère les coordonnées du tas de pierres
                    String coordonnees = messageRecu.split(":")[1];
                    Point position = new Point(
                            Integer.parseInt(coordonnees.split(",")[0]),
                            Integer.parseInt(coordonnees.split(",")[1])
                    );
                    if (!tasPierres.contains(position)) {
                        tasPierres.add(position);
                    }
                    // Envoie une conformation à l'explorateur
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.CONFIRM);
                    send(reply);
                    //System.out.println("Confirmation pour repartir");
                }
                // Réception de la confirmation d'un ramasseur
                if (msg.getPerformative() == ACLMessage.CONFIRM) {
                    System.out.println("Confirmation de collecte reçue de " + msg.getSender().getLocalName());
                    ramasseurs.add(msg.getSender().getLocalName());
                }
            } else {
                block();
            }
        }
    }

    /**
     * Comportement pour gérer l'envoi des missions de collecte aux ramasseurs
     */
    private class GestionRamasseursBehaviour extends CyclicBehaviour {

        public void action() {
            // Des tas de pierres sont détectés et des ramasseurs sont disponibles
            if (!tasPierres.isEmpty() && !ramasseurs.isEmpty()) {
                Point tas = tasPierres.remove(0);
                String ramasseur = ramasseurs.remove(0);
                ACLMessage mission = new ACLMessage(ACLMessage.REQUEST);
                mission.addReceiver(getAID(ramasseur));
                mission.setContent("DemandeCollecte :" + tas.x + "," + tas.y);
                send(mission);
                System.out.println("Mission de collecte envoyée à " + ramasseur);

            }
        }
    }
}
