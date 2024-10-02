package AgentJade;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgentOrganisateur extends Agent {
    @Override
    protected void setup() {
        // Attends un message d'un autre robot
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.or(MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchOntology("position")), MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchOntology("help")
                ));
                ACLMessage message = receive(mt);
                // Si le message est une information sur une position
                if(message.getPerformative() == ACLMessage.INFORM){
                    System.out.println("L'agent " + getLocalName() + " a reçu une information de position de " + message.getSender().getLocalName());
                    // Envoie un message aux robots ramasseurs
                    ACLMessage messagePosition = new ACLMessage(ACLMessage.PROPAGATE);
                    messagePosition.setOntology("position");
                    messagePosition.setContent("L'agent " + message.getSender().getLocalName() + " est à la position " + message.getContent());
                    messagePosition.addReceiver(getAID("ramasseurKailloux"));
                    send(messagePosition);
                }
                // Si le message est une demande d'aide
                else if(message.getPerformative() == ACLMessage.REQUEST){
                    System.out.println("L'agent " + getLocalName() + " a reçu une demande d'aide de " + message.getSender().getLocalName());
                    // Envoie un message aux super chargeurs
                    ACLMessage messageHelp = new ACLMessage(ACLMessage.REQUEST);
                    messageHelp.setOntology("help");
                    messageHelp.setContent("L'agent " + message.getSender().getLocalName() + " a besoin d'aide.");
                    messageHelp.addReceiver(getAID("superChargeur"));
                    send(messageHelp);
                }
                else{
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("L'agent " + getLocalName() + " s'arrête.");
    }

    public void doMove(Location destination) {
        System.out.println("Agent " + getLocalName() + " is moving to " + destination.getName());
        super.doMove(destination);
    }
}
