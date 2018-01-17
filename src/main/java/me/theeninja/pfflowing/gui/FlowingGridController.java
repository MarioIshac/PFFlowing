package me.theeninja.pfflowing.gui;

import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.util.Duration;
import me.theeninja.pfflowing.PFFlowing;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.configuration.Configuration;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.flowingregions.*;
import me.theeninja.pfflowing.utils.Utils;
import me.theeninja.pfflowing.utils.Pair;
import org.apache.commons.collections4.ListUtils;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
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
public class FlowingGridController implements Initializable, SingleViewController<FlowingGrid>, EventHandler<KeyEvent> {

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
        keyCodeCombinationMap.put(MERGE, this::merge);
        keyCodeCombinationMap.put(REFUTE, this::refute);
        keyCodeCombinationMap.put(EXTEND, this::extend);
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
        keyCodeCombinationMap.put(WRITE, () -> addProactiveFlowingRegionWriter(getSpeechList().getSelectedSpeech(), false));
        keyCodeCombinationMap.put(TOGGLE_FULLSCREEN, this::toggleFullscreen);
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
            Label oldSpeechHeader = (Label) getCorrelatingView().getNode(oldValue.getGridPaneColumn(), 0).get();
            oldSpeechHeader.setStyle("");
            Label newSpeechHeader = (Label) getCorrelatingView().getNode(newValue.getGridPaneColumn(), 0).get();
            newSpeechHeader.setStyle("-fx-font-weight: bold;");
        }));
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
            FlowingGridController.getFXMLInstance().getCorrelatingView().requestFocus();
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

    private static FlowingGridController fxmlInstance;

    public static FlowingGridController getFXMLInstance() {
        return fxmlInstance;
    }

    /**
     * Serves a list of all offensive flowing regions currently on the flowing pane. It is observable,
     * hence when an offensive flowing region is added, {@code colorUseManager} provides a {@link Pair}
     * of a {@link Color} and {@link Background} use to illustrate the relationship between the
     * offensive flowingregions and its targeted flowing regions.
     */
    private ObservableList<OffensiveFlowingRegion> offensiveFlowingRegions = FXCollections.observableArrayList();

    private static final Logger logger = Logger.getLogger(FlowingGridController.class.getSimpleName());

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

    private boolean areSameSpeech(List<FlowingRegion> flowingRegions) {
        return flowingRegions.stream()
                .allMatch(flowingRegion -> GridPane.getColumnIndex(flowingRegions.get(0)).equals(GridPane.getColumnIndex(flowingRegion)));
    }

    private void merge() {
        if (areSameSpeech(getSelectedFlowingRegions())) {
            Speech speech = getSpeechList().getSpeech(getLastSelected());

            // Ensures that the first flowing region is the top-most, which allows the later algorithm to function correctly
            getSelectedFlowingRegions().sort(Comparator.comparingInt(FlowingGrid::getRowIndex));

            FlowingRegion firstFlowingRegion = getSelectedFlowingRegions().get(0);
            StringBuilder labelDefenceTexts = new StringBuilder();
            StringBuilder labelOffenceTexts = new StringBuilder();
            StringBuilder labelExtensionTexts = new StringBuilder();

            List<FlowingRegion> flowingRegionsToRemove = new ArrayList<>();

            // There are 3 merging tasks: Merge defensive flowing regions, merge their respective refutations, and merge their respective extensions
            for (FlowingRegion flowingRegion : getSelectedFlowingRegions()) {
                // Task 1: Merge defensive flowing region
                labelDefenceTexts.append(flowingRegion.getText());
                if (flowingRegion != firstFlowingRegion)
                    flowingRegionsToRemove.add(flowingRegion);

                /* // Note that only one of possibleOffender or possibleExtension will be present, not both
                // Task 2: Merge offender, if it is existent
                Optional<OffensiveFlowingRegion> possibleOffender = getSpeechList().getOffendor(flowingRegion);
                possibleOffender.ifPresent(offender -> {
                    labelOffenceTexts.append(offender.getText());
                    if (firstOffender.isPresent()) {

                    }
                    if (flowingRegion != firstFlowingRegion)
                        flowingGrid.getChildren().add(offender);
                });

                // Task 3: Merge extension, if it is existent
                Optional<ExtensionFlowingRegion> possibleExtension = getSpeechList().getExtension(flowingRegion);
                possibleExtension.ifPresent(extension -> labelExtensionTexts.append(extension.getText())); */
            }
            firstFlowingRegion.setText(labelDefenceTexts.toString());

            flowingGrid.getChildren().removeAll(flowingRegionsToRemove);

            // this ensures that within the memory, the old flowing regions are no longer stored as selected
            getSelectedFlowingRegions().clear();

            setLastSelected(firstFlowingRegion);
        }
    }

    public void implementListeners(FlowingRegion flowingRegion) {
        flowingRegion.setOnMousePressed(mouseEvent -> {
            handleSelection(Optional.of(flowingRegion), mouseEvent.isControlDown());
            System.out.println("clicked");
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
     * Refutes all selected nodes in a position relative to the last selected node. This is done by:
     * 1) constructing a flowing region writer that, when submitted, yields an offensive flowing region
     * 2) constructing a visual link between the newly created offensive flowing region and the selected nodes
     */
    private void refute() {
        if (getSelectedFlowingRegions().size() > 1)
            return;

        Speech speech = getSpeech(getLastSelected());
        List<Speech> speechListSpeeches = getSpeechList().getSpeeches();

        // Utils.getRelativeElement(...) will wrap around, yet you cannot refute AT-Neg4 or AT-Aff4 CardContent
        if (Utils.isLastElement(speechListSpeeches, speech))
            return;

        Speech rightSpeech = Utils.getRelativeElement(speechListSpeeches, speech, 1);
        addProactiveFlowingRegionWriter(rightSpeech, false, text -> {
            OffensiveReasoning offensiveReasoning = new OffensiveReasoning(text, speech.getSide(), speech.getSide().getOpposite(), getLastSelected());
            addOffensiveFlowingRegion(rightSpeech, offensiveReasoning);
        }, FlowingGrid.getRowIndex(getLastSelected()));
    }

    private void extend() {
        if (!areSameSpeech(getSelectedFlowingRegions()))
            return;

        Speech speech = getSpeechList().getSpeech(getLastSelected()); // speech guaranteed to be the same for all selected

        for (FlowingRegion flowingRegion : getSelectedFlowingRegions()) {
            ExtensionFlowingRegion extension = new ExtensionFlowingRegion(flowingRegion.getText(), speech.getSide(), flowingRegion);
            addExtensionFlowingRegion(Utils.getRelativeElement(getSpeechList().getSpeeches(), speech, 2), extension);
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

        offensiveFlowingRegions.addListener(Utils.generateListChangeListener(this::link, this::delink));
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
        Text textHolder = new Text();

        private final TextArea textArea;

        TextAreaGenerator() {
            textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.setFont(Configuration.FONT);
            textArea.prefWidthProperty().bind(textArea.maxWidthProperty());
            textArea.setPrefHeight(12);

            textArea.addEventHandler(KeyEvent.KEY_TYPED, keyEvent -> {
                if (textArea.getScrollTop() > 0) {
                    textArea.setPrefHeight(textArea.getPrefHeight() + 12);
                    textArea.setScrollTop(0);
                }
            });
        }

        public TextArea getTextArea() {
            return textArea;
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
                postEnterAction.accept(textArea.getText());

                getCorrelatingView().getChildren().remove(textArea);

                if (createNewOne)
                    addProactiveFlowingRegionWriter(speech, true, postEnterAction, rowIndex);
            }
        };
    }

    /**
     * Adds a {@link TextArea} (the flowing region writer) to the flowing column. This flowing region writer
     * is designed so that on user submission, the text entered into the flowing region writer
     * would be used to create a flowing region representing what the user typed.
     */
    public void addProactiveFlowingRegionWriter(Speech speech, boolean createNewOne, Consumer<String> postEnterAction, int rowIndex) {
        TextArea textArea = new TextAreaGenerator().getTextArea();

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, generateHandler(speech, textArea, createNewOne, postEnterAction, rowIndex));

        System.out.println((speech.getGridPaneColumn() + " " + rowIndex));

        getCorrelatingView().add(textArea, speech.getGridPaneColumn(), rowIndex);
        textArea.requestFocus();

        FlowingGridController.getFXMLInstance().addCardSelectorSupport(textArea);
    }

    /**
     * Defaul post-enter specification for the above method
     * @param createNewOne
     */
    public void addProactiveFlowingRegionWriter(Speech speech, boolean createNewOne) {
        addProactiveFlowingRegionWriter(speech, createNewOne, text -> {
            DefensiveReasoning defensiveReasoning = new DefensiveReasoning(text);
            addDefensiveFlowingRegion(speech, defensiveReasoning);
        }, speech.getNextAvailableRow());
    }

    /**
     * @param duration The duration of the pause, before {@code consumer} is executed.
     * @param consumer The event handler to be executed after the duration is up.
     * @return Upon PauseTransition#play(), the timer is started until the duration is met, in which
     * {@code consumer} is executed.
     */
    private PauseTransition generatePauseTransition(Duration duration, EventHandler<ActionEvent> consumer) {
        PauseTransition pauseTransition = new PauseTransition(duration);
        pauseTransition.setOnFinished(consumer);
        return pauseTransition;
    }

    public Speech getSpeech(FlowingRegion flowingRegion) {
        return getSpeechList().getSpeeches().get(GridPane.getColumnIndex(flowingRegion));
    }

    public void addOffensiveFlowingRegion(Speech speech, OffensiveFlowingRegion offensiveFlowingRegion) {
        int rowIndex = GridPane.getRowIndex(offensiveFlowingRegion.getTargetRegion());
        addFlowingRegion(speech, offensiveFlowingRegion, rowIndex);
        speech.getChildren().put(rowIndex, offensiveFlowingRegion);

    }

    public void addDefensiveFlowingRegion(Speech speech, DefensiveFlowingRegion defensiveRegion) {
        int rowIndex = speech.getNextAvailableRow();
        addFlowingRegion(speech, defensiveRegion, rowIndex);
        speech.getChildren().put(rowIndex, defensiveRegion);
        if (getSpeechList().get(0).getFirst() == speech) { // is the first speech
            // the first speech is the speech whose proactive end we directly modify
            speech.setProactiveEnd(speech.getProactiveEnd() + 1);
        }
    }

    public void addExtensionFlowingRegion(Speech speech, ExtensionFlowingRegion extensionFlowingRegion) {
        int rowIndex = GridPane.getRowIndex(extensionFlowingRegion.getBase());
        addFlowingRegion(speech, extensionFlowingRegion, rowIndex);
        speech.getChildren().put(rowIndex, extensionFlowingRegion);
    }



    public void addFlowingRegion(Speech speech, FlowingRegion flowingRegion, int row) {
        System.out.println("called ad");

        implementListeners(flowingRegion);

        // Tooltip handler
        if (flowingRegion instanceof Card) {
            Card flowingCard = (Card) flowingRegion;

            CharacterFormatting characterFormatting = new CharacterFormatting(Arrays.asList(
                    Configuration.SPOKEN
            ));

            String tooltipText = flowingCard.getCardContent().getContent(characterFormatting);

            Tooltip flowingRegionTooltip = new Tooltip(tooltipText);

            characterFormatting.getCharacterStyles().stream()
                    .map(CharacterStyle::getCssClass)
                    .forEach(flowingRegionTooltip.getStyleClass()::add);

            Tooltip.install(flowingRegion, flowingRegionTooltip);
        }

        flowingRegion.setWrapText(true);
        flowingRegion.setTextFill(speech.getColor());

        getCorrelatingView().add(flowingRegion, speech.getGridPaneColumn(), row);
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