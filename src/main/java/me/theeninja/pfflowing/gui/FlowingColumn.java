package me.theeninja.pfflowing.gui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import me.theeninja.pfflowing.configuration.Configuration;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.flowingregions.CharacterFormatting;
import me.theeninja.pfflowing.flowingregions.CharacterStyle;
import me.theeninja.pfflowing.flowingregions.DefensiveReasoning;
import me.theeninja.pfflowing.flowing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class FlowingColumn extends VBox implements Bindable<Speech> {
    private final Speech speech;
    private final Label label;
    private final ContentContainer contentContainer;
    private Speech bindedSpeech;

    private Color color;

    private final boolean managesOpposite;

    public FlowingColumn(Speech speech) {
        this.speech = speech;

        this.label = new Label(speech.getLabelText());
        getLabel().prefWidthProperty().bind(this.widthProperty());

        this.contentContainer = new ContentContainer();
        getContentContainer().prefWidthProperty().bind(this.widthProperty());

        Bindable.bind(this, getSpeech());

        getChildren().add(getLabel());
        getChildren().add(getContentContainer());

        HBox.setHgrow(this, Priority.ALWAYS);

        managesOpposite = getBinded() instanceof DefensiveSpeech;

        if (getBinded() == DefensiveSpeech.AFF_1) {
            // for example
            Label label = new Label("A");
            label.prefWidthProperty().bind(this.widthProperty());
            this.getContentContainer().getChildren().add(label);
            System.out.println("This shows label exists: " + label.toString());
            System.out.println("Width" + label.getWidth());
            System.out.println("Pref Width" + label.getPrefWidth());

            TextField textField = new TextField();
            this.getContentContainer().getChildren().add(textField);
            System.out.println(textField.getWidth());
        }
    }

    public static List<FlowingColumn> of(SpeechList speechList) {
        List<FlowingColumn> flowingColumns = new ArrayList<>();
        for (Speech speech : speechList.getSpeeches()) {
            FlowingColumn flowColumn = speech.getSide() == speechList.getSide() ?
                    new FlowingColumn(speech) :
                    new FlowingColumn(speech);
            flowingColumns.add(flowColumn);
        }
        return flowingColumns;
    }

    public Label getLabel() {
        return label;
    }

    public Speech getSpeech() {
        return speech;
    }

    private TextArea generateInputTextArea() {
        TextArea textArea = new TextArea();
        textArea.prefWidthProperty().bind(this.widthProperty());
        textArea.setWrapText(true);
        textArea.setFont(Configuration.FONT);

        return textArea;
    }

    private final KeyCodeCombination TEXTAREA_SUBMIT = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    private EventHandler<KeyEvent> generateHandler(TextArea textArea, boolean createNewOne, Consumer<String> postEnterAction) {
        return (KeyEvent keyEvent) -> {
            if (TEXTAREA_SUBMIT.match(keyEvent)) {
                postEnterAction.accept(textArea.getText());

                getChildren().remove(textArea);

                if (createNewOne)
                    addFlowingRegionWriter(true, postEnterAction);
            }
        };
    }

    /**
     * Adds a {@link TextArea} (the flowing region writer) to the flowing column. This flowing region writer
     * is designed so that on the user hitting enter, the text entered into the flowing region writer
     * would be used to create a flowing region representing what the user typed.
     */
    public void addFlowingRegionWriter(boolean createNewOne, Consumer<String> postEnterAction) {
        TextArea textArea = generateInputTextArea();

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, generateHandler(textArea, createNewOne, postEnterAction));

        this.getChildren().add(textArea);
        textArea.requestFocus();

        FlowingColumnsController.getFXMLInstance().addCardSelectorSupport(textArea);
    }

    /**
     * Defaul post-enter specification for the above method
     * @param createNewOne
     */
    public void addFlowingRegionWriter(boolean createNewOne) {
        addFlowingRegionWriter(createNewOne, text -> {
            DefensiveReasoning defensiveReasoning = new DefensiveReasoning(text);
            addDefensiveFlowingRegion(defensiveReasoning);
        });
    }

    public void addOffensiveFlowingRegion(OffensiveFlowingRegion offensiveRegion) {
        addFlowingRegion(offensiveRegion);
        drawArrow(offensiveRegion);
    }

    private void drawArrow(OffensiveFlowingRegion offensiveRegion) {
        FlowingRegion starter = offensiveRegion.getTargetRegion();
        Bounds starterBounds = starter.localToScene(starter.getLayoutBounds());
        double startX = starterBounds.getMaxX() + Configuration.ARROW_MARGIN;
        double startY = (starterBounds.getMinY() + starterBounds.getMaxY()) / 2;

        System.out.println(starterBounds);

        System.out.println("Text" + offensiveRegion.getText());
        System.out.println("Width" + offensiveRegion.getWidth());

        System.out.println(((Label) getContentContainer().getChildren().get(0)).getText());
        System.out.println(((Label) getContentContainer().getChildren().get(0)).getWidth());

        Bounds finishBounds = offensiveRegion.localToScene(offensiveRegion.getLayoutBounds());
        double finishX = finishBounds.getMinX() - Configuration.ARROW_MARGIN;
        double finishY = (finishBounds.getMinY() + finishBounds.getMaxY()) / 2;

        System.out.println(finishBounds);
        Line line = new Line(startX, startY, finishX, finishY);

        PFFlowingApplicationController.getFXMLInstance().getCorrelatingView().getChildren().add(line);
    }

    public void addDefensiveFlowingRegion(DefensiveFlowingRegion defensiveRegion) {
        addFlowingRegion(defensiveRegion);
    }

    /**
     * Calculates the overlap of the bounds of the two given nodes.
     *
     * @param newNode The {@link Node} that recently was added, i.e the bound challenger of the old node.
     * @param checkedNode The node that {@code newNode} is being checked against in regards to overlap.
     * @return the overlap as a double in the case of an existent overlap; otherwise 0.
     */
    private double calculateOverlap(Node newNode, Node checkedNode) {
        Bounds newNodeBounds = newNode.getBoundsInParent();
        Bounds checkedNodeBounds = checkedNode.getBoundsInParent();
        if (checkedNodeBounds.getMinY() < newNodeBounds.getMaxY())
            return newNodeBounds.getMaxY() - checkedNodeBounds.getMinY();
        else
            return 0;
    }

    public void addFlowingRegion(FlowingRegion flowingRegion) {
        FlowingColumnsController.getFXMLInstance().implementListeners(flowingRegion);

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
        flowingRegion.setTextFill(color);

        flowingRegion.prefWidthProperty().bind(flowingRegion.getContentContainer().widthProperty());

        System.out.println(flowingRegion.getWidth());

        getContentContainer().getChildren().add(flowingRegion);
    }

    public ContentContainer getContentContainer() {
        return contentContainer;
    }

    @Override
    public void setBinded(Speech speech) {
        this.bindedSpeech = speech;
        this.color = speech.getSide() == Side.AFFIRMATIVE ? Color.BLACK : Color.RED;
        this.label.setTextFill(this.color);
    }

    @Override
    public Speech getBinded() {
        return bindedSpeech;
    }

    private FlowingColumn getOpposingFlowingColumn() {
        FlowingColumns flowingColumns = getParentFlowingColumns();
        SpeechListManager speechListManager = flowingColumns.getBinded();
        return speechListManager.getSpeechList(this.getBinded()).getOpposite(this.getBinded()).getBinded();
    }

    public boolean doesManageOpposite() {
        return managesOpposite;
    }

    public FlowingColumns getParentFlowingColumns() {
        return (FlowingColumns) getParent();
    }
}
