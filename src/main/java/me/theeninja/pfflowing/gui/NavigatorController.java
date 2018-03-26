package me.theeninja.pfflowing.gui;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.FlowApp;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.configuration.ConfigEditorController;
import me.theeninja.pfflowing.drive.google.GDriveConnector;
import me.theeninja.pfflowing.drive.GDriveFilePickerController;
import me.theeninja.pfflowing.flowingregions.Blocks;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.flowingregions.CardProcessor;
import me.theeninja.pfflowing.gui.cardparser.CardParserController;
import me.theeninja.pfflowing.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class NavigatorController implements SingleViewController<MenuBar>, Initializable {
    private final FlowApp flowApp;

    @FXML
    public Menu openRecent;

    public void loadOpenRecent() {
        try {
            Path cardsPath = EFlow.getInstance().getCardsPath();
            Files.walk(cardsPath)
                    .filter(path -> Utils.hasExtension(path.getFileName().toString(), "json"))
                    .map(Utils::readAsString)
                    .map(json -> EFlow.getInstance().getGSON().fromJson(json, Blocks.class))
                    .forEach(this::addLoadBlockMenuItem);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public MenuBar navigator;

    public NavigatorController(FlowApp flowApp) {
        this.flowApp = flowApp;
    }

    private boolean doesFlowFileExist;

    @FXML
    public void newFlow(ActionEvent actionEvent) {

    }

    @FXML
    public void openRound(ActionEvent actionEvent) {
        try {
            getPFFlowing().getFlowController().openRound();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openTournament(ActionEvent actionEvent) {
        try {
            getPFFlowing().getFlowController().openTournament();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void saveFlow(ActionEvent actionEvent) {

    }

    @FXML
    public void saveFlowAs(ActionEvent actionEvent) {
    }

    @FXML
    public void printFlow(ActionEvent actionEvent) {

    }

    @FXML
    public void emailFlow(ActionEvent actionEvent) {

    }

    @FXML
    public void configure(ActionEvent actionEvent) {
        try {
            ConfigEditorController configEditorController = new ConfigEditorController(EFlow.getInstance().getConfiguration());
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/config_editor.fxml"));
            fxmlLoader.setController(configEditorController);
            fxmlLoader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(configEditorController.getCorrelatingView());

            stage.setScene(scene);

            stage.show();
            stage.toFront();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void undo(ActionEvent actionEvent) {

    }

    @FXML
    public void redo(ActionEvent actionEvent) {

    }

    @FXML
    public void selectAll(ActionEvent actionEvent) {

    }

    @FXML
    public void openParserPopupGD() {
        try {
            // Build a new authorized API client service.
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
                FXMLLoader fxmlLoader = new FXMLLoader(NavigatorController.class.getResource("/drive_picker.fxml"));
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

                            Stage stage_ = new Stage();

                            FXMLLoader fxmlLoader_ = new FXMLLoader(getClass().getResource("/card_parser_gui/card_parser.fxml"));
                            CardParserController cardParserController = new CardParserController(getPFFlowing(), generateOnFinish(stage));
                            fxmlLoader_.setController(cardParserController);

                            try {
                                fxmlLoader_.load();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Scene scene_ = new Scene(cardParserController.getCorrelatingView());
                            stage_.setScene(scene_);

                            stage_.show();
                            stage_.toFront();

                            cardParserController.loadHTML(html);
                            cardParserController.startProcess();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openParserPopupFile() {
        Stage stage = new Stage();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/card_parser_gui/card_parser.fxml"));
        CardParserController cardParserController = new CardParserController(getPFFlowing(), generateOnFinish(stage));
        fxmlLoader.setController(cardParserController);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(cardParserController.getCorrelatingView());
        stage.setScene(scene);

        stage.show();
        stage.toFront();

        Path path = cardParserController.promptFile();
        String html = CardProcessor.toHTML(path);

        cardParserController.loadHTML(html);
        cardParserController.startProcess();
    }

    @FXML
    public void newRound(ActionEvent actionEvent) {
        getPFFlowing().getFlowController().promptRoundAddition();
    }

    @FXML
    public void newTournament(ActionEvent actionEvent) {
        getPFFlowing().getFlowController().newTournament();
    }

    @Override
    public MenuBar getCorrelatingView() {
        return navigator;
    }

    public FlowApp getPFFlowing() {
        return flowApp;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadOpenRecent();
    }

    private Consumer<List<Card>> generateOnFinish(Stage stage) {
        return cards -> {
            CardSelectorController cardSelectorController = getPFFlowing().getFlowController().getCardSelectorController();
            TreeItem<Card> treeViewRoot = cardSelectorController.getCorrelatingView().getRoot();

            cards.stream().map(TreeItem::new)
                    .peek(treeItem -> System.out.println(treeItem.getValue().toString()))
                    .forEach(treeViewRoot.getChildren()::add);

            stage.hide();
        };
    }

    private void addLoadBlockMenuItem(Blocks blocks) {
        MenuItem blocksMenuItem = new MenuItem();
        blocksMenuItem.setText(blocks.getName());

        blocksMenuItem.setOnAction(actionEvent ->
            getPFFlowing().getFlowController().getCardSelectorController().addBlocks(blocks)
        );

        openRecent.getItems().add(blocksMenuItem);
    }
}