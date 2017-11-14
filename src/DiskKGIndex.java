import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*** did not use. switched to serializing ***/

public class DiskKGIndex {
    private String mPath;
    private RandomAccessFile mKeyList;
    private RandomAccessFile mTerms;
    private long[] mKeyTable;

    // Opens a disk inverted index that was constructed in the given path.
    public DiskKGIndex(String path) {
        try {
            mPath = path;
            mKeyList = new RandomAccessFile(new File(path, "kgVocab.bin"), "r");
            mTerms = new RandomAccessFile(new File(path, "kgTerms.bin"), "r");
            mKeyTable = readVocabTable(path);
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    // pulls terms list for a specific kgram
    private static String[] readTermsFromFile(RandomAccessFile terms, long termsPosition) {
        try {
            // seek to the position in the file where the terms start.
            // "seek": sets the file-pointer offset
            terms.seek(termsPosition);

            // read the 4 bytes for the term frequency
            byte[] buffer = new byte[4];
            terms.read(buffer, 0, buffer.length);

            // use ByteBuffer to convert the 4 bytes into an int.
            int termFrequency = ByteBuffer.wrap(buffer).getInt();

            // initialize the array that will hold the terms.
            String[] termsList = new String[termFrequency];

            // reads 4 bytes at a time from file
            // grabs terms for a key in kgram index
            for (int i = 0; i < termFrequency; i++) {
                terms.read(buffer, 0, buffer.length);
                int termLength = ByteBuffer.wrap(buffer).getInt();
                byte[] tBuffer = new byte[termLength];
                terms.read(tBuffer, 0, tBuffer.length);


                String actualTerm = new String(tBuffer, StandardCharsets.UTF_8);

                termsList[i] = actualTerm;
            }

            return termsList;
        }
        catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }

    // Reads and returns a list of disk terms that contain the given kgram
    public ArrayList<String> getTerms(String term) {
        long termsPosition = binarySearchKey(term);
        if (termsPosition >= 0) {
            ArrayList<String> l = new ArrayList<>();
            for(String s : readTermsFromFile(mTerms, termsPosition)){
                l.add(s);
            }
            return l;
        }
        return new ArrayList<>();
    }

    private static DiskPosting binarySearchTerms(DiskPosting[] p, int docID) {
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

    // Locates the byte position of the terms for the given key.
    private long binarySearchKey(String term) {
        // do a binary search over the vocabulary, using the vocabTable and the file vocabList.
        int i = 0, j = mKeyTable.length / 2 - 1;
        while (i <= j) {
            try {
                int m = (i + j) / 2;
                long vListPosition = mKeyTable[m * 2];
                int termLength;
                if (m == mKeyTable.length / 2 - 1) {
                    termLength = (int) (mKeyList.length() - mKeyTable[m * 2]);
                }
                else {
                    termLength = (int) (mKeyTable[(m + 1) * 2] - vListPosition);
                }

                mKeyList.seek(vListPosition);

                byte[] buffer = new byte[termLength];
                mKeyList.read(buffer, 0, termLength);
                String fileTerm = new String(buffer, "ASCII");

                int compareValue = term.compareTo(fileTerm);
                if (compareValue == 0) {
                    // found it!
                    return mKeyTable[m * 2 + 1];
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

    // Reads the file kgVocabTable.bin into memory.
    private static long[] readVocabTable(String indexName) {
        try {
            long[] vocabTable;

            RandomAccessFile tableFile = new RandomAccessFile(
                    new File(indexName, "kgVocabTable.bin"),
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
}
