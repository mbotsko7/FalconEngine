
import org.tartarus.snowball.ext.englishStemmer;
import java.io.*;
import java.util.*;


public class Driver {

    PositionalInvertedIndex pIndex = new PositionalInvertedIndex();
    KGramIndex kGramIndex = new KGramIndex();
    DiskKGIndex diskKGIndex;
    HashMap<String, String> k = new HashMap<>();
    ArrayList<Double> documentWeights = new ArrayList<>();



    public boolean indexDirectory(File f) {
        if (f.exists() && f.isDirectory()) {
            try {
                String[] fileList = f.list();
                Arrays.sort(fileList, new FileComparator());    // sorts files before assigning docID
                Parser parser = new Parser();

                int i = 1;
                long begin = System.nanoTime();

                for (String path : fileList) {
                    DocumentWeight documentWeight = new DocumentWeight();
                    String[] file = parser.parseJSON(f.getPath() + "/" + path);
                    indexFile(file, pIndex, i, k, documentWeight);
                    documentWeights.add(documentWeight.calculateWeight());
                    i++;
                }
                System.out.println(System.nanoTime()-begin);
                begin = System.nanoTime();

                // creates binary files and saves them
                // in the same directory that was indexed
                String dir = f.toString();
                IndexWriter writer = new IndexWriter(dir);
                writer.buildIndex(pIndex, documentWeights);

//                for(String s : k.keySet()){
//                    kGramIndex.add(s);
//                }
                System.out.println(System.nanoTime()-begin);
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
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

    public List<Integer> searchBoolean(String dir, String query) {

        BooleanRetrieval search = new BooleanRetrieval(dir, diskKGIndex, k);
        return search.searchForQuery(query);
        //display.setContent(new Label(results.toString()));

    }

    public List<Integer> searchRanked(String query) {

        // TODO: Hook up ranked retrieval code here

        return null;
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
}