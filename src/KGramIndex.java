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
        char[] arr = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
        for(char c : arr)
            kIndex.put(c+"", new ArrayList<>());

    }



    public ArrayList<String> find(String str){
        return kIndex.get(str);
    }

    // splits query into 1, 2, and 3 gram indexes
    // adds each to kgramindex
    public void add(String str){
        //for a 1-gram
        char[] single = str.toCharArray();
        for(char c : single){
            kIndex.get(c + "").add(str);

        }

        String token = "$" + str + "$";
        //for a 2-gram and a 3 gram
        for(int c = 2; c < 4; c++) {
            for (int i = 0; i <= token.length() - c; i++) {
                String s = token.substring(i, i+c);
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

    }

}
