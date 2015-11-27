import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.text.Document;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        File html = new File("data/CORPUS/D1.html");
        org.jsoup.nodes.Document doc = Jsoup.parse(html,"UTF-8","");
        List<Elements> balises = new ArrayList<>();
        balises.add(doc.getElementsByTag("p"));
        balises.add(doc.getElementsByTag("a"));
        balises.add(doc.getElementsByTag("h1"));
        balises.add(doc.getElementsByTag("h2"));
        balises.add(doc.getElementsByTag("h3"));
        balises.add(doc.getElementsByTag("h4"));
        balises.add(doc.getElementsByTag("h5"));
        balises.add(doc.getElementsByTag("h6"));
        balises.add(doc.getElementsByTag("title"));

        File bad_words_file = new File("data/bad_words");
        List<String> bad_words = new ArrayList<>();
        Scanner scanner = new Scanner(bad_words_file);
        while (scanner.hasNextLine()) {
            bad_words.add(scanner.nextLine());
        }

        //String[] list = chaine.split("[.,;:?!' -/]+|");
        List<String> inner_elements = new ArrayList<>();
        for(Elements elements: balises) {
            for (Element element: elements) {
                inner_elements.add(element.text());
            }
        }

        List<String> tokens = new ArrayList<>();
        String[] tab_words;
        boolean found;
        for (String several_words: inner_elements) {
            tab_words = several_words.split("[.,;:?!'«» -/[\\xA0]]+");
            for (String word: tab_words) {
                if (word.length() > 2 && word.length() < 8) {
                    found = false;
                    for (String bad_word: bad_words) {
                        if (bad_word.equals(word)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        tokens.add(word);
                    }
                }
            }
        }
    }
}
