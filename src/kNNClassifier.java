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
        process();
    }

    public void process(){
        //for each string in the unclassified document
        double min = Double.MAX_VALUE;
        ArrayList<ArrayList<Double>> hamilton = getVector(indexArr[0]), madison = getVector(indexArr[1]), jay = getVector(indexArr[2]),
            unknown = getVector(unclassified);
//        hamilton.add(getVector(indexArr[0]));
//        madison.add(getVector(indexArr[1]));
//        jay.add(getVector(indexArr[2]));
//        unknown.add(getVector(unclassified));
        int h = 0, m = 0, j2 = 0;
        for(ArrayList<Double> vector : unknown){
            double ham = getAverageClassDistance(vector, hamilton, 5);
            double mad = getAverageClassDistance(vector, madison, 5);
            double j =  getAverageClassDistance(vector, jay, 5);
            switch (whodunnit(ham, mad, j)){
                case "Hamilton":
                    h++;
                    break;
                case "Madison":
                    m++;
                    break;
                case "Jay":
                    j2++;
                    break;
            }
        }
        System.out.println("Hamilton: "+h);
        System.out.println("Jay: "+j2);
        System.out.println("Madison"+m);
    }

    public ArrayList<ArrayList<Double>> getVector(FederalistIndex federalistIndex){
        ArrayList<DocumentWeight> documentWeights = federalistIndex.getDocWeights();
        ArrayList<ArrayList<Double>> list = new ArrayList<>();
        for(DocumentWeight doc : documentWeights){
            ArrayList<Double> weights = new ArrayList<>();
            HashMap<String, Integer> map = doc.getMap();
            for(String s : terms) {
                if (map.containsKey(s)){
                    weights.add( 1 + Math.log(map.get(s)));
                }
                else
                    weights.add(1.0);
            }
            list.add(weights);
        }
        return list;
    }

    public String whodunnit(double ham, double mad, double jay){
        if(ham < mad && ham < jay)
            return "Hamilton";
        else if(mad < ham && mad < jay)
            return "Madison";
        else
            return "Jay";
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
