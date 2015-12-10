import com.mongodb.*;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static File[] findHtmls(String folder_name) {
        File folder = new File(folder_name);
        File[] files = folder.listFiles();
        return files;
    }

    public static void main(String[] args) throws IOException, JSONException {
        File[] files = findHtmls("data/CORPUS/");
        File bad_words_file = new File("data/bad_words");
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("DBindex");
        DBCollection coll_token = db.getCollection("tokens");
        DBCollection coll_doc = db.getCollection("documents");

        BasicDBObject query1;
        BasicDBObject query2;
        DBCursor cursor1, cursor2;

        List<String> bad_words = new ArrayList<>();
        List<Elements> balises = new ArrayList<>();
        List<String> inner_elements = new ArrayList<>();
        List<Token> tokens = new ArrayList<>();

        String[] tab_words;
        String file_name;
        String key1 = null;
        String key2;
        String key3;
        String temp = "";
        boolean found;
        int index = 1;
        int word_count_all;
        int doc_count;
        int total_ite;
        int nb_doc=1;

        JSONObject json1;
        JSONObject json2;
        JSONObject json3;
        Iterator<String> iterator1;
        Iterator<String> iterator2;
        Iterator<String> iterator3;

        Scanner scanner = new Scanner(bad_words_file);
        while (scanner.hasNextLine()) {
            bad_words.add(scanner.nextLine());
        }

        for (File file: files) {
            file_name = file.getName().replace(".html","");
            word_count_all = 0;
            org.jsoup.nodes.Document doc = Jsoup.parse(file,"UTF-8","");

            if (file.getName().getBytes()[0] == '.') {
                continue;
            }

            balises.add(doc.getElementsByTag("p"));
            balises.add(doc.getElementsByTag("a"));
            balises.add(doc.getElementsByTag("h1"));
            balises.add(doc.getElementsByTag("h2"));
            balises.add(doc.getElementsByTag("h3"));
            balises.add(doc.getElementsByTag("h4"));
            balises.add(doc.getElementsByTag("h5"));
            balises.add(doc.getElementsByTag("h6"));
            balises.add(doc.getElementsByTag("title"));

            for(Elements elements: balises) {
                for (Element element: elements) {
                    inner_elements.add(element.text());
                }
            }


            for (String several_words: inner_elements) {
                tab_words = several_words.split("[.,;:?!'\\[\\]«»<>= -/[\\xA0][\\x5C][\\u2019][\\u2026][\\x5F]]+");
                for (String word: tab_words) {
                    try {
                        Integer i = Integer.parseInt(word);
                    } catch (NumberFormatException e) {
                        if (word.length() >= 2 && word.length() < 8) {
                            found = false;
                            for (String bad_word: bad_words) {
                                if (bad_word.equals(word)) {
                                    found = true;
                                    break;
                                }
                            }
                            if(!found) {
                                word_count_all++;
                                word = word.toLowerCase();
                                query1 = new BasicDBObject(word, new BasicDBObject("$exists",true));
                                cursor1 = coll_token.find(query1);
                                try {
                                    if (cursor1.hasNext()) {
                                        query1 = new BasicDBObject(word+'.'+file_name,
                                                new BasicDBObject("$exists",true));
                                        cursor2 = coll_token.find(query1);
                                        try {
                                            if (cursor2.hasNext()) {
                                                // increment
                                                query1 = new BasicDBObject(word+'.'+file_name, new BasicDBObject("$exists",true));
                                                query2 = new BasicDBObject("$inc",
                                                        new BasicDBObject(word+"."+file_name+".it",1));
                                                coll_token.update(query1,query2);
                                            }
                                            else {
                                                // insert doc in exiting token
                                                if (word.equals("abonner")) {
                                                    System.out.println("");
                                                }
                                                query1 = new BasicDBObject(word, new BasicDBObject("$exists",true));
                                                query2 = new BasicDBObject("$set",
                                                        new BasicDBObject(word+'.'+file_name,
                                                                new BasicDBObject("it",1)
                                                        )
                                                );
                                                coll_token.update(query1,query2);
                                            }
                                        }finally {
                                            cursor2.close();
                                        }
                                    } else {
                                        query1 = new BasicDBObject(word,
                                                new BasicDBObject(file_name,
                                                        new BasicDBObject("it",1)
                                                )
                                        );
                                        coll_token.insert(query1);
                                    }
                                }finally {
                                    cursor1.close();
                                }
                            }
                        }
                    }
                }
            }
            query1 = new BasicDBObject("name",file_name).append("word_count_all",word_count_all);
            coll_doc.insert(query1);
            index++;

            balises.clear();
            tokens.clear();
            inner_elements.clear();

            System.out.println("doc_num: "+nb_doc);
            nb_doc++;
        }

        cursor1 = coll_token.find();
        try {
            while (cursor1.hasNext()) {
                doc_count = 0;
                total_ite = 0;
                json1 = new JSONObject(cursor1.next().toString());
                iterator1 = json1.keys();
                while(iterator1.hasNext()) {
                    key1 = iterator1.next();
                    if (!key1.equals("_id") && !key1.equals("nb_doc") && !key1.equals("total_ite") && !key1.equals("idf")) {
                        temp = key1;
                        json2 = json1.getJSONObject(key1);
                        iterator2 = json2.keys();
                        while(iterator2.hasNext()) {
                            key2 = iterator2.next();
                            doc_count++;
                            json3 = json2.getJSONObject(key2);
                            iterator3 = json3.keys();
                            while (iterator3.hasNext()) {
                                key3 = iterator3.next();
                                if (key3.equals("it")) {
                                    total_ite+=json3.getInt(key3);
                                    query1 = new BasicDBObject("name",key2);
                                    cursor2 = coll_doc.find(query1);
                                    if (cursor2.hasNext()) {
                                        String doc = cursor2.next().toString();
                                        JSONObject doc_json = new JSONObject(doc);
                                        word_count_all = doc_json.getInt("word_count_all");
                                        System.out.println("doc: "+key2+" word_count: "+word_count_all);
                                        query1 = new BasicDBObject(key1,new BasicDBObject("$exists",true));
                                        query2 = new BasicDBObject("$set",
                                                new BasicDBObject(
                                                        key1+"."+key2+".tf",
                                                        json3.getInt(key3)/(1+ Math.log10(word_count_all))
                                                )
                                        );
                                        coll_token.update(query1,query2);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                if(!temp.equals("")) {
                    System.out.println(temp);
                    query1 = new BasicDBObject(temp, new BasicDBObject("$exists",true));
                    query2 = new BasicDBObject("$set",
                            new BasicDBObject("nb_doc",doc_count));
                    coll_token.update(query1,query2);

                    query1 = new BasicDBObject(temp, new BasicDBObject("$exists",true));
                    query2 = new BasicDBObject("$set",
                            new BasicDBObject("total_ite",total_ite));
                    coll_token.update(query1,query2);

                    query1 = new BasicDBObject(temp, new BasicDBObject("$exists",true));
                    query2 = new BasicDBObject("$set",
                            new BasicDBObject("idf",Math.log10(total_ite/doc_count)));
                    coll_token.update(query1,query2);
                }
            }
        } finally {
            cursor1.close();
        }
    }
}