import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

public class kNNClassifier {
    private FederalistIndex unclassified;
    private FederalistIndex[] indexArr;
    private ArrayList<String> terms;

    /*
    TO DO:
    vector of document, where each value is a Wdt
     */

    public kNNClassifier(FederalistIndex unclassified, FederalistIndex[] indexArr, ArrayList<String> l){
        this.unclassified = unclassified;
        this.indexArr = indexArr;
        terms = new ArrayList<>();
        for(String s : unclassified.getDictionary())
            terms.add(s);
        for(FederalistIndex f : indexArr){
            for(String s : f.getDictionary())
                terms.add(s);
        }
        Collections.sort(terms);
        String prev = terms.get(0);
        for(int i = 1; i < terms.size(); i++){
            if(terms.get(i).equals(prev)){
                terms.remove(i);
                i--;
            }
            else
                prev = terms.get(i);
        }
//        terms = l;
        process();
    }


    //CONSIDER ALL THE VOCABULARY
    public void process(){
        //for each string in the unclassified document
        double min = Double.MAX_VALUE;
        ArrayList<ArrayList<Double>> hamilton = getVector(indexArr[0]), madison = getVector(indexArr[1]), jay = getVector(indexArr[2]),
            unknown = getVector(unclassified);
//        hamilton.add(getVector(indexArr[0]));
//        madison.add(getVector(indexArr[1]));
//        jay.add(getVector(indexArr[2]));
//        unknown.add(getVector(unclassified));
        int h = 0, m = 0, j2 = 0, a = 0, b = 0, c = 0;
        for(ArrayList<Double> vector : unknown){
            int n = 1;
            double[] ham = getClosest(vector, hamilton,n);
            double[] mad = getClosest(vector, madison, n);
            double[] j = getClosest(vector, jay, n);
            for(Double d : ham) System.out.print(d+" ");
            System.out.println();
            for(Double d : mad) System.out.print(d+" ");
            System.out.println();

//            double ham = getAverageClassDistance(vector, hamilton,n);
//            double mad = getAverageClassDistance(vector, madison, n);
//            double j = getAverageClassDistance(vector, jay, n);
            switch (whodunnit2(ham, mad, j, n)){
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
        /*
        for(ArrayList<Double> vector : unknown){
            double ham = getAverageClassDistance(vector, hamilton, 5);
            double mad = getAverageClassDistance(vector, madison, 5);
            double j =  getAverageClassDistance(vector, jay, 5);
            System.out.println("h "+ham+"\nm "+mad+"\nj "+j);
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
        */
        System.out.println("Hamilton: "+h);
        System.out.println("Jay: "+j2);
        System.out.println("Madison: "+m);
    }



    public String whodunnit2(double[] ham, double[] mad, double[] jay, int n){
        int[] count = new int[3];
        int a = 0, b = 0, c = 0;
        while(n > 0){
            switch (whodunnit(ham[a], mad[b], jay[c])){
                case "Hamilton":
                    a++;
                    count[0]++;
                    break;
                case "Madison":
                    b++;
                    count[1]++;
                    break;
                case "Jay":
                    c++;
                    count[2]++;
                    break;
            }
            n--;
        }
        for(Integer i : count) System.out.print(i+" ");
        System.out.println();
        return whodunnit3(count[0], count[1], count[2]);
    }

    public String whodunnit3(double ham, double mad, double jay){
        if(ham > mad && ham > jay)
            return "Hamilton";
        else if(mad > ham && mad > jay)
            return "Madison";
        else
            return "Jay";
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
    public double[] getClosest(ArrayList<Double> vector, ArrayList<ArrayList<Double>> classVectors, int n){
        PriorityQueue<Double> priorityQueue = new PriorityQueue<>();
        for(ArrayList<Double> list : classVectors){
            priorityQueue.add(distance(vector, list));
        }
        double[] arr = new double[n];
        for(int i = 0; i < n; i++){
            arr[i] = priorityQueue.poll();
        }

        return arr;
    }

    //given a list of classified document vectors, calculate the distance of each
    //return average distance of the closest n
    public double getAverageClassDistance(ArrayList<Double> vector, ArrayList<ArrayList<Double>> classVectors, int n){
        PriorityQueue<Double> priorityQueue = new PriorityQueue<>();
        for(ArrayList<Double> list : classVectors){
            priorityQueue.add(distance(vector, list));
        }
        double avg = 0;
        for(int i = 0; i < n; i++){
            avg += priorityQueue.poll();
        }

        avg /= n;
        return avg;
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
            double len =  length(weights);
            for(int i = 0; i < weights.size(); i++){
                weights.set(i, weights.get(i)/len);
            }
            list.add(weights);
        }
        return list;
    }

    public double length(ArrayList<Double> vector){
        double total = 0;
        for(Double d : vector)
            total += Math.pow(d,2);
        return Math.sqrt(total);
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
