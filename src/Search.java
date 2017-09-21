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

        if (listA == null || listB == null) return null; //return null if there's an empty list

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

    public ArrayList<Integer> getDocIDList(String term) {
        // get list of documents that contain the given term
        ArrayList<PositionalIndex> postings = index.getPostings(term);
        ArrayList<Integer> docList = new ArrayList<Integer>(postingsList.size());
        for (int i = 0; i < postings.size() i++) {
            docList.set(i, postings.get(i).getDocID());
        }
        return docList;
    }

    public ArrayList<Integer> searchPhraseLiteral(String phrase) {
        // returns a list of docIDs that contain the entire phrase

        ArrayList<Integer> results = new ArrayList<Integer>();

        // separate phrase into individual tokens
        String[] tokens = Query.getPhraseTokens(phrase);

        // get list of documents that contain all the phrase tokens
        ArrayList<Integer> accum = getDocIDList(tokens[0]);
        for (int i = 1; i < tokens.length; i++) {
            ArrayList<Integer> curr = getDocIDList(tokens[i]);
            accum = mergeLists(accum, curr);
            if (accum == null) return null;
        }

        // loop through each doc that contains all the tokens
        for (Integer docID: merged) {
            ArrayList<Integer>[] tokenPositions = new ArrayList<Integer(tokens.length);
            // get position list of each phrase token in the current document
            for (int i= 0; i < tokenPositions.size(); i++) {
                tokenPositions[i] = index.getPostingByDoc(tokens[i], docID.value());
            }

            // loop through first token's positions and check if other tokens are adjacent
            boolean matched = true;
            for (int i = 0; i < tokenPositions[0].size(); i++) {
                int start = tokenPositions[0].get(i).intValue();
                for (int j = 1; j < tokenPositions.length; j++) {
                    if (!tokenPositions[j].contains(new Integer(start + j))) {
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    results.add(docID);
                    break;
                }
            }
        }
        return results;
    }

    public ArrayList<String> searchQuery(String query) {
        // process each subquery one at a time

        // then combine results with OR logic
        return null;
    }

    public void printResults() {

    }


}