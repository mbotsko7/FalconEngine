import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// utility methods for processing queries
public class Query {

    public static String[] getSubqueries(String query) {
        // breaks down query into list of subqueries, separating by +
        return query.split(" \\+ ");
    }

    public static ArrayList<String> getQueryLiterals(String str) {
        // breaks down a subquery and returns an ArrayList of query literals
        // (single tokens, near-k, phrase literals)
        str = str.trim();
        ArrayList<String> queryLiterals = new ArrayList<String>();

        Pattern phrasePat = Pattern.compile("\"([\\w-\']+\\s*)*\"");
        Matcher m = phrasePat.matcher(str);
        while (m.find()) {
            String matched = m.group();
            queryLiterals.add(matched);
            str = str.replaceFirst(matched, "");
        }

        Pattern nearPat = Pattern.compile("[\\w-\']+\\s+NEAR/\\d+\\s+[\\w-\']+");
        m = nearPat.matcher(str);
        while (m.find()) {
            String matched = m.group();
            queryLiterals.add(matched);
            str = str.replaceFirst(matched, "");
        }

        Pattern wordPat = Pattern.compile("[\\w-\']+");
        m = wordPat.matcher(str);
        while (m.find()) {
            String matched = m.group();
            matched = matched.replaceAll("-",  "");
            queryLiterals.add(matched);
            str = str.replaceFirst(matched,  "");
        }

        return queryLiterals;
    }


    public static String[] getPhraseTokens(String phrase) {
        // break a phrase literal down into individual stemmed tokens
        phrase = phrase.substring(1, phrase.length() - 1);
        String[] tokens = phrase.split(" ");
        return tokens;
    }
}