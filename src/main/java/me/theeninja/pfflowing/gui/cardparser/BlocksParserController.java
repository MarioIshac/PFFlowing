package me.theeninja.pfflowing.gui.cardparser;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowingregions.Blocks;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.gui.KeyCodeCombinationUtils;
import me.theeninja.pfflowing.utils.Utils;
import org.apache.tika.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.w3c.dom.html.HTMLAnchorElement;

public class BlocksParserController implements SingleViewController<HBox>, Initializable {
    private class DocumentListener implements ChangeListener<Worker.State>, org.w3c.dom.events.EventListener {
        @Override
        public void handleEvent(Event evt) {
            EventTarget target = evt.getCurrentTarget();
            HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
            String href = anchorElement.getHref();
            //handle opening URL outside JavaFX WebView
            System.out.println(href);
            evt.preventDefault();
        }

        @Override
        public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
            if (newValue == Worker.State.SUCCEEDED) {
                org.w3c.dom.Document document = documentDisplay.getEngine().getDocument();
                NodeList anchors = document.getElementsByTagName("a");
                for (int i = 0; i < anchors.getLength(); i++) {
                    Node node = anchors.item(i);
                    EventTarget eventTarget = (EventTarget) node;
                    eventTarget.addEventListener("click", this, false);
                }
            }
        }
    }

    private final Blocks blocks;
    private final Runnable cleanUp;

    /**
     * Represents what action to take based on the given host.
     */
    private final Map<Host, Supplier<FileFetcher<?>>> MEDIUM_HTML_CONSUMER_BLOCKS = Map.of(
        Host.GOOGLE_DRIVE, GoogleDriveFetcher::new,
        Host.FILE_SYSTEM, OfflineFileFetcher::new,
        Host.ONE_DRIVE, OneDriveFetcher::new
    );

    @FXML public TreeView<Card> parsedCardsColumn;
    @FXML public TreeItem<Card> parsedCardsRoot;
    @FXML public TextField cardNameRequest;

    @FXML public HBox cardParserArea;
    @FXML public WebView documentDisplay;
    @FXML public ProgressBar progressBar;
    @FXML public VBox rightContainer;

    public BlocksParserController(Blocks blocks, Runnable cleanUp) {
        this.blocks = blocks;
        this.cleanUp = cleanUp;
    }

    private String snapSelectionToWordJS;
    private String selectHTMLJS;

    @FXML
    public Button fileChooser;

    private static final String[] RESPONSE_HEADER = {"A2", "F2", "I2"};

    private static boolean unwrapOnce(Elements headers) {
        boolean[] haveUniqueParents = new boolean[headers.size()];
        Arrays.fill(haveUniqueParents, true);

        for (int headerIndex = 0; headerIndex < headers.size(); headerIndex++) {
            Element header = headers.get(headerIndex);
            Element parentHeader = header.parent();

            for (int otherHeaderIndex = 0; otherHeaderIndex < headers.size(); otherHeaderIndex++) {
                if (headerIndex == otherHeaderIndex) {
                    continue;
                }

                Element otherHeader = headers.get(otherHeaderIndex);
                Element otherParentHeader = otherHeader.parent();

                if (parentHeader == otherParentHeader) {
                    haveUniqueParents[headerIndex] = false;
                    haveUniqueParents[otherHeaderIndex] = false;
                }
            }
        }

        for (int headerIndex = 0; headerIndex < headers.size(); headerIndex++) {
            boolean hasUniqueParent = haveUniqueParents[headerIndex];

            if (hasUniqueParent) {
                Element header = headers.get(headerIndex);
                Element headerParent = header.parent();
                headers.set(headerIndex, headerParent);
            }
        }

        for (int index = 0; index < haveUniqueParents.length; index++) {
            boolean hasUniqueParent = haveUniqueParents[index];

            if (!hasUniqueParent) {
                return false;
            }
        }

        return true;
    }

    private static void unwrap(Elements headers) {
        boolean unwrappingFinished;

        do {
            unwrappingFinished = unwrapOnce(headers);
        }
        while (!unwrappingFinished);
    }

    private void attemptAutomaticParse(final String html) {
        final Document document = Jsoup.parse(html);

        final Elements headers = document
                .select("*:containsOwn(" + RESPONSE_HEADER[0] + ")")
                .stream()
                .collect(Collectors.toCollection(Elements::new));

        unwrap(headers);

        System.out.println("Printing headers' outer htmls");
        headers.forEach(header -> System.out.println(header.outerHtml()));

        for (int headerIndex = 0; headerIndex < headers.size() - 1; headerIndex++) {
            final int nextHeaderIndex = headerIndex + 1;

            final Element header = headers.get(headerIndex);
            final Element nextHeader = headers.get(nextHeaderIndex);

            final Elements childrenAtLevel = header.parent().children(); // firstHeader.siblings

            int startIndex = childrenAtLevel.indexOf(header);
            int endIndex = childrenAtLevel.lastIndexOf(nextHeader);

            /*
            Indicates that the headers are not one the same level, i.e they probably have different purposes
            for example one could be a chapter heading while another is simply a p that contains RESPONSE_HEADER[0]
            by coincidence
            */
            if (startIndex == -1 || endIndex == -1) {
                continue;
            }

            final Elements associatedContent = childrenAtLevel.subList(startIndex, endIndex).stream().collect(Collectors.toCollection(Elements::new));
            final Elements groupedContent = new Elements(header, nextHeader);

            /*
            Indicates that headers have no content between them (tags in between the headers that contain no content
            still cause a result of no content between the headers based on this method).
             */
            if (associatedContent.outerHtml().equals(groupedContent.outerHtml())) {
                System.out.println("Tabled of contents detected");
                continue;
            }

            final String cardRepresentation = nextHeader.text();

            final Card card = new Card(cardRepresentation, associatedContent.outerHtml());

            final TreeItem<Card> treeItem = new TreeItem<>(card);
            parsedCardsRoot.getChildren().add(treeItem);
        }
    }

    private final StringProperty loadedHTML = new SimpleStringProperty();

    @FXML
    public void onAutomaticParseRequest(ActionEvent actionEvent) {
        attemptAutomaticParse(getLoadedHTML());
    }

    @FXML
    public void onFileChooserClick(ActionEvent actionEvent) {
        String hostRep = parserOptionChooser.getValue();
        Host host = Host.getHost(hostRep);

        FileFetcher<?> fileFetcher = MEDIUM_HTML_CONSUMER_BLOCKS.get(host).get();

        try {
            fileFetcher.feedFetchedHTML(this::loadHTML);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public ComboBox<String> parserOptionChooser;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getParsedCards().addAll(blocks.getCards());

        parserOptionChooser.setMinWidth(Region.USE_PREF_SIZE);

        for (Host host : Host.values()) {
            parserOptionChooser.getItems().add(host.getRepresentation());
        }

        parserOptionChooser.getSelectionModel().selectFirst();

        fileChooser.prefWidthProperty().bind(rightContainer.widthProperty());

        InputStream snapSelectionToWordIS = getClass().getResourceAsStream("snap_selection_to_word.js");
        InputStream selectHTMLIS = getClass().getResourceAsStream("select_html.js");

        try {
            this.snapSelectionToWordJS = IOUtils.toString(snapSelectionToWordIS);
            this.selectHTMLJS = IOUtils.toString(selectHTMLIS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        parsedCardsColumn.setShowRoot(false);

        documentDisplay.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseSelectorReleased);

        cardNameRequest.setVisible(false);

        parsedCardsColumn.addEventHandler(KeyEvent.KEY_PRESSED, this::onRemoveParsedCard);

        loadedHTMLProperty().addListener((observable, oldValue, newValue) -> {
           documentDisplay.getEngine().loadContent(newValue);
        });

        final org.w3c.dom.events.EventListener eventListener = new DocumentListener();

        //documentDisplay.getEngine().getLoadWorker().stateProperty().addListener(eventListener);
    }

    public String getLoadedHTML() {
        return loadedHTML.get();
    }

    public StringProperty loadedHTMLProperty() {
        return loadedHTML;
    }

    public void loadHTML(String string) {
        loadedHTMLProperty().set(string);
    }

    public String getSelectHTMLJS() {
        return selectHTMLJS;
    }

    private void onMouseSelectorReleased(MouseEvent mouseEvent) {
        documentDisplay.getEngine().executeScript(getSnapSelectionToWordJS());
    }

    private void onRemoveParsedCard(KeyEvent keyEvent) {
        if (KeyCodeCombinationUtils.DELETE.match(keyEvent)) {
            TreeItem<Card> selectedTreeItem = parsedCardsColumn.getSelectionModel().getSelectedItem();

            parsedCardsRoot.getChildren().remove(selectedTreeItem);
        }
    }

    private final static KeyCodeCombination QUIT = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
    private final static KeyCodeCombination PARSE_CARD_PERFORMER = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    private Map<KeyCodeCombination, Runnable> PARSER_ACTIONS = Map.of(
        PARSE_CARD_PERFORMER, this::onCardParsed,
        QUIT, this::onAttemptFinish
    );

    public List<Card> getParsedCards() {
        return parsedCardsRoot.getChildren().stream()
                .map(TreeItem::getValue)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HBox getCorrelatingView() {
        return cardParserArea;
    }

    public String getSnapSelectionToWordJS() {
        return snapSelectionToWordJS;
    }

    private void onCardParsed() {
        String cardContent = getSelectedHTML();

        cardNameRequest.setOnAction(actionEvent -> {
            Card card = new Card(cardNameRequest.getText(), cardContent);
            cardNameRequest.setText(Utils.ZERO_LENGTH_STRING);

            clearSelection();

            TreeItem<Card> selectedTreeItem = parsedCardsColumn.getSelectionModel().getSelectedItem();

            // Means that user wishes to parse new card
            if (selectedTreeItem == null) {
                TreeItem<Card> cardTreeItem = new TreeItem<>(card);
                parsedCardsRoot.getChildren().add(cardTreeItem);
            }

            // Means that user wishes to reparse existing card
            else {
                selectedTreeItem.setValue(card);
            }

            cardNameRequest.setVisible(false);
            documentDisplay.requestFocus();
        });

        cardNameRequest.setVisible(true);
        cardNameRequest.requestFocus();
    }

    private void onAttemptFinish() {
        Path cardsPath = EFlow.getInstance().getFullAppPath().resolve(EFlow.BLOCKS_DIRECTORY);
        String withExtension = Utils.addExtension(blocks.getName(), "json");
        Path filePath = Paths.get(withExtension);
        Path fullPath = cardsPath.resolve(filePath);

        blocks.getCards().addAll(getParsedCards());
        getParsedCards().forEach(card -> card.setSide(blocks.getSide()));

        if (Files.exists(fullPath)) // name already in use
            return;

        try {
            Files.createFile(fullPath);

            String json = EFlow.getInstance().getGSON().toJson(blocks, Blocks.class);
            Files.write(fullPath, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        cleanUp.run();
    }

    public void startProcess() {
        cardParserArea.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            PARSER_ACTIONS.forEach((keyCodeCombination, runnable) -> {
                if (keyCodeCombination.match(keyEvent))
                    runnable.run();
                keyEvent.consume();
            });
        });
    }

    private String getSelectedHTML() {
        return (String) documentDisplay.getEngine().executeScript(getSelectHTMLJS());
    }

    private void clearSelection() {
        documentDisplay.getEngine().executeScript("window.getSelection().removeAllRanges()");
    }
}