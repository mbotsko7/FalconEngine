import java.util.*;

// NOT DONE

public class Search {
    // don't forget to stem

    private PositionalInvertedIndex index = new PositionalInvertedIndex();

    public Search(PositionalInvertedIndex index) {
        this.index = index;
    }

    public ArrayList<Integer> mergeLists(ArrayList<Integer> listA, ArrayList<Integer> listB) {
        // perform an AND intersection on two lists of docIDs
        // return a list of docIDs for documents that contain both terms

        ArrayList<Integer> results = new ArrayList<Integer>();
        int i = 0, j = 0;
        while (i < listA.size() && j < listB.size()) {
            if (listA.get(i).intValue() == listB.get(j).intValue()) {
                results.add(i);
                i++;
                j++;
            } else if (listA.get(i).compareTo(listB.get(j)) > 0) {
                j++;
            } else {
                i++;
            }
        }
        return results;
    }

//    public ArrayList<Integer> getDocIDList(String term) {
//        ArrayList<PositionalIndex> postingsList = index.getPostings(term);
//        ArrayList<Integer> docList = new ArrayList<Integer>(postingsList.size());
//        int i = 0;
//        for (PositionalIndex post: postingsList) {
//            docList.set(i,post.getDocID());
//            i++;
//        }
//        return docList;
//    }

//    public ArrayList<Integer> searchPhraseLiteral(String phrase) {
//        ArrayList<Integer> results = new ArrayList<Integer>();
//        // separate phrase into tokens
//        String[] tokens = Query.getPhraseTokens(phrase);
//
//        // cycle through each token and merge postings lists
//        ArrayList<Integer> merged = getDocIDList(tokens[0]);
//        for (int i = 1; i < tokens.length; i++) {
//            ArrayList<Integer> curr = getDocIDList(tokens[i]);
//            merged = mergeLists(merged, curr);
//        }
//
//        // check positions in each doc that contains all the tokens
//
//        // loop through each doc that matched
//        // loop through tokens[0]'s positions in the doc
//        // loop through the other tokens and see if they're adjacent..
//
//        for (Integer docID: merged) {
//            for (Integer position: index.getPostingsByDoc(tokens[0], docID.intValue())) {
//
//            }
//        }
//
//
//        return null;
//    }

    public ArrayList<String> searchQuery(String query) {
        // process each subquery one at a time

        // then combine results with OR logic
        return null;
    }

    public void printResults() {

    }


}