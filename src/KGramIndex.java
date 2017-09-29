import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Created by bardsko on 9/22/17.
 */
public class KGramIndex {
    private TreeMap<String, ArrayList<String>> kIndex;
    public KGramIndex(){
        kIndex = new TreeMap<>();
    }

    public void add(String str){
        ArrayList<String> kgrams = index(str);
        for (String s : kgrams){
            if(kIndex.containsKey(s)){
                kIndex.get(s).add(str);
            }
            else{
                ArrayList<String> temp = new ArrayList<>();
                temp.add(str);
                kIndex.put(s, temp);
            }
        }
    }

    public ArrayList<String> findByKGram(String str){
        return kIndex.get(str);
    }

    public ArrayList<String> index(String str){
        ArrayList<String> tokenList = new ArrayList<>();
        String token = "$" + str + "$";
        //for a 1-gram
        char[] single = token.toCharArray();
        for(char c : single){
            tokenList.add(""+c);
        }

        //for a 2-gram and a 3 gram
        for(int c = 2; c < 4; c++) {
            for (int i = 0; i <= token.length() - c; i++) {
                tokenList.add(token.substring(i, i + c));
            }
        }
        return tokenList;
    }




}
