package me.theeninja.pfflowing.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import me.theeninja.pfflowing.PFFlowing;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.utils.Utils;
import me.theeninja.pfflowing.flowingregions.OffensiveCard;
import me.theeninja.pfflowing.flowingregions.OffensiveReasoning;
import me.theeninja.pfflowing.utils.Pair;

import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static me.theeninja.pfflowing.gui.KeyCodeCombinationUtils.*;

/**
 * The controller for the actual flowing area on the application. This controller
 * is responsible for managing the relations between all flowing regions that exist.
 *
 * @author TheeNinja
 */
public class FlowingColumnsController implements Initializable, SingleViewController<FlowingColumns>, EventHandler<KeyEvent> {

    /**
     * The {@link HBox} that contains eight colums (one for each speech).
     * This provides an access bridge between multiple speeches, as this
     * is the parent of all {@link VBox}es responsible for managing their
     * associated speeches.
     */
    @FXML public FlowingColumns flowingColumns;

    /**
     * The an instance of {@link ColorUseManager} that manages what {@link Pair}
     * of a {@link Color} and {@link Background} is used for each relationship between
     * a {@link List<FlowingRegion>} and the associated offensive {@link FlowingRegion}
     */
    private ColorUseManager colorUseManager;

    /**
     *
     */
    private SpeechListManager speechListManager;

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

    /**
     * The last selected flowing region will always be the last element of
     * {@code selectedFlowingRegions}. As for why it is useful to keep track of the
     * last selected {@link FlowingRegion}, the arrow functions used to navigate
     * through the entire flowing area and select multiple things at once require
     * knowledge of the last selected {@link FlowingRegion}.
     */
    private FlowingRegion lastSelected;

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

    private void populateKeyCodeCombinationMap() {
        keyCodeCombinationMap.put(REFUTE, this::refute);
        keyCodeCombinationMap.put(EXTEND, this::extend);
        keyCodeCombinationMap.put(NARROW_BY_1, () -> {
            System.out.println("DADAD");
            getCorrelatingView().getSelectedDisplayShifter().narrowBy(1);
        });
        keyCodeCombinationMap.put(EDIT, this::edit);
        keyCodeCombinationMap.put(UPSCALE_BY_1, () -> getCorrelatingView().getSelectedDisplayShifter().upscaleBy(1));
        keyCodeCombinationMap.put(SHIFT_DISPLAY_RIGHT, () -> getCorrelatingView().getSelectedDisplayShifter().shift(1));
        keyCodeCombinationMap.put(SHIFT_DISPLAY_LEFT, () -> getCorrelatingView().getSelectedDisplayShifter().shift(-1));
        keyCodeCombinationMap.put(SELECT_LEFT_ONLY, () -> handleSelection(this::getLeft, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_RIGHT_ONLY, () -> handleSelection(this::getRight, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_DOWN_ONLY, () -> handleSelection(this::getDown, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_UP_ONLY, () -> handleSelection(this::getUp, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_LEFT_TOO, () -> handleSelection(this::getLeft, getLastSelected(), true));
        keyCodeCombinationMap.put(SELECT_RIGHT_TOO, () -> handleSelection(this::getRight, getLastSelected(), true));
        keyCodeCombinationMap.put(SELECT_DOWN_TOO, () -> handleSelection(this::getDown, getLastSelected(), true));
        keyCodeCombinationMap.put(SELECT_UP_TOO, () -> handleSelection(this::getUp, getLastSelected(), true));
        keyCodeCombinationMap.put(UNFOCUS, () -> flowingColumns.requestFocus());
        keyCodeCombinationMap.put(SWITCH_SPEECHLIST, () -> getSpeechListManager().switchSelectedSpeechMap());
        keyCodeCombinationMap.put(SELECT_RIGHT_SPEECH, () -> speechListManager.getSelectedSpeechList().selectSpeech(1));
        keyCodeCombinationMap.put(SELECT_LEFT_SPEECH, () -> speechListManager.getSelectedSpeechList().selectSpeech(-1));
        keyCodeCombinationMap.put(WRITE, () -> {
           speechListManager.getSelectedSpeechList().getSelectedSpeech().getBinded().addFlowingRegionWriter(false);
        });
        keyCodeCombinationMap.put(TOGGLE_FULLSCREEN, this::toggleFullscreen);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxmlInstance = this;

        initializeListeners();

        colorUseManager = new ColorUseManager();
        speechListManager = new SpeechListManager(getCorrelatingView());

        Bindable.bind(getSpeechListManager(), getCorrelatingView());

        // Set up the flowing pane for aff 1 speech
        getSpeechListManager().selectAffSpeechMap();
        getCorrelatingView().initializeDisplayShifters();
        getCorrelatingView().setSelectedDisplayShifter(getCorrelatingView().getAffDisplayShifter());

        // To allow spacing for the arrows
        // getCorrelatingView().setSpacing(Configuration.SPEECH_SEPERATION);

        // Must be called last
        populateKeyCodeCombinationMap();
    }

    public ObservableList<FlowingRegion> getSelectedFlowingRegions() {
        return selectedFlowingRegions;
    }

    public void edit() {
        FlowingRegion editedFlowingRegion = getLastSelected();
        TextField textField = new TextField(editedFlowingRegion.getText());
        textField.positionCaret(textField.getText().toCharArray().length);
        PFFlowingApplicationController.getFXMLInstance().getCorrelatingView().setBottom(textField);
        textField.requestFocus();

        textField.setOnAction(actionEvent -> {
            editedFlowingRegion.setText(textField.getText());
            PFFlowingApplicationController.getFXMLInstance().getCorrelatingView().setBottom(null);
            FlowingColumnsController.getFXMLInstance().getCorrelatingView().requestFocus();
        });
    }

    public FlowingRegion getLastSelected() {
        return lastSelected;
    }

    private void setLastSelected(FlowingRegion flowingRegion) {
        logger.log(Level.INFO,
                "Set last selected Flowing Region to {0}", flowingRegion);
        this.lastSelected = flowingRegion;
    }

    private static FlowingColumnsController fxmlInstance;

    public static FlowingColumnsController getFXMLInstance() {
        return fxmlInstance;
    }

    /**
     * Serves a list of all offensive flowing regions currently on the flowing pane. It is observable,
     * hence when an offensive flowing region is added, {@code colorUseManager} provides a {@link Pair}
     * of a {@link Color} and {@link Background} use to illustrate the relationship between the
     * offensive flowingregions and its targeted flowing regions.
     */
    ObservableList<OffensiveFlowingRegion> offensiveFlowingRegions = FXCollections.observableArrayList();

    @Deprecated
    ObservableList<Line> lineLinks = FXCollections.observableArrayList();

    private static final Logger logger = Logger.getLogger(FlowingColumnsController.class.getSimpleName());

    public void addCardSelectorSupport(TextArea textArea) {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.SEMICOLON) {

            }
        });
    }

    /**
     * Styles the given flowing region with the color and background of the given {@link Pair}. In addition,
     * the link element is given a font weight of bold in order to make its indivudal text color more prominent
     * and distinguishable from link elements of other links.
     *
     * @param flowingRegion The flowing region to style with the provided pair's color and background.
     * @param pair The tuple consisting of a {@link Color} and {@link Background} used to style the
     *             provided flowing region.
     */
    private void styleLinkElement(FlowingRegion flowingRegion, Pair<Color, Background> pair) {
        flowingRegion.setTextFill(pair.getFirst());
        flowingRegion.setBackground(pair.getSecond());
        flowingRegion.setStyle("-fx-font-weight: bold");
    }

    /**
     * When called, a visual link will be constructed between the provided offensive flowing region
     * and the flowing regions that it targets. The visual link is displayed by assigning a text color
     * and background color to each of the components involved in the link. The text color and background color
     * are distinct for each link.
     *
     * @param offensiveFlowingRegion The offensive flowing region subject to the requested link
     *                               with its targeted flowing regions.
     */
    private void link(OffensiveFlowingRegion offensiveFlowingRegion) {
        System.out.println("calling link");

        FlowingRegion targetFlowingRegion = offensiveFlowingRegion.getTargetRegion();

        if (colorUseManager.hasNext()) {
            Pair<Color, Background> pair = colorUseManager.next();
            Color color = pair.getFirst();
            Background background = pair.getSecond();

            styleLinkElement(targetFlowingRegion, pair);
            unselectedBackgrounds.put(targetFlowingRegion, targetFlowingRegion.getBackground());


            // The offensive flowing region itself must be styled in addition to its targets
            styleLinkElement(offensiveFlowingRegion, pair);
            unselectedBackgrounds.put(offensiveFlowingRegion, offensiveFlowingRegion.getBackground());
        }
        else
            System.out.println("Ran out of colors");
    }


    private <T extends FlowingRegion & Offensive> void delink(T offensiveFlowingRegion) {

    }

    private <K, V> Map<V, K> reverseMap(Map<K, V> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    private boolean areSameSpeech(List<FlowingRegion> flowingRegions) {
        FlowingColumn firstFlowingColumn = ((FlowingColumn) flowingRegions.get(0).getParent());
        Speech firstElementSpeech =
            getSpeechListManager().getSelectedSpeechList().findFirstSpeech(speech -> speech.getBinded() == firstFlowingColumn).orElse(null);
        return flowingRegions.stream().allMatch(flowingRegion ->
            ((FlowingColumn) flowingRegion.getParent().getParent()).getBinded() == firstElementSpeech);
    }

    private void merge() {
        if (areSameSpeech(getSelectedFlowingRegions())) {
            VBox baseContainer = (VBox) getSelectedFlowingRegions().get(0).getParent();
            System.out.println("1st: " + getSelectedFlowingRegions().get(0));
            System.out.println(baseContainer);
            StringBuilder labelTexts = new StringBuilder();
            for (FlowingRegion flowingRegion : getSelectedFlowingRegions()) {
                labelTexts.append(flowingRegion.getText());
                if (!Utils.isLastElement(getSelectedFlowingRegions(), flowingRegion)) {
                    labelTexts.append(System.lineSeparator());
                }
            }
            getSelectedFlowingRegions().get(0).setText(labelTexts.toString());

            baseContainer.getChildren().removeAll(getSelectedFlowingRegions()
                    .stream()
                    .filter(element -> element != getSelectedFlowingRegions().get(0)).collect(Collectors.toList()));

            getSelectedFlowingRegions().removeAll(selectedFlowingRegions
                    .stream()
                    .filter(element -> element != selectedFlowingRegions.get(0)).collect(Collectors.toList()));

            setLastSelected(selectedFlowingRegions.get(0));
        }
        else {
            System.out.println("not same speech");
        }
    }

    public void implementListeners(FlowingRegion flowingRegion) {
        System.out.println("called dude");

        flowingRegion.setOnMousePressed(mouseEvent ->
                handleSelection(Optional.of(flowingRegion), mouseEvent.isControlDown()));
    }

    private Map<FlowingRegion, Background> unselectedBackgrounds = new HashMap<>();

    private void removeSelectionStyling(FlowingRegion flowingRegion) {
        if (flowingRegion != null) {
            logger.log(Level.INFO,
                    "Removing selection styling of {0}", flowingRegion);
            flowingRegion.setBackground(unselectedBackgrounds.get(flowingRegion));
        }
    }

    private void addSelectionStyling(FlowingRegion flowingRegion) {
        if (flowingRegion != null) {
            logger.log(Level.INFO,
                    "Add selection styling of {0}", flowingRegion);
            flowingRegion.setBackground(Utils.generateBackgroundOfColor(Color.LIGHTBLUE));
        }
    }

    public void select(FlowingRegion flowingRegion, boolean multiSelect) {
        setLastSelected(flowingRegion);

        if (!multiSelect)
            getSelectedFlowingRegions().clear();
        getSelectedFlowingRegions().add(getLastSelected());
    }

    public void unselect(FlowingRegion flowingRegion, boolean multiUnselect) {
        setLastSelected(flowingRegion);

        getSelectedFlowingRegions().clear();
        getSelectedFlowingRegions().add(getLastSelected());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void handleSelection(Optional<FlowingRegion> flowingRegion, boolean multiSelect) {
        System.out.println("called dude");
        if (!flowingRegion.isPresent())
            return;

        FlowingRegion handledFlowingRegion = flowingRegion.get();

        if (getSelectedFlowingRegions().contains(handledFlowingRegion))
            unselect(handledFlowingRegion, multiSelect);
        else
            select(handledFlowingRegion, multiSelect);
    }

    /**
     * Provides the one, main view that is represented by this controller.
     *
     * @return The one, main view correlating to the controller.
     */
    @Override
    public FlowingColumns getCorrelatingView() {
        return flowingColumns;
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
     * Refutes all selected nodes in a position relative to the last selected node. This is done by:
     * 1) constructing a flowing region writer that, when submitted, yields an offensive flowing region
     * 2) constructing a visual link between the newly created offensive flowing region and the selected nodes
     */
    private void refute() {
        if (getSelectedFlowingRegions().size() > 1)
            return;

        Speech speech = getLastSelected().getFlowingColumn().getBinded();
        List<Speech> speechListSpeeches = getSpeechListManager().getSpeechList(speech).getSpeeches();

        // Utils.getRelativeElement(...) will wrap around, yet you cannot refute AT-Neg4 or AT-Aff4 CardContent
        if (Utils.isLastElement(speechListSpeeches, speech))
            return;

        FlowingColumn rightFlowingColumn = Utils.getRelativeElement(speechListSpeeches, speech, 1).getBinded();
        rightFlowingColumn.addFlowingRegionWriter(false, true, text -> {
            OffensiveReasoning offensiveReasoning = new OffensiveReasoning(text, speech.getSide(), speech.getSide().getOpposite(), getLastSelected());
            rightFlowingColumn.addOffensiveFlowingRegion(offensiveReasoning);
        });
    }

    private void extend() {
        if (!areSameSpeech(getSelectedFlowingRegions()))
            return;
        for (FlowingRegion flowingRegion : getSelectedFlowingRegions()) {

        }
    }

    private void organize() {
        Speech speech = getSpeechListManager().getSelectedSpeechList().getSelectedSpeech();
        List<DefensiveFlowingRegion> base = speech.getBinded().getContentContainer().getBaseContent();
        List<OffensiveFlowingRegion> ref = speech.getBinded().getContentContainer().getRefContent();

        VBox vbox = new VBox();
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

    public void generateLineLinksListener() {
        BorderPane parent = PFFlowingApplicationController
                .getFXMLInstance().getCorrelatingView();

        lineLinks.addListener(Utils.generateListChangeListener(
            parent.getChildren()::add,
            parent.getChildren()::remove
        ));
    }

    private void initializeListeners() {
        getSelectedFlowingRegions().addListener(Utils.generateListChangeListener(
            this::addSelectionStyling,
            this::removeSelectionStyling
        ));

        offensiveFlowingRegions.addListener(Utils.generateListChangeListener(this::link, this::delink));
    }

    //// Family of functions used for getting relative flowing regions through provided base flowing region

    // Internal functions

    /**
     *
     * @param flowingRegion The flowing region to base all directions upon.
     * @param offset How far the relative flowing region is in the up/down direction (+ -> up, - -> down)
     * @return Thee flowing region assuming the offset is within the currently utilized part of the flowing pane,
     * otherwise an empty {@link Optional}
     */
    private Optional<FlowingRegion> getVerticallyRelativeFlowingRegion(FlowingRegion flowingRegion, int offset) {
        ContentContainer contentContainer = (ContentContainer) flowingRegion.getParent();

        System.out.println(flowingRegion.getParent());

        int baseIndex = contentContainer.getChildren().indexOf(flowingRegion);

        // It makes more sense to the user for a positive offset to yield a higher node rather than
        // a lower node, hence the subtraction rather than the addition.
        int finalIndex = baseIndex - offset;

        if (finalIndex >= contentContainer.getChildren().size() || finalIndex < 0)
            return Optional.empty();

        Node node =  contentContainer.getChildren().get(finalIndex);

        if (node == null)
            return Optional.empty();

        if (!(node instanceof FlowingRegion))
            return Optional.empty();

        return Optional.of((FlowingRegion) node);
    }

    /**
     * @param flowingRegion The flowing region to base all directions upon.
     * @param offset How far the relative flowing region is in the right/left direction (+ -> right, - -> left)
     * @return Thee flowing region assuming the offset is within the currently utilized part of the flowing pane,
     * otherwise an empty {@link Optional}
     */
    private Optional<FlowingRegion> getHorizontallyRelativeFlowingRegion(FlowingRegion flowingRegion, int offset) {
        ContentContainer baseContentContainer = flowingRegion.getContainer();
        int indexInParent = baseContentContainer.getChildren().indexOf(flowingRegion);
        Speech baseSpeech = baseContentContainer.getFlowingColumn().getBinded();

        Speech relativeSpeech;

        try {
            relativeSpeech = Utils.getRelativeElement(getSpeechListManager().getSelectedSpeechList().getSpeeches(), baseSpeech, offset);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        ContentContainer relativeContentContainer = relativeSpeech.getBinded().getContentContainer();

        if (relativeContentContainer.getChildren().size() == 0)
            return Optional.empty();

        /*
        By now the parent of the flowing region the user seeks relative to the parent of the
        base flowing region exists. However, the node itself may not exist at the same index,
        as the parents' children have different sizes. Therefore, we adjust the index of the node
        to be the last element in the relative parent if it exceeds or equals the children's size.
         */
        if (indexInParent >= relativeContentContainer.getChildren().size())
            indexInParent  = relativeContentContainer.getChildren().size() - 1;

        Node node = relativeContentContainer.getChildren().get(indexInParent);

        if (!(node instanceof FlowingRegion))
            throw new Error("Unexpected error here. Node in this case should always be a flowing region.");

        return Optional.of((FlowingRegion) node);
    }

    // External functions

    /**
     * @param flowingRegion The flowing region to base all directions upon.
     * @return The flowing region located directly above the parameter (hence in
     * the same flowing column/speech).
     */
    private Optional<FlowingRegion> getUp(FlowingRegion flowingRegion) {
        return getVerticallyRelativeFlowingRegion(flowingRegion, 1);
    }

    /**
     * @param flowingRegion The flowing region to base all directions upon.
     * @return The flowing region located directly below the parameter (hence in
     * the same flowing column/speech).
     */
    private Optional<FlowingRegion> getDown(FlowingRegion flowingRegion) {
        return getVerticallyRelativeFlowingRegion(flowingRegion, -1);
    }

    /**
     * @param flowingRegion The flowing region to base all directions upon.
     * @return The flowing region located directly to the right of the parameter (hence in
     * the right flowing column/speech). If the base flowing region is in the right-most column,
     * an empty {@link Optional} is returned.
     */
    private Optional<FlowingRegion> getRight(FlowingRegion flowingRegion) {
        return getHorizontallyRelativeFlowingRegion(flowingRegion, 1);
    }

    /**
     * @param flowingRegion The flowing region to base all directions upon.
     * @return The flowing region located directly to the left of the parameter (hence in
     * the left flowing column/speech). If the base flowing region is in the left-most column,
     * an empty {@link Optional} is returned.
     */
    private Optional<FlowingRegion> getLeft(FlowingRegion flowingRegion) {
        return getHorizontallyRelativeFlowingRegion(flowingRegion, -1);
    }

    /**
     * @return The speech list manager that houses essential data such as selected speech,
     * selected speech list, etc.
     */
    public SpeechListManager getSpeechListManager() {
        return speechListManager;
    }
}