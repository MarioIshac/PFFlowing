package me.theeninja.pfflowing.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import me.theeninja.pfflowing.Action;
import me.theeninja.pfflowing.ActionManager;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.GraphicDecoration;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static javafx.scene.layout.GridPane.*;
import static me.theeninja.pfflowing.gui.KeyCodeCombinationUtils.*;


/**
 * The controller for the actual flowing area on the application. This controller
 * is responsible for managing the relations between all flowing regions that exist.
 *
 * @author TheeNinja
 */
public class FlowDisplayController implements Initializable, SingleViewController<FlowDisplay> {
    public FlowDisplayController(Side side) {
        this.side = side;
        setSpeechList(new SpeechList(side));
    }

    private ActionManager actionManager;

    public static FlowDisplayController newController(Side side) {
        FXMLLoader fxmlLoader = new FXMLLoader(FlowDisplayController.class.getResource("/flowing_display.fxml"));
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
    private static final String QUESTIONED_REGION_STYLECLASS = "marked";

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
        getActionManager().perform(new Delete(getSelectedFlowingRegions()));
    }

    public void attemptExpansion() {
        System.out.println("Attempting extension");

        for (FlowingRegion flowingRegion : getSelectedFlowingRegions())
            flowingRegion.setExpanded(!flowingRegion.getExpanded());
    }

    public void attemptSelectAll() {
        List<FlowingRegion> allFlowingRegions = Utils.getOfType(flowGrid.getChildren(), FlowingRegion.class);

        for (FlowingRegion flowingRegion : allFlowingRegions)
            select(flowingRegion, true);
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
        if (node instanceof DefensiveFlowingRegion) {
            DefensiveFlowingRegion flowingRegion = (DefensiveFlowingRegion) node;
            Speech speech = getSpeechList().getSpeech(flowingRegion);
            long defensiveRegionCount = flowGrid.getColumnChildren(speech.getColumn()).stream()
                    .filter(DefensiveFlowingRegion.class::isInstance)
                    .count();
            speech.setDefensiveRegionsNumber((int) defensiveRegionCount);
        }
    }

    private void onChildAdditionListenerUpdater(Node node) {
        if (node instanceof FlowingRegion) {
            FlowingRegion flowingRegion = (FlowingRegion) node;
            implementListeners(flowingRegion);
        }
    }

    ActionManager getActionManager() {
        return actionManager;
    }

    private void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    private final static int PREVIOUS_ROW_POSITION = 0;
    private final static int FINAL_ROW_POSITION = 1;

    private void onConfigurationBackgroundColorChange(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
        flowGrid.setBackground(Utils.generateBackgroundOfColor(newValue));
    }

    /**
     * Merging action. The process of merging involves collecting all the contents in the rows of all the flowing regions
     * into the row belonging to the top-most (determined by lowest row index) flowing region.
     * @author TheeNinja
     */
    private class Merge extends Action {

        private final List<FlowingRegion> addedRegions = new ArrayList<>();
        private final List<FlowingRegion> removedRegions;

        private final Map<FlowingRegion, List<Integer>> currentRegionModifications = new HashMap<>();

        private final int keptRow;

        Merge(List<FlowingRegion> flowingRegions) throws MergeException {
            // Represents the top most row of the flowing regions subject to merging
            keptRow = flowingRegions.stream().map(FlowGrid::getRowIndex).reduce(Integer::min).get();

            removedRegions = flowingRegions.stream()
                    .map(flowGrid::getWholeLink)
                    .sorted(Comparator.comparingInt(list -> getRowIndex(list.get(0))))
                    .distinct()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            Map<Integer, List<FlowingRegion>> columnRegionsMap = removedRegions.stream()
                    .collect(Collectors.groupingBy(FlowGrid::getColumnIndex));

            for (Map.Entry<Integer, List<FlowingRegion>> entry : columnRegionsMap.entrySet()) {
                int column = entry.getKey();
                List<FlowingRegion> flowingRegionList = entry.getValue();

                FlowingRegion toAdd = condense(flowingRegionList);

                FlowGrid.setConstraints(toAdd, column, getKeptRow());
                addedRegions.add(toAdd);
            }
        }

        private FlowingRegion condense(List<FlowingRegion> flowingRegions) throws MergeException {
            String condensedText = flowingRegions.stream()
                    .map(FlowingRegion::getFullText)
                    .collect(Collectors.joining("-"));

            if (flowingRegions.stream().allMatch(DefensiveFlowingRegion.class::isInstance))
                return new DefensiveFlowingRegion(condensedText);
            if (flowingRegions.stream().allMatch(OffensiveFlowingRegion.class::isInstance))
                return new OffensiveFlowingRegion(condensedText, Side.AFFIRMATIVE, flowingRegions.get(0));
            if (flowingRegions.stream().allMatch(ExtensionFlowingRegion.class::isInstance))
                return new ExtensionFlowingRegion(Side.AFFIRMATIVE, flowingRegions.get(0));

            throw new MergeException("Multiple types of flowing regions within single speech");
        }

        @Override
        public void execute() {
            flowGrid.getChildren().removeAll(removedRegions);
            flowGrid.getChildren().addAll(addedRegions);

            List<Integer> rows = removedRegions.stream()
                    .map(FlowGrid::getRowIndex)
                    .distinct()
                    .collect(Collectors.toList());

            // We are reinserting kept row anyway, so remove it from the rows to be removed.
            rows.remove(getKeptRow());

            getUpdateMap(rows).forEach((flowingRegion, integers) -> {
                FlowGrid.setRowIndex(flowingRegion, integers.get(FINAL_ROW_POSITION));
            });
        }

        @Override
        public void unexecute() {
            flowGrid.getChildren().removeAll(addedRegions);
            flowGrid.getChildren().addAll(removedRegions);

            currentRegionModifications.forEach((flowingRegion, integers) -> {
                FlowGrid.setRowIndex(flowingRegion, integers.get(PREVIOUS_ROW_POSITION));
            });
        }

        @Override
        public String getName() {
            return "Merge";
        }

        public int getKeptRow() {
            return keptRow;
        }
    }

    /**
     * Generates a map that represents what changes would occur to the flowing grid upon removing
     * a list of rows from {@code flowGrid}. However, we must take into account a row that may
     * be inserted in the case of actions such as merge, which collapses rows (listed as {@code removedRows}).
     *
     * Note that this method should be called before {@code removedRows} are removed.
     *
     * @param removedRows The rows that will be removed from the {@code flowGrid}.
     * @return a map consisting of each affected flowing region as a key and a list of two integers as each value,
     *         containing two elements. The first element represents the original row of the flowing region. The
     *         second element represents the new row of the flowing region, dependent on {@code removedRows}.
     */
    private Map<FlowingRegion, List<Integer>> getUpdateMap(List<Integer> removedRows) {
        Map<FlowingRegion, List<Integer>> map = new HashMap<>();

        for (Speech speech : getSpeechList().getSpeeches()) {
            List<Node> speechChildren = flowGrid.getColumnChildren(speech.getColumn());
            List<DefensiveFlowingRegion> defensiveFlowingRegions = Utils.getOfType(
                    speechChildren,
                    DefensiveFlowingRegion.class
            );

            // Only defensive flowing regions require iteration, as those are the ones affected by
            // the available row properties.
            for (DefensiveFlowingRegion defensiveFlowingRegion : defensiveFlowingRegions) {

                final int previousRow = getRowIndex(defensiveFlowingRegion);

                // This will be a removed row anyways, no need to tamper with
                if (removedRows.contains(previousRow))
                    continue;

                /*
                Imagine a tower of ham slices here. When you take a ham slice off from the bottom, all the other ham slices
                will fall down by one index within the ham tower. removedRowsUnder represents the number of rows
                that were collapsed under defensiveFlowingRegion
                 */
                int removedRowsUnder = (int) removedRows.stream().filter(removedRow -> previousRow > removedRow).count();

                int finalRow = previousRow - removedRowsUnder;

                map.put(defensiveFlowingRegion, List.of(previousRow, finalRow));
            }
        }
        return map;
    }

    private class Drop extends Action {
        private final List<FlowingRegion> droppedFlowingRegions;

        public Drop(List<FlowingRegion> flowingRegions) {
            droppedFlowingRegions = flowingRegions
                    .stream()
                    .map(flowGrid::getPostLink)
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList());

        }

        @Override
        public void execute() {
            for (FlowingRegion flowingRegion : getDroppedFlowingRegions())
                flowingRegion.getStyleClass().add("dropped");
        }

        @Override
        public void unexecute() {
            for (FlowingRegion flowingRegion : getDroppedFlowingRegions())
                flowingRegion.getStyleClass().remove("dropped");
        }

        @Override
        public String getName() {
            return "Drop";
        }

        public List<FlowingRegion> getDroppedFlowingRegions() {
            return droppedFlowingRegions;
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

            this.refFlowingRegion = new OffensiveFlowingRegion(text, baseSpeech.getSide(), getBaseFlowingRegion());
        }

        @Override
        public void execute() {
            addOffensiveFlowingRegion(refFlowingRegion);
        }

        @Override
        public void unexecute() {
            flowGrid.getChildren().remove(refFlowingRegion);
        }

        @Override
        public String getName() {
            return "Refute";
        }

        public FlowingRegion getBaseFlowingRegion() {
            return baseFlowingRegion;
        }
    }

    private static final Label QUESTION_MARK_LABEL = new Label("?");

    static {
        QUESTION_MARK_LABEL.setFont(Font.font(20));
    }

    private static final GraphicDecoration QUESTION_DECORATION = new GraphicDecoration(new Label("?"), Pos.CENTER_RIGHT);

    private class Question extends Action {
        private static final String MARKED_CLASS = "marked";

        private final FlowingRegion baseFlowingRegion;
        private final String questionMessage;

        Question(FlowingRegion baseFlowingRegion, String questionMessage) {
            this.baseFlowingRegion = baseFlowingRegion;
            this.questionMessage = questionMessage;
        }

        @Override
        public void execute() {
            getBaseFlowingRegion().getStyleClass().add(MARKED_CLASS);
            Decorator.addDecoration(getBaseFlowingRegion(), QUESTION_DECORATION);
        }

        @Override
        public void unexecute() {
            getBaseFlowingRegion().getStyleClass().remove(MARKED_CLASS);
            Decorator.removeDecoration(getBaseFlowingRegion(), QUESTION_DECORATION);
        }

        @Override
        public String getName() {
            return "Question";
        }

        public FlowingRegion getBaseFlowingRegion() {
            return baseFlowingRegion;
        }

        public String getQuestionMessage() {
            return questionMessage;
        }
    }

    private class Extend extends Action {
        private List<FlowingRegion> baseFlowingRegions;
        private Speech speech;
        private List<ExtensionFlowingRegion> extendFlowingRegions;
        private List<FlowingLink> flowingLinks;

        Extend(List<FlowingRegion> baseFlowingRegions) {
            this.baseFlowingRegions = baseFlowingRegions;
            this.speech = getSpeechList().getSpeech(baseFlowingRegions.get(0)); // speech guaranteed to be the same for all selected
            this.extendFlowingRegions = this.baseFlowingRegions.stream().map(FlowingRegion::duplicate).map(baseFlowingRegion ->
                    new ExtensionFlowingRegion(speech.getSide(), baseFlowingRegion)
            ).collect(Collectors.toList());
        }

        @Override
        public void execute() {
            this.extendFlowingRegions.forEach(FlowDisplayController.this::addExtensionFlowingRegion);
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.25));
            pauseTransition.setOnFinished(actionEvent -> {
                this.flowingLinks = this.extendFlowingRegions.stream().map(extensionFlowingRegion -> {
                    Speech firstSpeech = getSpeechList().getSpeech(extensionFlowingRegion.getBase());
                    Speech secondSpeech = getSpeechList().getSpeech(extensionFlowingRegion);
                    int row = getRowIndex(extensionFlowingRegion);

                    return new FlowingLink(firstSpeech.getColumn(), secondSpeech.getColumn(), row, FlowDisplayController.this);
                }).collect(Collectors.toList());

                flowGrid.getChildren().addAll(flowingLinks);
            });
            pauseTransition.play();
        }

        @Override
        public void unexecute() {
            flowGrid.getChildren().removeAll(this.extendFlowingRegions);
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
            deletedFlowingRegions = flowingRegions.stream()
                    .map(flowGrid::getPostLink)
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList());
        }


        @Override
        public void execute() {
            flowGrid.getChildren().removeAll(deletedFlowingRegions);

            List<Integer> rows = deletedFlowingRegions.stream()
                    .map(FlowGrid::getRowIndex)
                    .distinct()
                    .collect(Collectors.toList());

            // TODO: Investigate effects of removal on row indexes
            // However, post-removal, visibly, a flowing region previously on row 2 will be "seen" on row 1. Yet, its row is still 2 within memory.
            // At the time being, this is handled correctly naturally with implementation, and no measures need to be taken for this.

            getUpdateMap(rows).forEach((flowingRegion, integers) -> {
                FlowGrid.setRowIndex(flowingRegion, integers.get(FINAL_ROW_POSITION));
            });
        }

        @Override
        public void unexecute() {
            flowGrid.getChildren().addAll(deletedFlowingRegions);
        }

        @Override
        public String getName() {
            return "Delete";
        }
    }

    /**
     * Houses a list of all flowing regions that are currently selected. This serves
     * as input for many actions that occur throughout the flowing columns that require
     * target flowing regions, i.e, merging/refuting/speech constructor/etc. GDriveConnector listener
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

        getActionManager().perform(new Drop(getSelectedFlowingRegions()));

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
            Question questionAction = new Question(flowingRegion, questionText);

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

        System.out.println("Is fr null" + flowingRegion);

        if (flowGrid.getRefutation(flowingRegion).isPresent()) {
            notify("Selected flowing region already refuted.", Level.SEVERE);
            return;
        }

        Speech baseSpeech = getSpeech(getLastSelected());
        Speech offensiveSpeech = Utils.getRelativeElement(getSpeechList().getSpeeches(), baseSpeech, 1);

        TextArea textArea = getFlowingRegionWriter(
            offensiveSpeech,
            false,
            flowingTextArea -> getActionManager().perform(new Refute(flowingRegion, flowingTextArea.getText())),
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

        // No need to reextend already extended flowing regions
        List<FlowingRegion> flowingRegions = getSelectedFlowingRegions().stream().filter(
                flowingRegion -> !flowGrid.getExtension(flowingRegion).isPresent()
        ).collect(Collectors.toList());

        getActionManager().perform(new Extend(getSelectedFlowingRegions()));
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
                speechLabel.setTextFill(this.getColor(speech.getColumn()));
                speechLabelChildren.add(speechLabel);
                if (getSpeechList().getSelectedSpeech() == speech)
                    speechLabel.getStyleClass().add(SELECTED_SPEECH_STYLECLASS);
            }

        return speechLabelChildren;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setActionManager(new ActionManager());

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
            editedFlowingRegion.setText(nonTrimmedText.trim());
        }, getRowIndex(editedFlowingRegion));

        textArea.setText(editedFlowingRegion.getFullText());

        flowGrid.getChildren().add(textArea);
        textArea.requestFocus();
    }

    private static final Logger logger = Logger.getLogger(FlowDisplayController.class.getSimpleName());

    public void addCardSelectorMenu(FlowingTextArea textArea) {
        ContextMenu contextMenu = new ContextMenu();
    }

    public void addCardSelectorSupport(FlowingTextArea textArea) {
        textArea.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (!textArea.isCardSelectionMode() && keyEvent.getCode() == CARD_SELECTOR) {
                addCardSelectorMenu(textArea);
                textArea.setCardSelectionMode(true);
            }
        });
    }

    private boolean areSameSpeech(List<FlowingRegion> flowingRegions) {
        return flowingRegions.stream()
                .allMatch(flowingRegion -> getColumnIndex(flowingRegions.get(0)).equals(getColumnIndex(flowingRegion)));
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
            if (entry.getValue().stream().anyMatch(DefensiveFlowingRegion.class::isInstance)) {
                notify("Cannot merge new defensive content with old defensive content.", Level.SEVERE);
                return;
            }
        }

        try {
            Action merge = new Merge(getSelectedFlowingRegions());
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

            textArea.fontProperty().bind(EFlow.getInstance().getConfiguration().getFont().valueProperty());

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

            addCardSelectorSupport(textArea);
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
     * Adds a {@link FlowingTextArea} (the flowing region writer) to the flowing column. This flowing region writer
     * is designed so that on user submission, the text entered into the flowing region writer
     * would be used to create a flowing region representing what the user typed.
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
        // leads to the assumption that a user has attempted to add a flowing region when they haven't finished submission
        // of the previous Write action.
        if (flowGrid.getNode(speech.getColumn(), speech.getAvailableRow()).isPresent())
            return; // do not add allow the user to have two flowing writers / text areas at once

        TextArea textArea = getFlowingRegionWriter(speech, isCaseWriteMode(), flowingTextArea -> {
            DefensiveFlowingRegion defensiveFlowingRegion = new DefensiveFlowingRegion(flowingTextArea.getText());
            defensiveFlowingRegion.getAssociatedCards().addAll(flowingTextArea.getAddedCards());

            addDefensiveFlowingRegion(speech, defensiveFlowingRegion);

            /* Imagine a scenario like this, where R = defensive flowing region;

                 Column Index
               0 1 2 3 4 5 6 7
               R
               R
               R
                 R
                   R

               We would expect a user would add another flowing region to a column index greater or equal to 2. However,
               we must account for the fact that they may want to add another flowing region to column 0 (perhaps
               they forgot to flow something of the construction speech. In general, they may want to add something to
               a column that does not have the most recently added defensive flowing regions.

               To support this case, the following for-loop only runs if there are defensive flowing regions located
               in the speeches after the speech that the user is adding a defensive flowing region to. We increment
               the row indexes of all those defensive flowing regions by 1.
            */
            for (int column = speech.getColumn() + 1; column < Speech.SPEECH_SIZE; column++) {
                List<DefensiveFlowingRegion> affectedRegions =  Utils.getOfType(
                    flowGrid.getColumnChildren(column),
                    DefensiveFlowingRegion.class
                );

                for (DefensiveFlowingRegion affectedRegion : affectedRegions) {
                    int row = FlowGrid.getRowIndex(affectedRegion);
                    FlowGrid.setRowIndex(affectedRegion, row + 1);
                }
            }
        }, speech.getAvailableRow());

        flowGrid.getChildren().add(textArea);
        textArea.requestFocus();
    }

    public Speech getSpeech(FlowingRegion flowingRegion) {
        return getSpeechList().getSpeeches().get(getColumnIndex(flowingRegion));
    }

    public void addOffensiveFlowingRegion(OffensiveFlowingRegion offensiveFlowingRegion) {
        int rowIndex = getRowIndex(offensiveFlowingRegion.getTargetRegion());
        int refColumnIndex = getColumnIndex(offensiveFlowingRegion.getTargetRegion()) + 1;
        addFlowingRegion(offensiveFlowingRegion, refColumnIndex, rowIndex);
    }

    public void addDefensiveFlowingRegion(Speech speech, DefensiveFlowingRegion defensiveRegion) {
        int rowIndex = speech.getAvailableRow();
        addFlowingRegion(defensiveRegion, speech.getColumn(), rowIndex);
    }

    public void addExtensionFlowingRegion(ExtensionFlowingRegion extensionFlowingRegion) {
        int rowIndex = getRowIndex(extensionFlowingRegion.getBase());
        int extColumnIndex = getColumnIndex(extensionFlowingRegion.getBase()) + 2;
        addFlowingRegion(extensionFlowingRegion, extColumnIndex, rowIndex);
    }

    private static final String AFF_REGION_STYLECLASS = "affRegion";
    private static final String NEG_REGION_STYLECLASS = "negRegion";

    public void addFlowingRegion(FlowingRegion flowingRegion) {
        addFlowingRegion(flowingRegion, getColumnIndex(flowingRegion), getRowIndex(flowingRegion));
    }

    public void addFlowingRegion(FlowingRegion flowingRegion, int column, final int row) {
        Speech speech = getSpeechList().getSpeeches().get(column);

        flowGrid.add(flowingRegion, column, row);

        ColumnConstraints columnConstraints = flowGrid.getColumnConstraints().get(column);

        ObservableValue<Color> observedColor = getSide() == Side.AFFIRMATIVE ?
                EFlow.getInstance().getConfiguration().getAffColor().valueProperty() :
                EFlow.getInstance().getConfiguration().getNegColor().valueProperty();

        Utils.bindAndSet(observedColor, flowingRegion.textFillProperty());

        flowingRegion.minWidthProperty().bind(columnConstraints.prefWidthProperty());

        flowingRegion.setTextFill(getColor(column));
    }

    private Color getColor(int column) {
        return getSide() == Side.AFFIRMATIVE ?
                column % 2 == 0 ? Color.BLACK : Color.RED :
                column % 2 == 0 ? Color.RED : Color.BLACK;
    }

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
     * @return if column is in between start and end. Note that this considers wrapping around end.
     */
    private static boolean isInColumns(int start, int end, int column) {
        if (start == end)
            return column == end;

        // Refers to whether the node is inside column bounds that don't wrap, such as start: 5 and finish: 7,
        // which refer to the columns of 5 6 7
        boolean insideNonWrap = start <= column && column <= end;

        // 6 -> 0 7

        // Refers to whether the node is inside column bounds that do wrap, such as start: 7 and finish: 3,
        // which refer to the columns of 7 0 1 2 3 (wrapping around the last index, 7)
        boolean insideWrap = start <= column || column <= end;

        boolean dir = start < end;

        return dir ? insideNonWrap : insideWrap;
    }

    private ColumnConstraints firstColumnConstraint;

    private void onColumnBoundsChanged(int newStart, int newFinish) {
        System.out.println("column bounds onSelectedSpeechChange with " + newStart + " and " + newFinish);

        int numberOfColumns = getNumberOfColumns();
        System.out.println("number of columns");

        Speech selectedSpeech = getSpeechList().getSelectedSpeech();
        int baseColumnIndex = selectedSpeech == null ? 0 : selectedSpeech.getColumn();
        for (int relativeIndex = 0; relativeIndex < Speech.SPEECH_SIZE; relativeIndex++) {
            // Given that the column bounds could either wrap or not, both conditions should be false

            int column = wrap(baseColumnIndex + relativeIndex);

            ColumnConstraints columnConstraints = flowGrid.getColumnConstraints().get(column);
            boolean includeOnFlowGrid = isInColumns(newStart, newFinish, column);

            System.out.println("does " + column + " count " + includeOnFlowGrid);

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
        System.out.println("a");
        while (columnNumber < 0)
            columnNumber += Speech.SPEECH_SIZE;
        while (columnNumber >= Speech.SPEECH_SIZE)
            columnNumber -= Speech.SPEECH_SIZE;
        System.out.println("b");
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
        for (Node node : list)
            if (node instanceof FlowingRegion)
                return Optional.of((FlowingRegion) node);
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