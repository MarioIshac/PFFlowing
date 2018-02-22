package me.theeninja.pfflowing.gui.cardparser;

import javafx.beans.binding.Bindings;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.utils.Utils;
import org.apache.tika.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CardParserController implements SingleViewController<BorderPane>, Initializable {

    @FXML public VBox parsedCardsDisplay;
    private Stage associatedStage;

    private ParsedCardsDisplayController parsedCardsDisplayController;

    @FXML public BorderPane cardParserArea;
    @FXML public WebView documentDisplay;
    @FXML public ProgressBar progressBar;
    @FXML public VBox rightContainer;

    private String snapSelectionToWordJS;

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

        documentDisplay.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
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

        setParsedCardsDisplayController(Utils.getCorrelatingController("/card_parser_gui/parsed_cards_display.fxml"));
        getCorrelatingView().setTop(getParsedCardsDisplayController().getCorrelatingView());
        ParseCardsTask task = new ParseCardsTask();
        task.setDocumentDisplay(documentDisplay);
        task.setOnTextFieldPrompt(getCorrelatingView()::setLeft);

        Bindings.bindContent(getParsedCardsDisplayController().getParsedCards(), task.getParsedCards());

        task.call();

        task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, workerStateEvent -> {
            System.out.println(task.getParsedCards());
        });
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
        String cardSelectionCSSLocation = getClass().getResource("/card_parser_gui/card_selection.css").toExternalForm();
        documentDisplay.getEngine().setUserStyleSheetLocation(cardSelectionCSSLocation);
    }

    private void useContentSelectionStyling() {
        String contentSelectionCSSLocation = getClass().getResource("/card_parser_gui/content_selection.css").toExternalForm();
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
        return fileChooser.showOpenDialog(associatedStage).toPath();
    }

    public static final KeyCodeCombination LOAD_FILE = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination PARSE_SELECTED_TEXT = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_NEXT_LINE = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination UNSELECT_PREVIOUS_LINE = new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_NEXT_CHARACTER = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination UNSELECT_PREVIOUS_CHARACTER = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN);

    /**
     * {@inheritDoc}
     */
    @Override
    public BorderPane getCorrelatingView() {
        return cardParserArea;
    }

    public ParsedCardsDisplayController getParsedCardsDisplayController() {
        return parsedCardsDisplayController;
    }

    public void setParsedCardsDisplayController(ParsedCardsDisplayController parsedCardsDisplayController) {
        this.parsedCardsDisplayController = parsedCardsDisplayController;
    }

    public void setAssociatedStage(Stage associatedStage) {
        this.associatedStage = associatedStage;
    }

    public String getSnapSelectionToWordJS() {
        return snapSelectionToWordJS;
    }

    public void setSnapSelectionToWordJS(String snapSelectionToWordJS) {
        this.snapSelectionToWordJS = snapSelectionToWordJS;
    }
}
