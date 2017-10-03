import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class KGramIndexTest {
    private KGramIndex k = new KGramIndex();

    @Test
    public void add() throws Exception {
        String s = "marian";
        k.add(s);
        String[] kgrams = {"m", "a", "r", "i", "a", "n", "$m", "ma", "ar", "ri", "ia",
                            "an", "n$", "$ma", "mar", "ari", "ria", "ian", "an$"};
        for (int i = 0; i < kgrams.length; i++) {
            assertTrue(k.find(kgrams[i]).contains(s));
        }

        // $marian$
        // m, a, r, i, a, n
        // $m, ma, ar, ri, ia, an, n$
        // $ma, mar, ari, ria, ian, an$
    }

}