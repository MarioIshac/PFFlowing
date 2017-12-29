package me.theeninja.pfflowing.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.flowing.Speech;

public class SpeechListManager implements Bindable<FlowingColumns> {
    private final SpeechList AFF_SPEECHES;
    private final SpeechList NEG_SPEECHES;

    private FlowingColumns bindedFlowingColumns;

    private SpeechList getAffSpeechMap() {
        return AFF_SPEECHES;
    }

    private SpeechList getNegSpeechMap() {
        return NEG_SPEECHES;
    }

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

    private void setSelectedSpeechList(SpeechList selectedSpeechList) {
        this.selectedSpeechList.set(selectedSpeechList);
    }

    public void switchSelectedSpeechMap() {
        if (getSelectedSpeechList() == getAffSpeechMap())
            selectNegSpeechMap();
        else
            selectAffSpeechMap();
    }

    public void selectAffSpeechMap() {
        setSelectedSpeechList(getAffSpeechMap());
    }

    public void selectNegSpeechMap() {
        setSelectedSpeechList(getNegSpeechMap());
    }

    public SpeechList getSpeechList(Speech speech) {
        if (AFF_SPEECHES.getSpeeches().contains(speech))
            return AFF_SPEECHES;
        else
            return NEG_SPEECHES;
    }

    public SpeechList getSpeechList(Side side) {
        if (side == Side.AFFIRMATIVE)
            return getAffSpeechMap();
        else
            return getNegSpeechMap();
    }

    public Speech getVisibleSelectedSpeech() {
        return getSelectedSpeechList().getSelectedSpeech();
    }

    @Override
    public void setBinded(FlowingColumns flowingColumns) {
        this.bindedFlowingColumns = flowingColumns;
        System.out.println("called too");
        selectedSpeechList.addListener((observableValue, oldSpeechList, newSpeechList) -> {
            System.out.println("called");
            getBinded().display(newSpeechList);
        });
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
