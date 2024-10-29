import ag.ExplorateurAgent;
import ag.SuperviseurAgent;
import gui.CaillouxGui;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

public class MainContainer {
    public static void main(String[] args) throws ControllerException {
        Runtime rt = Runtime.instance();
        Properties p = new ExtendedProperties();
        p.setProperty("gui", "true");
        ProfileImpl pc = new ProfileImpl(p);

        AgentContainer container =  rt.createMainContainer(pc);
        container.start();
    }
}
