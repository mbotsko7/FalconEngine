import java.util.ArrayList;
import java.util.Collections;

public class SpellingCorrection {
    private String query;
    private KGramIndex kGramIndex;
    private final double JACCARD_WEIGHT = .99;
    private DiskInvertedIndex index;
    public SpellingCorrection(String q, KGramIndex k, DiskInvertedIndex i){
        kGramIndex = k;
        query = q;
        index = i;
    }

    public String correctSpelling(String term){
        //given a term
        //break it into it's KGRAMS
        ArrayList<String> kGramSet = kGramIndex.kGramify(term);
        ArrayList<String> possible =  new ArrayList<>();
        //for every term in possible
        int pos = 0, neg = 0;
        for(String s : kGramSet){
            for(String s2 : kGramIndex.find(s)) {
                //make it a KGRAM
                if(s2.charAt(0) != 'w'){
                    neg++;
                    continue;
                }

                ArrayList<String> kGramPossible = KGramIndex.kGramify(s2);
                //run JACCARD math
                double value = jaccard(kGramSet, kGramPossible);
                if (value >= JACCARD_WEIGHT) {
                    possible.add(s2);
                    pos++;
                }
                else
                    neg++;
            }
        }
        System.out.println("Pos"+pos+"Neg"+neg);
//        System.out.println(possible);
        int min = Integer.MAX_VALUE;
        String closest = "";
        for(String s : possible){
            int distance = dist(term.toCharArray(), term.length(), s.toCharArray(), s.length());
            if(distance < min){
                min = distance;
                closest = s;
            }
            else if(distance == min){
                if(index.getPostingsWithPositions(s).length > index.getPostingsWithPositions(closest).length){
                    min = distance;
                    closest = s;
                }

            }
        }

        return closest;
    }

    // edit distance
    private int dist(char[] a, int lenA, char[] b, int lenB){
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

    private double jaccard(ArrayList<String> a, ArrayList<String> b){
        ArrayList<String> c = mergeList(a,b);
        return (double)c.size()/(a.size()+b.size()-c.size());

    }

    private ArrayList<String> mergeList(ArrayList<String> listA, ArrayList<String> listB){
        ArrayList<String> results = new ArrayList<>();

        if (!listA.isEmpty() && !listB.isEmpty()) {
            int i = 0, j = 0;
            String prev = "", current = "";
            while (i < listA.size() && j < listB.size()) {
                int compare = listA.get(i).compareTo(listB.get(j));
                if (compare == 0) {
                    current = listA.get(i);
                    if(current.equals(prev) == false) {
                        prev = current;
                        results.add(prev);
                    }
                    i++;
                    j++;
                }
                else if (compare < 0) { // >
                    prev = listB.get(j);
                    results.add(prev);
                    j++;
                }
                else {
                    prev = listA.get(i);
                    results.add(prev);
                    i++;
                }
            }
        }
        else if(listA.isEmpty() && listB.isEmpty() == false){
            results.addAll(listB);
        }
        else if(listA.isEmpty() == false && listB.isEmpty()){
            results.addAll(listA);
        }

        return results;
    }
}
