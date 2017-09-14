import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by bardsko on 9/14/17.
 */
public class PositionalIndex {
    private ArrayList<Integer> positions;
    private int docID;

    public PositionalIndex (int doc, ArrayList<Integer> list){
        docID =  doc;
        positions = list;
    }

    public ArrayList<Integer> getPositions() {
        return positions;
    }

    public void setPositions(ArrayList<Integer> positions) {
        this.positions = positions;
    }

    public int getDocID() {
        return docID;
    }

    public void setDocID(int docID) {
        this.docID = docID;
    }

    public void addPosition(int pos){
        this.positions.add(pos);
    }

}
