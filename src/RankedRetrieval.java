import javax.print.Doc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class RankedRetrieval {
    protected ArrayList<String> query;
    private DiskInvertedIndex diskInvertedIndex;
    private ArrayList<DocumentWeight> documentWeights;
    private ArrayList<DocWeight> accumulator;

    public RankedRetrieval(ArrayList<String> q, DiskInvertedIndex index, ArrayList<DocumentWeight> docWeights){
        query = q;
        diskInvertedIndex = index;
        documentWeights = docWeights;
    }



    //same word multiple times in a query?
    public DocWeight[] rank(){
        DocWeight[] ret = new DocWeight[10];
        final double DOC_COUNT = 36803.0;
        //for each term in query
        PriorityQueue<DocWeight> pq = new PriorityQueue<>();
        for(String token : query){
            DiskPosting[] postings = diskInvertedIndex.getPostings(token);
            //calculate Wqt = ln(1+ (number of doc in collection/docs containing the term)
            double weightQueryTerm = Math.log(1+DOC_COUNT/postings.length);

            //for each doc in term posting list
            for(DiskPosting diskPosting : postings){
                //access accumulator the value
                DocWeight acc = accumulator.get(diskPosting.getDocID()-1);
                //get Wdt
                double weightDocumentTerm = 1.0 + Math.log(diskPosting.getTermFrequency());
                acc.setDocWeight(weightDocumentTerm * weightQueryTerm);
                accumulator.set(diskPosting.getDocID()-1, acc);
            }
        }
        //for every non-zero Ad
        for(DocWeight dw : accumulator){
            if(dw.getDocWeight() > 0){
                dw.setDocWeight(dw.getDocWeight() / 0.0); // the Ld of docweights.bin
                pq.add(dw);
            }
        }
        //return top ten
        for(int i = 0; i < 10; i++){
            ret[i] = pq.poll();
        }

        return ret;
    }





}

/*
TERM -> ArrayList {DOCUMENT, WEIGHT}
 */
