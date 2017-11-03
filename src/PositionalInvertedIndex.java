import java.util.*;

public class PositionalInvertedIndex {
    private HashMap<String, List<PositionalIndex>> mIndex;

    public PositionalInvertedIndex() {
        mIndex = new HashMap<String, List<PositionalIndex>>();
    }

    // adds term from document to postional inverted index
    public void addTerm(String term, int documentID, int position) {
        //check if the term is in the index already
        if (mIndex.containsKey(term)) {
            ArrayList<PositionalIndex> postings = (ArrayList<PositionalIndex>) getPostings2(term);
            if (postings.get(postings.size()-1).getDocID() == documentID) {
                postings.get(postings.size() - 1).addPosition(position);

            }
            else { //add doc and position to the index

                ArrayList<Integer> posList = new ArrayList<>();
                posList.add(position);
                postings.add(new PositionalIndex(documentID, posList));
                mIndex.put(term, postings);
            }

        }
        else { //if new term
            ArrayList<PositionalIndex> l = new ArrayList<>();
            ArrayList<Integer> posList = new ArrayList<>();
            posList.add(position);
            l.add(new PositionalIndex(documentID, posList));
            mIndex.put(term, l);
        }
    }


    // returns postings with positions
    public List<PositionalIndex> getPostings2(String term) {
        return mIndex.get(term);
    }

    // returns postings with just doc IDs
    public List<Integer> getPostings(String term) {
        List<PositionalIndex> pIndex = mIndex.get(term);
        List<Integer> docIDs = new ArrayList<>();
        for (PositionalIndex p : pIndex) {
            docIDs.add(p.getDocID());
        }
        return docIDs;
    }

    // for specified term and docID, return all positions
    // that term is located in that document
    public ArrayList<Integer> getPositionsInDoc(String term, int docID) {
        // retrieve a term's postings list from a specified document
        List<PositionalIndex> postings = mIndex.get(term);
        for (int i = 0; i < postings.size(); i++) {  // use binary search instead?
            if (postings.get(i).getDocID() == docID)
                return postings.get(i).getPositions();
        }
        return null;
    }

    public int getTermCount() {
        return mIndex.size();
    }

    // returns all keys in index
    public String[] getDictionary() {
        Iterator<String> itr = mIndex.keySet().iterator();
        ArrayList<String> list = new ArrayList<>();
        while (itr.hasNext()) {
            list.add(itr.next());
        }
        Collections.sort(list);
        return list.toArray(new String[list.size()]);
    }
}