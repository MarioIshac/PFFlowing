package me.theeninja.pfflowing.gui;

import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import me.theeninja.pfflowing.StringSerializable;
import me.theeninja.pfflowing.configuration.Configuration;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.flowingregions.CharacterFormatting;
import me.theeninja.pfflowing.flowingregions.CharacterStyle;
import me.theeninja.pfflowing.flowingregions.DefensiveReasoning;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;
import me.theeninja.pfflowing.flowing.*;
import org.apache.commons.collections4.ListUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FlowingGrid extends GridPane implements StringSerializable<FlowingGrid> {
    /**
     *
     */
    private Side side;

    private List<Speech> speeches
    /**
     * Generates the text area used for gathering input from the user. This input is eventually put into
     * a flowing region.
     * @return A text area with the appropriate properties designed for user input.
     */
    private TextArea generateInputTextArea() {
        TextArea textArea = new TextArea();
        textArea.prefWidthProperty().bind(this.widthProperty());
        textArea.setWrapText(true);
        textArea.setFont(Configuration.FONT);

        return textArea;
    }

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
    private EventHandler<KeyEvent> generateHandler(Speech speech, TextArea textArea, boolean createNewOne, Consumer<String> postEnterAction) {
        return (KeyEvent keyEvent) -> {
            if (TEXTAREA_SUBMIT.match(keyEvent)) {
                postEnterAction.accept(textArea.getText());

                getChildren().remove(textArea);

                if (createNewOne)
                    addFlowingRegionWriter(speech, true, postEnterAction);
            }
        };
    }

    /**
     * Adds a {@link TextArea} (the flowing region writer) to the flowing column. This flowing region writer
     * is designed so that on user submission, the text entered into the flowing region writer
     * would be used to create a flowing region representing what the user typed.
     */
    public void addFlowingRegionWriter(Speech speech, boolean createNewOne, Consumer<String> postEnterAction) {
        TextArea textArea = generateInputTextArea();

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, generateHandler(speech, textArea, createNewOne, postEnterAction));

        this.getChildren().add(textArea);
        textArea.requestFocus();

        FlowingGridController.getFXMLInstance().addCardSelectorSupport(textArea);
    }

    /**
     * Defaul post-enter specification for the above method
     * @param createNewOne
     */
    public void addFlowingRegionWriter(Speech speech, boolean createNewOne) {
        addFlowingRegionWriter(speech, createNewOne, text -> {
            DefensiveReasoning defensiveReasoning = new DefensiveReasoning(text);
            addDefensiveFlowingRegion(defensiveReasoning);
        });
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

    public void addOffensiveFlowingRegion(OffensiveFlowingRegion offensiveFlowingRegion) {
        addFlowingRegion(offensiveFlowingRegion);

        generatePauseTransition(Duration.seconds(0.25),
                event -> PFFlowingApplicationController.getFXMLInstance().getCorrelatingView()
                        .addLink(new FlowingLink(offensiveFlowingRegion.getTargetRegion(), offensiveFlowingRegion)))
                .play();

        PFFlowingApplicationController.getFXMLInstance().getCorrelatingView().organizeArrows();
    }

    public void addDefensiveFlowingRegion(DefensiveFlowingRegion defensiveRegion) {
        addFlowingRegion(defensiveRegion);
    }

    public void addExtensionFlowingRegion(ExtensionFlowingRegion extensionFlowingRegion) {
        addFlowingRegion(extensionFlowingRegion);
        generatePauseTransition(Duration.seconds(0.25),
                event -> PFFlowingApplicationController.getFXMLInstance().getCorrelatingView()
                        .addLink(new FlowingLink(extensionFlowingRegion.getBase(), extensionFlowingRegion)))
                .play();
    }

    public void addFlowingRegion(Speech speech, FlowingRegion flowingRegion) {
        FlowingGridController.getFXMLInstance().implementListeners(flowingRegion);

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

        // speech.getFlowingRegionList().size() will yield a position subsequent of the last flowing region
        // in the grid pane column
        this.add(flowingRegion, speech.getFlowingRegionList().size(), speech.getGridPaneColumn());

        speech.getFlowingRegionList().add(flowingRegion);
    }

    private final IntegerProperty startingColumn = new SimpleIntegerProperty();
    private final IntegerProperty finishingColumn = new SimpleIntegerProperty();
    private final List<Node> originalChildren = new ArrayList<>();

    public List<Node> getOriginalChildren() {
        return originalChildren;
    }

    FlowingGrid() {
        startingColumnProperty().addListener(this::onStartingColumnChanged);
        finishingColumnProperty().addListener(this::onFinishingColumnChanged);
    }

    private void onStartingColumnChanged(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
        onColumnBoundsChanged(newNumber.intValue(), finishingColumn.get());
    }

    private void onFinishingColumnChanged(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
        onColumnBoundsChanged(startingColumn.get(), newNumber.intValue());
    }

    private void onColumnBoundsChanged(int newStart, int newFinish) {
        getChildren().setAll(originalChildren.stream().filter(node -> {
            // Refers to whether the node is inside column bounds that don't wrap, such as start: 5 and finish: 7,
            // which refer to the columns of 5 6 7
            boolean insideNonWrap = newStart <= GridPane.getColumnIndex(node) && GridPane.getColumnIndex(node) <= newFinish

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
        setNumberOfColumns(getChildren().size() - reductionInNumOfColumns);
    }

    public void upscaleBy(int increaseInNumberOfColumns) {
        setNumberOfColumns(getChildren().size() + increaseInNumberOfColumns);
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

    public List<FlowingColumn> getFlowingColumns() {
        return getChildren().stream().map(FlowingColumn.class::cast).collect(Collectors.toList());
    }

    @Override
    public String serialize() {
        return getFlowingColumns().stream().map(FlowingColumn::serialize).collect(Collectors.joining("\n"));
    }

    @Override
    public FlowingGrid deserialize(String string) {
        return null;
    }


    public List<Node> getColumnChildren(int column) {
        return getChildren().stream().filter(node -> GridPane.getColumnIndex(node) == column).collect(Collectors.toList());
    }

    public List<Node> getRowChildren(int row) {
        return getChildren().stream().filter(node -> GridPane.getRowIndex(node) == row).collect(Collectors.toList());
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public int getFinishingColumn() {
        return finishingColumn.get();
    }

    public IntegerProperty finishingColumnProperty() {
        return finishingColumn;
    }

    public int wrap(int columnNumber) {
        while (columnNumber < 0)
            columnNumber += 8;
        while (columnNumber > 7)
            columnNumber -= 8;
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
}