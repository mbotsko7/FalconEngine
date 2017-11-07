import java.util.ArrayList;
import java.util.HashMap;

public class RankedRetrieval {
    protected ArrayList<String> query;
    private DiskInvertedIndex diskInvertedIndex;
    private ArrayList<DocumentWeight> documentWeights;

    public RankedRetrieval(ArrayList<String> q, DiskInvertedIndex index, ArrayList<DocumentWeight> docWeights){
        query = q;
        diskInvertedIndex = index;
        documentWeights = docWeights;
    }

    public double rank(){
        //calculate Wqt = ln(1+ (number of docs/docs containing the term)
        //for each doc in term posting list
        for(String token : query){
            DiskPosting[] postings = diskInvertedIndex.getPostings(token);
            for(DiskPosting diskPosting : postings){
                //accumlate the value
                //fetch Wdt
                //Ad += Wdt X Wqt
            }
            //for every non-zero Ad
                //Ad /= Ld of docWeights.bin
                //PQ add Ad
            //return top ten
        }
        return 0.0;
    }


}
