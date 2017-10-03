import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class WildcardQueryTest {
    private WildcardQuery w;
    private KGramIndex kIndex;

    @Before
    public void setUp() {
        w = new WildcardQuery("m*r*an");
        kIndex = new KGramIndex();

        kIndex.add("marian");
        kIndex.add("marlan");
        kIndex.add("marias");
    }

    @Test
    public void queryResult() throws Exception {
        ArrayList<String> expected = new ArrayList<>(Arrays.asList("marian", "marlan"));
        ArrayList<String> results = w.queryResult(kIndex);
        assertTrue(expected.equals(results));
    }

    @Test
    public void verify() throws Exception {
        boolean s = w.verify("butterfly", "butte*fly");
        assertTrue(s);
    }

    @Test
    public void mergePostings() throws Exception {
        ArrayList<String> expected = new ArrayList<>(Arrays.asList("marian", "marlan"));
        ArrayList<String> results = w.mergePostings(kIndex);
        assertTrue(expected.equals(results));
    }

}