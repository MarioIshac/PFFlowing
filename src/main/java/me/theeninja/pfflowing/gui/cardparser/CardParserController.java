package me.theeninja.pfflowing.gui.cardparser;

import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.card.CardContent;
import me.theeninja.pfflowing.configuration.GlobalConfiguration;
import me.theeninja.pfflowing.flowingregions.Author;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CardParserController implements SingleViewController<BorderPane>, Initializable {

    public TextParser getTextParser() {
        return textParser;
    }

    public void setTextParser(TextParser textParser) {
        this.textParser = textParser;
    }

    private class TextParser {
        private final Elements lines;
        private int startIndex;
        private int endIndex;

        TextParser(String string) {
            Document document = new Document(string);
            this.lines = document.select("p").stream().filter(element ->
                !element.html().isEmpty()
            ).collect(Collectors.toCollection(Elements::new));
        }

        public Elements getLines() {
            return lines;
        }

        public boolean canAppend() {
            return endIndex < getLines().size() - 1;
        }

        public boolean canTruncate() {
            return startIndex < endIndex;
        }

        public void append() {
            endIndex++;
        }

        public void offset(int length) {
            startIndex += length;
            endIndex += length;
        }

        public void truncate() {
            endIndex--;
        }

        private String getText(Function<Element, String> way) {
            return IntStream.of(startIndex, endIndex)
                    .mapToObj(getLines()::get).map(way)
                    .reduce((first, second) -> first + "\n" + second).get();
        }

        public String getFormatted() {
            return getText(Element::outerHtml);
        }

        public String getUnformatted() {
            return getText(Element::text);
        }
    }

    @FXML public HBox parsedCardsDispaly;
    @FXML public VBox authorRequest;
    @FXML public VBox sourceRequest;
    @FXML public VBox dateRequest;
    @FXML public WebView contentSelection;
    @FXML public BorderPane cardParserArea;
    @FXML public VBox dateOptions;
    @FXML public VBox sourceOptions;
    @FXML public VBox authorOptions;
    @FXML public HBox cardOptions;
    @FXML public VBox documentSectionDisplay;
    @FXML public ProgressBar progressBar;

    private HBox selectedRequest;

    private ObservableSet<Card> parsedCards;

    private List<HBox> requests;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void generateOptionIndexLabel(int index) {
        Label label = new Label(String.valueOf(index));
        label.setBackground(Utils.generateBackgroundOfColor(Color.LIGHTGRAY));
        label.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    private void generateOptionContentLabel(String string) {

    }

    private void setOptions(VBox optionBox, List<String> viableOptions) {
        for (int index = 0; index < viableOptions.size(); index++) {
            Label label = new Label(String.valueOf(index));
            HBox viableOptionContainer = new HBox();
        }
    }

    private TextParser textParser;

    private void startParseProcess(String string) {
        setTextParser(new TextParser(string));
    }

    private void resetRequests() {

    }

    private Label generateLabel(String string) {
        return new Label(string);
    }

    private TextField generateTextField(Consumer<String> postActionConsumer) {
        TextField textField = new TextField();
        textField.setOnAction(event -> {
            postActionConsumer.accept(textField.getText());
            if (Utils.isLastElement(getRequests(), getSelectedRequest())) {
                getParsedCards().add(parsedCards);
                resetRequests();
            }
            else
                setSelectedRequest(Utils.getRelativeElement(getRequests(), getSelectedRequest(), 1));
        });
        return textField;
    }

    private HBox generateInputRequest(String string, Consumer<String> postActionConsumer) {
        HBox inputHBox = new HBox();
        inputHBox.getChildren().addAll(generateLabel(string), generateTextField(postActionConsumer));
        return inputHBox;
    }

    public HBox getSelectedRequest() {
        return selectedRequest;
    }

    public void setSelectedRequest(HBox selectedRequest) {
        this.selectedRequest = selectedRequest;
    }

    public ObservableSet<Card> getParsedCards() {
        return parsedCards;
    }

    public void setParsedCards(ObservableSet<Card> parsedCards) {
        this.parsedCards = parsedCards;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BorderPane getCorrelatingView() {
        return cardParserArea;
    }

    public void finish() {

    }

    public void skip() {

    }

    public void appendToSelected() {
        if (getTextParser().canAppend())
            getTextParser().append();
    }

    public void truncateSelected() {
        if (getTextParser().canTruncate())
            getTextParser().truncate();
    }

    public List<HBox> getRequests() {
        return requests;
    }

    public void setRequests(List<HBox> requests) {
        this.requests = requests;
    }
}
