package me.theeninja.pfflowing.gui;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.Action;
import me.theeninja.pfflowing.FlowApp;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.bluetooth.EFlowConnector;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.printing.RoundPrinter;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;
import me.theeninja.pfflowing.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FlowController implements Initializable, SingleViewController<FlowingPane> {
    @FXML public FlowingPane pfFlowingMain;
    @FXML public HBox notificationDisplay;
    @FXML public TabPane roundsBar;

    private CardSelectorController cardSelectorController;
    private NavigatorController navigatorController;
    private final FlowApp flowApp;

    public FlowController(FlowApp flowApp) {
        this.flowApp = flowApp;
    }

    private void onRegionRemovalRemoveDragSupport(Node node) {
        if (node instanceof FlowingRegion) {
            FlowingRegion flowingRegion = (FlowingRegion) node;
            removeDragSupport(flowingRegion);
        }
    }

    private void onRegionAdditionAddDragSupport(Node node) {
        if (node instanceof FlowingRegion) {
            FlowingRegion flowingRegion = (FlowingRegion) node;
            addDragSupport(flowingRegion);
        }
    }

    private void onDragOver(DragEvent dragEvent) {
        Object source = dragEvent.getGestureSource();

        if (!(source instanceof CardTreeCell))
            return; // drag was initiated from an irrelevant component on the scene

        CardTreeCell cardTreeCell = (CardTreeCell) source;
        Card card = cardTreeCell.getTreeItem().getValue();

        if (card.getRepresentation() == null)
            return; // transfer was initiated from dummy card associated
                    // with header tree item, not content tree item

        dragEvent.acceptTransferModes(TransferMode.ANY);
    }

    @Override
    public FlowingPane getCorrelatingView() {
        return pfFlowingMain;
    }

    private EventHandler<KeyEvent> getKeyEventHandler() {
        return keyEvent -> {
            if (!roundsBar.getTabs().isEmpty()) {
                RoundTab selectedRoundTab = (RoundTab) roundsBar.getSelectionModel().getSelectedItem();
                Round selectedRound = selectedRoundTab.getRound();

                KeyEventProcessor keyEventProcessor = new KeyEventProcessor(this, keyEvent);
                keyEventProcessor.process();
            }
        };
    }

    public void switchSpeechList() {
        Round selectedRound = getSelectedRound();
        selectedRound.setDisplayedSide(selectedRound.getDisplayedSide().getOpposite());
        roundsBar.getSelectionModel().getSelectedItem().setContent(selectedRound.getSelectedController().flowDisplay);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roundsBar.addEventHandler(KeyEvent.KEY_PRESSED, getKeyEventHandler());

        // Keep file and directory choosers as global instance variables in order to preserve their state
        // through multiple saves and opens
        setFileChooser(new FileChooser());
        setDirectoryChooser(new DirectoryChooser());

        // Keep states of both choosers identical upon closing and opening of the choosers
        Bindings.bindBidirectional(getFileChooser().initialDirectoryProperty(), getDirectoryChooser().initialDirectoryProperty());

        FileChooser.ExtensionFilter eflowExtensionFilter = new FileChooser.ExtensionFilter("EFlow files (*.eflow)", "*.eflow");
        getFileChooser().getExtensionFilters().add(eflowExtensionFilter);

        CardSelectorController cardSelectorController = new CardSelectorController(getFlowApp());
        setUpController(cardSelectorController, "/gui/card_selector/card_selector.fxml", this::setCardSelectorController);
        getCorrelatingView().setLeft(cardSelectorController.getCorrelatingView());

        NavigatorController navigatorController = new NavigatorController(getFlowApp());
        setUpController(navigatorController, "/gui/navigator/navigator.fxml", this::setNavigatorController);
        getCorrelatingView().setTop(navigatorController.getCorrelatingView());

        getNavigatorController().getCorrelatingView().setPrefHeight(Region.USE_PREF_SIZE);
        roundsBar.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        roundsBar.getTabs().addListener(Utils.generateListChangeListener(this::onTabAdded, this::onTabRemoved));
    }

    public void printSelectedRound() {
        RoundPrinter.print(getSelectedRound());
    }

    public void attemptBluetoothShare() {
        Thread thread = new Thread(() -> {
            try {
                EFlowConnector eFlowConnector = new EFlowConnector("E0997131968B", getSelectedRound());
                eFlowConnector.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.setDaemon(true);

        thread.start();

    }

    private final ListChangeListener<? super Node> onChildrenChange = Utils.generateListChangeListener(
            this::onRegionAdditionAddDragSupport,
            this::onRegionRemovalRemoveDragSupport
    );

    private void onTabAdded(Tab tab) {
        if (!(tab instanceof RoundTab))
            throw new UnsupportedOperationException("Cannot add non-RoundTab to roundsBar");

        RoundTab roundTab = (RoundTab) tab;
        Round round = roundTab.getRound();

        for (FlowDisplayController controller : round.getSideControllers()) {
            controller.getCorrelatingView().maxWidthProperty().bind(roundsBar.widthProperty());
            controller.flowGrid.getChildren().addListener(onChildrenChange);
        }
    }

    private void onTabRemoved(Tab tab) {
        RoundTab roundTab = (RoundTab) tab;
        Round round = roundTab.getRound();

        for (FlowDisplayController controller : round.getSideControllers()) {
            controller.getCorrelatingView().maxWidthProperty().unbind();
            controller.flowGrid.getChildren().removeListener(onChildrenChange);
        }
    }

    private <T> void setUpController(T controllerInstance, String fileName, Consumer<T> setter) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fileName));
            fxmlLoader.setController(controllerInstance);
            fxmlLoader.load();
            setter.accept(controllerInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final KeyCodeCombination FINISH = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    private static final String SELECTION_LABEL_CLASS = "selectionLabel";
    private static final String SELECTED_SELECTION_LABEL_CLASS = "selectedSelectionLabel";

    private static final int PIXELS_TO_REMOVE_DROPDOWN = 30;

    public void promptRoundAddition() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/prompter/new_prompt.fxml"));
        RoundPrompterController roundPrompterController = new RoundPrompterController(this);
        fxmlLoader.setController(roundPrompterController);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(roundPrompterController.getCorrelatingView());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();

        roundPrompterController.finishButton.addEventHandler(ActionEvent.ACTION, actionEvent -> {
            roundPrompterController.finish();
            stage.hide();
        });

    }

    public void addRound(String roundName, Side side) {
        Round round = new Round(side);
        RoundTab roundTab = new RoundTab(round);

        round.setName(roundName);
        round.setDisplayedSide(side);

        roundsBar.getTabs().add(roundTab);
    }

    private void addDragSupport(FlowingRegion flowingRegion) {
        System.out.println("Added drag support for '" + flowingRegion.getFullText() + "'");
        flowingRegion.addEventHandler(DragEvent.DRAG_OVER, this::onDragOver);
        flowingRegion.addEventHandler(DragEvent.DRAG_DROPPED, this::onDragDroppedOnRegion);
    }

    private void removeDragSupport(FlowingRegion flowingRegion) {
        System.out.println("Removed drag support for '" + flowingRegion.getFullText() + "'");
        flowingRegion.removeEventHandler(DragEvent.DRAG_OVER, this::onDragOver);
        flowingRegion.removeEventHandler(DragEvent.DRAG_DROPPED, this::onDragDroppedOnRegion);
    }

    private static final String FILE_EXTENSION = "eflow";
    private static final String SAVE_TITLE = "Save an EFlow";

    private File getEFlowTypeFile(File file) {
        if (!file.getAbsolutePath().endsWith("." + FILE_EXTENSION))
            return new File(file.getAbsoluteFile() + "." + FILE_EXTENSION);
        else
            return file;
    }

    public void saveSelectedRound() throws IOException {
        Round selectedRound = getSelectedRound();

        // Indicates this round has not been saved before
        if (selectedRound.getPath() == null) {
            Stage allocatedStage = new Stage();

            getFileChooser().setTitle(SAVE_TITLE);

            File file = getFileChooser().showSaveDialog(allocatedStage);

            if (file == null)
                return;

            File eFlowFile = getEFlowTypeFile(file);
            Path returnedPath = eFlowFile.toPath();

            selectedRound.setPath(returnedPath);
        }

        String json = EFlow.getInstance().getGSON().toJson(selectedRound, Round.class);
        Files.write(selectedRound.getPath(), json.getBytes());
    }

    private static final String OPEN_ROUND_TITLE = "Open an EFlow Round";
    private static final String OPEN_TOURNAMENT_TITLE = "Open an EFlow Tournament";

    public void openRound() throws IOException {
        getFileChooser().setTitle(OPEN_ROUND_TITLE);

        Stage allocatedStage = new Stage();
        File file = getFileChooser().showOpenDialog(allocatedStage);

        // no file chosen
        if (file == null)
            return; // assume that user cancelled opening

        File eflowFile = getEFlowTypeFile(file);
        Path path = eflowFile.toPath();

        Round existingOpenedRound = getRoundByPath(path);

        // Indicates that this round is already opened
        if (existingOpenedRound != null) {
            // Rather than open second instance of round, select first instance
            RoundTab roundTab = getTab(existingOpenedRound);
            roundsBar.getSelectionModel().select(roundTab);

            return;
        }

        byte[] jsonBytes = Files.readAllBytes(path);
        String json = new String(jsonBytes);

        Round round = EFlow.getInstance().getGSON().fromJson(json, Round.class);
        round.setPath(path);

        RoundTab roundTab = new RoundTab(round);
        roundsBar.getTabs().add(roundTab);

        round.setDisplayedSide(round.getSide());
    }

    /**
     * @param round The round which is associated with the tab.
     * @return The round tab containing the round's content.
     */
    public RoundTab getTab(Round round) {
        for (Tab tab : roundsBar.getTabs()) {
            RoundTab roundTab = (RoundTab) tab;

            if (roundTab.getRound() == round)
                return roundTab;
        }

        return null;
    }

    /**
     * Maps each round tab to their associated round, and returns all rounds.
     *
     * @return All rounds represented by the rounds bar.
     */
    public List<Round> getRoundsOnBar() {
        return roundsBar.getTabs().stream()
                .map(RoundTab.class::cast)
                .map(RoundTab::getRound)
                .collect(Collectors.toList());
    }

    /**
     * @param path The path that is associated with the round.
     * @return null
     */
    public Round getRoundByPath(Path path) {
        List<Round> rounds = getRoundsOnBar();

        for (Round round : rounds) {
            // if round is not associated with path, skip it
            if (round.getPath() == null)
                continue;

            if (round.getPath().equals(path))
                return round;
        }

        return null;
    }

    public void openDirectory() throws IOException {
        getDirectoryChooser().setTitle(OPEN_TOURNAMENT_TITLE);
        File directory = getDirectoryChooser().showDialog(getFlowApp().getStage());

        // no directory chosen
        if (directory == null)
            return; // assume that user cancelled opening

        Path directoryPath = directory.toPath();

        List<Path> roundPaths = Files.walk(directoryPath).filter(Files::isRegularFile).collect(Collectors.toList());

        for (Path roundPath : roundPaths) {
            byte[] jsonBytes = Files.readAllBytes(roundPath);
            String json = new String(jsonBytes);

            Round round = EFlow.getInstance().getGSON().fromJson(json, Round.class);
            round.setPath(roundPath);
            RoundTab roundTab = new RoundTab(round);
            roundsBar.getTabs().add(roundTab);
        }
    }

    public void setFileChooser(FileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;

    public FileChooser getFileChooser() {
        return fileChooser;
    }

    public DirectoryChooser getDirectoryChooser() {
        return directoryChooser;
    }

    public void setDirectoryChooser(DirectoryChooser directoryChooser) {
        this.directoryChooser = directoryChooser;
    }

    public FlowApp getFlowApp() {
        return flowApp;
    }

    public CardSelectorController getCardSelectorController() {
        return cardSelectorController;
    }

    public void setCardSelectorController(CardSelectorController cardSelectorController) {
        this.cardSelectorController = cardSelectorController;
    }

    public void setNavigatorController(NavigatorController navigatorController) {
        this.navigatorController = navigatorController;
    }

    public NavigatorController getNavigatorController() {
        return navigatorController;
    }

    public Round getSelectedRound() {
        RoundTab roundTab = (RoundTab) roundsBar.getSelectionModel().getSelectedItem();
        return roundTab.getRound();
    }

    private void onDragDroppedOnRegion(DragEvent dragEvent) {
        Object target = dragEvent.getGestureTarget();

        // should never happen, as this event handler is only applied to actions regions
        if (!(target instanceof FlowingRegion)) {
            System.out.println("Target is not instance of actions region, is " + target.getClass().getName());
            return;
        }

        FlowingRegion flowingRegion = (FlowingRegion) target;

        Dragboard dragboard = dragEvent.getDragboard();

        // should never happen, as the DRAG_OVER event handler should confirm that the drag source
        // is onRegionRemovalRemoveDragSupport card tree cell. Thus, the drag event should have onRegionRemovalRemoveDragSupport string containing the card name
        if (!dragboard.hasString()) {
            System.out.println("Dragboard does not have string for '" + flowingRegion.getFullText() + "'");
            return;
        }

        String cardName = dragboard.getString();

        Card card = getCardSelectorController().getCard(cardName);

        if (flowingRegion.getAssociatedCards().contains(card)) {
            System.out.println("Already has card for '" + flowingRegion.getFullText() + "'");
            return; // if card has already been added, do not readd
        }

        Action modifyCard = new ModifyCard(flowingRegion, card);

        getSelectedRound().getSelectedController().getActionManager().perform(modifyCard);
    }

    private class ModifyCard extends Action {
        private final FlowingRegion targetFlowingRegion;
        private final Card card;

        ModifyCard(FlowingRegion flowingRegion, Card card) {
            this.targetFlowingRegion = flowingRegion;
            this.card = card;
        }

        @Override
        public void execute() {
            getTargetFlowingRegion().getAssociatedCards().add(card);
        }

        @Override
        public void unexecute() {
            getTargetFlowingRegion().getAssociatedCards().remove(card);
        }

        @Override
        public String getName() {
            return "Card(s) Change";
        }

        public FlowingRegion getTargetFlowingRegion() {
            return targetFlowingRegion;
        }

        public Card getCard() {
            return card;
        }
    }
}
