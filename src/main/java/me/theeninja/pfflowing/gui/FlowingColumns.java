package me.theeninja.pfflowing.gui;

import javafx.scene.layout.HBox;
import me.theeninja.pfflowing.utils.Utils;
import me.theeninja.pfflowing.flowing.*;
import org.apache.commons.collections4.ListUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FlowingColumns extends HBox implements Bindable<SpeechListManager> {
    private SpeechListManager bindedSpeechListManager;
    private SpeechList currentSpeechList;

    private DisplayShifter selectedDisplayShifter;
    private DisplayShifter affDisplayShifter;
    private DisplayShifter negDisplayShifter;

    public void initializeDisplayShifters() {
        affDisplayShifter = new DisplayShifter(getBinded().getAffSpeechList());
        negDisplayShifter = new DisplayShifter(getBinded().getNegSpeechList());
    }

    public void display(SpeechList speechList) {
        setCurrentSpeechList(speechList);

        boolean firstTime = false;

        if (getChildren().size() == 0)
            firstTime = true;

        for (int i = 0; i < DefensiveSpeech.DEFENSIVE_SPEECH_ORDER.size(); i++)
            if (firstTime)
                getChildren().add(speechList.getSpeeches().get(i).getBinded());
            else
                getChildren().set(i, speechList.getSpeeches().get(i).getBinded());
    }

    public SpeechList getCurrentSpeechList() {
        return currentSpeechList;
    }

    public void setCurrentSpeechList(SpeechList speechList) {
        this.currentSpeechList = speechList;
    }

    public DisplayShifter getSelectedDisplayShifter() {
        return selectedDisplayShifter;
    }

    public void setSelectedDisplayShifter(DisplayShifter selectedDisplayShifter) {
        this.selectedDisplayShifter = selectedDisplayShifter;
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

    // O -> (A -> B, C), D, E
    // return

    public DisplayShifter getAffDisplayShifter() {
        return affDisplayShifter;
    }

    public DisplayShifter getNegDisplayShifter() {
        return negDisplayShifter;
    }

    public class DisplayShifter {
        private final SpeechList speechList;
        private final List<FlowingColumn> allFlowingColumns;

        public DisplayShifter(SpeechList speechList) {
            this.speechList = speechList;
            this.allFlowingColumns = speechList.getSpeeches().stream().map(Speech::getBinded).collect(Collectors.toList());
        }

        public void setNumberOfColumns(int numberOfColumns) {
            getChildren().setAll(allFlowingColumns.subList(0, numberOfColumns));
        }

        public void narrowBy(int reductionInNumOfColumns) {
            setNumberOfColumns(getChildren().size() - reductionInNumOfColumns);
        }

        public void upscaleBy(int increaseInNumberOfColumns) {
            setNumberOfColumns(getChildren().size() + increaseInNumberOfColumns);
        }

        public void shift(int offset) {
            // getChildren().size() represents the number of flowing columns
            if (getChildren().size() == DefensiveSpeech.DEFENSIVE_SPEECH_ORDER.size())
                return; //it is impossible to shift a display that has no off-screen components

            int numberOfFlowingColumns = getChildren().size();
            List<FlowingColumn> children = getChildren().stream().map(FlowingColumn.class::cast).collect(Collectors.toList());
            getChildren().clear();

            for (int index = 0; index < numberOfFlowingColumns; index++)
                getChildren().add(Utils.getRelativeElement(allFlowingColumns, children.get(index), offset));
        }

        public SpeechList getSpeechList() {
            return speechList;
        }
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