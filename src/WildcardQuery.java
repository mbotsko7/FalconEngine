import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bardsko on 9/29/17.
 */
public class WildcardQuery {
    private String query;
    private ArrayList<String> parseList;
    private ArrayList<String> wildList;
    public WildcardQuery(String q){
        query = q;
        parseList = new ArrayList<>();
        wildList = new ArrayList<>();
        parse(query);
    }

    // goes through postings and removes non-candidate terms
    public ArrayList<String> queryResult(KGramIndex kIndex){
        ArrayList<String> possible = mergePostings(kIndex);
        for(int i = 0; i < possible.size(); i++){
            String str = possible.get(i);
            if(verify(str, query) ==  false){
                possible.remove(i);
                i--;
            }
        }
        return possible;
    }

    // verifies that the term matches wildcard pattern
    public boolean verify(String a, String pat){
        int p = 0, f = 0;
        char[] candidate = ("$"+a+"$").toCharArray();
        char[] pattern = ("$"+pat+"$").toCharArray();
        int i;
        for(i = 0; i < candidate.length;){
            if(f == 0) { //if wildcard has not been triggered
                if (pattern[p] == candidate[i]) { //see if it's the same character, if so continue
                    p++;
                    i++;
                }
                else if (pattern[p] == '*') { //if it is a wildcard
                    f = p;
                    p++;
                }
            }
            else {
                if(pattern[p] == '*'){ //if there is a wildcard, set it up
                    f = p;
                    p++;
                }
                else if(pattern[p] == candidate[i]){ //if they match, wildcard effect ends
                    p++;
                    i++;
                }
                else { //if not match, continue searching until the end of time :'(
                    p = f+1;
                    i++;
                }
            }
        }
        if(p == pat.length()+2)
            return true;
        return false;
    }

    // edit distance
    public int dist(char[] a, int lenA, char[] b, int lenB){
        int cost = 0;
        if(lenA == 0)
            return lenA;
        else if(lenB == 0)
            return lenB;
        else if(a[lenA-1] == b[lenB-1])
            cost++;

        int one = dist(a, lenA-1, b, lenB)+1;
        int two = dist(a, lenA, b, lenB-1)+1;
        int third = dist(a, lenA-1, b, lenB-1) + cost;
        if(one < two && one < third)
            return one;
        else if(two < one && two < third)
            return two;
        else
            return third;
    }

    // takes term and parses it into 1, 2, and 3 kgram indexes
    private void parse(String str){
        //for a 1-gram
        char[] single = str.replaceAll("\\*", "").toCharArray();
        for(char c : single){
            parseList.add(""+c);
        }

        String token = "$" + str + "$";
        //for a 2-gram and a 3 gram
        for(int c = 2; c < 4; c++) {
            for (int i = 0; i <= token.length() - c; i++) {
                String s = token.substring(i, i+c);
                if(s.contains("*"))
                    wildList.add(s);
                else
                    parseList.add(s);

            }
        }

    }

    private ArrayList<String> mergeList(ArrayList<String> one, ArrayList<String> two){
        ArrayList<String> results = new ArrayList<>();
        for (String s : one) {
            if (two.contains(s) && !results.contains(s))
                results.add(s);
        }
        return results;
    }


    public ArrayList<String> mergePostings(KGramIndex kIndex){
        int size = parseList.size();
        ArrayList<String> merged = new ArrayList<String>();
        if(size > 2){
            merged.addAll(mergeList(kIndex.find(parseList.get(0)), kIndex.find(parseList.get(1))));
            for(int i = 2; i < size; i++)
                merged = mergeList(merged, kIndex.find(parseList.get(i)));
        }
        else if(size == 1){
            merged = kIndex.find(parseList.get(0));
        }
        return merged;
    }
}
