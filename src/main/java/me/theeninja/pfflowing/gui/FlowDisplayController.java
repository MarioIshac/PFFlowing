package me.theeninja.pfflowing.gui;

import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import me.theeninja.pfflowing.ActionManager;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.actions.*;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionType;
import me.theeninja.pfflowing.flowing.FlowingTextArea;
import me.theeninja.pfflowing.flowing.Speech;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static javafx.scene.layout.GridPane.getColumnIndex;
import static javafx.scene.layout.GridPane.getRowIndex;


/**
 * The controller for the actual actions area on the application. This controller
 * is responsible for managing the relations between all actions regions that exist within its
 * managed flow display.
 *
 * @author TheeNinja
 */
public class FlowDisplayController implements Initializable, SingleViewController<FlowDisplay> {
    public static final int FLOWGRID_VERTICAL_GAP = 7;

    private final Map<Speech, Timeline> speechTimerMap = new HashMap<>();

    private BooleanProperty onDisplay;

    public FlowDisplayController(Side side) {
        this.actionManager = new ActionManager();

        this.side = side;
        setSpeechList(new SpeechList(side));
    }

    private final ActionManager actionManager;

    public static FlowDisplayController newController(Side side) {
        FXMLLoader fxmlLoader = new FXMLLoader(FlowDisplayController.class.getResource("/gui/flow/flowing_display.fxml"));
        FlowDisplayController flowDisplayController = new FlowDisplayController(side);
        fxmlLoader.setController(flowDisplayController);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flowDisplayController;
    }

    private static final String SELECTED_REGION_STYLECLASS = "selectedRegion";
    private static final String SELECTED_SPEECH_STYLECLASS = "selectedSpeech";

    @FXML
    public HBox speechLabels;

    @FXML
    public FlowDisplay flowDisplay;

    @FXML
    public FlowGrid flowGrid;

    private BooleanProperty edited = new SimpleBooleanProperty();
    private StringProperty fileName = new SimpleStringProperty();
    private final Side side;

    private boolean caseWriteMode = false;

    private static void onSelectedFlowingRegionsAddition(FlowingRegion node) {
        node.getStyleClass().add(SELECTED_REGION_STYLECLASS);
    }

    private static void onSelectedFlowingRegionsRemoval(FlowingRegion node) {
        node.getStyleClass().remove(SELECTED_REGION_STYLECLASS);
    }

    private void onFlowingRegionRemoved(Node node) {
        if (node instanceof FlowingRegion) {
            FlowingRegion flowingRegion = (FlowingRegion) node;
            flowingRegion.getReasoningLabel().textFillProperty().unbind();
            flowingRegion.prefWidthProperty().unbind();
        }
    }

    private void onChildAdditionSelectionUpdater(Node node) {
    }

    public boolean isCaseWriteMode() {
        return caseWriteMode;
    }

    public void setCaseWriteMode(boolean caseWriteMode) {
        this.caseWriteMode = caseWriteMode;
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public boolean isEdited() {
        return edited.get();
    }

    public BooleanProperty editedProperty() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited.set(edited);
    }

    public Side getSide() {
        return side;
    }

    public void addWriter() {
        addProactiveFlowingRegionWriter(getSpeechList().getSelectedSpeech());
    }

    public void attemptDelete() {
        Action deleteAction = new Delete(this, getSelectedFlowingRegions());

        getActionManager().perform(deleteAction);
    }

    public void attemptExpansion() {
        for (FlowingRegion flowingRegion : getSelectedFlowingRegions()) {
            flowingRegion.setExpanded(!flowingRegion.getExpanded());
        }
    }

    public void selectAll() {
        List<FlowingRegion> allFlowingRegions = Utils.getOfType(flowGrid.getChildren(), FlowingRegion.class);

        for (FlowingRegion flowingRegion : allFlowingRegions) {
            select(flowingRegion, true);
        }
    }

    private void onSelectedSpeechChange(ObservableValue<? extends Speech> observable, Speech oldValue, Speech newValue) {
        if (oldValue != null) {
            Node oldNode = speechLabels.getChildren().get(oldValue.getColumn());
            oldNode.getStyleClass().remove(SELECTED_SPEECH_STYLECLASS);
        }
        Node newNode = speechLabels.getChildren().get(newValue.getColumn());
        newNode.getStyleClass().add(SELECTED_SPEECH_STYLECLASS);
    }

    private void onChildRemovalSelectionUpdater(Node node) {
        if (node instanceof FlowingRegion) {
            getSelectedFlowingRegions().remove(node);
        }
    }

    private void onChildChangeListener(Node node) {
        if (!(node instanceof FlowingRegion)) {
            return;
        }

        FlowingRegion flowingRegion = (FlowingRegion) node;

        if (flowingRegion.getFlowingRegionType() != FlowingRegionType.PROACTIVE) {
            return;
        }

        Speech speech = getSpeechList().getSpeech(flowingRegion);

        long defensiveRegionCount = flowGrid.getColumnChildren(speech.getColumn()).stream()
                .filter(FlowingRegion.class::isInstance)
                .map(FlowingRegion.class::cast)
                .filter(FlowingRegion::isProactive)
                .count();

        speech.setDefensiveRegionsNumber((int) defensiveRegionCount);
    }

    private void onChildAdditionListenerUpdater(Node node) {
        if (node instanceof FlowingRegion) {
            FlowingRegion flowingRegion = (FlowingRegion) node;
            implementListeners(flowingRegion);
        }
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    private void onConfigurationBackgroundColorChange(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
        flowGrid.setBackground(Utils.generateBackgroundOfColor(newValue));
    }

    private void onFlowingRegionAdded(Node node) {
        if (node instanceof FlowingRegion) {
            final FlowingRegion flowingRegion = (FlowingRegion) node;

            Side sideToCheck = getSide();

            final int column = FlowGrid.getColumnIndex(flowingRegion);

            // Indicats that this is a refutation speech
            if (column % 2 == 1) {
                sideToCheck = sideToCheck.getOpposite();
            }

            ObservableValue<Color> observedColor = sideToCheck == Side.AFFIRMATIVE ?
                    EFlow.getInstance().getConfiguration().getAffColor().valueProperty() :
                    EFlow.getInstance().getConfiguration().getNegColor().valueProperty();

            flowingRegion.getReasoningLabel().textFillProperty().bind(observedColor);

            ColumnConstraints columnConstraints = flowGrid.getColumnConstraints().get(column);

            flowingRegion.prefWidthProperty().bind(columnConstraints.prefWidthProperty());
        }
    }

    public boolean isOnDisplay() {
        return onDisplay.get();
    }

    public BooleanProperty onDisplayProperty() {
        return onDisplay;
    }

    public void setOnDisplay(boolean onDisplay) {
        this.onDisplay.set(onDisplay);
    }

    public Map<Speech, Timeline> getSpeechTimerMap() {
        return speechTimerMap;
    }

    private static final Label QUESTION_MARK_LABEL = new Label("?");

    static {
        QUESTION_MARK_LABEL.setFont(Font.font(20));
    }

    /**
     * Houses a list of all actions regions that are currently selected. This serves
     * as input for many actions that occur throughout the actions columns that require
     * target actions regions, i.e, merging/refuting/speech constructor/etc. GoogleDriveConnector listener
     * is added to provide visual changes to the GUI when something is selected (it is
     * important to inform the user what they have selected currently in a visual
     * manner).
     */
    private ObservableList<FlowingRegion> selectedFlowingRegions =
            FXCollections.observableArrayList();

    private final Map<KeyCodeCombination, Runnable> keyCodeCombinationMap = new HashMap<>();

    public Map<KeyCodeCombination, Runnable> getKeyCodeCombinationMap() {
        return keyCodeCombinationMap;
    }

    private FlowingRegion getLastSelected() {
        return Utils.getLastElement(getSelectedFlowingRegions());
    }

    public void attemptDrop() {
        if (!isAnySelected())
            return;

        Action dropAction = new Drop(this, getSelectedFlowingRegions());

        getActionManager().perform(dropAction);

    }

    public boolean isAnySelected() {
        return getSelectedFlowingRegions().size() != 0;
    }

    public void attemptQuestion() {
        if (!isAnySelected())
            return;

        if (getSelectedFlowingRegions().size() > 1) {
            notify("Can only question one selection at a time.", Level.WARNING);
            return;
        }

        FlowingRegion flowingRegion = getLastSelected();

        TextField questionTextField = new TextField();
        questionTextField.prefWidthProperty().bind(flowingRegion.prefWidthProperty());

        PopOver popOver = new PopOver(questionTextField);
        popOver.show(flowingRegion);

        questionTextField.setOnAction(actionEvent -> {
            String questionText = questionTextField.getText();
            Action questionAction = new Question(this, flowingRegion, questionText);

            getActionManager().perform(questionAction);
            popOver.hide();
        });
    }

    public void attemptRefutation() {
        if (getSelectedFlowingRegions().size() > 1) {
            notify("Can only refute one selection at a time.", Level.WARNING);
            return;
        }

        FlowingRegion flowingRegion = getSelectedFlowingRegions().get(0);

        if (flowGrid.getRefutation(flowingRegion).isPresent()) {
            notify("Selected actions region already refuted.", Level.SEVERE);
            return;
        }

        Speech baseSpeech = getSpeech(getLastSelected());
        Speech offensiveSpeech = Utils.getRelativeElement(getSpeechList().getSpeeches(), baseSpeech, 1);

        TextArea textArea = getFlowingRegionWriter(
            offensiveSpeech,
            false,
            flowingTextArea -> getActionManager().perform(
                new Refute(
                    this,
                    flowingRegion,
                    flowingTextArea.getText()
                )
            ),
            getRowIndex(getLastSelected())
        );

        flowGrid.getChildren().add(textArea);
        textArea.requestFocus();
    }

    public void attemptExtension() {
        if (getSelectedFlowingRegions().stream().anyMatch(flowingRegion -> flowGrid.getRefutation(flowingRegion).isPresent())) {
            notify("Atleast one selection is refuted; unable to extend.", Level.WARNING);
            return;
        }

        if (!areSameSpeech(getSelectedFlowingRegions())) {
            notify("Selections are not in same speech; unable to extend.", Level.WARNING);
            return;
        }

        if (getSelectedFlowingRegions().stream().anyMatch(flowingRegion -> getColumnIndex(flowingRegion) >= Speech.SPEECH_SIZE - FlowGrid.EXT_COL_OFFSET)) {
            notify("Atleast one selection is in last two columns; unable to extend.", Level.SEVERE);
        }

        if (getSelectedFlowingRegions().stream().anyMatch(flowingRegion -> flowGrid.getExtension(flowingRegion).isPresent())) {
            notify("Atleast one selected already has been extended; unable to rextend", Level.SEVERE);
        }

        // No need to reextend already extended actions regions
        List<FlowingRegion> flowingRegions = getSelectedFlowingRegions().stream().filter(
            flowingRegion -> !flowGrid.getExtension(flowingRegion).isPresent()
        ).collect(Collectors.toList());

        Action extendAction = new Extend(this, getSelectedFlowingRegions());

        getActionManager().perform(extendAction);
    }

    private static int getColumnSpan(int startColumn, int endColumn) {
        if (endColumn > startColumn)
            return endColumn - startColumn + 1;
        else
            return Speech.SPEECH_SIZE + endColumn - startColumn + 1;
    }


    public List<Label> getSpeechLabelList(int start, int end) {
        int columnSpan = getColumnSpan(start, end);

        List<Label> speechLabelChildren = new ArrayList<>();

        for (Speech speech : getSpeechList().getSpeeches())
            if (isInColumns(start, end, speech.getColumn())) {
                SpeechLabel speechLabel = new SpeechLabel(speech);
                speechLabel.prefWidthProperty().bind(speechLabels.widthProperty().divide(columnSpan));
                speechLabelChildren.add(speechLabel);
                if (getSpeechList().getSelectedSpeech() == speech)
                    speechLabel.getStyleClass().add(SELECTED_SPEECH_STYLECLASS);
            }

        return speechLabelChildren;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        flowGrid.setVgap(FLOWGRID_VERTICAL_GAP);

        speechLabels.minWidthProperty().bind(flowDisplay.widthProperty());
        speechLabels.setBackground(Utils.generateBackgroundOfColor(Color.LIGHTBLUE));

        EFlow.getInstance()
                .getConfiguration()
                .getBackgroundColor()
                .valueProperty().addListener(this::onConfigurationBackgroundColorChange);

        flowGrid.minWidthProperty().bind(flowDisplay.minWidthProperty());
        flowGrid.maxWidthProperty().bind(flowDisplay.widthProperty());

        initializeListeners();

        flowGrid.getChildren().addListener(Utils.generateListChangeListener(
                this::onChildAdditionSelectionUpdater, // nothing should be done on when list is added too
                this::onChildRemovalSelectionUpdater
        ));

        flowGrid.getChildren().addListener(Utils.generateListChangeListener(
                this::onChildChangeListener,
                this::onChildChangeListener
        ));

        flowGrid.getChildren().addListener(Utils.generateListChangeListener(
                this::onChildAdditionListenerUpdater,
                node -> {
                    if (node instanceof FlowingRegion) {
                        FlowingRegion flowingRegion = (FlowingRegion) node;
                    }
                }
        ));

        flowGrid.getChildren().addListener(Utils.generateListChangeListener(
                this::onFlowingRegionAdded,
                this::onFlowingRegionRemoved
        ));

        flowGrid.getColumnConstraints().setAll(Collections.nCopies(Speech.SPEECH_SIZE, new ColumnConstraints()));

        // Force listener to activate, creating the initial percentage widths
        updateColumnBounds();

        getSpeechList().selectedSpeechProperty().addListener(this::onSelectedSpeechChange);

        getSpeechList().setSelectedSpeech(getSpeechList().get(0).getFirst());
    }

    public ObservableList<FlowingRegion> getSelectedFlowingRegions() {
        return selectedFlowingRegions;
    }

    public void attemptEdit() {
        FlowingRegion editedFlowingRegion = getLastSelected();

        TextArea textArea = getFlowingRegionWriter(getSpeechList().getSpeech(editedFlowingRegion), false, flowingTextArea -> {
            String nonTrimmedText = flowingTextArea.getText();
            String newText = nonTrimmedText.trim();

            Action<?> editAction = new Edit(editedFlowingRegion, newText);
            getActionManager().perform(editAction);
        }, getRowIndex(editedFlowingRegion));

        textArea.setText(editedFlowingRegion.getFullText());
        textArea.positionCaret(textArea.getLength());

        flowGrid.getChildren().add(textArea);
        textArea.requestFocus();
    }

    private static final Logger logger = Logger.getLogger(FlowDisplayController.class.getSimpleName());

    private boolean areSameSpeech(List<FlowingRegion> flowingRegions) {
        return flowingRegions.stream()
                .allMatch(flowingRegion -> getColumnIndex(flowingRegions.get(0)).equals(getColumnIndex(flowingRegion)));
    }

    public void attemptSplit() {
        FlowingRegion selectedFlowingRegion = getLastSelected();

        TextArea textArea = getFlowingRegionWriter(getSpeechList().getSpeech(selectedFlowingRegion), false, flowingTextArea -> {

            int caretPosition = flowingTextArea.getCaretPosition();

            try {
                Action<?> splitAction = new Split(this, selectedFlowingRegion, caretPosition);
                getActionManager().perform(splitAction);
            } catch (SplitException splitException) {
                notify(splitException.getMessage(), Level.SEVERE);
            }
        }, getRowIndex(selectedFlowingRegion));

        textArea.setEditable(false);
        textArea.setMouseTransparent(true);

        textArea.setText(selectedFlowingRegion.getFullText());

        // put it in the middle in order to make a split convenient regardless of final caret position
        textArea.positionCaret(textArea.getLength() / 2);

        flowGrid.getChildren().add(textArea);
        textArea.requestFocus();
    }

    public void attemptMerge() {
        if (!isAnySelected())
            return;

        Map<Integer, List<FlowingRegion>> groupedBySpeech = getSelectedFlowingRegions()
                .stream()
                .collect(Collectors.groupingBy(FlowGrid::getColumnIndex));

        int minColumn = groupedBySpeech.keySet().stream().reduce(Integer::min).orElse(Integer.MAX_VALUE);

        for (Map.Entry<Integer, List<FlowingRegion>> entry : groupedBySpeech.entrySet()) {
            if (entry.getKey() == minColumn)
                continue;
            if (entry.getValue().stream().anyMatch(FlowingRegion::isProactive)) {
                notify("Cannot merge new defensive content with old defensive content.", Level.SEVERE);
                return;
            }
        }

        try {
            Action<?> merge = new Merge(this, getSelectedFlowingRegions());
            getActionManager().perform(merge);
        } catch (MergeException e) {
            e.printStackTrace();
            notify(e.getMessage(), Level.SEVERE);
        }

    }

    public void implementListeners(FlowingRegion flowingRegion) {
        // implement selection listeners regarding mouse presses
        flowingRegion.setOnMousePressed(mouseEvent -> {
            handleSelection(flowingRegion, mouseEvent.isControlDown());
        });
    }

    public void select(FlowingRegion flowingRegion, boolean multiSelect) {
        if (!multiSelect)
            getSelectedFlowingRegions().clear();
        getSelectedFlowingRegions().add(flowingRegion);
    }

    public void unselect(FlowingRegion flowingRegion, boolean multiUnselect) {
        getSelectedFlowingRegions().clear();
        getSelectedFlowingRegions().remove(flowingRegion);
    }

    private void handleSelection(FlowingRegion flowingRegion, boolean multiSelect) {
        if (getSelectedFlowingRegions().contains(flowingRegion))
            unselect(flowingRegion, multiSelect);
        else
            select(flowingRegion, multiSelect);
    }

    /**
     * Provides the one, main view that is represented by this controller.
     *
     * @return The one, main view correlating to the controller.
     */
    @Override
    public FlowDisplay getCorrelatingView() {
        return flowDisplay;
    }

    public void handleSelection(Function<FlowingRegion, Optional<FlowingRegion>> function, Supplier<Optional<FlowingRegion>> defaultFunc, boolean multiSelect) {
        FlowingRegion flowingRegion = getLastSelected();

        Optional<FlowingRegion> optionalFlowingRegion =
                getLastSelected() == null ?
                        defaultFunc.get() :
                        function.apply(flowingRegion);

        optionalFlowingRegion.ifPresent(obtFlowingRegion -> handleSelection(obtFlowingRegion, multiSelect));
    }

    private void initializeListeners() {
        getSelectedFlowingRegions().addListener(Utils.generateListChangeListener(
                FlowDisplayController::onSelectedFlowingRegionsAddition,
                FlowDisplayController::onSelectedFlowingRegionsRemoval
        ));
    }

    private class FlowingTextAreaGenerator {
        private final FlowingTextArea textArea;

        private boolean isCardSelectorShown = false;

        FlowingTextAreaGenerator() {
            textArea = new FlowingTextArea();
            textArea.setWrapText(true);

            textArea.fontProperty().bind(EFlow.getInstance().getConfiguration().getReasoningFont().valueProperty());

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

            });

            // newValue will only change to false upon loss of focus obtained from FlowingTextArea#requestFocus();
            // even though textArea will not be focused on its creation, this does not trigger the listener
            textArea.focusedProperty().addListener(this::onFocusChanged);
        }

        public FlowingTextArea getFlowingTextArea() {
            return textArea;
        }

        public boolean isCardSelectorShown() {
            return isCardSelectorShown;
        }

        public void setCardSelectorShown(boolean cardSelectorShown) {
            isCardSelectorShown = cardSelectorShown;
        }

        private void onFocusChanged(ObservableValue<? extends Boolean> observable, boolean oldValue, boolean newValue) {
            if (!newValue)
                flowGrid.getChildren().remove(textArea);
        }
    }

    /**
     *
     */
    private SpeechList speechList;

    /**
     * Represents the keyboard combination required to submit a text area (and thus generate a actions region).
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
    private EventHandler<KeyEvent> generateHandler(Speech speech, FlowingTextArea textArea, boolean createNewOne, Consumer<FlowingTextArea> postEnterAction, int rowIndex) {
        return (KeyEvent keyEvent) -> {
            if (TEXTAREA_SUBMIT.match(keyEvent)) {
                postEnterAction.accept(textArea);

                flowGrid.getChildren().remove(textArea);

                if (createNewOne){
                    TextArea newTextArea = getFlowingRegionWriter(
                            speech, isCaseWriteMode(), postEnterAction, rowIndex + 1);
                    flowGrid.getChildren().add(newTextArea);
                }

                keyEvent.consume();

                flowGrid.requestFocus();
            }
        };
    }

    /**
     * Adds a {@link FlowingTextArea} (the actions region writer) to the actions column. This actions region writer
     * is designed so that on user submission, the text entered into the actions region writer
     * would be used to create a actions region representing what the user typed.
     */
    public TextArea getFlowingRegionWriter(Speech speech, boolean createNewOne, Consumer<FlowingTextArea> postEnterAction, int rowIndex) {
        FlowingTextArea textArea = new FlowingTextAreaGenerator().getFlowingTextArea();

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, generateHandler(speech, textArea, createNewOne, postEnterAction, rowIndex));

        FlowGrid.setConstraints(textArea, speech.getColumn(), rowIndex);
        textArea.requestFocus();

        ColumnConstraints columnConstraints = flowGrid.getColumnConstraints().get(speech.getColumn());
        textArea.prefWidthProperty().bind(columnConstraints.prefWidthProperty());

        return textArea;
    }

    /**
     * Default arguments for {@code getFlowingRegionWriter}, given a speech.
     */
    public void addProactiveFlowingRegionWriter(Speech speech) {
        // A node will only be present at this position if the previously added text area hasn't been submitted. This
        // leads to the assumption that a user has attempted to add a actions region when they haven't finished submission
        // of the previous ProactiveWrite action.
        if (flowGrid.getNode(speech.getColumn(), speech.getAvailableRow()).isPresent())
            return; // do not add allow the user to have two actions writers / text areas at once

        TextArea textArea = getFlowingRegionWriter(speech, isCaseWriteMode(), flowingTextArea -> {
            FlowingRegion defensiveFlowingRegion = new FlowingRegion(flowingTextArea.getText(), FlowingRegionType.PROACTIVE);
            defensiveFlowingRegion.getAssociatedCards().addAll(flowingTextArea.getAddedCards());

            FlowGrid.setColumnIndex(defensiveFlowingRegion, speech.getColumn());
            FlowGrid.setRowIndex(defensiveFlowingRegion, speech.getAvailableRow());

            Action<?> proactiveWrite = new ProactiveWrite(this, speech, defensiveFlowingRegion);

            getActionManager().perform(proactiveWrite);
        }, speech.getAvailableRow());

        flowGrid.getChildren().add(textArea);
        textArea.requestFocus();
    }

    public Speech getSpeech(FlowingRegion flowingRegion) {
        return getSpeechList().getSpeeches().get(getColumnIndex(flowingRegion));
    }

    private static final String AFF_REGION_STYLECLASS = "affRegion";
    private static final String NEG_REGION_STYLECLASS = "negRegion";


    private final IntegerProperty startingColumn = new SimpleIntegerProperty(0);
    private final IntegerProperty finishingColumn = new SimpleIntegerProperty(7);

    /**
     * Examples:
     * 7 0 -> 7 0
     * 7 6 -> 7 1 2 3 4 5 6
     * 6 7 -> 6 7
     * 1 1 -> 1 2 3 4 5 6 7 0
     * 2 0 -> 2 3 4 5 6 7 0
     *
     * @param start The starting column index.
     * @param end The ending column index.
     * @param column The checked column index.
     * @return if column is in between listen and end. Note that this considers wrapping around end.
     */
    private static boolean isInColumns(int start, int end, int column) {
        if (start == end)
            return column == end;

        // Refers to whether the node is inside column bounds that don't wrap, such as listen: 5 and finish: 7,
        // which refer to the columns of 5 6 7
        boolean insideNonWrap = start <= column && column <= end;

        // 6 -> 0 7

        // Refers to whether the node is inside column bounds that do wrap, such as listen: 7 and finish: 3,
        // which refer to the columns of 7 0 1 2 3 (wrapping around the last index, 7)
        boolean insideWrap = start <= column || column <= end;

        boolean dir = start < end;

        return dir ? insideNonWrap : insideWrap;
    }

    private ColumnConstraints firstColumnConstraint;

    private void onColumnBoundsChanged(int newStart, int newFinish) {
        int numberOfColumns = getNumberOfColumns();

        Speech selectedSpeech = getSpeechList().getSelectedSpeech();
        int baseColumnIndex = selectedSpeech == null ? 0 : selectedSpeech.getColumn();
        for (int relativeIndex = 0; relativeIndex < Speech.SPEECH_SIZE; relativeIndex++) {
            // Given that the column bounds could either wrap or not, both conditions should be false

            int column = wrap(baseColumnIndex + relativeIndex);

            ColumnConstraints columnConstraints = flowGrid.getColumnConstraints().get(column);
            boolean includeOnFlowGrid = isInColumns(newStart, newFinish, column);

            if (includeOnFlowGrid)
                columnConstraints.prefWidthProperty().bind(flowGrid.widthProperty().divide(numberOfColumns));
            else
                columnConstraints.prefWidthProperty().unbind();
        }

        speechLabels.getChildren().setAll(getSpeechLabelList(newStart, newFinish));
    }

    private void updateColumnBounds() {
        onColumnBoundsChanged(getStartingColumn(), getFinishingColumn());
    }

    public void shift(int offset) {
        setStartingColumn(getStartingColumn() + offset);
        setFinishingColumn(getFinishingColumn() + offset);
        updateColumnBounds();
    }

    public void shiftLeft() {
        shift(-1);
    }

    public void shiftRight() {
        shift(1);
    }

    public int getNumberOfColumns() {
        return getColumnSpan(getStartingColumn(), getFinishingColumn());
    }

    public void setNumberOfColumns(int numberOfColumns) {
        setFinishingColumn(getFinishingColumn() - (getNumberOfColumns() - numberOfColumns));
        updateColumnBounds();
    }

    public void narrowBy(int reductionInNumOfColumns) {
        setNumberOfColumns(getNumberOfColumns() - reductionInNumOfColumns);
    }

    public void narrowOnce() {
        narrowBy(1);
    }

    public void upscaleOnce() {
        upscaleBy(1);
    }

    public void upscaleBy(int increaseInNumberOfColumns) {
        setNumberOfColumns(getNumberOfColumns() + increaseInNumberOfColumns);
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

    public Optional<FlowingRegion> fromTop() {
        int column = getSpeechList().getSelectedSpeech().getColumn();
        List<Node> list = flowGrid.getColumnChildren(column);
        list.sort(Comparator.comparing(FlowGrid::getColumnIndex));
        for (Node node : list)
            if (node instanceof FlowingRegion)
                return Optional.of((FlowingRegion) node);
        return Optional.empty();
    }

    public Optional<FlowingRegion> fromBottom() {
        int column = getSpeechList().getSelectedSpeech().getColumn();
        List<Node> list = flowGrid.getColumnChildren(column);
        list.sort(Comparator.comparing(FlowGrid::getColumnIndex));
        Collections.reverse(list);

        for (Node node : list) {
            if (node instanceof FlowingRegion) {
                return Optional.of((FlowingRegion) node);
            }
        }

        return Optional.empty();
    }

    public Optional<FlowingRegion> fromLeft() {
        return flowGrid.getFlowingRegion(0, 0);
    }

    public Optional<FlowingRegion> fromRight() {
        return flowGrid.getFlowingRegion(Speech.SPEECH_SIZE - 1, 0);
    }

    public void selectRightSpeech() {
        selectSpeech(1);
    }

    public void selectLeftSpeech() {
        selectSpeech(-1);
    }

    public void selectSpeech(int offset) {
        if (offset == 0)
            return;

        /// /1 4, 2 1
        int currentColumn = getSpeechList().getSelectedSpeech().getColumn();
        int numberOfColumns = getNumberOfColumns();
        int newColumn = currentColumn;

        if (offset > 0) {

            // Wrap offset if it greater than the number of currently displayed speeches
            offset %= numberOfColumns;

            // The new selected speech is
            newColumn += offset;

            if (newColumn > getFinishingColumn())
                newColumn += getStartingColumn() - getFinishingColumn() - 1;

            Speech newSelectedSpeech = getSpeechList().getSpeeches().get(newColumn);
            getSpeechList().setSelectedSpeech(newSelectedSpeech);
        }
    }

    private final static Map<Level, Color> LEVEL_BACKGROUND_MAP = Map.of(
            Level.INFO, Color.WHITE,
            Level.WARNING, Color.YELLOW,
            Level.SEVERE, Color.LIGHTPINK
    );

    private void notify(String notificationText, Level notificationLevel) {
        Background background = Utils.generateBackgroundOfColor(LEVEL_BACKGROUND_MAP.get(notificationLevel));

        Label label = new Label(notificationText);
        label.setBackground(background);

        PopOver popOver = new PopOver(label);

        popOver.maxWidthProperty().bind(label.prefWidthProperty());

        //label.maxHeightProperty().bind(popOver.heightProperty());

        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show(flowGrid, flowGrid.getWidth() / 2);

        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(2));
        pauseTransition.setOnFinished(actionEvent -> {
            popOver.hide(Duration.seconds(1));
        });
        pauseTransition.play();
    }
}