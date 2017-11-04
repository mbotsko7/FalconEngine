public class RankedRetrieval {

    public RankedRetrieval(){

    }

    public double rank(){
        /*
        calculate Wqt = ln(1+ (number of docs/docs containing the term)
        for each doc in term posting list:
            accumulator shenanigans
            fetch Wdt
            Ad += Wdt X Wqt
        for every non-zero Ad
            Ad /= Ld //Ld in docWeights.bin
        PQ values return top ten
         */
        return 0.0;
    }


}
