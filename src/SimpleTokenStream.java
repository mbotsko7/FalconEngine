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
   private String[] hyphen;

   public SimpleTokenStream() {}
   /**
   Constructs a SimpleTokenStream to read from the specified file.
   */
   public SimpleTokenStream(File fileToOpen) throws FileNotFoundException {
      mReader = new Scanner(new FileReader(fileToOpen));
   }

   public String[] getHyphen(){
      return hyphen;
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

   public String parseAndStem(String str){
      String next = str.replaceAll("\\W", "").toLowerCase();
      next = next.replaceAll("_", "");
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
      return next;
   }

   /**
   Returns the next token from the stream, or null if there is no token
   available.
   */
   @Override
   public String nextToken() {
      String next = "";
      if(!hasNextToken()){
         return null;
      }
      else {
         next = mReader.next();
      }
      hyphen = null;
      if(next.contains("-")) {
         hyphen = next.split("-");
         for(int i = 0; i < hyphen.length; i++){
            hyphen[i] = parseAndStem(hyphen[i]);
         }
      }


      next = parseAndStem(next);
      if(next.length() > 0){
         return next;
      }
      else if(hasNextToken()){
         return nextToken();
      }
      return null;
   }


}