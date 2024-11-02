package Case;

public class Case {

    protected final boolean caseVaisseau;
    protected final boolean robot;
    protected int nbCailloux;

    public Case(boolean vaisseau, int caillou, boolean robot) {
        this.caseVaisseau = vaisseau;
        this.nbCailloux = caillou;
        this.robot = robot;
    }

    public boolean isCaseVaisseau() {
        return caseVaisseau;
    }
    public int getNbCailloux() {
        return nbCailloux;
    }
    public void setNbCailloux(int nbCailloux) {
        this.nbCailloux = nbCailloux;
    }
    public boolean hasRobot() {
        return robot;
    }
}
