
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiskInvertedIndex {

    private String mPath;
    private RandomAccessFile mVocabList;
    private RandomAccessFile mPostings;
    private long[] mVocabTable;
    private double[] mWeights;

    // Opens a disk inverted index that was constructed in the given path.
    public DiskInvertedIndex(String path) {
        try {
            mPath = path;
            mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
            mPostings = new RandomAccessFile(new File(path, "postings.bin"), "r");
            mVocabTable = readVocabTable(path);
            mWeights = readDocWeights(path);
            //mFileNames = readFileNames(path);       ??
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    public double[] getWeights() {
        return mWeights;
    }

    private static DiskPosting[] readPostingsFromFile(RandomAccessFile postings,
                                                      long postingsPosition, boolean withPositions) {
        try {
            // seek to the position in the file where the postings start.
            // "seek": sets the file-pointer offset
            postings.seek(postingsPosition);

            // read the 4 bytes for the document frequency
            byte[] buffer = new byte[4];
            postings.read(buffer, 0, buffer.length);

            // use ByteBuffer to convert the 4 bytes into an int.
            int documentFrequency = ByteBuffer.wrap(buffer).getInt();

            // initialize the array that will hold the postings.
            DiskPosting[] dPostings = new DiskPosting[documentFrequency];

            // reads 4 bytes at a time from file
            // grabs DocIDs for a vocabulary term
            int previousDocId = 0;
            for (int i = 0; i < documentFrequency; i++) {
                postings.read(buffer, 0, buffer.length);
                int docId = ByteBuffer.wrap(buffer).getInt() + previousDocId;
                previousDocId = docId;

                ArrayList<Integer> pList = new ArrayList<>();
                postings.read(buffer, 0, buffer.length);
                int positionFrequency = ByteBuffer.wrap(buffer).getInt();
                // if withPositions set to true, get positions
                if (withPositions) {
                    int previousPosition = 0;

                    //postings.read(buffer, 0, buffer.length);
                    for (int j = 0; j < positionFrequency; j++) {
                        postings.read(buffer, 0, buffer.length);
                        int position = ByteBuffer.wrap(buffer).getInt() + previousPosition;
                        pList.add(position);

                        previousPosition = position;
                    }

                }
                else {
                    postings.read(buffer, 0, buffer.length);
                    for (int j = 0; j < positionFrequency; j++) {
                        postings.read(buffer, 0, buffer.length);        // skips through all positions
                    }
                    pList = null;
                }
                dPostings[i] = new DiskPosting(docId, positionFrequency, pList);
            }

            return dPostings;
        }
        catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    // Reads and returns a list of disk postings that contain the given term.
    public DiskPosting[] getPostings(String term) {
        long postingsPosition = binarySearchVocabulary(term);
        if (postingsPosition >= 0) {
            return readPostingsFromFile(mPostings, postingsPosition, false);
        }
        return new DiskPosting[0];
    }

    public DiskPosting[] getPostingsWithPositions(String term) {
        long postingsPosition = binarySearchVocabulary(term);
        if (postingsPosition >= 0) {
            return readPostingsFromFile(mPostings, postingsPosition, true);
        }
        return new DiskPosting[0];
    }

    public List<Integer> getPositionsInDoc(String term, int docID) {
        DiskPosting[] postings = getPostingsWithPositions(term);
        DiskPosting posting = binarySearchPostings(postings, docID);
        if (posting != null) {
            return posting.getPositions();
        }
        return new ArrayList<>();
    }

    private static DiskPosting binarySearchPostings(DiskPosting[] p, int docID) {
        int i = 0, j = p.length -1;
        while(j >= i) {
            int m = (i + j) / 2;
            if (p[m].getDocID() == docID) {
                return p[m];
            }
            if (p[m].getDocID() < docID) {
                i = m + 1;
            }
            if (p[m].getDocID() > docID) {
                j = m - 1;
            }
        }
        return null;
    }

    // Locates the byte position of the postings for the given term.
    private long binarySearchVocabulary(String term) {
        // do a binary search over the vocabulary, using the vocabTable and the file vocabList.
        int i = 0, j = mVocabTable.length / 2 - 1;
        while (i <= j) {
            try {
                int m = (i + j) / 2;
                long vListPosition = mVocabTable[m * 2];
                int termLength;
                if (m == mVocabTable.length / 2 - 1) {
                    termLength = (int) (mVocabList.length() - mVocabTable[m * 2]);
                }
                else {
                    termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
                }

                mVocabList.seek(vListPosition);

                byte[] buffer = new byte[termLength];
                mVocabList.read(buffer, 0, termLength);
                String fileTerm = new String(buffer, "ASCII");

                int compareValue = term.compareTo(fileTerm);
                if (compareValue == 0) {
                    // found it!
                    return mVocabTable[m * 2 + 1];
                }
                else if (compareValue < 0) {
                    j = m - 1;
                }
                else {
                    i = m + 1;
                }
            }
            catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return -1;
    }

    private static double[] readDocWeights(String indexName){
        try {
            double[] docWeights;

            RandomAccessFile tableFile = new RandomAccessFile(
                    new File(indexName, "docWeights.bin"),
                    "r");

            byte[] byteBuffer = new byte[4];
            int tableIndex = 0;
            docWeights = new double[36803];
            byteBuffer = new byte[8];

            while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) {
                // while we keep reading 4 bytes
                docWeights[tableIndex] = ByteBuffer.wrap(byteBuffer).getDouble();
                tableIndex++;
            }
            tableFile.close();
            return docWeights;
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
        catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    // Reads the file vocabTable.bin into memory.
    private static long[] readVocabTable(String indexName) {
        try {
            long[] vocabTable;

            RandomAccessFile tableFile = new RandomAccessFile(
                    new File(indexName, "vocabTable.bin"),
                    "r");

            byte[] byteBuffer = new byte[4];
            tableFile.read(byteBuffer, 0, byteBuffer.length);

            int tableIndex = 0;
            vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];
            byteBuffer = new byte[8];

            while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) {
                // while we keep reading 4 bytes
                vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
                tableIndex++;
            }
            tableFile.close();
            return vocabTable;
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
        catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    public int getTermCount() {
        return mVocabTable.length / 2;
    }
}