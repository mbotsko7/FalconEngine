
import com.google.gson.*;
import org.tartarus.snowball.ext.englishStemmer;
import java.io.*;
import java.util.Arrays;
import java.util.Set;

public class Driver {

    PositionalInvertedIndex index = new PositionalInvertedIndex();
    KGramIndex kGramIndex = new KGramIndex();

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
                    indexFile(file, index, i);
                    i++;
                }
                String[] dict = index.getDictionary();
                for (int j = 0; j < dict.length; j++) {
                    kGramIndex.add(dict[j]);
                }
                return true;
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public String getVocabList() {

        String[] keys = index.getDictionary();
        String line = "";
        for (String k : keys) {
            line += k + "\n";
        }

        int vocabTotal = index.getTermCount();

        return "Total number of vocabulary terms: " + vocabTotal + "\n" + line;

    }

    public String stemToken(String token) {
        // stems a term given by the user

        englishStemmer stemmer = new englishStemmer();
        stemmer.setCurrent(token);
        stemmer.stem();
        String wordAfterStemmed = stemmer.getCurrent();
        return token + " --stemmed--> " + wordAfterStemmed;  // test
    }

    public Set<Integer> search(String query) {
        Search search = new Search(index);
        return search.searchForQuery(query);
        //display.setContent(new Label(results.toString()));

    }

    private static void indexFile(String[] fileData, PositionalInvertedIndex index,
                                  int docID) {
        try {
            int i = 0;
            SimpleTokenStream stream = new SimpleTokenStream(fileData[1]); //currently not including title in the indexing
            while (stream.hasNextToken()) {
                String next = stream.nextToken();
                if (next == null)
                    continue;
                index.addTerm(next, docID, i);
                if (stream.getHyphen() != null) {
                    for (String str : stream.getHyphen()) {
                        index.addTerm(str, docID, i);
                    }
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readDocument(File file) {
        String text = "";
        try {
            System.out.println("Displaying document.. \n");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br.readLine()) != null) {
                text += line;
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: File does not exist.");
        } catch (IOException e) {
            System.out.println("Error");
        }

        return text;
    }
}