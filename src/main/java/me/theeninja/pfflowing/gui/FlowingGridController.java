package me.theeninja.pfflowing.gui;

import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import me.theeninja.pfflowing.Action;
import me.theeninja.pfflowing.PFFlowing;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.configuration.GlobalConfiguration;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.flowingregions.*;
import me.theeninja.pfflowing.utils.Utils;
import me.theeninja.pfflowing.utils.Pair;
import org.apache.commons.collections4.ListUtils;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static me.theeninja.pfflowing.gui.KeyCodeCombinationUtils.*;

/**
 * The controller for the actual flowing area on the application. This controller
 * is responsible for managing the relations between all flowing regions that exist.
 *
 * @author TheeNinja
 */
public class FlowingGridController implements Initializable, SingleViewController<FlowingGrid>, EventHandler<KeyEvent> {
    /**
     * Merging action. The process of merging involves collecting all the contents in the rows of all the flowing regions
     * into the row belonging to the top-most (determined by lowest row index) flowing region.
     * @author TheeNinja
     */
    private class Merge extends Action {

        private final List<FlowingRegion> flowingRegions;

        Merge(List<FlowingRegion> flowingRegions) {
            this.flowingRegions = flowingRegions.stream().map(FlowingRegion::duplicate).collect(Collectors.toList());
            getFlowingRegions().sort(Comparator.comparingInt(FlowingGrid::getRowIndex));
        }

        private FlowingRegion condense(List<FlowingRegion> flowingRegions) {
            if (flowingRegions.size() == 1) {
                return flowingRegions.get(0).duplicate();
            }

            FlowingRegion condensedFlowingRegion = new FlowingRegion(flowingRegions.get(0).getInstance());
            flowingRegions.stream().map(FlowingRegion::duplicate).forEach(flowingRegion -> {
                condensedFlowingRegion.getChildren().addAll(flowingRegion.getChildren());
                if (!Utils.isLastElement(flowingRegions, flowingRegion))
                    condensedFlowingRegion.getChildren().add(new Text(GlobalConfiguration.MERGE_SEPERATOR));
            });
            return condensedFlowingRegion;
        }

        @Override
        public void execute() {
            FlowingRegion firstFlowingRegion = getFlowingRegions().get(0);
            int row = FlowingGrid.getRowIndex(firstFlowingRegion);

            List<List<FlowingRegion>> linksList = new ArrayList<>();
            getFlowingRegions().stream().map(getCorrelatingView()::getWholeLink).forEach(linksList::add);

            int maxLinkLength = linksList.stream().map(List::size).reduce(Integer::max).get();

            System.out.println(maxLinkLength);

            List<List<FlowingRegion>> i_thFlowingRegions = IntStream.range(0, maxLinkLength).mapToObj(value -> linksList.stream().map(
                    list -> list.get(value)).collect(Collectors.toList())
            ).collect(Collectors.toList());

            int minColumn = FlowingGrid.getColumnIndex(i_thFlowingRegions.get(0).get(0));

            for (int column = minColumn; column < i_thFlowingRegions.size(); column++) {
                FlowingRegion condensedFlowingRegion = this.condense(i_thFlowingRegions.get(column));
                addFlowingRegion(condensedFlowingRegion, column, row);
            }

            List<FlowingRegion> flowingRegionsToRemove = linksList.stream().flatMap(List::stream).collect(Collectors.toList());

            getCorrelatingView().getChildren().removeAll(flowingRegionsToRemove);
        }

        @Override
        public void unexecute() {

        }

        @Override
        public String getName() {
            return "Merge";
        }

        public List<FlowingRegion> getFlowingRegions() {
            return flowingRegions;
        }
    }

    /**
     * Refutes all selected nodes in a position relative to the last selected node. This is done by:
     * 1) constructing a flowing region writer that, when submitted, yields an offensive flowing region
     * 2) constructing a visual link between the newly created offensive flowing region and the selected nodes
     */
    private class Refute extends Action {

        private final FlowingRegion baseFlowingRegion;
        private OffensiveFlowingRegion refFlowingRegion;

        public Refute(FlowingRegion baseFlowingRegion, String text) {
            this.baseFlowingRegion = baseFlowingRegion;

            Speech baseSpeech = getSpeechList().getSpeech(baseFlowingRegion);
            // Utils.getRelativeElement(...) will wrap around, yet you cannot refute AT-Neg4 or AT-Aff4 CardContent
            if (Utils.isLastElement(getSpeechList().getSpeeches(), baseSpeech))
                return;

            this.refFlowingRegion = new OffensiveFlowingRegion(new Reasoning(text), baseSpeech.getSide(), getBaseFlowingRegion(), FlowingGridController.this);
        }

        @Override
        public void execute() {
            addOffensiveFlowingRegion(refFlowingRegion);
        }

        @Override
        public void unexecute() {

            getCorrelatingView().getChildren().remove(refFlowingRegion);
        }

        @Override
        public String getName() {
            return "Refute";
        }

        public FlowingRegion getBaseFlowingRegion() {
            return baseFlowingRegion;
        }
    }

    private class Extend extends Action {
        private List<FlowingRegion> baseFlowingRegions;
        private Speech speech;
        private List<ExtensionFlowingRegion> extendFlowingRegions;
        private List<FlowingLink> flowingLinks;

        Extend(List<FlowingRegion> baseFlowingRegions) {
            this.baseFlowingRegions = baseFlowingRegions;
            System.out.println(baseFlowingRegions.size());
            this.speech = getSpeechList().getSpeech(baseFlowingRegions.get(0)); // speech guaranteed to be the same for all selected
            this.extendFlowingRegions = this.baseFlowingRegions.stream().map(FlowingRegion::duplicate).map(baseFlowingRegion ->
                    new ExtensionFlowingRegion(Utils.getOfType(baseFlowingRegion.getChildren(), FlowingText.class), speech.getSide(), baseFlowingRegion, FlowingGridController.this)
            ).collect(Collectors.toList());
        }

        @Override
        public void execute() {
            this.extendFlowingRegions.forEach(FlowingGridController.this::addExtensionFlowingRegion);
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.25));
            pauseTransition.setOnFinished(actionEvent -> {
                this.flowingLinks = this.extendFlowingRegions.stream().map(extensionFlowingRegion -> {
                    Speech firstSpeech = getSpeechList().getSpeech(extensionFlowingRegion.getBase());
                    Speech secondSpeech = getSpeechList().getSpeech(extensionFlowingRegion);
                    int row = FlowingGrid.getRowIndex(extensionFlowingRegion);

                    return new FlowingLink(firstSpeech.getGridPaneColumn(), secondSpeech.getGridPaneColumn(), row, FlowingGridController.this);
                }).collect(Collectors.toList());

                flowingLinks.forEach(FlowingLink::rebindProperties);
                PFFlowingApplicationController.getFXMLInstance().getCorrelatingView().getChildren().addAll(flowingLinks);
            });
            pauseTransition.play();
        }

        @Override
        public void unexecute() {
            getCorrelatingView().getChildren().removeAll(this.extendFlowingRegions);
        }

        @Override
        public String getName() {
            return "Extend";
        }
    }

    private class Delete extends Action {
        private List<FlowingRegion> deletedFlowingRegions;

        public Delete(List<FlowingRegion> flowingRegions) {
            // Remove duplicates, as they are a possibility. An example to demonstrate:
           /* S = Selected, N = Not Selected
           N
           S S N
           */
            // Assuming that the user wishes to remove all selected, the right-most selected flowing region is part of the link of
            // the left-most selected flowing region. Hence, I can expect this flowing region to be included twice in deletedFlowingRegions.
            deletedFlowingRegions = flowingRegions.stream().map(getCorrelatingView()::getPostLink).flatMap(Collection::stream).distinct().collect(Collectors.toList());
        }


        @Override
        public void execute() {
            getCorrelatingView().getChildren().removeAll(deletedFlowingRegions);

            // TODO: Investigate effects of removal on row indexes
            // However, post-removal, visibly, a flowing region previously on row 2 will be "seen" on row 1. Yet, its row is still 2 within memory.
            // At the time being, this is handled correctly naturally with implementation, and no measures need to be taken for this.
        }

        @Override
        public void unexecute() {
            getCorrelatingView().getChildren().addAll(deletedFlowingRegions);
        }

        @Override
        public String getName() {
            return "Delete";
        }
    }

    /**
     * Represents whether the flowing grid associated with this controller is shown as the center node of
     * the flowing pane.
     */
    private boolean isShown;

    public void hide() {
        if (!isShown())
            return;
        PFFlowingApplicationController.getFXMLInstance().getCorrelatingView().setCenter(null);
        PFFlowing.getInstance().getScene().setOnKeyReleased(null);
        setShown(false);
    }

    /**
     * Shows the flowing grid correlating to this controller as the center node of the flowing pane. By
     * showing said flowing grid, all speeches that belong to it
     */
    public void show() {
        if (isShown())
            return;
        PFFlowingApplicationController.getFXMLInstance().getCorrelatingView().setCenter(getCorrelatingView());
        PFFlowing.getInstance().getScene().setOnKeyReleased(this);
        getCorrelatingView().requestFocus();
        setShown(true);
    }

    /**
     * The {@link HBox} that contains eight colums (one for each speech).
     * This provides an access bridge between multiple speeches, as this
     * is the parent of all {@link VBox}es responsible for managing their
     * associated speeches.
     */
    @FXML public FlowingGrid flowingGrid;

    /**
     * The an instance of {@link ColorUseManager} that manages what {@link Pair}
     * of a {@link Color} and {@link Background} is used for each relationship between
     * a {@link List<FlowingRegion>} and the associated offensive {@link FlowingRegion}
     */
    private ColorUseManager colorUseManager;


    /**
     * Houses a list of all flowing regions that are currently selected. This serves
     * as input for many actions that occur throughout the flowing columns that require
     * target flowing regions, i.e, merging/refuting/speech constructor/etc. A listener
     * is added to provide visual changes to the GUI when something is selected (it is
     * important to inform the user what they have selected currently in a visual
     * manner).
     */
    private ObservableList<FlowingRegion> selectedFlowingRegions =
            FXCollections.observableArrayList();

    private final Map<KeyCodeCombination, Runnable> keyCodeCombinationMap = new HashMap<>();

    private void toggleFullscreen() {
        if (PFFlowing.getInstance().getStage().isFullScreen())
            PFFlowing.getInstance().getStage().setFullScreen(false);
        else
            PFFlowing.getInstance().getStage().setFullScreen(true);
    }

    public Map<KeyCodeCombination, Runnable> getKeyCodeCombinationMap() {
        return keyCodeCombinationMap;
    }

    private FlowingRegion getLastSelected() {
        return Utils.getLastElement(getSelectedFlowingRegions());
    }

    private void populateKeyCodeCombinationMap() {
        keyCodeCombinationMap.put(MERGE, this::attemptMerge);
        keyCodeCombinationMap.put(REFUTE, this::attemptRefutation);
        keyCodeCombinationMap.put(EXTEND, this::attemptExtension);
        keyCodeCombinationMap.put(NARROW_BY_1, () -> narrowBy(1));
        keyCodeCombinationMap.put(EDIT, this::edit);
        keyCodeCombinationMap.put(UPSCALE_BY_1, () -> upscaleBy(1));
        keyCodeCombinationMap.put(SHIFT_DISPLAY_RIGHT, () -> shift(1));
        keyCodeCombinationMap.put(SHIFT_DISPLAY_LEFT, () -> shift(-1));
        keyCodeCombinationMap.put(SELECT_LEFT_ONLY, () -> handleSelection(getCorrelatingView()::getLeft, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_RIGHT_ONLY, () -> handleSelection(getCorrelatingView()::getRight, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_DOWN_ONLY, () -> handleSelection(getCorrelatingView()::getBelow, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_UP_ONLY, () -> handleSelection(getCorrelatingView()::getAbove, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_LEFT_TOO, () -> handleSelection(getCorrelatingView()::getLeft, getLastSelected(), true));
        keyCodeCombinationMap.put(SELECT_RIGHT_TOO, () -> handleSelection(getCorrelatingView()::getRight, getLastSelected(), true));
        keyCodeCombinationMap.put(SELECT_DOWN_TOO, () -> handleSelection(getCorrelatingView()::getBelow, getLastSelected(), true));
        keyCodeCombinationMap.put(SELECT_UP_TOO, () -> handleSelection(getCorrelatingView()::getAbove, getLastSelected(), true));
        keyCodeCombinationMap.put(UNFOCUS, () -> flowingGrid.requestFocus());
        keyCodeCombinationMap.put(SWITCH_SPEECHLIST, PFFlowing.getInstance()::switchSpeechList);
        keyCodeCombinationMap.put(SELECT_RIGHT_SPEECH, () -> getSpeechList().selectSpeech(1));
        keyCodeCombinationMap.put(SELECT_LEFT_SPEECH, () -> getSpeechList().selectSpeech(-1));
        keyCodeCombinationMap.put(WRITE, () -> addProactiveFlowingRegionWriter(getSpeechList().getSelectedSpeech(), true));
        keyCodeCombinationMap.put(TOGGLE_FULLSCREEN, this::toggleFullscreen);
        keyCodeCombinationMap.put(UNDO, () -> PFFlowing.getInstance().getActionManager().undo());
        keyCodeCombinationMap.put(REDO, () -> PFFlowing.getInstance().getActionManager().redo());
        keyCodeCombinationMap.put(DELETE, () -> PFFlowing.getInstance().getActionManager().perform(new Delete(getSelectedFlowingRegions())));
    }

    public boolean isAnySelected() {
        return getSelectedFlowingRegions().size() != 0;
    }

    public void attemptRefutation() {
        System.out.println("Cuatro" + getSelectedFlowingRegions());
        System.out.println("is this my instance" + (myInstance == this));

        if (!isAnySelected()) {
            NotificationDisplayController.getFXMLInstance()
                    .warn("Nothing is selected; specify what to refute.");
            return;
        }

        if (getSelectedFlowingRegions().size() > 1) {
            NotificationDisplayController.getFXMLInstance()
                    .error("Can only refute one flowing region at a time.");
            return;
        }

        FlowingRegion flowingRegion = getSelectedFlowingRegions().get(0);

        if (getCorrelatingView().getRefutation(flowingRegion).isPresent()) {
            NotificationDisplayController.getFXMLInstance()
                    .error("Selected flowing region already refuted.");
            return;
        }

        Speech baseSpeech = getSpeech(getLastSelected());
        Speech offensiveSpeech = Utils.getRelativeElement(getSpeechList().getSpeeches(), baseSpeech, 1);

        addFlowingRegionWriter(offensiveSpeech, false,
                text -> PFFlowing.getInstance().getActionManager().perform(new Refute(flowingRegion, text)),
                GridPane.getRowIndex(getLastSelected()));
    }

    public void attemptExtension() {
        if (!isAnySelected()) {
            NotificationDisplayController.getFXMLInstance()
                    .warn("Nothing is selected; specify what to extend.");
            return;
        }

        if (getSelectedFlowingRegions().stream().anyMatch(flowingRegion -> getCorrelatingView().getRefutation(flowingRegion).isPresent())) {
            NotificationDisplayController.getFXMLInstance()
                    .error("Atleast one selected flowing region is refuted; unable to extend.");
            return;
        }

        if (!areSameSpeech(getSelectedFlowingRegions())) {
            NotificationDisplayController.getFXMLInstance()
                    .error("Selected flowing regions are not in same speech; unable to extend.");
            return;
        }

        // No need to reextend already extended flowing regions
        List<FlowingRegion> flowingRegions = getSelectedFlowingRegions().stream().filter(
            flowingRegion -> !getCorrelatingView().getExtension(flowingRegion).isPresent()
        ).collect(Collectors.toList());

        System.out.println(getSelectedFlowingRegions().size());
        PFFlowing.getInstance().getActionManager().perform(new Extend(getSelectedFlowingRegions()));
    }

    public void addLabels() {
        System.out.println("Is speech list null: " + getSpeechList());
        for (Speech speech : getSpeechList().getSpeeches())
            getCorrelatingView().add(new Label(speech.getLabelText()) {{
                GridPane.setHgrow(this, Priority.ALWAYS);
            }}, speech.getGridPaneColumn(), 0);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxmlInstance = this;

        initializeListeners();

        colorUseManager = new ColorUseManager();

        setShown(false);

        // Must be called last
        populateKeyCodeCombinationMap();

        startingColumnProperty().addListener(this::onStartingColumnChanged);
        finishingColumnProperty().addListener(this::onFinishingColumnChanged);

        getCorrelatingView().getColumnConstraints().addAll(Collections.nCopies(Speech.SPEECH_SIZE, new ColumnConstraints() {{
            setPercentWidth((100d / Speech.SPEECH_SIZE));
        }}));

        getSpeechList().selectedSpeechProperty().addListener(((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                getCorrelatingView().getNode(oldValue.getGridPaneColumn(), 0).ifPresent(node -> {
                    node.setStyle("");
                });
            }
            if (newValue != null) {
                getCorrelatingView().getNode(newValue.getGridPaneColumn(), 0).ifPresent(node -> {
                    node.setStyle("-fx-font-weight: bold;");
                });
            }
        }));

        getCorrelatingView().getChildren().addListener(Utils.generateListChangeListener(
                node -> {}, // nothing should be done on when list is added too
                node -> {
                    if (node instanceof FlowingRegion) {
                        getSelectedFlowingRegions().remove(node);
                    }
                }
        ));

        getCorrelatingView().getChildren().addListener(Utils.generateListChangeListener(
                node -> {
                    if (node instanceof DefensiveFlowingRegion) { // disregards labels at top
                        DefensiveFlowingRegion flowingRegion = (DefensiveFlowingRegion) node;
                        Speech speech = getSpeechList().getSpeech((flowingRegion));
                        getAllSpeechesAfterIncluding(speech).forEach(affectedSpeech -> affectedSpeech.setAvailableRow(affectedSpeech.getAvailableRow() + 1));
                    }
                },
                node -> {
                    /* if (node instanceof FlowingRegion) {
                        DefensiveFlowingRegion flowingRegion = (DefensiveFlowingRegion) node;
                        Speech speech = getSpeechList().getSpeech((flowingRegion));
                        speech.setAvailableRow(speech.getNextRowProperty() - 1);
                    } */
                }
        ));

        getSpeechList().setSelectedSpeech(getSpeechList().get(0).getFirst());

        implementStartEndBindings();
    }

    private List<Speech> getAllSpeechesAfterIncluding(Speech speechCompare) {
        return getSpeechList().getSpeeches().stream().filter(speech -> speech.getGridPaneColumn() >= speechCompare.getGridPaneColumn()).collect(Collectors.toList());
    }

    public ObservableList<FlowingRegion> getSelectedFlowingRegions() {
        return selectedFlowingRegions;
    }

    public void edit() {
        FlowingRegion editedFlowingRegion = getLastSelected();
        addFlowingRegionWriter(getSpeechList().getSpeech(editedFlowingRegion), false, text -> {
            editedFlowingRegion.getChildren().clear();
            editedFlowingRegion.getChildren().add(new FlowingText(text));
        }, FlowingGrid.getRowIndex(editedFlowingRegion), editedFlowingRegion.getUnformattedText());
    }

    private static FlowingGridController fxmlInstance;

    public static FlowingGridController getFXMLInstance() {
        return fxmlInstance;
    }

    private static final Logger logger = Logger.getLogger(FlowingGridController.class.getSimpleName());

    public void addCardSelectorSupport(TextArea textArea) {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.SEMICOLON) {

            }
        });
    }

    private <T extends FlowingRegion & Offensive> void delink(T offensiveFlowingRegion) {

    }

    private boolean areSameSpeech(List<FlowingRegion> flowingRegions) {
        return flowingRegions.stream()
                .allMatch(flowingRegion -> GridPane.getColumnIndex(flowingRegions.get(0)).equals(GridPane.getColumnIndex(flowingRegion)));
    }

    private void attemptMerge() {
        PFFlowing.getInstance().getActionManager().perform(new Merge(getSelectedFlowingRegions()));
    }

    public void implementListeners(FlowingRegion flowingRegion) {
        flowingRegion.setOnMousePressed(mouseEvent -> {
            handleSelection(Optional.of(flowingRegion), mouseEvent.isControlDown());
            System.out.println("Tres" + getSelectedFlowingRegions());
        });
    }

    private Map<FlowingRegion, Background> unselectedBackgrounds = new HashMap<>();

    private void removeSelectionStyling(FlowingRegion flowingRegion) {
        if (flowingRegion != null)
            flowingRegion.setBackground(unselectedBackgrounds.get(flowingRegion));
    }

    private void addSelectionStyling(FlowingRegion flowingRegion) {
        if (flowingRegion != null)
            flowingRegion.setBackground(Utils.generateBackgroundOfColor(Color.LIGHTBLUE));
    }

    public void select(FlowingRegion flowingRegion, boolean multiSelect) {
        if (!multiSelect)
            getSelectedFlowingRegions().clear();
        getSelectedFlowingRegions().add(flowingRegion);
        System.out.println("Uno" + getSelectedFlowingRegions());
    }

    public void unselect(FlowingRegion flowingRegion, boolean multiUnselect) {
        getSelectedFlowingRegions().clear();
        getSelectedFlowingRegions().remove(flowingRegion);
    }

    public static FlowingGridController myInstance;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void handleSelection(Optional<FlowingRegion> flowingRegion, boolean multiSelect) {
        if (!flowingRegion.isPresent())
            return;

        myInstance = this;

        FlowingRegion handledFlowingRegion = flowingRegion.get();

        System.out.println("Before null: " + handledFlowingRegion);

        if (getSelectedFlowingRegions().contains(handledFlowingRegion)) {
            System.out.println("first");
            unselect(handledFlowingRegion, multiSelect);
        }
        else {
            System.out.println("second");
            select(handledFlowingRegion, multiSelect);
        }

        System.out.println("Dos" + getSelectedFlowingRegions());
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
        pauseTransition.setOnFinished(actionEvent -> System.out.println(("2.5" + getSelectedFlowingRegions())));
        pauseTransition.play();
    }

    /**
     * Provides the one, main view that is represented by this controller.
     *
     * @return The one, main view correlating to the controller.
     */
    @Override
    public FlowingGrid getCorrelatingView() {
        return flowingGrid;
    }

    /**
     * Processes a {@link KeyEvent} that is newly generated upon a keyboard key press. In order to implement this listener,
     * this method fulfills the contract that being an {@link EventHandler<KeyEvent>} requires. In the main class,
     * the fxml instance of this class handles all pressed keys.
     *
     * @param keyEvent The event data that provides the key pressed, key combinations used, etc.
     */
    @Override
    public void handle(KeyEvent keyEvent) {
        for (KeyCodeCombination keyCodeCombination : getKeyCodeCombinationMap().keySet())
            if (keyCodeCombination.match(keyEvent)) {
                getKeyCodeCombinationMap().get(keyCodeCombination).run();
                System.out.println(keyCodeCombination);
                // Prevents the flowingregions selector from being selected on left arrow key
                keyEvent.consume();
            }
    }

    /**
     *
     * @param function
     * @param flowingRegion
     * @param isCtrlDown
     */
    private void handleSelection(Function<FlowingRegion, Optional<FlowingRegion>> function, FlowingRegion flowingRegion, boolean isCtrlDown) {
        handleSelection(function.apply(flowingRegion), isCtrlDown);
    }

    private void initializeListeners() {
        getSelectedFlowingRegions().addListener(Utils.generateListChangeListener(
                this::addSelectionStyling,
                this::removeSelectionStyling
        ));
    }

    public boolean isShown() {
        return isShown;
    }

    public void setShown(boolean shown) {
        isShown = shown;
    }

    boolean first = true;
    Text text;

    private class TextAreaGenerator {
        private final TextArea textArea;

        private boolean isCardSelectorShown = false;

        TextAreaGenerator() {
            textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.setFont(GlobalConfiguration.FONT);
            textArea.prefWidthProperty().bind(textArea.maxWidthProperty());
            textArea.setPrefHeight(12);


            textArea.addEventHandler(KeyEvent.KEY_TYPED, keyEvent -> {
                if (isCardSelectorShown())
                    return;

                if (textArea.getScrollTop() > 0) {
                    textArea.setPrefHeight(textArea.getPrefHeight() + 12);
                    textArea.setScrollTop(0);
                }
            });

            textArea.addEventHandler(KeyEvent.KEY_TYPED, keyEvent -> {
                if (keyEvent.getCharacter().equals(GlobalConfiguration.CARD_SELECTOR)) {
                    setCardSelectorShown(true);

                    ContextMenu contextMenu = new ContextMenu();
                    contextMenu.getItems().add(new MenuItem("Hello"));
                    Bounds textAreaBounds = textArea.localToScene(textArea.getBoundsInLocal());
                    // Max Y is used in order to have context menu under the text area
                    contextMenu.show(textArea, textAreaBounds.getMinX(), textAreaBounds.getMaxY());

                    contextMenu.getItems().forEach(menuItem -> {
                        menuItem.addEventHandler(ActionEvent.ACTION, actionEvent -> {

                            setCardSelectorShown(false);
                        });
                    });
                }
            });

            textArea.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
               if (KeyCodeCombinationUtils.BOLD.match(keyEvent)) {

               }
            });
        }

        public TextArea getTextArea() {
            return textArea;
        }

        public boolean isCardSelectorShown() {
            return isCardSelectorShown;
        }

        public void setCardSelectorShown(boolean cardSelectorShown) {
            isCardSelectorShown = cardSelectorShown;
        }
    }

    /**
     *
     */
    private SpeechList speechList;

    /**
     * Represents the keyboard combination required to submit a text area (and thus generate a flowing region).
     * This differs from the {@code ENTER} {@link KeyCode} because that is used for creating a newline within
     * the textarea, not for submission.
     */
    private static final KeyCodeCombination TEXTAREA_SUBMIT = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    /**
     *
     * @param textArea The text area that {@code postEnterAction} will receive the text from.
     * @param createNewOne Represents whether a new text area should be generated upon submission of the
     *                     current text area.
     * @param postEnterAction Represents any post-submission action that should be called upon using the text of
     *                        {@code textArea}.
     * @return An handler, upon execution, will:
     * <ol>
     *      <li>Determines whether {@code TEXTAREA_SUBMIT} matches the keyboard combination entered.</li>
     *      <li>If above is true, will feed {@code postEnterAction} with the text of {@code textArea}.</li>
     *      <li>Removes {@code textArea} from the grid (as submission has been handled).</li>
     *      <li>If {@code createNewOne} add a new text area in the subsequent position, designed for further
     *      submission by the user.</li>
     * </ol>
     */
    private EventHandler<KeyEvent> generateHandler(Speech speech, TextArea textArea, boolean createNewOne, Consumer<String> postEnterAction, int rowIndex) {
        return (KeyEvent keyEvent) -> {
            if (TEXTAREA_SUBMIT.match(keyEvent)) {
                System.out.println("matched");
                postEnterAction.accept(textArea.getText());

                getCorrelatingView().getChildren().remove(textArea);

                if (createNewOne)
                    addFlowingRegionWriter(speech, true, postEnterAction, rowIndex + 1);
            }
        };
    }

    public void addFlowingRegionWriter(Speech speech, boolean createNewOne, Consumer<String> postEnterAction, int rowIndex) {
        addFlowingRegionWriter(speech, createNewOne, postEnterAction, rowIndex, "");
    }

    /**
     * Adds a {@link TextArea} (the flowing region writer) to the flowing column. This flowing region writer
     * is designed so that on user submission, the text entered into the flowing region writer
     * would be used to create a flowing region representing what the user typed.
     */
    public void addFlowingRegionWriter(Speech speech, boolean createNewOne, Consumer<String> postEnterAction, int rowIndex, String preText) {
        TextArea textArea = new TextAreaGenerator().getTextArea();

        textArea.setText(preText);
        textArea.positionCaret(preText.length());

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, generateHandler(speech, textArea, createNewOne, postEnterAction, rowIndex));

        getCorrelatingView().add(textArea, speech.getGridPaneColumn(), rowIndex);
        textArea.requestFocus();

        FlowingGridController.getFXMLInstance().addCardSelectorSupport(textArea);
    }

    /**
     * Defaul post-enter specification for the above method
     * @param createNewOne
     */
    public void addProactiveFlowingRegionWriter(Speech speech, boolean createNewOne) {
        addFlowingRegionWriter(speech, createNewOne, text -> {
            Reasoning reasoning = new Reasoning(text);
            addDefensiveFlowingRegion(speech, new DefensiveFlowingRegion(reasoning, this));
        }, speech.getAvailableRow() + 1);
    }

    public Speech getSpeech(FlowingRegion flowingRegion) {
        return getSpeechList().getSpeeches().get(GridPane.getColumnIndex(flowingRegion));
    }

    public void addOffensiveFlowingRegion(OffensiveFlowingRegion offensiveFlowingRegion) {
        int rowIndex = GridPane.getRowIndex(offensiveFlowingRegion.getTargetRegion());
        int refColumnIndex = FlowingGrid.getColumnIndex(offensiveFlowingRegion.getTargetRegion()) + 1;
        addFlowingRegion(offensiveFlowingRegion, refColumnIndex, rowIndex);

    }

    public void addDefensiveFlowingRegion(Speech speech, DefensiveFlowingRegion defensiveRegion) {
        int rowIndex = speech.getAvailableRow() + 1;
        addFlowingRegion(defensiveRegion, speech.getGridPaneColumn(), rowIndex);
    }

    public void debug() {
        for (Speech speech : getSpeechList().getSpeeches()) {
            System.out.println(speech.getLabelText() + " Row " + speech.getAvailableRow());
        }
    }

    public void implementStartEndBindings() {

        /* Illustration Model: N_ = speech with index of _, S = proactive start index, E = proactive end index
        N0 N1 N2 N3 N4 N5 N6 N7
      0 S
      1
      2 E
      3    SE S
      4       E  SE S
      5
      6             E  SE SE SE
      7
      ... (no limit on number of rows, 7 suffices for example)

        The following facts will always be true:
        1) Subtracting the proactive start from the proactive end of a speech will yield the number of defensive flowing regions
        contained within that speech.
        2) The proactive start of the first speech is 0. This is because there is nothing to refute, hence no "reactive" flowing
        regions can occupy space before the proactive ones. In accordance with fact #1, size(children) for the first speech will
        be the proactive end.
        3) The proactive start of speech with index X is equal to 1 plus the proactive end of speech with index X - 1. This is because,
        given a end of N in the previous speech, we must allocate N slots for offensive flowing regions in order to address the N
        proactive flowing regions.
        4) If there are no proactive flowing regions in a speech (typical, but not impossible, in refutation speeches which exist primarily
        to refute defensive speeches), then the proactive start and proactive end will be equivalent.
         */
        for (int speechIndex = 1; speechIndex < SpeechList.NUMBER_OF_SUBROUNDS * 2; speechIndex++) {
            Speech leftSpeech = getSpeechList().getSpeeches().get(speechIndex - 1);
            Speech currentSpeech = getSpeechList().getSpeeches().get(speechIndex);
        }

        Speech firstSpeech = getSpeechList().get(0).getFirst();

        firstSpeech.setAvailableRow(0);
    }

    public void addExtensionFlowingRegion(ExtensionFlowingRegion extensionFlowingRegion) {
        int rowIndex = GridPane.getRowIndex(extensionFlowingRegion.getBase());
        int extColumnIndex = FlowingGrid.getColumnIndex(extensionFlowingRegion.getBase()) + 2;
        addFlowingRegion(extensionFlowingRegion, extColumnIndex, rowIndex);
    }



    public void addFlowingRegion(FlowingRegion flowingRegion, int column, int row) {
        //flowingRegion.getChildren().forEach(node -> ((FlowingText) node).setFill(speech.getColor()));

        getCorrelatingView().add(flowingRegion, column, row);

        debug();
    }

    private final IntegerProperty startingColumn = new SimpleIntegerProperty();
    private final IntegerProperty finishingColumn = new SimpleIntegerProperty();
    private final List<Node> originalChildren = new ArrayList<>();

    public List<Node> getOriginalChildren() {
        return originalChildren;
    }

    private void onStartingColumnChanged(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
        onColumnBoundsChanged(newNumber.intValue(), finishingColumn.get());
    }

    private void onFinishingColumnChanged(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
        onColumnBoundsChanged(startingColumn.get(), newNumber.intValue());
    }

    private void onColumnBoundsChanged(int newStart, int newFinish) {
        getCorrelatingView().getChildren().setAll(originalChildren.stream().filter(node -> {
            // Refers to whether the node is inside column bounds that don't wrap, such as start: 5 and finish: 7,
            // which refer to the columns of 5 6 7
            boolean insideNonWrap = newStart <= GridPane.getColumnIndex(node) && GridPane.getColumnIndex(node) <= newFinish;

            // Refers to whether the node is inside column bounds that do wrap, such as start: 7 and finish: 3,
            // which refer to the columns of 7 0 1 2 3 (wrapping around the last index, 7)
            boolean insideWrap    = newStart >= GridPane.getColumnIndex(node) && GridPane.getColumnIndex(node) >= newFinish;

            // Given that the column bounds could either wrap or not, only one of the conditions above should be true
            return insideNonWrap || insideWrap;
        }).collect(Collectors.toList()));
    }

    public void shift(int offset) {
        setStartingColumn(getStartingColumn() + offset);
        setFinishingColumn(getStartingColumn() + offset);
    }

    public void setNumberOfColumns(int numberOfColumns) {
        setFinishingColumn(getFinishingColumn() - numberOfColumns);
    }

    public void narrowBy(int reductionInNumOfColumns) {
        setNumberOfColumns(getCorrelatingView().getChildren().size() - reductionInNumOfColumns);
    }

    public void upscaleBy(int increaseInNumberOfColumns) {
        setNumberOfColumns(getCorrelatingView().getChildren().size() + increaseInNumberOfColumns);
    }

    public List<FlowingRegion> getSubFlowingRegions(FlowingRegion flowingRegion) {
        if (flowingRegion instanceof Defensive)
            return Collections.singletonList(flowingRegion);
        else
            return ListUtils.union(Collections.singletonList(flowingRegion),
                    getSubFlowingRegions(((Offensive) flowingRegion).getTargetRegion()));
    }

    public DefensiveFlowingRegion getBaseFlowingRegion(FlowingRegion flowingRegion) {
        return flowingRegion instanceof Defensive ? (DefensiveFlowingRegion) flowingRegion :
                getBaseFlowingRegion(((Offensive) flowingRegion).getTargetRegion());
    }

    public int getFinishingColumn() {
        return finishingColumn.get();
    }

    public IntegerProperty finishingColumnProperty() {
        return finishingColumn;
    }

    public int wrap(int columnNumber) {
        while (columnNumber < 0)
            columnNumber += Speech.SPEECH_SIZE;
        while (columnNumber >= Speech.SPEECH_SIZE)
            columnNumber -= Speech.SPEECH_SIZE;
        return columnNumber;
    }

    public void setFinishingColumn(int finishingColumn) {
        this.finishingColumn.set(wrap(finishingColumn));
    }

    public int getStartingColumn() {
        return startingColumn.get();
    }

    public IntegerProperty startingColumnProperty() {
        return startingColumn;
    }

    public void setStartingColumn(int startingColumn) {
        this.startingColumn.set(wrap(startingColumn));
    }

    public SpeechList getSpeechList() {
        return speechList;
    }

    public void setSpeechList(SpeechList speechList) {
        this.speechList = speechList;
    }
}