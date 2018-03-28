package me.theeninja.pfflowing.gui.cardparser;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.FlowApp;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowingregions.Blocks;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;
import org.apache.tika.io.IOUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CardParserController implements SingleViewController<BorderPane>, Initializable {
    private final FlowApp flowApp;
    private final Consumer<List<Card>> onQuit;

    @FXML public TreeView<Card> parsedCardsColumn;
    @FXML public TreeItem<Card> parsedCardsRoot;
    @FXML public TextField cardNameRequest;

    @FXML public BorderPane cardParserArea;
    @FXML public WebView documentDisplay;
    @FXML public ProgressBar progressBar;
    @FXML public VBox rightContainer;

    public CardParserController(FlowApp flowApp, Consumer<List<Card>> onQuit) {
        this.flowApp = flowApp;
        this.onQuit = onQuit;
    }

    private String snapSelectionToWordJS;
    private String selectHTMLJS;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InputStream first = getClass().getResourceAsStream("snap_selection_to_word.js");
        InputStream second = getClass().getResourceAsStream("select_html.js");
        try {
            setSnapSelectionToWordJS(IOUtils.toString(first, StandardCharsets.UTF_8.name()));
            setSelectHTMLJS(IOUtils.toString(second, StandardCharsets.UTF_8.name()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        parsedCardsColumn.setShowRoot(false);

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

        rightContainer.prefWidthProperty().bind(cardParserArea.widthProperty().subtract(parsedCardsColumn.widthProperty()));

        cardNameRequest.setVisible(false);
    }

    public void loadHTML(String string) {
        documentDisplay.getEngine().loadContent(string);
    }

    public FlowApp getFlowApp() {
        return flowApp;
    }

    public Consumer<List<Card>> getOnQuit() {
        return onQuit;
    }

    public String getSelectHTMLJS() {
        return selectHTMLJS;
    }

    public void setSelectHTMLJS(String selectHTMLJS) {
        this.selectHTMLJS = selectHTMLJS;
    }

    private class CardTreeItem extends TreeItem<Card> {
        private static final String RENAME = "Rename";
        private static final String RESELECT = "Reselect";

        CardTreeItem(Card card) {
            super(card);
        }

        public void onRename() {

        }

        public void onReselect() {

        }
    }

    private final static KeyCodeCombination QUIT = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);

    private final static KeyCodeCombination PARSE_CARD_PERFORMER = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    private Map<KeyCodeCombination, Runnable> PARSER_ACTIONS = Map.of(
        PARSE_CARD_PERFORMER, this::onCardParsed,
        QUIT, this::onAttemptFinish
    );

    public Path promptFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Parse");
        return fileChooser.showOpenDialog(getFlowApp().getStage()).toPath();
    }

    public List<Card> getParsedCards() {
        return parsedCardsRoot.getChildren().stream()
                .filter(CardTreeItem.class::isInstance)
                .map(CardTreeItem.class::cast)
                .map(CardTreeItem::getValue)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BorderPane getCorrelatingView() {
        return cardParserArea;
    }

    public String getSnapSelectionToWordJS() {
        return snapSelectionToWordJS;
    }

    public void setSnapSelectionToWordJS(String snapSelectionToWordJS) {
        this.snapSelectionToWordJS = snapSelectionToWordJS;
    }

    private void onCardParsed() {
        String cardContent = getSelectedHTML();
        cardNameRequest.setOnAction(actionEvent -> {
            Card card = new Card(cardNameRequest.getText(), cardContent);
            cardNameRequest.setText(Utils.ZERO_LENGTH_STRING);

            clearSelection();

            CardTreeItem cardTreeItem = new CardTreeItem(card);
            parsedCardsRoot.getChildren().add(cardTreeItem);

            cardNameRequest.setVisible(false);
            documentDisplay.requestFocus();
        });

        cardNameRequest.setVisible(true);
        cardNameRequest.requestFocus();
    }

    private boolean inQuit = false;

    private void onAttemptFinish() {
        cardNameRequest.setText(Utils.ZERO_LENGTH_STRING);
        cardNameRequest.setVisible(true);
        cardNameRequest.requestFocus();;

        cardNameRequest.setOnAction(actionEvent -> {
            try {
                Blocks blocks = new Blocks();
                blocks.setName(cardNameRequest.getText());

                Path cardsPath = EFlow.getInstance().getFullAppPath().resolve(EFlow.BLOCKS_DIRECTORY);
                String withExtension = Utils.addExtension(blocks.getName(), "json");
                Path filePath = Paths.get(withExtension);
                Path fullPath = cardsPath.resolve(filePath);

                blocks.setCards(getParsedCards());

                if (Files.exists(fullPath)) // name already in use
                    return;

                Files.createFile(fullPath);

                String json = EFlow.getInstance().getGSON().toJson(blocks, Blocks.class);
                Files.write(fullPath, json.getBytes());

                if (!inQuit) {
                    getOnQuit().accept(getParsedCards());
                    inQuit = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

    private String getSelectedText() {
        return (String) documentDisplay.getEngine().executeScript("window.getSelection().toString()");
    }

    private String getSelectedHTML() {
        String html = (String) documentDisplay.getEngine().executeScript(getSelectHTMLJS());
        return html;
    }

    private void clearSelection() {
        documentDisplay.getEngine().executeScript("window.getSelection().removeAllRanges()");
    }
}
