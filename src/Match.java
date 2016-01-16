import com.mongodb.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by remiprevost on 16/12/2015.
 */
public class Match {

    private List<String> tokens = new ArrayList<>();
    private String weight;
    private String method;
    private MongoClient mongoClient;
    private DB db;
    private DBCollection coll_token;
    private boolean ponderation;

    public Match(String query_path, String weight_in, String method_in, boolean ponderation) throws Exception {
        List<String> methods = Arrays.asList("sum","cos");
        List<String> weights = Arrays.asList("tf","tf-idf");
        BufferedReader buff;

        this.ponderation = ponderation;

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

        this.mongoClient = new MongoClient();
        this.db = this.mongoClient.getDB("DBindex");
        this.coll_token = this.db.getCollection("tokens");
    }

    public List<String> execute() {
        List<List<Document>> main_list = new ArrayList<>();
        List<Document> final_docs = new ArrayList<>();
        List<Document> token_docs;
        Integer bonus;
        List<Double> doc_weights;
        Document matched_doc;
        Boolean found;

        /*Document D1 = new Document("DOC1",1d);
        Document D2 = new Document("DOC2",2d);
        Document D3 = new Document("DOC3",3d);
        Document D4 = new Document("DOC2",1d);
        Document D5 = new Document("DOC3",2d);
        Document D6 = new Document("DOC4",3d);
        Document D7 = new Document("DOC3",6d);
        Document D8 = new Document("DOC4",7d);
        Document D9 = new Document("DOC5",2d);
        Document D10 = new Document("DOC6",2d);
        Document D11 = new Document("DOC1",9d);
        List<Document> L1 = new ArrayList<>();
        List<Document> L2 = new ArrayList<>();
        List<Document> L3 = new ArrayList<>();
        L1.add(D1);
        L1.add(D2);
        L1.add(D3);
        L2.add(D4);
        L2.add(D5);
        L2.add(D6);
        L3.add(D7);
        L3.add(D8);
        L3.add(D9);
        L3.add(D10);
        L3.add(D11);

        main_list.add(L1);
        main_list.add(L2);
        main_list.add(L3);*/

        for(String token : this.tokens) {
            main_list.add(find(token));
        }

        for (int index_main = 0; index_main < main_list.size(); index_main++) {
            token_docs = main_list.get(index_main);
            for (int index_docs = 0; index_docs < token_docs.size(); index_docs++) {
                doc_weights = new ArrayList<>();
                doc_weights.add(token_docs.get(index_docs).getWeight());
                bonus = ponderation ? 2 : 1;

                for (int k = 0; k < main_list.size(); k++) {
                    found = false;
                    if (k != index_main) {
                        int index_matched_doc = main_list.get(k).indexOf(token_docs.get(index_docs));
                        if (index_matched_doc != -1) {
                            matched_doc = main_list.get(k).get(index_matched_doc);
                            doc_weights.add(matched_doc.getWeight());
                            found = true;
                            main_list.get(k).remove(index_matched_doc);
                        }

                        if (!found) {
                            doc_weights.add(0.0);
                            bonus=1;
                        }
                    }
                }
                System.out.println(main_list.get(index_main).get(index_docs).getName()+" : "+sum(doc_weights)*bonus);
                if (this.method.equals("sum")) {
                    final_docs.add(new Document(main_list.get(index_main).get(index_docs).getName(),sum(doc_weights)*bonus));
                }
                else {
                    final_docs.add(new Document(main_list.get(index_main).get(index_docs).getName(),cos(doc_weights)*bonus));
                }
            }
        }

        Collections.sort(final_docs);
        Function<Document,String> extract = doc -> doc.getName();
        return final_docs.stream().map(extract).collect(Collectors.toList());

    }

    private List<Document> find(String token) {
        BasicDBObject query = new BasicDBObject(token, new BasicDBObject("$exists",true));
        BasicDBObject projection = new BasicDBObject(token,1);
        DBCursor cursor;
        List<Document> list_doc = new ArrayList<>();

        cursor = this.coll_token.find(query,projection);
        try {
            if (cursor.hasNext()) {
                Map map_docs = ((BasicDBObject)cursor.next().get(token)).toMap();
                Set keys = map_docs.keySet();
                for (Object key : keys) {
                    Map map_values = ((BasicDBObject) map_docs.get(key)).toMap();
                    list_doc.add(new Document(key.toString(),Double.parseDouble(map_values.get(this.weight).toString())));
                }
            }
            else {
                System.out.println("ERROR, token "+ token + "not found in collection");
            }
        }
        finally {
            cursor.close();
        }

        return list_doc;
    }

    private Double sum(List<Double> values) {
        return  values.stream().mapToDouble(Double::doubleValue).sum();
    }

    private Double cos(List<Double> values) {
        Function<Double,Double> square = x -> x * x;
        List<Double> list_square = values.stream().map(square).collect(Collectors.toList());

        return sum(values) / (Math.sqrt(sum(list_square))*Math.sqrt(values.size()));
    }

    public static HashMap<String,Boolean> readQrel(String path) {
        HashMap<String,Boolean> docToRelevance = new HashMap<>();
        BufferedReader buff;
        String[] words;

        try{
            buff = new BufferedReader(new FileReader(path));

            try {
                String line;
                while ((line = buff.readLine()) != null) {
                    words = line.split("\t");
                    docToRelevance.put(words[0].replace(".html",""),words[1].equals("1"));
                }
            } finally {
                buff.close();
            }
        } catch (IOException ioe) {
            System.out.println("Erreur --" + ioe.toString());
        }

        return docToRelevance;
    }

    public static void main(String[] args) throws Exception {
        Match match;
        List<List<String>> queriz_results = new ArrayList<>();
        List<String> query_results;
        List<String> queriz = Arrays.asList("Q1","Q2","Q3","Q4","Q5","Q6","Q7","Q8","Q9");
        String path_queriz = "data/Queriz/";
        String path_qrels = "data/qrels/";
        HashMap<String,Boolean> docToRelevance;
        String query;
        String qrel;
        boolean ponderation = false;

        float relDocsBefore5 = 0;
        float relDocsFrom5to10 = 0;
        float relDocsFrom10to25 = 0;

        float p5, p10, p25;

        if (args.length == 4) {
            queriz = Arrays.asList("Q"+args[3]);
            ponderation = Integer.parseInt(args[2]) == 1;
        }
        else if (args.length == 3) {
            ponderation = Integer.parseInt(args[2]) == 1;
        }
        else if (args.length != 2) {
            throw new Exception("Please provide a query, a weight and a method as input");
        }

        for (int i = 0; i < queriz.size(); i++) {
            match = new Match(path_queriz+queriz.get(i),args[0],args[1],ponderation);
            queriz_results.add(match.execute());
        }

        for (int i = 0; i < queriz.size(); i++) {
            docToRelevance = readQrel(path_qrels+"qrelQ"+(i+1)+".txt");

            if (i < queriz_results.size()) {
                query_results = queriz_results.get(i);
                for (int j=0; j<5; j++)  {
                    if(docToRelevance.get(query_results.get(j)) != null){
                        relDocsBefore5++;
                    }
                }

                for(int j=5; j<10; j++){
                    if(docToRelevance.get(query_results.get(j)) != null){
                        relDocsFrom5to10++;
                    }
                }

                for(int j=10; j<25; j++){
                    if(docToRelevance.get(query_results.get(j)) != null){
                        relDocsFrom10to25++;
                    }
                }
            }
            else {
                break;
            }
        }
        p5=(relDocsBefore5/5)/(float)queriz_results.size();
        p10=(relDocsFrom5to10/5)/(float)queriz_results.size();
        p25=(relDocsFrom10to25/15)/(float)queriz_results.size();

        System.out.println("p5: "+p5);
        System.out.println("p10: "+p10);
        System.out.println("p25: "+p25);
        System.out.println("Ponderation : "+ponderation);
    }
}
