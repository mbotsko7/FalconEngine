import java.util.*;

public class PositionalInvertedIndex {
    private HashMap<String, List<PositionalIndex>> mIndex;

    public PositionalInvertedIndex() {
        mIndex = new HashMap<String, List<PositionalIndex>>();
    }

    public void addTerm(String term, int documentID, int position) {
        //check if the term is in the index already
        if (mIndex.containsKey(term)) {
            //locate term
            PositionalIndex docIndex = null;
            for (PositionalIndex temp : getPostings(term)) {
                if (temp.getDocID() == documentID) {
                    docIndex = temp;
                }
            }
            //if doc exists, add the new position
            if (docIndex != null) {
                docIndex.addPosition(position);
            } else { //add doc and position to the index
                ArrayList<PositionalIndex> postings = (ArrayList<PositionalIndex>) getPostings(term);
                ArrayList<Integer> posList = new ArrayList<>();
                posList.add(position);
                postings.add(new PositionalIndex(documentID, posList));
                mIndex.put(term, postings);
            }

        } else { //if new term
            ArrayList<PositionalIndex> l = new ArrayList<>();
            ArrayList<Integer> posList = new ArrayList<>();
            posList.add(position);
            l.add(new PositionalIndex(documentID, posList));
            mIndex.put(term, l);
        }
    }


    public List<PositionalIndex> getPostings(String term) {
        return mIndex.get(term);
    }

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