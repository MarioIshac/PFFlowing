package me.theeninja.pfflowing.card;

import me.theeninja.pfflowing.Side;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CardProcessor {

    private static final Logger logger = Logger.getLogger(CardProcessor.class.getSimpleName());

    private final String filePath;
    private final Document document;

    private static final String LABEL_FOLLOWUP = ":";

    public CardProcessor(String filePath) {
        logger.log(Level.INFO, "New CardProcessor created for file path {0}.", filePath);

        this.filePath = filePath;
        this.document = parseAsDocument();
    }

    private Document parseAsDocument() {
        ContentHandler handler = new ToXMLContentHandler();
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        String xHTML = null;
        try (InputStream stream = new FileInputStream(new File(filePath))) {
            parser.parse(stream, handler, metadata);
            xHTML = handler.toString();
        } catch (IOException | SAXException | TikaException e) {
            e.printStackTrace();
        }
        System.out.println(xHTML);
        return Jsoup.parse(xHTML);
    }

    private <ComponentType> Optional<ComponentType> findCardComponent(CardComponent<ComponentType> cardComponent) {
        Elements cardComponentElements = document.select("p")
                .stream()
                .filter(element -> cardComponent
                        .getAcceptedLabels()
                        .stream().anyMatch(label -> element.text().startsWith(label + LABEL_FOLLOWUP)))
                .collect(Collectors.toCollection(Elements::new));

        if (cardComponentElements.size() > 1) {
            System.out.println("Only looking at first instance of card component declaration");
        }

        Element cardComponentElement = cardComponentElements.first();
        String cardComponentContent = cardComponentElement.html().split(LABEL_FOLLOWUP)[1];
        return Optional.ofNullable(cardComponent.getFunction().apply(cardComponentContent));
    }

    public Card generateCard() {
        Optional<Author> author = findCardComponent(CardComponent.AUTHOR);
        Optional<String> url = findCardComponent(CardComponent.SOURCE);
        Optional<Content> content = findCardComponent(CardComponent.CONTENT);
        Optional<Calendar> calendar = findCardComponent(CardComponent.DATE);

        if (author.isPresent() &&
                url.isPresent() &&
                content.isPresent() &&
                calendar.isPresent()) {
            return new Card(
                    author.get(),
                    url.get(),
                    calendar.get(),
                    content.get(),
                    Side.AFFIRMATIVE);
        }
        return null;
    }
}