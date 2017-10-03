import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.*;

public class PositionalIndexTest {
    private int docID = 1;
    private ArrayList<Integer> testList = new ArrayList<>(Arrays.asList(1, 3, 4, 23, 30));
    private PositionalIndex pIndex = new PositionalIndex(docID, testList);

    @Test
    public void getPositions() throws Exception {
        assertTrue(testList == pIndex.getPositions());
    }

    @Test
    public void setPositions() throws Exception {
        ArrayList<Integer> newList = new ArrayList<>(Arrays.asList(14,38,76,99));
        pIndex.setPositions(newList);
        assertTrue(newList == pIndex.getPositions());
    }

    @Test
    public void getDocID() throws Exception {
        assertEquals(docID, pIndex.getDocID());
    }

    @Test
    public void setDocID() throws Exception {
        int newDocID = 2;
        pIndex.setDocID(newDocID);
        assertEquals(newDocID, pIndex.getDocID());
    }

    @Test
    public void addPosition() throws Exception {
        testList.add(100);
        pIndex.addPosition(100);
        assertTrue(testList == pIndex.getPositions());
    }
}