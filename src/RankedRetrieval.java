import javax.print.Doc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class RankedRetrieval {
    protected ArrayList<String> query;
    private DiskInvertedIndex diskInvertedIndex;
    private ArrayList<DocWeight> accumulator;
    private final double DOC_COUNT = 36803.0;

    public RankedRetrieval(ArrayList<String> q, DiskInvertedIndex index){
        query = q;
        diskInvertedIndex = index;
        accumulator = new ArrayList<DocWeight>();
        for(int i = 0; i < DOC_COUNT; i++){
            accumulator.add(new DocWeight(i+1));
        }
    }



    //retunrs the top then scoring documents for a particular query
    public DocWeight[] rank(){
        DocWeight[] ret = new DocWeight[10];

        //for each term in query
        PriorityQueue<DocWeight> pq = new PriorityQueue<>();
        for(String token : query){
            DiskPosting[] postings = diskInvertedIndex.getPostingsWithPositions(token);
            //calculate Wqt = ln(1+ (number of doc in collection/docs containing the term)

            double weightQueryTerm = Math.log(1+DOC_COUNT/postings.length);
            //for each doc in term posting list
            for(DiskPosting diskPosting : postings){
                //access accumulator the value
                DocWeight acc = accumulator.get(diskPosting.getDocID()-1);
                //get Wdt
                double weightDocumentTerm = 1.0 + Math.log(diskPosting.getTermFrequency());
                acc.setDocWeight(acc.getDocWeight() + weightDocumentTerm * weightQueryTerm);
                accumulator.set(diskPosting.getDocID()-1, acc);
            }
        }
        //for every non-zero Ad
        double[] weightsFromDisk = diskInvertedIndex.getWeights();
        for(DocWeight dw : accumulator){
            if(dw != null && dw.getDocWeight() > 0){
                dw.setDocWeight(dw.getDocWeight() / weightsFromDisk[dw.getDocID()-1]);
                // the Ld of docweights.bin
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

