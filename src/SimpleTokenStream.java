import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.SnowballStemmer;

import java.io.*;
import java.util.*;

/**
Reads tokens one at a time from an input stream. Returns tokens with minimal
processing: removing all non-alphanumeric characters, and converting to 
lowercase.
*/
public class SimpleTokenStream implements TokenStream {
   private Scanner mReader;
   private ArrayList<String> buffer = new ArrayList<>();
   /**
   Constructs a SimpleTokenStream to read from the specified file.
   */
   public SimpleTokenStream(File fileToOpen) throws FileNotFoundException {
      mReader = new Scanner(new FileReader(fileToOpen));
   }
   
   /**
   Constructs a SimpleTokenStream to read from a String of text.
   */
   public SimpleTokenStream(String text) {
      mReader = new Scanner(text);
   }

   /**
   Returns true if the stream has tokens remaining.
   */
   @Override
   public boolean hasNextToken() {
      return mReader.hasNext();
   }

   /**
   Returns the next token from the stream, or null if there is no token
   available.
   */
   @Override
   public String nextToken() {
      String next = "";
      if(buffer.isEmpty() == false){
         next = buffer.remove(0);
      }
      else if(!hasNextToken()){
         return null;
      }
      else {
         next = mReader.next();
      }
      if(next.indexOf("-") != -1){
         String[] splitted = next.split("-");
         for(String str : splitted){
            buffer.add(str);
         }
      }
      next = next.replaceAll("\\W", "").toLowerCase();
      try {
         Class stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
         SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
         stemmer.setCurrent(next);
         stemmer.stem();
         next = stemmer.getCurrent();
      }
      catch (Exception e){
         System.out.println("Exception while stemming: "+e.getMessage());
         e.printStackTrace();
      }
      if(next.length() > 0){
         return next;
      }
      else if(buffer.size() > 0 || hasNextToken()){
         return nextToken();
      }
      return null;
   }
}