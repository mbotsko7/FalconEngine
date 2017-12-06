import java.util.Comparator;

public class MutualInfo {

    private String mTerm;
    private double mValue;

    public MutualInfo(String term, double n) {
        mTerm = term;
        mValue = n;
    }

    public String getTerm() {
        return mTerm;
    }

    public double getValue() {
        return mValue;
    }

    public void setTerm(String s) {
        mTerm = s;
    }

    public void setValue(double n) {
        mValue = n;
    }
}
