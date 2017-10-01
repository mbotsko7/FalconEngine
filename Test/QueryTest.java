import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class QueryTest {
    private Query q = new Query();

    @Test
    public void getSubqueries() throws Exception {
        String testTerm = "test + query";
        String[] expectedResults = {"test", "query"};
        String[] results = q.getSubqueries(testTerm);
        assertArrayEquals(expectedResults, results);
    }

    @Test
    public void getQueryLiterals() throws Exception {
        String testPhrase = "This is \"a testing\" phrase-simple.";
        ArrayList<String> results = q.getQueryLiterals(testPhrase);
        String[] actual = results.toArray(new String[results.size()]);
        String[] expected = {"This", "is", "\"a testing\"", "phrasesimple."};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getPhraseTokens() throws Exception {
        String phrase = "\"This is a test phrase.\"";
        String[] results = q.getPhraseTokens(phrase);
        String[] expected = {"This", "is", "a", "test", "phrase."};
        assertArrayEquals(expected, results);
    }

}