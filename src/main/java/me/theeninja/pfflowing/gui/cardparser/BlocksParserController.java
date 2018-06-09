package me.theeninja.pfflowing.gui.cardparser;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.FlowApp;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.drive.GDriveFilePickerController;
import me.theeninja.pfflowing.drive.google.GDriveConnector;
import me.theeninja.pfflowing.flowingregions.Blocks;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.flowingregions.CardProcessor;
import me.theeninja.pfflowing.gui.NavigatorController;
import me.theeninja.pfflowing.utils.Utils;
import org.apache.tika.io.IOUtils;

import javax.security.auth.callback.Callback;
import java.io.ByteArrayOutputStream;
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

public class BlocksParserController implements SingleViewController<BorderPane>, Initializable {
    private final FlowApp flowApp;
    private final Blocks blocks;
    private final Runnable cleanUp;

    private void getHTMLOffline(Consumer<String> htmlConsumer) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selected File to Parse into Block");

        Stage allocatedStage = new Stage();
        java.io.File file = fileChooser.showOpenDialog(allocatedStage);
        Path path = file.toPath();

        String html = CardProcessor.toHTML(path);

        htmlConsumer.accept(html);
    }

    private void getHTMLGoogleDrive(Consumer<String> htmlConsumer) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(NavigatorController.class.getResource("/gui/drive/drive_picker.fxml"));
            GDriveFilePickerController pickerController = new GDriveFilePickerController();
            fxmlLoader.setController(pickerController);
            fxmlLoader.load();

            Scene scene = new Scene(pickerController.getCorrelatingView());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();

            /*// Build a new authorized API client service.
            Drive service = GDriveConnector.getDriveService();

            Drive.Files serviceFiles = service.files();

            FileList result = serviceFiles.list()
                    //.setPageSize(10)
                    .setQ("(mimeType='application/vnd.google-apps.document')")
                    .setFields("nextPageToken, files(id, name, createdTime)")
                    .execute();

            List<File> files = result.getFiles();

            if (files.isEmpty())
                System.out.println("No files found.");
            else {
                FXMLLoader fxmlLoader = new FXMLLoader(NavigatorController.class.getResource("/gui/drive/drive_picker.fxml"));
                GDriveFilePickerController pickerController = new GDriveFilePickerController(files);
                fxmlLoader.setController(pickerController);
                fxmlLoader.load();

                Scene scene = new Scene(pickerController.getCorrelatingView());
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();

                pickerController.getCorrelatingView().addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.ENTER) {
                        try {
                            System.out.println("enter is pressed");
                            TreeItem<File> selectedTreeItem = pickerController.getCorrelatingView().getSelectionModel().getSelectedItem();
                            File selectedFile = selectedTreeItem.getValue();

                            String fileId = selectedFile.getId();

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            serviceFiles
                                    .export(fileId, "text/html")
                                    .executeMediaAndDownloadTo(outputStream);
                            byte[] bytes = outputStream.toByteArray();

                            String html = new String(bytes);

                            htmlConsumer.accept(html);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getHTMLOneDrive(Consumer<String> htmlAcceptor) {
        htmlAcceptor.accept(null);
    }


    private final Map<Host, Consumer<Consumer<String>>> MEDIUM_HTML_CONSUMER_BLOCKS = Map.of(
            Host.GOOGLE_DRIVE, this::getHTMLGoogleDrive,
            Host.FILE_SYSTEM, this::getHTMLOffline,
            Host.ONE_DRIVE, this::getHTMLOneDrive
    );

    @FXML public TreeView<Card> parsedCardsColumn;
    @FXML public TreeItem<Card> parsedCardsRoot;
    @FXML public TextField cardNameRequest;

    @FXML public BorderPane cardParserArea;
    @FXML public WebView documentDisplay;
    @FXML public ProgressBar progressBar;
    @FXML public VBox rightContainer;

    public BlocksParserController(FlowApp flowApp, Blocks blocks, Runnable cleanUp) {
        this.flowApp = flowApp;
        this.blocks = blocks;
        this.cleanUp = cleanUp;
    }

    private String snapSelectionToWordJS;
    private String selectHTMLJS;

    @FXML
    public Button fileChooser;

    @FXML
    public void onFileChooserClick(ActionEvent actionEvent) {
        Host host = parserOptionChooser.getValue();

        Consumer<Consumer<String>> htmlFetcher = MEDIUM_HTML_CONSUMER_BLOCKS.get(host);
        htmlFetcher.accept(this::loadHTML);
    }

    @FXML public ComboBox<Host> parserOptionChooser;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parserOptionChooser.getItems().setAll(Host.values());

        fileChooser.prefWidthProperty().bind(rightContainer.widthProperty());

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

            Card deleteDummyCard = new Card("Delete", null);
            TreeItem<Card> deleteTreeItem = new TreeItem<>(deleteDummyCard);
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

    private void onAttemptFinish() {
        System.out.println("Finish attempted");

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

    private String getSelectedText() {
        return (String) documentDisplay.getEngine().executeScript("window.getSelection().toString()");
    }

    private String getSelectedHTML() {
        return (String) documentDisplay.getEngine().executeScript(getSelectHTMLJS());
    }

    private void clearSelection() {
        documentDisplay.getEngine().executeScript("window.getSelection().removeAllRanges()");
    }
}
