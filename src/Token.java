/**
 * Created by remiprevost on 04/12/2015.
 */
public class Token {
    private String name;
    private float tf ;
    private float idf;
    private float tf_idf;

    public Token(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTf(float tf) {
        this.tf = tf;
    }

    public float getTf_idf() {
        return tf_idf;
    }

    public void setTf_idf(float tf_idf) {
        this.tf_idf = tf_idf;
    }

    public float getIdf() {
        return idf;
    }

    public void setIdf(float idf) {
        this.idf = idf;
    }

    public float getTf() {
        return tf;
    }

    @Override
    public String toString() {
        return "Token{" +
                "name='" + name + '\'' +
                //", tf=" + tf +
                //", idf=" + idf +
                //", tf_idf=" + tf_idf +
                '}';
    }
}
