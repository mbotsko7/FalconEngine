import com.google.gson.*;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        if(in.hasNextLine()){
            dir = in.nextLine();
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
            } else {
                System.out.println("Error: Directory invalid");
            }
        }
        System.out.println("end");

        String query = "";
        while (!query.equals(":q")) {
            System.out.println("Enter search query: ");
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
                // normal query ?
            }
        }

    }

    private static void indexFile(String[] fileData, PositionalInvertedIndex index,
                                  int docID){
        try{
            int  i = 0;
            SimpleTokenStream stream = new SimpleTokenStream(fileData[1]); //currently not including title in the indexing
            while (stream.hasNextToken()){
                index.addTerm(stream.nextToken(), docID, i);
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
