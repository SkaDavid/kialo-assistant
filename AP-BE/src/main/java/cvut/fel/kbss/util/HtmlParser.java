package cvut.fel.kbss.util;

import cvut.fel.kbss.model.TextSegment;
import cvut.fel.kbss.model.TextSegmentType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParser {
    public List<TextSegment> parseHtmlToSegments(String htmlContent) {
        List<TextSegment> segments = new ArrayList<>();
        Document document = Jsoup.parse(htmlContent);
        Element body = document.body();
        for(Node node : body.childNodes()){
            if (node instanceof TextNode) {
                String content = ((TextNode) node).getWholeText();
                if (!content.isBlank()) {
                    segments.add(new TextSegment(TextSegmentType.TEXT, content, null, null));
                }
            } else if (node instanceof Element element) {
                if (element.tagName().equals("span") && element.hasAttr("resource")) {
                    segments.add(new TextSegment(
                            TextSegmentType.TERM,
                            element.text(),
                            null,
                            element.attr("resource")
                    ));
                } else {
                    segments.add(new TextSegment(TextSegmentType.TEXT, element.text(), null, null));
                }
            }
        }
        return segments;
    }
}
