import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

public class HtmlTreePrinter {

    public static void exec() throws Exception {
        String url = "https://tenki.jp/forecast/1/2/1400/1100/10days.html";

        Document doc = Jsoup.connect(url).get();
        printNode(doc, 0);
    }

    static void printNode(Node node, int depth) {
        String indent = "  ".repeat(depth);

        if (node instanceof Element) {
            Element el = (Element) node;

            String id = el.id();
            if (!id.isEmpty()) {
                System.out.println(indent + el.tagName() + " #" + id);
            } else {
                System.out.println(indent + el.tagName());
            }
        } else {
            // textや#documentなど
            System.out.println(indent + node.nodeName());
        }

        for (Node child : node.childNodes()) {
            printNode(child, depth + 1);
        }
    }
}
