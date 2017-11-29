import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class FederalDriver {
    public static void main(String[] args){
        PositionalInvertedIndex pIndex;
        HashMap<String, String> k;
        ArrayList<Double> documentWeights;
        KGramIndex kGramIndex = new KGramIndex();
        RandomAccessFile kIndexDisk;
        Scanner in = new Scanner(System.in);

        System.out.println("Menu: ");
        System.out.println("1) Build Index");
        System.out.println("Choose a selection:");
        int choice = in.nextInt();
        in.nextLine();
        switch (choice){
            case 1:
                System.out.println("Enter the name of a directory to index: ");
                String folder = in.nextLine();

                // creates positional inverted index that will be written to disk
                pIndex = new PositionalInvertedIndex();
                k = new HashMap<>();
                documentWeights = new ArrayList<>();
                File f = new File(folder);

                // creates in memory positional inverted index and kgram index
                if (f.exists() && f.isDirectory()) {
                    String[] fileList = f.list();
                    Arrays.sort(fileList, new FileComparator());    // sorts files before assigning docID
                    Parser parser = new Parser();
                    int i = 1;
                    for (String path : fileList) {
                        DocumentWeight documentWeight = new DocumentWeight();
                        String[] file = parser.parseRawText(f.getPath() + "/" + path);
                        indexFile(file, pIndex, i, k, documentWeight);
                        documentWeights.add(documentWeight.calculateWeight());
                        i++;
                    }

                    for(String s : k.keySet()){
                        kGramIndex.add(s);
                    }
                }
                kGramIndex.setKeys(k);

                // creates binary files and saves them
                // in the same directory that was indexed
                IndexWriter writer = new IndexWriter(folder);
                writer.buildIndex(pIndex, documentWeights);
                documentWeights.clear();

                // creates binary files for kgram index
                // serializes kGramIndex object into binary file (kgIndex.bin)
                try {
                    FileOutputStream fileOut = new FileOutputStream(new File(folder, "kgIndex.bin"));
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(kGramIndex);
                    out.close();
                    fileOut.close();
                }
                catch (IOException i) {
                    i.printStackTrace();
                }
                break;
        }
    }

    // used to index file and create positional inverted index
    private static void indexFile(String[] fileData, PositionalInvertedIndex index,
                                  int docID, HashMap<String, String> k, DocumentWeight documentWeight) {

        try {
            int i = 0;
            SimpleTokenStream stream = new SimpleTokenStream(fileData[1]);
            while (stream.hasNextToken()) {
                String next = stream.nextToken();
                if (next == null) {
                    continue;
                }
                String original = stream.getOriginal();
                if(k.containsKey(original) == false) {
                    k.put(original, next);
                }
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
}
