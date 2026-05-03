import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

public class HtmlTreePrinter {

    public static void exec() throws Exception {
        String url = "https://example.com";

        Document doc = Jsoup.connect(url).get();

        printNode(doc, 0);
    }

    static void printNode(Node node, int depth) {
        // インデント
        System.out.println("  ".repeat(depth) + node.nodeName());

        // 子ノードを再帰処理
        for (Node child : node.childNodes()) {
            printNode(child, depth + 1);
        }
    }
}
