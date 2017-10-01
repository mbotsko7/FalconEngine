import org.junit.Test;
import java.io.File;
import static org.junit.Assert.*;

public class SimpleTokenStreamTest {
    @Test
    public void hasNextToken() throws Exception {
        File testFile = new File("nps/article1.json");
        SimpleTokenStream stream = new SimpleTokenStream(testFile);
        assertTrue(testFile.exists());
        assertTrue(stream.hasNextToken());
    }

    @Test
    public void parseAndStem() throws Exception {
        SimpleTokenStream stream = new SimpleTokenStream();
        String testMsg = "Simple_Tester.";
        String results = stream.parseAndStem(testMsg);
        assertEquals("simpletest", results);
    }

    @Test
    public void nextToken() throws Exception {
        File testFile = new File("nps/article1.json");
        SimpleTokenStream stream = new SimpleTokenStream(testFile);
        assertTrue(testFile.exists());
        if (stream.hasNextToken()) {
            String next = stream.nextToken();
            assertFalse(next == null);
            if (next.contains("-")) {
                assertFalse(stream.getHyphen() == null);
            } else {
                assertTrue(stream.getHyphen() == null);
            }

        } else {
            String next = stream.nextToken();
            assertTrue(next == null);
            assertTrue(stream.getHyphen() == null);
        }
    }

}