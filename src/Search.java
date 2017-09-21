import java.util.*;

// NOT DONE

public class Search {
    // don't forget to stem

    private PositionalInvertedIndex index = new PositionalInvertedIndex();
    private String query;

    public Search(PositionalInvertedIndex index, String query) {
        this.index = index;
        this.query = query;
    }

    public ArrayList<Integer> mergeLists(ArrayList<Integer> listA, ArrayList<Integer> listB) {
        // perform an AND intersection on two lists of docIDs
        // return a list of docIDs for documents that contain both terms

        if (listA == null || listB == null) return null; //return null if there's an empty list

        List<Integer> results = new ArrayList<Integer>();
        int i = 0, j = 0;
        while (i < listA.size() && j < listB.size()) {
            int compare = listA.get(i).compareTo(listB.get(j));
            if (compare == 0) {
                results.add(i);
                i++;
                j++;
            } else if (compare > 0) {
                j++;
            } else {
                i++;
            }
        }
        return results;
    }

    public List<Integer> getDocIDList(String term) {
        // get list of documents that contain the given term
        List<PositionalIndex> postings = index.getPostings(term);
        List<Integer> docList = new ArrayList<Integer>(postingsList.size());
        for (int i = 0; i < postings.size() i++) {
            docList.set(i, postings.get(i).getDocID());
        }
        return docList;
    }

    public ArrayList<Integer> searchPhraseLiteral(String phrase) {
        // returns a list of docIDs that contain the entire phrase

        List<Integer> results = new ArrayList<Integer>();

        // separate phrase into individual tokens
        String[] tokens = Query.getPhraseTokens(phrase);

        // get list of documents that contain all the phrase tokens
        List<Integer> accum = getDocIDList(tokens[0]);
        for (int i = 1; i < tokens.length; i++) {
            List<Integer> curr = getDocIDList(tokens[i]);
            accum = mergeLists(accum, curr);
            if (accum == null) return null;
        }

        // loop through each doc that contains all the tokens
        for (Integer docID: accum) {
            List<Integer>[] tokenPositions = new ArrayList<Integer(tokens.length);
            // get position list of each phrase token in the current document
            for (int i= 0; i < tokenPositions.size(); i++) {
                tokenPositions[i] = index.getPostingByDoc(tokens[i], docID.value());
            }

            // loop through first token's positions and check if other tokens are adjacent
            boolean matched = true;
            for (int i = 0; i < tokenPositions[0].size(); i++) {
                matched = true;
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

    public ArrayList<String> searchQuery() {
        // search the document for the query

        List<ArrayList<Integer>> subqueryResults = new ArrayList<ArrayList<Integer>>();
        Set<Integer> finalResults = new HashSet<Integer>();

        // process each subquery one at a time
        String[] subqueries = Query.getSubqueries(query);
        for (String subquery: subqueries) {
            List<ArrayList<Integer>> literalsPostings = new ArrayList<ArrayList<Integer>>();
            List<String> literals = Query.getQueryLiterals(subquery);
            // get postings lists for each literal
            for (String literal: literals) {
                if (literal.startsWith("\"")) {  // phrases
                    literalsPostings.add(searchPhraseLiteral(literal));
                } else { // single tokens
                    literalsPostings.add(index.getPostings(literal));
                }
            }
            // merge postings lists for each literal
            List<Integer> mergedList = literalsPostings.get(0);
            for (int i = 1; i < literalsPostings.size(); i++) {
                mergedList = mergeLists(mergedList, literalsPostings.get(i));
            }
            subqueryResults.add(mergedList);
        }

        // OR the results from the subqueries
        for (ArrayList<Integer> subquery: subqueryResults) {
            finalResults.addAll(subquery);
        }
        return finalResults;
    }

    public void printResults() {

    }


}