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
        char[] arr = "_0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
        for(char c : arr)
            kIndex.put(c+"", new ArrayList<>());

    }

//    public void add(String str){
//        ArrayList<String> kgrams = index(str);
//        for (String s : kgrams){
//            if(kIndex.containsKey(s)){
//                kIndex.get(s).add(str);
//            }
//            else{
//                ArrayList<String> temp = new ArrayList<>();
//                temp.add(str);
//                kIndex.put(s, temp);
//            }
//        }
//    }

    public ArrayList<String> findByKGram(String str){
        return kIndex.get(str);
    }

    public void add(String str){
        //for a 1-gram
        char[] single = str.toCharArray();
        for(char c : single){
            try {
                kIndex.get(c + "").add(str);
            }
            catch (NullPointerException e){
                System.out.println("Stop");
            }
            //tokenList.add(""+c);
        }

        String token = "$" + str + "$";
        //for a 2-gram and a 3 gram
        for(int c = 2; c < 4; c++) {
            for (int i = 0; i <= token.length() - c; i++) {
                String s = token.substring(i, i+c);
//                tokenList.add(token.substring(i, i + c));
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
