import java.util.Map;

public class MIEntry implements Map.Entry<String, Double>, Comparable<MIEntry> {
    private final String key;
    private Double value;

    public MIEntry(String term, Double i) {
        this.key = term;
        this.value = i;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public Double setValue(Double v) {
        Double old = this.value;
        this.value = v;
        return old;
    }

    public int compareTo(MIEntry x) {
        if (x.getValue() < value) {
            return 1;
        } else if (x.getValue() > value) {
            return -1;
        } else {
            return 0;
        }
    }
}
