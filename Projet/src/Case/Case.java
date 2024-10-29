package Case;

public class Case {

    private boolean accessible;
    private boolean caillou;
    private boolean robot;

    public Case(boolean accessible, boolean caillou, boolean robot) {
        this.accessible = accessible;
        this.caillou = caillou;
        this.robot = robot;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public boolean isCaillou() {
        return caillou;
    }

    public boolean isRobot() {
        return robot;
    }
}
