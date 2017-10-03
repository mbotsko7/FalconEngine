import java.util.ArrayList;
import java.util.Arrays;

// utility methods for processing queries
public class Query {

    public static String[] getSubqueries(String query) {
        // breaks down query into list of subqueries, separating by +
        return query.split(" \\+ ");
    }

    public static ArrayList<String> getQueryLiterals(String str) {
        // breaks down a subquery and returns an ArrayList of query literals
        // (single tokens and phrase literals)
        str = str.trim();
        ArrayList<String> queryLiterals = new ArrayList<String>();
        int i = str.indexOf('"');
        while (i != -1) {
            if (i != 0) {
                // separate out single tokens and add to list
                String[] singlesList = str.substring(0, i).split(" ");
                queryLiterals = addSingleTokensToList(singlesList, queryLiterals);
                str = str.substring(i, str.length());
            }
            // add phrase literals to list
            i = str.indexOf('"', 1);
            queryLiterals.add(str.substring(0, i + 1));
            if (i != str.length() - 1) {
                str = str.substring(i + 2, str.length());
                i = str.indexOf('"');
            }
            else {
                str = "";
                break;
            }
        }
        if (str.length() != 0)
            queryLiterals = addSingleTokensToList(str.split(" "), queryLiterals);
        return queryLiterals;
    }

    private static ArrayList<String> addSingleTokensToList(String[] singles, ArrayList<String> list) {
        for (String single : singles) {
            if (single.contains("-"))
                single = single.replace("-", "");
            list.add(single);
        }
        return list;
    }

    public static String[] getPhraseTokens(String phrase) {
        // break a phrase literal down into individual stemmed tokens
        phrase = phrase.substring(1, phrase.length() - 1);
        String[] tokens = phrase.split(" ");
        return tokens;
    }
}