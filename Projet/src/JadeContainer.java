import ag.ExplorateurAgent;
import ag.SuperviseurAgent;
import gui.CaillouxGui;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

public class JadeContainer {
    public static void main(String[] args) throws ControllerException {
        CaillouxGui carteGUI = new CaillouxGui(10, 10);
        Runtime rt = Runtime.instance();
        ProfileImpl pc = new ProfileImpl(false);
        pc.setParameter(ProfileImpl.MAIN_HOST, "localhost");

        ContainerController cc =  rt.createAgentContainer(pc);

        try {
            Object[] argsGUI = new Object[]{carteGUI};

            AgentController superviseur = cc.createNewAgent("superviseur", SuperviseurAgent.class.getName(), argsGUI);
            superviseur.start();

            AgentController explorateur = cc.createNewAgent("explorateur", ExplorateurAgent.class.getName(), argsGUI);
            explorateur.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
