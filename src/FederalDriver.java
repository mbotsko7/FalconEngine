import java.io.*;
import java.util.*;

public class FederalDriver {
    public static void main(String[] args) {
        FederalistIndex hIndex = new FederalistIndex();         // hamilton index
        FederalistIndex jIndex = new FederalistIndex();         // jay index
        FederalistIndex mIndex = new FederalistIndex();         // madison index
        FederalistIndex uIndex = new FederalistIndex();         //unknown ("disputed") index

        indexDirectory(uIndex, "DISPUTED");
        indexDirectory(hIndex, "HAMILTON");     // indexes hamilton papers
        indexDirectory(jIndex, "JAY");         // indexes jay papers
        indexDirectory(mIndex, "MADISON");      // indexes madison papers

        PriorityQueue<Double> maxHeapHamilton = new PriorityQueue<>(1, Collections.reverseOrder());
        HashMap<Double, String> hamiltonValues = new HashMap<>();
        for (String term : hIndex.getDictionary()) {
            double N = hIndex.getTotalDocuments() + jIndex.getTotalDocuments() + mIndex.getTotalDocuments();
            double N11 = hIndex.getDocumentFrequency(term);
            double N10 = jIndex.getDocumentFrequency(term) + mIndex.getDocumentFrequency(term);
            double N01 = hIndex.getTotalDocuments() - N11;
            double N00 = N - (N11 + N10 + N01);

            double I = (N11/N)*Math.log((N*N11)/((N11+N10)*(N11+N01))) +
                       (N01/N)*Math.log((N*N01)/((N01+N00)*(N11+N01))) +
                       (N10/N)*Math.log((N*N10)/((N11+N10)*(N00+N10))) +
                       (N00/N)*Math.log((N*N00)/((N00+N01)*(N00+N10)));
            // MutualInfo m = new MutualInfo(term, I);
            if (!Double.isNaN(I)) {
                maxHeapHamilton.add(I);
            }
            hamiltonValues.put(I, term);
        }

        // calculates I(t,c) for terms in jay
        // created discriminating set (different values for 'k')
        PriorityQueue<Double> maxHeapJay = new PriorityQueue<>(1, Collections.reverseOrder());
        HashMap<Double, String> jayValues = new HashMap<>();
        for (String term : jIndex.getDictionary()) {
            double N = hIndex.getTotalDocuments() + jIndex.getTotalDocuments() + mIndex.getTotalDocuments();
            double N11 = jIndex.getDocumentFrequency(term);
            double N10 = hIndex.getDocumentFrequency(term) + mIndex.getDocumentFrequency(term);
            double N01 = jIndex.getTotalDocuments() - N11;
            double N00 = N - (N11 + N10 + N01);

            double I = (N11/N)*Math.log((N*N11)/((N11+N10)*(N11+N01))) +
                    (N01/N)*Math.log((N*N01)/((N01+N00)*(N11+N01))) +
                    (N10/N)*Math.log((N*N10)/((N11+N10)*(N00+N10))) +
                    (N00/N)*Math.log((N*N00)/((N00+N01)*(N00+N10)));
            // MutualInfo m = new MutualInfo(term, I);
            if (!Double.isNaN(I)) {
                maxHeapJay.add(I);
            }
            jayValues.put(I, term);
        }

        // calculates I(t,c) for terms in madison
        // created discriminating set (different values for 'k')
        PriorityQueue<Double> maxHeapMadison = new PriorityQueue<>(1, Collections.reverseOrder());
        HashMap<Double, String> madisonValues = new HashMap<>();
        for (String term : mIndex.getDictionary()) {
            double N = hIndex.getTotalDocuments() + jIndex.getTotalDocuments() + mIndex.getTotalDocuments();
            double N11 = mIndex.getDocumentFrequency(term);
            double N10 = hIndex.getDocumentFrequency(term) + jIndex.getDocumentFrequency(term);
            double N01 = mIndex.getTotalDocuments() - N11;
            double N00 = N - (N11 + N10 + N01);

            double I = (N11/N)*Math.log((N*N11)/((N11+N10)*(N11+N01))) +
                    (N01/N)*Math.log((N*N01)/((N01+N00)*(N11+N01))) +
                    (N10/N)*Math.log((N*N10)/((N11+N10)*(N00+N10))) +
                    (N00/N)*Math.log((N*N00)/((N00+N01)*(N00+N10)));
            // MutualInfo m = new MutualInfo(term, I);
            if (!Double.isNaN(I)) {
                maxHeapMadison.add(I);
            }
            madisonValues.put(I, term);
        }

        // for testing
        for (int i = 0; i < 10; i++) {
            System.out.println(maxHeapHamilton.poll() + ". term = " + hamiltonValues.get(maxHeapHamilton.poll()));
        }
        System.out.println();
        for (int i = 0; i < 10; i++) {
            System.out.println(maxHeapJay.poll() + ". term = " + jayValues.get(maxHeapJay.poll()));
        }
        System.out.println();
        for (int i = 0; i < 10; i++) {
            System.out.println(maxHeapMadison.poll() + ". term = " + madisonValues.get(maxHeapMadison.poll()));
        }

        System.out.println("++ FINISH PROGRAM");
    }

    // used to index entire directory
    private static void indexDirectory(FederalistIndex index, String folderName) {
        System.out.println("starting " + folderName + " indexing...");
        File f = new File("/home/bardsko/FederalistPapers/" + folderName);
        if (f.exists() && f.isDirectory()) {
            String[] fileList = f.list();
            Arrays.sort(fileList, new FileComparator());    // sorts files before assigning docID
            Parser parser = new Parser();
            int i = 1;
            for (String path : fileList) {
                String[] file = parser.parseRawText(f.getPath() + "/" + path);
                indexFile(file, index, i);
                i++;
                // docIDs are going to be a little off but
                // I dont think we need to know exact docIDs?
            }
            index.setNumberOfDocuments(i - 1);
            System.out.println("number of documents: " + index.getTotalDocuments());
            System.out.println("finished " + folderName + " indexing.\n");
        }
    }

    // used to index individual files
    private static void indexFile(String[] fileData, FederalistIndex index, int docID) {
        try {
            SimpleTokenStream stream = new SimpleTokenStream(fileData[1]);
            while (stream.hasNextToken()) {
                String next = stream.nextToken();
                if (next == null) {
                    continue;
                }

                index.addTerm(next, docID);
                if (stream.getHyphen() != null) {
                    for (String str : stream.getHyphen()) {
                        index.addTerm(str, docID);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
