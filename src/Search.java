import java.util.*;

public class Search {

    private PositionalInvertedIndex index = new PositionalInvertedIndex();
    private SimpleTokenStream stream = new SimpleTokenStream();
    private KGramIndex kindex = new KGramIndex();

    public Search(PositionalInvertedIndex index, KGramIndex k) {
        this.index = index;
        this.kindex = k;
    }

    public List<Integer> intersectLists(List<Integer> listA, List<Integer> listB) {
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

    public List<Integer> unionLists(List<Integer> listA, List<Integer> listB) {
        // perform an OR operation on two lists of docIDs
        // return a list of docIDs that appear in either list
        List<Integer> results = new ArrayList<>();
        if (listA.isEmpty() && listB.isEmpty())
            return results;
        else if (listB.isEmpty())
            return listA;
        else if (listA.isEmpty())
            return listB;
        else {
            int i = 0, j = 0;
            while (i < listA.size() && j < listB.size()) {
                int compare = listA.get(i).compareTo(listB.get(j));
                if (compare == 0) {
                    results.add(listA.get(i));
                    i++;
                    j++;
                } else if (compare > 0) {
                    results.add(listB.get(j));
                    j++;
                } else {
                    results.add(listA.get(i));
                    i++;
                }
            }
            while (j < listB.size()) {
                results.add(listB.get(j));
                j++;
            }

            while (i < listA.size()) {
                results.add(listA.get(i));
                i++;
            }
            return results;
        }
    }

    public List<Integer> getDocIDList(String term) {
        // get list of documents that contain the given term
        List<PositionalIndex> postings = index.getPostings(term);
        List<Integer> docList = new ArrayList<>();
        if (postings != null) {
            for (int i = 0; i < postings.size(); i++) {
                int id = postings.get(i).getDocID();
                if (docList.isEmpty() || id != docList.get(docList.size() - 1))
                    docList.add(id);
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
            accum = intersectLists(accum, curr);
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
                int[] seekPos = new int[tokens.length]; // array of seek positions for all tokens in phrase
                for (int i = 0; i < seekPos.length; i++)      // initialize all seek positions as 0
                    seekPos[i] = 0;

                for (int i = 0; i < first.size(); i++) {
                    matched = true;
                    int start = first.get(i).intValue();
                    for (int j = 1; j < tokenPositions.size(); j++) { // look at jth adjacent word from first token in phrase
                        int pos = seekPos[j];
                        int listSize = tokenPositions.get(j).size();
                        while (pos < listSize - 1 && tokenPositions.get(j).get(pos) < start + j) {
                            pos++;
                        }
                        seekPos[j] = pos;   // update position in seekPos array
                        if (!tokenPositions.get(j).get(pos).equals(start + j)) {
                            matched = false;
                            break;
                        }
                    }
                    if (matched) {
                        results.add(docID);
                        break;
                    }
                }

//                for (int i = 0; i < first.size(); i++) {
//                    matched = true;
//                    int start = first.get(i).intValue();
//                    for (int j = 1; j < tokenPositions.size(); j++) {
//                        if (!tokenPositions.get(j).contains(new Integer(start + j))) {
//                            matched = false;
//                            break;
//                        }
//                    }
//                    if (matched) {
//                        results.add(docID);
//                        break;
//                    }
//                }
            }
        }
        return results;
    }

    public List<Integer> searchForQuery(String query) {
        // search the document for the query

        List<List<Integer>> subqueryResults = new ArrayList<List<Integer>>();
        List<Integer> finalResults = new ArrayList<>();

        // process each subquery one at a time
        String[] subqueries = Query.getSubqueries(query);
        for (String subquery : subqueries) {
            List<List<Integer>> literalsPostings = new ArrayList<List<Integer>>();
            List<String> literals = Query.getQueryLiterals(subquery);
            // get list of docs for each literal
            for (String literal : literals) {
                if (literal.startsWith("\"")) {  // for phrases
                    literalsPostings.add(searchPhraseLiteral(literal));
                } else if (literal.contains("*")) {
                    WildcardQuery q = new WildcardQuery(query);
                    for (String wild : q.queryResult(kindex)) {
                        literalsPostings.add(getDocIDList(wild));
                    }
                } else { // for single tokens
                    literal = stream.parseAndStem((literal));
                    literalsPostings.add(getDocIDList(literal));
                }
            }
            // merge doc lists for each literal
            List<Integer> mergedList = literalsPostings.get(0);
            for (int i = 1; i < literalsPostings.size(); i++) {
                mergedList = intersectLists(mergedList, literalsPostings.get(i));
                if (mergedList.isEmpty()) break;
            }
            subqueryResults.add(mergedList);
        }

        // OR the results from the subqueries
        for (int i = 0; i < subqueryResults.size(); i++) {
            finalResults = unionLists(finalResults, subqueryResults.get(i));
        }
        return finalResults;
        //printResults(finalResults);
    }

//    public void printResults(Set<Integer> results ) {
//        System.out.println("RESULTS:");
//        for (Integer docID: results) {
//            System.out.format("article%d.json %n", docID);
//        }
//        System.out.format("%nTotal documents: %d%n", results.size());
//    }


}