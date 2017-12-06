import org.tartarus.snowball.ext.englishStemmer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// need to build separate index for each of the three classes
public class FederalistIndex {
    // maps a vocab term to the term frequency
    private HashMap<String, Integer> tIndex;

    // maps vocab term to the documents that contain term
    // for bayesian part (a). might need to make separate class for this?
    private HashMap<String, List<Integer>> dIndex;

    // total number of documents in the indexed folder
    private int totalDocuments;

    public FederalistIndex() {
        tIndex = new HashMap<>();
        dIndex = new HashMap<>();
        totalDocuments = 0;
    }

    public void setNumberOfDocuments(int n) {
        totalDocuments = n;
    }

    public HashMap<String, Integer> getTermIndex() {
        return tIndex;
    }
    public HashMap<String, List<Integer>> getDocIndex() {
        return dIndex;
    }

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public int getTermFrequency(String term) {
        englishStemmer stemmer = new englishStemmer();
        stemmer.setCurrent(term);
        stemmer.stem();
        String termAfterStemmed = stemmer.getCurrent();
        return tIndex.get(termAfterStemmed);
    }

    public void addTerm(String term, int i) {
        if (tIndex.containsKey(term)) {
            // increments the term frequency by one if
            // the term is already in the tIndex
            tIndex.put(term, tIndex.get(term) + 1);
        } else {
            // adds new term to tIndex and initializes count to 1
            tIndex.put(term, 1);
        }

        if (dIndex.get(term) == null) {
            dIndex.put(term, new ArrayList<>());
        }

        if (!dIndex.get(term).contains(i)) {
            // adds document id to dIndex
            dIndex.get(term).add(i);
        }
    }
}
