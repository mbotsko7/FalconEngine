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

    private List<Integer> mergeLists(List<Integer> listA, List<Integer> listB) {
        // perform an AND intersection on two lists of docIDs
        // return a list of docIDs for documents that contain both terms
        List<Integer> results = new ArrayList<>();
        if (!listA.isEmpty() && !listB.isEmpty()) {
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
        }
        return results;
    }

    private List<Integer> getDocIDList(String term) {
        // get list of documents that contain the given term
        List<PositionalIndex> postings = index.getPostings(term);
        List<Integer> docList = new ArrayList<>(postings.size());
        for (int i = 0; i < postings.size(); i++) {
            docList.set(i, postings.get(i).getDocID());
        }
        return docList;
    }

    private List<Integer> searchPhraseLiteral(String phrase) {
        // returns a list of docIDs that contain the entire phrase

        List<Integer> results = new ArrayList<>();

        // separate phrase into individual tokens
        String[] tokens = Query.getPhraseTokens(phrase);

        // get list of documents that contain all the phrase tokens
        List<Integer> accum = getDocIDList(tokens[0]);
        for (int i = 1; i < tokens.length; i++) {
            List<Integer> curr = getDocIDList(tokens[i]);
            accum = mergeLists(accum, curr);
        }
        if (!accum.isEmpty()) {
            // loop through each doc that contains all the tokens
            for (Integer docID : accum) {
                //            List<Integer>[] tokenPositions = new ArrayList<Integer>(tokens.length);
                List<List<Integer>> tokenPositions = new ArrayList<List<Integer>>(tokens.length);
                // get position list of each phrase token in the current document
                for (int i = 0; i < tokenPositions.size(); i++) {
                    tokenPositions.set(i, index.getPositionsInDoc(tokens[i], docID.intValue()));
                }

                // loop through first token's positions and check if other tokens are adjacent
                boolean matched;
                List<Integer> first = tokenPositions.get(0);
                for (int i = 0; i < first.size(); i++) {
                    matched = true;
                    int start = first.get(i).intValue();
                    for (int j = 1; j < tokenPositions.size(); j++) {
                        if (!tokenPositions.get(j).contains(new Integer(start + j))) {
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
        }
        return results;
    }

    public void searchForQuery() {
        // search the document for the query

        List<List<Integer>> subqueryResults = new ArrayList<List<Integer>>();
        Set<Integer> finalResults = new HashSet<>();

        // process each subquery one at a time
        String[] subqueries = Query.getSubqueries(query);
        for (String subquery: subqueries) {
            List<List<Integer>> literalsPostings = new ArrayList<List<Integer>>();
            List<String> literals = Query.getQueryLiterals(subquery);
            // get list of docs for each literal
            for (String literal: literals) {
                if (literal.startsWith("\"")) {  // for phrases
                    literalsPostings.add(searchPhraseLiteral(literal));
                } else { // for single tokens
                    literalsPostings.add(getDocIDList(literal));
                }
            }
            // merge doc lists for each literal
            List<Integer> mergedList = literalsPostings.get(0);
            for (int i = 1; i < literalsPostings.size(); i++) {
                mergedList = mergeLists(mergedList, literalsPostings.get(i));
                if (mergedList.isEmpty()) break;
            }
            subqueryResults.add(mergedList);
        }

        // OR the results from the subqueries
        for (List<Integer> result: subqueryResults) {
            finalResults.addAll(result);
        }
        finalResults.remove(null);
        printResults(finalResults);
    }

    public void printResults(Set<Integer> results ) {
        for (Integer docID: results) {
            System.out.format("doc%d.json %n", docID);
        }
        System.out.format("%nTotal documents: %d", results.size());
    }


}