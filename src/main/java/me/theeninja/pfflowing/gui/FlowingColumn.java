package me.theeninja.pfflowing.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.Configuration;
import me.theeninja.pfflowing.card.*;
import me.theeninja.pfflowing.flowing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FlowingColumn extends VBox implements Bindable<Speech> {
    private final Speech speech;
    private final Label label;
    private final VBox container;
    private Speech bindedSpeech;

    public FlowingColumn(Speech speech) {
        this.speech = speech;
        this.label = new Label(speech.getLabelText());
        this.container = new VBox();

        this.setBinded(speech);

        getChildren().add(getLabel());
        getChildren().add(getContainer());

        setPrefWidth(FlowingColumnsController.getFXMLInstance().getCorrelatingView().getPrefWidth() / 8);
        HBox.setHgrow(this, Priority.ALWAYS);
    }

    public static List<FlowingColumn> of(SpeechList speechList) {
        List<FlowingColumn> flowingColumns = new ArrayList<>();
        for (Speech speech : speechList.getSpeeches())
            flowingColumns.add(new FlowingColumn(speech));
        return flowingColumns;
    }

    public Label getLabel() {
        return label;
    }

    public Speech getSpeech() {
        return speech;
    }


    /**
     * Adds a {@link TextArea} (the flowing region writer) to the flowing column. This flowing region writer
     * is designed so that on the user hitting enter, the text entered into the flowing region writer
     * would be used to create a flowing region representing what the user typed.
     */
    public void addFlowingRegionWriter() {
        TextArea textArea = new TextArea();
        textArea.prefWidthProperty().bind(this.prefWidthProperty());
        textArea.setWrapText(true);
        textArea.setFont(Configuration.FONT);

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER && keyEvent.isControlDown()) {
                addDefensiveFlowingRegion(new DefensiveReasoning(textArea.getText()));
                getChildren().remove(textArea);
                addFlowingRegionWriter();
            }
        });

        this.getChildren().add(textArea);
        textArea.requestFocus();

        FlowingColumnsController.getFXMLInstance().addCardSelectorSupport(textArea);
        FlowingColumnsController.getFXMLInstance().setCurrentFlowingRegionWriter(textArea);
    }

    public <T extends FlowingRegion & Offensive> void addOffensiveFlowingRegion(T offensiveRegion) {
        /* if (offensiveRegion instanceof OffensiveCard) {
            offensiveCards.add((OffensiveCard) offensiveRegion);
        }
        else if (offensiveRegion instanceof OffensiveReasoning) {
            offensiveReasonings.add((OffensiveReasoning) offensiveRegion);
        } */
        addFlowingRegion(offensiveRegion);
    }

    public <T extends FlowingRegion & Defensive> void addDefensiveFlowingRegion(T defensiveRegion) {
        addFlowingRegion(defensiveRegion);
    }

    public <T extends FlowingRegion> void addFlowingRegion(T flowingRegion) {
        FlowingColumnsController.getFXMLInstance().implementListeners(flowingRegion);
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

        flowingRegion.setWrapText(true);
        getChildren().add(flowingRegion);
    }


    public void removeAllFlowingRegionWriters() {
        getChildren().removeIf(node -> node instanceof TextArea);
    }

    public VBox getContainer() {
        return container;
    }

    @Override
    public void setBinded(Speech speech) {
        this.bindedSpeech = speech;
        speech.setBinded(this);
    }

    @Override
    public Speech getBinded() {
        return bindedSpeech;
    }
}
