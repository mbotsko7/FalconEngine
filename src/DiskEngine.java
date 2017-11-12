
import javafx.geometry.Pos;

import java.io.File;
import java.util.*;


public class DiskEngine {

    public static void main(String[] args) {
        PositionalInvertedIndex pIndex;
        HashMap<String, String> k;
        ArrayList<Double> documentWeights;
        KGramIndex kGramIndex = new KGramIndex();
        Scanner scan = new Scanner(System.in);

        System.out.println("Menu:");
        System.out.println("1) Build index");
        System.out.println("2) Read and query index");
        System.out.println("Choose a selection:");
        int menuChoice = scan.nextInt();
        scan.nextLine();

        switch (menuChoice) {
            // only need to run once to create binary files
            case 1:
                System.out.println("Enter the name of a directory to index: ");
                String folder = scan.nextLine();

                // creates positional inverted index that will be written to disk
                pIndex = new PositionalInvertedIndex();
                k = new HashMap<>();
                documentWeights = new ArrayList<>();
                File f = new File(folder);

                if (f.exists() && f.isDirectory()) {
                    String[] fileList = f.list();
                    Arrays.sort(fileList, new FileComparator());    // sorts files before assigning docID
                    Parser parser = new Parser();
                    int i = 1;
                    for (String path : fileList) {
                        DocumentWeight documentWeight = new DocumentWeight();
                        String[] file = parser.parseJSON(f.getPath() + "/" + path);
                        indexFile(file, pIndex, i, k, documentWeight);
                        documentWeights.add(documentWeight.calculateWeight());
                        i++;
                    }

                    for(String s : k.keySet()){
                        kGramIndex.add(s);
                    }
                }

                // creates binary files and saves them
                // in the same directory that was indexed
                IndexWriter writer = new IndexWriter(folder);
                writer.buildIndex(pIndex, documentWeights);
                documentWeights.clear();

                // creates binary files for kgram index
                KGIndexWriter kWriter = new KGIndexWriter(folder);
                kWriter.buildKGIndex(kGramIndex);

                List<PositionalIndex> test1 = pIndex.getPostings2("i");
                List<PositionalIndex> test2 = pIndex.getPostings2("am");
                break;

            case 2:
                System.out.println("Enter the name of an index to read:");
                String indexName = scan.nextLine();

//                DiskInvertedIndex index = new DiskInvertedIndex(indexName);

                System.out.println("Enter query mode:");
                System.out.println("1) boolean retrieval");
                System.out.println("2) ranked retrieval");

                int queryChoice = scan.nextInt();
                scan.nextLine();

                switch (queryChoice) {
                    case 1:
                        while (true) {
                            System.out.println("\nEnter boolearn retrieval query:");
                            String input = scan.nextLine();

                            if (input.equals("EXIT")) {
                                break;
                            }

                            // kgram index disk test
//                            DiskKGIndex kIndex = new DiskKGIndex(indexName);
//                            String[] testList = kIndex.getTerms(input);

                            // testing diskinvertedindex
                            DiskInvertedIndex piIndex = new DiskInvertedIndex(indexName);
                            DiskPosting[] diskPostings = piIndex.getPostingsWithPositions(input);

                            HashMap<String, String> keys = new HashMap<>();
                            BooleanRetrieval search = new BooleanRetrieval(indexName, kGramIndex, keys);

                            List<Integer> results = search.searchForQuery(input);

                            for (Integer result: results) {
                                System.out.println("Doc# " + result);
                            }

//                            DiskPosting[] postingsList = index.getPostings(input.toLowerCase());
//
//                            if (postingsList == null) {
//                                System.out.println("Term not found");
//                            } else {
//                                System.out.print("\nDocs: \n");
//                                for (DiskPosting d : postingsList) {
//                                    int docID = d.getDocID();
//                                    System.out.print("Doc# " + docID + "-- Positions: ");
//                                    for (int i = 0; i < d.getTermFrequency(); i++) {
//                                        System.out.print(d.getPositions().get(i) + " ");
//                                    }
//                                    System.out.println();
//                                }
//                                System.out.println();
//                                System.out.println();
//                            }
                        }
                        break;
                    case 2:
                        /*** ranked retrieval code ***/
                        System.out.println("Welcome! Ranked Retrieval is in testing...\n" +
                                "W_dt calculations are tested to be correct\n" +
                                "testing ranking...");
                        DiskInvertedIndex index = new DiskInvertedIndex(indexName);

                        System.out.println("Query: ");
                        try{
                            Scanner in = new Scanner(System.in);
                            String line = in.nextLine().trim();
                            SimpleTokenStream s = new SimpleTokenStream(line);
                            ArrayList<String> queryList = new ArrayList<>();
                            while (s.hasNextToken()){
                                String token = s.nextToken();
                                if(token.isEmpty() == false){
                                    queryList.add(token);
                                }
                            }
                            RankedRetrieval rankedRetrieval = new RankedRetrieval(queryList, index);
                            int i = 1;
                            for(DocWeight dw : rankedRetrieval.rank()){
                                if(dw == null) {
                                    System.out.println("No other documents scored for query");
                                    break;
                                }
                                System.out.println((i++) +". Doc"+ dw.getDocID()+" "+dw.getDocWeight());

                            }
                        }
                        catch (Exception e){e.printStackTrace();}
                        break;
                }
        }
    }

    // from driver.java
    private static void indexFile(String[] fileData, PositionalInvertedIndex index,
                                  int docID, HashMap<String, String> k, DocumentWeight documentWeight) {

        try {
            int i = 0;
            SimpleTokenStream stream = new SimpleTokenStream(fileData[0] + " " + fileData[1]); //currently not including url in the indexing
            while (stream.hasNextToken()) {
                String next = stream.nextToken();
                if (next == null)
                    continue;
                String original = stream.getOriginal();
                if(k.containsKey(original) == false)
                    k.put(original, next);
                documentWeight.addTerm(original);
                index.addTerm(next, docID, i);
                if (stream.getHyphen() != null) {
                    for (String str : stream.getHyphen()) {
                        index.addTerm(str, docID, i);
                    }
                }
                i++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
