package me.theeninja.pfflowing.gui;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FlowingColumn extends List<FlowingRegion> implements Bindable<Speech>, StringSerializable<FlowingColumn> {
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
        FlowingGrid flowingGrid = getParentFlowingColumns();
        SpeechListManager speechListManager = flowingGrid.getBinded();
        return speechListManager.getSpeechList(this.getBinded()).getOpposite(this.getBinded()).getBinded();
    }

    public boolean doesManageOpposite() {
        return managesOpposite;
    }

    public FlowingGrid getParentFlowingColumns() {
        return (FlowingGrid) getParent();
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
