package Reformulation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by remiprevost on 16/01/2016.
 */
public class Reformulation {
    private List<String> tokens_in = new ArrayList<>();
    private List<String> tokens_synonym = new ArrayList<>();
    private List<String> tokens_link = new ArrayList<>();
    private SparqlClient sparqlClient = new SparqlClient("localhost:3030/space");
    private String file_name;

    public Reformulation(String file_name) throws IOException{
        BufferedReader buff;
        String folder_path = "data/original_queriz/";
        buff =  new BufferedReader(new FileReader(folder_path +file_name));
        this.file_name = file_name;
        try {
            String line;
            while ((line = buff.readLine()) != null) {
                this.tokens_in.add(line);
            }
        } finally {
            buff.close();
        }

        System.out.println(tokens_in);
    }

    public void expandTokens() {
        for (String token : this.tokens_in) {
            synonymMethod(token);
        }
        linkMethod(tokens_in);
    }

    public void writeQuery() {
        String folder_new_path = "data/expanded_queriz/";
        List<String> to_write = new ArrayList<>();
        List<String> tokens_synonym_splitted = new ArrayList<>();
        List<String> tokens_link_splitted = new ArrayList<>();
        Function<String,String> weight1 = x -> x+",1";
        Function<String,String> truncate = x -> x.length() >= 8 ? x.substring(0,8) : x;

        for (String several_words : tokens_in) {
            to_write.addAll(split(several_words.toLowerCase()));
        }
        to_write = to_write.stream().map(truncate).collect(Collectors.toList());
        to_write = to_write.stream().map(weight1).collect(Collectors.toList());

        for (String several_words : tokens_synonym) {
            tokens_synonym_splitted.addAll(split(several_words.toLowerCase()));
        }
        tokens_synonym = tokens_synonym_splitted.stream().map(truncate).collect(Collectors.toList());

        for (String several_words : tokens_synonym) {
            tokens_synonym_splitted.addAll(split(several_words.toLowerCase()));
        }
        tokens_link = tokens_synonym_splitted.stream().map(truncate).collect(Collectors.toList());

        for (String token_synonym : tokens_synonym) {
            if (!contains(to_write,token_synonym)) {
                to_write.add(token_synonym+",0.5");
            }
        }

        for (String token_link : tokens_link) {
            if (!contains(to_write,token_link)) {
                to_write.add(token_link+",0.5");
            }
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(folder_new_path+file_name, "UTF-8");
            for(String token : to_write) {
                writer.println(token);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private boolean contains(List<String> to_write, String token) {
        Pattern p;

        for (String token_to_write : to_write) {
            p = Pattern.compile(token+",(1|0.5)");
            if (p.matcher(token_to_write).matches()) {
                return true;
            }
        }

        return false;
    }

    private List<String> split(String several_words) {
        return Arrays.asList(several_words.split("[.,;:?!'\\[\\]«»<>= -/[\\xA0][\\x5C][\\u2019][\\u2026][\\x5F]]+"));
    }

    private void synonymMethod(String token) {
        String query = "PREFIX : <http://ontologies.alwaysdata.net/space#>\n" +
                "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl:  <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                "\n" +
                "SELECT ?synonym\n" +
                "WHERE {\n" +
                "    ?token rdfs:label \""+token+"\"@fr.\n" +
                "    ?token rdfs:label ?synonym.\n" +
                "}\n" +
                "LIMIT 200";
        Iterable<Map<String, String>> results = sparqlClient.select(query);
        for (Map<String, String> result : results) {
            this.tokens_synonym.add(result.get("synonym"));
        }
    }

    public void linkMethod(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (i <tokens.size()-1) {
                this.tokens_link.addAll(getObjLinkedTo(tokens.get(i),tokens.get(i+1)));
                this.tokens_link.addAll(getObjLinkedTo(tokens.get(i+1),tokens.get(i)));
            }
        }
    }

    private List<String> getObjLinkedTo(String t1, String t2) {
        List<String> list_label = new ArrayList<>();
        String query = "PREFIX : <http://ontologies.alwaysdata.net/space#>\n" +
                "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl:  <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                "\n" +
                "SELECT ?label\n" +
                "WHERE {\n" +
                "    ?suj rdfs:label \""+t1+"\".\n" +
                "    ?prop rdfs:label \""+t2+"\"@fr.\n" +
                "    ?obj rdfs:label ?label.\n" +
                "    ?suj ?prop ?obj." +
                "}\n" +
                "LIMIT 200";
        Iterable<Map<String, String>> results = sparqlClient.select(query);
        for (Map<String, String> result : results) {
            list_label.add(result.get("label"));
        }

        query = "PREFIX : <http://ontologies.alwaysdata.net/space#>\n" +
                "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl:  <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                "\n" +
                "SELECT ?label\n" +
                "WHERE {\n" +
                "    ?suj rdfs:label \""+t1+"\".\n" +
                "    ?obj rdfs:label \""+t2+"\"@fr.\n" +
                "    ?prop rdfs:label ?label.\n" +
                "    ?suj ?prop ?obj." +
                "}\n" +
                "LIMIT 200";
        System.out.println();
        results = sparqlClient.select(query);
        for (Map<String, String> result : results) {
            list_label.add(result.get("label"));
        }

        query = "PREFIX : <http://ontologies.alwaysdata.net/space#>\n" +
                "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX owl:  <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                "\n" +
                "SELECT ?label\n" +
                "WHERE {\n" +
                "    ?prop rdfs:label \""+t1+"\"@fr.\n" +
                "    ?obj rdfs:label \""+t2+"\"@fr.\n" +
                "    ?suj rdfs:label ?label.\n" +
                "    ?suj ?prop ?obj." +
                "}\n" +
                "LIMIT 200";
        results = sparqlClient.select(query);
        for (Map<String, String> result : results) {
            list_label.add(result.get("label"));
        }
        return list_label;
    }

    public static void main(String[] args) throws Exception {
        Reformulation reformulation = new Reformulation("Q9");
        reformulation.expandTokens();
        reformulation.writeQuery();
    }
}
