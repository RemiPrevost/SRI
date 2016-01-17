package Reformulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by remiprevost on 16/01/2016.
 */
public class Reformulation {
    private List<String> tokens_in = new ArrayList<>();
    private List<String> tokens_synonym = new ArrayList<>();
    private List<String> tokens_link = new ArrayList<>();
    private SparqlClient sparqlClient = new SparqlClient("localhost:3030/space");
    private String folder_path = "data/original_queriz/";
    private String file_name;

    public Reformulation(String file_name) throws IOException{
        BufferedReader buff;
        buff =  new BufferedReader(new FileReader(this.folder_path +file_name));
        this.file_name = file_name;
        try {
            String line;
            while ((line = buff.readLine()) != null) {
                this.tokens_in.add(line);
            }
        } finally {
            buff.close();
        }
    }

    public void expandTokens() {
        for (String token : this.tokens_in) {
            synonymMethod(token);
        }
        linkMethod(tokens_in);
    }

    public void writeQuery() {

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
        Reformulation reformulation = new Reformulation("Q1");
        List<String> list =  new ArrayList<>();
        list.add("Omar Sy");
        list.add("Trappes");
        reformulation.linkMethod(list);
        //reformulation.expandTokens();
        reformulation.writeQuery();
    }
}
