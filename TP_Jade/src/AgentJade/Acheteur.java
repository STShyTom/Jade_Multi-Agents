package AgentJade;

import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Acheteur extends Agent {
    private String livre;
    @Override
    protected void setup() {
        Object [] args = getArguments();
        if(args.length == 1){
            livre = (String) args[0];
            System.out.println("L'agent " + getLocalName() + " veut acheter le livre " + livre);
            addBehaviour(new TickerBehaviour(this,20000) {
                private int compteur = 0;
                @Override
                protected void onTick() {
                    ++compteur;
                    System.out.println("L'agent " + getLocalName() + " a attendu " + compteur + " secondes");
                }
            });
            addBehaviour(new CyclicBehaviour() {
                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.and(
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchOntology("achat"));
                    ACLMessage message = receive(mt);
                    if(message != null){
                        System.out.println("L'agent " + getLocalName() + " a reçu le message : " + message.getContent());
                    }
                    else{
                        block();
                    }
                }
            });
        }
        else{
            System.out.println("Veuillez spécifier le livre à acheter en argument.");
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Agent " + getLocalName() + " is shutting down");
    }

    public void doMove(Location destination) {
        System.out.println("Agent " + getLocalName() + " is moving to " + destination.getName());
        super.doMove(destination);
    }
}
