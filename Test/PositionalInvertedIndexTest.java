import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PositionalInvertedIndexTest {
    private PositionalInvertedIndex pIndex;

    @Before
    public void setUp() {
        String dir = "junit_json";
        pIndex = new PositionalInvertedIndex();
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

        // positional inverted index
        // marian       1:<0>
        // is           1:<1>, 5:<2>
        // funny        1:<2>
        // michael      2:<0>
        // disagrees    2:<1>
        // we           3:<0>
        // are          3:<1>
        // chocolate    3:<2>, 4:<3>
        // i            4:<0>
        // want         4:<1>
        // some         4:<2, 4>
        // this         5:<0>
        // project      5:<1>
        // hard         5:<3>
    }

    @Test
    public void addTerm() throws Exception {
        List<PositionalIndex> expected = new ArrayList<>();
        pIndex.addTerm("we", 4, 10);

        PositionalIndex termIndex = new PositionalIndex(3, new ArrayList<>(Arrays.asList(0)));
        PositionalIndex termIndex2 = new PositionalIndex(4, new ArrayList<>(Arrays.asList(10)));
        expected.add(termIndex);
        expected.add(termIndex2);

        List<PositionalIndex> results = pIndex.getPostings("we");
        // checks to see that expected and results are same size
        // and that they contain same information
        if (results.size() == expected.size()) {
            for (int i = 0; i < results.size(); i++) {
                ArrayList<Integer> resultsPI = results.get(i).getPositions();
                ArrayList<Integer> expectedPI = expected.get(i).getPositions();
                assertTrue(resultsPI.equals(expectedPI));
            }
        } else {
            System.err.println("Error: not equal");
        }
    }

    @Test
    public void getPostings() throws Exception {
        PositionalIndex termIndex1 = new PositionalIndex(1, new ArrayList<>(Arrays.asList(1)));
        PositionalIndex termIndex2 = new PositionalIndex(5,  new ArrayList<>(Arrays.asList(2)));

        List<PositionalIndex> expected = new ArrayList<>();
        expected.add(termIndex1);
        expected.add(termIndex2);

        List<PositionalIndex> results = pIndex.getPostings("is");

        // checks to see that expected and results are same size
        // and that they contain same information
        if (results.size() == expected.size()) {
            for (int i = 0; i < results.size(); i++) {
                ArrayList<Integer> resultsPI = results.get(i).getPositions();
                ArrayList<Integer> expectedPI = expected.get(i).getPositions();
                assertTrue(resultsPI.equals(expectedPI));
            }
        } else {
            System.err.println("Error: not equal");
        }
    }

    @Test
    public void getPositionsInDoc() throws Exception {
        ArrayList<Integer> positions = new ArrayList<>(Arrays.asList(0));
        ArrayList<Integer> results = pIndex.getPositionsInDoc("marian", 1);
        assertTrue(positions.equals(results));
    }

    @Test
    public void getDictionary() throws Exception {
        // terms that should be in index
        // (after stemmed and in alphabetical order)
        String[] dictionary = {"are", "chocol", "disagre", "funni", "hard", "i", "is", "michael",
                                "marian", "project", "some", "this", "want", "we"};
        Arrays.sort(dictionary);
        assertArrayEquals(dictionary, pIndex.getDictionary());
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