package me.theeninja.pfflowing.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.HBox;
import me.theeninja.pfflowing.StringSerializable;
import me.theeninja.pfflowing.utils.Utils;
import me.theeninja.pfflowing.flowing.*;
import org.apache.commons.collections4.ListUtils;
import thredds.cataloggen.CollectionLevelScanner;

import java.util.*;
import java.util.stream.Collectors;

public class FlowingColumns extends HBox implements Bindable<SpeechListManager>, StringSerializable<FlowingColumns> {
    private SpeechListManager bindedSpeechListManager;

    public void updateDisplay() {
        System.out.println("Called with " + bindedSpeechListManager.getSelectedSpeechList().getSide());

        boolean firstTime = false;

        SpeechList speechList = bindedSpeechListManager.getSelectedSpeechList();

        if (getChildren().size() == 0)
            firstTime = true;

        for (int i = 0; i < DefensiveSpeech.DEFENSIVE_SPEECH_ORDER.size(); i++)
            if (firstTime)
                getChildren().add(speechList.getSpeeches().get(i).getBinded());
            else
                getChildren().set(i, speechList.getSpeeches().get(i).getBinded());
    }

    public void shift(int offset) {
        SpeechList speechList = bindedSpeechListManager.getSelectedSpeechList();

        // getChildren().size() represents the number of flowing columns
        if (getChildren().size() == DefensiveSpeech.DEFENSIVE_SPEECH_ORDER.size())
            return; //it is impossible to shift a updateDisplay that has no off-screen components

        int numberOfFlowingColumns = getChildren().size();
        List<FlowingColumn> children = getChildren().stream().map(FlowingColumn.class::cast).collect(Collectors.toList());
        getChildren().clear();

        for (int index = 0; index < numberOfFlowingColumns; index++)
            getChildren().add(Utils.getRelativeElement(
                    speechList.getSpeeches().stream().map(Bindable::getBinded).collect(Collectors.toList()),
                    children.get(index), offset));
    }

    public void setNumberOfColumns(int numberOfColumns) {
        SpeechList speechList = bindedSpeechListManager.getSelectedSpeechList();

        getChildren().setAll(speechList.getSpeeches().stream()
                .map(Bindable::getBinded).collect(Collectors.toList()).subList(0, numberOfColumns));
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
    public FlowingColumns deserialize(String string) {
        return null;
    }

    @Override
    public void setBinded(SpeechListManager speechListManager) {
        this.bindedSpeechListManager = speechListManager;
    }

    @Override
    public SpeechListManager getBinded() {
        return bindedSpeechListManager;
    }
}