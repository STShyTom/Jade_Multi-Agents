package Case;

public class Pierre extends Case {

    private int nbCailloux;
    public Pierre() {
        super(true, true, false);
        this.nbCailloux = (int)(Math.random() * 6) +1;
    }

    public int getNbCailloux() {
        return nbCailloux;
    }

    public void setNbCailloux(int nbCailloux) {
        this.nbCailloux = nbCailloux;
    }

    public void enleverCailloux(){
        this.nbCailloux--;
    }
}
