import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/*** still working on ***/

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
            mTerms = new RandomAccessFile(new File(path, "KGTerms.bin"), "r");
            mKeyTable = readVocabTable(path);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
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

            while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while we keep reading 4 bytes
                vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
                tableIndex++;
            }
            tableFile.close();
            return vocabTable;
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }
}
