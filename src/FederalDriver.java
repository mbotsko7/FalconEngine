import java.io.*;
import java.util.*;

public class FederalDriver {
    public static void main(String[] args) {
        FederalistIndex hIndex = new FederalistIndex();         // hamilton index
        FederalistIndex jIndex = new FederalistIndex();         // jay index
        FederalistIndex mIndex = new FederalistIndex();         // madison index

        indexDirectory(hIndex, "HAMILTON");     // indexes hamilton papers
        indexDirectory(jIndex, "JAY");         // indexes jay papers
        indexDirectory(mIndex, "MADISON");      // indexes madison papers

        // calculates I(t,c) for terms in hamilton
        // created discriminating set (different values for 'k')

//        PriorityQueue<MutualInfo> maxHeap = new PriorityQueue<>(1, new Comparator<MutualInfo>() {
//            @Override
//            public int compare(MutualInfo a, MutualInfo b) {
//                return Double.compare(a.getValue(), b.getValue());
//            }
//        });

        PriorityQueue<Double> maxHeap = new PriorityQueue<>(1, Collections.reverseOrder());
        HashMap<Double, String> t = new HashMap<>();
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
                maxHeap.add(I);
            }
            t.put(I, term);
        }

        // for testing
        for (int i = 0; i < 50; i++) {
            System.out.println(maxHeap.poll() + ". term = " + t.get(maxHeap.poll()));
        }

        // MI = (N11/N)log(N*N11/N1.*N.1) + (N01/N)log(N*N01/N0.*N.1)
        //    + (N10/N)log(N*N10/N1.*N.0) + (N00/N)log(N*N00/N0.*N.0)
        // N11 = has term and is in class, N11 = has term but not in class
        // N01 = doesnt have term & is in class, N00 = doesnt have term & not in class

        System.out.println("++ FINISH PROGRAM");
    }

    // used to index entire directory
    private static void indexDirectory(FederalistIndex index, String folderName) {
        System.out.println("starting " + folderName + " indexing...");
        File f = new File("FederalistPapers/" + folderName);
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
