import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BooleanRetrieval {

    private PositionalInvertedIndex index = new PositionalInvertedIndex();
    private SimpleTokenStream stream = new SimpleTokenStream();
    private KGramIndex kindex = new KGramIndex();
    private KGramIndex diskKGIndex;
    private HashMap<String, String> kGramKeys = new HashMap<>();

    private DiskInvertedIndex dIndex;

    public BooleanRetrieval(String path, KGramIndex k, HashMap<String, String> map) {
        this.dIndex = new DiskInvertedIndex(path);
        diskKGIndex = k;
        this.kGramKeys = map;
    }

    public BooleanRetrieval(PositionalInvertedIndex index, KGramIndex k, HashMap<String, String> map) {
        this.index = index;
        this.kindex = k;
        this.kGramKeys = map;
    }

    // perform an AND intersection on two lists of docIDs
    // return a list of docIDs for documents that contain both terms
    public List<Integer> intersectLists(List<Integer> listA, List<Integer> listB) {

        List<Integer> results = new ArrayList<>();
        if (!listA.isEmpty() && !listB.isEmpty()) {
            int i = 0, j = 0;
            while (i < listA.size() && j < listB.size()) {
                int compare = listA.get(i).compareTo(listB.get(j));
                if (compare == 0) {
                    results.add(listA.get(i));
                    i++;
                    j++;
                }
                else if (compare > 0) {
                    j++;
                }
                else {
                    i++;
                }
            }
        }
        return results;
    }

    // perform an OR operation on two lists of docIDs
    // return a list of docIDs that appear in either list
    public List<Integer> unionLists(List<Integer> listA, List<Integer> listB) {

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
                }
                else if (compare > 0) {
                    results.add(listB.get(j));
                    j++;
                }
                else {
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

    // get list of documents that contain the given term
    public List<Integer> getDocIDList(String term) {
        DiskPosting[] postings = dIndex.getPostingsWithPositions(term);
        List<Integer> docList = new ArrayList<>();
        for (DiskPosting posting: postings) {
            docList.add(posting.getDocID());
        }
        return docList;
    }

    //search for near k
    public List<Integer> searchNearK(String query) {
        List<Integer> results = new ArrayList<>();
        
        Pattern p = Pattern.compile("([\\w-\']+)\\sNEAR/(\\d+)\\s([\\w-\']+)");
        Matcher m = p.matcher(query);
        if (m.matches()) {
            String first = stream.parseAndStem(m.group(1));
            int k = Integer.parseInt(m.group(2));
            String second = stream.parseAndStem(m.group(3));

            // get list of docs that contain the first term
            List<Integer> firstDocs = getDocIDList(first);
            if (!firstDocs.isEmpty() && !getDocIDList(second).isEmpty()) {
                searchDoc:
                    // loop through each doc that contains the first term
                    for (Integer docID:firstDocs) {
                        List<Integer> positionsOfFirst = dIndex.getPositionsInDoc(first, docID);
                        List<Integer> positionsOfSecond = dIndex.getPositionsInDoc(second, docID);
                        // loop through first's positions in the current doc and compare with second's positions
                        int j = 0;
                        for (Integer firstPos: positionsOfFirst) {
                            if (positionsOfSecond.size() == 0) {
                                continue searchDoc;
                            }
                            while (j < positionsOfSecond.size()-1 && positionsOfSecond.get(j) < firstPos) {
                               j++;
                               int a = positionsOfSecond.get(j);
                           }
                           if (positionsOfSecond.get(j) > firstPos && positionsOfSecond.get(j) <= firstPos + k) {
                               results.add(docID);
                               continue searchDoc;
                           }
                        }
                    }
            }
        }
        return results;
    }

    // returns a list of docIDs that contain the entire phrase
    public List<Integer> searchPhraseLiteral(String phrase) {
        List<Integer> results = new ArrayList<>();

        // separate phrase into individual stemmed tokens
        String[] terms = Query.getPhraseTokens(phrase);
        for (int i = 0; i < terms.length; i++) {
            terms[i] = stream.parseAndStem(terms[i]);
        }

        // get list of documents that contain the first term
        List<Integer> firstTermDocs = getDocIDList(terms[0]);
        if (!firstTermDocs.isEmpty()) {
            // loop through each doc that contains the first term
            searchCurrentDoc:
                for (Integer docID : firstTermDocs) {
                    List<List<Integer>> termPositionsLists = new ArrayList<List<Integer>>();
                    // get position list of each phrase term in the current document
                    for (int i = 0; i < terms.length; i++) {
                        List<Integer> termPostingsInDoc = dIndex.getPositionsInDoc(terms[i], docID);
                        if (termPostingsInDoc.isEmpty()) {
                            // phrase is not in current doc, skip to next doc
                            continue searchCurrentDoc;
                        }
                        termPositionsLists.add(termPostingsInDoc);
                    }

                    // loop through first term's positions and check if other tokens are adjacent
                    boolean matched;
                    List<Integer> first = termPositionsLists.get(0);
                    int[] seekPos = new int[terms.length];
                    // array of seek positions for all terms in phrase
                    for (int i = 0; i < seekPos.length; i++) {
                        // initialize all seek positions as 0
                        seekPos[i] = 0;
                    }

                    for (int i = 0; i < first.size(); i++) {
                        matched = true;
                        int start = first.get(i);
                        // look at jth adjacent word from first term in phrase
                        for (int j = 1; j < termPositionsLists.size(); j++) {
                            int pos = seekPos[j];
                            int listSize = termPositionsLists.get(j).size();
                            while (pos < listSize - 1 && termPositionsLists.get(j).get(pos) < start + j) {
                                pos++;
                            }
                            seekPos[j] = pos;
                            // update position in seekPos array
                            if (!termPositionsLists.get(j).get(pos).equals(start + j)) {
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

    // search the document for the query
    public List<Integer> searchForQuery(String query) {
        List<List<Integer>> subqueryResults = new ArrayList<List<Integer>>();
        List<Integer> finalResults = new ArrayList<>();

        // process each subquery one at a time
        String[] subqueries = Query.getSubqueries(query);
        for (String subquery : subqueries) {
            List<List<Integer>> literalsPostings = new ArrayList<List<Integer>>();
            List<String> literals = Query.getQueryLiterals(subquery);
            // get list of docs for each literal
            for (String literal : literals) {
                // for phrases
                if (literal.startsWith("\"")) {
                    literalsPostings.add(searchPhraseLiteral(literal));
                }
                else if (literal.contains("*")) {
                    WildcardQuery q = new WildcardQuery(literal);
                    ArrayList<ArrayList<Integer>> wildcardListing = new ArrayList<>();
                    ArrayList<Integer> wildList = new ArrayList<>();

                    for (String wild : q.queryResult(diskKGIndex)) {
                        wild = kGramKeys.get(wild);
                        ArrayList<Integer> t = (ArrayList<Integer>) getDocIDList(wild);
                        wildcardListing.add(t);
                    }
                    wildList.addAll(wildcardListing.get(0));
                    if(wildcardListing.size() >= 2){
                        for(int i = 1; i < wildcardListing.size(); i++){
                            wildList = (ArrayList<Integer>) unionLists(wildList, wildcardListing.get(i));
                        }
                    }
                    literalsPostings.add(wildList);

                } else if (literal.contains("NEAR/")){
                    literalsPostings.add(searchNearK(literal));
                }
                else { // for single tokens
                    literal = stream.parseAndStem((literal));
                    literalsPostings.add(getDocIDList(literal));
                }
            }
            // merge doc lists for each literal
            List<Integer> mergedList = literalsPostings.get(0);
            for (int i = 1; i < literalsPostings.size(); i++) {
                mergedList = intersectLists(mergedList, literalsPostings.get(i));
                if (mergedList.isEmpty())
                    break;
            }
            subqueryResults.add(mergedList);
        }

        // OR the results from the subqueries
        for (int i = 0; i < subqueryResults.size(); i++) {
            finalResults = unionLists(finalResults, subqueryResults.get(i));
        }
        return finalResults;

    }
}
