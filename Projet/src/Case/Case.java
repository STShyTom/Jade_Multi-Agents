package Case;

public class Case {

    private final boolean caseVaisseau;
    private final boolean isAccessible;
    private int nbCailloux;

    public Case(boolean vaisseau, int caillou, boolean isAccessible) {
        this.caseVaisseau = vaisseau;
        this.nbCailloux = caillou;
        this.isAccessible = isAccessible;
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
    public boolean isAccessible() {
        return isAccessible;
    }
}
