import com.google.gson.*;

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
                    String[] file = parser.parseJSON(path);
                    indexFile(file, index, i);
                    i++;
                }

            }
            else {
                System.out.println("Error: Directory invalid");
            }
        }
    }

    private static void indexFile(String[] fileData, PositionalInvertedIndex index,
                                  int docID){
        try{
            int  i = 0;
            SimpleTokenStream stream = new SimpleTokenStream(fileData[2]); //currently not including title in the indexing
            while (stream.hasNextToken()){
                index.addTerm(stream.nextToken(), docID, i);
                i++;
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }
}
