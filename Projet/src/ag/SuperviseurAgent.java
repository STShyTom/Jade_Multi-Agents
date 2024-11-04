package ag;

import Utils.ClasseUtils;
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
    // Liste des tas de pierres trouvés avec leur position et s'ils sont en cours de collecte
    private final HashMap<Point, Boolean> tasPierres = new HashMap<>();
    // Liste des agents en panne avec leur position
    private final HashMap<String,Point> agentsEnPanne = new HashMap<>();
    // Liste des ramasseurs
    private final ArrayList<String> ramasseurs = new ArrayList<>();
    // Liste des superchargeurs
    private final ArrayList<String> superChargeurs = new ArrayList<>();
    // Nombre de cailloux total ramassé
    private int nbCaillouxRamasses = 0;

    @Override
    protected void setup() {
        for (int i = 0; i < 5; i++) {
            ramasseurs.add("ramasseur" + i);
        }
        for (int i = 0; i < 2; i++) {
            superChargeurs.add("superchargeur" + i);
        }
        addBehaviour(new ReceptionMessageBehaviour());
        addBehaviour(new GestionRamasseursBehaviour());
        addBehaviour(new GestionSuperchargeursBehaviour());
    }

    /**
     * Comportement pour gérer la réception des messages
     */
    private class ReceptionMessageBehaviour extends CyclicBehaviour {

        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                // Message reçu de l'explorateur pour signaler un tas de pierres
                if (msg.getPerformative() == ACLMessage.INFORM &&
                        msg.getContent().contains("PositionTas")) {
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
                }
                // Réception de la confirmation d'un ramasseur
                else if (msg.getPerformative() == ACLMessage.CONFIRM &&
                        msg.getSender().getLocalName().contains("ramasseur")) {
                    Point positionTas = new Point(
                            Integer.parseInt(msg.getContent().split(":")[1].split(",")[0]),
                            Integer.parseInt(msg.getContent().split(":")[1].split(",")[1])
                    );
                    // Il n'y a plus de cailloux dans le tas, le ramasseur peut repartir ailleurs
                    tasPierres.remove(positionTas);
                    ramasseurs.add(msg.getSender().getLocalName());
                }
                // Réception d'une demande de recharge
                else if (msg.getPerformative() == ACLMessage.REQUEST &&
                        msg.getContent().contains("DemandeAide")) {
                    // Récupère les coordonnées de l'agent en panne
                    String coordonnees = msg.getContent().split(":")[1];
                    Point positionRobotEnPanne = new Point(
                            Integer.parseInt(coordonnees.split(",")[0]),
                            Integer.parseInt(coordonnees.split(",")[1])
                    );
                    agentsEnPanne.put(msg.getSender().getLocalName(), positionRobotEnPanne);
                }
                // Réception de la confirmation d'un superchargeur
                else if (msg.getPerformative() == ACLMessage.CONFIRM &&
                        msg.getSender().getLocalName().contains("superchargeur")) {
                    superChargeurs.add(msg.getSender().getLocalName());
                }
            }

            ClasseUtils.sleep(500);
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
                    break;
                }
            }
            if (tasNonCollecte && !ramasseurs.isEmpty()) {
                tasPierres.put(tas, true);
                String ramasseur = ramasseurs.remove(0);
                ACLMessage mission = new ACLMessage(ACLMessage.REQUEST);
                mission.addReceiver(getAID(ramasseur));
                mission.setContent("DemandeCollecte :" + tas.x + "," + tas.y);
                send(mission);

            }
            ClasseUtils.sleep(1000);
        }
    }

    /**
     * Comportement pour gérer l'envoi des missions de recharge aux superchargeurs
     */
    private class GestionSuperchargeursBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            // Envoie un superchargeur si un agent est en panne
            if (!agentsEnPanne.isEmpty() && !superChargeurs.isEmpty()) {
                String superChargeur = superChargeurs.remove(0);
                String agentEnPanne = agentsEnPanne.keySet().iterator().next();
                Point positionAgentEnPanne = agentsEnPanne.get(agentEnPanne);
                agentsEnPanne.remove(agentEnPanne);
                // Envoie un message de mission
                ACLMessage mission = new ACLMessage(ACLMessage.REQUEST);
                mission.addReceiver(getAID(superChargeur));
                mission.setContent("DemandeRecharge :" + positionAgentEnPanne.x + "," + positionAgentEnPanne.y + ";" + agentEnPanne);
                send(mission);
                System.out.println("Mission de recharge envoyée à " + superChargeur + " pour l'agent en " + positionAgentEnPanne);
            }
        }
    }
}
