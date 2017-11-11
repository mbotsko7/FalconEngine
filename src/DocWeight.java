public class DocWeight implements Comparable {
    private int docID;
    private double docWeight;
    public DocWeight(int id, double weight){
        docID = id;
        docWeight = weight;
    }

    public double getDocWeight() {
        return docWeight;
    }

    public int getDocID() {
        return docID;
    }

    public void setDocWeight(double docWeight) {
        this.docWeight = docWeight;
    }

    public int compareTo(Object o){
        DocWeight obj = (DocWeight) o;
        Double x = new Double(docWeight);
        return x.compareTo(obj.docWeight);
    }
}