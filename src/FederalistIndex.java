import org.tartarus.snowball.ext.englishStemmer;
import java.util.HashMap;

// need to build separate index for each of the three classes
public class FederalistIndex {
    // maps a vocab term to the term frequency
    private HashMap<String, Integer> mIndex;
    private int numberOfDocuments;

    public FederalistIndex() {
        mIndex = new HashMap<>();
        numberOfDocuments = 0;
    }

    public void setNumberOfDocuments(int n) {
        numberOfDocuments = n;
    }

    public HashMap<String, Integer> getIndex() {
        return mIndex;
    }

    public int getNumberOfDocuments() {
        return numberOfDocuments;
    }

    public int getTermFrequency(String term) {
        englishStemmer stemmer = new englishStemmer();
        stemmer.setCurrent(term);
        stemmer.stem();
        String termAfterStemmed = stemmer.getCurrent();
        return mIndex.get(termAfterStemmed);
    }

    public void addTerm(String term) {
        if (mIndex.containsKey(term)) {
            // increments the term frequency by one if
            // the term is already in the index
            mIndex.put(term, mIndex.get(term) + 1);
        } else {
            // adds new term to index
            // initializes count to 1
            mIndex.put(term, 1);
        }
    }
}
