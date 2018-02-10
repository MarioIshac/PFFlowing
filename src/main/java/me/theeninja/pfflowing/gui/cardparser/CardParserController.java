package me.theeninja.pfflowing.gui.cardparser;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import javafx.collections.ObservableSet;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.card.CardContent;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;
import org.apache.tika.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class CardParserController implements SingleViewController<BorderPane>, Initializable {

    private Stage associatedStage;

    private String cardSelectionCSS;
    private String contentSelectionCSS;

    private StanfordCoreNLP pipeline;
    private ParsedCardsDisplayController pcdc;

    @FXML public BorderPane cardParserArea;
    @FXML public HBox cardOptions;
    @FXML public WebView documentDisplay;
    @FXML public ProgressBar progressBar;
    @FXML public VBox rightContainer;

    private HBox selectedRequest;

    private ObservableSet<Card> parsedCards;

    private List<HBox> requests;

    private String snapSelectionToWordJS;

    // https://stackoverflow.com/questions/13765349/multi-term-named-entities-in-stanford-named-entity-recognizer

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InputStream inputStream = getClass().getResourceAsStream("snap_selection_to_word.js");
        try {
            setSnapSelectionToWordJS(IOUtils.toString(inputStream, StandardCharsets.UTF_8.name()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(getSnapSelectionToWordJS());
        documentDisplay.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            System.out.println("Mouse has been released");
            documentDisplay.getEngine().executeScript(getSnapSelectionToWordJS());
        });

        progressBar.prefWidthProperty().bind(rightContainer.widthProperty());

        // Allows keyboard shortcuts to be used in order to navigate the document/fill the input boxes
        getCorrelatingView().addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            for (Map.Entry<KeyCodeCombination, Runnable> entry : PARSER_ACTIONS.entrySet())
                if (entry.getKey().match(keyEvent))
                    entry.getValue().run();
        });

        // Essential that the user is able to reach as much of the to-be-parsed document as possible
        VBox.setVgrow(documentDisplay, Priority.ALWAYS);

        // Splits border pane width equally between left and right components (no center is used)
        rightContainer.prefWidthProperty().bind(getCorrelatingView().widthProperty().divide(2));

        PipelineItitializerTask pipelineItitializerTask = new PipelineItitializerTask();
        pipelineItitializerTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            setPipeline(pipelineItitializerTask.getValue());
            System.out.println("Done");
            addRequestParseListener();
        });

        new Thread(pipelineItitializerTask).start();

        setPcdc(Utils.getCorrelatingController("/parsed_cards_display.fxml"));
        getCorrelatingView().setTop(getPcdc().getCorrelatingView());
    }

    public void loadPath(Path path) {
        try {
            String string = new String(Files.readAllBytes(path));
            this.documentDisplay.getEngine().loadContent(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void useCardSelectionStyling() {
        String cardSelectionCSSLocation = getClass().getResource("/card_parser/card_selection.css").toExternalForm();
        documentDisplay.getEngine().setUserStyleSheetLocation(cardSelectionCSSLocation);
    }

    private void useContentSelectionStyling() {
        String contentSelectionCSSLocation = getClass().getResource("/card_parser/content_selection.css").toExternalForm();
        documentDisplay.getEngine().setUserStyleSheetLocation(contentSelectionCSSLocation);
    }

    private void generateOptionIndexLabel(int index) {
        Label label = new Label(String.valueOf(index));
        label.setBackground(Utils.generateBackgroundOfColor(Color.LIGHTGRAY));
        label.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    private Map<KeyCodeCombination, Runnable> PARSER_ACTIONS = Map.of(

    );

    public Path askForFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Parse");
        return fileChooser.showOpenDialog(getAssociatedStage()).toPath();
    }

    private void setOnKeySubmission(Node node, EventHandler<KeyEvent> eventHandler) {
        node.setOnKeyPressed(keyEvent -> {
            if (PARSE_SELECTED_TEXT.match(keyEvent))
                eventHandler.handle(keyEvent);
        });
    }

    private void clearSelection() {
        documentDisplay.getEngine().executeScript("window.getSelection().removeAllRanges()");
    }

    private void addRequestParseListener() {
        clearSelection();
        useCardSelectionStyling();
        setOnKeySubmission(documentDisplay, cardParseKeyEvent -> {
            Card toBeParsedCard = new Card();

            CardPossibilitiesPopulatorTask cppt = new CardPossibilitiesPopulatorTask(getPipeline(), getSelectedText());
            cppt.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
                System.out.println("possiblity populator task done");

                CardPossibilities cardPossibilities = cppt.getValue();
                System.out.println(cardPossibilities.toString());

                // use the stanford api + selected text here
                CardPossibilityPrompterController cppc = Utils.getCorrelatingController("/card_possibility_prompter.fxml");

                cppc.setCardPossibilities(cardPossibilities);
                getCorrelatingView().setLeft(cppc.getCorrelatingView());

                cppc.getCorrelatingView().maxWidthProperty().bind(getCorrelatingView().widthProperty().divide(2));

                cppc.setManagedCard(toBeParsedCard);
                cppc.initialize(cardPossibilities);

                cppc.addChoosePaneChildren();

                cppc.setOnParse(card -> {
                    documentDisplay.requestFocus();

                    // Clear selection so the user can reselect the card content
                    clearSelection();

                    useContentSelectionStyling();

                    setOnKeySubmission(documentDisplay, contentParseKeyEvent -> {
                        toBeParsedCard.setCardContent(new CardContent(getSelectedText()));

                        getPcdc().addDisplayOfParsedCard(toBeParsedCard);

                        addRequestParseListener();
                    });
                });

                cppc.getChoosePanes().get(0).requestFocus();
            });

            new Thread(cppt).start();

            Label label = new Label();
            label.textProperty().bind(cppt.messageProperty());
            getCorrelatingView().setBottom(label);
            progressBar.progressProperty().bind(cppt.progressProperty());
        });
    }

    public static final KeyCodeCombination LOAD_FILE = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination PARSE_SELECTED_TEXT = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_NEXT_LINE = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination UNSELECT_PREVIOUS_LINE = new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_NEXT_CHARACTER = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination UNSELECT_PREVIOUS_CHARACTER = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN);

    private String getSelectedText() {
        return (String) documentDisplay.getEngine().executeScript("window.getSelection().toString()");
    }

    private void generateOptionContentLabel(String string) {

    }

    private void setOptions(VBox optionBox, List<String> viableOptions) {
        for (int index = 0; index < viableOptions.size(); index++) {
            Label label = new Label(String.valueOf(index));
            HBox viableOptionContainer = new HBox();
        }
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
                // getParsedCards().add(parsedCards);
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

    public List<HBox> getRequests() {
        return requests;
    }

    public void setRequests(List<HBox> requests) {
        this.requests = requests;
    }

    public Stage getAssociatedStage() {
        return associatedStage;
    }

    public void setAssociatedStage(Stage associatedStage) {
        this.associatedStage = associatedStage;
    }

    public StanfordCoreNLP getPipeline() {
        return pipeline;
    }

    public void setPipeline(StanfordCoreNLP pipeline) {
        this.pipeline = pipeline;
    }

    public ParsedCardsDisplayController getPcdc() {
        return pcdc;
    }

    public void setPcdc(ParsedCardsDisplayController pcdc) {
        this.pcdc = pcdc;
    }

    public String getSnapSelectionToWordJS() {
        return snapSelectionToWordJS;
    }

    public void setSnapSelectionToWordJS(String snapSelectionToWordJS) {
        this.snapSelectionToWordJS = snapSelectionToWordJS;
    }
}
