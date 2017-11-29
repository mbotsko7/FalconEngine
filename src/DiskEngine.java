
import javafx.geometry.Pos;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class DiskEngine {

    public static void main(String[] args) {
        PositionalInvertedIndex pIndex;
        HashMap<String, String> k;
        ArrayList<Double> documentWeights;
        KGramIndex kGramIndex = new KGramIndex();
        RandomAccessFile kIndexDisk;
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

                // creates in memory positional inverted index and kgram index
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
                kGramIndex.setKeys(k);

                // creates binary files and saves them
                // in the same directory that was indexed
                IndexWriter writer = new IndexWriter(folder);
                writer.buildIndex(pIndex, documentWeights);
                documentWeights.clear();

                // creates binary files for kgram index
                // serializes kGramIndex object into binary file (kgIndex.bin)
                try {
                    FileOutputStream fileOut = new FileOutputStream(new File(folder, "kgIndex.bin"));
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(kGramIndex);
                    out.close();
                    fileOut.close();
                }
                catch (IOException i) {
                    i.printStackTrace();
                }
                break;

            case 2:
                System.out.println("Enter the name of an index to read:");
                String indexName = scan.nextLine();

                // deserialize index written to binary
                // saves in memory to wildcardIndex
                KGramIndex wildcardIndex = new KGramIndex();
                try {
                    FileInputStream fileIn = new FileInputStream(indexName + "/kgIndex.bin");
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    wildcardIndex = null;
                    wildcardIndex = (KGramIndex) in.readObject();
                } catch (IOException i) {
                    i.printStackTrace();
                } catch (ClassNotFoundException c) {
                    System.out.println("class not found");
                    c.printStackTrace();
                }


                System.out.println("Enter query mode:");
                System.out.println("1) boolean retrieval");
                System.out.println("2) ranked retrieval");

                int queryChoice = scan.nextInt();
                scan.nextLine();

                switch (queryChoice) {
                    case 1:
                        while (true) {
                            System.out.println("\nEnter boolean retrieval query:");
                            String input = scan.nextLine();

                            if (input.equals("EXIT")) {
                                break;
                            }
                            SimpleTokenStream stream = new SimpleTokenStream();
                            HashMap<String, String> keys = new HashMap<>();

                            keys = wildcardIndex.getKeys();
                            ArrayList<String> natQuery = new ArrayList<>();

                            for(String s: input.split(" ")){
                                natQuery.add(s);
                            }
                            SpellingCorrection spellingCorrection = new SpellingCorrection(natQuery, wildcardIndex, new DiskInvertedIndex(indexName));
                            String spellcorrect = spellingCorrection.result();
                            if(spellcorrect.isEmpty() == false)
                                spellcorrect = "Did you mean: "+spellcorrect+"?";
                            System.out.println(spellcorrect);
                            BooleanRetrieval search = new BooleanRetrieval(indexName, wildcardIndex, keys);

                            List<Integer> results = search.searchForQuery(input);

                            for (Integer result: results) {
                                System.out.println("Doc# " + result);
                            }
                        }
                        break;
                    case 2:
                        /*** ranked retrieval code ***/
                        DiskInvertedIndex index = new DiskInvertedIndex(indexName);
                        System.out.println("Query: ");
                        try{
                            Scanner in = new Scanner(System.in);
                            String line = in.nextLine().trim();
                            SimpleTokenStream s = new SimpleTokenStream(line);
                            ArrayList<String> queryList = new ArrayList<>();
                            ArrayList<String> natQuery = new ArrayList<>();
                            while (s.hasNextToken()){
                                String token = s.nextToken();
                                if(token.isEmpty() == false){
                                    natQuery.add(s.getOriginal());
                                    if(token.contains("*") == false){
                                        queryList.add(token);

                                    }
                                    else{
                                        WildcardQuery q = new WildcardQuery(token);
                                        HashMap<String,String> val = wildcardIndex.getKeys();
                                        for(String str : q.queryResult(wildcardIndex)){
                                            queryList.add(val.get(str));
                                        }
                                    }
                                }
                            }
                            SpellingCorrection spellingCorrection = new SpellingCorrection(natQuery, wildcardIndex, index);
                            String spellcorrect = spellingCorrection.result();
                            if(spellcorrect.isEmpty() == false) {
                                spellcorrect = "Did you mean: " + spellcorrect + "?";
                            }
                            System.out.println(spellcorrect);
                            RankedRetrieval rankedRetrieval = new RankedRetrieval(queryList, index);

                            int i = 1;
                            for(DocWeight dw : rankedRetrieval.rank()) {
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

    // used to index file and create positional inverted index
    private static void indexFile(String[] fileData, PositionalInvertedIndex index,
                                  int docID, HashMap<String, String> k, DocumentWeight documentWeight) {

        try {
            int i = 0;
            SimpleTokenStream stream = new SimpleTokenStream(fileData[1]);
            while (stream.hasNextToken()) {
                String next = stream.nextToken();
                if (next == null) {
                    continue;
                }
                String original = stream.getOriginal();
                if(k.containsKey(original) == false) {
                    k.put(original, next);
                }
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

    // from driver.java


    // #kgrams, kgram, #terms, [terms], kgram, #terms, [terms], etc.
    // to read in binary file and save it to an in memory kgram index
    // (did not use -- couldn't get to read in properly. switched to serializing)
    private static void readWildcardIndex(RandomAccessFile k, HashMap<String,
                                            ArrayList<String>> wildcardIndex) {
        try {
            // read the 4 bytes for the kgram frequency
            byte[] buffer = new byte[4];
            k.read(buffer, 0, buffer.length);

            // use ByteBuffer to convert the 4 bytes into an int.
            int kFrequency = ByteBuffer.wrap(buffer).getInt();
            System.out.println("number of kgrams: " + kFrequency);

            for (int j = 0; j < kFrequency; j++) {
                k.read(buffer, 0, buffer.length);
                int kLength = ByteBuffer.wrap(buffer).getInt();

                byte[] tBuffer = new byte[kLength];
                k.read(tBuffer, 0, tBuffer.length);
                String kgram = new String(tBuffer, StandardCharsets.UTF_8);
                System.out.println("kgram: " + kgram);

                // read the 4 bytes for the term frequency
                k.read(buffer, 0, buffer.length);

                // use ByteBuffer to convert the 4 bytes into an int.
                int termFrequency = ByteBuffer.wrap(buffer).getInt();
                System.out.print("number of terms: " + termFrequency + "\nterms: ");

                // initialize the array that will hold the terms.
                ArrayList<String> termsList = new ArrayList<>();

                // reads 4 bytes at a time from file
                // grabs terms for a key in kgram index
                for (int i = 0; i < termFrequency; i++) {
                    k.read(buffer, 0, buffer.length);
                    int termLength = ByteBuffer.wrap(buffer).getInt();

                    //System.out.println(termLength);
                    tBuffer = new byte[termLength];
                    k.read(tBuffer, 0, tBuffer.length);

                    // converts byte array to readable string
                    String actualTerm = new String(tBuffer, StandardCharsets.UTF_8);
                    System.out.print(actualTerm + " ");
                    termsList.add(actualTerm);
                }
                System.out.println();
                System.out.println();
                wildcardIndex.put(kgram, termsList);
            }

        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }
}
