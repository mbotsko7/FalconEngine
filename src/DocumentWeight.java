import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class DocumentWeight {
    private HashMap<String, Integer> map;

    public DocumentWeight(){
        map = new HashMap<>();
    }

    public void addTerm(String s){
        Integer val = map.get(s);
        if(val != null) {
            map.put(s, val.intValue() + 1);
        }
        else {
            map.put(s, 1);
        }
    }

    public HashMap<String, Integer> getMap() {
        return map;
    }

    public double calculateWeight(){
        Set<String> keyset = map.keySet();
        double sum = 0.0;
        for(String key : keyset){
            double freq = (double) map.get(key);
            double w = 1+Math.log(freq);
            sum += Math.pow(w, 2);

        }
        return Math.pow(sum, .5);
    }
}
