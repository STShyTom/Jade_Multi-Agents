import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

public class JadeContainer{
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            ProfileImpl profile = new ProfileImpl(false);
            profile.setParameter(ProfileImpl.MAIN_HOST, "localhost");
            profile.setParameter(ProfileImpl.MAIN_PORT, "1099");

            AgentContainer mainContainer = rt.createAgentContainer(profile);
            AgentController agentController = mainContainer.createNewAgent(
                    "Acheteur1", "AgentJade.Acheteur", new Object[]{"XML"});
            agentController.start();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
}