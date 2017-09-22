import java.util.*;

// TODO deal with hyphens

public class Search {

    private PositionalInvertedIndex index = new PositionalInvertedIndex();
    private SimpleTokenStream stream = new SimpleTokenStream();

    public Search(PositionalInvertedIndex index) {
        this.index = index;
    }

    public List<Integer> mergeLists(List<Integer> listA, List<Integer> listB) {
        // perform an AND intersection on two lists of docIDs
        // return a list of docIDs for documents that contain both terms
        List<Integer> results = new ArrayList<>();
        if (!listA.isEmpty() && !listB.isEmpty()) {
            int i = 0, j = 0;
            while (i < listA.size() && j < listB.size()) {
                int compare = listA.get(i).compareTo(listB.get(j));
                if (compare == 0) {
                    results.add(listA.get(i));
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

    public List<Integer> getDocIDList(String term) {
        // get list of documents that contain the given term
        List<PositionalIndex> postings = index.getPostings(term);
        List<Integer> docList = new ArrayList<>();
        if (postings != null) {
            for (int i = 0; i < postings.size(); i++) {
                docList.add(postings.get(i).getDocID());
            }
        }
        return docList;
    }

    public List<Integer> searchPhraseLiteral(String phrase) {
        // returns a list of docIDs that contain the entire phrase

        List<Integer> results = new ArrayList<>();

        // separate phrase into individual stemmed tokens
        String[] tokens = Query.getPhraseTokens(phrase);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = stream.parseAndStem(tokens[i]);
        }

        // get list of documents that contain all the phrase tokens
        List<Integer> accum = getDocIDList(tokens[0]);
        for (int i = 1; i < tokens.length; i++) {
            List<Integer> curr = getDocIDList(tokens[i]);
            accum = mergeLists(accum, curr);
        }
        if (!accum.isEmpty()) {
            // loop through each doc that contains all the tokens
            for (Integer docID : accum) {
                List<List<Integer>> tokenPositions = new ArrayList<List<Integer>>();
                // get position list of each phrase token in the current document
                for (int i = 0; i < tokens.length; i++) {
                    tokenPositions.add(index.getPositionsInDoc(tokens[i], docID.intValue()));
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

    public void searchForQuery(String query) {
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
                    literal = stream.parseAndStem((literal));
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
        System.out.println("RESULTS:");
        for (Integer docID: results) {
            System.out.format("article%d.json %n", docID);
        }
        System.out.format("%nTotal documents: %d%n", results.size());
    }


}