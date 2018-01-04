package me.theeninja.pfflowing.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.flowing.Speech;

public class SpeechListManager implements Bindable<FlowingColumns> {
    private final SpeechList AFF_SPEECHES;
    private final SpeechList NEG_SPEECHES;

    private FlowingColumns bindedFlowingColumns;

    public SpeechListManager(FlowingColumns flowingColumns) {
        selectedSpeechList = new SimpleObjectProperty<>();

        AFF_SPEECHES = new SpeechList(Side.AFFIRMATIVE);
        NEG_SPEECHES = new SpeechList(Side.NEGATION);

        setBinded(flowingColumns);
    }

    private ObjectProperty<SpeechList> selectedSpeechList;

    public ObjectProperty<SpeechList> selectedSpeechListProperty() {
        return selectedSpeechList;
    }

    public SpeechList getSelectedSpeechList() {
        return selectedSpeechList.get();
    }

    public void setSelectedSpeechList(SpeechList selectedSpeechList) {
        this.selectedSpeechList.set(selectedSpeechList);
    }

    public void switchSelectedSpeechMap() {
        System.out.println("swithced");
        if (getSelectedSpeechList() == getAffSpeechList())
            setSelectedSpeechList(getNegSpeechList());
        else
            setSelectedSpeechList(getAffSpeechList());
    }

    public SpeechList getSpeechList(Speech speech) {
        if (getAffSpeechList().getSpeeches().contains(speech))
            return getAffSpeechList();
        else
            return getNegSpeechList();
    }

    public SpeechList getSpeechList(Side side) {
        if (side == Side.AFFIRMATIVE)
            return getAffSpeechList();
        else
            return getNegSpeechList();
    }

    public Speech getVisibleSelectedSpeech() {
        return getSelectedSpeechList().getSelectedSpeech();
    }

    @Override
    public void setBinded(FlowingColumns flowingColumns) {
        this.bindedFlowingColumns = flowingColumns;
        selectedSpeechListProperty().addListener(((observableValue, oldValue, newValue) ->
                getBinded().updateDisplay()));
    }

    @Override
    public FlowingColumns getBinded() {
        return bindedFlowingColumns;
    }

    public SpeechList getAffSpeechList() {
        return this.AFF_SPEECHES;
    }

    public SpeechList getNegSpeechList() {
        return this.NEG_SPEECHES;
    }
}
