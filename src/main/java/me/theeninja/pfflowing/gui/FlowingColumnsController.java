package me.theeninja.pfflowing.gui;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Popup;
import me.theeninja.pfflowing.Configuration;
import me.theeninja.pfflowing.PFFlowing;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.Utils;
import me.theeninja.pfflowing.card.*;
import me.theeninja.pfflowing.flowing.Defensive;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.Offensive;
import me.theeninja.pfflowing.flowing.Speech;
import org.apache.commons.lang.Validate;
import org.apache.sis.math.MathFunctions;

import javax.swing.text.html.CSS;
import javax.tools.Tool;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The controller for the actual flowing area on the application. This controller
 * is responsible for managing the relations between all flowing regions that exist.
 *
 * @author TheeNinja
 */
public class FlowingColumnsController implements Initializable, SingleViewController<HBox>, EventHandler<KeyEvent> {

    /**
     * The {@link HBox} that contains eight colums (one for each speech).
     * This provides an access bridge between multiple speeches, as this
     * is the parent of all {@link VBox}es responsible for managing their
     * associated speeches.
     */
    @FXML public HBox flowingColumns;

    /**
     * A {@link Map} that provides links between a speech and its representing
     * flow column (the {@link VBox}
     */
    private final Map<Speech, VBox> speechMap = new HashMap<>();

    @FXML public VBox aff_1;
    @FXML public VBox neg_1;
    @FXML public VBox aff_2;
    @FXML public VBox neg_2;
    @FXML public VBox aff_3;
    @FXML public VBox neg_3;
    @FXML public VBox aff_4;
    @FXML public VBox neg_4;

    private final Map<Speech, Label> speechLabelMap = new HashMap<>();

    @FXML public Label aff_1_header;
    @FXML public Label neg_1_header;
    @FXML public Label aff_2_header;
    @FXML public Label neg_2_header;
    @FXML public Label aff_3_header;
    @FXML public Label neg_3_header;
    @FXML public Label aff_4_header;
    @FXML public Label neg_4_header;

    private final Map<Speech, VBox> speechContentMap = new HashMap<>();

    @FXML public VBox aff_1_content;
    @FXML public VBox neg_1_content;
    @FXML public VBox aff_2_content;
    @FXML public VBox neg_2_content;
    @FXML public VBox aff_3_content;
    @FXML public VBox neg_3_content;
    @FXML public VBox aff_4_content;
    @FXML public VBox neg_4_content;

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

    public ObservableList<FlowingRegion> getSelectedFlowingRegions() {
        return selectedFlowingRegions;
    }

    public FlowingRegion getLastSelected() {
        return lastSelected;
    }

    private void setLastSelected(FlowingRegion flowingRegion) {
        logger.log(Level.INFO,
                "Set last selected Flowing Region to {0}", flowingRegion);
        this.lastSelected = flowingRegion;
    }

    /**
     * The selected speech is important to keep track of since many actions used
     * to modify individual flowing columns require knowledge of which speech they are
     * modifying (in order to access the children, change them, etc).
     */
    private Speech selectedSpeech;

    private static FlowingColumnsController fxmlInstance;

    public static FlowingColumnsController getFXMLInstance() {
        return fxmlInstance;
    }

    /**
     * Serves as a record of all the links between two {@link FlowingRegion}s, with
     * one being an implementor {@link Offensive}, i.e, targeting another
     * {@link FlowingRegion}. A listener is implemented responsible for visually
     * informing the user of all the links throughout the flowing area (so they are
     * aware of how they have used their blocks to respond to the opponent).
     */
    ObservableList<OffensiveCard> offensiveCards = FXCollections.observableArrayList();
    ObservableList<OffensiveReasoning> offensiveReasonings = FXCollections.observableArrayList();
    ObservableList<Line> lineLinks = FXCollections.observableArrayList();

    private static final Logger logger = Logger.getLogger(FlowingColumnsController.class.getSimpleName());

    private Optional<FlowingRegion> getVerticallyRelativeFlowingRegion(FlowingRegion flowingRegion, int offset) {
        Validate.notNull(flowingRegion, "Inputted relative flowing region is null.");

        VBox contentContainer = (VBox) flowingRegion.getParent();

        int baseIndex = contentContainer.getChildren().indexOf(flowingRegion);

        // It makes more sense to the user for a positive offset to yield a higher node rather than
        // a lower node, hence the subtraction rather than the summation.
        int finalIndex = baseIndex - offset;

        if (finalIndex >= contentContainer.getChildren().size() || finalIndex < 0) {
            logger.log(Level.INFO,
                    "Base flowing region provided, considering offset, will result in illegal index.");
            return Optional.empty();
        }

        Node node =  contentContainer.getChildren().get(finalIndex);

        if (node == null) {
            logger.log(Level.INFO,
                    "Node relative to base flowing region given offset {0} is null.", offset);
            return Optional.empty();
        }

        if (!(node instanceof FlowingRegion)) {
            logger.log(Level.INFO,
                    "Node under target base region is a TextField, returning empty.");
            return Optional.empty();
        }

        return Optional.of((FlowingRegion) node);
    }

    private void addCardSelectorSupport(TextField textField) {
        textField.setOnKeyPressed(keyEvent -> {
            logger.log(Level.INFO, "TextField received code {0}", keyEvent.getCode().getName());
            if (keyEvent.getCode() == KeyCode.SEMICOLON) {

            }
        });
    }

    private Optional<FlowingRegion> getHorizontallyRelativeFlowingRegion(FlowingRegion flowingRegion, int offset) {
        Validate.notNull(flowingRegion, "Inputted relative flowing region is null.");

        VBox baseContentContainer = (VBox) flowingRegion.getParent();
        int indexInParent = baseContentContainer.getChildren().indexOf(flowingRegion);
        Speech baseSpeech = reverseMap(speechContentMap).get(baseContentContainer);

        Speech relativeSpeech;

        try {
            relativeSpeech = Speech.getRelativeSpeech(baseSpeech, offset);
        } catch (IllegalArgumentException e) {
            logger.log(Level.INFO, "Given the base flowing region and the relative offset" +
                    ", there is no relative Speech -> illegal index.");
            return Optional.empty();
        }

        System.out.println(relativeSpeech.toString());

        VBox relativeContentContainer = speechContentMap.get(relativeSpeech);

        if (relativeContentContainer.getChildren().size() == 0) {
            logger.log(Level.INFO, "While the speech is there, it has no content. Hence, no flowing region can" +
                    "be returned.");
            return Optional.empty();
        }

        /*
        By now the parent of the flowing region the user seeks relative to the parent of the
        base flowing region exists. However, the node itself may not exist at the same index,
        as the parents' children have different sizes. Therefore, we adjust the index of the node
        to be the last element in the relative parent if it exceeds or equals the children's size.
         */
        if (indexInParent >= relativeContentContainer.getChildren().size()) {
            logger.log(Level.INFO,
                    "Adjusted relative node index to meet relative parent's children's size.");
            indexInParent = relativeContentContainer.getChildren().size() - 1;
        }

        Node node = relativeContentContainer.getChildren().get(indexInParent);

        if (!(node instanceof FlowingRegion)) {
            throw new Error("Unexpected error here. Node in this case should always be a flowing region.");
        }

        return Optional.of((FlowingRegion) node);
    }

    /**
     * @param flowingRegion The flowing region to base all directions upon.
     * @return The flowing region located directly above the parameter (hence in
     * the same flowing column/speech).
     */
    private Optional<FlowingRegion> getUp(FlowingRegion flowingRegion) {
        return getVerticallyRelativeFlowingRegion(flowingRegion, 1);
    }

    private Optional<FlowingRegion> getDown(FlowingRegion flowingRegion) {
        return getVerticallyRelativeFlowingRegion(flowingRegion, -1);
    }

    private Optional<FlowingRegion> getRight(FlowingRegion flowingRegion) {
        return getHorizontallyRelativeFlowingRegion(flowingRegion, 1);
    }

    private Optional<FlowingRegion> getLeft(FlowingRegion flowingRegion) {
        return getHorizontallyRelativeFlowingRegion(flowingRegion, -1);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxmlInstance = this;

        speechMap.put(Speech.AFF_1, aff_1);
        speechMap.put(Speech.NEG_1, neg_1);
        speechMap.put(Speech.AFF_2, aff_2);
        speechMap.put(Speech.NEG_2, neg_2);
        speechMap.put(Speech.AFF_3, aff_3);
        speechMap.put(Speech.NEG_3, neg_3);
        speechMap.put(Speech.AFF_4, aff_4);
        speechMap.put(Speech.NEG_4, neg_4);

        speechLabelMap.put(Speech.AFF_1, aff_1_header);
        speechLabelMap.put(Speech.NEG_1, neg_1_header);
        speechLabelMap.put(Speech.AFF_2, aff_2_header);
        speechLabelMap.put(Speech.NEG_2, neg_2_header);
        speechLabelMap.put(Speech.AFF_3, aff_3_header);
        speechLabelMap.put(Speech.NEG_3, neg_3_header);
        speechLabelMap.put(Speech.AFF_4, aff_4_header);
        speechLabelMap.put(Speech.NEG_4, neg_4_header);

        speechContentMap.put(Speech.AFF_1, aff_1_content);
        speechContentMap.put(Speech.NEG_1, neg_1_content);
        speechContentMap.put(Speech.AFF_2, aff_2_content);
        speechContentMap.put(Speech.NEG_2, neg_2_content);
        speechContentMap.put(Speech.AFF_3, aff_3_content);
        speechContentMap.put(Speech.NEG_3, neg_3_content);
        speechContentMap.put(Speech.AFF_4, aff_4_content);
        speechContentMap.put(Speech.NEG_4, neg_4_content);

        selectedFlowingRegions.addListener(Utils.generateListChangeListener(
            this::addSelectionStyling,
            this::removeSelectionStyling
        ));

        offensiveCards.addListener(Utils.generateListChangeListener(
             this::drawArrow,
             this::removeArrow
        ));

        offensiveReasonings.addListener(Utils.generateListChangeListener(
             this::drawArrow,
             this::removeArrow
        ));

        setSelectedSpeech(Speech.AFF_1);
        addFlowingRegionWriter(getSelectedSpeech());
    }

    private <T extends FlowingRegion & Offensive> void drawArrow(T offensiveFlowingRegion) {
        System.out.println("calling draw arrow");

        List<FlowingRegion> targetFlowingRegions = offensiveFlowingRegion.getTargetRegions();

        Bounds baseBounds = offensiveFlowingRegion
                .localToScene(offensiveFlowingRegion.getBoundsInLocal());
        double baseAverageYCoordinate = (baseBounds.getMaxY() + baseBounds.getMinY() / 2);
        double baseXCoordinate = baseBounds.getMaxX();

        targetFlowingRegions.stream()
                .map(flowingRegion -> {
                    Bounds bounds = flowingRegion.localToScene(flowingRegion.getBoundsInLocal());
                    double averageYCoordinate = (bounds.getMaxY() + bounds.getMinY()) / 2;
                    double xCoordinate = bounds.getMinX();

                    return Arrays.asList(averageYCoordinate, xCoordinate);
                }) // Obtains the coordinate pair denoting the position of the flowing region
                .map(coordinatePair -> new Line(
                    baseXCoordinate,
                    baseAverageYCoordinate,
                    coordinatePair.get(0),
                    coordinatePair.get(1))
                )
                .forEach(PFFlowingApplicationController
                        .getFXMLInstance().getCorrelatingView().getChildren()::add);
    }

    private <T extends FlowingRegion & Offensive> void removeArrow(T offensiveFlowingRegion) {

    }

    private VBox getContentContainer(Speech speech) {
        return (VBox) speechMap.get(speech).getChildren().get(1);
    }

    private <K, V> Map<V, K> reverseMap(Map<K, V> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    private void merge() {
        //noinspection SuspiciousMethodCalls
        Speech speech = reverseMap(speechContentMap)
                .get(selectedFlowingRegions.get(0).getParent());
        VBox vbox = new VBox();
        selectedFlowingRegions.forEach(vbox.getChildren()::add);
        int index = speechContentMap.get(speech).getChildren().indexOf(selectedFlowingRegions.get(0));
        speechContentMap.get(speech).getChildren().add(index, vbox);
        selectedFlowingRegions.forEach(speechContentMap.get(speech).getChildren()::remove);

    }

    private TextField currentFlowingRegionWriter;

    private void addFlowingRegionWriter(Speech speech) {
        TextField textField = new TextField();
        textField.prefWidthProperty().bind(speechMap.get(speech).prefWidthProperty());
        textField.setOnAction(actionEvent -> {
            addDefensiveFlowingRegion(speech,
                    new DefensiveReasoning(textField.getText()));
            speechContentMap.get(speech).getChildren().remove(textField);
            addFlowingRegionWriter(speech);
        });
        speechContentMap.get(speech).getChildren().add(textField);
        textField.requestFocus();
        addCardSelectorSupport(textField);
        setCurrentFlowingRegionWriter(textField);
    }

    private void removeAllFlowingRegionWriters(Speech speech) {
        speechContentMap.get(speech).getChildren().removeAll(getContentContainer(speech)
                        .getChildren()
                        .stream()
                        .filter(child -> child instanceof TextField)
                        .collect(Collectors.toList()));
    }

    private void implementListeners(FlowingRegion flowingRegion) {
        flowingRegion.setOnMousePressed(mouseEvent ->
                handleSelection(flowingRegion, mouseEvent.isControlDown()));
    }

    public <T extends FlowingRegion & Offensive> void addOffensiveFlowingRegion(Speech speech, T offensiveRegion) {
        logger.log(Level.INFO, "Adding Offensive Flowing Region in {0}", speech.toString());
        if (offensiveRegion instanceof OffensiveCard) {
            offensiveCards.add((OffensiveCard) offensiveRegion);
        }
        else if (offensiveRegion instanceof OffensiveReasoning) {
            offensiveReasonings.add((OffensiveReasoning) offensiveRegion);
        }
        addFlowingRegion(speech, offensiveRegion);
    }

    public <T extends FlowingRegion & Defensive> void addDefensiveFlowingRegion(Speech speech, T defensiveRegion) {
        logger.log(Level.INFO, "Adding Defensive Flowing Region in {0}", speech.toString());
        addFlowingRegion(speech, defensiveRegion);
    }

    private <T extends FlowingRegion> void addFlowingRegion(Speech speech, T flowingRegion) {
        implementListeners(flowingRegion);
        if (flowingRegion instanceof Card) {
            Card flowingCard = (Card) flowingRegion;

            CharacterFormatting characterFormatting = new CharacterFormatting(Arrays.asList(
                    Configuration.SPOKEN
            ));

            String tooltipText = flowingCard.getCardContent().getContent(characterFormatting);
            System.out.println("Tool tip text: " + tooltipText);

            Tooltip flowingRegionTooltip = new Tooltip(tooltipText);

            characterFormatting.getCharacterStyles().stream()
                    .map(CharacterStyle::getCssClass)
                    .forEach(flowingRegionTooltip.getStyleClass()::add);

            Tooltip.install(flowingRegion, flowingRegionTooltip);
        }

        speechContentMap.get(speech).getChildren().add(flowingRegion);

    }

    private final static String SELECTION_CLASS = "selectedFlowingRegion";

    private void removeSelectionStyling(FlowingRegion flowingRegion) {
        if (flowingRegion != null) {
            logger.log(Level.INFO,
                    "Removing selection styling of {0}", flowingRegion);
            flowingRegion.setStyle("");
        }
    }

    private void addSelectionStyling(FlowingRegion flowingRegion) {
        if (flowingRegion != null) {
            logger.log(Level.INFO,
                    "Add selection styling of {0}", flowingRegion);
            flowingRegion.setStyle("-fx-font-size: 20");
        }
    }

    public void select(FlowingRegion flowingRegion, boolean isCtrlDown) {
        Validate.notNull("Attempting to select null flowing region.");

        setLastSelected(flowingRegion);

        if (!isCtrlDown) {
            selectedFlowingRegions.clear();
        }
        selectedFlowingRegions.add(getLastSelected());
    }

    public void unselect(FlowingRegion flowingRegion, boolean isCtrlDown) {
        Validate.notNull("Attempting to select null flowing region.");

        setLastSelected(flowingRegion);

        selectedFlowingRegions.clear();
        selectedFlowingRegions.add(getLastSelected());
    }

    private void handleSelection(FlowingRegion flowingRegion, boolean isCtrlDown) {
        if (selectedFlowingRegions.contains(flowingRegion)){
            logger.log(Level.INFO,
                    "Unselecting flowing region {0}.", flowingRegion);
            unselect(flowingRegion, isCtrlDown);
        }
        else {
            logger.log(Level.INFO,
                    "Adding {0} to selected flowing regions", flowingRegion);
            select(flowingRegion, isCtrlDown);
        }
    }

    @Override
    public HBox getCorrelatingView() {
        return flowingColumns;
    }

    @Override
    public void handle(KeyEvent keyEvent) {
        logger.log(Level.INFO, "{0} key pressed", keyEvent.getCode().getName());
        KeyCode key = keyEvent.getCode();

        /*
          Handles the following keys:

          Alt + Up Arrow, Alt + Left Arrow, Alt + Down Arrow, Alt + Right Arrow
          (All with the optional Control modifier to preserve previous selections)
         */
        if (key.isArrowKey()) {
            // Declared as Optional since the Flowing Region may be on the border
            // and there may be no other FlowingRegion in the direction specified.
            Optional<FlowingRegion> selectedFlowingRegion = Optional.empty();

            // The user must select a FlowingRegion with the mouse before
            // they utlize the arrows.
            if (lastSelected == null)
                return;

            switch (keyEvent.getCode()) {
                case UP: {
                    selectedFlowingRegion = getUp(lastSelected);
                    break;
                }
                case DOWN: {
                    selectedFlowingRegion = getDown(lastSelected);
                    break;
                }
                case LEFT: {
                    selectedFlowingRegion = getLeft(lastSelected);
                    break;
                }
                case RIGHT: {
                    selectedFlowingRegion = getRight(lastSelected);
                    break;
                }
            }

            // If getUp, getDown, etc. has no FlowingRegion to return (as it is
            // on the edge), no action is taken.
            if (!selectedFlowingRegion.isPresent()) {
                logger.log(Level.INFO,
                        "No flowing region present in {0} direction.",
                        keyEvent.getCode());
                return;
            }

            // It has been confirmed that there is a newly selected FlowingRegion,
            // hence update the last selected FlowingRegion.
            setLastSelected(selectedFlowingRegion.get());

            handleSelection(selectedFlowingRegion.get(), keyEvent.isControlDown());
        }

        else if (keyEvent.isControlDown())
            switch (keyEvent.getCode()) {
                case M: merge();
                case N: {
                    removeAllFlowingRegionWriters(getSelectedSpeech());
                    setSelectedSpeech(Speech.getRelativeSpeech(getSelectedSpeech(), 1));
                    addFlowingRegionWriter(getSelectedSpeech());
                }
                case R: {
                    CardSelectorController.getFXMLInstance().getCorrelatingView().requestFocus();
                    CardSelectorController.getFXMLInstance().addCardSelectionListener();
                }
            }

        // Remove focus from the textfield and rewire the focus to the general pane
        if (key == KeyCode.ESCAPE) {
            getCorrelatingView().requestFocus();
        }
    }

    private Speech getSelectedSpeech() {
        return selectedSpeech;
    }

    private final static String SPEECH_SELECTION_CLASS = "speechSelected";

    private void setSelectedSpeech(Speech selectedSpeech) {

        Speech lastSelectedSpeech = this.selectedSpeech;

        if (lastSelectedSpeech != null)
            speechLabelMap.get(lastSelectedSpeech).getStyleClass()
                    .remove(SPEECH_SELECTION_CLASS);

        this.selectedSpeech = selectedSpeech;
        speechLabelMap.get(selectedSpeech).getStyleClass()
                .add(SPEECH_SELECTION_CLASS);
    }

    public TextField getCurrentFlowingRegionWriter() {
        return currentFlowingRegionWriter;
    }

    public void setCurrentFlowingRegionWriter(TextField currentFlowingRegionWriter) {
        this.currentFlowingRegionWriter = currentFlowingRegionWriter;
    }

    public void generateLineLinksListener() {
        BorderPane parent = PFFlowingApplicationController
                .getFXMLInstance().getCorrelatingView();

        lineLinks.addListener(Utils.generateListChangeListener(
                parent.getChildren()::add,
                parent.getChildren()::remove
        ));
    }
}