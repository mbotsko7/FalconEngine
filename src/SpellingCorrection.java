import java.util.ArrayList;

public class SpellingCorrection {
    private String query;
    public SpellingCorrection(String q){
        query = q;
    }

    public String correctSpelling(String term){
        //given a term
        //break it into it's KGRAMS
        ArrayList<String> kGramSet = KGramIndex.kGramify(term);
        //for every term in possible
        for(String s : kGramSet){
            //make it a KGRAM
            ArrayList<String> kGramPossible = KGramIndex.kGramify(s);
            //run JACCARD math
            jaccard(kGramSet, kGramPossible);
        }

        return term;
    }

    public double jaccard(ArrayList<String> a, ArrayList<String> b){
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
