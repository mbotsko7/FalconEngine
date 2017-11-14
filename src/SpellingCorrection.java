import java.util.ArrayList;
import java.util.Collections;

public class SpellingCorrection {
    private ArrayList<String> query;
    private KGramIndex kGramIndex;
    private final double JACCARD_WEIGHT = .80;
    private DiskInvertedIndex index;
    public SpellingCorrection(ArrayList<String> q, KGramIndex k, DiskInvertedIndex i){
        kGramIndex = k;
        query = q;
        index = i;
    }

    public String result(){
        String newQuery = "";
        boolean changed = false;
        for(String term : query){
            if(term.contains("*")){
                newQuery += " "+term;
                continue;
            }
            DiskPosting[] postings = index.getPostings(SimpleTokenStream.parseAndStem(term));
            if(postings.length == 0 || postings.length < 100){
                newQuery += " "+correctSpelling(term);
                changed = true;
            }
            else
                newQuery += " "+term;
        }
        if(changed)
            return newQuery;
        return "";
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
                if(s2.charAt(0) != term.charAt(0)){
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

        int min = Integer.MAX_VALUE;
        String closest = "";
        for(String s : possible){
            int distance = dist(term, s);//dist(term.toCharArray(), term.length(), s.toCharArray(), s.length());
            if(distance < min){
                min = distance;
                closest = s;
            }
            else if(distance == min){

                if(index.getPostingsWithPositions(SimpleTokenStream.parseAndStem(s)).length > index.getPostingsWithPositions(SimpleTokenStream.parseAndStem(closest)).length){
                    min = distance;
                    closest = s;
                }

            }
        }

        return SimpleTokenStream.parse(closest);
    }



    private int dist(String a, String b) {
        int len1 = a.length(), len2 = b.length();
        if (len1 == 0)
            return len2;
        else if (len2 == 0)
            return len1;
        //set up initial edit dist matrix
        int matrix[][] = new int[len1+1][len2+1];
        for (int i = 0; i <= len1; i++) {
            matrix[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            matrix[0][j] = j;
        }
        //run through matrix
        for (int i = 1; i <= len1; i++) {
            char val = a.charAt(i - 1);
            for (int j = 1; j <= len2; j++) {
                if (val == b.charAt(j-1))
                    matrix[i][j] = min(matrix[i-1][j]+1, matrix[i][j-1]+1, matrix[i-1][j-1]);
                else
                    matrix[i][j] = min(matrix[i-1][j]+1, matrix[i][j-1]+1, matrix[i-1][j-1] + 1);
            }
        }
        return matrix[len1][len2];
    }

    private int min(int a, int b, int c) {
        if(a < b && a < c)
            return a;
        else if(b < a && b < c)
            return b;
        else
            return c;
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
