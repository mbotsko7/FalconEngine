import java.util.ArrayList;
import java.util.Arrays;

// utility methods for processing queries
public class Query {
    // TODO deal with hyphens

    public static String[] getSubqueries(String query) {
        // breaks down query into list of subqueries, separating by +
        return query.toLowerCase().split(" \\+ ");
    }

    public static ArrayList<String> getQueryLiterals(String str) {
        // breaks down a subquery and returns an ArrayList of query literals
        // (single tokens and phrase literals)
        ArrayList<String> queryLiterals = new ArrayList<String>();
        int i = str.indexOf('"');
        while (i != -1) {
            if (i != 0) {
                // separate out single tokens and add to list
                String singles = str.substring(0,i);
                queryLiterals.addAll(Arrays.asList(singles.split(" ")));
                str = str.substring(i, str.length());
            }
            // add phrase literals to list
            i = str.indexOf('"', 1);
            queryLiterals.add(str.substring(0, i+1));
            str = str.substring(i+2, str.length());
            i = str.indexOf('"');
        }
        if (str.length() != 0)
            queryLiterals.addAll(Arrays.asList(str.split(" ")));
        return queryLiterals;
    }

    public static String[] getPhraseTokens(String phrase) {
        // break a phrase literal down into individual stemmed tokens
        phrase = phrase.substring(1, phrase.length()-1);
        String[] tokens = phrase.split(" ");
        /* need to stem */
        return tokens;
    }
}