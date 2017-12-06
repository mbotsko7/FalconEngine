import java.util.ArrayList;
import java.util.PriorityQueue;

public class kNNClassifier {
    private FederalistIndex unclassified;
    private FederalistIndex[] indexArr;
    private String[] terms;

    /*
    TO DO:
    vector of document, where each value is a Wdt
     */

    public kNNClassifier(FederalistIndex unclassified, FederalistIndex[] indexArr){
        this.unclassified = unclassified;
        this.indexArr = indexArr;
        terms = unclassified.getDictionary();
    }

    public void preprocess(){
        //for each string in the unclassified document
        double min = Double.MAX_VALUE;
        ArrayList<ArrayList<Double>> classWeightList = new ArrayList<>();
        for(FederalistIndex index : indexArr){
            //for every document I have
            ArrayList<Double> weightList = new ArrayList<>();
            for(String s : terms){
                //calculate the Wdts
//                DiskPosting[] diskPosting = index.g(s);
//                ArrayList<Double> termWeights = new ArrayList<>();
//                for(DiskPosting post : diskPosting){
//                    double weight = 1.0 + Math.log(post.getTermFrequency());
//                    termWeights.add(weight);
//                }
                double weight = 1.0+Math.log(index.getTermFrequency(s));

                weightList.add(weight);
            }
            classWeightList.add(weightList);
        }
        //NEED INDIVIDUAL DOCS
    }



    //given a list of classified document vectors, calculate the distance of each
    //return average distance of the closest n
    public double getAverageClassDistance(double[] vector, ArrayList<ArrayList<Double>> classVectors, int n){
        PriorityQueue<Double> priorityQueue = new PriorityQueue<>();
        for(ArrayList<Double> list : classVectors){
            priorityQueue.add(distance(vector, list));
        }
        double avg = 0;
        for(int i = 0; i < 10; i++){
            avg += priorityQueue.poll();
        }

        avg /= n;
        return avg;
    }

    //calculate euclidean distance
    public double distance(double[] vector1, ArrayList<Double> vector2){
        double total = 0;
        for(int i = 0; i < vector1.length; i++){
            total += Math.pow(vector1[i]-vector2.get(i), 2);

        }
        return Math.sqrt(total);
    }
}
