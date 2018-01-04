package me.theeninja.pfflowing.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import javafx.util.Duration;
import me.theeninja.pfflowing.StringSerializable;
import me.theeninja.pfflowing.configuration.Configuration;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.flowingregions.CharacterFormatting;
import me.theeninja.pfflowing.flowingregions.CharacterStyle;
import me.theeninja.pfflowing.flowingregions.DefensiveReasoning;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FlowingColumn extends VBox implements Bindable<Speech>, StringSerializable<FlowingColumn> {
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

                if (createNewOne) {
                    System.out.println("duplicated");
                    addFlowingRegionWriter(true, postEnterAction);
                }
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

        getContentContainer().getChildren().add(flowingRegion);

        // Must be after previous line due to content container being parent of flowing region
        flowingRegion.prefWidthProperty().bind(flowingRegion.getContentContainer().widthProperty());
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

    @Override
    public String serialize() {
        return getLabel()
                + "\n\n" +
                getContentContainer().getContent().stream()
                        .map(StringSerializable::serialize).collect(Collectors.joining("\n"))
                + "\n\n";
    }

    @Override
    public FlowingColumn deserialize(String string) {
        return null;
    }
}
