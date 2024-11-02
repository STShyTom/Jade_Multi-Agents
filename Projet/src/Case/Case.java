package Case;

public class Case {

    private final boolean caseVaisseau;
    private final boolean possedeCailloux;
    private final boolean robot;

    public Case(boolean vaisseau, boolean caillou, boolean robot) {
        this.caseVaisseau = vaisseau;
        this.possedeCailloux = caillou;
        this.robot = robot;
    }

    public boolean isCaseVaisseau() {
        return caseVaisseau;
    }

    public boolean hasCaillou() {
        return possedeCailloux;
    }

    public boolean hasRobot() {
        return robot;
    }
}
