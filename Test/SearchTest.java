import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SearchTest {
    private PositionalInvertedIndex pIndex;
    private KGramIndex kGramIndex;
    private Search search;

    @Before
    public void setUp() {
        pIndex = new PositionalInvertedIndex();
        kGramIndex = new KGramIndex();
        search = new Search(pIndex, kGramIndex);

        String dir = "junit_json";
        File f = new File(dir);

        if (f.exists() && f.isDirectory()) {
            String[] fileList = f.list();
            Arrays.sort(fileList, new FileComparator());    // sorts files before assigning docID
            Parser parser = new Parser();
            int i = 1;
            for (String path : fileList) {
                String[] file = parser.parseJSON(f.getPath() + "/" + path);
                indexFile(file, pIndex, i);
                i++;
            }
        }
    }

    @Test
    public void getDocIDList() throws Exception {
        String testTerm = "is";
        List<Integer> postings = search.getDocIDList(testTerm);
        assertNotNull(postings);    // shouldn't ever be null (just empty)
        List<Integer> expected = new ArrayList<>(Arrays.asList(1, 5));
        assertTrue(postings.equals(expected));
    }

    @Test
    // for phrases
    public void searchPhraseLiteral() throws Exception {
        List<Integer> list = search.searchPhraseLiteral("\"some chocolate\"");
        List<Integer> expected = new ArrayList<>(Arrays.asList(4));
        assertTrue(list.equals(expected));
    }

    @Test
    // AND
    public void intersectList() throws Exception {
        List<Integer> postings1 = search.getDocIDList("marian");
        List<Integer> postings2 = search.getDocIDList("is");

        List<Integer> expected = new ArrayList<>(Arrays.asList(1));
        List<Integer> results = search.intersectLists(postings1, postings2);
        assertTrue(expected.equals(results));
    }

    @Test
    // OR
    public void unionList() {
        List<Integer> postings1 = search.getDocIDList("marian");
        List<Integer> postings2 = search.getDocIDList("michael");
        List<Integer> expected = new ArrayList<>(Arrays.asList(1, 2));
        List<Integer> results = search.unionLists(postings1, postings2);
        assertTrue(expected.equals(results));
    }

    // from driver.java
    private static void indexFile(String[] fileData, PositionalInvertedIndex index,
                                  int docID){
        try{
            int  i = 0;
            SimpleTokenStream stream = new SimpleTokenStream(fileData[1]); //currently not including title in the indexing
            while (stream.hasNextToken()){
                String next = stream.nextToken();
                if(next == null)
                    continue;
                index.addTerm(next, docID, i);
                if(stream.getHyphen() != null){
                    for(String str : stream.getHyphen()){
                        index.addTerm(str, docID, i);
                    }
                }
                i++;
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }
}