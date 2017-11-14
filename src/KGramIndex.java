import java.io.Serializable;
import java.util.*;

/**
 * Created by bardsko on 9/22/17.
 */
public class KGramIndex implements Serializable {
    private TreeMap<String, ArrayList<String>> kIndex;

    public KGramIndex() {
        kIndex = new TreeMap<>();
        for(int i = 0; i < 256; i++){
            kIndex.put(((char)i)+"", new ArrayList<>());
        }

    }


    public ArrayList<String> find(String str) {
        return kIndex.get(str);
    }

    /*
    Breaks string into all KGRAM tokens
     */
    public static ArrayList<String> kGramify(String str){
        ArrayList<String> ret = new ArrayList<>();
        char[] single = str.toCharArray();
        for(char c: single){
            ret.add(c+"");
        }
        String token = "$"+str+"$";
        //for a 2-gram and a 3 gram
        for (int c = 2; c < 4; c++) {
            for (int i = 0; i <= token.length() - c; i++) {
                String s = token.substring(i, i + c);
                ret.add(s);
            }
        }
        Collections.sort(ret);
        String prev = ret.get(0);
        for(int i = 1; i < ret.size(); i++){
            String current = ret.get(i);
            if(prev.equals(current)){
                ret.remove(i);
                i--;
            }
            else
                prev = current;
        }
        return ret;
    }

    // splits query into 1, 2, and 3 gram indexes
    // adds each to kgramindex
    public void add(String str) {
        //for a 1-gram
        char[] single = str.toCharArray();
        for (char c : single) {
            ArrayList<String> temp;
            try {
                temp = kIndex.get(c + "");
                temp.add(str);
                kIndex.put(c+"", temp);
            }
            catch (Exception e) {
                temp = new ArrayList<>();
                temp.add(str);
                kIndex.put(c+"", temp);
            }
//            if(kIndex.containsKey(c+""))
//                kIndex.get(c + "").add(str);
//            else{
//                ArrayList<String> temp = new ArrayList<>();
//                temp.add(str);
//                kIndex.put(c+"", temp);
//            }

        }

        String token = "$" + str + "$";
        //for a 2-gram and a 3 gram
        for (int c = 2; c < 4; c++) {
            for (int i = 0; i <= token.length() - c; i++) {
                String s = token.substring(i, i + c);
                if (kIndex.containsKey(s)) {
                    ArrayList<String> l = kIndex.get(s);
                    l.add(str);
                    kIndex.put(s, l);
                }
                else {
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(str);
                    kIndex.put(s, temp);
                }
            }
        }

    }

    // returns all keys in index
    // used to create binary file
    public String[] getDictionary() {
        Iterator<String> itr = kIndex.keySet().iterator();
        ArrayList<String> list = new ArrayList<>();
        while (itr.hasNext()) {
            list.add(itr.next());
        }
        Collections.sort(list);
        return list.toArray(new String[list.size()]);
    }

    // returns all terms of a specified key
    // used to create binary file
    public List<String> getTerms(String key) {
        return kIndex.get(key);
    }
}
