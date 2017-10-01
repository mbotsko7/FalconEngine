import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SearchTest {
    private PositionalInvertedIndex index = new PositionalInvertedIndex();
    private Search search = new Search(index);

    @Test
    public void mergeLists() throws Exception {
        ArrayList<Integer> testList1 = new ArrayList<>(Arrays.asList(2, 5, 8, 20, 54, 77, 100));
        ArrayList<Integer> testList2 = new ArrayList<>(Arrays.asList(2, 8, 13, 49));
        ArrayList<Integer> testList3 = new ArrayList<>();   // empty list

        // first list longer than second
        List<Integer> results = search.mergeLists(testList1, testList2);
        List<Integer> expected = new ArrayList<>(Arrays.asList(2, 8));
        assertTrue(results.equals(expected));

        // second list longer than first
        results = search.mergeLists(testList2, testList1);
        assertTrue(results.equals(expected));

        // one of the lists is empty
        results = search.mergeLists(testList1, testList3);
        expected = Collections.emptyList();
        assertTrue(results.equals(expected));
    }

    @Test
    public void getDocIDList() throws Exception {
        String testTerm = "test";
        index.addTerm(testTerm, 1, 2);
        index.addTerm(testTerm, 4, 4);
        index.addTerm(testTerm, 12, 10);

        List<Integer> postings = search.getDocIDList(testTerm);
        assertNotNull(postings);    // shouldn't ever be null (just empty)
        List<Integer> expected = new ArrayList<>(Arrays.asList(1, 4, 12));
        assertTrue(postings.equals(expected));
    }

    @Test
    public void searchPhraseLiteral() throws Exception {
        index.addTerm("this", 1, 2);
        index.addTerm("this", 4, 4);
        index.addTerm("this", 6, 77);
        index.addTerm("is", 12, 10);
        index.addTerm("is", 6, 86);
        index.addTerm("a", 2, 2);
        index.addTerm("a", 6, 4);
        index.addTerm("test", 5, 4);
        index.addTerm("test", 8, 10);
        index.addTerm("test", 6, 2);
        index.addTerm("phrase", 3, 4);
        index.addTerm("phrase", 6, 10);

        // still working on
        List<Integer> postings = search.searchPhraseLiteral("this is a test phrase");
        List<Integer> expected = new ArrayList<>(Arrays.asList(6));
        assertTrue(postings.equals(expected));
    }


    // still working on. something seems off with the Search class
    // might need to make some changes on that
    @Test
    public void searchForQuery() throws Exception {

    }
}