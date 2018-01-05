package me.theeninja.pfflowing.card;

import me.theeninja.pfflowing.speech.HTMLStyleTag;
import me.theeninja.pfflowing.flowingregions.CharacterFormatting;
import me.theeninja.pfflowing.flowingregions.CharacterStyle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Comparator;
import java.util.stream.Collectors;

public class CardContent {
    private final String html;

    public CardContent(String html) {
        this.html = html;
    }

    public String getHTML() {
        return html;
    }

    public String getContent(CharacterFormatting characterFormatting) {
        Document document = Jsoup.parseBodyFragment(getHTML());

        System.out.println(generateCSSQuery(characterFormatting));

        Elements elements = document.select((generateCSSQuery(characterFormatting)));
        return String.join(" ", elements
                .stream()
                .map(Element::text)
                .collect(Collectors.toList()));
    }

    private static String generateCSSQuery(CharacterFormatting characterFormatting) {
        return String.join(" > ", characterFormatting
                .getCharacterStyles()
                .stream()
                .sorted(Comparator.comparingInt(CharacterStyle::getPriority))
                .map(CharacterStyle::getRepresentingTag)
                .map(HTMLStyleTag::getTagContent)
                .collect(Collectors.toList()));
    }
}