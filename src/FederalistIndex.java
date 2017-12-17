import org.tartarus.snowball.ext.englishStemmer;

import java.util.*;

// need to build separate index for each of the three classes
public class FederalistIndex {
    // maps a vocab term to the term frequency
    private HashMap<String, Integer> tIndex;
    private ArrayList<DocumentWeight> docWeights;

    // maps vocab term to the documents that contain term
    // for bayesian part (a). might need to make separate class for this?
    private HashMap<String, List<Integer>> dIndex;

    // map assigned doc ID to actual paper name
    private HashMap<Integer, String> docTitles;

    // total number of documents in the indexed folder
    private int totalDocuments;

    public FederalistIndex() {
        tIndex = new HashMap<>();
        dIndex = new HashMap<>();
        docWeights = new ArrayList<>();
        totalDocuments = 0;
    }

    public ArrayList<DocumentWeight> getDocWeights() {
        return docWeights;
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
//        englishStemmer stemmer = new englishStemmer();
//        stemmer.setCurrent(term);
//        stemmer.stem();
//        String termAfterStemmed = stemmer.getCurrent();
        if (getDocumentFrequency(term) > 0)
            return tIndex.get(term);
        return 0;
    }

    public int getDocumentFrequency(String term) {
        if (dIndex.get(term) == null)
            return 0;
        return dIndex.get(term).size();
    }


    public void addTerm(String term, int i) {
        //if document hasn't been added yet, add it
        if(docWeights.size() < i){
            docWeights.add(new DocumentWeight());
        }
        //access document, add term
        docWeights.get(i-1).addTerm(term);

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

    // returns all keys in index
    // used to calculate mutual information after index is complete
    public String[] getDictionary() {
        Iterator<String> itr = tIndex.keySet().iterator();
        ArrayList<String> list = new ArrayList<>();
        while (itr.hasNext()) {
            list.add(itr.next());
        }
        Collections.sort(list);
        return list.toArray(new String[list.size()]);
    }

    public void setDocIDTable(HashMap<Integer,String> docTitles) {
        this.docTitles = docTitles;
    }

    public String getDocTitle(int docID) {
        return docTitles.get(docID);
    }
}
