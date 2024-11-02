import ag.*;
import gui.CaillouxGui;
import jade.core.AID;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

public class JadeContainer {
    public static void main(String[] args) throws ControllerException {
        CaillouxGui carteGUI = new CaillouxGui(16, 16);
        Runtime rt = Runtime.instance();
        ProfileImpl pc = new ProfileImpl(false);
        pc.setParameter(ProfileImpl.MAIN_HOST, "localhost");

        ContainerController cc =  rt.createAgentContainer(pc); // Crée un nouveau conteneur d'agents

        try {
            Object[] argsGUI = new Object[]{carteGUI};

            // Création des explorateurs
            for (int i = 0; i < 3; i++) {
                AgentController explorateur = cc.createNewAgent("explorateur" + i, ExplorateurAgent.class.getName(), argsGUI);
                explorateur.start();
            }

            // Création des ramasseurs
            for (int i = 0; i < 3; i++) {
                AgentController ramasseur = cc.createNewAgent("ramasseur" + i, RamasseurAgent.class.getName(), argsGUI);
                ramasseur.start();
            }

            // Création du superviseur
            AgentController superviseur = cc.createNewAgent("superviseur", SuperviseurAgent.class.getName(), argsGUI);
            superviseur.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
