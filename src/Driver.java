
import org.tartarus.snowball.ext.englishStemmer;
import java.io.*;
import java.util.*;


public class Driver {

    PositionalInvertedIndex pIndex = new PositionalInvertedIndex();
    DiskKGIndex diskKGIndex;
    KGramIndex kGramIndex = new KGramIndex();
    HashMap<String, String> k = new HashMap<>();
    ArrayList<Double> documentWeights = new ArrayList<>();



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

            IndexWriter writer = new IndexWriter(folder);
            writer.buildIndex(pIndex, documentWeights);
            documentWeights.clear();

            KGIndexWriter kWriter = new KGIndexWriter(folder);
            kWriter.buildKGIndex(kGramIndex);
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

    public List<Integer> searchBoolean(String dir, String query) {

        BooleanRetrieval search = new BooleanRetrieval(dir, diskKGIndex, k);
        return search.searchForQuery(query);
        //display.setContent(new Label(results.toString()));

    }


    public DocWeight[] searchRanked(String indexName, String query) {

        DiskInvertedIndex index = new DiskInvertedIndex(indexName);
        DiskKGIndex kgIndex = new DiskKGIndex(indexName);
            SimpleTokenStream s = new SimpleTokenStream(query);
            ArrayList<String> queryList = new ArrayList<>();
            while (s.hasNextToken()){
                String token = s.nextToken();
                if(token.isEmpty() == false){
                    if(token.contains("*") == false)
                        queryList.add(token);
                    else{
                        ArrayList<String> wildTerms = new ArrayList<>();
                        for(String str : KGramIndex.kGramify(s.getOriginal())){
                            for(String str2:kgIndex.getTerms(str))
                                wildTerms.add(str2);
                        }
                        Collections.sort(wildTerms);
                        String prev = wildTerms.get(0);
                        for(int i = 0; i < wildTerms.size(); i++){
                            String current = wildTerms.get(i);
                            if(prev.equals(current)){
                                wildTerms.remove(i);
                                i--;
                            }
                            else
                                prev = current;
                        }
                        queryList.addAll(wildTerms);
                    }
                }
            }
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
}