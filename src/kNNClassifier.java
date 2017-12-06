import java.util.ArrayList;
import java.util.HashMap;
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
        ArrayList<ArrayList<Double>> hamilton = new ArrayList<>(), madison = new ArrayList<>(), jay = new ArrayList<>(),
            unknown = new ArrayList<>();
        hamilton.add(getVector(indexArr[0]));
        madison.add(getVector(indexArr[1]));
        jay.add(getVector(indexArr[2]));
        unknown.add(getVector(unclassified));

        for(ArrayList<Double> vector : unknown){
            getAverageClassDistance(vector, hamilton, 5);
        }
        //NEED INDIVIDUAL DOCS
    }

    public ArrayList<Double> getVector(FederalistIndex federalistIndex){
        ArrayList<DocumentWeight> documentWeights = federalistIndex.getDocWeights();
        ArrayList<Double> weights = new ArrayList<>();
        for(DocumentWeight doc : documentWeights){
            HashMap<String, Integer> map = doc.getMap();
            for(String s : terms) {
                if (map.containsKey(s)){
                    weights.add( 1 + Math.log(map.get(s)));
                }
                else
                    weights.add(1.0);
            }
        }
        return weights;
    }

    //given a list of classified document vectors, calculate the distance of each
    //return average distance of the closest n
    public double getAverageClassDistance(ArrayList<Double> vector, ArrayList<ArrayList<Double>> classVectors, int n){
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
    public double distance(ArrayList<Double> vector1, ArrayList<Double> vector2){
        double total = 0;
        for(int i = 0; i < vector1.size(); i++){
            total += Math.pow(vector1.get(i)-vector2.get(i), 2);

        }
        return Math.sqrt(total);
    }
}
