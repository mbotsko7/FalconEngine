import com.google.gson.*;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by bardsko on 9/11/17.
 */
public class Driver {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        System.out.println("Enter a directory to index: ");
        String dir = "";
        PositionalInvertedIndex index = new PositionalInvertedIndex();
        KGramIndex kGramIndex = new KGramIndex();
        if(in.hasNextLine()){
            dir = in.nextLine();
            File f = new File(dir);

            if(f.exists() && f.isDirectory()){
                String[] fileList = f.list();
                Arrays.sort(fileList, new FileComparator());    // sorts files before assigning docID
                Parser parser = new Parser();
                int i = 1;
                long begin = System.nanoTime();
                for(String path : fileList){

                    String[] file = parser.parseJSON(f.getPath()+"/"+path);
                    indexFile(file, index, i);
                    i++;
                    if(i % 1000 == 0)
                        System.out.println("#"+i+" "+(System.nanoTime()-begin));
                }
                System.out.println("building k-index");
                String[] dict = index.getDictionary();
                for(int j = 0; j < dict.length; j++){
                    kGramIndex.add(dict[j]);
                }
                System.out.println(System.nanoTime()-begin);
            } else {
                System.out.println("Error: Directory invalid");
            }
        }
        System.out.println("end");

        String query = "";
        while (!query.equals(":q")) {
            System.out.println("\nEnter search query: ");
            query = in.nextLine();

            if (query.startsWith(":stem")) {
                // stems a term given by the user
                englishStemmer stemmer = new englishStemmer();
                String wordToBeStemmed = query.substring(6);    // removes prefix

                stemmer.setCurrent(wordToBeStemmed);
                stemmer.stem();
                String wordAfterStemmed = stemmer.getCurrent();
                System.out.println(wordToBeStemmed + " --stemmed--> " + wordAfterStemmed);  // test
            } else if (query.startsWith(":index")) {
                // index the folder specified (exactly like above)
                dir = query.substring(7);
                File f = new File(dir);

                if(f.exists() && f.isDirectory()){
                    String[] fileList = f.list();
                    Parser parser = new Parser();
                    int i = 1;
                    for(String path : fileList){
                        String[] file = parser.parseJSON(f.getPath()+"/"+path);
                        indexFile(file, index, i);
                        i++;
                    }
                    System.out.println("building k-index");
                    String[] dict = index.getDictionary();
                    for(int j = 0; j < dict.length; j++){
                        kGramIndex.add(dict[j]);
                    }

                } else {
                    System.out.println("Error: Directory invalid");
                }
            } else if (query.startsWith(":vocab")) {
                // prints out all terms in vocabulary
                String[] keys = index.getDictionary();
                for (String k : keys) {
                    System.out.println(k);
                }

                int vocabTotal = index.getTermCount();
                System.out.println("\nTotal number of vocabulary terms: " + vocabTotal);
            } else {
                Search search = new Search(index);
                search.searchForQuery(query);

                System.out.print("\nDocument to view? ");
                String selectedDoc = in.nextLine();
                File fileToView = new File(dir + "/" + selectedDoc);

                try {
                    System.out.println("Displaying document.. \n");
                    BufferedReader br = new BufferedReader(new FileReader(fileToView));
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                    br.close();
                } catch (FileNotFoundException e) {
                    System.out.println("Error: File does not exist.");
                } catch (IOException e) {
                    System.out.println("Error");
                }

            }
        }

    }

    private static void indexFile(String[] fileData, PositionalInvertedIndex index,
                                  int docID){
        try{
            int  i = 0;
            SimpleTokenStream stream = new SimpleTokenStream(fileData[1]); //currently not including title in the indexing
            while (stream.hasNextToken()){
                String next = stream.nextToken();
                if(next == null)
                    continue;
                index.addTerm(next, docID, i);
                if(stream.getHyphen() != null){
                    for(String str : stream.getHyphen()){
                        index.addTerm(str, docID, i);
                    }
                }
                i++;
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }
}
