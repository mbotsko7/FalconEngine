import org.junit.Test;
import static org.junit.Assert.*;

public class FileComparatorTest {
    private FileComparator fileComparator = new FileComparator();

    @Test
    public void testEqual() throws Exception {
        String oneName = "article30.json";
        String twoName = "article30.json";
        int result = fileComparator.compare(oneName, twoName);
        assertTrue("expected to be equal", result == 0);
    }

    @Test
    public void testGreaterThan() throws Exception {
        String oneName = "article75.json";
        String twoName = "article4.json";
        int result = fileComparator.compare(oneName, twoName);
        assertTrue("expected to be greater than", result > 0);
    }

    @Test
    public void testLessThan() throws Exception {
        String oneName = "article28.json";
        String twoName = "article100.json";
        int result = fileComparator.compare(oneName, twoName);
        assertTrue("expected to be less than", result < 0);
    }
}