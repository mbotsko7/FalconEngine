
import com.google.gson.*;
import org.tartarus.snowball.ext.englishStemmer;
import java.io.*;
import java.util.*;


public class Driver {

    PositionalInvertedIndex index = new PositionalInvertedIndex();
    KGramIndex kGramIndex = new KGramIndex();
    HashMap<String, String> keys = new HashMap<>();

    public boolean indexDirectory(File f) {
        if (f.exists() && f.isDirectory()) {
            try {
                String[] fileList = f.list();
                Arrays.sort(fileList, new FileComparator());    // sorts files before assigning docID
                Parser parser = new Parser();
                int i = 1;
                long begin = System.nanoTime();
                for (String path : fileList) {
                    String[] file = parser.parseJSON(f.getPath() + "/" + path);
                    indexFile(file, index, i, keys);
                    i++;
                }
                System.out.println(System.nanoTime()-begin);
                begin = System.nanoTime();
                for(String s : keys.keySet()){
                    kGramIndex.add(s);
                }
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
        // returns all dictionary in positional inverted index
        return index.getDictionary();

    }

    public String stemToken(String token) {
        // stems a term given by the user
        englishStemmer stemmer = new englishStemmer();
        stemmer.setCurrent(token);
        stemmer.stem();
        String wordAfterStemmed = stemmer.getCurrent();
        return token + " --stemmed--> " + wordAfterStemmed;  // test
    }

    public List<Integer> search(String query) {
        Search search = new Search(index, kGramIndex, keys);
        return search.searchForQuery(query);
        //display.setContent(new Label(results.toString()));

    }

    private static void indexFile(String[] fileData, PositionalInvertedIndex index,
                                  int docID, HashMap<String, String> k) {

        try {
            int i = 0;
            SimpleTokenStream stream = new SimpleTokenStream(fileData[0] + " " + fileData[1]); //currently not including url in the indexing
            while (stream.hasNextToken()) {
                String next = stream.nextToken();
                if (next == null)
                    continue;
                if(k.containsKey(stream.getOriginal()) == false)
                    k.put(stream.getOriginal(), next);
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