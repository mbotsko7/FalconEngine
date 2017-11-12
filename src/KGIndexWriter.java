import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class KGIndexWriter {
    private String mFolderPath;

    public KGIndexWriter(String folderPath) {
        mFolderPath = folderPath;
    }

    public void buildKGIndex(KGramIndex index) {
        buildKGIndexForDirectory(index, mFolderPath);
    }

    private static void buildKGIndexForDirectory(KGramIndex index, String folder) {
        // "index" contains the in-memory k-gram index
        // now we save the index to disk, building three files: the terms index,
        // the vocabulary list, and the vocabulary table.

        // the array of terms
        String[] dictionary = index.getDictionary();
        // an array of positions in the vocabulary file
        long[] vocabPositions = new long[dictionary.length];

        buildKGVocabFile(folder, dictionary, vocabPositions);
        buildTermsFile(folder, index, dictionary, vocabPositions);
    }

    private static void buildTermsFile(String folder, KGramIndex index,
                                       String[] dictionary, long[] vocabPositions) {
        FileOutputStream termsFile = null;
        try {
            termsFile = new FileOutputStream(
                    new File(folder, "kgTerms.bin")
            );

            // simultaneously build the vocabulary table on disk, mapping a term index to a
            // file location in the postings file.
            FileOutputStream kgVocabTable = new FileOutputStream(
                    new File(folder, "kgVocabTable.bin")
            );

            // the first thing we must write to the vocabTable file is the number of vocab terms.
            byte[] tSize = ByteBuffer.allocate(4)
                    .putInt(dictionary.length).array();
            kgVocabTable.write(tSize, 0, tSize.length);
            int vocabI = 0;
            for (String s : dictionary) {
                // for each String (kgram key) in dictionary, retrieve its terms list.
                List<String> terms = index.getTerms(s);

                // write the vocab table entry for this key: the byte location of the key in the vocab list file,
                // and the byte location of the terms for the key in the terms file.
                byte[] vPositionBytes = ByteBuffer.allocate(8)
                        .putLong(vocabPositions[vocabI]).array();
                kgVocabTable.write(vPositionBytes, 0, vPositionBytes.length);

                byte[] pPositionBytes = ByteBuffer.allocate(8)
                        .putLong(termsFile.getChannel().position()).array();
                kgVocabTable.write(pPositionBytes, 0, pPositionBytes.length);

                // write the terms file for this key. first, the term frequency
                // for the key, then the terms.
                byte[] termFreqBytes = ByteBuffer.allocate(4)
                        .putInt(terms.size()).array();
                termsFile.write(termFreqBytes, 0, termFreqBytes.length);

                for (String term : terms) {
                    byte[] termSize = ByteBuffer.allocate(4)
                            .putInt(term.length()).array();
                    termsFile.write(termSize, 0, termSize.length);

                    byte[] termBytes = term.getBytes();
                    termsFile.write(termBytes, 0, termBytes.length);
                }

                vocabI++;
            }
            kgVocabTable.close();
            termsFile.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            try {
                termsFile.close();
            } catch (IOException ex) {
            }
        }
    }

    private static void buildKGVocabFile(String folder, String[] dictionary,
                                       long[] vocabPositions) {
        OutputStreamWriter kgVocabList = null;
        try {
            // first build the vocabulary list: a file of each key concatenated together.
            // also build an array associating each term with its byte location in this file.
            int vocabI = 0;
            kgVocabList = new OutputStreamWriter(
                    new FileOutputStream(new File(folder, "kgVocab.bin")), "ASCII"
            );

            int vocabPos = 0;
            for (String key : dictionary) {
                // for each String in dictionary, save the byte position where that term will start in the vocab file.
                vocabPositions[vocabI] = vocabPos;
                kgVocabList.write(key); // then write the String
                vocabI++;
                vocabPos += key.length();
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        } finally {
            try {
                kgVocabList.close();
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
    }
}
