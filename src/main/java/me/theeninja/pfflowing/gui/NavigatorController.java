package me.theeninja.pfflowing.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.*;
import me.theeninja.pfflowing.configuration.ConfigEditorController;
import me.theeninja.pfflowing.flowingregions.Blocks;
import me.theeninja.pfflowing.gui.cardparser.BlocksParserController;
import me.theeninja.pfflowing.tournament.Round;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class NavigatorController implements SingleViewController<MenuBar>, Initializable {
    private final FlowApp flowApp;

    @FXML public Menu openRecent;
    @FXML public MenuItem undoItem;
    @FXML public MenuItem redoItem;

    private static int compareByLastModifiedTime(Path o1, Path o2) {
        try {
            return Files.getLastModifiedTime(o1).compareTo(Files.getLastModifiedTime(o2));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    private final static int RECENT_SIZE = 5;

    public void loadOpenRecent() {
        try {
            Path cardsPath = EFlow.getInstance().getCardsPath();

            Files.walk(cardsPath)
                    .filter(path -> Utils.hasExtension(path.getFileName().toString(), "json"))
                    .sorted((NavigatorController::compareByLastModifiedTime))
                    .limit(RECENT_SIZE)
                    .map(Utils::readAsString)
                    .map(json -> EFlow.getInstance().getGSON().fromJson(json, Blocks.class))
                    .map(this::getLoadMenuItem)
                    .forEach(openRecent.getItems()::add);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onHelpClicked(ActionEvent actionEvent) {

    }

    @FXML
    public void onLoadBlocks(ActionEvent actionEvent) {
        Blocks blocks = requestForBlocks();
        loadBlocks(blocks);
    }

    @FXML
    public void onEditBlocks(ActionEvent actionEvent) {
        Blocks blocks = requestForBlocks();
        openBlocksEditor(blocks);
    }

    @FXML
    public MenuBar navigator;

    public NavigatorController(FlowApp flowApp) {
        this.flowApp = flowApp;
    }

    @FXML
    public void newFlow(ActionEvent actionEvent) {

    }

    @FXML
    public void openRound(ActionEvent actionEvent) {
        try {
            getFlowApp().getFlowController().openRound();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openDirectory(ActionEvent actionEvent) {
        try {
            getFlowApp().getFlowController().openDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void saveFlow(ActionEvent actionEvent) {
        try {
            getFlowApp().getFlowController().saveSelectedRound();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/config/config_editor.fxml"));
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

    private FlowDisplayController getSelectedController() {
        Tab selectedTab = getFlowApp().getFlowController().roundsBar.getSelectionModel().getSelectedItem();
        RoundTab selectedRoundTab = (RoundTab) selectedTab;
        Round selectedRound = selectedRoundTab.getRound();
        return selectedRound.getSelectedController();
    }

    @FXML
    public void undo(ActionEvent actionEvent) {
        getSelectedController().getActionManager().undo();
    }

    @FXML
    public void redo(ActionEvent actionEvent) {
        getSelectedController().getActionManager().redo();
    }

    @FXML
    public void selectAll(ActionEvent actionEvent) {
        getSelectedController().selectAll();
    }

    // ------------------------- //
    // -BLOCK RELATED FUNCTIONS- //
    // ------------------------- //

    /**
     * Open parser popup.
     *
     * @param actionEvent The action event that contains what option to use regarding fetching a file to parse
     *                    (options are between Offline System, Google Drive, One Drive).
     */
    /**@FXML
    private void openParserPopup(ActionEvent actionEvent) {
        MenuItem node = (MenuItem) actionEvent.getSource();
        Object userData = node.getUserData();
        String typeOfOpenStr = (String) userData;
        int typeOfOpen = Integer.parseInt(typeOfOpenStr);

        Consumer<Consumer<String>> htmlConsumerConsumer = MEDIUM_HTML_CONSUMER_BLOCKS.get(typeOfOpen);
        htmlConsumerConsumer.accept(htmlResult -> promptForNewBlocks(this::openBlocksEditor));
    } */

    @FXML
    public void onNewBlocks(ActionEvent actionEvent) {
        promptForNewBlocks(this::openBlocksEditor);
    }

    /**
     * Asks the user through a file chooser to open a block file. This block file is parsed through
     * GSON.
     *
     * @return The blocks object that correlates to the JSON of the file that was opened.
     */
    public Blocks requestForBlocks() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(EFlow.getInstance().getCardsPath().toFile());

        Stage allocatedStage = new Stage();
        java.io.File file = fileChooser.showOpenDialog(allocatedStage);
        Path path = file.toPath();

        byte[] jsonBytes = null;

        try {
            jsonBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String jsonString = new String(jsonBytes);

        Blocks blocks = EFlow.getInstance().getGSON().fromJson(jsonString, Blocks.class);

        return blocks;
    }

    // editBlocks(Blocks)
    // loadBlocks(Blocks)
    // newHTML /* GD, OFF, OD */ -> String

    /**
     * Shows a prompt for creating a new block file. Upon submission of this prompt, {@code blocksConsumer}
     * accepts the created {@link Blocks}.
     *
     * @param blocksConsumer The consumer of the created blocks. This is called after the prompt's submission.
     */
    public void promptForNewBlocks(Consumer<Blocks> blocksConsumer) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/prompter/block_prompt.fxml"));
        BlocksCreatorController blocksCreatorController = new BlocksCreatorController(blocksConsumer);

        fxmlLoader.setController(blocksCreatorController);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Stage stage = new Stage();
        Scene scene = new Scene(blocksCreatorController.getCorrelatingView());
        stage.setScene(scene);

        stage.show();

        blocksCreatorController.finishButton.addEventHandler(ActionEvent.ACTION, actionEvent -> stage.hide());
    }


    /**
     * Adds the given blocks to the instance of {@link CardSelectorController} within the
     * parent view.
     *
     * @param blocks The blocks to add.
     */
    private void loadBlocks(Blocks blocks) {
        getFlowApp().getFlowController().getCardSelectorController().addBlocks(blocks);
    }

    private void openBlocksEditor(Blocks blocks) {
        Stage stage = new Stage();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/cardParser/card_parser.fxml"));
        BlocksParserController blocksParserController = new BlocksParserController(
            getFlowApp(),
            blocks,
            stage::hide
        );
        fxmlLoader.setController(blocksParserController);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(blocksParserController.getCorrelatingView());
        stage.setScene(scene);

        stage.show();
        stage.toFront();

        blocksParserController.startProcess();
    }

    @FXML
    public void newRound(ActionEvent actionEvent) {
        getFlowApp().getFlowController().promptRoundAddition();
    }

    @Override
    public MenuBar getCorrelatingView() {
        return navigator;
    }

    public FlowApp getFlowApp() {
        return flowApp;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadOpenRecent();
    }

    private MenuItem getLoadMenuItem(Blocks blocks) {
        MenuItem blocksMenuItem = new MenuItem();
        blocksMenuItem.setText(blocks.getName());

        blocksMenuItem.setOnAction(actionEvent ->
            getFlowApp().getFlowController().getCardSelectorController().addBlocks(blocks)
        );

        return blocksMenuItem;
    }

    @FXML
    public void extend() {
        getSelectedController().attemptExtension();
    }

    @FXML
    public void refute() {
        getSelectedController().attemptRefutation();
    }

    @FXML
    public void write() {
        getSelectedController().addWriter();
    }

    @FXML
    public void drop() {
        getSelectedController().attemptDrop();
    }

    @FXML
    public void delete() {
        getSelectedController().attemptDelete();
    }

    @FXML
    public void merge() {
        getSelectedController().attemptMerge();
    }

    @FXML
    public void question() {
        getSelectedController().attemptQuestion();
    }
}