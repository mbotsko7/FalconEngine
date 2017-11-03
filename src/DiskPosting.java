import java.util.ArrayList;

public class DiskPosting {
    private int docID;
    private int termFrequency;
    private ArrayList<Integer> positions;

    // used to get postings with positions
    public DiskPosting(int d, int t, ArrayList<Integer> p) {
        docID = d;
        termFrequency = t;
        positions = p;
    }

    public int getDocID() {
        return docID;
    }

    public void setDocID(int docID) {
        this.docID = docID;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public void setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
    }

    public ArrayList<Integer> getPositions() {
        return positions;
    }

    public void setPositions(ArrayList<Integer> positions) {
        this.positions = positions;
    }

    public void addPosition(int p) {
        positions.add(p);
    }
}
