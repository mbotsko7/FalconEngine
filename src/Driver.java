
import org.tartarus.snowball.ext.englishStemmer;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Driver {

    PositionalInvertedIndex pIndex = new PositionalInvertedIndex();
    DiskKGIndex diskKGIndex;
    KGramIndex kGramIndex = new KGramIndex();
    HashMap<String, String> k = new HashMap<>();
    ArrayList<Double> documentWeights = new ArrayList<>();
    RandomAccessFile kIndexDisk;
    KGramIndex wildcardIndex = new KGramIndex();

    private String correction = "";

    public String getCorrection() {
        return correction;
    }

    public boolean indexDirectory(String folder) {
        File f = new File(folder);
        if (f.exists() && f.isDirectory()) {
            try {
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

                for (String s:k.keySet()){
                    kGramIndex.add(s);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            kGramIndex.setKeys(k);
            IndexWriter writer = new IndexWriter(folder);
            writer.buildIndex(pIndex, documentWeights);
            documentWeights.clear();

            // creates binary files from kgram index
//            KGIndexWriter kWriter = new KGIndexWriter(folder);
//            kWriter.buildKGIndex(kGramIndex);

            // serializes kGramIndex object into binary file (kgIndex.bin)
            try {
                FileOutputStream fileOut = new FileOutputStream(new File(folder, "kgIndex.bin"));
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(kGramIndex);
                out.close();
                fileOut.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public String[] getVocabList() {
        // returns all dictionary in positional inverted pIndex
        return pIndex.getDictionary();

    }

    public String stemToken(String token) {
        // stems a term given by the user
        englishStemmer stemmer = new englishStemmer();
        stemmer.setCurrent(token);
        stemmer.stem();
        String wordAfterStemmed = stemmer.getCurrent();
        return token + " --stemmed--> " + wordAfterStemmed;  // test
    }

    public void readWildcardIndex(String indexName) {
//        HashMap<String, ArrayList<String>> wildcardIndex;
        // deserialize index written to binary
        // saves in memory to wildcardIndex
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
    }

    public List<Integer> searchBoolean(String dir, String query) {
        SimpleTokenStream stream = new SimpleTokenStream();
        HashMap<String, String> keys = wildcardIndex.getKeys();

        BooleanRetrieval search = new BooleanRetrieval(dir, wildcardIndex, keys);
        ArrayList<String> natQuery = new ArrayList<>();
        for(String s: query.split(" ")){
            natQuery.add(s);
        }
        SpellingCorrection spellingCorrection = new SpellingCorrection(natQuery, wildcardIndex, new DiskInvertedIndex(dir));
        String spellcorrect = spellingCorrection.result();
        if(spellcorrect.isEmpty() == false)
            correction = spellcorrect;
        else
            correction = "";

        System.out.println(spellcorrect);
        return search.searchForQuery(query);
    }



    public DocWeight[] searchRanked(String indexName, String query) {

        DiskInvertedIndex index = new DiskInvertedIndex(indexName);
//        DiskKGIndex kgIndex = new DiskKGIndex(indexName);
        SimpleTokenStream s = new SimpleTokenStream(query);
        ArrayList<String> queryList = new ArrayList<>();
        ArrayList<String> natQuery = new ArrayList<>();
        while (s.hasNextToken()){
            String token = s.nextToken();
            if(token.isEmpty() == false){
                natQuery.add(s.getOriginal());
                if(token.contains("*") == false)
                    queryList.add(token);
                else{
                    WildcardQuery q = new WildcardQuery(token);
                    HashMap<String, String> val = wildcardIndex.getKeys();
                    for(String str : q.queryResult(wildcardIndex)){
                        queryList.add(val.get(str));
                    }
                }
            }
        }
        SpellingCorrection spellingCorrection = new SpellingCorrection(natQuery, wildcardIndex, index);
        String spellcorrect = spellingCorrection.result();
        if(spellcorrect.isEmpty() == false)
            correction = spellcorrect;
        else
            correction = "";
        System.out.println(spellcorrect);
        RankedRetrieval rankedRetrieval = new RankedRetrieval(queryList, index);
        int i = 1;
        for(DocWeight dw : rankedRetrieval.rank()){
            if(dw == null) {
                System.out.println("No other documents scored for query");
                break;
            }
            System.out.println((i++) +". Doc"+ dw.getDocID()+" "+dw.getDocWeight());

        }

        return rankedRetrieval.rank();
    }

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

    public static String readDocument(File file) {
        String text = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br.readLine()) != null) {
                text += line;
            }
            br.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Error: File does not exist.");
        }
        catch (IOException e) {
            System.out.println("Error");
        }

        return text;
    }

    // #kgrams, kgram, #terms, [terms], kgram, #terms, [terms], etc.
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