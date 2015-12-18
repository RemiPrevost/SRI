import javax.lang.model.util.Elements;

/**
 * Created by remiprevost on 17/12/2015.
 */
public class Document implements Comparable{
    private String name;
    private Double weight;

    public Document(String name, Double weight) {
        this.weight = weight;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object obj) {
        return ((Document)obj).getName().equals(this.name);
    }

    @Override
    public int compareTo(Object o) {
        if (this.getWeight() > ((Document)o).getWeight()) {
            return -1;
        }
        else if (this.getWeight() < ((Document)o).getWeight()) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
