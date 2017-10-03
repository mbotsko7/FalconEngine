import java.util.Comparator;

public class FileComparator implements Comparator<String> {
    @Override
    // to be used to sort files by article # (numerically)
    public int compare(String fileName1, String fileName2) {
        int fileNumber1 = Integer.parseInt(fileName1.substring(fileName1.indexOf('e') + 1, fileName1.indexOf('.')));
        int fileNumber2 = Integer.parseInt(fileName2.substring(fileName2.indexOf('e') + 1, fileName2.indexOf('.')));

        return fileNumber1 - fileNumber2;
    }
}
