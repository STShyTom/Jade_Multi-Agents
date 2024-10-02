import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.AgentContainer;
import jade.core.ProfileImpl;
import jade.core.Runtime;

public class Main {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            Properties p = new ExtendedProperties();
            p.setProperty("gui", "true");
            ProfileImpl profile = new ProfileImpl(p);

            AgentContainer mainContainer = rt.createMainContainer(profile);
            mainContainer.start();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
}