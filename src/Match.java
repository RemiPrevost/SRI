import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by remiprevost on 16/12/2015.
 */
public class Match {

    private List<String> tokens = new ArrayList<>();
    private String weight;
    private String method;

    public Match(String query_path, String weight_in, String method_in) throws Exception {
        List<String> methods = Arrays.asList("sum","cos");
        List<String> weights = Arrays.asList("tf","tf-idf");
        BufferedReader buff;

        if (methods.contains(method_in)) {
            this.method = method_in;
        }
        else {
            throw new Exception("Unknown method name :" +method_in);
        }

        if (weights.contains(weight_in)) {
            this.weight = weight_in;
        }
        else {
            throw new Exception("Unknown weight name :" +weight_in);
        }

        try{
             buff = new BufferedReader(new FileReader(query_path));

            try {
                String line;
                while ((line = buff.readLine()) != null) {
                    this.tokens.add(line);
                }
            } finally {
                buff.close();
            }
        } catch (IOException ioe) {
            System.out.println("Erreur --" + ioe.toString());
        }

        System.out.print("");
    }

    public static void main(String[] args) throws Exception {
        Match match;
        System.out.println(args[0]);

        if (args.length != 3) {
            throw new Exception("Please provide a query, a weight and a method as input");
        }

        match = new Match(args[0],args[1],args[2]);
    }
}
