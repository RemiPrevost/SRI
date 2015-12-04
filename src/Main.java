import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.text.Document;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static File[] findHtmls(String folder_name) {
        File folder = new File(folder_name);
        File[] files = folder.listFiles();
        return files;
    }

    public static void main(String[] args) throws IOException {
        File[] files = findHtmls("data/CORPUS/");
        File bad_words_file = new File("data/bad_words");
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("DBindex");
        DBCollection coll = db.getCollection("tokens");

        BasicDBObject doc_test = new BasicDBObject("name", "test")
                .append("surname","coucou");
        coll.insert(doc_test);

        List<String> bad_words = new ArrayList<>();
        List<Elements> balises = new ArrayList<>();
        List<String> inner_elements = new ArrayList<>();
        List<Token> tokens = new ArrayList<>();

        String[] tab_words;
        String json;
        boolean found;
        Integer i;
        int index = 1;

        Gson gson = new Gson();

        Scanner scanner = new Scanner(bad_words_file);
        while (scanner.hasNextLine()) {
            bad_words.add(scanner.nextLine());
        }

        for (File file: files) {
            org.jsoup.nodes.Document doc = Jsoup.parse(file,"UTF-8","");

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
                tab_words = several_words.split("[.,;:?!\\'\\[\\]«»<>= -/[\\xA0][\\x5C]]+");
                for (String word: tab_words) {
                    try {
                        i = Integer.parseInt(word);
                    } catch (NumberFormatException e) {
                        if (word.length() > 2 && word.length() < 8) {
                            found = false;
                            for (String bad_word: bad_words) {
                                if (bad_word.equals(word)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                tokens.add(new Token(word));
                            }
                        }
                    }
                }
            }
            /*json = gson.toJson(tokens);
            System.out.println(file.getName()+" "+json);*/
            System.out.println(file.getName());
            for (int j = 0; j < tokens.size(); j++) {
                System.out.println("   "+tokens.toString());
            }
            index++;

            balises.clear();
            tokens.clear();
            inner_elements.clear();
        }
    }
}