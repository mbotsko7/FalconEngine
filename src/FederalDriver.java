import java.io.*;
import java.util.*;

public class FederalDriver {
    public static void main(String[] args) {
        FederalistIndex hIndex = new FederalistIndex();         // hamilton index
        FederalistIndex jIndex = new FederalistIndex();         // jay index
        FederalistIndex mIndex = new FederalistIndex();         // madison index
        FederalistIndex uIndex = new FederalistIndex();         //unknown ("disputed") index

        // change 'k' as you see fit. 500 seemed okay?
        int k = 500;
        ArrayList<String> discriminatingSet = new ArrayList<>();

        indexDirectory(uIndex, "DISPUTED");
        indexDirectory(hIndex, "HAMILTON");     // indexes hamilton papers
        indexDirectory(jIndex, "JAY");         // indexes jay papers
        indexDirectory(mIndex, "MADISON");      // indexes madison papers

        PriorityQueue<MIEntry> maxHeap = new PriorityQueue<>(1, Collections.reverseOrder());

        // calculates I(t,c) for terms in hamilton
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

            if (!Double.isNaN(I)) {
                maxHeap.add(new MIEntry(term, I));
            }
        }

        // calculates I(t,c) for terms in jay
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

            if (!Double.isNaN(I)) {
                maxHeap.add(new MIEntry(term, I));
            }
        }

        // calculates I(t,c) for terms in madison
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

            if (!Double.isNaN(I)) {
                maxHeap.add(new MIEntry(term, I));
            }
        }

        // created discriminating set of vocab terms
        int i = 0;
        while (i < k) {
            String s = maxHeap.poll().getKey();
            if (!discriminatingSet.contains(s) && !s.equals("")) {
                discriminatingSet.add(s);
                i++;
            }
        }

//        int j = 1;
//        for (String term: discriminatingSet) {
//            System.out.println(j + ". " + term);
//            j++;
//        }

        // calculate lengths of the text for each class in the training set
        int hLength = getTextLength(hIndex, discriminatingSet);
        int jLength = getTextLength(jIndex, discriminatingSet);
        int mLength = getTextLength(mIndex, discriminatingSet);

        // create hashmap with term as key and list containing
        // ptHamilton, ptJay, ptMadison as value
        Map<String, List<Double>> trainingScores = new HashMap<>(k);
        for (String term:discriminatingSet) {
            List<Double> scores = new ArrayList<>(3);
            scores.add(getTrainingScore(term, hLength, k, hIndex));
            scores.add(getTrainingScore(term, jLength, k, jIndex));
            scores.add(getTrainingScore(term, mLength, k, mIndex));
            trainingScores.put(term, scores);
        }

        // print results of part b
//        for (String term: trainingScores.keySet()) {
//            System.out.println(term);
//            System.out.println(trainingScores.get(term).get(0));
//            System.out.println(trainingScores.get(term).get(1));
//            System.out.println(trainingScores.get(term).get(2));
//            System.out.println();
//        }

        int hSize = hIndex.getTotalDocuments();
        int jSize = jIndex.getTotalDocuments();
        int mSize = mIndex.getTotalDocuments();
        int uSize = uIndex.getTotalDocuments();
        int N = hSize + jSize + mSize;

        // a list of docs that each contain running totals for Hamilton, Jay, and Madison
        List<List<Double>> disputedDocList = new ArrayList<>(uSize);
        // initialize scores with relative frequency of each class (Nc/N)
        for (int n = 0; n < uSize; n++) {
            // list that holds the three scores for each doc
            List<Double> doc = new ArrayList<>(3);
            double relFreq = Math.log(((double) hSize)/N);
            doc.add(relFreq);
            relFreq = Math.log(((double) jSize)/N);
            doc.add(relFreq);
            relFreq = Math.log(((double) mSize)/N);
            doc.add(relFreq);
            disputedDocList.add(doc);
        }

        Map<String, List<Integer>> dIndex = uIndex.getDocIndex();
        for (String term: discriminatingSet) {
            // get list of p(t|c) for term and log them
            List<Double> termScores = trainingScores.get(term);
            for (int a = 0; a < termScores.size(); a++) {
                termScores.set(a, Math.log(termScores.get(a)));
            }

            // get list of docIDs for the term and add scores to their running totals
            List<Integer> docList = dIndex.get(term);
            if (docList == null) continue;
            for (int docID: docList) {
                List<Double> currScores = disputedDocList.get(docID-1);    // bc docIDs start at 1
                for (int x = 0; x < 3; x++) {
                    double updatedScore = currScores.get(x) + termScores.get(x);
                    currScores.set(x, updatedScore);
                }
                disputedDocList.set(docID-1, currScores);
            }
        }

        // calculate argmax for each doc to determine final result
        int count = 0;
        String author;
        for (List<Double> docScores: disputedDocList) {
            double maxScore = Collections.max(docScores);
            if (maxScore == docScores.get(0))
                author = "Hamilton";
            else if (maxScore == docScores.get(1))
                author = "Jay";
            else if (maxScore == docScores.get(2))
                author = "Madison";
            else
                author = "WTF";
            System.out.println(uIndex.getDocTitle(count) + ": " + author + ", max score = " + maxScore);
            count++;
        }


        System.out.println("++ FINISH PROGRAM");
        ArrayList<String> list = new ArrayList<>();
        kNNClassifier classify = new kNNClassifier(uIndex, new FederalistIndex[]{hIndex, mIndex, jIndex}, discriminatingSet);
    }

    // used to index entire directory
    private static void indexDirectory(FederalistIndex index, String folderName) {
        System.out.println("starting " + folderName + " indexing...");
        File f = new File("FederalistPapers/" + folderName);
        HashMap<Integer, String> docTitles = new HashMap<>();
        if (f.exists() && f.isDirectory()) {
            String[] fileList = f.list();
            Arrays.sort(fileList, new FileComparator());    // sorts files before assigning docID
            Parser parser = new Parser();
            int i = 1;
            for (String path : fileList) {
                String[] file = parser.parseRawText(f.getPath() + "/" + path);
                indexFile(file, index, i);
                docTitles.put(i,path);
                i++;
                // docIDs are going to be a little off but
                // I dont think we need to know exact docIDs?
            }
            index.setNumberOfDocuments(i - 1);
            index.setDocIDTable(docTitles);
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

    private static double getTrainingScore(String term, int classTextLength, int vocabSize, FederalistIndex index) {
        int termFreqInClass = index.getTermFrequency(term);
        return (termFreqInClass + 1.0) / (classTextLength + vocabSize);
    }

    private static int getTextLength(FederalistIndex index, ArrayList<String> vocabList) {
        int count = 0;
        for (String term: vocabList) {
            count += index.getTermFrequency(term);
        }
        return count;
    }
}