import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Created by bardsko on 9/22/17.
 */
public class KIndex {
    private TreeMap<String, ArrayList<String>> kIndex;
    public KIndex(){
        kIndex = new TreeMap<>();
    }


//    public void index(String str){
//        String token = "$" + str + "$";
//        ArrayList<String> splitted = new ArrayList<>();
//        for(int i = 0; i < token.length(); i++){
//
//        }
//        while (token.length() >= 3){
//            splitted.add(token.substring(0,3));
//            token = token.substring(3);
//        }
//        if(!token.isEmpty())
//            splitted.add(token);
//        for(String val : splitted){
//            ArrayList<String> list = kIndex.get(val);
//            if(list == null){
//                list = new ArrayList<>();
//            }
//            list.add(str);
//            Collections.sort(list);
//            kIndex.put(val, list);
//        }
//    }




}
